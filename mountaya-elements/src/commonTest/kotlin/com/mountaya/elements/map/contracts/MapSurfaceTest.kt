package com.mountaya.elements

import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.ZoomBounds
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapSurfaceTest {
    /** A surface drawing a base map of its own: sources it credits, labels it relabels and restores, every layer and source live. */
    private class Base : MapSurface {
        override val zoomLimits: ZoomBounds = ZoomBounds(0.0, 25.5)
        override val projection: ScreenProjection = ScreenProjection(point = { it }, position = { it })
        override var baseSources: List<CreditedSource> = emptyList()
        override var symbolLabels: List<SymbolLabel> = emptyList()

        /** Each label the engine set, by layer, null for one put back as its style wrote it. */
        val labels = mutableMapOf<String, JsonElement?>()

        override fun move(
            target: CameraTarget,
            motion: CameraMotion,
        ) = Unit

        override fun frame(
            bounds: List<Double>,
            fit: CameraFit,
            window: ZoomBounds,
        ): CameraPose? = null

        override fun holdsLayer(id: String): Boolean = true

        override fun holdsSource(id: String): Boolean = true

        override fun renderedFeatures(
            point: List<Double>,
            layers: List<String>,
        ): List<QueriedFeature> = emptyList()

        override fun relabel(
            id: String,
            label: JsonElement?,
        ) {
            labels[id] = label
        }
    }

    private fun engine(): MapEngine =
        MapEngine(
            Mountaya(Credential.Publishable("pk_test_a")),
            labelLanguage = RestSchema.Language.En,
            geolocation = InertGeolocation(),
        )

    @Test
    fun theBaseMapsSourcesASurfaceReadsAreCreditedAfterMountayaTheMapsOwnAfter() {
        val map = engine()
        val base = Base()
        map.attach(base)
        base.baseSources = listOf(CreditedSource("Base", isDrawn = true), CreditedSource("Hidden", isDrawn = false))
        map.addSource(
            MapSource("own", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList())), attribution = "Own"),
        )
        map.addLayer(MapLayer("own-lines", TileSchema.TileLayerType.Line, "own", rank = 50))
        assertEquals(listOf(MOUNTAYA_CREDIT, "Base", "Own"), map.credits)

        // A layer drawn outside its zoom window, or hidden, draws its source nowhere.
        map.editLayer("own-lines", layout = mapOf("visibility" to JsonPrimitive("none")))
        assertEquals(listOf(MOUNTAYA_CREDIT, "Base"), map.credits)
        map.editLayer("own-lines", layout = mapOf("visibility" to JsonPrimitive("visible")))
        map.addLayer(
            MapLayer(
                "own-lines",
                TileSchema.TileLayerType.Line,
                "own",
                rank = 50,
                zoom = ZoomBounds(10.0, 14.0),
            ),
        )
        assertEquals(listOf(MOUNTAYA_CREDIT, "Base"), map.credits, "the camera stands at 0, out of the window")
    }

    @Test
    fun aLabelRewrittenIsPutBackAsWrittenWhereTheLanguageLeavesItAsWritten() {
        val map = engine()
        val base = Base()
        map.attach(base)
        val written =
            JsonArray(
                listOf(
                    JsonPrimitive("coalesce"),
                    JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("name:de"))),
                    JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("name"))),
                ),
            )
        base.symbolLabels = listOf(SymbolLabel("place-label", written))

        map.setLabelLanguage(RestSchema.Language.Fr)
        val french = base.labels["place-label"]
        assertNotNull(french, "the base map labels in French")
        assertNotEquals<JsonElement>(written, french)

        map.setLabelLanguage(RestSchema.Language.De)
        assertTrue(base.labels.containsKey("place-label"))
        assertNull(base.labels["place-label"], "back to the label its style wrote")
    }

    @Test
    fun aSurfaceLetGoIsNoLongerAskedAndTheMapAnswersAsOneNoViewDraws() {
        val map = engine()
        val base = Base()
        map.attach(base)
        assertEquals(listOf(6.86, 45.83), map.project(listOf(6.86, 45.83)))

        map.detach(Base())
        assertEquals(listOf(6.86, 45.83), map.project(listOf(6.86, 45.83)), "another surface lets go of nothing")
        map.detach(base)
        assertTrue(map.project(listOf(6.86, 45.83)).all { it.isNaN() })
        assertTrue(map.featuresAt(listOf(0.0, 0.0)).isEmpty())
    }
}
