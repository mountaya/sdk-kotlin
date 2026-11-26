package com.mountaya.build.gates

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPostfixExpression
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * NamingGate is the naming law of SPEC section 5, for the names a file declares.
 *
 * It holds what the formatter does not: the refused prefixes and suffixes, the closed handler
 * verbs, acronyms spelled as words, where a SCREAMING_SNAKE name is allowed, the UpperCamelCase of
 * an open vocabulary's members, and the ban on the double-bang. It stops at the generated boundary,
 * because generated names are the contract's.
 */
object NamingGate {
    /** The task name the root build registers this gate under. */
    const val NAME = "lintNaming"

    /** The one prefix a type may not lead with, since the package already says it. */
    private const val PACKAGE_SUBJECT = "Mountaya"

    /**
     * What a `CompositionLocal` leads with: Compose's own word for one, `Local`, then the package subject, so the
     * subject appears under one spelling across the modules (`LocalMountayaClient`).
     */
    private const val COMPOSITION_LOCAL_LEAD = "Local$PACKAGE_SUBJECT"

    /** The calls that make a `CompositionLocal`, which a property's initializer names where it states no type. */
    private val COMPOSITION_LOCAL_FACTORIES = setOf("compositionLocalOf", "staticCompositionLocalOf")

    /**
     * Each of these means Engine, for a type the layout files under `engines`, which is what a
     * machine is in every SDK. Outside `engines` the words are the domain's: a value about an
     * edit in progress may be an edit session.
     */
    private val ENGINE_SYNONYMS = listOf("Controller", "Manager", "Session", "Service", "Coordinator")

    /** A lifecycle holder the consumer writes and this SDK never publishes, wherever it is. */
    private const val VIEW_MODEL = "ViewModel"

    /** What a type's author could not name. A type says what it is. */
    private val VAGUE_SUFFIXES = listOf("Helper", "Helpers", "Util", "Utils", "Utility", "Utilities")

    /**
     * A null object leads with `Inert`, and each of these, compared lowercased and whole, is
     * another word for it. `sdk/SPEC.md` section 9 in `mountaya/schemas` lists them for every SDK.
     */
    private val NULL_OBJECT_WORDS = setOf("null", "noop", "nop", "dummy", "never", "silent", "no")

    /** The closed handler verb set. A compound is built from one of them. */
    private val HANDLER_VERBS =
        setOf("Change", "Close", "Error", "Hover", "Select", "Click", "Toggle", "Ready", "Destroy")

    /** The kind directory that files a machine. */
    private const val ENGINES = "engines"

    private val SCREAMING_SNAKE = Regex("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$")
    private val UPPER_CAMEL = Regex("^[A-Z][A-Za-z0-9]*$")
    private val LOWER_CAMEL = Regex("^[a-z][A-Za-z0-9]*$")
    private val ACRONYM = Regex("[A-Z]{3,}|[A-Z]{2,}$")
    private val HANDLER = Regex("^on[A-Z].*")

    /** Runs the gate over [trees], reading exemptions by name from [exemptions]. */
    fun run(
        trees: List<SourceTree>,
        exemptions: Map<String, String>,
    ): GateResult {
        val exempt = Exemptions(exemptions)
        val violations = mutableListOf<String>()
        var checked = 0

        KotlinParser().use { parser ->
            for (source in trees.kotlinFiles()) {
                if (source.isGenerated) continue
                val file = parser.parse(source.file)

                // No double-bang in shipped source, tests included: it is an assertion with no
                // message, and it throws where a value is read rather than where the invariant broke.
                for (bang in file.collectDescendantsOfType<KtPostfixExpression> {
                    it.operationToken == KtTokens.EXCLEXCL
                }) {
                    violations += "${source.display}:${bang.line()}: `!!`. Narrow the value, or fail with a sentence."
                }

                if (source.tree.isTest) continue

                val isMachine = ENGINES in source.segments
                for (declared in declaredNames(file)) {
                    val (name, kind, line) = declared
                    checked += 1
                    val problem = problemWith(declared, isMachine) ?: continue
                    if (exempt.covers(name)) continue
                    violations += "${source.display}:$line: `$name` $problem"
                }
            }
        }

        violations += exempt.leftovers(NAME)
        return GateResult(NAME, checked, exempt.count, violations)
    }

    private enum class Kind { TYPE, FUNCTION, PROPERTY, CONSTANT, PARAMETER }

    private data class Declared(
        val name: String,
        val kind: Kind,
        val line: Int,
        val isPublished: Boolean,
        val isTopLevel: Boolean,
        val isCompositionLocal: Boolean = false,
        /** The value class whose open vocabulary the property is a member of, or null where it is none. */
        val vocabulary: String? = null,
    )

    /** Every name [file] declares that something outside the declaration's own body can read. */
    private fun declaredNames(file: KtFile): List<Declared> {
        val names = mutableListOf<Declared>()

        for (type in file.collectDescendantsOfType<KtClassOrObject>()) {
            if (type is KtEnumEntry || type.isLocal || type.isPrivate()) continue
            if (type is KtObjectDeclaration && type.isCompanion() && type.nameIdentifier == null) continue
            val name = type.name ?: continue
            names += Declared(name, Kind.TYPE, type.line(), type.isPublished(), type.isTopLevel())
        }
        for (alias in file.collectDescendantsOfType<KtTypeAlias>()) {
            if (alias.isPrivate()) continue
            val name = alias.name ?: continue
            names += Declared(name, Kind.TYPE, alias.line(), alias.isPublished(), alias.isTopLevel())
        }
        for (function in file.collectDescendantsOfType<KtNamedFunction>()) {
            if (function.isLocal || function.isPrivate()) continue
            val name = function.name ?: continue
            names += Declared(name, Kind.FUNCTION, function.line(), function.isPublished(), function.isTopLevel)
        }
        for (property in file.collectDescendantsOfType<KtProperty>()) {
            if (property.isLocal || property.isPrivate()) continue
            val name = property.name ?: continue
            val isConstant = property.isTopLevel && property.hasModifier(KtTokens.CONST_KEYWORD)
            val kind = if (isConstant) Kind.CONSTANT else Kind.PROPERTY
            names +=
                Declared(
                    name,
                    kind,
                    property.line(),
                    property.isPublished(),
                    property.isTopLevel,
                    isCompositionLocal(property),
                    vocabularyOf(property),
                )
        }
        for (parameter in file.collectDescendantsOfType<KtParameter>()) {
            val owner = parameter.ownerFunction
            val isSignature =
                owner is KtNamedFunction || owner is KtPrimaryConstructor || owner is KtSecondaryConstructor
            if (!isSignature || parameter.isPrivate()) continue
            val name = parameter.name ?: continue
            names += Declared(name, Kind.PARAMETER, parameter.line(), parameter.isPublished(), isTopLevel = false)
        }
        return names
    }

    /**
     * Whether [property] holds a `CompositionLocal`: its type names one, or, where it states no type, its initializer
     * calls the factory that makes one.
     */
    private fun isCompositionLocal(property: KtProperty): Boolean {
        val type = property.typeReference?.text
        if (type != null) return type.substringBefore('<').endsWith("CompositionLocal")
        val call = property.initializer as? KtCallExpression ?: return false
        return call.calleeExpression?.text in COMPOSITION_LOCAL_FACTORIES
    }

    /**
     * The value class [property] is a member of the open vocabulary of, or null where it is none: a member is a
     * constant a value class over its spelling, a `String`, declares in its companion of the class itself, its type
     * stated or its initializer the class's constructor, as `MapException.Kind.LayerNotFound` is. A value class over
     * anything else, `SearchPause` over its `Duration`, is no vocabulary, and its default instance is `standard`.
     */
    private fun vocabularyOf(property: KtProperty): String? {
        val companion = (property.parent as? KtClassBody)?.parent as? KtObjectDeclaration ?: return null
        if (!companion.isCompanion()) return null
        val owner = (companion.parent as? KtClassBody)?.parent as? KtClass ?: return null
        if (!owner.hasModifier(KtTokens.VALUE_KEYWORD)) return null
        val spelling =
            owner.primaryConstructor
                ?.valueParameters
                ?.singleOrNull()
                ?.typeReference
                ?.text
        if (spelling != "String" && spelling != "kotlin.String") return null
        val type =
            property.typeReference?.text
                ?: (property.initializer as? KtCallExpression)?.calleeExpression?.text
                ?: return null
        return owner.name?.takeIf { it == type.substringAfterLast('.') }
    }

    /** What is wrong with [declared], or null when nothing is; [isMachine] says it is filed under `engines`. */
    private fun problemWith(
        declared: Declared,
        isMachine: Boolean,
    ): String? {
        val (name, kind) = declared
        if (kind == Kind.CONSTANT) {
            if (SCREAMING_SNAKE.matches(name)) return null
            return "is a top-level constant and is not SCREAMING_SNAKE_CASE."
        }
        if (kind != Kind.TYPE && name.length > 1 && SCREAMING_SNAKE.matches(name)) {
            return "is SCREAMING_SNAKE_CASE, which only a top-level `const val` takes."
        }

        if (declared.isCompositionLocal) {
            // Compose names a `CompositionLocal` in UpperCamelCase after `Local`, `LocalContext`, and the law holds
            // this module's to the one subject, so it is read here and never as a lowerCamelCase member.
            if (!UPPER_CAMEL.matches(name) || !name.startsWith(COMPOSITION_LOCAL_LEAD) ||
                name == COMPOSITION_LOCAL_LEAD
            ) {
                return "is a CompositionLocal and does not lead with `$COMPOSITION_LOCAL_LEAD`: `LocalMountayaClient`."
            }
        } else if (declared.vocabulary != null) {
            // A member of an open vocabulary is named as Kotlin's own libraries name a value class's constants,
            // Compose's `TextAlign.Center` and Ktor's `HttpMethod.Get`, and as the printed surface names a wire
            // enum's members, `TileLayerType.Line`, so one rule spells every member a consumer reads in this SDK.
            if (!UPPER_CAMEL.matches(name)) {
                return "is a member of the open vocabulary `${declared.vocabulary}` and is not UpperCamelCase, " +
                    "as Compose's `TextAlign.Center` is."
            }
        } else if (kind == Kind.TYPE) {
            if (!UPPER_CAMEL.matches(name)) return "is a type and is not UpperCamelCase."
            if (name.startsWith(PACKAGE_SUBJECT) && name != PACKAGE_SUBJECT) {
                return "leads with `$PACKAGE_SUBJECT`, a prefix the package already says."
            }
            if (name.endsWith(VIEW_MODEL)) {
                return "is a `ViewModel`, a lifecycle holder the consumer writes and this SDK never publishes."
            }
            if (isMachine) {
                ENGINE_SYNONYMS.firstOrNull { name.endsWith(it) }?.let { suffix ->
                    return "ends in `$suffix`, which means Engine. A machine with a lifecycle ends in `Engine`."
                }
            }
            VAGUE_SUFFIXES.firstOrNull { name.endsWith(it) }?.let { suffix ->
                return "ends in `$suffix`, which names what its author could not. Say what it is."
            }
        } else if (!LOWER_CAMEL.matches(name) && !(kind == Kind.FUNCTION && UPPER_CAMEL.matches(name))) {
            // An UpperCamelCase function is a factory named after what it returns, or a
            // composable that emits UI. The formatter holds which of the two it may be.
            return "is not lowerCamelCase."
        }

        // The word as a whole word, followed by the thing it stands in for: `NoopClock`, a
        // top-level `noopClock`. `Nullable` is not `Null`.
        if (kind == Kind.TYPE || (kind == Kind.PROPERTY && declared.isTopLevel)) {
            val words = name.camelWords()
            val lead = words.firstOrNull()
            if (words.size > 1 && lead != null && lead.lowercase() in NULL_OBJECT_WORDS) {
                return "leads with `$lead`. A null object leads with `Inert`."
            }
        }

        if (ACRONYM.containsMatchIn(name)) return "spells an acronym in capitals. An acronym is a word: `loadHttpUrl`."

        // A handler is what a consumer is handed, so the closed set reads the published surface.
        if (kind != Kind.TYPE && declared.isPublished && HANDLER.matches(name)) {
            val words = name.removePrefix("on").camelWords()
            val verb = words.firstOrNull() in HANDLER_VERBS || words.lastOrNull() in HANDLER_VERBS
            if (!verb) {
                val verbs = HANDLER_VERBS.joinToString(", ") { "on$it" }
                return "is a handler whose verb is outside the closed set: $verbs, and compounds built from them."
            }
        }
        return null
    }
}
