package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.navigation.status`: the status a navigation reports after each of the case's `events`, from
 * the one every navigation starts in. The fixture's two closed sets are held to `NavigationStatus`'s
 * and `NavigationEvent`'s entries in order, its initial status to the status of `NavigationState.idle`,
 * and its table, cell by cell, to `navigationStatusAfter`.
 */
object GeoNavigationStatusRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNavigationConstants(
            case,
            mapOf(
                "events" to texts(NavigationEvent.entries.map { it.id }),
                "initial" to FixtureValue.Text(NavigationState.idle.status.id),
                "statuses" to texts(NavigationStatus.entries.map { it.id }),
                "transitions" to
                    FixtureValue.Object(
                        NavigationStatus.entries.map { status ->
                            status.id to
                                FixtureValue.Object(
                                    NavigationEvent.entries.map { event ->
                                        event.id to FixtureValue.Text(navigationStatusAfter(status, event).id)
                                    },
                                )
                        },
                    ),
            ),
        )
        var status = NavigationState.idle.status
        val after =
            case.fields.items("events").map { item ->
                val id = (item as? FixtureValue.Text)?.value ?: throw AssertionError("an event is not a string")
                val event =
                    NavigationEvent.entries.firstOrNull { it.id == id } ?: throw AssertionError("`$id` is no event")
                status = navigationStatusAfter(status, event)
                FixtureValue.Text(status.id)
            }
        return ConformanceOutcome.Value(FixtureValue.Array(after))
    }

    private fun texts(values: List<String>): FixtureValue = FixtureValue.Array(values.map { FixtureValue.Text(it) })
}
