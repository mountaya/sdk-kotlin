package com.mountaya.build.contract

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublishedNamesTest {
    /** What a klib dump declaring each of [lines], `<kind> <path>` with the path after the package, reads as. */
    private fun declaring(vararg lines: String): ApiDeclarations =
        ApiDeclarations.read(
            listOf(
                (
                    listOf("// Klib ABI Dump") +
                        lines.map { line ->
                            val (kind, path) = line.split(' ', limit = 2)
                            "$kind x // com.mountaya.x/$path|null[0]"
                        }
                ).joinToString("\n"),
            ),
        )

    @Test
    fun aNameIsDeclaredWhereADumpDeclaresItsPathAndATypeNeverAnswersAFunction() {
        val type = declaring("class ProfileTotals")
        val function = declaring("fun profileTotals")

        assertTrue(PublishedNames.isDeclared("ProfileTotals", type))
        assertFalse(PublishedNames.isDeclared("profileTotals", type))
        assertTrue(PublishedNames.isDeclared("profileTotals", function))
        assertFalse(PublishedNames.isDeclared("ProfileTotals", function))
    }

    /** The column records this SDK's own spelling, so no case is folded for it. */
    @Test
    fun anAcronymAnswersOnlyAsItIsSpelled() {
        val api = declaring("class POIStyle", "val directionsGeoJSON")

        assertTrue(PublishedNames.isDeclared("POIStyle", api))
        assertTrue(PublishedNames.isDeclared("directionsGeoJSON", api))
        assertFalse(PublishedNames.isDeclared("PoiStyle", api))
        assertFalse(PublishedNames.isDeclared("directionsGeoJson", api))
    }

    @Test
    fun aNestedTypeAnswersThroughTheTypeItIsNestedInAndNeverByItsOwnNameAlone() {
        val api = declaring("class ApiException.IdempotentAnswer")

        assertTrue(PublishedNames.isDeclared("ApiException.IdempotentAnswer", api))
        assertFalse(PublishedNames.isDeclared("IdempotentAnswer", api))
    }

    @Test
    fun aMemberAnswersThroughItsOwnTypeACompanionsThroughItsTypeAndNoOther() {
        val api = declaring("fun MapEngine.fitBounds", "val MapPaint.Companion.standard", "fun SearchEngine.setQuery")

        assertTrue(PublishedNames.isDeclared("MapEngine.fitBounds", api))
        assertTrue(PublishedNames.isDeclared("MapPaint.standard", api))
        assertFalse(PublishedNames.isDeclared("MapPaint.Companion.standard", api))
        assertFalse(PublishedNames.isDeclared("SearchEngine.fitBounds", api))
        assertFalse(PublishedNames.isDeclared("fitBounds", api))
    }

    /**
     * A class ends in `Exception` where the contract writes `Error` (SPEC.md section 11), and the column
     * records it so: the contract's own spelling names nothing here.
     */
    @Test
    fun anErrorIsDeclaredUnderTheExceptionTheColumnRecordsAndNotUnderTheContractsSpelling() {
        val api = declaring("class ApiException", "val WebhookSignatureException.reason", "fun isApiException")

        assertTrue(PublishedNames.isDeclared("ApiException", api))
        assertTrue(PublishedNames.isDeclared("WebhookSignatureException.reason", api))
        assertTrue(PublishedNames.isDeclared("isApiException", api))
        assertFalse(PublishedNames.isDeclared("ApiError", api))
        assertFalse(PublishedNames.isDeclared("WebhookSignatureError.reason", api))
        assertFalse(PublishedNames.isDeclared("isApiError", api))
    }

    @Test
    fun aJvmNameIsReadUnmangledAndATopLevelPropertyUnderItsOwnName() {
        val api =
            ApiDeclarations.read(
                listOf(
                    listOf(
                        "public final class com/mountaya/client/ConstructWebhookEventKt {",
                        "\tpublic static final fun constructWebhookEvent-8Mi8wO0 ([BLjava/lang/String;)V",
                        "\tpublic static final fun getLivelyPalette ()Ljava/util/List;",
                        "}",
                    ).joinToString("\n"),
                ),
            )

        assertTrue(PublishedNames.isDeclared("constructWebhookEvent", api))
        assertTrue(PublishedNames.isDeclared("livelyPalette", api))
        assertFalse(PublishedNames.isDeclared("ConstructWebhookEventKt", api))
    }
}
