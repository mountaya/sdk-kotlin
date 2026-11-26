package com.mountaya.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.LayersPanelCategory
import com.mountaya.geo.LayersPanelChoice
import com.mountaya.geo.LayersPanelMark
import com.mountaya.geo.LayersPanelPress
import com.mountaya.geo.LayersPanelRow
import com.mountaya.geo.LayersSelectionEngine
import com.mountaya.geo.generated.ElementLayout
import com.mountaya.geo.layersPanelPress
import com.mountaya.geo.layersPanelSections
import kotlin.time.Instant

/**
 * LayersPanel is a map's layers picker (`geo.layers.picker-rows`), in the sections and the order of the map studio's
 * layers panel:
 *
 * - **the terrains**, as picture cards, one base map among them;
 * - **the enrichments**, as switches, each on or off by itself, the points of interest left to `PoiPanel`, whose own
 *   picker drives them;
 * - **the classic overlays**, as picture cards, one among every classic category's, shown a category at a time behind a
 *   segmented switch of the categories that hold one, drawn where two or more do. It opens on the category the reader
 *   picked last in the switch, which the panel holds for as long as it lives, else on the one holding the selected
 *   overlay, else on the first;
 * - **the time**, where the host offers it, [offersTime]: a slider over the forecast window, one stop an hour from 71
 *   hours back to 144 ahead (`geo.layers.time-window`), ticked every day and labelled two days back, now, and two and
 *   five days ahead, filled from now to its thumb, which stands at the instant the selection holds, read out as the
 *   moment and its distance from now (`geo.layers.time-readout`), and pinning on release the instant of the stop let go
 *   of, or none on now, through the selection (`geo.layers.time-slider`, `LayersSelectionEngine.setInstant`). Its track
 *   is laid out around the clock `LocalMountayaClock` provides each time the panel is drawn, and it is held, under
 *   `layers.timeUnavailable`, where that clock reads an instant past the range every SDK holds a track to. The time is
 *   the host's to offer: the Tile API refuses a publishable key or a share token a stamped tile asked more than 15
 *   minutes from the moment it arrives, unless its organization's overlay time control allows it, so a slider offered
 *   where it does not would pin instants that draw nothing.
 *
 * The split between enrichments and classic overlays is the catalogue's: base maps replace one another, classic overlays
 * replace one another, since two of them repaint the same ground, and enrichments stack. A card shows the picture
 * [thumbnail] hands for its choice and its slug, and its name alone where it hands none.
 *
 * It lists what [selection] holds and drives it, a press asking the selection's command for its card or its switch
 * (`LayersSelectionEngine`), which drives the map. An entry [isLocked] names out of the viewer's reach shows its lock in
 * place of its tick or its switch, and a press on it selects nothing and is handed to [onLockedClick], for the app's own
 * notice: what an agreement includes is the API's to enforce, and this lets an app that knows it say so before the
 * request.
 *
 * Every word is the scope's (`MountayaScope`): the headings, the time's caption and readout its `layers` section's, and
 * each terrain, category and overlay by the name the catalogue gives it in the scope's language, else in English, else
 * by its slug; the readout's moment is written in the scope's locale and the device's time zone.
 *
 * Each card, switch and segment is a part of its own, handed equal values and pressed through a handler that stays as
 * the panel is drawn again, so a redraw of the panel that changes none of them, its host's or a press on another, draws
 * none of them (`panel-rows` in the contract's `sdk/hot-paths.yaml`). Each is the platform's touch target high, read by
 * accessibility services as selected while checked, and ringed while it holds the keyboard's focus.
 *
 * @param selection The selection the picker lists and drives.
 * @param modifier The host's modifier, as every composable takes one.
 * @param isLocked Whether an entry, by its slug, is out of the viewer's reach, or null for none.
 * @param offersTime Whether the host offers the time, which lists the time section last.
 * @param thumbnail The picture a card shows for a terrain or a classic overlay, by its choice and its slug, or null for
 * a card of its name alone. A card is drawn again where the picture handed is another, so a host hands the same painter
 * for an entry from one draw to the next, one it holds or `painterResource` remembered.
 * @param strings The words it states for itself, by path, `layers.terrain`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 * @param onLockedClick Handed the slug of each locked entry pressed.
 */
@Composable
public fun LayersPanel(
    selection: LayersSelectionEngine,
    modifier: Modifier = Modifier,
    isLocked: ((String) -> Boolean)? = null,
    offersTime: Boolean = false,
    thumbnail: ((LayersPanelChoice, String) -> Painter?)? = null,
    strings: Map<String, String>? = null,
    onLockedClick: ((String) -> Unit)? = null,
) {
    ProvideStrings(strings) {
        LocalMountayaRenderProbe.current?.report("LayersPanel")
        val scope = LocalMountayaScope.current
        val held by selection.state.collectAsState()
        // The category the reader last picked in the overlays' switch, the panel's own for as long as it lives.
        var picked by remember { mutableStateOf<String?>(null) }
        val sections = layersPanelSections(held, scope.language.value, isLocked ?: { false }, picked, offersTime)
        // What a press does, read from the panel as it was drawn last, behind a handler that stays as it is drawn
        // again.
        val press by rememberUpdatedState { choice: LayersPanelChoice, slug: String ->
            val row = sections.filter { it.choice == choice }.flatMap { it.rows }.firstOrNull { it.slug == slug }
            if (row != null) layersPress(layersPanelPress(choice, row), selection, onLockedClick)
        }
        val onRowPress = remember { { choice: LayersPanelChoice, slug: String -> press(choice, slug) } }
        val onPick = remember { { slug: String -> picked = slug } }
        val onRelease = remember(selection) { { pin: Instant? -> selection.setInstant(pin) } }
        // As wide as the embed's panel, its cards three a line at the measures the tokens table states.
        PanelSurface(title = scope.word("controls.layers"), modifier) {
            for (section in sections) {
                key(section.choice) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PanelHeading(scope.word(section.heading.word))
                        when (section.choice) {
                            LayersPanelChoice.Terrain -> {
                                LayersCards(section.choice, section.rows, thumbnail, onRowPress)
                            }

                            LayersPanelChoice.Enrichment -> {
                                for (row in section.rows) {
                                    key(row.slug) {
                                        LayersSwitch(row.slug, row.label, row.isChecked, row.mark, onRowPress)
                                    }
                                }
                            }

                            LayersPanelChoice.Overlay -> {
                                if (section.categories.size >= 2) {
                                    LayersCategorySwitch(section.categories, section.category, onPick)
                                }
                                LayersCards(section.choice, section.rows, thumbnail, onRowPress)
                            }

                            LayersPanelChoice.Time -> {
                                LayersTime(held.instant, onRelease)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The picture cards of [rows], each making [choice], `ElementLayout.panelLayerCardColumns` a line sharing the panel's
 * width, which at the panel's own width makes each `panel-layer-card-width` by `panel-layer-card-height`, pressed
 * through [onPress].
 */
@Composable
private fun LayersCards(
    choice: LayersPanelChoice,
    rows: List<LayersPanelRow>,
    thumbnail: ((LayersPanelChoice, String) -> Painter?)?,
    onPress: (LayersPanelChoice, String) -> Unit,
) {
    val columns = LocalMountayaPanelLayout.current?.layerCardColumns ?: ElementLayout.panelLayerCardColumns
    for (line in rows.chunked(columns)) {
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in line) {
                key(row.slug) {
                    LayersCard(
                        choice,
                        row.slug,
                        row.label,
                        row.isChecked,
                        row.mark,
                        thumbnail?.invoke(choice, row.slug),
                        onPress,
                        Modifier.weight(1f),
                    )
                }
            }
            repeat(columns - line.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

/**
 * A picture card of the picker, [slug]'s, making [choice]: [picture] where the host hands one, above its [label], edged
 * in the accent while [isChecked], showing [mark], pressed through [onPress].
 */
@Composable
private fun LayersCard(
    choice: LayersPanelChoice,
    slug: String,
    label: String,
    isChecked: Boolean,
    mark: LayersPanelMark?,
    picture: Painter?,
    onPress: (LayersPanelChoice, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LocalMountayaRenderProbe.current?.report("LayersCard $label")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val ink = tokenColor(tokens.panelFg, scheme)
    val edge = tokenColor(if (isChecked) tokens.accent else tokens.panelBorder, scheme)
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(6.dp)
    val locked = scope.word("layers.locked")
    val pictureHeight = LocalMountayaPanelLayout.current?.pictureHeight ?: ElementLayout.panelPictureHeight
    Column(
        modifier
            .heightIn(min = maxOf(controlSide, ElementLayout.panelLayerCardHeight.dp))
            .border(if (isChecked) 2.dp else 1.dp, edge, shape)
            .clip(shape)
            .keyboardFocusRing(interactions, shape)
            .selectable(
                selected = isChecked,
                interactionSource = interactions,
                indication = null,
                role = Role.RadioButton,
                onClick = { onPress(choice, slug) },
            ).semantics { if (mark == LayersPanelMark.Lock) stateDescription = locked }
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (picture != null) {
            Image(
                picture,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(pictureHeight.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                label,
                Modifier.weight(1f),
                style = TextStyle(color = ink, fontSize = 12.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            when (mark) {
                LayersPanelMark.Lock -> {
                    Glyph("locked", set = scope.controlGlyphs, size = 12.dp, color = ink)
                }

                LayersPanelMark.Tick -> {
                    Glyph("check", set = scope.controlGlyphs, size = 12.dp, color = ink)
                }

                null -> {}
            }
        }
    }
}

/** An enrichment's switch, [slug]'s, labelled [label], on while [isChecked], showing [mark], pressed through [onPress]. */
@Composable
private fun LayersSwitch(
    slug: String,
    label: String,
    isChecked: Boolean,
    mark: LayersPanelMark?,
    onPress: (LayersPanelChoice, String) -> Unit,
) {
    LocalMountayaRenderProbe.current?.report("LayersSwitch $label")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val ink = tokenColor(tokens.panelFg, scheme)
    val interactions = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = controlSide)
            .keyboardFocusRing(interactions)
            .toggleable(
                value = isChecked,
                interactionSource = interactions,
                indication = null,
                role = Role.Switch,
                onValueChange = { onPress(LayersPanelChoice.Enrichment, slug) },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(label, Modifier.weight(1f), style = TextStyle(color = ink, fontSize = 14.sp))
        if (mark == LayersPanelMark.Lock) {
            LayersLock(scope, ink)
        } else {
            val track = tokenColor(if (isChecked) tokens.accent else tokens.panelBorder, scheme)
            val knob = tokenColor(tokens.panelBg, scheme)
            Box(
                Modifier.size(36.dp, 20.dp).background(track, CircleShape),
                contentAlignment = if (isChecked) Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                Box(Modifier.padding(2.dp).size(16.dp).background(knob, CircleShape))
            }
        }
    }
}

/** The lock a locked entry shows in place of its switch, and the word saying so, in [ink]. */
@Composable
private fun LayersLock(
    scope: ElementsScope,
    ink: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        BasicText(scope.word("layers.locked"), style = TextStyle(color = ink, fontSize = 12.sp))
        Glyph("locked", set = scope.controlGlyphs, size = 14.dp, color = ink)
    }
}

/** The overlays' switch between [categories], the one [open] standing out, a pick handed to [onPick]. */
@Composable
private fun LayersCategorySwitch(
    categories: List<LayersPanelCategory>,
    open: String?,
    onPick: (String) -> Unit,
) {
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val shape = RoundedCornerShape(6.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .border(1.dp, tokenColor(tokens.panelBorder, scheme), shape)
            .clip(shape)
            .selectableGroup(),
    ) {
        for (category in categories) {
            key(category.slug) {
                LayersCategorySegment(category.slug, category.name, category.slug == open, onPick, Modifier.weight(1f))
            }
        }
    }
}

/** A segment of the overlays' switch, the category [slug]'s, named [name], standing out while [isOpen]. */
@Composable
private fun LayersCategorySegment(
    slug: String,
    name: String,
    isOpen: Boolean,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LocalMountayaRenderProbe.current?.report("LayersSegment $name")
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier
            .heightIn(min = controlSide)
            .background(if (isOpen) tokenColor(tokens.controlBgHover, scheme) else Color.Transparent)
            .keyboardFocusRing(interactions)
            .selectable(
                selected = isOpen,
                interactionSource = interactions,
                indication = null,
                role = Role.Tab,
                onClick = { onPick(slug) },
            ).padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            name,
            style =
                TextStyle(
                    color = tokenColor(if (isOpen) tokens.accent else tokens.panelFg, scheme),
                    fontSize = 12.sp,
                    fontWeight = if (isOpen) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Carries out what a press asks for: the selection's command, or the report of a locked entry to [onLockedClick]. */
internal fun layersPress(
    press: LayersPanelPress,
    selection: LayersSelectionEngine,
    onLockedClick: ((String) -> Unit)?,
) {
    when (press) {
        is LayersPanelPress.Locked -> {
            onLockedClick?.invoke(press.slug)
        }

        is LayersPanelPress.Command -> {
            when (press.choice) {
                LayersPanelChoice.Terrain -> {
                    selection.setTerrain(press.slug)
                }

                LayersPanelChoice.Overlay -> {
                    selection.setOverlay(press.slug)
                }

                LayersPanelChoice.Enrichment -> {
                    selection.toggleEnrichment(press.slug)
                }

                // The time lists no row, so no press asks for it: the slider pins through its release.
                LayersPanelChoice.Time -> {}
            }
        }
    }
}
