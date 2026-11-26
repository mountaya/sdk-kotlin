package com.mountaya.client

import com.mountaya.client.StructuredFieldList.Item
import com.mountaya.client.StructuredFieldList.Member
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StructuredFieldListTest {
    @Test
    fun aListOfStringsWithParametersIsReadInOrderEachParameterTyped() {
        val members =
            assertNotNull(
                StructuredFieldList.parse(""""minute";q=60;w=60, "month";q=100000;qu="requests";pk=:Y2xp:;x"""),
                "the field list is read",
            )
        assertEquals(2, members.size)
        val month = assertIs<Member.Single>(members[1])
        assertEquals(Item.Text("month"), month.item)
        assertEquals(listOf("q", "qu", "pk", "x"), month.parameters.map { it.key })
        assertEquals(Item.Integer(100_000), month.parameters[0].value)
        assertEquals(Item.Text("requests"), month.parameters[1].value)
        assertEquals(Item.Bytes("cli".encodeToByteArray().toList()), month.parameters[2].value)
        assertEquals(Item.Flag(true), month.parameters[3].value)
    }

    @Test
    fun everyBareItemOfRfc9651IsReadAnInnerListAmongTheMembers() {
        val members =
            assertNotNull(
                StructuredFieldList.parse("""token/a:b, -1.25, ?0, @1659578233, %"caf%c3%a9", (1 "two");p"""),
                "the field list is read",
            )
        assertEquals(6, members.size)
        assertEquals(Member.Single(Item.Token("token/a:b"), emptyList()), members[0])
        assertEquals(Member.Single(Item.Decimal(-1.25), emptyList()), members[1])
        assertEquals(Member.Single(Item.Flag(false), emptyList()), members[2])
        assertEquals(Member.Single(Item.Date(1_659_578_233), emptyList()), members[3])
        assertEquals(Member.Single(Item.DisplayText("café"), emptyList()), members[4])
        val inner = assertIs<Member.InnerList>(members[5])
        assertEquals(listOf(Item.Integer(1), Item.Text("two")), inner.items.map { it.item })
        assertEquals(listOf("p"), inner.parameters.map { it.key })
    }

    @Test
    fun aFieldThatDoesNotParseIsNoListAtAllRatherThanTheMembersBeforeTheFault() {
        assertNull(StructuredFieldList.parse(""""minute;q=60"""))
        assertNull(StructuredFieldList.parse(""""minute";r=59;t="""))
        assertNull(StructuredFieldList.parse(""""a", """))
        assertNull(StructuredFieldList.parse("1.2345"))
        assertNull(StructuredFieldList.parse("1234567890123456"))
        assertEquals(emptyList(), StructuredFieldList.parse(""))
    }

    @Test
    fun aKeyWrittenTwiceKeepsItsLastValue() {
        val member =
            assertIs<Member.Single>(
                assertNotNull(StructuredFieldList.parse(""""m";q=1;q=2"""), "the field list is read")[0],
            )
        assertEquals(listOf(StructuredFieldList.Parameter("q", Item.Integer(2))), member.parameters)
    }
}
