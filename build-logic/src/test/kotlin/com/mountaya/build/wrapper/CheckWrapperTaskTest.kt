package com.mountaya.build.wrapper

import kotlin.test.Test
import kotlin.test.assertEquals

class CheckWrapperTaskTest {
    private val checksum = "a".repeat(64)
    private val other = "b".repeat(64)

    private fun mise(
        pin: String = "9.7.1",
        taskVersion: String = pin,
        taskChecksum: String = checksum,
    ) = """
        [tools]
        gradle = "$pin"

        [tasks.wrapper]
        run = "gradle wrapper --gradle-version $taskVersion --distribution-type bin --gradle-distribution-sha256-sum $taskChecksum"
        """.trimIndent()

    private fun wrapper(
        version: String = "9.7.1",
        sum: String? = checksum,
    ) = listOfNotNull(
        "distributionBase=GRADLE_USER_HOME",
        sum?.let { "distributionSha256Sum=$it" },
        "distributionUrl=https\\://services.gradle.org/distributions/gradle-$version-bin.zip",
    ).joinToString("\n")

    @Test
    fun aWrapperThatQuotesThePinPasses() {
        assertEquals(emptyList(), CheckWrapperTask.problems(mise(), wrapper()))
    }

    @Test
    fun aWrapperOnAnotherGradleFails() {
        val problems = CheckWrapperTask.problems(mise(), wrapper(version = "9.6.0"))

        assertEquals(
            listOf("The wrapper downloads Gradle 9.6.0, and mise.toml pins 9.7.1. Run `mise run wrapper`."),
            problems,
        )
    }

    @Test
    fun aWrapperTaskThatDisagreesWithTheToolPinFails() {
        val problems = CheckWrapperTask.problems(mise(taskVersion = "9.6.0"), wrapper())

        assertEquals(listOf("mise.toml's wrapper task generates Gradle 9.6.0, and [tools] pins 9.7.1."), problems)
    }

    @Test
    fun aMissingOrDifferentChecksumFails() {
        assertEquals(
            listOf("The wrapper verifies no checksum of the distribution it downloads."),
            CheckWrapperTask.problems(mise(), wrapper(sum = null)),
        )
        assertEquals(
            listOf("The wrapper verifies checksum $other, and mise.toml's wrapper task writes $checksum."),
            CheckWrapperTask.problems(mise(), wrapper(sum = other)),
        )
    }
}
