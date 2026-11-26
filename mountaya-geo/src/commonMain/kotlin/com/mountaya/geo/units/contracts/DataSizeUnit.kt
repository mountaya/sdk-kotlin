package com.mountaya.geo

/**
 * DataSizeUnit is a unit an amount of data is stated in. The bit, the byte and their decimal
 * multiples are spelled by their ECMA-402 identifiers and take decimal prefixes, so a kilobyte is
 * 1,000 bytes and a megabit 1,000,000 bits. [Kibibyte] and [Mebibyte] are the binary units, 1,024
 * bytes and 1,024 kibibytes, under their IEC names, which ECMA-402 does not list; the contract
 * states its body ceilings in them.
 */
public enum class DataSizeUnit(
    /** The contract's unit id, as `geo/units-convert.json` spells it. */
    public val id: String,
) {
    /** The bit. */
    Bit("bit"),

    /** Eight bits. */
    Byte("byte"),

    /** 1,024 bytes. */
    Kibibyte("kibibyte"),

    /** 1,000 bits. */
    Kilobit("kilobit"),

    /** 1,000 bytes. */
    Kilobyte("kilobyte"),

    /** 1,024 kibibytes, 1,048,576 bytes. */
    Mebibyte("mebibyte"),

    /** 1,000,000 bits. */
    Megabit("megabit"),

    /** 1,000,000 bytes. */
    Megabyte("megabyte"),
}
