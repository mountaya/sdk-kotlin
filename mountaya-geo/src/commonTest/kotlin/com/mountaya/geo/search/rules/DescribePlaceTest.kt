package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class DescribePlaceTest {
    private val english = SearchWording()

    @Test
    fun aPlaceStatesItsCategoryRegionAndCountry() {
        assertEquals("Populated place", describePlace(SearchEntity.place(), english))
        assertEquals(
            "Populated place · Auvergne-Rhône-Alpes · France 🇫🇷",
            describePlace(
                SearchEntity.place(region = "Auvergne-Rhône-Alpes", country = "FR", countryName = "France"),
                english,
            ),
        )
        assertEquals(
            "Country",
            describePlace(SearchEntity.place(name = "France", category = "COUNTRY", region = "x"), english),
        )
        assertEquals(
            "1 Rue Royale",
            describePlace(SearchEntity.place(name = "Home", category = "ADDRESS", label = "1 Rue Royale"), english),
        )
    }

    @Test
    fun aPoiStatesItsElevationWhereItIsMeaningfulElseItsDistance() {
        assertEquals("Hut · 3835 m", describePoi(SearchEntity.poi(elevation = 3835.0, distance = 1200.0), english))
        assertEquals("Hut · 1.2 km", describePoi(SearchEntity.poi(elevation = 12.0, distance = 1200.0), english))
        assertEquals(
            "Hut · Chamonix",
            describePoi(SearchEntity.poi(distance = 3.0, city = "Chamonix", region = "Haute-Savoie"), english),
        )
        assertEquals(
            "Refuge · 1.2 km",
            describePoi(
                SearchEntity.poi(distance = 1200.0),
                SearchWording(resolveLabel = {
                    "Refuge"
                }),
            ),
        )
    }

    @Test
    fun aDistanceRoundsToTenMetersThenToATenthOfAKilometre() {
        assertEquals("850 m", distanceToken(850.0, null))
        assertEquals("990 m", distanceToken(994.0, null))
        assertEquals("1.0 km", distanceToken(996.0, null))
        assertEquals("1.0 km", distanceToken(995.0, null))
        assertEquals("10 m", distanceToken(5.0, null))
        assertEquals("", distanceToken(4.9, null))
        // Half away from zero on the decimal value: 1,150 m is 1.15 km, which reads 1.2.
        assertEquals("1.2 km", distanceToken(1150.0, null))
    }

    @Test
    fun aDistanceInTheReadersUnitsIsWrittenInTheirSystemAndDigits() {
        val french = UnitPreferences(language = "fr")
        assertEquals(unitPatterns("fr").meter.replace("{0}", "850"), distanceToken(850.0, french))
        assertEquals(unitPatterns("fr").kilometer.replace("{0}", "1,2"), distanceToken(1200.0, french))
        val imperial = UnitPreferences(language = "en", length = RestSchema.UnitLength.Imperial)
        // 300 m is 984.25 ft, which reads 980.
        assertEquals("980 ft", distanceToken(300.0, imperial))
        assertEquals("0.2 mi", distanceToken(305.0, imperial))
        assertEquals("1,531 m", elevationToken(1531.0, UnitPreferences(language = "en")))
    }

    @Test
    fun aWayStatesItsTagItsGradeOrSurfaceAndWhereItIs() {
        assertEquals("Path", describeWay(SearchEntity.way(), english))
        assertEquals(
            "Path · Mountain hiking",
            describeWay(SearchEntity.way(sacScale = "mountain_hiking", surface = "gravel"), english),
        )
        assertEquals(
            "Path · gravel surface",
            describeWay(SearchEntity.way(sacScale = "hiking", surface = "gravel"), english),
        )
        assertEquals(
            "Path · Schotter",
            describeWay(
                SearchEntity.way(surface = "gravel"),
                SearchWording(resolveLabel = {
                    if (it is SearchLabel.Surface) "Schotter" else null
                }),
            ),
        )
    }

    @Test
    fun aPublishedLineReadsInEnglishTitlesAndMetricFiguresWhereItIsGivenNoWording() {
        assertEquals(
            "Populated place \u00B7 France \uD83C\uDDEB\uD83C\uDDF7",
            describePlace(SearchEntity.place(name = "Lyon", country = "FR", countryName = "France")),
            "a place with no region",
        )
        assertEquals(
            "Populated place",
            describePlace(SearchEntity.place(name = "Annecy", region = "annecy")),
            "a region that repeats the title",
        )
        assertEquals(
            "Valais \u00B7 Suisse \uD83C\uDDE8\uD83C\uDDED",
            describePlace(SearchEntity.place(name = "", region = "Valais", country = "CH", countryName = "Suisse")),
            "an unnamed place titled by what it is",
        )
        assertEquals(
            "12 Rue du Lac, Annecy \u00B7 France \uD83C\uDDEB\uD83C\uDDF7",
            describePlace(
                SearchEntity.place(
                    name = "12 Rue du Lac",
                    category = "ADDRESS",
                    label = "12 Rue du Lac, Annecy",
                    country = "FR",
                    countryName = "France",
                ),
            ),
        )
        assertEquals("200 m", describePoi(SearchEntity.poi(name = "", distance = 200.0)))
        assertEquals("200 m", describePoi(SearchEntity.poi(name = "Hut", distance = 200.0)))
        assertEquals("Hut \u00B7 100 m", describePoi(SearchEntity.poi(elevation = 100.0, distance = 350.0)))
        assertEquals("Hut \u00B7 350 m", describePoi(SearchEntity.poi(elevation = 99.0, distance = 350.0)))
        assertEquals("gravel surface", describeWay(SearchEntity.way(name = "", surface = "gravel")))
    }

    @Test
    fun aFigureInTheReadersImperialUnitsReadsInMilesFeetToTheNearestTenOrFeet() {
        val imperial =
            SearchWording(UnitPreferences(language = "en", length = RestSchema.UnitLength.Imperial))
        assertEquals("Hut \u00B7 2.6 mi", describePoi(SearchEntity.poi(name = "P1", distance = 4200.0), imperial))
        assertEquals("Hut \u00B7 450 ft", describePoi(SearchEntity.poi(name = "P1", distance = 137.0), imperial))
        assertEquals("Hut \u00B7 8,146 ft", describePoi(SearchEntity.poi(elevation = 2483.0), imperial))
    }
}
