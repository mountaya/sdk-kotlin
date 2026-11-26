package com.mountaya.build.size

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppSizeTest {
    /** What `apkanalyzer dex packages --defined-only` printed of a release build, every package row of note kept. */
    private val captured: String =
        assertNotNull(javaClass.getResource("/size/dex-packages.txt"), "the captured output is missing").readText()

    /** What an app's build wrote of its release runtime classpath, the modules of the libraries of note kept. */
    private val resolved: String =
        assertNotNull(javaClass.getResource("/size/release-runtime-versions.txt"), "the captured list is missing")
            .readText()

    /** The graph Trail's build wrote of its release runtime classpath, whole. */
    private val drawn: String =
        assertNotNull(javaClass.getResource("/size/release-runtime-graph.txt"), "the captured graph is missing")
            .readText()

    @Test
    fun aPackageReadsItsBytesThePackagesBelowItIncludedAndTheTotalRowTheWhole() {
        val packages = DexPackages.read(captured)

        assertEquals(1_753_002, packages.total)
        assertEquals(129_879, packages.bytesOf("com.mountaya.client"), "the printed surface included")
        assertEquals(91_816, packages.bytesOf("com.mountaya.client.generated"))
        assertEquals(218_364, packages.bytesOf("com.mountaya"))
        assertEquals(0, packages.bytesOf("com.mountaya.studio"), "a package the dex code holds no class under")
    }

    @Test
    fun eachModuleIsAShareAndEachLibraryTheSdkBringsAnotherReportedApartAtTheVersionItResolvedAt() {
        val size = AppSize.measure(DexPackages.read(captured), ResolvedVersions.read(resolved), 1, "9.5.1", "2.4.21")

        assertEquals(
            mapOf(
                "com.mountaya.client" to 129_879L,
                "com.mountaya.elements" to 44_494L,
                "com.mountaya.geo" to 36_322L,
                "com.mountaya.navigation" to 7_669L,
            ),
            size.modules,
        )
        assertEquals(
            mapOf(
                "io.ktor" to 133_954L,
                "kotlinx.serialization" to 59_418L,
                "okhttp3" to 117_353L,
                "org.maplibre" to 147_130L,
            ),
            size.brought,
        )
        assertEquals(size.modules.keys + size.brought.keys, size.shares.keys)
        assertEquals(1_753_002, size.dex)
        assertEquals(1, size.scenario)
        assertEquals(129_879L + 44_494 + 36_322 + 7_669, size.sdk, "the SDK's modules together")
        assertEquals(
            mapOf(
                "io.ktor" to "3.6.1",
                "kotlinx.serialization" to "1.13.0",
                "okhttp3" to "5.5.1",
                "org.maplibre" to "13.7.1",
            ),
            size.versions,
            "each library at its module that names its release, MapLibre's GeoJSON and Turf modules left out",
        )
    }

    @Test
    fun aLibraryWhoseModuleTheClasspathResolvesNotIsRefusedNamingIt() {
        val without =
            ResolvedVersions.read(
                resolved.lines().filterNot { it.startsWith("org.maplibre.gl:android-sdk:") }.joinToString("\n"),
            )

        val refused =
            assertFailsWith<IllegalArgumentException> {
                AppSize.measure(DexPackages.read(captured), without, 1, "9.5.1", "2.4.21")
            }
        assertEquals(
            "the release runtime classpath resolves no org.maplibre.gl:android-sdk, the module `org.maplibre`'s " +
                "version is read from",
            refused.message,
        )
    }

    @Test
    fun theVersionsAreReadModuleByModuleAndAListOfAnyOtherShapeIsRefusedSayingWhy() {
        val read = ResolvedVersions.read("\nio.ktor:ktor-client-core:3.6.1\n\norg.maplibre.gl:android-sdk:13.8.0\n")
        assertEquals("3.6.1", read.versionOf("io.ktor:ktor-client-core"))
        assertEquals("13.8.0", read.versionOf("org.maplibre.gl:android-sdk"))
        assertNull(read.versionOf("io.ktor:ktor-client-okhttp"), "a module the classpath does not hold")

        val unversioned =
            assertFailsWith<IllegalArgumentException> { ResolvedVersions.read("io.ktor:ktor-client-core\n") }
        assertTrue("line 1 names no module at a version" in unversioned.message.orEmpty(), unversioned.message)
        val twice = assertFailsWith<IllegalArgumentException> { ResolvedVersions.read("a:b:1\na:b:2\n") }
        assertTrue("line 2 names a:b a second time" in twice.message.orEmpty(), twice.message)
    }

    @Test
    fun outputOfAnyOtherShapeOrWithNoTotalIsRefusedSayingWhy() {
        val noTotal = assertFailsWith<IllegalArgumentException> { DexPackages.read("P d 1\t1\t10\tcom.mountaya.geo\n") }
        assertTrue("no row holds the whole" in noTotal.message.orEmpty())

        val garbled = assertFailsWith<IllegalArgumentException> { DexPackages.read("Successfully loaded maps\n") }
        assertTrue("line 1 is no row apkanalyzer prints" in garbled.message.orEmpty())

        assertEquals(10, DexPackages.read("\nP d 1\t1\t10\t<TOTAL>\n\n").total, "a blank line is no row")
    }

    /**
     * Trail declares the Compose foundation it lays its screen out with and the activity it is set into, and every
     * other library an SDK module reaches is named by a share or reached through one: none is unnamed.
     */
    @Test
    fun trailsReleaseClasspathBringsNoLibraryNoShareNames() {
        assertEquals(emptyList(), ReleaseGraph.read(drawn).unnamed())
    }

    /**
     * An app that declares no Compose foundation of its own carries the foundation `mountaya-elements` brings, and
     * every library below it the app does not reach itself, all through that module and none named.
     */
    @Test
    fun aLibraryAnSdkModuleBringsThatNoShareNamesAndTheAppDoesNotReachIsUnnamed() {
        val declared = "app -> androidx.compose.foundation:foundation"
        val undeclared = drawn.lines().filterNot { it == declared }.joinToString("\n")

        val unnamed = ReleaseGraph.read(undeclared).unnamed()

        val foundation = ReleaseGraph.Unnamed("androidx.compose.foundation:foundation", "mountaya-elements")
        assertTrue(foundation in unnamed, "$unnamed")
        assertTrue(unnamed.all { it.through == "mountaya-elements" }, "$unnamed")
        assertTrue(unnamed.none { it.library.startsWith("com.squareup.okio:") }, "OkHttp's own dependency is OkHttp's")
    }

    /**
     * A named library's own dependencies are its release's, a library the app reaches through its own dependencies is
     * the app's, and one an SDK module reaches through neither is the SDK's, by the first module it is reached through.
     */
    @Test
    fun theWalkStopsAtANamedLibraryAndAtTheAppsOwn() {
        val graph =
            ReleaseGraph.read(
                """
                app -> sdk:mountaya-elements
                app -> androidx.activity:activity
                androidx.activity:activity -> androidx.core:core
                sdk:mountaya-elements -> sdk:mountaya-geo
                sdk:mountaya-elements -> org.maplibre.gl:android-sdk
                org.maplibre.gl:android-sdk -> com.google.code.gson:gson
                sdk:mountaya-elements -> androidx.core:core
                sdk:mountaya-geo -> org.jetbrains.kotlinx:kotlinx-datetime
                org.jetbrains.kotlinx:kotlinx-datetime -> org.jetbrains.kotlinx:kotlinx-serialization-core
                org.jetbrains.kotlinx:kotlinx-datetime -> org.example:below
                """.trimIndent(),
            )

        assertEquals(
            listOf(
                ReleaseGraph.Unnamed("org.example:below", "mountaya-geo"),
                ReleaseGraph.Unnamed("org.jetbrains.kotlinx:kotlinx-datetime", "mountaya-geo"),
            ),
            graph.unnamed(),
        )
        assertEquals("kotlinx.serialization", AppSize.shareNaming("org.jetbrains.kotlinx:kotlinx-serialization-core"))
        assertEquals("org.maplibre", AppSize.shareNaming("org.maplibre.gl:android-sdk-geojson"))
        assertEquals(null, AppSize.shareNaming("org.jetbrains.kotlinx:kotlinx-datetime"))
    }

    @Test
    fun aGraphOfAnyOtherShapeOrWithNoEdgeFromTheAppIsRefusedSayingWhy() {
        val refused =
            mapOf(
                "app -> io.ktor:ktor-client-core:3.6.1" to "line 1 names io.ktor:ktor-client-core:3.6.1",
                "app => sdk:mountaya-client" to "line 1 is no edge",
                "sdk:mountaya-client -> io.ktor:ktor-client-core" to "no edge leaves the app",
            )
        for ((text, reason) in refused) {
            val failure = assertFailsWith<IllegalArgumentException>(text) { ReleaseGraph.read(text) }
            assertTrue(reason in failure.message.orEmpty(), "$text: ${failure.message}")
        }
    }
}
