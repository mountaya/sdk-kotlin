package com.mountaya.build.publication

import org.gradle.api.Project
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/**
 * ReleaseRepository is the release repository of SPEC section 10: the Maven repository `mise run release` publishes
 * every module into, signed, before it zips it into the one bundle the Central Portal is handed. [problems] holds it to
 * what the Portal validates, so a bundle the Portal would refuse is refused on this machine first.
 */
object ReleaseRepository {
    /** The name of the repository every published module declares, which names its publishing tasks. */
    const val NAME = "release"

    /** Where a release is published: `-Pmountaya.releaseRepository`, or `build/release/maven` at the root. */
    const val DIRECTORY_PROPERTY = "mountaya.releaseRepository"

    /** Where its bundle is written: `-Pmountaya.releaseBundle`, or `build/release/central-bundle.zip`. */
    const val BUNDLE_PROPERTY = "mountaya.releaseBundle"

    /**
     * `-Pmountaya.unsigned=true` publishes unsigned, which a dry run without a key asks for and a
     * release never does.
     */
    const val UNSIGNED_PROPERTY = "mountaya.unsigned"

    /** The variable holding the ASCII-armoured secret key every publication is signed with. */
    const val SIGNING_KEY = "MOUNTAYA_SIGNING_KEY"

    /** The variable naming the key's signing subkey, where the primary key does not sign. */
    const val SIGNING_KEY_ID = "MOUNTAYA_SIGNING_KEY_ID"

    /** The variable holding the key's passphrase. */
    const val SIGNING_PASSWORD = "MOUNTAYA_SIGNING_PASSWORD"

    /** The checksums Maven Central requires beside every file but a signature. */
    val CHECKSUMS: List<String> = listOf("md5", "sha1")

    /**
     * What the bundle leaves out, since the Portal reads none of it: the repository's own metadata,
     * the checksums of a signature, and the longer checksums.
     */
    val UNREAD: List<String> = listOf("**/maven-metadata.xml*", "**/*.asc.*", "**/*.sha256", "**/*.sha512")

    private const val SIGNATURE = "-----BEGIN PGP SIGNATURE-----"

    private val SIDE_FILES = listOf(".asc", ".md5", ".sha1", ".sha256", ".sha512")

    /** Returns the release repository [project]'s build publishes into. */
    fun directory(project: Project): File =
        (project.findProperty(DIRECTORY_PROPERTY) as String?)?.let { project.rootProject.file(it) }
            ?: project.rootProject.layout.buildDirectory
                .dir("release/maven")
                .get()
                .asFile

    /** Returns the bundle [project]'s build zips the release repository into. */
    fun bundle(project: Project): File =
        (project.findProperty(BUNDLE_PROPERTY) as String?)?.let { project.rootProject.file(it) }
            ?: project.rootProject.layout.buildDirectory
                .file("release/central-bundle.zip")
                .get()
                .asFile

    /** Answers whether [project]'s build signs what it publishes, which it does unless a dry run asks otherwise. */
    fun signed(project: Project): Boolean = (project.findProperty(UNSIGNED_PROPERTY) as String?)?.toBoolean() != true

    /** Returns the directories of the machine building [project] that no published file may name ([machinePaths]). */
    fun machinePaths(project: Project): List<String> =
        machinePaths(project.rootDir.absolutePath, System.getProperty("user.home").orEmpty())

    /**
     * Returns the directories no published file may name: the repository's [root] and the user's [home], each where it
     * is deep enough, two segments or more, to be told from a word a file holds for its own reasons (`/root` is not).
     */
    fun machinePaths(
        root: String,
        home: String,
    ): List<String> = listOf(root, home).filter { path -> path.count { it == '/' } >= 2 }.distinct()

    /**
     * Returns every way [root] falls short of a deployment the Central Portal validates: [modules] each published under
     * [group] at [version] with its Gradle module metadata, no other version beside it, every POM stating what
     * [Publication.problems] asks, a sources and a javadoc jar beside every artifact that is not a POM alone, beside
     * every file its checksums and, where [signed], its signature, and no file, nor any archive inside one, naming one
     * of [machinePaths].
     */
    fun problems(
        root: File,
        group: String,
        version: String,
        modules: Set<String>,
        signed: Boolean,
        machinePaths: List<String> = emptyList(),
    ): List<String> {
        val directory = File(root, group.replace('.', '/'))
        if (!directory.isDirectory) return listOf("Nothing is published under $group in $root.")
        val problems = mutableListOf<String>()
        val artifacts =
            directory
                .listFiles()
                .orEmpty()
                .filter { it.isDirectory }
                .sortedBy { it.name }
        for (module in modules.sorted()) {
            if (!File(directory, "$module/$version/$module-$version.module").isFile) {
                problems += "$module $version is not published with its Gradle module metadata."
            }
        }
        for (artifact in artifacts) {
            val versions =
                artifact
                    .listFiles()
                    .orEmpty()
                    .filter { it.isDirectory }
                    .map { it.name }
                    .sorted()
            if (versions != listOf(version)) {
                val held = versions.joinToString(", ")
                problems += "${artifact.name} holds $held, where the release is $version alone."
            }
            val published = File(artifact, version).takeIf { it.isDirectory } ?: continue
            problems += artifactProblems(published, artifact.name, version, signed, machinePaths)
        }
        return problems
    }

    private fun artifactProblems(
        directory: File,
        artifact: String,
        version: String,
        signed: Boolean,
        machinePaths: List<String>,
    ): List<String> {
        val problems = mutableListOf<String>()
        val name = "$artifact $version"
        val pom = File(directory, "$artifact-$version.pom")
        if (!pom.isFile) return listOf("$name has no POM.")
        val text = pom.readText()
        problems += Publication.problems(text).map { "$name: its POM: $it" }
        if (Publication.packaging(text) != "pom") {
            for (classifier in listOf("sources", "javadoc")) {
                if (!File(directory, "$artifact-$version-$classifier.jar").isFile) {
                    problems += "$name has no $classifier jar."
                }
            }
        }
        val files = directory.listFiles().orEmpty().filter { it.isFile && SIDE_FILES.none(it.name::endsWith) }
        for (file in files.sortedBy { it.name }) {
            for (algorithm in CHECKSUMS) {
                val checksum = File(directory, "${file.name}.$algorithm")
                val expected = digest(file, algorithm)
                val stated =
                    checksum
                        .takeIf { it.isFile }
                        ?.readText()
                        ?.trim()
                        ?.substringBefore(' ')
                if (stated == null) {
                    problems += "${file.name} has no .$algorithm checksum."
                } else if (!stated.equals(expected, ignoreCase = true)) {
                    problems += "${file.name}.$algorithm states $stated, where the file's is $expected."
                }
            }
            val signature = File(directory, "${file.name}.asc")
            if (signed && !(signature.isFile && signature.readText().startsWith(SIGNATURE))) {
                problems += "${file.name} has no signature."
            }
            val named = namedPath(file.readBytes(), machinePaths)
            if (named != null) problems += "${file.name} names $named, a directory of the machine that built it."
        }
        return problems
    }

    /** The first of [paths] that [bytes] hold, read through every archive inside them too. */
    private fun namedPath(
        bytes: ByteArray,
        paths: List<String>,
    ): String? {
        paths.firstOrNull { holds(bytes, it.toByteArray()) }?.let { return it }
        if (paths.isEmpty() || !isArchive(bytes)) return null
        ZipInputStream(ByteArrayInputStream(bytes)).use { archive ->
            while (true) {
                val entry = archive.nextEntry ?: break
                if (!entry.isDirectory) namedPath(archive.readBytes(), paths)?.let { return it }
            }
        }
        return null
    }

    /** Whether [bytes] open as a zip archive does, as a jar, an AAR and a klib do. */
    private fun isArchive(bytes: ByteArray): Boolean =
        bytes.size > 3 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte() &&
            bytes[2] == 3.toByte() && bytes[3] == 4.toByte()

    private fun holds(
        bytes: ByteArray,
        sequence: ByteArray,
    ): Boolean {
        if (sequence.isEmpty() || sequence.size > bytes.size) return false
        return (0..bytes.size - sequence.size).any { start ->
            sequence.indices.all { bytes[start + it] == sequence[it] }
        }
    }

    private fun digest(
        file: File,
        algorithm: String,
    ): String {
        val name = if (algorithm == "sha1") "SHA-1" else algorithm.uppercase()
        return MessageDigest
            .getInstance(name)
            .digest(file.readBytes())
            .joinToString("") { "%02x".format(it) }
    }
}
