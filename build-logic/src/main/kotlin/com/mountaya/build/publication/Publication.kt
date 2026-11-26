package com.mountaya.build.publication

import com.mountaya.build.licence.LicenceCheck
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Publication is what every module's published metadata states beside its coordinates and its licence (SPEC section
 * 10): the project's page, who publishes it and where its sources are kept. Maven Central refuses a publication whose
 * POM leaves one of them out, so [problems] holds every POM the build writes to them in the gate, long before a release
 * would find out.
 */
object Publication {
    /** The group every module is published under, the namespace Maven Central verifies for mountaya.com. */
    const val GROUP = "com.mountaya"

    /** The project's page: the SDK's documentation, which every POM's `url` names. */
    const val PAGE = "https://docs.mountaya.com/en/sdks/kotlin/"

    /** The publisher's site, which every POM names as its developer's organization. */
    const val URL = "https://mountaya.com"

    /** The publisher, named as the one developer of every publication. */
    const val DEVELOPER_ID = "mountaya"

    /** The publisher's name. */
    const val DEVELOPER_NAME = "Mountaya"

    /** Where a consumer reaches the publisher, the address the TypeScript packages give for their issues. */
    const val DEVELOPER_EMAIL = "support@mountaya.com"

    /**
     * The repository's public, read-only mirror on GitHub, which Maven Central's page links as the source. The
     * repository itself is on Entire, where a contributor clones it, and a Depot workflow pushes every push of `main`
     * and every release tag from there to the mirror.
     */
    const val SCM_URL = "https://github.com/mountaya/sdk-kotlin"

    /** The repository, as an SCM connection. */
    const val SCM_CONNECTION = "scm:git:$SCM_URL"

    /** Returns the README every javadoc jar of [module] holds in place of the Javadoc a Kotlin module has none of. */
    fun javadocReadme(
        module: String,
        version: String,
    ): String =
        """
        # $module $version

        This jar holds no Javadoc: $module is written in Kotlin, and the documentation of its API is the
        KDoc of every public declaration, in the sources jar published beside this one.

        It is published under the ${LicenceCheck.NAME}: ${LicenceCheck.URL}
        """.trimIndent() + "\n"

    /** Returns every element Maven Central requires that [pom] leaves out or leaves blank. */
    fun problems(pom: String): List<String> {
        val project =
            runCatching { parse(pom) }.getOrElse { return listOf("It is no XML document: ${it.message}") }
        val problems = mutableListOf<String>()
        for (element in listOf("name", "description", "url")) {
            if (project.text(element).isBlank()) problems += "It names no <$element>."
        }
        val licences = project.child("licenses")?.children("license").orEmpty()
        if (licences.isEmpty()) problems += "It names no licence."
        for (licence in licences) {
            if (licence.text("name").isBlank()) problems += "A licence has no <name>."
            if (licence.text("url").isBlank()) problems += "The licence ${licence.text("name")} has no <url>."
        }
        val developers = project.child("developers")?.children("developer").orEmpty()
        if (developers.none { it.text("name").isNotBlank() }) problems += "It names no developer."
        val scm = project.child("scm")
        if (scm == null || scm.text("url").isBlank() || scm.text("connection").isBlank()) {
            problems += "It names no source repository: <scm> with its <url> and <connection>."
        }
        return problems
    }

    /**
     * Answers whether [archive], a jar or an AAR, holds [entry]: at its root, or in the `classes.jar` an AAR packs its
     * classes and their resources into.
     */
    fun carries(
        archive: File,
        entry: String,
    ): Boolean =
        ZipFile(archive).use { zip ->
            if (zip.getEntry(entry) != null) return@use true
            val classes = zip.getEntry("classes.jar") ?: return@use false
            ZipInputStream(zip.getInputStream(classes)).use { inner ->
                generateSequence { inner.nextEntry }.any { it.name == entry }
            }
        }

    /** Returns the POM's `<packaging>`, which is `jar` where the POM states none, as Maven reads it. */
    fun packaging(pom: String): String = runCatching { parse(pom).text("packaging") }.getOrDefault("").ifBlank { "jar" }

    private fun parse(pom: String): Element {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        return factory.newDocumentBuilder().parse(InputSource(StringReader(pom))).documentElement
    }

    private fun Element.children(name: String): List<Element> {
        val nodes = childNodes
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }.filter { it.tagName == name }
    }

    private fun Element.child(name: String): Element? = children(name).firstOrNull()

    private fun Element.text(name: String): String = child(name)?.textContent?.trim().orEmpty()
}
