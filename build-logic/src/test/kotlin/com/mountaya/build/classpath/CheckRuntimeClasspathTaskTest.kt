package com.mountaya.build.classpath

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckRuntimeClasspathTaskTest {
    private fun refuses(
        module: String,
        group: String,
    ) = CheckRuntimeClasspathTask.refusedFor(module).any { it.matches(group) }

    /** The client's admin unit draws nothing either: it carries what the core carries and no more. */
    @Test
    fun aHeadlessModuleReachesNoRendererNoNavigationLibraryAndNoAndroidx() {
        for (module in listOf("mountaya-client", "mountaya-admin", "mountaya-geo")) {
            assertTrue(refuses(module, "org.maplibre.gl"))
            assertTrue(refuses(module, "com.stadiamaps.ferrostar"))
            assertTrue(refuses(module, "androidx.core"))
            assertFalse(refuses(module, "org.jetbrains.kotlinx"))
        }
    }

    @Test
    fun theRendererIsReachedFromItsOwnModuleAlone() {
        assertFalse(refuses("mountaya-elements", "org.maplibre.gl"))
        assertTrue(refuses("mountaya-navigation", "org.maplibre.gl"))
        assertFalse(refuses("mountaya-navigation", "androidx.core"))
    }

    /**
     * The navigation is Geo's machine with its guidance, written here: no module carries a navigation
     * library, the navigation module least of all.
     */
    @Test
    fun noModuleReachesANavigationLibrary() {
        for (module in listOf(
            "mountaya-client",
            "mountaya-admin",
            "mountaya-geo",
            "mountaya-navigation",
            "mountaya-elements",
        )) {
            assertTrue(refuses(module, "com.stadiamaps.ferrostar"), module)
        }
    }

    @Test
    fun theAndroidxOkHttpBringsIsLetInThroughOkHttpAloneAndNoOtherAndroidxIs() {
        val androidx = CheckRuntimeClasspathTask.refusedFor("mountaya-client").single { it.prefix == "androidx" }
        val throughOkHttp =
            listOf("mountaya-client", "com.squareup.okhttp3:okhttp", "com.squareup.okhttp3:okhttp-android")

        assertTrue(androidx.allows("androidx.startup", throughOkHttp))
        assertTrue(androidx.allows("androidx.tracing", throughOkHttp + "androidx.startup:startup-runtime"))
        assertFalse(androidx.allows("androidx.compose.ui", throughOkHttp))
        assertFalse(androidx.allows("androidx.startup", listOf("mountaya-client", "com.example:other")))
        assertFalse(
            CheckRuntimeClasspathTask
                .refusedFor(
                    "mountaya-elements",
                ).any { it.allows("androidx.startup", throughOkHttp) },
        )
    }

    @Test
    fun aGroupMatchesItselfAndWhatIsBeneathItOnly() {
        val group = CheckRuntimeClasspathTask.RefusedGroup("org.maplibre", "because")

        assertEquals(
            listOf(true, true, false),
            listOf("org.maplibre", "org.maplibre.gl", "org.maplibrex").map(group::matches),
        )
    }
}
