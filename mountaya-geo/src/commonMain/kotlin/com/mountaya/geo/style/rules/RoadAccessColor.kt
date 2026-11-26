package com.mountaya.geo

/**
 * roadAccessColor answers the colour a restricted stretch of [kind] is drawn in: its stripes along a
 * route and the disc its warning icon stands on (`map.icons.stripe-pattern`).
 *
 * A kind the vocabulary gains is an entry this rule does not compile without, never a stretch drawn in
 * no colour.
 */
public fun roadAccessColor(kind: RoadAccessKind): HexColor =
    when (kind) {
        RoadAccessKind.Forbidden -> HexColor("#e03131")
        RoadAccessKind.Private -> HexColor("#e8590c")
        RoadAccessKind.Permit -> HexColor("#9c36b5")
        RoadAccessKind.Service -> HexColor("#1971c2")
        RoadAccessKind.LocalAccess -> HexColor("#0c8599")
        RoadAccessKind.Restricted -> HexColor("#495057")
    }
