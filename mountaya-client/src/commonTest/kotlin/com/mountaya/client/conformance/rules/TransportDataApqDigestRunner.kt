package com.mountaya.client

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `transport.data.apq-digest`: the digest a document is sent as, from this target's own SHA-256.
 * The fixture's `constants` name the algorithm, the bytes and the encoding the rule implements,
 * and are held to them, so a contract that moved any of the three fails here by name.
 */
object TransportDataApqDigestRunner : ConformanceRunner {
    private val implemented = mapOf("algorithm" to "SHA-256", "bytes" to "utf-8", "encoding" to "lowercase-hex")

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val constants = case.root("constants")
        for ((key, value) in implemented) {
            val stated = constants.text(key)
            if (stated != value) throw AssertionError("the fixture's $key is \"$stated\", the rule's is \"$value\"")
        }
        return ConformanceOutcome.Value(FixtureValue.Text(persistedQueryDigest(case.fields.text("document"))))
    }
}
