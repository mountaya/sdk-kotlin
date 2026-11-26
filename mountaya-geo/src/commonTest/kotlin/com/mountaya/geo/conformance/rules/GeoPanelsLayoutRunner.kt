package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.generated.ElementLayout
import kotlinx.coroutines.test.TestScope

/**
 * `geo.panels.layout`: how a panel lays out over a map of the case's `mapWidth` and `mapHeight`, its top `topInset`
 * below the map's. Every measure the fixture declares is held to the tokens table's `layout`, as `ElementLayout` prints
 * it, and the columns to the ones `panelLayoutOf` lays the cards in.
 */
object GeoPanelsLayoutRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val layout = panelLayoutOf(input.number("mapWidth"), input.number("mapHeight"), input.number("topInset"))
        holdConstantsExactly(
            case,
            mapOf(
                "layerCardColumns" to FixtureValue.Number(layout.layerCardColumns.toDouble()),
                "narrowBottomInset" to FixtureValue.Number(ElementLayout.panelNarrowBottomInset),
                "narrowMapWidth" to FixtureValue.Number(ElementLayout.panelNarrowMapWidth),
                "pictureHeight" to FixtureValue.Number(ElementLayout.panelPictureHeight),
                "pictureHeightNarrow" to FixtureValue.Number(ElementLayout.panelPictureHeightNarrow),
                "preferenceCardColumns" to FixtureValue.Number(layout.preferenceCardColumns.toDouble()),
                "width" to FixtureValue.Number(ElementLayout.panelWidth),
            ),
        )
        return ConformanceOutcome.Value(
            profileObject(
                "fill" to FixtureValue.Bool(layout.fill),
                "maxHeight" to FixtureValue.Number(layout.maxHeight),
                "pictureHeight" to FixtureValue.Number(layout.pictureHeight),
                "width" to profileNumber(layout.width),
            ),
        )
    }
}
