package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.LinkAnnotation
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.ItineraryPlace
import com.mountaya.geo.WaypointDetails
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WaypointCardTest {
    @get:Rule
    val compose = createComposeRule()

    /** The details of a place recording its services, its contact and a link, its website no web address. */
    private val details =
        WaypointDetails(
            amenities =
                listOf(
                    RestSchema.WaypointAmenity.Water,
                    RestSchema.WaypointAmenity.HotMeal,
                ),
            contact = WaypointDetails.Contact(phone = "+33 4 50 00 00 00", website = "javascript:alert(1)"),
            media =
                WaypointDetails.Media(
                    links = listOf(WaypointDetails.Link("Guide", "https://refuge.example/guide")),
                ),
        )

    /** A hut recording [details], in the card's own colour where it names none, [color]. */
    private fun hut(color: String? = null): ItineraryPlace =
        ItineraryPlace(
            "hut",
            listOf(6.0, 45.005),
            name = "Refuge",
            color = color,
            glyph = RestSchema.WaypointIcon.Bed,
            details = details,
        )

    /** The addresses the text of the node anchors, each a link at a URL. */
    private fun SemanticsNodeInteraction.addresses(): List<String> =
        fetchSemanticsNode()
            .config
            .getOrElse(SemanticsProperties.Text) { emptyList() }
            .flatMap { text -> text.getLinkAnnotations(0, text.length) }
            .mapNotNull { (it.item as? LinkAnnotation.Url)?.url }

    @Test
    fun theCardIsOnThePanelsGroundItsMarkThePlacesColourTheCardsOwnByDefault() {
        compose.setContent {
            Column {
                Drawn("own") { WaypointCard(hut()) }
                Drawn("coloured") { WaypointCard(hut("#e03131")) }
                Drawn("dark", dark = true) { WaypointCard(hut()) }
            }
        }
        val own = compose.image("own")
        val coloured = compose.image("coloured")
        val dark = compose.image("dark")
        val bounds = compose.onNodeWithTag("own").getBoundsInRoot()
        val middle = (bounds.right - bounds.left).value / 2.0

        // Inside the mark's disc, clear of its glyph.
        assertTrue(compose.pixel(own, 15.0, 28.0).isNear(color("#3b82f6")))
        assertTrue(compose.pixel(coloured, 15.0, 28.0).isNear(color("#e03131")))
        assertTrue(compose.pixel(own, middle, 4.0).isNear(color("#ffffff")))
        assertTrue(compose.pixel(dark, middle, 4.0).isNear(color("#25262b")))
    }

    @Test
    fun aPlaceRecordingNothingDrawsItsOneLineShorterThanAPlacesSections() {
        compose.setContent {
            Column {
                Drawn("full") { WaypointCard(hut()) }
                Drawn("bare") { WaypointCard(ItineraryPlace("spring", listOf(6.0, 45.015))) }
            }
        }
        val full = compose.onNodeWithTag("full").getBoundsInRoot()
        val bare = compose.onNodeWithTag("bare").getBoundsInRoot()

        assertTrue(bare.bottom - bare.top < full.bottom - full.top, "$bare against $full")
        assertTrue((full.right - full.left).value >= 240f, "a card is a panel at least")
        compose.onAllNodesWithText("Nothing recorded yet").assertCountEquals(1)
    }

    @Test
    fun theNameAndEachSectionAreReadAsHeadingsALinkAnchoredOnlyAtAWebAddress() {
        compose.setContent { WaypointCard(hut()) }

        compose.onNodeWithText("Refuge").assert(isHeading())
        // The line under the name says it is equipped, in the word heading the services.
        compose
            .onAllNodesWithText("Services")
            .assertCountEquals(2)
            .filter(isHeading())
            .assertCountEquals(1)
        compose.onNodeWithText("Contact").assert(isHeading())
        compose.onNodeWithText("Photos and links").assert(isHeading())
        assertEquals(listOf("tel:+33 4 50 00 00 00"), compose.onNodeWithText("+33 4 50 00 00 00").addresses())
        assertTrue(compose.onNodeWithText("javascript:alert(1)").addresses().isEmpty(), "no script is ever opened")
        assertEquals(listOf("https://refuge.example/guide"), compose.onNodeWithText("Guide").addresses())
    }

    @Test
    fun aPhotoIsDrawnByTheAppsOwnLoaderElseLinkedUnderItsCaptionOneAtNoWebAddressNever() {
        val photographed =
            ItineraryPlace(
                "hut",
                listOf(6.0, 45.005),
                name = "Refuge",
                details =
                    WaypointDetails(
                        media =
                            WaypointDetails.Media(
                                photos =
                                    listOf(
                                        WaypointDetails.Photo("https://refuge.example/hut.jpg", "The hut"),
                                        WaypointDetails.Photo("javascript:alert(1)", "A script"),
                                    ),
                            ),
                    ),
            )
        compose.setContent {
            Column {
                Drawn("loaded") { WaypointCard(photographed, photo = { shown -> BasicText("Photo at ${shown.url}") }) }
                Drawn("linked") { WaypointCard(photographed) }
            }
        }

        compose.onAllNodesWithText("Photo at https://refuge.example/hut.jpg").assertCountEquals(1)
        compose.onAllNodesWithText("Photo at javascript:alert(1)").assertCountEquals(0)
        assertEquals(
            listOf("https://refuge.example/hut.jpg"),
            compose.onNode(hasText("The hut") and hasAnyAncestor(hasTestTag("linked"))).addresses(),
        )
        compose.onAllNodesWithText("The hut").assertCountEquals(1)
        compose.onAllNodesWithText("A script").assertCountEquals(0)
    }

    /**
     * A card's own strings stand over the scope's words for it alone: the heading of its services, and the line Geo's
     * rule writes under its name, which reads the scope the card hands it, and a card beside it reads the scope's.
     */
    @Test
    fun aCardsOwnStringsStandOverTheScopesWordsTheLineGeoWritesAmongThem() {
        compose.setContent {
            Column {
                WaypointCard(hut(), strings = mapOf("waypointCard.amenities" to "Hut services"))
                WaypointCard(
                    ItineraryPlace("spring", listOf(6.0, 45.015)),
                    strings = mapOf("waypointCard.empty" to "Nothing yet"),
                )
                WaypointCard(ItineraryPlace("well", listOf(6.0, 45.02)))
            }
        }

        compose.onAllNodesWithText("Hut services").assertCountEquals(2)
        compose.onAllNodesWithText("Nothing yet").assertCountEquals(1)
        compose.onAllNodesWithText("Nothing recorded yet").assertCountEquals(1)
    }
}
