package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema

/**
 * Every member of REST's `ManeuverType` this build spells, in the order of their values: what the
 * steps' adapters recognise a maneuver by, and what an instruction has a key for.
 */
internal val navigationManeuvers: List<RestSchema.ManeuverType> =
    listOf(
        RestSchema.ManeuverType.Continue,
        RestSchema.ManeuverType.Depart,
        RestSchema.ManeuverType.EnterRoundabout,
        RestSchema.ManeuverType.ExitRoundabout,
        RestSchema.ManeuverType.Goal,
        RestSchema.ManeuverType.KeepLeft,
        RestSchema.ManeuverType.KeepRight,
        RestSchema.ManeuverType.SharpLeft,
        RestSchema.ManeuverType.SharpRight,
        RestSchema.ManeuverType.SlightLeft,
        RestSchema.ManeuverType.SlightRight,
        RestSchema.ManeuverType.TurnLeft,
        RestSchema.ManeuverType.TurnRight,
        RestSchema.ManeuverType.Unknown,
        RestSchema.ManeuverType.Uturn,
    )

/**
 * waypointManeuver is the maneuver of a waypoint passed on the way, an arrival before a route's last, which a route
 * joined over an itinerary's segments says where one segment ends and the next begins
 * (`geo.navigation.itinerary-route`). No surface writes it: a step either surface sends spelled so reads as `unknown`.
 */
public val waypointManeuver: RestSchema.ManeuverType = RestSchema.ManeuverType("waypoint")

/** The members by their REST spelling. */
private val restManeuvers: Map<String, RestSchema.ManeuverType> = navigationManeuvers.associateBy { it.value }

/**
 * The members by their Data API spelling. The transform is the contract's own: a REST member is its
 * SDL member lowercased with each underscore turned into a hyphen, so the SDL member is the REST
 * member uppercased with each hyphen turned into an underscore. Every member is ASCII.
 */
private val dataManeuvers: Map<String, RestSchema.ManeuverType> =
    navigationManeuvers.associateBy { it.value.uppercase().replace('-', '_') }

/**
 * Returns the maneuver [type] names as REST spells it: the member of the contract's set it is, or
 * `unknown` where this build spells no member that way, a member the API added since or another
 * surface's spelling (`TURN_LEFT`). Matched exactly, letter case included.
 */
internal fun maneuverOf(type: RestSchema.ManeuverType): RestSchema.ManeuverType =
    restManeuvers[type.value] ?: RestSchema.ManeuverType.Unknown

/**
 * Returns the REST member the Data API's [type] names, as SCREAMING_SNAKE spells it: `TURN_LEFT` is
 * `turn-left`. A value the Data API does not spell, a member it added since or REST's own spelling
 * (`turn-left`), reads as `unknown`, and so does a step that states none.
 */
internal fun dataManeuverOf(type: DataSchema.ManeuverType?): RestSchema.ManeuverType =
    type?.let { dataManeuvers[it.value] } ?: RestSchema.ManeuverType.Unknown
