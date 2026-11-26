package com.mountaya.geo

/**
 * ClimbCategory is the category a climb is badged with, in ascending severity, the roadbook's
 * vocabulary: Cat 4, the lightest categorized climb, through Cat 1, then hors catégorie, beyond
 * categorization. Each is spelled by its contract id and reached at [minimumScore], a climb's
 * length in meters times its average grade in percent.
 */
public enum class ClimbCategory(
    /** The category as `geo/climbs.json` spells it, `4` to `1`, then `hc`. */
    public val id: String,
    /** The least score of the category. */
    public val minimumScore: Double,
) {
    /** The lightest category, the roadbook's Cat 4, from a score of 8,000: 2 km at 4 %. */
    Category4("4", 8_000.0),

    /** The category above the lightest, the roadbook's Cat 3, from 16,000: 4 km at 4 %. */
    Category3("3", 16_000.0),

    /** The category below the hardest numbered one, the roadbook's Cat 2, from 32,000: 4 km at 8 %. */
    Category2("2", 32_000.0),

    /** The hardest numbered category, the roadbook's Cat 1, from 64,000: 6.4 km at 10 %. */
    Category1("1", 64_000.0),

    /** The category beyond categorization, the roadbook's hors catégorie, from 80,000: 8 km at 10 %. */
    HorsCategorie("hc", 80_000.0),
}
