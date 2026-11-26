// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

@file:kotlin.OptIn(com.mountaya.client.InternalMountayaApi::class)
@file:kotlinx.serialization.UseSerializers(com.mountaya.client.TimestampSerializer::class)

package com.mountaya.admin.generated

/**
 * WebhookEvent is a webhook event as Mountaya delivers it to an endpoint, typed by its `type`: one
 * class to each public type the contract's `webhooks` lists, carrying every member of the envelope,
 * the object the type carries at `data.object` and, at `data.previous_attributes`, the former value
 * of each field its change moved.
 *
 * An event this build cannot type is [Unknown], holding the contract's envelope,
 * `AdminSchema.WebhookEvent`: one of a type it does not know, which a later contract adds, or one
 * whose payload its types do not read. It is delivered rather than refused, so a receiver still
 * acknowledges it and deduplicates it by its `id`, and every member of the envelope is read off
 * every event alike. A type the contract adds is one more class, so a `when` over an event ends in
 * `else`.
 */
@kotlinx.serialization.Serializable(with = WebhookEvent.Serializer::class)
public abstract class WebhookEvent internal constructor() {
    /** The event's type, as the wire spells it. */
    public abstract val type: AdminSchema.WebhookEventType

    /**
     * Identifier of the event. Its leading bits are the moment it was recorded, so
     * identifiers sort by time. A delivery can arrive more than once, so a receiver
     * deduplicates on it.
     */
    public abstract val id: kotlin.String

    /** Always `webhook_event`. */
    public abstract val `object`: kotlin.String

    /**
     * The contract version the payload follows: the `info.version` of the document that
     * describes it.
     */
    public abstract val api_version: kotlin.String

    /** When the change was made. */
    public abstract val created_at: kotlin.time.Instant

    /** Identifier of the organization the change was made in. */
    public abstract val organization_id: kotlin.String

    /**
     * The task that recorded the event: the resource's path and what was done, such as
     * `organization.member.update`. The contract's `tasks.yaml` lists every task with
     * what triggers it and the event types it records.
     */
    public abstract val task: kotlin.String

    /** The object the event is about. */
    public abstract val related_object: AdminSchema.WebhookEventRelatedObject

    /**
     * The request that made the change, a change made signed in to Mountaya's web
     * application or by Mountaya's staff included. Null only when Mountaya made the
     * change on its own: a schedule, work finished after a request was answered, or the
     * payment provider's report.
     */
    public abstract val request: AdminSchema.WebhookEventRequest?

    /** Who made the change. */
    public abstract val actor: AdminSchema.WebhookEventActor

    /**
     * Typed is an event of a type this build knows: every member of the contract's envelope,
     * `AdminSchema.WebhookEvent`, but its `type`, which its class names, held in its [payload],
     * with the [data] that type carries.
     *
     * `Object` is the object the type carries at `data.object`, and `PreviousAttributes` the former
     * value of each field its change moved, `Nothing` for a type whose change moves none.
     */
    public abstract class Typed<Object : kotlin.Any, PreviousAttributes : kotlin.Any> internal constructor(
        /** The event as it was delivered, but its `type`, which its class names. */
        public val payload: WebhookEvent.Payload<Object, PreviousAttributes>,
    ) : WebhookEvent() {
        /** The object, and what the change moved. */
        public val data: WebhookEvent.PayloadData<Object, PreviousAttributes>
            get() = payload.data

        override val id: kotlin.String
            get() = payload.id

        override val `object`: kotlin.String
            get() = payload.`object`

        override val api_version: kotlin.String
            get() = payload.api_version

        override val created_at: kotlin.time.Instant
            get() = payload.created_at

        override val organization_id: kotlin.String
            get() = payload.organization_id

        override val task: kotlin.String
            get() = payload.task

        override val related_object: AdminSchema.WebhookEventRelatedObject
            get() = payload.related_object

        override val request: AdminSchema.WebhookEventRequest?
            get() = payload.request

        override val actor: AdminSchema.WebhookEventActor
            get() = payload.actor

        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.Typed<*, *> && other.type == this.type && other.payload == this.payload

        override fun hashCode(): kotlin.Int = 31 * this.type.hashCode() + this.payload.hashCode()

        override fun toString(): kotlin.String =
            "WebhookEvent(type=" + this.type + ", payload=" + this.payload + ")"
    }

    /** `collection.created`: A collection was created. */
    public class CollectionCreated(
        payload: WebhookEvent.Payload<AdminSchema.CollectionEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.created")
    }

    /** `collection.deleted`: A collection was deleted. */
    public class CollectionDeleted(
        payload: WebhookEvent.Payload<AdminSchema.CollectionEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.deleted")
    }

    /** `collection.itinerary.added`: An itinerary was added to a collection. */
    public class CollectionItineraryAdded(
        payload: WebhookEvent.Payload<AdminSchema.CollectionItineraryEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionItineraryEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.itinerary.added")
    }

    /** `collection.itinerary.moved`: An itinerary moved within a collection. */
    public class CollectionItineraryMoved(
        payload: WebhookEvent.Payload<AdminSchema.CollectionItineraryEventObject, WebhookEvent.CollectionItineraryMovedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionItineraryEventObject, WebhookEvent.CollectionItineraryMovedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.itinerary.moved")
    }

    /** `collection.itinerary.removed`: An itinerary left a collection. */
    public class CollectionItineraryRemoved(
        payload: WebhookEvent.Payload<AdminSchema.CollectionItineraryEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionItineraryEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.itinerary.removed")
    }

    /** `collection.share_link.created`: A collection's share link was created. */
    public class CollectionShareLinkCreated(
        payload: WebhookEvent.Payload<AdminSchema.CollectionShareLinkEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionShareLinkEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.share_link.created")
    }

    /** `collection.share_link.deleted`: A collection's share link was removed. */
    public class CollectionShareLinkDeleted(
        payload: WebhookEvent.Payload<AdminSchema.CollectionShareLinkEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionShareLinkEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.share_link.deleted")
    }

    /** `collection.share_link.rotated`: A collection's share link was rotated. */
    public class CollectionShareLinkRotated(
        payload: WebhookEvent.Payload<AdminSchema.CollectionShareLinkEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionShareLinkEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.share_link.rotated")
    }

    /** `collection.updated`: A collection's name, description or privacy changed. */
    public class CollectionUpdated(
        payload: WebhookEvent.Payload<AdminSchema.CollectionEventObject, WebhookEvent.CollectionUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionEventObject, WebhookEvent.CollectionUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.updated")
    }

    /** `collection.zone.added`: A zone was added to a collection. */
    public class CollectionZoneAdded(
        payload: WebhookEvent.Payload<AdminSchema.CollectionZoneEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionZoneEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.zone.added")
    }

    /** `collection.zone.removed`: A zone left a collection. */
    public class CollectionZoneRemoved(
        payload: WebhookEvent.Payload<AdminSchema.CollectionZoneEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.CollectionZoneEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("collection.zone.removed")
    }

    /** `itinerary.created`: An itinerary was created. */
    public class ItineraryCreated(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.created")
    }

    /** `itinerary.deleted`: An itinerary was deleted. */
    public class ItineraryDeleted(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.deleted")
    }

    /** `itinerary.published`: An itinerary was published. */
    public class ItineraryPublished(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.published")
    }

    /** `itinerary.segment.converted`: A segment's mode changed. */
    public class ItinerarySegmentConverted(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentEventObject, WebhookEvent.ItinerarySegmentConvertedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentEventObject, WebhookEvent.ItinerarySegmentConvertedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment.converted")
    }

    /** `itinerary.segment.created`: A segment was added to an itinerary. */
    public class ItinerarySegmentCreated(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment.created")
    }

    /** `itinerary.segment.deleted`: A segment was removed from an itinerary. */
    public class ItinerarySegmentDeleted(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment.deleted")
    }

    /** `itinerary.segment.merged`: A segment absorbed the one after it. */
    public class ItinerarySegmentMerged(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment.merged")
    }

    /** `itinerary.segment.split`: A segment was cut in two. */
    public class ItinerarySegmentSplit(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment.split")
    }

    /** `itinerary.segment.updated`: A segment changed. */
    public class ItinerarySegmentUpdated(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentEventObject, WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentEventObject, WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment.updated")
    }

    /** `itinerary.segment_group.created`: A segment group was created. */
    public class ItinerarySegmentGroupCreated(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentGroupEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentGroupEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment_group.created")
    }

    /** `itinerary.segment_group.deleted`: A segment group was dissolved. */
    public class ItinerarySegmentGroupDeleted(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentGroupEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentGroupEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment_group.deleted")
    }

    /** `itinerary.segment_group.updated`: A segment group changed. */
    public class ItinerarySegmentGroupUpdated(
        payload: WebhookEvent.Payload<AdminSchema.ItinerarySegmentGroupEventObject, WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.ItinerarySegmentGroupEventObject, WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.segment_group.updated")
    }

    /** `itinerary.share_link.created`: An itinerary's share link was created. */
    public class ItineraryShareLinkCreated(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryShareLinkEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryShareLinkEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.share_link.created")
    }

    /** `itinerary.share_link.deleted`: An itinerary's share link was removed. */
    public class ItineraryShareLinkDeleted(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryShareLinkEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryShareLinkEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.share_link.deleted")
    }

    /** `itinerary.share_link.rotated`: An itinerary's share link was rotated. */
    public class ItineraryShareLinkRotated(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryShareLinkEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryShareLinkEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.share_link.rotated")
    }

    /** `itinerary.unpublished`: An itinerary was taken down. */
    public class ItineraryUnpublished(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.unpublished")
    }

    /** `itinerary.updated`: An itinerary's name, description or privacy changed. */
    public class ItineraryUpdated(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryEventObject, WebhookEvent.ItineraryUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryEventObject, WebhookEvent.ItineraryUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.updated")
    }

    /** `itinerary.waypoint.created`: A waypoint was added to an itinerary. */
    public class ItineraryWaypointCreated(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryWaypointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryWaypointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.waypoint.created")
    }

    /** `itinerary.waypoint.deleted`: A waypoint was removed from an itinerary. */
    public class ItineraryWaypointDeleted(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryWaypointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryWaypointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.waypoint.deleted")
    }

    /** `itinerary.waypoint.updated`: A waypoint changed. */
    public class ItineraryWaypointUpdated(
        payload: WebhookEvent.Payload<AdminSchema.ItineraryWaypointEventObject, WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.ItineraryWaypointEventObject, WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("itinerary.waypoint.updated")
    }

    /** `organization.ai_provider_config.created`: An AI provider was configured. */
    public class OrganizationAiProviderConfigCreated(
        payload: WebhookEvent.Payload<AdminSchema.AiProviderConfigEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.AiProviderConfigEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.ai_provider_config.created")
    }

    /** `organization.ai_provider_config.deleted`: An AI provider configuration was deleted. */
    public class OrganizationAiProviderConfigDeleted(
        payload: WebhookEvent.Payload<AdminSchema.AiProviderConfigEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.AiProviderConfigEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.ai_provider_config.deleted")
    }

    /** `organization.ai_provider_config.updated`: An AI provider configuration changed. */
    public class OrganizationAiProviderConfigUpdated(
        payload: WebhookEvent.Payload<AdminSchema.AiProviderConfigEventObject, WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.AiProviderConfigEventObject, WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.ai_provider_config.updated")
    }

    /** `organization.billing.info.updated`: An organization's billing information changed. */
    public class OrganizationBillingInfoUpdated(
        payload: WebhookEvent.Payload<AdminSchema.BillingInfoEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.BillingInfoEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.info.updated")
    }

    /** `organization.billing.invoice.paid`: An invoice was paid. */
    public class OrganizationBillingInvoicePaid(
        payload: WebhookEvent.Payload<AdminSchema.InvoiceEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.InvoiceEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.invoice.paid")
    }

    /**
     * `organization.billing.invoice.payment_action_required`: An invoice's payment needs the
     * payer's action.
     */
    public class OrganizationBillingInvoicePaymentActionRequired(
        payload: WebhookEvent.Payload<AdminSchema.InvoiceEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.InvoiceEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.invoice.payment_action_required")
    }

    /** `organization.billing.invoice.payment_failed`: An invoice's payment failed. */
    public class OrganizationBillingInvoicePaymentFailed(
        payload: WebhookEvent.Payload<AdminSchema.InvoiceEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.InvoiceEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.invoice.payment_failed")
    }

    /** `organization.billing.payment_method.attached`: A payment method was saved. */
    public class OrganizationBillingPaymentMethodAttached(
        payload: WebhookEvent.Payload<AdminSchema.PaymentMethodEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.PaymentMethodEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.payment_method.attached")
    }

    /** `organization.billing.payment_method.detached`: A payment method was removed. */
    public class OrganizationBillingPaymentMethodDetached(
        payload: WebhookEvent.Payload<AdminSchema.PaymentMethodEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.PaymentMethodEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.payment_method.detached")
    }

    /**
     * `organization.billing.payment_method.updated`: A payment method became, or stopped being, the
     * default.
     */
    public class OrganizationBillingPaymentMethodUpdated(
        payload: WebhookEvent.Payload<AdminSchema.PaymentMethodEventObject, WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.PaymentMethodEventObject, WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.payment_method.updated")
    }

    /** `organization.billing.subscription.canceled`: A Data API subscription ended. */
    public class OrganizationBillingSubscriptionCanceled(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationSubscriptionEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationSubscriptionEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.subscription.canceled")
    }

    /** `organization.billing.subscription.created`: A Data API subscription was opened. */
    public class OrganizationBillingSubscriptionCreated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationSubscriptionEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationSubscriptionEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.subscription.created")
    }

    /** `organization.billing.subscription.paused`: A Data API subscription was paused. */
    public class OrganizationBillingSubscriptionPaused(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationSubscriptionEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationSubscriptionEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.subscription.paused")
    }

    /** `organization.billing.subscription.resumed`: A Data API subscription was resumed. */
    public class OrganizationBillingSubscriptionResumed(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationSubscriptionEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationSubscriptionEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.subscription.resumed")
    }

    /** `organization.billing.subscription.updated`: A Data API subscription changed. */
    public class OrganizationBillingSubscriptionUpdated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationSubscriptionEventObject, WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationSubscriptionEventObject, WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.billing.subscription.updated")
    }

    /** `organization.created`: An organization was created. */
    public class OrganizationCreated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.created")
    }

    /** `organization.entitlements.updated`: What an organization holds changed. */
    public class OrganizationEntitlementsUpdated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationEntitlementsEventObject, WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationEntitlementsEventObject, WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.entitlements.updated")
    }

    /** `organization.invitation.accepted`: An invitation was accepted. */
    public class OrganizationInvitationAccepted(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.invitation.accepted")
    }

    /** `organization.invitation.created`: Somebody was invited to an organization. */
    public class OrganizationInvitationCreated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.invitation.created")
    }

    /** `organization.invitation.declined`: An invitation was declined. */
    public class OrganizationInvitationDeclined(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.invitation.declined")
    }

    /** `organization.invitation.deleted`: An invitation was superseded. */
    public class OrganizationInvitationDeleted(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.invitation.deleted")
    }

    /** `organization.invitation.expired`: An invitation lapsed unanswered. */
    public class OrganizationInvitationExpired(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.invitation.expired")
    }

    /** `organization.invitation.revoked`: An invitation was withdrawn. */
    public class OrganizationInvitationRevoked(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationInvitationEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.invitation.revoked")
    }

    /** `organization.member.created`: Somebody joined an organization. */
    public class OrganizationMemberCreated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationMemberEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationMemberEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.member.created")
    }

    /** `organization.member.deleted`: A member left an organization or was removed from it. */
    public class OrganizationMemberDeleted(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationMemberEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationMemberEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.member.deleted")
    }

    /** `organization.member.updated`: A member's role changed. */
    public class OrganizationMemberUpdated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationMemberEventObject, WebhookEvent.OrganizationMemberUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationMemberEventObject, WebhookEvent.OrganizationMemberUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.member.updated")
    }

    /** `organization.publishable_key.created`: A publishable key was created. */
    public class OrganizationPublishableKeyCreated(
        payload: WebhookEvent.Payload<AdminSchema.PublishableKeyEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.PublishableKeyEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.publishable_key.created")
    }

    /** `organization.publishable_key.revoked`: A publishable key was revoked. */
    public class OrganizationPublishableKeyRevoked(
        payload: WebhookEvent.Payload<AdminSchema.PublishableKeyEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.PublishableKeyEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.publishable_key.revoked")
    }

    /** `organization.publishable_key.updated`: A publishable key's name or scopes changed. */
    public class OrganizationPublishableKeyUpdated(
        payload: WebhookEvent.Payload<AdminSchema.PublishableKeyEventObject, WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.PublishableKeyEventObject, WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.publishable_key.updated")
    }

    /** `organization.secret_key.created`: A secret key was created. */
    public class OrganizationSecretKeyCreated(
        payload: WebhookEvent.Payload<AdminSchema.SecretKeyEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.SecretKeyEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.secret_key.created")
    }

    /** `organization.secret_key.revoked`: A secret key was revoked. */
    public class OrganizationSecretKeyRevoked(
        payload: WebhookEvent.Payload<AdminSchema.SecretKeyEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.SecretKeyEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.secret_key.revoked")
    }

    /** `organization.secret_key.updated`: A secret key was renamed. */
    public class OrganizationSecretKeyUpdated(
        payload: WebhookEvent.Payload<AdminSchema.SecretKeyEventObject, WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.SecretKeyEventObject, WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.secret_key.updated")
    }

    /** `organization.settings.updated`: An organization's Map Publishing settings changed. */
    public class OrganizationSettingsUpdated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationSettingsEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationSettingsEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.settings.updated")
    }

    /** `organization.updated`: An organization's name changed. */
    public class OrganizationUpdated(
        payload: WebhookEvent.Payload<AdminSchema.OrganizationEventObject, WebhookEvent.OrganizationUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.OrganizationEventObject, WebhookEvent.OrganizationUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.updated")
    }

    /** `organization.webhook_endpoint.created`: A webhook endpoint was created. */
    public class OrganizationWebhookEndpointCreated(
        payload: WebhookEvent.Payload<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.webhook_endpoint.created")
    }

    /** `organization.webhook_endpoint.deleted`: A webhook endpoint was deleted. */
    public class OrganizationWebhookEndpointDeleted(
        payload: WebhookEvent.Payload<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.webhook_endpoint.deleted")
    }

    /** `organization.webhook_endpoint.disabled`: A webhook endpoint was disabled. */
    public class OrganizationWebhookEndpointDisabled(
        payload: WebhookEvent.Payload<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.webhook_endpoint.disabled")
    }

    /** `organization.webhook_endpoint.enabled`: A webhook endpoint was enabled. */
    public class OrganizationWebhookEndpointEnabled(
        payload: WebhookEvent.Payload<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.webhook_endpoint.enabled")
    }

    /** `organization.webhook_endpoint.failing`: A webhook endpoint has failed for 24 hours. */
    public class OrganizationWebhookEndpointFailing(
        payload: WebhookEvent.Payload<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.webhook_endpoint.failing")
    }

    /**
     * `organization.webhook_endpoint.recovered`: A webhook endpoint reported failing succeeded
     * again.
     */
    public class OrganizationWebhookEndpointRecovered(
        payload: WebhookEvent.Payload<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.webhook_endpoint.recovered")
    }

    /** `organization.webhook_endpoint.secret_rotated`: A webhook endpoint's secret was rotated. */
    public class OrganizationWebhookEndpointSecretRotated(
        payload: WebhookEvent.Payload<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.WebhookEndpointEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.webhook_endpoint.secret_rotated")
    }

    /** `organization.webhook_endpoint.updated`: A webhook endpoint changed. */
    public class OrganizationWebhookEndpointUpdated(
        payload: WebhookEvent.Payload<AdminSchema.WebhookEndpointEventObject, WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.WebhookEndpointEventObject, WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("organization.webhook_endpoint.updated")
    }

    /** `zone.created`: A zone was created. */
    public class ZoneCreated(
        payload: WebhookEvent.Payload<AdminSchema.ZoneEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ZoneEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("zone.created")
    }

    /** `zone.deleted`: A zone was deleted. */
    public class ZoneDeleted(
        payload: WebhookEvent.Payload<AdminSchema.ZoneEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ZoneEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("zone.deleted")
    }

    /** `zone.published`: A zone was published. */
    public class ZonePublished(
        payload: WebhookEvent.Payload<AdminSchema.ZoneEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ZoneEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("zone.published")
    }

    /** `zone.unpublished`: A zone was taken down. */
    public class ZoneUnpublished(
        payload: WebhookEvent.Payload<AdminSchema.ZoneEventObject, kotlin.Nothing>,
    ) : WebhookEvent.Typed<AdminSchema.ZoneEventObject, kotlin.Nothing>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("zone.unpublished")
    }

    /** `zone.updated`: A zone changed. */
    public class ZoneUpdated(
        payload: WebhookEvent.Payload<AdminSchema.ZoneEventObject, WebhookEvent.ZoneUpdatedPreviousAttributes>,
    ) : WebhookEvent.Typed<AdminSchema.ZoneEventObject, WebhookEvent.ZoneUpdatedPreviousAttributes>(payload) {
        override val type: AdminSchema.WebhookEventType
            get() = AdminSchema.WebhookEventType("zone.updated")
    }

    /**
     * Unknown is an event this build cannot type: a type it does not know, or a payload its types
     * do not read, as the envelope holds it.
     */
    public class Unknown(
        /** The event as the contract's envelope holds it. */
        public val event: AdminSchema.WebhookEvent,
    ) : WebhookEvent() {
        override val type: AdminSchema.WebhookEventType
            get() = event.type

        override val id: kotlin.String
            get() = event.id

        override val `object`: kotlin.String
            get() = event.`object`

        override val api_version: kotlin.String
            get() = event.api_version

        override val created_at: kotlin.time.Instant
            get() = event.created_at

        override val organization_id: kotlin.String
            get() = event.organization_id

        override val task: kotlin.String
            get() = event.task

        override val related_object: AdminSchema.WebhookEventRelatedObject
            get() = event.related_object

        override val request: AdminSchema.WebhookEventRequest?
            get() = event.request

        override val actor: AdminSchema.WebhookEventActor
            get() = event.actor

        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.Unknown && other.event == this.event

        override fun hashCode(): kotlin.Int = this.event.hashCode()

        override fun toString(): kotlin.String = "Unknown(" + this.event + ")"
    }

    /**
     * Payload is an event of a type this build knows, as it was delivered: every member of the
     * contract's envelope, `AdminSchema.WebhookEvent`, but its `type`, which the event's class
     * names, with the `data` that type carries.
     *
     * `Object` is the object the type carries at `data.object`, and `PreviousAttributes` the former
     * value of each field its change moved, `Nothing` for a type whose change moves none.
     */
    @kotlinx.serialization.Serializable
    public class Payload<Object : kotlin.Any, PreviousAttributes : kotlin.Any> private constructor(
        /**
         * Identifier of the event. Its leading bits are the moment it was recorded, so
         * identifiers sort by time. A delivery can arrive more than once, so a receiver
         * deduplicates on it.
         */
        public val id: kotlin.String,
        /** Always `webhook_event`. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "webhook_event",
        /**
         * The contract version the payload follows: the `info.version` of the document that
         * describes it.
         */
        public val api_version: kotlin.String,
        /** When the change was made. */
        public val created_at: kotlin.time.Instant,
        /** Identifier of the organization the change was made in. */
        public val organization_id: kotlin.String,
        /**
         * The task that recorded the event: the resource's path and what was done, such as
         * `organization.member.update`. The contract's `tasks.yaml` lists every task with
         * what triggers it and the event types it records.
         */
        public val task: kotlin.String,
        /** The object the event is about. */
        public val related_object: AdminSchema.WebhookEventRelatedObject,
        /** The object, and what the change moved. */
        public val data: WebhookEvent.PayloadData<Object, PreviousAttributes>,
        /**
         * The request that made the change, a change made signed in to Mountaya's web
         * application or by Mountaya's staff included. Null only when Mountaya made the
         * change on its own: a schedule, work finished after a request was answered, or the
         * payment provider's report.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val request: AdminSchema.WebhookEventRequest? = null,
        /** Who made the change. */
        public val actor: AdminSchema.WebhookEventActor,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.Payload<*, *> &&
                other.id == this.id &&
                other.`object` == this.`object` &&
                other.api_version == this.api_version &&
                other.created_at == this.created_at &&
                other.organization_id == this.organization_id &&
                other.task == this.task &&
                other.related_object == this.related_object &&
                other.data == this.data &&
                other.request == this.request &&
                other.actor == this.actor

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.api_version.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.task.hashCode()
            hash = 31 * hash + this.related_object.hashCode()
            hash = 31 * hash + this.data.hashCode()
            hash = 31 * hash + (this.request?.hashCode() ?: 0)
            hash = 31 * hash + this.actor.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "Payload(" +
                "id=" + this.id +
                ", object=" + this.`object` +
                ", api_version=" + this.api_version +
                ", created_at=" + this.created_at +
                ", organization_id=" + this.organization_id +
                ", task=" + this.task +
                ", related_object=" + this.related_object +
                ", data=" + this.data +
                ", request=" + this.request +
                ", actor=" + this.actor +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.Payload], each by assigning it. */
        public class Builder<Object : kotlin.Any, PreviousAttributes : kotlin.Any> internal constructor() {
        }

        /** Builds a [WebhookEvent.Payload]. */
        public companion object {
            /** Returns a [WebhookEvent.Payload] of the required properties given here and the optional ones [optional] sets. */
            public operator fun <Object : kotlin.Any, PreviousAttributes : kotlin.Any> invoke(
                id: kotlin.String,
                api_version: kotlin.String,
                created_at: kotlin.time.Instant,
                organization_id: kotlin.String,
                task: kotlin.String,
                related_object: AdminSchema.WebhookEventRelatedObject,
                data: WebhookEvent.PayloadData<Object, PreviousAttributes>,
                request: AdminSchema.WebhookEventRequest?,
                actor: AdminSchema.WebhookEventActor,
                optional: WebhookEvent.Payload.Builder<Object, PreviousAttributes>.() -> kotlin.Unit = {},
            ): WebhookEvent.Payload<Object, PreviousAttributes> {
                val given = WebhookEvent.Payload.Builder<Object, PreviousAttributes>().apply(optional)
                return WebhookEvent.Payload<Object, PreviousAttributes>(
                    id = id,
                    api_version = api_version,
                    created_at = created_at,
                    organization_id = organization_id,
                    task = task,
                    related_object = related_object,
                    data = data,
                    request = request,
                    actor = actor,
                )
            }
        }
    }

    /**
     * PayloadData is what an event of a type this build knows says about its object: the object as
     * the type carries it, and the former value of each field its change moved.
     */
    @kotlinx.serialization.Serializable
    public class PayloadData<Object : kotlin.Any, PreviousAttributes : kotlin.Any> private constructor(
        /**
         * The object once the change was made, as lean as its kind allows: identifiers and
         * bounded figures, never geometry, a file or a credential. For a type ending in
         * `deleted`, `revoked`, `removed` or `detached`, the object as it was, its `status`
         * and every other member included. Where the object carries a `version`, a delete
         * is its last version change: the `deleted` event carries a version past the last
         * the object held, so a receiver that applies an event only when its `version` is
         * newer applies the delete too, and never an event that arrives after it. Its
         * `object` names its kind, which is the family of the event's type.
         */
        public val `object`: Object,
        /**
         * The fields of `data.object` the change moved, each with the value it held before.
         * Present on the types whose entry in `webhooks` says so, such as
         * `organization.member.updated`, which carries the member's former `role`. A field
         * that did not move is not in it.
         *
         * It is always null where `PreviousAttributes` is `Nothing`, a type whose change moves no
         * field: a `previous_attributes` such an event carries is not read, as a key this build
         * does not know is not.
         */
        public val previous_attributes: PreviousAttributes? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.PayloadData<*, *> &&
                other.`object` == this.`object` &&
                other.previous_attributes == this.previous_attributes

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + (this.previous_attributes?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PayloadData(" +
                "object=" + this.`object` +
                ", previous_attributes=" + this.previous_attributes +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.PayloadData], each by assigning it. */
        public class Builder<Object : kotlin.Any, PreviousAttributes : kotlin.Any> internal constructor() {
            internal var slot1: PreviousAttributes? = null

            /**
             * The fields of `data.object` the change moved, each with the value it held before.
             * Present on the types whose entry in `webhooks` says so, such as
             * `organization.member.updated`, which carries the member's former `role`. A field
             * that did not move is not in it.
             *
             * It is always null where `PreviousAttributes` is `Nothing`, a type whose change moves no
             * field: a `previous_attributes` such an event carries is not read, as a key this build
             * does not know is not.
             */
            public var previous_attributes: PreviousAttributes
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [WebhookEvent.PayloadData]. */
        public companion object {
            /** Returns a [WebhookEvent.PayloadData] of the required properties given here and the optional ones [optional] sets. */
            public operator fun <Object : kotlin.Any, PreviousAttributes : kotlin.Any> invoke(
                `object`: Object,
                optional: WebhookEvent.PayloadData.Builder<Object, PreviousAttributes>.() -> kotlin.Unit = {},
            ): WebhookEvent.PayloadData<Object, PreviousAttributes> {
                val given = WebhookEvent.PayloadData.Builder<Object, PreviousAttributes>().apply(optional)
                return WebhookEvent.PayloadData<Object, PreviousAttributes>(
                    `object` = `object`,
                    previous_attributes = given.slot1,
                )
            }
        }
    }

    /**
     * CollectionItineraryMovedPreviousAttributes is the former value of each field a
     * `collection.itinerary.moved` event's change moved, as its `data.previous_attributes` carries
     * them, each typed as `AdminSchema.CollectionItineraryEventObject` types it: a field the change
     * did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class CollectionItineraryMovedPreviousAttributes private constructor(
        /**
         * Where the itinerary sits in the collection once this change was made, counted from
         * 0. Absent on `collection.itinerary.removed`. A move shifts the itineraries between
         * its two positions, which record no event of their own.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val position: kotlin.Int? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.CollectionItineraryMovedPreviousAttributes &&
                other.position == this.position

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.position?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionItineraryMovedPreviousAttributes(" +
                "position=" + this.position +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.CollectionItineraryMovedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Int? = null

            /**
             * Where the itinerary sits in the collection once this change was made, counted from
             * 0. Absent on `collection.itinerary.removed`. A move shifts the itineraries between
             * its two positions, which record no event of their own.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var position: kotlin.Int
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [WebhookEvent.CollectionItineraryMovedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.CollectionItineraryMovedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.CollectionItineraryMovedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.CollectionItineraryMovedPreviousAttributes {
                val given = WebhookEvent.CollectionItineraryMovedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.CollectionItineraryMovedPreviousAttributes(
                    position = given.slot0,
                )
            }
        }
    }

    /**
     * CollectionUpdatedPreviousAttributes is the former value of each field a `collection.updated`
     * event's change moved, as its `data.previous_attributes` carries them, each typed as
     * `AdminSchema.CollectionEventObject` types it: a field the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class CollectionUpdatedPreviousAttributes private constructor(
        /**
         * The collection's name.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val name: kotlin.String? = null,
        @kotlinx.serialization.SerialName("description")
        private val settable1: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent,
        /**
         * Whether the collection is private to the organization.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val is_private: kotlin.Boolean? = null,
    ) {
        /**
         * The collection's description, null when it has none.
         *
         * The value it held before the change, a `Stated` holding null where it held null, and null
         * where the change did not move it.
         */
        public val description: com.mountaya.client.Stated<kotlin.String?>?
            get() = settable1.stated

        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.CollectionUpdatedPreviousAttributes &&
                other.name == this.name &&
                other.settable1 == this.settable1 &&
                other.is_private == this.is_private

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + this.settable1.hashCode()
            hash = 31 * hash + (this.is_private?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ", description=" + this.description +
                ", is_private=" + this.is_private +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.CollectionUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent
            internal var slot2: kotlin.Boolean? = null

            /**
             * The collection's name.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The collection's description, null when it has none.
             *
             * The value it held before the change, a `Stated` holding null where it held null, and null
             * where the change did not move it.
             *
             * Left unset it states nothing; set to `null` it states that it held `null`.
             */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = com.mountaya.client.Settable.Present(value)
                }

            /**
             * Whether the collection is private to the organization.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var is_private: kotlin.Boolean
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [WebhookEvent.CollectionUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.CollectionUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.CollectionUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.CollectionUpdatedPreviousAttributes {
                val given = WebhookEvent.CollectionUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.CollectionUpdatedPreviousAttributes(
                    name = given.slot0,
                    settable1 = given.slot1,
                    is_private = given.slot2,
                )
            }
        }
    }

    /**
     * ItinerarySegmentConvertedPreviousAttributes is the former value of each field a
     * `itinerary.segment.converted` event's change moved, as its `data.previous_attributes` carries
     * them, each typed as `AdminSchema.ItinerarySegmentEventObject` types it: a field the change
     * did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class ItinerarySegmentConvertedPreviousAttributes private constructor(
        /**
         * How the segment is drawn once this change was made.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val mode: com.mountaya.client.generated.RestSchema.SegmentMode? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.ItinerarySegmentConvertedPreviousAttributes &&
                other.mode == this.mode

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.mode?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerarySegmentConvertedPreviousAttributes(" +
                "mode=" + this.mode +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.ItinerarySegmentConvertedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.generated.RestSchema.SegmentMode? = null

            /**
             * How the segment is drawn once this change was made.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var mode: com.mountaya.client.generated.RestSchema.SegmentMode
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [WebhookEvent.ItinerarySegmentConvertedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.ItinerarySegmentConvertedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.ItinerarySegmentConvertedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.ItinerarySegmentConvertedPreviousAttributes {
                val given = WebhookEvent.ItinerarySegmentConvertedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.ItinerarySegmentConvertedPreviousAttributes(
                    mode = given.slot0,
                )
            }
        }
    }

    /**
     * ItinerarySegmentUpdatedPreviousAttributes is the former value of each field a
     * `itinerary.segment.updated` event's change moved, as its `data.previous_attributes` carries
     * them, each typed as `AdminSchema.ItinerarySegmentEventObject` types it: a field the change
     * did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class ItinerarySegmentUpdatedPreviousAttributes private constructor(
        /**
         * The segment's name.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val name: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes &&
                other.name == this.name

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerarySegmentUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null

            /**
             * The segment's name.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes {
                val given = WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes(
                    name = given.slot0,
                )
            }
        }
    }

    /**
     * ItinerarySegmentGroupUpdatedPreviousAttributes is the former value of each field a
     * `itinerary.segment_group.updated` event's change moved, as its `data.previous_attributes`
     * carries them, each typed as `AdminSchema.ItinerarySegmentGroupEventObject` types it: a field
     * the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class ItinerarySegmentGroupUpdatedPreviousAttributes private constructor(
        /**
         * The segment group's name.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val name: kotlin.String? = null,
        /**
         * The group's colour, six lowercase hexadecimal digits after `#`.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val color: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes &&
                other.name == this.name &&
                other.color == this.color

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.color?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerarySegmentGroupUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ", color=" + this.color +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null

            /**
             * The segment group's name.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The group's colour, six lowercase hexadecimal digits after `#`.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var color: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes {
                val given = WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes(
                    name = given.slot0,
                    color = given.slot1,
                )
            }
        }
    }

    /**
     * ItineraryUpdatedPreviousAttributes is the former value of each field a `itinerary.updated`
     * event's change moved, as its `data.previous_attributes` carries them, each typed as
     * `AdminSchema.ItineraryEventObject` types it: a field the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class ItineraryUpdatedPreviousAttributes private constructor(
        /**
         * The itinerary's name.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val name: kotlin.String? = null,
        @kotlinx.serialization.SerialName("description")
        private val settable1: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent,
        /**
         * Whether the itinerary is private to the organization.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val is_private: kotlin.Boolean? = null,
    ) {
        /**
         * The itinerary's description, null when it has none.
         *
         * The value it held before the change, a `Stated` holding null where it held null, and null
         * where the change did not move it.
         */
        public val description: com.mountaya.client.Stated<kotlin.String?>?
            get() = settable1.stated

        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.ItineraryUpdatedPreviousAttributes &&
                other.name == this.name &&
                other.settable1 == this.settable1 &&
                other.is_private == this.is_private

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + this.settable1.hashCode()
            hash = 31 * hash + (this.is_private?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItineraryUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ", description=" + this.description +
                ", is_private=" + this.is_private +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.ItineraryUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent
            internal var slot2: kotlin.Boolean? = null

            /**
             * The itinerary's name.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The itinerary's description, null when it has none.
             *
             * The value it held before the change, a `Stated` holding null where it held null, and null
             * where the change did not move it.
             *
             * Left unset it states nothing; set to `null` it states that it held `null`.
             */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = com.mountaya.client.Settable.Present(value)
                }

            /**
             * Whether the itinerary is private to the organization.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var is_private: kotlin.Boolean
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [WebhookEvent.ItineraryUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.ItineraryUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.ItineraryUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.ItineraryUpdatedPreviousAttributes {
                val given = WebhookEvent.ItineraryUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.ItineraryUpdatedPreviousAttributes(
                    name = given.slot0,
                    settable1 = given.slot1,
                    is_private = given.slot2,
                )
            }
        }
    }

    /**
     * ItineraryWaypointUpdatedPreviousAttributes is the former value of each field a
     * `itinerary.waypoint.updated` event's change moved, as its `data.previous_attributes` carries
     * them, each typed as `AdminSchema.ItineraryWaypointEventObject` types it: a field the change
     * did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class ItineraryWaypointUpdatedPreviousAttributes private constructor(
        @kotlinx.serialization.SerialName("name")
        private val settable0: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent,
        /**
         * What kind of waypoint it is.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val mode: com.mountaya.client.generated.RestSchema.WaypointMode? = null,
    ) {
        /**
         * The waypoint's name, null on one that carries none.
         *
         * The value it held before the change, a `Stated` holding null where it held null, and null
         * where the change did not move it.
         */
        public val name: com.mountaya.client.Stated<kotlin.String?>?
            get() = settable0.stated

        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes &&
                other.settable0 == this.settable0 &&
                other.mode == this.mode

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.settable0.hashCode()
            hash = 31 * hash + (this.mode?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ItineraryWaypointUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ", mode=" + this.mode +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent
            internal var slot1: com.mountaya.client.generated.RestSchema.WaypointMode? = null

            /**
             * The waypoint's name, null on one that carries none.
             *
             * The value it held before the change, a `Stated` holding null where it held null, and null
             * where the change did not move it.
             *
             * Left unset it states nothing; set to `null` it states that it held `null`.
             */
            public var name: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = com.mountaya.client.Settable.Present(value)
                }

            /**
             * What kind of waypoint it is.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var mode: com.mountaya.client.generated.RestSchema.WaypointMode
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes {
                val given = WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes(
                    settable0 = given.slot0,
                    mode = given.slot1,
                )
            }
        }
    }

    /**
     * OrganizationAiProviderConfigUpdatedPreviousAttributes is the former value of each field a
     * `organization.ai_provider_config.updated` event's change moved, as its
     * `data.previous_attributes` carries them, each typed as
     * `AdminSchema.AiProviderConfigEventObject` types it: a field the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationAiProviderConfigUpdatedPreviousAttributes private constructor(
        /**
         * The provider's model agent runs use.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val model: kotlin.String? = null,
        /**
         * The last four characters of the stored credential, as
         * `getOrganizationAiProviderConfig` shows them, and empty for a credential shorter
         * than 12 characters, of which four would be too large a part.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val credential_hint: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes &&
                other.model == this.model &&
                other.credential_hint == this.credential_hint

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.model?.hashCode() ?: 0)
            hash = 31 * hash + (this.credential_hint?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationAiProviderConfigUpdatedPreviousAttributes(" +
                "model=" + this.model +
                ", credential_hint=" + this.credential_hint +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null

            /**
             * The provider's model agent runs use.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var model: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The last four characters of the stored credential, as
             * `getOrganizationAiProviderConfig` shows them, and empty for a credential shorter
             * than 12 characters, of which four would be too large a part.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var credential_hint: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes(
                    model = given.slot0,
                    credential_hint = given.slot1,
                )
            }
        }
    }

    /**
     * OrganizationBillingPaymentMethodUpdatedPreviousAttributes is the former value of each field a
     * `organization.billing.payment_method.updated` event's change moved, as its
     * `data.previous_attributes` carries them, each typed as `AdminSchema.PaymentMethodEventObject`
     * types it: a field the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationBillingPaymentMethodUpdatedPreviousAttributes private constructor(
        /**
         * Whether the organization is charged on it once this change was made.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val is_default: kotlin.Boolean? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes &&
                other.is_default == this.is_default

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.is_default?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationBillingPaymentMethodUpdatedPreviousAttributes(" +
                "is_default=" + this.is_default +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.Boolean? = null

            /**
             * Whether the organization is charged on it once this change was made.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var is_default: kotlin.Boolean
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes(
                    is_default = given.slot0,
                )
            }
        }
    }

    /**
     * OrganizationBillingSubscriptionUpdatedPreviousAttributes is the former value of each field a
     * `organization.billing.subscription.updated` event's change moved, as its
     * `data.previous_attributes` carries them, each typed as
     * `AdminSchema.OrganizationSubscriptionEventObject` types it: a field the change did not move
     * is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationBillingSubscriptionUpdatedPreviousAttributes private constructor(
        /**
         * The subscription's status once this change was made.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val status: AdminSchema.SubscriptionStatus? = null,
        /**
         * Whether the subscription ends when its current period does.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val cancel_at_period_end: kotlin.Boolean? = null,
        /**
         * When the subscription's current period ends.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val current_period_end_at: kotlin.time.Instant? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes &&
                other.status == this.status &&
                other.cancel_at_period_end == this.cancel_at_period_end &&
                other.current_period_end_at == this.current_period_end_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.status?.hashCode() ?: 0)
            hash = 31 * hash + (this.cancel_at_period_end?.hashCode() ?: 0)
            hash = 31 * hash + (this.current_period_end_at?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationBillingSubscriptionUpdatedPreviousAttributes(" +
                "status=" + this.status +
                ", cancel_at_period_end=" + this.cancel_at_period_end +
                ", current_period_end_at=" + this.current_period_end_at +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: AdminSchema.SubscriptionStatus? = null
            internal var slot1: kotlin.Boolean? = null
            internal var slot2: kotlin.time.Instant? = null

            /**
             * The subscription's status once this change was made.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var status: AdminSchema.SubscriptionStatus
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Whether the subscription ends when its current period does.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var cancel_at_period_end: kotlin.Boolean
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * When the subscription's current period ends.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var current_period_end_at: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes(
                    status = given.slot0,
                    cancel_at_period_end = given.slot1,
                    current_period_end_at = given.slot2,
                )
            }
        }
    }

    /**
     * OrganizationEntitlementsUpdatedPreviousAttributes is the former value of each field a
     * `organization.entitlements.updated` event's change moved, as its `data.previous_attributes`
     * carries them, each typed as `AdminSchema.OrganizationEntitlementsEventObject` types it: a
     * field the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationEntitlementsUpdatedPreviousAttributes private constructor(
        /**
         * The capabilities the organization holds once this change was made.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val capabilities: kotlin.collections.List<AdminSchema.Capability>? = null,
        /**
         * The studio features the organization's agreement grants once this change was made.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val studio_entitlements: kotlin.collections.List<com.mountaya.client.generated.RestSchema.StudioEntitlement>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes &&
                other.capabilities == this.capabilities &&
                other.studio_entitlements == this.studio_entitlements

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.capabilities?.hashCode() ?: 0)
            hash = 31 * hash + (this.studio_entitlements?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationEntitlementsUpdatedPreviousAttributes(" +
                "capabilities=" + this.capabilities +
                ", studio_entitlements=" + this.studio_entitlements +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<AdminSchema.Capability>? = null
            internal var slot1: kotlin.collections.List<com.mountaya.client.generated.RestSchema.StudioEntitlement>? = null

            /**
             * The capabilities the organization holds once this change was made.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var capabilities: kotlin.collections.List<AdminSchema.Capability>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The studio features the organization's agreement grants once this change was made.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var studio_entitlements: kotlin.collections.List<com.mountaya.client.generated.RestSchema.StudioEntitlement>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes(
                    capabilities = given.slot0,
                    studio_entitlements = given.slot1,
                )
            }
        }
    }

    /**
     * OrganizationMemberUpdatedPreviousAttributes is the former value of each field a
     * `organization.member.updated` event's change moved, as its `data.previous_attributes` carries
     * them, each typed as `AdminSchema.OrganizationMemberEventObject` types it: a field the change
     * did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationMemberUpdatedPreviousAttributes private constructor(
        /**
         * The role the member holds once this change was made, or held when they left.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val role: com.mountaya.client.generated.RestSchema.OrganizationRole? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationMemberUpdatedPreviousAttributes &&
                other.role == this.role

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.role?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationMemberUpdatedPreviousAttributes(" +
                "role=" + this.role +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationMemberUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.generated.RestSchema.OrganizationRole? = null

            /**
             * The role the member holds once this change was made, or held when they left.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var role: com.mountaya.client.generated.RestSchema.OrganizationRole
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationMemberUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationMemberUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationMemberUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationMemberUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationMemberUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationMemberUpdatedPreviousAttributes(
                    role = given.slot0,
                )
            }
        }
    }

    /**
     * OrganizationPublishableKeyUpdatedPreviousAttributes is the former value of each field a
     * `organization.publishable_key.updated` event's change moved, as its
     * `data.previous_attributes` carries them, each typed as
     * `AdminSchema.PublishableKeyEventObject` types it: a field the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationPublishableKeyUpdatedPreviousAttributes private constructor(
        /**
         * The key's name.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val name: kotlin.String? = null,
        /**
         * The scopes the key carries once this change was made.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes &&
                other.name == this.name &&
                other.scopes == this.scopes

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.scopes?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationPublishableKeyUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ", scopes=" + this.scopes +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.collections.List<AdminSchema.PublishableKeyScope>? = null

            /**
             * The key's name.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The scopes the key carries once this change was made.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes(
                    name = given.slot0,
                    scopes = given.slot1,
                )
            }
        }
    }

    /**
     * OrganizationSecretKeyUpdatedPreviousAttributes is the former value of each field a
     * `organization.secret_key.updated` event's change moved, as its `data.previous_attributes`
     * carries them, each typed as `AdminSchema.SecretKeyEventObject` types it: a field the change
     * did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationSecretKeyUpdatedPreviousAttributes private constructor(
        /**
         * The key's name.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val name: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes &&
                other.name == this.name

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationSecretKeyUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null

            /**
             * The key's name.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes(
                    name = given.slot0,
                )
            }
        }
    }

    /**
     * OrganizationUpdatedPreviousAttributes is the former value of each field a
     * `organization.updated` event's change moved, as its `data.previous_attributes` carries them,
     * each typed as `AdminSchema.OrganizationEventObject` types it: a field the change did not move
     * is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationUpdatedPreviousAttributes private constructor(
        /**
         * The organization's name.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val name: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationUpdatedPreviousAttributes &&
                other.name == this.name

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null

            /**
             * The organization's name.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationUpdatedPreviousAttributes(
                    name = given.slot0,
                )
            }
        }
    }

    /**
     * OrganizationWebhookEndpointUpdatedPreviousAttributes is the former value of each field a
     * `organization.webhook_endpoint.updated` event's change moved, as its
     * `data.previous_attributes` carries them, each typed as
     * `AdminSchema.WebhookEndpointEventObject` types it: a field the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationWebhookEndpointUpdatedPreviousAttributes private constructor(
        /**
         * The host of the URL Mountaya delivers the endpoint's events to, with its port
         * when the URL names one: never its path, its query or the credentials it may
         * carry, which a receiver's URL often uses as its secret.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val host: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes &&
                other.host == this.host

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.host?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationWebhookEndpointUpdatedPreviousAttributes(" +
                "host=" + this.host +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null

            /**
             * The host of the URL Mountaya delivers the endpoint's events to, with its port
             * when the URL names one: never its path, its query or the credentials it may
             * carry, which a receiver's URL often uses as its secret.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var host: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes {
                val given = WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes(
                    host = given.slot0,
                )
            }
        }
    }

    /**
     * ZoneUpdatedPreviousAttributes is the former value of each field a `zone.updated` event's
     * change moved, as its `data.previous_attributes` carries them, each typed as
     * `AdminSchema.ZoneEventObject` types it: a field the change did not move is null.
     */
    @kotlinx.serialization.Serializable
    public class ZoneUpdatedPreviousAttributes private constructor(
        /**
         * The zone's name.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val name: kotlin.String? = null,
        @kotlinx.serialization.SerialName("description")
        private val settable1: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent,
        /**
         * The zone's severity level.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val level: com.mountaya.client.generated.RestSchema.ZoneLevel? = null,
        /**
         * How the zone's area was drawn.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val mode: com.mountaya.client.generated.RestSchema.ZoneMode? = null,
        /**
         * Whether the zone is private to the organization.
         *
         * The value it held before the change, and null where the change did not move it.
         */
        public val is_private: kotlin.Boolean? = null,
        @kotlinx.serialization.SerialName("start_at")
        private val settable5: com.mountaya.client.Settable<kotlin.time.Instant?> = com.mountaya.client.Settable.Absent,
        @kotlinx.serialization.SerialName("end_at")
        private val settable6: com.mountaya.client.Settable<kotlin.time.Instant?> = com.mountaya.client.Settable.Absent,
    ) {
        /**
         * The zone's description, null when it has none.
         *
         * The value it held before the change, a `Stated` holding null where it held null, and null
         * where the change did not move it.
         */
        public val description: com.mountaya.client.Stated<kotlin.String?>?
            get() = settable1.stated

        /**
         * When the zone's validity window opens, null when it has always been in force.
         *
         * The value it held before the change, a `Stated` holding null where it held null, and null
         * where the change did not move it.
         */
        public val start_at: com.mountaya.client.Stated<kotlin.time.Instant?>?
            get() = settable5.stated

        /**
         * When the zone's validity window closes, null when it never lapses.
         *
         * The value it held before the change, a `Stated` holding null where it held null, and null
         * where the change did not move it.
         */
        public val end_at: com.mountaya.client.Stated<kotlin.time.Instant?>?
            get() = settable6.stated

        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is WebhookEvent.ZoneUpdatedPreviousAttributes &&
                other.name == this.name &&
                other.settable1 == this.settable1 &&
                other.level == this.level &&
                other.mode == this.mode &&
                other.is_private == this.is_private &&
                other.settable5 == this.settable5 &&
                other.settable6 == this.settable6

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + this.settable1.hashCode()
            hash = 31 * hash + (this.level?.hashCode() ?: 0)
            hash = 31 * hash + (this.mode?.hashCode() ?: 0)
            hash = 31 * hash + (this.is_private?.hashCode() ?: 0)
            hash = 31 * hash + this.settable5.hashCode()
            hash = 31 * hash + this.settable6.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ZoneUpdatedPreviousAttributes(" +
                "name=" + this.name +
                ", description=" + this.description +
                ", level=" + this.level +
                ", mode=" + this.mode +
                ", is_private=" + this.is_private +
                ", start_at=" + this.start_at +
                ", end_at=" + this.end_at +
                ")"

        /** Builder sets the optional properties of a [WebhookEvent.ZoneUpdatedPreviousAttributes], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent
            internal var slot2: com.mountaya.client.generated.RestSchema.ZoneLevel? = null
            internal var slot3: com.mountaya.client.generated.RestSchema.ZoneMode? = null
            internal var slot4: kotlin.Boolean? = null
            internal var slot5: com.mountaya.client.Settable<kotlin.time.Instant?> = com.mountaya.client.Settable.Absent
            internal var slot6: com.mountaya.client.Settable<kotlin.time.Instant?> = com.mountaya.client.Settable.Absent

            /**
             * The zone's name.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The zone's description, null when it has none.
             *
             * The value it held before the change, a `Stated` holding null where it held null, and null
             * where the change did not move it.
             *
             * Left unset it states nothing; set to `null` it states that it held `null`.
             */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = com.mountaya.client.Settable.Present(value)
                }

            /**
             * The zone's severity level.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var level: com.mountaya.client.generated.RestSchema.ZoneLevel
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * How the zone's area was drawn.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var mode: com.mountaya.client.generated.RestSchema.ZoneMode
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * Whether the zone is private to the organization.
             *
             * The value it held before the change, and null where the change did not move it.
             */
            public var is_private: kotlin.Boolean
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * When the zone's validity window opens, null when it has always been in force.
             *
             * The value it held before the change, a `Stated` holding null where it held null, and null
             * where the change did not move it.
             *
             * Left unset it states nothing; set to `null` it states that it held `null`.
             */
            public var start_at: kotlin.time.Instant?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = com.mountaya.client.Settable.Present(value)
                }

            /**
             * When the zone's validity window closes, null when it never lapses.
             *
             * The value it held before the change, a `Stated` holding null where it held null, and null
             * where the change did not move it.
             *
             * Left unset it states nothing; set to `null` it states that it held `null`.
             */
            public var end_at: kotlin.time.Instant?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = com.mountaya.client.Settable.Present(value)
                }
        }

        /** Builds a [WebhookEvent.ZoneUpdatedPreviousAttributes]. */
        public companion object {
            /** Returns a [WebhookEvent.ZoneUpdatedPreviousAttributes] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: WebhookEvent.ZoneUpdatedPreviousAttributes.Builder.() -> kotlin.Unit = {},
            ): WebhookEvent.ZoneUpdatedPreviousAttributes {
                val given = WebhookEvent.ZoneUpdatedPreviousAttributes.Builder().apply(optional)
                return WebhookEvent.ZoneUpdatedPreviousAttributes(
                    name = given.slot0,
                    settable1 = given.slot1,
                    level = given.slot2,
                    mode = given.slot3,
                    is_private = given.slot4,
                    settable5 = given.slot5,
                    settable6 = given.slot6,
                )
            }
        }
    }

    /**
     * Serializer reads an event as the class its `type` names, or as [Unknown] for a type this
     * build does not know and for a payload the class's types do not read, and writes it as the
     * envelope it was delivered in.
     */
    internal object Serializer : kotlinx.serialization.KSerializer<WebhookEvent> {
        /**
         * The envelope's members in the order the contract declares them, which an event is written
         * in.
         */
        private val order: kotlin.collections.List<kotlin.String> =
            kotlin.collections.listOf("id", "object", "type", "api_version", "created_at", "organization_id", "task", "related_object", "data", "request", "actor")

        override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
            kotlinx.serialization.descriptors.buildClassSerialDescriptor("com.mountaya.admin.generated.WebhookEvent")

        override fun serialize(
            encoder: kotlinx.serialization.encoding.Encoder,
            value: WebhookEvent,
        ) {
            val output =
                encoder as? kotlinx.serialization.json.JsonEncoder
                    ?: throw kotlinx.serialization.SerializationException("WebhookEvent is written to JSON only.")
            val written =
                when (value) {
                    is WebhookEvent.CollectionCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.CollectionDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.CollectionItineraryAdded ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionItineraryEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.CollectionItineraryMoved ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionItineraryEventObject.serializer(), WebhookEvent.CollectionItineraryMovedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.CollectionItineraryRemoved ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionItineraryEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.CollectionShareLinkCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionShareLinkEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.CollectionShareLinkDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionShareLinkEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.CollectionShareLinkRotated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionShareLinkEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.CollectionUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionEventObject.serializer(), WebhookEvent.CollectionUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.CollectionZoneAdded ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionZoneEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.CollectionZoneRemoved ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionZoneEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryPublished ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItinerarySegmentConverted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), WebhookEvent.ItinerarySegmentConvertedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.ItinerarySegmentCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItinerarySegmentDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItinerarySegmentMerged ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItinerarySegmentSplit ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItinerarySegmentUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.ItinerarySegmentGroupCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentGroupEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItinerarySegmentGroupDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentGroupEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItinerarySegmentGroupUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentGroupEventObject.serializer(), WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.ItineraryShareLinkCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryShareLinkEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryShareLinkDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryShareLinkEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryShareLinkRotated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryShareLinkEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryUnpublished ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), WebhookEvent.ItineraryUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.ItineraryWaypointCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryWaypointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryWaypointDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryWaypointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ItineraryWaypointUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryWaypointEventObject.serializer(), WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationAiProviderConfigCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.AiProviderConfigEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationAiProviderConfigDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.AiProviderConfigEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationAiProviderConfigUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.AiProviderConfigEventObject.serializer(), WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationBillingInfoUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.BillingInfoEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingInvoicePaid ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.InvoiceEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingInvoicePaymentActionRequired ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.InvoiceEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingInvoicePaymentFailed ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.InvoiceEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingPaymentMethodAttached ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PaymentMethodEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingPaymentMethodDetached ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PaymentMethodEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingPaymentMethodUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PaymentMethodEventObject.serializer(), WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationBillingSubscriptionCanceled ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingSubscriptionCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingSubscriptionPaused ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingSubscriptionResumed ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationBillingSubscriptionUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationEntitlementsUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationEntitlementsEventObject.serializer(), WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationInvitationAccepted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationInvitationCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationInvitationDeclined ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationInvitationDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationInvitationExpired ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationInvitationRevoked ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationMemberCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationMemberEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationMemberDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationMemberEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationMemberUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationMemberEventObject.serializer(), WebhookEvent.OrganizationMemberUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationPublishableKeyCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PublishableKeyEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationPublishableKeyRevoked ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PublishableKeyEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationPublishableKeyUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PublishableKeyEventObject.serializer(), WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationSecretKeyCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.SecretKeyEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationSecretKeyRevoked ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.SecretKeyEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationSecretKeyUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.SecretKeyEventObject.serializer(), WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationSettingsUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSettingsEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationEventObject.serializer(), WebhookEvent.OrganizationUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.OrganizationWebhookEndpointCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationWebhookEndpointDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationWebhookEndpointDisabled ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationWebhookEndpointEnabled ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationWebhookEndpointFailing ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationWebhookEndpointRecovered ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationWebhookEndpointSecretRotated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.OrganizationWebhookEndpointUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.ZoneCreated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ZoneDeleted ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ZonePublished ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ZoneUnpublished ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), Unread), value.payload)
                    is WebhookEvent.ZoneUpdated ->
                        output.json.encodeToJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), WebhookEvent.ZoneUpdatedPreviousAttributes.serializer()), value.payload)
                    is WebhookEvent.Unknown -> {
                        output.encodeJsonElement(output.json.encodeToJsonElement(AdminSchema.WebhookEvent.serializer(), value.event))
                        return
                    }
                    else -> throw kotlinx.serialization.SerializationException("WebhookEvent has no class of this kind.")
                }
            val members =
                written as? kotlinx.serialization.json.JsonObject
                    ?: throw kotlinx.serialization.SerializationException("A webhook event is written as a JSON object.")
            val type = output.json.encodeToJsonElement(AdminSchema.WebhookEventType.serializer(), value.type)
            output.encodeJsonElement(
                kotlinx.serialization.json.JsonObject(
                    kotlin.collections.buildMap<kotlin.String, kotlinx.serialization.json.JsonElement> {
                        for (name in order) {
                            if (name == "type") put(name, type) else members[name]?.let { put(name, it) }
                        }
                    },
                ),
            )
        }

        override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): WebhookEvent {
            val input =
                decoder as? kotlinx.serialization.json.JsonDecoder
                    ?: throw kotlinx.serialization.SerializationException("WebhookEvent is read from JSON only.")
            val json =
                input.decodeJsonElement() as? kotlinx.serialization.json.JsonObject
                    ?: throw kotlinx.serialization.SerializationException("A webhook event is a JSON object.")
            val type =
                (json["type"] as? kotlinx.serialization.json.JsonPrimitive)?.takeIf { it.isString }?.content
                    ?: throw kotlinx.serialization.SerializationException("A webhook event names its type in a string `type`.")
            val typed =
                when (type) {
                    "collection.created" ->
                        readOrNull {
                            WebhookEvent.CollectionCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "collection.deleted" ->
                        readOrNull {
                            WebhookEvent.CollectionDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "collection.itinerary.added" ->
                        readOrNull {
                            WebhookEvent.CollectionItineraryAdded(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionItineraryEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "collection.itinerary.moved" ->
                        readOrNull {
                            WebhookEvent.CollectionItineraryMoved(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionItineraryEventObject.serializer(), WebhookEvent.CollectionItineraryMovedPreviousAttributes.serializer()), json),
                            )
                        }
                    "collection.itinerary.removed" ->
                        readOrNull {
                            WebhookEvent.CollectionItineraryRemoved(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionItineraryEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "collection.share_link.created" ->
                        readOrNull {
                            WebhookEvent.CollectionShareLinkCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionShareLinkEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "collection.share_link.deleted" ->
                        readOrNull {
                            WebhookEvent.CollectionShareLinkDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionShareLinkEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "collection.share_link.rotated" ->
                        readOrNull {
                            WebhookEvent.CollectionShareLinkRotated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionShareLinkEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "collection.updated" ->
                        readOrNull {
                            WebhookEvent.CollectionUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionEventObject.serializer(), WebhookEvent.CollectionUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "collection.zone.added" ->
                        readOrNull {
                            WebhookEvent.CollectionZoneAdded(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionZoneEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "collection.zone.removed" ->
                        readOrNull {
                            WebhookEvent.CollectionZoneRemoved(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.CollectionZoneEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.created" ->
                        readOrNull {
                            WebhookEvent.ItineraryCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.deleted" ->
                        readOrNull {
                            WebhookEvent.ItineraryDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.published" ->
                        readOrNull {
                            WebhookEvent.ItineraryPublished(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.segment.converted" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentConverted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), WebhookEvent.ItinerarySegmentConvertedPreviousAttributes.serializer()), json),
                            )
                        }
                    "itinerary.segment.created" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.segment.deleted" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.segment.merged" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentMerged(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.segment.split" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentSplit(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.segment.updated" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentEventObject.serializer(), WebhookEvent.ItinerarySegmentUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "itinerary.segment_group.created" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentGroupCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentGroupEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.segment_group.deleted" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentGroupDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentGroupEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.segment_group.updated" ->
                        readOrNull {
                            WebhookEvent.ItinerarySegmentGroupUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItinerarySegmentGroupEventObject.serializer(), WebhookEvent.ItinerarySegmentGroupUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "itinerary.share_link.created" ->
                        readOrNull {
                            WebhookEvent.ItineraryShareLinkCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryShareLinkEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.share_link.deleted" ->
                        readOrNull {
                            WebhookEvent.ItineraryShareLinkDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryShareLinkEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.share_link.rotated" ->
                        readOrNull {
                            WebhookEvent.ItineraryShareLinkRotated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryShareLinkEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.unpublished" ->
                        readOrNull {
                            WebhookEvent.ItineraryUnpublished(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.updated" ->
                        readOrNull {
                            WebhookEvent.ItineraryUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryEventObject.serializer(), WebhookEvent.ItineraryUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "itinerary.waypoint.created" ->
                        readOrNull {
                            WebhookEvent.ItineraryWaypointCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryWaypointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.waypoint.deleted" ->
                        readOrNull {
                            WebhookEvent.ItineraryWaypointDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryWaypointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "itinerary.waypoint.updated" ->
                        readOrNull {
                            WebhookEvent.ItineraryWaypointUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ItineraryWaypointEventObject.serializer(), WebhookEvent.ItineraryWaypointUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.ai_provider_config.created" ->
                        readOrNull {
                            WebhookEvent.OrganizationAiProviderConfigCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.AiProviderConfigEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.ai_provider_config.deleted" ->
                        readOrNull {
                            WebhookEvent.OrganizationAiProviderConfigDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.AiProviderConfigEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.ai_provider_config.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationAiProviderConfigUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.AiProviderConfigEventObject.serializer(), WebhookEvent.OrganizationAiProviderConfigUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.billing.info.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingInfoUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.BillingInfoEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.invoice.paid" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingInvoicePaid(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.InvoiceEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.invoice.payment_action_required" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingInvoicePaymentActionRequired(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.InvoiceEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.invoice.payment_failed" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingInvoicePaymentFailed(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.InvoiceEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.payment_method.attached" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingPaymentMethodAttached(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PaymentMethodEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.payment_method.detached" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingPaymentMethodDetached(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PaymentMethodEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.payment_method.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingPaymentMethodUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PaymentMethodEventObject.serializer(), WebhookEvent.OrganizationBillingPaymentMethodUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.billing.subscription.canceled" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingSubscriptionCanceled(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.subscription.created" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingSubscriptionCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.subscription.paused" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingSubscriptionPaused(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.subscription.resumed" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingSubscriptionResumed(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.billing.subscription.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationBillingSubscriptionUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSubscriptionEventObject.serializer(), WebhookEvent.OrganizationBillingSubscriptionUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.created" ->
                        readOrNull {
                            WebhookEvent.OrganizationCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.entitlements.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationEntitlementsUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationEntitlementsEventObject.serializer(), WebhookEvent.OrganizationEntitlementsUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.invitation.accepted" ->
                        readOrNull {
                            WebhookEvent.OrganizationInvitationAccepted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.invitation.created" ->
                        readOrNull {
                            WebhookEvent.OrganizationInvitationCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.invitation.declined" ->
                        readOrNull {
                            WebhookEvent.OrganizationInvitationDeclined(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.invitation.deleted" ->
                        readOrNull {
                            WebhookEvent.OrganizationInvitationDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.invitation.expired" ->
                        readOrNull {
                            WebhookEvent.OrganizationInvitationExpired(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.invitation.revoked" ->
                        readOrNull {
                            WebhookEvent.OrganizationInvitationRevoked(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationInvitationEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.member.created" ->
                        readOrNull {
                            WebhookEvent.OrganizationMemberCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationMemberEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.member.deleted" ->
                        readOrNull {
                            WebhookEvent.OrganizationMemberDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationMemberEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.member.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationMemberUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationMemberEventObject.serializer(), WebhookEvent.OrganizationMemberUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.publishable_key.created" ->
                        readOrNull {
                            WebhookEvent.OrganizationPublishableKeyCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PublishableKeyEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.publishable_key.revoked" ->
                        readOrNull {
                            WebhookEvent.OrganizationPublishableKeyRevoked(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PublishableKeyEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.publishable_key.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationPublishableKeyUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.PublishableKeyEventObject.serializer(), WebhookEvent.OrganizationPublishableKeyUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.secret_key.created" ->
                        readOrNull {
                            WebhookEvent.OrganizationSecretKeyCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.SecretKeyEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.secret_key.revoked" ->
                        readOrNull {
                            WebhookEvent.OrganizationSecretKeyRevoked(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.SecretKeyEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.secret_key.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationSecretKeyUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.SecretKeyEventObject.serializer(), WebhookEvent.OrganizationSecretKeyUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.settings.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationSettingsUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationSettingsEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.OrganizationEventObject.serializer(), WebhookEvent.OrganizationUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "organization.webhook_endpoint.created" ->
                        readOrNull {
                            WebhookEvent.OrganizationWebhookEndpointCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.webhook_endpoint.deleted" ->
                        readOrNull {
                            WebhookEvent.OrganizationWebhookEndpointDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.webhook_endpoint.disabled" ->
                        readOrNull {
                            WebhookEvent.OrganizationWebhookEndpointDisabled(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.webhook_endpoint.enabled" ->
                        readOrNull {
                            WebhookEvent.OrganizationWebhookEndpointEnabled(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.webhook_endpoint.failing" ->
                        readOrNull {
                            WebhookEvent.OrganizationWebhookEndpointFailing(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.webhook_endpoint.recovered" ->
                        readOrNull {
                            WebhookEvent.OrganizationWebhookEndpointRecovered(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.webhook_endpoint.secret_rotated" ->
                        readOrNull {
                            WebhookEvent.OrganizationWebhookEndpointSecretRotated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "organization.webhook_endpoint.updated" ->
                        readOrNull {
                            WebhookEvent.OrganizationWebhookEndpointUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.WebhookEndpointEventObject.serializer(), WebhookEvent.OrganizationWebhookEndpointUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    "zone.created" ->
                        readOrNull {
                            WebhookEvent.ZoneCreated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "zone.deleted" ->
                        readOrNull {
                            WebhookEvent.ZoneDeleted(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "zone.published" ->
                        readOrNull {
                            WebhookEvent.ZonePublished(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "zone.unpublished" ->
                        readOrNull {
                            WebhookEvent.ZoneUnpublished(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), Unread), unread(json)),
                            )
                        }
                    "zone.updated" ->
                        readOrNull {
                            WebhookEvent.ZoneUpdated(
                                input.json.decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.ZoneEventObject.serializer(), WebhookEvent.ZoneUpdatedPreviousAttributes.serializer()), json),
                            )
                        }
                    else -> null
                }
            return typed ?: WebhookEvent.Unknown(untyped(input, json, type))
        }

        /** Returns the event [read] reads, or null where its payload does not read as its types say. */
        private inline fun readOrNull(read: () -> WebhookEvent): WebhookEvent? =
            try {
                read()
            } catch (unread: kotlin.IllegalArgumentException) {
                null
            }

        /**
         * Returns [event] without its `data.previous_attributes`, which a type whose change moves
         * no field never reads.
         */
        private fun unread(event: kotlinx.serialization.json.JsonObject): kotlinx.serialization.json.JsonObject {
            val data = event["data"] as? kotlinx.serialization.json.JsonObject ?: return event
            if ("previous_attributes" !in data) return event
            return kotlinx.serialization.json.JsonObject(event + ("data" to kotlinx.serialization.json.JsonObject(data - "previous_attributes")))
        }

        /**
         * Returns an event this build cannot type as the envelope: as the envelope's own types read
         * it, or, where they do not read its object either, with its object and its previous
         * attributes as they arrived. It throws where the body holds no envelope at all.
         */
        private fun untyped(
            input: kotlinx.serialization.json.JsonDecoder,
            json: kotlinx.serialization.json.JsonObject,
            type: kotlin.String,
        ): AdminSchema.WebhookEvent {
            try {
                return input.json.decodeFromJsonElement(AdminSchema.WebhookEvent.serializer(), json)
            } catch (unread: kotlin.IllegalArgumentException) {
            }
            val payload =
                input.json.decodeFromJsonElement(
                    WebhookEvent.Payload.serializer(
                        kotlinx.serialization.json.JsonElement.serializer(),
                        kotlinx.serialization.builtins.MapSerializer(kotlinx.serialization.serializer<kotlin.String>(), kotlinx.serialization.json.JsonElement.serializer()),
                    ),
                    json,
                )
            val arrived =
                payload.data.`object` as? kotlinx.serialization.json.JsonObject
                    ?: throw kotlinx.serialization.SerializationException("A webhook event's object is a JSON object.")
            val data =
                AdminSchema.WebhookEventData(`object` = AdminSchema.WebhookEventObject.Unknown(arrived)) {
                    payload.data.previous_attributes?.let { this.previous_attributes = it }
                }
            return AdminSchema.WebhookEvent(
                id = payload.id,
                type = AdminSchema.WebhookEventType(type),
                api_version = payload.api_version,
                created_at = payload.created_at,
                organization_id = payload.organization_id,
                task = payload.task,
                related_object = payload.related_object,
                data = data,
                request = payload.request,
                actor = payload.actor,
            )
        }

        /**
         * Unread is what a type whose change moves no field reads its previous attributes with:
         * nothing, since they are never read.
         */
        private object Unread : kotlinx.serialization.KSerializer<kotlin.Nothing> {
            override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
                kotlinx.serialization.descriptors.buildClassSerialDescriptor("kotlin.Nothing")

            override fun serialize(
                encoder: kotlinx.serialization.encoding.Encoder,
                value: kotlin.Nothing,
            ) {
            }

            override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): kotlin.Nothing =
                throw kotlinx.serialization.SerializationException("A type whose change moves no field reads no previous attributes.")
        }
    }
}
