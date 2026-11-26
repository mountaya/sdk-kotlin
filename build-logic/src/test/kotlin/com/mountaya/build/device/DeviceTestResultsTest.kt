package com.mountaya.build.device

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceTestResultsTest {
    private fun report(tests: Int) =
        """
        <?xml version='1.0' encoding='UTF-8' ?>
        <testsuites tests="$tests" failures="0" errors="0" skipped="0" time="0.000" hostname="localhost">
        </testsuites>
        """.trimIndent()

    private fun results(vararg files: Pair<String, String>): File {
        val directory = createTempDirectory("device-results").toFile()
        for ((path, text) in files) File(directory, path).apply { parentFile.mkdirs() }.writeText(text)
        return directory
    }

    @Test
    fun aRunCountsTheTestsEachOfItsReportsNames() {
        val directory =
            results(
                "androidMain/TEST-mountaya-api37(AVD) - 17.xml" to report(191),
                "androidMain/other/TEST-another-device.xml" to report(12),
            )

        assertEquals(203, DeviceTestResults.testsRun(directory, since = 0))
    }

    /** What AGP's test engine leaves when an APK never installed: a report of no test, or none. */
    @Test
    fun aRunWithNoReportOrAReportOfNoTestRanNone() {
        assertEquals(0, DeviceTestResults.testsRun(results("androidMain/TEST-device.xml" to report(0)), since = 0))
        assertEquals(0, DeviceTestResults.testsRun(results("androidMain/test-result.pb" to "\u0000"), since = 0))
        assertEquals(0, DeviceTestResults.testsRun(File(results(), "connected"), since = 0))
    }

    @Test
    fun aReportAnEarlierRunLeftIsNeverCounted() {
        val directory = results("androidMain/TEST-device.xml" to report(191))
        File(directory, "androidMain/TEST-device.xml").setLastModified(1_000_000L)

        assertEquals(0, DeviceTestResults.testsRun(directory, since = 2_000_000L))
        assertEquals(191, DeviceTestResults.testsRun(directory, since = 1_000_000L))
    }

    @Test
    fun onlyAJUnitReportCounts() {
        val directory =
            results(
                "androidMain/logcat-Test.txt" to report(5),
                "androidMain/TEST-device.json" to report(5),
            )

        assertEquals(0, DeviceTestResults.testsRun(directory, since = 0))
    }
}
