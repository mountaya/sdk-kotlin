package com.mountaya.conformance

import com.mountaya.client.NamedException
import com.mountaya.client.isApiException
import com.mountaya.client.isMountayaException
import kotlinx.coroutines.test.TestScope

/**
 * `transport.errors.structural`: each value built as a class of the runner's own, which no class of the
 * SDK's made, answered with what the published predicates read off it.
 *
 * A value the case `declares` of a type, or names by a `name`, declares that name as data through
 * [NamedException], as a second copy of the SDK or code that never linked it hands one over, and carries
 * the case's status and entries as members of its own; a value naming neither is a plain `Throwable`
 * carrying the status alone. So the predicates are proven to read the declared name, whatever class
 * declares it, and never the status.
 */
object TransportErrorsStructuralRunner : ConformanceRunner {
    /** Declared is a failure of a class of the runner's own, declaring [name] as data and carrying what the case states. */
    private class Declared(
        override val name: String,
        val status: Int?,
        val errors: List<FixtureValue>,
    ) : Exception(name),
        NamedException

    /** Foreign is a failure that declares no name, carrying a status of its own. */
    private class Foreign(
        val status: Int?,
    ) : Exception("a failure that declares no name")

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val declared = input.textOrNull("declares")
        val named = input.textOrNull("name")
        if (declared != null && named != null) throw AssertionError("a value declares one name")
        val status = input.numberOrNull("status")?.toInt()
        val errors = (input["errors"] as? FixtureValue.Array)?.items.orEmpty()
        val name = declared ?: named
        val value: Throwable = if (name != null) Declared(name, status, errors) else Foreign(status)
        val family = isMountayaException(value)
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "api" to FixtureValue.Bool(isApiException(value)),
                "class" to if (family) Scripted.text((value as? NamedException)?.name) else FixtureValue.Null,
                "family" to FixtureValue.Bool(family),
            ),
        )
    }
}
