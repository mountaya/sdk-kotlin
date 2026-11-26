package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.FixtureValue

/**
 * The route [fields] writes, its `line` of `[longitude, latitude]` vertices, its `steps` written as
 * `geo.navigation.progress` writes them, and its `duration` where it states one, as a route source answers
 * one, before it is followed.
 */
internal fun fixtureRoute(fields: FixtureValue.Object): NavigationRoute =
    NavigationRoute(
        line = fields.items("line").map(::fixtureVertex),
        steps = fields.items("steps").map(::fixtureStep),
        duration = fields.numberOrNull("duration"),
    )

/** The followed route of the `line` and `steps` [fields] writes, through [followedRouteOf], as an engine follows one. */
internal fun fixtureFollowedRoute(fields: FixtureValue.Object): FollowedRoute =
    followedRouteOf(fixtureRoute(fields)) ?: throw AssertionError("the case's line holds no vertex")

/** A vertex or a position a case writes, its numbers in order. */
internal fun fixtureVertex(value: FixtureValue): List<Double> =
    (value as? FixtureValue.Array)?.items?.map {
        (it as? FixtureValue.Number)?.value
            ?: throw AssertionError("a coordinate is no number")
    }
        ?: throw AssertionError("a vertex is no list")

/** The step [value] writes in the steps vocabulary; a member the fixture leaves out or writes null is none. */
private fun fixtureStep(value: FixtureValue): NavigationStep {
    val fields = value as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
    return NavigationStep(
        fromIndex = whole(fields, "fromIndex") ?: throw AssertionError("a step has no `fromIndex`"),
        toIndex = whole(fields, "toIndex") ?: throw AssertionError("a step has no `toIndex`"),
        maneuver = RestSchema.ManeuverType(fields.text("maneuver")),
        name = fields.textOrNull("name"),
        distance = fields.numberOrNull("distance"),
        duration = fields.numberOrNull("duration"),
        exitNumber = whole(fields, "exitNumber"),
    )
}

/** The progress [value] writes as `geo.navigation.progress` answers one, or null for none. */
internal fun fixtureProgress(value: FixtureValue?): NavigationProgress? {
    val fields = value as? FixtureValue.Object ?: return null
    return NavigationProgress(
        along = fields.number("along"),
        step = whole(fields, "step") ?: throw AssertionError("a progress names no `step`"),
        next = whole(fields, "next"),
        toManeuver = fixtureRemainder(fields.objectOrNull("toManeuver")),
        toArrival = fixtureRemainder(fields.objectOrNull("toArrival")),
    )
}

/** [progress] as `geo.navigation.progress` writes it, or null for none. */
internal fun progressValue(progress: NavigationProgress?): FixtureValue {
    progress ?: return FixtureValue.Null
    return FixtureValue.Object(
        listOf(
            "along" to FixtureValue.Number(progress.along),
            "next" to (progress.next?.let { FixtureValue.Number(it.toDouble()) } ?: FixtureValue.Null),
            "step" to FixtureValue.Number(progress.step.toDouble()),
            "toArrival" to remainderValue(progress.toArrival),
            "toManeuver" to remainderValue(progress.toManeuver),
        ),
    )
}

private fun fixtureRemainder(fields: FixtureValue.Object?): NavigationRemainder {
    val written = fields ?: throw AssertionError("a progress states no remainder")
    return NavigationRemainder(written.number("distance"), written.numberOrNull("duration"))
}

private fun remainderValue(remainder: NavigationRemainder): FixtureValue =
    FixtureValue.Object(
        listOf(
            "distance" to FixtureValue.Number(remainder.distance),
            "duration" to (remainder.duration?.let { FixtureValue.Number(it) } ?: FixtureValue.Null),
        ),
    )

/** The whole number [fields] writes at [key], or null where it writes none. */
private fun whole(
    fields: FixtureValue.Object,
    key: String,
): Int? {
    val number = fields.numberOrNull(key) ?: return null
    if (number != kotlin.math.floor(number)) throw AssertionError("`$key` is no whole number")
    return number.toInt()
}
