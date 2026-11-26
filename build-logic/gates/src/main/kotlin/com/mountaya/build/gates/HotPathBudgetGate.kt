package com.mountaya.build.gates

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * HotPathBudgetGate is the hot-path law of SPEC section 7: every hot path the contract lists for this SDK keeps a live
 * budget test.
 *
 * A hot path is work that runs per pointer frame, per camera frame or per change: a hit-test a pointer move pays
 * for, a redraw a scrub causes, a source a change sends the renderer again. Its failure is the one no correctness
 * test sees, since the output stays right and only the count of work grows, so each path keeps a budget test
 * beside its subject, which drives the path through a realistic burst and asserts its count against a constant
 * the reader can argue with. A budget test guards only while it exists, and nothing else notices when it stops
 * existing: this gate is what makes the absence loud.
 *
 * A budget test is a function of a test source set, device ones included, named `hotPathBudget` and the hot
 * path's id in UpperCamelCase, then what it shows (`hotPathBudgetProfileCursorRedrawsOnlyTheCursor` for
 * `profile-cursor`), annotated `@Test`. Its body hands the path's constant, the id in capitals with its hyphens
 * as underscores and `_BUDGET` after it (`PROFILE_CURSOR_BUDGET`), to an `assert…` call, and the file it stands
 * in declares that constant as a top-level `private const val`. Every file is read with the compiler's parser,
 * so a name in a comment or a string names nothing.
 *
 * It fails on a path listed for this SDK with no live budget test, on a budget test naming a path the list does
 * not hold for this SDK or none at all, on one that is no `@Test`, on one switched off by an `@Ignore` or a
 * `@Disabled` on it or on a class around it, on one that hands its constant to no assertion, and on one whose
 * file declares no such constant. Where no list was read, each budget test is held to its constant alone, and a
 * run that requires the list fails. What it does not check is whether a budget is tight, which is the reviewer's,
 * nor whether a burst reaches its path, which is why each budget test asserts too that the work still happened.
 */
object HotPathBudgetGate {
    /** The task name the root build registers this gate under. */
    const val NAME = "checkHotPathBudgets"

    /** The name the contract's list gives this SDK. */
    const val SDK = "kotlin"

    /** What a budget test's name opens with, before the UpperCamelCase id of the path it budgets. */
    const val PREFIX = "hotPathBudget"

    /** The contract's list, as a finding names it. */
    const val CONTRACT = "sdk/hot-paths.yaml"

    /** The annotation that makes a function a test, kotlin.test's and JUnit's alike. */
    private const val TEST = "Test"

    /** The annotations that switch a test off: kotlin.test's and JUnit 4's `@Ignore`, and JUnit 5's `@Disabled`. */
    private val SWITCHES = setOf("Ignore", "Disabled")

    /** What every call that asserts is named after: kotlin.test's `assertEquals`, `assertTrue` and the rest. */
    private const val ASSERTION = "assert"

    /** What a budget constant's name ends with, after its path's id. */
    private const val SUFFIX = "_BUDGET"

    private val CONSTANT = Regex("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$SUFFIX$")

    /**
     * BudgetTest is one function a scan found whose name opens with [PREFIX]: the path it names, where it stands,
     * and what makes it live or not.
     */
    data class BudgetTest(
        /** The id of the path its name names, or null where it names none. */
        val id: String?,
        /** Its name, as declared. */
        val name: String,
        /** The file it stands in, as a finding names it. */
        val file: String,
        /** The line its declaration starts on. */
        val line: Int,
        /** Whether it is annotated `@Test`. */
        val isTest: Boolean,
        /** Whether its body hands its path's constant to an `assert…` call. */
        val asserted: Boolean,
        /** Whether its file declares its path's constant as a top-level `private const val`. */
        val declared: Boolean,
        /** What switches it off, as written (`@Ignore on PoiPanelTest`), or null where nothing does. */
        val switchedOff: String?,
    ) {
        /** Whether it holds its path: it names one, runs, and asserts against a constant its file declares. */
        val isLive: Boolean get() = id != null && isTest && switchedOff == null && asserted && declared
    }

    /** Returns the constant a budget test of [id] asserts against: `PROFILE_CURSOR_BUDGET` for `profile-cursor`. */
    fun constantOf(id: String): String = id.uppercase().replace('-', '_') + SUFFIX

    /** Returns what the name of a budget test of [id] opens with: `hotPathBudgetProfileCursor` for `profile-cursor`. */
    fun nameOf(id: String): String = PREFIX + camelOf(id)

    /**
     * Runs the gate over the test source sets of [trees], holding them to [listed], the contract's list, or to
     * their constants alone where it is null; [required] makes a null list a failure, as it is under CI.
     */
    fun run(
        trees: List<SourceTree>,
        listed: List<HotPath>?,
        required: Boolean,
    ): GateResult {
        val files = trees.filter { it.isTest }.kotlinFiles()
        val known = listed?.map { it.id }.orEmpty()
        val budgets = mutableListOf<BudgetTest>()
        KotlinParser().use { parser ->
            for (source in files) {
                // Most files hold no budget test, and the text check spares parsing every one of them.
                if (PREFIX !in source.file.readText()) continue
                budgets += budgetTestsIn(parser.parse(source.file), source.display, known)
            }
        }

        val violations = mutableListOf<String>()
        if (listed == null && required) {
            violations += "$CONTRACT: no list of hot paths was read, the contract checkout being absent, and a run " +
                "under CI holds every listed path to a live budget test: fetch the schemas checkout beside this " +
                "one, or name one with -Pmountaya.schemasDir."
        }
        for (test in budgets) violations += problemsOf(test)
        val ours = listed?.filter { SDK in it.sdks }?.map { it.id }
        if (ours != null) {
            val live = budgets.filter { it.isLive }.mapNotNull { it.id }.toSet()
            for (id in ours) {
                if (id in live) continue
                violations += "$CONTRACT: `$id` is listed for $SDK with no live budget test: write one beside its " +
                    "subject, `@Test fun ${nameOf(id)}…()`, asserting a count against a top-level " +
                    "`private const val ${constantOf(id)}`."
            }
            for (test in budgets) {
                val id = test.id ?: continue
                if (id in ours) continue
                violations += "${test.file}:${test.line}: `${test.name}` budgets `$id`, which $CONTRACT does not " +
                    "list for $SDK: list it there, with the sentence its budget holds, or name the test after the " +
                    "path it budgets."
            }
        }

        val counted = "${budgets.size} budget tests in ${files.size} test files"
        val line =
            if (ours == null) {
                "$NAME: no list of hot paths was read, the contract checkout being absent, so each of the $counted " +
                    "is held to its constant alone"
            } else {
                "$NAME: ${ours.size} hot paths listed for $SDK checked, 0 exempt ($counted)"
            }
        return GateResult(NAME, ours?.size ?: 0, 0, violations, line)
    }

    /**
     * Every budget test of [file], shown as [display]: each function whose name opens with [PREFIX], the next
     * letter a capital or none, the path its name names among [known] and the paths whose constants the file
     * declares, the longest that names a whole word, whether it is a test, what switches it off, whether it
     * asserts against its path's constant, and whether the file declares that constant.
     */
    internal fun budgetTestsIn(
        file: KtFile,
        display: String,
        known: Collection<String>,
    ): List<BudgetTest> {
        val properties = file.declarations.filterIsInstance<KtProperty>()
        val declared =
            properties
                .filter { it.hasModifier(KtTokens.CONST_KEYWORD) && it.hasModifier(KtTokens.PRIVATE_KEYWORD) }
                .mapNotNull { it.name }
                .toSet()
        val fromConstants =
            properties
                .mapNotNull { it.name }
                .filter { CONSTANT.matches(it) }
                .map { it.removeSuffix(SUFFIX).lowercase().replace('_', '-') }
        val candidates = (known + fromConstants).distinct().sortedByDescending { camelOf(it).length }

        return file
            .collectDescendantsOfType<KtNamedFunction> { isBudgetName(it.name) }
            .sortedBy { it.textOffset }
            .map { function ->
                val name = function.name.orEmpty()
                val id = idNamed(name, candidates)
                BudgetTest(
                    id = id,
                    name = name,
                    file = display,
                    line = function.line(),
                    isTest = function.annotationNames().contains(TEST),
                    asserted = id != null && assertsAgainst(function, constantOf(id)),
                    declared = id != null && constantOf(id) in declared,
                    switchedOff = switchOf(function),
                )
            }
    }

    /** What is wrong with [test], one finding a reason. */
    private fun problemsOf(test: BudgetTest): List<String> {
        val at = "${test.file}:${test.line}: `${test.name}`"
        val id = test.id
        val problems = mutableListOf<String>()
        if (id == null) {
            problems += "$at names no hot path: a budget test's name continues `$PREFIX` with the UpperCamelCase " +
                "id of a path $CONTRACT lists, as `${nameOf("route-measure")}…` budgets `route-measure`."
        }
        if (!test.isTest) {
            problems += "$at is not annotated `@Test`, so it never runs, and a budget that does not run holds nothing."
        }
        test.switchedOff?.let { switch ->
            problems += "$at is switched off by $switch, and a budget that does not run holds nothing."
        }
        if (id == null) return problems
        val constant = constantOf(id)
        if (!test.asserted) {
            problems += "$at hands `$constant` to no assertion: pass it to an `assert…` call, so the name's promise " +
                "is the assertion's."
        }
        if (!test.declared) {
            problems += "$at stands in a file that declares no top-level `private const val $constant`: declare " +
                "the number beside the subject it budgets."
        }
        return problems
    }

    /** Whether [name] is a budget test's: [PREFIX] alone, or followed by a capital. */
    private fun isBudgetName(name: String?): Boolean =
        name != null &&
            name.startsWith(PREFIX) &&
            (name.length == PREFIX.length || name[PREFIX.length].isUpperCase())

    /** The path [name] budgets: the longest of [candidates] whose UpperCamelCase words follow [PREFIX] whole, or null. */
    private fun idNamed(
        name: String,
        candidates: List<String>,
    ): String? {
        val rest = name.removePrefix(PREFIX)
        return candidates.firstOrNull { id ->
            val words = camelOf(id)
            rest.startsWith(words) && (rest.length == words.length || rest[words.length].isUpperCase())
        }
    }

    /** [id]'s words in UpperCamelCase: `ProfileCursor` for `profile-cursor`. */
    private fun camelOf(id: String): String =
        id.split('-').joinToString("") { word -> word.replaceFirstChar(Char::uppercaseChar) }

    /** The short names of the annotations on [this]. */
    private fun KtAnnotated.annotationNames(): List<String> = annotationEntries.mapNotNull { it.shortName?.asString() }

    /**
     * What switches [function] off, as a finding writes it: an annotation of [SWITCHES] on it, or on the nearest class
     * or object around it that carries one, or null where none does.
     */
    private fun switchOf(function: KtNamedFunction): String? {
        function.annotationNames().firstOrNull { it in SWITCHES }?.let { return "`@$it`" }
        for (owner in generateSequence(function.parent) { it.parent }.filterIsInstance<KtClassOrObject>()) {
            val switch = owner.annotationNames().firstOrNull { it in SWITCHES } ?: continue
            return "`@$switch` on `${owner.name ?: "its class"}`"
        }
        return null
    }

    /**
     * Whether the body of [function] hands [constant] to an assertion: the name stands among the arguments, at any
     * depth, of a call whose callee is named `assert…`.
     */
    private fun assertsAgainst(
        function: KtNamedFunction,
        constant: String,
    ): Boolean {
        val body = function.bodyExpression ?: return false
        return body.collectDescendantsOfType<KtCallExpression>().any { call ->
            val callee = (call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()
            callee != null &&
                callee.startsWith(ASSERTION) &&
                call.valueArguments.any { argument ->
                    val expression = argument.getArgumentExpression() ?: return@any false
                    expression.mentions(constant)
                }
        }
    }

    /** Whether [this] expression names [constant], itself or anywhere within it. */
    private fun KtExpression.mentions(constant: String): Boolean =
        (this is KtNameReferenceExpression && getReferencedName() == constant) ||
            collectDescendantsOfType<KtNameReferenceExpression>().any { it.getReferencedName() == constant }
}
