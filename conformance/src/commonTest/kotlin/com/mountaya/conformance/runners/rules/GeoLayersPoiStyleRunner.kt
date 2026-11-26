package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.PoiStyle
import com.mountaya.geo.poiCategoryStyles
import com.mountaya.geo.poiStyleOf
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.poi-style`: how a point of interest of the case's `category` is drawn, as `{color, glyph}`,
 * or null where Geo names no style for it. The table is held to the one Geo publishes, entry by entry.
 */
object GeoLayersPoiStyleRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val styles =
            FixtureValue.Object(
                poiCategoryStyles.entries.sortedBy { it.key.value }.map { (category, style) ->
                    category.value to
                        written(style)
                },
            )
        holdDeclaredConstants(case, emptyMap(), values = mapOf("styles" to styles))
        val style = poiStyleOf(DataSchema.POICategory(case.fields.text("category")))
        return ConformanceOutcome.Value(style?.let(::written) ?: FixtureValue.Null)
    }

    /**
     * A point of interest's style as the fixture writes one, `{color, glyph}`. The fixture writes a colour as
     * `#rrggbb` alone, so a colour Geo states at another opacity fails the case.
     */
    private fun written(style: PoiStyle): FixtureValue {
        if (style.color.opacity != 1.0) {
            throw AssertionError("the glyph `${style.glyph}` is drawn at an opacity of ${style.color.opacity}")
        }
        return FixtureValue.Object(
            listOf("color" to FixtureValue.Text(style.color.hex), "glyph" to FixtureValue.Text(style.glyph)),
        )
    }
}
