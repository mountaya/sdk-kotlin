package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.FixtureValue

/**
 * Holds every constant the case's fixture declares to [stated], the value the rule states for it,
 * compared exactly, as the navigation fixtures state no tolerance. A constant the fixture declares
 * and [stated] does not name fails, and so does one [stated] names that the fixture no longer
 * declares, so a constant the contract adds or drops is caught the day it lands.
 *
 * The conformance module's own holder reads only the public surface, and these entries' rules are
 * internal, so the runners of this module hold their constants here.
 */
internal fun holdNavigationConstants(
    case: ConformanceCase,
    stated: Map<String, FixtureValue>,
) {
    holdConstantsExactly(case, stated)
}
