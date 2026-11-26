package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * WaypointCardFacts is everything a card about one place states, in the order a card states it, before any of
 * it is drawn (`geo.profile.waypoint-card`): its headline, its services by name, when it can be used, how to
 * reach it, its fields, its links and its photos, each label a word of the reader's scope and each figure
 * written for the reader's units, and whether it states nothing at all.
 *
 * Two are equal where every member is.
 */
public class WaypointCardFacts(
    /**
     * The line under the place's name: its author's description, or the summary its record assembles, null where
     * neither says anything.
     */
    public val headline: String?,
    /** The services the place offers, in its order, each named. */
    public val services: List<Service>,
    /** When the place can be used: its cut-off, through the caller's formatter alone, then its planned stop. */
    public val timing: List<Line>,
    /** How to reach the place: opening hours, address, phone, email, website and booking page. */
    public val contact: List<Line>,
    /** The fields the place's author published, each written by its declared format. */
    public val fields: List<WaypointField>,
    /** The place's media links, each with the address it may be anchored by. */
    public val links: List<MediaLink>,
    /**
     * The place's photos a renderer may load, each at an address read past its white space, a photo at any other
     * address left out.
     */
    public val photos: List<WaypointDetails.Photo>,
    /** Whether the place states no fact at all, its description apart, as `waypointDeclaresNothing` reads a record. */
    public val isEmpty: Boolean,
) {
    /**
     * Service is one service a place offers: the wire's own value, which names its glyph, and the reader's name
     * for it, or the value itself where the reader's words hold none.
     */
    public class Service(
        /** The service as the wire spells it. */
        public val amenity: RestSchema.WaypointAmenity,
        /** The reader's name for it. */
        public val name: String,
    ) {
        override fun equals(other: Any?): Boolean = other is Service && other.amenity == amenity && other.name == name

        override fun hashCode(): Int = amenity.hashCode() * 31 + name.hashCode()

        override fun toString(): String = "Service(amenity=$amenity, name=$name)"
    }

    /**
     * Line is one labelled fact a card states: the reader's noun and the value beside it, the address a reader
     * acts on it through where it has one, a phone's `tel:` and an email's `mailto:` among them, and whether that
     * address leaves the app.
     */
    public class Line(
        /** The reader's noun for the fact, a word of the scope's `waypointCard` section. */
        public val label: String,
        /** The fact, as written. */
        public val value: String,
        /** The address a reader acts on the fact through, null where it is none. */
        public val href: String? = null,
        /** Whether [href] leaves the app, a website or a booking page. */
        public val isExternal: Boolean = false,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Line &&
                other.label == label &&
                other.value == value &&
                other.href == href &&
                other.isExternal == isExternal

        override fun hashCode(): Int = listOf(label, value, href, isExternal).hashCode()

        override fun toString(): String = "Line(label=$label, value=$value, href=$href, isExternal=$isExternal)"
    }

    /**
     * MediaLink is one media link of a place: its author's label and address, as written, and the address a
     * renderer anchors it by, only where the address read past its white space is a web address; any other is
     * printed as its label.
     */
    public class MediaLink(
        /** The link's label, as written. */
        public val label: String,
        /** The link's address, as written, text to read rather than an address to anchor. */
        public val url: String,
        /** The address a renderer anchors the link by, null where it is no web address. */
        public val href: String?,
    ) {
        override fun equals(other: Any?): Boolean =
            other is MediaLink && other.label == label && other.url == url && other.href == href

        override fun hashCode(): Int = listOf(label, url, href).hashCode()

        override fun toString(): String = "MediaLink(label=$label, url=$url, href=$href)"
    }

    override fun equals(other: Any?): Boolean =
        other is WaypointCardFacts &&
            other.headline == headline &&
            other.services == services &&
            other.timing == timing &&
            other.contact == contact &&
            other.fields == fields &&
            other.links == links &&
            other.photos == photos &&
            other.isEmpty == isEmpty

    override fun hashCode(): Int =
        listOf(headline, services, timing, contact, fields, links, photos, isEmpty).hashCode()

    override fun toString(): String =
        "WaypointCardFacts(headline=$headline, services=$services, timing=$timing, contact=$contact, " +
            "fields=$fields, links=$links, photos=$photos, isEmpty=$isEmpty)"
}
