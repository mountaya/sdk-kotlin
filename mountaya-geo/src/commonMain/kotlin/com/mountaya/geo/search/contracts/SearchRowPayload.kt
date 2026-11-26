package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * SearchRowPayload is what picking a search result hands back, one kind per section, so one handler
 * answers every section: where to move the camera, what to frame and what to open. A position is
 * `[longitude, latitude]`, an altitude the wire's point carries dropped, and a box
 * `[west, south, east, north]`, each null where the search answered none that names a place. A way's
 * `coordinates` are its line as the wire traced it.
 *
 * Two are equal where they are the same kind holding the same values, a list of numbers compared as
 * every list of the geo layer is.
 */
public sealed interface SearchRowPayload {
    /** Place moves the camera to a geocoded place's point, framing its bounds where it has some. */
    public class Place(
        /** The place's point. */
        public val point: List<Double>,
        /** The place's extent. */
        public val bounds: List<Double>?,
    ) : SearchRowPayload {
        override fun equals(other: Any?): Boolean = other is Place && other.point == point && other.bounds == bounds

        override fun hashCode(): Int = point.hashCode() * 31 + bounds.hashCode()

        override fun toString(): String = "SearchRowPayload.Place(point=$point, bounds=$bounds)"
    }

    /** Poi moves the camera to a point of interest, its category picking the pin. */
    public class Poi(
        /** The point of interest's point. */
        public val point: List<Double>,
        /** Its category, as the Data API spells it. */
        public val category: DataSchema.POICategory?,
    ) : SearchRowPayload {
        override fun equals(other: Any?): Boolean = other is Poi && other.point == point && other.category == category

        override fun hashCode(): Int = point.hashCode() * 31 + category.hashCode()

        override fun toString(): String = "SearchRowPayload.Poi(point=$point, category=$category)"
    }

    /** Way moves the camera to a way's anchor, framing its bounds and tracing its line where the search verified one. */
    public class Way(
        /** The way's anchor, its centroid. */
        public val anchor: List<Double>,
        /** The way's extent. */
        public val bounds: List<Double>?,
        /** The way's line, where the search verified it. */
        public val coordinates: List<List<Double>>?,
    ) : SearchRowPayload {
        override fun equals(other: Any?): Boolean =
            other is Way && other.anchor == anchor && other.bounds == bounds && other.coordinates == coordinates

        override fun hashCode(): Int = (anchor.hashCode() * 31 + bounds.hashCode()) * 31 + coordinates.hashCode()

        override fun toString(): String =
            "SearchRowPayload.Way(anchor=$anchor, bounds=$bounds, coordinates=$coordinates)"
    }

    /** Itinerary opens an itinerary by its id, framed on its extent and started where it starts. */
    public class Itinerary(
        /** The itinerary's id. */
        public val itineraryId: String,
        /** Its extent. */
        public val bounds: List<Double>?,
        /** Its start. */
        public val start: List<Double>?,
    ) : SearchRowPayload {
        override fun equals(other: Any?): Boolean =
            other is Itinerary && other.itineraryId == itineraryId && other.bounds == bounds && other.start == start

        override fun hashCode(): Int = (itineraryId.hashCode() * 31 + bounds.hashCode()) * 31 + start.hashCode()

        override fun toString(): String = "SearchRowPayload.Itinerary($itineraryId, bounds=$bounds, start=$start)"
    }

    /** Collection opens a collection, carrying the fields an editor over it shows at once. */
    public class Collection(
        /** The collection's id. */
        public val collectionId: String,
        /** Its name. */
        public val name: String?,
        /** Its description, null for one that has none. */
        public val description: String?,
        /** Whether it is private. */
        public val isPrivate: Boolean?,
    ) : SearchRowPayload {
        override fun equals(other: Any?): Boolean =
            other is Collection && other.collectionId == collectionId && other.name == name &&
                other.description == description &&
                other.isPrivate == isPrivate

        override fun hashCode(): Int =
            ((collectionId.hashCode() * 31 + name.hashCode()) * 31 + description.hashCode()) * 31 + isPrivate.hashCode()

        override fun toString(): String = "SearchRowPayload.Collection($collectionId, name=$name)"
    }

    /** Zone opens a zone by its id, at its anchor and framed on its extent where it has them. */
    public class Zone(
        /** The zone's id. */
        public val zoneId: String,
        /** Its anchor point. */
        public val point: List<Double>?,
        /** Its extent. */
        public val bounds: List<Double>?,
    ) : SearchRowPayload {
        override fun equals(other: Any?): Boolean =
            other is Zone && other.zoneId == zoneId && other.point == point && other.bounds == bounds

        override fun hashCode(): Int = (zoneId.hashCode() * 31 + point.hashCode()) * 31 + bounds.hashCode()

        override fun toString(): String = "SearchRowPayload.Zone($zoneId, point=$point, bounds=$bounds)"
    }
}
