package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueryDocumentTest {
    @Test
    fun twoSpacesToALevelOneFieldToALineAndOneNewlineAtTheEnd() {
        val document =
            QueryDocument.print(
                name = "terrainProfile",
                inputType = "TerrainProfileInput!",
                selection =
                    FieldSelection {
                        field("profile") { field("points") { field("altitude") } }
                        field("duration")
                    },
            )

        assertEquals(
            "query TerrainProfile(\$input: TerrainProfileInput!) {\n" +
                "  terrainProfile(input: \$input) {\n" +
                "    profile {\n" +
                "      points {\n" +
                "        altitude\n" +
                "      }\n" +
                "    }\n" +
                "    duration\n" +
                "  }\n" +
                "}\n",
            document,
        )
    }

    @Test
    fun theOperationIsNamedAfterItsFieldWithOnlyTheFirstLetterUpperCased() {
        val document = QueryDocument.print("pois", "POIsInput!", FieldSelection { field("total") })

        assertTrue(document.startsWith("query Pois(\$input: POIsInput!) {\n  pois(input: \$input) {\n"), document)
    }
}
