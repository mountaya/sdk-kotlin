package com.mountaya.geo

/**
 * WatermarkPage is the browser document a map is drawn in, as the watermark reads it (`geo.organization.watermark`):
 * its serialized origin and whether it is a top-level page, no frame holding it. A map this SDK draws is in no browser
 * document, so no renderer holds one: it is the contract's browser case, which the rule answers and the conformance
 * suite hands it, and nothing a host reaches.
 */
internal class WatermarkPage(
    /** The document's serialized origin, `https://app.mountaya.com`. */
    val origin: String,
    /** Whether the document is a top-level page, no frame holding it. */
    val isTopLevel: Boolean,
)
