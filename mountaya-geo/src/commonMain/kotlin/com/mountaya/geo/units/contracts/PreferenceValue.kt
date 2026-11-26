package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * PreferenceValue is a value a preference takes, the option of a preferences picker a press names
 * (`geo.units.preference-choices`), in the wire's own vocabulary.
 *
 * Two are equal where they are of one preference and spell the same text.
 */
public sealed interface PreferenceValue {
    /** The preference the value is one of. */
    public val field: PreferenceField

    /** The value as the wire spells it, `per-distance`. */
    public val id: String

    /** Length is a measurement system. */
    public class Length(
        /** The system. */
        public val value: RestSchema.UnitLength,
    ) : PreferenceValue {
        override val field: PreferenceField get() = PreferenceField.Length
        override val id: String get() = value.value

        override fun equals(other: Any?): Boolean = sameValue(this, other)

        override fun hashCode(): Int = valueHash(this)

        override fun toString(): String = "Length($id)"
    }

    /** Pace is a form a travel rate takes. */
    public class Pace(
        /** The form. */
        public val value: RestSchema.UnitPace,
    ) : PreferenceValue {
        override val field: PreferenceField get() = PreferenceField.Pace
        override val id: String get() = value.value

        override fun equals(other: Any?): Boolean = sameValue(this, other)

        override fun hashCode(): Int = valueHash(this)

        override fun toString(): String = "Pace($id)"
    }

    /** Slope is a slope notation. */
    public class Slope(
        /** The notation. */
        public val value: RestSchema.UnitSlope,
    ) : PreferenceValue {
        override val field: PreferenceField get() = PreferenceField.Slope
        override val id: String get() = value.value

        override fun equals(other: Any?): Boolean = sameValue(this, other)

        override fun hashCode(): Int = valueHash(this)

        override fun toString(): String = "Slope($id)"
    }

    /** Temperature is a temperature scale. */
    public class Temperature(
        /** The scale. */
        public val value: RestSchema.UnitTemperature,
    ) : PreferenceValue {
        override val field: PreferenceField get() = PreferenceField.Temperature
        override val id: String get() = value.value

        override fun equals(other: Any?): Boolean = sameValue(this, other)

        override fun hashCode(): Int = valueHash(this)

        override fun toString(): String = "Temperature($id)"
    }

    /** Language is a language, which the map labels in too. */
    public class Language(
        /** The language, as a tag. */
        public val value: RestSchema.Language,
    ) : PreferenceValue {
        override val field: PreferenceField get() = PreferenceField.Language
        override val id: String get() = value.value

        override fun equals(other: Any?): Boolean = sameValue(this, other)

        override fun hashCode(): Int = valueHash(this)

        override fun toString(): String = "Language($id)"
    }
}

/** Whether [other] is a value of the same preference as [value], spelled the same. */
private fun sameValue(
    value: PreferenceValue,
    other: Any?,
): Boolean = other is PreferenceValue && other.field == value.field && other.id == value.id

/** The hash of [value]'s preference and spelling. */
private fun valueHash(value: PreferenceValue): Int = value.field.hashCode() * 31 + value.id.hashCode()
