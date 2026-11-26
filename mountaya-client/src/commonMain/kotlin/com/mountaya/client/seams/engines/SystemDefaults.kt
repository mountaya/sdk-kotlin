package com.mountaya.client

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * SystemDefaults holds the module's only reads of the environment: the wall clock, a random
 * source and a dispatcher.
 *
 * Everything that needs one of them takes it as an argument and defaults to the value here, so
 * a rule never reads the environment itself and a test replaces each one at construction.
 *
 * It is `InternalMountayaApi`: the admin unit, `mountaya-admin`, judges a webhook delivery by its clock where the
 * receiver states none, from beside the core, and no consumer reads it (SPEC.md section 7).
 */
@InternalMountayaApi
public object SystemDefaults {
    /** The wall clock. */
    public val clock: Clock = Clock.System

    /**
     * Uniform draws from the platform's default random number generator, and keys from
     * `Uuid.random`, which reads the platform's cryptographic random source on every target.
     */
    public val random: RandomSource =
        object : RandomSource {
            override fun unitDraw(): Double = Random.nextDouble()

            override fun cryptographicUuid(): Uuid = Uuid.random()
        }

    /** The dispatcher work runs on when the caller names none. */
    public val dispatcher: CoroutineDispatcher = Dispatchers.Default
}
