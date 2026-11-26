package com.mountaya.elements

/** ControlSlot is one place of a map's control stack, top to bottom. */
internal sealed interface ControlSlot {
    /** The zoom pair. */
    object Zoom : ControlSlot

    /** The panel toggles, one group, in the order given. */
    class Panels(
        val panels: List<MapPanel>,
    ) : ControlSlot {
        override fun equals(other: Any?): Boolean = other is Panels && other.panels == panels

        override fun hashCode(): Int = panels.hashCode()

        override fun toString(): String = "Panels($panels)"
    }

    /** The app's own controls. */
    object Content : ControlSlot

    /** The locate control. */
    object Geolocate : ControlSlot

    /** The compass. */
    object Compass : ControlSlot

    companion object {
        /**
         * The places of a stack showing the controls each flag turns on, from the top: zoom; the layers,
         * points-of-interest and preferences toggles as one group; the app's own controls; geolocate; the compass.
         */
        fun stack(
            zoom: Boolean,
            layers: Boolean,
            pois: Boolean,
            preferences: Boolean,
            geolocate: Boolean,
            compass: Boolean,
        ): List<ControlSlot> {
            val panels =
                listOf(
                    layers to MapPanel.Layers,
                    pois to MapPanel.Pois,
                    preferences to MapPanel.Preferences,
                ).filter {
                    it.first
                }.map { it.second }
            return buildList {
                if (zoom) add(Zoom)
                if (panels.isNotEmpty()) add(Panels(panels))
                add(Content)
                if (geolocate) add(Geolocate)
                if (compass) add(Compass)
            }
        }
    }
}
