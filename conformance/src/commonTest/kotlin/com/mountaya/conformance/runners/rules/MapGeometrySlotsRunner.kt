package com.mountaya.conformance

import com.mountaya.elements.GeometrySlot
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapSource
import com.mountaya.elements.SlotGeometry
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.JsonPrimitive

/**
 * `map.geometry.slots`: what each geometry slot holds after each step, through the engine: `set` is
 * `setGeometry`, `edit` `editGeometry` and `clear` `clearGeometry`. The answer is `geometry` of each slot, which
 * the runner holds to what the state draws for it: the slot's source holding its geometry, and its line in its
 * colour, or nothing where it holds none.
 */
object MapGeometrySlotsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val answers = mutableListOf<FixtureValue>()
        for (item in case.fields.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            val named = step.text("slot")
            val slot =
                GeometrySlot.entries.firstOrNull { it.id == named }
                    ?: throw AssertionError("no slot is called `$named`")
            val geometry = step["geometry"]?.takeUnless { it is FixtureValue.Null }?.let(::fixtureGeometry)
            val color = step.textOrNull("color")
            when (val name = step.text("step")) {
                "set" -> map.setGeometry(geometry, slot, color)
                "edit" -> map.editGeometry(slot, geometry, color)
                "clear" -> map.clearGeometry(slot)
                else -> throw AssertionError("no step is called `$name`")
            }
            answers +=
                FixtureValue.Object(
                    GeometrySlot.entries.sortedBy { it.id }.map { each ->
                        val holds = map.geometry(each)
                        holdDrawn(holds, each, map)
                        each.id to
                            MapFixtures.objectOf(
                                "color" to FixtureValue.Text(holds.color),
                                "geometry" to (holds.geometry?.let(::writtenGeometry) ?: FixtureValue.Null),
                            )
                    },
                )
        }
        return ConformanceOutcome.Value(FixtureValue.Array(answers))
    }

    /**
     * Holds what the state draws for a geometry slot to what it holds: its source's one feature its geometry and
     * its line its colour, or neither where it holds no geometry.
     */
    private fun holdDrawn(
        held: SlotGeometry,
        slot: GeometrySlot,
        map: MapEngine,
    ) {
        val source =
            map.state.value.sources
                .firstOrNull { it.id == "mountaya-${slot.id}" }
        val line =
            map.state.value.layers
                .firstOrNull { it.id == "mountaya-${slot.id}-line" }
        val geometry = held.geometry
        if (geometry == null) {
            if (source != null || line != null) throw AssertionError("the ${slot.id} holds no geometry and draws one")
            return
        }
        val drawn = (source?.kind as? MapSource.Kind.GeoJson)?.features?.features
        if (drawn?.size != 1 || drawn.first().geometry != geometry ||
            line?.paint?.get("line-color") != JsonPrimitive(held.color)
        ) {
            throw AssertionError("the ${slot.id} draws other than it holds")
        }
    }
}
