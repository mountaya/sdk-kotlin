package com.mountaya.build.generator

/**
 * The name each enum member is printed under: its words, which `sdk/SPEC.md` section 9 reads off the name the
 * model gives it, written in UpperCamelCase (SPEC.md section 3).
 *
 * A REST or Tile member's model name is its `x-enum-varnames` entry, its type's name followed by its own, so
 * printed whole it repeats the type at every use: `TileLayerType.TileLayerTypeLine`. Its words are that name with
 * the type's name taken off the front, split before each capital, `TileSourceTypeRasterDem` leaving `Raster` and
 * `Dem`; a Data API member's are its name split at each underscore, `HIKING_AND_TRAIL` leaving `HIKING`, `AND` and
 * `TRAIL`. A first word that starts with a digit keeps the last word of its type's name in front of it, since no
 * identifier starts with one: `ElevationTileSize512` is `Size512`. Kotlin writes each word with a capital and the
 * rest in lowercase, as Compose's `TextAlign.Center` and Ktor's `HttpMethod.Get` spell a value class's constants:
 * `TileLayerType.Line`, `ActivitySlug.HikingAndTrail`. The model carries no key for the words, so a name the rule
 * cannot read is refused rather than guessed at, and so is a name another member, or the type itself, already
 * prints.
 */
internal object EnumMembers {
    /** A REST or Tile member's own name, as the contract holds it: words, each a capital or a digit, then lowercase letters and digits. */
    private val OWN_WORDS = Regex("(?:[A-Z0-9][a-z0-9]*)+")

    /** One word of a REST or Tile member's own name. */
    private val WORD = Regex("[A-Z0-9][a-z0-9]*")

    /** One word of a Data API member's name, between two underscores. */
    private val DATA_WORD = Regex("[A-Za-z0-9]+")

    /** The last word of a type's name, which a member whose first word starts with a digit keeps in front. */
    private val LAST_WORD = Regex("[A-Z][a-z0-9]*$")

    /**
     * What a printed open enum and its companion declare of their own: the wire's `value`, what every class
     * overrides, the `serializer()` kotlinx.serialization adds and the companion itself. A member printed under one
     * of them, in any case, would shadow it or read as it.
     */
    private val OWN_MEMBERS = listOf("value", "toString", "equals", "hashCode", "serializer", "Companion")

    /** The name the member [name], from `x-enum-varnames`, of the REST or Tile enum [type] is printed under. */
    fun openApi(
        type: String,
        name: String,
    ): String {
        if (!name.startsWith(type)) {
            throw ModelException(
                "$type: the member $name does not start with its type's name, so no words can be read off it",
            )
        }
        val own = name.removePrefix(type)
        if (!OWN_WORDS.matches(own)) {
            throw ModelException(
                "$type: the member $name leaves \"$own\" after its type's name, which is no words: each word " +
                    "is a capital or a digit followed by lowercase letters and digits",
            )
        }
        return upperCamel(type, WORD.findAll(own).map { it.value }.toList())
    }

    /** The name the member [name] of the Data API enum [type] is printed under. */
    fun data(
        type: String,
        name: String,
    ): String {
        val words = name.split('_')
        if (words.any { !DATA_WORD.matches(it) }) {
            throw ModelException(
                "$type: the member $name is not words joined by single underscores, so no words can be read off it",
            )
        }
        return upperCamel(type, words)
    }

    /**
     * Refuses the members of [type] where two are printed under one name, or one under a name the printed type
     * declares of its own, compared without regard to case as the contract compares two members' words: on the
     * jvm two names that differ by the case of their first letter share a getter.
     */
    fun check(
        type: String,
        members: List<SchemaPrinter.EnumConstant>,
    ) {
        val seen = mutableMapOf<String, SchemaPrinter.EnumConstant>()
        for (member in members) {
            val folded = member.name.lowercase()
            OWN_MEMBERS.firstOrNull { it.lowercase() == folded }?.let { own ->
                throw ModelException(
                    "$type: the member ${member.source} is printed as ${member.name}, which reads as the `$own` " +
                        "the printed type declares of its own",
                )
            }
            seen.put(folded, member)?.let { earlier ->
                val printed =
                    if (earlier.name == member.name) member.name else "${earlier.name} and ${member.name}"
                throw ModelException(
                    "$type: the members ${earlier.source} and ${member.source} are printed as $printed, one name " +
                        "once case is set aside, and the printer renames nothing",
                )
            }
        }
    }

    private fun upperCamel(
        type: String,
        words: List<String>,
    ): String {
        val led = if (words.first().first().isDigit()) listOf(LAST_WORD.find(type)?.value ?: type) + words else words
        return led.joinToString("") { word -> word.lowercase().replaceFirstChar(Char::uppercaseChar) }
    }
}
