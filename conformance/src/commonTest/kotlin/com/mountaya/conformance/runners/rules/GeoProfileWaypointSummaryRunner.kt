package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.WaypointSummaryWords
import com.mountaya.geo.waypointDeclaresNothing
import com.mountaya.geo.waypointHeadline
import com.mountaya.geo.waypointSummary
import kotlinx.coroutines.test.TestScope
import kotlin.time.Instant

/** A template's one placeholder, `{moment}`. */
private val placeholder = Regex("\\{[a-z]+\\}")

/**
 * `geo.profile.waypoint-summary`: what the place the case's `details` describe says about itself, its
 * headline, and whether it declares nothing. The place is built as the REST API's waypoint. Its `details`
 * are written as `geo.data.itinerary-waypoints` answers a place's: a timing's `cutoffAt` and `stopDuration`
 * and a contact's `openingHours`. The fixture's description has a rule that takes the REST API's waypoint
 * read them at its edge as `cutoff_at`, `stop_duration` and `opening_hours`, and the runner does so. The
 * words are the fixture's templates, each with one
 * placeholder, and a case's `cutoffMoment` is what the caller's formatter answers for the place's own
 * cut-off; a case that states none has a caller with no formatter.
 */
object GeoProfileWaypointSummaryRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val copy = case.root("copy")
        val summary = copy.objectOrNull("summary") ?: throw AssertionError("the copy has no `summary`")
        val words =
            WaypointSummaryWords(
                amenities = copy.text("amenities"),
                openingHours = clause(summary.text("openingHours")),
                cutoffAt = clause(summary.text("cutoffAt")),
                stop = clause(summary.text("stop")),
            )
        val units = fixtureUnits(case.root("units"))
        val details = case.fields.objectOrNull("details") ?: throw AssertionError("the case has no `details`")
        val timing = details.objectOrNull("timing")
        val contact = details.objectOrNull("contact")
        val cutoffAt = timing?.textOrNull("cutoffAt")?.let(Instant::parse)
        val place =
            fixtureWaypoint(
                amenities = services(details),
                description = details.textOrNull("description"),
                timing =
                    timing?.let {
                        RestSchema.WaypointTiming {
                            cutoff_at = cutoffAt
                            stop_duration = wholeOrNull(it, "stopDuration")
                        }
                    },
                contact =
                    contact?.let {
                        RestSchema.WaypointContact {
                            phone = it.textOrNull("phone")
                            opening_hours = it.textOrNull("openingHours")
                        }
                    },
            )
        val moment = case.fields.textOrNull("cutoffMoment")
        val formatCutoffAt: ((Instant) -> String)? =
            moment?.let {
                { instant ->
                    if (instant != cutoffAt) throw AssertionError("the formatter is handed $instant, not the cut-off")
                    moment
                }
            }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "declaresNothing" to FixtureValue.Bool(waypointDeclaresNothing(place)),
                    "headline" to textOrNull(waypointHeadline(place, words, units, formatCutoffAt)),
                    "summary" to textOrNull(waypointSummary(place, words, units, formatCutoffAt)),
                ),
            ),
        )
    }

    /** The services [details] lists, none where it lists none. */
    private fun services(details: FixtureValue.Object): List<String> {
        if (details["amenities"] == null) return emptyList()
        return details.items("amenities").map {
            (it as? FixtureValue.Text)?.value
                ?: throw AssertionError("a service is no string")
        }
    }

    /** The clause [template] reads, its one placeholder taken by the value. */
    private fun clause(template: String): (String) -> String {
        if (placeholder.findAll(template).count() != 1 || template.count { it == '{' } != 1) {
            throw AssertionError("the template `$template` does not hold exactly one placeholder")
        }
        return { value -> placeholder.replace(template) { value } }
    }
}
