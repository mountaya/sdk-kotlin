package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.geo.generated.ElementLayout
import kotlinx.coroutines.test.TestScope

/**
 * `geo.organization.watermark`: the watermark a map draws once its organization's read stands where the case's `read`
 * says, `answered` with its `whiteLabel`, `waiting`, `failed`, `timed-out`, which fails a read as any failure does, or
 * `no-key`, in the words' `language`. The drawing, the frame's width and inset, the label's path and the links are held
 * to the ones `watermarkOf` draws by.
 *
 * Every case runs the rule. A case's `page`, the browser document a map is drawn in, is handed to it as a
 * `WatermarkPage`, its `origin` and whether it is `topLevel`, and null where the case names none, as `MapView` hands
 * none: a Kotlin map is drawn in no document, so only this suite hands the rule one. `firstPartyOrigins` is held to the
 * origins the rule omits the mark on.
 */
object GeoOrganizationWatermarkRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val drawing = GlyphDrawing.artwork.getValue(WATERMARK_DRAWING)
        holdConstantsExactly(
            case,
            mapOf(
                "drawing" to FixtureValue.Text(WATERMARK_DRAWING),
                "firstPartyOrigins" to FixtureValue.Array(watermarkFirstPartyOrigins.map { FixtureValue.Text(it) }),
                "inset" to FixtureValue.Number(ElementLayout.watermarkInset),
                "label" to FixtureValue.Text(WATERMARK_LABEL),
                "links" to
                    FixtureValue.Object(
                        watermarkLinks.map { (language, link) ->
                            language.value to
                                FixtureValue.Text(link)
                        },
                    ),
                "root" to FixtureValue.Text(WATERMARK_ROOT),
                "viewBox" to FixtureValue.Array(drawing.viewBox.map { FixtureValue.Number(it) }),
                "width" to FixtureValue.Number(ElementLayout.watermarkWidth),
            ),
        )
        val input = case.fields
        val page =
            input.objectOrNull("page")?.let { document ->
                WatermarkPage(origin = document.text("origin"), isTopLevel = document.bool("topLevel"))
            }
        val read =
            when (val written = input.text("read")) {
                "answered" -> OrganizationRead.Answered(input.bool("whiteLabel"))
                "waiting" -> OrganizationRead.Waiting
                "failed" -> OrganizationRead.Failed
                "timed-out" -> OrganizationRead.TimedOut
                "no-key" -> OrganizationRead.NoKey
                else -> throw AssertionError("no read stands at `$written`")
            }
        val mark =
            watermarkOf(read, RestSchema.Language(input.text("language")), page)
                ?: return ConformanceOutcome.Value(FixtureValue.Null)
        val name =
            GlyphDrawing.artwork.entries
                .firstOrNull { it.value == mark.drawing }
                ?.key
                ?: throw AssertionError("the watermark is drawn from no drawing of the glyph table")
        return ConformanceOutcome.Value(
            profileObject(
                "drawing" to FixtureValue.Text(name),
                "frame" to
                    profileObject(
                        "height" to FixtureValue.Number(mark.height),
                        "width" to FixtureValue.Number(mark.width),
                        "x" to FixtureValue.Number(mark.x),
                        "y" to FixtureValue.Number(mark.y),
                    ),
                "href" to FixtureValue.Text(mark.link),
                "label" to FixtureValue.Text(mark.label),
            ),
        )
    }
}
