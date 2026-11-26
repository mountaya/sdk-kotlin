package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.geo.HexColor
import com.mountaya.geo.ItineraryPlace
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.WaypointCardFacts
import com.mountaya.geo.WaypointDetails
import com.mountaya.geo.amenityGlyph
import com.mountaya.geo.placeGlyph
import com.mountaya.geo.waypointCard
import kotlin.time.Instant

/** The side of a card's mark, in density-independent pixels. */
internal const val WAYPOINT_CARD_MARK_DP: Int = 32

/** How opaque a line's label is beside its value. */
private const val LINE_LABEL_OPACITY = 0.7f

/**
 * WaypointCard is a card about one place of an itinerary, [place]: its mark and its name, the line under it, and what
 * it records, its services by name, when it can be used, how to reach it, its own fields, its links and its photos,
 * each section drawn only where it states a line (`geo.profile.waypoint-card`).
 *
 * ```
 * WaypointCard(place, formatCutoffAt = writeCutoff)
 * ```
 *
 * The line under the name is the author's description, else the summary the place's record assembles. A cut-off, and
 * a `date` field, are written only by [formatCutoffAt], the time zone being the app's: `writeCutoff` above writes one
 * with `formatDateTime` in the device's zone, `TimeZone.getDefault()`, and is null where the platform knows no zone of
 * that name, `formatDateTime` answering null, so neither is written, never the instant in UTC, as `Instant.toString()`
 * writes it, nor an empty word in its place. A phone, an email, a website, a booking page, a link field and a media
 * link are each a link only where `waypointCard` gives it an address, a website or a booking page only at a web
 * address, so no script is ever opened. Compose's foundation loads no image, so each photo, at a web address alone, is
 * drawn by [photo], the app's own image loader, and where the app hands none it is a link to the photo under its
 * caption. A place that records nothing says so.
 *
 * Its words are the scope's `waypointCard` and `amenities` sections, its glyphs the scope's marker set, its figures
 * written in [units], and its colours the tokens'.
 *
 * @param place The place the card is about.
 * @param modifier The host's modifier, as every composable takes one.
 * @param units The reader's units, the scope's locale in metric where null.
 * @param formatCutoffAt How a cut-off and a `date` field are written; neither is written where it is null.
 * @param strings The words it states for itself, by path, `controls.zoomIn`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 * @param photo Draws one photo with the app's own image loader, or null to link each photo instead.
 */
@Composable
public fun WaypointCard(
    place: ItineraryPlace,
    modifier: Modifier = Modifier,
    units: UnitPreferences? = null,
    formatCutoffAt: ((Instant) -> String)? = null,
    strings: Map<String, String>? = null,
    photo: (@Composable (WaypointDetails.Photo) -> Unit)? = null,
) {
    ProvideStrings(strings) {
        val scope = LocalMountayaScope.current
        val tokens = LocalMountayaTokens.current
        val scheme = themeSchemeInForce()
        val facts = waypointCard(place.details, scope, units ?: UnitPreferences(scope.locale), formatCutoffAt)
        val ink = tokenColor(tokens.panelFg, scheme)
        val dimmed = tokenColor(tokens.dimmed, scheme)
        val accent = tokenColor(tokens.accent, scheme)
        PanelSurface(title = null, modifier) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val glyph = placeGlyph(place.glyph?.value, scope.markerGlyphs)
                Box(
                    Modifier
                        .size(WAYPOINT_CARD_MARK_DP.dp)
                        .background(
                            place.color?.let { hexColor(HexColor(it)) }
                                ?: tokenColor(tokens.waypointCardMarkColor, scheme),
                            CircleShape,
                        ).clearAndSetSemantics {},
                    contentAlignment = Alignment.Center,
                ) {
                    if (glyph != null) {
                        Glyph(
                            glyph,
                            set = scope.markerGlyphs,
                            size = 18.dp,
                            color = tokenColor(tokens.waypointCardMarkFg, scheme),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText(
                        place.name ?: scope.word("profile.unlabelledWaypoint"),
                        Modifier.semantics { heading() },
                        style = TextStyle(color = ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    )
                    facts.headline?.let { BasicText(it, style = TextStyle(color = dimmed, fontSize = 13.sp)) }
                }
            }
            if (facts.isEmpty) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText(
                        scope.word("waypointCard.empty"),
                        style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    )
                    BasicText(scope.word("waypointCard.emptyHint"), style = TextStyle(color = dimmed, fontSize = 13.sp))
                }
            }
            if (facts.services.isNotEmpty()) {
                CardSection(scope.word("waypointCard.amenities")) {
                    for (service in facts.services) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val serviceGlyph = amenityGlyph(service.amenity)
                            if (serviceGlyph in scope.markerGlyphs.markup) {
                                Glyph(serviceGlyph, set = scope.markerGlyphs, size = 14.dp, color = ink)
                            }
                            BasicText(service.name, style = TextStyle(color = ink, fontSize = 14.sp))
                        }
                    }
                }
            }
            if (facts.timing.isNotEmpty()) {
                CardSection(
                    scope.word("waypointCard.timing"),
                ) { CardLines(facts.timing, ink, accent) }
            }
            if (facts.contact.isNotEmpty()) {
                CardSection(scope.word("waypointCard.contact")) {
                    CardLines(facts.contact, ink, accent)
                }
            }
            if (facts.fields.isNotEmpty()) {
                CardSection(scope.word("waypointCard.properties")) {
                    CardLines(
                        facts.fields.map {
                            WaypointCardFacts.Line(
                                it.label,
                                it.value,
                                it.href,
                                isExternal = it.href != null,
                            )
                        },
                        ink,
                        accent,
                    )
                }
            }
            if (facts.links.isNotEmpty() || facts.photos.isNotEmpty()) {
                CardSection(scope.word("waypointCard.media")) {
                    for (shown in facts.photos) {
                        if (photo != null) {
                            photo(shown)
                        } else {
                            BasicText(
                                linked(shown.caption ?: place.name ?: shown.url, shown.url, accent),
                                style = TextStyle(color = ink, fontSize = 14.sp),
                            )
                        }
                    }
                    for (link in facts.links) {
                        BasicText(
                            linked(link.label, link.href, accent),
                            style = TextStyle(color = ink, fontSize = 14.sp),
                        )
                    }
                }
            }
        }
    }
}

/** A section of a card under its [heading]. */
@Composable
private fun CardSection(
    heading: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PanelHeading(heading)
        content()
    }
}

/** Labelled lines, each a link where it has an address, its label dimmed beside its value. */
@Composable
private fun CardLines(
    lines: List<WaypointCardFacts.Line>,
    ink: Color,
    accent: Color,
) {
    for (line in lines) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicText(
                line.label,
                style = TextStyle(color = ink.copy(alpha = ink.alpha * LINE_LABEL_OPACITY), fontSize = 13.sp),
            )
            BasicText(linked(line.value, line.href, accent), style = TextStyle(color = ink, fontSize = 14.sp))
        }
    }
}

/** [text], opening [href] where it is given, as a link drawn in [accent] and underlined, and plain text where it is not. */
internal fun linked(
    text: String,
    href: String?,
    accent: Color,
): AnnotatedString =
    if (href == null) {
        AnnotatedString(text)
    } else {
        buildAnnotatedString {
            withLink(
                LinkAnnotation.Url(
                    href,
                    TextLinkStyles(SpanStyle(color = accent, textDecoration = TextDecoration.Underline)),
                ),
            ) {
                append(text)
            }
        }
    }
