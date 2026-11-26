package com.mountaya.build.publication

import com.mountaya.build.licence.LicenceCheck
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PublicationTest {
    @Test
    fun aPomStatingEveryElementMavenCentralRequiresHasNothingToSay() {
        assertEquals(emptyList(), Publication.problems(pom()))
    }

    @Test
    fun eachElementLeftOutOrLeftBlankIsNamed() {
        val pom =
            pom()
                .replace("<description>What Mountaya decides about its data.</description>", "")
                .replace("<url>https://mountaya.com</url>", "<url> </url>")
                .replace("<url>${LicenceCheck.URL}</url>", "")
                .replace(Regex("<developers>.*</developers>"), "")
                .replace(Regex("<connection>[^<]*</connection>"), "")

        assertEquals(
            listOf(
                "It names no <description>.",
                "It names no <url>.",
                "The licence ${LicenceCheck.NAME} has no <url>.",
                "It names no developer.",
                "It names no source repository: <scm> with its <url> and <connection>.",
            ),
            Publication.problems(pom),
        )
    }

    @Test
    fun theProjectsOwnNameIsReadAndNotTheNameOfItsLicenceOrItsDeveloper() {
        val problems = Publication.problems(pom().replace("<name>mountaya-geo</name>", ""))

        assertEquals(listOf("It names no <name>."), problems)
        val unlicensed = pom().replace(Regex("<licenses>.*</licenses>"), "")
        assertEquals(listOf("It names no licence."), Publication.problems(unlicensed))
    }

    @Test
    fun aDocumentThatIsNoXmlIsSaidToBeNone() {
        val problems = Publication.problems("mountaya-geo 0.1.0")

        assertEquals(1, problems.size)
        assertTrue(problems.single().startsWith("It is no XML document"), "$problems")
    }

    @Test
    fun thePackagingIsJarWhereThePomStatesNone() {
        assertEquals("jar", Publication.packaging(pom()))
        assertEquals("klib", Publication.packaging(pom().replace("</url>\n", "</url>\n<packaging>klib</packaging>\n")))
        val aar = pom().replace("<modelVersion>", "<packaging>aar</packaging><modelVersion>")
        assertEquals("aar", Publication.packaging(aar))
    }

    @Test
    fun theJavadocReadmeNamesTheModuleItsVersionWhereItsApiIsDocumentedAndTheLicence() {
        val readme = Publication.javadocReadme("mountaya-geo", "0.1.0")

        assertTrue(readme.startsWith("# mountaya-geo 0.1.0\n"), readme)
        assertTrue("sources jar" in readme && LicenceCheck.URL in readme && LicenceCheck.NAME in readme, readme)
        assertTrue(readme.endsWith("\n") && !readme.endsWith("\n\n"), readme)
    }

    @Test
    fun theSourceRepositoryIsThePublicMirrorAsAConnectionToo() {
        assertEquals("https://github.com/mountaya/sdk-kotlin", Publication.SCM_URL)
        assertEquals("scm:git:${Publication.SCM_URL}", Publication.SCM_CONNECTION)
    }

    @Test
    fun anArchiveCarriesAnEntryAtItsRootOrInTheClassesJarAnAarPacks() {
        val entry = "META-INF/mountaya-geo/THIRD-PARTY-NOTICES.txt"
        val directory = Files.createTempDirectory("archives").toFile()
        val jar = File(directory, "geo.jar").apply { writeBytes(zip(entry to "ISC License".toByteArray())) }
        val aar =
            File(
                directory,
                "geo.aar",
            ).apply { writeBytes(zip("classes.jar" to zip(entry to "ISC".toByteArray()))) }
        val bare =
            File(
                directory,
                "bare.aar",
            ).apply { writeBytes(zip("classes.jar" to zip("a/B.class" to ByteArray(1)))) }

        assertTrue(Publication.carries(jar, entry))
        assertTrue(Publication.carries(aar, entry))
        assertTrue(!Publication.carries(bare, entry))
        assertTrue(!Publication.carries(jar, "META-INF/other.txt"))
    }

    /** A zip archive holding [entries], as a jar or an AAR is one. */
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

    /** A POM as the build writes one, every required element stated. */
    private fun pom(): String =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0">
        <modelVersion>4.0.0</modelVersion>
        <groupId>com.mountaya</groupId>
        <artifactId>mountaya-geo</artifactId>
        <version>0.1.0</version>
        <name>mountaya-geo</name>
        <description>What Mountaya decides about its data.</description>
        <url>https://mountaya.com</url>
        <licenses><license><name>${LicenceCheck.NAME}</name><url>${LicenceCheck.URL}</url></license></licenses>
        <developers><developer><id>mountaya</id><name>Mountaya</name></developer></developers>
        <scm><connection>${Publication.SCM_CONNECTION}</connection><url>${Publication.SCM_URL}</url></scm>
        </project>
        """.trimIndent()
}
