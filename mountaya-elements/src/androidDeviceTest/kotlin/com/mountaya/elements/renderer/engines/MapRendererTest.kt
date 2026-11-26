package com.mountaya.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon
import com.mountaya.client.Mountaya
import com.mountaya.client.Url
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.MapWindow.onMain
import com.mountaya.elements.MapWindow.showing
import com.mountaya.elements.MapWindow.until
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeature
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.LayerRank
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.PixelPadding
import com.mountaya.geo.SearchHighlight
import com.mountaya.geo.ZoomBounds
import com.mountaya.geo.cameraForBounds
import com.mountaya.geo.watermarkHeight
import com.mountaya.geo.watermarkOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import org.junit.Rule
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration

/** The renderer on MapLibre's own map, on the device it draws on, every request answered by the stub. */
class MapRendererTest {
    @get:Rule
    val compose = createComposeRule()

    private fun engine(
        labelLanguage: RestSchema.Language? = null,
        glyphs: GlyphSet = GlyphSet.marker,
    ): MapEngine =
        MapEngine(
            Mountaya(
                Credential.Publishable("pk_test_a"),
                language = "fr",
                baseUrls = BaseUrls(tiles = StubTileApi.tiles),
            ),
            style = StubTileApi.styleUrl,
            camera = CameraPose(listOf(6.86, 45.83), 10.0, 0.0, 0.0),
            labelLanguage = labelLanguage,
            glyphs = glyphs,
        )

    private val pins =
        GeoJsonFeatureCollection(
            listOf(
                GeoJsonFeature(
                    GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))),
                    JsonObject(mapOf("name" to JsonPrimitive("Sentier"))),
                ),
            ),
        )

    /** Every layer MapLibre's map draws, bottom to top. */
    private fun MapRenderer.drawnLayers(): List<String> =
        map
            ?.style
            ?.layers
            ?.map { it.id }
            .orEmpty()

    /**
     * The map's own layers as MapLibre draws them, bottom to top, where they are the topmost of the
     * style, above the base map's, and nothing where they are not.
     */
    private fun MapRenderer.ownLayers(): List<String> {
        val all = drawnLayers()
        val own = all.filter { !it.startsWith("base-") && !it.startsWith("org.maplibre.") }
        return if (all.takeLast(own.size) == own) own else emptyList()
    }

    /**
     * A symbol layer's label as MapLibre reads it back, as JSON. A label that is an expression is held as
     * MapLibre's JSON array, which is no `Formatted`, so the value is read as it is held, never cast.
     */
    private fun SymbolLayer.label(): String {
        val label: PropertyValue<*> = textField
        return label.value.toString()
    }

    /**
     * The floor MapLibre holds where the one it is handed is 0: the zoom at which the world, 512 pixels a
     * side at zoom 0, fills the height of MapLibre's frame, the view's height rounded up to whole
     * density-independent pixels, below which MapLibre Native's camera never goes.
     */
    private fun MapRenderer.worldFillsTheFrame(): Double {
        val density = mapView.resources.displayMetrics.density
        return log2(ceil((mapView.height.toFloat() / density).toDouble()) / 512.0)
    }

    @Test
    fun theMapDrawsItsStatesStyleAndHandsMapLibresMapOverOnceItIsLoaded() {
        val renderer = compose.showing(engine())

        compose.onMain {
            val style = assertNotNull(renderer.map?.style, "the map holds its style")
            assertNotNull(style.getLayer("base-background"), "the style holds its background")
            assertTrue(style.getLayer("base-water") is FillLayer)
            val camera = assertNotNull(renderer.map, "the map is ready").cameraPosition
            assertTrue(abs(camera.zoom - 10) < 0.01, "${camera.zoom}")
            val target = assertNotNull(camera.target, "the camera has a target")
            assertTrue(abs(target.longitude - 6.86) < 0.001, "$target")
        }
    }

    @Test
    fun layersAreDrawnAboveTheBaseMapInTheRegistrysOrderWhateverOrderTheyCameIn() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain {
            map.addSource(MapSource("trail", MapSource.Kind.GeoJson(pins)))
            map.addLayer(
                MapLayer(
                    id = "cursor",
                    type = TileSchema.TileLayerType.Circle,
                    source = "trail",
                    rank = LayerRank.Chrome.rank,
                ),
            )
            map.addLayer(
                MapLayer(
                    id = "route",
                    type = TileSchema.TileLayerType.Line,
                    source = "trail",
                    rank = LayerRank.ItineraryLine.rank,
                    paint = mapOf("line-color" to JsonPrimitive("#e64980"), "line-width" to JsonPrimitive(4)),
                ),
            )
            map.addLayer(
                MapLayer(
                    id = "zone",
                    type = TileSchema.TileLayerType.Fill,
                    source = "trail",
                    rank = LayerRank.Zone.rank,
                ),
            )
        }

        compose.until { renderer.ownLayers() == listOf("zone", "route", "cursor") }
        compose.onMain {
            assertEquals(
                listOf("base-background", "base-elsewhere", "base-water", "base-labels"),
                renderer.drawnLayers().take(4),
            )
            map.addLayer(
                MapLayer(
                    id = "shade",
                    type = TileSchema.TileLayerType.Line,
                    source = "trail",
                    rank = LayerRank.Hillshade.rank,
                ),
            )
        }
        compose.until { renderer.ownLayers() == listOf("shade", "zone", "route", "cursor") }
        compose.onMain { map.removeLayer("route") }
        compose.until { renderer.ownLayers() == listOf("shade", "zone", "cursor") }
    }

    /**
     * MapLibre Native's tile sources take their addresses as they are built, so a source whose addresses a pin moved is
     * given MapLibre anew, its layers made again in their places, and no other source is sent (`map.overlays.instant`).
     */
    @Test
    fun aSourceWhoseAddressesAPinMovedIsGivenMapLibreAnewItsLayersInTheirPlaces() {
        val map = engine()
        val renderer = compose.showing(map)

        fun snow(stamp: String): MapSource =
            MapSource(
                "snow",
                MapSource.Kind.Vector(
                    listOf("https://tiles.example.test/v1/snow/{z}/{x}/{y}$stamp"),
                    ZoomBounds(0.0, 14.0),
                ),
            )
        compose.onMain {
            map.addSource(snow(""))
            map.addLayer(
                MapLayer(
                    id = "snow-fills",
                    type = TileSchema.TileLayerType.Fill,
                    source = "snow",
                    sourceLayer = "snow",
                    rank = LayerRank.Overlay.rank,
                ),
            )
            map.addSource(MapSource("trail", MapSource.Kind.GeoJson(pins)))
            map.addLayer(
                MapLayer(
                    id = "route",
                    type = TileSchema.TileLayerType.Line,
                    source = "trail",
                    rank = LayerRank.ItineraryLine.rank,
                ),
            )
        }
        compose.until { renderer.ownLayers() == listOf("snow-fills", "route") }
        val (unstamped, trail) =
            compose.onMain { renderer.map?.style?.getSource("snow") to renderer.map?.style?.getSource("trail") }

        compose.onMain { map.addSource(snow("?timestamp=2026-03-14T09%3A00%3A00.000Z")) }
        compose.until {
            renderer.map
                ?.style
                ?.getSource("snow")
                .let { it != null && it !== unstamped }
        }

        compose.onMain {
            assertEquals(listOf("snow-fills", "route"), renderer.ownLayers())
            assertSame(trail, renderer.map?.style?.getSource("trail"), "no other source is given MapLibre anew")
        }
    }

    @Test
    fun aPatchTheEngineMergesIsThePropertyTheMapDrawsJsonNullBackToTheDefault() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain {
            map.addSource(MapSource("trail", MapSource.Kind.GeoJson(pins)))
            map.addLayer(
                MapLayer(
                    id = "route",
                    type = TileSchema.TileLayerType.Line,
                    source = "trail",
                    rank = LayerRank.ItineraryLine.rank,
                    paint =
                        mapOf(
                            "line-color" to JsonPrimitive("#e64980"),
                            "line-width" to JsonPrimitive(4),
                            "line-opacity" to JsonPrimitive(0.5),
                        ),
                    layout = mapOf("line-cap" to JsonPrimitive("round")),
                ),
            )
        }
        compose.until { renderer.map?.style?.getLayer("route") != null }
        val drawn =
            compose.onMain {
                assertNotNull(renderer.map?.style?.getLayer("route") as? LineLayer, "the route is a line layer")
            }
        compose.onMain {
            assertEquals(4f, drawn.lineWidth.value)
            assertEquals(Color.parseColor("#e64980"), drawn.lineColorAsInt)
            map.editLayer(
                "route",
                paint = mapOf("line-width" to JsonPrimitive(8), "line-opacity" to null),
                layout = mapOf("visibility" to JsonPrimitive("none")),
            )
        }
        compose.until { drawn.lineWidth.value == 8f }
        compose.onMain {
            assertEquals(0.5f, drawn.lineOpacity.value)
            assertEquals("none", drawn.visibility.value)
            map.editLayer("route", paint = mapOf("line-width" to JsonNull))
        }
        // A property set back to the style's default holds no value of its own, which MapLibre draws as its default.
        compose.until { drawn.lineWidth.isNull }
    }

    @Test
    fun theBaseMapsLabelsSpeakTheStatesLanguageAndChangeWithIt() {
        val map = engine()
        val renderer = compose.showing(map)
        val labels =
            compose.onMain {
                assertNotNull(renderer.map?.style?.getLayer("base-labels") as? SymbolLayer, "the labels are symbols")
            }

        compose.onMain { assertTrue("\"name:fr\"" in labels.label(), labels.label()) }
        compose.onMain { map.setLabelLanguage(RestSchema.Language.De) }
        compose.until { renderer.drawn?.labelLanguage == RestSchema.Language.De }
        compose.until { "\"name:de\"" in labels.label() }
        compose.onMain { assertFalse("\"name:fr\"" in labels.label(), labels.label()) }

        // A layer of the map's own keeps the label it states, whatever the language (`map.style.label-scope`).
        compose.onMain {
            map.addSource(MapSource("trail", MapSource.Kind.GeoJson(pins)))
            map.addLayer(
                MapLayer(
                    id = "names",
                    type = TileSchema.TileLayerType.Symbol,
                    source = "trail",
                    rank = LayerRank.Poi.rank,
                    layout = mapOf("text-field" to JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("name")))),
                ),
            )
        }
        compose.until { renderer.map?.style?.getLayer("names") is SymbolLayer }
        compose.onMain { map.setLabelLanguage(RestSchema.Language.It) }
        compose.until { "\"name:it\"" in labels.label() }
        compose.onMain {
            val names =
                assertNotNull(renderer.map?.style?.getLayer("names") as? SymbolLayer, "the names are a symbol layer")
            assertFalse("name:" in names.label(), names.label())
        }
    }

    @Test
    fun anEditsMagnetMeasuresInTheScreensDensityIndependentPixelsThroughMapLibresProjection() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain { map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 15.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { abs((renderer.map?.cameraPosition?.zoom ?: 0.0) - 15) < 0.01 }

        compose.onMain {
            val line = listOf(listOf(6.85, 45.83), listOf(6.87, 45.83))
            val onLine = renderer.projection.point(listOf(6.86, 45.83))
            val near = renderer.projection.position(listOf(onLine[0], onLine[1] - 30))
            val far = renderer.projection.position(listOf(onLine[0], onLine[1] - 50))

            val snapped = renderer.snapped(near, listOf(line))
            assertTrue(snapped.isSnapped)
            assertTrue(abs(snapped.position[1] - 45.83) < 1e-6, "${snapped.position}")
            assertFalse(renderer.snapped(far, listOf(line)).isSnapped)
        }
    }

    @Test
    fun aCameraTheAppSetsMovesMapLibresItsTiltLeavingItsZoom() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain { map.jump(CameraTarget(center = listOf(7.5, 46.0), zoom = 13.0, bearing = 45.0, pitch = 30.0)) }
        compose.until { renderer.drawn?.camera?.zoom == 13.0 }

        compose.until { abs((renderer.map?.cameraPosition?.zoom ?: 0.0) - 13) < 0.01 }
        compose.onMain {
            val camera = assertNotNull(renderer.map, "the map is ready").cameraPosition
            val target = assertNotNull(camera.target, "the camera has a target")
            assertTrue(abs(target.latitude - 46.0) < 0.001, "$target")
            assertTrue(abs(camera.bearing - 45) < 0.01, "${camera.bearing}")
            assertTrue(abs(camera.tilt - 30) < 0.01, "${camera.tilt}")
            assertTrue(abs(camera.zoom - 13) < 0.01, "${camera.zoom}")
        }
    }

    /** The pixel of [image] under ([x], [y]), as `Pixels.pixel` reads it, a point the image holds. */
    private fun pixelAt(
        image: Bitmap,
        x: Double,
        y: Double,
    ): Pixels.Pixel = assertNotNull(Pixels.pixel(image, x, y), "($x, $y) lies inside the image")

    /** The pins of the markers MapLibre's map shows, bottom to top. */
    private fun MapRenderer.markerPins(): List<MapRenderer.MarkerPinView> =
        (0 until pins.childCount).mapNotNull { pins.getChildAt(it) as? MapRenderer.MarkerPinView }

    /**
     * The views on the layer the pins stand on in the order the platform draws them, bottom to top, the last one taking a
     * touch first: by their height, `z`, and among views of one height by their place on the layer, as a view group
     * orders the children it draws and hands a touch to.
     */
    private fun MapRenderer.drawingOrder(): List<View> =
        (0 until pins.childCount)
            .map(pins::getChildAt)
            .sortedBy { it.z }

    /** Whether the pin of the waypoint being placed is shown. */
    private fun MapRenderer.showsDraft(): Boolean =
        (0 until pins.childCount).any {
            pins.getChildAt(it) is MapRenderer.DraftPinView
        }

    /** Whether what MapLibre's map shows lies within [lock], `[west, south, east, north]`, to a hair. */
    private fun MapRenderer.showsWithin(lock: List<Double>): Boolean {
        val shown = assertNotNull(map, "the map is ready").projection.visibleRegion.latLngBounds
        val hair = 1e-4
        return shown.longitudeWest >= lock[0] - hair &&
            shown.latitudeSouth >= lock[1] - hair &&
            shown.longitudeEast <= lock[2] + hair &&
            shown.latitudeNorth <= lock[3] + hair
    }

    @Test
    fun markersArePinsOfTheRenderersOwnAboveEveryLayerEachWhereTheStatePlacesItAsItChanges() {
        val map = engine()
        val renderer = compose.showing(map)
        val layers = compose.onMain { renderer.drawnLayers() }

        compose.onMain {
            map.addMarker(MapMarker("hut", listOf(6.86, 45.83), label = "Refuge"))
            map.addMarker(MapMarker("summit", listOf(6.87, 45.84), color = "#e64980"))
        }
        compose.until { renderer.markerPins().size == 2 }
        compose.onMain {
            val shown = renderer.markerPins()
            assertEquals(listOf("hut", "summit"), shown.map { it.marker }, "the last one added on top")
            assertSame(renderer.view, renderer.pins.parent, "the pins are views above MapLibre's map")
            assertSame(renderer.view, renderer.mapView.parent, "beside MapLibre's own view, never inside it")
            assertTrue(
                renderer.view.indexOfChild(renderer.pins) > renderer.view.indexOfChild(renderer.mapView),
                "the layer stands above MapLibre's view",
            )
            assertEquals(layers, renderer.drawnLayers(), "a marker is no layer of the style")
            val disc = pixelAt(shown.last().image, 14.0, 4.5)
            assertTrue(disc.isNear(Pixels.Pixel(0xe6, 0x49, 0x80, 255)), "$disc")
            val density = renderer.mapView.resources.displayMetrics.density
            val hut = shown.first()
            val drawnAt = renderer.projection.point(listOf(6.86, 45.83))
            val across = (hut.translationX + hut.layoutParams.width / 2f) / density
            val down = (hut.translationY + hut.layoutParams.height / 2f) / density
            assertTrue(abs(across - drawnAt[0]) < 1, "$across against ${drawnAt[0]}")
            assertTrue(abs(down - drawnAt[1]) < 1, "$down against ${drawnAt[1]}")
        }

        compose.onMain {
            map.editMarker("hut", position = listOf(6.9, 45.9))
            map.removeMarker("summit")
        }
        compose.until { renderer.markerPins().singleOrNull()?.position == listOf(6.9, 45.9) }
    }

    /**
     * The watermark keeps its mark through every change of the pins it stands among, a marker added, moved and taken
     * off after it is drawn, standing above every pin and beneath the label a marker shows, and painting its letters.
     */
    @Test
    fun theWatermarkKeepsItsMarkThroughEveryChangeOfThePinsAfterItIsDrawn() {
        val map = engine()
        val renderer = compose.showing(map)
        val mark =
            assertNotNull(watermarkOf(OrganizationRead.NoKey, RestSchema.Language.Fr), "a map with no key draws it")
        val view =
            compose.onMain {
                watermarkViewOf(renderer.mapView.context, mark, "Visiter Mountaya").also { renderer.watermark = it }
            }
        compose.until { view.isAttachedToWindow && view.width > 0 }

        compose.onMain { map.addMarker(MapMarker("hut", listOf(6.86, 45.83), label = "Refuge")) }
        compose.until { renderer.markerPins().size == 1 }
        compose.onMain {
            map.addMarker(MapMarker("summit", listOf(6.87, 45.84), color = "#e64980"))
            map.editMarker("hut", position = listOf(6.9, 45.9))
        }
        compose.until { renderer.markerPins().size == 2 }
        compose.onMain { map.removeMarker("summit") }
        compose.until { renderer.markerPins().size == 1 }

        compose.onMain {
            assertSame(renderer.pins, view.parent, "the mark stays on the layer the pins stand on")
            assertTrue(view.isAttachedToWindow, "the mark stays in its window")
            // The platform draws by height before order, so the mark stands as high as a pin, and the label as the mark.
            val shown = renderer.markerPins()
            assertTrue(shown.all { it.z > 0f && it.z <= view.z }, "the mark stands as high as every pin at least")
            assertTrue(renderer.callout.z >= view.z, "a marker's label stands as high as the mark at least")
            val order = renderer.drawingOrder()
            val at = order.indexOf(view)
            assertTrue(
                shown.all { order.indexOf(it) < at },
                "the mark is drawn above every pin, and takes a touch first",
            )
            assertTrue(order.indexOf(renderer.callout) > at, "a marker's label is drawn above the mark")
            val image = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(image))
            val pixels = IntArray(view.width * view.height)
            image.getPixels(pixels, 0, view.width, 0, 0, view.width, view.height)
            assertTrue(
                pixels.any { (it ushr 24) > 200 && (it shr 16 and 0xff) > 200 },
                "the mark's letters are painted",
            )
        }
    }

    /**
     * Before MapLibre hands its map over, and where it never does, its surface never drawing, the mark stands at its frame
     * in the safe area at once, above MapLibre's load colour, which covers MapLibre's own view until its style is drawn,
     * while no pin and no label shows, none having a place on a map not drawn yet.
     */
    @Test
    fun theMarkShowsBeforeMapLibreHandsItsMapOverAndNoPinDoes() {
        val map = engine()
        val mark =
            assertNotNull(watermarkOf(OrganizationRead.NoKey, RestSchema.Language.Fr), "a map with no key draws it")
        compose.onMain {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            MapLibre.getInstance(context.applicationContext)
            // A view in no window makes no surface, so MapLibre hands no map over while the test reads it.
            val renderer = MapRenderer(map, MapView(context), StubTileApi.client) {}
            try {
                val density = renderer.mapView.resources.displayMetrics.density
                renderer.safeArea = listOf(0.0, 24.0, 390.0, 844.0)
                map.addMarker(MapMarker("hut", listOf(6.86, 45.83), label = "Refuge"))
                renderer.draw(map.state.value)
                val view = watermarkViewOf(context, mark, "Visiter Mountaya").also { renderer.watermark = it }
                val width = (390 * density).roundToInt()
                val height = (844 * density).roundToInt()
                renderer.view.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
                )
                renderer.view.layout(0, 0, width, height)

                assertNull(renderer.map, "MapLibre has handed no map over")
                assertSame(renderer.pins, view.parent, "the mark is laid on the layer the pins stand on")
                assertEquals(View.VISIBLE, renderer.pins.visibility, "the layer shows")
                assertEquals(View.VISIBLE, view.visibility, "the mark shows")
                assertTrue(renderer.markerPins().none { it.visibility == View.VISIBLE }, "no pin shows")
                assertEquals(View.GONE, renderer.callout.visibility, "no label shows")
                val left = view.left + view.translationX
                val top = view.top + view.translationY
                assertTrue(abs(left - mark.x * density) < 1, "the mark stands at the safe area's left, at $left")
                assertTrue(abs(top - (24.0 + mark.y) * density) < 1, "the mark stands at the safe area's top, at $top")

                // MapLibre's load colour covers its own view and nothing beside it: the mark is drawn over it.
                val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                renderer.view.draw(Canvas(image))
                val covered = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                renderer.mapView.draw(Canvas(covered))
                val load = covered.getPixel(width / 2, height / 2)
                val x0 = (left + view.width * 0.2f).roundToInt()
                val x1 = (left + view.width * 0.8f).roundToInt()
                val y0 = (top + view.height * 0.3f).roundToInt()
                val y1 = (top + view.height * 0.7f).roundToInt()
                val drawn =
                    (x0 until x1).sumOf { x ->
                        (y0 until y1).count { y -> image.getPixel(x, y) != load }
                    }
                assertTrue(drawn > 0, "the mark is drawn over MapLibre's load colour")
            } finally {
                renderer.close()
                // MapLibre's view was never driven, so its native map is let go of here.
                renderer.mapView.onDestroy()
            }
        }
    }

    /**
     * A marker's label stands past its pin toward the end its text runs to: to its right in a layout that runs left to
     * right, and to its left in one that runs right to left, centred on the pin from top to bottom.
     */
    @Test
    fun aMarkersLabelStandsPastItsPinTowardTheEndItsTextRunsTo() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain { map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 15.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { abs((renderer.map?.cameraPosition?.zoom ?: 0.0) - 15) < 0.01 }
        val at = listOf(200.0, 400.0)
        compose.onMain {
            renderer.safeArea = listOf(0.0, 24.0, 390.0, 844.0)
            map.addMarker(MapMarker("hut", renderer.projection.position(at), label = "Refuge"))
        }
        compose.until { renderer.markerPins().size == 1 }

        /** The label's left, right, top and bottom edges, in density-independent pixels from the map's corner. */
        fun edges(): List<Double> {
            val density =
                renderer.mapView.resources.displayMetrics.density
                    .toDouble()
            val callout = renderer.callout
            val left = (callout.left + callout.translationX) / density
            val top = (callout.top + callout.translationY) / density
            return listOf(left, left + callout.width / density, top, top + callout.height / density)
        }
        compose.onMain { renderer.clicked(at) }
        compose.until { renderer.callout.visibility == View.VISIBLE && renderer.callout.width > 0 }
        compose.onMain {
            val (left, _, top, bottom) = edges()
            assertTrue(abs(left - (at[0] + 18)) < 1, "the label starts 18 right of its pin, at $left")
            assertTrue(abs((top + bottom) / 2 - at[1]) < 1, "centred on its pin, from $top to $bottom")
            renderer.mapView.layoutDirection = View.LAYOUT_DIRECTION_RTL
            renderer.clicked(at)
        }
        compose.until { abs(edges()[1] - (at[0] - 18)) < 1 }
        compose.onMain {
            val (_, right, top, bottom) = edges()
            assertTrue(abs(right - (at[0] - 18)) < 1, "right to left, the label ends 18 left of its pin, at $right")
            assertTrue(abs((top + bottom) / 2 - at[1]) < 1, "centred on its pin, from $top to $bottom")
        }
    }

    /**
     * In a layout that runs right to left the mark still stands at the top left of the map's safe area, the layer it
     * stands on laid out left to right, and a pin over its position as it stands in one that runs left to right.
     */
    @Test
    fun inALayoutThatRunsRightToLeftTheMarkStandsAtTheMapsTopLeft() {
        val map = engine()
        val renderer = compose.showing(map)
        val mark =
            assertNotNull(watermarkOf(OrganizationRead.NoKey, RestSchema.Language.Fr), "a map with no key draws it")
        compose.onMain { map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 15.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { abs((renderer.map?.cameraPosition?.zoom ?: 0.0) - 15) < 0.01 }
        val view =
            compose.onMain {
                renderer.mapView.layoutDirection = View.LAYOUT_DIRECTION_RTL
                renderer.safeArea = listOf(0.0, 24.0, 390.0, 844.0)
                map.addMarker(MapMarker("hut", listOf(6.86, 45.83)))
                watermarkViewOf(renderer.mapView.context, mark, "Visiter Mountaya").also { renderer.watermark = it }
            }
        compose.until { view.isAttachedToWindow && view.width > 0 && renderer.markerPins().size == 1 }

        compose.onMain {
            val density = renderer.mapView.resources.displayMetrics.density
            assertEquals(View.LAYOUT_DIRECTION_LTR, renderer.pins.layoutDirection, "the layer runs left to right")
            val left = view.left + view.translationX
            val top = view.top + view.translationY
            assertTrue(abs(left - mark.x * density) < 1, "the mark stands at the map's left, at $left")
            assertTrue(abs(top - (24.0 + mark.y) * density) < 1, "the mark stands at the safe area's top, at $top")
            val pin = renderer.markerPins().single()
            val drawnAt = renderer.projection.point(listOf(6.86, 45.83))
            val middle = (pin.left + pin.translationX + pin.width / 2f) / density
            assertTrue(abs(middle - drawnAt[0]) < 1, "the pin stands over its position, at $middle of ${drawnAt[0]}")
        }
    }

    /**
     * A marker's label is the SDK's own interface: it keeps to the map's safe area and moves clear of the watermark's
     * frame by the smallest move, as the readout does, drawn above the mark and never over it.
     */
    @Test
    fun aMarkersLabelKeepsToTheSafeAreaAndClearOfTheWatermarksFrame() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain { map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 15.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { abs((renderer.map?.cameraPosition?.zoom ?: 0.0) - 15) < 0.01 }
        val frame = listOf(-4.0, 20.0, 180.0, watermarkHeight)
        // A marker under the frame, 60 across and 70 down from the map's corner.
        val under = listOf(60.0, 70.0)
        compose.onMain {
            renderer.safeArea = listOf(0.0, 24.0, 390.0, 844.0)
            renderer.keepClear = frame
            map.addMarker(MapMarker("hut", renderer.projection.position(under), label = "Refuge du Goûter"))
        }
        compose.until { renderer.markerPins().size == 1 }

        compose.onMain { renderer.clicked(under) }
        compose.until { renderer.callout.visibility == View.VISIBLE && renderer.callout.width > 0 }
        compose.onMain {
            val density = renderer.mapView.resources.displayMetrics.density
            val callout = renderer.callout
            val left = (callout.left + callout.translationX) / density
            val top = (callout.top + callout.translationY) / density
            val right = left + callout.width / density
            val bottom = top + callout.height / density
            assertTrue(
                top >= frame[1] + frame[3] - 0.5 || left >= frame[0] + frame[2] - 0.5,
                "the label at ($left, $top) stands clear of the frame",
            )
            assertTrue(left >= -0.5 && top >= 23.5 && right <= 390.5 && bottom <= 844.5, "inside the safe area")
            val order = renderer.drawingOrder()
            assertTrue(
                order.indexOf(callout) > order.indexOf(renderer.markerPins().single()),
                "the label is drawn above the pins",
            )
        }
    }

    /**
     * A marker's label shows only while its marker's point stands on the map: a camera that takes the point off the map
     * takes the label with it, never holding it at the map's edge without its pin, and one that brings the point back
     * shows it again, the label kept for its marker meanwhile.
     */
    @Test
    fun aMarkersLabelShowsOnlyWhileItsPointStandsOnTheMap() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain { map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 15.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { abs((renderer.map?.cameraPosition?.zoom ?: 0.0) - 15) < 0.01 }
        val at = listOf(200.0, 400.0)
        val hut = compose.onMain { renderer.projection.position(at) }
        compose.onMain {
            renderer.safeArea = listOf(0.0, 24.0, 390.0, 844.0)
            map.addMarker(MapMarker("hut", hut, label = "Refuge"))
        }
        compose.until { renderer.markerPins().size == 1 }
        compose.onMain { renderer.clicked(at) }
        compose.until { renderer.callout.visibility == View.VISIBLE && renderer.callout.width > 0 }

        // At zoom 15 the map is about 0.017 degrees wide: a camera 0.04 east leaves the hut far off its left edge.
        compose.onMain { map.jump(CameraTarget(center = listOf(6.90, 45.83), zoom = 15.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { renderer.projection.point(hut)[0] < -100 }
        compose.until { renderer.callout.visibility == View.GONE }
        compose.onMain { assertEquals("Refuge", renderer.callout.text.toString(), "the label is kept for its marker") }

        compose.onMain { map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 15.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { renderer.callout.visibility == View.VISIBLE }
        compose.onMain {
            val point = renderer.projection.point(hut)
            assertTrue(point[0] in 0.0..390.0 && point[1] in 0.0..844.0, "the hut stands on the map again, at $point")
        }
    }

    /**
     * A marker's label is drawn in the tooltip's colours, `tooltip-fg` on `tooltip-bg`, on a card rounded at 4 that casts
     * no shadow, as `sdk-swift` and `sdk-dart` draw theirs: the standard tokens under the light scheme until the
     * composable hands the host's, and a new pair paints it again. It is raised for the order it is drawn in alone.
     */
    @Test
    fun aMarkersLabelIsDrawnInTheTooltipsColoursOnACardRoundedAtFour() {
        val map = engine()
        val renderer = compose.showing(map)

        compose.onMain {
            val density = renderer.mapView.resources.displayMetrics.density
            val card = assertIs<GradientDrawable>(renderer.callout.background, "the label stands on a card")
            assertEquals(4 * density, card.cornerRadius, "rounded at 4")
            assertNull(renderer.callout.outlineProvider, "the platform casts no shadow of a label with no outline")
            assertTrue(renderer.callout.elevation > 0f, "the label is raised, which orders it above the mark")
            assertEquals(Color.parseColor("#212529"), card.color?.defaultColor, "the standard tooltip-bg, light")
            assertEquals(Color.WHITE, renderer.callout.currentTextColor, "the standard tooltip-fg, light")

            renderer.tooltipBg = HexColor("#f1f3f5")
            renderer.tooltipFg = HexColor("#141517")
            assertEquals(Color.parseColor("#f1f3f5"), card.color?.defaultColor, "the ground handed")
            assertEquals(Color.parseColor("#141517"), renderer.callout.currentTextColor, "the text handed")
        }
    }

    @Test
    fun aClickTellsOnClickWhereItFellAndOnWhichMarkerOnePlacingAWaypointPlacesIt() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain { map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 15.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { abs((renderer.map?.cameraPosition?.zoom ?: 0.0) - 15) < 0.01 }
        val taps = mutableListOf<MapTap>()
        compose.onMain {
            renderer.onClick = { taps += it }
            map.addMarker(MapMarker("hut", listOf(6.86, 45.83), label = "Refuge"))
        }
        compose.until { renderer.markerPins().size == 1 }

        val onHut = compose.onMain { renderer.projection.point(listOf(6.86, 45.83)) }
        // A click is told to `onClick` through the engine's events, a moment after it lands.
        compose.onMain { renderer.clicked(listOf(onHut[0] + 10, onHut[1])) }
        compose.until { taps.size == 1 }
        compose.onMain {
            assertEquals(View.VISIBLE, renderer.callout.visibility, "a click on a marker shows its label")
            assertEquals("Refuge", renderer.callout.text.toString())
            renderer.clicked(listOf(onHut[0], onHut[1] + 100))
        }
        compose.until { taps.size == 2 }
        compose.onMain {
            assertEquals(listOf("hut", null), taps.map { it.marker })
            assertEquals(View.GONE, renderer.callout.visibility, "a click elsewhere takes the label off")
            val away = renderer.projection.position(listOf(onHut[0], onHut[1] + 100))
            assertTrue(abs(taps.last().position[1] - away[1]) < 1e-9)
            assertEquals(listOf(onHut[0], onHut[1] + 100), taps.last().point)

            map.setActiveSegments(listOf(listOf(listOf(6.85, 45.83), listOf(6.87, 45.83))))
            map.beginWaypointEdit("w1")
            renderer.clicked(listOf(onHut[0] + 50, onHut[1] - 30))
            assertEquals(2, taps.size, "a click the edit takes is told to no handler")
            val placed = assertNotNull(map.state.value.waypointEdit, "a waypoint is being placed")
            assertTrue(placed.isSnapped)
            assertTrue(abs((placed.position?.get(1) ?: 0.0) - 45.83) < 1e-6, "$placed")
        }
        compose.until { renderer.showsDraft() }
        compose.onMain { map.endWaypointEdit() }
        compose.until { !renderer.showsDraft() }
    }

    @Test
    fun theZoomRangeAndThePanLockAreMapLibresCameraLimitsWhatTheViewShowsHeldWithinTheLock() {
        val map = engine()
        val renderer = compose.showing(map)
        val maplibre = assertNotNull(renderer.map, "the map is ready")
        val lock = listOf(6.7, 45.7, 7.0, 46.0)

        compose.onMain { map.setZoomBounds(ZoomLimit.Zoom(5.0), ZoomLimit.Zoom(12.0)) }
        compose.until { maplibre.maxZoomLevel == 12.0 }
        compose.onMain { assertEquals(5.0, maplibre.minZoomLevel) }
        compose.onMain { map.setZoomBounds(ZoomLimit.Zoom(14.0), ZoomLimit.Zoom(16.0)) }
        compose.until { maplibre.minZoomLevel == 14.0 }
        compose.onMain {
            assertEquals(16.0, maplibre.maxZoomLevel)
            assertTrue(abs(maplibre.cameraPosition.zoom - 14) < 0.01, "the engine held the camera at 14")
        }
        compose.onMain { map.setZoomBounds(ZoomLimit.Released, ZoomLimit.Released) }
        // MapLibre Native holds its ceiling as a scale, so 25.5 reads back to a hair under or over.
        compose.until { abs(maplibre.maxZoomLevel - 25.5) < 1e-9 }
        // The floor handed over is 0, and MapLibre holds its own over it: the zoom at which the world fills
        // its frame's height, under which it draws nothing.
        compose.onMain { assertEquals(renderer.worldFillsTheFrame(), maplibre.minZoomLevel, 1e-9) }

        compose.onMain {
            map.jump(CameraTarget(center = listOf(6.85, 45.85), zoom = 12.0, bearing = 0.0, pitch = 0.0))
            map.setMaxBounds(lock)
        }
        compose.until { renderer.drawn?.maxBounds == lock }
        compose.onMain { assertTrue(renderer.showsWithin(lock), "${maplibre.projection.visibleRegion}") }
        compose.onMain { map.jump(CameraTarget(center = listOf(8.0, 47.0), zoom = 12.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { renderer.drawn?.camera?.center == listOf(8.0, 47.0) }
        compose.onMain { assertTrue(renderer.showsWithin(lock), "a camera set outside the lock is held within it") }
        compose.onMain { map.jump(CameraTarget(center = listOf(6.85, 45.85), zoom = 3.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { renderer.drawn?.camera?.zoom == 3.0 }
        compose.onMain {
            assertTrue(maplibre.cameraPosition.zoom > 9, "zoomed out no farther than the lock fills the view")
            assertTrue(renderer.showsWithin(lock))
        }

        compose.onMain { map.setMaxBounds(null) }
        compose.until { renderer.drawn?.maxBounds == null }
        compose.onMain { map.jump(CameraTarget(center = listOf(8.0, 47.0), zoom = 12.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { abs((maplibre.cameraPosition.target?.longitude ?: 0.0) - 8.0) < 1e-6 }
        compose.onMain {
            assertEquals(renderer.worldFillsTheFrame(), maplibre.minZoomLevel, 1e-9, "a lift restores the floor")
        }
    }

    @Test
    fun aCameraALockMovesIsReportedToTheEngineSoTheStateIsTheMapAsShown() {
        val map = engine()
        val renderer = compose.showing(map)
        val lock = listOf(6.7, 45.7, 7.0, 46.0)
        compose.onMain { map.jump(CameraTarget(center = listOf(8.0, 47.0), zoom = 12.0, bearing = 0.0, pitch = 0.0)) }
        compose.until {
            abs(
                (
                    renderer.map
                        ?.cameraPosition
                        ?.target
                        ?.longitude ?: 0.0
                ) - 8.0,
            ) < 1e-6
        }

        compose.onMain { map.setMaxBounds(lock) }
        compose.until { map.state.value.camera.center[0] < 7.0 }
        compose.onMain {
            val center = map.state.value.camera.center
            assertTrue(center[0] in lock[0]..lock[2] && center[1] in lock[1]..lock[3], "$center")
            assertTrue(renderer.showsWithin(lock))
        }
    }

    @Test
    fun aCameraTheAppSetsAsTheLimitsChangeIsTheOneShownNeverTheOneTheyHeld() {
        val map = engine()
        val renderer = compose.showing(map)
        val maplibre = assertNotNull(renderer.map, "the map is ready")
        val lock = listOf(6.7, 45.7, 7.0, 46.0)
        val wanted = CameraPose(listOf(7.5, 46.5), 3.0, 0.0, 0.0)

        // The lock lifted and a camera set in one turn, twice: drawn as the flow hands the renderer the
        // newest state, then with the state the lift left drawn first, as a renderer drawing a state the
        // engine has moved past draws it, where MapLibre's camera held under the lock is no news.
        for (drawsTheLiftFirst in listOf(false, true)) {
            compose.onMain { map.setMaxBounds(lock) }
            compose.until { renderer.drawn?.maxBounds == lock && renderer.drawn == map.state.value }
            compose.onMain {
                map.jump(
                    CameraTarget(center = listOf(8.0, 47.0), zoom = 12.0, bearing = 0.0, pitch = 0.0),
                )
            }
            compose.until { renderer.drawn?.camera?.center == listOf(8.0, 47.0) }

            compose.onMain {
                map.setMaxBounds(null)
                val lifted = map.state.value
                map.jump(
                    CameraTarget(
                        center = wanted.center,
                        zoom = wanted.zoom,
                        bearing = wanted.bearing,
                        pitch = wanted.pitch,
                    ),
                )
                if (drawsTheLiftFirst) renderer.draw(lifted)
                assertEquals(wanted, map.state.value.camera, "drawn the lift first: $drawsTheLiftFirst")
            }
            compose.until { renderer.drawn == map.state.value && renderer.drawn?.maxBounds == null }
            compose.onMain { assertEquals(wanted, map.state.value.camera, "drawn the lift first: $drawsTheLiftFirst") }
            compose.until { abs(maplibre.cameraPosition.zoom - 3) < 0.01 }
            compose.onMain {
                val longitude = assertNotNull(maplibre.cameraPosition.target, "the camera has a target").longitude
                assertTrue(abs(longitude - 7.5) < 1e-6, "$longitude")
            }
        }
    }

    /**
     * Sets the camera at zoom 5 just west of the antimeridian, then just east of it, and asserts each time
     * that MapLibre's camera stands where it was set and that the view shows a degree past the
     * antimeridian, as a map that holds no wall there shows it.
     */
    private fun assertTheViewCrossesTheAntimeridian(
        map: MapEngine,
        renderer: MapRenderer,
        why: String,
    ) {
        for (longitude in listOf(179.99, -179.99)) {
            compose.onMain {
                map.jump(
                    CameraTarget(center = listOf(longitude, 0.0), zoom = 5.0, bearing = 0.0, pitch = 0.0),
                )
            }
            compose.until {
                val target = renderer.map?.cameraPosition?.target
                target != null && abs(target.longitude - longitude) < 1e-6
            }
            compose.onMain {
                val beyond = if (longitude > 0) -179.0 else 179.0
                val drawnAt = renderer.projection.point(listOf(beyond, 0.0))
                val density = renderer.mapView.resources.displayMetrics.density
                val across = renderer.mapView.width / density.toDouble()
                assertTrue(drawnAt[0] in 0.0..across, "$why: $beyond drawn at ${drawnAt[0]} of $across")
            }
        }
    }

    @Test
    fun aLiftedLockLetsTheViewCrossTheAntimeridianAsAMapNeverLocked() {
        val map = engine()
        val renderer = compose.showing(map)
        val lock = listOf(6.7, 45.7, 7.0, 46.0)
        assertTheViewCrossesTheAntimeridian(map, renderer, "never locked")

        compose.onMain { map.setMaxBounds(lock) }
        compose.until { renderer.drawn?.maxBounds == lock && renderer.drawn == map.state.value }
        compose.onMain { assertTrue(renderer.showsWithin(lock), "${renderer.map?.projection?.visibleRegion}") }

        // A lift hands MapLibre Android no bound at all, the core's unbounded one, and its constrain mode
        // is never moved, so nothing is left to hold the view at the world's edges.
        compose.onMain { map.setMaxBounds(null) }
        compose.until { renderer.drawn?.maxBounds == null && renderer.drawn == map.state.value }
        assertTheViewCrossesTheAntimeridian(map, renderer, "after a lift")
    }

    @Test
    fun theCameraAtRestIsToldToOnCameraChangeWhoeverMovedIt() {
        val map = engine()
        val renderer = compose.showing(map)
        val rested = mutableListOf<CameraPose>()
        compose.onMain { renderer.onCameraChange = { rested += it } }

        compose.onMain { map.jump(CameraTarget(center = listOf(7.5, 46.0), zoom = 13.0, bearing = 0.0, pitch = 0.0)) }
        compose.until { rested.any { abs(it.zoom - 13) < 0.01 } }
        val before = compose.onMain { rested.size }
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.until { rested.size > before && map.state.value.camera == rested.last() }
        compose.onMain {
            assertEquals(map.state.value.camera, rested.last(), "a reader's move is the state's camera too")
        }
    }

    @Test
    fun aMapDrawsTheMarkerSpriteAnImageItsStyleNamesAndDoesNotHoldNamesThroughMapLibreItself() {
        val map = engine()
        val renderer = compose.showing(map)

        compose.onMain {
            val hut = assertNotNull(renderer.missingImage("section-icon-hut-1C7ED6"), "the hut's image is drawn")
            val density = hut.density / 160.0
            assertEquals(renderer.mapView.resources.displayMetrics.densityDpi, hut.density)
            assertEquals(Math.round(48 * density).toInt(), hut.width)
            val disc = pixelAt(hut, 24.0, 8.0)
            assertTrue(disc.isNear(Pixels.Pixel(0x1c, 0x7e, 0xd6, 255)), "$disc")
            assertNotNull(
                renderer.missingImage("poi-icon-campground-0b7285"),
                "the renderer draws the image the style names",
            )
            assertNotNull(
                renderer.missingImage("section-icon-road-access-permit-9c36b5"),
                "the renderer draws the image the style names",
            )
            // Any other id is left missing: one under no prefix, one whose tail is no glyph and colour, and one
            // naming a glyph the set cannot draw.
            assertNull(renderer.missingImage("some-basemap-sprite"))
            assertNull(renderer.missingImage("section-icon-hut-fff"))
            assertNull(renderer.missingImage("section-icon-activity-hiking-1c7ed6"))
            // MapLibre asks for each image a symbol layer names as it lays a symbol out, and draws the sprite it
            // is answered with under the id.
            map.addSource(
                MapSource(
                    "pins",
                    MapSource.Kind.GeoJson(
                        GeoJsonFeatureCollection(
                            listOf(GeoJsonFeature(GeoJsonPoint(listOf(6.86, 45.83)), JsonObject(emptyMap()))),
                        ),
                    ),
                ),
            )
            for ((id, image) in listOf(
                "waypoints" to "section-icon-flag-start-e03131",
                "basemap-icons" to "some-basemap-sprite",
            )) {
                map.addLayer(
                    MapLayer(
                        id = id,
                        type = TileSchema.TileLayerType.Symbol,
                        source = "pins",
                        rank = LayerRank.ItineraryPoint.rank,
                        layout =
                            mapOf(
                                "icon-image" to JsonPrimitive(image),
                                "icon-allow-overlap" to JsonPrimitive(true),
                            ),
                    ),
                )
            }
        }
        compose.until { renderer.map?.style?.getImage("section-icon-flag-start-e03131") != null }
        compose.onMain {
            val style = assertNotNull(renderer.map?.style, "the map holds its style")
            // The image MapLibre holds is the sprite's pixels, read at the 48 pixels it is drawn across.
            val drawn = assertNotNull(style.getImage("section-icon-flag-start-e03131"), "the style holds the flag")
            val disc = assertNotNull(Pixels.pixel(drawn, 24.0, 8.0, scale = drawn.width / 48.0), "inside the image")
            assertTrue(disc.isNear(Pixels.Pixel(0xe0, 0x31, 0x31, 255)), "$disc")
            assertNull(style.getImage("some-basemap-sprite"))
        }
    }

    @Test
    fun aMapsSpritesAreDrawnFromTheGlyphSetItsEngineIsGiven() {
        val licensed =
            assertNotNull(
                GlyphSet.of(
                    "licensed",
                    GlyphSet.Paint.Fill,
                    markup =
                        mapOf("refuge" to """<rect width="24" height="24"/>"""),
                ),
                "the licensed set is a set",
            )
        val map = engine(glyphs = licensed)
        val renderer = compose.showing(map)

        compose.onMain {
            val refuge =
                assertNotNull(renderer.missingImage("section-icon-refuge-000000"), "the refuge's image is drawn")
            // The glyph, a square filling its box, is the white square at the sprite's centre.
            val centre = pixelAt(refuge, 24.0, 24.0)
            assertTrue(centre.isNear(Pixels.white), "$centre")
            assertNull(renderer.missingImage("section-icon-hut-000000"))
            // A marker's pin carries a glyph of that set too.
            map.addMarker(MapMarker("refuge", listOf(6.86, 45.83), glyph = "refuge"))
        }
        compose.until { renderer.markerPins().isNotEmpty() }
        compose.onMain {
            // Inside the glyph's square and outside where a dot would be: the square is drawn.
            val corner = pixelAt(renderer.markerPins().first().image, 7.0, 7.0)
            assertTrue(corner.isNear(Pixels.white), "$corner")
        }
    }

    private val accent = Pixels.Pixel(0x1c, 0x7e, 0xd6, 255)
    private val red = Pixels.Pixel(0xe0, 0x31, 0x31, 255)

    /** Runs [command] on the main thread, where the engine is driven, and answers what it answers once it ends. */
    private fun <T> onMainSuspending(command: suspend () -> T): T = runBlocking(Dispatchers.Main) { command() }

    @Test
    fun aMarkerIsShownAsItsPinTheAccentAndADotWhereItNamesNeitherElseItsOwn() {
        val map = engine()
        val renderer = compose.showing(map)

        compose.onMain {
            map.addMarker(MapMarker("plain", listOf(6.86, 45.83)))
            map.addMarker(MapMarker("hut", listOf(6.87, 45.84, 1834.0), color = "#e03131", glyph = "hut"))
        }
        compose.until { renderer.markerPins().size == 2 }
        compose.until { renderer.markerPins().all { it.isShown } }
        compose.onMain {
            val (plain, hut) = renderer.markerPins()
            val metrics = renderer.mapView.resources.displayMetrics
            assertEquals(Math.round(28 * metrics.density), plain.layoutParams.width)
            assertEquals(plain.layoutParams.width, plain.layoutParams.height)
            assertTrue(plain.elevation > 0f, "a soft shadow lifts the pin off the map")
            assertTrue(pixelAt(plain.image, 14.0, 6.0).isNear(accent))
            assertTrue(pixelAt(plain.image, 14.0, 14.0).isNear(Pixels.white))
            assertTrue(pixelAt(plain.image, 14.0, 1.5).isNear(Pixels.white))
            val described =
                markerPinImage(
                    describeMarkerPin("#e03131", "hut", map.glyphs),
                    HexColor("#1c7ed6"),
                    map.glyphs,
                    metrics.densityDpi,
                )
            assertTrue(hut.image.sameAs(described), "the pin the rule describes")
            assertTrue(pixelAt(hut.image, 14.0, 4.5).isNear(red))
            // An altitude is accepted, and the pin stands on the longitude and latitude.
            val drawnAt = renderer.projection.point(listOf(6.87, 45.84))
            val across = (hut.translationX + hut.layoutParams.width / 2f) / metrics.density
            val down = (hut.translationY + hut.layoutParams.height / 2f) / metrics.density
            assertTrue(abs(across - drawnAt[0]) < 1 && abs(down - drawnAt[1]) < 1, "($across, $down) against $drawnAt")
        }

        // An edit draws the pin again, with what the edit changed.
        val edited =
            compose.onMain {
                map.editMarker("plain", glyph = "hut")
                renderer.pinImage(MapMarker("plain", listOf(6.86, 45.83), glyph = "hut"))
            }
        compose.until {
            renderer
                .markerPins()
                .firstOrNull()
                ?.image
                ?.sameAs(edited) == true
        }
    }

    @Test
    fun theHostsAccentDrawsEveryPinAgainAMarkerNamingItsColourKeepingIt() {
        val map = engine()
        val renderer = compose.showing(map)
        val orange = Pixels.Pixel(0xe9, 0x50, 0x1e, 255)
        compose.onMain {
            map.addMarker(MapMarker("plain", listOf(6.86, 45.83)))
            map.addMarker(MapMarker("red", listOf(6.87, 45.84), color = "#e03131"))
        }
        compose.until { renderer.markerPins().size == 2 }
        val before = compose.onMain { renderer.markerPins().first() }
        compose.onMain {
            assertEquals(
                HexColor("#1c7ed6"),
                renderer.accent,
                "the standard accent, under the light scheme",
            )
        }

        compose.onMain { renderer.accent = HexColor("#e9501e") }

        compose.onMain {
            val (plain, coloured) = renderer.markerPins()
            assertTrue(plain !== before, "every pin is drawn again")
            assertTrue(pixelAt(plain.image, 14.0, 6.0).isNear(orange))
            assertTrue(pixelAt(plain.image, 14.0, 14.0).isNear(Pixels.white))
            assertTrue(
                pixelAt(coloured.image, 14.0, 6.0).isNear(red),
                "a marker naming its colour keeps it",
            )
        }
    }

    @Test
    fun aLookIsDrawnOnceForEveryPinCarryingItAndAPinIsKeptWhileItsLookStands() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain {
            map.addMarker(MapMarker("a", listOf(6.86, 45.83), glyph = "hut"))
            map.addMarker(MapMarker("b", listOf(6.87, 45.84), glyph = "hut"))
            map.addMarker(MapMarker("c", listOf(6.88, 45.85), color = "#e03131"))
        }
        compose.until { renderer.markerPins().size == 3 }
        val before = compose.onMain { renderer.markerPins() }
        compose.onMain {
            val (a, b, c) = before
            assertSame(a.image, b.image, "one image for one look")
            assertTrue(a.image !== c.image)
        }

        compose.onMain {
            map.addMarker(MapMarker("d", listOf(6.89, 45.86), glyph = "hut"))
            map.editMarker("b", position = listOf(6.9, 45.9))
        }
        compose.until { renderer.markerPins().size == 4 }

        compose.onMain {
            val after = renderer.markerPins()
            assertEquals(listOf("a", "b", "c", "d"), after.map { it.marker })
            assertSame(before[0], after[0], "a pin whose look stands is kept and not drawn again")
            assertSame(before[1], after[1], "a pin moved keeps its view and its image")
            assertEquals(listOf(6.9, 45.9), after[1].position)
            assertSame(before[2], after[2])
            assertSame(before[0].image, after[3].image, "a new pin of a look shown takes its image")
        }
    }

    @Test
    fun aRestrictedStretchIsStripedInItsKindsPatternWhichMapLibreAsksForAndAdds() {
        val map = engine()
        val renderer = compose.showing(map)
        // The permit kind's colour, `roadAccessColor(RoadAccessKind.Permit)`.
        val purple = Pixels.Pixel(0x9c, 0x36, 0xb5, 255)

        compose.onMain {
            val permit =
                assertNotNull(renderer.missingImage("road-access-stripe-permit"), "the permit stripe's image is drawn")
            assertEquals(Math.round(16 * permit.density / 160f), permit.width)
            val stripe = pixelAt(permit, 8.0, 8.0)
            assertTrue(stripe.isNear(purple), "on the stripe through the tile: $stripe")
            val ground = pixelAt(permit, 4.0, 4.0)
            assertTrue(ground.isNear(Pixels.white), "the white ground between two stripes: $ground")
            assertNotNull(
                renderer.missingImage("road-access-stripe-local-access"),
                "the renderer draws the image the style names",
            )
            assertNull(renderer.missingImage("road-access-stripe-toll"), "a tail that is no kind")

            map.addSource(
                MapSource(
                    "stretches",
                    MapSource.Kind.GeoJson(
                        GeoJsonFeatureCollection(
                            listOf(
                                GeoJsonFeature(
                                    GeoJsonLineString(listOf(listOf(6.85, 45.83), listOf(6.87, 45.83))),
                                    JsonObject(mapOf("kind" to JsonPrimitive("permit"))),
                                ),
                            ),
                        ),
                    ),
                ),
            )
            map.addLayer(
                MapLayer(
                    id = "stretch-stripes",
                    type = TileSchema.TileLayerType.Line,
                    source = "stretches",
                    rank = LayerRank.ItineraryRestriction.rank,
                    paint =
                        mapOf(
                            "line-width" to JsonPrimitive(8),
                            "line-pattern" to
                                JsonArray(
                                    listOf(
                                        JsonPrimitive("match"),
                                        JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("kind"))),
                                        JsonPrimitive("permit"),
                                        JsonPrimitive("road-access-stripe-permit"),
                                        JsonPrimitive("road-access-stripe-restricted"),
                                    ),
                                ),
                        ),
                ),
            )
        }
        compose.until { renderer.map?.style?.getImage("road-access-stripe-permit") != null }
    }

    /** A specification of an itinerary on the stub Tile API: its line under its casing, and its waypoints. */
    private val trip =
        TileSchema.TileSpecification(
            listOf(
                TileSchema.TileSource(
                    id = "trip",
                    layers =
                        listOf(
                            TileSchema.TileLayer(
                                "trip-lines-casing",
                                key = "lines",
                                layout = null,
                                paint = mapOf("line-width" to JsonPrimitive(7)),
                                source_layer = "itinerary_lines",
                                type = TileSchema.TileLayerType.Line,
                                zoom_max = 22,
                                zoom_min = 0,
                            ),
                            TileSchema.TileLayer(
                                "trip-lines",
                                key = "lines",
                                layout = null,
                                paint = mapOf("line-width" to JsonPrimitive(4)),
                                source_layer = "itinerary_lines",
                                type = TileSchema.TileLayerType.Line,
                                zoom_max = 22,
                                zoom_min = 0,
                            ),
                            TileSchema.TileLayer(
                                "trip-points",
                                key = "points",
                                layout = null,
                                paint = null,
                                source_layer = "itinerary_points",
                                type = TileSchema.TileLayerType.Symbol,
                                zoom_max = 22,
                                zoom_min = 0,
                            ),
                        ),
                    tiles = listOf("https://tiles.example.test/v1/itineraries/trip/{z}/{x}/{y}"),
                    type = TileSchema.TileSourceType.Vector,
                    zoom_max = 14,
                    zoom_min = 0,
                ),
            ),
        )

    /** Whether MapLibre's map draws the layer [id] shown, `visible` as MapLibre reads it back, null where it draws no such layer. */
    private fun MapRenderer.shows(id: String): Boolean? =
        map
            ?.style
            ?.getLayer(id)
            ?.visibility
            ?.value
            ?.let { it == "visible" }

    @Test
    fun aSpecificationDrawsItsRestrictedStretchesBesideItsLineAndItsContentSurvivesASwitch() {
        val map = engine()
        val renderer = compose.showing(map)
        val drawn =
            compose.onMain {
                map.setSpecification(trip, "trip")
                map.addSource(MapSource("trail", MapSource.Kind.GeoJson(pins)))
                map.addLayer(
                    MapLayer(
                        id = "route",
                        type = TileSchema.TileLayerType.Line,
                        source = "trail",
                        rank = LayerRank.ItineraryLine.rank,
                    ),
                )
                map.state.value.layers
                    .map { it.id }
            }
        assertTrue("trip-lines__road-access-stripe" in drawn && "trip-lines__road-access-icon" in drawn, "$drawn")
        compose.until { renderer.ownLayers() == drawn }

        compose.onMain {
            val style = assertNotNull(renderer.map?.style, "the map holds its style")
            val stripes = assertIs<LineLayer>(style.getLayer("trip-lines__road-access-stripe"))
            val icons = assertIs<SymbolLayer>(style.getLayer("trip-lines__road-access-icon"))
            assertEquals("itinerary_restrictions", stripes.sourceLayer)
            assertEquals("trip", stripes.sourceId)
            assertEquals("itinerary_restrictions", icons.sourceLayer)
            assertTrue(renderer.shows(stripes.id) == true && renderer.shows(icons.id) == true, "shown by default")
            map.setAccessRestrictionsVisibility(false)
        }
        compose.until {
            renderer.shows("trip-lines__road-access-stripe") == false &&
                renderer.shows("trip-lines__road-access-icon") == false
        }
        compose.onMain { assertEquals(true, renderer.shows("trip-lines"), "the itinerary's own line stays shown") }

        // A base map switched to draws the map's content again on it, the layer the app added by hand and the
        // stretches' hidden word included.
        val dark = Url("https://tiles.example.test/v1/terrains/outdoor/dark/style")
        compose.onMain { map.setStyle(dark) }
        compose.until { renderer.map?.style?.uri == dark.toString() && renderer.ownLayers() == drawn }
        compose.onMain { assertEquals(false, renderer.shows("trip-lines__road-access-stripe")) }

        // A base map that does not arrive gives the map back the one shown, its content drawn on it.
        compose.onMain { map.setStyle(Url("https://tiles.example.test/v1/terrains/missing/light/style")) }
        compose.until { map.state.value.style == dark }
        compose.until { renderer.map?.style?.uri == dark.toString() && renderer.ownLayers() == drawn }
    }

    @Test
    fun aPickedResultIsDrawnAboveTheContentOnMapLibresMapAgainAfterASwitchAndTakenOffAtOnce() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain {
            map.addSource(MapSource("trail", MapSource.Kind.GeoJson(pins)))
            map.addLayer(
                MapLayer(
                    id = "route",
                    type = TileSchema.TileLayerType.Line,
                    source = "trail",
                    rank = LayerRank.ItineraryLine.rank,
                ),
            )
            map.setSearchHighlight(
                SearchHighlight.Poi(listOf(6.86, 45.83), DataSchema.POICategory.Peak),
                HexColor("#1c7ed6"),
            )
        }
        val drawn = listOf("route", "mountaya-search-highlight-poi")
        assertEquals(
            drawn,
            compose.onMain {
                map.state.value.layers
                    .map { it.id }
            },
        )
        compose.until { renderer.ownLayers() == drawn }
        compose.onMain {
            val pin = assertIs<SymbolLayer>(renderer.map?.style?.getLayer("mountaya-search-highlight-poi"))
            assertEquals("mountaya-search-highlight", pin.sourceId)
            assertNotNull(renderer.missingImage("poi-icon-mountain-5d4037"), "the pin's sprite")
        }

        // A base map switched to draws the highlight again on it, above the content.
        val dark = Url("https://tiles.example.test/v1/terrains/outdoor/dark/style")
        compose.onMain { map.setStyle(dark) }
        compose.until { renderer.map?.style?.uri == dark.toString() && renderer.ownLayers() == drawn }

        // The latest call wins, and a way's ends read the points alone.
        compose.onMain {
            map.setSearchHighlight(
                SearchHighlight.Way(listOf(6.865, 45.835), null, listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))),
            )
        }
        val way =
            listOf(
                "route",
                "mountaya-search-highlight-halo",
                "mountaya-search-highlight-casing",
                "mountaya-search-highlight-line",
                "mountaya-search-highlight-endpoints",
            )
        compose.until { renderer.ownLayers() == way }
        compose.onMain {
            val ends = assertIs<CircleLayer>(renderer.map?.style?.getLayer("mountaya-search-highlight-endpoints"))
            assertNotNull(ends.filter, "a circle filtered to the points")
        }

        // A clear takes it down at once, its source with it.
        compose.onMain { map.setSearchHighlight(null) }
        compose.until {
            renderer.ownLayers() == listOf("route") &&
                renderer.map?.style?.getSource("mountaya-search-highlight") == null
        }
    }

    @Test
    fun theGeometrySlotsAndADataApiResultAreDrawnAtTheirBandsAndTakenOff() {
        val map = engine()
        val renderer = compose.showing(map)
        val band =
            DataSchema.Isochrone {
                value = 600.0
                geometry =
                    GeoJsonPolygon(
                        listOf(
                            listOf(listOf(6.85, 45.82), listOf(6.87, 45.82), listOf(6.87, 45.84), listOf(6.85, 45.82)),
                        ),
                    )
            }
        compose.onMain {
            drawTrail(map)
            map.setGeometry(GeoJsonLineString(listOf(listOf(6.85, 45.83), listOf(6.87, 45.84))), GeometrySlot.Preview)
            map.setGeometry(GeoJsonPoint(listOf(6.86, 45.83)), GeometrySlot.Highlight)
        }
        val reach = onMainSuspending { map.addIsochrones(DataSchema.IsochronesResult { isochrones = listOf(band) }) }
        val drawn =
            compose.onMain {
                map.state.value.layers
                    .map { it.id }
            }
        compose.until { renderer.ownLayers() == drawn }

        compose.onMain {
            val layers = map.state.value.layers
            val reached = drawn.indices.filter { layers[it].source == "mountaya-${reach.id}" }
            val route = drawn.indexOf("route")
            val preview = drawn.indexOf("mountaya-preview-line")
            val huts = drawn.indexOf("huts")
            val highlight = drawn.indexOf("mountaya-highlight-point")
            assertTrue(reached.isNotEmpty() && reached.all { it < route }, "the result under the route: $drawn")
            assertTrue(route < preview && preview < huts, "the preview just above the route: $drawn")
            assertEquals(drawn.size - 1, highlight, "the highlight above everything: $drawn")

            map.clearGeometry(GeometrySlot.Preview)
            map.removeAnalysis(reach.id)
        }
        val left =
            compose.onMain {
                map.state.value.layers
                    .map { it.id }
            }
        assertFalse("mountaya-preview-line" in left)
        compose.until { renderer.ownLayers() == left }
        compose.onMain { assertNull(renderer.map?.style?.getSource("mountaya-${reach.id}")) }
    }

    /** Every event [map] tells from the moment it is made, collected on the main thread, where the engine tells them. */
    private class Told(
        map: MapEngine,
    ) {
        val events: MutableList<MapEvent> = java.util.Collections.synchronizedList(mutableListOf())
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        init {
            scope.launch(start = CoroutineStart.UNDISPATCHED) { map.events().collect { events += it } }
        }

        /** Stops collecting. */
        fun close() {
            scope.cancel()
        }
    }

    /** Draws a line along 45.83 from 6.85 to 6.87, an itinerary's leg, and a waypoint at its middle, each at its band. */
    private fun drawTrail(map: MapEngine) {
        map.addSource(
            MapSource(
                "trail",
                MapSource.Kind.GeoJson(
                    GeoJsonFeatureCollection(
                        listOf(
                            GeoJsonFeature(
                                GeoJsonLineString(listOf(listOf(6.85, 45.83), listOf(6.87, 45.83))),
                                JsonObject(mapOf("segment_id" to JsonPrimitive("climb"))),
                            ),
                            GeoJsonFeature(
                                GeoJsonPoint(listOf(6.86, 45.83)),
                                JsonObject(
                                    mapOf(
                                        "waypoint_id" to JsonPrimitive("hut"),
                                        "kinds" to JsonArray(listOf(JsonPrimitive("refuge"), JsonPrimitive("water"))),
                                        "beds" to JsonPrimitive(24),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
        map.addLayer(
            MapLayer(
                id = "route",
                type = TileSchema.TileLayerType.Line,
                source = "trail",
                rank = LayerRank.ItineraryLine.rank,
                paint = mapOf("line-width" to JsonPrimitive(12)),
                filter =
                    JsonArray(
                        listOf(
                            JsonPrimitive("=="),
                            JsonArray(listOf(JsonPrimitive("geometry-type"))),
                            JsonPrimitive("LineString"),
                        ),
                    ),
            ),
        )
        map.addLayer(
            MapLayer(
                id = "huts",
                type = TileSchema.TileLayerType.Circle,
                source = "trail",
                rank = LayerRank.ItineraryPoint.rank,
                paint = mapOf("circle-radius" to JsonPrimitive(10)),
                filter =
                    JsonArray(
                        listOf(
                            JsonPrimitive("=="),
                            JsonArray(listOf(JsonPrimitive("geometry-type"))),
                            JsonPrimitive("Point"),
                        ),
                    ),
            ),
        )
    }

    /** Where MapLibre's camera stands, for a message. */
    private fun MapRenderer.shownCamera(): String = map?.cameraPosition.toString()

    @Test
    fun aMoveTheEngineHandsMapLibreLandsWhereTheEngineHoldsItJumpEaseAndFlyAlike() {
        val map = engine()
        val renderer = compose.showing(map)
        val maplibre = assertNotNull(renderer.map, "the map is ready")

        compose.onMain {
            map.ease(CameraTarget(center = listOf(7.0, 46.0), zoom = 12.0))
            assertEquals(12.0, map.state.value.camera.zoom)
        }
        compose.until {
            abs(maplibre.cameraPosition.zoom - 12) < 0.01 &&
                abs((maplibre.cameraPosition.target?.longitude ?: 0.0) - 7.0) < 1e-6
        }
        compose.onMain { map.fly(CameraTarget(center = listOf(6.9, 45.9), zoom = 13.0, bearing = 30.0)) }
        compose.until {
            abs(maplibre.cameraPosition.zoom - 13) < 0.01 &&
                abs(maplibre.cameraPosition.bearing - 30) < 0.01 &&
                abs((maplibre.cameraPosition.target?.latitude ?: 0.0) - 45.9) < 1e-6
        }
        compose.onMain { map.jump(CameraTarget(pitch = 40.0)) }
        compose.until { abs(maplibre.cameraPosition.tilt - 40) < 0.01 }
        compose.onMain {
            assertTrue(
                abs(maplibre.cameraPosition.zoom - 13) < 0.01,
                "a move keeps what it leaves out: ${renderer.shownCamera()}",
            )
            assertTrue(abs(maplibre.cameraPosition.bearing - 30) < 0.01, renderer.shownCamera())
            map.zoomIn()
            assertEquals(14.0, map.state.value.camera.zoom)
        }
        compose.until { abs(maplibre.cameraPosition.zoom - 14) < 0.01 }
        compose.onMain {
            map.resetNorth()
            assertEquals(0.0, map.state.value.camera.bearing)
            assertEquals(0.0, map.state.value.camera.pitch)
        }
        // MapLibre's own reset of north keeps the pitch; the engine's turns it to 0.
        compose.until { abs(maplibre.cameraPosition.bearing) < 0.01 && maplibre.cameraPosition.tilt < 0.01 }
        compose.onMain {
            assertTrue(abs(maplibre.cameraPosition.zoom - 14) < 0.01, renderer.shownCamera())
            assertFailsWith<MapException> { map.ease(CameraTarget(zoom = Double.NaN)) }
        }
    }

    @Test
    fun aFitLandsWhereCameraForBoundsFramesTheBoxOnTheViewsOwnCanvasFacingNorth() {
        val map = engine()
        val renderer = compose.showing(map)
        val maplibre = assertNotNull(renderer.map, "the map is ready")
        val box = listOf(6.8, 45.8, 6.95, 45.9)
        compose.onMain { map.jump(CameraTarget(bearing = 45.0)) }
        compose.until { abs(maplibre.cameraPosition.bearing - 45) < 0.01 }

        val expected =
            compose.onMain {
                map.fitBounds(box, CameraFit(padding = PixelPadding(24.0), maxZoom = 15.0, duration = Duration.ZERO))
                val density = renderer.mapView.resources.displayMetrics.density
                val canvas =
                    listOf(
                        renderer.mapView.width / density.toDouble(),
                        renderer.mapView.height / density.toDouble(),
                    )
                val expected =
                    assertNotNull(
                        cameraForBounds(box, canvas, map.state.value.zoomBounds, PixelPadding(24.0), 15.0),
                        "the bounds fit the canvas",
                    )
                assertTrue(abs(map.state.value.camera.zoom - expected.zoom) < 1e-9, "${map.state.value.camera}")
                assertEquals(0.0, map.state.value.camera.bearing)
                expected
            }
        compose.until {
            val camera = maplibre.cameraPosition
            abs(camera.zoom - expected.zoom) < 1e-3 &&
                abs((camera.target?.longitude ?: 0.0) - expected.center[0]) < 1e-6 &&
                abs((camera.target?.latitude ?: 0.0) - expected.center[1]) < 1e-6 &&
                abs(camera.bearing) < 0.01
        }
        compose.onMain {
            val shown = maplibre.projection.visibleRegion.latLngBounds
            assertTrue(
                shown.longitudeWest <= 6.8 && shown.longitudeEast >= 6.95 && shown.latitudeSouth <= 45.8 &&
                    shown.latitudeNorth >= 45.9,
                "$shown at ${maplibre.cameraPosition.zoom}",
            )
        }
    }

    @Test
    fun aPointQueryAnswersWhatTheMapsOwnLayersDrewThereTopmostFirstAsFeatures() {
        val map = engine()
        compose.showing(map)
        compose.onMain {
            map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 14.0, bearing = 0.0, pitch = 0.0))
            drawTrail(map)
        }
        val point = compose.onMain { map.project(listOf(6.86, 45.83)) }
        assertTrue(point.size == 2 && point.all { it.isFinite() }, "$point")
        compose.until { map.featuresAt(point).size >= 2 }

        compose.onMain {
            val found = map.featuresAt(point)
            assertEquals(listOf("huts", "route"), found.map { it.layer }, "the waypoint above its leg")
            val hut = found.first()
            assertEquals(JsonPrimitive("hut"), hut.properties["waypoint_id"])
            assertEquals(24.0, (hut.properties["beds"] as? JsonPrimitive)?.doubleOrNull)
            assertNull(hut.properties["kinds"], "a property holding a list is left out")
            assertNull(hut.sourceLayer, "a GeoJSON source has no source layer")
            // A rendered feature's geometry is the one its tile holds, cut on the tile's grid at the zoom drawn.
            val drawn = assertIs<GeoJsonPoint>(hut.geometry)
            assertTrue(abs(drawn.coordinates[0] - 6.86) < 1e-5, "${drawn.coordinates}")
            assertEquals(listOf("huts", "route"), map.itinerariesAt(point).map { it.layer })
            assertTrue(map.poisAt(point).isEmpty())
            assertTrue(map.zonesAt(point).isEmpty())
            val back = map.unproject(point)
            assertTrue(abs(back[0] - 6.86) < 1e-6 && abs(back[1] - 45.83) < 1e-6, "$back")
        }
    }

    /**
     * A GeoJSON source's id is answered as its source gives it, a number a number and a text text, though MapLibre
     * Android hands every id back as text (`map.query.feature-shape`): the style's document is where it is read.
     */
    @Test
    fun aGeoJsonSourcesIdIsAnsweredAsItsSourceGivesIt() {
        val map = engine()
        compose.showing(map)
        compose.onMain {
            map.jump(CameraTarget(center = listOf(6.865, 45.83), zoom = 14.0, bearing = 0.0, pitch = 0.0))
            map.addLayer(
                MapLayer(
                    "style-huts-drawn",
                    TileSchema.TileLayerType.Circle,
                    "style-huts",
                    rank = LayerRank.Overlay.rank,
                ),
            )
        }
        val five = compose.onMain { map.project(listOf(6.86, 45.83)) }
        val six = compose.onMain { map.project(listOf(6.87, 45.83)) }
        compose.until { map.featuresAt(five).isNotEmpty() && map.featuresAt(six).isNotEmpty() }

        compose.onMain {
            assertEquals(QueriedFeature.Id.Number(5.0), map.featuresAt(five).first().id)
            assertEquals(QueriedFeature.Id.Text("6"), map.featuresAt(six).first().id)
        }
    }

    /** A hovering mouse's [action] at [point] of the view, in density-independent pixels. */
    private fun MapRenderer.hover(
        action: Int,
        point: List<Double>,
    ) {
        val density = mapView.resources.displayMetrics.density
        val now = SystemClock.uptimeMillis()
        val event =
            MotionEvent.obtain(
                now,
                now,
                action,
                (point[0] * density).toFloat(),
                (point[1] * density).toFloat(),
                0,
            )
        try {
            hovered(event)
        } finally {
            event.recycle()
        }
    }

    @Test
    fun aClickIsToldAndAgainWithTheFeaturesItLandedOnALongClickAndAHoverToo() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain {
            map.jump(CameraTarget(center = listOf(6.86, 45.83), zoom = 14.0, bearing = 0.0, pitch = 0.0))
            drawTrail(map)
        }
        val point = compose.onMain { map.project(listOf(6.86, 45.83)) }
        compose.until { map.featuresAt(point).size >= 2 }
        val told = compose.onMain { Told(map) }

        compose.onMain {
            renderer.clicked(point)
            renderer.longClicked(point)
            renderer.hover(MotionEvent.ACTION_HOVER_MOVE, point)
        }
        // A hover is told at the next frame the platform draws.
        compose.until { synchronized(told.events) { told.events.any { it is MapEvent.FeatureHover } } }
        compose.onMain { renderer.hover(MotionEvent.ACTION_HOVER_EXIT, listOf(-1.0, point[1])) }
        compose.until { told.events.size >= 5 }

        compose.onMain {
            val events = synchronized(told.events) { told.events.toList() }
            told.close()
            assertEquals(5, events.size, "$events")
            val click = assertIs<MapEvent.Click>(events[0]).tap
            assertTrue(abs(click.position[0] - 6.86) < 1e-6 && click.point == point, "$click")
            val featureClick = assertIs<MapEvent.FeatureClick>(events[1])
            assertEquals(click.position, featureClick.position)
            assertEquals(listOf("huts", "route"), featureClick.features.map { it.layer })
            val longClick = assertIs<MapEvent.LongClick>(events[2]).tap
            assertTrue(abs(longClick.position[1] - 45.83) < 1e-6, "$longClick")
            assertEquals(listOf("huts", "route"), assertIs<MapEvent.FeatureHover>(events[3]).features.map { it.layer })
            assertTrue(
                assertIs<MapEvent.FeatureHover>(events[4]).features.isEmpty(),
                "a pointer leaving is told with no feature",
            )
        }
    }

    @Test
    fun aTileOfTheMapsOwnRefusedA402IsToldAsARefusalOfAccessWhichStands() {
        val map = engine()
        compose.showing(map)
        val told = compose.onMain { Told(map) }

        compose.onMain {
            map.addSource(
                MapSource(
                    "refused",
                    MapSource.Kind.Vector(
                        listOf("https://tiles.example.test/v1/refused/{z}/{x}/{y}"),
                        ZoomBounds(0.0, 14.0),
                    ),
                ),
            )
            map.addLayer(
                MapLayer(
                    id = "refused-lines",
                    type = TileSchema.TileLayerType.Line,
                    source = "refused",
                    sourceLayer = "lines",
                    rank = LayerRank.Overlay.rank,
                ),
            )
        }
        compose.until { map.state.value.isAccessRequired }
        compose.until {
            synchronized(told.events) {
                told.events.any {
                    it is MapEvent.AccessRefused && it.status == 402 &&
                        it.url.startsWith("https://tiles.example.test/v1/refused/")
                }
            }
        }
        compose.onMain {
            val events = synchronized(told.events) { told.events.toList() }
            told.close()
            assertTrue(events.none { it is MapEvent.Failure }, "a refusal is never also a failure: $events")
        }
    }

    /**
     * A storm of failures OkHttp's threads hear while the main thread is busy, as a frame being drawn holds it, reaches
     * the engine in one hop, where each failure was a hop of its own: every one told once, each thread's in the order
     * that thread heard them.
     */
    @Test
    fun aStormOfFailuresHeardWhileTheMainThreadIsBusyReachesTheEngineInOneHopEachToldInTheOrderHeard() {
        val map = engine()
        val renderer = compose.showing(map)
        val told = compose.onMain { Told(map) }
        val urls = (0 until 40).map { "https://elsewhere.example.test/fails/$it.pbf" }
        val hopsBefore = compose.onMain { renderer.heard.hops }

        val busy = CountDownLatch(1)
        val held = CountDownLatch(1)
        renderer.mapView.post {
            held.countDown()
            busy.await()
        }
        held.await()
        val threads =
            (0 until 4).map { index ->
                thread {
                    for (url in urls.filterIndexed { at, _ -> at % 4 == index }) {
                        renderer.heard.hear(HeardFailures.Failure(url, 500, false, null))
                    }
                }
            }
        threads.forEach(Thread::join)
        busy.countDown()

        compose.until { synchronized(told.events) { told.events.count { it is MapEvent.Failure } } >= urls.size }
        compose.onMain {
            val failures = synchronized(told.events) { told.events.filterIsInstance<MapEvent.Failure>() }
            told.close()
            assertEquals(urls.toSet(), failures.map { it.url }.toSet())
            assertEquals(urls.size, failures.size, "each failure told once")
            assertTrue(failures.all { it.status == 500 && it.cause == null }, "$failures")
            for (index in 0 until 4) {
                val its = urls.filterIndexed { at, _ -> at % 4 == index }
                assertEquals(its, failures.map { it.url }.filter { it in its }, "thread $index's order")
            }
            val hops = renderer.heard.hops - hopsBefore
            assertTrue(hops <= 1, "one hop for the storm: $hops")
        }
    }

    @Test
    fun theMapCreditsMountayaFirstThenEachSourceItDrawsAndItsButtonListsThem() {
        val map = engine()
        val renderer = compose.showing(map)
        val elsewhere = "<a href=\"https://elsewhere.example.test/\" target=\"_blank\">&copy; Elsewhere</a>"

        compose.onMain {
            assertEquals(listOf(MOUNTAYA_CREDIT, elsewhere), map.credits)
            map.addSource(MapSource("trails", MapSource.Kind.GeoJson(pins), attribution = "&copy; Trails of the Alps"))
            assertEquals(listOf(MOUNTAYA_CREDIT, elsewhere), map.credits, "a source no layer draws is not credited")
            map.addLayer(
                MapLayer(
                    id = "trail-lines",
                    type = TileSchema.TileLayerType.Line,
                    source = "trails",
                    rank = LayerRank.ItineraryLine.rank,
                ),
            )
            assertEquals(listOf(MOUNTAYA_CREDIT, elsewhere, "&copy; Trails of the Alps"), map.credits)

            val button = assertNotNull(renderer.map, "the map is ready").uiSettings.attributionDialogManager
            assertIs<MapRenderer.Credits>(button, "the credit button opens the map's own credits")
            // A dialog lays its rows out as it is created.
            val sheet = creditSheet(renderer.mapView.context, map.credits) {}.create()
            sheet.create()
            val rows = sheet.listView.adapter as ArrayAdapter<*>
            assertEquals(
                listOf("\u00A9 Mountaya", "\u00A9 Elsewhere", "\u00A9 Trails of the Alps"),
                (0 until rows.count).map { rows.getItem(it).toString() },
            )
        }
    }

    @Test
    fun aDataElementsCursorMarkerIsThePinsDotAndTheLegItPointsAtTheEmphasisLine() {
        val map = engine()
        val renderer = compose.showing(map)
        val leg = GeoJsonLineString(listOf(listOf(6.85, 45.83), listOf(6.87, 45.84)))
        val line = GeometrySlot.Emphasis.sourceId + "-line"
        val link = MapLink<Int>(PROFILE_CURSOR_MARKER_ID)
        compose.onMain { link.write(MapTarget(listOf(6.86, 45.835), leg), map, null, null) }

        compose.until { renderer.markerPins().size == 1 && renderer.map?.style?.getLayer(line) is LineLayer }
        compose.onMain {
            val cursor = renderer.markerPins().single()
            assertEquals(PROFILE_CURSOR_MARKER_ID, cursor.marker)
            assertEquals(
                CURSOR_MARKER_GLYPH,
                map.state.value.markers
                    .single()
                    .glyph,
            )
            val dot =
                markerPinImage(
                    describeMarkerPin(null, null, map.glyphs),
                    HexColor("#1c7ed6"),
                    map.glyphs,
                    renderer.mapView.resources.displayMetrics.densityDpi,
                )
            assertTrue(cursor.image.sameAs(dot), "the marker set draws no filled pin, so the marker is the pin's dot")
            assertTrue(pixelAt(cursor.image, 14.0, 14.0).isNear(Pixels.white))
            assertEquals(leg, map.geometry(GeometrySlot.Emphasis).geometry)

            link.write(MapTarget(), map, null, null)
        }
        compose.until { renderer.markerPins().isEmpty() && renderer.map?.style?.getLayer(line) == null }
        compose.onMain { assertNull(map.geometry(GeometrySlot.Emphasis).geometry) }
    }

    /** Every view under this one, this one first, depth first. */
    private fun View.descendants(): Sequence<View> =
        sequence {
            yield(this@descendants)
            if (this@descendants is ViewGroup) {
                for (index in 0 until childCount) yieldAll(getChildAt(index).descendants())
            }
        }

    /**
     * MapLibre's own compass shows on a map turned off north, beside the one `MapControls` draws, and its credit
     * button sits bottom-left: the renderer turns the compass off and puts the credit button bottom-right, where
     * `sdk-swift` and `sdk-dart` put theirs.
     */
    @Test
    fun mapLibresOwnCompassStaysHiddenOnATurnedMapAndTheCreditButtonSitsBottomRight() {
        val map = engine()
        val renderer = compose.showing(map)
        compose.onMain { map.jump(CameraTarget(bearing = 90.0)) }
        compose.until { abs((renderer.map?.cameraPosition?.bearing ?: 0.0) - 90) < 0.01 }
        compose.waitForIdle()

        compose.onMain {
            val view = renderer.mapView
            val compasses = view.descendants().filter { it.javaClass.simpleName == "CompassView" }.toList()
            assertTrue(compasses.isNotEmpty(), "MapLibre's view holds its compass")
            assertTrue(compasses.none { it.isShown && it.alpha > 0f }, "MapLibre's compass shows beside the controls'")
            // The credit button is the one image MapLibre's view presses besides its compass.
            val credit =
                (0 until view.childCount)
                    .map(view::getChildAt)
                    .filterIsInstance<ImageView>()
                    .single { it.isClickable && it !in compasses }
            assertTrue(credit.isShown, "the credit button shows")
            assertTrue(credit.left > view.width / 2, "the credit button sits at ${credit.left} of ${view.width}")
            assertTrue(credit.top > view.height / 2, "the credit button sits at ${credit.top} of ${view.height}")
        }
    }

    /**
     * MapLibre's credit button and its logo, the two images its view shows beside its compass: the one it presses and
     * the one it does not.
     */
    private fun MapRenderer.ornaments(): Pair<ImageView, ImageView> {
        val images =
            (0 until mapView.childCount)
                .map(mapView::getChildAt)
                .filterIsInstance<ImageView>()
                .filter { it.javaClass.simpleName != "CompassView" }
        return images.single { it.isClickable } to images.single { !it.isClickable }
    }

    /**
     * MapLibre lays its credit button and its logo out from its view's edges and applies no window inset, so a system
     * bar over a map drawn edge to edge would cover them: the renderer keeps both 4 dp inside the map's safe area, the
     * button in its bottom-right corner and the logo in its bottom-left one, and moves them as the safe area moves, as
     * `sdk-swift`'s MapLibre anchors its ornaments to the safe area and `sdk-dart` places its credits inside it.
     */
    @Test
    fun theCreditButtonAndTheLogoStandFourInsideTheSafeArea() {
        val map = engine()
        val renderer = compose.showing(map)
        val (credit, logo) = compose.onMain { renderer.ornaments() }

        // A safe area inset on every edge, a display cutout down the left, the status bar over the top and the
        // navigation bar down the right and along the bottom, then one that no inset reaches.
        for (area in listOf(listOf(32.0, 24.0, 342.0, 796.0), listOf(0.0, 0.0, 390.0, 844.0))) {
            compose.onMain { renderer.safeArea = area }
            compose.until { !credit.isLayoutRequested && !logo.isLayoutRequested }
            compose.onMain {
                val view = renderer.mapView
                val density = view.resources.displayMetrics.density
                val right = view.width / density - area[2] + 4
                val bottom = view.height / density - area[3] + 4
                assertTrue(credit.isShown && logo.isShown, "the credit button and the logo show in $area")
                val creditRight = (view.width - credit.right) / density
                val creditBottom = (view.height - credit.bottom) / density
                val logoLeft = logo.left / density
                val logoBottom = (view.height - logo.bottom) / density
                assertTrue(abs(creditRight - right) < 1, "the credit button is $creditRight from the right in $area")
                assertTrue(abs(creditBottom - bottom) < 1, "the credit button is $creditBottom from the foot in $area")
                assertTrue(abs(logoLeft - (area[0] + 4)) < 1, "the logo is $logoLeft from the left in $area")
                assertTrue(abs(logoBottom - bottom) < 1, "the logo is $logoBottom from the bottom in $area")
            }
        }
    }
}
