package com.mountaya.elements

import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor

/**
 * PinImages is the image of each look the markers shown carry, as the renderer shows a marker as its pin (`map.marker.pin`):
 * drawn once for its look, the pin its colour and glyph describe on the accent where it names no colour, shared by
 * every marker of that look, and kept for as long as a marker shown carries it, so a marker moved, added or taken off
 * draws no look shown already (`pin-images` in the contract's `sdk/hot-paths.yaml`). A drawing rasterizes the disc, the
 * ring and the glyph anew, which is what it spares: [draw] is handed each look the first time a marker carries it.
 *
 * It is driven from the one thread its renderer is.
 */
internal class PinImages<Image>(
    private val draw: (Look) -> Image,
) {
    /** Look is what tells two pins apart: what the pin is drawn as, the accent where its disc takes it, and the screen's density. */
    internal data class Look(
        /** What the pin is drawn as. */
        val pin: MarkerPin,
        /** The accent its disc is drawn in, where it names no colour of its own, or null where it names one. */
        val accent: HexColor?,
        /** The density of the screen it is drawn for. */
        val densityDpi: Int,
    )

    /** The image of each look a marker shown carries. */
    private var kept: Map<Look, Image> = emptyMap()

    /** How many images were drawn, one look at a time. */
    var drawn: Int = 0
        private set

    /**
     * The image [marker] is shown as, its glyph drawn from [glyphs], on [accent] where it names no colour, at [densityDpi]:
     * the image of its look a marker shown carries already, or one drawn for it, which is not kept.
     */
    fun imageOf(
        marker: MapMarker,
        glyphs: GlyphSet,
        accent: HexColor,
        densityDpi: Int,
    ): Image {
        val look = lookOf(marker, glyphs, accent, densityDpi)
        return kept[look] ?: drawOne(look)
    }

    /**
     * The images [markers], the markers shown now, are shown as, in their order, as [imageOf] draws each: a look no marker
     * shown before carried is drawn once and kept, and every look no marker carries now is let go.
     */
    fun show(
        markers: List<MapMarker>,
        glyphs: GlyphSet,
        accent: HexColor,
        densityDpi: Int,
    ): List<Image> {
        val images = LinkedHashMap<Look, Image>()
        val shown =
            markers.map { marker ->
                val look = lookOf(marker, glyphs, accent, densityDpi)
                images.getOrPut(look) { kept[look] ?: drawOne(look) }
            }
        kept = images
        return shown
    }

    /** The look [marker]'s pin has: what it is drawn as, the accent where its disc takes it, and the screen's density. */
    private fun lookOf(
        marker: MapMarker,
        glyphs: GlyphSet,
        accent: HexColor,
        densityDpi: Int,
    ): Look {
        val pin = describeMarkerPin(marker.color, marker.glyph, glyphs)
        return Look(pin, accent.takeIf { pin.fill == null }, densityDpi)
    }

    /** Draws the pin [look] describes, counted. */
    private fun drawOne(look: Look): Image {
        drawn += 1
        return draw(look)
    }
}
