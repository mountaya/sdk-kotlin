package com.mountaya.elements

import kotlinx.serialization.json.JsonElement

/** SymbolLabel is the label a symbol layer draws: the layer's [id] and its `text-field`, as a style writes one. Two are equal where both are. */
internal class SymbolLabel(
    /** The symbol layer's id. */
    val id: String,
    /** The label, as a style writes a `text-field`. */
    val textField: JsonElement,
) {
    override fun equals(other: Any?): Boolean = other is SymbolLabel && other.id == id && other.textField == textField

    override fun hashCode(): Int = id.hashCode() * 31 + textField.hashCode()

    override fun toString(): String = "SymbolLabel($id, $textField)"
}
