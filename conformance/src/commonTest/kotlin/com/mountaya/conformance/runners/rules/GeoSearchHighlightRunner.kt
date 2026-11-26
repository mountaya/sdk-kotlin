package com.mountaya.conformance

import com.mountaya.geo.highlightParts
import kotlinx.coroutines.test.TestScope

/** `geo.search.highlight`: the parts the case's `title` is drawn in once its `query` is matched against it. */
object GeoSearchHighlightRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome =
        ConformanceOutcome.Value(
            FixtureValue.Array(
                highlightParts(case.fields.text("title"), case.fields.text("query")).map { part ->
                    FixtureValue.Object(
                        listOf(
                            "match" to FixtureValue.Bool(part.match),
                            "text" to FixtureValue.Text(part.text),
                        ),
                    )
                },
            ),
        )
}
