package com.mountaya.elements

/**
 * RenderProbe counts what the composables draw, for a test that holds a hot path to its budget (`sdk/hot-paths.yaml` in
 * the contract): each composition of a part that reports to it, and each drawing of a layer that does, by the part's
 * name. Compose tells nobody when it composes a function again, so each part a budget reads says so itself, through
 * `LocalMountayaRenderProbe`.
 *
 * Nothing reports anywhere unless a test provides a probe: without one, a report is a read of a composition local that
 * holds none, and the name it would carry is never built. It also holds what a test cannot reach through a drawn
 * composable, the paths of the profile chart composed last, whose builds and draws a test reads. It is driven from the
 * main thread, where Compose composes and draws.
 */
internal class RenderProbe {
    private val counted = mutableMapOf<String, Int>()

    /** The paths of the profile chart composed last, or null before one is. */
    var plotPaths: PlotPaths? = null

    /** Counts one composition or drawing of [part]. */
    fun report(part: String) {
        counted[part] = (counted[part] ?: 0) + 1
    }

    /** How many times [part] was composed or drawn, 0 for a part never reported. */
    operator fun get(part: String): Int = counted[part] ?: 0

    /** How many times each part was composed or drawn, as they stand now. */
    val counts: Map<String, Int> get() = counted.toMap()
}
