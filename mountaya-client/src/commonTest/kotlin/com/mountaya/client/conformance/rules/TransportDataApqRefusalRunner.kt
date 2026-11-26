package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import com.mountaya.client.generated.DataOperations
import com.mountaya.client.generated.DataSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * `transport.data.apq-refusal`: what the errors a digest-only attempt was answered with mean, as the
 * fixture's `register`, `unsupported` or null. The fixture's spellings, its entries and its scopes
 * are held to the rule's own, so a spelling the contract adds fails here by name rather than in a
 * case that happens to use it.
 *
 * The answer is the client's own: the printed `directions`, whose first attempt sends its digest
 * alone, is answered with the case's `status`, 200 where it states none, and its `errors`, and what
 * the client sends next reads the refusal: its document and no digest is `unsupported`, both is
 * `register`, and nothing, the call ending in its failure, is null. A case asking about the
 * `next-call` answers instead with what the first attempt of the next operation on the same client
 * sends after that answer.
 *
 * A case whose digest was `rejected` is one whose first operation could not compute it, so that
 * operation sent its document alone and the errors answer that; no document the client sends fails
 * its digest, so that case is read off the rule the client reads, a transport's own
 * `PersistedQuerySupport`, under the 200 it states, and the next operation sends the same document
 * and computes its digest again.
 */
object TransportDataApqRefusalRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val constants = case.root("constants")
        agree("registerMessages", constants.words("registerMessages"), PersistedQueryRefusal.Register.messages)
        agree(
            "registerCodes",
            constants.words("registerCodes"),
            PersistedQueryRefusal.Register.codes
                .map { it.value }
                .toSet(),
        )
        agree("unsupportedMessages", constants.words("unsupportedMessages"), PersistedQueryRefusal.Unsupported.messages)
        agree(
            "unsupportedCodes",
            constants.words("unsupportedCodes"),
            PersistedQueryRefusal.Unsupported.codes
                .map { it.value }
                .toSet(),
        )
        val entries =
            constants.items("unsupportedEntries").map { item ->
                val entry = item as? FixtureValue.Object ?: throw AssertionError("an entry that is not an object")
                "${entry.text("code")} ${entry.text("message")}"
            }
        agree(
            "unsupportedEntries",
            entries.toSet(),
            PersistedQueryRefusal.unsupportedEntries
                .map {
                    "${it.first.value} ${it.second}"
                }.toSet(),
        )
        if (constants.text("unsupportedScope") != "transport-lifetime" ||
            constants.text("rejectedDigestScope") != "dropped-not-remembered"
        ) {
            throw AssertionError("the fixture's scopes are not the rule's")
        }

        val input = case.fields
        val errors =
            input.items("errors").map { item ->
                val entry = item as? FixtureValue.Object ?: throw AssertionError("an error entry that is not an object")
                ErrorEntry(
                    entry.text("message"),
                    entry.objectOrNull("extensions")?.textOrNull("code")?.let(::ApiErrorCode),
                )
            }
        val status = input.numberOrNull("status")?.toInt() ?: 200
        val ask = input.textOrNull("ask")
        if (ask != null && ask != "next-call") throw AssertionError("no question is `$ask`")
        val registration = input["registration"]?.let { input.items("registration") }
        when (val written = input.textOrNull("digest")) {
            null -> {
                val errors = input.items("errors")
                return ConformanceOutcome.Value(answered(scope, status, errors, registration, ask != null))
            }

            "rejected" -> {
                if (status != 200) throw AssertionError("a rejected digest is answered 200 alone")
            }

            else -> {
                throw AssertionError("no digest is `$written`")
            }
        }
        val document = DataOperations.directions.document
        val support = PersistedQuerySupport()
        val refusal = support.read(errors, support.firstAttempt(null))

        return when (ask) {
            null -> {
                ConformanceOutcome.Value(
                    when (refusal) {
                        PersistedQueryRefusal.Register -> FixtureValue.Text("register")
                        PersistedQueryRefusal.Unsupported -> FixtureValue.Text("unsupported")
                        null -> FixtureValue.Null
                    },
                )
            }

            "next-call" -> {
                val next = support.firstAttempt(persistedQueryDigest(document))
                ConformanceOutcome.Value(
                    FixtureValue.Object(
                        listOf(
                            "sendsDigest" to FixtureValue.Bool(next.digest != null),
                            "sendsDocument" to FixtureValue.Bool(next.sendsDocument),
                        ),
                    ),
                )
            }

            else -> {
                throw AssertionError("no question is `$ask`")
            }
        }
    }

    /**
     * What the client does once a digest-only attempt of the printed `directions` is answered [status]
     * carrying [errors]: the refusal what it sends next reads as, or, where [nextCall] says so, what the
     * first attempt of the next operation on the same client sends.
     *
     * Where [registration] is given, the first answer asks for the document, and the attempt that sends it
     * beside its digest is answered 200 carrying [registration]: the refusal is then that answer's, read off
     * what the client sends after it.
     */
    private suspend fun answered(
        scope: TestScope,
        status: Int,
        errors: List<FixtureValue>,
        registration: List<FixtureValue>?,
        nextCall: Boolean,
    ): FixtureValue {
        fun body(entries: List<FixtureValue>): ByteArray =
            JsonObject(mapOf("errors" to Streams.json(FixtureValue.Array(entries)))).toString().encodeToByteArray()
        val success = Scripted.Reply.Answer(200, body = """{"data":{"directions":{"routes":[]}}}""".encodeToByteArray())
        val replies =
            listOfNotNull(
                Scripted.Reply.Answer(status, body = body(errors)),
                registration?.let { Scripted.Reply.Answer(200, body = body(it)) },
            )
        val exchange = Scripted.Exchange(scope, replies, success)
        val client = Scripted.client(scope, exchange, Scripted.session, Scripted.noRetries)
        val input =
            DataSchema.DirectionsInput(
                DataSchema.ActivitySlug.Running,
                listOf(listOf(6.87, 45.92), listOf(6.93, 45.98)),
            )

        suspend fun directions() {
            try {
                client.data.directions(input)
            } catch (failure: MountayaException) {
                // The call ending in its failure is the answer that reads as no refusal.
            }
        }
        directions()
        val first = sends(exchange.sent.first().request)
        if (first.second || !first.first) throw AssertionError("the first attempt sent $first, not its digest alone")
        // The attempt whose answer the case asks about: the registration's where the case names one.
        val asked = if (registration == null) 0 else 1
        if (registration != null && exchange.sent.getOrNull(1)?.let { sends(it.request) } != (true to true)) {
            throw AssertionError("the first answer asked for the document, and the next attempt did not register it")
        }
        if (nextCall) {
            val before = exchange.sent.size
            directions()
            if (exchange.sent.size == before) throw AssertionError("the next call sent nothing")
            val next = sends(exchange.sent[before].request)
            return FixtureValue.Object(
                listOf(
                    "sendsDigest" to FixtureValue.Bool(next.first),
                    "sendsDocument" to FixtureValue.Bool(next.second),
                ),
            )
        }
        val after = exchange.sent.getOrNull(asked + 1) ?: return FixtureValue.Null
        return when (val sent = sends(after.request)) {
            false to true -> FixtureValue.Text("unsupported")
            true to true -> FixtureValue.Text("register")
            else -> throw AssertionError("the attempt after the refusal sent $sent")
        }
    }

    /** Whether [attempt] sent a persisted query's digest, and its document. */
    private fun sends(attempt: ExchangeRequest): Pair<Boolean, Boolean> {
        val body = Json.parseToJsonElement(attempt.body?.decodeToString() ?: "{}").jsonObject
        val extensions = body["extensions"] as? JsonObject
        return (extensions?.get("persistedQuery") != null) to (body["query"] != null)
    }

    private fun FixtureValue.Object.words(key: String): Set<String> =
        items(key)
            .map { word ->
                (word as? FixtureValue.Text)?.value ?: throw AssertionError("`$key` holds a value that is not a string")
            }.toSet()

    private fun agree(
        name: String,
        fixture: Set<String>,
        rule: Set<String>,
    ) {
        if (fixture == rule) return
        throw AssertionError("the fixture's $name are ${fixture.sorted()}, the rule's are ${rule.sorted()}")
    }
}
