package com.mountaya.build.gates

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import java.io.File

/**
 * FileStructureGate is the layout law of SPEC section 6.
 *
 * The directory names the topic and the kind, and the package is the module's: one kind of file
 * per directory, the topic being the directory above it, and every file declaring its module's one
 * package wherever it sits; a file named after what it declares; no empty directory; a test at the
 * mirrored path; `expect` and `actual` under `seams` only, in main code and in test code alike. The
 * generated packages are outside the law, since the generator lays them out.
 */
object FileStructureGate {
    /** The task name the root build registers this gate under. */
    const val NAME = "lintFileStructure"

    /** The kinds are closed. `components` is `mountaya-elements`' alone. */
    private val KINDS = setOf("contracts", "engines", "rules", "testing")
    private const val COMPONENTS = "components"
    private const val ELEMENTS = "mountaya-elements"
    private const val GEO = "mountaya-geo"
    private const val SEAMS = "seams"
    private const val RULES = "rules"
    private const val COMMON_MAIN = "commonMain"
    private const val COMMON_TEST = "commonTest"

    private val UPPER_CAMEL_FILE = Regex("^[A-Z][A-Za-z0-9]*\\.kt$")

    /**
     * Runs the gate over [trees]. [untested] exempts a main file from needing a test, and [packages] a
     * file from declaring its module's package, each keyed by the path a violation would name it by.
     */
    fun run(
        trees: List<SourceTree>,
        untested: Map<String, String>,
        packages: Map<String, String> = emptyMap(),
    ): GateResult {
        val exempt = Exemptions(untested)
        val ownPackage = Exemptions(packages)
        val violations = mutableListOf<String>()
        val files = trees.kotlinFiles().filterNot { it.isGenerated }

        for (tree in trees) violations += emptyDirectories(tree)

        KotlinParser().use { parser ->
            val parsed = files.associateWith { parser.parse(it.file) }

            for ((source, file) in parsed) {
                violations += placement(source)
                violations += declaredPackage(source, file, ownPackage)
                violations += naming(source, file)
                violations += platformDeclarations(source, file, parsed)
            }
        }

        for (source in files.filterNot { it.tree.isTest }) {
            if (hasTest(source, trees)) continue
            if (exempt.covers(source.display)) continue
            violations += "${source.display}: no test at the mirrored path of its test source set " +
                "(${source.file.nameWithoutExtension}Test.kt)."
        }

        violations += twins(files)
        violations += exempt.leftovers(NAME)
        violations += ownPackage.leftovers(NAME)
        return GateResult(NAME, files.size, exempt.count + ownPackage.count, violations)
    }

    /** A file sits at `<module directory>/<topic>/<kind>/<Name>.kt`, and nowhere else. */
    private fun placement(source: SourceFile): List<String> {
        val below =
            source.belowBase
                ?: return listOf(
                    "${source.display}: is outside the module's directory, ${source.tree.base.joinToString("/")}/.",
                )
        if (below.size != 3) {
            return listOf(
                "${source.display}: sits ${below.size - 1} directories below the module's. A file sits at <topic>/<kind>/.",
            )
        }
        val kind = below[1]
        val allowed = if (source.tree.module == ELEMENTS) KINDS + COMPONENTS else KINDS
        val problems = mutableListOf<String>()
        if (kind !in allowed) {
            problems +=
                "${source.display}: `$kind` is not a kind. The kinds are ${allowed.sorted().joinToString(", ")}."
        }
        // Every geo rule is common code: a rule one target has is a rule the fixtures could
        // pass on the JVM while another target answers differently.
        if (source.tree.module == GEO && kind == RULES && below[0] != SEAMS && !source.tree.isTest &&
            source.tree.sourceSet != COMMON_MAIN
        ) {
            problems += "${source.display}: a geo rule outside commonMain."
        }
        return problems
    }

    /**
     * A file declares its module's one package, whatever directory it sits in: a consumer imports one
     * package per module, and the directory alone names the topic and the kind.
     */
    private fun declaredPackage(
        source: SourceFile,
        file: KtFile,
        exempt: Exemptions,
    ): List<String> {
        val declared = file.packageFqName.asString()
        if (declared == source.tree.modulePackage || exempt.covers(source.display)) return emptyList()
        val named = if (declared.isEmpty()) "no package" else "`$declared`"
        return listOf(
            "${source.display}: declares $named, where every file of ${source.tree.module} declares " +
                "`${source.tree.modulePackage}`. The directory names the topic and the kind; the package is the module's.",
        )
    }

    /** A file is `UpperCamelCase.kt`, named after a declaration it holds. */
    private fun naming(
        source: SourceFile,
        file: KtFile,
    ): List<String> {
        val fileName = source.file.name
        if (!UPPER_CAMEL_FILE.matches(fileName)) {
            return listOf("${source.display}: the file name is not UpperCamelCase.kt.")
        }
        val expected = source.file.nameWithoutExtension
        val declared = file.declarations.filterIsInstance<KtNamedDeclaration>().mapNotNull { it.name }
        val matches = declared.any { it == expected || it.replaceFirstChar(Char::uppercaseChar) == expected }
        return if (matches) {
            emptyList()
        } else {
            listOf(
                "${source.display}: declares nothing named `$expected`. A file is named after its primary declaration.",
            )
        }
    }

    /**
     * `expect` and `actual` appear under `seams` only, and an `actual` sits at the mirrored path
     * of an `expect` in common code: commonMain for main code, and commonTest for a test's own
     * seam, such as a runner reaching a declaration one target alone has.
     */
    private fun platformDeclarations(
        source: SourceFile,
        file: KtFile,
        parsed: Map<SourceFile, KtFile>,
    ): List<String> {
        val declarations = file.collectDescendantsOfType<KtDeclaration>()
        val expects = declarations.any { it.hasModifier(KtTokens.EXPECT_KEYWORD) }
        val actuals = declarations.any { it.hasModifier(KtTokens.ACTUAL_KEYWORD) }
        if (!expects && !actuals) return emptyList()

        val problems = mutableListOf<String>()
        val common = if (source.tree.isTest) COMMON_TEST else COMMON_MAIN
        if (source.topic != SEAMS) {
            problems += "${source.display}: `expect` and `actual` appear under `$SEAMS` only."
        }
        if (expects && source.tree.sourceSet != common) {
            problems += "${source.display}: an `expect` outside $common."
        }
        if (actuals) {
            val mirrored =
                parsed.entries.any { (other, otherFile) ->
                    other.tree.module == source.tree.module &&
                        other.tree.sourceSet == common &&
                        other.segments == source.segments &&
                        otherFile.collectDescendantsOfType<KtDeclaration>().any {
                            it.hasModifier(
                                KtTokens.EXPECT_KEYWORD,
                            )
                        }
                }
            if (!mirrored) {
                problems +=
                    "${source.display}: an `actual` with no `expect` at the mirrored path of $common."
            }
        }
        return problems
    }

    /** A directory holds something, all the way down. */
    private fun emptyDirectories(tree: SourceTree): List<String> =
        tree.directory
            .walkTopDown()
            .filter { it.isDirectory && it.listFiles().isNullOrEmpty() }
            .map {
                "${tree.module}/${tree.sourceSet}/${it.relativeTo(
                    tree.directory,
                ).invariantSeparatorsPath}: an empty directory."
            }.toList()

    /**
     * A main file has `<Name>Test.kt` at the same package path of its own test source set. A
     * platform file may be tested from common code instead, where the one test runs against
     * every target's implementation.
     */
    private fun hasTest(
        source: SourceFile,
        trees: List<SourceTree>,
    ): Boolean {
        val packagePath = source.segments.dropLast(1)
        val testName = "${source.file.nameWithoutExtension}Test.kt"
        return trees.any { tree ->
            tree.module == source.tree.module &&
                tree.isTest &&
                tree.sourceSet in testSourceSetsFor(source.tree.sourceSet) &&
                File(tree.directory, (packagePath + testName).joinToString("/")).isFile
        }
    }

    /** The test source sets that may hold the test of a file in [mainSourceSet]. */
    internal fun testSourceSetsFor(mainSourceSet: String): Set<String> {
        if (mainSourceSet == "main") return setOf("test", "androidTest")
        val stem = mainSourceSet.removeSuffix("Main")
        val own =
            when (stem) {
                "android" -> setOf("androidHostTest", "androidDeviceTest", "androidTest")
                else -> setOf("${stem}Test")
            }
        return own + "commonTest"
    }

    /**
     * The jvm and android source sets cannot share a source directory, so a file both need
     * exists twice. The two copies are held identical here, so the second is never a variant.
     */
    private fun twins(files: List<SourceFile>): List<String> {
        val android = files.filter { it.tree.sourceSet == "androidMain" }
        return files
            .filter { it.tree.sourceSet == "jvmMain" }
            .mapNotNull { jvm ->
                val twin = android.firstOrNull { it.tree.module == jvm.tree.module && it.segments == jvm.segments }
                if (twin == null || twin.file.readText() == jvm.file.readText()) {
                    null
                } else {
                    "${jvm.display}: differs from its copy in androidMain. A file both source sets hold is the same file twice."
                }
            }
    }
}
