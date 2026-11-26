package com.mountaya.build.generator

import com.mountaya.build.generator.KotlinText.kdoc

/**
 * The elements' contract data as `sdk/elements/` in the contract checkout holds it: the words, the
 * tokens, the glyph table, and the notice of the markup that table carries.
 */
internal class ElementsData(
    val words: Any?,
    val tokens: Any?,
    val glyphs: Any?,
    val notice: String,
) {
    companion object {
        /** Reads the four files, refusing one of the three tables that is no JSON. */
        fun parse(
            words: String,
            tokens: String,
            glyphs: String,
            notice: String,
        ): ElementsData {
            fun json(
                text: String,
                file: String,
            ): Any? =
                try {
                    readOrderedJson(text)
                } catch (refused: ModelException) {
                    throw ModelException("sdk/elements/$file is ${refused.message}")
                }
            return ElementsData(
                json(words, "words.json"),
                json(tokens, "tokens.json"),
                json(glyphs, "glyphs.json"),
                notice,
            )
        }
    }
}

/**
 * Prints the elements' contract data into `mountaya-geo`, as [ClientPrinter] prints the model into
 * `mountaya-client`: the words as the bundle `WordBundle.elements` publishes and as
 * `languageEndonyms`, the tokens as `ElementTokens`, the glyph table's two sets as the ones
 * `GlyphSet.marker` and `GlyphSet.control` publish and its drawings as the ones
 * `GlyphDrawing.artwork` does, and the licences the glyph markup carries into
 * `THIRD-PARTY-NOTICES.txt` at the repository root.
 *
 * The printer refuses any member of a table it has no place for, so a table the contract grows is
 * printed whole or not at all.
 */
internal object ElementsPrinter {
    /** The package the printed files declare. */
    const val PACKAGE = "com.mountaya.geo.generated"

    /** The printed package's directory, below a source set's `kotlin` directory. */
    const val DIRECTORY = "com/mountaya/geo/generated"

    /** The third-party notices file at the repository root. */
    const val NOTICES_FILE = "THIRD-PARTY-NOTICES.txt"

    /** The path a published archive carries the notices at. */
    const val NOTICES_ENTRY = "META-INF/mountaya-geo/THIRD-PARTY-NOTICES.txt"

    /** CLDR's plural categories, the keys a counted word's forms take. */
    private val PLURAL_CATEGORIES = setOf("zero", "one", "two", "few", "many", "other")

    /** The sets the glyph table holds, in the order they are printed, each with the file it prints into. */
    private val SET_FILES = linkedMapOf("marker" to "GlyphSetMarker.kt", "control" to "GlyphSetControl.kt")

    private val LANGUAGE = Regex("[a-z]{2,3}")
    private val HEX = Regex("#[0-9a-f]{6}")
    private val KEBAB_WORD = Regex("[a-z0-9]+")

    /**
     * Prints the tables, by file name, each file's header naming [contract], the tag the model names, and
     * [digest], the `modelDigest` of the files the tables are among (`ModelDigest`), as every file the client
     * printer writes names both.
     */
    fun files(
        data: ElementsData,
        contract: String,
        digest: String,
    ): Map<String, String> {
        if (!ModelDigest.isWellFormed(digest)) {
            throw ModelException("modelDigest: `$digest` is not 64 lowercase hexadecimal characters")
        }
        if (contract.isBlank() || contract.any { it.isWhitespace() }) {
            throw ModelException("sdk/model.json names no contract tag the headers can carry")
        }
        val tokens = Tokens.read(data.tokens)
        val glyphs = Glyphs.read(data.glyphs, tokens)
        val licence = licence(data.notice)
        val header = Header(contract, digest)
        val files = sortedMapOf<String, String>()
        files["WordBundleElements.kt"] = wordsFile(data.words, header)
        files["LanguageEndonyms.kt"] = endonymsFile(data.words, header)
        files["ElementTokens.kt"] = tokensFile(tokens, header)
        files["ElementLayout.kt"] = layoutFile(tokens, header)
        files["GlyphDrawingArtwork.kt"] = artworkFile(glyphs, header)
        for (set in glyphs.sets) {
            files[SET_FILES.getValue(set.key)] = glyphSetFile(set, glyphs.source, licence, header)
        }
        return files
    }

    /** What every printed file's header names: the contract's tag and the model's digest. */
    private class Header(
        val contract: String,
        val digest: String,
    )

    /** The third-party notices file: where the markup is printed and published, then the licences, verbatim. */
    fun notices(data: ElementsData): String {
        val glyphs = Glyphs.read(data.glyphs, Tokens.read(data.tokens))
        val licence = licence(data.notice)
        val heading = "${glyphs.source.packageName} (v${glyphs.source.version})"
        return buildString {
            append("Third-party licences in sdk-kotlin\n")
            append("==================================\n")
            append('\n')
            append(heading).append('\n')
            append("-".repeat(heading.length)).append('\n')
            append("The glyph sets mountaya-geo publishes hold the markup of icons from Lucide, printed from the\n")
            append("glyph table of mountaya/schemas into\n")
            append('\n')
            for (set in glyphs.sets) {
                append("  mountaya-geo/src/commonMain/kotlin/").append(DIRECTORY).append('/')
                append(SET_FILES.getValue(set.key)).append('\n')
            }
            append('\n')
            append("each carrying the licences below too, as every jar and AAR mountaya-geo publishes does at\n")
            append(NOTICES_ENTRY).append(": the ISC licence for Lucide, and the MIT\n")
            append("licence for the icons Lucide derives from Feather, which it lists.\n")
            append('\n')
            append(licence).append('\n')
        }
    }

    // The words.

    private fun wordsFile(
        json: Any?,
        header: Header,
    ): String {
        val members = members(json, "words.json", setOf("//", "endonyms", "languages"))
        val languages =
            members(members["languages"] ?: refuse("words.json carries no `languages`"), "words.json `languages`")
        if ("en" !in languages.keys) refuse("words.json carries no English, which every language falls back to")
        languages.order.forEach(::checkLanguage)
        val translated = languages.order.filter { it != "en" }
        val body =
            buildString {
                for (code in languages.order) {
                    kdoc("", listOf("The elements' words in the language `$code`."))
                    append("private val ").append(wordsName(code)).append(": JsonObject =\n")
                    append("    buildJsonObject {\n")
                    wordsTree(languages[code], code, "        ")
                    append("    }\n")
                    append('\n')
                }
                kdoc(
                    "",
                    listOf(
                        KotlinText.wrapped(
                            "elementsWordBundle is the words the elements put on screen, by section, as the " +
                                "contract's words table holds them: English, which every language falls back to, " +
                                "and the translations it carries (${translated.joinToString(", ")}). A word is a " +
                                "string, a template a string holding `{name}` placeholders, and a counted word an " +
                                "object of the forms CLDR's plural rules name, `other` among them. " +
                                "`WordBundle.elements` publishes it, and a consumer's own words are folded over it " +
                                "by `resolveBundle`.",
                            96,
                        ),
                    ),
                )
                append("internal val elementsWordBundle: WordBundle =\n")
                append("    WordBundle(\n")
                append("        english = ").append(wordsName("en")).append(",\n")
                if (translated.isEmpty()) {
                    append("        translations = emptyMap(),\n")
                } else {
                    append("        translations =\n")
                    append("            mapOf(\n")
                    for (code in translated) {
                        append("                ").append(language(code)).append(" to ").append(wordsName(code))
                        append(",\n")
                    }
                    append("            ),\n")
                }
                append("    )\n")
            }
        val imports =
            listOf(
                "com.mountaya.client.generated.RestSchema".takeIf { translated.isNotEmpty() },
                "com.mountaya.geo.WordBundle",
                "kotlinx.serialization.json.JsonObject",
                "kotlinx.serialization.json.buildJsonObject",
                "kotlinx.serialization.json.put",
                "kotlinx.serialization.json.putJsonObject",
            )
        return file("words.json", header, imports.filterNotNull(), body)
    }

    private fun wordsName(code: String): String = "words" + KotlinText.capitalized(code)

    private fun checkLanguage(code: String) {
        if (!LANGUAGE.matches(code)) refuse("words.json names a language `$code` no wire spells")
    }

    private fun language(code: String): String = "RestSchema.Language(${KotlinText.literal(code)})"

    /** Writes a section's members, each a word, a counted word or a section, as a JSON builder states them. */
    private fun StringBuilder.wordsTree(
        json: Any?,
        path: String,
        indent: String,
    ) {
        val section = json as? JsonMembers ?: refuse("words.json `$path` is no section")
        for ((key, value) in members(section, "words.json `$path`").entries) {
            val place = "$path.$key"
            if (key.isEmpty()) refuse("words.json `$path` holds a member with no name")
            when (value) {
                is String -> {
                    append(indent).append("put(").append(KotlinText.literal(key)).append(", ")
                    append(KotlinText.literal(value)).append(")\n")
                }

                is JsonMembers -> {
                    val keys = value.members.map { it.first }.filter { it != "//" }
                    val named = keys.filter { it in PLURAL_CATEGORIES }
                    if (named.isNotEmpty()) {
                        val counted =
                            named.size == keys.size && "other" in keys &&
                                value.members.all { it.first == "//" || it.second is String }
                        if (!counted) {
                            refuse(
                                "words.json `$place` names a plural form and is no counted word: a counted word's " +
                                    "keys are CLDR's categories alone, `other` among them",
                            )
                        }
                    }
                    append(indent).append("putJsonObject(").append(KotlinText.literal(key)).append(") {")
                    if (keys.isEmpty()) {
                        append("}\n")
                        continue
                    }
                    append('\n')
                    wordsTree(value, place, "$indent    ")
                    append(indent).append("}\n")
                }

                else -> {
                    refuse("words.json `$place` is ${kind(value)}: a word is a string, a counted word or a section")
                }
            }
        }
    }

    private fun endonymsFile(
        json: Any?,
        header: Header,
    ): String {
        val members = members(json, "words.json", setOf("//", "endonyms", "languages"))
        val endonyms =
            members(members["endonyms"] ?: refuse("words.json carries no `endonyms`"), "words.json `endonyms`")
        val body =
            buildString {
                kdoc(
                    "",
                    listOf(
                        KotlinText.wrapped(
                            "languageEndonyms is each label language's name in its own tongue, the same whatever " +
                                "language the reader reads: a French reader picking German reads `Deutsch`.",
                            96,
                        ),
                    ),
                )
                append("public val languageEndonyms: Map<RestSchema.Language, String> =\n")
                if (endonyms.order.isEmpty()) {
                    append("    emptyMap()\n")
                    return@buildString
                }
                append("    mapOf(\n")
                for (code in endonyms.order) {
                    checkLanguage(code)
                    val name = endonyms[code] as? String ?: refuse("words.json `endonyms.$code` is no string")
                    append(
                        "        ",
                    ).append(language(code)).append(" to ").append(KotlinText.literal(name)).append(",\n")
                }
                append("    )\n")
            }
        return file("words.json", header, listOf("com.mountaya.client.generated.RestSchema"), body)
    }

    // The tokens.

    /** The tokens table, read and checked: every token by the member it is printed as, and every measure. */
    private class Tokens(
        val colors: List<Token>,
        val shadows: List<Token>,
        val dashes: List<Token>,
        val layout: List<Measure>,
    ) {
        class Token(
            val name: String,
            val member: String,
            val light: String,
            val dark: String?,
        )

        /** A measure of the `layout` group: a length, or a count of columns where [isCount]. */
        class Measure(
            val name: String,
            val member: String,
            val value: Double,
            val isCount: Boolean,
        )

        companion object {
            /** A measure whose name ends so is a count of columns, a whole number from 1. */
            private const val COUNT_SUFFIX = "-columns"

            fun read(json: Any?): Tokens {
                val members = members(json, "tokens.json", setOf("//", "colors", "shadows", "dashes", "layout"))
                val taken = mutableSetOf<String>()

                fun group(
                    name: String,
                    value: (Any?, String) -> String,
                ): List<Token> {
                    val tokens = members(members[name] ?: return emptyList(), "tokens.json `$name`")
                    return tokens.order.map { token ->
                        val place = "tokens.json `$name.$token`"
                        val schemes = members(tokens[token], place, setOf("light", "dark"))
                        val light = schemes["light"] ?: refuse("$place states no light value")
                        val member = camelCase(token, place)
                        if (!taken.add(member)) refuse("$place prints as `$member`, which a token holds")
                        val dark = schemes["dark"]?.let { value(it, "$place dark") }
                        Token(token, member, value(light, "$place light"), dark)
                    }
                }

                val colors = group("colors") { value, place -> color(value, place) }
                val shadows =
                    group("shadows") { value, place ->
                        val layers =
                            (value as? List<*>)?.takeIf { it.isNotEmpty() } ?: refuse("$place is no list of colours")
                        layers
                            .mapIndexed { position, layer -> color(layer, "$place [$position]") }
                            .joinToString(", ", "listOf(", ")")
                    }
                val dashes =
                    group("dashes") { value, place ->
                        val lengths =
                            (value as? List<*>)?.takeIf { it.isNotEmpty() } ?: refuse("$place is no list of lengths")
                        lengths.joinToString(", ", "listOf(", ")") { length ->
                            val number = length as? Double
                            if (number == null || !number.isFinite() || number < 0) {
                                refuse("$place holds a length that is no number from 0")
                            }
                            number(number)
                        }
                    }
                val measures = members(members["layout"] ?: JsonMembers(emptyList()), "tokens.json `layout`")
                val printedAs = mutableSetOf<String>()
                val layout =
                    measures.order.map { name ->
                        val place = "tokens.json `layout.$name`"
                        val value = measures[name] as? Double
                        if (value == null || !value.isFinite()) refuse("$place is no finite number")
                        val isCount = name.endsWith(COUNT_SUFFIX)
                        if (isCount && (value < 1 || value != Math.rint(value))) {
                            refuse("$place counts columns and is no whole number from 1")
                        }
                        val member = camelCase(name, place)
                        if (!printedAs.add(member)) refuse("$place prints as `$member`, which a measure holds")
                        Measure(name, member, value, isCount)
                    }
                return Tokens(colors, shadows, dashes, layout)
            }
        }
    }

    private fun tokensFile(
        tokens: Tokens,
        header: Header,
    ): String {
        class Group(
            val tokens: List<Tokens.Token>,
            val type: String,
            val what: String,
            val map: String,
            val noun: String,
        )

        val groups =
            listOf(
                Group(tokens.colors, "ThemeToken<HexColor>", "the colour token `%s`.", "colorTokens", "colour"),
                Group(
                    tokens.shadows,
                    "ThemeToken<List<HexColor>>",
                    "the shadow token `%s`: each layer's colour, in drawing order.",
                    "shadowTokens",
                    "shadow",
                ),
                Group(
                    tokens.dashes,
                    "ThemeToken<List<Double>>",
                    "the dash token `%s`: lengths, alternately drawn and skipped.",
                    "dashTokens",
                    "dash",
                ),
            )
        val all = groups.flatMap { group -> group.tokens.map { it to group } }
        val body =
            buildString {
                kdoc(
                    "",
                    listOf(
                        KotlinText.wrapped(
                            "ElementTokens is every colour and dash the elements are drawn with, as the " +
                                "contract's tokens table names them, each spelled in camel case: `control-bg` is " +
                                "`controlBg`. The sizes, the radii, the fonts and the geometry of the shadows are " +
                                "each platform's own and are not here.",
                            96,
                        ),
                        KotlinText.wrapped(
                            "A consumer restyles the elements by copying [standard], every token as the table " +
                                "states it, with the tokens it changes: " +
                                "`ElementTokens.standard.copy { accent = ... }`. " +
                                "Two are equal where every token is.",
                            96,
                        ),
                    ),
                )
                append("public class ElementTokens private constructor(\n")
                for ((token, group) in all) {
                    kdoc("    ", listOf(KotlinText.wrapped("${token.member} is ${group.what.format(token.name)}", 96)))
                    append("    public val ")
                        .append(token.member)
                        .append(": ")
                        .append(group.type)
                        .append(",\n")
                }
                append(") {\n")
                kdoc(
                    "    ",
                    listOf(
                        "copy answers these tokens with the ones [edit] sets on the [Builder] it is handed, " +
                            "every other kept.",
                    ),
                )
                append(
                    "    public fun copy(edit: Builder.() -> Unit): ElementTokens = " +
                        "Builder(this).apply(edit).build()\n",
                )
                append('\n')
                kdoc("    ", listOf("Builder holds every token of the tokens [copy] starts from, each one settable."))
                append("    public class Builder internal constructor(\n")
                append("        tokens: ElementTokens,\n")
                append("    ) {\n")
                for ((token, group) in all) {
                    kdoc(
                        "        ",
                        listOf(KotlinText.wrapped("${token.member} is ${group.what.format(token.name)}", 92)),
                    )
                    append("        public var ").append(token.member).append(": ").append(group.type)
                    append(" = tokens.").append(token.member).append('\n')
                }
                append('\n')
                append("        internal fun build(): ElementTokens =\n")
                append("            ElementTokens(\n")
                for ((token, _) in all) {
                    append(
                        "                ",
                    ).append(token.member).append(" = ").append(token.member).append(",\n")
                }
                append("            )\n")
                append("    }\n")
                append('\n')
                append("    override fun equals(other: Any?): Boolean =\n")
                append("        other is ElementTokens")
                for ((token, _) in all) {
                    append(
                        " &&\n            ",
                    ).append(token.member).append(" == other.").append(token.member)
                }
                append('\n')
                append('\n')
                if (all.isEmpty()) {
                    append("    override fun hashCode(): Int = 0\n")
                } else {
                    append("    override fun hashCode(): Int {\n")
                    append("        var hash = 0\n")
                    for ((token, _) in all) {
                        append(
                            "        hash = 31 * hash + ",
                        ).append(token.member).append(".hashCode()\n")
                    }
                    append("        return hash\n")
                    append("    }\n")
                }
                append('\n')
                append("    override fun toString(): String =\n")
                append("        \"ElementTokens(\" +\n")
                all.forEachIndexed { position, (token, _) ->
                    val separator = if (position == 0) "" else ", "
                    append(
                        "            \"",
                    ).append(separator).append(token.member).append("=\" + ").append(token.member).append(" +\n")
                }
                append("            \")\"\n")
                append('\n')
                append("    public companion object {\n")
                kdoc("        ", listOf("standard is every token as the contract's tokens table states it."))
                append("        public val standard: ElementTokens =\n")
                append("            ElementTokens(\n")
                for ((token, _) in all) {
                    append("                ").append(token.member).append(" = ThemeToken(light = ").append(token.light)
                    if (token.dark != null) append(", dark = ").append(token.dark)
                    append("),\n")
                }
                append("            )\n")
                for (group in groups) {
                    append('\n')
                    kdoc(
                        "        ",
                        listOf("${group.map} is each ${group.noun} token's member, by the name the table gives it."),
                    )
                    append("        internal val ").append(group.map).append(": Map<String, KProperty1<ElementTokens, ")
                    append(group.type).append(">> =\n")
                    if (group.tokens.isEmpty()) {
                        append("            emptyMap()\n")
                        continue
                    }
                    append("            mapOf(\n")
                    for (token in group.tokens) {
                        append("                ").append(KotlinText.literal(token.name)).append(" to ElementTokens::")
                        append(token.member).append(",\n")
                    }
                    append("            )\n")
                }
                append("    }\n")
                append("}\n")
            }
        val imports =
            listOf(
                "com.mountaya.geo.HexColor",
                "com.mountaya.geo.ThemeToken",
                "kotlin.reflect.KProperty1",
            )
        return file("tokens.json", header, imports, body)
    }

    private fun layoutFile(
        tokens: Tokens,
        header: Header,
    ): String {
        val body =
            buildString {
                kdoc(
                    "",
                    listOf(
                        KotlinText.wrapped(
                            "ElementLayout is every measure the elements are laid out by, as the contract's tokens " +
                                "table names them under `layout`, each spelled in camel case: `panel-width` is " +
                                "`panelWidth`. A length is in density-independent pixels, the unit the table's " +
                                "logical pixel is on Android, and reads alike in both schemes; a measure whose name " +
                                "ends in `columns` is a count of columns. Every other size, radius, font, padding " +
                                "and shadow offset is each platform's own and is not here.",
                            96,
                        ),
                    ),
                )
                append("public object ElementLayout {\n")
                tokens.layout.forEachIndexed { position, measure ->
                    if (position > 0) append('\n')
                    val what = if (measure.isCount) "a count of columns" else "a length in dp"
                    val sentence = "${measure.member} is the layout measure `${measure.name}`, $what."
                    kdoc("    ", listOf(KotlinText.wrapped(sentence, 92)))
                    append("    public val ").append(measure.member)
                    if (measure.isCount) {
                        append(": Int = ").append(measure.value.toLong()).append('\n')
                    } else {
                        append(": Double = ").append(number(measure.value)).append('\n')
                    }
                }
                append("}\n")
            }
        return file("tokens.json", header, emptyList(), body)
    }

    /** Reads a colour, `{hex, opacity?}`, into the Kotlin that builds it. */
    private fun color(
        json: Any?,
        place: String,
    ): String = hexColor(members(json, place, setOf("hex", "opacity")), place)

    private fun hexColor(
        members: Members,
        place: String,
    ): String {
        val hex =
            (members["hex"] as? String)?.takeIf(HEX::matches) ?: refuse("$place holds no `hex` of six lowercase digits")
        val opacity = opacity(members, place) ?: return "HexColor(hex = ${KotlinText.literal(hex)})"
        return "HexColor(hex = ${KotlinText.literal(hex)}, opacity = ${number(opacity)})"
    }

    private fun opacity(
        members: Members,
        place: String,
    ): Double? {
        val value = members["opacity"] ?: return null
        val opacity = value as? Double
        if (opacity == null || opacity < 0 ||
            opacity > 1
        ) {
            refuse("$place holds an `opacity` that is no number from 0 to 1")
        }
        return opacity
    }

    // The glyph table.

    /** The glyph table, read and checked. */
    private class Glyphs(
        val source: Source,
        val sets: List<NamedSet>,
        val drawings: List<Drawing>,
    ) {
        class Source(
            val packageName: String,
            val version: String,
        )

        class NamedSet(
            val key: String,
            val paint: String,
            val strokeWidth: Double,
            val viewBox: Double,
            val glyphs: List<Pair<String, String>>,
        )

        class Drawing(
            val name: String,
            val viewBox: List<Double>,
            val shapes: List<String>,
            val opacity: Double?,
            val shadow: String?,
        )

        companion object {
            fun read(
                json: Any?,
                tokens: Tokens,
            ): Glyphs {
                val members = members(json, "glyphs.json", setOf("//", "source", "artwork") + SET_FILES.keys)
                val source = members(members["source"], "glyphs.json `source`", setOf("package", "version"))
                val packageName = source["package"] as? String
                val version = source["version"] as? String
                if (packageName == null || version == null) refuse("glyphs.json `source` names no package and version")
                val sets =
                    SET_FILES.keys.map { key ->
                        set(members[key] ?: refuse("glyphs.json carries no `$key` set"), key)
                    }
                val colors = tokens.colors.associate { it.name to it.member }
                val artwork =
                    (members["artwork"] ?: emptyList<Any?>()) as? List<*> ?: refuse("glyphs.json `artwork` is no list")
                val names = mutableSetOf<String>()
                val drawings =
                    artwork.map { entry ->
                        drawing(entry, colors).also { drawing ->
                            if (!names.add(
                                    drawing.name,
                                )
                            ) {
                                refuse("glyphs.json names the drawing `${drawing.name}` twice")
                            }
                        }
                    }
                return Glyphs(Source(packageName, version), sets, drawings)
            }

            private fun set(
                json: Any?,
                key: String,
            ): NamedSet {
                val place = "glyphs.json `$key`"
                val members = members(json, place, setOf("paint", "strokeWidth", "viewBox", "glyphs"))
                val paint = (members["paint"] as? String)?.takeIf { it == "fill" || it == "stroke" }
                if (paint == null) refuse("$place paints neither `fill` nor `stroke`")
                // A set is printed only where it can be drawn: a stroke set strokes at a width above 0, and a
                // fill set reads one from 0, for a shape that asks for a stroke of its own.
                val width = members["strokeWidth"] as? Double
                val drawable = width != null && width.isFinite() && if (paint == "stroke") width > 0 else width >= 0
                if (!drawable) refuse("$place states no `strokeWidth` ${if (paint == "stroke") "above" else "from"} 0")
                val box = members["viewBox"] as? Double
                if (box == null || !box.isFinite() || box <= 0) refuse("$place states no `viewBox` above 0")
                val entries = members["glyphs"] as? List<*> ?: refuse("$place holds no `glyphs` list")
                val names = mutableSetOf<String>()
                val glyphs =
                    entries.map { entry ->
                        val glyph = members(entry, "$place glyph", setOf("name", "lucide", "markup"))
                        val name = (glyph["name"] as? String)?.takeIf { it.isNotEmpty() }
                        val markup = glyph["markup"] as? String
                        if (name == null || glyph["lucide"] !is String || markup == null) {
                            refuse("$place holds a glyph with no name, Lucide name or markup")
                        }
                        if (!names.add(name)) refuse("$place names the glyph `$name` twice")
                        name to markup
                    }
                return NamedSet(key, paint, width, box, glyphs)
            }

            private fun drawing(
                json: Any?,
                colors: Map<String, String>,
            ): Drawing {
                val members =
                    members(json, "glyphs.json drawing", setOf("name", "viewBox", "shapes", "opacity", "shadow"))
                val name =
                    (members["name"] as? String)?.takeIf { it.isNotEmpty() }
                        ?: refuse("glyphs.json holds a drawing with no name")
                val place = "glyphs.json `artwork.$name`"
                val read = (members["viewBox"] as? List<*>).orEmpty()
                val box = read.mapNotNull { it as? Double }.filter { it.isFinite() }
                if (read.size != 4 || box.size != 4 || box[2] <= 0 || box[3] <= 0) {
                    refuse("$place states no `viewBox` of four numbers")
                }
                val shapes =
                    (members["shapes"] as? List<*>)?.takeIf { it.isNotEmpty() } ?: refuse("$place holds no shapes")
                val printed =
                    shapes.mapIndexed { position, shapeJson ->
                        val at = "$place shape $position"
                        val shape = members(shapeJson, at, setOf("markup", "fill", "stroke"))
                        val markup = shape["markup"] as? String ?: refuse("$at holds no markup")
                        val arguments = mutableListOf("markup = ${KotlinText.literal(markup)}")
                        shape["fill"]?.let { fill ->
                            val gradient = (fill as? JsonMembers)?.members?.any { it.first == "linear" } == true
                            arguments +=
                                if (gradient) {
                                    "fill = " + linear(members(fill, "$at fill", setOf("linear"))["linear"], "$at fill")
                                } else {
                                    "fill = " + paint(fill, "$at fill", colors, setOf("hex", "token", "opacity"))
                                }
                        }
                        shape["stroke"]?.let { stroke ->
                            val strokeMembers = members(stroke, "$at stroke", setOf("hex", "token", "opacity", "width"))
                            val width = strokeMembers["width"] as? Double
                            if (width == null || !width.isFinite() ||
                                width <= 0
                            ) {
                                refuse("$at stroke states no `width` above 0")
                            }
                            arguments +=
                                "stroke = " +
                                paint(stroke, "$at stroke", colors, setOf("hex", "token", "opacity", "width"))
                            arguments += "strokeWidth = ${number(width)}"
                        }
                        "GlyphDrawing.PaintedShape(${arguments.joinToString(", ")})"
                    }
                val opacity = opacity(members, place)
                val shadow = members["shadow"]?.let { shadow(it, "$place shadow") }
                return Drawing(name, box, printed, opacity, shadow)
            }

            /**
             * Reads a drawing's drop shadow, `{blur, dx, dy, hex, opacity?}`, its blur a Gaussian's standard
             * deviation from 0 and its offsets in the drawing's units, into its Kotlin.
             */
            private fun shadow(
                json: Any?,
                place: String,
            ): String {
                val members = members(json, place, setOf("blur", "dx", "dy", "hex", "opacity"))
                val blur = members["blur"] as? Double
                if (blur == null || !blur.isFinite() || blur < 0) refuse("$place states no `blur` from 0")
                val offsets =
                    listOf("dx", "dy").map { axis ->
                        (members[axis] as? Double)?.takeIf { it.isFinite() }
                            ?: refuse("$place states no `$axis` that is a finite number")
                    }
                return "GlyphDrawing.Shadow(blur = ${number(blur)}, dx = ${number(offsets[0])}, " +
                    "dy = ${number(offsets[1])}, color = ${hexColor(members, place)})"
            }

            /**
             * Reads a linear gradient, `{from, to, stops}`, its two ends points of the drawing's units and each
             * stop an `offset` from 0 to 1 and a colour, the offsets in order, into its Kotlin.
             */
            private fun linear(
                json: Any?,
                place: String,
            ): String {
                val members = members(json, place, setOf("from", "to", "stops"))
                val ends =
                    listOf("from", "to").map { end ->
                        val written = (members[end] as? List<*>).orEmpty()
                        val point = written.mapNotNull { (it as? Double)?.takeIf(Double::isFinite) }
                        if (point.size != 2 || written.size != 2) {
                            refuse("$place states no `$end` of two finite numbers")
                        }
                        point.joinToString(", ", "listOf(", ")", transform = ::number)
                    }
                val stops =
                    (members["stops"] as? List<*>)?.takeIf { it.isNotEmpty() }
                        ?: refuse("$place holds no stops")
                var last = 0.0
                val printed =
                    stops.mapIndexed { position, stopJson ->
                        val at = "$place stop $position"
                        val stop = members(stopJson, at, setOf("offset", "hex", "opacity"))
                        val offset = stop["offset"] as? Double
                        if (offset == null || offset < last || offset > 1) {
                            refuse("$at states no `offset` from 0 to 1 past the stop before it")
                        }
                        last = offset
                        "GlyphDrawing.GradientStop(offset = ${number(offset)}, color = ${hexColor(stop, at)})"
                    }
                return "GlyphDrawing.Paint.LinearGradient(from = ${ends[0]}, to = ${ends[1]}, " +
                    "stops = listOf(${printed.joinToString(", ")}))"
            }

            /** Reads a drawing's paint, a colour or one of the tokens' colours, into its Kotlin. */
            private fun paint(
                json: Any?,
                place: String,
                colors: Map<String, String>,
                allowed: Set<String>,
            ): String {
                val members = members(json, place, allowed)
                val hex = members["hex"]
                val token = members["token"]
                return when {
                    hex != null && token == null -> {
                        "GlyphDrawing.Paint.Color(${hexColor(members, place)})"
                    }

                    hex == null && token != null -> {
                        val member =
                            (token as? String)?.let(colors::get) ?: refuse("$place names no colour of the tokens table")
                        val opacity = opacity(members, place) ?: 1.0
                        "GlyphDrawing.Paint.Token(ElementTokens::$member, opacity = ${number(opacity)})"
                    }

                    else -> {
                        refuse("$place states neither a `hex` nor a `token`, or both")
                    }
                }
            }
        }
    }

    private fun glyphSetFile(
        set: Glyphs.NamedSet,
        source: Glyphs.Source,
        licence: String,
        header: Header,
    ): String {
        val name = set.key + "GlyphSet"
        val body =
            buildString {
                kdoc(
                    "",
                    listOf(
                        KotlinText.wrapped(
                            "$name is the ${set.key} set of the contract's glyph table, drawn from " +
                                "${source.packageName} ${source.version}: ${set.glyphs.size} glyphs, by name. " +
                                "`GlyphSet.${set.key}` publishes it.",
                            96,
                        ),
                    ),
                )
                append("internal val ").append(name).append(": GlyphSet =\n")
                append("    GlyphSet(\n")
                // The set's internal constructor skips the bounds a host's set is held to, which the read
                // above holds a printed set to; the set is named after its table.
                append("        name = ").append(KotlinText.literal(set.key)).append(",\n")
                append("        paint = GlyphSet.Paint.").append(KotlinText.capitalized(set.paint)).append(",\n")
                append("        strokeWidth = ").append(number(set.strokeWidth)).append(",\n")
                append("        viewBox = ").append(number(set.viewBox)).append(",\n")
                if (set.glyphs.isEmpty()) {
                    append("        markup = emptyMap(),\n")
                } else {
                    append("        markup =\n")
                    append("            mapOf(\n")
                    for ((glyph, markup) in set.glyphs) {
                        append("                ").append(KotlinText.literal(glyph)).append(" to ")
                        append(KotlinText.literal(markup)).append(",\n")
                    }
                    append("            ),\n")
                }
                append("    )\n")
            }
        val comment =
            buildString {
                append("// The markup is that of icons from Lucide, read from ").append(source.packageName)
                append(" at version ").append(source.version).append(", and\n")
                append("// carries the licences `sdk/elements/NOTICE` reproduces:\n")
                append("//\n")
                for (line in licence.split('\n')) append(if (line.isEmpty()) "//\n" else "// $line\n")
            }
        return file("glyphs.json", header, listOf("com.mountaya.geo.GlyphSet"), body, comment)
    }

    private fun artworkFile(
        glyphs: Glyphs,
        header: Header,
    ): String {
        val body =
            buildString {
                kdoc(
                    "",
                    listOf(
                        KotlinText.wrapped(
                            "artworkDrawings is Mountaya's own drawings the contract's glyph table carries, by the " +
                                "name it gives each. `GlyphDrawing.artwork` publishes them.",
                            96,
                        ),
                    ),
                )
                append("internal val artworkDrawings: Map<String, GlyphDrawing> =\n")
                if (glyphs.drawings.isEmpty()) {
                    append("    emptyMap()\n")
                    return@buildString
                }
                append("    mapOf(\n")
                for (drawing in glyphs.drawings) {
                    append("        ").append(KotlinText.literal(drawing.name)).append(" to\n")
                    append("            GlyphDrawing(\n")
                    append(
                        "                viewBox = listOf(",
                    ).append(drawing.viewBox.joinToString(", ", transform = ::number))
                    append("),\n")
                    append("                shapes =\n")
                    append("                    listOf(\n")
                    for (shape in drawing.shapes) append("                        ").append(shape).append(",\n")
                    append("                    ),\n")
                    drawing.opacity?.let { append("                opacity = ").append(number(it)).append(",\n") }
                    drawing.shadow?.let { append("                shadow = ").append(it).append(",\n") }
                    append("            ),\n")
                }
                append("    )\n")
            }
        val imports =
            listOf("com.mountaya.geo.GlyphDrawing") +
                listOf("com.mountaya.geo.HexColor").filter { "HexColor(" in body }
        return file("glyphs.json", header, imports, body)
    }

    /** The licences a notice carries, from its first, the ISC licence, to its end. */
    private fun licence(notice: String): String {
        val start = notice.indexOf("ISC License")
        if (start < 0 || "The MIT License" !in notice) refuse("NOTICE carries no ISC and MIT licence to reproduce")
        return notice.substring(start).trimEnd('\n')
    }

    // Shared.

    /** An object's members by key, in document order. */
    private class Members(
        val order: List<String>,
        private val values: Map<String, Any?>,
    ) {
        val keys: Set<String> get() = order.toSet()

        val entries: List<Pair<String, Any?>> get() = order.map { it to values[it] }

        operator fun get(key: String): Any? = values[key]
    }

    /**
     * Reads an object, refusing anything else, a key written twice, and, where [allowed] is given, a key
     * it does not name. A `//` member is a description, not data.
     */
    private fun members(
        json: Any?,
        place: String,
        allowed: Set<String>? = null,
    ): Members {
        val members = json as? JsonMembers ?: refuse("$place is ${kind(json)}, where an object stands")
        val seen = mutableSetOf<String>()
        for ((key, _) in members.members) {
            if (!seen.add(key)) refuse("$place writes `$key` twice")
            if (allowed != null && key !in allowed) refuse("$place holds `$key`, which this printer has no place for")
        }
        return Members(members.members.map { it.first }.filter { it != "//" }, members.members.toMap())
    }

    private fun kind(json: Any?): String =
        when (json) {
            null -> "null"
            is Boolean -> "a boolean"
            is Double -> "a number"
            is String -> "a string"
            is List<*> -> "a list"
            else -> "an object"
        }

    /**
     * Spells a kebab-case name in camel case, `control-bg` as `controlBg`, refusing one that is no plain
     * identifier.
     */
    private fun camelCase(
        name: String,
        place: String,
    ): String {
        val words = name.split('-')
        if (words.any { !KEBAB_WORD.matches(it) } || words.first().first().isDigit()) {
            refuse("$place names no Kotlin member in camel case")
        }
        val spelled = words.first() + words.drop(1).joinToString("") { KotlinText.capitalized(it) }
        if (!KotlinText.isPlain(spelled)) refuse("$place names no Kotlin member in camel case")
        return spelled
    }

    /** Spells a number as the Kotlin `Double` literal that reads back as it: a whole number with `.0`. */
    private fun number(value: Double): String =
        if (value == Math.rint(value) && kotlin.math.abs(value) < 9_007_199_254_740_992.0) {
            "${value.toLong()}.0"
        } else {
            value.toString()
        }

    /**
     * A printed file: the header naming its [source] table, the contract's tag and the model's digest [header]
     * holds, any [comment] beside it, the package, the [imports], and the [body].
     */
    private fun file(
        source: String,
        header: Header,
        imports: List<String>,
        body: String,
        comment: String? = null,
    ): String =
        buildString {
            append("// GENERATED FILE. DO NOT EDIT.\n")
            append("//\n")
            append("// Printed by `./gradlew :mountaya-geo:generateElements` from sdk/elements/").append(source)
            append(" of\n")
            append("// mountaya/schemas, contract ").append(header.contract).append(", model digest\n")
            append("// ").append(header.digest).append(".\n")
            append("// A value that is wrong here is a defect in the contract's table, and the next print overwrites\n")
            append("// any edit.\n")
            if (comment != null) {
                append("//\n")
                append(comment)
            }
            append('\n')
            append("package ").append(PACKAGE).append('\n')
            append('\n')
            for (import in imports.sorted()) append("import ").append(import).append('\n')
            if (imports.isNotEmpty()) append('\n')
            append(body)
        }

    private fun refuse(problem: String): Nothing = throw ModelException("sdk/elements/$problem")
}
