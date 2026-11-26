package com.mountaya.build

import org.gradle.api.provider.MapProperty

/**
 * MountayaBuildExtension is what the root build file states for the gates: the exemptions, the scopes and the module
 * each registry entry runs in.
 *
 * An exemption or a scope is keyed by what it exempts or scopes and valued by the sentence that
 * justifies it, and an ownership entry is keyed by layer and valued by a module. All of them are
 * written in the root build file, in one place, so a reviewer sees each one move.
 */
abstract class MountayaBuildExtension {
    /** Names the naming law refuses and this repository keeps, each with its sentence. */
    abstract val namingExemptions: MapProperty<String, String>

    /**
     * Declarations a consumer reaches that carry no KDoc, each keyed by its path below its package, as
     * `lintDocumentation` prints it, and valued by its sentence (`SPEC.md` section 7).
     */
    abstract val documentationExemptions: MapProperty<String, String>

    /** Main files with no test at the mirrored path, each with its sentence. */
    abstract val untestedFiles: MapProperty<String, String>

    /** Files that declare a package other than their module's, each with its sentence (`SPEC.md` section 6). */
    abstract val packageExemptions: MapProperty<String, String>

    /**
     * Every `@Suppress`, `@SuppressLint` and `@SuppressWarnings` in the repository, keyed as `checkSuppressions`
     * prints it.
     */
    abstract val suppressions: MapProperty<String, String>

    /** Layers this SDK does not carry, each valued by the title of its `SPEC.md` section 11 bullet. */
    abstract val scopedLayers: MapProperty<String, String>

    /** Capabilities this SDK does not carry, each valued by the title of its section 11 bullet. */
    abstract val scopedCapabilities: MapProperty<String, String>

    /** The module that runs each layer's `api: true` entries, keyed by layer (`SPEC.md` section 9). */
    abstract val apiRunnerModules: MapProperty<String, String>

    /** The module that runs each layer's `api: false` entries, keyed by layer (`SPEC.md` section 9). */
    abstract val internalRunnerModules: MapProperty<String, String>
}
