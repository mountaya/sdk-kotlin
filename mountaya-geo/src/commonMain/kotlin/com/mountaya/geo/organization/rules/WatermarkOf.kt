package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.generated.ElementLayout
import com.mountaya.geo.generated.artworkDrawings

/** The name of the drawing the watermark is drawn from in the glyph table's artwork. */
internal const val WATERMARK_DRAWING: String = "watermark"

/** The path of the word the watermark is labelled by. */
internal const val WATERMARK_LABEL: String = "watermark.label"

/** Mountaya's site, where the watermark links in a language it holds no page of. */
internal const val WATERMARK_ROOT: String = "https://www.mountaya.com/"

/** The pages of Mountaya's site the watermark links to, by the language the words are in. */
internal val watermarkLinks: Map<RestSchema.Language, String> =
    mapOf(
        RestSchema.Language.En to "https://www.mountaya.com/en/",
        RestSchema.Language.Fr to "https://www.mountaya.com/fr/",
    )

/**
 * The origins of Mountaya's own, on which a browser's top-level page omits the mark, since a visitor there is already
 * on Mountaya, each compared whole.
 */
internal val watermarkFirstPartyOrigins: List<String> =
    listOf("https://app.mountaya.com", "https://mountaya.com", "https://www.mountaya.com")

/**
 * watermarkWidth is how wide the frame the Mountaya watermark is drawn in is, in density-independent pixels, the
 * elements' `watermark-width`, 180 (`geo.organization.watermark`). With [watermarkHeight] and [watermarkInset] it is
 * the extent a host keeps its own interface clear of, from the top-left corner of the map's safe area, as Trail's search
 * box does.
 */
public val watermarkWidth: Double = ElementLayout.watermarkWidth

/**
 * watermarkHeight is the height of the frame the Mountaya watermark is drawn in, in density-independent pixels:
 * [watermarkWidth], 180, as tall as the drawing's box's proportion makes it, 180 × 58 / 138. The frame is
 * [watermarkWidth] wide, its top-left corner [watermarkInset], -4, from the top-left corner of the map's safe area on
 * both axes, so a host keeps its own interface clear of the mark from these three figures.
 */
public val watermarkHeight: Double = heightOf(artworkDrawings[WATERMARK_DRAWING]?.viewBox)

/**
 * watermarkInset is where the Mountaya watermark's frame has its top-left corner, from the top-left corner of the map's
 * safe area on both axes, in density-independent pixels, the elements' `watermark-inset`, -4, so the empty margin the
 * drawing keeps round the mark falls outside the safe area (`geo.organization.watermark`).
 */
public val watermarkInset: Double = ElementLayout.watermarkInset

/**
 * watermarkClearance is how far below the top of a map's safe area the Mountaya watermark's frame ends, in
 * density-independent pixels: its inset, [watermarkInset], and its [watermarkHeight]. Nothing of the SDK's own lays
 * over the frame, and a host laying its own view over that corner of the map, as Trail's search box, starts it this far
 * below the safe area's top to keep the mark clear the same way.
 */
public val watermarkClearance: Double = watermarkInset + watermarkHeight

/** The height of a frame `ElementLayout.watermarkWidth` wide holding a drawing of [box], or 0 for a box of no area. */
private fun heightOf(box: List<Double>?): Double =
    if (box == null || box.size != 4 || !(box[2] > 0.0) || !(box[3] > 0.0)) {
        0.0
    } else {
        ElementLayout.watermarkWidth * box[3] / box[2]
    }

/**
 * watermarkOf answers the Mountaya watermark a map draws once its organization's read stands at [read]
 * (`geo.organization.watermark`), or null where it draws none: while the read's first attempt is waiting, and once it
 * answered `white_label` true. Once one attempt's time budget has passed without an answer, `OrganizationRead.TimedOut`,
 * it draws the mark even while the read's further attempts go on. A read that failed, one that reached the transport's
 * own time budget among them, draws it, as a client holding no publishable key does at once, since it reads nothing. A
 * map an SDK draws is drawn in no browser document, so nothing else takes the mark off, and nothing a host states does.
 *
 * It is the drawing `watermark` of the glyph table, [watermarkWidth] wide and [watermarkHeight] tall, its top-left
 * corner [watermarkInset] from the top-left corner of the map's safe area on both axes, linked to Mountaya's site in
 * [language], the language the elements' words are in: its English and French pages, and its root in every other
 * language. Null too where the glyph table carries no such drawing.
 */
public fun watermarkOf(
    read: OrganizationRead,
    language: RestSchema.Language,
): MapWatermark? = watermarkOf(read, language, page = null)

/**
 * watermarkOf answers the Mountaya watermark a map drawn in [page] draws once its organization's read stands at
 * [read], as the published rule answers it where [page] is null (`geo.organization.watermark`).
 *
 * [page] is the browser document a map is drawn in, which a browser alone has: a top-level page on one of
 * [watermarkFirstPartyOrigins], compared whole, omits the mark whatever the read, and a framed one keeps it on every
 * origin. A map this SDK draws is in no browser document, so `MapView` hands none, and once its read has answered,
 * white label alone keeps its mark off; the clause stands here so the rule answers every case the contract states,
 * and is internal, so no host reaches it.
 */
internal fun watermarkOf(
    read: OrganizationRead,
    language: RestSchema.Language,
    page: WatermarkPage?,
): MapWatermark? {
    if (page != null && page.isTopLevel && page.origin in watermarkFirstPartyOrigins) return null
    when (read) {
        is OrganizationRead.Waiting -> return null
        is OrganizationRead.Answered -> if (read.whiteLabel) return null
        is OrganizationRead.Failed, is OrganizationRead.TimedOut, is OrganizationRead.NoKey -> Unit
    }
    // The table the module holds, never the view `GlyphDrawing.artwork` publishes, so nothing a host does to a map it
    // reads takes the mark away.
    val drawing = artworkDrawings[WATERMARK_DRAWING] ?: return null
    if (!(watermarkHeight > 0.0)) return null
    return MapWatermark(
        drawing = drawing,
        x = ElementLayout.watermarkInset,
        y = ElementLayout.watermarkInset,
        width = ElementLayout.watermarkWidth,
        height = watermarkHeight,
        link = watermarkLinks[language] ?: WATERMARK_ROOT,
        label = WATERMARK_LABEL,
    )
}
