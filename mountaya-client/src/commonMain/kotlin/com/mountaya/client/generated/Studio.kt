// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

package com.mountaya.client.generated

/** Studio is the `studio` operations of the REST API, one property per resource. */
public class Studio internal constructor(
    sender: com.mountaya.client.OperationSender,
) {
    /** The `studio.activities` operations. */
    public val activities: Studio.Activities = Studio.Activities(sender)

    /** The `studio.collab` operations. */
    public val collab: Studio.Collab = Studio.Collab(sender)

    /** The `studio.collections` operations. */
    public val collections: Studio.Collections = Studio.Collections(sender)

    /** The `studio.itineraries` operations. */
    public val itineraries: Studio.Itineraries = Studio.Itineraries(sender)

    /** The `studio.layers` operations. */
    public val layers: Studio.Layers = Studio.Layers(sender)

    /** The `studio.organizations` operations. */
    public val organizations: Studio.Organizations = Studio.Organizations(sender)

    /** The `studio.zones` operations. */
    public val zones: Studio.Zones = Studio.Zones(sender)

    /** Activities is the `studio.activities` operations of the REST API. */
    public class Activities internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * List all activity categories and their activities.
         *
         * Returns all available activities grouped by category. Each activity includes
         * its capabilities (e.g., routing support, elevation profiles). It is a
         * catalog, the same for every caller, so it is not paged: every category
         * comes back at once.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * Requires a publishable key with the `publishing` scope, or the
         * `studio-embedded` scope that includes it, or a share token: a share view
         * names each segment's activity from the same catalog an embed does.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getActivities(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetActivitiesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<RestSchema.CategoryActivities>, kotlinx.serialization.json.JsonObject?> {
            RestSchema.GetActivitiesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getActivities,
                    path = "/v1/activities",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(RestSchema.CategoryActivities.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Collab is the `studio.collab` operations of the REST API. */
    public class Collab internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Stream one itinerary's collaboration session over SSE.
         *
         * Opens a Server-Sent Events (SSE) stream of one itinerary's real-time editing
         * session, so every member with the itinerary open converges on the same view
         * and sees who else is editing. The response Content-Type is
         * `text/event-stream` and the connection stays open until the client
         * disconnects or the credential it was opened with ends.
         *
         * The member's access is checked again about every ten seconds while the
         * stream is open. When the member does not reach the itinerary, the stream
         * ends, and reopening it answers 404, or 402 when the organization does not
         * hold Map Studio. A member who holds no write role keeps watching, and any
         * soft lock or uncommitted draft they hold is released and cleared for the
         * other members (`lock.released`, `draft.clear`).
         *
         * On connect the server sends, directly to this connection, a
         * `presence.snapshot` of the members currently present and a `lock.snapshot`
         * of every soft lock already held, then broadcasts a `presence.join` to the
         * session, which reaches this connection too, as every signal of the
         * member's own comes back to it. It then re-sends a fresh `presence.snapshot` and
         * `lock.snapshot` down this connection periodically (every few seconds); the
         * pair sent on connect can be up to one such interval old, and so can miss
         * this connection's own arrival. A peer that disconnects uncleanly, from a
         * killed tab or a dropped network that sends no `presence.leave` or
         * `lock.released`, keeps its entries until they lapse, 45 seconds for its
         * presence and 60 seconds for its locks after its stream ends, so its stale
         * avatar and lock badge clear on the first snapshot after that: within about
         * a minute. Each event is framed as `id: <stream-id>`, `event: <type>`, and
         * `data: <json>`; only the durable change events carry an `id:`, the
         * live-only signals do not.
         *
         * The stream ends with its credential. When the session token reaches its
         * expiry, or the key is revoked, loses the `studio-embedded` scope or its
         * organization is canceled, the stream writes a last `reconnect` event,
         * whose `data:` carries `reason` `credential-expired` or
         * `credential-revoked`, and closes. That is not a failure: reopen at once
         * with a fresh session token and `Last-Event-ID`, and the durable changes
         * written in between are replayed. A `401` on the reopen means the
         * credential itself is gone, and the stream ends there. A `HEAD` on this
         * path opens no subscription.
         *
         * It also ends when the server cannot keep delivering its events, because
         * the reader fell behind or the server's subscription was lost: it writes a
         * last `reconnect` event whose `reason` is `interrupted`, and closes. Reopen
         * at once with `Last-Event-ID`, and the durable events written in between
         * are replayed.
         *
         * The `event:` type is one of:
         *
         * - `itinerary.changed`, `segment.changed`, `waypoint.changed`: a durable
         *   structural change to the itinerary or one of its segments/waypoints.
         *
         * - `presence.snapshot`: the authoritative set of members present, sent on
         *   connect and re-sent periodically; a client replaces its roster wholesale
         *   from it.
         *
         * - `lock.snapshot`: the authoritative full set of held locks, sent on connect
         *   and re-sent periodically; a client replaces its lock state wholesale from
         *   it, so a lock held by an uncleanly disconnected peer clears once its
         *   registry entry lapses.
         *
         * - `presence.join`, `presence.leave`: a member joined or left the session.
         * - `selection`: a member's current selection (a segment or waypoint), or
         *   null.
         * - `cursor`: a member's cursor position, or null.
         * - `lock.acquired`, `lock.released`: a member took or released the soft edit
         *   lock on an entity (a live delta between snapshots).
         *
         * - `draft.update`, `draft.clear`: a member's uncommitted draft of an entity,
         *   or its clear.
         *
         * - `reconnect`: the credential the stream was opened with ended, or the
         *   server cannot keep delivering the stream, and the stream closes after
         *   it (see above).
         *
         * The `event:` type and the shape of the matching `data:` payload are the
         * CollabStreamEvent schema: it is a union discriminated on `type`, so every
         * frame kind this stream can send is stated once and cannot drift from the
         * list above. The union spans all three collaboration streams; the frames
         * named here are the subset this one carries.
         *
         * To resume after a dropped connection, the client reconnects with the
         * `Last-Event-ID` request header set to the last `id:` it received; the server
         * replays the durable change events after that id before resuming the live
         * feed. The live-only signals (presence, cursor, lock, draft) are never
         * replayed. Without the header, the server first replays every durable
         * change it still holds for the itinerary, so a fresh connection receives
         * recent changes it may already reflect; applying one again is harmless,
         * since each only names what to refetch. The stream keeps about the last
         * thousand changes, for an hour after the latest one, so a `Last-Event-ID`
         * older than that replays only what remains: after a long disconnect,
         * refetch the itinerary rather than rely on the replay. A `Last-Event-ID`
         * that is not a stream id (two numbers joined by a hyphen, the form of every
         * `id:`) is a **400**, answered before the stream opens.
         *
         * While the stream is open the server writes a comment (a line beginning
         * with a colon) every 15 seconds, whatever else the stream carries. A
         * comment is not an event: it has no `id:` and no `data:`, and a client
         * discards it. It keeps the connection, and any proxy in between, from
         * closing a quiet stream as idle.
         *
         * Any member of the key's organization may watch, whatever their role;
         * sending any signal through `postItineraryCollab`, a cursor or a selection
         * as much as a lock or a draft, needs a write role. A personal itinerary,
         * owned by no organization, has no collaboration session.
         *
         * Opening the stream is limited to 60 opens a minute per user and 1200 a
         * minute per organization, counted across every event stream, and every open
         * is a read made with a session token besides, limited to 1200 requests a
         * minute per user and 6000 a minute per organization; past any of them, the
         * open is refused with **429** and `Retry-After`, and no stream starts. A
         * reopen after a `reconnect` event counts as an open.
         *
         * At most 20 streams are held open at once for each user, 50 for each client
         * IP address and 1,000 for each organization, counted across every event
         * stream. An open past any of them is a `429` with `Retry-After`, and no
         * stream starts; a stream gives its place back when it ends.
         *
         * The `studio-embedded` scope is NOT the scope the resource itself reads
         * under. `getItinerary` takes `publishing`, and holding `publishing` does not
         * grant `studio-embedded`: a key carrying only `publishing` reads the resource
         * and is refused here with a `403`. The stream is scoped to `studio-embedded`
         * because it is the live editing session an embedded studio opens on behalf
         * of a member, not a change feed on a resource.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with any role,
         * whom the request acts as. A publishable key without a session token minted
         * for `studio-embedded` naming a user, a token minted for another surface
         * included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public fun getItineraryEvents(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetItineraryEventsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.EventStream<RestSchema.CollabStreamEvent> {
            RestSchema.GetItineraryEventsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getItineraryEvents,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/events",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.stream(call, RestSchema.CollabStreamEvent.serializer())
        }

        /**
         * Stream an organization's list events over SSE.
         *
         * Opens a Server-Sent Events (SSE) stream of the changes to the target
         * organization's itineraries, zones and collections, so every member
         * watching the studio sees its lists converge without polling. The path
         * names the key's own organization: a key is pinned to the organization it
         * was issued for, and naming another is a `403` whether or not it exists.
         * The response Content-Type is `text/event-stream` and the connection stays
         * open until the client disconnects or the credential it was opened with
         * ends. A `HEAD` on this path answers the stream's headers and opens no
         * subscription.
         *
         * The member's membership is checked again about every ten seconds while the
         * stream is open. When the member does not belong to the organization, the
         * stream ends, and reopening it answers 403, or 402 when the organization does
         * not hold Map Studio.
         *
         * A stream ends with its credential. When the session token reaches its
         * expiry, or the publishable key is revoked, loses the `studio-embedded`
         * scope or its organization is canceled, the stream writes one last record,
         * `event: reconnect` with `data: {"reason":"credential-expired"}` or
         * `data: {"reason":"credential-revoked"}`, and closes. That close is not a
         * failure: reopen at once with a fresh session token and the
         * `Last-Event-ID` last received, and the events written in between are
         * replayed. A `401` on that reopen means the credential itself is gone, and
         * the stream ends there.
         *
         * It also ends when the server cannot keep delivering its events, because
         * the reader fell behind or the server's subscription was lost: it writes a
         * last `reconnect` event whose `reason` is `interrupted`, and closes. Reopen
         * at once with `Last-Event-ID`, and the durable events written in between
         * are replayed.
         *
         * The stream carries only durable events that move a resource in or out of a
         * member's list, or change how it is filed: an itinerary, a zone or a
         * collection changing in the organization. It does not carry the editing
         * signals of one itinerary or one zone, which are on the editor streams,
         * `getItineraryEvents` and `getZoneEvents`. Each event is framed as
         * `id: <stream-id>`, `event: <type>`, and `data: <json>`.
         *
         * The `event:` type is one of:
         *
         * - `itinerary.changed`: an itinerary was created, updated, or deleted in the
         *   organization.
         *
         * - `zone.changed`: a zone was created, updated, or deleted in the
         *   organization.
         *
         * - `collection.changed`: a collection was created, updated, or deleted in the
         *   organization, or an itinerary or zone entered or left one. A collection
         *   has no editor stream of its own: it is a folder, so every change to it is
         *   a list concern and arrives here. The frame names the COLLECTION in `id`
         *   even when what moved was one of its members, because the consumer
         *   refetches the group; naming the itinerary that entered or left would be
         *   both ambiguous and useless. `itinerary_id` is the all-zero UUID and
         *   `revision` is always 0, since a collection is not revisioned.
         *
         * - `reconnect`: the last record, written when the credential ends or the
         *   server cannot keep delivering the stream. It carries no `id:` and is
         *   never replayed.
         *
         * The `event:` type and the shape of the matching `data:` payload are the
         * CollabStreamEvent schema: it is a union discriminated on `type`, so every
         * frame kind this stream can send is stated once and cannot drift from the
         * list above. The union spans all three collaboration streams; the four
         * frames named here are the subset this one carries, and the live-only
         * editing signals are not among them.
         *
         * To resume after a dropped connection, the client reconnects with the
         * `Last-Event-ID` request header set to the last `id:` it received; the server
         * replays events after that id from the organization's durable stream before
         * resuming the live feed. Without the header, the server first replays every
         * event it still holds for the organization, so a fresh connection receives
         * recent changes its lists may already reflect; applying one again is
         * harmless, since each only names what to refetch. The stream keeps about
         * the last thousand events, for an hour after the latest one, so a
         * `Last-Event-ID` older than that replays only what remains: after a long
         * disconnect, refetch the lists rather than rely on the replay. A
         * `Last-Event-ID` that is not a stream id (two numbers joined by a hyphen,
         * the form of every `id:`) is a **400**, answered before the stream opens.
         *
         * While the stream is open the server writes a comment (a line beginning
         * with a colon) every 15 seconds, whatever else the stream carries. A
         * comment is not an event: it has no `id:` and no `data:`, and a client
         * discards it. It keeps the connection, and any proxy in between, from
         * closing a quiet stream as idle.
         *
         * Opening the stream is limited to 60 opens a minute per user and 1,200 per
         * organization, and each open is also a read made with a session token, held to
         * the 1200 reads per user and the 6000 per organization per minute every such
         * read shares; over any of them, the open is a `429` carrying `Retry-After`,
         * and no stream starts.
         *
         * At most 20 streams are held open at once for each user, 50 for each client
         * IP address and 1,000 for each organization, counted across every event
         * stream. An open past any of them is a `429` with `Retry-After`, and no
         * stream starts; a stream gives its place back when it ends.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with any role,
         * whom the request acts as. A publishable key without a session token minted
         * for `studio-embedded` naming a user, a token minted for another surface
         * included, is refused with **401**.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public fun getOrganizationEvents(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetOrganizationEventsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.EventStream<RestSchema.CollabStreamEvent> {
            RestSchema.GetOrganizationEventsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationEvents,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/events",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.stream(call, RestSchema.CollabStreamEvent.serializer())
        }

        /**
         * Stream one zone's collaboration session over SSE.
         *
         * Opens a Server-Sent Events (SSE) stream of one zone's real-time editing
         * session, so every member with the zone open converges on the same view and
         * sees who else is editing. The response Content-Type is `text/event-stream`
         * and the connection stays open until the client disconnects or the
         * credential it was opened with ends. It is the zone twin of the itinerary
         * editor stream, `getItineraryEvents`. A `HEAD` on this path answers the
         * stream's headers and opens no subscription.
         *
         * The member's access is checked again about every ten seconds while the
         * stream is open. When the member does not reach the zone, the stream ends,
         * and reopening it answers 404, or 402 when the organization does not hold
         * Map Studio. A member who holds no write role keeps watching, and any soft
         * lock or uncommitted draft they hold is released and cleared for the other
         * members (`lock.released`, `draft.clear`).
         *
         * A stream ends with its credential. When the session token reaches its
         * expiry, or the publishable key is revoked, loses the `studio-embedded`
         * scope or its organization is canceled, the stream writes one last record,
         * `event: reconnect` with `data: {"reason":"credential-expired"}` or
         * `data: {"reason":"credential-revoked"}`, and closes. That close is not a
         * failure: reopen at once with a fresh session token and the
         * `Last-Event-ID` last received, and the durable changes written in between
         * are replayed. A `401` on that reopen means the credential itself is gone,
         * and the stream ends there.
         *
         * It also ends when the server cannot keep delivering its events, because
         * the reader fell behind or the server's subscription was lost: it writes a
         * last `reconnect` event whose `reason` is `interrupted`, and closes. Reopen
         * at once with `Last-Event-ID`, and the durable events written in between
         * are replayed.
         *
         * On connect the server sends, directly to this connection, a
         * `presence.snapshot` of the members currently present and a `lock.snapshot`
         * of every soft lock already held, then broadcasts a `presence.join` to the
         * session, which reaches this connection too, as every signal of the
         * member's own comes back to it. It then re-sends a fresh `presence.snapshot` and
         * `lock.snapshot` down this connection periodically (every few seconds); the
         * pair sent on connect can be up to one such interval old, and so can miss
         * this connection's own arrival. A peer that disconnects uncleanly, from a
         * killed tab or a dropped network that sends no `presence.leave` or
         * `lock.released`, keeps its entries until they lapse, 45 seconds for its
         * presence and 60 seconds for its locks after its stream ends, so its stale
         * avatar and lock badge clear on the first snapshot after that: within about
         * a minute. Each event is framed as `id: <stream-id>`, `event: <type>`, and
         * `data: <json>`; only the durable change events carry an `id:`, the
         * live-only signals and the closing `reconnect` do not.
         *
         * The `event:` type is one of:
         *
         * - `zone.changed`: a durable structural change to the zone.
         * - `presence.snapshot`: the authoritative set of members present, sent on
         *   connect and re-sent periodically; a client replaces its roster wholesale
         *   from it.
         *
         * - `lock.snapshot`: the authoritative full set of held locks, sent on connect
         *   and re-sent periodically; a client replaces its lock state wholesale from
         *   it, so a lock held by an uncleanly disconnected peer clears once its
         *   registry entry lapses.
         *
         * - `presence.join`, `presence.leave`: a member joined or left the session.
         * - `selection`: a member's current selection, or null.
         * - `cursor`: a member's cursor position, or null.
         * - `lock.acquired`, `lock.released`: a member took or released the soft edit
         *   lock on an entity (a live delta between snapshots).
         *
         * - `draft.update`, `draft.clear`: a member's uncommitted draft of an entity,
         *   or its clear.
         *
         * - `reconnect`: the last record, written when the credential ends or the
         *   server cannot keep delivering the stream.
         *
         * The `event:` type and the shape of the matching `data:` payload are the
         * CollabStreamEvent schema: it is a union discriminated on `type`, so every
         * frame kind this stream can send is stated once and cannot drift from the
         * list above. The union spans all three collaboration streams; the frames
         * named here are the subset this one carries.
         *
         * To resume after a dropped connection, the client reconnects with the
         * `Last-Event-ID` request header set to the last `id:` it received; the server
         * replays the durable change events after that id before resuming the live
         * feed. The live-only signals (presence, cursor, lock, draft) are never
         * replayed. Without the header, the server first replays every durable
         * change it still holds for the zone, so a fresh connection receives recent
         * changes it may already reflect; applying one again is harmless, since each
         * only names what to refetch. The stream keeps about the last thousand
         * changes, for an hour after the latest one, so a `Last-Event-ID` older than
         * that replays only what remains: after a long disconnect, refetch the zone
         * rather than rely on the replay. A `Last-Event-ID` that is not a stream id
         * (two numbers joined by a hyphen, the form of every `id:`) is a **400**,
         * answered before the stream opens.
         *
         * While the stream is open the server writes a comment (a line beginning
         * with a colon) every 15 seconds, whatever else the stream carries. A
         * comment is not an event: it has no `id:` and no `data:`, and a client
         * discards it. It keeps the connection, and any proxy in between, from
         * closing a quiet stream as idle.
         *
         * Any member of the key's organization may watch, whatever their role;
         * sending any signal through `postZoneCollab`, a cursor or a selection as
         * much as a lock or a draft, needs a write role. A personal zone, owned by no
         * organization, has no collaboration session.
         *
         * The `studio-embedded` scope is NOT the scope the resource itself reads
         * under. `getZone` takes `publishing`, and holding `publishing` does not grant
         * `studio-embedded`: a key carrying only `publishing` reads the resource and
         * is refused here with a `403`. The stream is scoped to `studio-embedded`
         * because it is the live editing session an embedded studio opens on behalf
         * of a member, not a change feed on a resource.
         *
         * Opening the stream is limited to 60 opens a minute per user and 1,200 per
         * organization, and each open is also a read made with a session token, held to
         * the 1200 reads per user and the 6000 per organization per minute every such
         * read shares; over any of them, the open is a `429` carrying `Retry-After`,
         * and no stream starts.
         *
         * At most 20 streams are held open at once for each user, 50 for each client
         * IP address and 1,000 for each organization, counted across every event
         * stream. An open past any of them is a `429` with `Retry-After`, and no
         * stream starts; a stream gives its place back when it ends.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with any role,
         * whom the request acts as. A publishable key without a session token minted
         * for `studio-embedded` naming a user, a token minted for another surface
         * included, is refused with **401**.
         *
         * @param zone_id Identifier of the zone the operation acts on.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public fun getZoneEvents(
            zone_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetZoneEventsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.EventStream<RestSchema.CollabStreamEvent> {
            RestSchema.GetZoneEventsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getZoneEvents,
                    path = "/v1/zones/" + com.mountaya.client.encodePathSegment(zone_id) + "/events",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.stream(call, RestSchema.CollabStreamEvent.serializer())
        }

        /**
         * Send a collaboration signal into an itinerary's session.
         *
         * Sends a live collaboration signal into an itinerary's real-time session,
         * fanned out over the editor stream (getItineraryEvents) to every connection
         * on it. The sender's own connections receive it too, recognizable by their
         * `user_id`, which is the sender's. The request body shape is selected by its
         * `type` discriminator:
         *
         * - `selection`: the current selection (a segment or waypoint), or null to
         *   clear it.
         * - `cursor`: the cursor position, or null to clear it.
         * - `lock.acquire`: take the soft edit lock on an entity; refused with 409
         *   when another member holds it. The lock lapses after 60 seconds unless
         *   the member keeps the editor stream open.
         * - `lock.release`: release the soft edit lock the member holds on an entity.
         * - `draft.update`: an uncommitted draft of an entity being edited; requires
         *   the member to already hold the entity's lock (409 otherwise).
         * - `draft.clear`: clear the member's uncommitted draft; requires the lock
         *   too.
         *
         * The lock and draft signals name an entity this session guards, one the
         * itinerary's writes check: a segment, a segment group or a waypoint. A zone
         * is locked in its own session, `postZoneCollab`; named here, it is the
         * `400` below and takes no lock.
         *
         * The server always stamps the user the session token names onto the
         * broadcast; a user id inside the body is never trusted. Every signal needs a
         * member with a write role, a `selection` or a `cursor` as much as a lock: a
         * viewer may watch the stream but not send into it. Beyond that, the
         * itinerary must be one the key's organization owns: another organization's,
         * public ones included, is the 404 below. The response is 200 with no data
         * once the signal is accepted.
         *
         * A signal states where the sender is rather than adding to anything: a
         * selection, a cursor, a lock the sender holds, a draft. Sending the same
         * signal again restates it, so the request is safe to repeat, and it takes
         * no `Idempotency-Key`: a retry needs no stored answer to be safe.
         *
         * Signals are limited to 1200 a minute per user, across every collaboration
         * session; past that, the request is refused with **429** and `Retry-After`.
         * A client that sends a signal on every pointer move throttles it to well
         * under that.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItineraryCollab(
            itinerary_id: kotlin.String,
            body: RestSchema.CollabSignalParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryCollabParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            RestSchema.PostItineraryCollabParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItineraryCollab,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/collab",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.CollabSignalParams.serializer(), body)),
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Send a collaboration signal into a zone's session.
         *
         * Sends a live collaboration signal into a zone's real-time session, fanned
         * out over the editor stream, `getZoneEvents`, to every connection on it. The
         * sender's own connections receive it too, recognizable by their `user_id`,
         * which is the sender's. It is the zone twin of `postItineraryCollab` and
         * reuses the same signal shapes. The request body shape is selected by its `type`
         * discriminator:
         *
         * - `selection`: the current selection, or null to clear it.
         * - `cursor`: the cursor position, or null to clear it.
         * - `lock.acquire`: take the soft edit lock on an entity; refused with 409
         *   when another member holds it. The lock lapses after 60 seconds unless
         *   the member keeps the editor stream open.
         * - `lock.release`: release the soft edit lock the member holds on an entity.
         * - `draft.update`: an uncommitted draft of an entity being edited; requires
         *   the member to already hold the entity's lock (409 otherwise).
         * - `draft.clear`: clear the member's uncommitted draft; requires the lock
         *   too.
         *
         * The lock and draft signals name the one entity this session guards, the
         * zone itself, of kind `zone`. A segment, a segment group or a waypoint is
         * locked in its itinerary's session, `postItineraryCollab`; named here, it
         * is the `400` below and takes no lock.
         *
         * The server always stamps the user the session token names onto the
         * broadcast; a user id inside the body is never trusted. Every signal needs a
         * member with a write role, a `selection` or a `cursor` as much as a lock: a
         * viewer may watch the stream but not send into it. Beyond that, `selection`
         * and `cursor` need the zone to be one this credential can view, and the lock
         * and draft signals need it to be one the member can edit. The response is 200
         * with no data once the signal is accepted.
         *
         * A signal sets live state rather than adding to it: sending the same one
         * again leaves the session as the first one left it, and taking a lock the
         * member already holds renews it. A lock on the zone itself also refuses `patchZone` from every
         * other member while it is held. That is why it takes no `Idempotency-Key`:
         * a signal sent twice has no second effect, so a retry needs no stored
         * answer to be safe.
         *
         * Signals are limited to 1200 per user per minute; over it, the signal is a
         * `429` carrying `Retry-After`, and it is not sent.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param zone_id Identifier of the zone the operation acts on.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postZoneCollab(
            zone_id: kotlin.String,
            body: RestSchema.CollabSignalParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostZoneCollabParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            RestSchema.PostZoneCollabParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postZoneCollab,
                    path = "/v1/zones/" + com.mountaya.client.encodePathSegment(zone_id) + "/collab",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.CollabSignalParams.serializer(), body)),
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Collections is the `studio.collections` operations of the REST API. */
    public class Collections internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Delete a collection.
         *
         * Permanently deletes the collection. The itineraries and zones it held are
         * NOT deleted: only the references linking them to this collection are, so
         * every resource survives in the organization that owns it. This action cannot
         * be undone.
         *
         * Deleting is an administrative act on a shared folder, so it takes the same
         * grant as renaming one: a write role, and on top of it either the
         * collection's authorship or `owner` / `admin` in the owning organization. A
         * `contributor`, who may add and remove the collection's contents, cannot
         * delete a folder it did not create: changing what a collection CONTAINS and
         * changing what it IS are separate permissions on purpose. Any active share
         * link is disabled with it, and every URL built from that link stops
         * resolving.
         *
         * Send the collection's `etag` as `If-Match` to make the delete conditional:
         * a collection changed since that read is a `412`, and nothing is deleted.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteCollection(
            collection_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteCollectionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteCollectionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteCollection,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Remove an itinerary from a collection.
         *
         * Removes one itinerary from the collection and closes the collection's
         * ordering up around the gap. The itinerary itself is not deleted: only its
         * place in this collection is, so it stays in its organization and in every
         * other collection holding it. A link minted for this collection stops
         * reaching it. The answer is the collection after the change, as
         * `getCollection` reads it.
         *
         * Removing an itinerary the collection does not hold is not refused: it
         * changes nothing, and the answer is the collection as it stands, so a
         * removal sent twice answers alike both times. An itinerary this credential
         * cannot view is the `404` below, exactly like an identifier that names no
         * itinerary, so the call cannot be used to learn which itinerary ids exist.
         *
         * Send the collection's `etag` as `If-Match` to make the removal
         * conditional: a collection changed since that read, its contents included,
         * is a `412`, and nothing is removed.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteCollectionItinerary(
            collection_id: kotlin.String,
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteCollectionItineraryParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Collection, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteCollectionItineraryParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteCollectionItinerary,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Collection.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Remove the collection's share link.
         *
         * Removes the collection's share link, so every URL built from its token stops
         * resolving. Mountaya refuses the old token from the next request on, with a
         * `401`. An answer a shared cache already holds for a request carrying it is
         * served from there until its lifetime runs out: at most 5 minutes for a
         * public tile and 25 hours for the layer catalog, as each states in its
         * `Cache-Control`. It takes the same write role the mint takes. A collection
         * with no share link is answered the same way, so removing a link that is
         * already gone is not an error.
         *
         * A link is not part of what `getCollection` reads, so removing one does not
         * move the collection's `etag`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteCollectionShareLink(
            collection_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteCollectionShareLinkParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteCollectionShareLinkParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteCollectionShareLink,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/share-link",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Remove a zone from a collection.
         *
         * Removes one zone from the collection. The zone itself is not deleted: only
         * its place in this collection is, so it stays in its organization and in
         * every other collection holding it. A link minted for this collection stops
         * reaching it. The answer is the collection after the change, as
         * `getCollection` reads it.
         *
         * Removing a zone the collection does not hold is not refused: it changes
         * nothing, and the answer is the collection as it stands, so a removal sent
         * twice answers alike both times. A zone this credential cannot view is the
         * `404` below, exactly like an identifier that names no zone, so the call
         * cannot be used to learn which zone ids exist.
         *
         * Send the collection's `etag` as `If-Match` to make the removal
         * conditional: a collection changed since that read, its contents included,
         * is a `412`, and nothing is removed.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param zone_id Identifier of the zone the operation acts on.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteCollectionZone(
            collection_id: kotlin.String,
            zone_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteCollectionZoneParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Collection, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteCollectionZoneParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteCollectionZone,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/zones/" + com.mountaya.client.encodePathSegment(zone_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Collection.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve a single collection with its itineraries and zones.
         *
         * Returns the collection details, with counts of the itineraries and zones it
         * contains.
         *
         * This is the read an embed makes, and the one a share link serves.
         *
         * A collection is scoped by the organization that owns it. A private
         * collection is reached only with that organization's key, and a personal one,
         * owned by no organization, is reached by a key only when it is public. A
         * public collection is additionally readable by id across organizations, but
         * only the collection itself: under another organization's key, its
         * itineraries and zones are neither listed nor drawn.
         *
         * A share token minted for this collection reaches it read-only whatever its
         * privacy, and reaches the itineraries and zones it contains. A collection
         * this credential cannot view answers 404, exactly like an id that matches no
         * collection.
         *
         * A collection has no published snapshot: every credential reads it as it
         * stands, and the itineraries and zones it holds are served by their own
         * read tiers, as last published to every credential but a session naming a
         * member.
         * A bare publishable key reaches its own organization's collections whether
         * public or private, and another organization's public one, and so does the
         * key with a session token minted without `user_id`, which reads as the key
         * alone does; a share token reaches only what its link names. See the read
         * tiers in the API description.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * A publishable key needs the `publishing` scope, or the `studio-embedded`
         * scope that includes it. A share token carries its own authority and needs
         * none.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getCollection(
            collection_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetCollectionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Collection, kotlinx.serialization.json.JsonObject?> {
            RestSchema.GetCollectionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getCollection,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, RestSchema.Collection.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List the itineraries a collection contains.
         *
         * Returns the itineraries filed in the collection, one page at a time, in
         * the collection's own ordering unless `sort` names another.
         *
         * This is the forward direction of the membership question. `getCollections`
         * with `itinerary_id` answers the inverse, which collections hold a given
         * itinerary; this one answers which itineraries a given collection holds.
         * `getCollection` reports how many there are as a count, and this returns
         * them.
         *
         * The listing reflects the READ TIER the credential opens. On a collection
         * your organization owns, a publishable key alone lists its public
         * itineraries, and the same key with a session token naming a member lists
         * its private ones too, while a session token minted without `user_id` lists
         * what the key alone does. Another organization's public collection is
         * readable by id, but under your key this lists none of its itineraries and
         * its tiles draw none. See the read tiers in the API description.
         *
         * A SHARE TOKEN minted for this collection reads it, and this is the operation
         * a collection link enumerates through: list the itineraries here, then read
         * each one by id with the same token. `getItineraries`, the global listing,
         * refuses a share token, so enumeration stays bounded by the collection the
         * link names.
         *
         * The page is cut after authorization, so it never holds an itinerary the
         * caller may not see, and the collection's `count`, which counts every
         * member, can be larger than what this lists. The listing ends at the page
         * whose `extensions.metadata.pagination.next_cursor` is `null`.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * A publishable key needs the `publishing` scope, or the `studio-embedded`
         * scope that includes it. A share token carries its own authority and needs
         * none.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getCollectionItineraries(
            collection_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetCollectionItinerariesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<RestSchema.Itinerary>, RestSchema.ListMetadata> {
            val given = RestSchema.GetCollectionItinerariesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getCollectionItineraries,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/itineraries",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("sort" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(RestSchema.Itinerary.serializer()), RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getCollectionItineraries` answers, one row at a time, requesting a
         * page only as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetCollectionItineraries(
            collection_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetCollectionItinerariesParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<RestSchema.Itinerary> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getCollectionItineraries(
                    collection_id = collection_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Read the collection's share link.
         *
         * Returns the collection's share link, its token and when that token was
         * minted, or `null` when the collection has none. Reading never mints one:
         * `postCollectionShareLink` does.
         *
         * Reading a link takes the same write role minting one takes, because the
         * token opens the collection to anyone who holds it. A member who can only
         * view the collection cannot read its link, and neither can another
         * organization's key that sees the collection only because it is public.
         *
         * Like every read made with a session token, it is limited to 1200 a minute
         * per user the token names and then to 6000 a minute per organization; past
         * either, the answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getCollectionShareLink(
            collection_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetCollectionShareLinkParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ShareLink?, kotlinx.serialization.json.JsonObject?> {
            RestSchema.GetCollectionShareLinkParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getCollectionShareLink,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/share-link",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.nullableSerializer(RestSchema.ShareLink.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List the zones a collection contains.
         *
         * Returns the zones filed in the collection, one page at a time.
         *
         * This is the forward direction of the membership question. `getCollections`
         * with `zone_id` answers the inverse, which collections hold a given zone;
         * this one answers which zones a given collection holds. `getCollection`
         * reports how many there are as a count, and this returns them.
         *
         * The listing reflects the READ TIER the credential opens. On a collection
         * your organization owns, a publishable key alone lists its public zones
         * that are currently published, each as last published, and the same key
         * with a session token naming a member lists its private and unpublished
         * ones too, each as its draft, while a session token minted without
         * `user_id` lists what the key alone does. A share token minted for the
         * collection lists the zones it holds that are currently published, each
         * as last published, whatever their privacy. A zone never published, or
         * taken down, is absent from every listing served the published snapshot.
         * Another organization's public collection is readable by id, but under
         * your key this lists none of its zones and its tiles draw none. See the
         * read tiers in the API description.
         *
         * A SHARE TOKEN minted for this collection reads it, which is what lets a
         * collection link draw and label the zones it carries: the tiles arrive from
         * `getCollectionTile`, and the detail behind each of them from here.
         * `getZones`, the organization's listing, refuses a share token, so a link
         * never reaches past the collection it names.
         *
         * The page is cut after authorization, so it never holds a zone the caller
         * may not see, and the collection's `count`, which counts every member, can
         * be larger than what this lists. The listing ends at the page whose
         * `extensions.metadata.pagination.next_cursor` is `null`.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * A publishable key needs the `publishing` scope, or the `studio-embedded`
         * scope that includes it. A share token carries its own authority and needs
         * none.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getCollectionZones(
            collection_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetCollectionZonesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<RestSchema.Zone>, RestSchema.ListMetadata> {
            val given = RestSchema.GetCollectionZonesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getCollectionZones,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/zones",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("sort" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(RestSchema.Zone.serializer()), RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getCollectionZones` answers, one row at a time, requesting a page only
         * as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetCollectionZones(
            collection_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetCollectionZonesParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<RestSchema.Zone> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getCollectionZones(
                    collection_id = collection_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * List collections.
         *
         * Returns the collections of the key's organization that this credential
         * reaches, one page at a time. A collection belongs to the organization that
         * owns it, not to the user who created it, so this is the listing every
         * member shares.
         *
         * The listing reflects the READ TIER the credential opens. A bare publishable
         * key lists the organization's PUBLIC collections only: a private collection
         * is never discoverable with one. The same key with a session token naming
         * a member lists the organization's collections whole, private included,
         * and with a session token minted without `user_id` lists the public ones
         * only, as the key alone does. Either way each collection reports the role
         * `viewer`, because a key holds no membership. Another organization's public
         * collection is read by its identifier with `getCollection` and never appears
         * here, and a key lists no personal collection, owned by no organization. A
         * share token lists no collection: the one its link names is read with
         * `getCollection`. See the read tiers in the API description.
         *
         * `itinerary_id` and `zone_id` list only the collections holding that
         * itinerary or that zone, which is how a client shows where something is
         * filed. An identifier that names nothing this credential can view lists
         * nothing, the same answer as one no collection holds, so neither filter can
         * be used to learn which identifiers exist. Every filter sent narrows the
         * listing further, and none of them widens what the credential reaches.
         *
         * The page is cut after authorization, so it never holds a collection the
         * caller may not see. The listing ends at the page whose
         * `extensions.metadata.pagination.next_cursor` is `null`.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization, and a key with a session token
         * naming a user to 1200 per user the token names and then 6000 per
         * organization, counted apart from the reads made under the key alone. Past
         * any of them the read is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `publishing` scope, or the
         * `studio-embedded` scope that includes it.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getCollections(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetCollectionsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<RestSchema.Collection>, RestSchema.ListMetadata> {
            val given = RestSchema.GetCollectionsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getCollections,
                    path = "/v1/collections",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("organization_id" to it) }
                            given.slot3?.let { add("itinerary_id" to it) }
                            given.slot4?.let { add("zone_id" to it) }
                            given.slot5?.let { add("is_private" to it.toString()) }
                            given.slot6?.let { add("updated_since" to com.mountaya.client.formatTimestamp(it)) }
                            given.slot7?.let { add("q" to it) }
                            given.slot8?.let { add("sort" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(RestSchema.Collection.serializer()), RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getCollections` answers, one row at a time, requesting a page only as
         * its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetCollections(
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetCollectionsParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<RestSchema.Collection> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getCollections(
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Update an existing collection.
         *
         * Updates the collection's name, description and privacy. Every field is
         * optional and an omitted field keeps its current value; send `null`, or a
         * description that trims to nothing, to clear the description.
         *
         * Renaming or re-describing a collection is an administrative act on a shared
         * folder, so it needs more than a write role: the user the session token
         * names must have created the collection, or hold `owner` or `admin` in the
         * owning organization. A contributor may change what a collection CONTAINS
         * but not what it IS. Anything less is 403.
         *
         * Returns 409 when another collection in the organization already carries the
         * new name. The response carries the conflicting collection's id in
         * `extensions.metadata`, so a client can link straight to it rather than
         * making the user hunt for the clash.
         *
         * Send the collection's `etag` as `If-Match` to make the change conditional:
         * a collection changed since that read is a `412`, and nothing is changed.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchCollection(
            collection_id: kotlin.String,
            body: RestSchema.CollectionUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PatchCollectionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Collection, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PatchCollectionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchCollection,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.CollectionUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Collection.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Set an itinerary's position within a collection.
         *
         * Sets the zero-based display position of an itinerary within the collection
         * and renumbers the remaining itineraries so positions stay contiguous. A
         * position beyond the current range is clamped to the last slot. The answer
         * is the collection after the move, as `getCollection` reads it.
         *
         * Send the collection's `etag` as `If-Match` to make the move conditional: a
         * collection changed since that read, its contents included, is a `412`, and
         * nothing moves.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchCollectionItinerary(
            collection_id: kotlin.String,
            itinerary_id: kotlin.String,
            body: RestSchema.CollectionItineraryUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PatchCollectionItineraryParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Collection, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PatchCollectionItineraryParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchCollectionItinerary,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.CollectionItineraryUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Collection.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Create a new collection.
         *
         * Creates a new collection in the key's organization. Returns 409 Conflict
         * when another collection in that organization already carries the name.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postCollection(
            body: RestSchema.CollectionCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostCollectionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Collection, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostCollectionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postCollection,
                    path = "/v1/collections",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.CollectionCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Collection.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Add itineraries to a collection.
         *
         * Adds one or more itineraries to the collection. New members are appended at
         * the end of the collection's ordering. Itineraries that are already in the
         * collection keep their place and are otherwise silently ignored, so
         * re-sending the same set is safe: it changes nothing, and answers the
         * collection as it stands. The answer is the collection after the change, as
         * `getCollection` reads it, so its `count` and `etag` include what was added.
         *
         * Every itinerary must belong to the SAME organization as the collection. A
         * collection is scoped by its organization, so holding a resource from another
         * one would make it reachable to that organization's members through the
         * folder. An itinerary from elsewhere whose publication this credential is
         * served, another organization's public itinerary while it is published and
         * that organization's agreement includes Map Publishing, is rejected with `400`,
         * and the validation entry names the offending position in the submitted
         * array. One it is served nothing of is the `404` below, as its read is: one
         * it cannot view, and one it reads as published that has never been published,
         * has been taken down, or whose organization's agreement does not include Map
         * Publishing. Either way the whole call is refused, not partially applied.
         *
         * Send the collection's `etag` as `If-Match` to make the add conditional: a
         * collection changed since that read, its contents included, is a `412`, and
         * nothing is added.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postCollectionItineraries(
            collection_id: kotlin.String,
            body: RestSchema.CollectionItinerariesAddParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostCollectionItinerariesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Collection, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostCollectionItinerariesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postCollectionItineraries,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/itineraries",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.CollectionItinerariesAddParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Collection.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Mint the collection's share link, or return the one it has.
         *
         * Sets the collection's share link: mints one when the collection has none,
         * answering `201`, and returns the existing one otherwise, answering `200`,
         * so calling again never mints a second token. Both private and public
         * collections are shareable.
         *
         * Minting takes a write role in the organization that owns the collection. A
         * member who can only view the collection cannot mint a link, and neither can
         * another organization's key that sees it only because it is public, so a link
         * never outlives the publisher flipping the collection private. A personal
         * collection, owned by no organization, has no share link: minting one for
         * it, which only Mountaya's web application could ask for, is refused with a
         * `403`.
         *
         * The resulting link exposes the collection's itineraries and zones
         * read-only, each as last published.
         * That is the deliberate difference from an itinerary share link, which
         * exposes a single itinerary and never its collection's siblings.
         *
         * A link is not part of what `getCollection` reads, so minting one does not
         * move the collection's `etag`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postCollectionShareLink(
            collection_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostCollectionShareLinkParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ShareLink, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostCollectionShareLinkParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postCollectionShareLink,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/share-link",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ShareLink.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Rotate the collection's share link.
         *
         * Replaces the collection's share token with a freshly generated one and
         * returns the link with it, so every URL built from the previous token stops
         * resolving (the cut-off for a link that has spread too far or outlived its
         * intended audience). It takes the same write role the mint takes. A
         * collection with no share link has nothing to rotate, and is a `409`: mint
         * one with `postCollectionShareLink`. A personal collection, owned by no
         * organization, has no share link, and rotating one for it, which only
         * Mountaya's web application could ask for, is refused with a `403` as the
         * mint is.
         *
         * **Send an `Idempotency-Key`.** Each rotation mints a new token, so a retry
         * without one rotates again: if the first call succeeded but its response
         * was lost (a timeout, a dropped connection) the retry mints a third token
         * and invalidates the second, leaving a link nobody holds. A retry carrying
         * the first call's key is answered with the first call's token instead.
         *
         * A link is not part of what `getCollection` reads, so rotating one does not
         * move the collection's `etag`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postCollectionShareLinkRotate(
            collection_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostCollectionShareLinkRotateParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ShareLink, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostCollectionShareLinkRotateParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postCollectionShareLinkRotate,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/share-link/rotate",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ShareLink.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Add zones to a collection.
         *
         * Adds one or more zones to the collection. Zones that are already in the
         * collection are silently ignored, so re-sending the same set is safe: it
         * changes nothing, and answers the collection as it stands. The answer is the
         * collection after the change, as `getCollection` reads it, so its `count`
         * and `etag` include what was added.
         *
         * Every zone must belong to the SAME organization as the collection. A
         * collection is scoped by its organization, so holding a resource from another
         * one would make it reachable to that organization's members through the
         * folder. A zone from elsewhere whose publication this credential is
         * served, another organization's public zone while it is published and that
         * organization's agreement includes Map Publishing, is rejected with `400`,
         * and the validation entry names the offending position in the submitted
         * array. One it is served nothing of is the `404` below, as its read is: one
         * it cannot view, and one it reads as published that has never been
         * published, has been taken down, or whose organization's agreement does not
         * include Map Publishing. Either way the whole call is refused, not
         * partially applied.
         *
         * The collection's organization must hold Map Studio, or the call is refused
         * with a `402`. A personal collection, owned by no organization, holds no
         * zone: adding one to it, which only Mountaya's web application could ask
         * for, is refused with a `403`.
         *
         * Send the collection's `etag` as `If-Match` to make the add conditional: a
         * collection changed since that read, its contents included, is a `412`, and
         * nothing is added.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param collection_id Identifier of the collection the operation acts on. A collection this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which collection ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postCollectionZones(
            collection_id: kotlin.String,
            body: RestSchema.CollectionZonesAddParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostCollectionZonesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Collection, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostCollectionZonesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postCollectionZones,
                    path = "/v1/collections/" + com.mountaya.client.encodePathSegment(collection_id) + "/zones",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.CollectionZonesAddParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Collection.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Itineraries is the `studio.itineraries` operations of the REST API. */
    public class Itineraries internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Delete an itinerary.
         *
         * Permanently deletes the itinerary and all of its segments and waypoints.
         * This action cannot be undone.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the delete conditional
         * on nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is deleted.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteItinerary(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteItineraryParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteItineraryParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteItinerary,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Take the itinerary's publication down.
         *
         * Takes the itinerary off the public surfaces. Mountaya stops serving it from
         * the next request: share links and embeds stop resolving it, and Mountaya's
         * own caches stop serving its published tiles: they are not evicted, but
         * nothing reaches them and they age out. An answer a shared cache
         * between a reader and Mountaya already holds is served from there until its
         * lifetime runs out, which taking the itinerary down does not shorten: at
         * most 6 minutes for a published read of the itinerary or its pace
         * (`max-age=300` and `stale-while-revalidate=60`), and 5 for a public tile
         * of it (`max-age=300`).
         *
         * It acts on a state, so an itinerary that is not published is answered
         * `200` as well, whether it never was or is already taken down: the request
         * leaves it unpublished either way, and a repeated request is harmless.
         *
         * The publication is kept, not deleted. Publishing again therefore creates the
         * NEXT publication rather than reusing the number that was live: a publication
         * number is never reissued, so a client that cached one can always tell the
         * two apart. What unpublishing clears is whether a publication is live, not
         * the publication itself.
         *
         * The draft is untouched. The studio reads and writes the draft whatever is
         * published, so unpublishing changes nothing a member sees in the editor; it
         * changes what everyone else sees, which is nothing.
         *
         * Unlike publishing, this is never refused for a held collaboration lock.
         * Publishing freezes a state and so cannot run over an in-flight edit;
         * unpublishing freezes nothing.
         *
         * Unpublishing frees the itinerary's place under the organization's
         * published-itineraries limit at once. It is never refused for Map Studio,
         * for Map Publishing or for that limit, so an organization whose agreement
         * includes neither can still take its itineraries down, and the one 402 it
         * declares is the one every `studio-embedded` operation shares.
         *
         * Taking the publication down changes what a read of the itinerary returns,
         * so it moves the itinerary's `etag`. Send the itinerary's `ETag` as
         * `If-Match` to make it conditional on nobody having changed the itinerary
         * since you read it; a stale tag is refused with `412`.
         *
         * Takes no request body. Returns the itinerary with its full segments,
         * waypoints, and groups, with `status` back to draft and `published_at` null.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteItineraryPublication(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteItineraryPublicationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteItineraryPublicationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteItineraryPublication,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/publication",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Delete a segment from an itinerary.
         *
         * Permanently deletes the segment. This action cannot be undone.
         *
         * An itinerary's segments form a chain, each one starting where the previous
         * ends, so only a segment at an END of that chain may be deleted: the first,
         * the last, or (on a single-segment itinerary) the one that is both. Removing
         * an interior segment would leave the two it sat between disconnected, and is
         * refused with a `409` whose `extensions.code` is `CONFLICT`: the chain's
         * shape forbids it, and the same request is refused the same way until the
         * chain changes.
         *
         * Deleting an end is otherwise lossless: the remaining segments keep their
         * order and their geometry is not recomputed, because a promoted first segment
         * has no predecessor to meet. The one exception is grouping: when the deleted
         * segment was one of a group's only two members, the group is dissolved and
         * the member left behind is un-grouped, since a group never covers fewer than
         * two segments.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does, so the client sees the chain
         * renumbered and any group dissolved.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the delete conditional
         * on nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is deleted.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param segment_id Identifier of the segment within the itinerary named by `itinerary_id`.
         * A segment belonging to another itinerary is a 404, not a 403.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteItinerarySegment(
            itinerary_id: kotlin.String,
            segment_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteItinerarySegmentParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteItinerarySegmentParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteItinerarySegment,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segments/" + com.mountaya.client.encodePathSegment(segment_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Delete a segment group.
         *
         * Dissolves the group. Its members are un-grouped and are otherwise untouched:
         * each keeps its own name, color, speed, geometry, metrics, and position, and
         * the elevation profile goes back to showing them as separate bands. No
         * segment is ever deleted by this operation. Clearing a member's `group_id`
         * writes no new revision of it, so its `revision` does not move.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does, so the client sees the cleared
         * group_id on every former member.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the delete conditional
         * on nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is deleted.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param group_id Identifier of the segment group within the itinerary named by
         * `itinerary_id`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteItinerarySegmentGroup(
            itinerary_id: kotlin.String,
            group_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteItinerarySegmentGroupParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteItinerarySegmentGroupParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteItinerarySegmentGroup,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segment-groups/" + com.mountaya.client.encodePathSegment(group_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Remove the itinerary's share link.
         *
         * Removes the itinerary's share link, so every URL built from its token stops
         * resolving. Mountaya refuses the old token from the next request on, with a
         * `401`. An answer a shared cache already holds for a request carrying it is
         * served from there until its lifetime runs out: at most 5 minutes for a
         * public tile and 25 hours for the layer catalog, as each states in its
         * `Cache-Control`. It takes the same write role the mint takes. There is no
         * undo: minting again produces a new token, not the old one.
         *
         * It acts on a state, so an itinerary that has no share link is answered
         * `200` as well, whether it never had one or it was already removed: the
         * request leaves it without a link either way, and a repeated request is
         * harmless.
         *
         * A share link does not change what a read of the itinerary returns, so this
         * leaves the itinerary's `etag` where it is and takes no `If-Match`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteItineraryShareLink(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteItineraryShareLinkParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteItineraryShareLinkParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteItineraryShareLink,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/share-link",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Delete a waypoint from an itinerary.
         *
         * Permanently deletes the waypoint. This action cannot be undone. The
         * remaining waypoints keep their positions: nothing is renumbered.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does. Send the itinerary's `ETag` as
         * `If-Match` to make the delete conditional on nobody having changed the
         * itinerary since you read it; a stale tag is refused with `412` and nothing
         * is deleted.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param waypoint_id Identifier of the waypoint within the itinerary named by `itinerary_id`.
         * A waypoint belonging to another itinerary is a 404.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteItineraryWaypoint(
            itinerary_id: kotlin.String,
            waypoint_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteItineraryWaypointParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteItineraryWaypointParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteItineraryWaypoint,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/waypoints/" + com.mountaya.client.encodePathSegment(waypoint_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List the itineraries the credential reaches.
         *
         * Returns the itineraries of the key's organization that the request's
         * credential reaches, one page at a time. Each itinerary carries its summary
         * fields and counts but not its segments and waypoints, which
         * `getItinerary` returns.
         *
         * The listing is always the key's organization. `organization_id` may name
         * that organization, which changes nothing, and naming any other
         * organization is a `403` whether or not it exists. A personal itinerary,
         * owned by no organization, never appears.
         *
         * WHICH itineraries appear depends on the credential. A publishable key alone
         * lists the organization's PUBLIC itineraries that are currently published,
         * and serves the published snapshot of each: one never published, or taken
         * down, is absent, and so is every itinerary while the organization's
         * agreement does not include Map Publishing. The same key with a session token
         * naming a member of the organization lists its private ones and its
         * unpublished ones as well, and serves the draft of each; a session token
         * minted without `user_id`, whatever surface it was minted for, lists and
         * serves what the key alone does, and so does one naming a user whose
         * invitation is still pending or a member removed since it was minted,
         * membership being checked again on every request. A caller that finds its own
         * itineraries missing is almost always sending the key without a session
         * naming a member. There is no "current user" on this read: whether the
         * session names a member decides the tier, and which member it names decides
         * nothing, the scope being the organization and the privacy of each itinerary,
         * never the identity of whoever holds the key. A share token does not open
         * this listing: a collection's link lists what it holds through
         * `getCollectionItineraries`, and an itinerary's link names one itinerary and
         * lists nothing.
         *
         * The filters narrow that set and never widen it, and they combine: an
         * itinerary is listed only when it matches every one sent. `status` and
         * `is_private` read each itinerary as it is served, so under a key alone,
         * and under a session token minted without `user_id`, `status=draft` and
         * `is_private=true` list nothing. `in_collection` counts only the
         * collections this credential lists: under a key alone, or a token minted
         * without `user_id`, that is the organization's public collections, so an
         * itinerary filed only in a private one is listed by
         * `in_collection=false`; with a token naming a member every collection of
         * the organization counts. `getCollectionItineraries` lists
         * the itineraries of one collection.
         *
         * The listing is paged by cursor. Send the `next_cursor` a page reports, with
         * the same filters and `sort`, to read the next one; the last page reports a
         * `next_cursor` of `null`. `sort` orders the whole listing before it is
         * paged, so a walk from the first page to the last serves once every
         * matching itinerary whose sort key does not change during the walk. An
         * itinerary edited during a walk ordered by `updated_at` moves in that
         * order: past the cursor it is served again, and behind it, not at all.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization, and a key with a session token
         * naming a user to 1200 per user the token names and then 6000 per
         * organization, counted apart from the reads made under the key alone. Past
         * any of them the read is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `publishing` scope, or the
         * `studio-embedded` scope that includes it.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getItineraries(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetItinerariesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<RestSchema.Itinerary>, RestSchema.ListMetadata> {
            val given = RestSchema.GetItinerariesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getItineraries,
                    path = "/v1/itineraries",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("organization_id" to it) }
                            given.slot1?.let { add("status" to it.value) }
                            given.slot2?.let { add("is_private" to it.toString()) }
                            given.slot3?.let { add("activity_id" to it) }
                            given.slot4?.let { add("in_collection" to it.toString()) }
                            given.slot5?.let { add("updated_since" to com.mountaya.client.formatTimestamp(it)) }
                            given.slot6?.let { add("q" to it) }
                            given.slot7?.let { add("sort" to it.value) }
                            given.slot8?.let { add("cursor" to it) }
                            given.slot9?.let { add("limit" to it.toString()) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(RestSchema.Itinerary.serializer()), RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getItineraries` answers, one row at a time, requesting a page only as
         * its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetItineraries(
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetItinerariesParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<RestSchema.Itinerary> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getItineraries(
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Retrieve a single itinerary with its segments and waypoints.
         *
         * Returns the full itinerary including all of its segments and waypoints. Each
         * segment carries its geometry and computed metrics; each waypoint carries its
         * location.
         *
         * WHICH VERSION you are served depends on the credential. A publishable key
         * alone, and a share token, are served the last PUBLISHED snapshot; the same
         * key with a session token naming a member is served its own organization's
         * DRAFT, which is what the studio edits, unless it passes
         * `surface=published`. A session token minted without `user_id`, such as the
         * one a read-only viewer runs on, is served what the key alone is, the
         * published snapshot and never the draft, whatever surface it was minted
         * for. Another organization's public itinerary is served as it was last
         * published whatever the credential. Whenever the published snapshot is what
         * a request is served, an itinerary that has never been published, or has
         * been taken down, answers 404, and so does one whose organization's
         * agreement does not include Map Publishing, which withdraws what the
         * organization published rather than refusing it; a share token of such an
         * organization does not resolve at all and answers `401`. A bare publishable
         * key, and the key with a session token minted without `user_id`, reaches
         * its own organization's itineraries whether public or private, and another
         * organization's public one; a share token reaches only what its link names.
         * See the read tiers in the API description.
         *
         * Refused with 403 when `surface=published` names a PERSONAL itinerary, that
         * is, one owned by no organization. The published-surface opt-in is how the
         * studio's video and embed previews read an itinerary, and a video and an
         * embed are outputs of an organization's products, which a personal itinerary,
         * owned by none, never has, whatever the credential. A request without the
         * opt-in is not refused this way. Only the person whose itinerary it is reads
         * it as its draft and is refused this way; every other caller, every
         * publishable key included, reads it as published, is served nothing of a
         * personal itinerary, which is never published, and is answered `404` before
         * this refusal is reached, as a caller who cannot view it is.
         *
         * A published snapshot is a copy, and its segments and waypoints carry the
         * snapshot's own ids: they differ from the draft's, and they change at every
         * publish. An id read from one version names nothing in the other, so take
         * the `segment_id` of an export, or the `segment_ids` of a duplicate, from a
         * read of the version that operation reads.
         *
         * The answer carries `ETag`, the version of what it returns, which is also
         * the body's `etag`: send it as `If-Match` on a write to the itinerary to
         * make that write conditional on nobody having changed it since.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * A publishable key needs the `publishing` scope, or the `studio-embedded`
         * scope that includes it. A share token carries its own authority and needs
         * none.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getItinerary(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetItineraryParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.GetItineraryParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getItinerary,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id),
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("surface" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Export an itinerary as a downloadable file.
         *
         * Exports the itinerary in the requested format. The optional segment_id query
         * parameter limits the export to a single segment within the itinerary. Every
         * format carries track point elevation when the geometry holds it. GPX emits a
         * single track with one segment per itinerary segment; GeoJSON returns a
         * FeatureCollection; KML and KMZ carry per-segment styling; FIT and TCX are
         * time-series formats whose file type the optional variant query parameter
         * selects; CSV is a flat coordinate table and is lossy. The format query
         * parameter is required.
         *
         * WHICH VERSION is exported depends on the credential, exactly as on
         * getItinerary. A publishable key alone, and a share token, export the last
         * PUBLISHED snapshot; the same key with a session token naming a member
         * exports its own organization's DRAFT unless it passes
         * `surface=published`, and a session token minted without `user_id` exports
         * the published snapshot as the key alone does. Whenever the published
         * snapshot is what a request is served, an itinerary that has never been
         * published answers 404, and so does one that has been taken down, before any
         * refusal below is reached, exactly as getItinerary answers it. A
         * `format` or `variant` that is not one of its values is a `400` before that,
         * since the request is validated before the itinerary is looked up; a
         * `variant` sent with a format it does not apply to is refused only once the
         * itinerary is found.
         *
         * Refused with 403 when the itinerary is PERSONAL, that is, owned by no
         * organization, and the request reads its draft, which only the person whose
         * itinerary it is does. A downloadable file is an output of an organization's
         * products, like the embed and the share link, and a personal itinerary, owned
         * by none, has none of them. Every other caller, every publishable key
         * included, reads a personal itinerary as published, and since one is never
         * published it is answered the `404` below instead. A share token minted for a
         * personal itinerary never resolves, so it answers `401` before this refusal
         * is reached.
         *
         * Refused with a plain 402 `PAYMENT_REQUIRED` when the DRAFT is what the
         * request is served and its organization does not hold Map Publishing: a
         * draft leaves the studio as a file only for an organization whose agreement
         * with Mountaya includes Map Publishing, the product downloads belong to,
         * and the refusal clears when the agreement changes rather than when the
         * request does. This refusal never asks for Map Studio: a download changes
         * nothing. It is decided after the personal refusal above and before
         * `format` and `variant` are checked against each other. A request served
         * the published snapshot is never refused this way: an organization whose
         * agreement does not include Map Publishing has its publications withdrawn,
         * so its published snapshot answers the `404` below, which tells a visitor
         * nothing about its owner.
         *
         * Refused with 413 when the itinerary holds more than 500,000 positions,
         * every position of its segments' geometry, the vias of its routed lines
         * included, plus one per waypoint: more than one answer carries. They are
         * counted over the whole itinerary, before anything is loaded, so a single
         * segment's export is refused as well. This is decided after every other
         * refusal, the `404` of a surface not served included.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * An export is held to a limit of its own beside them, at the figure the
         * signed download URL of the same file is held to: 60 exports a minute per
         * client IP address under a publishable key sent without a session token
         * or with one minted without `user_id`, and under a share token, and 60
         * exports a minute per user under a key with a session token naming a
         * user. It is checked before the read limits, so an export it refuses
         * spends nothing of them, and past it the export is a `429` carrying
         * `Retry-After` as well.
         *
         * A publishable key needs the `publishing` scope, or the `studio-embedded`
         * scope that includes it. A share token carries its own authority and needs
         * none.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param format File format to render. Required: there is no default, because a download
         * whose format the caller did not choose is never the right guess. One of
         * `gpx`, `geojson`, `kml`, `kmz`, `fit`, `tcx`, or `csv`; the schema below
         * describes what each one carries. The value also decides the response
         * `Content-Type` and the extension in `Content-Disposition`, so it is the
         * single input that determines what the browser saves.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getItineraryExport(
            itinerary_id: kotlin.String,
            format: RestSchema.ExportFormat,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetItineraryExportParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.BinaryResponse {
            val given = RestSchema.GetItineraryExportParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getItineraryExport,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/export",
                    query =
                        kotlin.collections.buildList {
                            add("format" to format.value)
                            given.slot0?.let { add("variant" to it.value) }
                            given.slot1?.let { add("segment_id" to it) }
                            given.slot2?.let { add("surface" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.download(call)
        }

        /**
         * Re-time an itinerary at a travel pace the caller names.
         *
         * Answers how long the itinerary takes at the flat pace the `speed` query
         * parameter names, for the caller asking and for nobody else. The itinerary's
         * stored speeds are not touched, no revision is written, and the next reader
         * without a pace of their own is served the author's figures. This is what
         * lets an embedded visitor read a route at their own rate: a fast walker and a
         * slow one are looking at the same published itinerary.
         *
         * The requested pace REPLACES every per-segment `speed` and every segment
         * group's `speed` override for the length of the answer. One pace means one
         * pace: a group override that survived would produce a run whose duration
         * disagreed with the total returned beside it.
         *
         * Only the flat pace moves. The ground does not: the model still slows a
         * climb, speeds a descent, and charges for a coarse surface or a technical
         * trail, exactly as it does for the committed durations. That is why the
         * answer carries `speed_info`, the per-stretch speed each duration was
         * integrated over, and no other info-family member: a pace does not change
         * what is underfoot. It also costs no routing call and no elevation read, the
         * ground is already on the stored segments, so this is a re-integration of
         * what the itinerary already holds.
         *
         * WHICH VERSION is re-timed depends on the credential, exactly as on
         * getItinerary. A publishable key alone, and a share token, re-time the last
         * PUBLISHED snapshot; the same key with a session token naming a member
         * re-times its own organization's DRAFT unless it passes
         * `surface=published`, and a session token minted without `user_id`
         * re-times the published snapshot as the key alone does.
         * Whenever the published snapshot is what a request is served, an itinerary
         * that has never been published answers 404.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * A publishable key needs the `publishing` scope, or the `studio-embedded`
         * scope that includes it. A share token carries its own authority and needs
         * none.
         *
         * Refused with 403 when `surface=published` names a PERSONAL itinerary, that
         * is, one owned by no organization. The published-surface opt-in is how the
         * studio's video and embed previews re-time an itinerary, and a video and an
         * embed are outputs of an organization's products, which a personal itinerary,
         * owned by none, never has, whatever the credential. A request without the
         * opt-in is not refused this way. Only the person whose itinerary it is reads
         * it as its draft and is refused this way; every other caller, every
         * publishable key included, reads it as published, is served nothing of a
         * personal itinerary, which is never published, and is answered `404` before
         * this refusal is reached, as a caller who cannot view it is.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param speed Travel speed in km/h on LEVEL ground: the flat pace the travel-time model
         * starts from, the same quantity `ItinerarySegment.speed` carries and never
         * the resolved per-stretch speed `speed_info` reports. Required: there is no
         * default, because the itinerary already has one and a caller who did not
         * name a pace should read that instead.
         *
         * km/h regardless of what the caller displays. Units are a display
         * preference and the wire carries one unit, so a client showing mph, min/km
         * or min/mi converts before sending. Outside 0.5 and 100 the value is
         * REJECTED rather than clamped: a pace nobody walks is a mistake worth
         * reporting, not one worth guessing at.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getItineraryPace(
            itinerary_id: kotlin.String,
            speed: kotlin.Double,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetItineraryPaceParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryPace, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.GetItineraryPaceParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getItineraryPace,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/pace",
                    query =
                        kotlin.collections.buildList {
                            add("speed" to speed.toString())
                            given.slot0?.let { add("surface" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryPace.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Read the itinerary's share link.
         *
         * Returns the itinerary's share link, its token and when it was minted, or
         * `data: null` when the itinerary has none: none was ever minted, or the
         * last one was removed. An itinerary has at most one link at a time.
         * `postItineraryShareLink` mints one, `postItineraryShareLinkRotate`
         * replaces its token and `deleteItineraryShareLink` removes it.
         *
         * Reading a link takes the same write role minting one does, in the
         * organization that owns the itinerary: the token opens the itinerary to
         * anyone holding it, so a member who can only view the itinerary cannot
         * read it, and neither can another organization's key that sees the
         * itinerary only because it is public. `getItinerary` never carries the
         * token, in any form.
         *
         * Like every read made with a session token, it is limited to 1200 a minute
         * per user the token names and then to 6000 a minute per organization; past
         * either, the answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getItineraryShareLink(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetItineraryShareLinkParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ShareLink?, kotlinx.serialization.json.JsonObject?> {
            RestSchema.GetItineraryShareLinkParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getItineraryShareLink,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/share-link",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.nullableSerializer(RestSchema.ShareLink.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update an existing itinerary.
         *
         * Updates the itinerary's name, description and privacy. Every field is
         * optional and an omitted field keeps its current value; send null to clear
         * the description. Returns 409 Conflict when another itinerary in the
         * organization already carries the requested name. The response is the
         * itinerary's full draft: `description`, `distance` and `ascent` are `null`
         * when it has none.
         *
         * Every accepted request writes a new revision, even one that changes
         * nothing: it raises `revision`, moves `updated_at` and, on a published
         * itinerary, sets `has_unpublished_changes`.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the update conditional
         * on nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is changed.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchItinerary(
            itinerary_id: kotlin.String,
            body: RestSchema.ItineraryUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PatchItineraryParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PatchItineraryParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchItinerary,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.ItineraryUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update an existing segment.
         *
         * Updates an existing segment within the itinerary. Every field is optional:
         * an omitted field keeps its current value, and `null` clears a nullable one.
         * A new `geojson` re-derives the segment's geometry and totals, and the shared
         * points of the neighbouring segments follow it. The segment's mode is changed
         * by its own operation, `postItinerarySegmentConvert`.
         *
         * Waypoints the routing engine cannot route between, in this segment in
         * `directions` mode or in a `directions` neighbour the update re-routes, are a
         * 400 naming `geojson`, and nothing is written.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does: a new `geojson` moves the shared
         * points of the neighbours, and the itinerary's totals follow the segment's.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the update conditional
         * on nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is changed.
         *
         * Segment updates are routed writes, limited together with every other
         * routed segment write (create, split, merge and convert) to 120 requests a
         * minute per user and 1200 a minute per organization; past either, the
         * request is refused with **429** and `Retry-After`. The routing they do is
         * recorded as the organization's `studio-embedded` data usage, which
         * `getOrganizationUsageData` reports and which never counts toward the Data
         * API's per-minute limit, monthly allowance or invoice.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param segment_id Identifier of the segment within the itinerary named by `itinerary_id`.
         * A segment belonging to another itinerary is a 404, not a 403.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchItinerarySegment(
            itinerary_id: kotlin.String,
            segment_id: kotlin.String,
            body: RestSchema.SegmentUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PatchItinerarySegmentParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PatchItinerarySegmentParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchItinerarySegment,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segments/" + com.mountaya.client.encodePathSegment(segment_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.SegmentUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update a segment group.
         *
         * Updates the group's name, color, speed, or membership. Membership changes
         * obey the same rules as creation: the segments must form a contiguous run of
         * the chain and at least two are required, each a 400 otherwise, and none may
         * already belong to another group, a 409 naming each such segment's position
         * in `segment_ids`. Segments dropped from the run are un-grouped.
         *
         * A group is presentation only, so no update here ever touches a member
         * segment's geometry, metrics, or position. The lock this operation contends
         * on is the group's own, so renaming a group while a collaborator edits one of
         * its members' geometry is legal and expected.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does: a membership change rewrites the
         * group_id of every segment entering or leaving the run.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the update conditional
         * on nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is changed.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param group_id Identifier of the segment group within the itinerary named by
         * `itinerary_id`.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchItinerarySegmentGroup(
            itinerary_id: kotlin.String,
            group_id: kotlin.String,
            body: RestSchema.SegmentGroupUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PatchItinerarySegmentGroupParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PatchItinerarySegmentGroupParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchItinerarySegmentGroup,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segment-groups/" + com.mountaya.client.encodePathSegment(group_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.SegmentGroupUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update an existing waypoint.
         *
         * Updates an existing waypoint within the itinerary. Every field is optional:
         * an omitted field keeps its current value, and `null` clears a nullable one.
         * A `contact`, `media` or `timing` object whose members are all null or absent
         * (a `media` with empty lists included) is stored as none, so sending one
         * clears the field.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does. Send the itinerary's `ETag` as
         * `If-Match` to make the update conditional on nobody having changed the
         * itinerary since you read it; a stale tag is refused with `412` and nothing
         * is changed.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param waypoint_id Identifier of the waypoint within the itinerary named by `itinerary_id`.
         * A waypoint belonging to another itinerary is a 404.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchItineraryWaypoint(
            itinerary_id: kotlin.String,
            waypoint_id: kotlin.String,
            body: RestSchema.WaypointUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PatchItineraryWaypointParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PatchItineraryWaypointParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchItineraryWaypoint,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/waypoints/" + com.mountaya.client.encodePathSegment(waypoint_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.WaypointUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Create a new itinerary.
         *
         * Creates a new itinerary in the key's organization. Returns 409 Conflict when
         * another itinerary in that organization already carries the name. The
         * response is the created itinerary's full draft: its segments and waypoints
         * arrays are empty, and `description`, `distance` and `ascent` are `null` when
         * it has none.
         *
         * The organization must hold Map Studio, or the create is refused with a
         * `402`. An organization keeps any number of itineraries. A personal
         * itinerary, owned by no organization, is created only from Mountaya's web
         * application, and a person keeps at most three: a fourth is refused with a
         * `429` coded `LIMIT_REACHED`, whose `extensions.metadata` names the
         * ceiling, `personal-itineraries`, with what it allows and what is held, and
         * which carries no `Retry-After`, since no wait lifts it.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItinerary(
            body: RestSchema.ItineraryCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItineraryParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItinerary,
                    path = "/v1/itineraries",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.ItineraryCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Duplicate an itinerary into the key's organization.
         *
         * Copies the selected segments and waypoints of an existing itinerary into a
         * new one in the key's organization, which is also what an omitted
         * `organization_id` means. Reading the source needs only view access (matching
         * the "anyone with the link" model), so another organization's public
         * itinerary can be copied, as it was last published; writing the copy needs a
         * write role in the key's organization. Returns 409 Conflict when another itinerary in that
         * organization already carries the requested name.
         *
         * The copy is a pure copy: every selected segment keeps its stored geometry
         * and metrics verbatim and is never re-routed, so duplicating is
         * deterministic, instantaneous, and free of routing-engine failure modes.
         * Positions are renumbered from zero over the kept segments.
         *
         * Omitting `segment_ids` and `waypoint_ids` copies the itinerary whole,
         * however many segments and waypoints it holds, from the source as it is
         * read. A source holding more than 500,000 positions is refused with a `413`:
         * every position of its segments' geometry, the vias of its routed lines
         * included, plus one per waypoint, more than one request copies. They are
         * counted over the whole source, for a partial copy as well, before any of
         * it is loaded: once the source is known to be one the credential reads,
         * and before the selection is held to it and the organization the copy goes
         * to is looked at. A partial copy names what it keeps, and its segment selection must
         * be contiguous: max(position) - min(position) + 1 must equal the number of
         * segments given. That is what keeps the copy a connected route. A source group carries over only when at least two of its members are
         * kept. The copy is always private, never carries the source's share link, and
         * starts unpublished.
         *
         * A copy is a new itinerary, so it passes the same gates a create does: the
         * organization it is created in must hold Map Studio, or the copy is refused
         * with a `402`. A copy into the personal space, owned by no organization,
         * is made only from Mountaya's web application, and a person keeps at most
         * three personal itineraries, so a fourth is refused with a `429` coded
         * `LIMIT_REACHED`, whose `extensions.metadata` names the ceiling,
         * `personal-itineraries`, and which carries no `Retry-After`, since no wait
         * lifts it. No other ceiling counts against a copy: an itinerary takes
         * any number of segments and waypoints, and an organization keeps any
         * number of itineraries.
         *
         * Copies are limited to 30 a minute per user and 300 a minute per
         * organization; past either, the copy is a `429` carrying `Retry-After`, and
         * nothing is copied.
         *
         * Returns the new itinerary with its full segments, waypoints, and groups.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItineraryDuplicate(
            itinerary_id: kotlin.String,
            body: RestSchema.ItineraryDuplicateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryDuplicateParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItineraryDuplicateParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItineraryDuplicate,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/duplicate",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("surface" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.ItineraryDuplicateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Confirm an itinerary import, persisting the reviewed draft.
         *
         * Persists a reviewed import draft atomically: the itinerary, its ordered
         * segments and its waypoints are created together or not at all. Every
         * segment's metrics are recomputed server-side from its defining geometry,
         * never trusted from the client, and a segment the routing engine cannot
         * route is a 400 naming that segment's `geojson`, with nothing created.
         * Returns 409 Conflict when another itinerary in the key's organization
         * already carries the name. The response is the created itinerary with its
         * full segments and waypoints.
         *
         * Every field of the draft, each segment's and each waypoint's included, is
         * validated before any segment is routed, and so is the name: a draft
         * refused for what it holds is refused before any routing is spent.
         *
         * This is a general "create itinerary with full details" path and is not
         * import-specific; the preview step (POST /v1/itineraries/preview) is
         * optional.
         *
         * The organization the itinerary is created in must hold Map Studio, or the
         * confirm is refused with a `402` before any segment is routed. An import into
         * the personal space, owned by no organization, is made only from Mountaya's
         * web application, and a person keeps at most three personal itineraries, so
         * a fourth is refused with a `429` coded `LIMIT_REACHED`, whose
         * `extensions.metadata` names the ceiling, `personal-itineraries`, and which
         * carries no `Retry-After`, since no wait lifts it. No other ceiling holds an
         * import: an organization keeps any number of itineraries, and an itinerary
         * takes any number of segments and waypoints. The bounds on `segments` and
         * `waypoints` in the body are on the size of one request, and a draft over
         * either is a `400` naming the field. What else counts against an import is
         * the rate limits below.
         *
         * The routing an import does is recorded as the organization's `studio-embedded`
         * data usage, which `getOrganizationUsageData` reports and which never
         * counts toward the Data API's per-minute limit, monthly allowance or
         * invoice. Imports and previews are limited together to 10 requests a minute
         * per user and 60 a minute per organization, and the requests they send the
         * routing engine to 4,000 a minute per user and 24,000 per organization. A
         * confirm holds, while it runs, one for each segment of its draft and 120
         * more, so a draft at its 1,000 segments holds 1,120: a routed segment is
         * one route, a freehand segment one analysis of its line, and the 120 are
         * the most it sends to analyse in parts a line the engine could not analyse
         * whole, in rounds: every freehand segment it could not analyse whole is
         * split in two before any half is split again, and where the 120 run out
         * within a round, the segments first in the draft are split. Past them, a
         * stretch left unanalysed is stored with an unknown surface, way type and
         * difficulty. Once it ends, whatever its answer, it is charged the ones it
         * sent, so a draft refused before it routes, its name already taken among
         * them, is charged none. Past any of these limits, the request is refused
         * with **429** and `Retry-After`, and nothing is routed or created.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItineraryImport(
            body: RestSchema.ItineraryImportConfirmParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryImportParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItineraryImportParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItineraryImport,
                    path = "/v1/itineraries/import",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.ItineraryImportConfirmParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Preview an itinerary import without persisting it.
         *
         * Parses an uploaded itinerary file, segments each track, and computes
         * per-segment mode candidates (directions and/or linestring) with a server
         * recommendation. Nothing is persisted: the response is a reviewable draft the
         * client edits before confirming it with `postItineraryImport`. Since a
         * preview creates nothing, the request is safe to repeat.
         *
         * The body is `multipart/form-data` carrying the uploaded `file`, the
         * `provider` naming its format (gpx, geojson, kml, kmz, fit, tcx) and the
         * `activity_id` the import is for, plus optional form fields. No file format
         * carries an activity the server reads, so `activity_id` is required. FIT and
         * TCX uploads may be either a course or a recorded activity; the file type is
         * detected from the file and never supplied by the client. An upload is at
         * most 25 MiB; a larger one is refused with `413`. Every other form field is
         * at most 4 KiB; a longer one is a `400`.
         *
         * A source holding more than 500,000 points across its tracks is refused with
         * `413` as well, whatever its size and whichever format it comes in, and
         * so is one with a stretch the confirm could not take back as it is proposed:
         * a freehand segment longer than 500 km or of more than 10,000 positions, or
         * one whose duration at the activity's pace cannot be stored. Split such a
         * source, or preview it with a finer `granularity`.
         *
         * Previewing routes, so it passes the gates a confirm does: the itinerary
         * would be created in the key's organization, which must hold Map Studio, and
         * the activity must be one `getActivities` lists. A preview into the personal
         * space, which only Mountaya's web application makes, is refused as the
         * confirm would be when its person already keeps three personal itineraries:
         * a `429` coded `LIMIT_REACHED`, whose `extensions.metadata` names the
         * ceiling, `personal-itineraries`, and which carries no `Retry-After`, since
         * no wait lifts it. A source is split into at
         * most 60 proposed segments, a bound on the routing one upload can spend
         * rather than on what an itinerary may hold. It proposes at most 5,000
         * waypoints, the most a confirm takes in one request: a source carrying more
         * proposes its first 5,000 and reports `waypoints-truncated` in `warnings`.
         *
         * The routing a preview does is recorded as the organization's `studio-embedded` data
         * usage, which `getOrganizationUsageData` reports and which never counts
         * toward the Data API's per-minute limit, monthly allowance or invoice.
         * Previews and imports are limited together to 10 requests a minute per user
         * and 60 a minute per organization, and the requests they send the routing
         * engine to 4,000 a minute per user and 24,000 per organization. A preview
         * holds 1,188 of them while it runs, the most it may send, and once it ends,
         * whatever its answer, it is charged the ones it sent: a preview of a
         * recording that follows mapped ways sends a few dozen. Of those, it sends
         * at most 120 to analyse in parts a line the engine could not analyse whole,
         * in rounds: every line it could not analyse whole is split in two before
         * any half is split again, and where the 120 run out within a round, the
         * lines first in the file are split. Past them, a stretch left unanalysed is
         * reported with an unknown surface, way type and difficulty. Past any of
         * these limits, the request is refused with **429** and `Retry-After`, and
         * nothing is routed.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param body The request body, sent as `multipart/form-data`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItineraryPreview(
            body: RestSchema.PostItineraryPreviewMultipartBody,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryPreviewParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryImportPreview, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItineraryPreviewParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItineraryPreview,
                    path = "/v1/itineraries/preview",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Multipart(
                        kotlin.collections.buildList {
                            add(com.mountaya.client.MultipartPart.Text("provider", body.provider.value))
                            add(com.mountaya.client.MultipartPart.File("file", body.file, contentType = "application/octet-stream"))
                            add(com.mountaya.client.MultipartPart.Text("activity_id", body.activity_id))
                            body.organization_id?.let { add(com.mountaya.client.MultipartPart.Text("organization_id", it)) }
                            body.granularity?.let { add(com.mountaya.client.MultipartPart.Text("granularity", it.value)) }
                        },
                    ),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryImportPreview.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Publish the itinerary's current draft.
         *
         * Sets the itinerary's publication to its current draft: the draft's content
         * is frozen into a publication and becomes what the public surfaces serve.
         * Until the first publish, share links and embeds resolve nothing for the
         * itinerary; after it, they serve that snapshot and are unaffected by any
         * further draft edit until the itinerary is published again. The studio
         * always reads and writes the draft, whatever is published.
         *
         * The publication is a state the itinerary is set to, so the request is safe
         * to repeat: sent again with nothing changed in between, it leaves what
         * readers are served as it is and returns the itinerary. Sent after the
         * draft has been edited, it publishes the edits.
         *
         * Publishing copies content rather than referencing it, so the published
         * version can never be altered or hollowed out by a later draft edit, not even
         * by deleting a segment outright. Each publish creates the next publication
         * and the live one is never rewritten in place.
         *
         * One publication is retained: the most recent. Publishing again supersedes
         * the previous one and its content is discarded, so there is no version
         * history to read back. The publication NUMBER is never reissued; it only ever
         * climbs, which is what lets a client tell one publication's cached tiles
         * apart from another's.
         *
         * Publication is what every published output of an itinerary hangs off: the
         * embed, the share link, and every export made without a session of the
         * owning organization naming a member all read the live publication this
         * sets. Only that organization's key with a session token naming a member
         * exports the draft instead, unless it passes `surface=published`; a token
         * minted without `user_id` reads as the key alone does. Publishing charges
         * nothing and raises no invoice: how many itineraries an organization may
         * have published at once is set by its agreement with Mountaya.
         *
         * Refused with 402 for one of three reasons. The first is the one every
         * `studio-embedded` operation shares: the organization does not hold
         * Embedded Studio. The second is the one every change to an organization's
         * itineraries shares: the organization does not hold Map Studio. The third
         * is publishing's own: the organization does not hold Map Publishing, the
         * product every published output of an itinerary belongs to. All three are
         * a plain `PAYMENT_REQUIRED`. Taking an itinerary down is never refused for
         * the last two, so the one 402 `deleteItineraryPublication` declares is the
         * first. A personal itinerary, owned by no organization, is never published,
         * and is refused with a `403`.
         *
         * Refused with a `429` coded `LIMIT_REACHED` for the organization's limit:
         * it already has as many itineraries published as its agreement allows,
         * which is none until the agreement names a number, and a higher one is
         * agreed rather than bought. The entry carries a `path`,
         * `["request", "params", "itinerary_id"]`, naming the itinerary that could
         * not take a place, and `extensions.metadata` names the ceiling,
         * `published-itineraries`, with how many it allows and how many are
         * published, so a client says what is full without a second read. It
         * carries no `Retry-After`, since no wait lifts it: it clears by
         * unpublishing something or by agreeing a higher limit. It refuses a FIRST
         * publish only: republishing an itinerary that is already live is never
         * refused by it. It holds for publishes sent at the same moment too: two
         * requests cannot both take the last place. Taking an itinerary down is
         * never refused by it.
         *
         * Refused with 409 when the itinerary has no segments: there would be
         * nothing to serve, so the itinerary's state forbids the publish until a
         * segment is added. The segments are counted again as the publication is
         * taken, so a last segment deleted while the publish runs is refused with
         * the same `409` rather than published empty. Refused with 409 as well when
         * ANY segment, waypoint, or group of the itinerary is locked in the
         * collaboration session, including by the user the session token names: a
         * held lock means an in-flight edit that has not been committed, and
         * publishing it would freeze a half-finished state.
         * When more than one refusal applies, the missing Embedded Studio product is
         * answered first (`402`), then the missing Map Studio product (`402`), then
         * the missing Map Publishing product (`402`), then an itinerary with no
         * segments (`409`), then the held lock (`409`), then the published-itinerary
         * limit (`429`), and last a stale `If-Match` (`412`). An itinerary with
         * nothing to publish is told so before the limit is asked, and a stale tag is
         * answered only for a publish that would otherwise go through.
         *
         * Publishing changes what a read of the itinerary returns, so it moves the
         * itinerary's `etag`. Send the itinerary's `ETag` as `If-Match` to publish
         * only the version you read; a stale tag is refused with `412` and nothing is
         * published.
         *
         * Takes no request body. Returns the itinerary with its full segments,
         * waypoints, and groups, with the derived status, published_at, and
         * has_unpublished_changes fields reflecting the new publication.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItineraryPublication(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryPublicationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItineraryPublicationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItineraryPublication,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/publication",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Create a segment, at the end of the chain or before another.
         *
         * Creates a new segment within the itinerary, at the end of its chain or
         * anywhere before it, and renumbers the chain.
         *
         * Without `before_segment_id`, the segment is appended to the end of the
         * chain, and it starts where the chain ends: on an itinerary that already has
         * segments, the first position of `geojson` is replaced with the last point
         * of the current last segment before the 100 km limit is checked.
         *
         * With `before_segment_id`, the segment is inserted ahead of that one, so the
         * first segment's identifier inserts at the head of the chain, and every
         * segment from that one on moves down a `position`. The new segment keeps
         * the geometry sent, and its neighbours move to meet it: the segment before
         * it has its last point moved onto the new segment's first point, and the
         * segment it is inserted before has its first point moved onto the new
         * segment's last point. Each neighbour that moved is recomputed through its
         * own mode, a `directions` neighbour being re-routed. An identifier that
         * names no segment of this itinerary's chain is a 400 naming
         * `before_segment_id`.
         *
         * A routed segment, appended, inserted or a neighbour re-routed, is pinned
         * onto the junctions it meets: the route the engine returns is joined to
         * each junction it does not reach, one off the routable network, by a short
         * straight connector. So a `directions` segment starts where the chain ends
         * and stays exactly connected to its neighbours, as a `linestring` one does.
         *
         * The create is atomic: if any of its routing fails, the request fails and
         * the itinerary is left exactly as it was. Waypoints the routing engine
         * cannot route between, one away from any routable way or two with no
         * connection between them, in the new segment or in a re-routed neighbour,
         * are a 400 naming `geojson`.
         *
         * The new segment joins a group only when BOTH of its neighbours already
         * belong to the same one. A segment appended at the end, or inserted at the
         * head, has only one neighbour and joins none, since joining would silently
         * extend the group's boundary.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does: a create can renumber every
         * following position and rewrite up to two neighbours, which one segment
         * would not show. `Location` and `extensions.metadata.created.id` name the
         * segment created.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the create conditional
         * on nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is created.
         *
         * Segment creates are routed writes, limited together with every other
         * routed segment write (split, merge, convert and update) to 120 requests a
         * minute per user and 1200 a minute per organization; past either, the
         * request is refused with **429** and `Retry-After`. The routing they do is
         * recorded as the organization's `studio-embedded` data usage, which
         * `getOrganizationUsageData` reports and which never counts toward the Data
         * API's per-minute limit, monthly allowance or invoice.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItinerarySegment(
            itinerary_id: kotlin.String,
            body: RestSchema.SegmentCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItinerarySegmentParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, RestSchema.ResponseMetadataCreatedPart> {
            val given = RestSchema.PostItinerarySegmentParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItinerarySegment,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segments",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.SegmentCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), RestSchema.ResponseMetadataCreatedPart.serializer())
        }

        /**
         * Convert a segment between routed and freehand.
         *
         * Converts the segment to the mode the body names, routed (`directions`) or
         * freehand (`linestring`), and re-derives its geometry accordingly. This is
         * its own operation rather than a field on `patchItinerarySegment` because
         * it can fail routing where a metadata update cannot, and giving it a
         * dedicated operation leaves exactly one way to change a segment's engine.
         *
         * directions to linestring keeps the routed line as the freehand line,
         * simplified and resampled as a line drawn on creation is
         * (`SegmentCreateParams.geojson`), and clears the turn-by-turn steps, which
         * describe nothing a freehand line carries. A routed line of more positions
         * than the 10,000 a segment write takes, if the simplification leaves it
         * past them, is simplified further at a coarser tolerance until it fits
         * them, keeping both its ends, so the freehand segment, like every segment,
         * is accepted back as read.
         *
         * linestring to directions derives the waypoints to route through, using the
         * segment's stored waypoints when it has them and otherwise a simplification
         * of the drawn line, tightened until it fits within the directions limit of
         * 45. It preserves the original endpoints exactly, and the routed segment is
         * held to the 100 km every routed segment is, measured along the straight
         * line through those waypoints. A routing failure aborts the change and
         * writes nothing: waypoints the routing engine cannot route between are a
         * 400 naming `geojson`, the segment's geometry.
         *
         * The mode is a state the segment is set to, so the request is safe to
         * repeat: sending the mode the segment already has changes nothing and
         * answers 200 with the itinerary.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does: the answer carries the
         * itinerary's recomputed totals beside the segment. Neither neighbour
         * changes, since both directions keep the segment's endpoints where they
         * are.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the conversion
         * conditional on nobody having changed the itinerary since you read it; a
         * stale tag is refused with `412` and nothing is changed.
         *
         * Conversions are routed writes, limited together with every other routed
         * segment write (create, split, merge and update) to 120 requests a minute
         * per user and 1200 a minute per organization; past either, the request is
         * refused with **429** and `Retry-After`. The routing they do is recorded as
         * the organization's `studio-embedded` data usage, which `getOrganizationUsageData`
         * reports and which never counts toward the Data API's per-minute limit,
         * monthly allowance or invoice.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param segment_id Identifier of the segment within the itinerary named by `itinerary_id`.
         * A segment belonging to another itinerary is a 404, not a 403.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItinerarySegmentConvert(
            itinerary_id: kotlin.String,
            segment_id: kotlin.String,
            body: RestSchema.SegmentConvertParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItinerarySegmentConvertParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItinerarySegmentConvertParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItinerarySegmentConvert,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segments/" + com.mountaya.client.encodePathSegment(segment_id) + "/convert",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.SegmentConvertParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Group a contiguous run of an itinerary's segments.
         *
         * Creates a segment group over the given segments. The segments themselves are
         * unchanged: grouping only overrides the name, color, and speed shown for that
         * run on the elevation profile and in the segment chooser, and every member
         * stays individually selectable and editable.
         *
         * Refused with 400 when the segments do not form a contiguous run of the
         * chain, when fewer than two are given, or when any of them does not belong
         * to this itinerary.
         *
         * Refused with 409, its entries coded `CONFLICT`, when any of them already
         * belongs to another group: one entry per such segment, naming its position
         * in `segment_ids`. That is a state of the chain rather than a fault in the
         * request, refused the same way until the other group lets the segment go,
         * so leave it out of the run or change that group first.
         *
         * Refused with 409 as well when a concurrent writer reached the same segments first,
         * either by grouping one of them between the validation and the write, or by
         * reordering the chain so the run the request names stops being contiguous.
         * Both are validated against the chain as the caller last read it and
         * re-asserted under the chain lock before anything is written, so the loser of
         * a race is told to reload rather than silently taking the winner's members or
         * committing a broken run. The two read the same on the wire and call for the
         * same response: re-read the itinerary and decide again against what it holds.
         *
         * Unlike the 409 on patchItinerarySegmentGroup and
         * deleteItinerarySegmentGroup, this one never carries
         * `extensions.metadata.holder` and uses the generic conflict shape: a holder
         * identifies the collaborator whose lock refused the write, and there is no
         * group to hold a lock on yet.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does: creating a group rewrites the
         * group_id of every member, so the group alone would leave the client's
         * segments stale. `Location` and `extensions.metadata.created.id` name the
         * group created.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the create conditional
         * on nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is created.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItinerarySegmentGroup(
            itinerary_id: kotlin.String,
            body: RestSchema.SegmentGroupCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItinerarySegmentGroupParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, RestSchema.ResponseMetadataCreatedPart> {
            val given = RestSchema.PostItinerarySegmentGroupParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItinerarySegmentGroup,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segment-groups",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.SegmentGroupCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), RestSchema.ResponseMetadataCreatedPart.serializer())
        }

        /**
         * Merge a segment with the one that follows it.
         *
         * Fuses the segment into a single segment together with the next one in the
         * chain, and renumbers the chain. The direction is fixed and takes no
         * parameter: on an ordered chain, merging segment i with i-1 is the same
         * operation as merging i-1 with its next, so a direction field would only add
         * a redundant way to express the same request. The merged result keeps the
         * identifier of the lower-positioned of the two.
         *
         * In directions mode the union of both segments' waypoints is re-routed as one
         * request. The two existing geometries are never simply joined end to end: the
         * junction may have been snapped, so the joined line would be a path routing
         * would never return, leaving distance, duration and steps inconsistent with
         * the geometry. In linestring mode the two stored lines are joined end to end,
         * and the joined line is simplified, resampled and analysed as a newly drawn
         * line is. The 3 m simplification removes the positions resampling added, and
         * with them any vertex drawn that lies within 3 m of the straight line between
         * the vertices kept on either side of it, the point where the two lines met
         * included. The MultiPoint of vertices drawn that either segment's `geojson`
         * carries is not read: the merged segment's vertices drawn are those of the
         * joined line that the simplification keeps.
         *
         * Refused with 409, its entry coded `CONFLICT` and naming no field, when the
         * chain does not allow the merge as it stands: the segment is the last of the
         * chain, so there is no next segment to fuse with, or the two segments use
         * different modes, or they belong to different groups. The same request is
         * refused the same way until the chain changes. Refused with 400 when their
         * combined waypoint count exceeds the directions limit of 45. The limit is
         * checked before the routing call, and the error states the 45-waypoint
         * limit. In directions mode the merged segment is held to the 100 km every
         * routed segment is, measured along the straight line through its
         * waypoints, so two segments that together exceed it are refused with 400
         * too, and waypoints the routing engine cannot route between are a 400
         * naming `geojson`, the merged segment's geometry, although a merge takes no
         * body. In linestring mode the joined line is held to the 500 km every
         * freehand segment is, measured as its stored distance is, so two lines that
         * together measure more are refused with 400 naming `geojson` in the same
         * way, and nothing is merged. When the vertices the simplification keeps of
         * the joined line number more than the 10,000 positions a segment's
         * `geojson` may carry, the merge is refused with 400, naming no field,
         * before anything is analysed, and nothing is merged; the error states the
         * 10,000-position limit.
         *
         * Takes no request body. Returns the whole itinerary with its segments,
         * waypoints, and groups, as every write to an itinerary's parts does: a
         * merge renumbers every following position and can change group membership.
         * When the two segments were a group's only two members, the group is
         * dissolved and the merged segment un-grouped, since a group never covers
         * fewer than two segments.
         *
         * Not safe to repeat blindly: sent twice, it merges the merged segment with
         * the one after it in turn. Send an `Idempotency-Key` and repeat it on a
         * retry, which is then answered with the first attempt's outcome. Send the
         * itinerary's `ETag` as `If-Match` to make the merge conditional on nobody
         * having changed the itinerary since you read it; a stale tag is refused
         * with `412` and nothing is merged.
         *
         * Merges are routed writes, limited together with every other routed segment
         * write (create, split, convert and update) to 120 requests a minute per
         * user and 1200 a minute per organization; past either, the request is
         * refused with **429** and `Retry-After`. The routing they do is recorded as
         * the organization's `studio-embedded` data usage, which `getOrganizationUsageData`
         * reports and which never counts toward the Data API's per-minute limit,
         * monthly allowance or invoice.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param segment_id Identifier of the segment within the itinerary named by `itinerary_id`.
         * A segment belonging to another itinerary is a 404, not a 403.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItinerarySegmentMerge(
            itinerary_id: kotlin.String,
            segment_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItinerarySegmentMergeParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItinerarySegmentMergeParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItinerarySegmentMerge,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segments/" + com.mountaya.client.encodePathSegment(segment_id) + "/merge",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Split a segment in two at a fraction of its length.
         *
         * Cuts the segment into two consecutive segments at the given fraction of its
         * length, and renumbers the chain. The first half keeps the original
         * identifier; the second is created after it. Both halves inherit the original
         * segment's group, so a split never breaks a group's contiguity.
         *
         * The first half keeps the original's description, color, speed and planned
         * start, and its name unless `first_name` renames it. The second half takes
         * the original's color and speed, and its name unless `second_name` is sent;
         * it starts with no description and no planned start, since both describe the
         * original as a whole.
         *
         * The fraction is projected server-side onto the stored geometry and inserted
         * as the first half's last vertex and the second half's first vertex, so the
         * two halves stay exactly connected and no neighbour recompute is triggered.
         * Both halves also take that junction as a waypoint. In directions mode BOTH
         * halves are re-routed (a sub-path of a route is not, in general, the route of
         * a sub-path), so a routing failure aborts the split and writes nothing:
         * waypoints the routing engine cannot route between, in either half, are a
         * 400 naming `geojson`, the segment's geometry.
         *
         * Refused with 400 when the projected point lands within 10 m of either
         * endpoint, since a small fraction is degenerate on a short segment.
         *
         * A split creates a segment, the second half, so it answers `201`: the
         * whole itinerary with its segments, waypoints, and groups, as every write
         * to an itinerary's parts does, since a split renumbers every following
         * position. `Location` and `extensions.metadata.created.id` name the second
         * half; the first keeps the identifier in the path.
         *
         * Send the itinerary's `ETag` as `If-Match` to make the split conditional on
         * nobody having changed the itinerary since you read it; a stale tag is
         * refused with `412` and nothing is split.
         *
         * Splits are routed writes, limited together with every other routed segment
         * write (create, merge, convert and update) to 120 requests a minute per
         * user and 1200 a minute per organization; past either, the request is
         * refused with **429** and `Retry-After`. The routing they do is recorded as
         * the organization's `studio-embedded` data usage, which `getOrganizationUsageData`
         * reports and which never counts toward the Data API's per-minute limit,
         * monthly allowance or invoice.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param segment_id Identifier of the segment within the itinerary named by `itinerary_id`.
         * A segment belonging to another itinerary is a 404, not a 403.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItinerarySegmentSplit(
            itinerary_id: kotlin.String,
            segment_id: kotlin.String,
            body: RestSchema.SegmentSplitParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItinerarySegmentSplitParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, RestSchema.ResponseMetadataCreatedPart> {
            val given = RestSchema.PostItinerarySegmentSplitParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItinerarySegmentSplit,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/segments/" + com.mountaya.client.encodePathSegment(segment_id) + "/split",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.SegmentSplitParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), RestSchema.ResponseMetadataCreatedPart.serializer())
        }

        /**
         * Mint the itinerary's share link, or return the one it has.
         *
         * Sets the itinerary's share link: mints one when the itinerary has none,
         * answering `201` with `Location` naming the link, and returns the existing
         * one otherwise, answering `200` with the same token as before. Calling it
         * again never mints a second link and never changes the token, so it is safe
         * to repeat; `postItineraryShareLinkRotate` is what replaces a token. Both
         * private and public itineraries can be shared.
         *
         * Minting takes a write role in the organization that owns the itinerary. A
         * member who can only view the itinerary cannot mint a link, and neither can
         * another organization's key that sees it only because it is public, so a
         * link never outlives the publisher making the itinerary private.
         *
         * A share link does not change what a read of the itinerary returns, so it
         * leaves the itinerary's `etag` where it is, takes no `If-Match`, and answers
         * no `ETag`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItineraryShareLink(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryShareLinkParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ShareLink, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItineraryShareLinkParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItineraryShareLink,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/share-link",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ShareLink.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Rotate the itinerary's share link.
         *
         * Replaces the token of the itinerary's share link with a freshly generated
         * one and returns it, so every URL built from the previous token stops
         * resolving: the cut-off for a link that has spread too far or outlived its
         * intended audience, without a window where no link works. It takes the same
         * write role the mint takes. Refused with 409 when the itinerary has no share
         * link to rotate: mint one with `postItineraryShareLink`.
         *
         * **Not safe to repeat blindly.** Every rotation mints a new token, so a
         * retry sent without the first attempt's `Idempotency-Key` rotates again: if
         * the first call succeeded but its answer was lost, the retry mints a third
         * token and invalidates the second, leaving a link nobody holds. Send an
         * `Idempotency-Key` and repeat it on the retry, which is then answered with
         * the first attempt's token; without one, read the link back with
         * `getItineraryShareLink` before deciding to call this again.
         *
         * A share link does not change what a read of the itinerary returns, so a
         * rotation leaves the itinerary's `etag` where it is and takes no
         * `If-Match`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItineraryShareLinkRotate(
            itinerary_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryShareLinkRotateParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ShareLink, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostItineraryShareLinkRotateParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItineraryShareLinkRotate,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/share-link/rotate",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ShareLink.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Create a new waypoint within an itinerary.
         *
         * Creates a new waypoint within the specified itinerary. Its `position` is
         * stored as the request gives it: a waypoint's order is the author's choice
         * rather than something the route derives.
         *
         * Returns the whole itinerary with its segments, waypoints, and groups, as
         * every write to an itinerary's parts does. `Location` and
         * `extensions.metadata.created.id` name the waypoint created.
         *
         * A create meets no lock, unlike `patchItineraryWaypoint` and
         * `deleteItineraryWaypoint`: a waypoint that does not exist yet cannot be
         * locked by a collaborator, so its one `409` is a request whose
         * `Idempotency-Key` is still in use. Send the itinerary's `ETag` as
         * `If-Match` to make the create conditional on nobody having changed the
         * itinerary since you read it; a stale tag is refused with `412` and nothing
         * is created.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param itinerary_id Identifier of the itinerary the operation acts on. An itinerary this
         * credential does not reach is a 404 rather than a 403, and so is one that does
         * not exist, so the parameter cannot be used to learn which itinerary ids exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postItineraryWaypoint(
            itinerary_id: kotlin.String,
            body: RestSchema.WaypointCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostItineraryWaypointParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.ItineraryWithDetails, RestSchema.ResponseMetadataCreatedPart> {
            val given = RestSchema.PostItineraryWaypointParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postItineraryWaypoint,
                    path = "/v1/itineraries/" + com.mountaya.client.encodePathSegment(itinerary_id) + "/waypoints",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.WaypointCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.ItineraryWithDetails.serializer(), RestSchema.ResponseMetadataCreatedPart.serializer())
        }
    }

    /** Layers is the `studio.layers` operations of the REST API. */
    public class Layers internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * List terrains, overlays, and the zone layer.
         *
         * Returns the map layer catalog: the terrain base maps (satellite among them),
         * the grouped overlay layers (for example slopes), the elevation raster the
         * terrains are shaded from, and the two layers that draw itineraries and
         * zones. Which overlay groups it carries follows the studio scope the
         * optional `organization_id` names. Under a publishable key or a share token
         * it may name only the credential's own organization and changes nothing,
         * and the overlays a map draws there are the ones the organization's overlay
         * allowlist admits, to which the Tile API holds every overlay tile. From
         * Mountaya's web application it
         * chooses between the person's own space and an organization they are a
         * member of, as its description states, and the catalog lists every overlay
         * in either, the ones the Tile API does not draw in that scope included.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * Requires a publishable key with the `publishing` scope, or the
         * `studio-embedded` scope that includes it, or a share token: a share view
         * draws its map from the same catalog an embed does. A map drawn with the
         * Tile API alone reads the same catalog from the Tile API, `getTileLayers`,
         * under the `tiles` scope and without Map Publishing; that one lists only the
         * overlays the organization's overlay allowlist admits.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getLayers(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetLayersParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Layers, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.GetLayersParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getLayers,
                    path = "/v1/layers",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("organization_id" to it) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, RestSchema.Layers.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Organizations is the `studio.organizations` operations of the REST API. */
    public class Organizations internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Resolve the organization a publishable key belongs to.
         *
         * Takes a publishable key and returns the organization it was minted for. It
         * is the bootstrap read an embed host makes before it knows anything: given
         * only the key it was configured with, it needs the branding, the theme and
         * the `safety_rules` its host page will be checked against, and it has no
         * organization id to ask `getOrganization` with.
         *
         * It is the one operation here that takes NO credential. The key travels in
         * the body as the subject of the question rather than in `X-API-Key` as the
         * answer to one, which is why sending it as a header does nothing. The answer
         * is the organization exactly as `getOrganization` serves it: its identity,
         * its settings and `white_label`, which is everything an embed paints by and
         * nothing about the account behind it. Neither blanket refusal in this
         * document's introduction applies here.
         *
         * UNKNOWN, REVOKED and UNDER-SCOPED keys, and every key of a canceled
         * organization, all answer `404` with the same body. What an embed needs is
         * the `publishing` scope, or the `studio-embedded` scope that includes it, and
         * no other scope is asked for. A key that exists but carries neither is
         * refused exactly as one that never existed, so this cannot be used to sort
         * real keys from invented ones, and there is deliberately no `403` for the
         * same reason: a refusal that said "this key is real but not for you" would
         * be the oracle the single answer exists to close.
         *
         * Whether the organization holds Map Publishing is not asked here: an
         * organization whose agreement does not include it is answered `200` all the
         * same, and the reads its embed then makes, `getOrganization` among them,
         * answer the plain `402` `PAYMENT_REQUIRED` while it does not. So a `200`
         * says the key can open an embed, not that the embed will draw.
         *
         * It is limited to 60 requests a minute per client IP address rather than
         * per organization, since there is no organization until it answers, an
         * IPv6 address counted by its `/64` prefix. Every request counts, a `404`
         * included, and one past the limit is a `429` carrying `Retry-After`, the
         * seconds left in the minute. The limit is best effort.
         *
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationLookup(
            body: RestSchema.OrganizationLookupParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostOrganizationLookupParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Organization, kotlinx.serialization.json.JsonObject?> {
            RestSchema.PostOrganizationLookupParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationLookup,
                    path = "/v1/organizations/lookup",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.OrganizationLookupParams.serializer(), body)),
                    options = options,
                )
            return sender.send(call, RestSchema.Organization.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Zones is the `studio.zones` operations of the REST API. */
    public class Zones internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Delete a zone.
         *
         * Permanently deletes the zone and removes it from every collection that
         * held it. This action cannot be undone.
         *
         * Send the zone's `etag` as `If-Match` to make the delete conditional: a
         * zone changed since that read is a `412`, and nothing is deleted.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param zone_id Identifier of the zone the operation acts on.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteZone(
            zone_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteZoneParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteZoneParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteZone,
                    path = "/v1/zones/" + com.mountaya.client.encodePathSegment(zone_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Take the zone's publication down.
         *
         * Takes the zone off the public surfaces. Mountaya stops serving it from the
         * next request: a read of it as published answers `404`, the tiles drawn from
         * published content, Mountaya's hosted embed among them, stop drawing it, and
         * Mountaya's own caches stop serving its published tiles: they are not
         * evicted, but nothing reaches them and they age out. An answer a
         * shared cache between a reader and Mountaya already holds is served from
         * there until its lifetime runs out, which taking the zone down does not
         * shorten: at most 6 minutes for a published read of the zone
         * (`max-age=300` and `stale-while-revalidate=60`), and 5 for a public tile
         * of it (`max-age=300`).
         *
         * It acts on a state, so a zone that is not published is answered `200` as
         * well, whether it never was or is already taken down: the request leaves it
         * unpublished either way, and a repeated request is harmless.
         *
         * The publication is kept, not deleted. Publishing again therefore creates the
         * NEXT publication rather than reusing the number that was live: a publication
         * number is never reissued, so a client that cached one can always tell the
         * two apart. What unpublishing clears is whether a publication is live, not
         * the publication itself.
         *
         * The draft is untouched. The studio reads and writes the draft whatever is
         * published, so unpublishing changes nothing a member sees in the editor; it
         * changes what everyone else sees, which is nothing.
         *
         * Unlike publishing, this is never refused for a held collaboration lock.
         * Publishing freezes a state and so cannot run over an in-flight edit;
         * unpublishing freezes nothing.
         *
         * Unpublishing frees the zone's place under the organization's
         * published-zones limit at once. It is never refused for Map Studio, for Map
         * Publishing or for that limit, so an organization whose agreement includes
         * neither can still take its zones down, and the one 402 it declares is the
         * one every `studio-embedded` operation shares.
         *
         * Taking the publication down changes what a read of the zone returns, so it
         * moves the zone's `etag`. Send the zone's `ETag` as `If-Match` to make it
         * conditional on nobody having changed the zone since you read it; a stale
         * tag is refused with `412`.
         *
         * Takes no request body. Returns the zone's draft, with `status` back to
         * draft and `published_at` null.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param zone_id Identifier of the zone the operation acts on.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteZonePublication(
            zone_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.DeleteZonePublicationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Zone, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.DeleteZonePublicationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteZonePublication,
                    path = "/v1/zones/" + com.mountaya.client.encodePathSegment(zone_id) + "/publication",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Zone.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve a single zone with its geometry.
         *
         * Returns the zone, including the full `geojson` geometry, `created_by`, and
         * the temporal validity window (`start_at` / `end_at`). This is the
         * authoritative geometry for editing, unlike the simplified, clipped
         * geometry served by the tile endpoint.
         *
         * A COLLECTION share token also reaches this endpoint, for the zones inside
         * the collection it was minted for, read-only and regardless of privacy. An
         * itinerary token never does: it grants exactly its one itinerary. A zone
         * has no share link of its own.
         *
         * WHICH VERSION you are served depends on the credential. A publishable key
         * alone, and a share token, are served the last PUBLISHED snapshot; the same
         * key with a session token naming a member is served its own organization's
         * DRAFT, which is what the studio edits, unless it passes
         * `surface=published`. A session token minted without `user_id`, such as the
         * one a read-only viewer runs on, is served what the key alone is, the
         * published snapshot and never the draft, whatever surface it was minted
         * for. Another organization's public zone is served as it was last published
         * whatever the credential. Whenever the published snapshot is what a request
         * is served, a zone that has never been published, or has been taken down,
         * answers 404, and so does one whose organization's agreement does not
         * include Map Publishing, which withdraws what the organization published
         * rather than refusing it; a share token of such an organization does not
         * resolve at all and answers `401`. A bare publishable key, and the key with
         * a session token minted without `user_id`, reaches its own organization's
         * zones whether public or private, and another organization's public one; a
         * share token reaches only what its link names. See the read tiers in the
         * API description.
         *
         * Refused with 403 when `surface=published` names a PERSONAL zone, that is,
         * one owned by no organization, as a personal itinerary is. The published
         * surface is what an embed is made of, and an embed is an output of an
         * organization's products, which a personal zone, owned by none, never has,
         * whatever the credential. A request without the opt-in is not refused this
         * way. Only the person whose zone it is reads it as its draft and is refused
         * this way; every other caller, every publishable key included, reads it as
         * published, is served nothing of a personal zone, which is never published,
         * and is answered `404` before this refusal is reached, as a caller who
         * cannot view it is.
         *
         * The answer carries `ETag`, the version of what it returns, which is also
         * the body's `etag`: send it as `If-Match` on a write to the zone to make
         * that write conditional on nobody having changed it since.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * A publishable key needs the `publishing` scope, or the `studio-embedded`
         * scope that includes it. A share token carries its own authority and needs
         * none.
         *
         * @param zone_id Identifier of the zone the operation acts on.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getZone(
            zone_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetZoneParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Zone, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.GetZoneParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getZone,
                    path = "/v1/zones/" + com.mountaya.client.encodePathSegment(zone_id),
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("surface" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, RestSchema.Zone.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List zones.
         *
         * Returns the zones of the key's organization, one page at a time. A zone
         * belongs to the organization that owns it, not to the user who drew it, so
         * this is the listing every member shares.
         *
         * The listing is always the key's organization. `organization_id` may name
         * that organization, which changes nothing, and naming any other
         * organization is a `403` whether or not it exists. A personal zone, owned
         * by no organization, never appears.
         *
         * WHICH zones appear depends on the credential. A publishable key alone
         * lists the organization's PUBLIC zones that are currently published, and
         * serves the published snapshot of each: one never published, or taken
         * down, is absent, and so is every zone while the organization's agreement
         * does not include Map Publishing. The same key with a session token naming
         * a member lists its private ones and its unpublished ones as well, and
         * serves the draft of each; a session token minted without `user_id`,
         * whatever surface it was minted for, lists and serves what the key alone
         * does. A caller that finds its own zones missing is almost always sending
         * the key without a session naming a member. A share token does not open
         * this listing: the zones a collection link reaches are listed with
         * `getCollectionZones`. See the read tiers in the API description.
         *
         * `level`, `mode` and `active_at` narrow the listing to the zones a map
         * draws: of one severity, drawn with one tool, or in force at one instant.
         * Every filter sent narrows the listing further, and none of them widens
         * what the credential reaches: a zone is listed only when it matches every
         * one sent. They read each zone as it is served, so under a key alone a zone
         * is matched on its last published snapshot. `status` and `is_private` read
         * each zone as it is served too, so under a key alone, and under a session
         * token minted without `user_id`, `status=draft` and `is_private=true` list
         * nothing.
         *
         * The page is cut after authorization, so it never holds a zone the caller
         * may not see. The listing ends at the page whose
         * `extensions.metadata.pagination.next_cursor` is `null`.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization, and a key with a session token
         * naming a user to 1200 per user the token names and then 6000 per
         * organization, counted apart from the reads made under the key alone. Past
         * any of them the read is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `publishing` scope, or the
         * `studio-embedded` scope that includes it.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getZones(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetZonesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<RestSchema.Zone>, RestSchema.ListMetadata> {
            val given = RestSchema.GetZonesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getZones,
                    path = "/v1/zones",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("organization_id" to it) }
                            given.slot3?.let { add("status" to it.value) }
                            given.slot4?.let { add("is_private" to it.toString()) }
                            given.slot5?.let { add("level" to it.value) }
                            given.slot6?.let { add("mode" to it.value) }
                            given.slot7?.let { add("active_at" to com.mountaya.client.formatTimestamp(it)) }
                            given.slot8?.let { add("updated_since" to com.mountaya.client.formatTimestamp(it)) }
                            given.slot9?.let { add("q" to it) }
                            given.slot10?.let { add("sort" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(RestSchema.Zone.serializer()), RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getZones` answers, one row at a time, requesting a page only as its
         * collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetZones(
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.GetZonesParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<RestSchema.Zone> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getZones(
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Update an existing zone.
         *
         * Updates the zone's name, description, level and temporal validity window.
         * Every field is optional and an omitted field keeps its current value. The
         * area may be replaced by providing `geojson`, which must be a Polygon whose
         * outline no meridian and no parallel crosses more than 512 times, every
         * ring counted, and which is a valid polygon in degrees as sent and once
         * projected to Web Mercator, as a map draws it: an outline too intricate, a
         * ring that crosses itself or another ring, in either, a ring that touches
         * itself, or a hole outside its shell is a `400` naming `geojson.geometry`. When omitted
         * the existing area is carried forward, as it is. An area computed from a
         * `source` held to none of these, or of more than 10,000 positions, is a
         * `400` naming `source`, and nothing is written. A `mode` is sent with the area
         * it describes: beside `geojson` any mode, beside a `source` only `polygon`,
         * and a `mode` sent with neither is a `400` naming `mode`. Only a zone whose
         * mode, sent or kept, is `polygon` takes a `source`: on any other mode it is a
         * `400` naming `source`. `is_private` makes the zone private or public,
         * independently of whether it is published. An `end_at` earlier than the
         * zone's resulting `start_at`, whether either was sent or kept, is a `400`
         * naming `end_at`. Every call writes a new revision, even one that changes
         * nothing, so `revision` and `updated_at` move and, on a published zone,
         * `has_unpublished_changes` is set: what the public surfaces serve moves
         * only at the next `postZonePublication`, privacy aside, which is read from
         * the draft and takes effect at once.
         *
         * A zone's name is unique within the organization: renaming it to a name
         * another zone there already carries is a `409` naming that zone. Sending its
         * current name again is not a conflict.
         *
         * While another member of the zone's collaboration session holds the soft
         * edit lock on the zone, taken with a `lock.acquire` through
         * `postZoneCollab`, the update is a `409` naming that member, and nothing
         * is written. A lock held by the user the session token names does not
         * refuse it. A lock lapses 60 seconds after its holder's editor stream ends,
         * so the update succeeds once the holder releases it or goes away.
         *
         * Send the zone's `etag` as `If-Match` to make the update conditional: a
         * zone changed since that read is a `412`, and nothing is written.
         *
         * The organization the zone belongs to must hold Map Studio, or the update is
         * refused with a `402`. A zone owned by no organization is personal and is
         * never edited: it stays readable and can be deleted, and an update to it is
         * refused with a `403`.
         *
         * An update that sends a `source` runs that computation again for the
         * organization. Under a key each run is recorded as `studio-embedded` data usage, so
         * the organization sees it, and is never counted toward the Data API's
         * per-minute limit, its monthly allowance or its invoice.
         *
         * Updating zones is limited to 30 per user and 300 per organization per
         * minute; over either, the update is a `429` carrying `Retry-After`, and
         * nothing is written.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param zone_id Identifier of the zone the operation acts on.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchZone(
            zone_id: kotlin.String,
            body: RestSchema.ZoneUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PatchZoneParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Zone, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PatchZoneParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchZone,
                    path = "/v1/zones/" + com.mountaya.client.encodePathSegment(zone_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.ZoneUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Zone.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Create a new zone.
         *
         * Creates a zone in the key's organization: a named area drawn by hand as a
         * single Polygon, or computed from a `source`. Any geometry other than a
         * Polygon, a `null` geometry included, is a `400` naming `geojson.geometry`,
         * and so is a Polygon whose outline one meridian or one parallel crosses
         * more than 512 times, every ring counted, which is too intricate to be
         * stored, and one that is not a valid polygon, in degrees as sent or once
         * projected to Web Mercator, as a map draws it: a ring that crosses itself or
         * another ring, a ring that touches itself, or a hole outside its shell. A
         * straight edge in degrees is drawn as another line on a map, so an outline
         * valid in degrees can cross itself once projected, and is refused. An
         * area computed from a `source` held to none of these, or of more than 10,000
         * positions, is a `400` naming `source`: change its location or range, or
         * draw the zone. An `end_at` earlier than `start_at` is a `400` naming
         * `end_at`.
         *
         * A new zone is a draft: the public surfaces serve nothing of it until
         * `postZonePublication` publishes it, whatever `is_private` says, and
         * `is_private` decides who may read it once it is.
         *
         * A zone's name is unique within the organization: a name another zone there
         * already carries is a `409` naming that zone. So is an id: a `geojson.id`
         * another zone already has is a `409` naming it.
         *
         * A new zone always belongs to an organization, and the organization must hold
         * Map Studio, or the create is refused with a `402`. No new zone is
         * personal: a request that would create one owned by no organization, which
         * only Mountaya's web application can send, is refused with a `403`.
         *
         * A zone computed from a `source` runs that computation for the
         * organization. Under a key each run is recorded as `studio-embedded` data usage, so
         * the organization sees it, and is never counted toward the Data API's
         * per-minute limit, its monthly allowance or its invoice.
         *
         * Creating zones is limited to 30 per user and 300 per organization per
         * minute; over either, the create is a `429` carrying `Retry-After`, and
         * nothing is created.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postZone(
            body: RestSchema.ZoneCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostZoneParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Zone, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostZoneParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postZone,
                    path = "/v1/zones",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(RestSchema.ZoneCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Zone.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Publish the zone's current draft.
         *
         * Sets the zone's publication to its current draft: the draft's name,
         * description, level, mode, area, validity window and source are frozen into
         * a publication and become what the public surfaces serve. Until the first
         * publish, the public surfaces resolve nothing for the zone: a read of it as
         * published answers `404`, and the tiles drawn from published content,
         * Mountaya's hosted embed among them, do not draw it. After it, they serve
         * that snapshot and are unaffected by any further draft edit until the zone
         * is published again. The studio always reads and writes the draft, whatever
         * is published.
         *
         * The publication is a state the zone is set to, so the request is safe to
         * repeat: sent again with nothing changed in between, it leaves what readers
         * are served as it is and returns the zone. Sent after the draft has been
         * edited, it publishes the edits.
         *
         * Publishing copies content rather than referencing it, so the published
         * version can never be altered by a later draft edit. Each publish creates
         * the next publication and the live one is never rewritten in place.
         *
         * One publication is retained: the most recent. Publishing again supersedes
         * the previous one and its content is discarded, so there is no version
         * history to read back. The publication NUMBER is never reissued; it only ever
         * climbs, which is what lets a client tell one publication's cached tiles
         * apart from another's.
         *
         * Publication is what every published output of a zone hangs off: every
         * read, listing and tile made without a session of the owning organization
         * naming a member, a collection's share link among them, and Mountaya's
         * hosted embed, which draws a collection's published public zones, all read
         * the live publication this sets. Only that organization's key with a
         * session token naming a member reads the draft instead, unless it passes
         * `surface=published`; a token minted without `user_id` reads as the key
         * alone does. A zone has no share link of its own. Publishing charges
         * nothing and raises no invoice: how many zones an organization may have
         * published at once is set by its agreement with Mountaya.
         *
         * Refused with 402 for one of three reasons. The first is the one every
         * `studio-embedded` operation shares: the organization does not hold
         * Embedded Studio. The second is the one every change to an organization's
         * zones shares: the organization does not hold Map Studio. The third is
         * publishing's own: the organization does not hold Map Publishing, the
         * product every published output of a zone belongs to. All three are a
         * plain `PAYMENT_REQUIRED`. Taking a zone down is never refused for the last
         * two, so the one 402 `deleteZonePublication` declares is the first. A
         * personal zone, owned by no organization, is never published, and is
         * refused with a `403`.
         *
         * Refused with a `429` coded `LIMIT_REACHED` for the organization's limit:
         * it already has as many zones published as its agreement allows, which is
         * none until the agreement names a number, and a higher one is agreed rather
         * than bought. The entry carries a `path`,
         * `["request", "params", "zone_id"]`, naming the zone that could not take a
         * place, and `extensions.metadata` names the ceiling, `published-zones`,
         * with how many it allows and how many are published, so a client says what
         * is full without a second read. It carries no `Retry-After`, since no wait
         * lifts it: it clears by unpublishing something or by agreeing a higher
         * limit. It refuses a FIRST publish only: republishing a zone that is
         * already live is never refused by it. It holds for publishes sent at the
         * same moment too: two requests cannot both take the last place. Taking a
         * zone down is never refused by it.
         *
         * Refused with 409 when the zone is locked in the collaboration session,
         * including by the user the session token names: a held lock means an
         * in-flight edit that has not been committed, and publishing it would freeze
         * a half-finished state. When more than one refusal applies, the missing
         * Embedded Studio product is answered first (`402`), then the missing Map
         * Studio product (`402`), then the missing Map Publishing product (`402`),
         * then the held lock (`409`), then the published-zone limit (`429`), and
         * last a stale `If-Match` (`412`), so a stale tag is answered only for a
         * publish that would otherwise go through.
         *
         * Publishing changes what a read of the zone returns, so it moves the zone's
         * `etag`. Send the zone's `ETag` as `If-Match` to publish only the version you
         * read; a stale tag is refused with `412` and nothing is published.
         *
         * Takes no request body. Returns the zone's draft, with the derived status,
         * published_at, and has_unpublished_changes fields reflecting the new
         * publication.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member with a write
         * role, whom the request acts as. A publishable key without a session token
         * minted for `studio-embedded` naming a user, a token minted for another
         * surface included, is refused with **401**.
         *
         * @param zone_id Identifier of the zone the operation acts on.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postZonePublication(
            zone_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: RestSchema.PostZonePublicationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<RestSchema.Zone, kotlinx.serialization.json.JsonObject?> {
            val given = RestSchema.PostZonePublicationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postZonePublication,
                    path = "/v1/zones/" + com.mountaya.client.encodePathSegment(zone_id) + "/publication",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, RestSchema.Zone.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }
}
