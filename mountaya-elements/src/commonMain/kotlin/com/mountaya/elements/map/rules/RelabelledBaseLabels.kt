package com.mountaya.elements

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.relabelTextField
import kotlinx.serialization.json.JsonElement

/**
 * relabelledBaseLabels is how a map's label [language] relabels its [labels] (`map.style.label-scope`): each
 * symbol layer of the base map, in the order given, with the label `relabelTextField` rewrites its written
 * label to for the language, or null where the label is left as written (`geo.style.label-relabel`).
 *
 * A layer the map placed itself, [isPlaced], is never relabelled and is left out of the answer: it draws from
 * a source the base map knows nothing about, and its `name` is its own. A switch of base map relabels the one
 * it brings in, the map's own layers drawn again over it left as they are.
 */
internal fun relabelledBaseLabels(
    labels: List<SymbolLabel>,
    isPlaced: (String) -> Boolean,
    language: RestSchema.Language,
): List<Pair<String, JsonElement?>> =
    labels.filterNot { isPlaced(it.id) }.map { it.id to relabelTextField(it.textField, language) }
