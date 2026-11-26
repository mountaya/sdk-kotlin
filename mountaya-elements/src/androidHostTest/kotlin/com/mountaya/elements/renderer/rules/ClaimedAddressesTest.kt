package com.mountaya.elements

import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.ZoomBounds
import kotlin.test.Test
import kotlin.test.assertEquals

/** What a map claims of the addresses its requests go to, which the process's request hook tells an answer by. */
class ClaimedAddressesTest {
    private val zoom = ZoomBounds(0.0, 14.0)

    private fun state(
        style: Url?,
        vararg sources: MapSource,
    ): MapState =
        MapState(
            style,
            CameraPose(listOf(6.86, 45.83), 12.0, 0.0, 0.0),
            RestSchema.Language.Fr,
            sources = sources.toList(),
        )

    @Test
    fun aMapClaimsItsSourcesTilesAndWhatSitsBesideItsStyle() {
        val claimed =
            claimedAddresses(
                state(
                    Url("https://tiles.example.test/v1/terrains/outdoor/light/style"),
                    MapSource(
                        "zones",
                        MapSource.Kind.Vector(listOf("https://tiles.example.test/v1/zones/{z}/{x}/{y}"), zoom),
                    ),
                    MapSource(
                        "shade",
                        MapSource.Kind.Raster(
                            listOf("https://a.example.test/{z}/{x}/{y}.png", "https://b.example.test/one.png"),
                            256,
                            zoom,
                        ),
                    ),
                    MapSource("mine", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))),
                ),
            )

        assertEquals(
            listOf(
                "https://tiles.example.test/v1/zones/",
                "https://a.example.test/",
                "https://b.example.test/one.png",
                "https://tiles.example.test/v1/terrains/outdoor/light/",
            ),
            claimed,
        )
    }

    /**
     * Cut before the query and the fragment, then at the path's last `/`, as `sdk-swift` and `sdk-dart` cut it: a
     * style of no path claims nothing by its directory, where a style at the root claims its host.
     */
    @Test
    fun aStylesDirectoryLeavesItsQueryAndFragmentOutAndAStyleOfNoPathClaimsNothing() {
        assertEquals(
            listOf("https://maps.example.test/styles/"),
            claimedAddresses(state(Url("https://maps.example.test/styles/basic.json?key=a/b#c/d"))),
        )
        assertEquals(emptyList(), claimedAddresses(state(Url("https://maps.example.test"))))
        assertEquals(emptyList(), claimedAddresses(state(Url("https://maps.example.test?key=a/b"))))
        assertEquals(
            listOf("https://maps.example.test/"),
            claimedAddresses(state(Url("https://maps.example.test/style.json"))),
        )
        assertEquals(emptyList(), claimedAddresses(state(null)))
    }

    @Test
    fun aTemplateThatOpensOnAPlaceholderClaimsNothing() {
        val opening = MapSource("any", MapSource.Kind.Vector(listOf("{scheme}://tiles.example.test/{z}/{x}/{y}"), zoom))

        assertEquals(emptyList(), tilePrefixes(opening))
    }
}
