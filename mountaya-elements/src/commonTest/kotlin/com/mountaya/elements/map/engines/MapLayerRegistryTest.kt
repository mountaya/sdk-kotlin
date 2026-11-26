package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.LayerRank
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapLayerRegistryTest {
    private fun layer(
        id: String,
        rank: LayerRank,
        source: String = "s",
    ): MapLayer = MapLayer(id = id, type = TileSchema.TileLayerType.Line, source = source, rank = rank.rank)

    @Test
    fun layersDrawByRankWhateverOrderTheyWereAddedInTwoOfOneRankTheLaterAbove() {
        val registry = MapLayerRegistry()
        registry.add(layer("pois", LayerRank.Poi))
        registry.add(layer("route", LayerRank.ItineraryLine))
        registry.add(layer("shade", LayerRank.Hillshade))
        registry.add(layer("detour", LayerRank.ItineraryLine))
        registry.add(layer("cursor", LayerRank.Chrome))

        assertEquals(listOf("shade", "route", "detour", "pois", "cursor"), registry.layers.map { it.id })
    }

    @Test
    fun aRankBetweenTwoBandsDrawsBetweenThem() {
        val registry = MapLayerRegistry()
        registry.add(layer("pois", LayerRank.Poi))
        registry.add(layer("waypoints", LayerRank.ItineraryPoint))
        registry.add(
            MapLayer(
                id = "mine",
                type = TileSchema.TileLayerType.Line,
                source = "s",
                rank =
                    LayerRank.Poi.rank + 2,
            ),
        )

        // 82 stands between the points of interest and the waypoints, a band of no layer of Mountaya's.
        assertEquals(listOf("pois", "mine", "waypoints"), registry.layers.map { it.id })
    }

    @Test
    fun aLayerPlacedAgainAtTheRankItHoldsKeepsItsPlaceAndTakesItsNewDefinition() {
        val registry = MapLayerRegistry()
        registry.add(layer("route", LayerRank.ItineraryLine))
        registry.add(layer("detour", LayerRank.ItineraryLine))
        registry.add(layer("route", LayerRank.ItineraryLine, source = "other"))

        assertEquals(listOf("route", "detour"), registry.layers.map { it.id })
        assertEquals("other", registry.layer("route")?.source)
    }

    @Test
    fun aLayerPlacedAgainAtAnotherRankMovesToTheTopOfItsNewBandAndBack() {
        val registry = MapLayerRegistry()
        registry.add(layer("a", LayerRank.Poi))
        registry.add(layer("b", LayerRank.Poi))
        registry.add(layer("c", LayerRank.ItineraryPoint))
        registry.add(layer("a", LayerRank.ItineraryPoint))
        assertEquals(listOf("b", "c", "a"), registry.layers.map { it.id })
        registry.add(layer("a", LayerRank.Poi))
        assertEquals(listOf("b", "a", "c"), registry.layers.map { it.id })
        registry.remove("a")
        registry.add(layer("a", LayerRank.Poi))
        assertEquals(listOf("b", "a", "c"), registry.layers.map { it.id }, "a layer removed takes the band's top")
        registry.remove("missing")
        assertEquals(listOf("b", "a", "c"), registry.layers.map { it.id })
    }

    @Test
    fun aLayerIsChangedInItsPlaceAndTakenOffByItsIdOrByItsSource() {
        val registry = MapLayerRegistry()
        registry.add(layer("route", LayerRank.ItineraryLine, source = "a"))
        registry.add(layer("detour", LayerRank.ItineraryLine, source = "b"))
        val changed =
            layer("route", LayerRank.ItineraryLine, source = "a").let {
                it.with(
                    mapOf("line-width" to JsonPrimitive(4)),
                    it.layout,
                )
            }

        assertTrue(registry.replace("route", changed))
        assertEquals(listOf("route", "detour"), registry.layers.map { it.id })
        assertEquals(mapOf("line-width" to JsonPrimitive(4)), registry.layer("route")?.paint)
        assertFalse(registry.replace("gone", changed))
        assertTrue(registry.remove("route"))
        assertFalse(registry.remove("route"))
        registry.removeLayers("b")
        assertTrue(registry.layers.isEmpty())
    }

    @Test
    fun anIdIsMatchedAsWritten() {
        val registry = MapLayerRegistry()
        registry.add(layer("café", LayerRank.Poi))

        assertNull(registry.layer("café"))
        assertNotNull(registry.layer("café"), "the registry holds the layer")
    }

    @Test
    fun theLayersAreHeldInTheOrderTheyWerePlacedOnePlacedAgainAtItsRankKeepingItsPlace() {
        val registry = MapLayerRegistry()
        registry.add(layer("itinerary-point", LayerRank.ItineraryPoint))
        registry.add(layer("poi-food", LayerRank.Poi))
        registry.add(layer("itinerary-line", LayerRank.ItineraryLine))
        registry.add(layer("poi-water", LayerRank.Poi))

        assertEquals(
            listOf("itinerary-point", "poi-food", "itinerary-line", "poi-water"),
            registry.placement.map { it.id },
        )
        assertEquals(
            listOf("itinerary-line", "poi-food", "poi-water", "itinerary-point"),
            registry.layers.map { it.id },
        )
        registry.add(layer("poi-food", LayerRank.Poi))
        assertEquals(
            listOf("itinerary-point", "poi-food", "itinerary-line", "poi-water"),
            registry.placement.map { it.id },
        )
        registry.add(layer("poi-food", LayerRank.Zone))
        assertEquals(
            listOf("itinerary-point", "itinerary-line", "poi-water", "poi-food"),
            registry.placement.map { it.id },
        )
        assertEquals(PlacedLayer("poi-food", 40), registry.placement.last())
        registry.remove("itinerary-line")
        assertEquals(listOf("itinerary-point", "poi-water", "poi-food"), registry.placement.map { it.id })
        registry.removeLayers("s")
        assertTrue(registry.placement.isEmpty())
    }
}
