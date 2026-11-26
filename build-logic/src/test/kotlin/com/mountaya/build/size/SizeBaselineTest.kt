package com.mountaya.build.size

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SizeBaselineTest {
    private val path = "examples/trail/size/android.json"

    private fun size(
        client: Long = 129_879,
        ktor: Long = 133_954,
        maplibre: Long = 147_130,
        maplibreVersion: String = "13.7.1",
        agp: String = "9.5.1",
        kotlin: String = "2.4.21",
        scenario: Int = 1,
    ): AppSize =
        AppSize(
            scenario = scenario,
            agp = agp,
            kotlin = kotlin,
            dex = 1_753_002,
            modules =
                mapOf(
                    "com.mountaya.client" to client,
                    "com.mountaya.elements" to 44_494,
                    "com.mountaya.geo" to 36_322,
                    "com.mountaya.navigation" to 7_669,
                ),
            brought =
                mapOf(
                    "io.ktor" to ktor,
                    "kotlinx.serialization" to 59_418,
                    "okhttp3" to 117_353,
                    "org.maplibre" to maplibre,
                ),
            versions =
                mapOf(
                    "io.ktor" to "3.6.1",
                    "kotlinx.serialization" to "1.13.0",
                    "okhttp3" to "5.5.1",
                    "org.maplibre" to maplibreVersion,
                ),
        )

    @Test
    fun aShareMayGrowByTwoPercentOfItsBaselineOrSixteenKibibytesWhicheverIsLarger() {
        assertEquals(16 * 1024L, SizeBaseline.tolerance(0))
        assertEquals(16 * 1024L, SizeBaseline.tolerance(129_879))
        assertEquals(20_000L, SizeBaseline.tolerance(1_000_000))
    }

    @Test
    fun aBaselineReadsBackAsTheFiguresItWasPrintedFrom() {
        val printed = SizeBaseline.print(size())

        assertEquals(size(), SizeBaseline.read(printed))
        assertTrue(printed.startsWith("{\n  \"platform\": \"android\",\n"), printed)
        assertTrue(
            "  \"build\": \"./gradlew -p examples/trail assembleRelease\",\n  \"scenario\": 1,\n" in printed,
            printed,
        )
        assertTrue("\"agp\": \"9.5.1\"" in printed && "\"kotlin\": \"2.4.21\"" in printed, printed)
        assertTrue("  \"versions\": {\n    \"io.ktor\": \"3.6.1\",\n" in printed, printed)
        assertTrue("    \"org.maplibre\": \"13.7.1\"\n  }\n}\n" in printed, printed)
        assertTrue(printed.endsWith("}\n"))
    }

    @Test
    fun theSameFiguresPassEachShareStatingItsMove() {
        val verdict = SizeBaseline.compare(size(), SizeBaseline.print(size()), path)

        assertEquals(emptyList(), verdict.findings)
        assertEquals(9, verdict.notes.size)
        assertTrue(verdict.notes.dropLast(1).all { "+0 B on the baseline" in it }, verdict.notes.joinToString("\n"))
        assertEquals(
            "the SDK's modules together 213.2 KiB, of the 1.67 MiB of the app's dex code",
            verdict.notes.last(),
        )
    }

    /**
     * The scenario decides what the app does, so a baseline of another version of it is reported and not compared,
     * however far a share moved, and the change that moves the version records the baseline again.
     */
    @Test
    fun aBaselineOfAnotherScenarioIsReportedAndNotCompared() {
        val verdict = SizeBaseline.compare(size(client = 900_000, scenario = 2), SizeBaseline.print(size()), path)

        assertEquals(emptyList(), verdict.findings)
        assertTrue(
            verdict.notes.first().startsWith(
                "Trail follows version 2 of sdk/trail.yaml, and the baseline was measured against version 1, so the " +
                    "figures are reported and not compared",
            ),
            verdict.notes.first(),
        )
        assertTrue(verdict.notes.any { "the baseline 126.8 KiB" in it }, verdict.notes.joinToString("\n"))
    }

    /**
     * A library an SDK module brings that no share names fails the gate by its name and the module it comes through.
     */
    @Test
    fun aLibraryNoShareNamesIsAFindingNamingTheModuleItComesThrough() {
        assertEquals(
            listOf(
                "`org.jetbrains.kotlinx:kotlinx-datetime` reaches the app through `mountaya-geo`, and no share names " +
                    "it: name it in AppSize, its package in BROUGHT with the module its version is read at and its " +
                    "modules in NAMES, then run ${SizeBaseline.RECORD}",
            ),
            SizeBaseline.unnamed(
                listOf(ReleaseGraph.Unnamed("org.jetbrains.kotlinx:kotlinx-datetime", "mountaya-geo")),
            ),
        )
        assertEquals(emptyList(), SizeBaseline.unnamed(emptyList()))
    }

    @Test
    fun aShareGrownPastItsToleranceFailsAndOneWithinItPasses() {
        val baseline = SizeBaseline.print(size())

        val within = SizeBaseline.compare(size(client = 129_879 + 16 * 1024), baseline, path)
        assertEquals(emptyList(), within.findings)

        val grown =
            SizeBaseline.compare(
                size(client = 129_879 + 16 * 1024 + 1, ktor = 133_954 + 40_000),
                baseline,
                path,
            )
        assertEquals(2, grown.findings.size, grown.findings.joinToString("\n"))
        assertTrue(
            grown.findings[0].startsWith("$path: `com.mountaya.client`'s share grew from 126.8 KiB to 142.8 KiB"),
        )
        assertTrue("which the SDK brings, grew from" in grown.findings[1], grown.findings[1])
        assertTrue(grown.findings.all { SizeBaseline.RECORD in it })
    }

    /** A library resolved at the version its baseline records is held to its share as any module is. */
    @Test
    fun aLibraryResolvedAtTheVersionItsBaselineRecordsIsCompared() {
        val grown = SizeBaseline.compare(size(maplibre = 147_130 + 40_000), SizeBaseline.print(size()), path)

        assertEquals(
            listOf(
                "$path: `org.maplibre`'s share, which the SDK brings, grew from 143.7 KiB to 182.7 KiB, +39.1 KiB " +
                    "(+27.2%), past the tolerance of 16.0 KiB: find what a keep rule or a reflective read holds " +
                    "that R8 would shrink, and after a deliberate change run ${SizeBaseline.RECORD}",
            ),
            grown.findings,
        )
    }

    /**
     * A release a range admits, MapLibre's within `[13.7.1,14.0)`, moves the library's share on a commit that changed
     * nothing: the share is reported beside both versions and not compared, however far it moved.
     */
    @Test
    fun aLibraryResolvedAtAnotherVersionIsReportedAndNotCompared() {
        val verdict =
            SizeBaseline.compare(
                size(maplibre = 147_130 + 400_000, maplibreVersion = "13.8.0"),
                SizeBaseline.print(size()),
                path,
            )

        assertEquals(emptyList(), verdict.findings)
        assertEquals(
            "`org.maplibre`'s share, which the SDK brings, resolved at 13.8.0 where the baseline records 13.7.1, so " +
                "its 534.3 KiB is reported and not compared with the baseline's 143.7 KiB: the library's own release " +
                "shapes its share as much as the SDK does, and ${SizeBaseline.RECORD} records it at 13.8.0",
            verdict.notes.single { "org.maplibre" in it },
        )
        assertEquals(7, verdict.notes.count { "on the baseline, within" in it }, "every other share is compared")
    }

    /**
     * The SDK's own shares are compared whatever its libraries resolved at, so a library's new release never hides a
     * module's growth.
     */
    @Test
    fun theSdksOwnSharesAreComparedWhateverItsLibrariesResolvedAt() {
        val verdict =
            SizeBaseline.compare(
                size(client = 129_879 + 40_000, maplibreVersion = "13.8.0"),
                SizeBaseline.print(size()),
                path,
            )

        assertEquals(1, verdict.findings.size, verdict.findings.joinToString("\n"))
        assertTrue(verdict.findings.single().startsWith("$path: `com.mountaya.client`'s share grew from"))
        assertTrue(verdict.notes.any { "resolved at 13.8.0 where the baseline records 13.7.1" in it })
    }

    @Test
    fun aShareShrunkPastItsToleranceIsANoteAndNoFinding() {
        val verdict = SizeBaseline.compare(size(client = 60_000), SizeBaseline.print(size()), path)

        assertEquals(emptyList(), verdict.findings)
        assertTrue(
            verdict.notes.any { "`com.mountaya.client`'s share shrank from" in it },
            verdict.notes.joinToString("\n"),
        )
    }

    @Test
    fun aMissingOrUnreadableBaselineFailsAndOneMissingAShareOrAVersionFailsForIt() {
        assertTrue(
            SizeBaseline
                .compare(size(), null, path)
                .findings
                .single()
                .startsWith("$path: no baseline: run"),
        )
        assertTrue(
            "is no size baseline, since it is no JSON" in SizeBaseline.compare(size(), "{", path).findings.single(),
        )

        val partial = SizeBaseline.print(size()).replace("    \"okhttp3\": 117353,\n", "")
        assertEquals(
            listOf("$path records no `okhttp3` share, which the gate now holds: run ${SizeBaseline.RECORD}"),
            SizeBaseline.compare(size(), partial, path).findings,
        )

        val unversioned = SizeBaseline.print(size()).replace(",\n    \"org.maplibre\": \"13.7.1\"", "")
        assertEquals(
            listOf("$path records no version of `org.maplibre`, which the gate now holds: run ${SizeBaseline.RECORD}"),
            SizeBaseline.compare(size(), unversioned, path).findings,
        )
    }

    @Test
    fun aBaselineOfAnotherToolchainIsReportedAndNotCompared() {
        val older = SizeBaseline.print(size(client = 10, agp = "9.4.0"))

        val verdict = SizeBaseline.compare(size(client = 900_000), older, path)

        assertEquals(emptyList(), verdict.findings)
        assertTrue(
            verdict.notes.first().startsWith(
                "built with the Android Gradle plugin 9.5.1 and Kotlin 2.4.21, and the baseline with 9.4.0",
            ),
        )
        assertTrue(verdict.notes.any { "the baseline 10 B" in it }, verdict.notes.joinToString("\n"))
        assertTrue(
            "`org.maplibre`'s share, which the SDK brings, 143.7 KiB at 13.7.1, the baseline 143.7 KiB at 13.7.1" in
                verdict.notes,
            verdict.notes.joinToString("\n"),
        )
        val otherKotlin =
            SizeBaseline.compare(
                size(client = 900_000),
                SizeBaseline.print(size(kotlin = "2.4.20")),
                path,
            )
        assertEquals(emptyList(), otherKotlin.findings)
    }

    @Test
    fun aDocumentOfAnyOtherShapeIsNoBaseline() {
        val figures =
            "\"platform\": \"android\", \"scenario\": 1, \"agp\": \"9.5.1\", \"kotlin\": \"2.4.21\", \"dex\": 1, " +
                "\"modules\": {}, \"brought\": {\"io.ktor\": 1}"
        val refused =
            mapOf(
                "[]" to "it holds no object",
                "{\"platform\": \"ios\"}" to "it records ios, not android",
                "{\"platform\": \"android\", \"agp\": \"9.5.1\"}" to "it records no version of the scenario",
                "{\"platform\": \"android\", \"scenario\": 0}" to "it records no version of the scenario",
                "{\"platform\": \"android\", \"scenario\": 1, \"agp\": \"9.5.1\"}" to
                    "it names no Android Gradle plugin and Kotlin",
                "{\"platform\": \"android\", \"scenario\": 1, \"agp\": \"9.5.1\", \"kotlin\": \"2.4.21\", " +
                    "\"dex\": -1}" to "`dex` holds no count of bytes",
                "{$figures}" to "it holds no `versions`",
                "{$figures, \"versions\": {\"io.ktor\": 3}}" to "`versions.io.ktor` holds no version",
                "{$figures, \"versions\": {\"io.ktor\": \" \"}}" to "`versions.io.ktor` holds no version",
            )
        for ((text, reason) in refused) {
            val failure = assertFailsWith<IllegalArgumentException>(text) { SizeBaseline.read(text) }
            assertTrue(reason in failure.message.orEmpty(), "$text: ${failure.message}")
        }
    }

    @Test
    fun bytesAreWrittenAsAPersonReadsThem() {
        assertEquals("512 B", SizeBaseline.formatBytes(512))
        assertEquals("126.8 KiB", SizeBaseline.formatBytes(129_879))
        assertEquals("1.67 MiB", SizeBaseline.formatBytes(1_753_002))
    }
}
