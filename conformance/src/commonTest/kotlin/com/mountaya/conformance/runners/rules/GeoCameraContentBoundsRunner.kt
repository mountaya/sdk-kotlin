package com.mountaya.conformance

import com.mountaya.geo.itineraryBounds
import com.mountaya.geo.specificationBounds
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.content-bounds`: the box a drawing call frames the case's content on. An `itinerary` is
 * framed on `itineraryBounds` of its `segments`, each built as the REST API's segment
 * from the `position` and the `geojson` the fixture writes, a placeholder in every other member; its
 * `waypoints` are no segment's and are handed to nothing. A `collection` is framed on `specificationBounds`
 * of its `bbox`, null where it states none or leaves it out.
 */
object GeoCameraContentBoundsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val itinerary = input.objectOrNull("itinerary")
        val box =
            if (itinerary != null) {
                val segments =
                    itinerary.items("segments").map {
                        fixtureSegment(it as? FixtureValue.Object ?: throw AssertionError("a segment is not an object"))
                    }
                itineraryBounds(segments)
            } else {
                val collection = input.objectOrNull("collection") ?: throw AssertionError("the input states no content")
                val bbox =
                    collection["bbox"]?.takeUnless { it is FixtureValue.Null }?.let { written ->
                        (written as? FixtureValue.Array)?.items?.map {
                            (it as? FixtureValue.Number)?.value
                                ?: throw AssertionError("`bbox` holds a value that is no number")
                        } ?: throw AssertionError("`bbox` is not an array")
                    }
                specificationBounds(bbox)
            }
        return ConformanceOutcome.Value(
            box?.let { edges ->
                FixtureValue.Array(
                    edges.map {
                        FixtureValue.Number(it)
                    },
                )
            } ?: FixtureValue.Null,
        )
    }
}
