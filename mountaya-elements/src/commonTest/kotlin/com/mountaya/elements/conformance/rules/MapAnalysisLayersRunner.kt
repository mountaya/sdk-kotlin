package com.mountaya.elements

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.FEATURE_COLOR_PROPERTY
import com.mountaya.geo.FEATURE_KIND_PROPERTY
import com.mountaya.geo.FEATURE_WIDTH_PROPERTY
import com.mountaya.geo.MapPaint
import kotlinx.coroutines.test.TestScope

/**
 * `map.analysis.layers`: the layers a Data API result of the case's kind draws through, from the source
 * `sourceId`, at its kind's rank, through the internal rule the engine applies, the map's paint the shipped one
 * with the case's patch folded on as a consumer restyles a copy. The palette, the properties a feature is
 * stamped with and each kind's rank are held to the rule's own.
 */
object MapAnalysisLayersRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        fun number(value: Double) = FixtureValue.Number(value)

        fun pair(vararg members: Pair<String, FixtureValue>) = FixtureValue.Object(members.toList())
        MapRunnerFixtures.holdConstants(
            case,
            mapOf(
                "palette" to
                    pair(
                        "directionsLine" to FixtureValue.Text(DIRECTIONS_LINE_COLOR),
                        "isochroneFillOpacity" to number(ISOCHRONE_FILL_OPACITY),
                        "isochroneOutline" to
                            pair(
                                "color" to FixtureValue.Text(ISOCHRONE_OUTLINE_COLOR),
                                "width" to number(ISOCHRONE_OUTLINE_WIDTH),
                            ),
                        "matrixLine" to
                            pair(
                                "color" to FixtureValue.Text(MATRIX_LINE_COLOR),
                                "opacity" to number(MATRIX_LINE_OPACITY),
                            ),
                        "resultPoint" to
                            pair(
                                "radius" to number(RESULT_POINT_RADIUS),
                                "strokeColor" to FixtureValue.Text(RESULT_POINT_STROKE_COLOR),
                                "strokeWidth" to number(RESULT_POINT_STROKE_WIDTH),
                            ),
                    ),
                "properties" to
                    pair(
                        "color" to FixtureValue.Text(FEATURE_COLOR_PROPERTY),
                        "kind" to FixtureValue.Text(FEATURE_KIND_PROPERTY),
                        "width" to FixtureValue.Text(FEATURE_WIDTH_PROPERTY),
                    ),
                "ranks" to
                    FixtureValue.Object(
                        AnalysisKind.entries.map { kind ->
                            kind.id to
                                number(
                                    (analysisLayers(kind, "s", MapPaint.standard).firstOrNull()?.rank ?: -1).toDouble(),
                                )
                        },
                    ),
            ),
        )
        val input = case.fields
        val named = input.text("kind")
        val kind =
            AnalysisKind.entries.firstOrNull { it.id == named } ?: throw AssertionError("no kind is called `$named`")
        val layers = analysisLayers(kind, input.text("sourceId"), MapRunnerFixtures.paint(input["paint"]))
        val rank =
            layers.map { it.rank }.distinct().singleOrNull()
                ?: throw AssertionError("the kind's layers draw at more than one rank")
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "layers" to FixtureValue.Array(layers.map(MapRunnerFixtures::styleLayer)),
                    "rank" to number(rank.toDouble()),
                ),
            ),
        )
    }
}
