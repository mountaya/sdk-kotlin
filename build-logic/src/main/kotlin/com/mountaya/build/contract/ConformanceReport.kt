package com.mountaya.build.contract

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * ConformanceReport is the conformance report, in the shape the contract's parity table is generated from.
 *
 * A capability is `scoped` when the scope table names it, `pass` when a runner exists and every
 * one of its cases passed, and `fail` otherwise. A capability with no runner is a `fail`: a
 * report cannot say less about a rule nobody implemented than about one that is wrong.
 */
object ConformanceReport {
    private const val SDK = "kotlin"

    /** Returns the status of every capability, keyed and sorted by id. */
    fun statuses(
        registry: ContractRegistry,
        scopes: Scopes,
        manifests: List<ConformanceManifest>,
        passed: Set<Pair<String, String>>,
    ): Map<String, String> {
        val runners = manifests.flatMap { it.runners }.associateBy { it.id }
        return registry.capabilities
            .associate { capability ->
                val runner = runners[capability.id]
                val status =
                    when {
                        scopes.sentenceFor(capability) != null -> "scoped"
                        runner == null || runner.cases.isEmpty() -> "fail"
                        runner.cases.all { (runner.className to it.function) in passed } -> "pass"
                        else -> "fail"
                    }
                capability.id to status
            }.toSortedMap()
    }

    /**
     * Returns the report's `published` member: for every capability whose `kotlin` column is a list, the names of it
     * the committed ABI dumps declare, in the column's order, none where they declare none, so the report equals the
     * column where every name is declared. The contract's gate holds it to the column.
     */
    fun published(
        registry: ContractRegistry,
        api: ApiDeclarations,
    ): Map<String, List<String>> =
        registry.capabilities
            .mapNotNull { capability ->
                val column = capability.published?.kotlin as? PublishedColumn.Names ?: return@mapNotNull null
                capability.id to column.names.filter { PublishedNames.isDeclared(it, api) }
            }.toMap()

    /**
     * Renders the report: sorted keys, two-space indent, a trailing newline. [modelDigest] is the digest of the
     * files the run's surface was printed from (`sdk/SPEC.md` section 11), which the contract's gate holds to its
     * own files. [published] is each capability's declared names in its column's order, and null leaves the
     * member out.
     */
    fun render(
        statuses: Map<String, String>,
        registryVersion: Int,
        modelDigest: String,
        published: Map<String, List<String>>? = null,
    ): String =
        buildString {
            appendLine("{")
            appendLine("  \"capabilities\": {")
            val entries = statuses.toSortedMap().entries.toList()
            entries.forEachIndexed { index, (id, status) ->
                appendLine("    \"$id\": \"$status\"${if (index < entries.size - 1) "," else ""}")
            }
            appendLine("  },")
            appendLine("  \"modelDigest\": \"$modelDigest\",")
            if (published != null && published.isEmpty()) {
                appendLine("  \"published\": {},")
            } else if (published != null) {
                appendLine("  \"published\": {")
                val names = published.toSortedMap().entries.toList()
                names.forEachIndexed { index, (id, declared) ->
                    val comma = if (index < names.size - 1) "," else ""
                    if (declared.isEmpty()) {
                        appendLine("    \"$id\": []$comma")
                    } else {
                        appendLine("    \"$id\": [")
                        declared.forEachIndexed { position, name ->
                            appendLine("      \"$name\"${if (position < declared.size - 1) "," else ""}")
                        }
                        appendLine("    ]$comma")
                    }
                }
                appendLine("  },")
            }
            appendLine("  \"registryVersion\": $registryVersion,")
            appendLine("  \"sdk\": \"$SDK\"")
            appendLine("}")
        }

    /**
     * Reads the test results under [directories] and returns every test that passed, as its
     * class name and function name. A test that failed, errored or was skipped did not pass.
     */
    fun passedTests(directories: Collection<File>): Set<Pair<String, String>> {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val passed = mutableSetOf<Pair<String, String>>()
        val files =
            directories.flatMap { directory ->
                directory.walkTopDown().filter { it.isFile && it.extension == "xml" }
            }
        for (file in files) {
            val cases = builder.parse(file).getElementsByTagName("testcase")
            for (index in 0 until cases.length) {
                val case = cases.item(index)
                val children = case.childNodes
                val failed =
                    (0 until children.length).any { child ->
                        children.item(child).nodeName in setOf("failure", "error", "skipped")
                    }
                if (failed) continue
                val className = case.attributes.getNamedItem("classname")?.nodeValue ?: continue
                // A multiplatform test run appends its target to the name, as in `case[jvm]`.
                val function =
                    case.attributes
                        .getNamedItem("name")
                        ?.nodeValue
                        ?.substringBefore('[') ?: continue
                passed += className to function
            }
        }
        return passed
    }
}
