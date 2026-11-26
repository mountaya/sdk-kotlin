package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase

/**
 * Holds that the case's fixture declares no constant, for an entry whose rule states none: a constant the contract
 * adds fails the entry the day it lands rather than being read past.
 */
internal fun holdNoConstants(case: ConformanceCase) {
    val declared =
        case.fixture
            .objectOrNull("constants")
            ?.keys
            .orEmpty()
    if (declared.isNotEmpty()) {
        throw AssertionError("the fixture declares ${declared.sorted().joinToString(", ")}, which no rule here states")
    }
}
