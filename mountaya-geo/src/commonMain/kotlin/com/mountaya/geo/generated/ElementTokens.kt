// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew :mountaya-geo:generateElements` from sdk/elements/tokens.json of
// mountaya/schemas, contract v0.9.0, model digest
// f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// A value that is wrong here is a defect in the contract's table, and the next print overwrites
// any edit.

package com.mountaya.geo.generated

import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeToken
import kotlin.reflect.KProperty1

/**
 * ElementTokens is every colour and dash the elements are drawn with, as the contract's tokens
 * table names them, each spelled in camel case: `control-bg` is `controlBg`. The sizes, the radii,
 * the fonts and the geometry of the shadows are each platform's own and are not here.
 *
 * A consumer restyles the elements by copying [standard], every token as the table states it, with
 * the tokens it changes: `ElementTokens.standard.copy { accent = ... }`. Two are equal where every
 * token is.
 */
public class ElementTokens private constructor(
    /** accent is the colour token `accent`. */
    public val accent: ThemeToken<HexColor>,
    /** controlBg is the colour token `control-bg`. */
    public val controlBg: ThemeToken<HexColor>,
    /** controlBgHover is the colour token `control-bg-hover`. */
    public val controlBgHover: ThemeToken<HexColor>,
    /** controlFg is the colour token `control-fg`. */
    public val controlFg: ThemeToken<HexColor>,
    /** controlFgDisabled is the colour token `control-fg-disabled`. */
    public val controlFgDisabled: ThemeToken<HexColor>,
    /** dimmed is the colour token `dimmed`. */
    public val dimmed: ThemeToken<HexColor>,
    /** focusRing is the colour token `focus-ring`. */
    public val focusRing: ThemeToken<HexColor>,
    /** panelBg is the colour token `panel-bg`. */
    public val panelBg: ThemeToken<HexColor>,
    /** panelBorder is the colour token `panel-border`. */
    public val panelBorder: ThemeToken<HexColor>,
    /** panelFg is the colour token `panel-fg`. */
    public val panelFg: ThemeToken<HexColor>,
    /** profileCursor is the colour token `profile-cursor`. */
    public val profileCursor: ThemeToken<HexColor>,
    /** profileGridColor is the colour token `profile-grid-color`. */
    public val profileGridColor: ThemeToken<HexColor>,
    /** profileLitFill is the colour token `profile-lit-fill`. */
    public val profileLitFill: ThemeToken<HexColor>,
    /** profileNavigatorHandle is the colour token `profile-navigator-handle`. */
    public val profileNavigatorHandle: ThemeToken<HexColor>,
    /** profileNavigatorVeil is the colour token `profile-navigator-veil`. */
    public val profileNavigatorVeil: ThemeToken<HexColor>,
    /** profileTooltipBg is the colour token `profile-tooltip-bg`. */
    public val profileTooltipBg: ThemeToken<HexColor>,
    /** profileTooltipFg is the colour token `profile-tooltip-fg`. */
    public val profileTooltipFg: ThemeToken<HexColor>,
    /** profileWaypointColor is the colour token `profile-waypoint-color`. */
    public val profileWaypointColor: ThemeToken<HexColor>,
    /** profileWaypointLine is the colour token `profile-waypoint-line`. */
    public val profileWaypointLine: ThemeToken<HexColor>,
    /** tableBorder is the colour token `table-border`. */
    public val tableBorder: ThemeToken<HexColor>,
    /** tableRowHover is the colour token `table-row-hover`. */
    public val tableRowHover: ThemeToken<HexColor>,
    /** tableSparkBase is the colour token `table-spark-base`. */
    public val tableSparkBase: ThemeToken<HexColor>,
    /** tooltipBg is the colour token `tooltip-bg`. */
    public val tooltipBg: ThemeToken<HexColor>,
    /** tooltipFg is the colour token `tooltip-fg`. */
    public val tooltipFg: ThemeToken<HexColor>,
    /** waypointCardMarkColor is the colour token `waypoint-card-mark-color`. */
    public val waypointCardMarkColor: ThemeToken<HexColor>,
    /** waypointCardMarkFg is the colour token `waypoint-card-mark-fg`. */
    public val waypointCardMarkFg: ThemeToken<HexColor>,
    /** controlShadow is the shadow token `control-shadow`: each layer's colour, in drawing order. */
    public val controlShadow: ThemeToken<List<HexColor>>,
    /**
     * profileTooltipShadow is the shadow token `profile-tooltip-shadow`: each layer's colour, in
     * drawing order.
     */
    public val profileTooltipShadow: ThemeToken<List<HexColor>>,
    /**
     * profileCursorDash is the dash token `profile-cursor-dash`: lengths, alternately drawn and
     * skipped.
     */
    public val profileCursorDash: ThemeToken<List<Double>>,
) {
    /** copy answers these tokens with the ones [edit] sets on the [Builder] it is handed, every other kept. */
    public fun copy(edit: Builder.() -> Unit): ElementTokens = Builder(this).apply(edit).build()

    /** Builder holds every token of the tokens [copy] starts from, each one settable. */
    public class Builder internal constructor(
        tokens: ElementTokens,
    ) {
        /** accent is the colour token `accent`. */
        public var accent: ThemeToken<HexColor> = tokens.accent
        /** controlBg is the colour token `control-bg`. */
        public var controlBg: ThemeToken<HexColor> = tokens.controlBg
        /** controlBgHover is the colour token `control-bg-hover`. */
        public var controlBgHover: ThemeToken<HexColor> = tokens.controlBgHover
        /** controlFg is the colour token `control-fg`. */
        public var controlFg: ThemeToken<HexColor> = tokens.controlFg
        /** controlFgDisabled is the colour token `control-fg-disabled`. */
        public var controlFgDisabled: ThemeToken<HexColor> = tokens.controlFgDisabled
        /** dimmed is the colour token `dimmed`. */
        public var dimmed: ThemeToken<HexColor> = tokens.dimmed
        /** focusRing is the colour token `focus-ring`. */
        public var focusRing: ThemeToken<HexColor> = tokens.focusRing
        /** panelBg is the colour token `panel-bg`. */
        public var panelBg: ThemeToken<HexColor> = tokens.panelBg
        /** panelBorder is the colour token `panel-border`. */
        public var panelBorder: ThemeToken<HexColor> = tokens.panelBorder
        /** panelFg is the colour token `panel-fg`. */
        public var panelFg: ThemeToken<HexColor> = tokens.panelFg
        /** profileCursor is the colour token `profile-cursor`. */
        public var profileCursor: ThemeToken<HexColor> = tokens.profileCursor
        /** profileGridColor is the colour token `profile-grid-color`. */
        public var profileGridColor: ThemeToken<HexColor> = tokens.profileGridColor
        /** profileLitFill is the colour token `profile-lit-fill`. */
        public var profileLitFill: ThemeToken<HexColor> = tokens.profileLitFill
        /** profileNavigatorHandle is the colour token `profile-navigator-handle`. */
        public var profileNavigatorHandle: ThemeToken<HexColor> = tokens.profileNavigatorHandle
        /** profileNavigatorVeil is the colour token `profile-navigator-veil`. */
        public var profileNavigatorVeil: ThemeToken<HexColor> = tokens.profileNavigatorVeil
        /** profileTooltipBg is the colour token `profile-tooltip-bg`. */
        public var profileTooltipBg: ThemeToken<HexColor> = tokens.profileTooltipBg
        /** profileTooltipFg is the colour token `profile-tooltip-fg`. */
        public var profileTooltipFg: ThemeToken<HexColor> = tokens.profileTooltipFg
        /** profileWaypointColor is the colour token `profile-waypoint-color`. */
        public var profileWaypointColor: ThemeToken<HexColor> = tokens.profileWaypointColor
        /** profileWaypointLine is the colour token `profile-waypoint-line`. */
        public var profileWaypointLine: ThemeToken<HexColor> = tokens.profileWaypointLine
        /** tableBorder is the colour token `table-border`. */
        public var tableBorder: ThemeToken<HexColor> = tokens.tableBorder
        /** tableRowHover is the colour token `table-row-hover`. */
        public var tableRowHover: ThemeToken<HexColor> = tokens.tableRowHover
        /** tableSparkBase is the colour token `table-spark-base`. */
        public var tableSparkBase: ThemeToken<HexColor> = tokens.tableSparkBase
        /** tooltipBg is the colour token `tooltip-bg`. */
        public var tooltipBg: ThemeToken<HexColor> = tokens.tooltipBg
        /** tooltipFg is the colour token `tooltip-fg`. */
        public var tooltipFg: ThemeToken<HexColor> = tokens.tooltipFg
        /** waypointCardMarkColor is the colour token `waypoint-card-mark-color`. */
        public var waypointCardMarkColor: ThemeToken<HexColor> = tokens.waypointCardMarkColor
        /** waypointCardMarkFg is the colour token `waypoint-card-mark-fg`. */
        public var waypointCardMarkFg: ThemeToken<HexColor> = tokens.waypointCardMarkFg
        /** controlShadow is the shadow token `control-shadow`: each layer's colour, in drawing order. */
        public var controlShadow: ThemeToken<List<HexColor>> = tokens.controlShadow
        /**
         * profileTooltipShadow is the shadow token `profile-tooltip-shadow`: each layer's colour, in
         * drawing order.
         */
        public var profileTooltipShadow: ThemeToken<List<HexColor>> = tokens.profileTooltipShadow
        /**
         * profileCursorDash is the dash token `profile-cursor-dash`: lengths, alternately drawn and
         * skipped.
         */
        public var profileCursorDash: ThemeToken<List<Double>> = tokens.profileCursorDash

        internal fun build(): ElementTokens =
            ElementTokens(
                accent = accent,
                controlBg = controlBg,
                controlBgHover = controlBgHover,
                controlFg = controlFg,
                controlFgDisabled = controlFgDisabled,
                dimmed = dimmed,
                focusRing = focusRing,
                panelBg = panelBg,
                panelBorder = panelBorder,
                panelFg = panelFg,
                profileCursor = profileCursor,
                profileGridColor = profileGridColor,
                profileLitFill = profileLitFill,
                profileNavigatorHandle = profileNavigatorHandle,
                profileNavigatorVeil = profileNavigatorVeil,
                profileTooltipBg = profileTooltipBg,
                profileTooltipFg = profileTooltipFg,
                profileWaypointColor = profileWaypointColor,
                profileWaypointLine = profileWaypointLine,
                tableBorder = tableBorder,
                tableRowHover = tableRowHover,
                tableSparkBase = tableSparkBase,
                tooltipBg = tooltipBg,
                tooltipFg = tooltipFg,
                waypointCardMarkColor = waypointCardMarkColor,
                waypointCardMarkFg = waypointCardMarkFg,
                controlShadow = controlShadow,
                profileTooltipShadow = profileTooltipShadow,
                profileCursorDash = profileCursorDash,
            )
    }

    override fun equals(other: Any?): Boolean =
        other is ElementTokens &&
            accent == other.accent &&
            controlBg == other.controlBg &&
            controlBgHover == other.controlBgHover &&
            controlFg == other.controlFg &&
            controlFgDisabled == other.controlFgDisabled &&
            dimmed == other.dimmed &&
            focusRing == other.focusRing &&
            panelBg == other.panelBg &&
            panelBorder == other.panelBorder &&
            panelFg == other.panelFg &&
            profileCursor == other.profileCursor &&
            profileGridColor == other.profileGridColor &&
            profileLitFill == other.profileLitFill &&
            profileNavigatorHandle == other.profileNavigatorHandle &&
            profileNavigatorVeil == other.profileNavigatorVeil &&
            profileTooltipBg == other.profileTooltipBg &&
            profileTooltipFg == other.profileTooltipFg &&
            profileWaypointColor == other.profileWaypointColor &&
            profileWaypointLine == other.profileWaypointLine &&
            tableBorder == other.tableBorder &&
            tableRowHover == other.tableRowHover &&
            tableSparkBase == other.tableSparkBase &&
            tooltipBg == other.tooltipBg &&
            tooltipFg == other.tooltipFg &&
            waypointCardMarkColor == other.waypointCardMarkColor &&
            waypointCardMarkFg == other.waypointCardMarkFg &&
            controlShadow == other.controlShadow &&
            profileTooltipShadow == other.profileTooltipShadow &&
            profileCursorDash == other.profileCursorDash

    override fun hashCode(): Int {
        var hash = 0
        hash = 31 * hash + accent.hashCode()
        hash = 31 * hash + controlBg.hashCode()
        hash = 31 * hash + controlBgHover.hashCode()
        hash = 31 * hash + controlFg.hashCode()
        hash = 31 * hash + controlFgDisabled.hashCode()
        hash = 31 * hash + dimmed.hashCode()
        hash = 31 * hash + focusRing.hashCode()
        hash = 31 * hash + panelBg.hashCode()
        hash = 31 * hash + panelBorder.hashCode()
        hash = 31 * hash + panelFg.hashCode()
        hash = 31 * hash + profileCursor.hashCode()
        hash = 31 * hash + profileGridColor.hashCode()
        hash = 31 * hash + profileLitFill.hashCode()
        hash = 31 * hash + profileNavigatorHandle.hashCode()
        hash = 31 * hash + profileNavigatorVeil.hashCode()
        hash = 31 * hash + profileTooltipBg.hashCode()
        hash = 31 * hash + profileTooltipFg.hashCode()
        hash = 31 * hash + profileWaypointColor.hashCode()
        hash = 31 * hash + profileWaypointLine.hashCode()
        hash = 31 * hash + tableBorder.hashCode()
        hash = 31 * hash + tableRowHover.hashCode()
        hash = 31 * hash + tableSparkBase.hashCode()
        hash = 31 * hash + tooltipBg.hashCode()
        hash = 31 * hash + tooltipFg.hashCode()
        hash = 31 * hash + waypointCardMarkColor.hashCode()
        hash = 31 * hash + waypointCardMarkFg.hashCode()
        hash = 31 * hash + controlShadow.hashCode()
        hash = 31 * hash + profileTooltipShadow.hashCode()
        hash = 31 * hash + profileCursorDash.hashCode()
        return hash
    }

    override fun toString(): String =
        "ElementTokens(" +
            "accent=" + accent +
            ", controlBg=" + controlBg +
            ", controlBgHover=" + controlBgHover +
            ", controlFg=" + controlFg +
            ", controlFgDisabled=" + controlFgDisabled +
            ", dimmed=" + dimmed +
            ", focusRing=" + focusRing +
            ", panelBg=" + panelBg +
            ", panelBorder=" + panelBorder +
            ", panelFg=" + panelFg +
            ", profileCursor=" + profileCursor +
            ", profileGridColor=" + profileGridColor +
            ", profileLitFill=" + profileLitFill +
            ", profileNavigatorHandle=" + profileNavigatorHandle +
            ", profileNavigatorVeil=" + profileNavigatorVeil +
            ", profileTooltipBg=" + profileTooltipBg +
            ", profileTooltipFg=" + profileTooltipFg +
            ", profileWaypointColor=" + profileWaypointColor +
            ", profileWaypointLine=" + profileWaypointLine +
            ", tableBorder=" + tableBorder +
            ", tableRowHover=" + tableRowHover +
            ", tableSparkBase=" + tableSparkBase +
            ", tooltipBg=" + tooltipBg +
            ", tooltipFg=" + tooltipFg +
            ", waypointCardMarkColor=" + waypointCardMarkColor +
            ", waypointCardMarkFg=" + waypointCardMarkFg +
            ", controlShadow=" + controlShadow +
            ", profileTooltipShadow=" + profileTooltipShadow +
            ", profileCursorDash=" + profileCursorDash +
            ")"

    public companion object {
        /** standard is every token as the contract's tokens table states it. */
        public val standard: ElementTokens =
            ElementTokens(
                accent = ThemeToken(light = HexColor(hex = "#1c7ed6")),
                controlBg = ThemeToken(light = HexColor(hex = "#ffffff"), dark = HexColor(hex = "#25262b")),
                controlBgHover = ThemeToken(light = HexColor(hex = "#f1f3f5"), dark = HexColor(hex = "#373a40")),
                controlFg = ThemeToken(light = HexColor(hex = "#212529"), dark = HexColor(hex = "#c1c2c5")),
                controlFgDisabled = ThemeToken(light = HexColor(hex = "#adb5bd"), dark = HexColor(hex = "#5c5f66")),
                dimmed = ThemeToken(light = HexColor(hex = "#868e96")),
                focusRing = ThemeToken(light = HexColor(hex = "#1c7ed6")),
                panelBg = ThemeToken(light = HexColor(hex = "#ffffff"), dark = HexColor(hex = "#25262b")),
                panelBorder = ThemeToken(light = HexColor(hex = "#dee2e6"), dark = HexColor(hex = "#373a40")),
                panelFg = ThemeToken(light = HexColor(hex = "#212529"), dark = HexColor(hex = "#c1c2c5")),
                profileCursor = ThemeToken(light = HexColor(hex = "#212529"), dark = HexColor(hex = "#c1c2c5")),
                profileGridColor = ThemeToken(light = HexColor(hex = "#dee2e6"), dark = HexColor(hex = "#373a40")),
                profileLitFill = ThemeToken(light = HexColor(hex = "#e9501e", opacity = 0.12)),
                profileNavigatorHandle = ThemeToken(light = HexColor(hex = "#212529"), dark = HexColor(hex = "#c1c2c5")),
                profileNavigatorVeil = ThemeToken(light = HexColor(hex = "#ffffff", opacity = 0.62), dark = HexColor(hex = "#000000", opacity = 0.55)),
                profileTooltipBg = ThemeToken(light = HexColor(hex = "#ffffff"), dark = HexColor(hex = "#2c2e33")),
                profileTooltipFg = ThemeToken(light = HexColor(hex = "#212529"), dark = HexColor(hex = "#c1c2c5")),
                profileWaypointColor = ThemeToken(light = HexColor(hex = "#868e96")),
                profileWaypointLine = ThemeToken(light = HexColor(hex = "#adb5bd"), dark = HexColor(hex = "#5c5f66")),
                tableBorder = ThemeToken(light = HexColor(hex = "#dee2e6"), dark = HexColor(hex = "#373a40")),
                tableRowHover = ThemeToken(light = HexColor(hex = "#f1f3f5"), dark = HexColor(hex = "#2c2e33")),
                tableSparkBase = ThemeToken(light = HexColor(hex = "#dee2e6"), dark = HexColor(hex = "#373a40")),
                tooltipBg = ThemeToken(light = HexColor(hex = "#212529"), dark = HexColor(hex = "#f1f3f5")),
                tooltipFg = ThemeToken(light = HexColor(hex = "#ffffff"), dark = HexColor(hex = "#141517")),
                waypointCardMarkColor = ThemeToken(light = HexColor(hex = "#3b82f6")),
                waypointCardMarkFg = ThemeToken(light = HexColor(hex = "#ffffff")),
                controlShadow = ThemeToken(light = listOf(HexColor(hex = "#000000", opacity = 0.05), HexColor(hex = "#000000", opacity = 0.1))),
                profileTooltipShadow = ThemeToken(light = listOf(HexColor(hex = "#000000", opacity = 0.12))),
                profileCursorDash = ThemeToken(light = listOf(3.0, 3.0)),
            )

        /** colorTokens is each colour token's member, by the name the table gives it. */
        internal val colorTokens: Map<String, KProperty1<ElementTokens, ThemeToken<HexColor>>> =
            mapOf(
                "accent" to ElementTokens::accent,
                "control-bg" to ElementTokens::controlBg,
                "control-bg-hover" to ElementTokens::controlBgHover,
                "control-fg" to ElementTokens::controlFg,
                "control-fg-disabled" to ElementTokens::controlFgDisabled,
                "dimmed" to ElementTokens::dimmed,
                "focus-ring" to ElementTokens::focusRing,
                "panel-bg" to ElementTokens::panelBg,
                "panel-border" to ElementTokens::panelBorder,
                "panel-fg" to ElementTokens::panelFg,
                "profile-cursor" to ElementTokens::profileCursor,
                "profile-grid-color" to ElementTokens::profileGridColor,
                "profile-lit-fill" to ElementTokens::profileLitFill,
                "profile-navigator-handle" to ElementTokens::profileNavigatorHandle,
                "profile-navigator-veil" to ElementTokens::profileNavigatorVeil,
                "profile-tooltip-bg" to ElementTokens::profileTooltipBg,
                "profile-tooltip-fg" to ElementTokens::profileTooltipFg,
                "profile-waypoint-color" to ElementTokens::profileWaypointColor,
                "profile-waypoint-line" to ElementTokens::profileWaypointLine,
                "table-border" to ElementTokens::tableBorder,
                "table-row-hover" to ElementTokens::tableRowHover,
                "table-spark-base" to ElementTokens::tableSparkBase,
                "tooltip-bg" to ElementTokens::tooltipBg,
                "tooltip-fg" to ElementTokens::tooltipFg,
                "waypoint-card-mark-color" to ElementTokens::waypointCardMarkColor,
                "waypoint-card-mark-fg" to ElementTokens::waypointCardMarkFg,
            )

        /** shadowTokens is each shadow token's member, by the name the table gives it. */
        internal val shadowTokens: Map<String, KProperty1<ElementTokens, ThemeToken<List<HexColor>>>> =
            mapOf(
                "control-shadow" to ElementTokens::controlShadow,
                "profile-tooltip-shadow" to ElementTokens::profileTooltipShadow,
            )

        /** dashTokens is each dash token's member, by the name the table gives it. */
        internal val dashTokens: Map<String, KProperty1<ElementTokens, ThemeToken<List<Double>>>> =
            mapOf(
                "profile-cursor-dash" to ElementTokens::profileCursorDash,
            )
    }
}
