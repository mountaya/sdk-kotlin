package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.NavigationStep

/**
 * The navigation step [value] writes in the steps vocabulary, `fromIndex`, `toIndex`, `maneuver`,
 * `name`, `distance`, `duration` and `exitNumber`, as Geo's [NavigationStep]. A member the fixture
 * leaves out or writes null is none; a range is required.
 */
internal fun fixtureNavigationStep(value: FixtureValue): NavigationStep {
    val fields = value as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
    return NavigationStep(
        fromIndex = wholeOrNull(fields, "fromIndex") ?: throw AssertionError("a step has no `fromIndex`"),
        toIndex = wholeOrNull(fields, "toIndex") ?: throw AssertionError("a step has no `toIndex`"),
        maneuver = RestSchema.ManeuverType(fields.text("maneuver")),
        name = fields.textOrNull("name"),
        distance = fields.numberOrNull("distance"),
        duration = fields.numberOrNull("duration"),
        exitNumber = wholeOrNull(fields, "exitNumber"),
    )
}

/** [step] as the fixtures write a step of the vocabulary, every member stated and null where it holds none. */
internal fun navigationStepValue(step: NavigationStep): FixtureValue =
    FixtureValue.Object(
        listOf(
            "distance" to numberOrNull(step.distance),
            "duration" to numberOrNull(step.duration),
            "exitNumber" to numberOrNull(step.exitNumber?.toDouble()),
            "fromIndex" to FixtureValue.Number(step.fromIndex.toDouble()),
            "maneuver" to FixtureValue.Text(step.maneuver.value),
            "name" to (step.name?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
            "toIndex" to FixtureValue.Number(step.toIndex.toDouble()),
        ),
    )
