package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapException
import com.mountaya.elements.ScriptedMapSurface
import kotlinx.coroutines.test.TestScope

/**
 * `map.camera.fit-options`: what a drawing call frames its content with, through the engine's drawing call,
 * `setCollection(id, fit)`, over an exchange answering a collection's specification stating a box, and the
 * scripted surface, which records each box it is asked to frame: `true` is a fit stating nothing, `false` and a fit
 * left out are none, and options are a `CameraFit` of the members they state, each `unstated` member left
 * null. A fit that is neither a boolean nor options cannot be handed to the call at all, and a padding or a
 * ceiling a `CameraFit` cannot hold, text, is refused by the type before any call. A duration of no finite
 * length is a Kotlin `Duration.INFINITE`, which the engine refuses itself. A refused fit reads nothing, which
 * the runner holds to. The options framed with are written as the case wrote them, a padding of one number for
 * every edge as that number.
 */
object MapCameraFitOptionsRunner : ConformanceRunner {
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
        val fit =
            when (val read = MapFixtures.readFit(input["fit"], unstated)) {
                is MapFixtures.FitReading.Refused -> return ConformanceOutcome.Value(
                    MapFixtures.fitAnswer(false, null, read.kind),
                )

                is MapFixtures.FitReading.Read -> read.fit
            }

        val body = MapContentFixtures.specificationBody(MapFixtures.collection)
        val exchange = MapContentFixtures.Serving { 200 to body }
        val map = MapEngine(MapFixtures.client(exchange = exchange), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        val drawn =
            try {
                map.setCollection("c", fit)
            } catch (refused: MapException) {
                if (map.state.value.layers
                        .isNotEmpty() || surface.frames.isNotEmpty() || exchange.sent.isNotEmpty()
                ) {
                    throw AssertionError("a refused fit read, drew or framed something")
                }
                return ConformanceOutcome.Value(MapFixtures.fitAnswer(false, null, MapFixtures.kind(refused)))
            }
        if (surface.frames.size > 1) throw AssertionError("the call framed its content more than once")
        val paddingAsOne = (input["fit"] as? FixtureValue.Object)?.get("padding") is FixtureValue.Number
        val framed = surface.frames.firstOrNull()?.let { MapFixtures.fitOptions(it.fit, paddingAsOne) }
        return ConformanceOutcome.Value(MapFixtures.fitAnswer(drawn, framed ?: FixtureValue.Null, null))
    }
}
