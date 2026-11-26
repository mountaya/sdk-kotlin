package com.mountaya.conformance

import com.mountaya.geo.AspectCardinal
import com.mountaya.geo.aspectCardinal
import kotlinx.coroutines.test.TestScope

/**
 * `geo.terrain.aspect-cardinal`: the compass direction a bearing of the case's `degrees` faces, by its code, or null.
 * The directions are held to `AspectCardinal`'s codes in order, and the sector to the step between two headings.
 */
object GeoTerrainAspectCardinalRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val directions = AspectCardinal.entries
        holdDeclaredConstants(
            case,
            rules = mapOf("sectorDegrees" to directions[1].heading - directions[0].heading),
            values = mapOf("cardinals" to textArray(directions.map { it.code })),
        )
        val direction = aspectCardinal(case.fields.number("degrees"))
        return ConformanceOutcome.Value(direction?.let { FixtureValue.Text(it.code) } ?: FixtureValue.Null)
    }
}
