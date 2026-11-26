package com.mountaya.geo

/**
 * RoadAccessKind is the kind a restricted stretch of a route is shown as, on a map and in a prompt,
 * each spelled by its contract id. The entries are listed most severe first, so of two kinds the more
 * severe is the lesser, and a stretch is shown as the most severe kind among its access values.
 */
public enum class RoadAccessKind(
    /** The kind as `geo/road-access-kinds.json` spells it, `local-access`. */
    public val id: String,
) {
    /** No access at all: `no`. */
    Forbidden("forbidden"),

    /** The owner and their guests alone: `private`. */
    Private("private"),

    /** Whoever holds an individual permit: `permit`. */
    Permit("permit"),

    /** Vehicles servicing the way or the premises on it: `service`. */
    Service("service"),

    /** Traffic to a place on the way alone, never through it: `destination`, `customers` and `delivery`. */
    LocalAccess("local-access"),

    /**
     * A restriction that names no one, or whose nature is not recorded: `restricted` and `unknown`, and
     * a value the rule's table does not name, so a restriction the vocabulary gains later is never
     * shown as none.
     */
    Restricted("restricted"),
}
