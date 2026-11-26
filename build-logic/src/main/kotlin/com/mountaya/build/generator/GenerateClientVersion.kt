package com.mountaya.build.generator

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * GenerateClientVersion prints the version every request names this SDK by,
 * `X-Mountaya-Client: mountaya-kotlin/<version>` (`sdk/SPEC.md` section 1), from the version catalog's `mountaya`,
 * the one place the version is written, so the header never names a version the modules are not published at.
 *
 * It prints into a source directory of the build's own, which `mountaya-client`'s common code compiles
 * with, and never into the source tree: every build prints it again from the catalog, so there is no
 * copy for a gate to hold to its source.
 */
@CacheableTask
abstract class GenerateClientVersion : DefaultTask() {
    /** The catalog's `mountaya`, the version every module is published at. */
    @get:Input
    abstract val sdkVersion: Property<String>

    /** The package the constant is declared in. */
    @get:Input
    abstract val packageName: Property<String>

    /** The source directory the file is printed under. */
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    /** Prints [FILE], and nothing else, under [output]. */
    @TaskAction
    fun print() {
        val root = output.get().asFile
        root.deleteRecursively()
        val file = File(root, "${packageName.get().replace('.', '/')}/$FILE")
        file.parentFile.mkdirs()
        file.writeText(source(packageName.get(), sdkVersion.get()))
    }

    /** The printed file and its source. */
    companion object {
        /** The file the constant is printed in. */
        const val FILE = "ClientVersion.kt"

        /** A version a header carries and Maven publishes under: letters, digits, dots, plus and minus signs. */
        private val VERSION = Regex("""^[0-9A-Za-z][0-9A-Za-z.+-]*$""")

        /** Returns the source of [FILE]: [version] as the `internal` constant `CLIENT_VERSION` of [packageName]. */
        fun source(
            packageName: String,
            version: String,
        ): String {
            require(VERSION.matches(version)) {
                "The version catalog's `mountaya` is \"$version\", which no header carries as a version."
            }
            return buildString {
                append("// GENERATED FILE. DO NOT EDIT.\n")
                append("//\n")
                append("// Printed by `generateClientVersion` from `mountaya` in gradle/libs.versions.toml,\n")
                append("// which every build prints again.\n")
                append('\n')
                append("package ").append(packageName).append('\n')
                append('\n')
                append("/** The version every module of this SDK is published at, the catalog's `mountaya`. */\n")
                append("internal const val CLIENT_VERSION: String = ").append(KotlinText.literal(version)).append('\n')
            }
        }
    }
}
