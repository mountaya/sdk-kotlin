package com.mountaya.elements

import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.ShapePaint
import com.mountaya.geo.roadAccessColor

/**
 * The side of a stripe pattern's square tile, in density-independent pixels: one stripe and its gap at
 * the zooms a route is drawn at, a power of two a renderer repeats cleanly.
 */
internal const val STRIPE_TILE_PX: Double = 16.0

/**
 * The width of each stripe of a restricted stretch's pattern, in density-independent pixels: the stripes
 * and the ground between them close to equal, so the stretch reads as hazard tape at a route's narrow
 * width.
 */
internal const val ROAD_ACCESS_STRIPE_WIDTH_PX: Double = 6.0

/**
 * What shows between the stripes of a restricted stretch: white, so the stripes of a kind read over a
 * route drawn in any colour, that kind's own included.
 */
internal const val ROAD_ACCESS_STRIPE_GROUND: String = "#ffffff"

/**
 * roadAccessStripePattern answers how a restricted stretch of [kind] is striped: a 16-pixel tile of
 * stripes 6 pixels wide rising at 45 degrees with round ends, in the kind's colour (`roadAccessColor`) on
 * white (`map.icons.stripe-pattern`).
 *
 * The stripe through the tile runs from its lower left corner to its upper right one, and the two halves
 * that complete its neighbours run past the tile's edges, so the tile repeats with no seam.
 */
internal fun roadAccessStripePattern(kind: RoadAccessKind): StripePattern {
    val size = STRIPE_TILE_PX
    val half = size / 2.0
    return StripePattern(
        size = size,
        width = ROAD_ACCESS_STRIPE_WIDTH_PX,
        color = roadAccessColor(kind).hex,
        ground = ROAD_ACCESS_STRIPE_GROUND,
        lineCap = ShapePaint.LineCap.Round,
        segments =
            listOf(
                listOf(listOf(-half, half), listOf(half, -half)),
                listOf(listOf(0.0, size), listOf(size, 0.0)),
                listOf(listOf(half, size + half), listOf(size + half, half)),
            ),
    )
}
