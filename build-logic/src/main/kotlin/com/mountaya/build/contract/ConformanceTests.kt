package com.mountaya.build.contract

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File

/**
 * Ownership says which module runs each registry entry: the root build's table, by layer, on each side of the
 * registry's `api` flag (SPEC.md section 9).
 *
 * A module is named by the project name it takes in the build, and it is named before it is in
 * the build: its entries then read as having no runner, the work left, rather than as owned by
 * nobody. A layer or a flag the table names no module for fails the coverage gate.
 */
data class Ownership(
    /** The module that runs each layer's `api: true` entries, keyed by layer. */
    val apiModules: Map<String, String>,
    /** The module that runs each layer's `api: false` entries, keyed by layer. */
    val internalModules: Map<String, String>,
) {
    /** Returns the module that runs [capability], or null when the table names none for its layer and flag. */
    fun moduleFor(capability: Capability): String? =
        (if (capability.api) apiModules else internalModules)[capability.layer]
}

/** CaseManifest is one generated test: the fixture case's own name, and the function that runs it. */
data class CaseManifest(
    /** The case's name, as its fixture writes it. */
    val name: String,
    /** The name of the function that runs it. */
    val function: String,
)

/** RunnerManifest is one runner a module holds, and the test class generated over it. */
data class RunnerManifest(
    /** The capability the runner proves. */
    val id: String,
    /** The name of the test class generated over it. */
    val className: String,
    /** Its cases, in the fixture's order. */
    val cases: List<CaseManifest>,
)

/**
 * ConformanceManifest is what a module's generated conformance tests cover, written beside them.
 *
 * The coverage gate and the report read these rather than the test sources, so both answer from
 * what was actually generated.
 */
data class ConformanceManifest(
    /** The module, by its project name. */
    val module: String,
    /** The capabilities the root build's table gives it to run. */
    val owned: List<String>,
    /** The runners it holds, and the tests generated over each. */
    val runners: List<RunnerManifest>,
    /** The runners it holds for a capability it does not own. */
    val strays: List<String>,
) {
    /** Writes this manifest as JSON. */
    fun encode(): String =
        JsonOutput.prettyPrint(
            JsonOutput.toJson(
                mapOf(
                    "module" to module,
                    "owned" to owned,
                    "runners" to
                        runners.map { runner ->
                            mapOf(
                                "id" to runner.id,
                                "className" to runner.className,
                                "cases" to runner.cases.map { mapOf("name" to it.name, "function" to it.function) },
                            )
                        },
                    "strays" to strays,
                ),
            ),
        ) + "\n"

    /** The reading of a manifest. */
    companion object {
        /** Reads a manifest written by [encode]. */
        fun decode(file: File): ConformanceManifest {
            val root = JsonSlurper().parse(file) as Map<*, *>
            return ConformanceManifest(
                module = root["module"] as String,
                owned = (root["owned"] as List<*>).map { it as String },
                runners =
                    (root["runners"] as List<*>).map { entry ->
                        entry as Map<*, *>
                        RunnerManifest(
                            id = entry["id"] as String,
                            className = entry["className"] as String,
                            cases =
                                (entry["cases"] as List<*>).map { case ->
                                    case as Map<*, *>
                                    CaseManifest(case["name"] as String, case["function"] as String)
                                },
                        )
                    },
                strays = (root["strays"] as List<*>).map { it as String },
            )
        }
    }
}

/**
 * ConformanceTests generates one `kotlin.test` function per fixture case, for every capability a module owns and holds
 * a runner for.
 *
 * `kotlin.test` has no dynamic tests and a function name cannot carry a dot or a slash on every
 * target, so each case becomes a function with a sanitised name, and the exact
 * `"<id> · <case>"` string leads every failure it reports.
 *
 * A runner is found by its file: the capability `transport.retry.backoff` is run by
 * `TransportRetryBackoffRunner.kt` in the module's runner directory, declared in the module's
 * package. No list of runners is kept anywhere, so none can disagree with the registry.
 */
object ConformanceTests {
    /** Request is what one module asks to have generated. */
    data class Request(
        /** The module the tests are generated for, by its project name. */
        val module: String,
        /** The module that runs each layer's entries. */
        val ownership: Ownership,
        /** The registry, or null when the contract checkout is absent. */
        val registry: ContractRegistry?,
        /** The capabilities this SDK does not carry. */
        val scopes: Scopes,
        /** The case names of a capability's fixture, in the fixture's order. */
        val casesOf: (Capability) -> List<String>,
        /** The runner files of the module's common tests, by name and without the extension. */
        val runnerFiles: List<String>,
        /** The runner files of the module's jvm tests, whose entries run on the jvm alone. */
        val jvmRunnerFiles: List<String> = emptyList(),
        /** The package the module's runners are declared in. */
        val runnerPackage: String,
        /** The package the generated tests are declared in. */
        val generatedPackage: String,
        /** Whether this module is the one that states the scoped capabilities as tests. */
        val statesScopes: Boolean,
    )

    /**
     * Generated is the sources to write, keyed by file name: [files] into the common tests, [jvmFiles] into the jvm
     * tests, and the manifest that describes both.
     */
    data class Generated(
        /** The common tests, keyed by file name. */
        val files: Map<String, String>,
        /** The jvm tests, keyed by file name. */
        val jvmFiles: Map<String, String>,
        /** The manifest that describes both. */
        val manifest: ConformanceManifest,
    )

    private const val TASK = "generateConformanceTests"
    private const val HARNESS = "com.mountaya.conformance.runConformanceCase"

    /** Returns the name of the object that runs [id]: its dotted and hyphenated words, then `Runner`. */
    fun runnerName(id: String): String = "${camel(id)}Runner"

    private fun camel(id: String): String =
        id.split('.', '-').joinToString("") { word -> word.replaceFirstChar(Char::uppercaseChar) }

    /**
     * Generates the tests [request] asks for.
     *
     * An entry runs whole on a target or not at all there, so a runner sits in the common tests,
     * which run on every target, or in the jvm tests, which run on the jvm alone, and never in
     * both (SPEC.md section 9).
     */
    fun generate(request: Request): Generated {
        val files = sortedMapOf<String, String>()
        val jvmFiles = sortedMapOf<String, String>()
        val registry = request.registry

        val twice = request.runnerFiles.toSet() intersect request.jvmRunnerFiles.toSet()
        require(twice.isEmpty()) {
            "${request.module}: ${twice.sorted().joinToString(", ")} sits in both the common and the jvm tests. " +
                "An entry runs whole on a target or not at all there, so its runner sits in one of them."
        }

        if (registry == null) {
            if (request.statesScopes) {
                files["ContractCheckoutConformanceTest.kt"] =
                    absentCheckout(request.generatedPackage)
            }
            return Generated(
                files,
                jvmFiles,
                ConformanceManifest(request.module, emptyList(), emptyList(), emptyList()),
            )
        }

        val owned =
            registry.capabilities
                .filter { request.ownership.moduleFor(it) == request.module }
                .filter { request.scopes.sentenceFor(it) == null }
        val expected = owned.associateBy { runnerName(it.id) }
        val common = request.runnerFiles.toSet()
        val jvm = request.jvmRunnerFiles.toSet()
        val held = common + jvm

        val runners =
            owned.filter { runnerName(it.id) in held }.map { capability ->
                val className = "${camel(capability.id)}ConformanceTest"
                val cases = functionNames(request.casesOf(capability))
                val into = if (runnerName(capability.id) in jvm) jvmFiles else files
                into["$className.kt"] = runnerTests(request, capability, className, cases)
                RunnerManifest(capability.id, "${request.generatedPackage}.$className", cases)
            }

        if (request.statesScopes) {
            val scoped =
                registry.capabilities.mapNotNull { capability ->
                    request.scopes.sentenceFor(capability)?.let { sentence -> capability to sentence }
                }
            if (scoped.isNotEmpty()) {
                files["ScopedCapabilitiesConformanceTest.kt"] =
                    scopedTests(request.generatedPackage, scoped)
            }
        }

        return Generated(
            files,
            jvmFiles,
            ConformanceManifest(
                module = request.module,
                owned = owned.map { it.id },
                runners = runners,
                strays = held.filterNot { it in expected }.sorted(),
            ),
        )
    }

    /** A function name per case: the case's own name with everything but letters and digits as `_`. */
    internal fun functionNames(cases: List<String>): List<CaseManifest> {
        val taken = mutableSetOf<String>()
        return cases.map { case ->
            val sanitised = case.map { if (it.isAsciiLetterOrDigit()) it else '_' }.joinToString("")
            val legal =
                when {
                    sanitised.isEmpty() || sanitised.first().isDigit() -> "case_$sanitised"
                    sanitised in KEYWORDS -> "case_$sanitised"
                    else -> sanitised
                }
            var name = legal
            var suffix = 2
            while (!taken.add(name)) name = "${legal}_${suffix++}"
            CaseManifest(case, name)
        }
    }

    private fun Char.isAsciiLetterOrDigit(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'

    private val KEYWORDS =
        setOf(
            "as",
            "break",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "for",
            "fun",
            "if",
            "in",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "typeof",
            "val",
            "var",
            "when",
            "while",
        )

    private fun runnerTests(
        request: Request,
        capability: Capability,
        className: String,
        cases: List<CaseManifest>,
    ): String =
        buildString {
            append(generatedHeader(TASK))
            appendLine("package ${request.generatedPackage}")
            appendLine()
            appendLine("import $HARNESS")
            appendLine("import ${request.runnerPackage}.${runnerName(capability.id)}")
            appendLine("import kotlinx.coroutines.test.TestResult")
            appendLine("import kotlin.test.Test")
            appendLine()
            appendLine("class $className {")
            cases.forEachIndexed { index, case ->
                if (index > 0) appendLine()
                val runner = runnerName(capability.id)
                val arguments = "$runner, ${kotlinLiteral(capability.id)}, ${kotlinLiteral(case.name)}"
                appendLine("    @Test")
                appendLine("    fun ${case.function}(): TestResult = runConformanceCase($arguments)")
            }
            appendLine("}")
        }

    private fun scopedTests(
        generatedPackage: String,
        scoped: List<Pair<Capability, String>>,
    ): String =
        buildString {
            append(generatedHeader(TASK))
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import kotlin.test.Test")
            appendLine()
            appendLine("/** Each scoped capability, stated as a test so that a scope is reported and never skipped. */")
            appendLine("class ScopedCapabilitiesConformanceTest {")
            functionNames(scoped.map { it.first.id }).forEachIndexed { index, case ->
                if (index > 0) appendLine()
                appendLine("    // ${case.name} · scoped. SPEC.md section 11: ${scoped[index].second}")
                appendLine("    @Test")
                appendLine("    fun ${case.function}__scoped() = Unit")
            }
            appendLine("}")
        }

    private fun absentCheckout(generatedPackage: String): String =
        buildString {
            append(generatedHeader(TASK))
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import kotlin.test.Test")
            appendLine()
            appendLine("/** Says, in the test report itself, that this run had no contract to run against. */")
            appendLine("class ContractCheckoutConformanceTest {")
            appendLine("    @Test")
            appendLine("    fun the_contract_checkout_is_absent_so_no_fixture_ran() = Unit")
            appendLine("}")
        }
}
