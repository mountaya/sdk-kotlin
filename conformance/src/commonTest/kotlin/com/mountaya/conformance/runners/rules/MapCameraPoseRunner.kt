package com.mountaya.conformance

import com.mountaya.elements.CameraTarget
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.ScriptedMapSurface
import kotlinx.coroutines.test.TestScope

/**
 * `map.camera.pose`: what a camera move hands the renderer, through the engine a consumer drives and the
 * published scripted surface, which records each move: the members the case's `pose` states, each `unstated`
 * member left null, which is how a Kotlin caller forwards a member it may not have. A member a `CameraTarget`
 * cannot hold, a centre written as an object or a pitch written as text, is refused by the type before any
 * call, as the kind the fixture names for that member. A refused move hands the renderer nothing.
 */
object MapCameraPoseRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val unstated =
            input["unstated"]
                ?.let {
                    fixtureTexts((it as? FixtureValue.Array)?.items.orEmpty(), "unstated")
                }.orEmpty()
                .toSet()
        val pose = input.objectOrNull("pose") ?: FixtureValue.Object(emptyList())
        var center: List<Double>? = null
        val writtenCenter = pose["center"]
        if (writtenCenter != null && "center" !in unstated) {
            center =
                MapFixtures.numbersOrNull(writtenCenter)
                    ?: return ConformanceOutcome.Value(MapFixtures.refusedPose("position-refused"))
        }
        val numbers = mutableMapOf<String, Double>()
        for (member in listOf("zoom", "bearing", "pitch")) {
            if (member in unstated) continue
            val written = pose[member] ?: continue
            numbers[member] =
                (written as? FixtureValue.Number)?.value
                    ?: return ConformanceOutcome.Value(MapFixtures.refusedPose("number-refused"))
        }
        val target = CameraTarget(center, numbers["zoom"], numbers["bearing"], numbers["pitch"])
        val move = input.text("move")

        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        try {
            when (move) {
                "jump" -> map.jump(target)
                "ease" -> map.ease(target)
                "fly" -> map.fly(target)
                else -> throw AssertionError("no camera move is called `$move`")
            }
        } catch (refused: MapException) {
            if (surface.moves.isNotEmpty()) throw AssertionError("a refused move handed the renderer a move")
            return ConformanceOutcome.Value(MapFixtures.refusedPose(MapFixtures.kind(refused)))
        }
        val handed =
            surface.moves.singleOrNull()?.target
                ?: throw AssertionError("the move handed the renderer ${surface.moves}")
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "pose" to MapFixtures.target(handed),
                "refused" to FixtureValue.Null,
            ),
        )
    }
}
