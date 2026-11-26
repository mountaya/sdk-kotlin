package com.mountaya.elements

/**
 * accessStandingAfter is the refusals of access that stand once a map hears one more [answer] over those
 * [standing], and whether that answer ended the last of them, which is what is told
 * (`map.events.access-standing`).
 *
 * A refusal stands for its source until a tile of that source loads, and one naming no source, of a style, a
 * sprite, a glyph or the layers manifest, until the next style loads; each answer also ends the refusal of
 * every source the style no longer holds, [holdsSource] read as the answer arrives, so a source that left the
 * style takes its refusal with it. A refusal adds its source, once, to those standing. An answer while
 * nothing stands ends nothing, so the end is told once, and again only after a later refusal has ended in
 * turn.
 */
internal fun accessStandingAfter(
    standing: StandingRefusals,
    answer: AccessAnswer,
    holdsSource: (String) -> Boolean,
): Pair<StandingRefusals, Boolean> =
    when (answer) {
        is AccessAnswer.Refused -> {
            val source = answer.source
            when {
                source == null -> StandingRefusals(standing.sources, unsourced = true) to false
                source in standing.sources -> standing to false
                else -> StandingRefusals(standing.sources + source, standing.unsourced) to false
            }
        }

        is AccessAnswer.Tile -> {
            if (standing.isEmpty) {
                standing to false
            } else {
                val after =
                    StandingRefusals(
                        standing.sources.filterNot { it == answer.source || !holdsSource(it) },
                        standing.unsourced,
                    )
                after to after.isEmpty
            }
        }

        is AccessAnswer.Style -> {
            if (standing.isEmpty) {
                standing to false
            } else {
                val after = StandingRefusals(standing.sources.filter(holdsSource), unsourced = false)
                after to after.isEmpty
            }
        }
    }
