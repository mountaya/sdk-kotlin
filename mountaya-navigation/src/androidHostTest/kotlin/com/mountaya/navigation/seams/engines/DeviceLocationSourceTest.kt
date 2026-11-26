package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The device's source over a platform the test answers, so its rules run on the host JVM. */
class DeviceLocationSourceTest {
    private val whenInUse = LocationStanding(LocationAuthorization.WhenInUse, LocationAccuracy.Full)
    private val approximate = LocationStanding(LocationAuthorization.WhenInUse, LocationAccuracy.Reduced)
    private val notAsked = LocationStanding(LocationAuthorization.NotDetermined, LocationAccuracy.Full)

    /**
     * A platform whose standing, fixes, failures, resumes and manifest the test sets, which records what the source
     * asks of it and every failure it dropped.
     */
    private class Platform(
        var standing: LocationStanding,
        var starts: Boolean = true,
        var declaresBackground: Boolean = false,
    ) : DeviceLocationSource.Platform {
        var receive: ((List<Double>) -> Unit)? = null
        var fail: ((LocationException) -> Unit)? = null
        var resumed: (() -> Unit)? = null
        var requested = 0
        var removed = 0
        var reads = 0

        /** The rate the reader was last asked to read at. */
        var rate: LocationRate? = null

        /** What the reader reports as it is asked to start, before it answers whether it did. */
        var failingAtStart: LocationException? = null

        /** How often the source read the app's manifest for its background declaration. */
        var manifestReads = 0

        /** Every failure the source dropped, for want of an `onError`. */
        val dropped = mutableListOf<Throwable>()

        override fun standing(): LocationStanding {
            reads += 1
            return standing
        }

        override fun request(
            rate: LocationRate,
            receive: (position: List<Double>) -> Unit,
            fail: (failure: LocationException) -> Unit,
        ): Boolean {
            requested += 1
            this.rate = rate
            this.receive = receive
            this.fail = fail
            failingAtStart?.let(fail)
            return starts
        }

        override fun remove() {
            removed += 1
            receive = null
        }

        override fun watch(resumed: () -> Unit) {
            this.resumed = resumed
        }

        override fun unwatch() {
            resumed = null
        }

        override fun declaresBackgroundLocation(): Boolean {
            manifestReads += 1
            return declaresBackground
        }

        override fun dropped(failure: Throwable) {
            dropped += failure
        }
    }

    @Test
    fun theDeviceIsReadAtANavigationsRateUnlessTheHostNamesAnother() {
        val navigating = Platform(whenInUse)
        val navigation = DeviceLocationSource(navigating)
        navigation.start {}
        assertEquals(LocationRate.Navigation, navigation.rate)
        assertEquals(LocationRate.Navigation, navigating.rate)
        navigation.stop()

        val walking = Platform(whenInUse)
        val walker = DeviceLocationSource(walking, LocationRate.Walking)
        walker.start {}
        assertEquals(LocationRate.Walking, walker.rate)
        assertEquals(LocationRate.Walking, walking.rate)
        walker.stop()
    }

    @Test
    fun eachLocationThePlatformReportsIsStatedAsLongitudeThenLatitude() {
        val platform = Platform(whenInUse)
        val source = DeviceLocationSource(platform)
        val stated = mutableListOf<List<Double>>()
        source.start { stated += it }

        platform.receive?.invoke(listOf(6.8652, 45.9237))
        platform.receive?.invoke(listOf(6.9271, 45.9682))
        source.stop()

        assertEquals(listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682)), stated)
    }

    @Test
    fun nothingIsStatedBeforeAStartOrAfterAStopAndAStartedSourceStartsOnce() {
        val platform = Platform(whenInUse)
        val source = DeviceLocationSource(platform)
        val stated = mutableListOf<List<Double>>()
        source.receive(listOf(6.0, 45.0))

        source.start { stated += it }
        source.start { stated += listOf(0.0) }
        source.stop()
        source.stop()
        source.receive(listOf(6.0, 45.0))

        assertTrue(stated.isEmpty())
        assertEquals(1, platform.requested)
        assertEquals(1, platform.removed)
    }

    /** The standing is null before any read, read as the source starts, and held as each recheck answers it. */
    @Test
    fun theStandingIsReadAsTheSourceStartsAndAsEachRecheckAnswersIt() {
        val platform = Platform(notAsked)
        val source = DeviceLocationSource(platform)
        assertNull(source.standing.value)

        source.start {}
        assertEquals(notAsked, source.standing.value)
        assertEquals(0, platform.requested, "a permission never asked for reads nothing")

        platform.standing = whenInUse
        assertEquals(whenInUse, source.recheckPermission())
        assertEquals(whenInUse, source.standing.value)
        assertEquals(1, platform.requested)
        source.stop()
    }

    /**
     * The source never asks for the permission: without it, or with an approximate location alone, it reads
     * nothing, and the app, told of its own request's result, has it read again.
     */
    @Test
    fun itReadsPositionsOnlyAtFullAccuracyWhileTheAppHoldsThePermission() {
        val platform = Platform(approximate)
        val source = DeviceLocationSource(platform)
        source.start {}
        assertEquals(0, platform.requested, "an approximate location places a traveller kilometres away")

        platform.standing = LocationStanding(LocationAuthorization.Always, LocationAccuracy.Full)
        source.recheckPermission()
        source.recheckPermission()
        assertEquals(1, platform.requested)

        platform.standing = notAsked
        source.recheckPermission()
        assertEquals(1, platform.removed)
        source.stop()
        assertEquals(1, platform.removed)
    }

    /**
     * A permission granted in the system's settings becomes known as the app comes back to the foreground:
     * the source reads it again at each resume while it is started, and at none once stopped.
     */
    @Test
    fun theStandingIsReadAgainAtEachResumeWhileTheSourceIsStarted() {
        val platform = Platform(notAsked)
        val source = DeviceLocationSource(platform)
        source.start {}
        val reads = platform.reads

        platform.standing = whenInUse
        platform.resumed?.invoke()
        assertEquals(whenInUse, source.standing.value)
        assertEquals(reads + 1, platform.reads)
        assertEquals(1, platform.requested)

        source.stop()
        assertNull(platform.resumed, "a stopped source watches no resume")
    }

    @Test
    fun eachFailureOfTheReaderReachesOnErrorWhileTheSourceIsStarted() {
        val platform = Platform(whenInUse)
        val failures = mutableListOf<Throwable>()
        val source = DeviceLocationSource(platform) { failures += it }
        source.start {}

        platform.fail?.invoke(LocationException(LocationException.Kind.ProviderDisabled))
        assertEquals(LocationException.Kind.ProviderDisabled, assertIs<LocationException>(failures.single()).kind)

        val told = platform.fail
        source.stop()
        told?.invoke(LocationException(LocationException.Kind.ProviderDisabled))
        assertEquals(1, failures.size, "nothing is told once the source is stopped")
    }

    /**
     * A reader that does not start, a device with no satellite positioning or a permission revoked as it
     * starts, is told, and asked again at the next recheck rather than left for good.
     */
    @Test
    fun aReaderThatDoesNotStartIsToldAndAskedAgainAtTheNextRecheck() {
        val platform = Platform(whenInUse, starts = false)
        platform.failingAtStart = LocationException(LocationException.Kind.PermissionRevoked)
        val failures = mutableListOf<Throwable>()
        val source = DeviceLocationSource(platform) { failures += it }

        source.start {}
        assertEquals(LocationException.Kind.PermissionRevoked, assertIs<LocationException>(failures.single()).kind)

        platform.starts = true
        platform.failingAtStart = null
        source.recheckPermission()
        assertEquals(2, platform.requested)
        source.stop()
        assertEquals(1, platform.removed)
    }

    /**
     * Background updates are the app's to declare, which the platform reads before it hands a backgrounded app a
     * fix: a request the manifest does not declare is refused as the source starts, as `sdk-dart`'s Android side
     * refuses it, and the fixes go on while the app is in use.
     */
    @Test
    fun backgroundUpdatesTheManifestDoesNotDeclareAreRefusedAsTheSourceStartsAndTheFixesGoOn() {
        val platform = Platform(whenInUse)
        val failures = mutableListOf<Throwable>()
        val source = DeviceLocationSource(platform, backgroundUpdates = true) { failures += it }
        val stated = mutableListOf<List<Double>>()

        source.start { stated += it }
        platform.receive?.invoke(listOf(6.8652, 45.9237))

        assertEquals(LocationException.Kind.BackgroundUndeclared, assertIs<LocationException>(failures.single()).kind)
        assertEquals(listOf(listOf(6.8652, 45.9237)), stated, "the fixes go on while the app is in use")
        assertTrue(source.backgroundUpdates)
        source.stop()
    }

    @Test
    fun backgroundUpdatesTheManifestDeclaresAreReadWithNoWordAndASourceAskingNoneReadsNoManifest() {
        val declared = Platform(whenInUse, declaresBackground = true)
        val failures = mutableListOf<Throwable>()
        val background =
            DeviceLocationSource(declared, LocationRate.Walking, backgroundUpdates = true) { failures += it }
        background.start {}
        assertEquals(emptyList(), failures)
        assertEquals(1, declared.manifestReads)
        assertEquals(1, declared.requested)
        background.stop()

        val foreground = Platform(whenInUse)
        val source = DeviceLocationSource(foreground) { failures += it }
        source.start {}
        assertEquals(false, source.backgroundUpdates)
        assertEquals(0, foreground.manifestReads, "a source that reads while in use alone asks the manifest nothing")
        assertEquals(emptyList(), failures)
        source.stop()
    }

    /**
     * A source made with no onError drops each failure, never throwing it at the platform's thread, and the
     * platform logs it where the app is debuggable.
     */
    @Test
    fun aSourceGivenNoOnErrorDropsEachFailureToThePlatformsLogAndThrowsNothing() {
        val platform = Platform(whenInUse, starts = false)
        platform.failingAtStart = LocationException(LocationException.Kind.NoProvider)
        val source = DeviceLocationSource(platform, backgroundUpdates = true)

        source.start {}
        platform.fail?.invoke(LocationException(LocationException.Kind.ProviderDisabled))

        assertEquals(
            listOf(
                LocationException.Kind.BackgroundUndeclared,
                LocationException.Kind.NoProvider,
                LocationException.Kind.ProviderDisabled,
            ),
            platform.dropped.map { assertIs<LocationException>(it).kind },
        )
        source.stop()
    }

    /**
     * Any other failure of the platform's reader is told as one, carrying what the platform threw, and the
     * reader is asked again at the next recheck.
     */
    @Test
    fun anyOtherFailureOfThePlatformsReaderIsToldAsPlatformFailedWithItsCause() {
        val thrown = IllegalStateException("the location service is gone")
        val platform = Platform(whenInUse, starts = false)
        platform.failingAtStart = LocationException(LocationException.Kind.PlatformFailed, thrown)
        val failures = mutableListOf<Throwable>()
        val source = DeviceLocationSource(platform) { failures += it }

        source.start {}

        val failure = assertIs<LocationException>(failures.single())
        assertEquals(LocationException.Kind.PlatformFailed, failure.kind)
        assertEquals(thrown, failure.cause)
        source.stop()
    }

    @Test
    fun aFixTheEngineRefusesIsDroppedAndTheNextIsStated() {
        val platform = Platform(whenInUse)
        val source = DeviceLocationSource(platform)
        val stated = mutableListOf<List<Double>>()
        source.start { position ->
            require(position[0].isFinite()) { "refused" }
            stated += position
        }

        platform.receive?.invoke(listOf(Double.NaN, 45.0))
        platform.receive?.invoke(listOf(6.0, 45.0))

        assertEquals(listOf(listOf(6.0, 45.0)), stated)
        source.stop()
    }
}
