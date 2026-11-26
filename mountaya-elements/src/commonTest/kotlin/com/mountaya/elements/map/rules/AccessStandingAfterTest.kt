package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessStandingAfterTest {
    /** Each answer played in order on a style holding [held], whether a refusal stands after it and whether it ended the last. */
    private fun play(
        answers: List<AccessAnswer>,
        held: Set<String>,
    ): List<Pair<Boolean, Boolean>> {
        var standing = StandingRefusals()
        return answers.map { answer ->
            val (after, restored) = accessStandingAfter(standing, answer) { it in held }
            standing = after
            !after.isEmpty to restored
        }
    }

    private fun refused(source: String?): AccessAnswer = AccessAnswer.Refused(source)

    private fun tile(source: String): AccessAnswer = AccessAnswer.Tile(source)

    @Test
    fun aSourcesRefusalEndsWithATileOfItsOwnAndTheEndOfTheLastIsToldOnce() {
        val played =
            play(
                listOf(refused("slope"), refused("zones"), tile("slope"), tile("zones"), tile("zones")),
                setOf("slope", "zones"),
            )

        assertEquals(listOf(true, true, true, false, false), played.map { it.first })
        assertEquals(listOf(false, false, false, true, false), played.map { it.second })
    }

    @Test
    fun aRefusalNamingNoSourceEndsWithTheNextStyleWhichKeepsOneItStillHolds() {
        val unsourced = play(listOf(refused(null), tile("slope"), AccessAnswer.Style), setOf("slope"))
        assertEquals(listOf(true, true, false), unsourced.map { it.first })
        assertEquals(listOf(false, false, true), unsourced.map { it.second })

        val kept = play(listOf(refused("slope"), AccessAnswer.Style, tile("slope")), setOf("slope"))
        assertEquals(listOf(true, true, false), kept.map { it.first })
    }

    @Test
    fun aSourceTheStyleNoLongerHoldsTakesItsRefusalWithItAtTheNextAnswer() {
        val played = play(listOf(refused("slope"), refused("zones"), tile("zones")), setOf("zones"))
        assertEquals(listOf(false, false, true), played.map { it.second })
        assertEquals(
            listOf(false, true),
            play(listOf(refused("slope"), AccessAnswer.Style), emptySet()).map { it.second },
        )
        assertEquals(
            listOf(true, true),
            play(listOf(refused("slope"), tile("zones")), setOf("slope", "zones")).map { it.first },
            "a tile of another source ends nothing",
        )
    }

    @Test
    fun anAnswerWhileNothingStandsEndsNothingAndARefusalAfterTheEndStandsAgain() {
        assertEquals(
            listOf(false, false),
            play(listOf(tile("slope"), AccessAnswer.Style), setOf("slope")).map { it.second },
        )
        val played = play(listOf(refused("slope"), tile("slope"), refused("slope"), tile("slope")), setOf("slope"))
        assertEquals(listOf(false, true, false, true), played.map { it.second })

        var twice = StandingRefusals()
        twice = accessStandingAfter(twice, refused("a")) { true }.first
        twice = accessStandingAfter(twice, refused("a")) { true }.first
        assertEquals(listOf("a"), twice.sources, "a source refused twice stands once")
    }
}
