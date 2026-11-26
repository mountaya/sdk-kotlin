package com.mountaya.elements

/**
 * AnalysisHandle is what drawing a Data API result answers: the [id] the map draws it under, which its edit
 * and its removal name, and the [result] itself, the client's answer or the one handed over
 * (`map.analysis.slots`). Two are equal where both are.
 */
public class AnalysisHandle<out Result>(
    /** The id the result is drawn under. */
    public val id: String,
    /** The result drawn. */
    public val result: Result,
) {
    override fun equals(other: Any?): Boolean = other is AnalysisHandle<*> && other.id == id && other.result == result

    override fun hashCode(): Int = id.hashCode() * 31 + result.hashCode()

    override fun toString(): String = "AnalysisHandle($id, $result)"
}
