package com.mountaya.build.gates

import java.io.File
import java.nio.file.Files

/** Builds throwaway source trees for a gate to run over, the way a module's `src/` holds them. */
class Trees {
    private val root: File = Files.createTempDirectory("gates").toFile()
    private val trees = mutableListOf<SourceTree>()

    /** Adds the source set [sourceSet] of [module], with [files] keyed by path below `kotlin/`. */
    fun add(
        module: String,
        sourceSet: String,
        files: Map<String, String>,
        emptyDirectories: List<String> = emptyList(),
    ): Trees {
        val directory = File(root, "$module/src/$sourceSet/kotlin")
        directory.mkdirs()
        for ((path, text) in files) {
            File(directory, path).apply {
                parentFile.mkdirs()
                writeText(text.trimIndent() + "\n")
            }
        }
        for (path in emptyDirectories) File(directory, path).mkdirs()
        trees += SourceTree(module, sourceSet, sourceSet.endsWith("Test"), directory)
        return this
    }

    /** The trees added so far. */
    fun build(): List<SourceTree> = trees.toList()
}
