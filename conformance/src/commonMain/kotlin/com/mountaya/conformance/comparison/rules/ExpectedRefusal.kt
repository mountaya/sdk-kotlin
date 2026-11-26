package com.mountaya.conformance

/**
 * Returns the class a case expects a refusal of, or null when it expects a value.
 *
 * A refusal made before anything leaves the process is written `{"refused": "X"}`, its class
 * (`sdk/SPEC.md` section 10). That spelling is matched exactly, so an expectation that carries a
 * `refused` key beside others, or `"refused": null` where nothing is refused, is a value its runner
 * answers whole.
 */
internal fun expectedRefusal(expect: FixtureValue): String? {
    if (expect !is FixtureValue.Object) return null
    val refused = expect["refused"]
    if (expect.keys.toSet() == setOf("refused") && refused is FixtureValue.Text) return refused.value
    return null
}
