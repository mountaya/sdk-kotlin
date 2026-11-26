package com.mountaya.build.gates

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * DocumentationGate is the documentation law of SPEC section 7: every declaration a consumer can reach carries KDoc.
 *
 * A declaration is reached where it and everything enclosing it is `public` or `protected`, outside a
 * function's body, an object expression and an enum entry's body: a type, a companion object named or
 * not, a type alias, a function, a property, a property its primary constructor declares, an enum entry
 * and a secondary constructor. An `override` is documented by what it overrides, so it is exempt. Test
 * sources publish nothing, and the generated packages carry the contract's descriptions, printed, so
 * the gate reads neither. Anything else with no KDoc is exempt by its name alone, with its sentence.
 */
object DocumentationGate {
    /** The task name the root build registers this gate under. */
    const val NAME = "lintDocumentation"

    /** The name a companion object takes in a declaration's path where it states none. */
    private const val COMPANION = "Companion"

    /** The name a secondary constructor takes in a declaration's path. */
    private const val CONSTRUCTOR = "constructor"

    /**
     * Runs the gate over [trees]. An exemption is keyed by the declaration's path below its package, as
     * the gate prints it (`MapPaint.Companion`, `formatDistance`), and valued by the sentence that
     * justifies it.
     */
    fun run(
        trees: List<SourceTree>,
        exemptions: Map<String, String>,
    ): GateResult {
        val exempt = Exemptions(exemptions)
        val violations = mutableListOf<String>()
        var checked = 0

        KotlinParser().use { parser ->
            for (source in trees.kotlinFiles()) {
                if (source.isGenerated || source.tree.isTest) continue
                val file = parser.parse(source.file)
                for (declaration in reachedDeclarations(file)) {
                    checked += 1
                    if (declaration.docComment != null) continue
                    val path = pathOf(declaration)
                    if (exempt.covers(path)) continue
                    val visibility = if (declaration.isProtected()) "protected" else "public"
                    violations += "${source.display}:${declaration.line()}: `$path` is $visibility and carries no " +
                        "KDoc. Say what it is in a `/** */` comment, or exempt it by name with the sentence that " +
                        "justifies it."
                }
            }
        }

        violations += exempt.leftovers(NAME)
        return GateResult(NAME, checked, exempt.count, violations)
    }

    /** Every declaration of [file] a consumer reaches and that is not an `override`, in the file's order. */
    private fun reachedDeclarations(file: KtFile): List<KtDeclaration> =
        file
            .collectDescendantsOfType<KtDeclaration> { declaration ->
                isDocumentedKind(declaration) &&
                    !declaration.hasModifier(KtTokens.OVERRIDE_KEYWORD) &&
                    declaration.isReached()
            }.sortedBy { it.textOffset }

    /** Whether [declaration] is of a kind the law reads. */
    private fun isDocumentedKind(declaration: KtDeclaration): Boolean =
        when (declaration) {
            is KtClassOrObject -> !(declaration is KtObjectDeclaration && declaration.isObjectLiteral())
            is KtParameter -> declaration.hasValOrVar() && declaration.ownerFunction is KtPrimaryConstructor
            is KtTypeAlias, is KtNamedFunction, is KtProperty, is KtSecondaryConstructor -> true
            else -> false
        }

    /**
     * Whether a consumer reaches [this] declaration: nothing on the way to its file is a function's body,
     * an object expression, a local type or an enum entry's body, and neither it nor anything enclosing it
     * is private or internal.
     */
    private fun KtDeclaration.isReached(): Boolean {
        if (isHidden()) return false
        var node: PsiElement? = parent
        while (node != null && node !is KtFile) {
            when (node) {
                is KtClassBody, is KtParameterList, is KtPrimaryConstructor -> Unit
                is KtEnumEntry -> return false
                is KtObjectDeclaration -> if (node.isObjectLiteral() || node.isLocal || node.isHidden()) return false
                is KtClassOrObject -> if (node.isLocal || node.isHidden()) return false
                else -> return false
            }
            node = node.parent
        }
        return true
    }

    /** Whether [this] declaration is private or internal, which no consumer reaches. */
    private fun KtDeclaration.isHidden(): Boolean =
        hasModifier(KtTokens.PRIVATE_KEYWORD) || hasModifier(KtTokens.INTERNAL_KEYWORD)

    /** Whether [this] declaration, or a type it is declared in, is protected. */
    private fun KtDeclaration.isProtected(): Boolean =
        generateSequence<PsiElement>(this) { it.parent }
            .filterIsInstance<KtDeclaration>()
            .any { it.hasModifier(KtTokens.PROTECTED_KEYWORD) }

    /**
     * The path of [declaration] below its package, every type it is declared in named on the way: a
     * companion object by its own name or `Companion`, and a secondary constructor as `constructor`.
     */
    private fun pathOf(declaration: KtDeclaration): String =
        generateSequence<PsiElement>(declaration) { it.parent }
            .filterIsInstance<KtDeclaration>()
            .filter { it === declaration || it is KtClassOrObject }
            .map { nameOf(it) }
            .toList()
            .asReversed()
            .joinToString(".")

    /** The name [declaration] takes in a path. */
    private fun nameOf(declaration: KtDeclaration): String =
        when (declaration) {
            is KtSecondaryConstructor -> CONSTRUCTOR
            is KtObjectDeclaration -> declaration.name ?: COMPANION
            else -> declaration.name ?: "?"
        }
}
