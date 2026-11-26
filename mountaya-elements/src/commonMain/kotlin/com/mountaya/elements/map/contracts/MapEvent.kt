package com.mountaya.elements

import com.mountaya.client.generated.TileSchema

/**
 * MapEvent is what a map tells its host, which `MapEngine.events` hands each collector in order: a gesture
 * on the map, the features a hovering pointer is over, a refusal of access and its end, a failure of one of
 * the map's own requests, and a change of the layers catalogue it holds.
 *
 * A gesture's position is the place under the pointer, `[longitude, latitude]`, its longitude brought within
 * [-180, 180] only where the renderer read it past that range, and a gesture an edit in progress took is told
 * to nobody (`map.events.pointer`). Kotlin names a gesture as Compose and MapLibre Android do, a click and a
 * long click, and hands each as the `MapTap` every SDK names the value. Two events are equal where they are
 * of one kind and hold the same.
 */
public sealed interface MapEvent {
    /** Click is a click on the map, with the place and the point of the view it fell at and the topmost marker within its reach. */
    public class Click(
        /** The click. */
        public val tap: MapTap,
    ) : MapEvent {
        override fun equals(other: Any?): Boolean = other is Click && other.tap == tap

        override fun hashCode(): Int = tap.hashCode()

        override fun toString(): String = "Click($tap)"
    }

    /** LongClick is a long press on the map, or a secondary click of a pointer, told as a click is. */
    public class LongClick(
        /** The long press. */
        public val tap: MapTap,
    ) : MapEvent {
        override fun equals(other: Any?): Boolean = other is LongClick && other.tap == tap

        override fun hashCode(): Int = tap.hashCode()

        override fun toString(): String = "LongClick($tap)"
    }

    /**
     * FeatureClick is a click that landed on features the map drew, told after its [Click], with every such
     * feature topmost first, as `MapEngine.featuresAt` answers them.
     */
    public class FeatureClick(
        /** The place clicked, `[longitude, latitude]`. */
        public val position: List<Double>,
        /** The features the click landed on, topmost first. */
        public val features: List<QueriedFeature>,
    ) : MapEvent {
        override fun equals(other: Any?): Boolean =
            other is FeatureClick && other.position == position && other.features == features

        override fun hashCode(): Int = position.hashCode() * 31 + features.hashCode()

        override fun toString(): String = "FeatureClick(position=$position, features=$features)"
    }

    /**
     * FeatureHover is where a hovering pointer ended a frame, with the features of an itinerary's line and
     * points under it, topmost first, and with none as it leaves the map (`map.events.feature-hover`). It is
     * told only where a pointer hovers without pressing, a mouse, a trackpad or a stylus, and never for a
     * finger.
     */
    public class FeatureHover(
        /** The place under the pointer, `[longitude, latitude]`. */
        public val position: List<Double>,
        /** The features of an itinerary's line and points under the pointer, topmost first. */
        public val features: List<QueriedFeature>,
    ) : MapEvent {
        override fun equals(other: Any?): Boolean =
            other is FeatureHover && other.position == position && other.features == features

        override fun hashCode(): Int = position.hashCode() * 31 + features.hashCode()

        override fun toString(): String = "FeatureHover(position=$position, features=$features)"
    }

    /**
     * AccessRefused is one of the map's own requests refused access, a 401, a 402 or a 403, with the status
     * and the address refused, the empty text where the renderer reported none (`map.events.access-refused`).
     * It is never told as a [Failure] too.
     */
    public class AccessRefused(
        /** The status the refusal carried. */
        public val status: Int,
        /** The address refused, or the empty text where none was reported. */
        public val url: String,
    ) : MapEvent {
        override fun equals(other: Any?): Boolean = other is AccessRefused && other.status == status && other.url == url

        override fun hashCode(): Int = status * 31 + url.hashCode()

        override fun toString(): String = "AccessRefused(status=$status, url=$url)"
    }

    /** AccessRestored is the end of the last refusal of access that stood, told once (`map.events.access-standing`). */
    public object AccessRestored : MapEvent {
        override fun toString(): String = "AccessRestored"
    }

    /**
     * Failure is one of the map's own requests failing for any other reason, with the address it was made to
     * and the status it carried, or null where none answered, and what failed where the map knows it: a request
     * that never left because the client's credential could not be read for it carries what the read threw
     * (`map.events.access-refused`), as every SDK's failure carries it.
     */
    public class Failure(
        /** The address the request was made to, or the empty text where none was reported. */
        public val url: String,
        /** The status the answer carried, or null where none answered. */
        public val status: Int?,
        /**
         * What failed, where the map knows it: what a read of the client's credential threw, for a request that
         * never left for want of one; null for every other failure.
         */
        public val cause: Throwable? = null,
    ) : MapEvent {
        override fun equals(other: Any?): Boolean =
            other is Failure && other.url == url && other.status == status && other.cause == cause

        override fun hashCode(): Int = (url.hashCode() * 31 + (status ?: 0)) * 31 + (cause?.hashCode() ?: 0)

        override fun toString(): String =
            "Failure(url=$url, status=$status" + (cause?.let { ", cause=$it" } ?: "") + ")"
    }

    /**
     * LayersChange is the layers catalogue the map holds changing (`map.layers.manifest`): a read that landed,
     * carrying the catalogue it holds after it, or a move of the studio scope that dropped the one held,
     * carrying null.
     */
    public class LayersChange(
        /** The catalogue the map holds after the change, or null where the change dropped it. */
        public val layers: TileSchema.Layers?,
    ) : MapEvent {
        override fun equals(other: Any?): Boolean = other is LayersChange && other.layers == layers

        override fun hashCode(): Int = layers?.hashCode() ?: 0

        override fun toString(): String = "LayersChange($layers)"
    }
}
