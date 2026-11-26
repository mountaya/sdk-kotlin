package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoadAccessKindOfTest {
    private fun rest(vararg values: String) = roadAccessKindOf(values.map(RestSchema::RoadAccessRestrictionType))

    private fun data(vararg values: String) = roadAccessKindOf(values.map(DataSchema::RoadAccessRestrictionType))

    @Test
    fun eachValueTheContractNamesReadsAsItsKindAlone() {
        assertEquals(RoadAccessKind.Forbidden, rest("no"))
        assertEquals(RoadAccessKind.Private, rest("private"))
        assertEquals(RoadAccessKind.Permit, rest("permit"))
        assertEquals(RoadAccessKind.Service, rest("service"))
        assertEquals(RoadAccessKind.LocalAccess, rest("destination"))
        assertEquals(RoadAccessKind.LocalAccess, rest("customers"))
        assertEquals(RoadAccessKind.LocalAccess, rest("delivery"))
        assertEquals(RoadAccessKind.Restricted, rest("restricted"))
        assertEquals(RoadAccessKind.Restricted, rest("unknown"))
    }

    @Test
    fun aValueOfNoKindAndNoValueAtAllReadAsNone() {
        assertNull(rest("permissive"))
        assertNull(rest("agricultural"))
        assertNull(rest("forestry"))
        assertNull(rest("agricultural", "forestry", "permissive"))
        assertNull(rest())
        assertNull(data())
    }

    @Test
    fun theMostSevereKindAmongTheValuesWinsWhateverTheirOrder() {
        assertEquals(RoadAccessKind.Private, rest("destination", "private"))
        assertEquals(RoadAccessKind.Private, rest("private", "destination"))
        assertEquals(RoadAccessKind.Forbidden, rest("permit", "private", "no"))
        assertEquals(RoadAccessKind.Service, rest("customers", "service"))
        assertEquals(RoadAccessKind.LocalAccess, rest("unknown", "delivery"))
        assertEquals(RoadAccessKind.Permit, rest("permissive", "permit"))
    }

    @Test
    fun aValueTheTableDoesNotNameReadsAsRestricted() {
        assertEquals(RoadAccessKind.Restricted, rest("military"))
        assertEquals(RoadAccessKind.Restricted, rest("permissive", "military"))
        assertEquals(RoadAccessKind.Permit, rest("military", "permit"))
        assertEquals(RoadAccessKind.Restricted, rest(""))
        assertEquals(RoadAccessKind.Restricted, rest(" private"))
    }

    @Test
    fun theDataApiSpellingReadsAsRestsAndSoDoesEitherSpellingInEitherType() {
        assertEquals(RoadAccessKind.Private, data("PRIVATE"))
        assertEquals(RoadAccessKind.LocalAccess, data("PERMISSIVE", "DESTINATION"))
        assertNull(data("AGRICULTURAL", "FORESTRY"))
        assertEquals(RoadAccessKind.Private, rest("PRIVATE"))
        assertEquals(RoadAccessKind.Private, data("private"))
        assertEquals(RoadAccessKind.Private, rest("Private"))
    }

    /** Only A to Z fold: a capital outside ASCII, or a full-width letter, names another value. */
    @Test
    fun onlyTheAsciiCapitalsFold() {
        assertEquals(RoadAccessKind.Restricted, rest("PRİVATE"))
        assertEquals(RoadAccessKind.Restricted, data("ＰＲＩＶＡＴＥ"))
    }

    @Test
    fun theTableNamesEveryValueEachSurfaceSpellsAndBothSpellingsReadAlike() {
        val table =
            mapOf(
                "no" to RoadAccessKind.Forbidden,
                "private" to RoadAccessKind.Private,
                "permit" to RoadAccessKind.Permit,
                "service" to RoadAccessKind.Service,
                "destination" to RoadAccessKind.LocalAccess,
                "customers" to RoadAccessKind.LocalAccess,
                "delivery" to RoadAccessKind.LocalAccess,
                "restricted" to RoadAccessKind.Restricted,
                "unknown" to RoadAccessKind.Restricted,
                "permissive" to null,
                "agricultural" to null,
                "forestry" to null,
            )
        assertEquals(table, roadAccessValueKinds.mapKeys { it.key.value })
        val rest =
            listOf(
                RestSchema.RoadAccessRestrictionType.No,
                RestSchema.RoadAccessRestrictionType.Customers,
                RestSchema.RoadAccessRestrictionType.Destination,
                RestSchema.RoadAccessRestrictionType.Delivery,
                RestSchema.RoadAccessRestrictionType.Private,
                RestSchema.RoadAccessRestrictionType.Permissive,
                RestSchema.RoadAccessRestrictionType.Permit,
                RestSchema.RoadAccessRestrictionType.Restricted,
                RestSchema.RoadAccessRestrictionType.Unknown,
                RestSchema.RoadAccessRestrictionType.Service,
                RestSchema.RoadAccessRestrictionType.Agricultural,
                RestSchema.RoadAccessRestrictionType.Forestry,
            )
        val data =
            listOf(
                DataSchema.RoadAccessRestrictionType.No,
                DataSchema.RoadAccessRestrictionType.Customers,
                DataSchema.RoadAccessRestrictionType.Destination,
                DataSchema.RoadAccessRestrictionType.Delivery,
                DataSchema.RoadAccessRestrictionType.Private,
                DataSchema.RoadAccessRestrictionType.Permissive,
                DataSchema.RoadAccessRestrictionType.Permit,
                DataSchema.RoadAccessRestrictionType.Restricted,
                DataSchema.RoadAccessRestrictionType.Unknown,
                DataSchema.RoadAccessRestrictionType.Service,
                DataSchema.RoadAccessRestrictionType.Agricultural,
                DataSchema.RoadAccessRestrictionType.Forestry,
            )
        assertEquals(table.keys, rest.map { it.value }.toSet())
        for ((restValue, dataValue) in rest.zip(data)) {
            assertEquals(table[restValue.value], roadAccessKindOf(listOf(restValue)), restValue.value)
            assertEquals(table[restValue.value], roadAccessKindOf(listOf(dataValue)), dataValue.value)
        }
    }
}
