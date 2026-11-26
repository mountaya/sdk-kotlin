package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * StatStripCell is one figure the stat strip states (`geo.profile.stat-strip`): which it is, and its value in
 * meters, seconds or degrees, or the difficulty as the itinerary writes it, before a formatter writes any of it
 * for a reader.
 *
 * Two are equal where they are of one figure and hold the same value, a number by the same-number relation and
 * a difficulty as written.
 */
public sealed interface StatStripCell {
    /** The figure the cell states. */
    public val stat: StatStripStat

    /** The cell's value where it is a number, null for the difficulty. */
    public val number: Double?

    /** Distance is a length, in meters. */
    public class Distance(
        override val number: Double,
    ) : StatStripCell {
        override val stat: StatStripStat get() = StatStripStat.Distance

        override fun equals(other: Any?): Boolean = other is Distance && sameNumber(other.number, number)

        override fun hashCode(): Int = stat.hashCode() * 31 + number.hashCode()

        override fun toString(): String = "StatStripCell.Distance($number)"
    }

    /** Duration is a time, in seconds. */
    public class Duration(
        override val number: Double,
    ) : StatStripCell {
        override val stat: StatStripStat get() = StatStripStat.Duration

        override fun equals(other: Any?): Boolean = other is Duration && sameNumber(other.number, number)

        override fun hashCode(): Int = stat.hashCode() * 31 + number.hashCode()

        override fun toString(): String = "StatStripCell.Duration($number)"
    }

    /** Ascent is the meters climbed. */
    public class Ascent(
        override val number: Double,
    ) : StatStripCell {
        override val stat: StatStripStat get() = StatStripStat.Ascent

        override fun equals(other: Any?): Boolean = other is Ascent && sameNumber(other.number, number)

        override fun hashCode(): Int = stat.hashCode() * 31 + number.hashCode()

        override fun toString(): String = "StatStripCell.Ascent($number)"
    }

    /** Descent is the meters lost, stated positive. */
    public class Descent(
        override val number: Double,
    ) : StatStripCell {
        override val stat: StatStripStat get() = StatStripStat.Descent

        override fun equals(other: Any?): Boolean = other is Descent && sameNumber(other.number, number)

        override fun hashCode(): Int = stat.hashCode() * 31 + number.hashCode()

        override fun toString(): String = "StatStripCell.Descent($number)"
    }

    /** Elevation is an altitude, in meters above sea level. */
    public class Elevation(
        override val number: Double,
    ) : StatStripCell {
        override val stat: StatStripStat get() = StatStripStat.Elevation

        override fun equals(other: Any?): Boolean = other is Elevation && sameNumber(other.number, number)

        override fun hashCode(): Int = stat.hashCode() * 31 + number.hashCode()

        override fun toString(): String = "StatStripCell.Elevation($number)"
    }

    /** Grade is a slope, in degrees. */
    public class Grade(
        override val number: Double,
    ) : StatStripCell {
        override val stat: StatStripStat get() = StatStripStat.Grade

        override fun equals(other: Any?): Boolean = other is Grade && sameNumber(other.number, number)

        override fun hashCode(): Int = stat.hashCode() * 31 + number.hashCode()

        override fun toString(): String = "StatStripCell.Grade($number)"
    }

    /** Difficulty is the hardest difficulty, as the itinerary writes it. */
    public class Difficulty(
        /** The difficulty, as the itinerary writes it. */
        public val difficulty: RestSchema.TrailDifficultyType,
    ) : StatStripCell {
        override val stat: StatStripStat get() = StatStripStat.Difficulty

        override val number: Double? get() = null

        override fun equals(other: Any?): Boolean = other is Difficulty && other.difficulty == difficulty

        override fun hashCode(): Int = stat.hashCode() * 31 + difficulty.hashCode()

        override fun toString(): String = "StatStripCell.Difficulty($difficulty)"
    }
}
