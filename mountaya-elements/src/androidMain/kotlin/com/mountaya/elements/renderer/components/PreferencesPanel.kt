package com.mountaya.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.PreferenceLabel
import com.mountaya.geo.PreferenceValue
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.generated.ElementLayout
import com.mountaya.geo.preferenceChoices
import com.mountaya.geo.preferencePress

/**
 * PreferencesPanel is the units and language picker (`geo.units.preference-choices`): the length system, the form a
 * travel rate takes, the slope notation, the temperature scale and the label language, in that order, each option in
 * the order the contract lists it, a unit by its word and a language by its own name, which a reader finds fastest
 * written the way they write it.
 *
 * It holds no set of its own: units are the app's concern, read by surfaces a picker cannot see, so [preferences] is
 * the app's, and a press hands [onPreferencesChange] the whole set with one preference changed. A language is the map's
 * concern too, so a press on one also relabels [map] (`MapEngine.setLabelLanguage`). A language is ticked as the map
 * reads the set's tag, so one holding `fr-CA` ticks French. Its words are the scope's `preferences` section
 * (`MountayaScope`). Each option is a card, two to a row sharing the panel's width, which at the panel's own width
 * makes each `panel-preference-card-width` by `panel-preference-card-height` (`geo.panels.layout`).
 *
 * @param preferences The set the picker shows.
 * @param onPreferencesChange Handed the set a press asks for.
 * @param modifier The host's modifier, as every composable takes one.
 * @param map The map a press on a language relabels, or null for none.
 * @param strings The words it states for itself, by path, `controls.zoomIn`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 */
@Composable
public fun PreferencesPanel(
    preferences: UnitPreferences,
    onPreferencesChange: (UnitPreferences) -> Unit,
    modifier: Modifier = Modifier,
    map: MapEngine? = null,
    strings: Map<String, String>? = null,
) {
    ProvideStrings(strings) {
        val scope = LocalMountayaScope.current
        val columns =
            LocalMountayaPanelLayout.current?.preferenceCardColumns ?: ElementLayout.panelPreferenceCardColumns
        PanelSurface(title = scope.word("controls.preferences"), modifier) {
            for (choice in preferenceChoices(preferences)) {
                key(choice.field) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PanelHeading(scope.word("preferences.${choice.field.id}"))
                        for (line in choice.options.chunked(columns)) {
                            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (option in line) {
                                    key(option.value.id) {
                                        PreferenceCard(
                                            label = preferenceLabel(option.label, scope),
                                            isChecked = option.isChecked,
                                            onClick = {
                                                preferenceSelect(option.value, preferences, onPreferencesChange, map)
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                                repeat(columns - line.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Carries out a press on the option [value]: the set it asks for handed to [onPreferencesChange], and [map] relabelled where it is a language. */
internal fun preferenceSelect(
    value: PreferenceValue,
    preferences: UnitPreferences,
    onPreferencesChange: (UnitPreferences) -> Unit,
    map: MapEngine?,
) {
    val press = preferencePress(preferences, value)
    onPreferencesChange(press.next)
    press.relabel?.let { map?.setLabelLanguage(it) }
}

/** The text an option is labelled with: a word of the scope's `preferences` section, or a language's own name. */
internal fun preferenceLabel(
    label: PreferenceLabel,
    scope: ElementsScope,
): String =
    when (label) {
        is PreferenceLabel.Word -> scope.word("preferences.${label.word}")
        is PreferenceLabel.Name -> label.name
    }

/**
 * A card of the picker, labelled [label], edged in the accent and ticked while [isChecked], `panel-preference-card-height`
 * tall, pressed through [onClick] and read by accessibility services as selected while it is checked.
 */
@Composable
private fun PreferenceCard(
    label: String,
    isChecked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LocalMountayaRenderProbe.current?.report("PanelRow $label")
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val ink = tokenColor(tokens.panelFg, scheme)
    val edge = tokenColor(if (isChecked) tokens.accent else tokens.panelBorder, scheme)
    val interactions = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier
            .heightIn(min = maxOf(controlSide, ElementLayout.panelPreferenceCardHeight.dp))
            .border(if (isChecked) 2.dp else 1.dp, edge, shape)
            .clip(shape)
            .keyboardFocusRing(interactions, shape)
            .selectable(
                selected = isChecked,
                interactionSource = interactions,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            ).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicText(
            label,
            Modifier.weight(1f),
            style = TextStyle(color = ink, fontSize = 13.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (isChecked) Glyph("check", set = scope.controlGlyphs, size = 14.dp, color = ink)
    }
}
