package com.mountaya.client

import kotlin.uuid.Uuid

/**
 * RandomSource is what the client draws at random: the jitter of every wait between two attempts
 * and between two opens of an event stream, and the `Idempotency-Key` of every write that
 * declares one.
 *
 * A client given none draws from the platform. A test stands in for the platform, so that a wait
 * and a key are the ones its fixture names.
 */
public interface RandomSource {
    /** Returns a number drawn uniformly from 0, inclusive, to 1, exclusive. */
    public fun unitDraw(): Double

    /**
     * Returns a version 4 UUID drawn from a cryptographic random source, or null where the runtime
     * offers none.
     *
     * A write whose key could not be drawn sends none, never one drawn from a weaker source: two
     * calls that drew the same key would each be answered with the other's answer.
     */
    public fun cryptographicUuid(): Uuid?
}
