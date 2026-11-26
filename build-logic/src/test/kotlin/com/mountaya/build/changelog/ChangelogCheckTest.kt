package com.mountaya.build.changelog

import kotlin.test.Test
import kotlin.test.assertEquals

class ChangelogCheckTest {
    private val modules = setOf("mountaya-client", "mountaya-elements")
    private val published = setOf("Mountaya", "EventStream", "readingAsUser", "MapView", "SCHEMA_VERSION")

    @Test
    fun anUnreleasedSectionThenEachReleaseWithItsDateNewestFirstIsTheShape() {
        val changelog =
            listOf(
                "# Changelog",
                "## [Unreleased]",
                "- Next.",
                "## [0.10.0] - 2027-01-31",
                "## [0.2.0] - 2026-12-01",
                "## [0.1.0] - 2026-11-18",
            ).joinToString("\n\n")

        assertEquals(emptyList(), ChangelogCheck.problems(changelog, GROUP, modules, published))
        val first = "# Changelog\n\n## [Unreleased]\n"
        assertEquals(emptyList(), ChangelogCheck.problems(first, GROUP, modules, published))
    }

    @Test
    fun aSectionOutOfShapeOrOutOfOrderIsRefused() {
        val changelog =
            listOf(
                "## [0.1.0] - 2026-11-18",
                "## [0.3.0]",
                "## [0.2.0] - 2026-02-30",
                "## [01.0.0] - 2026-11-18",
                "## [0.2.0] - 2026-10-01",
                "## [Unreleased]",
            ).joinToString("\n\n")

        assertEquals(
            listOf(
                "The first section is ## [0.1.0] - 2026-11-18, where it is ## [Unreleased].",
                "`## [0.3.0]` is no `## [MAJOR.MINOR.PATCH] - YYYY-MM-DD`.",
                "`## [0.2.0] - 2026-02-30` dates no day of the calendar.",
                "`## [01.0.0] - 2026-11-18` is no `## [MAJOR.MINOR.PATCH] - YYYY-MM-DD`.",
                "`## [0.2.0] - 2026-10-01` is not older than the release above it, and the releases go newest first.",
                "`## [Unreleased]` is no `## [MAJOR.MINOR.PATCH] - YYYY-MM-DD`.",
            ),
            ChangelogCheck.problems(changelog, GROUP, modules, published),
        )
    }

    @Test
    fun aModuleItsCoordinateAndAPathOfPublishedDeclarationsAreNames() {
        val changelog =
            "## [Unreleased]\n\n`mountaya-client` is `com.mountaya:mountaya-client` in `com.mountaya`: `Mountaya`, " +
                "`EventStream.readingAsUser`, `MapView()` on `android`, `SCHEMA_VERSION`.\n"

        assertEquals(emptyList(), ChangelogCheck.problems(changelog, GROUP, modules, published))
    }

    @Test
    fun whatIsNoNameIsLeftAloneAndANameNoModulePublishesIsRefused() {
        val changelog =
            "## [Unreleased]\n\n`mise run release <version>`, `mountaya/schemas`, `v0.8.0` and `[Unreleased]` " +
                "are no names. `mountaya-maps`, `com.mountaya:mountaya-maps`, `org.example:mountaya-client`, " +
                "`EventStream.readingAs` and `MapEngine` are, and nothing publishes them; `MapEngine` is said once.\n"

        val problems = ChangelogCheck.problems(changelog, GROUP, modules, published)

        assertEquals(
            listOf(
                "mountaya-maps",
                "com.mountaya:mountaya-maps",
                "org.example:mountaya-client",
                "EventStream.readingAs",
                "MapEngine",
            ),
            problems.map { it.substringAfter('`').substringBefore('`') },
        )
    }

    @Test
    fun aKlibDumpPublishesTheDeclarationsItSignsAndAJvmDumpItsClassesAndMembers() {
        val klib =
            listOf(
                "// Klib ABI Dump",
                "// Library unique name: <com.mountaya:mountaya-geo>",
                "final enum class geo/LayerRank : kotlin/Enum<geo/LayerRank> { // geo/LayerRank|null[0]",
                "    enum entry ItineraryRestriction // geo/LayerRank.ItineraryRestriction|null[0]",
                "    final val entries // geo/LayerRank.entries|#static{}entries[0]",
                "        final fun <get-entries>(): EnumEntries // geo/LayerRank.entries.<get-entries>|[0]",
                "}",
            ).joinToString("\n")
        val jvm =
            listOf(
                "public final class com/mountaya/client/Mountaya\$Companion {",
                "\tpublic final fun getLanguage ()Ljava/lang/String;",
                "\tpublic static synthetic fun constructWebhookEvent\$default (Ljava/lang/String;)V",
                "\tpublic static final field SCHEMA_VERSION Ljava/lang/String;",
                "}",
            ).joinToString("\n")

        assertEquals(setOf("LayerRank", "ItineraryRestriction", "entries"), ChangelogCheck.publishedNames(klib))
        assertEquals(
            setOf("Mountaya", "Companion", "getLanguage", "language", "constructWebhookEvent", "SCHEMA_VERSION"),
            ChangelogCheck.publishedNames(jvm),
        )
    }

    @Test
    fun aMemberWhoseJvmNameKotlinManglesForAValueClassIsPublishedUnderItsDeclaredName() {
        val android =
            listOf(
                "public final class com/mountaya/elements/GlyphKt {",
                "\tpublic static final fun Glyph-NKG1LGE (Ljava/lang/String;FJLandroidx/compose/runtime/Composer;II)V",
                "\tpublic static final fun activityMarkerGlyph-q8Fr0-I (Ljava/lang/String;)Ljava/lang/String;",
                "}",
            ).joinToString("\n")

        assertEquals(setOf("GlyphKt", "Glyph", "activityMarkerGlyph"), ChangelogCheck.publishedNames(android))
    }

    @Test
    fun aCompositionLocalIsPublishedUnderItsNameInUpperCamelCaseAndNoOtherPropertyIs() {
        val android =
            listOf(
                "public final class com/mountaya/elements/LocalMountayaScopeKt {",
                "\tpublic static final fun getLocalMountayaScope ()Landroidx/compose/runtime/ProvidableCompositionLocal;",
                "\tpublic static final fun getStandardScope ()Lcom/mountaya/geo/ElementsScope;",
                "}",
            ).joinToString("\n")

        assertEquals(
            setOf(
                "LocalMountayaScopeKt",
                "getLocalMountayaScope",
                "localMountayaScope",
                "LocalMountayaScope",
                "getStandardScope",
                "standardScope",
            ),
            ChangelogCheck.publishedNames(android),
        )
    }

    private companion object {
        const val GROUP = "com.mountaya"
    }
}
