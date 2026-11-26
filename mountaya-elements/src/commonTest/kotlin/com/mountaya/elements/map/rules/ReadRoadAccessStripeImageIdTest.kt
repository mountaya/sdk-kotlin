package com.mountaya.elements

import com.mountaya.geo.RoadAccessKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadRoadAccessStripeImageIdTest {
    @Test
    fun eachKindsStripeIdNamesThatKindAKindOfTwoWordsReadWhole() {
        for (kind in RoadAccessKind.entries) {
            assertEquals(
                kind,
                readRoadAccessStripeImageId(ROAD_ACCESS_STRIPE_IMAGE_PREFIX + kind.id),
            )
        }
        assertEquals(RoadAccessKind.LocalAccess, readRoadAccessStripeImageId("road-access-stripe-local-access"))
        assertEquals(RoadAccessKind.Restricted, readRoadAccessStripeImageId("road-access-stripe-restricted"))
    }

    @Test
    fun aTailThatIsNoKindOrAKindWrittenOtherwiseNamesNone() {
        for (id in listOf(
            "road-access-stripe-",
            "road-access-stripe-toll",
            "road-access-stripe-Permit",
            "road-access-stripe-permit ",
            "road-access-stripe-local_access",
        )) {
            assertNull(readRoadAccessStripeImageId(id), id)
        }
    }

    /** A prefix read exactly: a hyphen of another code point is another prefix. */
    @Test
    fun anIdUnderAnotherPrefixNamesNoneTheWaypointSpriteOfAKindsWarningIncluded() {
        for (id in listOf(
            "permit",
            "section-icon-road-access-permit-9c36b5",
            "road-access-permit",
            "road‐access-stripe-permit",
            "Road-access-stripe-permit",
        )) {
            assertNull(readRoadAccessStripeImageId(id), id)
        }
    }
}
