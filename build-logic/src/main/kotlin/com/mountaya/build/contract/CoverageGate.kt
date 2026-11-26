package com.mountaya.build.contract

import com.mountaya.build.gates.GateResult

/**
 * CoverageGate is the half of the coverage pair that lives in an SDK.
 *
 * The contract repository holds its registry and its fixtures to each other. It cannot see this
 * repository's tests, so the other direction is held here, both ways: every capability has a
 * runner or a scope, and every runner and every scope answers to something the registry carries.
 * It cannot see this repository's API either, so the names a capability is published under are held
 * here too: each one the registry's `kotlin` column records for a capability this SDK does not scope
 * is declared by a module's committed ABI dumps, spelled exactly so ([PublishedNames]), and the column
 * is `scoped` exactly where this SDK scopes the capability.
 *
 * Which module runs an entry is read from the root build's table rather than from the modules,
 * so an entry of a module not yet in the build is listed with the work left, under that module's
 * name, and an entry of a layer or a flag the table names no module for fails on its own line.
 */
object CoverageGate {
    /** The task name the root build registers this gate under. */
    const val NAME = "checkConformanceCoverage"

    private const val DIVERGENCES = "## 11. Deliberate divergences"

    /**
     * Runs the gate. [registry] is null when the contract checkout is absent, which a
     * contributor's machine tolerates and [ci] does not. [manifests] are those of the modules in
     * the build; a module [ownership] names and no manifest comes from is not in the build yet.
     * [api] is what the modules' committed ABI dumps declare.
     */
    fun run(
        registry: ContractRegistry?,
        scopes: Scopes,
        ownership: Ownership,
        manifests: List<ConformanceManifest>,
        spec: String,
        ci: Boolean,
        api: ApiDeclarations,
    ): GateResult {
        if (registry == null) {
            val violations =
                if (ci) {
                    listOf("The contract checkout is absent. A green run here has to mean the fixtures ran.")
                } else {
                    emptyList()
                }
            return GateResult(NAME, 0, 0, violations, "$NAME: the contract checkout is absent, so nothing was compared")
        }

        val violations = mutableListOf<String>()

        // A scope is accepted only for an entry the divergences section names with its sentence.
        val divergences = spec.substringAfter(DIVERGENCES, "").substringBefore("\n## ")
        if (divergences.isBlank()) violations += "SPEC.md has no \"$DIVERGENCES\" section to hold a scope to."
        for (sentence in scopes.sentences) {
            if ("- **$sentence**" !in divergences) {
                violations +=
                    "A capability is scoped by \"$sentence\", and SPEC.md section 11 carries no bullet with that title."
            }
        }
        for ((layer, _) in scopes.layers) {
            if (registry.capabilities.none { it.layer == layer }) {
                violations +=
                    "The layer `$layer` is scoped, and the registry has no such layer."
            }
        }
        for ((id, _) in scopes.ids) {
            if (registry.capabilities.none { it.id == id }) {
                violations +=
                    "`$id` is scoped, and the registry carries no such capability."
            }
        }

        val built = manifests.associateBy { it.module }
        var withRunner = 0
        var scoped = 0
        // The entries with no runner, by the module the table names for them, in the registry's order.
        val missing = linkedMapOf<String, MutableList<String>>()

        for (capability in registry.capabilities) {
            // A scope is held to its sentence above, and a scoped entry needs no module to run it.
            if (scopes.sentenceFor(capability) != null) {
                scoped += 1
                continue
            }
            val module = ownership.moduleFor(capability)
            when {
                module == null -> {
                    violations +=
                        "`${capability.id}`: the root build names no module for `${capability.layer}` entries " +
                        "with `api: ${capability.api}`, so nothing could run it."
                }

                built[module]?.runners.orEmpty().any { it.id == capability.id } -> {
                    withRunner += 1
                }

                else -> {
                    missing.getOrPut(module) { mutableListOf() } += capability.id
                }
            }
        }

        // A runner for an id the registry dropped proves a rule the contract does not declare.
        for (manifest in manifests) {
            for (stray in manifest.strays) {
                violations += "${manifest.module}: `$stray` runs nothing this module owns. " +
                    "Its capability left the registry, is scoped, or belongs to another module."
            }
        }

        // The work left, one line per module, a module the build does not hold yet said as such.
        for ((module, ids) in missing) {
            val where = if (module in built) module else "$module, not in the build yet"
            val count = if (ids.size == 1) "1 capability has" else "${ids.size} capabilities have"
            violations += "$where: $count no runner: ${ids.joinToString(", ")}."
        }

        // The registry's `kotlin` column is `scoped` exactly where this SDK scopes the capability.
        for (capability in registry.capabilities) {
            val column = capability.published?.kotlin ?: continue
            val sentence = scopes.sentenceFor(capability)
            if (column == PublishedColumn.Scoped && sentence == null) {
                violations += "`${capability.id}`: the registry's `kotlin` column is `scoped`, and this SDK " +
                    "carries the capability: no bullet of SPEC.md section 11 scopes it."
            }
            if (column is PublishedColumn.Names && sentence != null) {
                violations += "`${capability.id}`: the registry's `kotlin` column names " +
                    "${column.names.joinToString(", ") { "`$it`" }}, and this SDK scopes the capability " +
                    "(\"$sentence\"): the column is `scoped`."
            }
        }

        // Every name a capability this SDK carries is published under, looked for in the ABI dumps as
        // the registry's `kotlin` column spells it.
        val carried = registry.capabilities.filter { scopes.sentenceFor(it) == null }
        val names =
            carried.flatMap { capability ->
                (capability.published?.kotlin as? PublishedColumn.Names)?.names.orEmpty().map { capability to it }
            }
        if (names.isNotEmpty() && api.isEmpty) {
            violations += "No ABI dump declares anything, so no published name could be looked for in one."
        }
        var declared = 0
        for ((capability, name) in names) {
            if (PublishedNames.isDeclared(name, api)) {
                declared += 1
                continue
            }
            violations += "`${capability.id}` is published here as `$name`, and no module's ABI dump declares it."
        }

        val summary =
            "$NAME: ${registry.capabilities.size} capabilities, $withRunner with a runner, $scoped scoped, " +
                "${missing.values.sumOf { it.size }} with no runner; " +
                "$declared of ${names.size} published names declared"
        return GateResult(NAME, registry.capabilities.size, scoped, violations, summary)
    }
}
