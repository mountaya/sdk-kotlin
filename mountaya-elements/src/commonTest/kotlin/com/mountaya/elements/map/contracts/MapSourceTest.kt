package com.mountaya.elements

import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.GeoJsonFeature
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.ZoomBounds
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class MapSourceTest {
    private val tiles = listOf("https://tiles.mountaya.com/v1/zones/{z}/{x}/{y}")
    private val zoom = ZoomBounds(2.0, 14.0)

    private fun tileSource(
        type: TileSchema.TileSourceType,
        size: TileSchema.TileSourceSize? = null,
    ): TileSchema.TileSource =
        TileSchema.TileSource(
            id = "zones",
            layers = emptyList(),
            tiles = tiles,
            type = type,
            zoom_max = 14,
            zoom_min = 2,
        ) {
            if (size != null) this.size = size
            attribution = "Mountaya"
        }

    @Test
    fun aTileSpecificationsSourceIsReadAsTheTilesItServesAtTheirZoomLevels() {
        val source = MapSource(tileSource(TileSchema.TileSourceType.Vector))

        assertEquals("zones", source.id)
        assertEquals("Mountaya", source.attribution)
        assertEquals(MapSource.Kind.Vector(tiles, zoom), source.kind)
    }

    @Test
    fun aRasterAndAnElevationRasterKeepTheirSizeTheRenderersOwnWhereNoneIsStated() {
        assertEquals(
            MapSource.Kind.Raster(tiles, 256, zoom),
            MapSource(
                tileSource(TileSchema.TileSourceType.Raster, TileSchema.TileSourceSize.Size256),
            ).kind,
        )
        assertEquals(
            MapSource.Kind.RasterDem(tiles, null, zoom),
            MapSource(tileSource(TileSchema.TileSourceType.RasterDem)).kind,
        )
        assertEquals(
            MapSource.Kind.RasterDem(tiles, 512, zoom),
            MapSource(
                tileSource(
                    TileSchema.TileSourceType.RasterDem,
                    TileSchema.TileSourceSize.Size512,
                ),
            ).kind,
        )
    }

    @Test
    fun aSourceOfATypeTheContractDoesNotDeclareIsRefusedNamingTheType() {
        for (type in listOf("pmtiles", "geojson", "Vector", "")) {
            assertEquals(
                MapException(MapException.Kind.TypeRefused, type),
                assertFailsWith<MapException> { MapSource(tileSource(TileSchema.TileSourceType(type))) },
            )
        }
    }

    @Test
    fun twoSourcesAreEqualWhereTheyServeTheSameEachTemplateAsWritten() {
        val features =
            GeoJsonFeatureCollection(listOf(GeoJsonFeature(GeoJsonPoint(listOf(6.86, 45.83)), JsonObject(emptyMap()))))
        val source = MapSource("pins", MapSource.Kind.GeoJson(features))

        assertEquals(MapSource("pins", MapSource.Kind.GeoJson(features)), source)
        assertNotEquals(MapSource("pins", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))), source)
        assertNotEquals(
            MapSource("a", MapSource.Kind.Vector(listOf("café"), ZoomBounds(0.0, 1.0))),
            MapSource("a", MapSource.Kind.Vector(listOf("café"), ZoomBounds(0.0, 1.0))),
        )
        assertNotEquals<MapSource.Kind>(
            MapSource.Kind.Raster(tiles, 512, zoom),
            MapSource.Kind.RasterDem(tiles, 512, zoom),
        )
    }
}
