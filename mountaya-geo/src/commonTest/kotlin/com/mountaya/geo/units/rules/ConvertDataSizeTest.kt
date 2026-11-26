package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertDataSizeTest {
    @Test
    fun theKiloAndMegaNamesAreDecimal() {
        assertEquals(8.0, convertDataSize(1.0, DataSizeUnit.Byte, DataSizeUnit.Bit))
        assertEquals(1_000.0, convertDataSize(1.0, DataSizeUnit.Kilobyte, DataSizeUnit.Byte))
        assertEquals(1_000_000.0, convertDataSize(1.0, DataSizeUnit.Megabit, DataSizeUnit.Bit))
        assertEquals(30.0, convertDataSize(240.0, DataSizeUnit.Megabit, DataSizeUnit.Megabyte))
        assertEquals(0.008, convertDataSize(1.0, DataSizeUnit.Kilobyte, DataSizeUnit.Megabit))
    }

    /** A mebibyte, the unit the contract states its body ceilings in, is not a megabyte. */
    @Test
    fun theKibiAndMebiNamesAreBinary() {
        assertEquals(4_096.0, convertDataSize(4.0, DataSizeUnit.Kibibyte, DataSizeUnit.Byte))
        assertEquals(26_214_400.0, convertDataSize(25.0, DataSizeUnit.Mebibyte, DataSizeUnit.Byte))
        assertEquals(0.95367431640625, convertDataSize(1.0, DataSizeUnit.Megabyte, DataSizeUnit.Mebibyte))
        assertEquals(1.048576, convertDataSize(1.0, DataSizeUnit.Mebibyte, DataSizeUnit.Megabyte))
        assertEquals(1_024.0, convertDataSize(1.0, DataSizeUnit.Mebibyte, DataSizeUnit.Kibibyte))
    }

    @Test
    fun aSizeInItsOwnUnitIsReturnedAsItIs() {
        for (unit in DataSizeUnit.entries) assertEquals(0.1, convertDataSize(0.1, unit, unit))
    }
}
