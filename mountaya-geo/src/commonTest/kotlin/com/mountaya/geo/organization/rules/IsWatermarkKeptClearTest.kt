package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsWatermarkKeptClearTest {
    @Test
    fun theFrameIsHeldWhileTheReadWaitsAndInEveryStateThatDrawsTheMark() {
        val holding =
            listOf(
                OrganizationRead.Waiting,
                OrganizationRead.Answered(whiteLabel = false),
                OrganizationRead.Failed,
                OrganizationRead.TimedOut,
                OrganizationRead.NoKey,
            )

        for (read in holding) assertTrue(isWatermarkKeptClear(read), "$read holds the frame")
    }

    @Test
    fun theFrameIsReleasedOnlyOnceTheReadAnswersWhiteLabel() {
        assertFalse(isWatermarkKeptClear(OrganizationRead.Answered(whiteLabel = true)))
    }

    /** Every read that draws the mark holds its frame, and the one read that holds none draws no mark. */
    @Test
    fun theFrameIsHeldWhereverTheMarkIsDrawnAndWhileTheReadWaits() {
        val reads =
            listOf(
                OrganizationRead.Waiting,
                OrganizationRead.Answered(whiteLabel = false),
                OrganizationRead.Answered(whiteLabel = true),
                OrganizationRead.Failed,
                OrganizationRead.TimedOut,
                OrganizationRead.NoKey,
            )

        for (read in reads) {
            val drawn = watermarkOf(read, RestSchema.Language.En) != null
            assertEquals(drawn || read == OrganizationRead.Waiting, isWatermarkKeptClear(read), "$read")
        }
    }
}
