package com.mountaya.geo

/**
 * UnitPatterns is how one language writes a number with each unit a measurement is stated in:
 * `{0}` stands for the number, written by the platform's locale services. The patterns are the
 * short ones, and the narrow ones for an hour and a minute of a duration, which is how
 * `2h 35m` reads in English.
 *
 * Neither `java.text` nor Foundation on every platform formats a unit, so the patterns are this
 * table, one for both native SDKs, for the seven languages the contract's `Locales` is keyed by.
 * Each is CLDR 48's, as ICU 78 carries it and as `Intl.NumberFormat` writes it, with the space
 * each language puts between a number and its unit: none, a space, a no-break space or a narrow
 * no-break space. [minimumGroupingDigits] is CLDR's too: a language of 2 groups no number under
 * 10,000, so 1531 is `1531` in Spanish and Italian and 15,310 is `15.310`.
 */
internal class UnitPatterns(
    val kilometer: String,
    val mile: String,
    val meter: String,
    val foot: String,
    val centimeter: String,
    val inch: String,
    val percent: String,
    val kilometerPerHour: String,
    val milePerHour: String,
    val minutePerKilometer: String,
    val minutePerMile: String,
    val celsius: String,
    val fahrenheit: String,
    val hour: String,
    val minute: String,
    val minimumGroupingDigits: Int,
)

/** The table, by primary language subtag. */
private val unitPatternsByLanguage: Map<String, UnitPatterns> =
    mapOf(
        "de" to
            UnitPatterns(
                kilometer = "{0} km",
                mile = "{0} mi",
                meter = "{0} m",
                foot = "{0} ft",
                centimeter = "{0} cm",
                inch = "{0} in",
                percent = "{0} %",
                kilometerPerHour = "{0} km/h",
                milePerHour = "{0} mi/h",
                minutePerKilometer = "{0} Min./km",
                minutePerMile = "{0} Min./mi",
                celsius = "{0} °C",
                fahrenheit = "{0} °F",
                hour = "{0}h",
                minute = "{0} Min.",
                minimumGroupingDigits = 1,
            ),
        "en" to
            UnitPatterns(
                kilometer = "{0} km",
                mile = "{0} mi",
                meter = "{0} m",
                foot = "{0} ft",
                centimeter = "{0} cm",
                inch = "{0} in",
                percent = "{0}%",
                kilometerPerHour = "{0} km/h",
                milePerHour = "{0} mph",
                minutePerKilometer = "{0} min/km",
                minutePerMile = "{0} min/mi",
                celsius = "{0}°C",
                fahrenheit = "{0}°F",
                hour = "{0}h",
                minute = "{0}m",
                minimumGroupingDigits = 1,
            ),
        "es" to
            UnitPatterns(
                kilometer = "{0} km",
                mile = "{0} mi",
                meter = "{0} m",
                foot = "{0} ft",
                centimeter = "{0} cm",
                inch = "{0} in",
                percent = "{0} %",
                kilometerPerHour = "{0} km/h",
                milePerHour = "{0} mi/h",
                minutePerKilometer = "{0} min/km",
                minutePerMile = "{0} min/mi",
                celsius = "{0} °C",
                fahrenheit = "{0} °F",
                hour = "{0}h",
                minute = "{0}min",
                minimumGroupingDigits = 2,
            ),
        "fr" to
            UnitPatterns(
                kilometer = "{0} km",
                mile = "{0} mi",
                meter = "{0} m",
                foot = "{0} pi",
                centimeter = "{0} cm",
                inch = "{0} po",
                percent = "{0} %",
                kilometerPerHour = "{0} km/h",
                milePerHour = "{0} mi/h",
                minutePerKilometer = "{0} min/km",
                minutePerMile = "{0} min/mi",
                celsius = "{0} °C",
                fahrenheit = "{0} °F",
                hour = "{0}h",
                minute = "{0}min",
                minimumGroupingDigits = 1,
            ),
        "it" to
            UnitPatterns(
                kilometer = "{0} km",
                mile = "{0} mi",
                meter = "{0} m",
                foot = "{0} ft",
                centimeter = "{0} cm",
                inch = "{0} in",
                percent = "{0}%",
                kilometerPerHour = "{0} km/h",
                milePerHour = "{0} mi/h",
                minutePerKilometer = "{0} min/km",
                minutePerMile = "{0} min/mi",
                celsius = "{0} °C",
                fahrenheit = "{0} °F",
                hour = "{0}h",
                minute = "{0}min",
                minimumGroupingDigits = 2,
            ),
        "ja" to
            UnitPatterns(
                kilometer = "{0} km",
                mile = "{0} mi",
                meter = "{0} m",
                foot = "{0} ft",
                centimeter = "{0} cm",
                inch = "{0} in",
                percent = "{0}%",
                kilometerPerHour = "{0} km/h",
                milePerHour = "{0} mph",
                minutePerKilometer = "{0} 分/km",
                minutePerMile = "{0} 分/mi",
                celsius = "{0}°C",
                fahrenheit = "{0}°F",
                hour = "{0}h",
                minute = "{0}m",
                minimumGroupingDigits = 1,
            ),
        "zh" to
            UnitPatterns(
                kilometer = "{0}公里",
                mile = "{0}英里",
                meter = "{0}米",
                foot = "{0}英尺",
                centimeter = "{0}厘米",
                inch = "{0}英寸",
                percent = "{0}%",
                kilometerPerHour = "{0} km/h",
                milePerHour = "{0} mi/h",
                minutePerKilometer = "{0}分钟/公里",
                minutePerMile = "{0}分钟/英里",
                celsius = "{0}°C",
                fahrenheit = "{0}°F",
                hour = "{0}小时",
                minute = "{0}分钟",
                minimumGroupingDigits = 1,
            ),
    )

/**
 * Returns the patterns of [language], read by its primary subtag without regard to case, as a
 * label language is read: `fr-CA` writes French patterns and `frr` does not. A language the table
 * holds none for writes English ones, its digits still the locale's own.
 */
internal fun unitPatterns(language: String): UnitPatterns =
    unitPatternsByLanguage[primaryLanguageOf(language)] ?: unitPatternsByLanguage.getValue("en")

/** The languages the table holds patterns for, the ones the contract's `Locales` is keyed by. */
internal val unitPatternLanguages: Set<String> get() = unitPatternsByLanguage.keys
