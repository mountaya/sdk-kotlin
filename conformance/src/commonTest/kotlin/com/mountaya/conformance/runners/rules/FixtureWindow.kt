package com.mountaya.conformance

import com.mountaya.geo.ElevationDomain
import com.mountaya.geo.ProfileRange

/** The case's `{from, to}` window, null where the case states none or writes it null. */
internal fun fixtureWindow(value: FixtureValue?): ProfileRange? {
    if (value == null || value is FixtureValue.Null) return null
    val window = value as? FixtureValue.Object ?: throw AssertionError("`window` is not an object")
    return ProfileRange(window.number("from"), window.number("to"))
}

/** The case's `{min, max}` elevation domain. */
internal fun fixtureDomain(value: FixtureValue?): ElevationDomain {
    val domain = value as? FixtureValue.Object ?: throw AssertionError("`domain` is not an object")
    return ElevationDomain(domain.number("min"), domain.number("max"))
}

/** [domain] as the fixtures write one, `{max, min}`. */
internal fun domainValue(domain: ElevationDomain): FixtureValue =
    FixtureValue.Object(listOf("max" to FixtureValue.Number(domain.max), "min" to FixtureValue.Number(domain.min)))

/** An object of [entries], in the order given. */
internal fun objectValue(vararg entries: Pair<String, FixtureValue>): FixtureValue =
    FixtureValue.Object(entries.toList())
