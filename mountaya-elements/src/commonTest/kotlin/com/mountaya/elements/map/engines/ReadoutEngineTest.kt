package com.mountaya.elements

import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile
import com.mountaya.geo.elevationTilesAt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutEngineTest {
    /** A source whose every tile read is held until the test answers it. */
    private class Held : ElevationReader {
        val reads = mutableListOf<Pair<ElevationTile.Address, CompletableDeferred<ElevationTile>>>()

        override suspend fun source(): ElevationSource = ElevationSource(ElevationEncoding.Mapbox, 4, 16)

        override suspend fun tile(address: ElevationTile.Address): ElevationTile {
            val answer = CompletableDeferred<ElevationTile>()
            reads += address to answer
            return answer.await()
        }
    }

    /** A tile of [address] whose every pixel reads 1500 meters. */
    private fun flat(address: ElevationTile.Address) =
        ElevationTile(
            address,
            4,
            IntArray(16) {
                (1 shl 16) or
                    (193 shl 8) or
                    56
            },
        )

    private val here = listOf(6.8652, 45.9237)

    @Test
    fun aPressOpensTheReadoutAtOnceAndItsTerrainJoinsOnceTheTileAnswers() =
        runTest {
            val reader = Held()
            val readout = ReadoutEngine(backgroundScope, reader)
            readout.press(here, listOf(1.0, 2.0), 14.0)
            runCurrent()

            assertNull(assertNotNull(readout.open.value, "the readout is open").terrain)
            assertEquals(1, reader.reads.size)
            reader.reads.single().let { (address, answer) -> answer.complete(flat(address)) }
            runCurrent()
            assertEquals(
                1500.0,
                assertNotNull(readout.open.value?.terrain, "the terrain joined the readout").elevation,
                1e-6,
            )
        }

    @Test
    fun belowTheDetailZoomNothingIsRead() =
        runTest {
            val reader = Held()
            val readout = ReadoutEngine(backgroundScope, reader)
            readout.press(here, listOf(0.0, 0.0), 12.5)
            runCurrent()

            assertTrue(reader.reads.isEmpty())
            assertNotNull(readout.open.value, "the readout is open")
        }

    @Test
    fun anAnswerForAReadoutSinceClosedLandsNowhere() =
        runTest {
            val reader = Held()
            val readout = ReadoutEngine(backgroundScope, reader)
            readout.press(here, listOf(0.0, 0.0), 14.0)
            runCurrent()
            readout.close()
            reader.reads.single().let { (address, answer) -> answer.complete(flat(address)) }
            runCurrent()

            assertNull(readout.open.value)
        }

    @Test
    fun aMapWhoseReadoutIsOffOpensNothingAndReadsNothing() =
        runTest {
            val reader = Held()
            val readout = ReadoutEngine(backgroundScope, reader)
            readout.isEnabled = false
            readout.press(here, listOf(0.0, 0.0), 14.0)
            runCurrent()

            assertNull(readout.open.value)
            assertTrue(reader.reads.isEmpty())
        }

    @Test
    fun aSourceThatCannotBeReadLeavesTheReadoutWithoutTerrain() =
        runTest {
            val failing =
                object : ElevationReader {
                    override suspend fun source(): ElevationSource = throw IllegalStateException("no catalogue")

                    override suspend fun tile(address: ElevationTile.Address): ElevationTile = error("no tile")
                }
            val readout = ReadoutEngine(backgroundScope, failing)
            readout.press(here, listOf(0.0, 0.0), 14.0)
            runCurrent()

            assertNull(assertNotNull(readout.open.value, "the readout is open").terrain)
        }

    /** Runs the last block handed to it first, so a block dispatched later overtakes one dispatched before it. */
    private class Backwards : CoroutineDispatcher() {
        private val queued = ArrayDeque<Runnable>()

        override fun dispatch(
            context: CoroutineContext,
            block: Runnable,
        ) {
            queued.addFirst(block)
        }

        /** Runs every block queued, and every block those queue, the latest first. */
        fun runAll() {
            while (queued.isNotEmpty()) queued.removeFirst().run()
        }
    }

    @Test
    fun theTileReadsLeaveInTheOrderElevationTilesAtAnswersWhateverTheDispatcherRunsFirst() {
        val reader = Held()
        val backwards = Backwards()
        val readout = ReadoutEngine(CoroutineScope(backwards), reader)
        val nearAnEdge = listOf(6.8665924072265625, 45.9237)
        readout.press(nearAnEdge, listOf(0.0, 0.0), 14.0)
        backwards.runAll()

        val tiles = elevationTilesAt(nearAnEdge, ElevationSource(ElevationEncoding.Mapbox, 4, 16))
        assertEquals(2, tiles.size, "the point needs two tiles")
        assertEquals(tiles, reader.reads.map { it.first }, "by x then y")
    }
}
