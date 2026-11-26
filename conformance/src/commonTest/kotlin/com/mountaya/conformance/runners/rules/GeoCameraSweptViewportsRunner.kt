package com.mountaya.conformance

import com.mountaya.geo.CameraPose
import com.mountaya.geo.capturesSweptViewports
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.swept-viewports`: whether a recording capturing the case's `captured` poses captures
 * the viewports its sweep jumped through, its `swept` poses, each pose built as its own object.
 */
object GeoCameraSweptViewportsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        return ConformanceOutcome.Value(
            FixtureValue.Bool(capturesSweptViewports(poses(input, "swept"), poses(input, "captured"))),
        )
    }

    private fun poses(
        input: FixtureValue.Object,
        key: String,
    ): List<CameraPose> =
        input.items(key).map { item ->
            val pose = item as? FixtureValue.Object ?: throw AssertionError("a pose of `$key` is not an object")
            CameraPose(
                center = fixturePosition(pose["center"], "center"),
                zoom = pose.number("zoom"),
                pitch = pose.number("pitch"),
                bearing = pose.number("bearing"),
            )
        }
}
