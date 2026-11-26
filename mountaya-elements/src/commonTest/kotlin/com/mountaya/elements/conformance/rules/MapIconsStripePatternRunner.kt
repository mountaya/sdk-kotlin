package com.mountaya.elements

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.RoadAccessKind
import kotlinx.coroutines.test.TestScope

/**
 * `map.icons.stripe-pattern`: how a restricted stretch of the case's `kind` is striped. The tile's size,
 * the stripes' width, the ground and every kind's colour are held to what the rule answers for each kind,
 * exactly, and a constant the fixture adds or drops fails the entry. The rule is the map's own, so it is
 * reached as an internal.
 */
object MapIconsStripePatternRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstants(case)
        val written = case.fields.text("kind")
        val kind =
            RoadAccessKind.entries.firstOrNull { it.id == written }
                ?: throw AssertionError("`$written` is no kind of restricted stretch")
        val pattern = roadAccessStripePattern(kind)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "color" to FixtureValue.Text(pattern.color),
                    "ground" to (pattern.ground?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                    "lineCap" to FixtureValue.Text(pattern.lineCap.id),
                    "segments" to
                        FixtureValue.Array(
                            pattern.segments.map { segment ->
                                FixtureValue.Array(
                                    segment.map { end ->
                                        FixtureValue.Array(end.map { FixtureValue.Number(it) })
                                    },
                                )
                            },
                        ),
                    "size" to FixtureValue.Number(pattern.size),
                    "width" to FixtureValue.Number(pattern.width),
                ),
            ),
        )
    }

    /** Holds the fixture's constants to the patterns the rule answers, the ones it declares and no other. */
    private fun holdConstants(case: ConformanceCase) {
        val declared = case.root("constants")
        if (declared.keys.toSet() != setOf("ground", "kindColors", "stripeWidth", "tileSize")) {
            throw AssertionError(
                "the fixture declares ${declared.keys}, and the rule states ground, kindColors, stripeWidth and tileSize",
            )
        }
        val patterns = RoadAccessKind.entries.associateWith(::roadAccessStripePattern)
        for ((kind, pattern) in patterns) {
            if (declared.number("tileSize").compareTo(pattern.size) != 0 ||
                declared.number("stripeWidth").compareTo(pattern.width) != 0 ||
                declared.textOrNull("ground") != pattern.ground
            ) {
                throw AssertionError(
                    "the fixture declares a tile, a width or a ground the rule does not draw ${kind.id} in",
                )
            }
        }
        val colors = declared.objectOrNull("kindColors") ?: throw AssertionError("the fixture declares no `kindColors`")
        val stated = patterns.entries.associate { (kind, pattern) -> kind.id to pattern.color }
        val written = colors.entries.associate { (id, color) -> id to (color as? FixtureValue.Text)?.value }
        if (written == stated) return
        throw AssertionError("the fixture declares the colours $written, and the rule states $stated")
    }
}
