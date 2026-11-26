// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

package com.mountaya.client.generated

/** Tiles is the operations of the Tile API, which is un-namespaced. */
public class Tiles internal constructor(
    private val sender: com.mountaya.client.OperationSender,
) {
    /**
     * Retrieve the tile specification for a collection.
     *
     * Returns the tile specification (sources and layers) for rendering a
     * collection on a MapLibre GL map. The response includes tile URLs, layer
     * configuration, paint properties, and zoom range constraints needed to add
     * the collection as a vector source.
     *
     * Collection tiles carry zone and itinerary layers. Zone layers are painted by
     * zone level (info, warning, danger, resolved); itinerary layers take their
     * color per feature, and a feature that carries none falls back to a fixed
     * default color, the last argument of the `coalesce` in the layer's `paint`.
     *
     * The declared layers are narrowed by the resolved `with_zones` and
     * `with_itineraries` toggles, whatever the credential, and on a
     * publishable-key request those toggles are clamped to the organization's
     * safety rules, so the specification never declares a layer the toggles
     * switch off. They are not narrowed by what the credential may see: the tile
     * draws only the members this credential could list, so the zone layer
     * declared to a publishable key without a session token naming a member
     * draws the collection's published public zones alone, and every layer of
     * another organization's collection stays empty.
     *
     * The answer also carries `bbox`, the extent of the itineraries the tiles
     * draw for this same credential, the same `surface` and the same
     * `with_itineraries`, so an embed opens on the whole collection: fit the
     * map's camera to it once the sources are registered. Zones do not count
     * toward it, it is not clipped to the organization's geographic boundaries,
     * and it is null when the tiles draw no itinerary. Ask for the
     * specification with the `surface` the tiles are asked for with, so the
     * two agree.
     *
     * A publishable key needs the `tiles` scope, or `publishing` on a request from
     * a page on Mountaya's own domain. A share token carries its own authority and
     * needs none.
     *
     * @param collection_id Identifier of the collection the operation acts on. A collection this
     * credential does not reach is a 404 rather than a 403, and so is one that does
     * not exist, so the parameter cannot be used to learn which collection ids
     * exist.
     * @param options What this call overrides of its client's configuration.
     * @param parameters Sets the optional parameters, each by assigning it.
     */
    public suspend fun getCollectionSpecification(
        collection_id: kotlin.String,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
        parameters: TileSchema.GetCollectionSpecificationParameters.() -> kotlin.Unit = {},
    ): com.mountaya.client.Response<TileSchema.TileSpecification, kotlinx.serialization.json.JsonObject?> {
        val given = TileSchema.GetCollectionSpecificationParameters().apply(parameters)
        val call =
            com.mountaya.client.OperationCall(
                facts = OperationTable.Tiles.getCollectionSpecification,
                path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/specification",
                query =
                    kotlin.collections.buildList {
                        given.slot0?.let { add("surface" to it.value) }
                        given.slot1?.let { add("with_zones" to it.toString()) }
                        given.slot2?.let { add("with_itineraries" to it.toString()) }
                    },
                headers = kotlin.collections.emptyList(),
                body = com.mountaya.client.RequestBody.Empty,
                options = options,
            )
        return sender.send(call, TileSchema.TileSpecification.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
    }

    /**
     * Retrieve the tile specification for the organization's itineraries.
     *
     * Returns the tile specification (sources and layers) for rendering the
     * organization's itineraries on a MapLibre GL map. The response includes
     * tile URLs, layer configuration, paint properties, and zoom range
     * constraints needed to add itineraries as a vector source.
     *
     * Itinerary tiles contain three source layers: `itinerary_lines` for segment
     * line geometries, `itinerary_points` for point geometries and
     * `itinerary_restrictions` for the restricted stretches of a segment, which
     * no layer of this specification draws (see the tile route). The
     * `itinerary_points` layer carries both true waypoints and the per-itinerary
     * start and end markers, all rendered by a single symbol layer keyed on
     * `section-icon-<icon>-<hex>`. Every feature exposes `icon` and `color`, with
     * `icon = 'flag-start' | 'flag-end'` for endpoint markers. Endpoint marker
     * features also carry an informational `marker_role` (`start` / `end`) that
     * is absent on waypoints. The itineraries the tiles draw are the ones
     * `getItineraries` lists for the same credential: the organization's public
     * itineraries as last published under a key alone or with a session token
     * minted without `user_id`; its public and private itineraries as drafts when
     * a session token naming a member accompanies the key; and none under a share
     * token.
     *
     * A publishable key needs the `tiles` scope, or `publishing` on a request from
     * a page on Mountaya's own domain. A share token carries its own authority and
     * needs none.
     *
     * @param options What this call overrides of its client's configuration.
     * @param parameters Sets the optional parameters, each by assigning it.
     */
    public suspend fun getItinerariesSpecification(
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
        parameters: TileSchema.GetItinerariesSpecificationParameters.() -> kotlin.Unit = {},
    ): com.mountaya.client.Response<TileSchema.TileSpecification, kotlinx.serialization.json.JsonObject?> {
        TileSchema.GetItinerariesSpecificationParameters().apply(parameters)
        val call =
            com.mountaya.client.OperationCall(
                facts = OperationTable.Tiles.getItinerariesSpecification,
                path = "/v1/itineraries/specification",
                query = kotlin.collections.emptyList(),
                headers = kotlin.collections.emptyList(),
                body = com.mountaya.client.RequestBody.Empty,
                options = options,
            )
        return sender.send(call, TileSchema.TileSpecification.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
    }

    /**
     * Retrieve the tile specification for a single itinerary.
     *
     * Returns the tile specification (sources and layers) for rendering a single
     * itinerary on a MapLibre GL map. The response includes tile URLs, layer
     * configuration, paint properties, and zoom range constraints needed to add
     * the itinerary as a vector source.
     *
     * Itinerary tiles contain three source layers: `itinerary_lines` for line
     * geometries, `itinerary_points` for point geometries (waypoints and the
     * per-itinerary endpoint markers), and `itinerary_restrictions` for the
     * restricted stretches of a segment, which no layer of this specification
     * draws (see the tile route).
     *
     * A publishable key needs the `tiles` scope, or `publishing` on a request from
     * a page on Mountaya's own domain. A share token carries its own authority and
     * needs none.
     *
     * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
     * credential does not reach is a 404 rather than a 403, and so is one that does
     * not exist, so the parameter cannot be used to learn which itinerary ids exist.
     * @param options What this call overrides of its client's configuration.
     * @param parameters Sets the optional parameters, each by assigning it.
     */
    public suspend fun getItinerarySpecification(
        itinerary_id: kotlin.String,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
        parameters: TileSchema.GetItinerarySpecificationParameters.() -> kotlin.Unit = {},
    ): com.mountaya.client.Response<TileSchema.TileSpecification, kotlinx.serialization.json.JsonObject?> {
        TileSchema.GetItinerarySpecificationParameters().apply(parameters)
        val call =
            com.mountaya.client.OperationCall(
                facts = OperationTable.Tiles.getItinerarySpecification,
                path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/specification",
                query = kotlin.collections.emptyList(),
                headers = kotlin.collections.emptyList(),
                body = com.mountaya.client.RequestBody.Empty,
                options = options,
            )
        return sender.send(call, TileSchema.TileSpecification.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
    }

    /**
     * Retrieve the tile specification for a map overlay.
     *
     * Returns the tile specification (sources and layers) for rendering a map
     * overlay on a MapLibre GL map. The response includes tile URLs, layer
     * configuration, paint properties, legend data with units, and zoom range
     * constraints needed to add the overlay as a source.
     *
     * Overlays can be either vector or raster sources depending on the data type.
     * The specification includes legend information with color scales and unit
     * labels (e.g., km/h for wind speed, meters for snow depth).
     *
     * The optional `organization_id` names the studio scope the overlay is drawn
     * for, and the URL templates the specification returns carry it on to every
     * tile.
     *
     * A publishable key needs the `tiles` scope, or `publishing` on a request from
     * a page on Mountaya's own domain. A share token carries its own authority and
     * needs none.
     *
     * @param overlay Overlay slug, such as `wind` or `snowdepth`. It is the `slug` this API's
     * `getTileLayers`, and the Map Studio API's `getLayers`, report for each
     * overlay, and it carries that field's shape
     * exactly: lowercase letters, digits and single hyphens. Which overlays a
     * publishable key or a share token may draw is set by the organization's
     * overlay allowlist safety rule, and an overlay outside it answers 403 rather
     * than 404. The allowlist starts empty on a new organization, which admits no
     * overlay, so every slug answers 403 under a key until the allowlist names
     * slugs or `*`, which Mountaya sets as the organization's agreement states.
     * From Mountaya's web application the studio scope `organization_id` names
     * decides instead, and an overlay outside it answers 403 as well. Read the
     * matching specification endpoint for the ones it serves.
     * @param options What this call overrides of its client's configuration.
     * @param parameters Sets the optional parameters, each by assigning it.
     */
    public suspend fun getOverlaySpecification(
        overlay: kotlin.String,
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
        parameters: TileSchema.GetOverlaySpecificationParameters.() -> kotlin.Unit = {},
    ): com.mountaya.client.Response<TileSchema.TileSpecification, kotlinx.serialization.json.JsonObject?> {
        val given = TileSchema.GetOverlaySpecificationParameters().apply(parameters)
        val call =
            com.mountaya.client.OperationCall(
                facts = OperationTable.Tiles.getOverlaySpecification,
                path = "/v1/overlays/" + com.mountaya.client.encodePathSegment(overlay) + "/specification",
                query =
                    kotlin.collections.buildList {
                        given.slot0?.let { add("organization_id" to it) }
                    },
                headers = kotlin.collections.emptyList(),
                body = com.mountaya.client.RequestBody.Empty,
                options = options,
            )
        return sender.send(call, TileSchema.TileSpecification.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
    }

    /**
     * List the terrains, overlays and layers this API draws.
     *
     * Returns the map layer catalog this API draws from: the terrain base maps,
     * the elevation raster the terrains are shaded from, the grouped overlay
     * layers, and the two layers that draw itineraries and zones. It is the
     * catalog the Map Studio API's `getLayers` answers at its `/v1/layers`, in the
     * same shape, served here under the `tiles` scope, so a map drawn with the
     * Tile API alone finds every layer by its slug without Map Publishing.
     *
     * Under a publishable key or a share token, `overlays` lists only the
     * overlays the organization's overlay allowlist admits, the ones this API
     * draws for the credential, and a group left with none is dropped: the
     * allowlist starts empty on a new organization, so the groups are empty until
     * Mountaya sets it as the organization's agreement states. An overlay listed
     * here may still be drawn only at the present instant: the organization's
     * overlay time control decides whether one whose `capabilities.server.timestamp`
     * is true is drawn more than 15 minutes from the current instant, which this
     * catalog does not report. The terrains, the elevation raster and the
     * itinerary and zone layers are listed for every credential.
     *
     * The optional `organization_id` names the studio scope the catalog is drawn
     * for, as on the overlay routes. Under a publishable key or a share token it
     * may name only the credential's own organization and changes nothing. From
     * Mountaya's web application it chooses between the person's own space and an
     * organization they are a member of, and `overlays` lists every overlay in
     * either, so the studio can show the ones the scope does not draw, locked.
     * This API draws the points of interest alone in the person's own space and
     * in an organization that does not hold Map Studio, and every overlay in one
     * that does, and the itinerary and zone layers draw the scope's own
     * itineraries and zones. When it is sent, the tile URL template of every
     * overlay and of the itinerary and zone layers carries it on, so the tiles a
     * map fetches are drawn in the scope the catalog was served for.
     *
     * Like a specification, the catalog is never recorded, and it counts toward
     * the per-minute bounds "What is counted" lists for its surface.
     *
     * A publishable key needs the `tiles` scope, or `publishing` on a request from
     * a page on Mountaya's own domain. A share token carries its own authority and
     * needs none.
     *
     * @param options What this call overrides of its client's configuration.
     * @param parameters Sets the optional parameters, each by assigning it.
     */
    public suspend fun getTileLayers(
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
        parameters: TileSchema.GetTileLayersParameters.() -> kotlin.Unit = {},
    ): com.mountaya.client.Response<TileSchema.Layers, kotlinx.serialization.json.JsonObject?> {
        val given = TileSchema.GetTileLayersParameters().apply(parameters)
        val call =
            com.mountaya.client.OperationCall(
                facts = OperationTable.Tiles.getTileLayers,
                path = "/v1/tile-layers",
                query =
                    kotlin.collections.buildList {
                        given.slot0?.let { add("organization_id" to it) }
                    },
                headers = kotlin.collections.emptyList(),
                body = com.mountaya.client.RequestBody.Empty,
                options = options,
            )
        return sender.send(call, TileSchema.Layers.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
    }

    /**
     * Retrieve the tile specification for the organization's zones.
     *
     * Returns the tile specification (sources and layers) for rendering zone
     * annotations on a MapLibre GL map. The response includes tile URLs, layer
     * configuration, paint properties, and zoom range constraints needed to add
     * zones as a vector source.
     *
     * Zone tiles carry one source-layer, `fills`, holding each zone's polygon,
     * color-coded by severity level (info, warning, danger, resolved): one
     * feature for each of its polygons, clipped exactly to a window from two
     * tiles' width before the tile's square to one past it, so a feature may
     * extend past the square and is not the zone's whole outline. A zone stored
     * before outlines were checked on write is approximated where it passes
     * through that window many times until Mountaya has checked it, shortly
     * after the release, and for good when its outline breaks a rule a write now
     * holds it to: a valid polygon of at most 10,000 positions that no meridian
     * or parallel crosses more than 512 times.
     *
     * A zone drawn from its draft carries its `id`, `name`, `level`, `mode`,
     * `is_private`, `revision` and `created_by` as properties, and its
     * `organization_id`, `description`, `start_at` and `end_at` when it has them: a
     * personal zone, owned by no organization, carries no `organization_id`. A zone
     * drawn from its publication carries what a published itinerary does: its
     * `id`, `name`, `level` and `mode`, and its `description`, `start_at` and
     * `end_at` when the publication has them, never who made it, which
     * organization holds it, its privacy or its revision. The two bounds are Unix
     * seconds, so a style can filter on a zone's validity window. The
     * zones the tiles draw are the ones `getZones` lists for the same
     * credential: the organization's public zones as last published under a key
     * alone or with a session token minted without `user_id`; its public and
     * private zones as drafts when a session token naming a member accompanies
     * the key; and none under a share token.
     *
     * A publishable key needs the `tiles` scope, or `publishing` on a request from
     * a page on Mountaya's own domain. A share token carries its own authority and
     * needs none.
     *
     * @param options What this call overrides of its client's configuration.
     * @param parameters Sets the optional parameters, each by assigning it.
     */
    public suspend fun getZonesSpecification(
        options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
        parameters: TileSchema.GetZonesSpecificationParameters.() -> kotlin.Unit = {},
    ): com.mountaya.client.Response<TileSchema.TileSpecification, kotlinx.serialization.json.JsonObject?> {
        TileSchema.GetZonesSpecificationParameters().apply(parameters)
        val call =
            com.mountaya.client.OperationCall(
                facts = OperationTable.Tiles.getZonesSpecification,
                path = "/v1/zones/specification",
                query = kotlin.collections.emptyList(),
                headers = kotlin.collections.emptyList(),
                body = com.mountaya.client.RequestBody.Empty,
                options = options,
            )
        return sender.send(call, TileSchema.TileSpecification.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
    }
}
