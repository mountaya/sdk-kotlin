package com.mountaya.build.publication

import com.mountaya.build.licence.LicenceCheck
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReleaseRepositoryTest {
    @Test
    fun aRepositoryHoldingEveryModuleCompleteSignedAndCheckedHasNothingToSay() {
        val root = repository()

        assertEquals(emptyList(), ReleaseRepository.problems(root, GROUP, VERSION, MODULES, signed = true))
    }

    @Test
    fun aFileWithoutItsSignatureIsRefusedUnlessTheRepositoryIsUnsigned() {
        val root = repository()
        File(root, "com/mountaya/mountaya-geo-jvm/0.1.0/mountaya-geo-jvm-0.1.0.jar.asc").delete()
        File(root, "com/mountaya/mountaya-geo/0.1.0/mountaya-geo-0.1.0.module.asc").writeText("not a signature")

        assertEquals(
            listOf("mountaya-geo-0.1.0.module has no signature.", "mountaya-geo-jvm-0.1.0.jar has no signature."),
            ReleaseRepository.problems(root, GROUP, VERSION, MODULES, signed = true),
        )
        assertEquals(emptyList(), ReleaseRepository.problems(root, GROUP, VERSION, MODULES, signed = false))
    }

    @Test
    fun aChecksumThatIsMissingOrStatesAnotherFileIsRefused() {
        val root = repository()
        val directory = File(root, "com/mountaya/mountaya-geo-jvm/0.1.0")
        File(directory, "mountaya-geo-jvm-0.1.0.jar.md5").delete()
        File(directory, "mountaya-geo-jvm-0.1.0-sources.jar.sha1").writeText("0123456789")

        val problems = ReleaseRepository.problems(root, GROUP, VERSION, MODULES, signed = true)

        assertEquals(2, problems.size, "$problems")
        assertEquals("mountaya-geo-jvm-0.1.0.jar has no .md5 checksum.", problems[1])
        val stated = "mountaya-geo-jvm-0.1.0-sources.jar.sha1 states 0123456789, where"
        assertTrue(problems[0].startsWith(stated), "$problems")
    }

    @Test
    fun anArtifactWithoutItsSourcesOrItsJavadocIsRefusedAndAPomAloneNeedsNeither() {
        val root = repository()
        File(root, "com/mountaya/mountaya-geo-jvm/0.1.0/mountaya-geo-jvm-0.1.0-javadoc.jar").delete()
        publish(root, "mountaya-bom", "pom", files = emptyList())

        assertEquals(
            listOf("mountaya-geo-jvm 0.1.0 has no javadoc jar."),
            ReleaseRepository.problems(root, GROUP, VERSION, MODULES, signed = true),
        )
    }

    @Test
    fun anotherVersionBesideTheReleaseOrAModuleWithoutItsMetadataIsRefused() {
        val root = repository()
        File(root, "com/mountaya/mountaya-geo-jvm/0.0.9").mkdirs()

        assertEquals(
            listOf(
                "mountaya-client 0.1.0 is not published with its Gradle module metadata.",
                "mountaya-geo-jvm holds 0.0.9, 0.1.0, where the release is 0.1.0 alone.",
            ),
            ReleaseRepository.problems(root, GROUP, VERSION, MODULES + "mountaya-client", signed = true),
        )
    }

    @Test
    fun aPomMissingWhatMavenCentralRequiresIsRefusedNamingItsArtifact() {
        val root = repository()
        val pom = File(root, "com/mountaya/mountaya-geo/0.1.0/mountaya-geo-0.1.0.pom")
        pom.writeText(pom.readText().replace(Regex("<developers>.*</developers>"), ""))
        sign(pom)

        assertEquals(
            listOf("mountaya-geo 0.1.0: its POM: It names no developer."),
            ReleaseRepository.problems(root, GROUP, VERSION, MODULES, signed = true),
        )
        assertEquals(
            listOf("Nothing is published under com.mountaya in ${root.parentFile}."),
            ReleaseRepository.problems(root.parentFile, GROUP, VERSION, MODULES, signed = true),
        )
    }

    @Test
    fun aFileNamingADirectoryOfTheMachineThatBuiltItIsRefusedAnArchiveInsideItRead() {
        val root = repository()
        val machine = "/home/builder/sdk-kotlin"
        val directory = File(root, "com/mountaya/mountaya-geo-jvm/0.1.0")
        // A source's absolute path, as a klib's string table records it where no base is given.
        val jar = File(directory, "mountaya-geo-jvm-0.1.0.jar")
        jar.writeBytes(zip("Geo.class" to "$machine/mountaya-geo/src/Geo.kt".toByteArray()))
        sign(jar)
        // An archive inside an archive, as an AAR holds its classes.jar.
        val sources = File(directory, "mountaya-geo-jvm-0.1.0-sources.jar")
        sources.writeBytes(zip("classes.jar" to zip("Map.class" to "built in $machine".toByteArray())))
        sign(sources)

        val named = "names $machine, a directory of the machine that built it."
        assertEquals(
            listOf("mountaya-geo-jvm-0.1.0-sources.jar $named", "mountaya-geo-jvm-0.1.0.jar $named"),
            ReleaseRepository.problems(root, GROUP, VERSION, MODULES, signed = true, machinePaths = listOf(machine)),
        )
        val elsewhere = listOf("/home/someone-else")
        assertEquals(emptyList(), ReleaseRepository.problems(root, GROUP, VERSION, MODULES, true, elsewhere))
    }

    @Test
    fun theMachinesDirectoriesAreItsRootAndItsHomeEachDeeperThanOneSegment() {
        assertEquals(
            listOf("/home/builder/sdk-kotlin", "/home/builder"),
            ReleaseRepository.machinePaths("/home/builder/sdk-kotlin", "/home/builder"),
        )
        assertEquals(listOf("/work/sdk-kotlin"), ReleaseRepository.machinePaths("/work/sdk-kotlin", "/root"))
    }

    @Test
    fun whatTheBundleLeavesOutIsWhatThePortalNeverReads() {
        assertEquals(listOf("md5", "sha1"), ReleaseRepository.CHECKSUMS)
        assertTrue(ReleaseRepository.UNREAD.none { it.endsWith(".md5") || it.endsWith(".sha1") || it.endsWith(".asc") })
    }

    /** A repository holding `mountaya-geo` as a multiplatform build publishes it: its root and its jvm variant. */
    private fun repository(): File {
        val root = File(Files.createTempDirectory("release").toFile(), "maven")
        publish(root, "mountaya-geo", "jar", listOf(".module", ".jar", "-sources.jar", "-javadoc.jar"))
        publish(root, "mountaya-geo-jvm", "jar", listOf(".module", ".jar", "-sources.jar", "-javadoc.jar"))
        return root
    }

    private fun publish(
        root: File,
        artifact: String,
        packaging: String,
        files: List<String>,
    ) {
        val directory = File(root, "com/mountaya/$artifact/$VERSION").apply { mkdirs() }
        val pom = File(directory, "$artifact-$VERSION.pom")
        pom.writeText(
            """
            <project><name>$artifact</name><description>Geo.</description><url>${Publication.URL}</url>
            <packaging>$packaging</packaging>
            <licenses><license><name>${LicenceCheck.NAME}</name><url>${LicenceCheck.URL}</url></license></licenses>
            <developers><developer><name>${Publication.DEVELOPER_NAME}</name></developer></developers>
            <scm><connection>${Publication.SCM_CONNECTION}</connection><url>${Publication.SCM_URL}</url></scm></project>
            """.trimIndent(),
        )
        sign(pom)
        for (suffix in files) sign(File(directory, "$artifact-$VERSION$suffix").apply { writeText("$artifact$suffix") })
        File(directory, "../maven-metadata.xml").writeText("<metadata/>")
    }

    /** Writes [file]'s checksums and a signature beside it. */
    private fun sign(file: File) {
        for ((extension, algorithm) in listOf("md5" to "MD5", "sha1" to "SHA-1")) {
            val bytes = MessageDigest.getInstance(algorithm).digest(file.readBytes())
            val digest = bytes.joinToString("") { "%02x".format(it) }
            File(file.parentFile, "${file.name}.$extension").writeText(digest)
        }
        val signature = "-----BEGIN PGP SIGNATURE-----\n\n-----END PGP SIGNATURE-----\n"
        File(file.parentFile, "${file.name}.asc").writeText(signature)
    }

    /** A zip archive holding [entries], as a jar, an AAR or a klib is one. */
    private fun zip(vararg entries: Pair<String, ByteArray>): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { archive ->
            for ((name, content) in entries) {
                archive.putNextEntry(ZipEntry(name))
                archive.write(content)
                archive.closeEntry()
            }
        }
        return bytes.toByteArray()
    }

    private companion object {
        const val GROUP = "com.mountaya"
        const val VERSION = "0.1.0"
        val MODULES = setOf("mountaya-geo")
    }
}
