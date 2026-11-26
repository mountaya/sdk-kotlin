package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TrailDifficultyRankTest {
    @Test
    fun eachGradeIsRankedWithinItsScale() {
        val ranks =
            mapOf(
                "hiking" to 1,
                "mountain-hiking" to 2,
                "demanding-mountain-hiking" to 3,
                "alpine-hiking" to 4,
                "demanding-alpine-hiking" to 5,
                "difficult-alpine-hiking" to 6,
                "mtb-0" to 0,
                "mtb-1" to 1,
                "mtb-2" to 2,
                "mtb-3" to 3,
                "mtb-4" to 4,
                "mtb-5" to 5,
                "mtb-6" to 6,
            )
        for ((grade, rank) in ranks) {
            assertEquals(
                rank,
                trailDifficultyRank(RestSchema.TrailDifficultyType(grade)),
                grade,
            )
        }
    }

    @Test
    fun anUnknownGradeOrOneThisBuildHasNeverHeardOfHasNoRank() {
        assertNull(trailDifficultyRank(RestSchema.TrailDifficultyType.Unknown))
        assertNull(trailDifficultyRank(RestSchema.TrailDifficultyType("via-ferrata-c")))
    }
}
