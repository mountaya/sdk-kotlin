// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew :mountaya-geo:generateElements` from sdk/elements/words.json of
// mountaya/schemas, contract v0.9.0, model digest
// f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// A value that is wrong here is a defect in the contract's table, and the next print overwrites
// any edit.

package com.mountaya.geo.generated

import com.mountaya.client.generated.RestSchema

/**
 * languageEndonyms is each label language's name in its own tongue, the same whatever language the
 * reader reads: a French reader picking German reads `Deutsch`.
 */
public val languageEndonyms: Map<RestSchema.Language, String> =
    mapOf(
        RestSchema.Language("de") to "Deutsch",
        RestSchema.Language("en") to "English",
        RestSchema.Language("es") to "Espa\u00f1ol",
        RestSchema.Language("fr") to "Fran\u00e7ais",
        RestSchema.Language("it") to "Italiano",
        RestSchema.Language("ja") to "\u65e5\u672c\u8a9e",
        RestSchema.Language("zh") to "\u4e2d\u6587",
    )
