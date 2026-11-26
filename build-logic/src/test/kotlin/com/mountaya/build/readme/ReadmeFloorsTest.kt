package com.mountaya.build.readme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReadmeFloorsTest {
    private val floors =
        mapOf("kotlin-floor" to "2.4", "jvm-target" to "17", "android-minSdk" to "23", "android-compileSdk" to "37")

    /** A README whose floors table states each floor once, at [kotlin] for the Kotlin floor, with prose around it. */
    private fun readme(
        kotlin: String = "2.4",
        rows: List<String> = emptyList(),
        after: String = "",
    ): String =
        (
            listOf(
                "# The SDK",
                "",
                "## Install",
                "",
                "A consumer's build meets these floors:",
                "",
                "| Floor | Version |",
                "| --- | --- |",
                "| The Kotlin a consumer compiles with | Kotlin $kotlin or newer |",
                "| The Java its jvm and Android code targets | Java 17 or newer |",
                "| The API level an Android app runs from | `minSdk 23` or higher |",
                "| The API level an Android app compiles against | `compileSdk 37` or higher |",
            ) + rows + listOf("", "## A first call", "", after)
        ).joinToString("\n")

    @Test
    fun aTableStatingEachFloorOnceAtTheCatalogsFigureHasNoProblem() {
        assertEquals(emptyList(), ReadmeFloors.problems("README.md", readme(), floors))
    }

    @Test
    fun aRowStatingAnotherFigureFailsAtItsLine() {
        assertEquals(
            listOf(
                "README.md, line 9: `Kotlin 2.3 or newer` states the Kotlin a consumer compiles with as 2.3, and the " +
                    "version catalog's `kotlin-floor` is 2.4.",
            ),
            ReadmeFloors.problems("README.md", readme(kotlin = "2.3"), floors),
        )
    }

    /**
     * The table is the README's one copy of each floor, so a floor restated in prose fails even at the catalog's figure.
     */
    @Test
    fun aFloorStatedOutsideTheTableFailsWhereItStands() {
        val restated = readme(after = "Your module sets\n`compileSdk = 37` too.")

        assertEquals(
            listOf(
                "README.md, line 17: `compileSdk = 37` states the API level an app compiles against outside the " +
                    "floors table at line 7, its one copy in README.md: state it on the table's row alone.",
            ),
            ReadmeFloors.problems("README.md", restated, floors),
        )
    }

    @Test
    fun aFloorTheTableStatesNowhereOrTwiceFails() {
        val missing = readme().replace("| The Java its jvm and Android code targets | Java 17 or newer |\n", "")
        val twice = readme(rows = listOf("| The API level an app builds with | `minSdk 23` or higher |"))

        assertEquals(
            listOf(
                "README.md's floors table states nowhere the Java a consumer's jvm and Android code targets: the " +
                    "version catalog's `jvm-target` is 17, which a row states as `Java 17 or newer`.",
            ),
            ReadmeFloors.problems("README.md", missing, floors),
        )
        assertEquals(
            listOf(
                "README.md's floors table states the API level the Android modules run from 2 times, at lines 11 " +
                    "and 13: one row states it.",
            ),
            ReadmeFloors.problems("README.md", twice, floors),
        )
    }

    @Test
    fun aReadmeWithNoFloorsTableOrTwoFails() {
        val none = readme().replace(ReadmeFloors.HEADER, "| Requirement | Version |")
        val two = readme(after = "${ReadmeFloors.HEADER}\n| --- | --- |")

        assertEquals(
            listOf(
                "README.md holds no floors table, the `| Floor | Version |` table that states each floor a " +
                    "consumer's build meets once, as the version catalog holds it: `Kotlin 2.4 or newer`, " +
                    "`Java 17 or newer`, `from API level 23`, `against API level 37`.",
            ),
            ReadmeFloors.problems("README.md", none, floors),
        )
        assertEquals(
            listOf("README.md holds 2 floors tables, at lines 7 and 16: one table states each floor once."),
            ReadmeFloors.problems("README.md", two, floors),
        )
    }

    @Test
    fun aFloorTheBuildDoesNotHandIsRefused() {
        assertFailsWith<IllegalArgumentException> {
            ReadmeFloors.problems("README.md", readme(), floors - "jvm-target")
        }
    }
}
