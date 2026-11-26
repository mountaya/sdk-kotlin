package com.mountaya.elements

/**
 * HeldAnalysis is what a map holds of a Data API result it drew, enough for an edit to merge a patch onto
 * and redraw: its [kind], the [input] it was asked with, null where the result was handed over, the
 * [result], and the options it is drawn under (`map.analysis.edit`). The input and the result are of the
 * types the kind reads, which the engine reads them back as.
 */
internal data class HeldAnalysis(
    /** The result's kind. */
    val kind: AnalysisKind,
    /** The input the result was asked with, or null where it was handed over. */
    val input: Any?,
    /** The result drawn. */
    val result: Any,
    /** The suggested itinerary drawn solid, by its place in the result. */
    val chosen: Int,
    /** How the camera frames the result once it is drawn, or null for no fit. */
    val fit: CameraFit?,
)
