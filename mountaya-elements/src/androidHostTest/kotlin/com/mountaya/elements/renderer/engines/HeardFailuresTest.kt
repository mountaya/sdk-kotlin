package com.mountaya.elements

import java.net.ConnectException
import java.util.Collections
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The failures the request hook hears reach the main thread a batch at a time: here a test's own queue stands for the
 * main thread's, and each hop posted waits in it until the test runs it, as a main thread busy drawing a frame holds
 * what is posted to it.
 */
class HeardFailuresTest {
    /** The hops posted, waiting for the main thread. */
    private val posted: MutableList<Runnable> = Collections.synchronizedList(mutableListOf())

    /** The failures told, in the order told. */
    private val told = mutableListOf<HeardFailures.Failure>()

    private val heard = HeardFailures({ posted += it }) { told += it }

    private fun failure(
        url: String,
        status: Int? = 401,
    ): HeardFailures.Failure = HeardFailures.Failure(url, status, false, null)

    /** Runs the hop posted [index]th, as the main thread would. */
    private fun runHop(index: Int) = posted[index].run()

    /**
     * Eight threads of OkHttp's hear a hundred refused tiles each while the main thread is busy: one hop, where each
     * failure was a hop of its own, and it tells every failure once, each thread's in the order that thread heard them.
     */
    @Test
    fun aStormHeardBeforeTheMainThreadRunsIsOneHopEachFailureToldOnceInTheOrderHeard() {
        val start = CountDownLatch(1)
        val threads =
            (0 until 8).map { thread ->
                thread {
                    start.await()
                    for (tile in 0 until 100) heard.hear(failure("https://tiles.example.test/$thread/$tile"))
                }
            }
        start.countDown()
        threads.forEach(Thread::join)

        assertEquals(1, posted.size, "one hop for the storm")
        assertEquals(1, heard.hops)
        assertTrue(told.isEmpty(), "nothing is told before the main thread runs the hop")
        runHop(0)

        assertEquals(800, told.size)
        assertEquals(800, told.map { it.url }.toSet().size, "each failure told once")
        for (thread in 0 until 8) {
            val its = told.map { it.url }.filter { it.startsWith("https://tiles.example.test/$thread/") }
            assertEquals((0 until 100).map { "https://tiles.example.test/$thread/$it" }, its, "thread $thread's order")
        }
    }

    @Test
    fun aFailureHeardAfterTheHopRanOpensTheNextBatchAndItsHop() {
        heard.hear(failure("a"))
        heard.hear(failure("b"))
        runHop(0)
        heard.hear(failure("c"))

        assertEquals(2, posted.size)
        assertEquals(listOf("a", "b"), told.map { it.url })
        runHop(1)
        assertEquals(listOf("a", "b", "c"), told.map { it.url })
        assertEquals(2, heard.hops)
    }

    /**
     * A failure heard while the hop tells its batch, as another of OkHttp's threads may hear one meanwhile, waits for
     * the next hop: it is neither lost nor told twice.
     */
    @Test
    fun aFailureHeardWhileTheHopTellsItsBatchWaitsForTheNextHop() {
        lateinit var meanwhile: HeardFailures
        meanwhile =
            HeardFailures({ posted += it }) { one ->
                told += one
                // Heard as the batch is told: the batch was taken before it.
                if (one.url == "a") meanwhile.hear(failure("d"))
            }
        meanwhile.hear(failure("a"))
        meanwhile.hear(failure("b"))
        runHop(0)

        assertEquals(listOf("a", "b"), told.map { it.url })
        assertEquals(2, posted.size)
        runHop(1)
        assertEquals(listOf("a", "b", "d"), told.map { it.url })
        assertEquals(2, meanwhile.hops)
    }

    @Test
    fun aFailureIsToldAsItWasHeardItsStatusItsCredentialsWordAndItsCause() {
        val cause = IllegalStateException("the session token was refused")
        val unreadable = HeardFailures.Failure("https://tiles.example.test/v1/style.json", null, false, cause)
        val superseded = HeardFailures.Failure("https://tiles.example.test/v1/7/1/2.pbf", 401, true, null)
        val dropped = HeardFailures.Failure("https://tiles.example.test/v1/7/1/3.pbf", null, false, ConnectException())

        heard.hear(unreadable)
        heard.hear(superseded)
        heard.hear(dropped)
        runHop(0)

        assertEquals(3, told.size)
        assertSame(unreadable, told[0])
        assertSame(superseded, told[1])
        assertSame(dropped, told[2])
    }
}
