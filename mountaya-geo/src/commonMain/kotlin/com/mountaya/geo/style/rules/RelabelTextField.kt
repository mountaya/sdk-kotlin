package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * MAX_LABEL_DEPTH is how many arrays deep a label may nest and still be relabelled, the read of the name
 * counted as one. A label a designer writes is a few levels deep; one a stored style nests by the thousand is
 * left alone, so the walk, which reads a level per call, never ends on the runtime's stack.
 */
public const val MAX_LABEL_DEPTH: Int = 64

/**
 * A feature property that holds a name: `name` and its variants, `name:fr`, `name:latin`,
 * `name_en`, matched whole, so `place_name` is another field. The variant is any run of characters
 * but the line terminators, as an ECMAScript `.` reads one, so one pattern means one field in every
 * SDK.
 */
private val nameField = Regex("name(?:[:_][^\\n\\r\\u2028\\u2029]+)?")

/** A legacy `{field}` token: a field named in one character or more, so empty braces are text. */
private val token = Regex("\\{([^{}]+)\\}")

/** The operators whose operands are text the label draws, each walked where nothing and a string differ. */
private val textOperators = setOf("downcase", "string", "to-string", "upcase")

/** The operators that draw one of several outputs. */
private val branchOperators = setOf("case", "match", "step")

/**
 * relabelTextField answers the `text-field` a symbol layer of a map takes to label in [language],
 * the label [textField] as the style writes it relabelled, or null where the layer is left as it is.
 *
 * A read of a name field, `name` or a variant of it (`name:fr`, `name:latin`, `name_en`) by a `get`
 * or a legacy `{token}`, reads `["coalesce", ["get", "name:{language}"], ["get", "name"]]`: the
 * language's name, falling back to the default one, so a feature with no translation still draws a
 * name. The rest of the label stays: a token string is written as the `concat` that says the same
 * thing, an expression keeps its shape, and each branch of a `case`, a `match` and a `step` says the
 * name where it read one, the decisions left as written. `coalesce`, `concat`, `format`, the result of
 * a `let`, `upcase`, `downcase`, `string` and `to-string` are walked through; a `format` keeps each
 * section's options.
 *
 * A row, what a token string, a `concat` or a `format` sets side by side, says the name once: name
 * variants joined by nothing but white space read as one name, the white space going with the second,
 * and a row of nothing but names and white space is the name alone. Where nothing and a string differ
 * (an operand of `coalesce` or of a text operator, a branch under a row) the name standing for a row
 * is a `concat` of one, since a row is a string for a feature that holds no name.
 *
 * Null, the layer left as it is, where the label reads no name field; where it reads one under an
 * operator the walk does not rewrite (`slice`, a binding of a `let`, a legacy function that is not the
 * name alone at every stop); where a row holds the name more than once beside another field or
 * beside constant text that is not white space (`"{name} ({name:en})"`), since localized one by one
 * each read would say one name twice and the name alone would drop text; where the label nests more
 * than [MAX_LABEL_DEPTH] arrays deep; and where the label already reads the language, so a second
 * call writes nothing. The language is written as the wire spells it, `name:fr` for French.
 */
public fun relabelTextField(
    textField: JsonElement?,
    language: RestSchema.Language,
): JsonElement? {
    if (textField == null || textField is JsonNull) return null
    if (textField is JsonArray && nestsPast(textField, MAX_LABEL_DEPTH)) return null
    val walk = LabelWalk(language.value)
    val label =
        when (textField) {
            is JsonPrimitive -> if (textField.isString) walk.tokens(textField.content) else walk.part(textField)
            is JsonArray -> walk.output(textField, Place.Label)
            is JsonObject -> walk.function(textField)
        }
    if (!label.hasName || label.isUnreadable || label.isUnfolded) return null
    return label.value.takeUnless { sameLabel(it, textField) }
}

/**
 * Where a node of a label stands, by what the style makes of nothing there: the label itself and the
 * outputs drawn in its place, a part of a row, which writes nothing for nothing, or an operand, where
 * nothing and a string differ.
 */
private enum class Place { Label, Row, Operand }

/** Where the outputs of a branch stand, by where the branch does. */
private fun branchPlace(place: Place): Place = if (place == Place.Label) Place.Label else Place.Operand

/**
 * One node of a label as the walk reads it: whether it draws the localized name, another field, or
 * constant text that is not white space; whether it is the name itself and nothing more; whether it
 * is a row whose names fold into no single one; whether it reads a name in a form the walk does not
 * rewrite; and the node as the relabelled label states it. A plain class, so a format's options are
 * keyed by the very part they belong to.
 */
private class LabelPart(
    val value: JsonElement,
    val hasName: Boolean = false,
    val hasOther: Boolean = false,
    val hasText: Boolean = false,
    val isName: Boolean = false,
    val isUnfolded: Boolean = false,
    val isUnreadable: Boolean = false,
)

/** Whether [value] is a constant the label draws as text: a string not white space alone, a number or a boolean. */
private fun isDrawnText(value: JsonElement): Boolean =
    value is JsonPrimitive && (if (value.isString) trimWhiteSpace(value.content).isNotEmpty() else value !is JsonNull)

/** Whether the value a `literal` states is drawn as text: a constant [isDrawnText] reads so, an array or an object. */
private fun isDrawnLiteral(value: JsonElement?): Boolean =
    value != null && (isDrawnText(value) || value is JsonArray || value is JsonObject)

/** Whether [node] reads a name field off the feature: `["get", "name:latin"]`. */
private fun isNameRead(node: JsonElement): Boolean {
    if (node !is JsonArray || node.size != 2) return false
    val operator = node[0]
    val field = node[1]
    return operator is JsonPrimitive && operator.isString && operator.content == "get" &&
        field is JsonPrimitive && field.isString && nameField.matches(field.content)
}

/** Whether [node] reads a name field anywhere inside it, through arrays. */
private fun readsName(node: JsonElement): Boolean = node is JsonArray && (isNameRead(node) || node.any(::readsName))

/** The operator [node] opens with, or null where it states none. */
private fun operatorOf(node: JsonArray): String? =
    (node.firstOrNull() as? JsonPrimitive)?.takeIf { it.isString }?.content

/** Whether [value] is a string. */
private fun isString(value: JsonElement): Boolean = value is JsonPrimitive && value.isString

/** The walk over one label, for one language. */
private class LabelWalk(
    language: String,
) {
    private val localized: JsonElement =
        JsonArray(
            listOf(
                JsonPrimitive("coalesce"),
                JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("name:$language"))),
                JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("name"))),
            ),
        )

    /** The localized name, a new part each time it is stated. */
    fun name(): LabelPart = LabelPart(localized, hasName = true, isName = true)

    /** A node that is not a name, built over the parts under it. */
    fun part(
        value: JsonElement,
        under: List<LabelPart> = emptyList(),
    ): LabelPart =
        LabelPart(
            value,
            hasName = under.any { it.hasName },
            hasOther = under.any { it.hasOther },
            hasText = under.any { it.hasText } || isDrawnText(value),
            isUnfolded = under.any { it.isUnfolded },
            isUnreadable = under.any { it.isUnreadable },
        )

    private fun other(value: JsonElement): LabelPart = part(value).copy(hasOther = true)

    private fun unreadable(value: JsonElement): LabelPart = part(value).copy(isUnreadable = true)

    private fun unfolded(
        value: JsonElement,
        under: List<LabelPart>,
    ): LabelPart = part(value, under).copy(isUnfolded = true)

    private fun LabelPart.copy(
        hasText: Boolean = this.hasText,
        hasOther: Boolean = this.hasOther,
        isUnfolded: Boolean = this.isUnfolded,
        isUnreadable: Boolean = this.isUnreadable,
    ): LabelPart = LabelPart(value, hasName, hasOther, hasText, isName, isUnfolded, isUnreadable)

    /**
     * The names a row prints side by side folded into one, or null for a row that still holds the
     * name more than once: two names with nothing but white space between them read as the first.
     */
    private fun joinedNames(parts: List<LabelPart>): List<LabelPart>? {
        val folded = mutableListOf<LabelPart>()
        var text = mutableListOf<LabelPart>()
        var before: LabelPart? = null
        for (part in parts) {
            if (isString(part.value) && !part.hasName) {
                text += part
                continue
            }
            val isSameName =
                part.isName && before?.isName == true &&
                    text.all { trimWhiteSpace((it.value as JsonPrimitive).content).isEmpty() }
            if (!isSameName) {
                folded += text
                folded += part
                before = part
            }
            text = mutableListOf()
        }
        folded += text
        return if (folded.count { it.hasName } > 1) null else folded
    }

    /** Whether a row draws nothing but names and white space, each read in a form the walk rewrites. */
    private fun isNamesAlone(parts: List<LabelPart>): Boolean =
        parts.all {
            !it.hasOther && !it.hasText &&
                !it.isUnreadable
        }

    private fun row(
        operator: String,
        values: List<JsonElement>,
    ): JsonElement = JsonArray(listOf(JsonPrimitive(operator)) + values)

    /** Parts as one row: the name alone where that is all they come to. */
    private fun concat(
        parts: List<LabelPart>,
        place: Place,
    ): LabelPart {
        val folded = joinedNames(parts)
        if (folded == null && !isNamesAlone(parts)) return unfolded(row("concat", parts.map { it.value }), parts)
        val stated = folded ?: listOf(name())
        val only = stated.singleOrNull()
        if (only == null || !only.isName) return part(row("concat", stated.map { it.value }), stated)
        return if (place == Place.Operand) part(row("concat", listOf(only.value)), listOf(only)) else only
    }

    /** A legacy token string: each `{field}` a read, each stretch of text itself. */
    fun tokens(text: String): LabelPart {
        val parts = mutableListOf<LabelPart>()
        var from = 0
        for (match in token.findAll(text)) {
            parts += part(JsonPrimitive(text.substring(from, match.range.first)))
            val field = match.groupValues[1]
            parts += if (nameField.matches(field)) name() else other(row("get", listOf(JsonPrimitive(field))))
            from = match.range.last + 1
        }
        parts += part(JsonPrimitive(text.substring(from)))
        return concat(
            parts.filterNot { isString(it.value) && (it.value as JsonPrimitive).content.isEmpty() },
            Place.Label,
        )
    }

    /** A `format`, whose arguments are sections: a text, then the options it is drawn with where it states any. */
    private fun format(sections: List<JsonElement>): LabelPart {
        val options = HashMap<LabelPart, JsonElement>()
        val parts = mutableListOf<LabelPart>()
        for (argument in sections) {
            val last = parts.lastOrNull()
            if (argument is JsonObject && last != null && last !in options) {
                options[last] = argument
            } else {
                parts += output(argument, Place.Row)
            }
        }

        fun stated(part: LabelPart): List<JsonElement> = listOfNotNull(part.value, options[part])

        val folded = joinedNames(parts)
        if (folded != null) return part(row("format", folded.flatMap(::stated)), folded)
        if (!isNamesAlone(parts)) return unfolded(row("format", parts.flatMap(::stated)), parts)
        val name = name()
        parts.firstOrNull { it.hasName }?.let { first -> options[first]?.let { options[name] = it } }
        return part(row("format", stated(name)), listOf(name))
    }

    /** The outputs of a branch walked, its decisions left as written. */
    private fun branches(
        operator: String,
        operands: List<JsonElement>,
        place: Place,
    ): LabelPart {
        val last = operands.lastIndex
        val outputs = mutableListOf<LabelPart>()
        val stated =
            operands.mapIndexed { index, operand ->
                val isOutput =
                    if (operator == "match") {
                        index == last || (index > 0 && index % 2 == 0)
                    } else {
                        index % 2 == 1 || (operator == "case" && index == last)
                    }
                if (!isOutput) {
                    operand
                } else {
                    output(operand, branchPlace(place)).also { outputs += it }.value
                }
            }
        return part(row(operator, stated), outputs)
    }

    /** A `let`: its result walked, its bindings as written, and a name a binding reads unreadable. */
    private fun result(
        operands: List<JsonElement>,
        place: Place,
    ): LabelPart {
        val bindings = operands.dropLast(1)
        if (bindings.any(::readsName)) return unreadable(row("let", operands))
        val result = output(operands.last(), branchPlace(place))
        return part(row("let", bindings + result.value), listOf(result))
    }

    /** One node whose value the label draws, standing at [place]. */
    fun output(
        node: JsonElement,
        place: Place,
    ): LabelPart {
        if (node !is JsonArray) return part(node)
        if (isNameRead(node)) return name()
        val operator = operatorOf(node) ?: return part(node)
        val operands = node.drop(1)
        return when {
            operator == "literal" -> {
                part(node).copy(hasText = isDrawnLiteral(operands.firstOrNull()))
            }

            operator == "concat" -> {
                concat(operands.map { output(it, Place.Row) }, place)
            }

            operator == "coalesce" -> {
                val walked = operands.map { output(it, Place.Operand) }
                val parts =
                    walked.filterIndexed { index, part ->
                        !(
                            part.isName &&
                                walked.getOrNull(index - 1)?.isName == true
                        )
                    }
                val only = parts.singleOrNull()
                if (only != null && only.isName) only else part(row("coalesce", parts.map { it.value }), parts)
            }

            operator == "format" -> {
                format(operands)
            }

            operator in branchOperators -> {
                branches(operator, operands, place)
            }

            operator == "let" && operands.isNotEmpty() -> {
                result(operands, place)
            }

            operator in textOperators -> {
                val parts = operands.map { output(it, Place.Operand) }
                part(row(operator, parts.map { it.value }), parts)
            }

            readsName(node) -> {
                unreadable(node)
            }

            else -> {
                other(node)
            }
        }
    }

    /**
     * A legacy function, `{"stops": [[zoom, output]]}`, which takes no expression for an output, so a
     * name cannot be localized inside it: the name alone where every output is, else left as written.
     */
    fun function(textField: JsonObject): LabelPart {
        val stops = textField["stops"] as? JsonArray ?: JsonArray(emptyList())
        val outputs =
            stops.map { stop ->
                val output = (stop as? JsonArray)?.getOrNull(1)
                if (output is JsonPrimitive && output.isString) tokens(output.content) else unreadable(stop)
            }
        return if (outputs.isNotEmpty() && outputs.all { it.isName }) name() else unreadable(textField)
    }
}

/** Whether [expression] nests arrays deeper than [limit], read with a stack of its own and never by recursion. */
private fun nestsPast(
    expression: JsonArray,
    limit: Int,
): Boolean {
    val pending = ArrayDeque<Pair<Int, JsonArray>>()
    pending.addLast(1 to expression)
    while (pending.isNotEmpty()) {
        val (depth, node) = pending.removeLast()
        if (depth > limit) return true
        for (child in node) if (child is JsonArray) pending.addLast(depth + 1 to child)
    }
    return false
}

/** Whether two labels state the same thing, member by member, read with a stack of its own at any depth. */
private fun sameLabel(
    a: JsonElement,
    b: JsonElement,
): Boolean {
    val pending = ArrayDeque<Pair<JsonElement, JsonElement>>()
    pending.addLast(a to b)
    while (pending.isNotEmpty()) {
        val (left, right) = pending.removeLast()
        when {
            left is JsonArray && right is JsonArray -> {
                if (left.size != right.size) return false
                left.indices.forEach { pending.addLast(left[it] to right[it]) }
            }

            left is JsonObject && right is JsonObject -> {
                if (left.keys != right.keys) return false
                left.forEach { (key, value) -> pending.addLast(value to right.getValue(key)) }
            }

            left is JsonPrimitive && right is JsonPrimitive -> {
                if (left != right) return false
            }

            else -> {
                return false
            }
        }
    }
    return true
}
