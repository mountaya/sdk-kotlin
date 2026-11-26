package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

/** The ids a style's GeoJSON sources write inline, which a renderer reads a feature's id back as. */
class WrittenFeatureIdsTest {
    /**
     * A number a GeoJSON source gives is a number, keyed by the digits MapLibre hands it back as, and a text stays text,
     * one that reads as a number included.
     */
    @Test
    fun aGeoJsonSourcesIdIsReadAsItsDocumentWritesIt() {
        val style =
            """
            {"version": 8, "sources": {
              "huts": {"type": "geojson", "data": {"type": "FeatureCollection", "features": [
                {"type": "Feature", "id": 5, "geometry": null, "properties": {}},
                {"type": "Feature", "id": "6", "geometry": null, "properties": {}},
                {"type": "Feature", "id": 7.5, "geometry": null, "properties": {}},
                {"type": "Feature", "geometry": null, "properties": {}}
              ]}},
              "summit": {"type": "geojson", "data": {"type": "Feature", "id": "top", "geometry": null, "properties": {}}},
              "remote": {"type": "geojson", "data": "https://tiles.example.test/huts.geojson"},
              "relief": {"type": "vector", "url": "https://tiles.example.test/relief.json"}
            }}
            """.trimIndent()

        assertEquals(
            mapOf(
                "huts" to
                    mapOf(
                        "5" to QueriedFeature.Id.Number(5.0),
                        "6" to QueriedFeature.Id.Text("6"),
                        "7.5" to QueriedFeature.Id.Number(7.5),
                    ),
                "summit" to mapOf("top" to QueriedFeature.Id.Text("top")),
            ),
            writtenFeatureIds(style),
        )
    }

    @Test
    fun aDocumentThatDoesNotParseOrHoldsNoSourceHoldsNoId() {
        assertEquals(emptyMap(), writtenFeatureIds("{"))
        assertEquals(emptyMap(), writtenFeatureIds("""{"version": 8}"""))
    }
}
