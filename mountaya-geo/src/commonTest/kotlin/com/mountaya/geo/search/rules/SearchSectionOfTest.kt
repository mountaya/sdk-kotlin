package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchSectionOfTest {
    @Test
    fun eachBranchFillsItsSectionFromItsOwnMemberOfTheAnswerInTheAnswersOrder() {
        val english = SearchWording()
        for ((branch, kind) in listOf(
            DataSchema.SearchTarget.Places to "place",
            DataSchema.SearchTarget.Pois to "poi",
            DataSchema.SearchTarget.Ways to "way",
            DataSchema.SearchTarget.Itineraries to "itinerary",
            DataSchema.SearchTarget.Collections to "collection",
            DataSchema.SearchTarget.Zones to "zone",
        )) {
            val section =
                searchSectionOf(
                    HeldSearch.result(branch, listOf("Mont Blanc", "Mont Joly")),
                    branch,
                    searchNeedleOf("mont"),
                    english,
                )
            assertEquals(branch, section.id)
            assertTrue(section.state.isSettled)
            assertEquals(listOf("Mont Blanc", "Mont Joly"), section.rows.map { it.title }, kind)
            assertTrue(section.rows.all { it.id.startsWith("$kind:") })
            assertEquals(
                1,
                section.rows
                    .first()
                    .matched.size,
                "the query emphasized",
            )
        }
    }

    @Test
    fun aBranchReadsItsOwnMemberAloneAndNoneWhereTheAnswerHoldsNone() {
        val places = HeldSearch.result(DataSchema.SearchTarget.Places, listOf("Chamonix"))
        assertTrue(
            searchSectionOf(places, DataSchema.SearchTarget.Zones, searchNeedleOf(""), SearchWording()).rows.isEmpty(),
        )
        assertTrue(
            searchSectionOf(
                DataSchema.SearchResult(),
                DataSchema.SearchTarget.Places,
                searchNeedleOf(""),
                SearchWording(),
            ).state.isSettled,
        )
        val unknown = searchSectionOf(places, DataSchema.SearchTarget("TRAILS"), searchNeedleOf(""), SearchWording())
        assertTrue(unknown.rows.isEmpty() && unknown.state.isSettled)
    }

    @Test
    fun anErrorTheAnswerReportsForTheBranchWinsOverWhatCameWithIt() {
        val pois = HeldSearch.result(DataSchema.SearchTarget.Pois, listOf("Refuge"))
        val answer =
            DataSchema.SearchResult {
                this.pois = pois.pois
                errors =
                    listOf(
                        DataSchema.SearchTargetError {
                            target = DataSchema.SearchTarget.Zones
                            message = "zones failed"
                        },
                        DataSchema.SearchTargetError {
                            target = DataSchema.SearchTarget.Pois
                            code = DataSchema.ErrorCode.BadGateway
                            message = "pois failed"
                        },
                    )
            }
        val section = searchSectionOf(answer, DataSchema.SearchTarget.Pois, searchNeedleOf(""), SearchWording())
        assertTrue(section.rows.isEmpty())
        assertEquals(
            SearchBranchException(DataSchema.SearchTarget.Pois, DataSchema.ErrorCode.BadGateway, "pois failed"),
            section.state.failure,
        )
        assertTrue(
            searchSectionOf(
                answer,
                DataSchema.SearchTarget.Places,
                searchNeedleOf(""),
                SearchWording(),
            ).state.isSettled,
            "another branch's report is not this one's",
        )
    }

    @Test
    fun aResultThatRendersAsNoRowIsLeftOutAndARowWordedForTheReader() {
        val answer =
            DataSchema.SearchResult {
                places =
                    DataSchema.GeocodeResult {
                        places =
                            listOf(
                                SearchEntity.place(name = "Annecy"),
                                SearchEntity.place(name = "Nowhere", point = null),
                            )
                    }
            }
        val section =
            searchSectionOf(
                answer,
                DataSchema.SearchTarget.Places,
                searchNeedleOf(""),
                SearchWording(resolveLabel = {
                    "Localité"
                }),
            )
        assertEquals(listOf("Annecy"), section.rows.map { it.title })
        assertEquals(SearchRowSecondary.Text("Localité"), section.rows.first().secondary)
    }
}
