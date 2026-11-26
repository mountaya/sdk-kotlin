package com.mountaya.conformance

import com.mountaya.client.ApiException
import com.mountaya.client.ConnectionException
import com.mountaya.client.ErrorEntry
import com.mountaya.client.MountayaException
import com.mountaya.client.generated.ApiErrorCode
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement

/**
 * `transport.errors.ladder`: the class a failure raises, a response's status and code through
 * `ApiException.of`, and a request that never arrived as a `ConnectionException`.
 *
 * A case naming a `surface` is about that surface alone, and the one the fixture names is the Data
 * API, whose answer carrying errors under a success is a `DataException`, an `ApiException` like
 * any other the API answered. A success raises nothing on the other surfaces, so a success case
 * naming one and no `body` is not a case this runner can answer.
 *
 * A case stating a `body` is answered by a call, since what an undecodable body raises is the
 * transport's: a read by its declaration, or the printed `directions` on the Data API, answered the
 * case's status with that text, and the error it raised held to carry that status and the
 * decoder's failure as its cause.
 *
 * The no-status case builds the `ConnectionException` itself: mapping a failed request onto it is
 * the transport's, which is proven by its own tests rather than by this fixture.
 */
object TransportErrorsLadderRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val code = case.fields.textOrNull("code")
        val surface = case.fields.textOrNull("surface")
        if (surface !in setOf(null, "data", "rest", "tiles")) throw AssertionError("no surface is called `$surface`")
        val status = case.fields.numberOrNull("status")?.toInt()
        val body = case.fields.textOrNull("body")
        val failure =
            when {
                status == null -> {
                    ConnectionException()
                }

                body != null -> {
                    answered(scope, status, body, surface == "data")
                }

                else -> {
                    if (status in 200..299 && surface != "data") {
                        throw AssertionError("a success raises an error on the Data API alone")
                    }
                    val errors = if (code == null) emptyList() else listOf(ErrorEntry("", ApiErrorCode(code)))
                    ApiException.of(status, errors)
                }
            }
        return ConformanceOutcome.Value(FixtureValue.Text(failure.name))
    }

    /** The error a call answered [status] carrying [body] raises, on the Data API where [data] says so. */
    private suspend fun answered(
        scope: TestScope,
        status: Int,
        body: String,
        data: Boolean,
    ): MountayaException {
        val exchange = Scripted.Exchange(scope, listOf(Scripted.Reply.Answer(status, body = body.encodeToByteArray())))
        val error =
            try {
                if (data) {
                    Scripted
                        .client(
                            scope,
                            exchange,
                            Scripted.session,
                            Scripted.noRetries,
                        ).data
                        .directions(directionsInput)
                } else {
                    Scripted.client(scope, exchange, retry = Scripted.noRetries).send(
                        aRead,
                        "/v1/things",
                        JsonElement.serializer(),
                        JsonElement.serializer().nullable,
                    )
                }
                throw AssertionError("the answer raised nothing")
            } catch (raised: MountayaException) {
                raised
            }
        if (error is ApiException) {
            if (error.status !=
                status
            ) {
                throw AssertionError("the error carries ${error.status}, and the answer was $status")
            }
            if (error.cause == null) throw AssertionError("the error carries no decoder's failure as its cause")
        }
        return error
    }
}
