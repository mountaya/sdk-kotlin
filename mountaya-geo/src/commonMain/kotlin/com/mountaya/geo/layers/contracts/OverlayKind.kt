package com.mountaya.geo

/**
 * OverlayKind is how an overlay of the layers manifest draws: a classic one replaces the classic one
 * drawn before it, and an enrichment stacks over whatever is drawn. A map draws the two through two
 * calls, so a lookup may name the kind it draws.
 */
public enum class OverlayKind {
    /** An overlay that replaces the one drawn before it: the overlays of every category but one. */
    Classic,

    /** An overlay that stacks: those of the category keyed `enrichment`. */
    Enrichment,
}
