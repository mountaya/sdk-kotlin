package com.mountaya.build.gates

import java.io.File

/**
 * SourceTree is one source directory of one module, as the Gradle model names it.
 *
 * The gates never keep a list of modules or source sets by hand: every tree they walk is read
 * from the build, so a module added tomorrow is scanned tomorrow.
 */
data class SourceTree(
    /** The module's project name, such as `mountaya-client`. */
    val module: String,
    /** The source set's name, such as `commonMain`. */
    val sourceSet: String,
    /** Whether the source set holds tests. */
    val isTest: Boolean,
    /** The source directory. */
    val directory: File,
) {
    /** The directory every hand-written file of this module sits under, its topic and its kind below it. */
    val base: List<String> = listOf("com", "mountaya", module.removePrefix("mountaya-").replace("-", ""))

    /** The one package every hand-written file of this module declares, whatever directory it sits in. */
    val modulePackage: String = base.joinToString(".")

    /** Encodes this tree as one line, the form a task input carries it in. */
    fun encode(): String = listOf(module, sourceSet, isTest.toString(), directory.path).joinToString(SEPARATOR)

    /** The line form a tree is carried in. */
    companion object {
        private const val SEPARATOR = "|"

        /** Decodes a line written by [encode]. */
        fun decode(line: String): SourceTree {
            val parts = line.split(SEPARATOR, limit = 4)
            require(parts.size == 4) { "Not a source tree: $line" }
            return SourceTree(parts[0], parts[1], parts[2].toBoolean(), File(parts[3]))
        }
    }
}

/**
 * SourceFile is one Kotlin file of a [SourceTree], with the path segments below the tree's directory.
 *
 * Every law the gates hold reads where a file sits, never the package it declares: the directory
 * names the module, the topic and the kind, and the package is the module's (SPEC section 6).
 */
data class SourceFile(
    /** The tree the file is under. */
    val tree: SourceTree,
    /** The file. */
    val file: File,
) {
    /** The path below the source directory, one entry per segment, the file name last. */
    val segments: List<String> = file.relativeTo(tree.directory).invariantSeparatorsPath.split("/")

    /** The path a message names the file by: the module, the source set, then the path below the source directory. */
    val display: String = "${tree.module}/${tree.sourceSet}/${segments.joinToString("/")}"

    /**
     * The directory the file sits in, read with dots: `com.mountaya.client.seams.engines` for a file of the
     * client's `seams` topic, filed under `engines`. It is the path a boundary names a topic or a kind by.
     */
    val place: String = segments.dropLast(1).joinToString(".")

    /** Whether the generator laid this file out, which puts it outside every law the gates hold. */
    val isGenerated: Boolean = GENERATED in segments

    /** The segments below the module's base directory, or null when the file is not under it. */
    val belowBase: List<String>? =
        if (segments.size > tree.base.size && segments.subList(0, tree.base.size) == tree.base) {
            segments.subList(tree.base.size, segments.size)
        } else {
            null
        }

    /** The topic the file belongs to: the directory directly below the module's own. */
    val topic: String? = belowBase?.takeIf { it.size >= 2 }?.first()

    /** The directory the generated files sit in. */
    companion object {
        /** The name of the directory the generator lays its files out in. */
        const val GENERATED = "generated"
    }
}

/** kotlinFiles lists every Kotlin file under each tree, in a stable order. */
fun List<SourceTree>.kotlinFiles(): List<SourceFile> =
    flatMap { tree ->
        tree.directory
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .sortedBy { it.invariantSeparatorsPath }
            .map { SourceFile(tree, it) }
            .toList()
    }
