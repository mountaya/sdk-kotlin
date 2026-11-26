package com.mountaya.conformance

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.elements.GeometrySlot
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.LegEmphasisTarget
import com.mountaya.elements.MapEngine
import com.mountaya.elements.writeLegEmphasis
import com.mountaya.geo.lineBetween
import kotlinx.coroutines.test.TestScope

/**
 * `map.profile.emphasis`: plays the case's steps over a real map. Each element lights the stretch of the case's
 * `series` between the leg's ends, `lineBetween`, through `writeLegEmphasis`, holding whether it lit the slot, and
 * a step that points at none, or an element going, writes none. Each write is recorded as the element made it, and
 * the slot is read off the map at the end, its colour null where it is the slot's own, which an empty slot reads
 * first.
 */
object MapProfileEmphasisRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val series = fixtureSeries(input["series"])
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val own = map.geometry(GeometrySlot.Emphasis).color
        val recorder = RecordedEmphasis(map)
        val lit = HashMap<String, Boolean>()
        for (item in input.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            val name = step.text("element")
            recorder.element = name
            var stretch: GeoJsonGeometry? = null
            var color: String? = null
            when (val kind = step.text("step")) {
                "point" -> {
                    stretch = lineBetween(series.points, step.number("from"), step.number("to"))
                    color = step.textOrNull("color")
                }

                "leave", "unmount" -> {
                    stretch = null
                }

                else -> {
                    throw AssertionError("`$kind` is no step")
                }
            }
            lit[name] = writeLegEmphasis(recorder, lit[name] ?: false, stretch, color)
        }
        val held = map.geometry(GeometrySlot.Emphasis)
        val slot =
            held.geometry?.let { geometry ->
                objectValue(
                    "color" to (if (held.color == own) FixtureValue.Null else FixtureValue.Text(held.color)),
                    "geometry" to writtenGeometry(geometry),
                )
            } ?: FixtureValue.Null
        return ConformanceOutcome.Value(objectValue("slot" to slot, "writes" to FixtureValue.Array(recorder.writes)))
    }

    /** A map that hands each emphasis write to a real engine and records it, under the element writing it. */
    private class RecordedEmphasis(
        val map: MapEngine,
    ) : LegEmphasisTarget {
        var element = ""
        val writes = ArrayList<FixtureValue>()

        override fun setGeometry(
            geometry: GeoJsonGeometry?,
            slot: GeometrySlot,
            color: String?,
        ) {
            map.setGeometry(geometry, slot, color)
            writes +=
                objectValue(
                    "color" to textOrNull(color),
                    "element" to FixtureValue.Text(element),
                    "geometry" to (geometry?.let(::writtenGeometry) ?: FixtureValue.Null),
                    "write" to FixtureValue.Text("set"),
                )
        }

        override fun clearGeometry(slot: GeometrySlot) {
            map.clearGeometry(slot)
            writes += objectValue("element" to FixtureValue.Text(element), "write" to FixtureValue.Text("clear"))
        }
    }
}
