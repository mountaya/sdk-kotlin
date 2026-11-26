package com.mountaya.elements

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.time.Clock
import kotlin.time.Instant

class LocalMountayaClockTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aComposableUnderNoClockReadsTheSystemsAndTheClockProvidedReachesEveryComposableUnderIt() {
        val pinned =
            object : Clock {
                override fun now(): Instant = Instant.parse("2026-03-14T09:26:53.900Z")
            }
        var standard: Clock? = null
        var provided: Clock? = null
        compose.setContent {
            standard = LocalMountayaClock.current
            CompositionLocalProvider(LocalMountayaClock provides pinned) { provided = LocalMountayaClock.current }
        }

        compose.runOnIdle {
            assertSame<Clock?>(Clock.System, standard)
            assertSame<Clock?>(pinned, provided)
        }
    }
}
