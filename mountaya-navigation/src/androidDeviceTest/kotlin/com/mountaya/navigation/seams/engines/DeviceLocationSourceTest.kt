package com.mountaya.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The device's source over the platform's own package manager, on the device: the manifest the build merged into
 * this test's own APK, as it merges the library's into an app's, declares no background location, neither
 * `ACCESS_BACKGROUND_LOCATION` nor a foreground service of type `location`, since `mountaya-navigation` declares
 * none, so a source asking for background updates is refused as it starts, as `sdk-dart`'s Android side refuses it,
 * and one given no `onError` drops the refusal and throws nothing.
 */
class DeviceLocationSourceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    /** The kinds a source made with [backgroundUpdates] tells as it starts and stops on the main thread. */
    private fun toldAsItStarts(backgroundUpdates: Boolean): List<LocationException.Kind> {
        val failures = CopyOnWriteArrayList<Throwable>()
        instrumentation.runOnMainSync {
            val source = DeviceLocationSource(context, backgroundUpdates = backgroundUpdates) { failures += it }
            source.start {}
            source.stop()
        }
        return failures.filterIsInstance<LocationException>().map { it.kind }
    }

    @Test
    fun theLibraryDeclaresNoBackgroundLocationForAnAppToMergeIn() {
        val flags = PackageManager.GET_PERMISSIONS
        val permissions =
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
                .requestedPermissions
                .orEmpty()

        assertFalse(Manifest.permission.ACCESS_BACKGROUND_LOCATION in permissions, permissions.toList().toString())
    }

    @Test
    fun backgroundUpdatesTheMergedManifestDoesNotDeclareAreRefusedAsTheSourceStarts() {
        assertTrue(LocationException.Kind.BackgroundUndeclared in toldAsItStarts(backgroundUpdates = true))
    }

    @Test
    fun aSourceAskingNoBackgroundUpdatesIsToldNothingOfTheManifest() {
        assertFalse(LocationException.Kind.BackgroundUndeclared in toldAsItStarts(backgroundUpdates = false))
    }

    /** A debuggable app, as this test's APK is, logs the failure it dropped; nothing is thrown at the main thread. */
    @Test
    fun aSourceGivenNoOnErrorDropsTheRefusalAndThrowsNothing() {
        var thrown: Throwable? = null
        instrumentation.runOnMainSync {
            try {
                val source = DeviceLocationSource(context, backgroundUpdates = true)
                source.start {}
                source.stop()
            } catch (failure: Throwable) {
                thrown = failure
            }
        }

        assertEquals(null, thrown)
        assertTrue(logsDroppedFailures(context.applicationInfo.flags), "the test's APK is debuggable")
    }
}
