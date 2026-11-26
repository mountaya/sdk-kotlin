// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

@file:kotlinx.serialization.UseSerializers(com.mountaya.client.TimestampSerializer::class)

package com.mountaya.client.generated

/** DataSchema is the Data API's types, as the SDL names them. */
public object DataSchema {
    /**
     * The result of `analyzeGeometry`.
     *
     * It wraps `GeometryAnalysis` rather than returning it directly, which is how it
     * carries `duration` beside the analysis without changing the type that
     * `DirectionsRoute` and `SuggestionRoute` also expose as `analysis`. Every other
     * query on this API returns a `*Result` for the same reason.
     */
    @kotlinx.serialization.Serializable
    public class AnalyzeGeometryResult private constructor(
        /**
         * The analysis of the submitted geometry: elevation, slope, surface, way type,
         * trail difficulty and route-level advisories.
         *
         * A stretch the routing engine fails to classify on Mountaya's side is reported
         * as one UNKNOWN segment, as a stretch it has no data for is, and the field then
         * carries an INTERNAL_ERROR entry in `errors` beside the analysis, which keeps
         * everything else it read. Such a request is recorded as a failure (5xx), so it
         * counts toward neither the monthly allowance nor an invoice.
         */
        public val analysis: DataSchema.GeometryAnalysis? = null,
        /**
         * Estimated travel duration of the submitted geometry in seconds, over the
         * terrain the analysis just read.
         *
         * It is the same travel-time model, over the same ground, that `directions` and
         * `suggestions` report, so a freehand line previewed here and the segment it
         * becomes agree. Integrating `analysis.speedInfo` over the geometry reproduces
         * this number exactly. Example: 18720.0 for a 5h12m line.
         */
        public val duration: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.AnalyzeGeometryResult &&
                other.analysis == this.analysis &&
                other.duration == this.duration

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.analysis?.hashCode() ?: 0)
            hash = 31 * hash + (this.duration?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "AnalyzeGeometryResult(" +
                "analysis=" + this.analysis +
                ", duration=" + this.duration +
                ")"

        /** Builder sets the optional properties of a [DataSchema.AnalyzeGeometryResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: DataSchema.GeometryAnalysis? = null
            internal var slot1: kotlin.Double? = null

            /**
             * The analysis of the submitted geometry: elevation, slope, surface, way type,
             * trail difficulty and route-level advisories.
             *
             * A stretch the routing engine fails to classify on Mountaya's side is reported
             * as one UNKNOWN segment, as a stretch it has no data for is, and the field then
             * carries an INTERNAL_ERROR entry in `errors` beside the analysis, which keeps
             * everything else it read. Such a request is recorded as a failure (5xx), so it
             * counts toward neither the monthly allowance nor an invoice.
             */
            public var analysis: DataSchema.GeometryAnalysis?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Estimated travel duration of the submitted geometry in seconds, over the
             * terrain the analysis just read.
             *
             * It is the same travel-time model, over the same ground, that `directions` and
             * `suggestions` report, so a freehand line previewed here and the segment it
             * becomes agree. Integrating `analysis.speedInfo` over the geometry reproduces
             * this number exactly. Example: 18720.0 for a 5h12m line.
             */
            public var duration: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.AnalyzeGeometryResult]. */
        public companion object {
            /** Returns a [DataSchema.AnalyzeGeometryResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.AnalyzeGeometryResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.AnalyzeGeometryResult {
                val given = DataSchema.AnalyzeGeometryResult.Builder().apply(optional)
                return DataSchema.AnalyzeGeometryResult(
                    analysis = given.slot0,
                    duration = given.slot1,
                )
            }
        }
    }

    /**
     * Aggregate counts for a collection returned by the collections target. Modelled
     * as an object (rather than a flat scalar) so a further count can be added without
     * a breaking change. Mirrors `CollectionCount` on the REST resource field for
     * field, so one collection reports the same totals through either API.
     */
    @kotlinx.serialization.Serializable
    public class CollectionCount private constructor(
        /** Total number of itineraries contained in the collection. */
        public val itineraries: kotlin.Int? = null,
        /** Total number of zones contained in the collection. */
        public val zones: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.CollectionCount &&
                other.itineraries == this.itineraries &&
                other.zones == this.zones

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.itineraries?.hashCode() ?: 0)
            hash = 31 * hash + (this.zones?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionCount(" +
                "itineraries=" + this.itineraries +
                ", zones=" + this.zones +
                ")"

        /** Builder sets the optional properties of a [DataSchema.CollectionCount], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null

            /** Total number of itineraries contained in the collection. */
            public var itineraries: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Total number of zones contained in the collection. */
            public var zones: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.CollectionCount]. */
        public companion object {
            /** Returns a [DataSchema.CollectionCount] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.CollectionCount.Builder.() -> kotlin.Unit = {},
            ): DataSchema.CollectionCount {
                val given = DataSchema.CollectionCount.Builder().apply(optional)
                return DataSchema.CollectionCount(
                    itineraries = given.slot0,
                    zones = given.slot1,
                )
            }
        }
    }

    /** A lightweight collection reference returned by the collections target. */
    @kotlinx.serialization.Serializable
    public class CollectionSearchResult private constructor(
        /** Collection identifier. */
        public val id: kotlin.String? = null,
        /** Display name of the collection. */
        public val name: kotlin.String? = null,
        /** Free-text description of the collection. Null when it has none. */
        public val description: kotlin.String? = null,
        /** Aggregate counts for the collection. */
        public val count: DataSchema.CollectionCount? = null,
        /**
         * Bounding box of the collection's itineraries as [west, south, east, north],
         * measured as the caller sees them: the same box as the `bbox` of the
         * collection's tile specification, so one across the antimeridian has west
         * greater than east. It covers itineraries only, never a zone. Under a session
         * token minted without `user_id`, the public itineraries as last published, of
         * owners whose agreement includes Map Publishing; under one naming a member,
         * the drafts of the itineraries that member reaches. Null when none is drawn
         * for the caller.
         */
        public val bounds: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Whether the collection is private. A private collection is reached only with a
         * key of the organization that owns it; no other organization's key reaches it,
         * even with its id. A public one ('anyone with the link') is also readable by id
         * with any other organization's key, but is never listed in another
         * organization's search results.
         */
        public val isPrivate: kotlin.Boolean? = null,
        /**
         * Identifier of the organization that owns this collection. Null for a personal
         * collection, made from an individual account and owned by no organization.
         */
        public val organizationId: kotlin.String? = null,
        /**
         * Relevance rank over the collection's name and description, always greater than 0,
         * higher being a better match; a match in the name weighs twice one in the
         * description. It orders the collections section of ONE response and nothing more.
         * The value comes from whichever of two ranking passes produced the match: a
         * full-text rank, which is unbounded above though in practice below 1, or, when
         * the full-text pass matches nothing and the query is at least two characters
         * long, a fuzzy similarity between 0.3 and 1. The two scales differ, so a score is
         * not comparable across responses, across targets, or against a fixed threshold.
         */
        public val score: kotlin.Double? = null,
        /** Field that produced the match. */
        public val matchedField: DataSchema.MatchedField? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.CollectionSearchResult &&
                other.id == this.id &&
                other.name == this.name &&
                other.description == this.description &&
                other.count == this.count &&
                other.bounds == this.bounds &&
                other.isPrivate == this.isPrivate &&
                other.organizationId == this.organizationId &&
                other.score == this.score &&
                other.matchedField == this.matchedField

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.id?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.bounds?.hashCode() ?: 0)
            hash = 31 * hash + (this.isPrivate?.hashCode() ?: 0)
            hash = 31 * hash + (this.organizationId?.hashCode() ?: 0)
            hash = 31 * hash + (this.score?.hashCode() ?: 0)
            hash = 31 * hash + (this.matchedField?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionSearchResult(" +
                "id=" + this.id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", count=" + this.count +
                ", bounds=" + this.bounds +
                ", isPrivate=" + this.isPrivate +
                ", organizationId=" + this.organizationId +
                ", score=" + this.score +
                ", matchedField=" + this.matchedField +
                ")"

        /** Builder sets the optional properties of a [DataSchema.CollectionSearchResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: DataSchema.CollectionCount? = null
            internal var slot4: kotlin.collections.List<kotlin.Double>? = null
            internal var slot5: kotlin.Boolean? = null
            internal var slot6: kotlin.String? = null
            internal var slot7: kotlin.Double? = null
            internal var slot8: DataSchema.MatchedField? = null

            /** Collection identifier. */
            public var id: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Display name of the collection. */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Free-text description of the collection. Null when it has none. */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Aggregate counts for the collection. */
            public var count: DataSchema.CollectionCount?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Bounding box of the collection's itineraries as [west, south, east, north],
             * measured as the caller sees them: the same box as the `bbox` of the
             * collection's tile specification, so one across the antimeridian has west
             * greater than east. It covers itineraries only, never a zone. Under a session
             * token minted without `user_id`, the public itineraries as last published, of
             * owners whose agreement includes Map Publishing; under one naming a member,
             * the drafts of the itineraries that member reaches. Null when none is drawn
             * for the caller.
             */
            public var bounds: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Whether the collection is private. A private collection is reached only with a
             * key of the organization that owns it; no other organization's key reaches it,
             * even with its id. A public one ('anyone with the link') is also readable by id
             * with any other organization's key, but is never listed in another
             * organization's search results.
             */
            public var isPrivate: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Identifier of the organization that owns this collection. Null for a personal
             * collection, made from an individual account and owned by no organization.
             */
            public var organizationId: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Relevance rank over the collection's name and description, always greater than 0,
             * higher being a better match; a match in the name weighs twice one in the
             * description. It orders the collections section of ONE response and nothing more.
             * The value comes from whichever of two ranking passes produced the match: a
             * full-text rank, which is unbounded above though in practice below 1, or, when
             * the full-text pass matches nothing and the query is at least two characters
             * long, a fuzzy similarity between 0.3 and 1. The two scales differ, so a score is
             * not comparable across responses, across targets, or against a fixed threshold.
             */
            public var score: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /** Field that produced the match. */
            public var matchedField: DataSchema.MatchedField?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }
        }

        /** Builds a [DataSchema.CollectionSearchResult]. */
        public companion object {
            /** Returns a [DataSchema.CollectionSearchResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.CollectionSearchResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.CollectionSearchResult {
                val given = DataSchema.CollectionSearchResult.Builder().apply(optional)
                return DataSchema.CollectionSearchResult(
                    id = given.slot0,
                    name = given.slot1,
                    description = given.slot2,
                    count = given.slot3,
                    bounds = given.slot4,
                    isPrivate = given.slot5,
                    organizationId = given.slot6,
                    score = given.slot7,
                    matchedField = given.slot8,
                )
            }
        }
    }

    /** Result of the collections target. */
    @kotlinx.serialization.Serializable
    public class CollectionsResult private constructor(
        /** Number of collections returned: the length of `collections`, at most `limit`. */
        public val count: kotlin.Int? = null,
        /** List of collection references ordered by relevance. */
        public val collections: kotlin.collections.List<DataSchema.CollectionSearchResult>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.CollectionsResult &&
                other.count == this.count &&
                other.collections == this.collections

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.collections?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionsResult(" +
                "count=" + this.count +
                ", collections=" + this.collections +
                ")"

        /** Builder sets the optional properties of a [DataSchema.CollectionsResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.collections.List<DataSchema.CollectionSearchResult>? = null

            /** Number of collections returned: the length of `collections`, at most `limit`. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** List of collection references ordered by relevance. */
            public var collections: kotlin.collections.List<DataSchema.CollectionSearchResult>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.CollectionsResult]. */
        public companion object {
            /** Returns a [DataSchema.CollectionsResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.CollectionsResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.CollectionsResult {
                val given = DataSchema.CollectionsResult.Builder().apply(optional)
                return DataSchema.CollectionsResult(
                    count = given.slot0,
                    collections = given.slot1,
                )
            }
        }
    }

    /** Result of a directions computation, containing one or more possible routes. */
    @kotlinx.serialization.Serializable
    public class DirectionsResult private constructor(
        /** List of computed routes between the provided waypoints. */
        public val routes: kotlin.collections.List<DataSchema.DirectionsRoute>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.DirectionsResult &&
                other.routes == this.routes

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.routes?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "DirectionsResult(" +
                "routes=" + this.routes +
                ")"

        /** Builder sets the optional properties of a [DataSchema.DirectionsResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<DataSchema.DirectionsRoute>? = null

            /** List of computed routes between the provided waypoints. */
            public var routes: kotlin.collections.List<DataSchema.DirectionsRoute>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [DataSchema.DirectionsResult]. */
        public companion object {
            /** Returns a [DataSchema.DirectionsResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.DirectionsResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.DirectionsResult {
                val given = DataSchema.DirectionsResult.Builder().apply(optional)
                return DataSchema.DirectionsResult(
                    routes = given.slot0,
                )
            }
        }
    }

    /**
     * A single computed route with distance, duration, elevation, geometry, and
     * analysis.
     */
    @kotlinx.serialization.Serializable
    public class DirectionsRoute private constructor(
        /**
         * Total route distance in meters, measured as the planimetric great-circle
         * length of the geometry field, with elevation ignored.
         * Example: 15200.0 for a 15.2 km route.
         */
        public val distance: kotlin.Double? = null,
        /**
         * Estimated route duration in seconds, computed over the terrain the route
         * crosses rather than over its distance: the model charges for grade, surface
         * and trail difficulty edge by edge, anchored on the requested `speed` (or the
         * activity default) as the pace on level ground. Integrating
         * `analysis.speedInfo` over the geometry reproduces this number exactly.
         * Example: 18720.0 for a 5h12m route.
         */
        public val duration: kotlin.Double? = null,
        /**
         * Elevation summary including altitude bounds, total gain/loss, and gradient
         * extremes.
         */
        public val elevation: DataSchema.ElevationSummary? = null,
        /** GeoJSON geometry representing the route path. */
        public val geometry: com.mountaya.client.GeoJsonGeometry? = null,
        /**
         * Surface and way type analysis for the route geometry, including the
         * `advisories` naming any tollway or access-restricted way it crosses.
         */
        public val analysis: DataSchema.GeometryAnalysis? = null,
        /**
         * What the request's `avoid` list achieved for this route. NOT_REQUESTED when
         * none was given.
         */
        public val avoidance: DataSchema.AvoidanceOutcome? = null,
        /**
         * Human-readable route summary.
         * Example: "15.2 km, ~4h12m, +720m / -430m elevation, 62% path, 28% track."
         */
        public val summary: kotlin.String? = null,
        /** Turn-by-turn direction steps along the route, in travel order. */
        public val steps: kotlin.collections.List<DataSchema.DirectionsStep>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.DirectionsRoute &&
                other.distance == this.distance &&
                other.duration == this.duration &&
                other.elevation == this.elevation &&
                other.geometry == this.geometry &&
                other.analysis == this.analysis &&
                other.avoidance == this.avoidance &&
                other.summary == this.summary &&
                other.steps == this.steps

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.distance?.hashCode() ?: 0)
            hash = 31 * hash + (this.duration?.hashCode() ?: 0)
            hash = 31 * hash + (this.elevation?.hashCode() ?: 0)
            hash = 31 * hash + (this.geometry?.hashCode() ?: 0)
            hash = 31 * hash + (this.analysis?.hashCode() ?: 0)
            hash = 31 * hash + (this.avoidance?.hashCode() ?: 0)
            hash = 31 * hash + (this.summary?.hashCode() ?: 0)
            hash = 31 * hash + (this.steps?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "DirectionsRoute(" +
                "distance=" + this.distance +
                ", duration=" + this.duration +
                ", elevation=" + this.elevation +
                ", geometry=" + this.geometry +
                ", analysis=" + this.analysis +
                ", avoidance=" + this.avoidance +
                ", summary=" + this.summary +
                ", steps=" + this.steps +
                ")"

        /** Builder sets the optional properties of a [DataSchema.DirectionsRoute], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Double? = null
            internal var slot1: kotlin.Double? = null
            internal var slot2: DataSchema.ElevationSummary? = null
            internal var slot3: com.mountaya.client.GeoJsonGeometry? = null
            internal var slot4: DataSchema.GeometryAnalysis? = null
            internal var slot5: DataSchema.AvoidanceOutcome? = null
            internal var slot6: kotlin.String? = null
            internal var slot7: kotlin.collections.List<DataSchema.DirectionsStep>? = null

            /**
             * Total route distance in meters, measured as the planimetric great-circle
             * length of the geometry field, with elevation ignored.
             * Example: 15200.0 for a 15.2 km route.
             */
            public var distance: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Estimated route duration in seconds, computed over the terrain the route
             * crosses rather than over its distance: the model charges for grade, surface
             * and trail difficulty edge by edge, anchored on the requested `speed` (or the
             * activity default) as the pace on level ground. Integrating
             * `analysis.speedInfo` over the geometry reproduces this number exactly.
             * Example: 18720.0 for a 5h12m route.
             */
            public var duration: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Elevation summary including altitude bounds, total gain/loss, and gradient
             * extremes.
             */
            public var elevation: DataSchema.ElevationSummary?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** GeoJSON geometry representing the route path. */
            public var geometry: com.mountaya.client.GeoJsonGeometry?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Surface and way type analysis for the route geometry, including the
             * `advisories` naming any tollway or access-restricted way it crosses.
             */
            public var analysis: DataSchema.GeometryAnalysis?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * What the request's `avoid` list achieved for this route. NOT_REQUESTED when
             * none was given.
             */
            public var avoidance: DataSchema.AvoidanceOutcome?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Human-readable route summary.
             * Example: "15.2 km, ~4h12m, +720m / -430m elevation, 62% path, 28% track."
             */
            public var summary: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /** Turn-by-turn direction steps along the route, in travel order. */
            public var steps: kotlin.collections.List<DataSchema.DirectionsStep>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }
        }

        /** Builds a [DataSchema.DirectionsRoute]. */
        public companion object {
            /** Returns a [DataSchema.DirectionsRoute] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.DirectionsRoute.Builder.() -> kotlin.Unit = {},
            ): DataSchema.DirectionsRoute {
                val given = DataSchema.DirectionsRoute.Builder().apply(optional)
                return DataSchema.DirectionsRoute(
                    distance = given.slot0,
                    duration = given.slot1,
                    elevation = given.slot2,
                    geometry = given.slot3,
                    analysis = given.slot4,
                    avoidance = given.slot5,
                    summary = given.slot6,
                    steps = given.slot7,
                )
            }
        }
    }

    /**
     * A single turn-by-turn direction step referencing a coordinate index range on
     * the route geometry (same index basis as SurfaceInfo/WayTypeInfo/SlopeInfo).
     * Shared leaf type for both DirectionsRoute and SuggestionRoute.
     */
    @kotlinx.serialization.Serializable
    public class DirectionsStep private constructor(
        /** Start index in the route coordinates array (inclusive). */
        public val fromIndex: kotlin.Int? = null,
        /** End index in the route coordinates array (inclusive). */
        public val toIndex: kotlin.Int? = null,
        /** Maneuver to perform at the start of this step. */
        public val type: DataSchema.ManeuverType? = null,
        /** Name of the way/street for this step. May be empty or "-" when unnamed. */
        public val name: kotlin.String? = null,
        /**
         * Length of this step in meters: the routing engine's figure for the step,
         * rescaled by the factor that takes the engine's own route length to the route's
         * measured `distance`.
         */
        public val distance: kotlin.Double? = null,
        /**
         * Estimated duration of this step in seconds, from the same travel-time model as
         * the route's `duration`, over the step's stretch of the geometry.
         */
        public val duration: kotlin.Double? = null,
        /**
         * For a roundabout maneuver, the 1-based exit to take. Null for any other
         * maneuver, matching `exit_number` on the REST resource, which is nullable for
         * the same reason.
         */
        public val exitNumber: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.DirectionsStep &&
                other.fromIndex == this.fromIndex &&
                other.toIndex == this.toIndex &&
                other.type == this.type &&
                other.name == this.name &&
                other.distance == this.distance &&
                other.duration == this.duration &&
                other.exitNumber == this.exitNumber

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.fromIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.toIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.type?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.distance?.hashCode() ?: 0)
            hash = 31 * hash + (this.duration?.hashCode() ?: 0)
            hash = 31 * hash + (this.exitNumber?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "DirectionsStep(" +
                "fromIndex=" + this.fromIndex +
                ", toIndex=" + this.toIndex +
                ", type=" + this.type +
                ", name=" + this.name +
                ", distance=" + this.distance +
                ", duration=" + this.duration +
                ", exitNumber=" + this.exitNumber +
                ")"

        /** Builder sets the optional properties of a [DataSchema.DirectionsStep], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: DataSchema.ManeuverType? = null
            internal var slot3: kotlin.String? = null
            internal var slot4: kotlin.Double? = null
            internal var slot5: kotlin.Double? = null
            internal var slot6: kotlin.Int? = null

            /** Start index in the route coordinates array (inclusive). */
            public var fromIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** End index in the route coordinates array (inclusive). */
            public var toIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Maneuver to perform at the start of this step. */
            public var type: DataSchema.ManeuverType?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Name of the way/street for this step. May be empty or "-" when unnamed. */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Length of this step in meters: the routing engine's figure for the step,
             * rescaled by the factor that takes the engine's own route length to the route's
             * measured `distance`.
             */
            public var distance: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Estimated duration of this step in seconds, from the same travel-time model as
             * the route's `duration`, over the step's stretch of the geometry.
             */
            public var duration: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * For a roundabout maneuver, the 1-based exit to take. Null for any other
             * maneuver, matching `exit_number` on the REST resource, which is nullable for
             * the same reason.
             */
            public var exitNumber: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }
        }

        /** Builds a [DataSchema.DirectionsStep]. */
        public companion object {
            /** Returns a [DataSchema.DirectionsStep] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.DirectionsStep.Builder.() -> kotlin.Unit = {},
            ): DataSchema.DirectionsStep {
                val given = DataSchema.DirectionsStep.Builder().apply(optional)
                return DataSchema.DirectionsStep(
                    fromIndex = given.slot0,
                    toIndex = given.slot1,
                    type = given.slot2,
                    name = given.slot3,
                    distance = given.slot4,
                    duration = given.slot5,
                    exitNumber = given.slot6,
                )
            }
        }
    }

    /**
     * Elevation summary for a computed route, including altitude bounds, total
     * elevation gain/loss, and gradient extremes.
     */
    @kotlinx.serialization.Serializable
    public class ElevationSummary private constructor(
        /** Altitude range along the route in meters above sea level. */
        public val altitude: DataSchema.MetricRange? = null,
        /** Total elevation gain (ascent) in meters. */
        public val ascent: kotlin.Double? = null,
        /** Total elevation loss (descent) in meters. */
        public val descent: kotlin.Double? = null,
        /**
         * Gradient range along the route in degrees. Positive values indicate uphill,
         * negative values indicate downhill.
         */
        public val gradient: DataSchema.MetricRange? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ElevationSummary &&
                other.altitude == this.altitude &&
                other.ascent == this.ascent &&
                other.descent == this.descent &&
                other.gradient == this.gradient

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.altitude?.hashCode() ?: 0)
            hash = 31 * hash + (this.ascent?.hashCode() ?: 0)
            hash = 31 * hash + (this.descent?.hashCode() ?: 0)
            hash = 31 * hash + (this.gradient?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ElevationSummary(" +
                "altitude=" + this.altitude +
                ", ascent=" + this.ascent +
                ", descent=" + this.descent +
                ", gradient=" + this.gradient +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ElevationSummary], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: DataSchema.MetricRange? = null
            internal var slot1: kotlin.Double? = null
            internal var slot2: kotlin.Double? = null
            internal var slot3: DataSchema.MetricRange? = null

            /** Altitude range along the route in meters above sea level. */
            public var altitude: DataSchema.MetricRange?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Total elevation gain (ascent) in meters. */
            public var ascent: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Total elevation loss (descent) in meters. */
            public var descent: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Gradient range along the route in degrees. Positive values indicate uphill,
             * negative values indicate downhill.
             */
            public var gradient: DataSchema.MetricRange?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [DataSchema.ElevationSummary]. */
        public companion object {
            /** Returns a [DataSchema.ElevationSummary] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ElevationSummary.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ElevationSummary {
                val given = DataSchema.ElevationSummary.Builder().apply(optional)
                return DataSchema.ElevationSummary(
                    altitude = given.slot0,
                    ascent = given.slot1,
                    descent = given.slot2,
                    gradient = given.slot3,
                )
            }
        }
    }

    /** A prepared itinerary download. */
    @kotlinx.serialization.Serializable
    public class ExportItineraryResult private constructor(
        /**
         * Signed download URL. It is valid for ten minutes, may be fetched once or many
         * times within that window, and requires no further authentication.
         */
        public val url: com.mountaya.client.Url? = null,
        /**
         * Suggested file name: the itinerary's name keeping only the letters a to z and A
         * to Z, the accented letters French uses (such as é, è, à, ç and œ), digits,
         * spaces, dots, underscores and hyphens, trimmed, or "export" when nothing is
         * left, followed by the extension for the requested format. A name written in
         * another script, such as Chinese or Japanese, therefore comes out as "export".
         * Example: "Tour du Mont-Blanc.gpx".
         */
        public val filename: kotlin.String? = null,
        /** Format the download is encoded in, echoed from the request. */
        public val format: DataSchema.ExportFormat? = null,
        /**
         * When the URL stops working, ten minutes after it was minted, as an RFC 3339
         * timestamp.
         * Example: "2026-07-28T12:10:00Z".
         */
        public val expiresAt: kotlin.time.Instant? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ExportItineraryResult &&
                other.url == this.url &&
                other.filename == this.filename &&
                other.format == this.format &&
                other.expiresAt == this.expiresAt

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.url?.hashCode() ?: 0)
            hash = 31 * hash + (this.filename?.hashCode() ?: 0)
            hash = 31 * hash + (this.format?.hashCode() ?: 0)
            hash = 31 * hash + (this.expiresAt?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ExportItineraryResult(" +
                "url=" + this.url +
                ", filename=" + this.filename +
                ", format=" + this.format +
                ", expiresAt=" + this.expiresAt +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ExportItineraryResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.Url? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: DataSchema.ExportFormat? = null
            internal var slot3: kotlin.time.Instant? = null

            /**
             * Signed download URL. It is valid for ten minutes, may be fetched once or many
             * times within that window, and requires no further authentication.
             */
            public var url: com.mountaya.client.Url?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Suggested file name: the itinerary's name keeping only the letters a to z and A
             * to Z, the accented letters French uses (such as é, è, à, ç and œ), digits,
             * spaces, dots, underscores and hyphens, trimmed, or "export" when nothing is
             * left, followed by the extension for the requested format. A name written in
             * another script, such as Chinese or Japanese, therefore comes out as "export".
             * Example: "Tour du Mont-Blanc.gpx".
             */
            public var filename: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Format the download is encoded in, echoed from the request. */
            public var format: DataSchema.ExportFormat?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * When the URL stops working, ten minutes after it was minted, as an RFC 3339
             * timestamp.
             * Example: "2026-07-28T12:10:00Z".
             */
            public var expiresAt: kotlin.time.Instant?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [DataSchema.ExportItineraryResult]. */
        public companion object {
            /** Returns a [DataSchema.ExportItineraryResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ExportItineraryResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ExportItineraryResult {
                val given = DataSchema.ExportItineraryResult.Builder().apply(optional)
                return DataSchema.ExportItineraryResult(
                    url = given.slot0,
                    filename = given.slot1,
                    format = given.slot2,
                    expiresAt = given.slot3,
                )
            }
        }
    }

    /** Result of a forward geocoding query. */
    @kotlinx.serialization.Serializable
    public class GeocodeResult private constructor(
        /** Number of places returned: the length of `places`, at most `limit`. */
        public val count: kotlin.Int? = null,
        /** List of named places ordered by relevance. */
        public val places: kotlin.collections.List<DataSchema.Place>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.GeocodeResult &&
                other.count == this.count &&
                other.places == this.places

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.places?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "GeocodeResult(" +
                "count=" + this.count +
                ", places=" + this.places +
                ")"

        /** Builder sets the optional properties of a [DataSchema.GeocodeResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.collections.List<DataSchema.Place>? = null

            /** Number of places returned: the length of `places`, at most `limit`. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** List of named places ordered by relevance. */
            public var places: kotlin.collections.List<DataSchema.Place>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.GeocodeResult]. */
        public companion object {
            /** Returns a [DataSchema.GeocodeResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.GeocodeResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.GeocodeResult {
                val given = DataSchema.GeocodeResult.Builder().apply(optional)
                return DataSchema.GeocodeResult(
                    count = given.slot0,
                    places = given.slot1,
                )
            }
        }
    }

    /** Surface, way type, and steepness analysis result for a geometry. */
    @kotlinx.serialization.Serializable
    public class GeometryAnalysis private constructor(
        /**
         * Elevation summary over the analyzed geometry, including altitude bounds, total
         * elevation gain/loss, and gradient extremes.
         */
        public val elevation: DataSchema.ElevationSummary? = null,
        /**
         * Elevation in meters above sea level, one value per vertex, in order: per input
         * coordinate for `analyzeGeometry`, and per vertex of the returned route geometry
         * for `directions` and `suggestions`. Lets the client build a 3D version of the
         * line for an elevation profile. A vertex beyond 85.0511 degrees of latitude,
         * north or south, lies past the elevation source's coverage: it reads 0, which
         * is no data rather than an altitude, and is left out of the ascent, descent,
         * altitude, gradient and slope figures, which describe the covered part alone.
         */
        public val elevations: kotlin.collections.List<kotlin.Double>? = null,
        /** Slope angle segments along the geometry in degrees, referencing coordinate indices. */
        public val slopeInfo: kotlin.collections.List<DataSchema.SlopeInfo>? = null,
        /**
         * Resolved travel speed segments along the geometry in km/h, referencing
         * coordinate indices.
         *
         * This is the per-stretch speed the duration was integrated over, so summing
         * each stretch's length divided by its value reproduces the duration exactly.
         * The model slows a climb, speeds a descent, and charges for a coarse surface or
         * a technical trail; an unanalysed stretch is charged for neither.
         */
        public val speedInfo: kotlin.collections.List<DataSchema.SpeedInfo>? = null,
        /**
         * Surface type segments along the geometry, referencing coordinate indices.
         * `analyzeGeometry` covers a stretch it has no data for, or one the routing
         * engine failed on (which `errors` then flags), with one UNKNOWN segment.
         * Empty for the activities that carry no surface, way type or trail difficulty
         * analysis: the four winter activities (BACKCOUNTRY_SKIING, SKI_TOURING,
         * CROSS_COUNTRY_SKIING and SNOWSHOE_WALKING).
         */
        public val surfaceInfo: kotlin.collections.List<DataSchema.SurfaceInfo>? = null,
        /**
         * Trail difficulty segments along the geometry, referencing coordinate indices.
         * Graded on the SAC hiking scale for walking activities and the mountain-biking
         * scale for cycling ones. `analyzeGeometry` covers a stretch it has no data for,
         * or one the routing engine failed on (which `errors` then flags), with one
         * UNKNOWN segment. Empty for the activities with no analysis (see
         * surfaceInfo), and on a route whose routing graph carries no trail-difficulty
         * data.
         */
        public val trailDifficultyInfo: kotlin.collections.List<DataSchema.TrailDifficultyInfo>? = null,
        /**
         * Way type segments along the geometry, referencing coordinate indices.
         * `analyzeGeometry` covers a stretch it has no data for, or one the routing
         * engine failed on (which `errors` then flags), with one UNKNOWN segment.
         * Empty for the activities with no analysis (see surfaceInfo).
         */
        public val wayTypeInfo: kotlin.collections.List<DataSchema.WayTypeInfo>? = null,
        /**
         * Route-level hazards the analyzed geometry crosses: tollways and
         * access-restricted ways. For `analyzeGeometry` this is the union over the whole
         * geometry, so a hazard anywhere along it raises the flag. Both flags are false
         * and both lists empty for the activities with no analysis (see surfaceInfo)
         * under `analyzeGeometry`, and when the activity cannot be routed at all.
         */
        public val advisories: DataSchema.RouteAdvisories? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.GeometryAnalysis &&
                other.elevation == this.elevation &&
                other.elevations == this.elevations &&
                other.slopeInfo == this.slopeInfo &&
                other.speedInfo == this.speedInfo &&
                other.surfaceInfo == this.surfaceInfo &&
                other.trailDifficultyInfo == this.trailDifficultyInfo &&
                other.wayTypeInfo == this.wayTypeInfo &&
                other.advisories == this.advisories

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.elevation?.hashCode() ?: 0)
            hash = 31 * hash + (this.elevations?.hashCode() ?: 0)
            hash = 31 * hash + (this.slopeInfo?.hashCode() ?: 0)
            hash = 31 * hash + (this.speedInfo?.hashCode() ?: 0)
            hash = 31 * hash + (this.surfaceInfo?.hashCode() ?: 0)
            hash = 31 * hash + (this.trailDifficultyInfo?.hashCode() ?: 0)
            hash = 31 * hash + (this.wayTypeInfo?.hashCode() ?: 0)
            hash = 31 * hash + (this.advisories?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "GeometryAnalysis(" +
                "elevation=" + this.elevation +
                ", elevations=" + this.elevations +
                ", slopeInfo=" + this.slopeInfo +
                ", speedInfo=" + this.speedInfo +
                ", surfaceInfo=" + this.surfaceInfo +
                ", trailDifficultyInfo=" + this.trailDifficultyInfo +
                ", wayTypeInfo=" + this.wayTypeInfo +
                ", advisories=" + this.advisories +
                ")"

        /** Builder sets the optional properties of a [DataSchema.GeometryAnalysis], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: DataSchema.ElevationSummary? = null
            internal var slot1: kotlin.collections.List<kotlin.Double>? = null
            internal var slot2: kotlin.collections.List<DataSchema.SlopeInfo>? = null
            internal var slot3: kotlin.collections.List<DataSchema.SpeedInfo>? = null
            internal var slot4: kotlin.collections.List<DataSchema.SurfaceInfo>? = null
            internal var slot5: kotlin.collections.List<DataSchema.TrailDifficultyInfo>? = null
            internal var slot6: kotlin.collections.List<DataSchema.WayTypeInfo>? = null
            internal var slot7: DataSchema.RouteAdvisories? = null

            /**
             * Elevation summary over the analyzed geometry, including altitude bounds, total
             * elevation gain/loss, and gradient extremes.
             */
            public var elevation: DataSchema.ElevationSummary?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Elevation in meters above sea level, one value per vertex, in order: per input
             * coordinate for `analyzeGeometry`, and per vertex of the returned route geometry
             * for `directions` and `suggestions`. Lets the client build a 3D version of the
             * line for an elevation profile. A vertex beyond 85.0511 degrees of latitude,
             * north or south, lies past the elevation source's coverage: it reads 0, which
             * is no data rather than an altitude, and is left out of the ascent, descent,
             * altitude, gradient and slope figures, which describe the covered part alone.
             */
            public var elevations: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Slope angle segments along the geometry in degrees, referencing coordinate indices. */
            public var slopeInfo: kotlin.collections.List<DataSchema.SlopeInfo>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Resolved travel speed segments along the geometry in km/h, referencing
             * coordinate indices.
             *
             * This is the per-stretch speed the duration was integrated over, so summing
             * each stretch's length divided by its value reproduces the duration exactly.
             * The model slows a climb, speeds a descent, and charges for a coarse surface or
             * a technical trail; an unanalysed stretch is charged for neither.
             */
            public var speedInfo: kotlin.collections.List<DataSchema.SpeedInfo>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Surface type segments along the geometry, referencing coordinate indices.
             * `analyzeGeometry` covers a stretch it has no data for, or one the routing
             * engine failed on (which `errors` then flags), with one UNKNOWN segment.
             * Empty for the activities that carry no surface, way type or trail difficulty
             * analysis: the four winter activities (BACKCOUNTRY_SKIING, SKI_TOURING,
             * CROSS_COUNTRY_SKIING and SNOWSHOE_WALKING).
             */
            public var surfaceInfo: kotlin.collections.List<DataSchema.SurfaceInfo>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Trail difficulty segments along the geometry, referencing coordinate indices.
             * Graded on the SAC hiking scale for walking activities and the mountain-biking
             * scale for cycling ones. `analyzeGeometry` covers a stretch it has no data for,
             * or one the routing engine failed on (which `errors` then flags), with one
             * UNKNOWN segment. Empty for the activities with no analysis (see
             * surfaceInfo), and on a route whose routing graph carries no trail-difficulty
             * data.
             */
            public var trailDifficultyInfo: kotlin.collections.List<DataSchema.TrailDifficultyInfo>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Way type segments along the geometry, referencing coordinate indices.
             * `analyzeGeometry` covers a stretch it has no data for, or one the routing
             * engine failed on (which `errors` then flags), with one UNKNOWN segment.
             * Empty for the activities with no analysis (see surfaceInfo).
             */
            public var wayTypeInfo: kotlin.collections.List<DataSchema.WayTypeInfo>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Route-level hazards the analyzed geometry crosses: tollways and
             * access-restricted ways. For `analyzeGeometry` this is the union over the whole
             * geometry, so a hazard anywhere along it raises the flag. Both flags are false
             * and both lists empty for the activities with no analysis (see surfaceInfo)
             * under `analyzeGeometry`, and when the activity cannot be routed at all.
             */
            public var advisories: DataSchema.RouteAdvisories?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }
        }

        /** Builds a [DataSchema.GeometryAnalysis]. */
        public companion object {
            /** Returns a [DataSchema.GeometryAnalysis] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.GeometryAnalysis.Builder.() -> kotlin.Unit = {},
            ): DataSchema.GeometryAnalysis {
                val given = DataSchema.GeometryAnalysis.Builder().apply(optional)
                return DataSchema.GeometryAnalysis(
                    elevation = given.slot0,
                    elevations = given.slot1,
                    slopeInfo = given.slot2,
                    speedInfo = given.slot3,
                    surfaceInfo = given.slot4,
                    trailDifficultyInfo = given.slot5,
                    wayTypeInfo = given.slot6,
                    advisories = given.slot7,
                )
            }
        }
    }

    /** A single isochrone polygon with its associated range value and metadata. */
    @kotlinx.serialization.Serializable
    public class Isochrone private constructor(
        /**
         * The requested range value this polygon was computed for, in seconds or meters
         * as the input range type says.
         */
        public val value: kotlin.Double? = null,
        /**
         * Area of the isochrone polygon in square meters. Null when the routing
         * provider did not report it for this polygon.
         */
        public val area: kotlin.Double? = null,
        /**
         * How much of the theoretically reachable ground the isochrone actually
         * covers, from 0 to 1. The theoretical area is the circle the same travel
         * budget would cover with no network and no terrain in the way, so a value
         * near 1 means the network radiates freely from the origin and a low value
         * means it is constrained: a valley floor, a coastline, a single road out.
         * Null when the routing provider did not report it for this polygon.
         */
        public val reachFactor: kotlin.Double? = null,
        /**
         * Center point of the isochrone as [longitude, latitude] (WGS 84). Null when the
         * routing provider reports no center for this isochrone: the value is forwarded
         * verbatim from the provider's response and is not reconstructed from the input
         * location, so its absence is reported as null rather than as an empty array.
         */
        public val center: kotlin.collections.List<kotlin.Double>? = null,
        /** Polygon geometry of the isochrone. */
        public val geometry: com.mountaya.client.GeoJsonGeometry? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.Isochrone &&
                other.value == this.value &&
                other.area == this.area &&
                other.reachFactor == this.reachFactor &&
                other.center == this.center &&
                other.geometry == this.geometry

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            hash = 31 * hash + (this.area?.hashCode() ?: 0)
            hash = 31 * hash + (this.reachFactor?.hashCode() ?: 0)
            hash = 31 * hash + (this.center?.hashCode() ?: 0)
            hash = 31 * hash + (this.geometry?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "Isochrone(" +
                "value=" + this.value +
                ", area=" + this.area +
                ", reachFactor=" + this.reachFactor +
                ", center=" + this.center +
                ", geometry=" + this.geometry +
                ")"

        /** Builder sets the optional properties of a [DataSchema.Isochrone], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Double? = null
            internal var slot1: kotlin.Double? = null
            internal var slot2: kotlin.Double? = null
            internal var slot3: kotlin.collections.List<kotlin.Double>? = null
            internal var slot4: com.mountaya.client.GeoJsonGeometry? = null

            /**
             * The requested range value this polygon was computed for, in seconds or meters
             * as the input range type says.
             */
            public var value: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Area of the isochrone polygon in square meters. Null when the routing
             * provider did not report it for this polygon.
             */
            public var area: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * How much of the theoretically reachable ground the isochrone actually
             * covers, from 0 to 1. The theoretical area is the circle the same travel
             * budget would cover with no network and no terrain in the way, so a value
             * near 1 means the network radiates freely from the origin and a low value
             * means it is constrained: a valley floor, a coastline, a single road out.
             * Null when the routing provider did not report it for this polygon.
             */
            public var reachFactor: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Center point of the isochrone as [longitude, latitude] (WGS 84). Null when the
             * routing provider reports no center for this isochrone: the value is forwarded
             * verbatim from the provider's response and is not reconstructed from the input
             * location, so its absence is reported as null rather than as an empty array.
             */
            public var center: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** Polygon geometry of the isochrone. */
            public var geometry: com.mountaya.client.GeoJsonGeometry?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }
        }

        /** Builds a [DataSchema.Isochrone]. */
        public companion object {
            /** Returns a [DataSchema.Isochrone] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.Isochrone.Builder.() -> kotlin.Unit = {},
            ): DataSchema.Isochrone {
                val given = DataSchema.Isochrone.Builder().apply(optional)
                return DataSchema.Isochrone(
                    value = given.slot0,
                    area = given.slot1,
                    reachFactor = given.slot2,
                    center = given.slot3,
                    geometry = given.slot4,
                )
            }
        }
    }

    /** Result of an isochrones computation. */
    @kotlinx.serialization.Serializable
    public class IsochronesResult private constructor(
        /**
         * List of computed isochrones: one per location and range value, in the order
         * the routing engine returns them. Read each one's `value` rather than relying
         * on its position.
         */
        public val isochrones: kotlin.collections.List<DataSchema.Isochrone>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.IsochronesResult &&
                other.isochrones == this.isochrones

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.isochrones?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "IsochronesResult(" +
                "isochrones=" + this.isochrones +
                ")"

        /** Builder sets the optional properties of a [DataSchema.IsochronesResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<DataSchema.Isochrone>? = null

            /**
             * List of computed isochrones: one per location and range value, in the order
             * the routing engine returns them. Read each one's `value` rather than relying
             * on its position.
             */
            public var isochrones: kotlin.collections.List<DataSchema.Isochrone>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [DataSchema.IsochronesResult]. */
        public companion object {
            /** Returns a [DataSchema.IsochronesResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.IsochronesResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.IsochronesResult {
                val given = DataSchema.IsochronesResult.Builder().apply(optional)
                return DataSchema.IsochronesResult(
                    isochrones = given.slot0,
                )
            }
        }
    }

    /** Result of the itineraries target. */
    @kotlinx.serialization.Serializable
    public class ItinerariesResult private constructor(
        /** Number of itineraries returned: the length of `itineraries`, at most `limit`. */
        public val count: kotlin.Int? = null,
        /** List of itinerary references ordered by relevance. */
        public val itineraries: kotlin.collections.List<DataSchema.ItinerarySearchResult>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ItinerariesResult &&
                other.count == this.count &&
                other.itineraries == this.itineraries

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.itineraries?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerariesResult(" +
                "count=" + this.count +
                ", itineraries=" + this.itineraries +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ItinerariesResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.collections.List<DataSchema.ItinerarySearchResult>? = null

            /** Number of itineraries returned: the length of `itineraries`, at most `limit`. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** List of itinerary references ordered by relevance. */
            public var itineraries: kotlin.collections.List<DataSchema.ItinerarySearchResult>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.ItinerariesResult]. */
        public companion object {
            /** Returns a [DataSchema.ItinerariesResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ItinerariesResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ItinerariesResult {
                val given = DataSchema.ItinerariesResult.Builder().apply(optional)
                return DataSchema.ItinerariesResult(
                    count = given.slot0,
                    itineraries = given.slot1,
                )
            }
        }
    }

    /** A single itinerary with its segments and waypoints. */
    @kotlinx.serialization.Serializable
    public class ItineraryResult private constructor(
        /** Itinerary identifier. */
        public val id: kotlin.String? = null,
        /** Display name of the itinerary. */
        public val name: kotlin.String? = null,
        /** Free-text description of the itinerary. Null when it has none. */
        public val description: kotlin.String? = null,
        /** Itinerary activity slug; null when no activity is assigned. */
        public val activitySlug: DataSchema.ActivitySlug? = null,
        /**
         * Total distance across all of the itinerary's segments, in meters; null when no
         * segment contributes a distance value (for example the itinerary has no segments,
         * or none have been computed).
         */
        public val distance: kotlin.Double? = null,
        /**
         * Total elevation gain across all of the itinerary's segments, in meters; null when
         * no segment contributes an ascent value.
         */
        public val ascent: kotlin.Int? = null,
        /**
         * Whether the itinerary is private. A private itinerary is served only to a key
         * of the organization that owns it; no other organization's key reaches it, even
         * with its id. A public one ('anyone with the link') is also served by id to any
         * other organization's key, as last published, but is never listed in another
         * organization's search results.
         */
        public val isPrivate: kotlin.Boolean? = null,
        /**
         * Identifier of the organization that owns this itinerary. Null for a personal
         * itinerary, made from an individual account and owned by no organization.
         */
        public val organizationId: kotlin.String? = null,
        /** Segments in itinerary order. */
        public val segments: kotlin.collections.List<DataSchema.ItinerarySegment>? = null,
        /** Waypoints in itinerary order. */
        public val waypoints: kotlin.collections.List<DataSchema.ItineraryWaypoint>? = null,
        /**
         * Segment groups defined on this itinerary, in no guaranteed order: key them by
         * `id` rather than by position. Flat rather than nested inside the segments: the
         * ordered segment chain stays the itinerary's primary structure, and each segment
         * points back at its group through `groupId`. Empty when the itinerary has no
         * groups.
         */
        public val groups: kotlin.collections.List<DataSchema.ItinerarySegmentGroup>? = null,
        /** Number of segments on the itinerary. Always equals the length of `segments`. */
        public val segmentCount: kotlin.Int? = null,
        /** Number of waypoints on the itinerary. Always equals the length of `waypoints`. */
        public val waypointCount: kotlin.Int? = null,
        /** Number of segment groups on the itinerary. Always equals the length of `groups`. */
        public val groupCount: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ItineraryResult &&
                other.id == this.id &&
                other.name == this.name &&
                other.description == this.description &&
                other.activitySlug == this.activitySlug &&
                other.distance == this.distance &&
                other.ascent == this.ascent &&
                other.isPrivate == this.isPrivate &&
                other.organizationId == this.organizationId &&
                other.segments == this.segments &&
                other.waypoints == this.waypoints &&
                other.groups == this.groups &&
                other.segmentCount == this.segmentCount &&
                other.waypointCount == this.waypointCount &&
                other.groupCount == this.groupCount

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.id?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + (this.activitySlug?.hashCode() ?: 0)
            hash = 31 * hash + (this.distance?.hashCode() ?: 0)
            hash = 31 * hash + (this.ascent?.hashCode() ?: 0)
            hash = 31 * hash + (this.isPrivate?.hashCode() ?: 0)
            hash = 31 * hash + (this.organizationId?.hashCode() ?: 0)
            hash = 31 * hash + (this.segments?.hashCode() ?: 0)
            hash = 31 * hash + (this.waypoints?.hashCode() ?: 0)
            hash = 31 * hash + (this.groups?.hashCode() ?: 0)
            hash = 31 * hash + (this.segmentCount?.hashCode() ?: 0)
            hash = 31 * hash + (this.waypointCount?.hashCode() ?: 0)
            hash = 31 * hash + (this.groupCount?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItineraryResult(" +
                "id=" + this.id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", activitySlug=" + this.activitySlug +
                ", distance=" + this.distance +
                ", ascent=" + this.ascent +
                ", isPrivate=" + this.isPrivate +
                ", organizationId=" + this.organizationId +
                ", segments=" + this.segments +
                ", waypoints=" + this.waypoints +
                ", groups=" + this.groups +
                ", segmentCount=" + this.segmentCount +
                ", waypointCount=" + this.waypointCount +
                ", groupCount=" + this.groupCount +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ItineraryResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: DataSchema.ActivitySlug? = null
            internal var slot4: kotlin.Double? = null
            internal var slot5: kotlin.Int? = null
            internal var slot6: kotlin.Boolean? = null
            internal var slot7: kotlin.String? = null
            internal var slot8: kotlin.collections.List<DataSchema.ItinerarySegment>? = null
            internal var slot9: kotlin.collections.List<DataSchema.ItineraryWaypoint>? = null
            internal var slot10: kotlin.collections.List<DataSchema.ItinerarySegmentGroup>? = null
            internal var slot11: kotlin.Int? = null
            internal var slot12: kotlin.Int? = null
            internal var slot13: kotlin.Int? = null

            /** Itinerary identifier. */
            public var id: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Display name of the itinerary. */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Free-text description of the itinerary. Null when it has none. */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Itinerary activity slug; null when no activity is assigned. */
            public var activitySlug: DataSchema.ActivitySlug?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Total distance across all of the itinerary's segments, in meters; null when no
             * segment contributes a distance value (for example the itinerary has no segments,
             * or none have been computed).
             */
            public var distance: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Total elevation gain across all of the itinerary's segments, in meters; null when
             * no segment contributes an ascent value.
             */
            public var ascent: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Whether the itinerary is private. A private itinerary is served only to a key
             * of the organization that owns it; no other organization's key reaches it, even
             * with its id. A public one ('anyone with the link') is also served by id to any
             * other organization's key, as last published, but is never listed in another
             * organization's search results.
             */
            public var isPrivate: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Identifier of the organization that owns this itinerary. Null for a personal
             * itinerary, made from an individual account and owned by no organization.
             */
            public var organizationId: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /** Segments in itinerary order. */
            public var segments: kotlin.collections.List<DataSchema.ItinerarySegment>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /** Waypoints in itinerary order. */
            public var waypoints: kotlin.collections.List<DataSchema.ItineraryWaypoint>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }

            /**
             * Segment groups defined on this itinerary, in no guaranteed order: key them by
             * `id` rather than by position. Flat rather than nested inside the segments: the
             * ordered segment chain stays the itinerary's primary structure, and each segment
             * points back at its group through `groupId`. Empty when the itinerary has no
             * groups.
             */
            public var groups: kotlin.collections.List<DataSchema.ItinerarySegmentGroup>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot10 = value
                }

            /** Number of segments on the itinerary. Always equals the length of `segments`. */
            public var segmentCount: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot11 = value
                }

            /** Number of waypoints on the itinerary. Always equals the length of `waypoints`. */
            public var waypointCount: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot12 = value
                }

            /** Number of segment groups on the itinerary. Always equals the length of `groups`. */
            public var groupCount: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot13 = value
                }
        }

        /** Builds a [DataSchema.ItineraryResult]. */
        public companion object {
            /** Returns a [DataSchema.ItineraryResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ItineraryResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ItineraryResult {
                val given = DataSchema.ItineraryResult.Builder().apply(optional)
                return DataSchema.ItineraryResult(
                    id = given.slot0,
                    name = given.slot1,
                    description = given.slot2,
                    activitySlug = given.slot3,
                    distance = given.slot4,
                    ascent = given.slot5,
                    isPrivate = given.slot6,
                    organizationId = given.slot7,
                    segments = given.slot8,
                    waypoints = given.slot9,
                    groups = given.slot10,
                    segmentCount = given.slot11,
                    waypointCount = given.slot12,
                    groupCount = given.slot13,
                )
            }
        }
    }

    /** A lightweight itinerary reference returned by the itineraries target. */
    @kotlinx.serialization.Serializable
    public class ItinerarySearchResult private constructor(
        /** Itinerary identifier. */
        public val id: kotlin.String? = null,
        /** Display name of the itinerary. */
        public val name: kotlin.String? = null,
        /** Free-text description of the itinerary. Null when it has none. */
        public val description: kotlin.String? = null,
        /**
         * Total distance across all of the itinerary's segments, in meters (the sum of
         * each segment's current distance); null when no segment contributes a distance
         * value (e.g. the itinerary has no segments).
         */
        public val distance: kotlin.Double? = null,
        /**
         * Total elevation gain across all of the itinerary's segments, in meters (the
         * sum of each segment's current ascent); null when no segment contributes an
         * ascent value (e.g. the itinerary has no segments).
         */
        public val ascent: kotlin.Int? = null,
        /** Itinerary activity slug; null when no activity is assigned. */
        public val activitySlug: DataSchema.ActivitySlug? = null,
        /**
         * Representative start coordinate as [longitude, latitude]; null when not
         * available.
         */
        public val start: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Itinerary bounding box as [minLongitude, minLatitude, maxLongitude, maxLatitude];
         * null when not available.
         */
        public val bounds: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Whether the itinerary is private. A private itinerary is served only to a key
         * of the organization that owns it; no other organization's key reaches it, even
         * with its id. A public one ('anyone with the link') is also served by id to any
         * other organization's key, as last published, but is never listed in another
         * organization's search results.
         */
        public val isPrivate: kotlin.Boolean? = null,
        /**
         * Identifier of the organization that owns this itinerary. Null for a personal
         * itinerary, made from an individual account and owned by no organization.
         */
        public val organizationId: kotlin.String? = null,
        /**
         * Relevance rank over the itinerary's name and description, always greater than 0,
         * higher being a better match; a match in the name weighs twice one in the
         * description. It orders the itineraries section of ONE response and nothing more.
         * The value comes from whichever of two ranking passes produced the match: a
         * full-text rank, which is unbounded above though in practice below 1, or, when
         * the full-text pass matches nothing and the query is at least two characters
         * long, a fuzzy similarity between 0.3 and 1. The two scales differ, so a score is
         * not comparable across responses, across targets, or against a fixed threshold.
         */
        public val score: kotlin.Double? = null,
        /** Field that produced the match. */
        public val matchedField: DataSchema.MatchedField? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ItinerarySearchResult &&
                other.id == this.id &&
                other.name == this.name &&
                other.description == this.description &&
                other.distance == this.distance &&
                other.ascent == this.ascent &&
                other.activitySlug == this.activitySlug &&
                other.start == this.start &&
                other.bounds == this.bounds &&
                other.isPrivate == this.isPrivate &&
                other.organizationId == this.organizationId &&
                other.score == this.score &&
                other.matchedField == this.matchedField

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.id?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + (this.distance?.hashCode() ?: 0)
            hash = 31 * hash + (this.ascent?.hashCode() ?: 0)
            hash = 31 * hash + (this.activitySlug?.hashCode() ?: 0)
            hash = 31 * hash + (this.start?.hashCode() ?: 0)
            hash = 31 * hash + (this.bounds?.hashCode() ?: 0)
            hash = 31 * hash + (this.isPrivate?.hashCode() ?: 0)
            hash = 31 * hash + (this.organizationId?.hashCode() ?: 0)
            hash = 31 * hash + (this.score?.hashCode() ?: 0)
            hash = 31 * hash + (this.matchedField?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerarySearchResult(" +
                "id=" + this.id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", distance=" + this.distance +
                ", ascent=" + this.ascent +
                ", activitySlug=" + this.activitySlug +
                ", start=" + this.start +
                ", bounds=" + this.bounds +
                ", isPrivate=" + this.isPrivate +
                ", organizationId=" + this.organizationId +
                ", score=" + this.score +
                ", matchedField=" + this.matchedField +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ItinerarySearchResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.Double? = null
            internal var slot4: kotlin.Int? = null
            internal var slot5: DataSchema.ActivitySlug? = null
            internal var slot6: kotlin.collections.List<kotlin.Double>? = null
            internal var slot7: kotlin.collections.List<kotlin.Double>? = null
            internal var slot8: kotlin.Boolean? = null
            internal var slot9: kotlin.String? = null
            internal var slot10: kotlin.Double? = null
            internal var slot11: DataSchema.MatchedField? = null

            /** Itinerary identifier. */
            public var id: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Display name of the itinerary. */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Free-text description of the itinerary. Null when it has none. */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Total distance across all of the itinerary's segments, in meters (the sum of
             * each segment's current distance); null when no segment contributes a distance
             * value (e.g. the itinerary has no segments).
             */
            public var distance: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Total elevation gain across all of the itinerary's segments, in meters (the
             * sum of each segment's current ascent); null when no segment contributes an
             * ascent value (e.g. the itinerary has no segments).
             */
            public var ascent: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Itinerary activity slug; null when no activity is assigned. */
            public var activitySlug: DataSchema.ActivitySlug?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Representative start coordinate as [longitude, latitude]; null when not
             * available.
             */
            public var start: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Itinerary bounding box as [minLongitude, minLatitude, maxLongitude, maxLatitude];
             * null when not available.
             */
            public var bounds: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /**
             * Whether the itinerary is private. A private itinerary is served only to a key
             * of the organization that owns it; no other organization's key reaches it, even
             * with its id. A public one ('anyone with the link') is also served by id to any
             * other organization's key, as last published, but is never listed in another
             * organization's search results.
             */
            public var isPrivate: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /**
             * Identifier of the organization that owns this itinerary. Null for a personal
             * itinerary, made from an individual account and owned by no organization.
             */
            public var organizationId: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }

            /**
             * Relevance rank over the itinerary's name and description, always greater than 0,
             * higher being a better match; a match in the name weighs twice one in the
             * description. It orders the itineraries section of ONE response and nothing more.
             * The value comes from whichever of two ranking passes produced the match: a
             * full-text rank, which is unbounded above though in practice below 1, or, when
             * the full-text pass matches nothing and the query is at least two characters
             * long, a fuzzy similarity between 0.3 and 1. The two scales differ, so a score is
             * not comparable across responses, across targets, or against a fixed threshold.
             */
            public var score: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot10 = value
                }

            /** Field that produced the match. */
            public var matchedField: DataSchema.MatchedField?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot11 = value
                }
        }

        /** Builds a [DataSchema.ItinerarySearchResult]. */
        public companion object {
            /** Returns a [DataSchema.ItinerarySearchResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ItinerarySearchResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ItinerarySearchResult {
                val given = DataSchema.ItinerarySearchResult.Builder().apply(optional)
                return DataSchema.ItinerarySearchResult(
                    id = given.slot0,
                    name = given.slot1,
                    description = given.slot2,
                    distance = given.slot3,
                    ascent = given.slot4,
                    activitySlug = given.slot5,
                    start = given.slot6,
                    bounds = given.slot7,
                    isPrivate = given.slot8,
                    organizationId = given.slot9,
                    score = given.slot10,
                    matchedField = given.slot11,
                )
            }
        }
    }

    /**
     * One segment of an itinerary: a single routed or drawn leg, with its geometry and
     * the elevation figures computed for it.
     */
    @kotlinx.serialization.Serializable
    public class ItinerarySegment private constructor(
        /** Segment identifier. */
        public val id: kotlin.String? = null,
        /** Display name of the segment. Example: "Day 1". */
        public val name: kotlin.String? = null,
        /** Free-text description of the segment. Null when it has none. */
        public val description: kotlin.String? = null,
        /** Zero-based position of the segment within the itinerary's ordered chain. */
        public val position: kotlin.Int? = null,
        /**
         * Identifier of the segment group this segment belongs to, matching an entry of the
         * itinerary's `groups`; null when the segment belongs to none. Grouping is
         * presentation only and never changes the segment itself.
         */
        public val groupId: kotlin.String? = null,
        /** Segment distance in meters; null when the segment has not been computed. */
        public val distance: kotlin.Double? = null,
        /** Segment duration in seconds; null when the segment has not been computed. */
        public val duration: kotlin.Double? = null,
        /** Elevation gain along the segment in meters; null when unknown. */
        public val ascent: kotlin.Int? = null,
        /** Elevation loss along the segment in meters; null when unknown. */
        public val descent: kotlin.Int? = null,
        /**
         * Altitude range along the segment in meters above sea level; null when the segment
         * carries no elevation data.
         */
        public val altitude: DataSchema.MetricRange? = null,
        /**
         * Gradient range along the segment in degrees, positive uphill and negative
         * downhill; null when the segment carries no elevation data.
         */
        public val gradient: DataSchema.MetricRange? = null,
        /**
         * GeoJSON Geometry object for the segment path; null when the segment has no
         * geometry yet. It is one of two shapes. A drawn segment is a bare LineString,
         * simplified and resampled to about one position every 50 m. A routed segment
         * is a GeometryCollection holding the routed LineString and a MultiPoint of
         * the waypoints it was routed through, and so is a drawn one when resampling
         * added positions between the vertices it was drawn with and those vertices
         * number fewer than its sample budget, one position per 50 m of its distance
         * plus two, never fewer than 400 and never more than 4,000: its MultiPoint
         * then holds the vertices drawn. A dense recorded track is therefore a bare
         * LineString even where resampling filled a gap in it. A collection of one
         * member is never sent.
         *
         * Each position is [longitude, latitude, altitude] once the segment's
         * elevation has been read, and [longitude, latitude] otherwise. To reason
         * about the ground the segment covers with terrainProfile or analyzeGeometry,
         * take the LineString, the geometry itself or the collection's LineString
         * member, and send its positions reduced to [longitude, latitude] as their
         * `coordinates`: both take exactly two numbers per position and at most
         * 10,000 positions, so a longer line is sent in parts.
         */
        public val geometry: com.mountaya.client.GeoJsonGeometry? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ItinerarySegment &&
                other.id == this.id &&
                other.name == this.name &&
                other.description == this.description &&
                other.position == this.position &&
                other.groupId == this.groupId &&
                other.distance == this.distance &&
                other.duration == this.duration &&
                other.ascent == this.ascent &&
                other.descent == this.descent &&
                other.altitude == this.altitude &&
                other.gradient == this.gradient &&
                other.geometry == this.geometry

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.id?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + (this.position?.hashCode() ?: 0)
            hash = 31 * hash + (this.groupId?.hashCode() ?: 0)
            hash = 31 * hash + (this.distance?.hashCode() ?: 0)
            hash = 31 * hash + (this.duration?.hashCode() ?: 0)
            hash = 31 * hash + (this.ascent?.hashCode() ?: 0)
            hash = 31 * hash + (this.descent?.hashCode() ?: 0)
            hash = 31 * hash + (this.altitude?.hashCode() ?: 0)
            hash = 31 * hash + (this.gradient?.hashCode() ?: 0)
            hash = 31 * hash + (this.geometry?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerarySegment(" +
                "id=" + this.id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", position=" + this.position +
                ", groupId=" + this.groupId +
                ", distance=" + this.distance +
                ", duration=" + this.duration +
                ", ascent=" + this.ascent +
                ", descent=" + this.descent +
                ", altitude=" + this.altitude +
                ", gradient=" + this.gradient +
                ", geometry=" + this.geometry +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ItinerarySegment], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.Int? = null
            internal var slot4: kotlin.String? = null
            internal var slot5: kotlin.Double? = null
            internal var slot6: kotlin.Double? = null
            internal var slot7: kotlin.Int? = null
            internal var slot8: kotlin.Int? = null
            internal var slot9: DataSchema.MetricRange? = null
            internal var slot10: DataSchema.MetricRange? = null
            internal var slot11: com.mountaya.client.GeoJsonGeometry? = null

            /** Segment identifier. */
            public var id: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Display name of the segment. Example: "Day 1". */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Free-text description of the segment. Null when it has none. */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Zero-based position of the segment within the itinerary's ordered chain. */
            public var position: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Identifier of the segment group this segment belongs to, matching an entry of the
             * itinerary's `groups`; null when the segment belongs to none. Grouping is
             * presentation only and never changes the segment itself.
             */
            public var groupId: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Segment distance in meters; null when the segment has not been computed. */
            public var distance: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /** Segment duration in seconds; null when the segment has not been computed. */
            public var duration: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /** Elevation gain along the segment in meters; null when unknown. */
            public var ascent: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /** Elevation loss along the segment in meters; null when unknown. */
            public var descent: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /**
             * Altitude range along the segment in meters above sea level; null when the segment
             * carries no elevation data.
             */
            public var altitude: DataSchema.MetricRange?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }

            /**
             * Gradient range along the segment in degrees, positive uphill and negative
             * downhill; null when the segment carries no elevation data.
             */
            public var gradient: DataSchema.MetricRange?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot10 = value
                }

            /**
             * GeoJSON Geometry object for the segment path; null when the segment has no
             * geometry yet. It is one of two shapes. A drawn segment is a bare LineString,
             * simplified and resampled to about one position every 50 m. A routed segment
             * is a GeometryCollection holding the routed LineString and a MultiPoint of
             * the waypoints it was routed through, and so is a drawn one when resampling
             * added positions between the vertices it was drawn with and those vertices
             * number fewer than its sample budget, one position per 50 m of its distance
             * plus two, never fewer than 400 and never more than 4,000: its MultiPoint
             * then holds the vertices drawn. A dense recorded track is therefore a bare
             * LineString even where resampling filled a gap in it. A collection of one
             * member is never sent.
             *
             * Each position is [longitude, latitude, altitude] once the segment's
             * elevation has been read, and [longitude, latitude] otherwise. To reason
             * about the ground the segment covers with terrainProfile or analyzeGeometry,
             * take the LineString, the geometry itself or the collection's LineString
             * member, and send its positions reduced to [longitude, latitude] as their
             * `coordinates`: both take exactly two numbers per position and at most
             * 10,000 positions, so a longer line is sent in parts.
             */
            public var geometry: com.mountaya.client.GeoJsonGeometry?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot11 = value
                }
        }

        /** Builds a [DataSchema.ItinerarySegment]. */
        public companion object {
            /** Returns a [DataSchema.ItinerarySegment] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ItinerarySegment.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ItinerarySegment {
                val given = DataSchema.ItinerarySegment.Builder().apply(optional)
                return DataSchema.ItinerarySegment(
                    id = given.slot0,
                    name = given.slot1,
                    description = given.slot2,
                    position = given.slot3,
                    groupId = given.slot4,
                    distance = given.slot5,
                    duration = given.slot6,
                    ascent = given.slot7,
                    descent = given.slot8,
                    altitude = given.slot9,
                    gradient = given.slot10,
                    geometry = given.slot11,
                )
            }
        }
    }

    /**
     * A named, colored grouping over a contiguous run of an itinerary's segments, so
     * several physically-separate segments read as one logical leg ("Day 1", "Stage 3").
     *
     * A group is presentation only. Its members are unchanged by grouping: each keeps its
     * own geometry, metrics, and position. The group overrides only the name, color, and
     * (when set) the speed shown for that run. Membership is carried on each member
     * segment's `groupId` rather than nested here.
     */
    @kotlinx.serialization.Serializable
    public class ItinerarySegmentGroup private constructor(
        /** Group identifier. */
        public val id: kotlin.String? = null,
        /** Display name of the group. Example: "Day 1". */
        public val name: kotlin.String? = null,
        /**
         * Hex color code used to paint the whole run as one band. Always set, because a
         * group with no color would be indistinguishable from no group at all.
         */
        public val color: kotlin.String? = null,
        /**
         * Travel speed in km/h applied to the whole run when computing the displayed
         * duration; null means no override, so each member keeps its own speed and the run's
         * duration is the sum of its members'.
         */
        public val speed: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ItinerarySegmentGroup &&
                other.id == this.id &&
                other.name == this.name &&
                other.color == this.color &&
                other.speed == this.speed

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.id?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.color?.hashCode() ?: 0)
            hash = 31 * hash + (this.speed?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerarySegmentGroup(" +
                "id=" + this.id +
                ", name=" + this.name +
                ", color=" + this.color +
                ", speed=" + this.speed +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ItinerarySegmentGroup], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.Double? = null

            /** Group identifier. */
            public var id: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Display name of the group. Example: "Day 1". */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Hex color code used to paint the whole run as one band. Always set, because a
             * group with no color would be indistinguishable from no group at all.
             */
            public var color: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Travel speed in km/h applied to the whole run when computing the displayed
             * duration; null means no override, so each member keeps its own speed and the run's
             * duration is the sum of its members'.
             */
            public var speed: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [DataSchema.ItinerarySegmentGroup]. */
        public companion object {
            /** Returns a [DataSchema.ItinerarySegmentGroup] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ItinerarySegmentGroup.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ItinerarySegmentGroup {
                val given = DataSchema.ItinerarySegmentGroup.Builder().apply(optional)
                return DataSchema.ItinerarySegmentGroup(
                    id = given.slot0,
                    name = given.slot1,
                    color = given.slot2,
                    speed = given.slot3,
                )
            }
        }
    }

    /** One waypoint of an itinerary: a single point along it. */
    @kotlinx.serialization.Serializable
    public class ItineraryWaypoint private constructor(
        /** Waypoint identifier. */
        public val id: kotlin.String? = null,
        /**
         * Display name of the waypoint. Null only for a POINT, a standalone marker, which
         * may be unnamed; a POI, a point of interest, always carries one.
         */
        public val name: kotlin.String? = null,
        /** Free-text description of the waypoint. Null when it has none. */
        public val description: kotlin.String? = null,
        /** Zero-based position of the waypoint within the itinerary's ordered chain. */
        public val position: kotlin.Int? = null,
        /**
         * Waypoint location as [longitude, latitude] (WGS 84); null when the waypoint has
         * no geometry.
         */
        public val point: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Services and facilities available at this waypoint, in no particular order and
         * without duplicates. Empty when none are declared, never null, so it can be
         * iterated unconditionally.
         */
        public val amenities: kotlin.collections.List<DataSchema.WaypointAmenity>? = null,
        /** Cut-off and planned stop; null when the waypoint carries no timing. */
        public val timing: DataSchema.WaypointTiming? = null,
        /** How to reach the place this waypoint marks; null when the waypoint carries no contact detail. */
        public val contact: DataSchema.WaypointContact? = null,
        /** Photos and external links; null when none are attached, so a non-null value carries at least one of the two lists. */
        public val media: DataSchema.WaypointMedia? = null,
        /**
         * Customer-defined fields, in the order they should be shown. Keys are unique
         * within a waypoint. Null when the waypoint defines none. The empty case is
         * carried as null rather than as an empty list, so check for null before
         * iterating.
         */
        public val properties: kotlin.collections.List<DataSchema.WaypointProperty>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ItineraryWaypoint &&
                other.id == this.id &&
                other.name == this.name &&
                other.description == this.description &&
                other.position == this.position &&
                other.point == this.point &&
                other.amenities == this.amenities &&
                other.timing == this.timing &&
                other.contact == this.contact &&
                other.media == this.media &&
                other.properties == this.properties

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.id?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + (this.position?.hashCode() ?: 0)
            hash = 31 * hash + (this.point?.hashCode() ?: 0)
            hash = 31 * hash + (this.amenities?.hashCode() ?: 0)
            hash = 31 * hash + (this.timing?.hashCode() ?: 0)
            hash = 31 * hash + (this.contact?.hashCode() ?: 0)
            hash = 31 * hash + (this.media?.hashCode() ?: 0)
            hash = 31 * hash + (this.properties?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItineraryWaypoint(" +
                "id=" + this.id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", position=" + this.position +
                ", point=" + this.point +
                ", amenities=" + this.amenities +
                ", timing=" + this.timing +
                ", contact=" + this.contact +
                ", media=" + this.media +
                ", properties=" + this.properties +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ItineraryWaypoint], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.Int? = null
            internal var slot4: kotlin.collections.List<kotlin.Double>? = null
            internal var slot5: kotlin.collections.List<DataSchema.WaypointAmenity>? = null
            internal var slot6: DataSchema.WaypointTiming? = null
            internal var slot7: DataSchema.WaypointContact? = null
            internal var slot8: DataSchema.WaypointMedia? = null
            internal var slot9: kotlin.collections.List<DataSchema.WaypointProperty>? = null

            /** Waypoint identifier. */
            public var id: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Display name of the waypoint. Null only for a POINT, a standalone marker, which
             * may be unnamed; a POI, a point of interest, always carries one.
             */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Free-text description of the waypoint. Null when it has none. */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Zero-based position of the waypoint within the itinerary's ordered chain. */
            public var position: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Waypoint location as [longitude, latitude] (WGS 84); null when the waypoint has
             * no geometry.
             */
            public var point: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Services and facilities available at this waypoint, in no particular order and
             * without duplicates. Empty when none are declared, never null, so it can be
             * iterated unconditionally.
             */
            public var amenities: kotlin.collections.List<DataSchema.WaypointAmenity>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /** Cut-off and planned stop; null when the waypoint carries no timing. */
            public var timing: DataSchema.WaypointTiming?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /** How to reach the place this waypoint marks; null when the waypoint carries no contact detail. */
            public var contact: DataSchema.WaypointContact?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /** Photos and external links; null when none are attached, so a non-null value carries at least one of the two lists. */
            public var media: DataSchema.WaypointMedia?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /**
             * Customer-defined fields, in the order they should be shown. Keys are unique
             * within a waypoint. Null when the waypoint defines none. The empty case is
             * carried as null rather than as an empty list, so check for null before
             * iterating.
             */
            public var properties: kotlin.collections.List<DataSchema.WaypointProperty>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }
        }

        /** Builds a [DataSchema.ItineraryWaypoint]. */
        public companion object {
            /** Returns a [DataSchema.ItineraryWaypoint] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ItineraryWaypoint.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ItineraryWaypoint {
                val given = DataSchema.ItineraryWaypoint.Builder().apply(optional)
                return DataSchema.ItineraryWaypoint(
                    id = given.slot0,
                    name = given.slot1,
                    description = given.slot2,
                    position = given.slot3,
                    point = given.slot4,
                    amenities = given.slot5,
                    timing = given.slot6,
                    contact = given.slot7,
                    media = given.slot8,
                    properties = given.slot9,
                )
            }
        }
    }

    /**
     * Result of a matrix computation.
     *
     * These two grids are the only sparse lists in this schema. Everywhere else a
     * list of non-null elements is exactly that; here an inner value may be null,
     * because a pair with no route between it has no number to report. A client
     * that has generalized "lists here are never sparse" will get these wrong, so
     * check each cell before using it.
     */
    @kotlinx.serialization.Serializable
    public class MatrixResult private constructor(
        /**
         * Duration grid in seconds, one row per source and one column per destination.
         * These are the routing engine's own estimates: they take no `speed` and do not
         * use the terrain-aware model behind `duration` on `directions`, `suggestions`
         * and `analyzeGeometry`, so they are not comparable with it. Null inner values
         * indicate unreachable pairs. Only present when DURATION metric is requested.
         */
        public val durations: kotlin.collections.List<kotlin.collections.List<kotlin.Double?>>? = null,
        /**
         * Distance grid in meters, one row per source and one column per destination:
         * the routing engine's own figures, with no geometry returned to measure them
         * against. Null inner values indicate unreachable pairs. Only present when
         * DISTANCE metric is requested.
         */
        public val distances: kotlin.collections.List<kotlin.collections.List<kotlin.Double?>>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.MatrixResult &&
                other.durations == this.durations &&
                other.distances == this.distances

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.durations?.hashCode() ?: 0)
            hash = 31 * hash + (this.distances?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "MatrixResult(" +
                "durations=" + this.durations +
                ", distances=" + this.distances +
                ")"

        /** Builder sets the optional properties of a [DataSchema.MatrixResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<kotlin.collections.List<kotlin.Double?>>? = null
            internal var slot1: kotlin.collections.List<kotlin.collections.List<kotlin.Double?>>? = null

            /**
             * Duration grid in seconds, one row per source and one column per destination.
             * These are the routing engine's own estimates: they take no `speed` and do not
             * use the terrain-aware model behind `duration` on `directions`, `suggestions`
             * and `analyzeGeometry`, so they are not comparable with it. Null inner values
             * indicate unreachable pairs. Only present when DURATION metric is requested.
             */
            public var durations: kotlin.collections.List<kotlin.collections.List<kotlin.Double?>>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Distance grid in meters, one row per source and one column per destination:
             * the routing engine's own figures, with no geometry returned to measure them
             * against. Null inner values indicate unreachable pairs. Only present when
             * DISTANCE metric is requested.
             */
            public var distances: kotlin.collections.List<kotlin.collections.List<kotlin.Double?>>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.MatrixResult]. */
        public companion object {
            /** Returns a [DataSchema.MatrixResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.MatrixResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.MatrixResult {
                val given = DataSchema.MatrixResult.Builder().apply(optional)
                return DataSchema.MatrixResult(
                    durations = given.slot0,
                    distances = given.slot1,
                )
            }
        }
    }

    /** A minimum/maximum range. Units depend on the context in which it is used. */
    @kotlinx.serialization.Serializable
    public class MetricRange private constructor(
        /** Upper bound of the range. */
        public val max: kotlin.Double? = null,
        /** Lower bound of the range. */
        public val min: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.MetricRange &&
                other.max == this.max &&
                other.min == this.min

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.max?.hashCode() ?: 0)
            hash = 31 * hash + (this.min?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "MetricRange(" +
                "max=" + this.max +
                ", min=" + this.min +
                ")"

        /** Builder sets the optional properties of a [DataSchema.MetricRange], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Double? = null
            internal var slot1: kotlin.Double? = null

            /** Upper bound of the range. */
            public var max: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Lower bound of the range. */
            public var min: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.MetricRange]. */
        public companion object {
            /** Returns a [DataSchema.MetricRange] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.MetricRange.Builder.() -> kotlin.Unit = {},
            ): DataSchema.MetricRange {
                val given = DataSchema.MetricRange.Builder().apply(optional)
                return DataSchema.MetricRange(
                    max = given.slot0,
                    min = given.slot1,
                )
            }
        }
    }

    /** Reference to an OpenStreetMap feature with the matched tag. */
    @kotlinx.serialization.Serializable
    public class OSMRef private constructor(
        /** OpenStreetMap identifier of the feature. */
        public val id: kotlin.String? = null,
        /** Primitive kind of the feature: node, way, or relation. */
        public val type: DataSchema.OSMType? = null,
        /** Matched OSM tag key. Example: "place" or "highway". */
        public val key: kotlin.String? = null,
        /** Matched OSM tag value. Example: "city" or "residential". */
        public val value: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.OSMRef &&
                other.id == this.id &&
                other.type == this.type &&
                other.key == this.key &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.id?.hashCode() ?: 0)
            hash = 31 * hash + (this.type?.hashCode() ?: 0)
            hash = 31 * hash + (this.key?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OSMRef(" +
                "id=" + this.id +
                ", type=" + this.type +
                ", key=" + this.key +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [DataSchema.OSMRef], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: DataSchema.OSMType? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.String? = null

            /** OpenStreetMap identifier of the feature. */
            public var id: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Primitive kind of the feature: node, way, or relation. */
            public var type: DataSchema.OSMType?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Matched OSM tag key. Example: "place" or "highway". */
            public var key: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Matched OSM tag value. Example: "city" or "residential". */
            public var value: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [DataSchema.OSMRef]. */
        public companion object {
            /** Returns a [DataSchema.OSMRef] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.OSMRef.Builder.() -> kotlin.Unit = {},
            ): DataSchema.OSMRef {
                val given = DataSchema.OSMRef.Builder().apply(optional)
                return DataSchema.OSMRef(
                    id = given.slot0,
                    type = given.slot1,
                    key = given.slot2,
                    value = given.slot3,
                )
            }
        }
    }

    /** A single OpenStreetMap tag attached to a feature. */
    @kotlinx.serialization.Serializable
    public class OSMTag private constructor(
        /** OSM tag key. Example: "ele" or "name:fr". */
        public val key: kotlin.String? = null,
        /** OSM tag value. Example: "4810" or "Mont Blanc". */
        public val value: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.OSMTag &&
                other.key == this.key &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.key?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OSMTag(" +
                "key=" + this.key +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [DataSchema.OSMTag], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null

            /** OSM tag key. Example: "ele" or "name:fr". */
            public var key: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** OSM tag value. Example: "4810" or "Mont Blanc". */
            public var value: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.OSMTag]. */
        public companion object {
            /** Returns a [DataSchema.OSMTag] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.OSMTag.Builder.() -> kotlin.Unit = {},
            ): DataSchema.OSMTag {
                val given = DataSchema.OSMTag.Builder().apply(optional)
                return DataSchema.OSMTag(
                    key = given.slot0,
                    value = given.slot1,
                )
            }
        }
    }

    /** A single point-of-interest result. */
    @kotlinx.serialization.Serializable
    public class POI private constructor(
        /** Reference to the underlying OpenStreetMap feature. */
        public val osm: DataSchema.OSMRef? = null,
        /** Localized display name. */
        public val name: kotlin.String? = null,
        /** Canonical POI category mapped from the underlying OSM tags. */
        public val category: DataSchema.POICategory? = null,
        /** Centroid as [longitude, latitude]. */
        public val point: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Elevation in meters above sea level, read from the OSM "ele" tag. Often null:
         * present only where the place data carries that tag.
         */
        public val elevation: kotlin.Double? = null,
        /**
         * Selected OSM tags useful for UI rendering, present only where the place data
         * carries them. Not exhaustive, and often empty.
         */
        public val tags: kotlin.collections.List<DataSchema.OSMTag>? = null,
        /**
         * Distance in meters from the buffered geometry itself, measured to its nearest
         * edge rather than its nearest vertex, and 0 for a POI inside a Polygon (outside
         * its holes). Only set when the query used a buffered area.
         */
        public val distanceMeters: kotlin.Double? = null,
        /**
         * ISO 3166-1 alpha-2 country code, e.g. "FR" or "BE". Null when the
         * upstream geocoder did not resolve admin context.
         */
        public val country: kotlin.String? = null,
        /** Localized display name of the POI's country. Null when not available. */
        public val countryName: kotlin.String? = null,
        /** Administrative region or state the POI belongs to (e.g. Haute-Savoie). Null when not available. */
        public val region: kotlin.String? = null,
        /** City the POI belongs to. Null when not available. */
        public val city: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.POI &&
                other.osm == this.osm &&
                other.name == this.name &&
                other.category == this.category &&
                other.point == this.point &&
                other.elevation == this.elevation &&
                other.tags == this.tags &&
                other.distanceMeters == this.distanceMeters &&
                other.country == this.country &&
                other.countryName == this.countryName &&
                other.region == this.region &&
                other.city == this.city

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.osm?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.category?.hashCode() ?: 0)
            hash = 31 * hash + (this.point?.hashCode() ?: 0)
            hash = 31 * hash + (this.elevation?.hashCode() ?: 0)
            hash = 31 * hash + (this.tags?.hashCode() ?: 0)
            hash = 31 * hash + (this.distanceMeters?.hashCode() ?: 0)
            hash = 31 * hash + (this.country?.hashCode() ?: 0)
            hash = 31 * hash + (this.countryName?.hashCode() ?: 0)
            hash = 31 * hash + (this.region?.hashCode() ?: 0)
            hash = 31 * hash + (this.city?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "POI(" +
                "osm=" + this.osm +
                ", name=" + this.name +
                ", category=" + this.category +
                ", point=" + this.point +
                ", elevation=" + this.elevation +
                ", tags=" + this.tags +
                ", distanceMeters=" + this.distanceMeters +
                ", country=" + this.country +
                ", countryName=" + this.countryName +
                ", region=" + this.region +
                ", city=" + this.city +
                ")"

        /** Builder sets the optional properties of a [DataSchema.POI], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: DataSchema.OSMRef? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: DataSchema.POICategory? = null
            internal var slot3: kotlin.collections.List<kotlin.Double>? = null
            internal var slot4: kotlin.Double? = null
            internal var slot5: kotlin.collections.List<DataSchema.OSMTag>? = null
            internal var slot6: kotlin.Double? = null
            internal var slot7: kotlin.String? = null
            internal var slot8: kotlin.String? = null
            internal var slot9: kotlin.String? = null
            internal var slot10: kotlin.String? = null

            /** Reference to the underlying OpenStreetMap feature. */
            public var osm: DataSchema.OSMRef?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Localized display name. */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Canonical POI category mapped from the underlying OSM tags. */
            public var category: DataSchema.POICategory?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Centroid as [longitude, latitude]. */
            public var point: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Elevation in meters above sea level, read from the OSM "ele" tag. Often null:
             * present only where the place data carries that tag.
             */
            public var elevation: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Selected OSM tags useful for UI rendering, present only where the place data
             * carries them. Not exhaustive, and often empty.
             */
            public var tags: kotlin.collections.List<DataSchema.OSMTag>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Distance in meters from the buffered geometry itself, measured to its nearest
             * edge rather than its nearest vertex, and 0 for a POI inside a Polygon (outside
             * its holes). Only set when the query used a buffered area.
             */
            public var distanceMeters: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * ISO 3166-1 alpha-2 country code, e.g. "FR" or "BE". Null when the
             * upstream geocoder did not resolve admin context.
             */
            public var country: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /** Localized display name of the POI's country. Null when not available. */
            public var countryName: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /** Administrative region or state the POI belongs to (e.g. Haute-Savoie). Null when not available. */
            public var region: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }

            /** City the POI belongs to. Null when not available. */
            public var city: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot10 = value
                }
        }

        /** Builds a [DataSchema.POI]. */
        public companion object {
            /** Returns a [DataSchema.POI] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.POI.Builder.() -> kotlin.Unit = {},
            ): DataSchema.POI {
                val given = DataSchema.POI.Builder().apply(optional)
                return DataSchema.POI(
                    osm = given.slot0,
                    name = given.slot1,
                    category = given.slot2,
                    point = given.slot3,
                    elevation = given.slot4,
                    tags = given.slot5,
                    distanceMeters = given.slot6,
                    country = given.slot7,
                    countryName = given.slot8,
                    region = given.slot9,
                    city = given.slot10,
                )
            }
        }
    }

    /**
     * A single geocoded place result. Coordinates are GeoJSON-shaped arrays
     * (longitude before latitude).
     */
    @kotlinx.serialization.Serializable
    public class Place private constructor(
        /** Reference to the underlying OpenStreetMap feature. */
        public val osm: DataSchema.OSMRef? = null,
        /** Localized display name. */
        public val name: kotlin.String? = null,
        /**
         * Single-line label built from the house number and street, the city (or the
         * district when there is no city), the region and the country, in that order,
         * leaving out any the place does not carry.
         */
        public val label: kotlin.String? = null,
        /** Canonical geocoding layer for this place. */
        public val layer: DataSchema.GeocodeLayer? = null,
        /**
         * Canonical category for this place. Drives category-specific UI and is the
         * dimension along which PlaceCategoriesInput filters geocoding output.
         */
        public val category: DataSchema.PlaceCategory? = null,
        /** Centroid as [longitude, latitude]. */
        public val point: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Optional extent as [minLongitude, minLatitude, maxLongitude, maxLatitude].
         * Null when no bounds are known.
         */
        public val bounds: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * ISO 3166-1 alpha-2 country code. Example: "FR".
         * Null when the upstream geocoder did not resolve administrative context for this result.
         */
        public val country: kotlin.String? = null,
        /** Localized country display name. Null when the upstream geocoder did not resolve administrative context for this result. */
        public val countryName: kotlin.String? = null,
        /** Administrative region or state. Null when the upstream geocoder did not resolve administrative context for this result. */
        public val region: kotlin.String? = null,
        /** Administrative county or department. Null when the upstream geocoder did not resolve administrative context for this result. */
        public val county: kotlin.String? = null,
        /** City name. Null when the upstream geocoder did not resolve administrative context for this result. */
        public val city: kotlin.String? = null,
        /**
         * Postal code. Null when the result is coarser than a postcode, or the
         * upstream geocoder did not resolve one.
         */
        public val postcode: kotlin.String? = null,
        /**
         * Street name. Null unless the result resolved to a street or an address
         * on one.
         */
        public val street: kotlin.String? = null,
        /**
         * Building number on the street. Null unless the result resolved to a
         * specific address.
         */
        public val houseNumber: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.Place &&
                other.osm == this.osm &&
                other.name == this.name &&
                other.label == this.label &&
                other.layer == this.layer &&
                other.category == this.category &&
                other.point == this.point &&
                other.bounds == this.bounds &&
                other.country == this.country &&
                other.countryName == this.countryName &&
                other.region == this.region &&
                other.county == this.county &&
                other.city == this.city &&
                other.postcode == this.postcode &&
                other.street == this.street &&
                other.houseNumber == this.houseNumber

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.osm?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.label?.hashCode() ?: 0)
            hash = 31 * hash + (this.layer?.hashCode() ?: 0)
            hash = 31 * hash + (this.category?.hashCode() ?: 0)
            hash = 31 * hash + (this.point?.hashCode() ?: 0)
            hash = 31 * hash + (this.bounds?.hashCode() ?: 0)
            hash = 31 * hash + (this.country?.hashCode() ?: 0)
            hash = 31 * hash + (this.countryName?.hashCode() ?: 0)
            hash = 31 * hash + (this.region?.hashCode() ?: 0)
            hash = 31 * hash + (this.county?.hashCode() ?: 0)
            hash = 31 * hash + (this.city?.hashCode() ?: 0)
            hash = 31 * hash + (this.postcode?.hashCode() ?: 0)
            hash = 31 * hash + (this.street?.hashCode() ?: 0)
            hash = 31 * hash + (this.houseNumber?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "Place(" +
                "osm=" + this.osm +
                ", name=" + this.name +
                ", label=" + this.label +
                ", layer=" + this.layer +
                ", category=" + this.category +
                ", point=" + this.point +
                ", bounds=" + this.bounds +
                ", country=" + this.country +
                ", countryName=" + this.countryName +
                ", region=" + this.region +
                ", county=" + this.county +
                ", city=" + this.city +
                ", postcode=" + this.postcode +
                ", street=" + this.street +
                ", houseNumber=" + this.houseNumber +
                ")"

        /** Builder sets the optional properties of a [DataSchema.Place], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: DataSchema.OSMRef? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: DataSchema.GeocodeLayer? = null
            internal var slot4: DataSchema.PlaceCategory? = null
            internal var slot5: kotlin.collections.List<kotlin.Double>? = null
            internal var slot6: kotlin.collections.List<kotlin.Double>? = null
            internal var slot7: kotlin.String? = null
            internal var slot8: kotlin.String? = null
            internal var slot9: kotlin.String? = null
            internal var slot10: kotlin.String? = null
            internal var slot11: kotlin.String? = null
            internal var slot12: kotlin.String? = null
            internal var slot13: kotlin.String? = null
            internal var slot14: kotlin.String? = null

            /** Reference to the underlying OpenStreetMap feature. */
            public var osm: DataSchema.OSMRef?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Localized display name. */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Single-line label built from the house number and street, the city (or the
             * district when there is no city), the region and the country, in that order,
             * leaving out any the place does not carry.
             */
            public var label: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Canonical geocoding layer for this place. */
            public var layer: DataSchema.GeocodeLayer?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Canonical category for this place. Drives category-specific UI and is the
             * dimension along which PlaceCategoriesInput filters geocoding output.
             */
            public var category: DataSchema.PlaceCategory?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Centroid as [longitude, latitude]. */
            public var point: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Optional extent as [minLongitude, minLatitude, maxLongitude, maxLatitude].
             * Null when no bounds are known.
             */
            public var bounds: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * ISO 3166-1 alpha-2 country code. Example: "FR".
             * Null when the upstream geocoder did not resolve administrative context for this result.
             */
            public var country: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /** Localized country display name. Null when the upstream geocoder did not resolve administrative context for this result. */
            public var countryName: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /** Administrative region or state. Null when the upstream geocoder did not resolve administrative context for this result. */
            public var region: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }

            /** Administrative county or department. Null when the upstream geocoder did not resolve administrative context for this result. */
            public var county: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot10 = value
                }

            /** City name. Null when the upstream geocoder did not resolve administrative context for this result. */
            public var city: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot11 = value
                }

            /**
             * Postal code. Null when the result is coarser than a postcode, or the
             * upstream geocoder did not resolve one.
             */
            public var postcode: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot12 = value
                }

            /**
             * Street name. Null unless the result resolved to a street or an address
             * on one.
             */
            public var street: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot13 = value
                }

            /**
             * Building number on the street. Null unless the result resolved to a
             * specific address.
             */
            public var houseNumber: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot14 = value
                }
        }

        /** Builds a [DataSchema.Place]. */
        public companion object {
            /** Returns a [DataSchema.Place] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.Place.Builder.() -> kotlin.Unit = {},
            ): DataSchema.Place {
                val given = DataSchema.Place.Builder().apply(optional)
                return DataSchema.Place(
                    osm = given.slot0,
                    name = given.slot1,
                    label = given.slot2,
                    layer = given.slot3,
                    category = given.slot4,
                    point = given.slot5,
                    bounds = given.slot6,
                    country = given.slot7,
                    countryName = given.slot8,
                    region = given.slot9,
                    county = given.slot10,
                    city = given.slot11,
                    postcode = given.slot12,
                    street = given.slot13,
                    houseNumber = given.slot14,
                )
            }
        }
    }

    /** Result of a points-of-interest query. */
    @kotlinx.serialization.Serializable
    public class PoisResult private constructor(
        /** Number of POIs returned: the length of `pois`, at most `limit`. */
        public val count: kotlin.Int? = null,
        /**
         * List of POIs ordered by relevance. A buffered area does not reorder them by
         * distance: read each one's `distanceMeters`.
         */
        public val pois: kotlin.collections.List<DataSchema.POI>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.PoisResult &&
                other.count == this.count &&
                other.pois == this.pois

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.pois?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PoisResult(" +
                "count=" + this.count +
                ", pois=" + this.pois +
                ")"

        /** Builder sets the optional properties of a [DataSchema.PoisResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.collections.List<DataSchema.POI>? = null

            /** Number of POIs returned: the length of `pois`, at most `limit`. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * List of POIs ordered by relevance. A buffered area does not reorder them by
             * distance: read each one's `distanceMeters`.
             */
            public var pois: kotlin.collections.List<DataSchema.POI>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.PoisResult]. */
        public companion object {
            /** Returns a [DataSchema.PoisResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.PoisResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.PoisResult {
                val given = DataSchema.PoisResult.Builder().apply(optional)
                return DataSchema.PoisResult(
                    count = given.slot0,
                    pois = given.slot1,
                )
            }
        }
    }

    /** Result of a reverse geocoding query. */
    @kotlinx.serialization.Serializable
    public class ReverseGeocodeResult private constructor(
        /** Echo of the anchor [longitude, latitude] point used for the lookup. */
        public val point: kotlin.collections.List<kotlin.Double>? = null,
        /** List of named places, in the order the geocoder returns them for the point. */
        public val places: kotlin.collections.List<DataSchema.Place>? = null,
        /** Number of places returned: the length of `places`, at most `limit`. */
        public val count: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ReverseGeocodeResult &&
                other.point == this.point &&
                other.places == this.places &&
                other.count == this.count

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.point?.hashCode() ?: 0)
            hash = 31 * hash + (this.places?.hashCode() ?: 0)
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ReverseGeocodeResult(" +
                "point=" + this.point +
                ", places=" + this.places +
                ", count=" + this.count +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ReverseGeocodeResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<kotlin.Double>? = null
            internal var slot1: kotlin.collections.List<DataSchema.Place>? = null
            internal var slot2: kotlin.Int? = null

            /** Echo of the anchor [longitude, latitude] point used for the lookup. */
            public var point: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** List of named places, in the order the geocoder returns them for the point. */
            public var places: kotlin.collections.List<DataSchema.Place>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Number of places returned: the length of `places`, at most `limit`. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.ReverseGeocodeResult]. */
        public companion object {
            /** Returns a [DataSchema.ReverseGeocodeResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ReverseGeocodeResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ReverseGeocodeResult {
                val given = DataSchema.ReverseGeocodeResult.Builder().apply(optional)
                return DataSchema.ReverseGeocodeResult(
                    point = given.slot0,
                    places = given.slot1,
                    count = given.slot2,
                )
            }
        }
    }

    /** A coordinate index range with the access restrictions that apply to it. */
    @kotlinx.serialization.Serializable
    public class RoadAccessRestrictionInfo private constructor(
        /** Start index in the coordinates array (inclusive). */
        public val fromIndex: kotlin.Int? = null,
        /** End index in the coordinates array (inclusive). */
        public val toIndex: kotlin.Int? = null,
        /**
         * Every access restriction tagged on this coordinate range, for the activity's
         * mode of travel. A way can carry several at once. Never empty: a range with no
         * restriction is not reported at all.
         */
        public val values: kotlin.collections.List<DataSchema.RoadAccessRestrictionType>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.RoadAccessRestrictionInfo &&
                other.fromIndex == this.fromIndex &&
                other.toIndex == this.toIndex &&
                other.values == this.values

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.fromIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.toIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.values?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "RoadAccessRestrictionInfo(" +
                "fromIndex=" + this.fromIndex +
                ", toIndex=" + this.toIndex +
                ", values=" + this.values +
                ")"

        /** Builder sets the optional properties of a [DataSchema.RoadAccessRestrictionInfo], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: kotlin.collections.List<DataSchema.RoadAccessRestrictionType>? = null

            /** Start index in the coordinates array (inclusive). */
            public var fromIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** End index in the coordinates array (inclusive). */
            public var toIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Every access restriction tagged on this coordinate range, for the activity's
             * mode of travel. A way can carry several at once. Never empty: a range with no
             * restriction is not reported at all.
             */
            public var values: kotlin.collections.List<DataSchema.RoadAccessRestrictionType>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.RoadAccessRestrictionInfo]. */
        public companion object {
            /** Returns a [DataSchema.RoadAccessRestrictionInfo] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.RoadAccessRestrictionInfo.Builder.() -> kotlin.Unit = {},
            ): DataSchema.RoadAccessRestrictionInfo {
                val given = DataSchema.RoadAccessRestrictionInfo.Builder().apply(optional)
                return DataSchema.RoadAccessRestrictionInfo(
                    fromIndex = given.slot0,
                    toIndex = given.slot1,
                    values = given.slot2,
                )
            }
        }
    }

    /**
     * Route-level hazards a client should surface before committing a computed route.
     *
     * Each flag is decided by the matching info list when the routing engine returns
     * per-stretch data for the route, and by the warning the engine attaches to the
     * route when it does not. A flag can therefore be true while its info list is
     * empty. On `directions` and `suggestions`, a SKI_TOURING, CROSS_COUNTRY_SKIING or
     * SNOWSHOE_WALKING route never carries per-stretch data, so its flags always come
     * from the warning.
     */
    @kotlinx.serialization.Serializable
    public class RouteAdvisories private constructor(
        /**
         * True when the route crosses at least one way that charges a toll for the
         * activity's mode of travel.
         */
        public val tollway: kotlin.Boolean? = null,
        /**
         * True when the route crosses at least one way carrying a genuine access
         * restriction. PERMISSIVE grants access rather than restricting it, and
         * AGRICULTURAL and FORESTRY are ordinary terrain on foot, so none of the three
         * raises this flag where the routing engine returns per-stretch access data.
         * Where it returns none, the flag follows the engine's route-level warning,
         * which any access tag raises, those three included.
         */
        public val restrictedAccess: kotlin.Boolean? = null,
        /**
         * The stretches that cross a tollway, referencing coordinate indices, and only
         * those. Empty when there are none, or when the engine returned no per-stretch
         * toll data (read `tollway` then).
         */
        public val tollwayInfo: kotlin.collections.List<DataSchema.TollwayInfo>? = null,
        /**
         * The stretches that carry an access tag, referencing coordinate indices, and
         * only those. PERMISSIVE, AGRICULTURAL and FORESTRY stretches are listed too, as
         * information. Empty when there are none, or when the engine returned no
         * per-stretch access data (read `restrictedAccess` then).
         */
        public val roadAccessInfo: kotlin.collections.List<DataSchema.RoadAccessRestrictionInfo>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.RouteAdvisories &&
                other.tollway == this.tollway &&
                other.restrictedAccess == this.restrictedAccess &&
                other.tollwayInfo == this.tollwayInfo &&
                other.roadAccessInfo == this.roadAccessInfo

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.tollway?.hashCode() ?: 0)
            hash = 31 * hash + (this.restrictedAccess?.hashCode() ?: 0)
            hash = 31 * hash + (this.tollwayInfo?.hashCode() ?: 0)
            hash = 31 * hash + (this.roadAccessInfo?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "RouteAdvisories(" +
                "tollway=" + this.tollway +
                ", restrictedAccess=" + this.restrictedAccess +
                ", tollwayInfo=" + this.tollwayInfo +
                ", roadAccessInfo=" + this.roadAccessInfo +
                ")"

        /** Builder sets the optional properties of a [DataSchema.RouteAdvisories], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Boolean? = null
            internal var slot1: kotlin.Boolean? = null
            internal var slot2: kotlin.collections.List<DataSchema.TollwayInfo>? = null
            internal var slot3: kotlin.collections.List<DataSchema.RoadAccessRestrictionInfo>? = null

            /**
             * True when the route crosses at least one way that charges a toll for the
             * activity's mode of travel.
             */
            public var tollway: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * True when the route crosses at least one way carrying a genuine access
             * restriction. PERMISSIVE grants access rather than restricting it, and
             * AGRICULTURAL and FORESTRY are ordinary terrain on foot, so none of the three
             * raises this flag where the routing engine returns per-stretch access data.
             * Where it returns none, the flag follows the engine's route-level warning,
             * which any access tag raises, those three included.
             */
            public var restrictedAccess: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * The stretches that cross a tollway, referencing coordinate indices, and only
             * those. Empty when there are none, or when the engine returned no per-stretch
             * toll data (read `tollway` then).
             */
            public var tollwayInfo: kotlin.collections.List<DataSchema.TollwayInfo>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * The stretches that carry an access tag, referencing coordinate indices, and
             * only those. PERMISSIVE, AGRICULTURAL and FORESTRY stretches are listed too, as
             * information. Empty when there are none, or when the engine returned no
             * per-stretch access data (read `restrictedAccess` then).
             */
            public var roadAccessInfo: kotlin.collections.List<DataSchema.RoadAccessRestrictionInfo>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [DataSchema.RouteAdvisories]. */
        public companion object {
            /** Returns a [DataSchema.RouteAdvisories] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.RouteAdvisories.Builder.() -> kotlin.Unit = {},
            ): DataSchema.RouteAdvisories {
                val given = DataSchema.RouteAdvisories.Builder().apply(optional)
                return DataSchema.RouteAdvisories(
                    tollway = given.slot0,
                    restrictedAccess = given.slot1,
                    tollwayInfo = given.slot2,
                    roadAccessInfo = given.slot3,
                )
            }
        }
    }

    /**
     * Result of the unified search query. Each target's section is non-null only
     * when that target was enabled and its branch succeeded; failed branches leave
     * their section null and surface an entry in errors[].
     *
     * That includes `query` and `limit`, which each target checks against its own
     * rules, so a value one target refuses surfaces as its VALIDATION_FAILED entry
     * instead of failing the request. A request whose every enabled target failed is
     * answered no result and one error rather than a result holding nothing but its
     * `errors`: where every target refused the request, the refusal of the first
     * target in the order the sections are listed (VALIDATION_FAILED at the field it
     * names, or its own code), and INTERNAL_ERROR as soon as a target failed on
     * Mountaya's side. Such a request counts toward neither the monthly allowance nor
     * an invoice. A feature two targets found is returned in one
     * section only, as `search` states, so a section can hold fewer entries than its
     * target alone would return.
     *
     * The one exception to a failed branch leaving its section null is the ways
     * target asked to verify (`verifyWays`) whose verification failed on Mountaya's
     * side for some way: it keeps its section, each activity it could not check kept
     * as `verifyWays` keeps an unchecked one, and surfaces an INTERNAL_ERROR entry in
     * errors[] beside it. A request one of whose targets failed on Mountaya's side,
     * or answered only in part because of a failure on Mountaya's side, keeps every
     * section that answered and is recorded as a failure (5xx), so it counts toward
     * neither the monthly allowance nor an invoice.
     */
    @kotlinx.serialization.Serializable
    public class SearchResult private constructor(
        /** Echo of the input query. */
        public val query: kotlin.String? = null,
        /**
         * Language the place, POI and way names were returned in: the requested language
         * when it is EN or FR, and EN otherwise, including when none was given.
         */
        public val language: DataSchema.Language? = null,
        /** Echo of the per-target enable flags carried by the input. */
        public val targets: DataSchema.SearchTargets? = null,
        /**
         * Geocoded places section. Null when the places target was disabled or its
         * branch errored.
         */
        public val places: DataSchema.GeocodeResult? = null,
        /**
         * Points-of-interest section. Null when the pois target was disabled or its
         * branch errored.
         */
        public val pois: DataSchema.PoisResult? = null,
        /**
         * Named-ways section. Null when the ways target was disabled or its branch
         * errored.
         */
        public val ways: DataSchema.WaysResult? = null,
        /**
         * Itineraries section. Null when the itineraries target was disabled or its
         * branch errored.
         */
        public val itineraries: DataSchema.ItinerariesResult? = null,
        /**
         * Collections section. Null when the collections target was disabled or its
         * branch errored.
         */
        public val collections: DataSchema.CollectionsResult? = null,
        /**
         * Zones section. Null when the zones target was disabled or its branch
         * errored.
         */
        public val zones: DataSchema.ZonesResult? = null,
        /**
         * Graceful-degradation entries: one per target whose branch failed, or answered
         * only in part because of a failure on Mountaya's side. Empty when every enabled
         * branch succeeded.
         */
        public val errors: kotlin.collections.List<DataSchema.SearchTargetError>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SearchResult &&
                other.query == this.query &&
                other.language == this.language &&
                other.targets == this.targets &&
                other.places == this.places &&
                other.pois == this.pois &&
                other.ways == this.ways &&
                other.itineraries == this.itineraries &&
                other.collections == this.collections &&
                other.zones == this.zones &&
                other.errors == this.errors

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.query?.hashCode() ?: 0)
            hash = 31 * hash + (this.language?.hashCode() ?: 0)
            hash = 31 * hash + (this.targets?.hashCode() ?: 0)
            hash = 31 * hash + (this.places?.hashCode() ?: 0)
            hash = 31 * hash + (this.pois?.hashCode() ?: 0)
            hash = 31 * hash + (this.ways?.hashCode() ?: 0)
            hash = 31 * hash + (this.itineraries?.hashCode() ?: 0)
            hash = 31 * hash + (this.collections?.hashCode() ?: 0)
            hash = 31 * hash + (this.zones?.hashCode() ?: 0)
            hash = 31 * hash + (this.errors?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SearchResult(" +
                "query=" + this.query +
                ", language=" + this.language +
                ", targets=" + this.targets +
                ", places=" + this.places +
                ", pois=" + this.pois +
                ", ways=" + this.ways +
                ", itineraries=" + this.itineraries +
                ", collections=" + this.collections +
                ", zones=" + this.zones +
                ", errors=" + this.errors +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SearchResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: DataSchema.Language? = null
            internal var slot2: DataSchema.SearchTargets? = null
            internal var slot3: DataSchema.GeocodeResult? = null
            internal var slot4: DataSchema.PoisResult? = null
            internal var slot5: DataSchema.WaysResult? = null
            internal var slot6: DataSchema.ItinerariesResult? = null
            internal var slot7: DataSchema.CollectionsResult? = null
            internal var slot8: DataSchema.ZonesResult? = null
            internal var slot9: kotlin.collections.List<DataSchema.SearchTargetError>? = null

            /** Echo of the input query. */
            public var query: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Language the place, POI and way names were returned in: the requested language
             * when it is EN or FR, and EN otherwise, including when none was given.
             */
            public var language: DataSchema.Language?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Echo of the per-target enable flags carried by the input. */
            public var targets: DataSchema.SearchTargets?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Geocoded places section. Null when the places target was disabled or its
             * branch errored.
             */
            public var places: DataSchema.GeocodeResult?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Points-of-interest section. Null when the pois target was disabled or its
             * branch errored.
             */
            public var pois: DataSchema.PoisResult?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Named-ways section. Null when the ways target was disabled or its branch
             * errored.
             */
            public var ways: DataSchema.WaysResult?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Itineraries section. Null when the itineraries target was disabled or its
             * branch errored.
             */
            public var itineraries: DataSchema.ItinerariesResult?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Collections section. Null when the collections target was disabled or its
             * branch errored.
             */
            public var collections: DataSchema.CollectionsResult?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /**
             * Zones section. Null when the zones target was disabled or its branch
             * errored.
             */
            public var zones: DataSchema.ZonesResult?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /**
             * Graceful-degradation entries: one per target whose branch failed, or answered
             * only in part because of a failure on Mountaya's side. Empty when every enabled
             * branch succeeded.
             */
            public var errors: kotlin.collections.List<DataSchema.SearchTargetError>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }
        }

        /** Builds a [DataSchema.SearchResult]. */
        public companion object {
            /** Returns a [DataSchema.SearchResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SearchResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SearchResult {
                val given = DataSchema.SearchResult.Builder().apply(optional)
                return DataSchema.SearchResult(
                    query = given.slot0,
                    language = given.slot1,
                    targets = given.slot2,
                    places = given.slot3,
                    pois = given.slot4,
                    ways = given.slot5,
                    itineraries = given.slot6,
                    collections = given.slot7,
                    zones = given.slot8,
                    errors = given.slot9,
                )
            }
        }
    }

    /**
     * Single graceful-degradation entry. Emitted in SearchResult.errors when a
     * per-target branch fails.
     */
    @kotlinx.serialization.Serializable
    public class SearchTargetError private constructor(
        /** Identifier of the target whose branch failed. */
        public val target: DataSchema.SearchTarget? = null,
        /**
         * Why this target's branch failed. The same `extensions.code` that querying this
         * target on its own would have emitted on the spec `errors[]` array.
         */
        public val code: DataSchema.ErrorCode? = null,
        /** Message describing the failure, localized by the Accept-Language header. */
        public val message: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SearchTargetError &&
                other.target == this.target &&
                other.code == this.code &&
                other.message == this.message

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.target?.hashCode() ?: 0)
            hash = 31 * hash + (this.code?.hashCode() ?: 0)
            hash = 31 * hash + (this.message?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SearchTargetError(" +
                "target=" + this.target +
                ", code=" + this.code +
                ", message=" + this.message +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SearchTargetError], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: DataSchema.SearchTarget? = null
            internal var slot1: DataSchema.ErrorCode? = null
            internal var slot2: kotlin.String? = null

            /** Identifier of the target whose branch failed. */
            public var target: DataSchema.SearchTarget?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Why this target's branch failed. The same `extensions.code` that querying this
             * target on its own would have emitted on the spec `errors[]` array.
             */
            public var code: DataSchema.ErrorCode?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Message describing the failure, localized by the Accept-Language header. */
            public var message: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.SearchTargetError]. */
        public companion object {
            /** Returns a [DataSchema.SearchTargetError] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SearchTargetError.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SearchTargetError {
                val given = DataSchema.SearchTargetError.Builder().apply(optional)
                return DataSchema.SearchTargetError(
                    target = given.slot0,
                    code = given.slot1,
                    message = given.slot2,
                )
            }
        }
    }

    /**
     * Echo of the per-target enable flags. Mirrors SearchTargetsInput on the
     * output side so clients can drive UI from the result without re-reading the
     * request.
     */
    @kotlinx.serialization.Serializable
    public class SearchTargets private constructor(
        /** Whether the places target was enabled. */
        public val places: kotlin.Boolean? = null,
        /** Whether the pois target was enabled. */
        public val pois: kotlin.Boolean? = null,
        /** Whether the ways target was enabled. */
        public val ways: kotlin.Boolean? = null,
        /** Whether the itineraries target was enabled. */
        public val itineraries: kotlin.Boolean? = null,
        /** Whether the collections target was enabled. */
        public val collections: kotlin.Boolean? = null,
        /** Whether the zones target was enabled. */
        public val zones: kotlin.Boolean? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SearchTargets &&
                other.places == this.places &&
                other.pois == this.pois &&
                other.ways == this.ways &&
                other.itineraries == this.itineraries &&
                other.collections == this.collections &&
                other.zones == this.zones

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.places?.hashCode() ?: 0)
            hash = 31 * hash + (this.pois?.hashCode() ?: 0)
            hash = 31 * hash + (this.ways?.hashCode() ?: 0)
            hash = 31 * hash + (this.itineraries?.hashCode() ?: 0)
            hash = 31 * hash + (this.collections?.hashCode() ?: 0)
            hash = 31 * hash + (this.zones?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SearchTargets(" +
                "places=" + this.places +
                ", pois=" + this.pois +
                ", ways=" + this.ways +
                ", itineraries=" + this.itineraries +
                ", collections=" + this.collections +
                ", zones=" + this.zones +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SearchTargets], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Boolean? = null
            internal var slot1: kotlin.Boolean? = null
            internal var slot2: kotlin.Boolean? = null
            internal var slot3: kotlin.Boolean? = null
            internal var slot4: kotlin.Boolean? = null
            internal var slot5: kotlin.Boolean? = null

            /** Whether the places target was enabled. */
            public var places: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Whether the pois target was enabled. */
            public var pois: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Whether the ways target was enabled. */
            public var ways: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Whether the itineraries target was enabled. */
            public var itineraries: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** Whether the collections target was enabled. */
            public var collections: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Whether the zones target was enabled. */
            public var zones: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [DataSchema.SearchTargets]. */
        public companion object {
            /** Returns a [DataSchema.SearchTargets] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SearchTargets.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SearchTargets {
                val given = DataSchema.SearchTargets.Builder().apply(optional)
                return DataSchema.SearchTargets(
                    places = given.slot0,
                    pois = given.slot1,
                    ways = given.slot2,
                    itineraries = given.slot3,
                    collections = given.slot4,
                    zones = given.slot5,
                )
            }
        }
    }

    /** A coordinate index range with an associated slope angle in degrees. */
    @kotlinx.serialization.Serializable
    public class SlopeInfo private constructor(
        /** Start index in the coordinates array (inclusive). */
        public val fromIndex: kotlin.Int? = null,
        /** End index in the coordinates array (inclusive). */
        public val toIndex: kotlin.Int? = null,
        /** Slope angle in degrees. Positive values indicate uphill, negative values indicate downhill. */
        public val value: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SlopeInfo &&
                other.fromIndex == this.fromIndex &&
                other.toIndex == this.toIndex &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.fromIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.toIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SlopeInfo(" +
                "fromIndex=" + this.fromIndex +
                ", toIndex=" + this.toIndex +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SlopeInfo], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: kotlin.Double? = null

            /** Start index in the coordinates array (inclusive). */
            public var fromIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** End index in the coordinates array (inclusive). */
            public var toIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Slope angle in degrees. Positive values indicate uphill, negative values indicate downhill. */
            public var value: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.SlopeInfo]. */
        public companion object {
            /** Returns a [DataSchema.SlopeInfo] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SlopeInfo.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SlopeInfo {
                val given = DataSchema.SlopeInfo.Builder().apply(optional)
                return DataSchema.SlopeInfo(
                    fromIndex = given.slot0,
                    toIndex = given.slot1,
                    value = given.slot2,
                )
            }
        }
    }

    /**
     * A coordinate index range with an associated travel speed in km/h.
     *
     * This is the RESOLVED speed the travel-time model computed for that stretch from
     * the flat pace and the ground it crosses, not the flat pace itself.
     */
    @kotlinx.serialization.Serializable
    public class SpeedInfo private constructor(
        /** Start index in the coordinates array (inclusive). */
        public val fromIndex: kotlin.Int? = null,
        /** End index in the coordinates array (inclusive). */
        public val toIndex: kotlin.Int? = null,
        /**
         * Travel speed in km/h over this stretch. Always positive. Slower than the flat
         * pace on a climb, faster on a descent, and reduced further by a coarse surface
         * or a technical trail. Never above the activity's maximum speed, in km/h: 12 for
         * HIKING_AND_TRAIL, 20 for PROFESSIONAL_TRAIL, 22 for RUNNING, 60 for
         * ROAD_CYCLING, 40 for BACKCOUNTRY_SKIING and SKI_TOURING, 30 for
         * CROSS_COUNTRY_SKIING and 8 for SNOWSHOE_WALKING. A stretch at that cap stays
         * there whatever `speed` asks for.
         */
        public val value: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SpeedInfo &&
                other.fromIndex == this.fromIndex &&
                other.toIndex == this.toIndex &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.fromIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.toIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SpeedInfo(" +
                "fromIndex=" + this.fromIndex +
                ", toIndex=" + this.toIndex +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SpeedInfo], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: kotlin.Double? = null

            /** Start index in the coordinates array (inclusive). */
            public var fromIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** End index in the coordinates array (inclusive). */
            public var toIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Travel speed in km/h over this stretch. Always positive. Slower than the flat
             * pace on a climb, faster on a descent, and reduced further by a coarse surface
             * or a technical trail. Never above the activity's maximum speed, in km/h: 12 for
             * HIKING_AND_TRAIL, 20 for PROFESSIONAL_TRAIL, 22 for RUNNING, 60 for
             * ROAD_CYCLING, 40 for BACKCOUNTRY_SKIING and SKI_TOURING, 30 for
             * CROSS_COUNTRY_SKIING and 8 for SNOWSHOE_WALKING. A stretch at that cap stays
             * there whatever `speed` asks for.
             */
            public var value: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.SpeedInfo]. */
        public companion object {
            /** Returns a [DataSchema.SpeedInfo] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SpeedInfo.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SpeedInfo {
                val given = DataSchema.SpeedInfo.Builder().apply(optional)
                return DataSchema.SpeedInfo(
                    fromIndex = given.slot0,
                    toIndex = given.slot1,
                    value = given.slot2,
                )
            }
        }
    }

    /**
     * A single suggested route candidate with distance, duration, elevation,
     * geometry, and analysis.
     */
    @kotlinx.serialization.Serializable
    public class SuggestionRoute private constructor(
        /**
         * Total route distance in meters, measured as the planimetric great-circle
         * length of the geometry field, with elevation ignored. This is measured from
         * the candidate, so it may differ from the requested target distance.
         */
        public val distance: kotlin.Double? = null,
        /**
         * Estimated route duration in seconds, computed over the terrain the candidate
         * crosses rather than over its distance, anchored on the requested `speed` (or
         * the activity default) as the pace on level ground. Integrating
         * `analysis.speedInfo` over the geometry reproduces this number exactly.
         */
        public val duration: kotlin.Double? = null,
        /**
         * Elevation summary including altitude bounds, total gain/loss, and gradient
         * extremes.
         */
        public val elevation: DataSchema.ElevationSummary? = null,
        /** GeoJSON geometry representing the route path. */
        public val geometry: com.mountaya.client.GeoJsonGeometry? = null,
        /**
         * Surface and way type analysis for the route geometry. Its `advisories` raise
         * no flag, because a candidate that crosses a tollway or an access-restricted
         * way is never returned.
         */
        public val analysis: DataSchema.GeometryAnalysis? = null,
        /**
         * Human-readable route summary.
         * Example: "20.1 km, ~5h30m, +850m / -850m elevation, 45% path, 38% track."
         */
        public val summary: kotlin.String? = null,
        /** Turn-by-turn direction steps along this candidate, in travel order. */
        public val steps: kotlin.collections.List<DataSchema.DirectionsStep>? = null,
        /**
         * Seed value used to generate this route candidate. Pass this seed back in
         * SuggestionsInput.seeds.include to pin this route into a future result, or
         * in .exclude to filter it out of the fresh candidate pool. Reproducing a
         * route also requires passing the same start/end/distance/points as the
         * original request. Mismatched inputs will produce a different route even
         * with the same seed.
         */
        public val seed: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SuggestionRoute &&
                other.distance == this.distance &&
                other.duration == this.duration &&
                other.elevation == this.elevation &&
                other.geometry == this.geometry &&
                other.analysis == this.analysis &&
                other.summary == this.summary &&
                other.steps == this.steps &&
                other.seed == this.seed

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.distance?.hashCode() ?: 0)
            hash = 31 * hash + (this.duration?.hashCode() ?: 0)
            hash = 31 * hash + (this.elevation?.hashCode() ?: 0)
            hash = 31 * hash + (this.geometry?.hashCode() ?: 0)
            hash = 31 * hash + (this.analysis?.hashCode() ?: 0)
            hash = 31 * hash + (this.summary?.hashCode() ?: 0)
            hash = 31 * hash + (this.steps?.hashCode() ?: 0)
            hash = 31 * hash + (this.seed?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SuggestionRoute(" +
                "distance=" + this.distance +
                ", duration=" + this.duration +
                ", elevation=" + this.elevation +
                ", geometry=" + this.geometry +
                ", analysis=" + this.analysis +
                ", summary=" + this.summary +
                ", steps=" + this.steps +
                ", seed=" + this.seed +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SuggestionRoute], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Double? = null
            internal var slot1: kotlin.Double? = null
            internal var slot2: DataSchema.ElevationSummary? = null
            internal var slot3: com.mountaya.client.GeoJsonGeometry? = null
            internal var slot4: DataSchema.GeometryAnalysis? = null
            internal var slot5: kotlin.String? = null
            internal var slot6: kotlin.collections.List<DataSchema.DirectionsStep>? = null
            internal var slot7: kotlin.Int? = null

            /**
             * Total route distance in meters, measured as the planimetric great-circle
             * length of the geometry field, with elevation ignored. This is measured from
             * the candidate, so it may differ from the requested target distance.
             */
            public var distance: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Estimated route duration in seconds, computed over the terrain the candidate
             * crosses rather than over its distance, anchored on the requested `speed` (or
             * the activity default) as the pace on level ground. Integrating
             * `analysis.speedInfo` over the geometry reproduces this number exactly.
             */
            public var duration: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Elevation summary including altitude bounds, total gain/loss, and gradient
             * extremes.
             */
            public var elevation: DataSchema.ElevationSummary?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** GeoJSON geometry representing the route path. */
            public var geometry: com.mountaya.client.GeoJsonGeometry?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Surface and way type analysis for the route geometry. Its `advisories` raise
             * no flag, because a candidate that crosses a tollway or an access-restricted
             * way is never returned.
             */
            public var analysis: DataSchema.GeometryAnalysis?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Human-readable route summary.
             * Example: "20.1 km, ~5h30m, +850m / -850m elevation, 45% path, 38% track."
             */
            public var summary: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /** Turn-by-turn direction steps along this candidate, in travel order. */
            public var steps: kotlin.collections.List<DataSchema.DirectionsStep>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Seed value used to generate this route candidate. Pass this seed back in
             * SuggestionsInput.seeds.include to pin this route into a future result, or
             * in .exclude to filter it out of the fresh candidate pool. Reproducing a
             * route also requires passing the same start/end/distance/points as the
             * original request. Mismatched inputs will produce a different route even
             * with the same seed.
             */
            public var seed: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }
        }

        /** Builds a [DataSchema.SuggestionRoute]. */
        public companion object {
            /** Returns a [DataSchema.SuggestionRoute] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SuggestionRoute.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SuggestionRoute {
                val given = DataSchema.SuggestionRoute.Builder().apply(optional)
                return DataSchema.SuggestionRoute(
                    distance = given.slot0,
                    duration = given.slot1,
                    elevation = given.slot2,
                    geometry = given.slot3,
                    analysis = given.slot4,
                    summary = given.slot5,
                    steps = given.slot6,
                    seed = given.slot7,
                )
            }
        }
    }

    /** Result of a suggestions query, containing multiple route candidates. */
    @kotlinx.serialization.Serializable
    public class SuggestionsResult private constructor(
        /**
         * Suggested route candidates: pinned seeds first, in the order given, then
         * fresh candidates nearest the target distance first, or nearest the distance
         * and ascent targets combined when `ascent` is positive. Can hold fewer routes
         * than `candidates`.
         *
         * A candidate crossing a hazard is dropped. For SKI_TOURING,
         * CROSS_COUNTRY_SKIING and SNOWSHOE_WALKING the hazard check reads the routing
         * engine's route-level warning, as there is no per-stretch data for those three
         * (see RouteAdvisories). That warning is raised by any access tag, so a
         * candidate crossing a PERMISSIVE, AGRICULTURAL or FORESTRY way is dropped as
         * well.
         *
         * When fewer candidates than `candidates` survive because one failed on
         * Mountaya's side, or a seed `seeds.include` names failed there (whether asked
         * for once more or not, as `start` states), the field keeps the candidates that
         * survived and carries an INTERNAL_ERROR entry in `errors` beside them, and the
         * request is recorded as a failure (5xx), so it counts toward neither the
         * monthly allowance nor an invoice. An answer holding every candidate asked for
         * is served whichever extra candidate failed, and a candidate the routing
         * engine found no routable point or no route for is no such failure.
         */
        public val routes: kotlin.collections.List<DataSchema.SuggestionRoute>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SuggestionsResult &&
                other.routes == this.routes

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.routes?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SuggestionsResult(" +
                "routes=" + this.routes +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SuggestionsResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<DataSchema.SuggestionRoute>? = null

            /**
             * Suggested route candidates: pinned seeds first, in the order given, then
             * fresh candidates nearest the target distance first, or nearest the distance
             * and ascent targets combined when `ascent` is positive. Can hold fewer routes
             * than `candidates`.
             *
             * A candidate crossing a hazard is dropped. For SKI_TOURING,
             * CROSS_COUNTRY_SKIING and SNOWSHOE_WALKING the hazard check reads the routing
             * engine's route-level warning, as there is no per-stretch data for those three
             * (see RouteAdvisories). That warning is raised by any access tag, so a
             * candidate crossing a PERMISSIVE, AGRICULTURAL or FORESTRY way is dropped as
             * well.
             *
             * When fewer candidates than `candidates` survive because one failed on
             * Mountaya's side, or a seed `seeds.include` names failed there (whether asked
             * for once more or not, as `start` states), the field keeps the candidates that
             * survived and carries an INTERNAL_ERROR entry in `errors` beside them, and the
             * request is recorded as a failure (5xx), so it counts toward neither the
             * monthly allowance nor an invoice. An answer holding every candidate asked for
             * is served whichever extra candidate failed, and a candidate the routing
             * engine found no routable point or no route for is no such failure.
             */
            public var routes: kotlin.collections.List<DataSchema.SuggestionRoute>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [DataSchema.SuggestionsResult]. */
        public companion object {
            /** Returns a [DataSchema.SuggestionsResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SuggestionsResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SuggestionsResult {
                val given = DataSchema.SuggestionsResult.Builder().apply(optional)
                return DataSchema.SuggestionsResult(
                    routes = given.slot0,
                )
            }
        }
    }

    /** A coordinate index range with an associated surface type. */
    @kotlinx.serialization.Serializable
    public class SurfaceInfo private constructor(
        /** Start index in the coordinates array (inclusive). */
        public val fromIndex: kotlin.Int? = null,
        /** End index in the coordinates array (inclusive). */
        public val toIndex: kotlin.Int? = null,
        /** Surface type for this coordinate range. */
        public val value: DataSchema.SurfaceType? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SurfaceInfo &&
                other.fromIndex == this.fromIndex &&
                other.toIndex == this.toIndex &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.fromIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.toIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SurfaceInfo(" +
                "fromIndex=" + this.fromIndex +
                ", toIndex=" + this.toIndex +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SurfaceInfo], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: DataSchema.SurfaceType? = null

            /** Start index in the coordinates array (inclusive). */
            public var fromIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** End index in the coordinates array (inclusive). */
            public var toIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Surface type for this coordinate range. */
            public var value: DataSchema.SurfaceType?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.SurfaceInfo]. */
        public companion object {
            /** Returns a [DataSchema.SurfaceInfo] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SurfaceInfo.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SurfaceInfo {
                val given = DataSchema.SurfaceInfo.Builder().apply(optional)
                return DataSchema.SurfaceInfo(
                    fromIndex = given.slot0,
                    toIndex = given.slot1,
                    value = given.slot2,
                )
            }
        }
    }

    /** Result of a gridded terrain query over an area. */
    @kotlinx.serialization.Serializable
    public class TerrainGridResult private constructor(
        /** Total number of cells returned (cols * rows). */
        public val count: kotlin.Int? = null,
        /**
         * Number of cells that fall inside DEM coverage. Weight any average by this
         * rather than by count.
         */
        public val coveredCount: kotlin.Int? = null,
        /**
         * Zoom level of the sampled DEM tiles, so a result can be reproduced exactly.
         * A standard Web Mercator XYZ zoom, chosen by the server rather than the
         * caller: every request samples at the same zoom, the deepest the elevation
         * source serves, so two answers about the same ground are always comparable.
         * Treat it as reported, not requested.
         */
        public val zoom: kotlin.Int? = null,
        /**
         * Ground resolution of one DEM pixel in meters, measured at the first sample's
         * latitude. It varies with latitude, so over a result spanning a wide range of
         * latitudes it describes the first sample's ground rather than every sample's.
         */
        public val resolutionMeters: kotlin.Double? = null,
        /** Number of grid columns. */
        public val cols: kotlin.Int? = null,
        /** Number of grid rows. */
        public val rows: kotlin.Int? = null,
        /**
         * Grid cells in row-major order: the cell at (col, row) is at index row * cols + col.
         * Row 0 runs along the northern edge of the box and column 0 along its western
         * edge, and each cell is read at its centre rather than at a corner.
         */
        public val cells: kotlin.collections.List<DataSchema.TerrainSample>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TerrainGridResult &&
                other.count == this.count &&
                other.coveredCount == this.coveredCount &&
                other.zoom == this.zoom &&
                other.resolutionMeters == this.resolutionMeters &&
                other.cols == this.cols &&
                other.rows == this.rows &&
                other.cells == this.cells

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.coveredCount?.hashCode() ?: 0)
            hash = 31 * hash + (this.zoom?.hashCode() ?: 0)
            hash = 31 * hash + (this.resolutionMeters?.hashCode() ?: 0)
            hash = 31 * hash + (this.cols?.hashCode() ?: 0)
            hash = 31 * hash + (this.rows?.hashCode() ?: 0)
            hash = 31 * hash + (this.cells?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainGridResult(" +
                "count=" + this.count +
                ", coveredCount=" + this.coveredCount +
                ", zoom=" + this.zoom +
                ", resolutionMeters=" + this.resolutionMeters +
                ", cols=" + this.cols +
                ", rows=" + this.rows +
                ", cells=" + this.cells +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TerrainGridResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: kotlin.Int? = null
            internal var slot3: kotlin.Double? = null
            internal var slot4: kotlin.Int? = null
            internal var slot5: kotlin.Int? = null
            internal var slot6: kotlin.collections.List<DataSchema.TerrainSample>? = null

            /** Total number of cells returned (cols * rows). */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Number of cells that fall inside DEM coverage. Weight any average by this
             * rather than by count.
             */
            public var coveredCount: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Zoom level of the sampled DEM tiles, so a result can be reproduced exactly.
             * A standard Web Mercator XYZ zoom, chosen by the server rather than the
             * caller: every request samples at the same zoom, the deepest the elevation
             * source serves, so two answers about the same ground are always comparable.
             * Treat it as reported, not requested.
             */
            public var zoom: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Ground resolution of one DEM pixel in meters, measured at the first sample's
             * latitude. It varies with latitude, so over a result spanning a wide range of
             * latitudes it describes the first sample's ground rather than every sample's.
             */
            public var resolutionMeters: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** Number of grid columns. */
            public var cols: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Number of grid rows. */
            public var rows: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Grid cells in row-major order: the cell at (col, row) is at index row * cols + col.
             * Row 0 runs along the northern edge of the box and column 0 along its western
             * edge, and each cell is read at its centre rather than at a corner.
             */
            public var cells: kotlin.collections.List<DataSchema.TerrainSample>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }
        }

        /** Builds a [DataSchema.TerrainGridResult]. */
        public companion object {
            /** Returns a [DataSchema.TerrainGridResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.TerrainGridResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TerrainGridResult {
                val given = DataSchema.TerrainGridResult.Builder().apply(optional)
                return DataSchema.TerrainGridResult(
                    count = given.slot0,
                    coveredCount = given.slot1,
                    zoom = given.slot2,
                    resolutionMeters = given.slot3,
                    cols = given.slot4,
                    rows = given.slot5,
                    cells = given.slot6,
                )
            }
        }
    }

    /** Result of a terrain profile along a geometry. */
    @kotlinx.serialization.Serializable
    public class TerrainProfileResult private constructor(
        /** Total number of samples returned, ordered along the geometry. */
        public val count: kotlin.Int? = null,
        /**
         * Number of samples that fall inside DEM coverage. Weight any average by this
         * rather than by count.
         */
        public val coveredCount: kotlin.Int? = null,
        /**
         * Zoom level of the sampled DEM tiles, so a result can be reproduced exactly.
         * A standard Web Mercator XYZ zoom, chosen by the server rather than the
         * caller: every request samples at the same zoom, the deepest the elevation
         * source serves, so two answers about the same ground are always comparable.
         * Treat it as reported, not requested.
         */
        public val zoom: kotlin.Int? = null,
        /**
         * Ground resolution of one DEM pixel in meters, measured at the first sample's
         * latitude. It varies with latitude, so over a result spanning a wide range of
         * latitudes it describes the first sample's ground rather than every sample's.
         */
        public val resolutionMeters: kotlin.Double? = null,
        /**
         * Surface readings ordered along the geometry. Read each sample's own
         * distanceMeters rather than deriving it from the index: the interval is a target
         * that shortens across a retained input vertex and widens on a very long line.
         */
        public val samples: kotlin.collections.List<DataSchema.TerrainSample>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TerrainProfileResult &&
                other.count == this.count &&
                other.coveredCount == this.coveredCount &&
                other.zoom == this.zoom &&
                other.resolutionMeters == this.resolutionMeters &&
                other.samples == this.samples

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.coveredCount?.hashCode() ?: 0)
            hash = 31 * hash + (this.zoom?.hashCode() ?: 0)
            hash = 31 * hash + (this.resolutionMeters?.hashCode() ?: 0)
            hash = 31 * hash + (this.samples?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainProfileResult(" +
                "count=" + this.count +
                ", coveredCount=" + this.coveredCount +
                ", zoom=" + this.zoom +
                ", resolutionMeters=" + this.resolutionMeters +
                ", samples=" + this.samples +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TerrainProfileResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: kotlin.Int? = null
            internal var slot3: kotlin.Double? = null
            internal var slot4: kotlin.collections.List<DataSchema.TerrainSample>? = null

            /** Total number of samples returned, ordered along the geometry. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Number of samples that fall inside DEM coverage. Weight any average by this
             * rather than by count.
             */
            public var coveredCount: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Zoom level of the sampled DEM tiles, so a result can be reproduced exactly.
             * A standard Web Mercator XYZ zoom, chosen by the server rather than the
             * caller: every request samples at the same zoom, the deepest the elevation
             * source serves, so two answers about the same ground are always comparable.
             * Treat it as reported, not requested.
             */
            public var zoom: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Ground resolution of one DEM pixel in meters, measured at the first sample's
             * latitude. It varies with latitude, so over a result spanning a wide range of
             * latitudes it describes the first sample's ground rather than every sample's.
             */
            public var resolutionMeters: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Surface readings ordered along the geometry. Read each sample's own
             * distanceMeters rather than deriving it from the index: the interval is a target
             * that shortens across a retained input vertex and widens on a very long line.
             */
            public var samples: kotlin.collections.List<DataSchema.TerrainSample>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }
        }

        /** Builds a [DataSchema.TerrainProfileResult]. */
        public companion object {
            /** Returns a [DataSchema.TerrainProfileResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.TerrainProfileResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TerrainProfileResult {
                val given = DataSchema.TerrainProfileResult.Builder().apply(optional)
                return DataSchema.TerrainProfileResult(
                    count = given.slot0,
                    coveredCount = given.slot1,
                    zoom = given.slot2,
                    resolutionMeters = given.slot3,
                    samples = given.slot4,
                )
            }
        }
    }

    /**
     * A single surface reading. Shared by all three terrain queries.
     *
     * A reading is reported as outside DEM coverage, with a null elevation, slope and
     * aspect, both when the point lies beyond the DEM and when the DEM tile holding it
     * could not be fetched. A query fails with INTERNAL_ERROR only when no tile at all
     * could be fetched.
     */
    @kotlinx.serialization.Serializable
    public class TerrainSample private constructor(
        /** Echo of the sampled [longitude, latitude] point (WGS 84). */
        public val point: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Elevation in meters above sea level; null when the point falls outside DEM
         * coverage.
         */
        public val elevation: kotlin.Double? = null,
        /** Slope angle in degrees; null when the point falls outside DEM coverage. */
        public val slope: kotlin.Double? = null,
        /**
         * Downhill aspect as a compass bearing in degrees (0-360, 0 = north); null when
         * the point falls outside DEM coverage or the surface is flat.
         */
        public val aspect: kotlin.Double? = null,
        /** Compass octant of the downhill aspect; null whenever aspect is null. */
        public val aspectOctant: DataSchema.AspectOctant? = null,
        /**
         * Steepness band derived from the slope, matching the steepness overlay's legend;
         * null below 25 degrees or outside DEM coverage.
         */
        public val steepnessBand: DataSchema.SteepnessBand? = null,
        /**
         * Distance along the geometry from its start, in meters; null outside a
         * terrainProfile result.
         *
         * It is carried per sample rather than implied by the index because
         * `spacingMeters` is a target, not a constant: every input vertex is retained, so
         * a sample immediately after a bend sits closer than the interval, and a very long
         * line is sampled more coarsely. Multiplying the index by the spacing would place
         * every sample after the first bend in the wrong place.
         */
        public val distanceMeters: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TerrainSample &&
                other.point == this.point &&
                other.elevation == this.elevation &&
                other.slope == this.slope &&
                other.aspect == this.aspect &&
                other.aspectOctant == this.aspectOctant &&
                other.steepnessBand == this.steepnessBand &&
                other.distanceMeters == this.distanceMeters

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.point?.hashCode() ?: 0)
            hash = 31 * hash + (this.elevation?.hashCode() ?: 0)
            hash = 31 * hash + (this.slope?.hashCode() ?: 0)
            hash = 31 * hash + (this.aspect?.hashCode() ?: 0)
            hash = 31 * hash + (this.aspectOctant?.hashCode() ?: 0)
            hash = 31 * hash + (this.steepnessBand?.hashCode() ?: 0)
            hash = 31 * hash + (this.distanceMeters?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainSample(" +
                "point=" + this.point +
                ", elevation=" + this.elevation +
                ", slope=" + this.slope +
                ", aspect=" + this.aspect +
                ", aspectOctant=" + this.aspectOctant +
                ", steepnessBand=" + this.steepnessBand +
                ", distanceMeters=" + this.distanceMeters +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TerrainSample], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<kotlin.Double>? = null
            internal var slot1: kotlin.Double? = null
            internal var slot2: kotlin.Double? = null
            internal var slot3: kotlin.Double? = null
            internal var slot4: DataSchema.AspectOctant? = null
            internal var slot5: DataSchema.SteepnessBand? = null
            internal var slot6: kotlin.Double? = null

            /** Echo of the sampled [longitude, latitude] point (WGS 84). */
            public var point: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Elevation in meters above sea level; null when the point falls outside DEM
             * coverage.
             */
            public var elevation: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Slope angle in degrees; null when the point falls outside DEM coverage. */
            public var slope: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Downhill aspect as a compass bearing in degrees (0-360, 0 = north); null when
             * the point falls outside DEM coverage or the surface is flat.
             */
            public var aspect: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** Compass octant of the downhill aspect; null whenever aspect is null. */
            public var aspectOctant: DataSchema.AspectOctant?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Steepness band derived from the slope, matching the steepness overlay's legend;
             * null below 25 degrees or outside DEM coverage.
             */
            public var steepnessBand: DataSchema.SteepnessBand?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Distance along the geometry from its start, in meters; null outside a
             * terrainProfile result.
             *
             * It is carried per sample rather than implied by the index because
             * `spacingMeters` is a target, not a constant: every input vertex is retained, so
             * a sample immediately after a bend sits closer than the interval, and a very long
             * line is sampled more coarsely. Multiplying the index by the spacing would place
             * every sample after the first bend in the wrong place.
             */
            public var distanceMeters: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }
        }

        /** Builds a [DataSchema.TerrainSample]. */
        public companion object {
            /** Returns a [DataSchema.TerrainSample] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.TerrainSample.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TerrainSample {
                val given = DataSchema.TerrainSample.Builder().apply(optional)
                return DataSchema.TerrainSample(
                    point = given.slot0,
                    elevation = given.slot1,
                    slope = given.slot2,
                    aspect = given.slot3,
                    aspectOctant = given.slot4,
                    steepnessBand = given.slot5,
                    distanceMeters = given.slot6,
                )
            }
        }
    }

    /** Result of a point terrain query. */
    @kotlinx.serialization.Serializable
    public class TerrainSamplesResult private constructor(
        /** Total number of samples returned, one per input location, in input order. */
        public val count: kotlin.Int? = null,
        /**
         * Number of samples that fall inside DEM coverage. Weight any average by this
         * rather than by count.
         */
        public val coveredCount: kotlin.Int? = null,
        /**
         * Zoom level of the sampled DEM tiles, so a result can be reproduced exactly.
         * A standard Web Mercator XYZ zoom, chosen by the server rather than the
         * caller: every request samples at the same zoom, the deepest the elevation
         * source serves, so two answers about the same ground are always comparable.
         * Treat it as reported, not requested.
         */
        public val zoom: kotlin.Int? = null,
        /**
         * Ground resolution of one DEM pixel in meters, measured at the first sample's
         * latitude. It varies with latitude, so over a result spanning a wide range of
         * latitudes it describes the first sample's ground rather than every sample's.
         */
        public val resolutionMeters: kotlin.Double? = null,
        /** Surface readings, one per input location, in input order. */
        public val samples: kotlin.collections.List<DataSchema.TerrainSample>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TerrainSamplesResult &&
                other.count == this.count &&
                other.coveredCount == this.coveredCount &&
                other.zoom == this.zoom &&
                other.resolutionMeters == this.resolutionMeters &&
                other.samples == this.samples

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.coveredCount?.hashCode() ?: 0)
            hash = 31 * hash + (this.zoom?.hashCode() ?: 0)
            hash = 31 * hash + (this.resolutionMeters?.hashCode() ?: 0)
            hash = 31 * hash + (this.samples?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainSamplesResult(" +
                "count=" + this.count +
                ", coveredCount=" + this.coveredCount +
                ", zoom=" + this.zoom +
                ", resolutionMeters=" + this.resolutionMeters +
                ", samples=" + this.samples +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TerrainSamplesResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: kotlin.Int? = null
            internal var slot3: kotlin.Double? = null
            internal var slot4: kotlin.collections.List<DataSchema.TerrainSample>? = null

            /** Total number of samples returned, one per input location, in input order. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Number of samples that fall inside DEM coverage. Weight any average by this
             * rather than by count.
             */
            public var coveredCount: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Zoom level of the sampled DEM tiles, so a result can be reproduced exactly.
             * A standard Web Mercator XYZ zoom, chosen by the server rather than the
             * caller: every request samples at the same zoom, the deepest the elevation
             * source serves, so two answers about the same ground are always comparable.
             * Treat it as reported, not requested.
             */
            public var zoom: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Ground resolution of one DEM pixel in meters, measured at the first sample's
             * latitude. It varies with latitude, so over a result spanning a wide range of
             * latitudes it describes the first sample's ground rather than every sample's.
             */
            public var resolutionMeters: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** Surface readings, one per input location, in input order. */
            public var samples: kotlin.collections.List<DataSchema.TerrainSample>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }
        }

        /** Builds a [DataSchema.TerrainSamplesResult]. */
        public companion object {
            /** Returns a [DataSchema.TerrainSamplesResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.TerrainSamplesResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TerrainSamplesResult {
                val given = DataSchema.TerrainSamplesResult.Builder().apply(optional)
                return DataSchema.TerrainSamplesResult(
                    count = given.slot0,
                    coveredCount = given.slot1,
                    zoom = given.slot2,
                    resolutionMeters = given.slot3,
                    samples = given.slot4,
                )
            }
        }
    }

    /** A coordinate index range with an associated tollway classification. */
    @kotlinx.serialization.Serializable
    public class TollwayInfo private constructor(
        /** Start index in the coordinates array (inclusive). */
        public val fromIndex: kotlin.Int? = null,
        /** End index in the coordinates array (inclusive). */
        public val toIndex: kotlin.Int? = null,
        /** Whether this coordinate range crosses a tollway for the activity's mode of travel. */
        public val value: DataSchema.TollwayType? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TollwayInfo &&
                other.fromIndex == this.fromIndex &&
                other.toIndex == this.toIndex &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.fromIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.toIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TollwayInfo(" +
                "fromIndex=" + this.fromIndex +
                ", toIndex=" + this.toIndex +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TollwayInfo], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: DataSchema.TollwayType? = null

            /** Start index in the coordinates array (inclusive). */
            public var fromIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** End index in the coordinates array (inclusive). */
            public var toIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Whether this coordinate range crosses a tollway for the activity's mode of travel. */
            public var value: DataSchema.TollwayType?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.TollwayInfo]. */
        public companion object {
            /** Returns a [DataSchema.TollwayInfo] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.TollwayInfo.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TollwayInfo {
                val given = DataSchema.TollwayInfo.Builder().apply(optional)
                return DataSchema.TollwayInfo(
                    fromIndex = given.slot0,
                    toIndex = given.slot1,
                    value = given.slot2,
                )
            }
        }
    }

    /** A coordinate index range with an associated trail difficulty. */
    @kotlinx.serialization.Serializable
    public class TrailDifficultyInfo private constructor(
        /** Start index in the coordinates array (inclusive). */
        public val fromIndex: kotlin.Int? = null,
        /** End index in the coordinates array (inclusive). */
        public val toIndex: kotlin.Int? = null,
        /** Trail difficulty for this coordinate range, on the scale matching the activity. */
        public val value: DataSchema.TrailDifficultyType? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TrailDifficultyInfo &&
                other.fromIndex == this.fromIndex &&
                other.toIndex == this.toIndex &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.fromIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.toIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TrailDifficultyInfo(" +
                "fromIndex=" + this.fromIndex +
                ", toIndex=" + this.toIndex +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TrailDifficultyInfo], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: DataSchema.TrailDifficultyType? = null

            /** Start index in the coordinates array (inclusive). */
            public var fromIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** End index in the coordinates array (inclusive). */
            public var toIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Trail difficulty for this coordinate range, on the scale matching the activity. */
            public var value: DataSchema.TrailDifficultyType?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.TrailDifficultyInfo]. */
        public companion object {
            /** Returns a [DataSchema.TrailDifficultyInfo] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.TrailDifficultyInfo.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TrailDifficultyInfo {
                val given = DataSchema.TrailDifficultyInfo.Builder().apply(optional)
                return DataSchema.TrailDifficultyInfo(
                    fromIndex = given.slot0,
                    toIndex = given.slot1,
                    value = given.slot2,
                )
            }
        }
    }

    /** A single named way result. */
    @kotlinx.serialization.Serializable
    public class Way private constructor(
        /** Reference to the underlying OpenStreetMap feature. */
        public val osm: DataSchema.OSMRef? = null,
        /** Localized display name. */
        public val name: kotlin.String? = null,
        /** OSM highway tag value (e.g. path, track, footway). */
        public val highway: kotlin.String? = null,
        /** OSM surface tag value. Often null: present only where the way's data carries that tag. */
        public val surface: kotlin.String? = null,
        /** OSM sac_scale tag value. Often null: present only where the way's data carries that tag. */
        public val sacScale: kotlin.String? = null,
        /** Representative coordinate as [longitude, latitude]. */
        public val point: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Bounding box as [minLongitude, minLatitude, maxLongitude, maxLatitude].
         * Null when the upstream geocoder returned no extent.
         */
        public val bounds: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Polyline coordinates as a list of [longitude, latitude] pairs; null when
         * the upstream geocoder did not provide a polyline.
         */
        public val coordinates: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>? = null,
        /**
         * Activities whose routing profile accepts this way.
         *
         * Reports what was actually established, not the full catalog. Filtering the
         * search to one activity narrows this to that activity alone: the other
         * compatible ones are neither checked nor claimed, because a filtered search
         * verifies only what it was asked about. Read an absent activity as unreported
         * rather than as refused.
         */
        public val activityCompatible: kotlin.collections.List<DataSchema.ActivitySlug>? = null,
        /**
         * ISO 3166-1 alpha-2 country code, e.g. "FR" or "BE". Null when the
         * upstream geocoder did not resolve admin context.
         */
        public val country: kotlin.String? = null,
        /** Localized display name of the way's country. Null when not available. */
        public val countryName: kotlin.String? = null,
        /** Administrative region or state the way belongs to (e.g. Haute-Savoie). Null when not available. */
        public val region: kotlin.String? = null,
        /** City the way belongs to. Null when not available. */
        public val city: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.Way &&
                other.osm == this.osm &&
                other.name == this.name &&
                other.highway == this.highway &&
                other.surface == this.surface &&
                other.sacScale == this.sacScale &&
                other.point == this.point &&
                other.bounds == this.bounds &&
                other.coordinates == this.coordinates &&
                other.activityCompatible == this.activityCompatible &&
                other.country == this.country &&
                other.countryName == this.countryName &&
                other.region == this.region &&
                other.city == this.city

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.osm?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.highway?.hashCode() ?: 0)
            hash = 31 * hash + (this.surface?.hashCode() ?: 0)
            hash = 31 * hash + (this.sacScale?.hashCode() ?: 0)
            hash = 31 * hash + (this.point?.hashCode() ?: 0)
            hash = 31 * hash + (this.bounds?.hashCode() ?: 0)
            hash = 31 * hash + (this.coordinates?.hashCode() ?: 0)
            hash = 31 * hash + (this.activityCompatible?.hashCode() ?: 0)
            hash = 31 * hash + (this.country?.hashCode() ?: 0)
            hash = 31 * hash + (this.countryName?.hashCode() ?: 0)
            hash = 31 * hash + (this.region?.hashCode() ?: 0)
            hash = 31 * hash + (this.city?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "Way(" +
                "osm=" + this.osm +
                ", name=" + this.name +
                ", highway=" + this.highway +
                ", surface=" + this.surface +
                ", sacScale=" + this.sacScale +
                ", point=" + this.point +
                ", bounds=" + this.bounds +
                ", coordinates=" + this.coordinates +
                ", activityCompatible=" + this.activityCompatible +
                ", country=" + this.country +
                ", countryName=" + this.countryName +
                ", region=" + this.region +
                ", city=" + this.city +
                ")"

        /** Builder sets the optional properties of a [DataSchema.Way], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: DataSchema.OSMRef? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.String? = null
            internal var slot4: kotlin.String? = null
            internal var slot5: kotlin.collections.List<kotlin.Double>? = null
            internal var slot6: kotlin.collections.List<kotlin.Double>? = null
            internal var slot7: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>? = null
            internal var slot8: kotlin.collections.List<DataSchema.ActivitySlug>? = null
            internal var slot9: kotlin.String? = null
            internal var slot10: kotlin.String? = null
            internal var slot11: kotlin.String? = null
            internal var slot12: kotlin.String? = null

            /** Reference to the underlying OpenStreetMap feature. */
            public var osm: DataSchema.OSMRef?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Localized display name. */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** OSM highway tag value (e.g. path, track, footway). */
            public var highway: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** OSM surface tag value. Often null: present only where the way's data carries that tag. */
            public var surface: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** OSM sac_scale tag value. Often null: present only where the way's data carries that tag. */
            public var sacScale: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Representative coordinate as [longitude, latitude]. */
            public var point: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Bounding box as [minLongitude, minLatitude, maxLongitude, maxLatitude].
             * Null when the upstream geocoder returned no extent.
             */
            public var bounds: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Polyline coordinates as a list of [longitude, latitude] pairs; null when
             * the upstream geocoder did not provide a polyline.
             */
            public var coordinates: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /**
             * Activities whose routing profile accepts this way.
             *
             * Reports what was actually established, not the full catalog. Filtering the
             * search to one activity narrows this to that activity alone: the other
             * compatible ones are neither checked nor claimed, because a filtered search
             * verifies only what it was asked about. Read an absent activity as unreported
             * rather than as refused.
             */
            public var activityCompatible: kotlin.collections.List<DataSchema.ActivitySlug>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /**
             * ISO 3166-1 alpha-2 country code, e.g. "FR" or "BE". Null when the
             * upstream geocoder did not resolve admin context.
             */
            public var country: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }

            /** Localized display name of the way's country. Null when not available. */
            public var countryName: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot10 = value
                }

            /** Administrative region or state the way belongs to (e.g. Haute-Savoie). Null when not available. */
            public var region: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot11 = value
                }

            /** City the way belongs to. Null when not available. */
            public var city: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot12 = value
                }
        }

        /** Builds a [DataSchema.Way]. */
        public companion object {
            /** Returns a [DataSchema.Way] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.Way.Builder.() -> kotlin.Unit = {},
            ): DataSchema.Way {
                val given = DataSchema.Way.Builder().apply(optional)
                return DataSchema.Way(
                    osm = given.slot0,
                    name = given.slot1,
                    highway = given.slot2,
                    surface = given.slot3,
                    sacScale = given.slot4,
                    point = given.slot5,
                    bounds = given.slot6,
                    coordinates = given.slot7,
                    activityCompatible = given.slot8,
                    country = given.slot9,
                    countryName = given.slot10,
                    region = given.slot11,
                    city = given.slot12,
                )
            }
        }
    }

    /** A coordinate index range with an associated way type. */
    @kotlinx.serialization.Serializable
    public class WayTypeInfo private constructor(
        /** Start index in the coordinates array (inclusive). */
        public val fromIndex: kotlin.Int? = null,
        /** End index in the coordinates array (inclusive). */
        public val toIndex: kotlin.Int? = null,
        /** Way type for this coordinate range. */
        public val value: DataSchema.WayType? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.WayTypeInfo &&
                other.fromIndex == this.fromIndex &&
                other.toIndex == this.toIndex &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.fromIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.toIndex?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WayTypeInfo(" +
                "fromIndex=" + this.fromIndex +
                ", toIndex=" + this.toIndex +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [DataSchema.WayTypeInfo], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.Int? = null
            internal var slot2: DataSchema.WayType? = null

            /** Start index in the coordinates array (inclusive). */
            public var fromIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** End index in the coordinates array (inclusive). */
            public var toIndex: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Way type for this coordinate range. */
            public var value: DataSchema.WayType?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.WayTypeInfo]. */
        public companion object {
            /** Returns a [DataSchema.WayTypeInfo] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.WayTypeInfo.Builder.() -> kotlin.Unit = {},
            ): DataSchema.WayTypeInfo {
                val given = DataSchema.WayTypeInfo.Builder().apply(optional)
                return DataSchema.WayTypeInfo(
                    fromIndex = given.slot0,
                    toIndex = given.slot1,
                    value = given.slot2,
                )
            }
        }
    }

    /**
     * How to reach the place a waypoint marks. Deliberately flat and single-line rather
     * than a structured postal address: an itinerary waypoint is a place on a map, not
     * a billing party.
     */
    @kotlinx.serialization.Serializable
    public class WaypointContact private constructor(
        /** Absolute URL of this place's website; null when not provided. */
        public val website: com.mountaya.client.Url? = null,
        /** Contact phone number, in international format where known; null when not provided. */
        public val phone: kotlin.String? = null,
        /** Contact email address; null when not provided. */
        public val email: kotlin.String? = null,
        /** Postal address on a single line; null when not provided. */
        public val address: kotlin.String? = null,
        /** Opening hours in free text, as published by the place itself; null when not provided. */
        public val openingHours: kotlin.String? = null,
        /** Absolute URL where a stay, table or slot can be booked; null when not provided. */
        public val bookingUrl: com.mountaya.client.Url? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.WaypointContact &&
                other.website == this.website &&
                other.phone == this.phone &&
                other.email == this.email &&
                other.address == this.address &&
                other.openingHours == this.openingHours &&
                other.bookingUrl == this.bookingUrl

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.website?.hashCode() ?: 0)
            hash = 31 * hash + (this.phone?.hashCode() ?: 0)
            hash = 31 * hash + (this.email?.hashCode() ?: 0)
            hash = 31 * hash + (this.address?.hashCode() ?: 0)
            hash = 31 * hash + (this.openingHours?.hashCode() ?: 0)
            hash = 31 * hash + (this.bookingUrl?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WaypointContact(" +
                "website=" + this.website +
                ", phone=" + this.phone +
                ", email=" + this.email +
                ", address=" + this.address +
                ", openingHours=" + this.openingHours +
                ", bookingUrl=" + this.bookingUrl +
                ")"

        /** Builder sets the optional properties of a [DataSchema.WaypointContact], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.Url? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.String? = null
            internal var slot4: kotlin.String? = null
            internal var slot5: com.mountaya.client.Url? = null

            /** Absolute URL of this place's website; null when not provided. */
            public var website: com.mountaya.client.Url?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Contact phone number, in international format where known; null when not provided. */
            public var phone: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Contact email address; null when not provided. */
            public var email: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Postal address on a single line; null when not provided. */
            public var address: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** Opening hours in free text, as published by the place itself; null when not provided. */
            public var openingHours: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Absolute URL where a stay, table or slot can be booked; null when not provided. */
            public var bookingUrl: com.mountaya.client.Url?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [DataSchema.WaypointContact]. */
        public companion object {
            /** Returns a [DataSchema.WaypointContact] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.WaypointContact.Builder.() -> kotlin.Unit = {},
            ): DataSchema.WaypointContact {
                val given = DataSchema.WaypointContact.Builder().apply(optional)
                return DataSchema.WaypointContact(
                    website = given.slot0,
                    phone = given.slot1,
                    email = given.slot2,
                    address = given.slot3,
                    openingHours = given.slot4,
                    bookingUrl = given.slot5,
                )
            }
        }
    }

    /** A labelled external link attached to a waypoint. */
    @kotlinx.serialization.Serializable
    public class WaypointLink private constructor(
        /** Text shown for the link. */
        public val label: kotlin.String? = null,
        /** Absolute URL the link points to. */
        public val url: com.mountaya.client.Url? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.WaypointLink &&
                other.label == this.label &&
                other.url == this.url

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.label?.hashCode() ?: 0)
            hash = 31 * hash + (this.url?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WaypointLink(" +
                "label=" + this.label +
                ", url=" + this.url +
                ")"

        /** Builder sets the optional properties of a [DataSchema.WaypointLink], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: com.mountaya.client.Url? = null

            /** Text shown for the link. */
            public var label: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Absolute URL the link points to. */
            public var url: com.mountaya.client.Url?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.WaypointLink]. */
        public companion object {
            /** Returns a [DataSchema.WaypointLink] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.WaypointLink.Builder.() -> kotlin.Unit = {},
            ): DataSchema.WaypointLink {
                val given = DataSchema.WaypointLink.Builder().apply(optional)
                return DataSchema.WaypointLink(
                    label = given.slot0,
                    url = given.slot1,
                )
            }
        }
    }

    /**
     * Photos and external links attached to a waypoint. Media is referenced by URL
     * rather than uploaded: Mountaya stores the address, never the bytes.
     */
    @kotlinx.serialization.Serializable
    public class WaypointMedia private constructor(
        /**
         * Photographs of the place, in display order. Null when none are attached: the
         * empty case is carried as null rather than as an empty list.
         */
        public val photos: kotlin.collections.List<DataSchema.WaypointPhoto>? = null,
        /**
         * Labelled external links, in display order. Null when none are attached: the
         * empty case is carried as null rather than as an empty list.
         */
        public val links: kotlin.collections.List<DataSchema.WaypointLink>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.WaypointMedia &&
                other.photos == this.photos &&
                other.links == this.links

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.photos?.hashCode() ?: 0)
            hash = 31 * hash + (this.links?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WaypointMedia(" +
                "photos=" + this.photos +
                ", links=" + this.links +
                ")"

        /** Builder sets the optional properties of a [DataSchema.WaypointMedia], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<DataSchema.WaypointPhoto>? = null
            internal var slot1: kotlin.collections.List<DataSchema.WaypointLink>? = null

            /**
             * Photographs of the place, in display order. Null when none are attached: the
             * empty case is carried as null rather than as an empty list.
             */
            public var photos: kotlin.collections.List<DataSchema.WaypointPhoto>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Labelled external links, in display order. Null when none are attached: the
             * empty case is carried as null rather than as an empty list.
             */
            public var links: kotlin.collections.List<DataSchema.WaypointLink>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.WaypointMedia]. */
        public companion object {
            /** Returns a [DataSchema.WaypointMedia] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.WaypointMedia.Builder.() -> kotlin.Unit = {},
            ): DataSchema.WaypointMedia {
                val given = DataSchema.WaypointMedia.Builder().apply(optional)
                return DataSchema.WaypointMedia(
                    photos = given.slot0,
                    links = given.slot1,
                )
            }
        }
    }

    /** A photograph of the place a waypoint marks. */
    @kotlinx.serialization.Serializable
    public class WaypointPhoto private constructor(
        /** Absolute URL of the image. */
        public val url: com.mountaya.client.Url? = null,
        /** Short caption describing the photo; null when not provided. */
        public val caption: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.WaypointPhoto &&
                other.url == this.url &&
                other.caption == this.caption

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.url?.hashCode() ?: 0)
            hash = 31 * hash + (this.caption?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WaypointPhoto(" +
                "url=" + this.url +
                ", caption=" + this.caption +
                ")"

        /** Builder sets the optional properties of a [DataSchema.WaypointPhoto], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.Url? = null
            internal var slot1: kotlin.String? = null

            /** Absolute URL of the image. */
            public var url: com.mountaya.client.Url?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Short caption describing the photo; null when not provided. */
            public var caption: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.WaypointPhoto]. */
        public companion object {
            /** Returns a [DataSchema.WaypointPhoto] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.WaypointPhoto.Builder.() -> kotlin.Unit = {},
            ): DataSchema.WaypointPhoto {
                val given = DataSchema.WaypointPhoto.Builder().apply(optional)
                return DataSchema.WaypointPhoto(
                    url = given.slot0,
                    caption = given.slot1,
                )
            }
        }
    }

    /**
     * A customer-defined fact about a waypoint, carried as a labelled key-value pair.
     *
     * The shape is fixed even though the vocabulary is not: key identifies the field
     * across waypoints so it can be read programmatically, label is what a reader sees,
     * and format says how to present value without inspecting it.
     */
    @kotlinx.serialization.Serializable
    public class WaypointProperty private constructor(
        /** Stable lowercase-slug identifier, unique within the waypoint. */
        public val key: kotlin.String? = null,
        /** Human-readable name of this field, shown to readers. */
        public val label: kotlin.String? = null,
        /** The field's value, always carried as a string. `format` says how to interpret it. */
        public val value: kotlin.String? = null,
        /** How `value` should be rendered; null is equivalent to TEXT. */
        public val format: DataSchema.WaypointPropertyFormat? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.WaypointProperty &&
                other.key == this.key &&
                other.label == this.label &&
                other.value == this.value &&
                other.format == this.format

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.key?.hashCode() ?: 0)
            hash = 31 * hash + (this.label?.hashCode() ?: 0)
            hash = 31 * hash + (this.value?.hashCode() ?: 0)
            hash = 31 * hash + (this.format?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WaypointProperty(" +
                "key=" + this.key +
                ", label=" + this.label +
                ", value=" + this.value +
                ", format=" + this.format +
                ")"

        /** Builder sets the optional properties of a [DataSchema.WaypointProperty], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: DataSchema.WaypointPropertyFormat? = null

            /** Stable lowercase-slug identifier, unique within the waypoint. */
            public var key: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Human-readable name of this field, shown to readers. */
            public var label: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** The field's value, always carried as a string. `format` says how to interpret it. */
            public var value: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** How `value` should be rendered; null is equivalent to TEXT. */
            public var format: DataSchema.WaypointPropertyFormat?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [DataSchema.WaypointProperty]. */
        public companion object {
            /** Returns a [DataSchema.WaypointProperty] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.WaypointProperty.Builder.() -> kotlin.Unit = {},
            ): DataSchema.WaypointProperty {
                val given = DataSchema.WaypointProperty.Builder().apply(optional)
                return DataSchema.WaypointProperty(
                    key = given.slot0,
                    label = given.slot1,
                    value = given.slot2,
                    format = given.slot3,
                )
            }
        }
    }

    /**
     * Timing published for a waypoint: the instant a participant must have left it
     * by, and how long they are expected to spend there. A cut-off is an absolute
     * instant and nothing else.
     */
    @kotlinx.serialization.Serializable
    public class WaypointTiming private constructor(
        /**
         * Absolute instant by which this waypoint must be left, as an RFC 3339 timestamp;
         * null when none is published.
         */
        public val cutoffAt: kotlin.time.Instant? = null,
        /** Seconds expected to be spent here; null when no stop is planned. */
        public val stopDuration: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.WaypointTiming &&
                other.cutoffAt == this.cutoffAt &&
                other.stopDuration == this.stopDuration

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.cutoffAt?.hashCode() ?: 0)
            hash = 31 * hash + (this.stopDuration?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WaypointTiming(" +
                "cutoffAt=" + this.cutoffAt +
                ", stopDuration=" + this.stopDuration +
                ")"

        /** Builder sets the optional properties of a [DataSchema.WaypointTiming], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.time.Instant? = null
            internal var slot1: kotlin.Int? = null

            /**
             * Absolute instant by which this waypoint must be left, as an RFC 3339 timestamp;
             * null when none is published.
             */
            public var cutoffAt: kotlin.time.Instant?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Seconds expected to be spent here; null when no stop is planned. */
            public var stopDuration: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.WaypointTiming]. */
        public companion object {
            /** Returns a [DataSchema.WaypointTiming] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.WaypointTiming.Builder.() -> kotlin.Unit = {},
            ): DataSchema.WaypointTiming {
                val given = DataSchema.WaypointTiming.Builder().apply(optional)
                return DataSchema.WaypointTiming(
                    cutoffAt = given.slot0,
                    stopDuration = given.slot1,
                )
            }
        }
    }

    /** Result of the ways target. */
    @kotlinx.serialization.Serializable
    public class WaysResult private constructor(
        /** Number of ways returned: the length of `ways`, at most `limit`. */
        public val count: kotlin.Int? = null,
        /** List of named ways ordered by relevance. */
        public val ways: kotlin.collections.List<DataSchema.Way>? = null,
        /**
         * Activity slug used to filter the ways, echoed back when one was supplied in
         * the input. Null when no activity filter was applied.
         */
        public val activityApplied: DataSchema.ActivitySlug? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.WaysResult &&
                other.count == this.count &&
                other.ways == this.ways &&
                other.activityApplied == this.activityApplied

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.ways?.hashCode() ?: 0)
            hash = 31 * hash + (this.activityApplied?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WaysResult(" +
                "count=" + this.count +
                ", ways=" + this.ways +
                ", activityApplied=" + this.activityApplied +
                ")"

        /** Builder sets the optional properties of a [DataSchema.WaysResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.collections.List<DataSchema.Way>? = null
            internal var slot2: DataSchema.ActivitySlug? = null

            /** Number of ways returned: the length of `ways`, at most `limit`. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** List of named ways ordered by relevance. */
            public var ways: kotlin.collections.List<DataSchema.Way>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Activity slug used to filter the ways, echoed back when one was supplied in
             * the input. Null when no activity filter was applied.
             */
            public var activityApplied: DataSchema.ActivitySlug?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.WaysResult]. */
        public companion object {
            /** Returns a [DataSchema.WaysResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.WaysResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.WaysResult {
                val given = DataSchema.WaysResult.Builder().apply(optional)
                return DataSchema.WaysResult(
                    count = given.slot0,
                    ways = given.slot1,
                    activityApplied = given.slot2,
                )
            }
        }
    }

    /**
     * A lightweight zone reference returned by the zones target. A zone matches on
     * its name or its description, and `matchedField` says which.
     */
    @kotlinx.serialization.Serializable
    public class ZoneSearchResult private constructor(
        /** Zone identifier. */
        public val id: kotlin.String? = null,
        /** Display name of the zone. */
        public val name: kotlin.String? = null,
        /** Free-text description of the zone. Null when it has none. */
        public val description: kotlin.String? = null,
        /**
         * How serious the zone is, which is what a renderer picks its styling from. It
         * carries no access meaning.
         */
        public val level: DataSchema.ZoneLevel? = null,
        /**
         * How the zone's area was drawn or computed. A zone computed from a source is
         * POLYGON.
         */
        public val mode: DataSchema.ZoneMode? = null,
        /** Representative coordinate as [longitude, latitude]; null when not available. */
        public val point: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Zone bounding box as [minLongitude, minLatitude, maxLongitude, maxLatitude];
         * null when not available.
         */
        public val bounds: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Whether the zone is private. A private zone is served only to a key of the
         * organization that owns it; no other organization's key reaches it, even with
         * its id. A public one is also served by id to any other organization's key, as
         * last published, but is never listed in another organization's search results.
         */
        public val isPrivate: kotlin.Boolean? = null,
        /**
         * Identifier of the organization that owns this zone. Null for a personal zone,
         * made from an individual account and owned by no organization.
         */
        public val organizationId: kotlin.String? = null,
        /**
         * Relevance rank over the zone's name and description, always greater than 0,
         * higher being a better match; a match in the name weighs twice one in the
         * description. It orders the zones section of ONE response and nothing more. The
         * value comes from whichever of two ranking passes produced the match: a
         * full-text rank, which is unbounded above though in practice below 1, or, when
         * the full-text pass matches nothing and the query is at least two characters
         * long, a fuzzy similarity between 0.3 and 1. The two scales differ, so a score is
         * not comparable across responses, across targets, or against a fixed threshold.
         */
        public val score: kotlin.Double? = null,
        /** Field that produced the match. */
        public val matchedField: DataSchema.MatchedField? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ZoneSearchResult &&
                other.id == this.id &&
                other.name == this.name &&
                other.description == this.description &&
                other.level == this.level &&
                other.mode == this.mode &&
                other.point == this.point &&
                other.bounds == this.bounds &&
                other.isPrivate == this.isPrivate &&
                other.organizationId == this.organizationId &&
                other.score == this.score &&
                other.matchedField == this.matchedField

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.id?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + (this.level?.hashCode() ?: 0)
            hash = 31 * hash + (this.mode?.hashCode() ?: 0)
            hash = 31 * hash + (this.point?.hashCode() ?: 0)
            hash = 31 * hash + (this.bounds?.hashCode() ?: 0)
            hash = 31 * hash + (this.isPrivate?.hashCode() ?: 0)
            hash = 31 * hash + (this.organizationId?.hashCode() ?: 0)
            hash = 31 * hash + (this.score?.hashCode() ?: 0)
            hash = 31 * hash + (this.matchedField?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ZoneSearchResult(" +
                "id=" + this.id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", level=" + this.level +
                ", mode=" + this.mode +
                ", point=" + this.point +
                ", bounds=" + this.bounds +
                ", isPrivate=" + this.isPrivate +
                ", organizationId=" + this.organizationId +
                ", score=" + this.score +
                ", matchedField=" + this.matchedField +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ZoneSearchResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: DataSchema.ZoneLevel? = null
            internal var slot4: DataSchema.ZoneMode? = null
            internal var slot5: kotlin.collections.List<kotlin.Double>? = null
            internal var slot6: kotlin.collections.List<kotlin.Double>? = null
            internal var slot7: kotlin.Boolean? = null
            internal var slot8: kotlin.String? = null
            internal var slot9: kotlin.Double? = null
            internal var slot10: DataSchema.MatchedField? = null

            /** Zone identifier. */
            public var id: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** Display name of the zone. */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** Free-text description of the zone. Null when it has none. */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * How serious the zone is, which is what a renderer picks its styling from. It
             * carries no access meaning.
             */
            public var level: DataSchema.ZoneLevel?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * How the zone's area was drawn or computed. A zone computed from a source is
             * POLYGON.
             */
            public var mode: DataSchema.ZoneMode?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Representative coordinate as [longitude, latitude]; null when not available. */
            public var point: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Zone bounding box as [minLongitude, minLatitude, maxLongitude, maxLatitude];
             * null when not available.
             */
            public var bounds: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Whether the zone is private. A private zone is served only to a key of the
             * organization that owns it; no other organization's key reaches it, even with
             * its id. A public one is also served by id to any other organization's key, as
             * last published, but is never listed in another organization's search results.
             */
            public var isPrivate: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /**
             * Identifier of the organization that owns this zone. Null for a personal zone,
             * made from an individual account and owned by no organization.
             */
            public var organizationId: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /**
             * Relevance rank over the zone's name and description, always greater than 0,
             * higher being a better match; a match in the name weighs twice one in the
             * description. It orders the zones section of ONE response and nothing more. The
             * value comes from whichever of two ranking passes produced the match: a
             * full-text rank, which is unbounded above though in practice below 1, or, when
             * the full-text pass matches nothing and the query is at least two characters
             * long, a fuzzy similarity between 0.3 and 1. The two scales differ, so a score is
             * not comparable across responses, across targets, or against a fixed threshold.
             */
            public var score: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }

            /** Field that produced the match. */
            public var matchedField: DataSchema.MatchedField?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot10 = value
                }
        }

        /** Builds a [DataSchema.ZoneSearchResult]. */
        public companion object {
            /** Returns a [DataSchema.ZoneSearchResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ZoneSearchResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ZoneSearchResult {
                val given = DataSchema.ZoneSearchResult.Builder().apply(optional)
                return DataSchema.ZoneSearchResult(
                    id = given.slot0,
                    name = given.slot1,
                    description = given.slot2,
                    level = given.slot3,
                    mode = given.slot4,
                    point = given.slot5,
                    bounds = given.slot6,
                    isPrivate = given.slot7,
                    organizationId = given.slot8,
                    score = given.slot9,
                    matchedField = given.slot10,
                )
            }
        }
    }

    /** Result of the zones target. */
    @kotlinx.serialization.Serializable
    public class ZonesResult private constructor(
        /** Number of zones returned: the length of `zones`, at most `limit`. */
        public val count: kotlin.Int? = null,
        /** List of zone references ordered by relevance. */
        public val zones: kotlin.collections.List<DataSchema.ZoneSearchResult>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ZonesResult &&
                other.count == this.count &&
                other.zones == this.zones

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.count?.hashCode() ?: 0)
            hash = 31 * hash + (this.zones?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ZonesResult(" +
                "count=" + this.count +
                ", zones=" + this.zones +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ZonesResult], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null
            internal var slot1: kotlin.collections.List<DataSchema.ZoneSearchResult>? = null

            /** Number of zones returned: the length of `zones`, at most `limit`. */
            public var count: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** List of zone references ordered by relevance. */
            public var zones: kotlin.collections.List<DataSchema.ZoneSearchResult>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.ZonesResult]. */
        public companion object {
            /** Returns a [DataSchema.ZonesResult] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.ZonesResult.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ZonesResult {
                val given = DataSchema.ZonesResult.Builder().apply(optional)
                return DataSchema.ZonesResult(
                    count = given.slot0,
                    zones = given.slot1,
                )
            }
        }
    }

    /** Input for analyzing surface and way type of an existing geometry. */
    @kotlinx.serialization.Serializable
    public class AnalyzeGeometryInput private constructor(
        /** Activity to use for analysis. */
        public val activity: DataSchema.ActivitySlug,
        /**
         * Ordered list of [longitude, latitude] coordinate pairs forming the geometry (WGS 84).
         * Between 2 and 10,000 positions; a count outside that range is VALIDATION_FAILED
         * at ["input", "coordinates"]. The line, measured along its positions, is at
         * most 500 km long; a longer one is VALIDATION_FAILED at
         * ["input", "coordinates"], decided before any elevation or routing work. Each
         * is exactly two numbers, a longitude between -180 and 180 and a latitude
         * between -90 and 90, and one that is not is VALIDATION_FAILED at
         * ["input", "coordinates", i].
         * Example: [[6.8667, 45.9195], [6.8712, 45.9231], [6.8790, 45.9160]] (a short trail segment).
         */
        public val coordinates: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
        /**
         * Travel speed in km/h on LEVEL ground, a finite number greater than 0 (anything
         * else, NaN and Infinity included, is VALIDATION_FAILED at ["input", "speed"],
         * never answered at the activity default), replacing the activity's default as
         * the pace `duration` is anchored to. Omitted uses the activity default. Each
         * stretch's resolved speed is capped at the activity's maximum speed (see
         * SpeedInfo).
         *
         * It is the same field, meaning the same thing, that `directions` and
         * `suggestions` take, which is the point of it being here. A drawn line
         * previewed at the pace it will be saved at and the segment it becomes report
         * the same duration; without it, analyzing a line could only ever answer at the
         * activity default while the saved segment answered at the user's speed.
         * Example: 4.5 for hiking, 25.0 for road cycling.
         */
        public val speed: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.AnalyzeGeometryInput &&
                other.activity == this.activity &&
                other.coordinates == this.coordinates &&
                other.speed == this.speed

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.activity.hashCode()
            hash = 31 * hash + this.coordinates.hashCode()
            hash = 31 * hash + (this.speed?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "AnalyzeGeometryInput(" +
                "activity=" + this.activity +
                ", coordinates=" + this.coordinates +
                ", speed=" + this.speed +
                ")"

        /** Builder sets the optional properties of a [DataSchema.AnalyzeGeometryInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: kotlin.Double? = null

            /**
             * Travel speed in km/h on LEVEL ground, a finite number greater than 0 (anything
             * else, NaN and Infinity included, is VALIDATION_FAILED at ["input", "speed"],
             * never answered at the activity default), replacing the activity's default as
             * the pace `duration` is anchored to. Omitted uses the activity default. Each
             * stretch's resolved speed is capped at the activity's maximum speed (see
             * SpeedInfo).
             *
             * It is the same field, meaning the same thing, that `directions` and
             * `suggestions` take, which is the point of it being here. A drawn line
             * previewed at the pace it will be saved at and the segment it becomes report
             * the same duration; without it, analyzing a line could only ever answer at the
             * activity default while the saved segment answered at the user's speed.
             * Example: 4.5 for hiking, 25.0 for road cycling.
             */
            public var speed: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.AnalyzeGeometryInput]. */
        public companion object {
            /** Returns a [DataSchema.AnalyzeGeometryInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                activity: DataSchema.ActivitySlug,
                coordinates: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
                optional: DataSchema.AnalyzeGeometryInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.AnalyzeGeometryInput {
                val given = DataSchema.AnalyzeGeometryInput.Builder().apply(optional)
                return DataSchema.AnalyzeGeometryInput(
                    activity = activity,
                    coordinates = coordinates,
                    speed = given.slot2,
                )
            }
        }
    }

    /**
     * Buffered-area input combining a GeoJSON geometry with a radial buffer
     * distance in meters. The buffer expands the geometry into a search area for
     * the POI query.
     */
    @kotlinx.serialization.Serializable
    public class BufferedAreaInput private constructor(
        /**
         * GeoJSON LineString or Polygon used as the spine of the buffered area. It
         * carries 2 to 10,000 positions, a Polygon's holes counting toward that total,
         * and the line, or a Polygon's outer ring, is at most 500 km long. Any other
         * type, or a geometry outside those bounds, is VALIDATION_FAILED at or below
         * ["input", "buffered", "geometry"].
         */
        public val geometry: com.mountaya.client.GeoJsonGeometry,
        /**
         * Radial buffer distance in meters used to expand the geometry. Must be
         * between 1 and 5000 (VALIDATION_FAILED at ["input", "buffered", "bufferMeters"]
         * otherwise).
         */
        public val bufferMeters: kotlin.Double,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.BufferedAreaInput &&
                other.geometry == this.geometry &&
                other.bufferMeters == this.bufferMeters

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.geometry.hashCode()
            hash = 31 * hash + this.bufferMeters.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "BufferedAreaInput(" +
                "geometry=" + this.geometry +
                ", bufferMeters=" + this.bufferMeters +
                ")"

        /** Builder sets the optional properties of a [DataSchema.BufferedAreaInput], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [DataSchema.BufferedAreaInput]. */
        public companion object {
            /** Returns a [DataSchema.BufferedAreaInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                geometry: com.mountaya.client.GeoJsonGeometry,
                bufferMeters: kotlin.Double,
                optional: DataSchema.BufferedAreaInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.BufferedAreaInput {
                val given = DataSchema.BufferedAreaInput.Builder().apply(optional)
                return DataSchema.BufferedAreaInput(
                    geometry = geometry,
                    bufferMeters = bufferMeters,
                )
            }
        }
    }

    /** Input for computing directions between waypoints. */
    @kotlinx.serialization.Serializable
    public class DirectionsInput private constructor(
        /** Activity to use for routing. */
        public val activity: DataSchema.ActivitySlug,
        /**
         * Ordered list of waypoints as [longitude, latitude] coordinate pairs (WGS 84).
         * Must contain between 2 and 45 waypoints, and the straight-line polyline
         * through them, waypoint to waypoint, may be at most 100 km long; either refusal
         * is VALIDATION_FAILED at ["input", "waypoints"]. Each waypoint is exactly two
         * numbers, a longitude between -180 and 180 and a latitude between -90 and 90,
         * and one that is not is VALIDATION_FAILED at ["input", "waypoints", i]. The
         * count, the length and `speed` are checked before any single waypoint, so an
         * out-of-range waypoint that stretches the polyline past 100 km is first reported
         * at ["input", "waypoints"]. Prefer the fewest waypoints that pin the intended
         * route.
         *
         * A waypoint that no routable way reaches for the activity, or waypoints no
         * route connects, is refused with VALIDATION_FAILED at ["input", "waypoints"].
         * A request within the 100 km bound on the waypoints' line that the routing
         * engine still measures past it is VALIDATION_FAILED at ["input"], and its
         * message is the engine's own, in English whatever the Accept-Language header
         * asks for. Those are the routing engine's only answers that are the caller's
         * to fix: any other failure of the engine, its refusal of a parameter this API
         * built included, is INTERNAL_ERROR.
         * Example: [[6.8712, 45.9231], [6.9271, 45.9822]] (Chamonix to Argentière).
         */
        public val waypoints: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
        /**
         * Travel speed in km/h on LEVEL ground, a finite number greater than 0 (anything
         * else, NaN and Infinity included, is VALIDATION_FAILED at ["input", "speed"]).
         * When provided, it replaces the
         * selected activity's default as the flat pace the travel-time model starts
         * from.
         *
         * It is not the speed the route is travelled at: the model resolves a speed per
         * stretch from this pace and the ground (slower on a climb, faster on a
         * descent, and slower again over a coarse surface or a technical trail), caps
         * each stretch at the activity's maximum speed (see SpeedInfo), and integrates
         * the duration over that. Read `analysis.speedInfo` for what each stretch
         * resolved to. While no stretch reaches that cap, duration is inversely
         * proportional to this figure, so halving it doubles the duration.
         * Example: 4.5 for hiking, 25.0 for road cycling.
         */
        public val speed: kotlin.Double? = null,
        /**
         * Hazards to route around. Omitted or empty routes normally.
         *
         * Naming a hazard costs a second routing round-trip only when the first route
         * actually crosses it. Avoidance is best-effort: read `avoidance` on the result
         * to learn whether it was applied, and the analysis `advisories` to learn what
         * the returned route still crosses. A waypoint placed ON a hazardous way cannot
         * be routed around and yields `NO_ROUTE`. A re-route that fails on Mountaya's
         * side keeps the first route as `NO_ROUTE` too, and the field then carries an
         * INTERNAL_ERROR entry in `errors` beside its data; such a request is recorded
         * as a failure (5xx), so it counts toward neither the monthly allowance nor an
         * invoice.
         *
         * Routing around a hazard needs the per-stretch hazard data the routing engine
         * returns with a route, and a SKI_TOURING, CROSS_COUNTRY_SKIING or
         * SNOWSHOE_WALKING route carries none. For those three, `avoidance` is always
         * NOT_NEEDED and the advisories alone report what the route crosses.
         */
        public val avoid: kotlin.collections.List<DataSchema.RouteHazard>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.DirectionsInput &&
                other.activity == this.activity &&
                other.waypoints == this.waypoints &&
                other.speed == this.speed &&
                other.avoid == this.avoid

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.activity.hashCode()
            hash = 31 * hash + this.waypoints.hashCode()
            hash = 31 * hash + (this.speed?.hashCode() ?: 0)
            hash = 31 * hash + (this.avoid?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "DirectionsInput(" +
                "activity=" + this.activity +
                ", waypoints=" + this.waypoints +
                ", speed=" + this.speed +
                ", avoid=" + this.avoid +
                ")"

        /** Builder sets the optional properties of a [DataSchema.DirectionsInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: kotlin.Double? = null
            internal var slot3: kotlin.collections.List<DataSchema.RouteHazard>? = null

            /**
             * Travel speed in km/h on LEVEL ground, a finite number greater than 0 (anything
             * else, NaN and Infinity included, is VALIDATION_FAILED at ["input", "speed"]).
             * When provided, it replaces the
             * selected activity's default as the flat pace the travel-time model starts
             * from.
             *
             * It is not the speed the route is travelled at: the model resolves a speed per
             * stretch from this pace and the ground (slower on a climb, faster on a
             * descent, and slower again over a coarse surface or a technical trail), caps
             * each stretch at the activity's maximum speed (see SpeedInfo), and integrates
             * the duration over that. Read `analysis.speedInfo` for what each stretch
             * resolved to. While no stretch reaches that cap, duration is inversely
             * proportional to this figure, so halving it doubles the duration.
             * Example: 4.5 for hiking, 25.0 for road cycling.
             */
            public var speed: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Hazards to route around. Omitted or empty routes normally.
             *
             * Naming a hazard costs a second routing round-trip only when the first route
             * actually crosses it. Avoidance is best-effort: read `avoidance` on the result
             * to learn whether it was applied, and the analysis `advisories` to learn what
             * the returned route still crosses. A waypoint placed ON a hazardous way cannot
             * be routed around and yields `NO_ROUTE`. A re-route that fails on Mountaya's
             * side keeps the first route as `NO_ROUTE` too, and the field then carries an
             * INTERNAL_ERROR entry in `errors` beside its data; such a request is recorded
             * as a failure (5xx), so it counts toward neither the monthly allowance nor an
             * invoice.
             *
             * Routing around a hazard needs the per-stretch hazard data the routing engine
             * returns with a route, and a SKI_TOURING, CROSS_COUNTRY_SKIING or
             * SNOWSHOE_WALKING route carries none. For those three, `avoidance` is always
             * NOT_NEEDED and the advisories alone report what the route crosses.
             */
            public var avoid: kotlin.collections.List<DataSchema.RouteHazard>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [DataSchema.DirectionsInput]. */
        public companion object {
            /** Returns a [DataSchema.DirectionsInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                activity: DataSchema.ActivitySlug,
                waypoints: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
                optional: DataSchema.DirectionsInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.DirectionsInput {
                val given = DataSchema.DirectionsInput.Builder().apply(optional)
                return DataSchema.DirectionsInput(
                    activity = activity,
                    waypoints = waypoints,
                    speed = given.slot2,
                    avoid = given.slot3,
                )
            }
        }
    }

    /**
     * Which itinerary to prepare a download for, and in what file format.
     *
     * The itinerary must be one you can already read; nothing here widens access. Only
     * `format` and `variant` shape the file, so the same itinerary can be prepared
     * repeatedly in different formats without side effects. `variant` applies to the two
     * time-series formats only; supplying it with any other format is refused.
     */
    @kotlinx.serialization.Serializable
    public class ExportItineraryInput private constructor(
        /**
         * Identifier of the itinerary to export, a UUID in its canonical form of 36
         * characters with four hyphens. Any other value, the nil UUID included, is
         * refused with VALIDATION_FAILED at ["input", "id"].
         * Example: "0b6d1a4e-6d3f-4f7a-9c2e-1f0a5b8c3d21".
         */
        public val id: kotlin.String,
        /** File format to export. */
        public val format: DataSchema.ExportFormat,
        /**
         * File type of a time-series export. Applies to FIT and TCX only: supplying it
         * with any other format is VALIDATION_FAILED at ["input", "variant"] rather than
         * ignored. Defaults to COURSE when omitted with FIT or TCX.
         */
        public val variant: DataSchema.ExportVariant? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ExportItineraryInput &&
                other.id == this.id &&
                other.format == this.format &&
                other.variant == this.variant

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.format.hashCode()
            hash = 31 * hash + (this.variant?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ExportItineraryInput(" +
                "id=" + this.id +
                ", format=" + this.format +
                ", variant=" + this.variant +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ExportItineraryInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: DataSchema.ExportVariant? = null

            /**
             * File type of a time-series export. Applies to FIT and TCX only: supplying it
             * with any other format is VALIDATION_FAILED at ["input", "variant"] rather than
             * ignored. Defaults to COURSE when omitted with FIT or TCX.
             */
            public var variant: DataSchema.ExportVariant?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.ExportItineraryInput]. */
        public companion object {
            /** Returns a [DataSchema.ExportItineraryInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                format: DataSchema.ExportFormat,
                optional: DataSchema.ExportItineraryInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ExportItineraryInput {
                val given = DataSchema.ExportItineraryInput.Builder().apply(optional)
                return DataSchema.ExportItineraryInput(
                    id = id,
                    format = format,
                    variant = given.slot2,
                )
            }
        }
    }

    /** Input for a forward geocoding query. */
    @kotlinx.serialization.Serializable
    public class GeocodeInput private constructor(
        /**
         * User-typed search text. Must be between 1 and 200 characters after trimming
         * (VALIDATION_FAILED at ["input", "query"] otherwise).
         * Example: "Chamonix" or "Mont Blanc".
         */
        public val query: kotlin.String,
        /**
         * Language of the returned names. Only EN and FR are localized; any other
         * language reads English. Defaults to English when omitted.
         */
        public val language: DataSchema.Language? = null,
        /**
         * Maximum number of places to return. Must be between 1 and 50; defaults to 10.
         * Omitting it returns at most the default rather than everything.
         *
         * Left out, the server applies its default: `10`.
         */
        public val limit: kotlin.Int? = null,
        /**
         * [longitude, latitude] center used to bias ranking toward nearby results:
         * exactly two numbers, in range. [0, 0] and an empty list both mean no focus.
         * Example: [6.8712, 45.9231] to bias results around Chamonix.
         */
        public val focus: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * [minLongitude, minLatitude, maxLongitude, maxLatitude] bounding box to
         * restrict the search area: exactly four values, each minimum strictly below its
         * maximum, and each axis spanning at most 90 degrees. [0, 0, 0, 0] and an empty
         * list both mean no box.
         */
        public val bbox: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Layers to restrict results to, at most 10. When omitted, all layers are
         * eligible. Like `categories`, it runs on the places the geocoder returned for
         * `limit`, so it can leave fewer than `limit`, or none, even where more places
         * of those layers exist: POSTCODE does not narrow the geocoder's own search,
         * and POI and NATURAL narrow it only to the one set both belong to. Raise
         * `limit` when a narrow filter leaves too few.
         */
        public val layers: kotlin.collections.List<DataSchema.GeocodeLayer>? = null,
        /**
         * Boolean-per-category filter applied to the geocoded place output. When
         * omitted, every category is allowed. It runs on the places the geocoder
         * returned for `limit`, so it can leave fewer than `limit`.
         */
        public val categories: DataSchema.PlaceCategoriesInput? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.GeocodeInput &&
                other.query == this.query &&
                other.language == this.language &&
                other.limit == this.limit &&
                other.focus == this.focus &&
                other.bbox == this.bbox &&
                other.layers == this.layers &&
                other.categories == this.categories

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.query.hashCode()
            hash = 31 * hash + (this.language?.hashCode() ?: 0)
            hash = 31 * hash + (this.limit?.hashCode() ?: 0)
            hash = 31 * hash + (this.focus?.hashCode() ?: 0)
            hash = 31 * hash + (this.bbox?.hashCode() ?: 0)
            hash = 31 * hash + (this.layers?.hashCode() ?: 0)
            hash = 31 * hash + (this.categories?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "GeocodeInput(" +
                "query=" + this.query +
                ", language=" + this.language +
                ", limit=" + this.limit +
                ", focus=" + this.focus +
                ", bbox=" + this.bbox +
                ", layers=" + this.layers +
                ", categories=" + this.categories +
                ")"

        /** Builder sets the optional properties of a [DataSchema.GeocodeInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: DataSchema.Language? = null
            internal var slot2: kotlin.Int? = null
            internal var slot3: kotlin.collections.List<kotlin.Double>? = null
            internal var slot4: kotlin.collections.List<kotlin.Double>? = null
            internal var slot5: kotlin.collections.List<DataSchema.GeocodeLayer>? = null
            internal var slot6: DataSchema.PlaceCategoriesInput? = null

            /**
             * Language of the returned names. Only EN and FR are localized; any other
             * language reads English. Defaults to English when omitted.
             */
            public var language: DataSchema.Language?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Maximum number of places to return. Must be between 1 and 50; defaults to 10.
             * Omitting it returns at most the default rather than everything.
             *
             * Left out, the server applies its default: `10`.
             */
            public var limit: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * [longitude, latitude] center used to bias ranking toward nearby results:
             * exactly two numbers, in range. [0, 0] and an empty list both mean no focus.
             * Example: [6.8712, 45.9231] to bias results around Chamonix.
             */
            public var focus: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * [minLongitude, minLatitude, maxLongitude, maxLatitude] bounding box to
             * restrict the search area: exactly four values, each minimum strictly below its
             * maximum, and each axis spanning at most 90 degrees. [0, 0, 0, 0] and an empty
             * list both mean no box.
             */
            public var bbox: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Layers to restrict results to, at most 10. When omitted, all layers are
             * eligible. Like `categories`, it runs on the places the geocoder returned for
             * `limit`, so it can leave fewer than `limit`, or none, even where more places
             * of those layers exist: POSTCODE does not narrow the geocoder's own search,
             * and POI and NATURAL narrow it only to the one set both belong to. Raise
             * `limit` when a narrow filter leaves too few.
             */
            public var layers: kotlin.collections.List<DataSchema.GeocodeLayer>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Boolean-per-category filter applied to the geocoded place output. When
             * omitted, every category is allowed. It runs on the places the geocoder
             * returned for `limit`, so it can leave fewer than `limit`.
             */
            public var categories: DataSchema.PlaceCategoriesInput?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }
        }

        /** Builds a [DataSchema.GeocodeInput]. */
        public companion object {
            /** Returns a [DataSchema.GeocodeInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                query: kotlin.String,
                optional: DataSchema.GeocodeInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.GeocodeInput {
                val given = DataSchema.GeocodeInput.Builder().apply(optional)
                return DataSchema.GeocodeInput(
                    query = query,
                    language = given.slot1,
                    limit = given.slot2,
                    focus = given.slot3,
                    bbox = given.slot4,
                    layers = given.slot5,
                    categories = given.slot6,
                )
            }
        }
    }

    /** Input for computing isochrones. */
    @kotlinx.serialization.Serializable
    public class IsochronesInput private constructor(
        /**
         * Activity to use for routing. BACKCOUNTRY_SKIING has no routing, and
         * GRAVEL_BIKE and MOUNTAIN_BIKE are unavailable: all three are
         * VALIDATION_FAILED at ["input", "activity"].
         */
        public val activity: DataSchema.ActivitySlug,
        /**
         * Starting locations as [longitude, latitude] coordinate pairs (WGS 84), one
         * or two (none, or more than two, is VALIDATION_FAILED at
         * ["input", "locations"]). Each is exactly two numbers, a longitude between -180 and 180 and a
         * latitude between -90 and 90, and one that is not is VALIDATION_FAILED at
         * ["input", "locations", i]. Each location is placed on the activity's network
         * as a `directions` waypoint is, within the same radius (400 meters for most
         * activities) and on the ways the activity routes on, so a way it avoids, a
         * ferry line for one, never places it. A location no such way lies near is
         * VALIDATION_FAILED at ["input", "locations", i], one entry per such location:
         * a point in a lake or out at sea, for example.
         * Example: [[6.8712, 45.9231]] (single origin at Chamonix).
         */
        public val locations: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
        /**
         * Range values in seconds (TIME) or meters (DISTANCE), one to six (none, or
         * more than six, is VALIDATION_FAILED at ["input", "range"]). Each is greater
         * than zero and at most 7,200 seconds or 25,000 meters, and one that is not is
         * VALIDATION_FAILED at ["input", "range", i]. Multiple values produce
         * concentric isochrones.
         * Example: [600, 1200, 1800] for 10, 20, 30 minute isochrones (TIME).
         * Example: [5000, 10000] for 5 km and 10 km isochrones (DISTANCE).
         */
        public val range: kotlin.collections.List<kotlin.Double>,
        /**
         * Whether the range is expressed in time or distance.
         *
         * A TIME budget is measured with the routing engine's own travel-time model,
         * not the terrain-aware model behind `duration` on `directions`, `suggestions`
         * and `analyzeGeometry`, so an isochrone's edge and a route's duration are not
         * interchangeable figures.
         */
        public val rangeType: DataSchema.RangeType,
        /**
         * Travel speed in km/h, a finite number greater than 0 (anything else, NaN and
         * Infinity included, is VALIDATION_FAILED at ["input", "speed"]). When
         * provided, the time budget is stretched by the ratio of this pace to the
         * routing engine's own pace for the activity, 5 km/h for the activities on foot
         * and on skis and 20 km/h for ROAD_CYCLING, so the polygon covers the ground
         * this pace reaches within the requested range. The stretched budget is held to
         * the same 7,200 seconds as a range: a speed that takes any range value past it,
         * once multiplied by that ratio, is VALIDATION_FAILED at ["input", "speed"]. At
         * 10 km/h on foot, for example, every range value must be at most 3,600 seconds.
         * Only meaningful when rangeType is TIME: a distance range is speed-independent,
         * and pairing the two is VALIDATION_FAILED at ["input", "speed"].
         */
        public val speed: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.IsochronesInput &&
                other.activity == this.activity &&
                other.locations == this.locations &&
                other.range == this.range &&
                other.rangeType == this.rangeType &&
                other.speed == this.speed

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.activity.hashCode()
            hash = 31 * hash + this.locations.hashCode()
            hash = 31 * hash + this.range.hashCode()
            hash = 31 * hash + this.rangeType.hashCode()
            hash = 31 * hash + (this.speed?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "IsochronesInput(" +
                "activity=" + this.activity +
                ", locations=" + this.locations +
                ", range=" + this.range +
                ", rangeType=" + this.rangeType +
                ", speed=" + this.speed +
                ")"

        /** Builder sets the optional properties of a [DataSchema.IsochronesInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot4: kotlin.Double? = null

            /**
             * Travel speed in km/h, a finite number greater than 0 (anything else, NaN and
             * Infinity included, is VALIDATION_FAILED at ["input", "speed"]). When
             * provided, the time budget is stretched by the ratio of this pace to the
             * routing engine's own pace for the activity, 5 km/h for the activities on foot
             * and on skis and 20 km/h for ROAD_CYCLING, so the polygon covers the ground
             * this pace reaches within the requested range. The stretched budget is held to
             * the same 7,200 seconds as a range: a speed that takes any range value past it,
             * once multiplied by that ratio, is VALIDATION_FAILED at ["input", "speed"]. At
             * 10 km/h on foot, for example, every range value must be at most 3,600 seconds.
             * Only meaningful when rangeType is TIME: a distance range is speed-independent,
             * and pairing the two is VALIDATION_FAILED at ["input", "speed"].
             */
            public var speed: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }
        }

        /** Builds a [DataSchema.IsochronesInput]. */
        public companion object {
            /** Returns a [DataSchema.IsochronesInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                activity: DataSchema.ActivitySlug,
                locations: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
                range: kotlin.collections.List<kotlin.Double>,
                rangeType: DataSchema.RangeType,
                optional: DataSchema.IsochronesInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.IsochronesInput {
                val given = DataSchema.IsochronesInput.Builder().apply(optional)
                return DataSchema.IsochronesInput(
                    activity = activity,
                    locations = locations,
                    range = range,
                    rangeType = rangeType,
                    speed = given.slot4,
                )
            }
        }
    }

    /**
     * Which itinerary to read.
     *
     * The identifier is the only input: the whole itinerary is returned with its ordered
     * segments and waypoints, and there is no field selection or windowing to declare
     * here. An id you may not read answers NOT_FOUND rather than FORBIDDEN, so an
     * unknown id and an inaccessible one are indistinguishable by design.
     */
    @kotlinx.serialization.Serializable
    public class ItineraryInput private constructor(
        /**
         * Identifier of the itinerary to read, a UUID in its canonical form of 36
         * characters with four hyphens. Any other value, the nil UUID included, is
         * refused with VALIDATION_FAILED at ["input", "id"].
         * Example: "0b6d1a4e-6d3f-4f7a-9c2e-1f0a5b8c3d21".
         */
        public val id: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ItineraryInput &&
                other.id == this.id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ItineraryInput(" +
                "id=" + this.id +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ItineraryInput], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [DataSchema.ItineraryInput]. */
        public companion object {
            /** Returns a [DataSchema.ItineraryInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                optional: DataSchema.ItineraryInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ItineraryInput {
                val given = DataSchema.ItineraryInput.Builder().apply(optional)
                return DataSchema.ItineraryInput(
                    id = id,
                )
            }
        }
    }

    /** Input for computing a time-distance matrix. */
    @kotlinx.serialization.Serializable
    public class MatrixInput private constructor(
        /**
         * Activity to use for routing. BACKCOUNTRY_SKIING has no routing, and
         * GRAVEL_BIKE and MOUNTAIN_BIKE are unavailable: all three are
         * VALIDATION_FAILED at ["input", "activity"].
         */
        public val activity: DataSchema.ActivitySlug,
        /**
         * Locations as [longitude, latitude] coordinate pairs (WGS 84), 2 to 50 (fewer
         * or more is VALIDATION_FAILED at ["input", "locations"]). Each is exactly two
         * numbers, a longitude between -180 and 180 and a latitude between -90 and 90,
         * and one that is not is VALIDATION_FAILED at ["input", "locations", i]. No two
         * locations may lie more than 20 km apart in a straight line, and a matrix two
         * of whose locations do is VALIDATION_FAILED at ["input", "locations"]: past
         * that span the routing engine cannot search between them. A location the
         * routing engine cannot place on the network answers its pairs null rather
         * than failing, and so does a location on ways no other location connects to,
         * an enclosed site for one: its pairs with the rest of the network are null, as
         * no route joins them, and the pairs between two locations on the same enclosed
         * site are answered. A matrix of many locations over a dense network, whose
         * search the engine cannot hold at once, is computed one source at a time and
         * answered exactly as the whole matrix would have been, recorded as one request
         * with the elements asked for, null cells included; one holding such an
         * enclosed location takes a few seconds more, and under load the engine is
         * waited for within the request's time budget. Any failure of the engine within those
         * bounds is INTERNAL_ERROR, never the caller's to fix: a ceiling of its own set
         * below them, or a source whose own search it cannot hold either.
         * Example: [[6.8694, 45.9237], [6.9271, 45.9822], [6.7997, 45.8906]] (Chamonix, Argentière, Les Houches).
         */
        public val locations: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
        /**
         * Indices of locations to use as sources. When omitted, all locations are used.
         * At most 50 (more is VALIDATION_FAILED at ["input", "sources"]). Each must be
         * the index of one of the locations, and one that is not is VALIDATION_FAILED
         * at ["input", "sources", i]. A repeated index is accepted.
         * Example: [0] to compute only from the first location to all others.
         */
        public val sources: kotlin.collections.List<kotlin.Int>? = null,
        /**
         * Indices of locations to use as destinations. When omitted, all locations are
         * used. At most 50 (more is VALIDATION_FAILED at ["input", "destinations"]).
         * Each must be the index of one of the locations, and one that is not is
         * VALIDATION_FAILED at ["input", "destinations", i]. A repeated index is
         * accepted.
         * Example: [0] to compute only from all locations to the first one.
         */
        public val destinations: kotlin.collections.List<kotlin.Int>? = null,
        /**
         * Metrics to compute. At least one metric is required (an empty list is
         * VALIDATION_FAILED at ["input", "metrics"]).
         */
        public val metrics: kotlin.collections.List<DataSchema.MatrixMetric>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.MatrixInput &&
                other.activity == this.activity &&
                other.locations == this.locations &&
                other.sources == this.sources &&
                other.destinations == this.destinations &&
                other.metrics == this.metrics

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.activity.hashCode()
            hash = 31 * hash + this.locations.hashCode()
            hash = 31 * hash + (this.sources?.hashCode() ?: 0)
            hash = 31 * hash + (this.destinations?.hashCode() ?: 0)
            hash = 31 * hash + this.metrics.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "MatrixInput(" +
                "activity=" + this.activity +
                ", locations=" + this.locations +
                ", sources=" + this.sources +
                ", destinations=" + this.destinations +
                ", metrics=" + this.metrics +
                ")"

        /** Builder sets the optional properties of a [DataSchema.MatrixInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: kotlin.collections.List<kotlin.Int>? = null
            internal var slot3: kotlin.collections.List<kotlin.Int>? = null

            /**
             * Indices of locations to use as sources. When omitted, all locations are used.
             * At most 50 (more is VALIDATION_FAILED at ["input", "sources"]). Each must be
             * the index of one of the locations, and one that is not is VALIDATION_FAILED
             * at ["input", "sources", i]. A repeated index is accepted.
             * Example: [0] to compute only from the first location to all others.
             */
            public var sources: kotlin.collections.List<kotlin.Int>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Indices of locations to use as destinations. When omitted, all locations are
             * used. At most 50 (more is VALIDATION_FAILED at ["input", "destinations"]).
             * Each must be the index of one of the locations, and one that is not is
             * VALIDATION_FAILED at ["input", "destinations", i]. A repeated index is
             * accepted.
             * Example: [0] to compute only from all locations to the first one.
             */
            public var destinations: kotlin.collections.List<kotlin.Int>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [DataSchema.MatrixInput]. */
        public companion object {
            /** Returns a [DataSchema.MatrixInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                activity: DataSchema.ActivitySlug,
                locations: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
                metrics: kotlin.collections.List<DataSchema.MatrixMetric>,
                optional: DataSchema.MatrixInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.MatrixInput {
                val given = DataSchema.MatrixInput.Builder().apply(optional)
                return DataSchema.MatrixInput(
                    activity = activity,
                    locations = locations,
                    sources = given.slot2,
                    destinations = given.slot3,
                    metrics = metrics,
                )
            }
        }
    }

    /**
     * Boolean-per-category filter applied to geocoded place output. Each field
     * defaults to true, so omitting the input yields raw geocoder results, which is
     * useful for agentic workflows and third-party API consumers. An interactive map
     * UI flips categories to false to drop ones it does not want surfaced.
     */
    @kotlinx.serialization.Serializable
    public class PlaceCategoriesInput private constructor(
        /**
         * Include populated places like cities, towns, villages, hamlets, and suburbs. Defaults to true.
         *
         * Left out, the server applies its default: `true`.
         */
        public val populatedPlace: kotlin.Boolean? = null,
        /**
         * Include administrative boundaries like regions, counties, departments, districts, and localities. Defaults to true.
         *
         * Left out, the server applies its default: `true`.
         */
        public val administrativeArea: kotlin.Boolean? = null,
        /**
         * Include country-level results such as France or Switzerland. Defaults to true.
         *
         * Left out, the server applies its default: `true`.
         */
        public val country: kotlin.Boolean? = null,
        /**
         * Include natural features like mountain peaks, lakes, glaciers, rivers, and forests. Defaults to true.
         *
         * Left out, the server applies its default: `true`.
         */
        public val naturalFeature: kotlin.Boolean? = null,
        /**
         * Include transit stations like train stations, bus terminals, airports, and cable car stations. Defaults to true.
         *
         * Left out, the server applies its default: `true`.
         */
        public val transitStation: kotlin.Boolean? = null,
        /**
         * Include amenities like cafes, restaurants, shops, tourist attractions, and leisure venues. Defaults to true.
         *
         * Left out, the server applies its default: `true`.
         */
        public val amenity: kotlin.Boolean? = null,
        /**
         * Include addresses like streets, house numbers, and postcodes. Defaults to true.
         *
         * Left out, the server applies its default: `true`.
         */
        public val address: kotlin.Boolean? = null,
        /**
         * Include the catch-all bucket for places that do not fit any of the other categories. Defaults to true.
         *
         * Left out, the server applies its default: `true`.
         */
        public val other: kotlin.Boolean? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.PlaceCategoriesInput &&
                other.populatedPlace == this.populatedPlace &&
                other.administrativeArea == this.administrativeArea &&
                other.country == this.country &&
                other.naturalFeature == this.naturalFeature &&
                other.transitStation == this.transitStation &&
                other.amenity == this.amenity &&
                other.address == this.address &&
                other.other == this.other

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.populatedPlace?.hashCode() ?: 0)
            hash = 31 * hash + (this.administrativeArea?.hashCode() ?: 0)
            hash = 31 * hash + (this.country?.hashCode() ?: 0)
            hash = 31 * hash + (this.naturalFeature?.hashCode() ?: 0)
            hash = 31 * hash + (this.transitStation?.hashCode() ?: 0)
            hash = 31 * hash + (this.amenity?.hashCode() ?: 0)
            hash = 31 * hash + (this.address?.hashCode() ?: 0)
            hash = 31 * hash + (this.other?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PlaceCategoriesInput(" +
                "populatedPlace=" + this.populatedPlace +
                ", administrativeArea=" + this.administrativeArea +
                ", country=" + this.country +
                ", naturalFeature=" + this.naturalFeature +
                ", transitStation=" + this.transitStation +
                ", amenity=" + this.amenity +
                ", address=" + this.address +
                ", other=" + this.other +
                ")"

        /** Builder sets the optional properties of a [DataSchema.PlaceCategoriesInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Boolean? = null
            internal var slot1: kotlin.Boolean? = null
            internal var slot2: kotlin.Boolean? = null
            internal var slot3: kotlin.Boolean? = null
            internal var slot4: kotlin.Boolean? = null
            internal var slot5: kotlin.Boolean? = null
            internal var slot6: kotlin.Boolean? = null
            internal var slot7: kotlin.Boolean? = null

            /**
             * Include populated places like cities, towns, villages, hamlets, and suburbs. Defaults to true.
             *
             * Left out, the server applies its default: `true`.
             */
            public var populatedPlace: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Include administrative boundaries like regions, counties, departments, districts, and localities. Defaults to true.
             *
             * Left out, the server applies its default: `true`.
             */
            public var administrativeArea: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Include country-level results such as France or Switzerland. Defaults to true.
             *
             * Left out, the server applies its default: `true`.
             */
            public var country: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Include natural features like mountain peaks, lakes, glaciers, rivers, and forests. Defaults to true.
             *
             * Left out, the server applies its default: `true`.
             */
            public var naturalFeature: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Include transit stations like train stations, bus terminals, airports, and cable car stations. Defaults to true.
             *
             * Left out, the server applies its default: `true`.
             */
            public var transitStation: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Include amenities like cafes, restaurants, shops, tourist attractions, and leisure venues. Defaults to true.
             *
             * Left out, the server applies its default: `true`.
             */
            public var amenity: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Include addresses like streets, house numbers, and postcodes. Defaults to true.
             *
             * Left out, the server applies its default: `true`.
             */
            public var address: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Include the catch-all bucket for places that do not fit any of the other categories. Defaults to true.
             *
             * Left out, the server applies its default: `true`.
             */
            public var other: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }
        }

        /** Builds a [DataSchema.PlaceCategoriesInput]. */
        public companion object {
            /** Returns a [DataSchema.PlaceCategoriesInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.PlaceCategoriesInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.PlaceCategoriesInput {
                val given = DataSchema.PlaceCategoriesInput.Builder().apply(optional)
                return DataSchema.PlaceCategoriesInput(
                    populatedPlace = given.slot0,
                    administrativeArea = given.slot1,
                    country = given.slot2,
                    naturalFeature = given.slot3,
                    transitStation = given.slot4,
                    amenity = given.slot5,
                    address = given.slot6,
                    other = given.slot7,
                )
            }
        }
    }

    /** Input for a points-of-interest query. */
    @kotlinx.serialization.Serializable
    public class PoisInput private constructor(
        /**
         * [minLongitude, minLatitude, maxLongitude, maxLatitude] bounding box to
         * restrict the search area: exactly four values, each minimum strictly below its
         * maximum, and each axis spanning at most 90 degrees. Mutually exclusive with
         * buffered: sending both is VALIDATION_FAILED at ["input", "bbox"].
         * [0, 0, 0, 0] and an empty list both mean no box.
         */
        public val bbox: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Polyline or polygon geometry expanded by a buffer in meters. Mutually
         * exclusive with bbox. POIs are returned only when within the buffer and
         * annotated with their distance to the geometry.
         *
         * A buffered area is asked as the bounding box around it, which the geocoder
         * ranks and caps at `limit` before the buffer is applied: along a long or
         * diagonal line, most of that box lies outside the buffer, and the answer can
         * hold fewer POIs than the area does. Ask for the buffered area in shorter
         * parts to cover it in full.
         */
        public val buffered: DataSchema.BufferedAreaInput? = null,
        /** POI categories to include. Required. Between 1 and 20 categories. */
        public val categories: kotlin.collections.List<DataSchema.POICategory>,
        /**
         * Language used to bias the result names. Defaults to English when omitted. Only
         * EN and FR are localized; any other language reads English.
         */
        public val language: DataSchema.Language? = null,
        /**
         * Maximum number of POIs to return. Must be between 1 and 50; defaults to 50, the
         * ceiling itself, because a POI sweep of an area is normally wanted whole.
         *
         * Left out, the server applies its default: `50`.
         */
        public val limit: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.PoisInput &&
                other.bbox == this.bbox &&
                other.buffered == this.buffered &&
                other.categories == this.categories &&
                other.language == this.language &&
                other.limit == this.limit

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.bbox?.hashCode() ?: 0)
            hash = 31 * hash + (this.buffered?.hashCode() ?: 0)
            hash = 31 * hash + this.categories.hashCode()
            hash = 31 * hash + (this.language?.hashCode() ?: 0)
            hash = 31 * hash + (this.limit?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PoisInput(" +
                "bbox=" + this.bbox +
                ", buffered=" + this.buffered +
                ", categories=" + this.categories +
                ", language=" + this.language +
                ", limit=" + this.limit +
                ")"

        /** Builder sets the optional properties of a [DataSchema.PoisInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<kotlin.Double>? = null
            internal var slot1: DataSchema.BufferedAreaInput? = null
            internal var slot3: DataSchema.Language? = null
            internal var slot4: kotlin.Int? = null

            /**
             * [minLongitude, minLatitude, maxLongitude, maxLatitude] bounding box to
             * restrict the search area: exactly four values, each minimum strictly below its
             * maximum, and each axis spanning at most 90 degrees. Mutually exclusive with
             * buffered: sending both is VALIDATION_FAILED at ["input", "bbox"].
             * [0, 0, 0, 0] and an empty list both mean no box.
             */
            public var bbox: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Polyline or polygon geometry expanded by a buffer in meters. Mutually
             * exclusive with bbox. POIs are returned only when within the buffer and
             * annotated with their distance to the geometry.
             *
             * A buffered area is asked as the bounding box around it, which the geocoder
             * ranks and caps at `limit` before the buffer is applied: along a long or
             * diagonal line, most of that box lies outside the buffer, and the answer can
             * hold fewer POIs than the area does. Ask for the buffered area in shorter
             * parts to cover it in full.
             */
            public var buffered: DataSchema.BufferedAreaInput?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Language used to bias the result names. Defaults to English when omitted. Only
             * EN and FR are localized; any other language reads English.
             */
            public var language: DataSchema.Language?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Maximum number of POIs to return. Must be between 1 and 50; defaults to 50, the
             * ceiling itself, because a POI sweep of an area is normally wanted whole.
             *
             * Left out, the server applies its default: `50`.
             */
            public var limit: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }
        }

        /** Builds a [DataSchema.PoisInput]. */
        public companion object {
            /** Returns a [DataSchema.PoisInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                categories: kotlin.collections.List<DataSchema.POICategory>,
                optional: DataSchema.PoisInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.PoisInput {
                val given = DataSchema.PoisInput.Builder().apply(optional)
                return DataSchema.PoisInput(
                    bbox = given.slot0,
                    buffered = given.slot1,
                    categories = categories,
                    language = given.slot3,
                    limit = given.slot4,
                )
            }
        }
    }

    /** Input for a reverse geocoding query. */
    @kotlinx.serialization.Serializable
    public class ReverseGeocodeInput private constructor(
        /**
         * [longitude, latitude] anchor point to reverse-geocode: exactly two numbers, in
         * range (VALIDATION_FAILED at ["input", "point"] otherwise).
         * Example: [6.8712, 45.9231] for Chamonix.
         */
        public val point: kotlin.collections.List<kotlin.Double>,
        /**
         * Language of the returned names. Only EN and FR are localized; any other
         * language reads English. Defaults to English when omitted.
         */
        public val language: DataSchema.Language? = null,
        /**
         * Maximum search radius in meters around the anchor point.
         * Must be between 1 and 5000.
         *
         * Left out, the server applies its default: `500`.
         */
        public val radiusMeters: kotlin.Int? = null,
        /**
         * Maximum number of places to return. Must be between 1 and 50; defaults to 1,
         * since a reverse lookup asks which single place a coordinate falls in.
         *
         * Left out, the server applies its default: `1`.
         */
        public val limit: kotlin.Int? = null,
        /**
         * Layers to restrict results to, at most 10. When omitted, all layers are
         * eligible. Like `categories`, it runs on the places the geocoder returned for
         * `limit`, so it can leave fewer than `limit`, or none, even where more places
         * of those layers exist: POSTCODE does not narrow the geocoder's own search,
         * and POI and NATURAL narrow it only to the one set both belong to. Raise
         * `limit` when a narrow filter leaves too few.
         */
        public val layers: kotlin.collections.List<DataSchema.GeocodeLayer>? = null,
        /**
         * Boolean-per-category filter applied to the geocoded place output. When
         * omitted, every category is allowed. It runs on the places the geocoder
         * returned for `limit`, so it can leave fewer than `limit`.
         */
        public val categories: DataSchema.PlaceCategoriesInput? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.ReverseGeocodeInput &&
                other.point == this.point &&
                other.language == this.language &&
                other.radiusMeters == this.radiusMeters &&
                other.limit == this.limit &&
                other.layers == this.layers &&
                other.categories == this.categories

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.point.hashCode()
            hash = 31 * hash + (this.language?.hashCode() ?: 0)
            hash = 31 * hash + (this.radiusMeters?.hashCode() ?: 0)
            hash = 31 * hash + (this.limit?.hashCode() ?: 0)
            hash = 31 * hash + (this.layers?.hashCode() ?: 0)
            hash = 31 * hash + (this.categories?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ReverseGeocodeInput(" +
                "point=" + this.point +
                ", language=" + this.language +
                ", radiusMeters=" + this.radiusMeters +
                ", limit=" + this.limit +
                ", layers=" + this.layers +
                ", categories=" + this.categories +
                ")"

        /** Builder sets the optional properties of a [DataSchema.ReverseGeocodeInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: DataSchema.Language? = null
            internal var slot2: kotlin.Int? = null
            internal var slot3: kotlin.Int? = null
            internal var slot4: kotlin.collections.List<DataSchema.GeocodeLayer>? = null
            internal var slot5: DataSchema.PlaceCategoriesInput? = null

            /**
             * Language of the returned names. Only EN and FR are localized; any other
             * language reads English. Defaults to English when omitted.
             */
            public var language: DataSchema.Language?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Maximum search radius in meters around the anchor point.
             * Must be between 1 and 5000.
             *
             * Left out, the server applies its default: `500`.
             */
            public var radiusMeters: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Maximum number of places to return. Must be between 1 and 50; defaults to 1,
             * since a reverse lookup asks which single place a coordinate falls in.
             *
             * Left out, the server applies its default: `1`.
             */
            public var limit: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Layers to restrict results to, at most 10. When omitted, all layers are
             * eligible. Like `categories`, it runs on the places the geocoder returned for
             * `limit`, so it can leave fewer than `limit`, or none, even where more places
             * of those layers exist: POSTCODE does not narrow the geocoder's own search,
             * and POI and NATURAL narrow it only to the one set both belong to. Raise
             * `limit` when a narrow filter leaves too few.
             */
            public var layers: kotlin.collections.List<DataSchema.GeocodeLayer>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Boolean-per-category filter applied to the geocoded place output. When
             * omitted, every category is allowed. It runs on the places the geocoder
             * returned for `limit`, so it can leave fewer than `limit`.
             */
            public var categories: DataSchema.PlaceCategoriesInput?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [DataSchema.ReverseGeocodeInput]. */
        public companion object {
            /** Returns a [DataSchema.ReverseGeocodeInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                point: kotlin.collections.List<kotlin.Double>,
                optional: DataSchema.ReverseGeocodeInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.ReverseGeocodeInput {
                val given = DataSchema.ReverseGeocodeInput.Builder().apply(optional)
                return DataSchema.ReverseGeocodeInput(
                    point = point,
                    language = given.slot1,
                    radiusMeters = given.slot2,
                    limit = given.slot3,
                    layers = given.slot4,
                    categories = given.slot5,
                )
            }
        }
    }

    /** Input for the unified search query. */
    @kotlinx.serialization.Serializable
    public class SearchInput private constructor(
        /**
         * User-typed search text, at most 200 characters after trimming. Every target
         * except pois also needs at least one character once trimmed; the pois target
         * accepts an empty query and lists POIs of the requested categories.
         */
        public val query: kotlin.String,
        /**
         * Language of the names the places, pois and ways targets return. Only EN and FR
         * are localized; any other language reads English. The itineraries, collections
         * and zones targets ignore it and return names as they were written. Defaults to
         * English when omitted.
         */
        public val language: DataSchema.Language? = null,
        /**
         * Per-target maximum number of results. Must be between 1 and 50; defaults to 8.
         * The cap applies to each enabled target independently, so the whole result can
         * hold up to `limit` × the number of enabled targets, which is why the default is
         * small. Omitting it caps each target at the default rather than returning
         * everything. A value outside
         * the range is refused by every enabled target, each in its own `errors` entry.
         *
         * Left out, the server applies its default: `8`.
         */
        public val limit: kotlin.Int? = null,
        /**
         * [longitude, latitude] center used to bias ranking toward nearby results for
         * the places, pois, and ways targets: exactly two numbers, in range, or the
         * request fails with VALIDATION_FAILED at ["input", "focus"]. [0, 0] and an empty
         * list both mean no focus.
         */
        public val focus: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * [minLongitude, minLatitude, maxLongitude, maxLatitude] bounding box used to
         * restrict the search area for the places, pois, and ways targets. [0, 0, 0, 0]
         * and an empty list both mean no box; any count other than four fails the
         * request with VALIDATION_FAILED at ["input", "bbox"]. Each of those three
         * targets also requires values in range, each minimum strictly below its maximum
         * and each axis spanning at most 90 degrees, and reports a box that breaks those
         * rules in `errors`.
         */
        public val bbox: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Activity slug used to filter the ways target: a way the activity cannot use is
         * left out, and the order of the rest is unchanged. Ignored unless
         * targets.ways = true.
         */
        public val activity: DataSchema.ActivitySlug? = null,
        /**
         * Per-target enable flags. Every field defaults to false; at least one must be
         * true for the query to execute work. When no flag is true, an empty result is
         * returned and no usage is recorded, so the request counts toward neither the
         * monthly allowance nor an invoice, though it still counts toward the
         * per-minute rate limit.
         */
        public val targets: DataSchema.SearchTargetsInput,
        /**
         * Optional POI categories filter applied to the pois target. When omitted,
         * every category is searched. Ignored unless targets.pois = true.
         */
        public val pois: kotlin.collections.List<DataSchema.POICategory>? = null,
        /**
         * Optional boolean-per-category filter applied to the places target. When
         * omitted, every category is allowed. Ignored unless targets.places = true.
         */
        public val categories: DataSchema.PlaceCategoriesInput? = null,
        /**
         * Opt-in to routing-graph verification for the ways target. When true, each
         * candidate way is cross-checked against the same geometry analysis
         * `analyzeGeometry` performs, and an activity for which that analysis finds a
         * stretch it cannot classify is dropped from the way's `activityCompatible`. An
         * activity that could not be checked is kept: a way with no polyline, an
         * analysis that failed, or an activity with no surface analysis, such as the
         * winter ones. Materially increases latency; ignored unless targets.ways = true.
         *
         * Left out, the server applies its default: `false`.
         */
        public val verifyWays: kotlin.Boolean? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SearchInput &&
                other.query == this.query &&
                other.language == this.language &&
                other.limit == this.limit &&
                other.focus == this.focus &&
                other.bbox == this.bbox &&
                other.activity == this.activity &&
                other.targets == this.targets &&
                other.pois == this.pois &&
                other.categories == this.categories &&
                other.verifyWays == this.verifyWays

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.query.hashCode()
            hash = 31 * hash + (this.language?.hashCode() ?: 0)
            hash = 31 * hash + (this.limit?.hashCode() ?: 0)
            hash = 31 * hash + (this.focus?.hashCode() ?: 0)
            hash = 31 * hash + (this.bbox?.hashCode() ?: 0)
            hash = 31 * hash + (this.activity?.hashCode() ?: 0)
            hash = 31 * hash + this.targets.hashCode()
            hash = 31 * hash + (this.pois?.hashCode() ?: 0)
            hash = 31 * hash + (this.categories?.hashCode() ?: 0)
            hash = 31 * hash + (this.verifyWays?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SearchInput(" +
                "query=" + this.query +
                ", language=" + this.language +
                ", limit=" + this.limit +
                ", focus=" + this.focus +
                ", bbox=" + this.bbox +
                ", activity=" + this.activity +
                ", targets=" + this.targets +
                ", pois=" + this.pois +
                ", categories=" + this.categories +
                ", verifyWays=" + this.verifyWays +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SearchInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: DataSchema.Language? = null
            internal var slot2: kotlin.Int? = null
            internal var slot3: kotlin.collections.List<kotlin.Double>? = null
            internal var slot4: kotlin.collections.List<kotlin.Double>? = null
            internal var slot5: DataSchema.ActivitySlug? = null
            internal var slot7: kotlin.collections.List<DataSchema.POICategory>? = null
            internal var slot8: DataSchema.PlaceCategoriesInput? = null
            internal var slot9: kotlin.Boolean? = null

            /**
             * Language of the names the places, pois and ways targets return. Only EN and FR
             * are localized; any other language reads English. The itineraries, collections
             * and zones targets ignore it and return names as they were written. Defaults to
             * English when omitted.
             */
            public var language: DataSchema.Language?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Per-target maximum number of results. Must be between 1 and 50; defaults to 8.
             * The cap applies to each enabled target independently, so the whole result can
             * hold up to `limit` × the number of enabled targets, which is why the default is
             * small. Omitting it caps each target at the default rather than returning
             * everything. A value outside
             * the range is refused by every enabled target, each in its own `errors` entry.
             *
             * Left out, the server applies its default: `8`.
             */
            public var limit: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * [longitude, latitude] center used to bias ranking toward nearby results for
             * the places, pois, and ways targets: exactly two numbers, in range, or the
             * request fails with VALIDATION_FAILED at ["input", "focus"]. [0, 0] and an empty
             * list both mean no focus.
             */
            public var focus: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * [minLongitude, minLatitude, maxLongitude, maxLatitude] bounding box used to
             * restrict the search area for the places, pois, and ways targets. [0, 0, 0, 0]
             * and an empty list both mean no box; any count other than four fails the
             * request with VALIDATION_FAILED at ["input", "bbox"]. Each of those three
             * targets also requires values in range, each minimum strictly below its maximum
             * and each axis spanning at most 90 degrees, and reports a box that breaks those
             * rules in `errors`.
             */
            public var bbox: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Activity slug used to filter the ways target: a way the activity cannot use is
             * left out, and the order of the rest is unchanged. Ignored unless
             * targets.ways = true.
             */
            public var activity: DataSchema.ActivitySlug?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Optional POI categories filter applied to the pois target. When omitted,
             * every category is searched. Ignored unless targets.pois = true.
             */
            public var pois: kotlin.collections.List<DataSchema.POICategory>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /**
             * Optional boolean-per-category filter applied to the places target. When
             * omitted, every category is allowed. Ignored unless targets.places = true.
             */
            public var categories: DataSchema.PlaceCategoriesInput?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /**
             * Opt-in to routing-graph verification for the ways target. When true, each
             * candidate way is cross-checked against the same geometry analysis
             * `analyzeGeometry` performs, and an activity for which that analysis finds a
             * stretch it cannot classify is dropped from the way's `activityCompatible`. An
             * activity that could not be checked is kept: a way with no polyline, an
             * analysis that failed, or an activity with no surface analysis, such as the
             * winter ones. Materially increases latency; ignored unless targets.ways = true.
             *
             * Left out, the server applies its default: `false`.
             */
            public var verifyWays: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }
        }

        /** Builds a [DataSchema.SearchInput]. */
        public companion object {
            /** Returns a [DataSchema.SearchInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                query: kotlin.String,
                targets: DataSchema.SearchTargetsInput,
                optional: DataSchema.SearchInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SearchInput {
                val given = DataSchema.SearchInput.Builder().apply(optional)
                return DataSchema.SearchInput(
                    query = query,
                    language = given.slot1,
                    limit = given.slot2,
                    focus = given.slot3,
                    bbox = given.slot4,
                    activity = given.slot5,
                    targets = targets,
                    pois = given.slot7,
                    categories = given.slot8,
                    verifyWays = given.slot9,
                )
            }
        }
    }

    /** Per-target enable flags for the unified search query. */
    @kotlinx.serialization.Serializable
    public class SearchTargetsInput private constructor(
        /**
         * Enable the geocoded places target.
         *
         * Left out, the server applies its default: `false`.
         */
        public val places: kotlin.Boolean? = null,
        /**
         * Enable the points-of-interest target.
         *
         * Left out, the server applies its default: `false`.
         */
        public val pois: kotlin.Boolean? = null,
        /**
         * Enable the named-ways target.
         *
         * Left out, the server applies its default: `false`.
         */
        public val ways: kotlin.Boolean? = null,
        /**
         * Enable the itineraries target.
         *
         * Left out, the server applies its default: `false`.
         */
        public val itineraries: kotlin.Boolean? = null,
        /**
         * Enable the collections target.
         *
         * Left out, the server applies its default: `false`.
         */
        public val collections: kotlin.Boolean? = null,
        /**
         * Enable the zones target.
         *
         * Left out, the server applies its default: `false`.
         */
        public val zones: kotlin.Boolean? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SearchTargetsInput &&
                other.places == this.places &&
                other.pois == this.pois &&
                other.ways == this.ways &&
                other.itineraries == this.itineraries &&
                other.collections == this.collections &&
                other.zones == this.zones

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.places?.hashCode() ?: 0)
            hash = 31 * hash + (this.pois?.hashCode() ?: 0)
            hash = 31 * hash + (this.ways?.hashCode() ?: 0)
            hash = 31 * hash + (this.itineraries?.hashCode() ?: 0)
            hash = 31 * hash + (this.collections?.hashCode() ?: 0)
            hash = 31 * hash + (this.zones?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SearchTargetsInput(" +
                "places=" + this.places +
                ", pois=" + this.pois +
                ", ways=" + this.ways +
                ", itineraries=" + this.itineraries +
                ", collections=" + this.collections +
                ", zones=" + this.zones +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SearchTargetsInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Boolean? = null
            internal var slot1: kotlin.Boolean? = null
            internal var slot2: kotlin.Boolean? = null
            internal var slot3: kotlin.Boolean? = null
            internal var slot4: kotlin.Boolean? = null
            internal var slot5: kotlin.Boolean? = null

            /**
             * Enable the geocoded places target.
             *
             * Left out, the server applies its default: `false`.
             */
            public var places: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Enable the points-of-interest target.
             *
             * Left out, the server applies its default: `false`.
             */
            public var pois: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Enable the named-ways target.
             *
             * Left out, the server applies its default: `false`.
             */
            public var ways: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * Enable the itineraries target.
             *
             * Left out, the server applies its default: `false`.
             */
            public var itineraries: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Enable the collections target.
             *
             * Left out, the server applies its default: `false`.
             */
            public var collections: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Enable the zones target.
             *
             * Left out, the server applies its default: `false`.
             */
            public var zones: kotlin.Boolean?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [DataSchema.SearchTargetsInput]. */
        public companion object {
            /** Returns a [DataSchema.SearchTargetsInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SearchTargetsInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SearchTargetsInput {
                val given = DataSchema.SearchTargetsInput.Builder().apply(optional)
                return DataSchema.SearchTargetsInput(
                    places = given.slot0,
                    pois = given.slot1,
                    ways = given.slot2,
                    itineraries = given.slot3,
                    collections = given.slot4,
                    zones = given.slot5,
                )
            }
        }
    }

    /**
     * Seed directives controlling which route candidates the server returns.
     * Combined with SuggestionsInput.candidates (the total result size), include
     * pins specific seeds into the result and exclude filters them out of the
     * fresh candidate pool. A seed appearing in both lists is VALIDATION_FAILED at
     * ["input", "seeds"]. The caller is responsible for passing the same
     * start/end/distance/points as the original request when reproducing a seed. The
     * server does not validate this, and mismatched inputs will produce a different
     * route even with the same seed.
     */
    @kotlinx.serialization.Serializable
    public class SuggestionSeedsInput private constructor(
        /**
         * Seeds to exclude from the fresh candidate pool, at most 100 (more is
         * VALIDATION_FAILED at ["input", "seeds", "exclude"]). Any
         * deterministically-generated seed matching a value in this list is skipped,
         * so clients can ask for new variations that differ from ones they have
         * already seen.
         */
        public val exclude: kotlin.collections.List<kotlin.Int>? = null,
        /**
         * Seeds to pin into the result, in the order provided, each returned as long as
         * it still routes without crossing a hazard. There may be no more of them than
         * `candidates`, and none may repeat; either refusal is VALIDATION_FAILED at
         * ["input", "seeds", "include"]. Any remaining slots are filled with fresh
         * candidates.
         */
        public val include: kotlin.collections.List<kotlin.Int>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SuggestionSeedsInput &&
                other.exclude == this.exclude &&
                other.include == this.include

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.exclude?.hashCode() ?: 0)
            hash = 31 * hash + (this.include?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SuggestionSeedsInput(" +
                "exclude=" + this.exclude +
                ", include=" + this.include +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SuggestionSeedsInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<kotlin.Int>? = null
            internal var slot1: kotlin.collections.List<kotlin.Int>? = null

            /**
             * Seeds to exclude from the fresh candidate pool, at most 100 (more is
             * VALIDATION_FAILED at ["input", "seeds", "exclude"]). Any
             * deterministically-generated seed matching a value in this list is skipped,
             * so clients can ask for new variations that differ from ones they have
             * already seen.
             */
            public var exclude: kotlin.collections.List<kotlin.Int>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Seeds to pin into the result, in the order provided, each returned as long as
             * it still routes without crossing a hazard. There may be no more of them than
             * `candidates`, and none may repeat; either refusal is VALIDATION_FAILED at
             * ["input", "seeds", "include"]. Any remaining slots are filled with fresh
             * candidates.
             */
            public var include: kotlin.collections.List<kotlin.Int>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.SuggestionSeedsInput]. */
        public companion object {
            /** Returns a [DataSchema.SuggestionSeedsInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: DataSchema.SuggestionSeedsInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SuggestionSeedsInput {
                val given = DataSchema.SuggestionSeedsInput.Builder().apply(optional)
                return DataSchema.SuggestionSeedsInput(
                    exclude = given.slot0,
                    include = given.slot1,
                )
            }
        }
    }

    /** Input for generating route suggestions matching distance constraints. */
    @kotlinx.serialization.Serializable
    public class SuggestionsInput private constructor(
        /**
         * Activity to use for routing. BACKCOUNTRY_SKIING, GRAVEL_BIKE and MOUNTAIN_BIKE
         * have no routing and are VALIDATION_FAILED at ["input", "activity"].
         */
        public val activity: DataSchema.ActivitySlug,
        /**
         * Target elevation gain (ascent) in meters, a finite number, 0 or more (a
         * negative value, NaN or Infinity is VALIDATION_FAILED at ["input", "ascent"]).
         * A positive target ranks fresh
         * candidates by their distance and ascent gaps combined, and drops every fresh
         * candidate whose ascent is more than 50% above or below it, so fewer routes than
         * `candidates`, or none, can come back with no error. Only a candidate that
         * failed on Mountaya's side adds an error to such an answer (see `start` for
         * when it is asked for once more, and SuggestionsResult.routes). A pinned seed
         * is kept whatever its ascent.
         * Example: 800.0 for 800 m of climbing.
         */
        public val ascent: kotlin.Double? = null,
        /**
         * Number of route candidates to generate. Must be between 1 and 10. Each
         * candidate uses a different random seed, producing different route shapes.
         * Example: 3 for three route options.
         */
        public val candidates: kotlin.Int,
        /**
         * Target total distance for the route in meters, greater than 0 and at most
         * 100 km, the routing engine's own ceiling for a route and for a loop. For a
         * point-to-point route it must also be at least the straight-line distance
         * from `start` to `end`. Either refusal is VALIDATION_FAILED at
         * ["input", "distance"].
         * Example: 20000 for a 20 km route.
         *
         * A target, not a promise. Candidates are shaped toward it while they are being
         * built, and the distance reported back is measured from each candidate's
         * finished geometry, so a loop in particular may land short of it. Rank on the
         * returned value rather than assuming the target was met.
         */
        public val distance: kotlin.Double,
        /**
         * End point as [longitude, latitude] (WGS 84): exactly two numbers, a longitude
         * between -180 and 180 and a latitude between -90 and 90 (VALIDATION_FAILED at
         * ["input", "end"] otherwise). When omitted, the route loops back to the start
         * point. When provided and different from start, generates a point-to-point
         * route shaped to meet the target distance.
         * Example: [6.7113, 45.8929] (Saint-Gervais-les-Bains).
         */
        public val end: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Number of route shaping points (2-10, default 5). Higher values produce
         * rounder, more circular loops. Only applies to loop routes.
         * Example: 5 (default). Higher values (8-10) produce rounder loops.
         *
         * Left out, the server applies its default: `5`.
         */
        public val points: kotlin.Int? = null,
        /**
         * Optional directives to pin specific seeds into the result (include) and/or
         * avoid specific seeds when generating fresh candidates (exclude). Useful to
         * reproduce previously-returned routes, discover new variations different
         * from ones already seen, or both in one call. See SuggestionSeedsInput for
         * details.
         */
        public val seeds: DataSchema.SuggestionSeedsInput? = null,
        /**
         * Travel speed in km/h on LEVEL ground, a finite number greater than 0 (anything
         * else, NaN and Infinity included, is VALIDATION_FAILED at ["input", "speed"]).
         * When provided, it replaces the
         * selected activity's default as the flat pace the travel-time model starts
         * from.
         *
         * It is not the speed the route is travelled at: the model resolves a speed per
         * stretch from this pace and the ground (slower on a climb, faster on a
         * descent, and slower again over a coarse surface or a technical trail), caps
         * each stretch at the activity's maximum speed (see SpeedInfo), and integrates
         * the duration over that. Read `analysis.speedInfo` for what each stretch
         * resolved to. While no stretch reaches that cap, duration is inversely
         * proportional to this figure, so halving it doubles the duration.
         * Example: 4.5 for hiking, 25.0 for road cycling.
         */
        public val speed: kotlin.Double? = null,
        /**
         * Starting point as [longitude, latitude] (WGS 84): exactly two numbers, a
         * longitude between -180 and 180 and a latitude between -90 and 90
         * (VALIDATION_FAILED at ["input", "start"] otherwise).
         * Example: [6.8712, 45.9231] (Chamonix).
         *
         * When no hazard-free candidate can be generated from the given points, the
         * request fails with VALIDATION_FAILED at ["input", "start"] rather than
         * returning one that crosses something, and so does a request for which no
         * candidate can be routed at all because the routing engine finds no routable
         * point or no route for any of them. A candidate that failed on Mountaya's side
         * is asked for once more when the answer would otherwise fall short or fail
         * because of it and the request's time budget leaves room, and one that
         * answers then leaves these answers as they are. When no candidate survives
         * and one of them failed on Mountaya's side, the request is INTERNAL_ERROR
         * instead, whichever candidate finished first.
         */
        public val start: kotlin.collections.List<kotlin.Double>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.SuggestionsInput &&
                other.activity == this.activity &&
                other.ascent == this.ascent &&
                other.candidates == this.candidates &&
                other.distance == this.distance &&
                other.end == this.end &&
                other.points == this.points &&
                other.seeds == this.seeds &&
                other.speed == this.speed &&
                other.start == this.start

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.activity.hashCode()
            hash = 31 * hash + (this.ascent?.hashCode() ?: 0)
            hash = 31 * hash + this.candidates.hashCode()
            hash = 31 * hash + this.distance.hashCode()
            hash = 31 * hash + (this.end?.hashCode() ?: 0)
            hash = 31 * hash + (this.points?.hashCode() ?: 0)
            hash = 31 * hash + (this.seeds?.hashCode() ?: 0)
            hash = 31 * hash + (this.speed?.hashCode() ?: 0)
            hash = 31 * hash + this.start.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "SuggestionsInput(" +
                "activity=" + this.activity +
                ", ascent=" + this.ascent +
                ", candidates=" + this.candidates +
                ", distance=" + this.distance +
                ", end=" + this.end +
                ", points=" + this.points +
                ", seeds=" + this.seeds +
                ", speed=" + this.speed +
                ", start=" + this.start +
                ")"

        /** Builder sets the optional properties of a [DataSchema.SuggestionsInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.Double? = null
            internal var slot4: kotlin.collections.List<kotlin.Double>? = null
            internal var slot5: kotlin.Int? = null
            internal var slot6: DataSchema.SuggestionSeedsInput? = null
            internal var slot7: kotlin.Double? = null

            /**
             * Target elevation gain (ascent) in meters, a finite number, 0 or more (a
             * negative value, NaN or Infinity is VALIDATION_FAILED at ["input", "ascent"]).
             * A positive target ranks fresh
             * candidates by their distance and ascent gaps combined, and drops every fresh
             * candidate whose ascent is more than 50% above or below it, so fewer routes than
             * `candidates`, or none, can come back with no error. Only a candidate that
             * failed on Mountaya's side adds an error to such an answer (see `start` for
             * when it is asked for once more, and SuggestionsResult.routes). A pinned seed
             * is kept whatever its ascent.
             * Example: 800.0 for 800 m of climbing.
             */
            public var ascent: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * End point as [longitude, latitude] (WGS 84): exactly two numbers, a longitude
             * between -180 and 180 and a latitude between -90 and 90 (VALIDATION_FAILED at
             * ["input", "end"] otherwise). When omitted, the route loops back to the start
             * point. When provided and different from start, generates a point-to-point
             * route shaped to meet the target distance.
             * Example: [6.7113, 45.8929] (Saint-Gervais-les-Bains).
             */
            public var end: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Number of route shaping points (2-10, default 5). Higher values produce
             * rounder, more circular loops. Only applies to loop routes.
             * Example: 5 (default). Higher values (8-10) produce rounder loops.
             *
             * Left out, the server applies its default: `5`.
             */
            public var points: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * Optional directives to pin specific seeds into the result (include) and/or
             * avoid specific seeds when generating fresh candidates (exclude). Useful to
             * reproduce previously-returned routes, discover new variations different
             * from ones already seen, or both in one call. See SuggestionSeedsInput for
             * details.
             */
            public var seeds: DataSchema.SuggestionSeedsInput?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Travel speed in km/h on LEVEL ground, a finite number greater than 0 (anything
             * else, NaN and Infinity included, is VALIDATION_FAILED at ["input", "speed"]).
             * When provided, it replaces the
             * selected activity's default as the flat pace the travel-time model starts
             * from.
             *
             * It is not the speed the route is travelled at: the model resolves a speed per
             * stretch from this pace and the ground (slower on a climb, faster on a
             * descent, and slower again over a coarse surface or a technical trail), caps
             * each stretch at the activity's maximum speed (see SpeedInfo), and integrates
             * the duration over that. Read `analysis.speedInfo` for what each stretch
             * resolved to. While no stretch reaches that cap, duration is inversely
             * proportional to this figure, so halving it doubles the duration.
             * Example: 4.5 for hiking, 25.0 for road cycling.
             */
            public var speed: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }
        }

        /** Builds a [DataSchema.SuggestionsInput]. */
        public companion object {
            /** Returns a [DataSchema.SuggestionsInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                activity: DataSchema.ActivitySlug,
                candidates: kotlin.Int,
                distance: kotlin.Double,
                start: kotlin.collections.List<kotlin.Double>,
                optional: DataSchema.SuggestionsInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.SuggestionsInput {
                val given = DataSchema.SuggestionsInput.Builder().apply(optional)
                return DataSchema.SuggestionsInput(
                    activity = activity,
                    ascent = given.slot1,
                    candidates = candidates,
                    distance = distance,
                    end = given.slot4,
                    points = given.slot5,
                    seeds = given.slot6,
                    speed = given.slot7,
                    start = start,
                )
            }
        }
    }

    /** Input for a gridded terrain query over an area. */
    @kotlinx.serialization.Serializable
    public class TerrainGridInput private constructor(
        /**
         * [minLongitude, minLatitude, maxLongitude, maxLatitude] bounding box to sample:
         * exactly four values, each minimum strictly below its maximum, both corners
         * valid coordinates, and an area of at most 100 km². A box that breaks any of
         * these is VALIDATION_FAILED at or below ["input", "bbox"]. The grid's cells
         * also fall in at most 256 distinct elevation tiles at zoom 14, counted from
         * the laid-out cells before any is fetched, and more is VALIDATION_FAILED at
         * ["input", "bbox"]. A tile's ground shrinks toward the poles, so a full 64 by
         * 64 grid over 100 km² fits up to about 70 degrees of latitude, and nearer the
         * poles needs a smaller box or fewer cells.
         * Example: [6.81, 45.90, 6.87, 45.94] (the Aiguille du Midi cirque).
         */
        public val bbox: kotlin.collections.List<kotlin.Double>,
        /**
         * Number of grid columns. Must be between 2 and 64 (VALIDATION_FAILED at
         * ["input", "cols"] otherwise).
         *
         * Left out, the server applies its default: `16`.
         */
        public val cols: kotlin.Int? = null,
        /**
         * Number of grid rows. Must be between 2 and 64 (VALIDATION_FAILED at
         * ["input", "rows"] otherwise).
         *
         * Left out, the server applies its default: `16`.
         */
        public val rows: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TerrainGridInput &&
                other.bbox == this.bbox &&
                other.cols == this.cols &&
                other.rows == this.rows

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.bbox.hashCode()
            hash = 31 * hash + (this.cols?.hashCode() ?: 0)
            hash = 31 * hash + (this.rows?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainGridInput(" +
                "bbox=" + this.bbox +
                ", cols=" + this.cols +
                ", rows=" + this.rows +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TerrainGridInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.Int? = null
            internal var slot2: kotlin.Int? = null

            /**
             * Number of grid columns. Must be between 2 and 64 (VALIDATION_FAILED at
             * ["input", "cols"] otherwise).
             *
             * Left out, the server applies its default: `16`.
             */
            public var cols: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Number of grid rows. Must be between 2 and 64 (VALIDATION_FAILED at
             * ["input", "rows"] otherwise).
             *
             * Left out, the server applies its default: `16`.
             */
            public var rows: kotlin.Int?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [DataSchema.TerrainGridInput]. */
        public companion object {
            /** Returns a [DataSchema.TerrainGridInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                bbox: kotlin.collections.List<kotlin.Double>,
                optional: DataSchema.TerrainGridInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TerrainGridInput {
                val given = DataSchema.TerrainGridInput.Builder().apply(optional)
                return DataSchema.TerrainGridInput(
                    bbox = bbox,
                    cols = given.slot1,
                    rows = given.slot2,
                )
            }
        }
    }

    /** Input for a terrain profile along a geometry. */
    @kotlinx.serialization.Serializable
    public class TerrainProfileInput private constructor(
        /**
         * Ordered list of [longitude, latitude] coordinate pairs forming the geometry (WGS 84).
         * Between 2 and 10,000 positions; any other count is VALIDATION_FAILED at
         * ["input", "coordinates"]. Each is exactly two numbers, a longitude between -180
         * and 180 and a latitude between -90 and 90, and one that is not is
         * VALIDATION_FAILED at ["input", "coordinates", i]. The line, measured along its
         * positions, is at most 500 km long, and its samples fall in at most 256
         * distinct elevation tiles at zoom 14, counted from the laid-out samples before
         * any is fetched; a line past either is VALIDATION_FAILED at
         * ["input", "coordinates"], before any elevation is read.
         * Example: [[6.8667, 45.9195], [6.8712, 45.9231], [6.8790, 45.9160]] (a short trail segment).
         */
        public val coordinates: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
        /**
         * Target sampling interval along the geometry in meters. Must be between 10 and
         * 1000 (VALIDATION_FAILED at ["input", "spacingMeters"] otherwise).
         *
         * It is a target, not a guarantee, in both directions. Every input vertex is
         * retained, so the interval across a bend is SHORTER; and a line long enough to
         * need more than 2,000 samples between its vertices is sampled at a coarser
         * interval, so one profile holds at most 12,000 samples. Read each sample's own
         * distanceMeters rather than assuming the interval.
         *
         * Left out, the server applies its default: `100`.
         */
        public val spacingMeters: kotlin.Double? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TerrainProfileInput &&
                other.coordinates == this.coordinates &&
                other.spacingMeters == this.spacingMeters

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.coordinates.hashCode()
            hash = 31 * hash + (this.spacingMeters?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainProfileInput(" +
                "coordinates=" + this.coordinates +
                ", spacingMeters=" + this.spacingMeters +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TerrainProfileInput], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.Double? = null

            /**
             * Target sampling interval along the geometry in meters. Must be between 10 and
             * 1000 (VALIDATION_FAILED at ["input", "spacingMeters"] otherwise).
             *
             * It is a target, not a guarantee, in both directions. Every input vertex is
             * retained, so the interval across a bend is SHORTER; and a line long enough to
             * need more than 2,000 samples between its vertices is sampled at a coarser
             * interval, so one profile holds at most 12,000 samples. Read each sample's own
             * distanceMeters rather than assuming the interval.
             *
             * Left out, the server applies its default: `100`.
             */
            public var spacingMeters: kotlin.Double?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [DataSchema.TerrainProfileInput]. */
        public companion object {
            /** Returns a [DataSchema.TerrainProfileInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                coordinates: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
                optional: DataSchema.TerrainProfileInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TerrainProfileInput {
                val given = DataSchema.TerrainProfileInput.Builder().apply(optional)
                return DataSchema.TerrainProfileInput(
                    coordinates = coordinates,
                    spacingMeters = given.slot1,
                )
            }
        }
    }

    /** Input for a point terrain query. */
    @kotlinx.serialization.Serializable
    public class TerrainSamplesInput private constructor(
        /**
         * Locations as [longitude, latitude] coordinate pairs (WGS 84).
         * Example: [[6.8712, 45.9231], [6.7991, 45.8915]] (Chamonix, Les Houches).
         * Must contain between 1 and 100 locations; any other count is
         * VALIDATION_FAILED at ["input", "locations"]. Each is exactly two numbers, a
         * longitude between -180 and 180 and a latitude between -90 and 90, and one that
         * is not is VALIDATION_FAILED at ["input", "locations", i]. The locations fall in
         * at most 256 distinct elevation tiles at zoom 14, counted before any is
         * fetched, and more is VALIDATION_FAILED at ["input", "locations"]; the 100
         * locations a query may carry reach at most 100.
         */
        public val locations: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is DataSchema.TerrainSamplesInput &&
                other.locations == this.locations

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.locations.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainSamplesInput(" +
                "locations=" + this.locations +
                ")"

        /** Builder sets the optional properties of a [DataSchema.TerrainSamplesInput], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [DataSchema.TerrainSamplesInput]. */
        public companion object {
            /** Returns a [DataSchema.TerrainSamplesInput] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                locations: kotlin.collections.List<kotlin.collections.List<kotlin.Double>>,
                optional: DataSchema.TerrainSamplesInput.Builder.() -> kotlin.Unit = {},
            ): DataSchema.TerrainSamplesInput {
                val given = DataSchema.TerrainSamplesInput.Builder().apply(optional)
                return DataSchema.TerrainSamplesInput(
                    locations = locations,
                )
            }
        }
    }

    /**
     * Activity identifier. Each activity uses a different routing profile optimized
     * for its terrain and movement style.
     *
     * The REST surfaces spell these values the way they spell every shared enum:
     * lowercased, with each underscore turned into a hyphen, so `HIKING_AND_TRAIL`
     * here is `hiking-and-trail` there.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ActivitySlug(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Hiking and trail running on footpaths. Balanced routing that favors trails, paths, and quieter, greener ways; waypoints must lie within about 400 m of a routable path. Default speed: ~4.5 km/h. */
            public val HikingAndTrail: ActivitySlug = ActivitySlug("HIKING_AND_TRAIL")

            /** Expert trail routing on the same trail network as HIKING_AND_TRAIL, tuned for maximum reach: most-direct routing, no scenic bias, and unlimited waypoint snapping, so routes can start or end far off-network and follow very unconventional lines (only ferries are excluded). Also supports freehand line editing. Default speed: ~8 km/h. */
            public val ProfessionalTrail: ActivitySlug = ActivitySlug("PROFESSIONAL_TRAIL")

            /** Road and path running. Fastest-preference routing that favors runnable surfaces; waypoints must lie within about 400 m of a routable way. For trail running, use HIKING_AND_TRAIL. Default speed: ~10 km/h. */
            public val Running: ActivitySlug = ActivitySlug("RUNNING")

            /** Road cycling on paved roads and cycleways. Routes on the paved network with surface analysis. Default speed: ~25 km/h. */
            public val RoadCycling: ActivitySlug = ActivitySlug("ROAD_CYCLING")

            /** Gravel biking on mixed-surface roads and tracks. Unavailable: the activity catalog does not list it, and a request naming it is refused, for routing, freehand line editing and analysis alike. Default speed: ~20 km/h. */
            public val GravelBike: ActivitySlug = ActivitySlug("GRAVEL_BIKE")

            /** Mountain biking on off-road trails and singletrack. Unavailable: the activity catalog does not list it, and a request naming it is refused, for routing, freehand line editing and analysis alike. Default speed: ~15 km/h. */
            public val MountainBike: ActivitySlug = ActivitySlug("MOUNTAIN_BIKE")

            /** Backcountry (off-piste) skiing. Freehand line editing only; turn-by-turn directions are not available, so you draw the route yourself. Default speed: ~4 km/h. */
            public val BackcountrySkiing: ActivitySlug = ActivitySlug("BACKCOUNTRY_SKIING")

            /** Ski touring, ascending and descending off-piste. Routes on the trail network with unlimited waypoint snapping, so high-alpine points far from mapped trails still route; also supports freehand line editing. Default speed: ~4 km/h. */
            public val SkiTouring: ActivitySlug = ActivitySlug("SKI_TOURING")

            /** Cross-country (nordic) skiing on groomed trails. Balanced routing that favors designated tracks; waypoints must lie within about 400 m of a routable way. Default speed: ~8 km/h. */
            public val CrossCountrySkiing: ActivitySlug = ActivitySlug("CROSS_COUNTRY_SKIING")

            /** Snowshoe walking on snow-covered terrain. Routes on the trail network with relaxed waypoint snapping (up to a few km) for off-trail backcountry points; also supports freehand line editing. Default speed: ~3.5 km/h. */
            public val SnowshoeWalking: ActivitySlug = ActivitySlug("SNOWSHOE_WALKING")
        }
    }

    /**
     * Compass octant of a downhill aspect direction.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class AspectOctant(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** North-facing (337.5-22.5 degrees). */
            public val North: AspectOctant = AspectOctant("NORTH")

            /** Northeast-facing (22.5-67.5 degrees). */
            public val NorthEast: AspectOctant = AspectOctant("NORTH_EAST")

            /** East-facing (67.5-112.5 degrees). */
            public val East: AspectOctant = AspectOctant("EAST")

            /** Southeast-facing (112.5-157.5 degrees). */
            public val SouthEast: AspectOctant = AspectOctant("SOUTH_EAST")

            /** South-facing (157.5-202.5 degrees). */
            public val South: AspectOctant = AspectOctant("SOUTH")

            /** Southwest-facing (202.5-247.5 degrees). */
            public val SouthWest: AspectOctant = AspectOctant("SOUTH_WEST")

            /** West-facing (247.5-292.5 degrees). */
            public val West: AspectOctant = AspectOctant("WEST")

            /** Northwest-facing (292.5-337.5 degrees). */
            public val NorthWest: AspectOctant = AspectOctant("NORTH_WEST")
        }
    }

    /**
     * What a directions request's `avoid` list actually achieved. Without it an
     * unchanged route is ambiguous: a caller cannot tell "there was nothing to avoid"
     * from "no route survived the constraint".
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class AvoidanceOutcome(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The request carried no `avoid` list. */
            public val NotRequested: AvoidanceOutcome = AvoidanceOutcome("NOT_REQUESTED")

            /** Avoidance was requested and no stretch of the route was found on a named hazard, so nothing was re-routed. A hazard known only from the routing engine's route-level warning leaves no stretch to route around, so read the advisories as well. */
            public val NotNeeded: AvoidanceOutcome = AvoidanceOutcome("NOT_NEEDED")

            /** The route returned was re-routed with the offending ways excluded, up to about 10 km of hazardous way in total, so a longer hazard can still be crossed. It may also carry a DIFFERENT hazard, so read the advisories rather than assuming. */
            public val Applied: AvoidanceOutcome = AvoidanceOutcome("APPLIED")

            /** The hazards were found and excluded, but no route survived, so the route returned is the original one and still crosses them. Usually means a waypoint sits on the hazardous way itself, which no exclusion can route around. Also the outcome of a re-route that failed on Mountaya's side, which an INTERNAL_ERROR entry in `errors` then flags beside the route. */
            public val NoRoute: AvoidanceOutcome = AvoidanceOutcome("NO_ROUTE")
        }
    }

    /**
     * The closed vocabulary of `extensions.code` on an error entry.
     *
     * It is declared as an enum so a client can switch on it exhaustively and have the
     * compiler say when a new code appears, rather than string-matching against a list
     * transcribed out of a description.
     *
     * This API sends BAD_REQUEST, VALIDATION_FAILED, UNAUTHORIZED, PAYMENT_REQUIRED,
     * FORBIDDEN, NOT_FOUND, METHOD_NOT_ALLOWED, PAYLOAD_TOO_LARGE, TOO_MANY_REQUESTS,
     * INTERNAL_ERROR, NOT_IMPLEMENTED and GATEWAY_TIMEOUT. CONFLICT, BAD_GATEWAY and
     * SERVICE_UNAVAILABLE are never sent here: they complete the vocabulary this API
     * shares with the REST surfaces, so one switch handles all three.
     *
     * The GraphQL protocol's own refusals, made before any field runs, carry codes
     * outside this enum, as the schema's introduction states: PERSISTED_QUERY_NOT_FOUND,
     * GRAPHQL_PARSE_FAILED and GRAPHQL_VALIDATION_FAILED, DEPTH_LIMIT_EXCEEDED for an
     * operation nested deeper than 15 levels, and COMPLEXITY_LIMIT_EXCEEDED for one
     * over the complexity ceiling of 500.
     *
     * The REST surfaces also define codes this API never sends.
     * AI_PROVIDER_NOT_CONFIGURED (on the Map Studio API) refines a 409 answered when an
     * agent run starts, which this API cannot do. IDEMPOTENCY_KEY_IN_USE and
     * IDEMPOTENCY_ANSWER_UNAVAILABLE (on the Map Studio API) refine the 409 of a write
     * sent with an Idempotency-Key, whose first request is still running or whose
     * first answer cannot be replayed, and PRECONDITION_FAILED (on the Map Studio
     * API) is the 412 of a write whose If-Match names a version the resource has moved
     * on from; this API takes no write, so it takes neither header.
     * REDELIVERY_IN_PROGRESS and SECRET_ROTATION_IN_OVERLAP (on the Map Studio API)
     * refine the 409 of a webhook endpoint's redelivery asked while another still
     * runs, and of its signing secret's rotation asked while the secret an earlier one
     * replaced still signs; this API takes no write. LIMIT_REACHED (on
     * the Map Studio API) refines the 429 that refuses a ceiling rather than a limit
     * counted over a window, and carries no Retry-After: the seats, the published
     * itineraries, the published zones, the invitations an organization may have
     * waiting at once (`pending-invitations`), the webhook endpoints it may hold,
     * the allowed domains it may list or the personal space's itineraries, none of
     * which this API reaches.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ErrorCode(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The request was malformed: on this API, a body not sent as application/json or that does not decode as a JSON object, or a request asking to upgrade its connection. The body is never echoed back. */
            public val BadRequest: ErrorCode = ErrorCode("BAD_REQUEST")

            /** One or more input fields were rejected. One entry is emitted per refused field, each carrying its own `path`. An input is checked in stages and a later stage runs only once the earlier ones pass, so correct what was reported and send again. */
            public val ValidationFailed: ErrorCode = ErrorCode("VALIDATION_FAILED")

            /** No publishable key, a secret key sent in its place, or an unknown or revoked key, including any key of a canceled organization; or a session token that is absent, invalid, expired or minted for another key. */
            public val Unauthorized: ErrorCode = ErrorCode("UNAUTHORIZED")

            /** The organization does not hold the Data API, or it is billed by card and has no payment method on file. The Data API is held while the metered subscription the organization's first card opens is in force, or by the organization's agreement with Mountaya. The product is checked before the key's scope, so a key without the scope in an organization that does not hold the Data API is answered this rather than FORBIDDEN. The payment method is checked after the scope, so the same key in an organization with no payment method on file is answered FORBIDDEN. `exportItinerary` also answers it for the draft of an itinerary whose organization does not hold Map Publishing. */
            public val PaymentRequired: ErrorCode = ErrorCode("PAYMENT_REQUIRED")

            /** The key does not carry a scope that authorizes this field, or the request's Origin or Referer is not among the organization's allowed domains. */
            public val Forbidden: ErrorCode = ErrorCode("FORBIDDEN")

            /** The addressed resource does not exist or is not visible to your key. The two are one answer, so it never confirms that an id exists. */
            public val NotFound: ErrorCode = ErrorCode("NOT_FOUND")

            /** The request used an HTTP method other than POST. */
            public val MethodNotAllowed: ErrorCode = ErrorCode("METHOD_NOT_ALLOWED")

            /** Never sent by this API, which changes no state. Part of the shared vocabulary. */
            public val Conflict: ErrorCode = ErrorCode("CONFLICT")

            /** The request body is over 32 MiB, the ceiling the Map Studio API holds most bodies to. It is refused once the credential has been checked and before anything reads the body, and no field runs. Or, as the entry of `itinerary` or `exportItinerary` in a 200 response, the itinerary holds more than 500,000 positions, more than one answer carries: that field is refused, its entry at the input as a whole rather than at any one of its fields, and the rest of the request runs. */
            public val PayloadTooLarge: ErrorCode = ErrorCode("PAYLOAD_TOO_LARGE")

            /** A limit on the organization's requests to this API is spent, and the response carries `Retry-After`, the whole seconds until it lifts. Either its per-minute limit, 60 requests a minute unless its agreement sets another figure, which every key of the organization draws on: wait out the minute. Or its monthly allowance, 300,000 unless its agreement sets another figure or leaves it unlimited, which counts the root fields answered without an error over the organization's current billing period, the MCP server's included: every request of the organization's own clients is refused until the next billing period begins. A request made with a session token minted for studio-embedded counts toward neither, and is refused instead once a bound of its own is spent, as this API's introduction states. So is a request from Mountaya's web application, made signed in to it rather than with a key, whose session cannot be checked yet because too many sessions sent from the client's IP address have been refused within the minute: the session may be perfectly valid, which is why it is not a 401, and `Retry-After` names the seconds until the minute's window turns. */
            public val TooManyRequests: ErrorCode = ErrorCode("TOO_MANY_REQUESTS")

            /** An unexpected server-side failure. Opaque by construction: no field path, no metadata. */
            public val InternalError: ErrorCode = ErrorCode("INTERNAL_ERROR")

            /** The field cannot be answered here. Sent only by `exportItinerary`, when downloads are unavailable. */
            public val NotImplemented: ErrorCode = ErrorCode("NOT_IMPLEMENTED")

            /** Never sent by this API. Part of the shared vocabulary. */
            public val BadGateway: ErrorCode = ErrorCode("BAD_GATEWAY")

            /** Never sent by this API. Part of the shared vocabulary. */
            public val ServiceUnavailable: ErrorCode = ErrorCode("SERVICE_UNAVAILABLE")

            /** The request was still running when its time budget of 30 seconds ran out, and it is answered with HTTP status 504 in place of whatever was in flight, with no data. It is the same code the REST surfaces answer a spent budget with. Nothing about the request is wrong: send it again, or narrow it, a smaller matrix or fewer root fields, where it asks a lot. */
            public val GatewayTimeout: ErrorCode = ErrorCode("GATEWAY_TIMEOUT")
        }
    }

    /**
     * File format for a route or itinerary export. `FIT` and `TCX` additionally accept
     * an ExportVariant selecting the course or activity file type; supplying one for
     * any other format is rejected.
     *
     * Declared in the same order as `ExportFormat` on the REST surfaces, where the
     * same seven values are spelled in lowercase.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ExportFormat(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** GPS Exchange Format (.gpx), compatible with most GPS devices and apps. */
            public val Gpx: ExportFormat = ExportFormat("GPX")

            /** GeoJSON FeatureCollection (.geojson), a standard format for geographic data. */
            public val Geojson: ExportFormat = ExportFormat("GEOJSON")

            /** Keyhole Markup Language (.kml), the Google Earth and Maps interchange format. */
            public val Kml: ExportFormat = ExportFormat("KML")

            /** Compressed Keyhole Markup Language (.kmz), a zipped KML document. */
            public val Kmz: ExportFormat = ExportFormat("KMZ")

            /** Flexible and Interoperable Data Transfer (.fit), the native Garmin, Wahoo and Coros format. Accepts an ExportVariant. */
            public val Fit: ExportFormat = ExportFormat("FIT")

            /** Training Center XML (.tcx), accepted by Garmin Connect, Strava and TrainingPeaks. Accepts an ExportVariant. A course's name is cut to the 15 characters the format allows, and a course point's to 10. */
            public val Tcx: ExportFormat = ExportFormat("TCX")

            /** Comma-separated values (.csv), a flat coordinate table for spreadsheets and analysis. Lossy: segments, styling and metadata are not preserved, so a CSV export cannot be imported back. A name beginning with =, +, -, @, a tab or a carriage return, or with the full-width =, +, - or @, is prefixed with a single quote (') so a spreadsheet reads it as text. */
            public val Csv: ExportFormat = ExportFormat("CSV")
        }
    }

    /**
     * File type of a time-series export, the distinction FIT and TCX draw natively.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ExportVariant(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /**
             * A planned route a device can navigate turn by turn. The default when no
             * variant is named.
             */
            public val Course: ExportVariant = ExportVariant("COURSE")

            /**
             * The same geometry as a completed workout, with timestamps synthesized from each
             * segment's planned start and estimated duration. The timeline only moves
             * forward: a segment's planned start is used when it comes after the previous
             * segment's end; otherwise the segment starts one second after it.
             */
            public val Activity: ExportVariant = ExportVariant("ACTIVITY")
        }
    }

    /**
     * Geocoding result layer. Indicates the administrative or feature scope of a
     * geocoded place, ordered from coarsest (country) to finest (house, natural
     * feature).
     *
     * This is a different axis from PlaceCategory, and a Place carries both. The layer
     * is the scope the geocoder resolved to and is what `layers` filters on; the
     * category is the coarse kind of thing the result is and is what
     * PlaceCategoriesInput filters on. Only COUNTRY is spelled the same in both, and
     * even there the two answer different questions. Read them independently.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class GeocodeLayer(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Country-level administrative boundary. */
            public val Country: GeocodeLayer = GeocodeLayer("COUNTRY")

            /** Region or state within a country. */
            public val Region: GeocodeLayer = GeocodeLayer("REGION")

            /** County or department within a region. */
            public val County: GeocodeLayer = GeocodeLayer("COUNTY")

            /** City, town, or village. */
            public val City: GeocodeLayer = GeocodeLayer("CITY")

            /** District or neighborhood within a city. */
            public val District: GeocodeLayer = GeocodeLayer("DISTRICT")

            /** Postal code area. */
            public val Postcode: GeocodeLayer = GeocodeLayer("POSTCODE")

            /** Street or road. */
            public val Street: GeocodeLayer = GeocodeLayer("STREET")

            /** House or building address. */
            public val House: GeocodeLayer = GeocodeLayer("HOUSE")

            /** Point of interest. */
            public val Poi: GeocodeLayer = GeocodeLayer("POI")

            /** Natural feature such as a mountain, lake, or forest. */
            public val Natural: GeocodeLayer = GeocodeLayer("NATURAL")
        }
    }

    /**
     * User-facing locale code. Determines the language of localized strings returned
     * by the API.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class Language(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** English. */
            public val En: Language = Language("EN")

            /** French. */
            public val Fr: Language = Language("FR")

            /** Spanish. */
            public val Es: Language = Language("ES")

            /** Italian. */
            public val It: Language = Language("IT")

            /** German. */
            public val De: Language = Language("DE")

            /** Chinese (Simplified). */
            public val Zh: Language = Language("ZH")

            /** Japanese. */
            public val Ja: Language = Language("JA")
        }
    }

    /**
     * Maneuver type for a turn-by-turn direction step, derived from the routing
     * engine's instruction analysis. Clients template localized instruction text from
     * this type plus the step's name.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ManeuverType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Turn left. */
            public val TurnLeft: ManeuverType = ManeuverType("TURN_LEFT")

            /** Turn right. */
            public val TurnRight: ManeuverType = ManeuverType("TURN_RIGHT")

            /** Sharp left. */
            public val SharpLeft: ManeuverType = ManeuverType("SHARP_LEFT")

            /** Sharp right. */
            public val SharpRight: ManeuverType = ManeuverType("SHARP_RIGHT")

            /** Slight left. */
            public val SlightLeft: ManeuverType = ManeuverType("SLIGHT_LEFT")

            /** Slight right. */
            public val SlightRight: ManeuverType = ManeuverType("SLIGHT_RIGHT")

            /** Continue straight. */
            public val Continue: ManeuverType = ManeuverType("CONTINUE")

            /** Enter a roundabout. */
            public val EnterRoundabout: ManeuverType = ManeuverType("ENTER_ROUNDABOUT")

            /** Exit a roundabout. */
            public val ExitRoundabout: ManeuverType = ManeuverType("EXIT_ROUNDABOUT")

            /** Make a U-turn. */
            public val Uturn: ManeuverType = ManeuverType("UTURN")

            /** Arrive at the destination. */
            public val Goal: ManeuverType = ManeuverType("GOAL")

            /** Depart from the start. */
            public val Depart: ManeuverType = ManeuverType("DEPART")

            /** Keep left. */
            public val KeepLeft: ManeuverType = ManeuverType("KEEP_LEFT")

            /** Keep right. */
            public val KeepRight: ManeuverType = ManeuverType("KEEP_RIGHT")

            /** Unknown or unclassified maneuver. */
            public val Unknown: ManeuverType = ManeuverType("UNKNOWN")
        }
    }

    /**
     * Field on a searchable entity that matched a search query. Used by full-text
     * search to indicate which property produced the match.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class MatchedField(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The entity's name field matched the query. */
            public val Name: MatchedField = MatchedField("NAME")

            /** The entity's description field matched the query. */
            public val Description: MatchedField = MatchedField("DESCRIPTION")
        }
    }

    /**
     * Metric type for matrix computation.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class MatrixMetric(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Distance in meters. */
            public val Distance: MatrixMetric = MatrixMetric("DISTANCE")

            /** Duration in seconds. */
            public val Duration: MatrixMetric = MatrixMetric("DURATION")
        }
    }

    /**
     * OpenStreetMap primitive type identifying how a feature is represented in the
     * OSM data model.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class OSMType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** OSM node (a single point with coordinates). */
            public val Node: OSMType = OSMType("NODE")

            /** OSM way (an ordered list of nodes forming a line or polygon). */
            public val Way: OSMType = OSMType("WAY")

            /** OSM relation (a grouping of nodes, ways, or other relations). */
            public val Relation: OSMType = OSMType("RELATION")
        }
    }

    /**
     * Point-of-interest category. Classifies POIs surfaced by search and on the
     * map by their primary outdoor function.
     *
     * Six members (VIEWPOINT, PARKING, TOILETS, SHELTER, CABLE_CAR and
     * ACCOMMODATION) are spelled the same as members of WaypointAmenity, and the
     * two are not interchangeable. This enum says what a place *is*; WaypointAmenity
     * says what a waypoint *offers*. A hut POI is HUT here and may offer
     * ACCOMMODATION there. Do not map one enum onto the other by name.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class POICategory(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Mountain hut or refuge providing shelter and meals. */
            public val Hut: POICategory = POICategory("HUT")

            /** Mountain summit or peak. */
            public val Peak: POICategory = POICategory("PEAK")

            /** Mountain pass or col between valleys. */
            public val Pass: POICategory = POICategory("PASS")

            /** Scenic viewpoint or panorama spot. */
            public val Viewpoint: POICategory = POICategory("VIEWPOINT")

            /** Parking area for vehicles, often at a trailhead. */
            public val Parking: POICategory = POICategory("PARKING")

            /** Drinkable water source such as a spring or fountain. */
            public val WaterSource: POICategory = POICategory("WATER_SOURCE")

            /** Cafe or coffee shop. */
            public val Cafe: POICategory = POICategory("CAFE")

            /** Restaurant serving meals. */
            public val Restaurant: POICategory = POICategory("RESTAURANT")

            /** Supermarket or grocery store. */
            public val Supermarket: POICategory = POICategory("SUPERMARKET")

            /** Pharmacy or chemist. */
            public val Pharmacy: POICategory = POICategory("PHARMACY")

            /** Public toilets. */
            public val Toilets: POICategory = POICategory("TOILETS")

            /** Campsite or camping pitch. */
            public val Campsite: POICategory = POICategory("CAMPSITE")

            /** Bivouac shelter or emergency refuge with no staff. */
            public val Shelter: POICategory = POICategory("SHELTER")

            /** Cable car, gondola, or chairlift station. */
            public val CableCar: POICategory = POICategory("CABLE_CAR")

            /** Bus station or major bus stop. */
            public val BusStation: POICategory = POICategory("BUS_STATION")

            /** Train station. */
            public val TrainStation: POICategory = POICategory("TRAIN_STATION")

            /** Airport or airfield. */
            public val Airport: POICategory = POICategory("AIRPORT")

            /** Hotel, hostel, guest house, or chalet. */
            public val Accommodation: POICategory = POICategory("ACCOMMODATION")

            /** Caravan or camper site. */
            public val CaravanSite: POICategory = POICategory("CARAVAN_SITE")

            /** Hospital. */
            public val Hospital: POICategory = POICategory("HOSPITAL")
        }
    }

    /**
     * Publishable-key scope that can authorize a Data API field, as declared by the
     * @pkeyScope directive. The two are alternatives, never a pair: a key reaches this
     * API directly with DATA, or through Mountaya's MCP server with MCP, and holding
     * one grants nothing about the other.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class PkeyScope(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Query this API directly, sending the key and a session token yourself. */
            public val Data: PkeyScope = PkeyScope("DATA")

            /** Reach this API through Mountaya's MCP server. */
            public val Mcp: PkeyScope = PkeyScope("MCP")
        }
    }

    /**
     * Coarse classification of a geocoded place. Used both as a response field
     * on Place (clients can render category-specific UI) and as the dimension
     * along which PlaceCategoriesInput filters forward / reverse geocoding
     * output.
     *
     * This is a different axis from GeocodeLayer, and a Place carries both: the layer
     * is the scope the geocoder resolved to, this is the coarse kind of thing the
     * result is. Only COUNTRY is spelled the same in both. Read them independently.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class PlaceCategory(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Populated places like cities, towns, villages, hamlets, and suburbs. */
            public val PopulatedPlace: PlaceCategory = PlaceCategory("POPULATED_PLACE")

            /** Administrative boundaries like regions, counties, departments, districts, and localities. */
            public val AdministrativeArea: PlaceCategory = PlaceCategory("ADMINISTRATIVE_AREA")

            /** Country-level results such as France or Switzerland. */
            public val Country: PlaceCategory = PlaceCategory("COUNTRY")

            /** Natural features like mountain peaks, lakes, glaciers, rivers, and forests. */
            public val NaturalFeature: PlaceCategory = PlaceCategory("NATURAL_FEATURE")

            /** Transit stations like train stations, bus terminals, airports, and cable car stations. */
            public val TransitStation: PlaceCategory = PlaceCategory("TRANSIT_STATION")

            /** Amenities like cafes, restaurants, shops, tourist attractions, and leisure venues. */
            public val Amenity: PlaceCategory = PlaceCategory("AMENITY")

            /** Addresses like streets, house numbers, and postcodes. */
            public val Address: PlaceCategory = PlaceCategory("ADDRESS")

            /** Catch-all for places that do not fit any of the other categories. */
            public val Other: PlaceCategory = PlaceCategory("OTHER")
        }
    }

    /**
     * Unit family a range value is measured in. Shared vocabulary with the Map Studio
     * API, which carries the same two members as `RangeType` for a sourced zone's
     * range values, because a sourced zone is the polygon an isochrone computed. Each
     * surface spells it idiomatically: `TIME` here, `time` there.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class RangeType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** A budget of travel time, in seconds. */
            public val Time: RangeType = RangeType("TIME")

            /** A budget of travel distance, in meters. */
            public val Distance: RangeType = RangeType("DISTANCE")
        }
    }

    /**
     * An access restriction tagged on a way in OpenStreetMap (the `access` key and
     * its mode-specific variants such as `foot`), resolved for the activity's mode of
     * travel.
     *
     * A stretch carries a LIST of these rather than a single value: a way can be
     * tagged with several restrictions at once, and every one found is reported. An
     * unrestricted stretch, the common case, is not reported at all.
     *
     * PERMISSIVE, AGRICULTURAL and FORESTRY are told apart from a genuine restriction
     * only where the routing engine returns per-stretch access data. Where it returns
     * none, the route-level warning that stands in for that data is raised by any
     * access tag, those three included (see RouteAdvisories).
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class RoadAccessRestrictionType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Access is forbidden (`access=no`). */
            public val No: RoadAccessRestrictionType = RoadAccessRestrictionType("NO")

            /** Access is limited to customers of the adjoining premises (`access=customers`). */
            public val Customers: RoadAccessRestrictionType = RoadAccessRestrictionType("CUSTOMERS")

            /** Access is limited to reaching a destination on the way itself, not passing through (`access=destination`). */
            public val Destination: RoadAccessRestrictionType = RoadAccessRestrictionType("DESTINATION")

            /** Access is limited to deliveries (`access=delivery`). */
            public val Delivery: RoadAccessRestrictionType = RoadAccessRestrictionType("DELIVERY")

            /** Access is limited to the owner and their guests (`access=private`). */
            public val Private: RoadAccessRestrictionType = RoadAccessRestrictionType("PRIVATE")

            /** The owner tolerates passage and may withdraw it at any time (`access=permissive`). This grants access rather than restricting it, so it is reported but is not treated as a hazard. */
            public val Permissive: RoadAccessRestrictionType = RoadAccessRestrictionType("PERMISSIVE")

            /** Access requires an individual permit (`access=permit`). */
            public val Permit: RoadAccessRestrictionType = RoadAccessRestrictionType("PERMIT")

            /** Access is restricted without the tagging saying to whom (`access=restricted`). Treated as a genuine restriction. */
            public val Restricted: RoadAccessRestrictionType = RoadAccessRestrictionType("RESTRICTED")

            /** The way carries an access restriction whose nature is not recorded (`access=unknown`). Treated as a genuine restriction, because an unrecorded restriction is not an absent one. */
            public val Unknown: RoadAccessRestrictionType = RoadAccessRestrictionType("UNKNOWN")

            /** Access is limited to vehicles servicing the way or the premises on it (`access=service`). */
            public val Service: RoadAccessRestrictionType = RoadAccessRestrictionType("SERVICE")

            /** Access is limited to agricultural traffic (`access=agricultural`). Reported so a route over a farm track is never a surprise, but NOT treated as a hazard: farm tracks are ordinary terrain on foot. */
            public val Agricultural: RoadAccessRestrictionType = RoadAccessRestrictionType("AGRICULTURAL")

            /** Access is limited to forestry traffic (`access=forestry`). Reported but, like AGRICULTURAL, not treated as a hazard. */
            public val Forestry: RoadAccessRestrictionType = RoadAccessRestrictionType("FORESTRY")
        }
    }

    /**
     * A route-level hazard a directions request can ask to route around.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class RouteHazard(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** A way that charges a toll for the activity's mode of travel. */
            public val Tollway: RouteHazard = RouteHazard("TOLLWAY")

            /** A way carrying a genuine access restriction. PERMISSIVE, AGRICULTURAL and FORESTRY ways do not count: the owner tolerates passage on the first, and the other two are ordinary terrain on foot. */
            public val RestrictedAccess: RouteHazard = RouteHazard("RESTRICTED_ACCESS")
        }
    }

    /**
     * Search target entity. Identifies which kind of entity a unified search query
     * should match against.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class SearchTarget(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Named places from the geocoder (cities, regions, natural features). */
            public val Places: SearchTarget = SearchTarget("PLACES")

            /** Points of interest such as huts, peaks, parking, and amenities. */
            public val Pois: SearchTarget = SearchTarget("POIS")

            /** Named ways such as trails, paths, and roads. */
            public val Ways: SearchTarget = SearchTarget("WAYS")

            /** Itineraries of your key's organization. */
            public val Itineraries: SearchTarget = SearchTarget("ITINERARIES")

            /** Collections of your key's organization. */
            public val Collections: SearchTarget = SearchTarget("COLLECTIONS")

            /** Zones of your key's organization. */
            public val Zones: SearchTarget = SearchTarget("ZONES")
        }
    }

    /**
     * Slope steepness band, matching the steepness overlay's legend. Covers 25 to 45+
     * degrees, the angle range where slopes become most hazardous.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class SteepnessBand(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** 25 to 30 degrees. */
            public val From25To30: SteepnessBand = SteepnessBand("FROM_25_TO_30")

            /** 30 to 35 degrees. */
            public val From30To35: SteepnessBand = SteepnessBand("FROM_30_TO_35")

            /** 35 to 40 degrees. */
            public val From35To40: SteepnessBand = SteepnessBand("FROM_35_TO_40")

            /** 40 to 45 degrees. */
            public val From40To45: SteepnessBand = SteepnessBand("FROM_40_TO_45")

            /** Above 45 degrees. Terrain rarely holds a deep snowpack at this angle but often presents other hazards such as cliffs. */
            public val Above45: SteepnessBand = SteepnessBand("ABOVE_45")
        }
    }

    /**
     * Classification of the ground surface a stretch of route is carried on: what it
     * is paved with, rather than the character of the way.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class SurfaceType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Unknown or unclassified surface. */
            public val Unknown: SurfaceType = SurfaceType("UNKNOWN")

            /** Generic paved surface. */
            public val Paved: SurfaceType = SurfaceType("PAVED")

            /** Generic unpaved surface. */
            public val Unpaved: SurfaceType = SurfaceType("UNPAVED")

            /** Asphalt surface. */
            public val Asphalt: SurfaceType = SurfaceType("ASPHALT")

            /** Concrete surface. */
            public val Concrete: SurfaceType = SurfaceType("CONCRETE")

            /** Cobblestone surface. */
            public val Cobblestone: SurfaceType = SurfaceType("COBBLESTONE")

            /** Metal surface. */
            public val Metal: SurfaceType = SurfaceType("METAL")

            /** Wooden surface. */
            public val Wood: SurfaceType = SurfaceType("WOOD")

            /** Compacted gravel surface. */
            public val CompactedGravel: SurfaceType = SurfaceType("COMPACTED_GRAVEL")

            /** Fine gravel surface. */
            public val FineGravel: SurfaceType = SurfaceType("FINE_GRAVEL")

            /** Gravel surface. */
            public val Gravel: SurfaceType = SurfaceType("GRAVEL")

            /** Dirt surface. */
            public val Dirt: SurfaceType = SurfaceType("DIRT")

            /** Natural ground surface. */
            public val Ground: SurfaceType = SurfaceType("GROUND")

            /** Ice surface. */
            public val Ice: SurfaceType = SurfaceType("ICE")

            /** Paving stones surface. */
            public val PavingStones: SurfaceType = SurfaceType("PAVING_STONES")

            /** Sand surface. */
            public val Sand: SurfaceType = SurfaceType("SAND")

            /** Woodchips surface. */
            public val Woodchips: SurfaceType = SurfaceType("WOODCHIPS")

            /** Grass surface. */
            public val Grass: SurfaceType = SurfaceType("GRASS")

            /** Grass paver surface. */
            public val GrassPaver: SurfaceType = SurfaceType("GRASS_PAVER")
        }
    }

    /**
     * Whether a stretch of route crosses a way that charges a toll, as tagged in
     * OpenStreetMap (`toll`). Resolved per activity: a way tolled only for heavy
     * goods vehicles is not a tollway on foot, while a way tolled for everyone is.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class TollwayType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** No toll on this stretch. Never reported: `tollwayInfo` lists only the stretches that cross a tollway. */
            public val None: TollwayType = TollwayType("NONE")

            /** This stretch crosses a way that charges a toll for the activity's mode of travel. */
            public val Tollway: TollwayType = TollwayType("TOLLWAY")
        }
    }

    /**
     * Trail difficulty as tagged in OpenStreetMap. Walking activities are graded on
     * the SAC hiking scale (`sac_scale`) and cycling activities on the mountain-biking
     * scale (`mtb:scale`); a stretch carries a value from whichever scale matches the
     * activity, and the value names its own scale.
     *
     * Most ways carry no difficulty tag at all, so UNKNOWN is the common value and
     * means "not classified", not "easy". On the SAC hiking scale a way tagged below
     * HIKING is reported as UNKNOWN too, because the routing engine does not tell it
     * apart from an untagged one.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class TrailDifficultyType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** No difficulty tag on this stretch. The most common value; it carries no difficulty claim. */
            public val Unknown: TrailDifficultyType = TrailDifficultyType("UNKNOWN")

            /** Hiking. Trail is well cleared; flat or slightly sloped, no fall hazard. */
            public val Hiking: TrailDifficultyType = TrailDifficultyType("HIKING")

            /** Mountain hiking. Trail is continuous, may have exposed sections; sure-footedness required. */
            public val MountainHiking: TrailDifficultyType = TrailDifficultyType("MOUNTAIN_HIKING")

            /** Demanding mountain hiking. Partly exposed with fall hazard; hands occasionally needed; sure-footedness, good boots and alpine experience required. */
            public val DemandingMountainHiking: TrailDifficultyType = TrailDifficultyType("DEMANDING_MOUNTAIN_HIKING")

            /** Alpine hiking. Often pathless with exposed scrambling; familiarity with exposed terrain and an ice axe may be required. */
            public val AlpineHiking: TrailDifficultyType = TrailDifficultyType("ALPINE_HIKING")

            /** Demanding alpine hiking. Frequent scrambling with climbing up to UIAA grade II; high alpine experience required. */
            public val DemandingAlpineHiking: TrailDifficultyType = TrailDifficultyType("DEMANDING_ALPINE_HIKING")

            /** Difficult alpine hiking. Sustained exposed scrambling and climbing; mountaineering equipment and experience required. */
            public val DifficultAlpineHiking: TrailDifficultyType = TrailDifficultyType("DIFFICULT_ALPINE_HIKING")

            /** mtb:scale=0. Solid surface, mostly gravel or packed dirt; no significant obstacles. */
            public val Mtb0: TrailDifficultyType = TrailDifficultyType("MTB_0")

            /** mtb:scale=1. Mostly solid with loose sections and small obstacles such as roots or stones. */
            public val Mtb1: TrailDifficultyType = TrailDifficultyType("MTB_1")

            /** mtb:scale=2. Loose surface with larger roots and stones; occasional dismounting expected. */
            public val Mtb2: TrailDifficultyType = TrailDifficultyType("MTB_2")

            /** mtb:scale=3. Slippery ground with large obstacles and switchbacks; advanced technique required. */
            public val Mtb3: TrailDifficultyType = TrailDifficultyType("MTB_3")

            /** mtb:scale=4. Loose scree, large boulders and tight switchbacks; expert technique required. */
            public val Mtb4: TrailDifficultyType = TrailDifficultyType("MTB_4")

            /** mtb:scale=5. Very steep and blocked with boulders and scree; extreme technique required. */
            public val Mtb5: TrailDifficultyType = TrailDifficultyType("MTB_5")

            /** mtb:scale=6. Barely rideable; sustained extreme obstacles and exposure. */
            public val Mtb6: TrailDifficultyType = TrailDifficultyType("MTB_6")
        }
    }

    /**
     * Classification of the kind of way a stretch of route follows: the character of
     * the path itself, rather than what it is paved with.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WayType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Unknown or unclassified way type. */
            public val Unknown: WayType = WayType("UNKNOWN")

            /** State road or highway. */
            public val StateRoad: WayType = WayType("STATE_ROAD")

            /** Standard road. */
            public val Road: WayType = WayType("ROAD")

            /** Urban street. */
            public val Street: WayType = WayType("STREET")

            /** Walking or hiking path. */
            public val Path: WayType = WayType("PATH")

            /** Unpaved track. */
            public val Track: WayType = WayType("TRACK")

            /** Designated cycleway. */
            public val Cycleway: WayType = WayType("CYCLEWAY")

            /** Pedestrian footway. */
            public val Footway: WayType = WayType("FOOTWAY")

            /** Stairs or steps. */
            public val Steps: WayType = WayType("STEPS")

            /** Ferry crossing. */
            public val Ferry: WayType = WayType("FERRY")

            /** Road under construction. */
            public val Construction: WayType = WayType("CONSTRUCTION")
        }
    }

    /**
     * A service or facility available at a waypoint. An amenity is a fact about the
     * place, independent of the icon its author chose to draw it with: a waypoint drawn
     * as a hut may offer WATER, HOT_MEAL and ACCOMMODATION, and the two vocabularies
     * never substitute for one another.
     *
     * Six members (VIEWPOINT, PARKING, TOILETS, SHELTER, CABLE_CAR and
     * ACCOMMODATION) are also POICategory members. That enum classifies what a place
     * is; this one lists what a waypoint offers. Matching names do not mean matching
     * meanings, so do not map one onto the other.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WaypointAmenity(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Drinking water available, from a tap, spring or refill point. */
            public val Water: WaypointAmenity = WaypointAmenity("WATER")

            /** Non-water drinks served, such as tea, coffee or soft drinks. */
            public val Drinks: WaypointAmenity = WaypointAmenity("DRINKS")

            /** Solid food available, without implying a cooked meal. */
            public val Food: WaypointAmenity = WaypointAmenity("FOOD")

            /** A cooked, hot meal is served. */
            public val HotMeal: WaypointAmenity = WaypointAmenity("HOT_MEAL")

            /** Light snacks, bars or fruit available. */
            public val Snacks: WaypointAmenity = WaypointAmenity("SNACKS")

            /** Staffed medical post with qualified personnel. */
            public val Medical: WaypointAmenity = WaypointAmenity("MEDICAL")

            /** First-aid kit or basic care available, not necessarily staffed. */
            public val FirstAid: WaypointAmenity = WaypointAmenity("FIRST_AID")

            /** Public toilets. */
            public val Toilets: WaypointAmenity = WaypointAmenity("TOILETS")

            /** Showers or washing facilities. */
            public val Showers: WaypointAmenity = WaypointAmenity("SHOWERS")

            /** Drop bags may be deposited or collected here. */
            public val DropBag: WaypointAmenity = WaypointAmenity("DROP_BAG")

            /** Luggage may be left in storage, beyond race drop bags. */
            public val LuggageStorage: WaypointAmenity = WaypointAmenity("LUGGAGE_STORAGE")

            /** Personal assistance or a support crew is permitted here. */
            public val Assistance: WaypointAmenity = WaypointAmenity("ASSISTANCE")

            /** A dedicated area to sit and recover. */
            public val RestArea: WaypointAmenity = WaypointAmenity("REST_AREA")

            /** Beds or overnight accommodation available. */
            public val Accommodation: WaypointAmenity = WaypointAmenity("ACCOMMODATION")

            /** Unstaffed shelter from weather, such as a bothy or refuge. */
            public val Shelter: WaypointAmenity = WaypointAmenity("SHELTER")

            /** Tents may be pitched here. */
            public val Camping: WaypointAmenity = WaypointAmenity("CAMPING")

            /** Picnic tables or an outdoor eating area. */
            public val Picnic: WaypointAmenity = WaypointAmenity("PICNIC")

            /** A shop where supplies can be bought. */
            public val Shop: WaypointAmenity = WaypointAmenity("SHOP")

            /** Vehicle parking available. */
            public val Parking: WaypointAmenity = WaypointAmenity("PARKING")

            /** A shuttle service serves this point. */
            public val Shuttle: WaypointAmenity = WaypointAmenity("SHUTTLE")

            /** Public transport, such as a bus or train, serves this point. */
            public val PublicTransport: WaypointAmenity = WaypointAmenity("PUBLIC_TRANSPORT")

            /** A cable car, gondola or chairlift serves this point. */
            public val CableCar: WaypointAmenity = WaypointAmenity("CABLE_CAR")

            /** Electric bike or vehicle charging available. */
            public val PowerCharging: WaypointAmenity = WaypointAmenity("POWER_CHARGING")

            /** Wireless internet access available. */
            public val Wifi: WaypointAmenity = WaypointAmenity("WIFI")

            /** Reliable mobile network coverage at this point. */
            public val MobileSignal: WaypointAmenity = WaypointAmenity("MOBILE_SIGNAL")

            /** Waste and recycling disposal available. */
            public val Waste: WaypointAmenity = WaypointAmenity("WASTE")

            /** Dogs or other pets are allowed here. */
            public val PetFriendly: WaypointAmenity = WaypointAmenity("PET_FRIENDLY")

            /** Reachable and usable by wheelchair. */
            public val WheelchairAccess: WaypointAmenity = WaypointAmenity("WHEELCHAIR_ACCESS")

            /** A notable view or scenic overlook. */
            public val Viewpoint: WaypointAmenity = WaypointAmenity("VIEWPOINT")

            /** Timing is recorded here, such as a chip mat or manual check-in. */
            public val TimingCheckpoint: WaypointAmenity = WaypointAmenity("TIMING_CHECKPOINT")
        }
    }

    /**
     * How a custom waypoint property's value should be rendered. The value is always
     * carried as a string; this says how to interpret it, so a renderer never has to
     * guess from the content.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WaypointPropertyFormat(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Plain text, rendered verbatim. */
            public val Text: WaypointPropertyFormat = WaypointPropertyFormat("TEXT")

            /** A number, rendered with the reader's locale grouping and decimal separator. */
            public val Number: WaypointPropertyFormat = WaypointPropertyFormat("NUMBER")

            /** A duration in seconds, zero or more, rendered as hours and minutes. */
            public val Duration: WaypointPropertyFormat = WaypointPropertyFormat("DURATION")

            /** A distance in meters, zero or more, rendered in the reader's length unit. */
            public val Distance: WaypointPropertyFormat = WaypointPropertyFormat("DISTANCE")

            /** An absolute URL, rendered as a link. */
            public val Url: WaypointPropertyFormat = WaypointPropertyFormat("URL")

            /** An RFC 3339 date-time, or a bare YYYY-MM-DD date, rendered in the reader's locale. */
            public val Date: WaypointPropertyFormat = WaypointPropertyFormat("DATE")
        }
    }

    /**
     * Severity level of a zone annotation.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ZoneLevel(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Informational notice with no hazard. */
            public val Info: ZoneLevel = ZoneLevel("INFO")

            /** Previously reported issue that has been resolved. */
            public val Resolved: ZoneLevel = ZoneLevel("RESOLVED")

            /** Moderate hazard or caution area. */
            public val Warning: ZoneLevel = ZoneLevel("WARNING")

            /** Serious hazard or danger area. */
            public val Danger: ZoneLevel = ZoneLevel("DANGER")
        }
    }

    /**
     * How a zone's area was drawn or computed. Every mode stores a single Polygon;
     * the mode records the tool that produced it, so a client can reopen the zone in
     * the same editor. A zone computed from a source is always POLYGON.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ZoneMode(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Drawn vertex by vertex, or computed from a `source`. */
            public val Polygon: ZoneMode = ZoneMode("POLYGON")

            /** A circle around a center, stored as the polygon that traces it. */
            public val Circle: ZoneMode = ZoneMode("CIRCLE")

            /** A sensor's coverage band between two radii, stored as the polygon that traces it. */
            public val Sensor: ZoneMode = ZoneMode("SENSOR")

            /** A pie slice swept from a center, stored as the polygon that traces it. */
            public val Sector: ZoneMode = ZoneMode("SECTOR")

            /** Drawn freehand. */
            public val Freehand: ZoneMode = ZoneMode("FREEHAND")
        }
    }
}
