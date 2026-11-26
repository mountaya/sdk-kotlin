package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ItineraryWaypointsTest {
    /** The wrapper the API stores a waypoint's point in: a collection of one Point. */
    private fun stored(
        longitude: Double,
        latitude: Double,
        altitude: Double = 1_035.0,
    ): GeoJsonGeometry = collectionOf(pointAt(longitude, latitude, altitude))

    /** The details of the one place a waypoint standing at 6, 45 reads as, its [waypoint] stated. */
    private fun details(waypoint: RestSchema.ItineraryWaypoint): WaypointDetails? =
        itineraryWaypoints(listOf(waypoint)).single().details

    private fun at(
        amenities: List<RestSchema.WaypointAmenity> = emptyList(),
        description: String? = null,
        timing: RestSchema.WaypointTiming? = null,
        contact: RestSchema.WaypointContact? = null,
        media: RestSchema.WaypointMedia? = null,
        properties: List<RestSchema.WaypointProperty>? = null,
    ) = restPlace(
        "w",
        geojson = stored(6.0, 45.0),
        amenities = amenities,
        description = description,
        timing = timing,
        contact = contact,
        media = media,
        properties = properties,
    )

    /**
     * The API stores every waypoint's point in a collection: a reader that matched a Point alone would
     * draw no waypoint of any real itinerary, and say nothing.
     */
    @Test
    fun aWaypointStandsAtTheFirstPointItsGeometryHoldsItsAltitudeLeftOut() {
        val places =
            itineraryWaypoints(
                listOf(
                    restPlace("nowhere", position = 0, geojson = null),
                    restPlace("line", position = 1, geojson = lineString(listOf(6.0, 45.0), listOf(6.1, 45.0))),
                    restPlace(
                        "real",
                        position = 2,
                        geojson =
                            collectionOf(
                                lineString(listOf(7.0, 46.0), listOf(7.1, 46.0)),
                                pointAt(6.0, 45.0, 1_200.0),
                                pointAt(8.0, 47.0),
                            ),
                    ),
                ),
            )

        assertEquals(listOf(ItineraryPlace("real", listOf(6.0, 45.0))), places)
    }

    @Test
    fun aBarePointIsReadTooSoAPlaceACallerBuildsIsNotTurnedAway() {
        val places = itineraryWaypoints(listOf(restPlace("bare", geojson = pointAt(6.87, 45.92))))

        assertEquals(listOf(listOf(6.87, 45.92)), places.map { it.lngLat })
    }

    @Test
    fun aPointIsFoundAtAnyDepthOfTheCollectionsItNestsIn() {
        val nested =
            collectionOf(
                collectionOf(lineString(listOf(7.0, 46.0), listOf(7.1, 46.0))),
                collectionOf(collectionOf(pointAt(6.0, 45.0))),
            )

        assertEquals(
            listOf(listOf(6.0, 45.0)),
            itineraryWaypoints(listOf(restPlace("deep", geojson = nested))).map {
                it.lngLat
            },
        )
    }

    /** A MultiPoint is a directions segment's vias, never the one place a waypoint is. */
    @Test
    fun aMultiPointIsNoPointAndAWaypointHoldingOnlyOneIsLeftOut() {
        val vias = GeoJsonMultiPoint(listOf(listOf(6.0, 45.0)))

        assertTrue(itineraryWaypoints(listOf(restPlace("vias", geojson = vias))).isEmpty())
    }

    @Test
    fun aPointThatNamesNoPlaceIsPassedOverForTheNextOneAndNoneIsNoPlace() {
        val places =
            itineraryWaypoints(
                listOf(
                    restPlace(
                        "next",
                        position = 0,
                        geojson = collectionOf(pointAt(Double.NaN, 45.0), pointAt(6.0, 45.0)),
                    ),
                    restPlace("short", position = 1, geojson = pointAt(6.0)),
                    restPlace("infinite", position = 2, geojson = pointAt(6.0, Double.POSITIVE_INFINITY)),
                ),
            )

        assertEquals(listOf(ItineraryPlace("next", listOf(6.0, 45.0))), places)
    }

    /**
     * A position is the API's ordinal: nothing on a waypoint states a distance, so it is placed by its
     * point.
     */
    @Test
    fun theWaypointsReadInChainOrderTwoAtOnePositionAsListed() {
        val places =
            itineraryWaypoints(
                listOf(
                    restPlace("finish", position = 2, geojson = stored(6.86, 45.83), name = "Chamonix"),
                    restPlace("start", position = 0, geojson = stored(6.86, 45.83), name = "Les Houches"),
                    restPlace("hut", position = 1, geojson = stored(6.86, 45.83), name = "Refuge de Miage"),
                    restPlace("hut-2", position = 1, geojson = stored(6.86, 45.83)),
                ),
            )

        assertEquals(listOf("start", "hut", "hut-2", "finish"), places.map { it.id })
        assertEquals(listOf("Les Houches", "Refuge de Miage", null, "Chamonix"), places.map { it.name })
        assertTrue(places.all { it.lngLat == listOf(6.86, 45.83) })
    }

    @Test
    fun theColourTheIconAsTheGlyphAndTheNameAreCarriedAsTheWireStatesThem() {
        val places =
            itineraryWaypoints(
                listOf(
                    restPlace(
                        "summit",
                        position = 0,
                        geojson = stored(6.86, 45.83),
                        name = "Mont Joly",
                        color = "#e03131",
                        icon = RestSchema.WaypointIcon("summit"),
                    ),
                    restPlace("bare", position = 1, geojson = stored(6.86, 45.83)),
                ),
            )

        assertEquals(
            listOf(
                ItineraryPlace(
                    "summit",
                    listOf(6.86, 45.83),
                    "Mont Joly",
                    "#e03131",
                    RestSchema.WaypointIcon("summit"),
                ),
                ItineraryPlace("bare", listOf(6.86, 45.83)),
            ),
            places,
        )
    }

    @Test
    fun aWaypointThatSaysNothingAboutItsPlaceHasNoDetails() {
        assertNull(details(at()))
    }

    @Test
    fun aBlockLeftWithNothingReadsAsNoneAsDoesDetailsLeftWithNothing() {
        val bare =
            at(
                contact =
                    RestSchema.WaypointContact {
                        address = null
                        booking_url = null
                        email = null
                        opening_hours = null
                        phone = null
                        website = null
                    },
                media =
                    RestSchema.WaypointMedia {
                        links = emptyList()
                        photos = emptyList()
                    },
                timing =
                    RestSchema.WaypointTiming {
                        cutoff_at = null
                        stop_duration = null
                    },
                properties = emptyList(),
            )

        assertNull(details(bare))
    }

    @Test
    fun aListWrittenNullAndAListAbsentReadAlikeAsNone() {
        assertNull(details(at(media = RestSchema.WaypointMedia { links = null })))
    }

    @Test
    fun theServicesAreCarriedWhereTheWireListsSome() {
        val water = RestSchema.WaypointAmenity.Water
        val meal = RestSchema.WaypointAmenity.HotMeal

        assertEquals(WaypointDetails(amenities = listOf(water, meal)), details(at(amenities = listOf(water, meal))))
    }

    @Test
    fun aContactKeepsTheMembersTheWireStatesEachALineOfItsOwn() {
        val website = Url("https://refuge.example")
        val reached =
            at(
                contact =
                    RestSchema.WaypointContact {
                        address = null
                        phone = "+33 4 50 47 77 70"
                        this.website = website
                    },
            )

        assertEquals(
            WaypointDetails(
                contact = WaypointDetails.Contact(phone = "+33 4 50 47 77 70", website = website.toString()),
            ),
            details(reached),
        )
    }

    @Test
    fun theTimingIsReadOntoItsOwnNamesABlockOfNullsReadingAsNone() {
        val cutoff = Instant.fromEpochSeconds(1_782_885_600)
        val stop =
            at(
                timing =
                    RestSchema.WaypointTiming {
                        cutoff_at = null
                        stop_duration = 900
                    },
            )
        val timed =
            at(
                timing =
                    RestSchema.WaypointTiming {
                        cutoff_at = cutoff
                        stop_duration = 900
                    },
            )

        assertEquals(WaypointDetails(timing = WaypointDetails.Timing(stopDuration = 900)), details(stop))
        assertEquals(WaypointDetails(timing = WaypointDetails.Timing(cutoff, 900)), details(timed))
    }

    @Test
    fun theMediaKeepEveryPhotoAndLinkTheWireStatesEachListNoneWhereItIsEmpty() {
        val fountain = Url("https://example.test/fountain.jpg")
        val bench = Url("https://example.test/bench.jpg")
        val timetable = Url("https://example.test/timetable")
        val shown =
            at(
                media =
                    RestSchema.WaypointMedia {
                        links = listOf(RestSchema.WaypointLink(label = "Timetable", url = timetable))
                        photos =
                            listOf(
                                RestSchema.WaypointPhoto(url = fountain) { caption = "The fountain" },
                                RestSchema.WaypointPhoto(url = bench) { caption = null },
                            )
                    },
            )
        val linked =
            at(
                media =
                    RestSchema.WaypointMedia {
                        links = listOf(RestSchema.WaypointLink(label = "Timetable", url = timetable))
                        photos = emptyList()
                    },
            )

        assertEquals(
            WaypointDetails(
                media =
                    WaypointDetails.Media(
                        photos =
                            listOf(
                                WaypointDetails.Photo(fountain.toString(), "The fountain"),
                                WaypointDetails.Photo(bench.toString()),
                            ),
                        links = listOf(WaypointDetails.Link("Timetable", timetable.toString())),
                    ),
            ),
            details(shown),
        )
        assertEquals(
            WaypointDetails(
                media = WaypointDetails.Media(links = listOf(WaypointDetails.Link("Timetable", timetable.toString()))),
            ),
            details(linked),
        )
    }

    /** The order lines a column up across waypoints, and a field with no format reads as text. */
    @Test
    fun theFieldsKeepTheOrderTheyAreShownInEachWithItsFormatWhereItStatesOne() {
        val duration = RestSchema.WaypointPropertyFormat.Duration
        val fielded =
            at(
                properties =
                    listOf(
                        RestSchema.WaypointProperty(key = "bib-check", label = "Bib check", value = "Mandatory") {
                            format =
                                null
                        },
                        RestSchema.WaypointProperty(key = "drop-bag", label = "Drop bag", value = "3600") {
                            format =
                                duration
                        },
                    ),
            )

        assertEquals(
            WaypointDetails(
                properties =
                    listOf(
                        WaypointDetails.Property("bib-check", "Bib check", "Mandatory"),
                        WaypointDetails.Property("drop-bag", "Drop bag", "3600", duration),
                    ),
            ),
            details(fielded),
        )
    }

    /** Text is text: an empty note is what its author wrote, and a card decides what to show. */
    @Test
    fun aNoteIsCarriedAsWrittenAnEmptyOneIncluded() {
        assertEquals("A bench by the fountain.", details(at(description = "A bench by the fountain."))?.description)
        assertEquals(WaypointDetails(description = ""), details(at(description = "")))
    }

    @Test
    fun anItineraryOfNoWaypointHasNoPlace() {
        assertTrue(itineraryWaypoints(emptyList()).isEmpty())
    }
}
