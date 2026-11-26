package com.mountaya.elements

import com.mountaya.client.GeoJsonPoint
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `map.geometry.layers`: how a geometry slot draws, through the internal rule the engine applies, in the case's
 * colour or the slot's own, in the map's paint with the case's patch folded on: its source, its band and its
 * layers. Each slot's source, band and own colour, and the circle a highlighted point is drawn as, are held to
 * the rule's own, and the engine is held to draw the same layers for a point.
 */
object MapGeometryLayersRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        MapRunnerFixtures.holdConstants(
            case,
            mapOf(
                "point" to
                    FixtureValue.Object(
                        listOf(
                            "radius" to FixtureValue.Number(RESULT_POINT_RADIUS),
                            "strokeWidth" to FixtureValue.Number(RESULT_POINT_STROKE_WIDTH),
                        ),
                    ),
                "slots" to
                    FixtureValue.Object(
                        GeometrySlot.entries.map { slot ->
                            slot.id to
                                FixtureValue.Object(
                                    listOf(
                                        "color" to FixtureValue.Text(slot.color),
                                        "rank" to FixtureValue.Number(slot.rank.toDouble()),
                                        "sourceId" to FixtureValue.Text(slot.sourceId),
                                    ),
                                )
                        },
                    ),
            ),
        )
        val input = case.fields
        val named = input.text("slot")
        val slot =
            GeometrySlot.entries.firstOrNull { it.id == named } ?: throw AssertionError("no slot is called `$named`")
        val paint = MapRunnerFixtures.paint(input["paint"])
        val color = input.textOrNull("color")
        val layers = geometrySlotLayers(slot, color ?: slot.color, paint)
        val map = MapEngine(MapRunnerFixtures.client(), paint = paint, geolocation = InertGeolocation())
        map.setGeometry(GeoJsonPoint(listOf(6.86, 45.83)), slot, color)
        if (map.state.value.layers !=
            layers
        ) {
            throw AssertionError("the map drew other layers: ${map.state.value.layers.map { it.id }}")
        }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "layers" to FixtureValue.Array(layers.map(MapRunnerFixtures::styleLayer)),
                    "rank" to FixtureValue.Number(slot.rank.toDouble()),
                    "sourceId" to FixtureValue.Text(slot.sourceId),
                ),
            ),
        )
    }
}
