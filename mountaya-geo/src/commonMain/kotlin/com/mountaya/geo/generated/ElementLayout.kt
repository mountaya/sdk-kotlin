// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew :mountaya-geo:generateElements` from sdk/elements/tokens.json of
// mountaya/schemas, contract v0.9.0, model digest
// f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// A value that is wrong here is a defect in the contract's table, and the next print overwrites
// any edit.

package com.mountaya.geo.generated

/**
 * ElementLayout is every measure the elements are laid out by, as the contract's tokens table
 * names them under `layout`, each spelled in camel case: `panel-width` is `panelWidth`. A length
 * is in density-independent pixels, the unit the table's logical pixel is on Android, and reads
 * alike in both schemes; a measure whose name ends in `columns` is a count of columns. Every other
 * size, radius, font, padding and shadow offset is each platform's own and is not here.
 */
public object ElementLayout {
    /** panelLayerCardColumns is the layout measure `panel-layer-card-columns`, a count of columns. */
    public val panelLayerCardColumns: Int = 3

    /** panelLayerCardHeight is the layout measure `panel-layer-card-height`, a length in dp. */
    public val panelLayerCardHeight: Double = 77.0

    /** panelLayerCardWidth is the layout measure `panel-layer-card-width`, a length in dp. */
    public val panelLayerCardWidth: Double = 98.0

    /** panelNarrowBottomInset is the layout measure `panel-narrow-bottom-inset`, a length in dp. */
    public val panelNarrowBottomInset: Double = 16.0

    /** panelNarrowMapWidth is the layout measure `panel-narrow-map-width`, a length in dp. */
    public val panelNarrowMapWidth: Double = 576.0

    /** panelPictureHeight is the layout measure `panel-picture-height`, a length in dp. */
    public val panelPictureHeight: Double = 52.0

    /**
     * panelPictureHeightNarrow is the layout measure `panel-picture-height-narrow`, a length in
     * dp.
     */
    public val panelPictureHeightNarrow: Double = 40.0

    /**
     * panelPreferenceCardColumns is the layout measure `panel-preference-card-columns`, a count of
     * columns.
     */
    public val panelPreferenceCardColumns: Int = 2

    /**
     * panelPreferenceCardHeight is the layout measure `panel-preference-card-height`, a length in
     * dp.
     */
    public val panelPreferenceCardHeight: Double = 57.0

    /**
     * panelPreferenceCardWidth is the layout measure `panel-preference-card-width`, a length in
     * dp.
     */
    public val panelPreferenceCardWidth: Double = 152.0

    /** panelWidth is the layout measure `panel-width`, a length in dp. */
    public val panelWidth: Double = 340.0

    /** readoutWidth is the layout measure `readout-width`, a length in dp. */
    public val readoutWidth: Double = 288.0

    /** watermarkInset is the layout measure `watermark-inset`, a length in dp. */
    public val watermarkInset: Double = -4.0

    /** watermarkWidth is the layout measure `watermark-width`, a length in dp. */
    public val watermarkWidth: Double = 180.0
}
