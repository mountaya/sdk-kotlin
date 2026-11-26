package com.mountaya.conformance

import com.mountaya.geo.DateLength
import com.mountaya.geo.TimeLength
import com.mountaya.geo.formatDateTime
import com.mountaya.geo.formatNumber
import kotlinx.coroutines.test.TestScope
import kotlin.time.Instant

/**
 * `geo.format.locale-services`: a number, a date or a time of day written by the platform's locale
 * services for the case's `locale`, never assembled. A number takes the one option the fixture names,
 * `maximumFractionDigits`; a `dateTime` case is the fixture's `referenceDate`, the constant every such
 * case reads, in the fixture's `timeZone` or the one the case names, its date at the [DateLength] its
 * `dateLength` names, its time of day at the [TimeLength] its `timeLength` names, or both, and null
 * where the zone data holds no such zone. A case naming neither length, any other option and any other
 * length fail the case rather than being read past, so no case is read as the long date
 * [formatDateTime] writes where no length is named.
 *
 * The strings are one locale-data vintage's: a platform whose data writes another fails the case
 * there, and no digit is assembled to match.
 */
object GeoFormatLocaleServicesRunner : ConformanceRunner {
    /** Each date length a case may name, by the word the fixture spells it with. */
    private val dateLengths =
        mapOf(
            "short" to DateLength.Short,
            "medium" to DateLength.Medium,
            "long" to DateLength.Long,
            "full" to DateLength.Full,
        )

    /** Each time length a case may name, by the word the fixture spells it with. */
    private val timeLengths =
        mapOf(
            "short" to TimeLength.Short,
            "medium" to TimeLength.Medium,
        )

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(case, rules = emptyMap(), inputs = setOf("referenceDate", "timeZone"))
        val input = case.fields
        val locale = input.text("locale")
        val options = input.objectOrNull("options")
        val text =
            when (val kind = input.text("kind")) {
                "number" -> {
                    val unknown = options?.keys.orEmpty() - "maximumFractionDigits"
                    if (unknown.isNotEmpty()) {
                        throw AssertionError(
                            "the runner knows no number option ${unknown.sorted()}",
                        )
                    }
                    val value = input.number("value")
                    val digits = options?.numberOrNull("maximumFractionDigits")?.toInt()
                    digits?.let { formatNumber(value, locale, it) } ?: formatNumber(value, locale)
                }

                "dateTime" -> {
                    if (options != null) throw AssertionError("a date-time case names its lengths, and no options")
                    val dateLength =
                        input.textOrNull("dateLength")?.let {
                            dateLengths[it] ?: throw AssertionError("no date length is called `$it`")
                        }
                    val timeLength =
                        input.textOrNull("timeLength")?.let {
                            timeLengths[it] ?: throw AssertionError("no time length is called `$it`")
                        }
                    if (dateLength == null && timeLength == null) {
                        throw AssertionError("a date-time case names a date length, a time length or both")
                    }
                    val constants = case.root("constants")
                    formatDateTime(
                        Instant.parse(constants.text("referenceDate")),
                        locale,
                        input.textOrNull("timeZone") ?: constants.text("timeZone"),
                        dateLength,
                        timeLength,
                    ) ?: return ConformanceOutcome.Value(FixtureValue.Null)
                }

                else -> {
                    throw AssertionError("`$kind` is no kind the runner knows")
                }
            }
        return ConformanceOutcome.Value(FixtureValue.Text(text))
    }
}
