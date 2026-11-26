package com.mountaya.conformance

import kotlinx.coroutines.test.TestScope

/**
 * ConformanceRunner runs one capability's fixture cases against this SDK.
 *
 * The capability `transport.retry.backoff` is run by an object named `TransportRetryBackoffRunner`
 * in its module's runner directory. The build finds it by that name and generates one test per
 * case over it, so no table of runners is kept anywhere.
 */
public interface ConformanceRunner {
    /** Runs [case] in [scope], whose virtual clock the runner may advance, and answers it. */
    public suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome
}
