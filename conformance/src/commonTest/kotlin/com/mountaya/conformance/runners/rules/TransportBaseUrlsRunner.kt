package com.mountaya.conformance

import com.mountaya.client.BaseUrls
import com.mountaya.client.ConfigurationException
import com.mountaya.client.Url
import kotlinx.coroutines.test.TestScope

/**
 * `transport.base-urls`: the origin a request on the case's surface goes to, from a client built
 * with the case's `overrides`, each one moving its own surface and no other. An override with no
 * scheme names no place here, where there is no page, and is refused as the client's addresses are
 * built: the case is then answered with the refusal's class.
 */
object TransportBaseUrlsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val overrides = case.fields.objectOrNull("overrides") ?: FixtureValue.Object(emptyList())
        val urls =
            try {
                val moved =
                    overrides.entries.associate { (surface, value) ->
                        if (surface !in setOf("data", "rest", "tiles")) {
                            throw AssertionError("no surface is called `$surface`")
                        }
                        val text =
                            (value as? FixtureValue.Text)?.value
                                ?: throw AssertionError("the override for `$surface` is not a URL")
                        surface to Url(text)
                    }
                BaseUrls(
                    data = moved["data"] ?: BaseUrls.standard.data,
                    rest = moved["rest"] ?: BaseUrls.standard.rest,
                    tiles = moved["tiles"] ?: BaseUrls.standard.tiles,
                )
            } catch (refused: ConfigurationException) {
                // An override that names no place where there is no page is refused as the client is built.
                return ConformanceOutcome.Refused(refused.name)
            }
        val url =
            when (val surface = case.fields.text("surface")) {
                "data" -> urls.data
                "rest" -> urls.rest
                "tiles" -> urls.tiles
                else -> throw AssertionError("no surface is called `$surface`")
            }
        return ConformanceOutcome.Value(FixtureValue.Text(url.toString()))
    }
}
