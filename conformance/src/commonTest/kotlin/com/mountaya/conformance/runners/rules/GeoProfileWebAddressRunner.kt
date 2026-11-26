package com.mountaya.conformance

import com.mountaya.geo.isWebAddress
import com.mountaya.geo.linkedWebAddress
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.web-address`: whether the case's `address`, as written, is one the API accepts, and
 * whether a card links it. A linked address is linked as it reads past the white space at its ends,
 * ECMAScript's `trim` set, and by no other spelling, which the runner holds too.
 */
object GeoProfileWebAddressRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val address = case.fields.text("address")
        val linked = linkedWebAddress(address)
        val readPastItsEnds = address.trim { it in ecmaScriptWhiteSpace }
        if (linked != null && linked != readPastItsEnds) throw AssertionError("the address is linked as `$linked`")
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "accepted" to FixtureValue.Bool(isWebAddress(address)),
                    "linked" to FixtureValue.Bool(linked != null),
                ),
            ),
        )
    }

    /**
     * What ECMAScript's `trim` removes, its white space and its line terminators: the contract's set, which holds
     * U+FEFF and which Kotlin's own `trim` leaves.
     */
    private val ecmaScriptWhiteSpace: Set<Char> =
        (
            "\u0009\u000A\u000B\u000C\u000D\u0020\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007" +
                "\u2008\u2009\u200A\u2028\u2029\u202F\u205F\u3000\uFEFF"
        ).toSet()
}
