package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WaypointPropertyLinkTest {
    private val url = RestSchema.WaypointPropertyFormat.Url

    @Test
    fun aUrlFieldLinksWhereItsTrimmedValueIsAWebAddressAndNoOtherFieldDoes() {
        val page = WaypointDetails.Property("page", "Page", " https://refuge.example/page ", url)
        assertEquals("https://refuge.example/page", waypointPropertyLink(page))
        val script = WaypointDetails.Property("s", "S", "javascript:alert(1)", url)
        assertNull(waypointPropertyLink(script))
        val text =
            WaypointDetails.Property(
                "t",
                "T",
                "https://refuge.example/page",
                RestSchema.WaypointPropertyFormat.Text,
            )
        assertNull(waypointPropertyLink(text))
        assertNull(waypointPropertyLink(WaypointDetails.Property("n", "N", "https://a.example")))
    }
}
