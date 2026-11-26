// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

package com.mountaya.client.generated

/** Data is the operations of the Data API, which is un-namespaced. Each returns its result type whole, and a field the caller did not select is null in it. */
public class Data internal constructor(
    private val sender: com.mountaya.client.OperationSender,
) {
    /**
     * Analyze surface and way type of an existing geometry. An analysis the routing
     * engine failed on in part is answered as `analysis` states.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun analyzeGeometry(
        input: DataSchema.AnalyzeGeometryInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.AnalyzeGeometryResult {
        val operation = DataOperations.analyzeGeometry
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.analyzeGeometry,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.AnalyzeGeometryInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.AnalyzeGeometryResult.serializer())
    }

    /**
     * Compute directions between waypoints for a given activity.
     * Returns one or more routes with distance, duration, geometry, and analysis.
     *
     * A waypoint the routing engine cannot route is refused as `waypoints` states,
     * and every other failure of the engine is INTERNAL_ERROR.
     * BACKCOUNTRY_SKIING has no routing at all, and GRAVEL_BIKE and MOUNTAIN_BIKE
     * are unavailable: all three are VALIDATION_FAILED at ["input", "activity"].
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun directions(
        input: DataSchema.DirectionsInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.DirectionsResult {
        val operation = DataOperations.directions
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.directions,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.DirectionsInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.DirectionsResult.serializer())
    }

    /**
     * Prepare a download of an itinerary in a route file format.
     *
     * It returns a short-lived signed URL rather than the file itself. The bytes of a
     * GPX or FIT are of no use to an assistant reading a response, and inlining them
     * would spend the whole reply on a payload nobody reads; a URL can be handed
     * straight to the user or fetched by a client that wants the file.
     *
     * What may be exported is what `itinerary` serves. An itinerary of your key's
     * own organization, public or private, is exported as its current draft under
     * a session token naming a member, provided the organization holds Map
     * Publishing: the draft of an organization whose agreement does not include it
     * is refused with PAYMENT_REQUIRED, and no URL is minted. Under a session token
     * minted without `user_id`, such as the one an Embedded Studio's read-only
     * viewer runs on, your own organization's itinerary, public or private, is
     * exported as it was last published instead, never as its draft: one never
     * published or taken down, or whose organization's agreement does not include
     * Map Publishing, answers NOT_FOUND. The token Mountaya's MCP server holds for
     * its own requests is the one exception, and exports the draft.
     * Another organization's public itinerary is exported as it was last
     * published, and one that organization has never published, or has taken down,
     * answers NOT_FOUND, as does one whose organization's agreement does not include
     * Map Publishing. An id no itinerary carries and an itinerary your key may not read
     * both answer NOT_FOUND.
     *
     * The URL names one itinerary in one format and nothing else, and stops working
     * ten minutes after it is minted. Every download checks again that the itinerary
     * is still served to the organization the URL was minted for, so a link to
     * another organization's itinerary stops working once that itinerary is made
     * private or taken down, or its organization's agreement stops including Map
     * Publishing. The URL also names the key that minted it, and every download
     * checks again that this key is still active and still of that organization:
     * revoking the key ends every URL it minted. A URL minted under a session token
     * naming a member is downloaded as that member only while they remain one: once
     * they are removed from the organization, the download is served what a URL
     * minted under a session token minted without `user_id` is, the itinerary as it
     * was last published, and 404 when it has no publication. Downloads beyond 60 a
     * minute from one address, an IPv6 address counted by its /64 prefix, answer
     * 429, and so do downloads beyond 6,000 a minute of the URLs one organization's
     * keys minted, or beyond 3,000 a minute of the URLs one person signed in to
     * Mountaya's web application minted, counted once a link is known to be genuine
     * and before the file is rendered. An expired or altered link, or one whose key has been revoked,
     * answers 404. The refusal a mint gives for the itinerary itself is given again by the
     * download when it has come to apply since, as it is rather than as that 404: a
     * plain 402 PAYMENT_REQUIRED for a draft whose organization does not hold Map
     * Publishing. Nothing is stored: this creates no share and leaves no state behind.
     *
     * A personal itinerary, made from an individual account and owned by no
     * organization, is never exported and no URL is minted: your key reads it as
     * published, as it reads another organization's public itinerary, and a personal
     * itinerary is never published, so it answers NOT_FOUND, public or private, like
     * any other itinerary your key may not read.
     *
     * An itinerary of more than 500,000 positions, every position of its segments'
     * geometry, the vias of its routed lines included, plus one per waypoint, is
     * more than one answer carries: it is refused with PAYLOAD_TOO_LARGE at
     * ["input"], naming none of its fields, and no URL is minted. The positions are
     * counted over the whole itinerary, after every other refusal, NOT_FOUND
     * included, and the download refuses it the same way, with a 413, once the
     * itinerary has grown past that since the URL was minted.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun exportItinerary(
        input: DataSchema.ExportItineraryInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.ExportItineraryResult {
        val operation = DataOperations.exportItinerary
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.exportItinerary,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.ExportItineraryInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.ExportItineraryResult.serializer())
    }

    /**
     * Forward geocode a free-text query into a list of named places. Returns
     * cities, regions, natural features, and addresses ranked by relevance.
     *
     * Every parameter the geocoder is asked with is checked or built by this API, so
     * a failure of the geocoder, its refusal of a request included, is
     * INTERNAL_ERROR, never the caller's to fix.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun geocode(
        input: DataSchema.GeocodeInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.GeocodeResult {
        val operation = DataOperations.geocode
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.geocode,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.GeocodeInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.GeocodeResult.serializer())
    }

    /**
     * Compute isochrones (reachability polygons) from one or more locations for a
     * given activity. Returns polygons representing areas reachable within the
     * specified range.
     *
     * The input's own bounds (one or two locations, one to six ranges, each range
     * at most 7,200 seconds or 25,000 meters, and a speed that stretches no time
     * range past 7,200 seconds) are checked first, each refusal at the field it
     * names, and then each location is placed on the activity's network, as
     * `locations` states. Any failure of the routing engine after that is
     * INTERNAL_ERROR, a ceiling of its own set below these bounds included.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun isochrones(
        input: DataSchema.IsochronesInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.IsochronesResult {
        val operation = DataOperations.isochrones
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.isochrones,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.IsochronesInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.IsochronesResult.serializer())
    }

    /**
     * Read a single itinerary by identifier, with its ordered segments and waypoints.
     *
     * What is served depends on who owns it. An itinerary of your key's own
     * organization, public or private, is served as its current draft under a
     * session token naming a member. Under a session token minted without
     * `user_id`, such as the one an Embedded Studio's read-only viewer runs on, it
     * is served as it was last published, exactly as a bare publishable key is
     * served it on the Map Studio API, and one never published, taken down, or
     * whose organization's agreement does not include Map Publishing answers
     * NOT_FOUND; the token Mountaya's MCP server holds for its own requests is the
     * one exception, and is served the draft. Another organization's public
     * itinerary ('anyone with the link') is served as it was last published, and
     * one that organization has never published answers NOT_FOUND, as does one
     * whose organization's agreement does not include Map Publishing;
     * another organization's private itinerary is never served. A
     * personal itinerary, made from an individual account and owned by no
     * organization, is readable only when it is public, and then exactly like
     * another organization's public itinerary. An id no itinerary carries and an
     * itinerary you may not read both answer NOT_FOUND, so the answer never confirms
     * that an id exists.
     *
     * An itinerary of more than 500,000 positions, every position of its segments'
     * geometry, the vias of its routed lines included, plus one per waypoint, is
     * more than one answer carries: it is refused with PAYLOAD_TOO_LARGE at
     * ["input"], naming none of its fields, as `exportItinerary` refuses it. The
     * positions are counted over the whole itinerary before anything is loaded,
     * whatever the selection asks for, and the refusal comes after every other,
     * NOT_FOUND included. Within that ceiling, the geometry and the points are
     * decoded only when the selection asks for them, which changes nothing in what
     * is answered.
     *
     * This is the companion to the itineraries target of `search`, which returns
     * references only. Pair it with `terrainProfile` or `analyzeGeometry` to answer
     * questions about the ground a saved route actually covers, sending a segment's
     * LineString positions reduced to [longitude, latitude], as
     * `ItinerarySegment.geometry` describes.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun itinerary(
        input: DataSchema.ItineraryInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.ItineraryResult {
        val operation = DataOperations.itinerary
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.itinerary,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.ItineraryInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.ItineraryResult.serializer())
    }

    /**
     * Compute a time-distance matrix between locations for a given activity.
     * Returns duration and/or distance grids between all source-destination pairs.
     *
     * The costs are the routing engine's over its whole network: unlike
     * `directions`, they do not leave out the ways an activity avoids, so a cost may
     * cross a ferry, a ford or steps the activity's own route would not take. Use
     * `directions` for a route exact to the activity.
     *
     * A matrix whose search the routing engine cannot hold at once, many locations
     * over a dense network, is computed one source at a time and answered exactly
     * as the whole matrix, recorded as one request, in a few seconds more (see
     * `locations`).
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun matrix(
        input: DataSchema.MatrixInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.MatrixResult {
        val operation = DataOperations.matrix
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.matrix,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.MatrixInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.MatrixResult.serializer())
    }

    /**
     * List points-of-interest matching one or more categories inside a bounding
     * box or a buffered geometry. Returns POIs ranked by relevance and, when a
     * buffered area is provided, annotated with their distance to the geometry.
     * Neither area is required: with neither, the categories are looked up with no
     * geographic filter.
     *
     * `limit` caps what the geocoder is asked for, and a POI outside the buffered
     * area, or a feature that maps to no POI category, is dropped after that, so
     * fewer than `limit` POIs can come back (see `buffered`).
     *
     * Every parameter the geocoder is asked with is checked or built by this API, so
     * a failure of the geocoder, its refusal of a request included, is
     * INTERNAL_ERROR, never the caller's to fix.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun pois(
        input: DataSchema.PoisInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.PoisResult {
        val operation = DataOperations.pois
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.pois,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.PoisInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.PoisResult.serializer())
    }

    /**
     * Reverse geocode a [longitude, latitude] point into a list of named places.
     * Returns the place closest to the point, optionally constrained by layer or
     * radius. A failure of the geocoder, its refusal of a lookup included, is
     * INTERNAL_ERROR, never the caller's to fix.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun reverseGeocode(
        input: DataSchema.ReverseGeocodeInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.ReverseGeocodeResult {
        val operation = DataOperations.reverseGeocode
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.reverseGeocode,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.ReverseGeocodeInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.ReverseGeocodeResult.serializer())
    }

    /**
     * Unified full-text search across geocoded places, POIs, ways, and Mountaya
     * entities (itineraries, collections, zones).
     *
     * Each enabled target runs on its own: a failed target leaves its section null
     * and adds an entry to `errors`, and `SearchResult` says how a failure is
     * answered and counted. A malformed `focus` or `bbox` fails the request. A feature
     * found by more than one of places, pois and ways is returned once, in `ways`
     * first, then `pois`.
     *
     * The entity targets list your key's own organization's entities only, never
     * another organization's or a personal one. A session token naming a member
     * lists them public and private, an itinerary or a zone as its draft. A
     * session token minted without `user_id` lists what a bare publishable key
     * lists: public itineraries and zones currently published, each as last
     * published, and public collections. The token Mountaya's MCP server holds
     * lists as a member's does.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope through
     * Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun search(
        input: DataSchema.SearchInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.SearchResult {
        val operation = DataOperations.search
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.search,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.SearchInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.SearchResult.serializer())
    }

    /**
     * Generate route suggestions matching distance constraints for a given
     * activity. Returns multiple route candidates with elevation data, allowing
     * the client to select based on elevation gain, distance, or shape preference.
     *
     * Suggested routes NEVER cross a tollway or an access-restricted way. There is
     * no opt-out: proposing somewhere to go is a different act from a user
     * deliberately routing across such a way, which `directions` still allows. Every
     * candidate's `analysis.advisories` therefore reports no hazard. A request no
     * candidate survives fails as `start` states, and `routes` says when fewer come
     * back than were asked for.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun suggestions(
        input: DataSchema.SuggestionsInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.SuggestionsResult {
        val operation = DataOperations.suggestions
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.suggestions,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.SuggestionsInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.SuggestionsResult.serializer())
    }

    /**
     * Sample the surface over a rectangular grid inside a bounding box, for area
     * reconnaissance: finding high ground, a steep band, or a slope facing a given
     * direction before routing through it.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun terrainGrid(
        input: DataSchema.TerrainGridInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.TerrainGridResult {
        val operation = DataOperations.terrainGrid
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.terrainGrid,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.TerrainGridInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.TerrainGridResult.serializer())
    }

    /**
     * Sample the surface along a geometry at a fixed interval, producing an elevation
     * profile as data. Use it to answer how steep a route is and where the hard
     * sections sit.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun terrainProfile(
        input: DataSchema.TerrainProfileInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.TerrainProfileResult {
        val operation = DataOperations.terrainProfile
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.terrainProfile,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.TerrainProfileInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.TerrainProfileResult.serializer())
    }

    /**
     * Sample elevation, slope, and aspect at standalone [longitude, latitude] points.
     * Use it for readings at known coordinates; for an existing line use terrainProfile,
     * and to search an area for features you have not located use terrainGrid.
     *
     * Requires a publishable key with the DATA scope, or with the MCP scope when the
     * call arrives through Mountaya's MCP server, and a session token.
     *
     * @param input The operation's input.
     * @param select The fields to fetch, checked against the operation's field tree before anything is sent, or null for every field.
     * @param options What this call overrides of its client's configuration.
     */
    public suspend fun terrainSamples(
        input: DataSchema.TerrainSamplesInput,
        select: com.mountaya.client.FieldSelection? = null,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
    ): DataSchema.TerrainSamplesResult {
        val operation = DataOperations.terrainSamples
        val call =
            com.mountaya.client.DataCall(
                facts = OperationTable.Data.terrainSamples,
                operation = operation,
                document = operation.document(select),
                input = com.mountaya.client.wireJson.encodeToString(DataSchema.TerrainSamplesInput.serializer(), input),
                options = options,
            )
        return sender.query(call, DataSchema.TerrainSamplesResult.serializer())
    }
}
