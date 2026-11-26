package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class PickTargetTest {
    private val box = listOf(6.8, 45.8, 6.9, 45.9)

    @Test
    fun aPlaceIsFramedOnItsExtentElseFlownToAtThePointZoomAndMarkedAtItsPoint() {
        assertEquals(13.0, POINT_ZOOM)
        assertEquals(
            SearchPick(SearchPickCamera.Fit(box), SearchHighlight.Place(listOf(6.86, 45.83))),
            pickTarget(SearchRowPayload.Place(listOf(6.86, 45.83), box)),
        )
        assertEquals(
            SearchPick(SearchPickCamera.Fly(listOf(6.86, 45.83), 13.0), SearchHighlight.Place(listOf(6.86, 45.83))),
            pickTarget(SearchRowPayload.Place(listOf(6.86, 45.83), null)),
        )
        assertEquals(
            SearchPickCamera.Fit(listOf(179.5, -17.0, -179.5, -16.0)),
            pickTarget(SearchRowPayload.Place(listOf(179.9, -16.5), listOf(179.5, -17.0, -179.5, -16.0))).camera,
            "an extent across the antimeridian as written",
        )
    }

    @Test
    fun aPointOfInterestIsFlownToAndMarkedAsItsCategoryOrAsNone() {
        assertEquals(
            SearchPick(
                SearchPickCamera.Fly(listOf(6.85, 45.84), 13.0),
                SearchHighlight.Poi(listOf(6.85, 45.84), DataSchema.POICategory.Hut),
            ),
            pickTarget(SearchRowPayload.Poi(listOf(6.85, 45.84), DataSchema.POICategory.Hut)),
        )
        assertEquals(
            SearchHighlight.Poi(listOf(6.85, 45.84), null),
            pickTarget(SearchRowPayload.Poi(listOf(6.85, 45.84), null)).mark,
        )
    }

    @Test
    fun aWayIsFramedOnItsExtentElseFlownToItsAnchorAndMarkedAsItself() {
        val line = listOf(listOf(6.8, 45.8), listOf(6.86, 45.83), listOf(6.9, 45.9))
        assertEquals(
            SearchPick(SearchPickCamera.Fit(box), SearchHighlight.Way(listOf(6.86, 45.83), box, line)),
            pickTarget(SearchRowPayload.Way(listOf(6.86, 45.83), box, line)),
        )
        assertEquals(
            SearchPick(
                SearchPickCamera.Fly(listOf(6.86, 45.83), 13.0),
                SearchHighlight.Way(listOf(6.86, 45.83), null, null),
            ),
            pickTarget(SearchRowPayload.Way(listOf(6.86, 45.83), null, null)),
        )
    }

    @Test
    fun anItineraryIsMarkedAtItsStartAndAZoneAtItsPointEachAsAPlace() {
        assertEquals(
            SearchPick(SearchPickCamera.Fit(box), SearchHighlight.Place(listOf(6.82, 45.82))),
            pickTarget(SearchRowPayload.Itinerary("it-1", box, listOf(6.82, 45.82))),
        )
        assertEquals(
            SearchPick(SearchPickCamera.Fit(box), null),
            pickTarget(SearchRowPayload.Itinerary("it-1", box, null)),
        )
        assertEquals(
            SearchPick(SearchPickCamera.Fly(listOf(6.82, 45.82), 13.0), SearchHighlight.Place(listOf(6.82, 45.82))),
            pickTarget(SearchRowPayload.Itinerary("it-1", null, listOf(6.82, 45.82))),
        )
        assertEquals(SearchPick(null, null), pickTarget(SearchRowPayload.Itinerary("it-1", null, null)))
        assertEquals(
            SearchPick(SearchPickCamera.Fly(listOf(6.88, 45.88), 13.0), SearchHighlight.Place(listOf(6.88, 45.88))),
            pickTarget(SearchRowPayload.Zone("zone-1", listOf(6.88, 45.88), null)),
        )
        assertEquals(
            SearchPick(SearchPickCamera.Fit(box), SearchHighlight.Place(listOf(6.88, 45.88))),
            pickTarget(SearchRowPayload.Zone("zone-1", listOf(6.88, 45.88), box)),
        )
    }

    @Test
    fun aCollectionMovesNothingAndMarksNothing() {
        assertEquals(
            SearchPick(null, null),
            pickTarget(SearchRowPayload.Collection("col-1", "Alpine", null, false)),
        )
    }
}
