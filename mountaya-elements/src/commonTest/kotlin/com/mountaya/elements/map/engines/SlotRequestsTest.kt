package com.mountaya.elements

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SlotRequestsTest {
    @Test
    fun theLatestRequestOwnsItsSlotARequestALaterOneSupersededNoLongerDoes() {
        val slots = SlotRequests()
        val first = slots.claim(ContentSlot.Itinerary)
        val second = slots.claim(ContentSlot.Itinerary)

        assertFalse(slots.owns(ContentSlot.Itinerary, first))
        assertTrue(slots.owns(ContentSlot.Itinerary, second))
        assertFalse(slots.owns(ContentSlot.Collection, second), "a slot is a kind of content of its own")
        assertTrue(second > first, "tokens only grow")
        slots.release(ContentSlot.Itinerary)
        assertFalse(slots.owns(ContentSlot.Itinerary, second))
        assertTrue(slots.requestedSlots().isEmpty())
    }

    @Test
    fun aCallWithdrawnBeforeItDrewGivesTheSlotBackToTheRequestItSuperseded() {
        val slots = SlotRequests()
        val first = slots.claim(ContentSlot.Itinerary)
        val second = slots.claim(ContentSlot.Itinerary)
        slots.giveBack(ContentSlot.Itinerary, second)

        assertTrue(slots.owns(ContentSlot.Itinerary, first))
        val third = slots.claim(ContentSlot.Collection)
        slots.giveBack(ContentSlot.Collection, third)
        assertFalse(slots.owns(ContentSlot.Collection, third), "a slot given back by its first request asks nothing")
        assertEquals(listOf<ContentSlot>(ContentSlot.Itinerary), slots.requestedSlots())
    }

    @Test
    fun aSupersededRequestWaitsForTheCallThatTookItsSlotAndLearnsWhetherItLost() =
        runTest {
            val slots = SlotRequests()
            val first = slots.claim(ContentSlot.Itinerary)
            val second = slots.claim(ContentSlot.Itinerary)
            val waiting = async { slots.regains(ContentSlot.Itinerary, first) }
            runCurrent()
            slots.giveBack(ContentSlot.Itinerary, second)
            assertTrue(waiting.await(), "given back, the request owns its slot again")

            val third = slots.claim(ContentSlot.Itinerary)
            val lost = async { slots.regains(ContentSlot.Itinerary, first) }
            runCurrent()
            slots.settle(third)
            assertFalse(lost.await(), "the call that drew took the slot for good")
        }

    @Test
    fun aRequestThatTakesItsSlotForGoodAndAClearEndEveryWait() =
        runTest {
            val slots = SlotRequests()
            val first = slots.claim(ContentSlot.Zones)
            slots.claim(ContentSlot.Zones)
            val waiting = async { slots.regains(ContentSlot.Zones, first) }
            runCurrent()
            val taken = slots.take(ContentSlot.Zones)
            assertFalse(waiting.await())
            assertTrue(slots.owns(ContentSlot.Zones, taken))

            val again = slots.claim(ContentSlot.Zones)
            val cleared = async { slots.regains(ContentSlot.Zones, taken) }
            runCurrent()
            slots.release(ContentSlot.Zones)
            assertFalse(cleared.await())
            assertFalse(slots.owns(ContentSlot.Zones, again))
        }

    @Test
    fun aRequestNoLaterCallSupersededHasNothingToWaitFor() =
        runTest {
            val slots = SlotRequests()
            val only = slots.claim(ContentSlot.Collection)
            assertTrue(slots.regains(ContentSlot.Collection, only))
        }

    /**
     * Kotlin's twin of a Swift wait ending with nothing given back: the cancelled wait throws its
     * cancellation, and its request is given nothing.
     */
    @Test
    fun aWaitWhoseCoroutineIsCancelledEndsWithNothingGivenBack() =
        runTest {
            val slots = SlotRequests()
            val first = slots.claim(ContentSlot.Itinerary)
            val second = slots.claim(ContentSlot.Itinerary)
            val waiting = async { slots.regains(ContentSlot.Itinerary, first) }
            runCurrent()
            waiting.cancel()
            assertTrue(runCatching { waiting.await() }.exceptionOrNull() is CancellationException)
            slots.giveBack(ContentSlot.Itinerary, second)
            assertFalse(
                slots.owns(ContentSlot.Itinerary, first),
                "a request withdrawn while it waited is given nothing",
            )
        }
}
