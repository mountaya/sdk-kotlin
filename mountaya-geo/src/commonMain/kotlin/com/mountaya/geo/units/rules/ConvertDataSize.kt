package com.mountaya.geo

/**
 * convertDataSize converts [value] from one data size unit to another through the whole number of
 * bits each holds: eight to a byte, decimal prefixes under the kilo and mega names and binary ones
 * under the kibi and mebi names, so a megabyte is 1,000,000 bytes and a mebibyte 1,048,576.
 */
public fun convertDataSize(
    value: Double,
    from: DataSizeUnit,
    to: DataSizeUnit,
): Double = scaleByRatio(value, from.bits, to.bits)

/** The unit's size in bits. */
private val DataSizeUnit.bits: Long
    get() =
        when (this) {
            DataSizeUnit.Bit -> 1L
            DataSizeUnit.Byte -> BYTE
            DataSizeUnit.Kibibyte -> KIBIBYTE
            DataSizeUnit.Kilobit -> 1_000L
            DataSizeUnit.Kilobyte -> 1_000L * BYTE
            DataSizeUnit.Mebibyte -> 1_024L * KIBIBYTE
            DataSizeUnit.Megabit -> 1_000_000L
            DataSizeUnit.Megabyte -> 1_000_000L * BYTE
        }

private const val BYTE = 8L
private const val KIBIBYTE = 1_024L * BYTE
