package com.mountaya.elements

import com.mountaya.geo.trimWhiteSpace

/**
 * The credit every map carries, the `mountayaCredit` `map/style-attribution.json` declares: markup a renderer
 * shows as it shows its own, a link to Mountaya. The `&copy;` entity is the one the Tile API writes on every
 * Mountaya source, so a source stating it adds no second credit.
 */
internal const val MOUNTAYA_CREDIT: String = "<a href=\"https://mountaya.com/\" target=\"_blank\">&copy; Mountaya</a>"

/**
 * mapCredits is the credits a map shows (`map.style.attribution`): Mountaya's first, then each source the map
 * draws by the credit it states, in the order [sources] lists them.
 *
 * Each credit is shown once, compared by its exact text, so a Mountaya source stating Mountaya's credit adds
 * nothing and two spellings of one credit are two credits. A credit that is absent, empty or white space alone
 * is left out, and so is the credit of a source no layer draws.
 */
internal fun mapCredits(sources: List<CreditedSource>): List<String> {
    val credits = mutableListOf(MOUNTAYA_CREDIT)
    for (source in sources) {
        val credit = source.attribution ?: continue
        if (!source.isDrawn || trimWhiteSpace(credit).isEmpty() || credit in credits) continue
        credits += credit
    }
    return credits
}
