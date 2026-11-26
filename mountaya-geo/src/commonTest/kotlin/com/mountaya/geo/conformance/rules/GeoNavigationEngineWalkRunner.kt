package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.navigation.engine-walk`: the case's script played over one [ScriptedNavigationEngine], whose
 * requests the case answers: `start`, `reroute` and `stop` are the host's commands, `answer` lands the
 * route the case's `routes` holds for the request named, as a route source answers it, `fail` fails that
 * request, and `position` states where the traveller is. Each command has its effect before it returns,
 * so the state the engine committed last is [NavigationEngine.state]'s value once a step is played, read
 * then, and never a count of the states it told. The answer is every request the engine sent, by name, in
 * the order they left, and the status and the progress read after each step, with the failure the state
 * holds where the case states `failures`, and `refused` on the state read after a position the engine
 * refused with the `IllegalArgumentException` a value it cannot take is refused with.
 */
object GeoNavigationEngineWalkRunner : ConformanceRunner {
    /** The failure a `fail` step answers a request with. */
    private class Failed : Exception("the route source failed")

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val routes = input.objectOrNull("routes") ?: throw AssertionError("the case holds no `routes`")
        val failures = (input["failures"] as? FixtureValue.Bool)?.value ?: false
        val engine = ScriptedNavigationEngine<String>()
        val states = mutableListOf<FixtureValue>()
        for (item in input.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            var refused = false
            when (val name = step.text("step")) {
                "start" -> {
                    engine.start(step.text("request"))
                }

                "reroute" -> {
                    engine.reroute(step.text("request"))
                }

                "answer" -> {
                    val request = step.text("request")
                    val route =
                        routes.objectOrNull(request) ?: throw AssertionError("the case holds no route `$request`")
                    sent(engine, request).answer(fixtureRoute(route))
                }

                "fail" -> {
                    sent(engine, step.text("request")).fail(Failed())
                }

                "position" -> {
                    val position = fixtureVertex(step["position"] ?: throw AssertionError("a position states none"))
                    val before = engine.state.value
                    try {
                        engine.setPosition(position)
                    } catch (refusal: IllegalArgumentException) {
                        // A position that names no place is refused, and the engine is left as it was.
                        if (engine.state.value != before) throw AssertionError("a refused position moved the engine")
                        refused = true
                    }
                }

                "stop" -> {
                    engine.stop()
                }

                else -> {
                    throw AssertionError("`$name` is no step")
                }
            }
            val state = engine.state.value
            states +=
                FixtureValue.Object(
                    listOfNotNull(
                        ("failure" to failureValue(state.error)).takeIf { failures },
                        "progress" to progressValue(state.progress),
                        ("refused" to FixtureValue.Bool(true)).takeIf { refused },
                        "status" to FixtureValue.Text(state.status.id),
                    ),
                )
        }
        val sent = engine.sent.map { FixtureValue.Text(it.request) }
        engine.close()
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "sent" to FixtureValue.Array(sent),
                    "states" to FixtureValue.Array(states),
                ),
            ),
        )
    }

    /**
     * The failure a state holds as the fixture writes it: `route-failed` where the source of the request the
     * engine acted on last failed it, `empty-route` where it answered a route whose line holds no vertex, null
     * where the engine holds none.
     */
    private fun failureValue(error: NavigationError?): FixtureValue =
        when (error) {
            null -> FixtureValue.Null
            is NavigationError.RouteFailed -> FixtureValue.Text("route-failed")
            is NavigationError.EmptyRoute -> FixtureValue.Text("empty-route")
            else -> throw AssertionError("the engine holds a failure the fixture names no kind for: $error")
        }

    /** The request named [request] the engine sent last, which a step answers. */
    private fun sent(
        engine: ScriptedNavigationEngine<String>,
        request: String,
    ): ScriptedRouteRequest<String> =
        engine.sent.lastOrNull { it.request == request }
            ?: throw AssertionError("`$request` is answered and was never sent")
}
