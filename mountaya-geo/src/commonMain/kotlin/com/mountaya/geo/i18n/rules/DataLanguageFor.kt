package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * dataLanguageFor turns a reader's language [tag] into the language the Data API answers a search in, spelled as the
 * API spells it: the language a map labels in for that tag (`labelLanguage`), in capitals. So the answers a search
 * returns and the labels on the map beside it are one language for every tag: `fr-CH` asks in `FR`, `de_AT` in `DE`,
 * and a tag naming none of the map's languages, or no tag, asks in `EN`. Only the ASCII letters are written in
 * capitals, the only ones a label language is spelled in.
 */
public fun dataLanguageFor(tag: String?): DataSchema.Language =
    DataSchema.Language(labelLanguage(tag).value.map { if (it in 'a'..'z') it - ('a' - 'A') else it }.joinToString(""))
