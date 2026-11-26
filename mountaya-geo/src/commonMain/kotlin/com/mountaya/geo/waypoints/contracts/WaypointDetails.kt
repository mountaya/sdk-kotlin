package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.time.Instant

/**
 * WaypointDetails is what a waypoint says about the place it marks, as an element shows it: the
 * services it lists, its note, its timing, its contact, its photos and links, and its own fields. Each
 * member is null where the waypoint states nothing of it, and a waypoint that states nothing has no
 * details (`itineraryWaypoints`).
 *
 * Every member keeps the wire's type but the web addresses: a cut-off is an [Instant] and a planned stop
 * whole seconds, and a website, a booking page, a photo's and a link's address are the text the wire
 * answers, as written, since an address a card states must read as its author wrote it and a card anchors
 * one only where it is a web address (`isWebAddress`). Two are equal where every member is, a text by its
 * code units.
 */
public class WaypointDetails(
    /** The services the place offers, in the order listed, null where it lists none. */
    public val amenities: List<RestSchema.WaypointAmenity>? = null,
    /** The waypoint's note, as written. */
    public val description: String? = null,
    /** When the place is to be reached and how long is spent there. */
    public val timing: Timing? = null,
    /** How the place is reached. */
    public val contact: Contact? = null,
    /** The place's photos and links. */
    public val media: Media? = null,
    /** The waypoint's own fields, in the order they are shown. */
    public val properties: List<Property>? = null,
) {
    /** Timing is when a place is to be reached by, and how long is spent there. */
    public class Timing(
        /** The instant the place is to be reached by. */
        public val cutoffAt: Instant? = null,
        /** The seconds planned at the place. */
        public val stopDuration: Int? = null,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Timing && other.cutoffAt == cutoffAt && other.stopDuration == stopDuration

        override fun hashCode(): Int = cutoffAt.hashCode() * 31 + stopDuration.hashCode()

        override fun toString(): String = "Timing(cutoffAt=$cutoffAt, stopDuration=$stopDuration)"
    }

    /** Contact is how a place is reached: each member the waypoint states. */
    public class Contact(
        /** The place's postal address. */
        public val address: String? = null,
        /** Where the place is booked, as written. */
        public val bookingUrl: String? = null,
        /** The place's email address. */
        public val email: String? = null,
        /** When the place is open, as written. */
        public val openingHours: String? = null,
        /** The place's phone number, as written. */
        public val phone: String? = null,
        /** The place's web site, as written. */
        public val website: String? = null,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Contact &&
                other.address == address &&
                other.bookingUrl == bookingUrl &&
                other.email == email &&
                other.openingHours == openingHours &&
                other.phone == phone &&
                other.website == website

        override fun hashCode(): Int = listOf(address, bookingUrl, email, openingHours, phone, website).hashCode()

        override fun toString(): String =
            "Contact(address=$address, bookingUrl=$bookingUrl, email=$email, openingHours=$openingHours, " +
                "phone=$phone, website=$website)"
    }

    /** Media is a place's photos and links, each list null where it holds none. */
    public class Media(
        /** The place's photos, in order. */
        public val photos: List<Photo>? = null,
        /** The place's links, in order. */
        public val links: List<Link>? = null,
    ) {
        override fun equals(other: Any?): Boolean = other is Media && other.photos == photos && other.links == links

        override fun hashCode(): Int = photos.hashCode() * 31 + links.hashCode()

        override fun toString(): String = "Media(photos=$photos, links=$links)"
    }

    /** Photo is one photo of a place, at its [url], with its [caption] where it states one. */
    public class Photo(
        /** Where the photo is, as written. */
        public val url: String,
        /** The photo's caption. */
        public val caption: String? = null,
    ) {
        override fun equals(other: Any?): Boolean = other is Photo && other.url == url && other.caption == caption

        override fun hashCode(): Int = url.hashCode() * 31 + caption.hashCode()

        override fun toString(): String = "Photo(url=$url, caption=$caption)"
    }

    /** Link is one link of a place: its [label], and where it leads. */
    public class Link(
        /** The words the link is shown with. */
        public val label: String,
        /** Where the link leads, as written. */
        public val url: String,
    ) {
        override fun equals(other: Any?): Boolean = other is Link && other.label == label && other.url == url

        override fun hashCode(): Int = label.hashCode() * 31 + url.hashCode()

        override fun toString(): String = "Link(label=$label, url=$url)"
    }

    /** Property is one of a waypoint's own fields: its [key], its [label], its [value] and its [format]. */
    public class Property(
        /** The field's key. */
        public val key: String,
        /** The words the field is shown with. */
        public val label: String,
        /** The field's value, as written. */
        public val value: String,
        /** How the value is to be read, null where it states none. */
        public val format: RestSchema.WaypointPropertyFormat? = null,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Property &&
                other.key == key &&
                other.label == label &&
                other.value == value &&
                other.format == format

        override fun hashCode(): Int = listOf(key, label, value, format).hashCode()

        override fun toString(): String = "Property(key=$key, label=$label, value=$value, format=$format)"
    }

    override fun equals(other: Any?): Boolean =
        other is WaypointDetails &&
            other.amenities == amenities &&
            other.description == description &&
            other.timing == timing &&
            other.contact == contact &&
            other.media == media &&
            other.properties == properties

    override fun hashCode(): Int = listOf(amenities, description, timing, contact, media, properties).hashCode()

    override fun toString(): String =
        "WaypointDetails(amenities=$amenities, description=$description, timing=$timing, contact=$contact, " +
            "media=$media, properties=$properties)"
}
