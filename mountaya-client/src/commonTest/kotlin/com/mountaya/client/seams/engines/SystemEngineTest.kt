package com.mountaya.client

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Each target's engine, as common code sees it; what each engine is configured with is that target's own test. */
class SystemEngineTest {
    @Test
    fun everyEngineBuiltIsOneOfItsOwn() {
        val first = systemEngine()
        val second = systemEngine()
        try {
            assertNotSame(first, second)
        } finally {
            first.close()
            second.close()
        }
    }

    @Test
    fun aFailureThePlatformDoesNotReportAsACancellationIsThrownAsItIs() {
        val failure = IllegalStateException("Canceled")

        assertFalse(isPlatformCancellation(failure))
        assertFalse(isPlatformCancellation(RuntimeException("Connection reset", failure)))
        assertSame(failure, SystemExchange.reading(failure))
        val cancelled = CancellationException("The caller left.")
        assertSame(cancelled, SystemExchange.reading(cancelled))
        assertTrue(SystemExchange.reading(cancelled) is CancellationException)
    }
}
