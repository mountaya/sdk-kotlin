package com.mountaya.geo

/**
 * MapWatermark is the Mountaya watermark a map draws (`geo.organization.watermark`): the [drawing] it is drawn from,
 * its frame in density-independent pixels from the top-left corner of the map's safe area, the part of the map the
 * device leaves visible, the address it links to and the path of the word it is labelled by for assistive technology.
 *
 * Two are equal where every member is, the numbers compared as every value of the geo layer compares them.
 */
public class MapWatermark(
    /** The drawing, `GlyphDrawing.artwork["watermark"]`. */
    public val drawing: GlyphDrawing,
    /** The left edge of its frame, from the left edge of the map's safe area. */
    public val x: Double,
    /** The top edge of its frame, from the top edge of the map's safe area. */
    public val y: Double,
    /** The width of its frame. */
    public val width: Double,
    /** The height of its frame, as the drawing's box's proportion makes it. */
    public val height: Double,
    /** The address it links to, opened outside the application. */
    public val link: String,
    /** The path of the word it is labelled by, `watermark.label`. */
    public val label: String,
) {
    override fun equals(other: Any?): Boolean =
        other is MapWatermark && other.drawing == drawing && sameNumber(other.x, x) && sameNumber(other.y, y) &&
            sameNumber(other.width, width) && sameNumber(other.height, height) && other.link == link &&
            other.label == label

    override fun hashCode(): Int {
        var hash = drawing.hashCode()
        for (number in listOf(x, y, width, height)) hash = 31 * hash + number.hashCode()
        return (31 * hash + link.hashCode()) * 31 + label.hashCode()
    }

    override fun toString(): String = "MapWatermark(x=$x, y=$y, width=$width, height=$height, link=$link, label=$label)"
}
