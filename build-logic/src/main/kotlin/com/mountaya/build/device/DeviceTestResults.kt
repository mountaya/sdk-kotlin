package com.mountaya.build.device

import java.io.File

/**
 * DeviceTestResults is what a device run leaves behind: the tests its JUnit reports name.
 *
 * AGP's test engine logs a test APK it could not install, or a device that stopped answering before a
 * run began, and still passes `connectedAndroidDeviceTest`, with a report naming no test or no report at
 * all. So each run is held to the report it wrote: one that names no test is a run that did not happen.
 */
object DeviceTestResults {
    private val SUITES = Regex("""<testsuites\b[^>]*?\btests="(\d+)"""")

    /**
     * Returns the tests named by the JUnit reports (`TEST-*.xml`) under [directory] that were written at or after
     * [since], in milliseconds since the epoch, so a report an earlier run left is never counted.
     */
    fun testsRun(
        directory: File,
        since: Long,
    ): Int =
        directory
            .walkTopDown()
            .filter { file -> file.isFile && isReport(file.name) && file.lastModified() >= since }
            .sumOf { report -> testsNamed(report.readText()) }

    /** Whether [name] is a JUnit report's, `TEST-*.xml`. */
    private fun isReport(name: String): Boolean = name.startsWith("TEST-") && name.endsWith(".xml")

    /** The tests the root of the JUnit report [text] names, or none where it names no count. */
    private fun testsNamed(text: String): Int =
        SUITES
            .find(text)
            ?.groupValues
            ?.get(1)
            ?.toInt() ?: 0
}
