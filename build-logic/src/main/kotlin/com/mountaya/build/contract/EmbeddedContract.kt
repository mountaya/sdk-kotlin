package com.mountaya.build.contract

/**
 * EmbeddedContract renders the registry and every fixture as Kotlin source.
 *
 * A test on the iOS simulator or on an Android device cannot read a sibling checkout from disk,
 * so the contract is compiled in: the same harness then runs the same fixtures on every target.
 */
object EmbeddedContract {
    /** The package the rendered source declares. */
    const val PACKAGE = "com.mountaya.conformance.contract.generated"

    /** The file the rendered source is written to. */
    const val FILE = "EmbeddedContract.kt"

    /**
     * Renders the source. [registry] is null when the contract checkout is absent, which yields
     * an empty contract that says so.
     */
    fun render(
        registry: ContractRegistry?,
        fixtures: Map<String, String>,
    ): String =
        buildString {
            append(generatedHeader("generateContractFixtures"))
            appendLine("package $PACKAGE")
            appendLine()
            appendLine("import com.mountaya.conformance.RegistryEntry")
            appendLine()
            appendLine("internal object EmbeddedContract {")
            appendLine("    /** Whether the contract checkout was there when this was generated. */")
            appendLine("    const val PRESENT: Boolean = ${registry != null}")
            appendLine()
            appendLine("    /** The registry's own version. */")
            appendLine("    const val REGISTRY_VERSION: Int = ${registry?.version ?: 0}")
            appendLine()
            appendLine("    /** Every capability of the registry, in its order. */")
            appendLine("    val entries: List<RegistryEntry> =")
            appendLine("        listOf(")
            for (capability in registry?.capabilities.orEmpty()) {
                appendLine(
                    "            RegistryEntry(${kotlinLiteral(capability.id)}, ${kotlinLiteral(capability.layer)}, " +
                        "${capability.api}, ${kotlinLiteral(capability.fixture)}),",
                )
            }
            appendLine("        )")
            appendLine()
            appendLine("    /** The text of the fixture at [path], relative to the fixtures directory. */")
            appendLine("    fun fixture(path: String): String? =")
            appendLine("        when (path) {")
            fixtures.keys.sorted().forEachIndexed { index, path ->
                appendLine("            ${kotlinLiteral(path)} -> fixture$index()")
            }
            appendLine("            else -> null")
            appendLine("        }")
            fixtures.keys.sorted().forEachIndexed { index, path ->
                appendLine()
                appendLine("    private fun fixture$index(): String =")
                appendLine("        buildString {")
                for (piece in chunks(fixtures.getValue(path))) appendLine("            append(${kotlinLiteral(piece)})")
                appendLine("        }")
            }
            appendLine("}")
        }
}
