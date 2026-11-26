package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class CollaborationFeedTest {
    private val reading = StreamedEvents.of(RestSchema.CollabStreamEvent.serializer(), "getOrganizationEvents")

    private fun role(
        event: String,
        data: String,
    ): EventRole? = reading?.read?.invoke(EventRecord(event, data, "1-0"))?.second

    @Test
    fun aChangeFrameMovesThePositionALiveFrameDoesNotAndSnapshotsOpenTheFeed() {
        assertSame(EventRole.openingSnapshot, role("presence.snapshot", """{"members":[]}"""))
        assertSame(EventRole.openingSnapshot, role("lock.snapshot", """{"locks":[]}"""))
        assertSame(
            EventRole.live,
            role(
                "presence.join",
                """{"user_id":"2b8d4f19-6a05-4e73-8c1f-9d3b7e05a24c","connected_at":"2026-05-12T09:14:09Z"}""",
            ),
        )
        assertSame(
            EventRole.ordinary,
            role(
                "segment.changed",
                """{"action":"updated","actor_id":"a","id":"s","itinerary_id":"i","kind":"segment",""" +
                    """"occurred_at":"2026-05-12T09:14:07Z","organization_id":"o","revision":4}""",
            ),
        )
        assertSame(EventRole.reconnect, role("reconnect", """{"reason":"interrupted"}"""))
        assertNull(role("brand-new.changed", "{}"))
        assertSame(EventStreamRules.collaborationFeed, reading?.rules)
    }

    @Test
    fun anItinerarysAndAZonesStreamAreEditorStreamsAndEveryOtherAListStream() {
        assertEquals(setOf("getItineraryEvents", "getZoneEvents"), CollaborationFeed.editorStreams)
        assertSame(EventStreamRules.collaborationEditor, CollaborationFeed.rules("getItineraryEvents"))
        assertSame(EventStreamRules.collaborationEditor, CollaborationFeed.rules("getZoneEvents"))
        assertSame(EventStreamRules.collaborationFeed, CollaborationFeed.rules("getOrganizationEvents"))
        assertSame(EventStreamRules.collaborationFeed, CollaborationFeed.rules("aStreamALaterContractAdds"))
        assertSame(
            EventStreamRules.collaborationEditor,
            StreamedEvents.of(RestSchema.CollabStreamEvent.serializer(), "getZoneEvents")?.rules,
        )
    }

    @Test
    fun aJoinNamesTheMemberWhoArrivedAndALeaveAndASnapshotNameNone() {
        val join = event("presence.join", """{"user_id":"7a4e","connected_at":"2026-05-12T09:14:09Z"}""")
        val leave = event("presence.leave", """{"user_id":"7a4e"}""")
        val snapshot = event("presence.snapshot", """{"members":[]}""")

        assertEquals("7a4e", join?.let(CollaborationFeed::arrivingMember))
        assertEquals("7a4e", join?.let { reading?.arrivingMember?.invoke(it) })
        assertNull(leave?.let(CollaborationFeed::arrivingMember))
        assertNull(snapshot?.let(CollaborationFeed::arrivingMember))
    }

    private fun event(
        name: String,
        data: String,
    ): RestSchema.CollabStreamEvent? = reading?.read?.invoke(EventRecord(name, data, null))?.first
}
