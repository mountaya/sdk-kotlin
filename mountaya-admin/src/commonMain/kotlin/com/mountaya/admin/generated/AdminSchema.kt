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
 * AdminSchema is the types of the REST API only its admin unit reaches, through its `admin`
 * operations and its webhook events, as the contract names them. A type the core holds too is
 * `RestSchema`'s, in `mountaya-client`.
 */
public object AdminSchema {
    /**
     * A postal billing address. The same shape on both sides of the exchange: it
     * is what a read of the billing information returns and what an update sends,
     * so a client reads one, edits a field and writes it back without reshaping it.
     * Every field is optional, and an object whose fields are all empty clears the
     * address.
     */
    @kotlinx.serialization.Serializable
    public class Address private constructor(
        /** The street address, PO box or company name. */
        public val line1: kotlin.String? = null,
        /** The apartment, suite, unit or building. */
        public val line2: kotlin.String? = null,
        /** The city, district, suburb, town or village. */
        public val city: kotlin.String? = null,
        /** The postal or ZIP code. */
        public val postal_code: kotlin.String? = null,
        /** The state, county, province or region. */
        public val state: kotlin.String? = null,
        /**
         * The two-letter ISO 3166-1 alpha-2 country code. An empty value clears the
         * country; any other value is exactly two characters.
         */
        public val country: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.Address &&
                other.line1 == this.line1 &&
                other.line2 == this.line2 &&
                other.city == this.city &&
                other.postal_code == this.postal_code &&
                other.state == this.state &&
                other.country == this.country

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.line1?.hashCode() ?: 0)
            hash = 31 * hash + (this.line2?.hashCode() ?: 0)
            hash = 31 * hash + (this.city?.hashCode() ?: 0)
            hash = 31 * hash + (this.postal_code?.hashCode() ?: 0)
            hash = 31 * hash + (this.state?.hashCode() ?: 0)
            hash = 31 * hash + (this.country?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "Address(" +
                "line1=" + this.line1 +
                ", line2=" + this.line2 +
                ", city=" + this.city +
                ", postal_code=" + this.postal_code +
                ", state=" + this.state +
                ", country=" + this.country +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.Address], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.String? = null
            internal var slot4: kotlin.String? = null
            internal var slot5: kotlin.String? = null

            /** The street address, PO box or company name. */
            public var line1: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** The apartment, suite, unit or building. */
            public var line2: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** The city, district, suburb, town or village. */
            public var city: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** The postal or ZIP code. */
            public var postal_code: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** The state, county, province or region. */
            public var state: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * The two-letter ISO 3166-1 alpha-2 country code. An empty value clears the
             * country; any other value is exactly two characters.
             */
            public var country: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [AdminSchema.Address]. */
        public companion object {
            /** Returns a [AdminSchema.Address] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.Address.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.Address {
                val given = AdminSchema.Address.Builder().apply(optional)
                return AdminSchema.Address(
                    line1 = given.slot0,
                    line2 = given.slot1,
                    city = given.slot2,
                    postal_code = given.slot3,
                    state = given.slot4,
                    country = given.slot5,
                )
            }
        }
    }

    /**
     * A single credential input a provider requires, as advertised by the AI
     * provider catalog. It is non-sensitive metadata describing the field to collect
     * (its key and a display label); it never carries a value.
     */
    @kotlinx.serialization.Serializable
    public class AiCredentialField private constructor(
        /**
         * The stable credential map key to submit under
         * AiProviderConfigParams.credential (for example "api_key"). A snake_case
         * slug, bounded the way every other key on this surface is.
         */
        public val key: kotlin.String,
        /** A human-readable field name for a configuration form. */
        public val label: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.AiCredentialField &&
                other.key == this.key &&
                other.label == this.label

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.key.hashCode()
            hash = 31 * hash + this.label.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "AiCredentialField(" +
                "key=" + this.key +
                ", label=" + this.label +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.AiCredentialField], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.AiCredentialField]. */
        public companion object {
            /** Returns a [AdminSchema.AiCredentialField] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                key: kotlin.String,
                label: kotlin.String,
                optional: AdminSchema.AiCredentialField.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.AiCredentialField {
                val given = AdminSchema.AiCredentialField.Builder().apply(optional)
                return AdminSchema.AiCredentialField(
                    key = key,
                    label = label,
                )
            }
        }
    }

    /**
     * The AI provider whose credentials an organization brings for the studio
     * co-pilot. Each provider exposes its own allow-list of selectable models.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class AiProvider(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Anthropic (Claude) models via the native Messages API. */
            public val Anthropic: AiProvider = AiProvider("anthropic")

            /** Mistral models via the OpenAI-compatible chat-completions API. */
            public val Mistral: AiProvider = AiProvider("mistral")
        }
    }

    /**
     * One provider's entry in the AI provider catalog: the provider id, a display
     * label, the model ids selectable for it, the default among them, and the
     * credential fields it requires. The catalog is the same for every caller and
     * drives the configuration picker.
     */
    @kotlinx.serialization.Serializable
    public class AiProviderCatalog private constructor(
        /**
         * Stable identifier of the provider, and the value `postOrganizationAiProviderConfig`
         * takes.
         */
        public val provider: AdminSchema.AiProvider,
        /**
         * The provider's name as a person reads it. For display only; branch on
         * `provider`.
         */
        public val label: kotlin.String,
        /** The allow-list of model ids selectable for this provider. */
        public val models: kotlin.collections.List<kotlin.String>,
        /**
         * The model id a fresh configuration seeds to: the one the picker preselects
         * and the co-pilot falls back to. Always one of models.
         */
        public val default_model: kotlin.String,
        /** The credential inputs this provider requires, in display order. */
        public val credential_fields: kotlin.collections.List<AdminSchema.AiCredentialField>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.AiProviderCatalog &&
                other.provider == this.provider &&
                other.label == this.label &&
                other.models == this.models &&
                other.default_model == this.default_model &&
                other.credential_fields == this.credential_fields

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.provider.hashCode()
            hash = 31 * hash + this.label.hashCode()
            hash = 31 * hash + this.models.hashCode()
            hash = 31 * hash + this.default_model.hashCode()
            hash = 31 * hash + this.credential_fields.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "AiProviderCatalog(" +
                "provider=" + this.provider +
                ", label=" + this.label +
                ", models=" + this.models +
                ", default_model=" + this.default_model +
                ", credential_fields=" + this.credential_fields +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.AiProviderCatalog], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.AiProviderCatalog]. */
        public companion object {
            /** Returns a [AdminSchema.AiProviderCatalog] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                provider: AdminSchema.AiProvider,
                label: kotlin.String,
                models: kotlin.collections.List<kotlin.String>,
                default_model: kotlin.String,
                credential_fields: kotlin.collections.List<AdminSchema.AiCredentialField>,
                optional: AdminSchema.AiProviderCatalog.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.AiProviderCatalog {
                val given = AdminSchema.AiProviderCatalog.Builder().apply(optional)
                return AdminSchema.AiProviderCatalog(
                    provider = provider,
                    label = label,
                    models = models,
                    default_model = default_model,
                    credential_fields = credential_fields,
                )
            }
        }
    }

    /**
     * The masked read view of an organization's AI provider configuration for the
     * studio co-pilot. It never contains the credential: it carries the provider,
     * the model, the base URL override if one is set, and a non-sensitive hint
     * (the last four characters of the key) so an admin can recognize which key is
     * in place. An organization with none configured reads `null` rather than this
     * object, and cannot start an agent run.
     */
    @kotlinx.serialization.Serializable
    public class AiProviderConfig private constructor(
        /**
         * The configured provider. It cannot be changed in place: delete the
         * configuration and create one to switch providers.
         */
        public val provider: AdminSchema.AiProvider,
        /**
         * The configured model id, one of the provider's models in the catalog
         * `getAiProviders` returns.
         */
        public val model: kotlin.String,
        /**
         * The provider API base URL override, an absolute `https` URL, or null
         * when completions go to the provider's default endpoint.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val base_url: com.mountaya.client.Url? = null,
        /**
         * A non-sensitive fragment of the configured credential, the last four
         * characters of the provider key, shown so an admin can recognize the key
         * without revealing it. Empty for a key shorter than 12 characters, of which
         * four would be too large a part.
         */
        public val credential_hint: kotlin.String,
        /**
         * The version of the configuration, the one the `ETag` of a read of it
         * carries, opaque and compared whole. Send it as `If-Match` to make
         * `patchOrganizationAiProviderConfig` or
         * `deleteOrganizationAiProviderConfig` conditional.
         */
        public val etag: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.AiProviderConfig &&
                other.provider == this.provider &&
                other.model == this.model &&
                other.base_url == this.base_url &&
                other.credential_hint == this.credential_hint &&
                other.etag == this.etag

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.provider.hashCode()
            hash = 31 * hash + this.model.hashCode()
            hash = 31 * hash + (this.base_url?.hashCode() ?: 0)
            hash = 31 * hash + this.credential_hint.hashCode()
            hash = 31 * hash + this.etag.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "AiProviderConfig(" +
                "provider=" + this.provider +
                ", model=" + this.model +
                ", base_url=" + this.base_url +
                ", credential_hint=" + this.credential_hint +
                ", etag=" + this.etag +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.AiProviderConfig], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.AiProviderConfig]. */
        public companion object {
            /** Returns a [AdminSchema.AiProviderConfig] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                provider: AdminSchema.AiProvider,
                model: kotlin.String,
                base_url: com.mountaya.client.Url?,
                credential_hint: kotlin.String,
                etag: kotlin.String,
                optional: AdminSchema.AiProviderConfig.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.AiProviderConfig {
                val given = AdminSchema.AiProviderConfig.Builder().apply(optional)
                return AdminSchema.AiProviderConfig(
                    provider = provider,
                    model = model,
                    base_url = base_url,
                    credential_hint = credential_hint,
                    etag = etag,
                )
            }
        }
    }

    /**
     * An organization's AI provider configuration for the studio co-pilot, created
     * when it has none. The credential is write-only: it is accepted here, encrypted
     * at rest, and never returned by any operation. Only owners and admins may
     * submit it.
     */
    @kotlinx.serialization.Serializable
    public class AiProviderConfigCreateParams private constructor(
        /**
         * The AI provider to use. It cannot be changed afterwards: delete the
         * configuration and create one to switch providers.
         */
        public val provider: AdminSchema.AiProvider,
        /**
         * The model id to use. It must be one of the provider's models in the
         * catalog `getAiProviders` returns, which is what decides whether a value
         * is accepted; the ceiling below is the envelope bound, not the
         * vocabulary.
         */
        public val model: kotlin.String,
        /**
         * A provider API base URL override, for routing completions through a
         * gateway of the organization's own. Optional and not nullable: omit it to
         * use the provider's default endpoint.
         *
         * Must be an absolute `https` URL on a public host, with no credentials in
         * the URL and no query or fragment. Requests to this endpoint carry the
         * configured provider credential, so plaintext `http`, loopback and private
         * addresses, and internal-only names are all refused.
         */
        public val base_url: @kotlinx.serialization.Serializable(with = com.mountaya.client.SentUrlSerializer::class) com.mountaya.client.Url? = null,
        /**
         * The provider credential as a set of named fields. Each provider declares
         * the fields it requires in the catalog `getAiProviders` returns; read
         * them from there rather than assuming a shape. Every field the catalog
         * names is sent, each non-empty, or the missing ones are a `400`; a field
         * the catalog does not name is a `400` naming it. Write-only: never
         * returned by any operation; a read answers with `credential_hint`
         * instead.
         *
         * At most eight fields, each named in snake_case and holding at most 4096
         * characters.
         *
         * A request sends it, and the server never answers it.
         */
        public val credential: kotlin.collections.Map<kotlin.String, kotlin.String>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.AiProviderConfigCreateParams &&
                other.provider == this.provider &&
                other.model == this.model &&
                other.base_url == this.base_url &&
                other.credential == this.credential

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.provider.hashCode()
            hash = 31 * hash + this.model.hashCode()
            hash = 31 * hash + (this.base_url?.hashCode() ?: 0)
            hash = 31 * hash + this.credential.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "AiProviderConfigCreateParams(" +
                "provider=" + this.provider +
                ", model=" + this.model +
                ", base_url=" + this.base_url +
                ", credential=" + this.credential +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.AiProviderConfigCreateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: com.mountaya.client.Url? = null

            /**
             * A provider API base URL override, for routing completions through a
             * gateway of the organization's own. Optional and not nullable: omit it to
             * use the provider's default endpoint.
             *
             * Must be an absolute `https` URL on a public host, with no credentials in
             * the URL and no query or fragment. Requests to this endpoint carry the
             * configured provider credential, so plaintext `http`, loopback and private
             * addresses, and internal-only names are all refused.
             */
            public var base_url: com.mountaya.client.Url
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [AdminSchema.AiProviderConfigCreateParams]. */
        public companion object {
            /** Returns a [AdminSchema.AiProviderConfigCreateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                provider: AdminSchema.AiProvider,
                model: kotlin.String,
                credential: kotlin.collections.Map<kotlin.String, kotlin.String>,
                optional: AdminSchema.AiProviderConfigCreateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.AiProviderConfigCreateParams {
                val given = AdminSchema.AiProviderConfigCreateParams.Builder().apply(optional)
                return AdminSchema.AiProviderConfigCreateParams(
                    provider = provider,
                    model = model,
                    base_url = given.slot2,
                    credential = credential,
                )
            }
        }
    }

    /**
     * An organization's AI provider configuration as a webhook event carries it: the
     * provider, the model, and the hint of the credential, which is never carried.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class AiProviderConfigEventObject private constructor(
        /**
         * The kind of object this is, always `organization.ai_provider_config`: the family
         * of the event types that carry it, and what tells the members of
         * `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.ai_provider_config",
        /** Identifier of the organization the configuration belongs to. */
        public val organization_id: kotlin.String,
        /** The provider agent runs are routed through. */
        public val provider: AdminSchema.AiProvider,
        /** The provider's model agent runs use. */
        public val model: kotlin.String,
        /**
         * The last four characters of the stored credential, as
         * `getOrganizationAiProviderConfig` shows them, and empty for a credential shorter
         * than 12 characters, of which four would be too large a part.
         */
        public val credential_hint: kotlin.String,
        /**
         * The configuration's version once this change was made. It climbs with every change
         * a read of the configuration shows, so of two events about one configuration the
         * one carrying the higher `version` is the later state: events are not delivered in
         * order. A delete is the configuration's last version change:
         * `organization.ai_provider_config.deleted` carries a version past every one the
         * configuration held, and below the one a configuration created afterwards starts
         * at, so an event that arrives after the delete is never mistaken for the later
         * state.
         */
        public val version: kotlin.Long,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.AiProviderConfigEventObject &&
                other.`object` == this.`object` &&
                other.organization_id == this.organization_id &&
                other.provider == this.provider &&
                other.model == this.model &&
                other.credential_hint == this.credential_hint &&
                other.version == this.version

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.provider.hashCode()
            hash = 31 * hash + this.model.hashCode()
            hash = 31 * hash + this.credential_hint.hashCode()
            hash = 31 * hash + this.version.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "AiProviderConfigEventObject(" +
                "object=" + this.`object` +
                ", organization_id=" + this.organization_id +
                ", provider=" + this.provider +
                ", model=" + this.model +
                ", credential_hint=" + this.credential_hint +
                ", version=" + this.version +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.AiProviderConfigEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.AiProviderConfigEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.AiProviderConfigEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                organization_id: kotlin.String,
                provider: AdminSchema.AiProvider,
                model: kotlin.String,
                credential_hint: kotlin.String,
                version: kotlin.Long,
                optional: AdminSchema.AiProviderConfigEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.AiProviderConfigEventObject {
                val given = AdminSchema.AiProviderConfigEventObject.Builder().apply(optional)
                return AdminSchema.AiProviderConfigEventObject(
                    organization_id = organization_id,
                    provider = provider,
                    model = model,
                    credential_hint = credential_hint,
                    version = version,
                )
            }
        }
    }

    /**
     * A change to an organization's AI provider configuration, merged into what is
     * stored: a field left out keeps its value. The provider is not among the
     * fields: delete the configuration and create one to switch providers. Only
     * owners and admins may submit it.
     */
    @kotlinx.serialization.Serializable
    public class AiProviderConfigUpdateParams private constructor(
        /**
         * The model id to use, one of the provider's models in the catalog
         * `getAiProviders` returns. Optional and not nullable: omit it to keep the
         * current model.
         */
        public val model: kotlin.String? = null,
        @kotlinx.serialization.SerialName("base_url")
        private val settable1: com.mountaya.client.Settable<@kotlinx.serialization.Serializable(with = com.mountaya.client.SentUrlSerializer::class) com.mountaya.client.Url?> = com.mountaya.client.Settable.Absent,
        /**
         * A new provider credential, which REPLACES the stored one whole: every
         * field the catalog names for the configured provider is sent, each
         * non-empty, as on a create. Optional and not nullable: omit it to keep
         * the stored credential, so the model can be changed without sending the
         * key again. A `base_url` changed to a new URL is the exception, and
         * requires it. Write-only: never returned by any operation.
         *
         * At most eight fields, each named in snake_case and holding at most 4096
         * characters.
         *
         * A request sends it, and the server never answers it.
         */
        public val credential: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,
    ) {
        /**
         * A provider API base URL override, held to the rules
         * `AiProviderConfigCreateParams` states for it. Omit it to keep the
         * current one, and send null to clear it, which routes completions to the
         * provider's default endpoint again.
         *
         * Requests to this endpoint carry the stored credential, so moving them to
         * a new host is a change only someone holding that credential may make: a
         * URL other than the one stored must come with `credential` in the same
         * request, and without it the update is a `400` with one entry at
         * `["credential"]` and nothing is changed. Null, which sends completions
         * back to the provider's own endpoint, and the URL already stored need no
         * credential.
         */
        public val base_url: com.mountaya.client.Url?
            get() = settable1.valueOrNull

        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.AiProviderConfigUpdateParams &&
                other.model == this.model &&
                other.settable1 == this.settable1 &&
                other.credential == this.credential

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.model?.hashCode() ?: 0)
            hash = 31 * hash + this.settable1.hashCode()
            hash = 31 * hash + (this.credential?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "AiProviderConfigUpdateParams(" +
                "model=" + this.model +
                ", base_url=" + this.base_url +
                ", credential=" + this.credential +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.AiProviderConfigUpdateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: com.mountaya.client.Settable<@kotlinx.serialization.Serializable(with = com.mountaya.client.SentUrlSerializer::class) com.mountaya.client.Url?> = com.mountaya.client.Settable.Absent
            internal var slot2: kotlin.collections.Map<kotlin.String, kotlin.String>? = null

            /**
             * The model id to use, one of the provider's models in the catalog
             * `getAiProviders` returns. Optional and not nullable: omit it to keep the
             * current model.
             */
            public var model: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * A provider API base URL override, held to the rules
             * `AiProviderConfigCreateParams` states for it. Omit it to keep the
             * current one, and send null to clear it, which routes completions to the
             * provider's default endpoint again.
             *
             * Requests to this endpoint carry the stored credential, so moving them to
             * a new host is a change only someone holding that credential may make: a
             * URL other than the one stored must come with `credential` in the same
             * request, and without it the update is a `400` with one entry at
             * `["credential"]` and nothing is changed. Null, which sends completions
             * back to the provider's own endpoint, and the URL already stored need no
             * credential.
             *
             * Left unset it is not sent and the stored value is untouched; set to `null` it is sent as `null`, which clears it.
             */
            public var base_url: com.mountaya.client.Url?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = com.mountaya.client.Settable.Present(value)
                }

            /**
             * A new provider credential, which REPLACES the stored one whole: every
             * field the catalog names for the configured provider is sent, each
             * non-empty, as on a create. Optional and not nullable: omit it to keep
             * the stored credential, so the model can be changed without sending the
             * key again. A `base_url` changed to a new URL is the exception, and
             * requires it. Write-only: never returned by any operation.
             *
             * At most eight fields, each named in snake_case and holding at most 4096
             * characters.
             *
             * A request sends it, and the server never answers it.
             */
            public var credential: kotlin.collections.Map<kotlin.String, kotlin.String>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [AdminSchema.AiProviderConfigUpdateParams]. */
        public companion object {
            /** Returns a [AdminSchema.AiProviderConfigUpdateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.AiProviderConfigUpdateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.AiProviderConfigUpdateParams {
                val given = AdminSchema.AiProviderConfigUpdateParams.Builder().apply(optional)
                return AdminSchema.AiProviderConfigUpdateParams(
                    model = given.slot0,
                    settable1 = given.slot1,
                    credential = given.slot2,
                )
            }
        }
    }

    /**
     * A role that can be granted to a member, by invitation or by changing an
     * existing member's role. It is `OrganizationRole` without `owner`: an
     * organization has exactly one owner, and that role is never granted through
     * either operation.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class AssignableOrganizationRole(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Manages content, members and their roles, and the organization's keys, settings, billing and AI configuration. */
            public val Admin: AssignableOrganizationRole = AssignableOrganizationRole("admin")

            /** Can add and edit content. */
            public val Contributor: AssignableOrganizationRole = AssignableOrganizationRole("contributor")

            /** Read-only access. */
            public val Viewer: AssignableOrganizationRole = AssignableOrganizationRole("viewer")
        }
    }

    /**
     * The billing information held for an organization: the name and email an
     * invoice is addressed to, the billing address, and the tax identifier when one
     * is registered. The address is absent when none is on file, and so is the tax
     * identifier.
     */
    @kotlinx.serialization.Serializable
    public class BillingInfo private constructor(
        /**
         * The name an invoice is addressed to. It starts as the organization's own
         * name and is changed through the billing information update. Never empty:
         * an update that sends an empty name restores the organization's own rather
         * than clearing this. The ceiling is the update's hundred characters rather
         * than the thirty an organization name is held to, because a payer is a
         * legal entity and the two are not the same field.
         */
        public val name: kotlin.String,
        /**
         * The address invoices and payment notices are sent to. It starts as the
         * email address of the member who opened billing, which on this API is the
         * user the session token names on `postOrganizationBillingProvision`, and
         * is changed through the billing information update.
         */
        public val email: kotlin.String,
        /**
         * The billing address. Absent when none is on file. While none is, saving
         * a card fills it from the card's billing address, when the card carries
         * one.
         */
        public val address: AdminSchema.Address? = null,
        /**
         * The tax identifier registered against the account, such as an EU VAT
         * number. Absent when none is registered.
         */
        public val tax_id: AdminSchema.TaxId? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.BillingInfo &&
                other.name == this.name &&
                other.email == this.email &&
                other.address == this.address &&
                other.tax_id == this.tax_id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + this.email.hashCode()
            hash = 31 * hash + (this.address?.hashCode() ?: 0)
            hash = 31 * hash + (this.tax_id?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "BillingInfo(" +
                "name=" + this.name +
                ", email=" + this.email +
                ", address=" + this.address +
                ", tax_id=" + this.tax_id +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.BillingInfo], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: AdminSchema.Address? = null
            internal var slot3: AdminSchema.TaxId? = null

            /**
             * The billing address. Absent when none is on file. While none is, saving
             * a card fills it from the card's billing address, when the card carries
             * one.
             */
            public var address: AdminSchema.Address
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * The tax identifier registered against the account, such as an EU VAT
             * number. Absent when none is registered.
             */
            public var tax_id: AdminSchema.TaxId
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [AdminSchema.BillingInfo]. */
        public companion object {
            /** Returns a [AdminSchema.BillingInfo] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                name: kotlin.String,
                email: kotlin.String,
                optional: AdminSchema.BillingInfo.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.BillingInfo {
                val given = AdminSchema.BillingInfo.Builder().apply(optional)
                return AdminSchema.BillingInfo(
                    name = name,
                    email = email,
                    address = given.slot2,
                    tax_id = given.slot3,
                )
            }
        }
    }

    /**
     * An organization's billing information as a webhook event carries it: whose it is, and
     * which of its parts the change wrote. What invoices are addressed to is never carried:
     * `getOrganizationBillingInfo` reads it.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class BillingInfoEventObject private constructor(
        /**
         * The kind of object this is, always `organization.billing.info`: the family of the
         * event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.billing.info",
        /** Identifier of the organization the billing information belongs to. */
        public val organization_id: kotlin.String,
        /**
         * The parts of the billing information the change wrote, each once. A part
         * written with the value it already held is listed too.
         */
        public val fields: kotlin.collections.List<AdminSchema.BillingInfoField>,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.BillingInfoEventObject &&
                other.`object` == this.`object` &&
                other.organization_id == this.organization_id &&
                other.fields == this.fields

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.fields.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "BillingInfoEventObject(" +
                "object=" + this.`object` +
                ", organization_id=" + this.organization_id +
                ", fields=" + this.fields +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.BillingInfoEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.BillingInfoEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.BillingInfoEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                organization_id: kotlin.String,
                fields: kotlin.collections.List<AdminSchema.BillingInfoField>,
                optional: AdminSchema.BillingInfoEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.BillingInfoEventObject {
                val given = AdminSchema.BillingInfoEventObject.Builder().apply(optional)
                return AdminSchema.BillingInfoEventObject(
                    organization_id = organization_id,
                    fields = fields,
                )
            }
        }
    }

    /**
     * A part of an organization's billing information a change wrote: what its
     * invoices are addressed to.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class BillingInfoField(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The name invoices are addressed to. */
            public val Name: BillingInfoField = BillingInfoField("name")

            /** The email invoices and receipts are sent to. */
            public val Email: BillingInfoField = BillingInfoField("email")

            /** The postal address invoices carry. */
            public val Address: BillingInfoField = BillingInfoField("address")

            /** The tax identifier invoices carry. */
            public val TaxId: BillingInfoField = BillingInfoField("tax-id")
        }
    }

    /**
     * The tax identifier to write with a billing information update. The type is
     * optional and resolves to `eu_vat` when omitted; any other type the payment
     * provider recognizes is registered as given. An empty value clears whatever
     * tax identifier is on file.
     */
    @kotlinx.serialization.Serializable
    public class BillingInfoTaxIdParams private constructor(
        /**
         * The registration number, for example a VAT number. An empty string
         * clears the tax identifier already on file.
         */
        public val value: kotlin.String,
        /**
         * Which register the value belongs to. Optional, and resolves to `eu_vat`
         * when omitted. The vocabulary is the payment provider's rather than this
         * contract's, so what is declared here is the length ceiling; a type the
         * provider does not recognize is refused with a `400`.
         *
         * Left out, the server applies its default: `"eu_vat"`.
         */
        public val type: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.BillingInfoTaxIdParams &&
                other.value == this.value &&
                other.type == this.type

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.value.hashCode()
            hash = 31 * hash + (this.type?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "BillingInfoTaxIdParams(" +
                "value=" + this.value +
                ", type=" + this.type +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.BillingInfoTaxIdParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null

            /**
             * Which register the value belongs to. Optional, and resolves to `eu_vat`
             * when omitted. The vocabulary is the payment provider's rather than this
             * contract's, so what is declared here is the length ceiling; a type the
             * provider does not recognize is refused with a `400`.
             *
             * Left out, the server applies its default: `"eu_vat"`.
             */
            public var type: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [AdminSchema.BillingInfoTaxIdParams]. */
        public companion object {
            /** Returns a [AdminSchema.BillingInfoTaxIdParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                value: kotlin.String,
                optional: AdminSchema.BillingInfoTaxIdParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.BillingInfoTaxIdParams {
                val given = AdminSchema.BillingInfoTaxIdParams.Builder().apply(optional)
                return AdminSchema.BillingInfoTaxIdParams(
                    value = value,
                    type = given.slot1,
                )
            }
        }
    }

    /**
     * A change to an organization's billing information. Every field is optional
     * and every field present is a replacement, so a form that submits the whole
     * record writes the whole record and one that submits a single group leaves the
     * rest as it stands.
     *
     * What each field does when it is absent, present, or present and empty:
     * - `name` absent leaves the invoicing name as it is. Present and empty
     *   restores the organization's own name rather than clearing it, which is why
     *   no minimum length is declared; any other value is at least two characters.
     * - `email` absent leaves the invoicing address as it is.
     * - `address` absent leaves the address as it is. An address object whose
     *   fields are all empty clears it.
     * - `tax_id` absent leaves the tax identifier as it is. A `tax_id` with an
     *   empty value clears it, and one with a value replaces it.
     */
    @kotlinx.serialization.Serializable
    public class BillingInfoUpdateParams private constructor(
        /**
         * The name to address invoices to. Present and empty restores the
         * organization's own name, which is why no minimum is declared: any OTHER
         * value is at least two characters, and the empty string is the one way to
         * ask for the default back. A minimum here would take that away from every
         * generated client.
         */
        public val name: kotlin.String? = null,
        /**
         * The address to send invoices and payment notices to. Optional and not
         * nullable: omit it to keep the current address.
         */
        public val email: kotlin.String? = null,
        /**
         * The billing address to write. Optional and not nullable: omit it to keep
         * the address on file; an object whose fields are all empty clears it.
         */
        public val address: AdminSchema.Address? = null,
        /**
         * The tax identifier to register. Optional and not nullable: omit it to keep
         * the one on file; a `value` of the empty string clears it.
         */
        public val tax_id: AdminSchema.BillingInfoTaxIdParams? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.BillingInfoUpdateParams &&
                other.name == this.name &&
                other.email == this.email &&
                other.address == this.address &&
                other.tax_id == this.tax_id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.email?.hashCode() ?: 0)
            hash = 31 * hash + (this.address?.hashCode() ?: 0)
            hash = 31 * hash + (this.tax_id?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "BillingInfoUpdateParams(" +
                "name=" + this.name +
                ", email=" + this.email +
                ", address=" + this.address +
                ", tax_id=" + this.tax_id +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.BillingInfoUpdateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null
            internal var slot2: AdminSchema.Address? = null
            internal var slot3: AdminSchema.BillingInfoTaxIdParams? = null

            /**
             * The name to address invoices to. Present and empty restores the
             * organization's own name, which is why no minimum is declared: any OTHER
             * value is at least two characters, and the empty string is the one way to
             * ask for the default back. A minimum here would take that away from every
             * generated client.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The address to send invoices and payment notices to. Optional and not
             * nullable: omit it to keep the current address.
             */
            public var email: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * The billing address to write. Optional and not nullable: omit it to keep
             * the address on file; an object whose fields are all empty clears it.
             */
            public var address: AdminSchema.Address
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * The tax identifier to register. Optional and not nullable: omit it to keep
             * the one on file; a `value` of the empty string clears it.
             */
            public var tax_id: AdminSchema.BillingInfoTaxIdParams
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [AdminSchema.BillingInfoUpdateParams]. */
        public companion object {
            /** Returns a [AdminSchema.BillingInfoUpdateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.BillingInfoUpdateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.BillingInfoUpdateParams {
                val given = AdminSchema.BillingInfoUpdateParams.Builder().apply(optional)
                return AdminSchema.BillingInfoUpdateParams(
                    name = given.slot0,
                    email = given.slot1,
                    address = given.slot2,
                    tax_id = given.slot3,
                )
            }
        }
    }

    /**
     * The recurring interval a price bills on. It reads beside a catalog price's
     * amount. Only recurring intervals appear: the catalog holds no one-time
     * prices, and the one price it sells, the Data API's, recurs every month.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class BillingInterval(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The price recurs every month. */
            public val Month: BillingInterval = BillingInterval("month")

            /** The price recurs every year. The catalog sells no such price. */
            public val Year: BillingInterval = BillingInterval("year")
        }
    }

    /**
     * What one price on a catalog product costs, for display. The catalog sells
     * one product, the Data API, on one metered monthly price: it is priced on
     * what is used, so it carries no flat amount and reads `unit_amount: null`
     * with `metered: true`. A price that is not metered would carry its flat
     * figure in `unit_amount` instead.
     */
    @kotlinx.serialization.Serializable
    public class BillingPricing private constructor(
        /**
         * The flat per-unit amount, in the currency's minor unit, so 15000 in EUR
         * is 150.00 euros. Null for a metered price, which has no flat amount to
         * show.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val unit_amount: kotlin.Int? = null,
        /** The lowercase ISO 4217 code the price bills in, for example `eur`. */
        public val currency: kotlin.String,
        /**
         * How often the price recurs: `month` for the one price the catalog
         * sells.
         */
        public val interval: AdminSchema.BillingInterval,
        /**
         * Whether the price is billed on usage. A metered price has no
         * `unit_amount`, so a reader shows it as metered rather than as a figure.
         */
        public val metered: kotlin.Boolean,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.BillingPricing &&
                other.unit_amount == this.unit_amount &&
                other.currency == this.currency &&
                other.interval == this.interval &&
                other.metered == this.metered

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.unit_amount?.hashCode() ?: 0)
            hash = 31 * hash + this.currency.hashCode()
            hash = 31 * hash + this.interval.hashCode()
            hash = 31 * hash + this.metered.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "BillingPricing(" +
                "unit_amount=" + this.unit_amount +
                ", currency=" + this.currency +
                ", interval=" + this.interval +
                ", metered=" + this.metered +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.BillingPricing], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.BillingPricing]. */
        public companion object {
            /** Returns a [AdminSchema.BillingPricing] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                unit_amount: kotlin.Int?,
                currency: kotlin.String,
                interval: AdminSchema.BillingInterval,
                metered: kotlin.Boolean,
                optional: AdminSchema.BillingPricing.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.BillingPricing {
                val given = AdminSchema.BillingPricing.Builder().apply(optional)
                return AdminSchema.BillingPricing(
                    unit_amount = unit_amount,
                    currency = currency,
                    interval = interval,
                    metered = metered,
                )
            }
        }
    }

    /**
     * One entry in the billing catalog. There is one product per unit of access an
     * organization can buy, and each carries the capabilities it grants, how it is
     * quantified, and the price it is sold on. The Data API is the one
     * product sold this way. What an agreement with Mountaya grants is not in the
     * catalog.
     */
    @kotlinx.serialization.Serializable
    public class BillingProduct private constructor(
        /**
         * Stable identifier of the product. It is what a client keys on; the label
         * beside it is what a client shows.
         */
        public val key: kotlin.String,
        /**
         * The product's name, as it reads to a buyer. Unbounded, unlike the labels
         * a caller authors on a waypoint or an AI credential field, because a
         * caller never writes it.
         */
        public val label: kotlin.String,
        /** The capabilities the organization holds once this product is bought. */
        public val granted_capabilities: kotlin.collections.List<AdminSchema.Capability>,
        /** How the product is counted. */
        public val unit_kind: AdminSchema.BillingUnitKind,
        /**
         * Whether the product is agreed rather than bought: it is granted by the
         * organization's agreement with Mountaya, and no card opens it.
         */
        public val contact_sales_only: kotlin.Boolean,
        /**
         * The prices the product is sold on, one entry each. The Data API is sold
         * on one metered price, billed by the month. A price whose display figure
         * could not be resolved is left out rather than sent empty, so a product
         * where none resolved carries an empty list and a reader shows it without
         * an amount rather than failing the whole catalog.
         */
        public val prices: kotlin.collections.List<AdminSchema.BillingPricing>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.BillingProduct &&
                other.key == this.key &&
                other.label == this.label &&
                other.granted_capabilities == this.granted_capabilities &&
                other.unit_kind == this.unit_kind &&
                other.contact_sales_only == this.contact_sales_only &&
                other.prices == this.prices

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.key.hashCode()
            hash = 31 * hash + this.label.hashCode()
            hash = 31 * hash + this.granted_capabilities.hashCode()
            hash = 31 * hash + this.unit_kind.hashCode()
            hash = 31 * hash + this.contact_sales_only.hashCode()
            hash = 31 * hash + this.prices.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "BillingProduct(" +
                "key=" + this.key +
                ", label=" + this.label +
                ", granted_capabilities=" + this.granted_capabilities +
                ", unit_kind=" + this.unit_kind +
                ", contact_sales_only=" + this.contact_sales_only +
                ", prices=" + this.prices +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.BillingProduct], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.BillingProduct]. */
        public companion object {
            /** Returns a [AdminSchema.BillingProduct] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                key: kotlin.String,
                label: kotlin.String,
                granted_capabilities: kotlin.collections.List<AdminSchema.Capability>,
                unit_kind: AdminSchema.BillingUnitKind,
                contact_sales_only: kotlin.Boolean,
                prices: kotlin.collections.List<AdminSchema.BillingPricing>,
                optional: AdminSchema.BillingProduct.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.BillingProduct {
                val given = AdminSchema.BillingProduct.Builder().apply(optional)
                return AdminSchema.BillingProduct(
                    key = key,
                    label = label,
                    granted_capabilities = granted_capabilities,
                    unit_kind = unit_kind,
                    contact_sales_only = contact_sales_only,
                    prices = prices,
                )
            }
        }
    }

    /**
     * How a billing product is counted. No kind takes a quantity from a caller: the
     * one product the catalog sells is metered.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class BillingUnitKind(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Billed per request, metered and invoiced in arrears at the end of each billing period, which runs from the day of the month the subscription opened to the same day of the next month rather than over a calendar month. The Data API is counted this way. */
            public val PerRequest: BillingUnitKind = BillingUnitKind("per-request")
        }
    }

    /**
     * A product capability an organization holds. These are the units of access the
     * account is entitled to, and four of them share a name with the publishable
     * key scope they open: `publishing`, `studio-embedded`, `tiles` and `data`. A
     * request under one of those scopes answers `402` while the organization does
     * not hold the capability, and `data` opens the `mcp` scope as well. The other
     * two do not. `studio-online` has no key scope of its own: it is what every
     * change to an organization's itineraries, collections and zones needs,
     * whichever credential sends it. `white-label` gates no request at all.
     *
     * `publishing` is asked beyond its scope as well, whichever credential sends
     * the request: publishing an itinerary or a zone needs it beside
     * `studio-online`, downloading a draft as a file needs it, and what an
     * organization has published, its share links and its hosted embed included,
     * is served only while it holds it.
     *
     * `data` is the one capability an organization obtains on its own: the first
     * card it saves opens the metered Data API subscription, unless the customer,
     * or that card, still owes an invoice, which is the `402` and opens nothing,
     * and `data` is held while that subscription is in force. Every other capability is granted by
     * the organization's agreement with Mountaya, and `data` can be granted that
     * way as well. A new organization holds none of them.
     *
     * An agreement grants what each product needs beside it. Embedded Studio
     * (`studio-embedded`) is granted with Map Studio (`studio-online`), the Tile
     * API (`tiles`) and the Data API (`data`), since the editor it mounts edits
     * through Map Studio, draws its map on the Tile API and asks its previews of
     * the Data API. Map Publishing (`publishing`) is granted with the
     * `published-itineraries` and `published-zones` limits it is held to.
     * Mountaya does not record an agreement that leaves one of them out.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class Capability(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Map Studio, the editor, with live collaboration and the in-studio agent. Every change to an organization's itineraries, collections and zones needs it, so an organization that does not hold it reads, deletes and takes down what it has and makes nothing new. Publishing an itinerary or a zone needs Map Publishing beside it. Granted by the organization's agreement. A person's personal space, owned by no organization, is a separate matter that no organization's agreement decides. */
            public val StudioOnline: Capability = Capability("studio-online")

            /** Map Publishing, for putting an organization's maps in front of people outside it. It covers publishing an itinerary or a zone, within the `published-itineraries` and `published-zones` limits the organization's agreement sets, Mountaya's hosted embed, which renders an interactive map inside another site, share links, downloading an itinerary as a route file such as GPX, the draft included, and video export. While the organization does not hold it, what it has published is withdrawn rather than refused, its share links, its hosted embed and its published snapshots answering as if nothing were published, and taking an itinerary or a zone down never needs it. Granted by the organization's agreement. The tiles Mountaya's hosted embed draws are recorded as the organization's Tile API usage on the `embedding` surface and counted toward its per-minute limit and its monthly allowance, as its own clients' tiles are. */
            public val Publishing: Capability = Capability("publishing")

            /** The Tile API. Granted by the organization's agreement. The hosted embed does not need it, since the tiles it draws are gated on Map Publishing, and share-link tiles do not either; each is recorded on a surface of its own. The hosted embed's tiles count toward the organization's per-minute limit and its monthly allowance, and share-link tiles never do. */
            public val Tiles: Capability = Capability("tiles")

            /** The Data API, the Agent Skills and the MCP server, which a key reaches through the `mcp` scope. Held while the metered Data API subscription the organization's first card opens is in force, or by the organization's agreement. */
            public val Data: Capability = Capability("data")

            /** Embedded Studio, the editor mounted inside a customer's own product, which is what a publishable key needs to act as a member. Granted by the organization's agreement. */
            public val StudioEmbedded: Capability = Capability("studio-embedded")

            /** Suppression of the Mountaya mark on an embedded map and on exported video. Granted by the organization's agreement. Tile attribution is a licence obligation and is never suppressed by it. */
            public val WhiteLabel: Capability = Capability("white-label")
        }
    }

    /**
     * A collection as a webhook event carries it: its identifiers, its name and description,
     * whether it is private, and its version. `getCollection` reads the itineraries and
     * zones it holds.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class CollectionEventObject private constructor(
        /**
         * The kind of object this is, always `collection`: the family of the event types
         * that carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "collection",
        /** Identifier of the collection. */
        public val id: kotlin.String,
        /** Identifier of the organization the collection belongs to. */
        public val organization_id: kotlin.String,
        /** The collection's name. */
        public val name: kotlin.String,
        /** The collection's description, null when it has none. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val description: kotlin.String? = null,
        /** Whether the collection is private to the organization. */
        public val is_private: kotlin.Boolean,
        /**
         * The collection's version once this change was made. It climbs with every change a
         * read of the collection shows, so of two events about one collection the one
         * carrying the higher `version` is the later state: events are not delivered in
         * order. A delete is the collection's last version change: `collection.deleted`
         * carries the version one past the last the collection held, so an event about it
         * that arrives after its delete is never the later state.
         */
        public val version: kotlin.Long,
        /** The user who created the collection. */
        public val created_by: com.mountaya.client.generated.RestSchema.RelationToUser,
        /** When the collection was created. */
        public val created_at: kotlin.time.Instant,
        /** When the collection last changed. */
        public val updated_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.CollectionEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.name == this.name &&
                other.description == this.description &&
                other.is_private == this.is_private &&
                other.version == this.version &&
                other.created_by == this.created_by &&
                other.created_at == this.created_at &&
                other.updated_at == this.updated_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + this.is_private.hashCode()
            hash = 31 * hash + this.version.hashCode()
            hash = 31 * hash + this.created_by.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.updated_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", is_private=" + this.is_private +
                ", version=" + this.version +
                ", created_by=" + this.created_by +
                ", created_at=" + this.created_at +
                ", updated_at=" + this.updated_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.CollectionEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.CollectionEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.CollectionEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                name: kotlin.String,
                description: kotlin.String?,
                is_private: kotlin.Boolean,
                version: kotlin.Long,
                created_by: com.mountaya.client.generated.RestSchema.RelationToUser,
                created_at: kotlin.time.Instant,
                updated_at: kotlin.time.Instant,
                optional: AdminSchema.CollectionEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.CollectionEventObject {
                val given = AdminSchema.CollectionEventObject.Builder().apply(optional)
                return AdminSchema.CollectionEventObject(
                    id = id,
                    organization_id = organization_id,
                    name = name,
                    description = description,
                    is_private = is_private,
                    version = version,
                    created_by = created_by,
                    created_at = created_at,
                    updated_at = updated_at,
                )
            }
        }
    }

    /**
     * An itinerary's place in a collection: which collection, which itinerary, and where it
     * sits. `getCollection` reads the order of the whole.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class CollectionItineraryEventObject private constructor(
        /**
         * The kind of object this is, always `collection.itinerary`: the family of the event
         * types that carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "collection.itinerary",
        /** Identifier of the collection. */
        public val collection_id: kotlin.String,
        /** Identifier of the itinerary. */
        public val itinerary_id: kotlin.String,
        /**
         * Where the itinerary sits in the collection once this change was made, counted from
         * 0. Absent on `collection.itinerary.removed`. A move shifts the itineraries between
         * its two positions, which record no event of their own.
         */
        public val position: kotlin.Int? = null,
        /**
         * The collection's `version` once this change was made, which every change to what
         * it holds moves. Of two events about one collection, the one carrying the higher
         * figure is the later state.
         */
        public val collection_version: kotlin.Long,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.CollectionItineraryEventObject &&
                other.`object` == this.`object` &&
                other.collection_id == this.collection_id &&
                other.itinerary_id == this.itinerary_id &&
                other.position == this.position &&
                other.collection_version == this.collection_version

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.collection_id.hashCode()
            hash = 31 * hash + this.itinerary_id.hashCode()
            hash = 31 * hash + (this.position?.hashCode() ?: 0)
            hash = 31 * hash + this.collection_version.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionItineraryEventObject(" +
                "object=" + this.`object` +
                ", collection_id=" + this.collection_id +
                ", itinerary_id=" + this.itinerary_id +
                ", position=" + this.position +
                ", collection_version=" + this.collection_version +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.CollectionItineraryEventObject], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot3: kotlin.Int? = null

            /**
             * Where the itinerary sits in the collection once this change was made, counted from
             * 0. Absent on `collection.itinerary.removed`. A move shifts the itineraries between
             * its two positions, which record no event of their own.
             */
            public var position: kotlin.Int
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [AdminSchema.CollectionItineraryEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.CollectionItineraryEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                collection_id: kotlin.String,
                itinerary_id: kotlin.String,
                collection_version: kotlin.Long,
                optional: AdminSchema.CollectionItineraryEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.CollectionItineraryEventObject {
                val given = AdminSchema.CollectionItineraryEventObject.Builder().apply(optional)
                return AdminSchema.CollectionItineraryEventObject(
                    collection_id = collection_id,
                    itinerary_id = itinerary_id,
                    position = given.slot3,
                    collection_version = collection_version,
                )
            }
        }
    }

    /**
     * A collection's share link as a webhook event carries it: the collection it opens and
     * when its token was minted. The token itself is never carried: `getCollectionShareLink`
     * reads it.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class CollectionShareLinkEventObject private constructor(
        /**
         * The kind of object this is, always `collection.share_link`: the family of the
         * event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "collection.share_link",
        /** Identifier of the collection the link opens. */
        public val collection_id: kotlin.String,
        /** When the link's token was minted: when the link was created, or last rotated. */
        public val created_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.CollectionShareLinkEventObject &&
                other.`object` == this.`object` &&
                other.collection_id == this.collection_id &&
                other.created_at == this.created_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.collection_id.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionShareLinkEventObject(" +
                "object=" + this.`object` +
                ", collection_id=" + this.collection_id +
                ", created_at=" + this.created_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.CollectionShareLinkEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.CollectionShareLinkEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.CollectionShareLinkEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                collection_id: kotlin.String,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.CollectionShareLinkEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.CollectionShareLinkEventObject {
                val given = AdminSchema.CollectionShareLinkEventObject.Builder().apply(optional)
                return AdminSchema.CollectionShareLinkEventObject(
                    collection_id = collection_id,
                    created_at = created_at,
                )
            }
        }
    }

    /**
     * A zone's membership of a collection: which collection and which zone.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class CollectionZoneEventObject private constructor(
        /**
         * The kind of object this is, always `collection.zone`: the family of the event
         * types that carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "collection.zone",
        /** Identifier of the collection. */
        public val collection_id: kotlin.String,
        /** Identifier of the zone. */
        public val zone_id: kotlin.String,
        /**
         * The collection's `version` once this change was made, which every change to what
         * it holds moves. Of two events about one collection, the one carrying the higher
         * figure is the later state.
         */
        public val collection_version: kotlin.Long,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.CollectionZoneEventObject &&
                other.`object` == this.`object` &&
                other.collection_id == this.collection_id &&
                other.zone_id == this.zone_id &&
                other.collection_version == this.collection_version

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.collection_id.hashCode()
            hash = 31 * hash + this.zone_id.hashCode()
            hash = 31 * hash + this.collection_version.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "CollectionZoneEventObject(" +
                "object=" + this.`object` +
                ", collection_id=" + this.collection_id +
                ", zone_id=" + this.zone_id +
                ", collection_version=" + this.collection_version +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.CollectionZoneEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.CollectionZoneEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.CollectionZoneEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                collection_id: kotlin.String,
                zone_id: kotlin.String,
                collection_version: kotlin.Long,
                optional: AdminSchema.CollectionZoneEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.CollectionZoneEventObject {
                val given = AdminSchema.CollectionZoneEventObject.Builder().apply(optional)
                return AdminSchema.CollectionZoneEventObject(
                    collection_id = collection_id,
                    zone_id = zone_id,
                    collection_version = collection_version,
                )
            }
        }
    }

    /**
     * An invoice on the organization's billing account, open or already settled.
     * Amounts are in the currency's minor unit, for example cents. The two links
     * are where the payer views and downloads the document, and neither is present
     * while an invoice is still a draft. An organization billed by its agreement
     * with Mountaya, or awaiting one, is given the PDF alone.
     */
    @kotlinx.serialization.Serializable
    public class Invoice private constructor(
        /**
         * Identifier of the invoice. Opaque, and issued by the payment provider
         * rather than by Mountaya, so do not parse it as a UUID the way every `id`
         * Mountaya issues can be.
         */
        public val id: kotlin.String,
        /**
         * The invoice number a payer quotes, for example `MTYA-0042`. Absent while
         * the invoice is a draft and has not been numbered.
         */
        public val number: kotlin.String? = null,
        /** When the invoice was raised. */
        public val created_at: kotlin.time.Instant,
        /** The invoice total, in the currency's minor unit. */
        public val total: kotlin.Long,
        /**
         * What is still owed, in the currency's minor unit: zero once the invoice
         * is paid, and on a void invoice, which nobody owes. An uncollectible
         * invoice keeps what was left unpaid, and a draft carries what it will ask
         * for once finalized.
         */
        public val amount_due: kotlin.Long,
        /**
         * The three-letter ISO 4217 code the invoice is denominated in, for
         * example `eur`.
         */
        public val currency: kotlin.String,
        /** Where the invoice stands in its lifecycle. */
        public val status: AdminSchema.InvoiceStatus,
        /**
         * A link the payer follows to view and settle the invoice online. Absent
         * while the invoice is a draft, and on every invoice of an organization
         * billed by its agreement with Mountaya, or awaiting one, which settles its
         * invoices as the agreement says rather than online.
         */
        public val hosted_invoice_url: com.mountaya.client.Url? = null,
        /** A link to the invoice as a PDF. Absent while the invoice is a draft. */
        public val invoice_pdf: com.mountaya.client.Url? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.Invoice &&
                other.id == this.id &&
                other.number == this.number &&
                other.created_at == this.created_at &&
                other.total == this.total &&
                other.amount_due == this.amount_due &&
                other.currency == this.currency &&
                other.status == this.status &&
                other.hosted_invoice_url == this.hosted_invoice_url &&
                other.invoice_pdf == this.invoice_pdf

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + (this.number?.hashCode() ?: 0)
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.total.hashCode()
            hash = 31 * hash + this.amount_due.hashCode()
            hash = 31 * hash + this.currency.hashCode()
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + (this.hosted_invoice_url?.hashCode() ?: 0)
            hash = 31 * hash + (this.invoice_pdf?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "Invoice(" +
                "id=" + this.id +
                ", number=" + this.number +
                ", created_at=" + this.created_at +
                ", total=" + this.total +
                ", amount_due=" + this.amount_due +
                ", currency=" + this.currency +
                ", status=" + this.status +
                ", hosted_invoice_url=" + this.hosted_invoice_url +
                ", invoice_pdf=" + this.invoice_pdf +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.Invoice], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null
            internal var slot7: com.mountaya.client.Url? = null
            internal var slot8: com.mountaya.client.Url? = null

            /**
             * The invoice number a payer quotes, for example `MTYA-0042`. Absent while
             * the invoice is a draft and has not been numbered.
             */
            public var number: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * A link the payer follows to view and settle the invoice online. Absent
             * while the invoice is a draft, and on every invoice of an organization
             * billed by its agreement with Mountaya, or awaiting one, which settles its
             * invoices as the agreement says rather than online.
             */
            public var hosted_invoice_url: com.mountaya.client.Url
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /** A link to the invoice as a PDF. Absent while the invoice is a draft. */
            public var invoice_pdf: com.mountaya.client.Url
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }
        }

        /** Builds a [AdminSchema.Invoice]. */
        public companion object {
            /** Returns a [AdminSchema.Invoice] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                created_at: kotlin.time.Instant,
                total: kotlin.Long,
                amount_due: kotlin.Long,
                currency: kotlin.String,
                status: AdminSchema.InvoiceStatus,
                optional: AdminSchema.Invoice.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.Invoice {
                val given = AdminSchema.Invoice.Builder().apply(optional)
                return AdminSchema.Invoice(
                    id = id,
                    number = given.slot1,
                    created_at = created_at,
                    total = total,
                    amount_due = amount_due,
                    currency = currency,
                    status = status,
                    hosted_invoice_url = given.slot7,
                    invoice_pdf = given.slot8,
                )
            }
        }
    }

    /**
     * An invoice as a webhook event carries it: its identifier, its status and its amounts.
     * The links to the invoice's page and document are never carried:
     * `getOrganizationBillingInvoices` lists them.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class InvoiceEventObject private constructor(
        /**
         * The kind of object this is, always `organization.billing.invoice`: the family of
         * the event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.billing.invoice",
        /** Identifier of the invoice, as the payment provider issues it. It is not a UUID. */
        public val id: kotlin.String,
        /** Identifier of the organization the invoice is addressed to. */
        public val organization_id: kotlin.String,
        /** The invoice's status once this change was made. */
        public val status: AdminSchema.InvoiceStatus,
        /** The invoice's total, in the currency's minor unit. */
        public val total: kotlin.Long,
        /** What is left to pay, in the currency's minor unit. */
        public val amount_due: kotlin.Long,
        /** The invoice's currency, a lowercase ISO 4217 code. */
        public val currency: kotlin.String,
        /** When the invoice was raised. */
        public val created_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.InvoiceEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.status == this.status &&
                other.total == this.total &&
                other.amount_due == this.amount_due &&
                other.currency == this.currency &&
                other.created_at == this.created_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + this.total.hashCode()
            hash = 31 * hash + this.amount_due.hashCode()
            hash = 31 * hash + this.currency.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "InvoiceEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", status=" + this.status +
                ", total=" + this.total +
                ", amount_due=" + this.amount_due +
                ", currency=" + this.currency +
                ", created_at=" + this.created_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.InvoiceEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.InvoiceEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.InvoiceEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                status: AdminSchema.InvoiceStatus,
                total: kotlin.Long,
                amount_due: kotlin.Long,
                currency: kotlin.String,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.InvoiceEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.InvoiceEventObject {
                val given = AdminSchema.InvoiceEventObject.Builder().apply(optional)
                return AdminSchema.InvoiceEventObject(
                    id = id,
                    organization_id = organization_id,
                    status = status,
                    total = total,
                    amount_due = amount_due,
                    currency = currency,
                    created_at = created_at,
                )
            }
        }
    }

    /**
     * Where an invoice stands. The order is the lifecycle: an invoice is drafted,
     * opened for payment, and then ends paid or void, or is left uncollectible
     * and still owed.
     *
     * These five are the payment provider's own set, echoed rather than translated.
     * A client may switch on them, and should treat a value outside them as
     * unrecognized rather than as an error: it would mean the provider had added a
     * status, which is not something this contract gets to refuse on its behalf.
     *
     * `hosted_invoice_url` and `invoice_pdf` are absent while the status is `draft`
     * and carry a link from `open` onwards (`hosted_invoice_url` never does for an
     * organization billed by its agreement with Mountaya, or awaiting one), so a
     * client that renders a pay button reads this field and offers the button only
     * when `hosted_invoice_url` is present.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class InvoiceStatus(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** A draft, not finalized: it cannot be paid, and no link to pay it exists. */
            public val Draft: InvoiceStatus = InvoiceStatus("draft")

            /** Finalized and payable. This is the status a payer is asked to act on. */
            public val Open: InvoiceStatus = InvoiceStatus("open")

            /** Settled in full. Terminal. */
            public val Paid: InvoiceStatus = InvoiceStatus("paid")

            /** Canceled before it was paid, and owed by nobody. Terminal. An organization's invoice history never lists one. */
            public val Void: InvoiceStatus = InvoiceStatus("void")

            /** Finalized and unpaid after every retry, and still owed: the organization's card is charged for it before another Data API subscription opens, and the open is refused with a `402` while it stays unpaid. Paying it moves it to `paid`. */
            public val Uncollectible: InvoiceStatus = InvoiceStatus("uncollectible")
        }
    }

    /**
     * An itinerary as a webhook event carries it: its identifiers, its name and description,
     * its status, whether it is private, and its version. Its segments, waypoints and
     * geometry are never carried: `getItinerary` reads them.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class ItineraryEventObject private constructor(
        /**
         * The kind of object this is, always `itinerary`: the family of the event types that
         * carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "itinerary",
        /** Identifier of the itinerary. */
        public val id: kotlin.String,
        /** Identifier of the organization the itinerary belongs to. */
        public val organization_id: kotlin.String,
        /** Identifier of the itinerary's activity, in the catalog `getActivities` lists. */
        public val activity_id: kotlin.String,
        /** The itinerary's name. */
        public val name: kotlin.String,
        /** The itinerary's description, null when it has none. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val description: kotlin.String? = null,
        /** Whether the itinerary has a live publication once this change was made. */
        public val status: com.mountaya.client.generated.RestSchema.ItineraryStatus,
        /** Whether the itinerary is private to the organization. */
        public val is_private: kotlin.Boolean,
        /**
         * The itinerary's version once this change was made. It climbs with every change a
         * read of the itinerary shows, so of two events about one itinerary the one carrying
         * the higher `version` is the later state: events are not delivered in order. A
         * delete is the itinerary's last version change: `itinerary.deleted` carries the
         * version one past the last the itinerary held, so an event about it that arrives
         * after its delete is never the later state.
         */
        public val version: kotlin.Long,
        /** How the itinerary came to be. Present on `itinerary.created` alone. */
        public val origin: AdminSchema.ItineraryOrigin? = null,
        /** The user who created the itinerary. */
        public val created_by: com.mountaya.client.generated.RestSchema.RelationToUser,
        /** When the itinerary was created. */
        public val created_at: kotlin.time.Instant,
        /** When the itinerary last changed. */
        public val updated_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ItineraryEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.activity_id == this.activity_id &&
                other.name == this.name &&
                other.description == this.description &&
                other.status == this.status &&
                other.is_private == this.is_private &&
                other.version == this.version &&
                other.origin == this.origin &&
                other.created_by == this.created_by &&
                other.created_at == this.created_at &&
                other.updated_at == this.updated_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.activity_id.hashCode()
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + this.is_private.hashCode()
            hash = 31 * hash + this.version.hashCode()
            hash = 31 * hash + (this.origin?.hashCode() ?: 0)
            hash = 31 * hash + this.created_by.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.updated_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ItineraryEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", activity_id=" + this.activity_id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", status=" + this.status +
                ", is_private=" + this.is_private +
                ", version=" + this.version +
                ", origin=" + this.origin +
                ", created_by=" + this.created_by +
                ", created_at=" + this.created_at +
                ", updated_at=" + this.updated_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ItineraryEventObject], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot9: AdminSchema.ItineraryOrigin? = null

            /** How the itinerary came to be. Present on `itinerary.created` alone. */
            public var origin: AdminSchema.ItineraryOrigin
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot9 = value
                }
        }

        /** Builds a [AdminSchema.ItineraryEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.ItineraryEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                activity_id: kotlin.String,
                name: kotlin.String,
                description: kotlin.String?,
                status: com.mountaya.client.generated.RestSchema.ItineraryStatus,
                is_private: kotlin.Boolean,
                version: kotlin.Long,
                created_by: com.mountaya.client.generated.RestSchema.RelationToUser,
                created_at: kotlin.time.Instant,
                updated_at: kotlin.time.Instant,
                optional: AdminSchema.ItineraryEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ItineraryEventObject {
                val given = AdminSchema.ItineraryEventObject.Builder().apply(optional)
                return AdminSchema.ItineraryEventObject(
                    id = id,
                    organization_id = organization_id,
                    activity_id = activity_id,
                    name = name,
                    description = description,
                    status = status,
                    is_private = is_private,
                    version = version,
                    origin = given.slot9,
                    created_by = created_by,
                    created_at = created_at,
                    updated_at = updated_at,
                )
            }
        }
    }

    /**
     * How an itinerary came to be.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ItineraryOrigin(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Created empty, with `postItinerary`. */
            public val Created: ItineraryOrigin = ItineraryOrigin("created")

            /** Created from a file, with `postItineraryImport`. */
            public val Imported: ItineraryOrigin = ItineraryOrigin("imported")

            /** Copied from another itinerary, with `postItineraryDuplicate`. */
            public val Duplicated: ItineraryOrigin = ItineraryOrigin("duplicated")
        }
    }

    /**
     * A segment as a webhook event carries it: its identifiers, its name and how it is
     * drawn. Its geometry and figures are never carried, and neither is its place in the
     * chain: an insert, a removal at the head, a merge or a split renumbers the segments
     * after it, which record no event of their own. `getItinerary` reads the whole chain.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class ItinerarySegmentEventObject private constructor(
        /**
         * The kind of object this is, always `itinerary.segment`: the family of the event
         * types that carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "itinerary.segment",
        /** Identifier of the segment. */
        public val id: kotlin.String,
        /** Identifier of the itinerary the segment belongs to. */
        public val itinerary_id: kotlin.String,
        /** The segment's name. */
        public val name: kotlin.String,
        /** How the segment is drawn once this change was made. */
        public val mode: com.mountaya.client.generated.RestSchema.SegmentMode,
        /**
         * The itinerary's `version` once this change was made, which every change to its
         * segments, segment groups and waypoints moves. Of two events about one itinerary,
         * the one carrying the higher figure is the later state.
         */
        public val itinerary_version: kotlin.Long,
        /** When the segment was created. */
        public val created_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ItinerarySegmentEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.itinerary_id == this.itinerary_id &&
                other.name == this.name &&
                other.mode == this.mode &&
                other.itinerary_version == this.itinerary_version &&
                other.created_at == this.created_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.itinerary_id.hashCode()
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + this.mode.hashCode()
            hash = 31 * hash + this.itinerary_version.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerarySegmentEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", itinerary_id=" + this.itinerary_id +
                ", name=" + this.name +
                ", mode=" + this.mode +
                ", itinerary_version=" + this.itinerary_version +
                ", created_at=" + this.created_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ItinerarySegmentEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.ItinerarySegmentEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.ItinerarySegmentEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                itinerary_id: kotlin.String,
                name: kotlin.String,
                mode: com.mountaya.client.generated.RestSchema.SegmentMode,
                itinerary_version: kotlin.Long,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.ItinerarySegmentEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ItinerarySegmentEventObject {
                val given = AdminSchema.ItinerarySegmentEventObject.Builder().apply(optional)
                return AdminSchema.ItinerarySegmentEventObject(
                    id = id,
                    itinerary_id = itinerary_id,
                    name = name,
                    mode = mode,
                    itinerary_version = itinerary_version,
                    created_at = created_at,
                )
            }
        }
    }

    /**
     * A segment group as a webhook event carries it: its identifiers, its name and its
     * colour. Which segments it holds is not carried: `getItinerary` reads it.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class ItinerarySegmentGroupEventObject private constructor(
        /**
         * The kind of object this is, always `itinerary.segment_group`: the family of the
         * event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "itinerary.segment_group",
        /** Identifier of the segment group. */
        public val id: kotlin.String,
        /** Identifier of the itinerary the group belongs to. */
        public val itinerary_id: kotlin.String,
        /** The segment group's name. */
        public val name: kotlin.String,
        /** The group's colour, six lowercase hexadecimal digits after `#`. */
        public val color: kotlin.String,
        /**
         * The itinerary's `version` once this change was made, which every change to its
         * segments, segment groups and waypoints moves. Of two events about one itinerary,
         * the one carrying the higher figure is the later state.
         */
        public val itinerary_version: kotlin.Long,
        /** When the group was created. */
        public val created_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ItinerarySegmentGroupEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.itinerary_id == this.itinerary_id &&
                other.name == this.name &&
                other.color == this.color &&
                other.itinerary_version == this.itinerary_version &&
                other.created_at == this.created_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.itinerary_id.hashCode()
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + this.color.hashCode()
            hash = 31 * hash + this.itinerary_version.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ItinerarySegmentGroupEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", itinerary_id=" + this.itinerary_id +
                ", name=" + this.name +
                ", color=" + this.color +
                ", itinerary_version=" + this.itinerary_version +
                ", created_at=" + this.created_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ItinerarySegmentGroupEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.ItinerarySegmentGroupEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.ItinerarySegmentGroupEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                itinerary_id: kotlin.String,
                name: kotlin.String,
                color: kotlin.String,
                itinerary_version: kotlin.Long,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.ItinerarySegmentGroupEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ItinerarySegmentGroupEventObject {
                val given = AdminSchema.ItinerarySegmentGroupEventObject.Builder().apply(optional)
                return AdminSchema.ItinerarySegmentGroupEventObject(
                    id = id,
                    itinerary_id = itinerary_id,
                    name = name,
                    color = color,
                    itinerary_version = itinerary_version,
                    created_at = created_at,
                )
            }
        }
    }

    /**
     * An itinerary's share link as a webhook event carries it: the itinerary it opens and
     * when its token was minted. The token itself is never carried: `getItineraryShareLink`
     * reads it.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class ItineraryShareLinkEventObject private constructor(
        /**
         * The kind of object this is, always `itinerary.share_link`: the family of the event
         * types that carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "itinerary.share_link",
        /** Identifier of the itinerary the link opens. */
        public val itinerary_id: kotlin.String,
        /** When the link's token was minted: when the link was created, or last rotated. */
        public val created_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ItineraryShareLinkEventObject &&
                other.`object` == this.`object` &&
                other.itinerary_id == this.itinerary_id &&
                other.created_at == this.created_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.itinerary_id.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ItineraryShareLinkEventObject(" +
                "object=" + this.`object` +
                ", itinerary_id=" + this.itinerary_id +
                ", created_at=" + this.created_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ItineraryShareLinkEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.ItineraryShareLinkEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.ItineraryShareLinkEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                itinerary_id: kotlin.String,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.ItineraryShareLinkEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ItineraryShareLinkEventObject {
                val given = AdminSchema.ItineraryShareLinkEventObject.Builder().apply(optional)
                return AdminSchema.ItineraryShareLinkEventObject(
                    itinerary_id = itinerary_id,
                    created_at = created_at,
                )
            }
        }
    }

    /**
     * A waypoint as a webhook event carries it: its identifiers, its name and its mode. Its
     * location and content are never carried: `getItinerary` reads them.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class ItineraryWaypointEventObject private constructor(
        /**
         * The kind of object this is, always `itinerary.waypoint`: the family of the event
         * types that carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "itinerary.waypoint",
        /** Identifier of the waypoint. */
        public val id: kotlin.String,
        /** Identifier of the itinerary the waypoint belongs to. */
        public val itinerary_id: kotlin.String,
        /** The waypoint's name, null on one that carries none. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val name: kotlin.String? = null,
        /** What kind of waypoint it is. */
        public val mode: com.mountaya.client.generated.RestSchema.WaypointMode,
        /**
         * The itinerary's `version` once this change was made, which every change to its
         * segments, segment groups and waypoints moves. Of two events about one itinerary,
         * the one carrying the higher figure is the later state.
         */
        public val itinerary_version: kotlin.Long,
        /** When the waypoint was created. */
        public val created_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ItineraryWaypointEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.itinerary_id == this.itinerary_id &&
                other.name == this.name &&
                other.mode == this.mode &&
                other.itinerary_version == this.itinerary_version &&
                other.created_at == this.created_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.itinerary_id.hashCode()
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + this.mode.hashCode()
            hash = 31 * hash + this.itinerary_version.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ItineraryWaypointEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", itinerary_id=" + this.itinerary_id +
                ", name=" + this.name +
                ", mode=" + this.mode +
                ", itinerary_version=" + this.itinerary_version +
                ", created_at=" + this.created_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ItineraryWaypointEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.ItineraryWaypointEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.ItineraryWaypointEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                itinerary_id: kotlin.String,
                name: kotlin.String?,
                mode: com.mountaya.client.generated.RestSchema.WaypointMode,
                itinerary_version: kotlin.Long,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.ItineraryWaypointEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ItineraryWaypointEventObject {
                val given = AdminSchema.ItineraryWaypointEventObject.Builder().apply(optional)
                return AdminSchema.ItineraryWaypointEventObject(
                    id = id,
                    itinerary_id = itinerary_id,
                    name = name,
                    mode = mode,
                    itinerary_version = itinerary_version,
                    created_at = created_at,
                )
            }
        }
    }

    /**
     * A person's membership of one organization they have joined. It pairs the
     * person and the organization, so it has no identifier of its own and names
     * both: `user_id` and `organization_id`. It carries the organization's
     * identity with the role, so a client can name the organization and read the
     * person's role in it without a second read.
     *
     * It is not a trimmed `Organization`: this one is about a PERSON's
     * relationship to an organization. It carries the products the organization
     * holds, so a client can tell whether the organization holds Map Studio
     * without reading the organization itself, and what that person may use of
     * Map Studio there, in `studio_entitlements`, which Mountaya resolves from the
     * organization's agreement and the member's role, so a studio shows what the
     * agreement covers without deciding it itself. An invitation waiting for an
     * answer is not a membership: it is a `UserInvitation`.
     */
    @kotlinx.serialization.Serializable
    public class Membership private constructor(
        /** Identifier of the person, the user the session token names. */
        public val user_id: kotlin.String,
        /**
         * Identifier of the organization. It is what every organization-scoped
         * operation takes as its `organization_id` path parameter.
         */
        public val organization_id: kotlin.String,
        /** The organization's display name, as it appears to its members. */
        public val organization_name: kotlin.String,
        /**
         * The products the organization holds, as `Capability` members, each at
         * most once and sorted alphabetically. Empty when it holds none, as a new
         * organization does. It is what the organization holds, never what this
         * member may do with it: the role and `studio_entitlements` say that.
         */
        public val organization_capabilities: kotlin.collections.List<AdminSchema.Capability>,
        /**
         * What this person may use of Map Studio in this organization, as
         * `StudioEntitlement` members, each at most once and sorted
         * alphabetically. Mountaya resolves it from what the organization holds
         * and from the role, as `StudioEntitlement` describes: `embed`,
         * `publish`, `video-export` and `video-organization-modes` only where the
         * organization holds Map Publishing, and `white-label` only where it holds
         * White label. A change of agreement or of role reaches it on the
         * next read. Read with a publishable key, it is narrowed by the
         * organization's safety rules, as what the key is served is:
         * `profile-insights` only when `with_profile_analysis` is set, `map-pois`
         * only when `with_overlays` admits the points of interest, and
         * `map-overlays` only when it admits another overlay. It authorizes
         * nothing by itself, since every request is still refused by its own rule.
         */
        public val studio_entitlements: kotlin.collections.List<com.mountaya.client.generated.RestSchema.StudioEntitlement>,
        /** What this person may do in this organization. */
        public val role: com.mountaya.client.generated.RestSchema.OrganizationRole,
        /**
         * When the person joined: when the invitation was accepted, or when the
         * organization was created for its owner.
         */
        public val joined_at: kotlin.time.Instant,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.Membership &&
                other.user_id == this.user_id &&
                other.organization_id == this.organization_id &&
                other.organization_name == this.organization_name &&
                other.organization_capabilities == this.organization_capabilities &&
                other.studio_entitlements == this.studio_entitlements &&
                other.role == this.role &&
                other.joined_at == this.joined_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.user_id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.organization_name.hashCode()
            hash = 31 * hash + this.organization_capabilities.hashCode()
            hash = 31 * hash + this.studio_entitlements.hashCode()
            hash = 31 * hash + this.role.hashCode()
            hash = 31 * hash + this.joined_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "Membership(" +
                "user_id=" + this.user_id +
                ", organization_id=" + this.organization_id +
                ", organization_name=" + this.organization_name +
                ", organization_capabilities=" + this.organization_capabilities +
                ", studio_entitlements=" + this.studio_entitlements +
                ", role=" + this.role +
                ", joined_at=" + this.joined_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.Membership], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.Membership]. */
        public companion object {
            /** Returns a [AdminSchema.Membership] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                user_id: kotlin.String,
                organization_id: kotlin.String,
                organization_name: kotlin.String,
                organization_capabilities: kotlin.collections.List<AdminSchema.Capability>,
                studio_entitlements: kotlin.collections.List<com.mountaya.client.generated.RestSchema.StudioEntitlement>,
                role: com.mountaya.client.generated.RestSchema.OrganizationRole,
                joined_at: kotlin.time.Instant,
                optional: AdminSchema.Membership.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.Membership {
                val given = AdminSchema.Membership.Builder().apply(optional)
                return AdminSchema.Membership(
                    user_id = user_id,
                    organization_id = organization_id,
                    organization_name = organization_name,
                    organization_capabilities = organization_capabilities,
                    studio_entitlements = studio_entitlements,
                    role = role,
                    joined_at = joined_at,
                )
            }
        }
    }

    /**
     * The usage dimension a metered subscription line reports against. It names
     * what is counted, not what is allowed: a dimension appearing here says the
     * line is billed on recorded usage, and says nothing about any ceiling.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class MeterDimension(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Tile API requests. The Tile API is granted by an organization's agreement with Mountaya, so no subscription an operation in this contract opens carries a line on it, and only one Mountaya arranged separately does. */
            public val Tiles: MeterDimension = MeterDimension("tiles")

            /** Data API requests, the MCP server's included, which the Data API subscription bills by the month. */
            public val Data: MeterDimension = MeterDimension("data")
        }
    }

    /** Whether a member receives one notification email about an organization. */
    @kotlinx.serialization.Serializable
    public class NotificationPreference private constructor(
        /** The notification. */
        public val notification: AdminSchema.NotificationType,
        /** Whether the member is sent it by email. On by default. */
        public val email: kotlin.Boolean,
        /**
         * Whether the member may turn it off. A security notification is always
         * sent, and is locked.
         */
        public val locked: kotlin.Boolean,
        /**
         * Why it cannot be turned off, in the request's `Accept-Language`, null on
         * a notification that can.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val locked_reason: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.NotificationPreference &&
                other.notification == this.notification &&
                other.email == this.email &&
                other.locked == this.locked &&
                other.locked_reason == this.locked_reason

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.notification.hashCode()
            hash = 31 * hash + this.email.hashCode()
            hash = 31 * hash + this.locked.hashCode()
            hash = 31 * hash + (this.locked_reason?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "NotificationPreference(" +
                "notification=" + this.notification +
                ", email=" + this.email +
                ", locked=" + this.locked +
                ", locked_reason=" + this.locked_reason +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.NotificationPreference], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.NotificationPreference]. */
        public companion object {
            /** Returns a [AdminSchema.NotificationPreference] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                notification: AdminSchema.NotificationType,
                email: kotlin.Boolean,
                locked: kotlin.Boolean,
                locked_reason: kotlin.String?,
                optional: AdminSchema.NotificationPreference.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.NotificationPreference {
                val given = AdminSchema.NotificationPreference.Builder().apply(optional)
                return AdminSchema.NotificationPreference(
                    notification = notification,
                    email = email,
                    locked = locked,
                    locked_reason = locked_reason,
                )
            }
        }
    }

    /** Whether to send a member one notification email. */
    @kotlinx.serialization.Serializable
    public class NotificationPreferenceUpdate private constructor(
        /**
         * The notification. A locked one, a security notification, is a `400`
         * naming it, and so is one the member is not listed for, such as a
         * webhook endpoint's for a member who is neither owner nor admin.
         */
        public val notification: AdminSchema.NotificationType,
        /** Whether the member is sent it by email. */
        public val email: kotlin.Boolean,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.NotificationPreferenceUpdate &&
                other.notification == this.notification &&
                other.email == this.email

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.notification.hashCode()
            hash = 31 * hash + this.email.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "NotificationPreferenceUpdate(" +
                "notification=" + this.notification +
                ", email=" + this.email +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.NotificationPreferenceUpdate], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.NotificationPreferenceUpdate]. */
        public companion object {
            /** Returns a [AdminSchema.NotificationPreferenceUpdate] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                notification: AdminSchema.NotificationType,
                email: kotlin.Boolean,
                optional: AdminSchema.NotificationPreferenceUpdate.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.NotificationPreferenceUpdate {
                val given = AdminSchema.NotificationPreferenceUpdate.Builder().apply(optional)
                return AdminSchema.NotificationPreferenceUpdate(
                    notification = notification,
                    email = email,
                )
            }
        }
    }

    /**
     * Which notification emails about an organization a member receives. A
     * member is sent each one unless they turned it off, and a security
     * notification is always sent.
     */
    @kotlinx.serialization.Serializable
    public class NotificationPreferences private constructor(
        /** Identifier of the organization the notifications are about. */
        public val organization_id: kotlin.String,
        /** Identifier of the member, the user the session token names. */
        public val user_id: kotlin.String,
        /**
         * One entry per notification the member can receive in the organization:
         * the security notifications, `member-role-changed` and `member-removed`,
         * locked on; `collection-deleted`; and for an owner or an admin,
         * `webhook-endpoint-failing` and `webhook-endpoint-disabled`. The
         * `invitation` email goes to an address before it is a member's, and is
         * never listed.
         */
        public val notifications: kotlin.collections.List<AdminSchema.NotificationPreference>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.NotificationPreferences &&
                other.organization_id == this.organization_id &&
                other.user_id == this.user_id &&
                other.notifications == this.notifications

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.user_id.hashCode()
            hash = 31 * hash + this.notifications.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "NotificationPreferences(" +
                "organization_id=" + this.organization_id +
                ", user_id=" + this.user_id +
                ", notifications=" + this.notifications +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.NotificationPreferences], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.NotificationPreferences]. */
        public companion object {
            /** Returns a [AdminSchema.NotificationPreferences] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                organization_id: kotlin.String,
                user_id: kotlin.String,
                notifications: kotlin.collections.List<AdminSchema.NotificationPreference>,
                optional: AdminSchema.NotificationPreferences.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.NotificationPreferences {
                val given = AdminSchema.NotificationPreferences.Builder().apply(optional)
                return AdminSchema.NotificationPreferences(
                    organization_id = organization_id,
                    user_id = user_id,
                    notifications = notifications,
                )
            }
        }
    }

    /**
     * The notification emails to turn on or off. A notification left out keeps
     * its setting.
     */
    @kotlinx.serialization.Serializable
    public class NotificationPreferencesUpdateParams private constructor(
        /**
         * One entry per notification to set, at least one, each notification at
         * most once: naming one twice is a `400` naming the second.
         */
        public val notifications: kotlin.collections.List<AdminSchema.NotificationPreferenceUpdate>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.NotificationPreferencesUpdateParams &&
                other.notifications == this.notifications

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.notifications.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "NotificationPreferencesUpdateParams(" +
                "notifications=" + this.notifications +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.NotificationPreferencesUpdateParams], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.NotificationPreferencesUpdateParams]. */
        public companion object {
            /** Returns a [AdminSchema.NotificationPreferencesUpdateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                notifications: kotlin.collections.List<AdminSchema.NotificationPreferenceUpdate>,
                optional: AdminSchema.NotificationPreferencesUpdateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.NotificationPreferencesUpdateParams {
                val given = AdminSchema.NotificationPreferencesUpdateParams.Builder().apply(optional)
                return AdminSchema.NotificationPreferencesUpdateParams(
                    notifications = notifications,
                )
            }
        }
    }

    /**
     * A notification email Mountaya sends a person about an organization.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class NotificationType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Somebody invited the address to the organization. Sent to the address invited, always. */
            public val Invitation: NotificationType = NotificationType("invitation")

            /** Somebody else changed the member's role. A security notification: always sent, and never turned off. */
            public val MemberRoleChanged: NotificationType = NotificationType("member-role-changed")

            /** Somebody else removed the member from the organization. A security notification: always sent, and never turned off. */
            public val MemberRemoved: NotificationType = NotificationType("member-removed")

            /** Somebody else deleted one of the organization's collections. Sent to every member, unless they turned it off. */
            public val CollectionDeleted: NotificationType = NotificationType("collection-deleted")

            /** Every delivery to one of the organization's webhook endpoints has failed for 24 hours. Sent to owners and admins, unless they turned it off. */
            public val WebhookEndpointFailing: NotificationType = NotificationType("webhook-endpoint-failing")

            /** One of the organization's webhook endpoints was disabled. Sent to owners and admins, unless they turned it off. */
            public val WebhookEndpointDisabled: NotificationType = NotificationType("webhook-endpoint-disabled")
        }
    }

    /**
     * The setup intent to save a card from, after Stripe Elements has confirmed it
     * in the browser. It is checked against the organization before anything is
     * saved: one that is unknown or belongs to another organization is a `404`,
     * the two answered alike, and one of this organization that has not been
     * confirmed, or that carries no payment method, is a `409` naming
     * `setup_intent_id`. One that is still processing, as a SEPA Direct Debit
     * mandate is for a while, is accepted.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationBillingPaymentMethodParams private constructor(
        /**
         * Identifier of the confirmed setup intent, as returned by
         * `postOrganizationBillingPaymentMethodSetup` and confirmed by Stripe
         * Elements: `seti_` and at most 250 more letters, digits and underscores.
         */
        public val setup_intent_id: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationBillingPaymentMethodParams &&
                other.setup_intent_id == this.setup_intent_id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.setup_intent_id.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationBillingPaymentMethodParams(" +
                "setup_intent_id=" + this.setup_intent_id +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationBillingPaymentMethodParams], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationBillingPaymentMethodParams]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationBillingPaymentMethodParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                setup_intent_id: kotlin.String,
                optional: AdminSchema.OrganizationBillingPaymentMethodParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationBillingPaymentMethodParams {
                val given = AdminSchema.OrganizationBillingPaymentMethodParams.Builder().apply(optional)
                return AdminSchema.OrganizationBillingPaymentMethodParams(
                    setup_intent_id = setup_intent_id,
                )
            }
        }
    }

    /**
     * How to open an organization's billing. Provisioning opens the billing
     * account and no subscription, and charges nothing. The metered Data API
     * subscription is opened by the first card the organization saves, unless the
     * customer, or that card, still owes an invoice, which is the `402` and opens
     * nothing, and is
     * invoiced at the end of each billing period, a month counted from the day it
     * opened, for the requests made in it, so there is
     * no interval to choose and the one thing a caller decides here is the
     * promotion code.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationBillingProvisionParams private constructor(
        /**
         * A promotion code for the organization's first Data API subscription, as
         * it was handed to the customer. It is checked against this organization,
         * so a code restricted to another customer is refused with a `400`, and
         * so is any code given to an organization that has already held a Data
         * API subscription, which no code applies to. An accepted one is recorded,
         * and applied when the first card opens the subscription. Omitted, none is
         * recorded, and one recorded by an earlier call is cleared. Letters,
         * digits, hyphens and underscores, at most 64, the form a code takes
         * wherever it is read.
         */
        public val promotion_code: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationBillingProvisionParams &&
                other.promotion_code == this.promotion_code

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.promotion_code?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationBillingProvisionParams(" +
                "promotion_code=" + this.promotion_code +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationBillingProvisionParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null

            /**
             * A promotion code for the organization's first Data API subscription, as
             * it was handed to the customer. It is checked against this organization,
             * so a code restricted to another customer is refused with a `400`, and
             * so is any code given to an organization that has already held a Data
             * API subscription, which no code applies to. An accepted one is recorded,
             * and applied when the first card opens the subscription. Omitted, none is
             * recorded, and one recorded by an earlier call is cleared. Letters,
             * digits, hyphens and underscores, at most 64, the form a code takes
             * wherever it is read.
             */
            public var promotion_code: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationBillingProvisionParams]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationBillingProvisionParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.OrganizationBillingProvisionParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationBillingProvisionParams {
                val given = AdminSchema.OrganizationBillingProvisionParams.Builder().apply(optional)
                return AdminSchema.OrganizationBillingProvisionParams(
                    promotion_code = given.slot0,
                )
            }
        }
    }

    /**
     * A new organization. The name is required. The website is optional and does
     * one thing: its host seeds the embed domain allowlist, so a new
     * organization's keys work from its own site from the start. Omit it and the
     * allowlist starts empty: no page but Mountaya's own can call the API with the
     * organization's keys from a browser, and no site but Mountaya's own can frame
     * its hosted embed, until it is set: by `patchOrganizationSettings` on a
     * self-serve organization, and by Mountaya, as the agreement states, once an
     * agreement covers it.
     * `agreement_requested` says which way
     * the organization is meant to be covered, by an agreement with Mountaya or by
     * the card it saves for the Data API.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationCreateParams private constructor(
        /**
         * Display name of the organization: 2 to 30 characters as sent, and still
         * at least 2 once leading and trailing whitespace is trimmed, which is the
         * form it is stored in. A name is one line: a tab, a line break or a line
         * separator between two of its characters is a `400`, and one at either end
         * is trimmed with the whitespace there.
         */
        public val name: kotlin.String,
        /**
         * The organization's website as a full URL, scheme included. Its host
         * seeds the embed domain allowlist, which is why the scheme is required
         * rather than merely conventional: a bare host, or a scheme that is not
         * http or https, would land in the allowlist and silently deny every
         * embed. An empty string is accepted and seeds nothing. A host longer than
         * the 253 characters a domain name may run to is a `400` at `website`, the
         * limit every entry of the allowlist is held to.
         *
         * Set once, at creation. `patchOrganization` takes the name alone, so there is
         * no operation in this contract that changes the website afterwards.
         *
         * The ceiling is the conventional URL one rather than the 500 a waypoint's
         * link carries. A waypoint link is one of many on a place a person
         * authored; this is one address per organization.
         */
        public val website: @kotlinx.serialization.Serializable(with = com.mountaya.client.SentUrlSerializer::class) com.mountaya.client.Url? = null,
        /**
         * Whether the organization is meant to be covered by an agreement with
         * Mountaya rather than by a card. True records that choice, which
         * `awaiting_agreement` on the organization then reports until an agreement
         * is set, and the organization holds no product until that agreement
         * grants the ones it names. It grants nothing itself and opens nothing.
         * Omitted or false, the organization is self-serve: it holds no product
         * until its first card opens the metered Data API subscription.
         *
         * Left out, the server applies its default: `false`.
         */
        public val agreement_requested: kotlin.Boolean? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationCreateParams &&
                other.name == this.name &&
                other.website == this.website &&
                other.agreement_requested == this.agreement_requested

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + (this.website?.hashCode() ?: 0)
            hash = 31 * hash + (this.agreement_requested?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationCreateParams(" +
                "name=" + this.name +
                ", website=" + this.website +
                ", agreement_requested=" + this.agreement_requested +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationCreateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: com.mountaya.client.Url? = null
            internal var slot2: kotlin.Boolean? = null

            /**
             * The organization's website as a full URL, scheme included. Its host
             * seeds the embed domain allowlist, which is why the scheme is required
             * rather than merely conventional: a bare host, or a scheme that is not
             * http or https, would land in the allowlist and silently deny every
             * embed. An empty string is accepted and seeds nothing. A host longer than
             * the 253 characters a domain name may run to is a `400` at `website`, the
             * limit every entry of the allowlist is held to.
             *
             * Set once, at creation. `patchOrganization` takes the name alone, so there is
             * no operation in this contract that changes the website afterwards.
             *
             * The ceiling is the conventional URL one rather than the 500 a waypoint's
             * link carries. A waypoint link is one of many on a place a person
             * authored; this is one address per organization.
             */
            public var website: com.mountaya.client.Url
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Whether the organization is meant to be covered by an agreement with
             * Mountaya rather than by a card. True records that choice, which
             * `awaiting_agreement` on the organization then reports until an agreement
             * is set, and the organization holds no product until that agreement
             * grants the ones it names. It grants nothing itself and opens nothing.
             * Omitted or false, the organization is self-serve: it holds no product
             * until its first card opens the metered Data API subscription.
             *
             * Left out, the server applies its default: `false`.
             */
            public var agreement_requested: kotlin.Boolean
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationCreateParams]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationCreateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                name: kotlin.String,
                optional: AdminSchema.OrganizationCreateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationCreateParams {
                val given = AdminSchema.OrganizationCreateParams.Builder().apply(optional)
                return AdminSchema.OrganizationCreateParams(
                    name = name,
                    website = given.slot1,
                    agreement_requested = given.slot2,
                )
            }
        }
    }

    /**
     * What an organization holds, as a webhook event carries it: its capabilities and the
     * studio features its agreement grants.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationEntitlementsEventObject private constructor(
        /**
         * The kind of object this is, always `organization.entitlements`: the family of the
         * event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.entitlements",
        /** Identifier of the organization. */
        public val organization_id: kotlin.String,
        /** The capabilities the organization holds once this change was made. */
        public val capabilities: kotlin.collections.List<AdminSchema.Capability>,
        /** The studio features the organization's agreement grants once this change was made. */
        public val studio_entitlements: kotlin.collections.List<com.mountaya.client.generated.RestSchema.StudioEntitlement>,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationEntitlementsEventObject &&
                other.`object` == this.`object` &&
                other.organization_id == this.organization_id &&
                other.capabilities == this.capabilities &&
                other.studio_entitlements == this.studio_entitlements

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.capabilities.hashCode()
            hash = 31 * hash + this.studio_entitlements.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationEntitlementsEventObject(" +
                "object=" + this.`object` +
                ", organization_id=" + this.organization_id +
                ", capabilities=" + this.capabilities +
                ", studio_entitlements=" + this.studio_entitlements +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationEntitlementsEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationEntitlementsEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationEntitlementsEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                organization_id: kotlin.String,
                capabilities: kotlin.collections.List<AdminSchema.Capability>,
                studio_entitlements: kotlin.collections.List<com.mountaya.client.generated.RestSchema.StudioEntitlement>,
                optional: AdminSchema.OrganizationEntitlementsEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationEntitlementsEventObject {
                val given = AdminSchema.OrganizationEntitlementsEventObject.Builder().apply(optional)
                return AdminSchema.OrganizationEntitlementsEventObject(
                    organization_id = organization_id,
                    capabilities = capabilities,
                    studio_entitlements = studio_entitlements,
                )
            }
        }
    }

    /**
     * An organization as a webhook event carries it: its identifier, its name and its
     * version. `getOrganization` reads the rest.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationEventObject private constructor(
        /**
         * The kind of object this is, always `organization`: the family of the event types
         * that carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization",
        /** Identifier of the organization. */
        public val id: kotlin.String,
        /** The organization's name. */
        public val name: kotlin.String,
        /**
         * The organization's version once this change was made. It climbs with every change
         * a read of the organization shows, so of two events about one organization the one
         * carrying the higher `version` is the later state: events are not delivered in
         * order.
         */
        public val version: kotlin.Long,
        /** When the organization was created. */
        public val created_at: kotlin.time.Instant,
        /** When the organization's own fields last changed. */
        public val updated_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.name == this.name &&
                other.version == this.version &&
                other.created_at == this.created_at &&
                other.updated_at == this.updated_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + this.version.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.updated_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", name=" + this.name +
                ", version=" + this.version +
                ", created_at=" + this.created_at +
                ", updated_at=" + this.updated_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                name: kotlin.String,
                version: kotlin.Long,
                created_at: kotlin.time.Instant,
                updated_at: kotlin.time.Instant,
                optional: AdminSchema.OrganizationEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationEventObject {
                val given = AdminSchema.OrganizationEventObject.Builder().apply(optional)
                return AdminSchema.OrganizationEventObject(
                    id = id,
                    name = name,
                    version = version,
                    created_at = created_at,
                    updated_at = updated_at,
                )
            }
        }
    }

    /**
     * An invitation to join an organization, held by an email address until the
     * person it names accepts or declines it, somebody withdraws it, or it
     * expires. It holds a seat from the moment it is made until then.
     *
     * It names the address and never an account: whether an account carries the
     * address is not part of any answer, so an invitation reads the same whether
     * or not the person has signed up. The person finds it, once their account
     * has verified the address, through `getMyInvitations`.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationInvitation private constructor(
        /**
         * Identifier of the invitation. It is what `getOrganizationInvitation` and
         * `deleteOrganizationInvitation` take, and what the invited person accepts
         * or declines under `/v1/me/invitations`.
         */
        public val id: kotlin.String,
        /** The address invited, as stored: trimmed and lowercased. */
        public val email: kotlin.String,
        /**
         * The role the person holds once they accept. `owner` is not among the
         * values: it is never granted by invitation.
         */
        public val role: AdminSchema.AssignableOrganizationRole,
        /** The member who made the invitation. */
        public val invited_by: com.mountaya.client.generated.RestSchema.RelationToUser,
        /** When the invitation was made. */
        public val created_at: kotlin.time.Instant,
        /**
         * When the invitation lapses unanswered, thirty days after it was made.
         * An expired invitation frees its seat and is listed nowhere, and the
         * address may be invited again.
         */
        public val expires_at: kotlin.time.Instant,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationInvitation &&
                other.id == this.id &&
                other.email == this.email &&
                other.role == this.role &&
                other.invited_by == this.invited_by &&
                other.created_at == this.created_at &&
                other.expires_at == this.expires_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.email.hashCode()
            hash = 31 * hash + this.role.hashCode()
            hash = 31 * hash + this.invited_by.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.expires_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationInvitation(" +
                "id=" + this.id +
                ", email=" + this.email +
                ", role=" + this.role +
                ", invited_by=" + this.invited_by +
                ", created_at=" + this.created_at +
                ", expires_at=" + this.expires_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationInvitation], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationInvitation]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationInvitation] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                email: kotlin.String,
                role: AdminSchema.AssignableOrganizationRole,
                invited_by: com.mountaya.client.generated.RestSchema.RelationToUser,
                created_at: kotlin.time.Instant,
                expires_at: kotlin.time.Instant,
                optional: AdminSchema.OrganizationInvitation.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationInvitation {
                val given = AdminSchema.OrganizationInvitation.Builder().apply(optional)
                return AdminSchema.OrganizationInvitation(
                    id = id,
                    email = email,
                    role = role,
                    invited_by = invited_by,
                    created_at = created_at,
                    expires_at = expires_at,
                )
            }
        }
    }

    /**
     * One invitation: the address invited and the role it grants on acceptance.
     * One request invites one address; a client inviting several sends one request
     * for each.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationInvitationCreateParams private constructor(
        /**
         * The address to invite, any well-formed address, stored trimmed and
         * lowercased. It need not belong to an account: the invitation waits for
         * the person to sign up and verify it. Nothing is sent to it by this
         * operation. Whether an account carries it never changes the answer.
         */
        public val email: kotlin.String,
        /**
         * The role the person holds once they accept. `owner` is not among the
         * values: it is never granted by invitation. Only the owner invites as
         * `admin`, signed in to Mountaya's web application: under a key, `admin`
         * is a `403` whoever asks.
         */
        public val role: AdminSchema.AssignableOrganizationRole,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationInvitationCreateParams &&
                other.email == this.email &&
                other.role == this.role

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.email.hashCode()
            hash = 31 * hash + this.role.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationInvitationCreateParams(" +
                "email=" + this.email +
                ", role=" + this.role +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationInvitationCreateParams], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationInvitationCreateParams]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationInvitationCreateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                email: kotlin.String,
                role: AdminSchema.AssignableOrganizationRole,
                optional: AdminSchema.OrganizationInvitationCreateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationInvitationCreateParams {
                val given = AdminSchema.OrganizationInvitationCreateParams.Builder().apply(optional)
                return AdminSchema.OrganizationInvitationCreateParams(
                    email = email,
                    role = role,
                )
            }
        }
    }

    /**
     * An invitation as a webhook event carries it: the address it names, the role it grants,
     * who made it and when it lapses. It is the invitation as it stood when the change was
     * made, so the one an `organization.invitation.accepted` carries is consumed by then.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationInvitationEventObject private constructor(
        /**
         * The kind of object this is, always `organization.invitation`: the family of the
         * event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.invitation",
        /** Identifier of the invitation. */
        public val id: kotlin.String,
        /** The address invited, trimmed and lowercased. */
        public val email: kotlin.String,
        /** The role the person holds once they accept. */
        public val role: AdminSchema.AssignableOrganizationRole,
        /** The member who made the invitation. */
        public val invited_by: com.mountaya.client.generated.RestSchema.RelationToUser,
        /** When the invitation was made. */
        public val created_at: kotlin.time.Instant,
        /** When the invitation lapses unanswered, thirty days after it was made. */
        public val expires_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationInvitationEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.email == this.email &&
                other.role == this.role &&
                other.invited_by == this.invited_by &&
                other.created_at == this.created_at &&
                other.expires_at == this.expires_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.email.hashCode()
            hash = 31 * hash + this.role.hashCode()
            hash = 31 * hash + this.invited_by.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.expires_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationInvitationEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", email=" + this.email +
                ", role=" + this.role +
                ", invited_by=" + this.invited_by +
                ", created_at=" + this.created_at +
                ", expires_at=" + this.expires_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationInvitationEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationInvitationEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationInvitationEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                email: kotlin.String,
                role: AdminSchema.AssignableOrganizationRole,
                invited_by: com.mountaya.client.generated.RestSchema.RelationToUser,
                created_at: kotlin.time.Instant,
                expires_at: kotlin.time.Instant,
                optional: AdminSchema.OrganizationInvitationEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationInvitationEventObject {
                val given = AdminSchema.OrganizationInvitationEventObject.Builder().apply(optional)
                return AdminSchema.OrganizationInvitationEventObject(
                    id = id,
                    email = email,
                    role = role,
                    invited_by = invited_by,
                    created_at = created_at,
                    expires_at = expires_at,
                )
            }
        }
    }

    /**
     * Somebody who has joined an organization, with the role they hold in it. An
     * invitation that has not been accepted is not a member: it is an
     * `OrganizationInvitation`, listed by `getOrganizationInvitations`.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationMember private constructor(
        /**
         * Identifier of the member's account. It is what names them on
         * `patchOrganizationMember` and `deleteOrganizationMember`, and the
         * `user_id` a session token is minted for when a page acts as them.
         */
        public val user_id: kotlin.String,
        /**
         * The member's account address. Absent for a member whose account cannot
         * be resolved at that moment, who is listed without it.
         */
        public val email: kotlin.String? = null,
        /**
         * The member's display name, present when the member has set one and their
         * account can be resolved. Unbounded, unlike the name of a resource this
         * API mints: it belongs to the person's account, and no operation here
         * writes it.
         */
        public val name: kotlin.String? = null,
        /** What the member may do in this organization. */
        public val role: com.mountaya.client.generated.RestSchema.OrganizationRole,
        /**
         * When the member joined: when the invitation was accepted, or when the
         * organization was created for its owner.
         */
        public val joined_at: kotlin.time.Instant,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationMember &&
                other.user_id == this.user_id &&
                other.email == this.email &&
                other.name == this.name &&
                other.role == this.role &&
                other.joined_at == this.joined_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.user_id.hashCode()
            hash = 31 * hash + (this.email?.hashCode() ?: 0)
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + this.role.hashCode()
            hash = 31 * hash + this.joined_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationMember(" +
                "user_id=" + this.user_id +
                ", email=" + this.email +
                ", name=" + this.name +
                ", role=" + this.role +
                ", joined_at=" + this.joined_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationMember], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null

            /**
             * The member's account address. Absent for a member whose account cannot
             * be resolved at that moment, who is listed without it.
             */
            public var email: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * The member's display name, present when the member has set one and their
             * account can be resolved. Unbounded, unlike the name of a resource this
             * API mints: it belongs to the person's account, and no operation here
             * writes it.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationMember]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationMember] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                user_id: kotlin.String,
                role: com.mountaya.client.generated.RestSchema.OrganizationRole,
                joined_at: kotlin.time.Instant,
                optional: AdminSchema.OrganizationMember.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationMember {
                val given = AdminSchema.OrganizationMember.Builder().apply(optional)
                return AdminSchema.OrganizationMember(
                    user_id = user_id,
                    email = given.slot1,
                    name = given.slot2,
                    role = role,
                    joined_at = joined_at,
                )
            }
        }
    }

    /**
     * Why a membership ended.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class OrganizationMemberDeletionReason(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Somebody else removed the member. */
            public val Removed: OrganizationMemberDeletionReason = OrganizationMemberDeletionReason("removed")

            /** The member left. */
            public val Left: OrganizationMemberDeletionReason = OrganizationMemberDeletionReason("left")
        }
    }

    /**
     * A membership as a webhook event carries it: the member's account and the role they
     * hold. The member's address and name are not carried: `getOrganizationMembers` lists
     * them while they are a member.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationMemberEventObject private constructor(
        /**
         * The kind of object this is, always `organization.member`: the family of the event
         * types that carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.member",
        /**
         * Identifier of the member's account, the `user_id` that names them on
         * `patchOrganizationMember` and `deleteOrganizationMember`.
         */
        public val user_id: kotlin.String,
        /** The role the member holds once this change was made, or held when they left. */
        public val role: com.mountaya.client.generated.RestSchema.OrganizationRole,
        /** When the member joined the organization. */
        public val joined_at: kotlin.time.Instant,
        /** Why the membership ended. Present on `organization.member.deleted` alone. */
        public val reason: AdminSchema.OrganizationMemberDeletionReason? = null,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationMemberEventObject &&
                other.`object` == this.`object` &&
                other.user_id == this.user_id &&
                other.role == this.role &&
                other.joined_at == this.joined_at &&
                other.reason == this.reason

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.user_id.hashCode()
            hash = 31 * hash + this.role.hashCode()
            hash = 31 * hash + this.joined_at.hashCode()
            hash = 31 * hash + (this.reason?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationMemberEventObject(" +
                "object=" + this.`object` +
                ", user_id=" + this.user_id +
                ", role=" + this.role +
                ", joined_at=" + this.joined_at +
                ", reason=" + this.reason +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationMemberEventObject], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot4: AdminSchema.OrganizationMemberDeletionReason? = null

            /** Why the membership ended. Present on `organization.member.deleted` alone. */
            public var reason: AdminSchema.OrganizationMemberDeletionReason
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationMemberEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationMemberEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                user_id: kotlin.String,
                role: com.mountaya.client.generated.RestSchema.OrganizationRole,
                joined_at: kotlin.time.Instant,
                optional: AdminSchema.OrganizationMemberEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationMemberEventObject {
                val given = AdminSchema.OrganizationMemberEventObject.Builder().apply(optional)
                return AdminSchema.OrganizationMemberEventObject(
                    user_id = user_id,
                    role = role,
                    joined_at = joined_at,
                    reason = given.slot4,
                )
            }
        }
    }

    /**
     * A change to one member's role. The role is the whole of what a membership
     * carries that a caller can change. `owner` cannot be granted here and the
     * owner's own role cannot be changed: ownership moves only in Mountaya's web
     * application. Only the owner grants `admin` or takes it away, and only
     * there: under a key, granting `admin` or changing the role of a member who
     * holds it is a `403`, the owner's own session included.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationMemberUpdateParams private constructor(
        /**
         * The role the member holds after the change. `owner` is not among the
         * values: it is never granted here.
         */
        public val role: AdminSchema.AssignableOrganizationRole,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationMemberUpdateParams &&
                other.role == this.role

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.role.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationMemberUpdateParams(" +
                "role=" + this.role +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationMemberUpdateParams], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationMemberUpdateParams]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationMemberUpdateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                role: AdminSchema.AssignableOrganizationRole,
                optional: AdminSchema.OrganizationMemberUpdateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationMemberUpdateParams {
                val given = AdminSchema.OrganizationMemberUpdateParams.Builder().apply(optional)
                return AdminSchema.OrganizationMemberUpdateParams(
                    role = role,
                )
            }
        }
    }

    /**
     * An organization's Map Publishing settings as a webhook event carries them: whose they
     * are, and their version. `getOrganizationSettings` reads the settings themselves.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationSettingsEventObject private constructor(
        /**
         * The kind of object this is, always `organization.settings`: the family of the
         * event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.settings",
        /** Identifier of the organization the settings belong to. */
        public val organization_id: kotlin.String,
        /**
         * The settings's version once this change was made. It climbs with every change a
         * read of the settings shows, so of two events about one settings the one carrying
         * the higher `version` is the later state: events are not delivered in order.
         */
        public val version: kotlin.Long,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationSettingsEventObject &&
                other.`object` == this.`object` &&
                other.organization_id == this.organization_id &&
                other.version == this.version

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.version.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationSettingsEventObject(" +
                "object=" + this.`object` +
                ", organization_id=" + this.organization_id +
                ", version=" + this.version +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationSettingsEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationSettingsEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationSettingsEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                organization_id: kotlin.String,
                version: kotlin.Long,
                optional: AdminSchema.OrganizationSettingsEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationSettingsEventObject {
                val given = AdminSchema.OrganizationSettingsEventObject.Builder().apply(optional)
                return AdminSchema.OrganizationSettingsEventObject(
                    organization_id = organization_id,
                    version = version,
                )
            }
        }
    }

    /**
     * A partial update to the settings that shape an organization's embeds:
     * the default props, the map theme, and the safety rules. Every group is
     * optional: only the groups present in the request are replaced; the rest are
     * preserved. The organization's limits, its per-minute rate limits and monthly
     * allowances among them, are deliberately absent: they are set by its
     * agreement rather than by a caller, so no operation in this contract writes
     * them.
     *
     * Three safety rules are set by Mountaya rather than by the caller:
     * `safety_rules.with_overlays` and `safety_rules.with_overlays_control` on
     * every organization, and `safety_rules.domains` on an organization covered
     * by an agreement. A `safety_rules` group carries them as every group carries
     * all its fields, and must carry them as they stand: a request that changes
     * any of them is a `403` naming that field, and nothing in the request is
     * applied.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationSettingsPatchParams private constructor(
        /**
         * What an embed starts with when it names nothing itself. Sent whole: the
         * object REPLACES the stored one rather than merging into it, and every
         * field in it is required, so a group that leaves one out is a 400 rather
         * than a partial change.
         */
        public val default_props: com.mountaya.client.generated.RestSchema.EmbeddedStudioDefaultProps? = null,
        /**
         * The organization's colors and typography for an embed. Sent whole: the
         * object REPLACES the stored one rather than merging into it, and every
         * field in it is required.
         */
        public val theme: com.mountaya.client.generated.RestSchema.EmbeddedStudioTheme? = null,
        /**
         * The ceilings an embed cannot raise. Sent whole: the object REPLACES the
         * stored one rather than merging into it, and every rule in it is required,
         * `require_session_token` included, so a group that leaves one out is a 400
         * rather than a change that drops it. `with_overlays`,
         * `with_overlays_control`, and `domains` on an organization covered by an
         * agreement, are sent as they stand, since Mountaya sets them: changing any
         * of them is a `403`.
         */
        public val safety_rules: com.mountaya.client.generated.RestSchema.EmbeddedStudioSafetyRules? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationSettingsPatchParams &&
                other.default_props == this.default_props &&
                other.theme == this.theme &&
                other.safety_rules == this.safety_rules

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.default_props?.hashCode() ?: 0)
            hash = 31 * hash + (this.theme?.hashCode() ?: 0)
            hash = 31 * hash + (this.safety_rules?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationSettingsPatchParams(" +
                "default_props=" + this.default_props +
                ", theme=" + this.theme +
                ", safety_rules=" + this.safety_rules +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationSettingsPatchParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.generated.RestSchema.EmbeddedStudioDefaultProps? = null
            internal var slot1: com.mountaya.client.generated.RestSchema.EmbeddedStudioTheme? = null
            internal var slot2: com.mountaya.client.generated.RestSchema.EmbeddedStudioSafetyRules? = null

            /**
             * What an embed starts with when it names nothing itself. Sent whole: the
             * object REPLACES the stored one rather than merging into it, and every
             * field in it is required, so a group that leaves one out is a 400 rather
             * than a partial change.
             */
            public var default_props: com.mountaya.client.generated.RestSchema.EmbeddedStudioDefaultProps
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The organization's colors and typography for an embed. Sent whole: the
             * object REPLACES the stored one rather than merging into it, and every
             * field in it is required.
             */
            public var theme: com.mountaya.client.generated.RestSchema.EmbeddedStudioTheme
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * The ceilings an embed cannot raise. Sent whole: the object REPLACES the
             * stored one rather than merging into it, and every rule in it is required,
             * `require_session_token` included, so a group that leaves one out is a 400
             * rather than a change that drops it. `with_overlays`,
             * `with_overlays_control`, and `domains` on an organization covered by an
             * agreement, are sent as they stand, since Mountaya sets them: changing any
             * of them is a `403`.
             */
            public var safety_rules: com.mountaya.client.generated.RestSchema.EmbeddedStudioSafetyRules
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationSettingsPatchParams]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationSettingsPatchParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.OrganizationSettingsPatchParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationSettingsPatchParams {
                val given = AdminSchema.OrganizationSettingsPatchParams.Builder().apply(optional)
                return AdminSchema.OrganizationSettingsPatchParams(
                    default_props = given.slot0,
                    theme = given.slot1,
                    safety_rules = given.slot2,
                )
            }
        }
    }

    /**
     * One of an organization's subscriptions, as a client reads it: where it
     * stands, how it is paid for, what it bills, and when the current period ends.
     * It is a read: no operation writes it directly. It changes as a consequence
     * of the billing operations, for example when a card is saved.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationSubscription private constructor(
        /**
         * Where the subscription stands. Read it with `paused` to know whether the
         * capabilities it grants are in force: a subscription can read `active`
         * while `paused` is true, and nothing it grants is in force then.
         */
        public val status: AdminSchema.SubscriptionStatus,
        /**
         * How the subscription is paid for, which is what says whether adding a
         * card changes anything about it.
         */
        public val collection_method: AdminSchema.SubscriptionCollectionMethod,
        /**
         * The lowercase ISO 4217 code the subscription bills in, for example
         * `eur`.
         */
        public val currency: kotlin.String,
        /** What the subscription bills, one entry per line. */
        public val items: kotlin.collections.List<AdminSchema.OrganizationSubscriptionItem>,
        /**
         * When the current billing period closes. The Data API subscription bills
         * by the month, so this is the end of the current month of usage, which
         * is when the requests made in it are invoiced. While
         * `cancel_at_period_end` is true, it is also when the subscription ends.
         */
        public val current_period_end_at: kotlin.time.Instant,
        /**
         * Whether the subscription ends at `current_period_end_at`: its end was
         * scheduled by `deleteOrganizationBillingSubscription`, or by the
         * organization's agreement with Mountaya taking over its billing. It is
         * still in force until then, so the capabilities it grants are unaffected
         * while this is true, and the period is invoiced as usual when it closes.
         * `postOrganizationBillingSubscription` takes the end back before then.
         */
        public val cancel_at_period_end: kotlin.Boolean,
        /**
         * Whether billing is suspended. A paused subscription invoices nothing and
         * suspends the capabilities it grants exactly as a canceled one does; the
         * flag is here so a client can say that it was paused rather than
         * canceled, which is a different thing to ask the payer to fix.
         */
        public val paused: kotlin.Boolean,
        /**
         * The discount on this subscription: on the Data API subscription, the one
         * the promotion code given to `postOrganizationBillingProvision` put on it
         * when the organization's first card opened it. Absent when it carries none.
         */
        public val discount: AdminSchema.SubscriptionDiscount? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationSubscription &&
                other.status == this.status &&
                other.collection_method == this.collection_method &&
                other.currency == this.currency &&
                other.items == this.items &&
                other.current_period_end_at == this.current_period_end_at &&
                other.cancel_at_period_end == this.cancel_at_period_end &&
                other.paused == this.paused &&
                other.discount == this.discount

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + this.collection_method.hashCode()
            hash = 31 * hash + this.currency.hashCode()
            hash = 31 * hash + this.items.hashCode()
            hash = 31 * hash + this.current_period_end_at.hashCode()
            hash = 31 * hash + this.cancel_at_period_end.hashCode()
            hash = 31 * hash + this.paused.hashCode()
            hash = 31 * hash + (this.discount?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationSubscription(" +
                "status=" + this.status +
                ", collection_method=" + this.collection_method +
                ", currency=" + this.currency +
                ", items=" + this.items +
                ", current_period_end_at=" + this.current_period_end_at +
                ", cancel_at_period_end=" + this.cancel_at_period_end +
                ", paused=" + this.paused +
                ", discount=" + this.discount +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationSubscription], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot7: AdminSchema.SubscriptionDiscount? = null

            /**
             * The discount on this subscription: on the Data API subscription, the one
             * the promotion code given to `postOrganizationBillingProvision` put on it
             * when the organization's first card opened it. Absent when it carries none.
             */
            public var discount: AdminSchema.SubscriptionDiscount
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationSubscription]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationSubscription] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                status: AdminSchema.SubscriptionStatus,
                collection_method: AdminSchema.SubscriptionCollectionMethod,
                currency: kotlin.String,
                items: kotlin.collections.List<AdminSchema.OrganizationSubscriptionItem>,
                current_period_end_at: kotlin.time.Instant,
                cancel_at_period_end: kotlin.Boolean,
                paused: kotlin.Boolean,
                optional: AdminSchema.OrganizationSubscription.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationSubscription {
                val given = AdminSchema.OrganizationSubscription.Builder().apply(optional)
                return AdminSchema.OrganizationSubscription(
                    status = status,
                    collection_method = collection_method,
                    currency = currency,
                    items = items,
                    current_period_end_at = current_period_end_at,
                    cancel_at_period_end = cancel_at_period_end,
                    paused = paused,
                    discount = given.slot7,
                )
            }
        }
    }

    /**
     * An organization's Data API subscription as a webhook event carries it: its
     * identifier, its status, whether it is paused, and when its period ends.
     * `getOrganizationBillingSubscription` reads its items and discount.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationSubscriptionEventObject private constructor(
        /**
         * The kind of object this is, always `organization.billing.subscription`: the family
         * of the event types that carry it, and what tells the members of
         * `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.billing.subscription",
        /**
         * Identifier of the subscription, the payment provider's `sub_` identifier. It is
         * not a UUID. An organization holds one Data API subscription at a time, and one
         * opened after another ended carries another identifier: the events of one
         * subscription share it.
         */
        public val id: kotlin.String,
        /** Identifier of the organization the subscription belongs to. */
        public val organization_id: kotlin.String,
        /** The subscription's status once this change was made. */
        public val status: AdminSchema.SubscriptionStatus,
        /** Whether collection on the subscription is paused. */
        public val paused: kotlin.Boolean,
        /** Whether the subscription ends when its current period does. */
        public val cancel_at_period_end: kotlin.Boolean,
        /** When the subscription's current period ends. */
        public val current_period_end_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationSubscriptionEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.status == this.status &&
                other.paused == this.paused &&
                other.cancel_at_period_end == this.cancel_at_period_end &&
                other.current_period_end_at == this.current_period_end_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + this.paused.hashCode()
            hash = 31 * hash + this.cancel_at_period_end.hashCode()
            hash = 31 * hash + this.current_period_end_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationSubscriptionEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", status=" + this.status +
                ", paused=" + this.paused +
                ", cancel_at_period_end=" + this.cancel_at_period_end +
                ", current_period_end_at=" + this.current_period_end_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationSubscriptionEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.OrganizationSubscriptionEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationSubscriptionEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                status: AdminSchema.SubscriptionStatus,
                paused: kotlin.Boolean,
                cancel_at_period_end: kotlin.Boolean,
                current_period_end_at: kotlin.time.Instant,
                optional: AdminSchema.OrganizationSubscriptionEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationSubscriptionEventObject {
                val given = AdminSchema.OrganizationSubscriptionEventObject.Builder().apply(optional)
                return AdminSchema.OrganizationSubscriptionEventObject(
                    id = id,
                    organization_id = organization_id,
                    status = status,
                    paused = paused,
                    cancel_at_period_end = cancel_at_period_end,
                    current_period_end_at = current_period_end_at,
                )
            }
        }
    }

    /**
     * One line of a subscription: what is being billed, how much of it, and the
     * usage dimension it reports against when it is metered. No line states how
     * often it bills, because there is nothing to choose: the Data API line is
     * metered and billed by the month.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationSubscriptionItem private constructor(
        /** Identifier of the price this line bills at. */
        public val price_id: kotlin.String,
        /**
         * How many units the line bills. On a metered line, which the Data API
         * line is, it says nothing about what was used, which is read from the
         * usage operations; a count of units billed as such is carried only by a
         * line of a subscription Mountaya arranged separately.
         */
        public val quantity: kotlin.Int,
        /**
         * The usage dimension this line is metered on. Absent on a line billed by
         * a count of units rather than by usage, which only a subscription
         * Mountaya arranged separately carries.
         */
        public val meter: AdminSchema.MeterDimension? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationSubscriptionItem &&
                other.price_id == this.price_id &&
                other.quantity == this.quantity &&
                other.meter == this.meter

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.price_id.hashCode()
            hash = 31 * hash + this.quantity.hashCode()
            hash = 31 * hash + (this.meter?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationSubscriptionItem(" +
                "price_id=" + this.price_id +
                ", quantity=" + this.quantity +
                ", meter=" + this.meter +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationSubscriptionItem], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: AdminSchema.MeterDimension? = null

            /**
             * The usage dimension this line is metered on. Absent on a line billed by
             * a count of units rather than by usage, which only a subscription
             * Mountaya arranged separately carries.
             */
            public var meter: AdminSchema.MeterDimension
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationSubscriptionItem]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationSubscriptionItem] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                price_id: kotlin.String,
                quantity: kotlin.Int,
                optional: AdminSchema.OrganizationSubscriptionItem.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationSubscriptionItem {
                val given = AdminSchema.OrganizationSubscriptionItem.Builder().apply(optional)
                return AdminSchema.OrganizationSubscriptionItem(
                    price_id = price_id,
                    quantity = quantity,
                    meter = given.slot2,
                )
            }
        }
    }

    /**
     * An organization's subscription, as the billing operations report it. `api`
     * is the metered Data API subscription, billed by the month on the Data API
     * requests the organization makes, and the one subscription an
     * operation in this contract opens: the organization's first saved card opens
     * it, unless the customer, or that card, still owes an invoice, which is the
     * `402` and opens nothing, and the organization holds `data` while it is in
     * force. Opening billing
     * opens nothing, so an organization has no summary at all until its first card
     * has opened that subscription. One that has ended is still reported, with the
     * status it ended on. What an organization's agreement with Mountaya grants,
     * Map Studio, Map Publishing, the Tile API, Embedded Studio and White label, is
     * not a subscription and is never reported here.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationSubscriptionSummary private constructor(
        /**
         * The metered Data API subscription. It carries the Data API line, which
         * bills the requests the organization makes, the MCP server's included,
         * at the end of each billing period, a month counted from the day the
         * subscription opened. A line's price says
         * nothing a client should read a product from: `organization_capabilities`
         * on `getMyOrganizations` says which products the organization holds. An
         * organization that has never held one, as before its first card opens
         * it, gets no summary at all rather than a summary without it. It is
         * also where a Data API subscription Mountaya arranged separately for the
         * organization is reported, which no operation in this contract opens:
         * only such a one is read on a trial, billed by invoice, or with a line
         * on the `tiles` dimension.
         */
        public val api: AdminSchema.OrganizationSubscription? = null,
        /**
         * Whether the organization has a default payment method, which is what the
         * metered use of the Data API needs: a key's Data API request answers
         * `402` while the subscription is in force and no payment method is on
         * file. It is what lets a client ask for a card before that happens. It
         * says nothing about the subscription beside it: a default payment method
         * can be on file while no subscription is in force, as when one has ended,
         * and a card saved without becoming the default leaves it false. Present
         * whenever a summary is returned. An organization that has never held a
         * subscription gets no summary at all, whether or not it has saved a card:
         * `getOrganizationBillingPaymentMethods` is the read for that.
         */
        public val has_payment_method: kotlin.Boolean? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationSubscriptionSummary &&
                other.api == this.api &&
                other.has_payment_method == this.has_payment_method

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.api?.hashCode() ?: 0)
            hash = 31 * hash + (this.has_payment_method?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationSubscriptionSummary(" +
                "api=" + this.api +
                ", has_payment_method=" + this.has_payment_method +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationSubscriptionSummary], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: AdminSchema.OrganizationSubscription? = null
            internal var slot1: kotlin.Boolean? = null

            /**
             * The metered Data API subscription. It carries the Data API line, which
             * bills the requests the organization makes, the MCP server's included,
             * at the end of each billing period, a month counted from the day the
             * subscription opened. A line's price says
             * nothing a client should read a product from: `organization_capabilities`
             * on `getMyOrganizations` says which products the organization holds. An
             * organization that has never held one, as before its first card opens
             * it, gets no summary at all rather than a summary without it. It is
             * also where a Data API subscription Mountaya arranged separately for the
             * organization is reported, which no operation in this contract opens:
             * only such a one is read on a trial, billed by invoice, or with a line
             * on the `tiles` dimension.
             */
            public var api: AdminSchema.OrganizationSubscription
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Whether the organization has a default payment method, which is what the
             * metered use of the Data API needs: a key's Data API request answers
             * `402` while the subscription is in force and no payment method is on
             * file. It is what lets a client ask for a card before that happens. It
             * says nothing about the subscription beside it: a default payment method
             * can be on file while no subscription is in force, as when one has ended,
             * and a card saved without becoming the default leaves it false. Present
             * whenever a summary is returned. An organization that has never held a
             * subscription gets no summary at all, whether or not it has saved a card:
             * `getOrganizationBillingPaymentMethods` is the read for that.
             */
            public var has_payment_method: kotlin.Boolean
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationSubscriptionSummary]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationSubscriptionSummary] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.OrganizationSubscriptionSummary.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationSubscriptionSummary {
                val given = AdminSchema.OrganizationSubscriptionSummary.Builder().apply(optional)
                return AdminSchema.OrganizationSubscriptionSummary(
                    api = given.slot0,
                    has_payment_method = given.slot1,
                )
            }
        }
    }

    /**
     * A change to the organization's own identity, merged into what is stored: a
     * field left out keeps its value. The display name is the whole of what is
     * settable here: settings and entitlements each have their own surface or none
     * at all, so a rename cannot carry one of them along by accident. An empty
     * object changes nothing and is answered with the organization as it stands.
     */
    @kotlinx.serialization.Serializable
    public class OrganizationUpdateParams private constructor(
        /**
         * Display name of the organization: 2 to 30 characters as sent, and still
         * at least 2 once leading and trailing whitespace is trimmed, which is the
         * form it is stored in. Optional and not nullable: omit it to keep the
         * current name. A name is one line: a tab, a line break or a line separator
         * between two of its characters is a `400`, and one at either end is trimmed
         * with the whitespace there.
         */
        public val name: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.OrganizationUpdateParams &&
                other.name == this.name

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "OrganizationUpdateParams(" +
                "name=" + this.name +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.OrganizationUpdateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null

            /**
             * Display name of the organization: 2 to 30 characters as sent, and still
             * at least 2 once leading and trailing whitespace is trimmed, which is the
             * form it is stored in. Optional and not nullable: omit it to keep the
             * current name. A name is one line: a tab, a line break or a line separator
             * between two of its characters is a `400`, and one at either end is trimmed
             * with the whitespace there.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [AdminSchema.OrganizationUpdateParams]. */
        public companion object {
            /** Returns a [AdminSchema.OrganizationUpdateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.OrganizationUpdateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.OrganizationUpdateParams {
                val given = AdminSchema.OrganizationUpdateParams.Builder().apply(optional)
                return AdminSchema.OrganizationUpdateParams(
                    name = given.slot0,
                )
            }
        }
    }

    /**
     * A payment method saved against the organization's billing account. `type`
     * says which instrument it is and which sub-object is present: `card` carries
     * `card`, and `sepa_debit` carries `sepa_debit`. `is_default` marks the one
     * invoices and subscriptions charge; at most one saved method carries it, and
     * none does until a default has been chosen.
     */
    @kotlinx.serialization.Serializable
    public class PaymentMethod private constructor(
        /**
         * Identifier of the payment method. It is what names the method when
         * making it the default or detaching it. Opaque, and issued by the payment
         * provider rather than by Mountaya, so do not parse it as a UUID the way
         * every `id` Mountaya issues can be. It is the same `pm_` identifier the
         * `payment_method_id` path parameter takes.
         */
        public val id: kotlin.String,
        /**
         * Which instrument the method is, in the payment provider's own words, and
         * so an open set rather than an enum: `card` and `sepa_debit` are the two
         * that carry a sub-object below. A method of any other kind is listed too,
         * with neither sub-object, so a client that can only show those two reads
         * this to skip the rest rather than failing on it.
         */
        public val type: kotlin.String,
        /** Whether this is the method invoices and subscriptions charge. */
        public val is_default: kotlin.Boolean,
        /** The card's display details. Present only when `type` is `card`. */
        public val card: AdminSchema.PaymentMethodCard? = null,
        /**
         * The mandate's display details. Present only when `type` is
         * `sepa_debit`.
         */
        public val sepa_debit: AdminSchema.PaymentMethodSepaDebit? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PaymentMethod &&
                other.id == this.id &&
                other.type == this.type &&
                other.is_default == this.is_default &&
                other.card == this.card &&
                other.sepa_debit == this.sepa_debit

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.type.hashCode()
            hash = 31 * hash + this.is_default.hashCode()
            hash = 31 * hash + (this.card?.hashCode() ?: 0)
            hash = 31 * hash + (this.sepa_debit?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PaymentMethod(" +
                "id=" + this.id +
                ", type=" + this.type +
                ", is_default=" + this.is_default +
                ", card=" + this.card +
                ", sepa_debit=" + this.sepa_debit +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PaymentMethod], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot3: AdminSchema.PaymentMethodCard? = null
            internal var slot4: AdminSchema.PaymentMethodSepaDebit? = null

            /** The card's display details. Present only when `type` is `card`. */
            public var card: AdminSchema.PaymentMethodCard
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * The mandate's display details. Present only when `type` is
             * `sepa_debit`.
             */
            public var sepa_debit: AdminSchema.PaymentMethodSepaDebit
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }
        }

        /** Builds a [AdminSchema.PaymentMethod]. */
        public companion object {
            /** Returns a [AdminSchema.PaymentMethod] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                type: kotlin.String,
                is_default: kotlin.Boolean,
                optional: AdminSchema.PaymentMethod.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PaymentMethod {
                val given = AdminSchema.PaymentMethod.Builder().apply(optional)
                return AdminSchema.PaymentMethod(
                    id = id,
                    type = type,
                    is_default = is_default,
                    card = given.slot3,
                    sepa_debit = given.slot4,
                )
            }
        }
    }

    /**
     * The display details of a card. Only what a payer needs to recognize which
     * card it is: the brand, the last four digits and the expiry. The card number
     * itself is never carried by this contract.
     */
    @kotlinx.serialization.Serializable
    public class PaymentMethodCard private constructor(
        /** The card brand, for example `visa`, `mastercard` or `amex`. */
        public val brand: kotlin.String,
        /** The last four digits of the card number. */
        public val last4: kotlin.String,
        /** The expiry month, 1 through 12. */
        public val exp_month: kotlin.Long,
        /** The expiry year, four digits. */
        public val exp_year: kotlin.Long,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PaymentMethodCard &&
                other.brand == this.brand &&
                other.last4 == this.last4 &&
                other.exp_month == this.exp_month &&
                other.exp_year == this.exp_year

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.brand.hashCode()
            hash = 31 * hash + this.last4.hashCode()
            hash = 31 * hash + this.exp_month.hashCode()
            hash = 31 * hash + this.exp_year.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "PaymentMethodCard(" +
                "brand=" + this.brand +
                ", last4=" + this.last4 +
                ", exp_month=" + this.exp_month +
                ", exp_year=" + this.exp_year +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PaymentMethodCard], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.PaymentMethodCard]. */
        public companion object {
            /** Returns a [AdminSchema.PaymentMethodCard] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                brand: kotlin.String,
                last4: kotlin.String,
                exp_month: kotlin.Long,
                exp_year: kotlin.Long,
                optional: AdminSchema.PaymentMethodCard.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PaymentMethodCard {
                val given = AdminSchema.PaymentMethodCard.Builder().apply(optional)
                return AdminSchema.PaymentMethodCard(
                    brand = brand,
                    last4 = last4,
                    exp_month = exp_month,
                    exp_year = exp_year,
                )
            }
        }
    }

    /**
     * A payment method as a webhook event carries it: its identifier and whether it is the
     * one the organization is charged on. `getOrganizationBillingPaymentMethods` reads its
     * details.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class PaymentMethodEventObject private constructor(
        /**
         * The kind of object this is, always `organization.billing.payment_method`: the
         * family of the event types that carry it, and what tells the members of
         * `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.billing.payment_method",
        /**
         * Identifier of the payment method, the payment provider's `pm_` identifier. It is
         * not a UUID.
         */
        public val id: kotlin.String,
        /** Identifier of the organization the payment method belongs to. */
        public val organization_id: kotlin.String,
        /** Whether the organization is charged on it once this change was made. */
        public val is_default: kotlin.Boolean,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PaymentMethodEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.is_default == this.is_default

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.is_default.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "PaymentMethodEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", is_default=" + this.is_default +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PaymentMethodEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.PaymentMethodEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.PaymentMethodEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                is_default: kotlin.Boolean,
                optional: AdminSchema.PaymentMethodEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PaymentMethodEventObject {
                val given = AdminSchema.PaymentMethodEventObject.Builder().apply(optional)
                return AdminSchema.PaymentMethodEventObject(
                    id = id,
                    organization_id = organization_id,
                    is_default = is_default,
                )
            }
        }
    }

    /**
     * The display details of a SEPA Direct Debit mandate. Only what a payer needs
     * to recognize which account it is: the last four characters of the IBAN, the
     * country the account is held in, and the bank and branch codes when they are
     * known. The IBAN itself is never carried by this contract.
     */
    @kotlinx.serialization.Serializable
    public class PaymentMethodSepaDebit private constructor(
        /** The last four characters of the IBAN. */
        public val last4: kotlin.String,
        /**
         * The two-letter ISO 3166-1 alpha-2 code of the country the account is
         * held in. Always exactly two characters here, unlike the country on a
         * billing address, which an update may send empty to clear.
         */
        public val country: kotlin.String,
        /** The code identifying the bank holding the account. */
        public val bank_code: kotlin.String? = null,
        /** The code identifying the branch holding the account. */
        public val branch_code: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PaymentMethodSepaDebit &&
                other.last4 == this.last4 &&
                other.country == this.country &&
                other.bank_code == this.bank_code &&
                other.branch_code == this.branch_code

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.last4.hashCode()
            hash = 31 * hash + this.country.hashCode()
            hash = 31 * hash + (this.bank_code?.hashCode() ?: 0)
            hash = 31 * hash + (this.branch_code?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PaymentMethodSepaDebit(" +
                "last4=" + this.last4 +
                ", country=" + this.country +
                ", bank_code=" + this.bank_code +
                ", branch_code=" + this.branch_code +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PaymentMethodSepaDebit], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: kotlin.String? = null
            internal var slot3: kotlin.String? = null

            /** The code identifying the bank holding the account. */
            public var bank_code: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** The code identifying the branch holding the account. */
            public var branch_code: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [AdminSchema.PaymentMethodSepaDebit]. */
        public companion object {
            /** Returns a [AdminSchema.PaymentMethodSepaDebit] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                last4: kotlin.String,
                country: kotlin.String,
                optional: AdminSchema.PaymentMethodSepaDebit.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PaymentMethodSepaDebit {
                val given = AdminSchema.PaymentMethodSepaDebit.Builder().apply(optional)
                return AdminSchema.PaymentMethodSepaDebit(
                    last4 = last4,
                    country = country,
                    bank_code = given.slot2,
                    branch_code = given.slot3,
                )
            }
        }
    }

    /**
     * A user's own display preferences: the language they read in, the theme they
     * see, and the four units every figure this API returns is rendered in. They
     * belong to the PERSON rather than to an organization, so they follow the user
     * across every organization they are a member of.
     *
     * They shape rendering only. Nothing here changes what an operation returns: a
     * distance comes back in meters whichever length unit is set, and a client
     * formats it. The organization's own defaults, which seed what an embed shows a
     * visitor who has no preferences of their own, are in `OrganizationSettings`.
     */
    @kotlinx.serialization.Serializable
    public class Preferences private constructor(
        /** The language user-facing content is written in. */
        public val language: com.mountaya.client.generated.RestSchema.Language,
        /** The color theme the interface is painted in. */
        public val theme: com.mountaya.client.generated.RestSchema.Theme,
        /** The measurement system distances, elevations and lengths are shown in. */
        public val length: com.mountaya.client.generated.RestSchema.UnitLength,
        /**
         * The form a travel rate is shown in, time over distance or distance over
         * time.
         */
        public val pace: com.mountaya.client.generated.RestSchema.UnitPace,
        /** The unit a gradient is shown in. */
        public val slope: com.mountaya.client.generated.RestSchema.UnitSlope,
        /** The unit a temperature is shown in. */
        public val temperature: com.mountaya.client.generated.RestSchema.UnitTemperature,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.Preferences &&
                other.language == this.language &&
                other.theme == this.theme &&
                other.length == this.length &&
                other.pace == this.pace &&
                other.slope == this.slope &&
                other.temperature == this.temperature

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.language.hashCode()
            hash = 31 * hash + this.theme.hashCode()
            hash = 31 * hash + this.length.hashCode()
            hash = 31 * hash + this.pace.hashCode()
            hash = 31 * hash + this.slope.hashCode()
            hash = 31 * hash + this.temperature.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "Preferences(" +
                "language=" + this.language +
                ", theme=" + this.theme +
                ", length=" + this.length +
                ", pace=" + this.pace +
                ", slope=" + this.slope +
                ", temperature=" + this.temperature +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.Preferences], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.Preferences]. */
        public companion object {
            /** Returns a [AdminSchema.Preferences] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                language: com.mountaya.client.generated.RestSchema.Language,
                theme: com.mountaya.client.generated.RestSchema.Theme,
                length: com.mountaya.client.generated.RestSchema.UnitLength,
                pace: com.mountaya.client.generated.RestSchema.UnitPace,
                slope: com.mountaya.client.generated.RestSchema.UnitSlope,
                temperature: com.mountaya.client.generated.RestSchema.UnitTemperature,
                optional: AdminSchema.Preferences.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.Preferences {
                val given = AdminSchema.Preferences.Builder().apply(optional)
                return AdminSchema.Preferences(
                    language = language,
                    theme = theme,
                    length = length,
                    pace = pace,
                    slope = slope,
                    temperature = temperature,
                )
            }
        }
    }

    /**
     * The preferences to change. Every field is optional and each one is
     * independent: send only what is changing, and the rest is left as it is.
     *
     * There is nothing to clear. A preference always has a value, so there is no
     * null and no empty string here; omitting a field is the only way to leave it
     * alone, and sending one is the only way to change it. A value outside a
     * field's closed set is a 400 naming that field, the empty string included,
     * which is not a member of any of the six.
     */
    @kotlinx.serialization.Serializable
    public class PreferencesUpdateParams private constructor(
        /**
         * The language user-facing content is written in. Optional and not
         * nullable: omit it to keep the current language.
         */
        public val language: com.mountaya.client.generated.RestSchema.Language? = null,
        /**
         * The color theme the interface is painted in. Optional and not nullable:
         * omit it to keep the current theme.
         */
        public val theme: com.mountaya.client.generated.RestSchema.Theme? = null,
        /**
         * The measurement system distances, elevations and lengths are shown in.
         * Optional and not nullable: omit it to keep the current system.
         */
        public val length: com.mountaya.client.generated.RestSchema.UnitLength? = null,
        /**
         * The form a travel rate is shown in, time over distance or distance over
         * time. Optional and not nullable: omit it to keep the current form.
         */
        public val pace: com.mountaya.client.generated.RestSchema.UnitPace? = null,
        /**
         * The unit a gradient is shown in. Optional and not nullable: omit it to
         * keep the current unit.
         */
        public val slope: com.mountaya.client.generated.RestSchema.UnitSlope? = null,
        /**
         * The unit a temperature is shown in. Optional and not nullable: omit it to
         * keep the current unit.
         */
        public val temperature: com.mountaya.client.generated.RestSchema.UnitTemperature? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PreferencesUpdateParams &&
                other.language == this.language &&
                other.theme == this.theme &&
                other.length == this.length &&
                other.pace == this.pace &&
                other.slope == this.slope &&
                other.temperature == this.temperature

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.language?.hashCode() ?: 0)
            hash = 31 * hash + (this.theme?.hashCode() ?: 0)
            hash = 31 * hash + (this.length?.hashCode() ?: 0)
            hash = 31 * hash + (this.pace?.hashCode() ?: 0)
            hash = 31 * hash + (this.slope?.hashCode() ?: 0)
            hash = 31 * hash + (this.temperature?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PreferencesUpdateParams(" +
                "language=" + this.language +
                ", theme=" + this.theme +
                ", length=" + this.length +
                ", pace=" + this.pace +
                ", slope=" + this.slope +
                ", temperature=" + this.temperature +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PreferencesUpdateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.generated.RestSchema.Language? = null
            internal var slot1: com.mountaya.client.generated.RestSchema.Theme? = null
            internal var slot2: com.mountaya.client.generated.RestSchema.UnitLength? = null
            internal var slot3: com.mountaya.client.generated.RestSchema.UnitPace? = null
            internal var slot4: com.mountaya.client.generated.RestSchema.UnitSlope? = null
            internal var slot5: com.mountaya.client.generated.RestSchema.UnitTemperature? = null

            /**
             * The language user-facing content is written in. Optional and not
             * nullable: omit it to keep the current language.
             */
            public var language: com.mountaya.client.generated.RestSchema.Language
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The color theme the interface is painted in. Optional and not nullable:
             * omit it to keep the current theme.
             */
            public var theme: com.mountaya.client.generated.RestSchema.Theme
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * The measurement system distances, elevations and lengths are shown in.
             * Optional and not nullable: omit it to keep the current system.
             */
            public var length: com.mountaya.client.generated.RestSchema.UnitLength
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * The form a travel rate is shown in, time over distance or distance over
             * time. Optional and not nullable: omit it to keep the current form.
             */
            public var pace: com.mountaya.client.generated.RestSchema.UnitPace
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * The unit a gradient is shown in. Optional and not nullable: omit it to
             * keep the current unit.
             */
            public var slope: com.mountaya.client.generated.RestSchema.UnitSlope
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * The unit a temperature is shown in. Optional and not nullable: omit it to
             * keep the current unit.
             */
            public var temperature: com.mountaya.client.generated.RestSchema.UnitTemperature
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [AdminSchema.PreferencesUpdateParams]. */
        public companion object {
            /** Returns a [AdminSchema.PreferencesUpdateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.PreferencesUpdateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PreferencesUpdateParams {
                val given = AdminSchema.PreferencesUpdateParams.Builder().apply(optional)
                return AdminSchema.PreferencesUpdateParams(
                    language = given.slot0,
                    theme = given.slot1,
                    length = given.slot2,
                    pace = given.slot3,
                    slope = given.slot4,
                    temperature = given.slot5,
                )
            }
        }
    }

    /**
     * What a promotion code would be worth to this organization, so a client can
     * show the discount before the code is given to provisioning, rather than
     * finding out once the first card opens the subscription it discounts.
     *
     * When `valid` is false every other field is absent and nothing further is
     * said: an unknown code, an expired one, and one restricted to a different
     * customer are deliberately indistinguishable, so this operation cannot be
     * used to discover which codes exist.
     */
    @kotlinx.serialization.Serializable
    public class PromotionCodePreview private constructor(
        /**
         * Whether the code can be applied by this organization. False for a code
         * that is unknown, inactive, expired, or restricted to someone else, and
         * the four are not told apart. It judges the code alone: a code discounts
         * an organization's first Data API subscription only, so one that has
         * already held a subscription is refused every code by
         * `postOrganizationBillingProvision`, whatever this reads.
         */
        public val valid: kotlin.Boolean,
        /**
         * How to name the discount to the payer. Present only when `valid` is
         * true. Unbounded, unlike the labels a caller authors, because the text
         * comes back from the payment provider and this contract does not get to
         * set its ceiling.
         */
        public val label: kotlin.String? = null,
        /**
         * The discount as a percentage, so 20 is twenty percent off. Present only
         * when `valid` is true and the discount is proportional, in which case
         * `amount_off` is absent.
         */
        public val percent_off: kotlin.Double? = null,
        /**
         * The discount as a fixed sum, in the currency's minor unit. Present only
         * when `valid` is true and the discount is a fixed sum, in which case
         * `percent_off` is absent.
         */
        public val amount_off: kotlin.Long? = null,
        /**
         * The ISO 4217 code `amount_off` is denominated in. Present only alongside
         * `amount_off`.
         */
        public val currency: kotlin.String? = null,
        /**
         * How long the discount lasts: `once`, `repeating` or `forever`. Present
         * only when `valid` is true. A word rather than a number, unlike the
         * `duration` on a segment or a directions step, which is a count of
         * seconds. The vocabulary is the payment provider's, so it is declared as
         * a string rather than as an enum this contract would have to bump a tag
         * to widen.
         */
        public val duration: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PromotionCodePreview &&
                other.valid == this.valid &&
                other.label == this.label &&
                other.percent_off == this.percent_off &&
                other.amount_off == this.amount_off &&
                other.currency == this.currency &&
                other.duration == this.duration

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.valid.hashCode()
            hash = 31 * hash + (this.label?.hashCode() ?: 0)
            hash = 31 * hash + (this.percent_off?.hashCode() ?: 0)
            hash = 31 * hash + (this.amount_off?.hashCode() ?: 0)
            hash = 31 * hash + (this.currency?.hashCode() ?: 0)
            hash = 31 * hash + (this.duration?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PromotionCodePreview(" +
                "valid=" + this.valid +
                ", label=" + this.label +
                ", percent_off=" + this.percent_off +
                ", amount_off=" + this.amount_off +
                ", currency=" + this.currency +
                ", duration=" + this.duration +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PromotionCodePreview], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.Double? = null
            internal var slot3: kotlin.Long? = null
            internal var slot4: kotlin.String? = null
            internal var slot5: kotlin.String? = null

            /**
             * How to name the discount to the payer. Present only when `valid` is
             * true. Unbounded, unlike the labels a caller authors, because the text
             * comes back from the payment provider and this contract does not get to
             * set its ceiling.
             */
            public var label: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * The discount as a percentage, so 20 is twenty percent off. Present only
             * when `valid` is true and the discount is proportional, in which case
             * `amount_off` is absent.
             */
            public var percent_off: kotlin.Double
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * The discount as a fixed sum, in the currency's minor unit. Present only
             * when `valid` is true and the discount is a fixed sum, in which case
             * `percent_off` is absent.
             */
            public var amount_off: kotlin.Long
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * The ISO 4217 code `amount_off` is denominated in. Present only alongside
             * `amount_off`.
             */
            public var currency: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * How long the discount lasts: `once`, `repeating` or `forever`. Present
             * only when `valid` is true. A word rather than a number, unlike the
             * `duration` on a segment or a directions step, which is a count of
             * seconds. The vocabulary is the payment provider's, so it is declared as
             * a string rather than as an enum this contract would have to bump a tag
             * to widen.
             */
            public var duration: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [AdminSchema.PromotionCodePreview]. */
        public companion object {
            /** Returns a [AdminSchema.PromotionCodePreview] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                valid: kotlin.Boolean,
                optional: AdminSchema.PromotionCodePreview.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PromotionCodePreview {
                val given = AdminSchema.PromotionCodePreview.Builder().apply(optional)
                return AdminSchema.PromotionCodePreview(
                    valid = valid,
                    label = given.slot1,
                    percent_off = given.slot2,
                    amount_off = given.slot3,
                    currency = given.slot4,
                    duration = given.slot5,
                )
            }
        }
    }

    /**
     * A publishable key: the credential a browser presents. It is safe to ship in
     * a page, which is why what it reaches is narrowed by its scopes and why a
     * `management` scope is never enough on its own.
     */
    @kotlinx.serialization.Serializable
    public class PublishableKey private constructor(
        /** Timestamp of the publishable key's creation. */
        public val created_at: kotlin.time.Instant,
        /**
         * The version of the key, the one the `ETag` of a read of it carries,
         * opaque and compared whole. It moves on every change to the key's name
         * or scopes and when it is revoked. Send it as `If-Match` to make
         * `patchOrganizationPublishableKey` or `deleteOrganizationPublishableKey`
         * conditional.
         */
        public val etag: kotlin.String,
        /**
         * Identifier of the key. It is what names the key on an update or a
         * revocation, and is not the key itself.
         */
        public val id: kotlin.String,
        /**
         * The key value, the `pk_` token a client sends as `X-API-Key`. Always the
         * `pk_` prefix followed by 32 lowercase hexadecimal characters, 35 in all,
         * the shape every key is minted with. It is returned in full on every read,
         * because a publishable key is meant to be seen.
         */
        public val key: kotlin.String,
        /**
         * Timestamp of the last request that presented this key, recorded at most
         * once a minute, so it may trail the latest use by up to a minute. Absent on
         * a key never used.
         */
        public val last_used_at: kotlin.time.Instant? = null,
        /** Display name of the publishable key. */
        public val name: kotlin.String,
        /** Identifier of the organization the key belongs to. */
        public val organization_id: kotlin.String,
        /** Timestamp of the key's revocation. Absent on a key still in force. */
        public val revoked_at: kotlin.time.Instant? = null,
        /**
         * The scopes the key carries, each at most once, in the order
         * `PublishableKeyScope` declares them whatever order they were sent in.
         * A key in force carries at least one, and one carrying `management`
         * carries no other. A revoked key lists what it carried
         * when it was revoked, which can be none.
         * The list is what the key was given and nothing more, so no scope appears
         * because another is present. A scope stays listed when its product is
         * later switched off for the organization, and answers `402` where it is
         * used until the product returns.
         */
        public val scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PublishableKey &&
                other.created_at == this.created_at &&
                other.etag == this.etag &&
                other.id == this.id &&
                other.key == this.key &&
                other.last_used_at == this.last_used_at &&
                other.name == this.name &&
                other.organization_id == this.organization_id &&
                other.revoked_at == this.revoked_at &&
                other.scopes == this.scopes

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.etag.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.key.hashCode()
            hash = 31 * hash + (this.last_used_at?.hashCode() ?: 0)
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + (this.revoked_at?.hashCode() ?: 0)
            hash = 31 * hash + this.scopes.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "PublishableKey(" +
                "created_at=" + this.created_at +
                ", etag=" + this.etag +
                ", id=" + this.id +
                ", key=" + this.key +
                ", last_used_at=" + this.last_used_at +
                ", name=" + this.name +
                ", organization_id=" + this.organization_id +
                ", revoked_at=" + this.revoked_at +
                ", scopes=" + this.scopes +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PublishableKey], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot4: kotlin.time.Instant? = null
            internal var slot7: kotlin.time.Instant? = null

            /**
             * Timestamp of the last request that presented this key, recorded at most
             * once a minute, so it may trail the latest use by up to a minute. Absent on
             * a key never used.
             */
            public var last_used_at: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** Timestamp of the key's revocation. Absent on a key still in force. */
            public var revoked_at: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }
        }

        /** Builds a [AdminSchema.PublishableKey]. */
        public companion object {
            /** Returns a [AdminSchema.PublishableKey] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                created_at: kotlin.time.Instant,
                etag: kotlin.String,
                id: kotlin.String,
                key: kotlin.String,
                name: kotlin.String,
                organization_id: kotlin.String,
                scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>,
                optional: AdminSchema.PublishableKey.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PublishableKey {
                val given = AdminSchema.PublishableKey.Builder().apply(optional)
                return AdminSchema.PublishableKey(
                    created_at = created_at,
                    etag = etag,
                    id = id,
                    key = key,
                    last_used_at = given.slot4,
                    name = name,
                    organization_id = organization_id,
                    revoked_at = given.slot7,
                    scopes = scopes,
                )
            }
        }
    }

    /**
     * A new publishable key. Both fields are required: a key with no scopes reaches
     * nothing, so there is no useful default to fall back on, and an empty `scopes`
     * list is a 400. The key is given exactly the scopes listed: none is added
     * because another is present.
     */
    @kotlinx.serialization.Serializable
    public class PublishableKeyCreateParams private constructor(
        /**
         * Display name of the publishable key: 2 to 30 characters as sent, and still
         * at least 2 once leading and trailing whitespace is trimmed, which is the
         * form it is stored in. A name is one line: a tab, a line break or a line
         * separator between two of its characters is a `400`, and one at either end
         * is trimmed with the whitespace there.
         */
        public val name: kotlin.String,
        /**
         * The scopes to give the key, each at most once. A key carries at least
         * one: an empty list is a `400` naming `scopes`. A scope is granted only
         * while the organization holds its product, so one it does not hold is a
         * `402` naming its position in the list. `management` is carried alone:
         * a list naming it beside another scope is a `400` naming the position of
         * each scope beside it. `studio-embedded` is carried with `tiles` and
         * `data`: a list naming it without one of them is a `400` naming
         * `scopes`, once per missing scope.
         */
        public val scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PublishableKeyCreateParams &&
                other.name == this.name &&
                other.scopes == this.scopes

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + this.scopes.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "PublishableKeyCreateParams(" +
                "name=" + this.name +
                ", scopes=" + this.scopes +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PublishableKeyCreateParams], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.PublishableKeyCreateParams]. */
        public companion object {
            /** Returns a [AdminSchema.PublishableKeyCreateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                name: kotlin.String,
                scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>,
                optional: AdminSchema.PublishableKeyCreateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PublishableKeyCreateParams {
                val given = AdminSchema.PublishableKeyCreateParams.Builder().apply(optional)
                return AdminSchema.PublishableKeyCreateParams(
                    name = name,
                    scopes = scopes,
                )
            }
        }
    }

    /**
     * A publishable key as a webhook event carries it: its identifiers, its name, its scopes
     * and the last four characters of its value. The value itself is never carried:
     * `getOrganizationPublishableKey` reads it.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class PublishableKeyEventObject private constructor(
        /**
         * The kind of object this is, always `organization.publishable_key`: the family of
         * the event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.publishable_key",
        /** Identifier of the key. */
        public val id: kotlin.String,
        /** Identifier of the organization the key belongs to. */
        public val organization_id: kotlin.String,
        /** The key's name. */
        public val name: kotlin.String,
        /** The scopes the key carries once this change was made. */
        public val scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>,
        /** The last four characters of the key's value. */
        public val suffix: kotlin.String,
        /**
         * The key's version once this change was made. It climbs with every change a read of
         * the key shows, so of two events about one key the one carrying the higher
         * `version` is the later state: events are not delivered in order.
         */
        public val version: kotlin.Long,
        /** When the key was created. */
        public val created_at: kotlin.time.Instant,
        /** When the key was revoked. Present once it is. */
        public val revoked_at: kotlin.time.Instant? = null,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PublishableKeyEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.name == this.name &&
                other.scopes == this.scopes &&
                other.suffix == this.suffix &&
                other.version == this.version &&
                other.created_at == this.created_at &&
                other.revoked_at == this.revoked_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + this.scopes.hashCode()
            hash = 31 * hash + this.suffix.hashCode()
            hash = 31 * hash + this.version.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + (this.revoked_at?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PublishableKeyEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", name=" + this.name +
                ", scopes=" + this.scopes +
                ", suffix=" + this.suffix +
                ", version=" + this.version +
                ", created_at=" + this.created_at +
                ", revoked_at=" + this.revoked_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PublishableKeyEventObject], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot8: kotlin.time.Instant? = null

            /** When the key was revoked. Present once it is. */
            public var revoked_at: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }
        }

        /** Builds a [AdminSchema.PublishableKeyEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.PublishableKeyEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                name: kotlin.String,
                scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>,
                suffix: kotlin.String,
                version: kotlin.Long,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.PublishableKeyEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PublishableKeyEventObject {
                val given = AdminSchema.PublishableKeyEventObject.Builder().apply(optional)
                return AdminSchema.PublishableKeyEventObject(
                    id = id,
                    organization_id = organization_id,
                    name = name,
                    scopes = scopes,
                    suffix = suffix,
                    version = version,
                    created_at = created_at,
                    revoked_at = given.slot8,
                )
            }
        }
    }

    /**
     * A scope a publishable key carries. Each one names a product, except
     * `management`, which names the organization's own account. A key reaches
     * nothing beyond the scopes it carries: a request under a scope it lacks answers
     * `403`, whatever else is right about it.
     *
     * A scope opens its product only while the organization holds that product. A
     * request under a scope whose product the organization does not hold answers
     * `402`, and the product is checked before the key's scopes, so a key that
     * lacks the scope as well is answered `402` rather than `403`.
     *
     * A key carries exactly the scopes it was given, and none is added because
     * another is present. One scope INCLUDES another, which is a different thing:
     * an operation that asks for `publishing` accepts a key carrying
     * `studio-embedded`. `x-pkey-scope-includes` on this schema states that
     * relation, keyed by the scope that includes and listing the scopes it
     * includes, and it is the whole of it: no other scope includes another.
     *
     * A key carrying `management` carries no other scope, so a key either ships to
     * pages, with product scopes, or administers, with `management` alone.
     * Creating or updating a key with `management` beside another scope is a
     * `400`, one entry per scope listed beside it.
     *
     * A key carrying `studio-embedded` carries `tiles` and `data` beside it, the
     * scopes its editor's map and previews are requested under, since that scope
     * reaches neither the Tile API nor the Data API. Neither is added for it:
     * creating or updating a key whose list names `studio-embedded` without one of
     * them is a `400`, one entry per missing scope, each naming `scopes`, judged
     * with the rest of the body and before any scope's product.
     * `x-pkey-scope-needs` on this schema states that requirement, keyed by the
     * scope that needs and listing the scopes it needs, and it is the whole of it:
     * no other scope needs another.
     *
     * On the Map Studio API the inclusion also widens which products answer. An
     * operation that takes `publishing` passes for a key carrying `studio-embedded`
     * when the organization holds Map Publishing, or holds Embedded Studio and Map
     * Studio, the two products that scope belongs to, so an organization that
     * edits its itineraries in an Embedded Studio reads them back without Map
     * Publishing. A key carrying `publishing` alone is asked for Map Publishing. The
     * tiles of Mountaya's hosted embed are not widened this way: on the Tile API
     * they are checked against Map Publishing whichever of the two scopes the key
     * carries.
     *
     * Two operations behind `management` carry no organization's data at all,
     * `getAiProviders` and `getBillingProducts`, and they are there because of when
     * they are read rather than what they hold: each is the catalog an
     * administrator picks from while configuring the thing beside it. The catalogs
     * an ordinary caller reads, `getActivities` and `getLayers`, take `publishing`
     * and reach no account at all. The Tile API answers its own layer catalog,
     * `getTileLayers`, under `tiles`, so a map drawn with the Tile API alone needs
     * no `publishing` to find its layers.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class PublishableKeyScope(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Map Publishing. Opens Mountaya's hosted embed and reads what a published map needs, read-only. That is the layer and activity catalogs, itineraries, collections, zones and the organization itself, and an itinerary's route file. Requires Map Publishing, which the organization's agreement grants. Publishing an itinerary or a zone is not done under it: that acts as a member, so it takes `studio-embedded`, and asks for Map Publishing there. The tiles the hosted embed draws are recorded as the organization's Tile API usage on the `embedding` surface and counted toward its per-minute limit and its monthly allowance, held per client IP address as a key sent without a session token is, as the Tile API states. */
            public val Publishing: PublishableKeyScope = PublishableKeyScope("publishing")

            /**
             * Embedded Studio, the editor mounted inside your own product. Everything `publishing` reaches on the Map Studio API, plus every operation that acts as a member. That is creating, editing, publishing and sharing itineraries, collections and zones, the agent, and live collaboration. Requires two products, Embedded Studio and Map Studio, both granted by the organization's agreement, and an organization lacking either is answered `402` before the member is looked at. Publishing an itinerary or a zone, `postItineraryPublication` and `postZonePublication`, asks for Map Publishing as well. Some operations ask for Embedded Studio alone, so they answer when the agreement does not include Map Studio. They are the acts that take something down, `deleteItinerary`, `deleteItineraryPublication`, `deleteItineraryShareLink`, `deleteCollection`, `deleteCollectionItinerary`, `deleteCollectionZone`, `deleteCollectionShareLink`, `deleteZone` and `deleteZonePublication`, and the operations under `/v1/me`, which concern the person alone. The three operations on an agent run already started, `postAgentRunInput`, `getAgentRunEvents` and `postAgentRunFeedback`, ask for Embedded Studio alone as well, and leave Map Studio to the run. The run answers an input or an events connect `402` when the organization it acts in does not hold Map Studio, and closes, and a rating does not ask for Map Studio at all. A `publishing` read reached through this scope passes when the organization holds Map Publishing, or Embedded Studio and Map Studio. Every operation behind it requires a session token minted for the `studio-embedded` surface, which your own backend asks `postSession` for, and every operation that acts as a member requires that token to name the member, so a key alone acts as nobody; a token minted for any other surface is a `401` here. It reaches neither the Tile API nor the Data API: the studio's map draws its tiles under `tiles` and its search and isochrone previews ask the Data API under `data`, so its key carries those as well, and a create or update listing this scope without `tiles` or without `data` is a `400` naming `scopes`, once per missing scope. Each of those requests rides the studio's `studio-embedded` session, is recorded as `studio-embedded` usage and is never counted toward the organization's per-minute limits, its monthly allowances or its invoice, held instead to the bounds each API states for it, for as long as the organization holds Embedded Studio.
             *
             * It includes `publishing`: a key carrying it reaches what that scope reaches.
             *
             * A key carrying it carries `tiles` and `data` beside it.
             */
            public val StudioEmbedded: PublishableKeyScope = PublishableKeyScope("studio-embedded")

            /** The Tile API, its layer catalog included. Requires the Tile API product, which the organization's agreement grants. Tiles drawn by Mountaya's hosted embed are checked against `publishing` instead, so a key carrying `tiles` alone is refused there. A request is taken for the hosted embed's when its `Origin`, or failing that its `Referer`, names a page on Mountaya's own domain, which only a browser guarantees, since a client that is not a browser sets those headers as it likes. */
            public val Tiles: PublishableKeyScope = PublishableKeyScope("tiles")

            /** The Data API, queried directly. Requires the Data API product, which the organization holds once its first card has opened the metered Data API subscription, or by its agreement. It does not grant the MCP server. */
            public val Data: PublishableKeyScope = PublishableKeyScope("data")

            /** Mountaya's MCP server. Requires the Data API product, held as `data` describes. It does not grant the Data API directly. */
            public val Mcp: PublishableKeyScope = PublishableKeyScope("mcp")

            /** Administering the organization, meaning its keys, billing, members, settings, AI configuration, webhooks and usage. It belongs to no product. Every operation behind it also requires a session token, so a key alone never suffices. A write requires a token naming a user, the member it acts as, who holds the role the write asks. A read accepts a token minted for `api` without `user_id` and asks it for no member role, which is why such a token, minted for a key carrying this scope, stays on your own backend; one minted for `studio-embedded` without `user_id` is the `401` a key alone gets; a token naming a user is answered only while that user holds the role the read asks, and is otherwise a `403`. It carries no other scope: creating or updating a key with it beside another scope is a `400`, so the administering key is a key of its own. Never ship it in a page. */
            public val Management: PublishableKeyScope = PublishableKeyScope("management")
        }
    }

    /**
     * A change to a publishable key. Both fields are optional; an omitted field
     * keeps its current value. `scopes` is replaced whole rather than merged, so a
     * request that sends it lists every scope the key is to keep, and a scope left
     * out is removed. An empty `scopes` list is a 400, and none is added to the
     * ones listed.
     */
    @kotlinx.serialization.Serializable
    public class PublishableKeyUpdateParams private constructor(
        /**
         * Display name of the publishable key. Omit to keep the current value.
         * When sent, it is 2 to 30 characters as sent, and still at least 2 once
         * leading and trailing whitespace is trimmed, which is the form it is
         * stored in. A name is one line: a tab, a line break or a line separator
         * between two of its characters is a `400`, and one at either end is trimmed
         * with the whitespace there.
         */
        public val name: kotlin.String? = null,
        /**
         * The scopes the key carries from now on, each at most once, replacing the
         * list it held. Omit to keep the current scopes. A key carries at least
         * one: an empty list is a `400` naming `scopes`. A scope the key does not
         * already carry is granted only while the organization holds its product,
         * so one it does not hold is a `402` naming its position in the list; a
         * scope the key already carries is kept whatever has become of its product.
         * `management` is carried alone: a list naming it beside another scope is
         * a `400` naming the position of each scope beside it. `studio-embedded`
         * is carried with `tiles` and `data`: a list naming it without one of
         * them is a `400` naming `scopes`, once per missing scope, whatever the
         * key carries now.
         */
        public val scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.PublishableKeyUpdateParams &&
                other.name == this.name &&
                other.scopes == this.scopes

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.name?.hashCode() ?: 0)
            hash = 31 * hash + (this.scopes?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "PublishableKeyUpdateParams(" +
                "name=" + this.name +
                ", scopes=" + this.scopes +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.PublishableKeyUpdateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.collections.List<AdminSchema.PublishableKeyScope>? = null

            /**
             * Display name of the publishable key. Omit to keep the current value.
             * When sent, it is 2 to 30 characters as sent, and still at least 2 once
             * leading and trailing whitespace is trimmed, which is the form it is
             * stored in. A name is one line: a tab, a line break or a line separator
             * between two of its characters is a `400`, and one at either end is trimmed
             * with the whitespace there.
             */
            public var name: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The scopes the key carries from now on, each at most once, replacing the
             * list it held. Omit to keep the current scopes. A key carries at least
             * one: an empty list is a `400` naming `scopes`. A scope the key does not
             * already carry is granted only while the organization holds its product,
             * so one it does not hold is a `402` naming its position in the list; a
             * scope the key already carries is kept whatever has become of its product.
             * `management` is carried alone: a list naming it beside another scope is
             * a `400` naming the position of each scope beside it. `studio-embedded`
             * is carried with `tiles` and `data`: a list naming it without one of
             * them is a `400` naming `scopes`, once per missing scope, whatever the
             * key carries now.
             */
            public var scopes: kotlin.collections.List<AdminSchema.PublishableKeyScope>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [AdminSchema.PublishableKeyUpdateParams]. */
        public companion object {
            /** Returns a [AdminSchema.PublishableKeyUpdateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.PublishableKeyUpdateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.PublishableKeyUpdateParams {
                val given = AdminSchema.PublishableKeyUpdateParams.Builder().apply(optional)
                return AdminSchema.PublishableKeyUpdateParams(
                    name = given.slot0,
                    scopes = given.slot1,
                )
            }
        }
    }

    /**
     * What a refused invitation conflicts with, at `extensions.metadata` of the
     * `409` of `postOrganizationInvitation`. Exactly one of the two is present:
     * `member` when the address already belongs to a member of the organization,
     * and `invitation` when an invitation to it is already waiting for an answer.
     */
    @kotlinx.serialization.Serializable
    public class ResponseMetadataPostOrganizationInvitation private constructor(
        /** The member whose account carries the address. */
        public val member: AdminSchema.ResponseMetadataPostOrganizationInvitationMember? = null,
        /** The invitation already waiting for an answer. */
        public val invitation: AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ResponseMetadataPostOrganizationInvitation &&
                other.member == this.member &&
                other.invitation == this.invitation

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.member?.hashCode() ?: 0)
            hash = 31 * hash + (this.invitation?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "ResponseMetadataPostOrganizationInvitation(" +
                "member=" + this.member +
                ", invitation=" + this.invitation +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ResponseMetadataPostOrganizationInvitation], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: AdminSchema.ResponseMetadataPostOrganizationInvitationMember? = null
            internal var slot1: AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation? = null

            /** The member whose account carries the address. */
            public var member: AdminSchema.ResponseMetadataPostOrganizationInvitationMember
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /** The invitation already waiting for an answer. */
            public var invitation: AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [AdminSchema.ResponseMetadataPostOrganizationInvitation]. */
        public companion object {
            /** Returns a [AdminSchema.ResponseMetadataPostOrganizationInvitation] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.ResponseMetadataPostOrganizationInvitation.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ResponseMetadataPostOrganizationInvitation {
                val given = AdminSchema.ResponseMetadataPostOrganizationInvitation.Builder().apply(optional)
                return AdminSchema.ResponseMetadataPostOrganizationInvitation(
                    member = given.slot0,
                    invitation = given.slot1,
                )
            }
        }
    }

    /**
     * A secret key as a webhook event carries it: its identifiers, its name and the last
     * four characters of its value, which is never carried. Secret keys are made, renamed
     * and revoked signed in to Mountaya's web application, and this contract reads none.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class SecretKeyEventObject private constructor(
        /**
         * The kind of object this is, always `organization.secret_key`: the family of the
         * event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.secret_key",
        /** Identifier of the key. */
        public val id: kotlin.String,
        /** Identifier of the organization the key belongs to. */
        public val organization_id: kotlin.String,
        /** The key's name. */
        public val name: kotlin.String,
        /** The last four characters of the key's value. */
        public val suffix: kotlin.String,
        /** When the key was created. */
        public val created_at: kotlin.time.Instant,
        /** When the key was revoked. Present once it is. */
        public val revoked_at: kotlin.time.Instant? = null,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.SecretKeyEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.name == this.name &&
                other.suffix == this.suffix &&
                other.created_at == this.created_at &&
                other.revoked_at == this.revoked_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + this.suffix.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + (this.revoked_at?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SecretKeyEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", name=" + this.name +
                ", suffix=" + this.suffix +
                ", created_at=" + this.created_at +
                ", revoked_at=" + this.revoked_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.SecretKeyEventObject], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot6: kotlin.time.Instant? = null

            /** When the key was revoked. Present once it is. */
            public var revoked_at: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }
        }

        /** Builds a [AdminSchema.SecretKeyEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.SecretKeyEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                name: kotlin.String,
                suffix: kotlin.String,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.SecretKeyEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.SecretKeyEventObject {
                val given = AdminSchema.SecretKeyEventObject.Builder().apply(optional)
                return AdminSchema.SecretKeyEventObject(
                    id = id,
                    organization_id = organization_id,
                    name = name,
                    suffix = suffix,
                    created_at = created_at,
                    revoked_at = given.slot6,
                )
            }
        }
    }

    /**
     * Parameters for creating a session. Requires a valid publishable key value,
     * and names the user the session acts as where it will act as one.
     */
    @kotlinx.serialization.Serializable
    public class SessionCreateParams private constructor(
        /**
         * The publishable key value to bind the session to. Always the `pk_` prefix
         * followed by 32 lowercase hexadecimal characters, 35 in all, the shape
         * every key is minted with.
         */
        public val publishable_key: kotlin.String,
        /**
         * The user the session acts as, which your backend asserts: a member of the
         * publishable key's organization, or a user whose invitation to it is still
         * pending, matched by an address the user's account has verified. The
         * token is signed with it, and every operation whose `x-acting-user` is
         * `required` acts as the user the token names, so a page holding the token
         * acts as this user and nobody else. A `management` read under the token is answered only while this
         * user holds the role that read asks of a member of the organization. The
         * session governs the members with at most an admin's say, whoever it
         * names: a session naming the owner is answered as an admin's on the
         * owner's own acts, which are made only signed in to Mountaya's web
         * application. A user who is neither a member nor invited answers `403`,
         * the same answer whether or not the user exists, and nothing is minted.
         *
         * Omitted, the session is minted without `user_id`, and no operation whose
         * `x-acting-user` is `required` accepts it. Whatever its surface, it reads
         * what the publishable key alone does: the last published snapshot and
         * never a draft, and public-only listings. Minted for `api` it also makes
         * every `management` read its key's `management` scope allows, so a
         * session minted without `user_id` for a key carrying `management` stays
         * on your own backend, and one you hand to a page is minted for a key
         * without `management`; minted for `studio-embedded`, for an Embedded
         * Studio's read-only viewer, it makes no `management` read, which answers
         * it `401`. Name a member when a backend or a viewer must read drafts. A
         * malformed value, or the nil UUID, is a `400` naming the field.
         */
        public val user_id: kotlin.String? = null,
        /**
         * The surface the session is minted for, which it keeps for its whole
         * life. Omitted, it is `api`, the surface of your own clients.
         *
         * Ask for `studio-embedded` for every Embedded Studio you mount, the
         * editor and its read-only viewer alike: it is the only session a
         * `studio-embedded` operation accepts, and what the studio then reaches on
         * the Data API and the Tile API is recorded as `studio-embedded` usage and
         * never counted toward the organization's limits, allowances or invoice.
         * It is minted only for a publishable key that reaches the
         * `studio-embedded` scope, and a key that does not is a `403`, and only
         * for an organization that holds Embedded Studio, one that does not being
         * the `402` a `studio-embedded` operation answers. It may name a member,
         * for the editor, or be minted without `user_id`, for a viewer that only
         * reads, which is then served what the publishable key alone is: published
         * content and never a draft, and the organization's private resources only
         * by their id, never in a listing.
         *
         * `mcp` is minted only for Mountaya's MCP server, and asked for here it is
         * a `403`.
         */
        public val surface: com.mountaya.client.generated.RestSchema.SessionSurface? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.SessionCreateParams &&
                other.publishable_key == this.publishable_key &&
                other.user_id == this.user_id &&
                other.surface == this.surface

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.publishable_key.hashCode()
            hash = 31 * hash + (this.user_id?.hashCode() ?: 0)
            hash = 31 * hash + (this.surface?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SessionCreateParams(" +
                "publishable_key=" + this.publishable_key +
                ", user_id=" + this.user_id +
                ", surface=" + this.surface +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.SessionCreateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null
            internal var slot2: com.mountaya.client.generated.RestSchema.SessionSurface? = null

            /**
             * The user the session acts as, which your backend asserts: a member of the
             * publishable key's organization, or a user whose invitation to it is still
             * pending, matched by an address the user's account has verified. The
             * token is signed with it, and every operation whose `x-acting-user` is
             * `required` acts as the user the token names, so a page holding the token
             * acts as this user and nobody else. A `management` read under the token is answered only while this
             * user holds the role that read asks of a member of the organization. The
             * session governs the members with at most an admin's say, whoever it
             * names: a session naming the owner is answered as an admin's on the
             * owner's own acts, which are made only signed in to Mountaya's web
             * application. A user who is neither a member nor invited answers `403`,
             * the same answer whether or not the user exists, and nothing is minted.
             *
             * Omitted, the session is minted without `user_id`, and no operation whose
             * `x-acting-user` is `required` accepts it. Whatever its surface, it reads
             * what the publishable key alone does: the last published snapshot and
             * never a draft, and public-only listings. Minted for `api` it also makes
             * every `management` read its key's `management` scope allows, so a
             * session minted without `user_id` for a key carrying `management` stays
             * on your own backend, and one you hand to a page is minted for a key
             * without `management`; minted for `studio-embedded`, for an Embedded
             * Studio's read-only viewer, it makes no `management` read, which answers
             * it `401`. Name a member when a backend or a viewer must read drafts. A
             * malformed value, or the nil UUID, is a `400` naming the field.
             */
            public var user_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * The surface the session is minted for, which it keeps for its whole
             * life. Omitted, it is `api`, the surface of your own clients.
             *
             * Ask for `studio-embedded` for every Embedded Studio you mount, the
             * editor and its read-only viewer alike: it is the only session a
             * `studio-embedded` operation accepts, and what the studio then reaches on
             * the Data API and the Tile API is recorded as `studio-embedded` usage and
             * never counted toward the organization's limits, allowances or invoice.
             * It is minted only for a publishable key that reaches the
             * `studio-embedded` scope, and a key that does not is a `403`, and only
             * for an organization that holds Embedded Studio, one that does not being
             * the `402` a `studio-embedded` operation answers. It may name a member,
             * for the editor, or be minted without `user_id`, for a viewer that only
             * reads, which is then served what the publishable key alone is: published
             * content and never a draft, and the organization's private resources only
             * by their id, never in a listing.
             *
             * `mcp` is minted only for Mountaya's MCP server, and asked for here it is
             * a `403`.
             */
            public var surface: com.mountaya.client.generated.RestSchema.SessionSurface
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [AdminSchema.SessionCreateParams]. */
        public companion object {
            /** Returns a [AdminSchema.SessionCreateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                publishable_key: kotlin.String,
                optional: AdminSchema.SessionCreateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.SessionCreateParams {
                val given = AdminSchema.SessionCreateParams.Builder().apply(optional)
                return AdminSchema.SessionCreateParams(
                    publishable_key = publishable_key,
                    user_id = given.slot1,
                    surface = given.slot2,
                )
            }
        }
    }

    /**
     * A single-use secret for collecting a card in the caller's own page rather
     * than sending the payer elsewhere. Hand `client_secret` to Stripe Elements, which
     * confirms the card in the browser, then name the confirmed setup intent on
     * `postOrganizationBillingPaymentMethod` to save the card against the
     * organization.
     *
     * Its `toString` prints every property but its secrets, each printed as `<redacted>`, so a log never carries one, and each secret stays readable.
     */
    @kotlinx.serialization.Serializable
    public class SetupIntentSession private constructor(
        /**
         * The secret Stripe Elements confirms the card with. It is single-use and
         * belongs to this organization alone.
         *
         * It is a secret: its class's `toString` prints it as `<redacted>`, and it stays readable here.
         */
        public val client_secret: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.SetupIntentSession &&
                other.client_secret == this.client_secret

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.client_secret.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "SetupIntentSession(" +
                "client_secret=" + "<redacted>" +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.SetupIntentSession], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.SetupIntentSession]. */
        public companion object {
            /** Returns a [AdminSchema.SetupIntentSession] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                client_secret: kotlin.String,
                optional: AdminSchema.SetupIntentSession.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.SetupIntentSession {
                val given = AdminSchema.SetupIntentSession.Builder().apply(optional)
                return AdminSchema.SetupIntentSession(
                    client_secret = client_secret,
                )
            }
        }
    }

    /**
     * How a subscription is paid for. It is what tells a reader whether adding a
     * card changes anything: a subscription collected by invoice is settled outside
     * the card on file, and one charged automatically is not. The Data API
     * subscription, the one an operation in this contract opens, is charged
     * automatically; one collected by invoice is one Mountaya arranged
     * separately.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class SubscriptionCollectionMethod(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Charged automatically to the card on file. */
            public val ChargeAutomatically: SubscriptionCollectionMethod = SubscriptionCollectionMethod("charge-automatically")

            /** Billed by invoice and settled separately. */
            public val SendInvoice: SubscriptionCollectionMethod = SubscriptionCollectionMethod("send-invoice")
        }
    }

    /**
     * The discount on a subscription. It is read from the subscription itself rather
     * than remembered from the moment the code was given, so a client shows the
     * active discount on every visit and not only on the one where the code was
     * entered. It says how long the discount lasts and, when it stops, when, so a
     * client never presents a discount that is about to lapse as a lasting one.
     * Absent from a subscription carrying no discount.
     */
    @kotlinx.serialization.Serializable
    public class SubscriptionDiscount private constructor(
        /**
         * How long the discount lasts: `once`, `repeating` or `forever`, the same
         * vocabulary the promotion-code preview answers, so a client describes
         * the running discount the way it described the code. A word rather than
         * a number, unlike the `duration` on a segment or a directions step,
         * which is a count of seconds. The vocabulary is the payment provider's,
         * so it is declared as a string rather than as an enum this contract
         * would have to bump a tag to widen.
         */
        public val duration: kotlin.String,
        /**
         * When the discount stops applying, for a `repeating` discount: invoices
         * issued after it carry no discount. Null for a `forever` discount, which
         * never ends, and for a `once` discount, which applies to a single
         * invoice and has no date of its own.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val ends_at: kotlin.time.Instant? = null,
        /**
         * How to name the discount to the payer, never empty: the discount's own
         * name, else the promotion code it was applied with, else, for an unnamed
         * discount applied without a code, the discount's identifier at the
         * payment provider.
         */
        public val label: kotlin.String,
        /**
         * The discount as a percentage, so 20 is twenty percent off. Present only
         * when the discount is proportional, in which case `amount_off` is absent.
         */
        public val percent_off: kotlin.Double? = null,
        /**
         * The discount as a fixed sum, in the currency's minor unit. Present only
         * when the discount is a fixed sum, in which case `percent_off` is absent.
         */
        public val amount_off: kotlin.Long? = null,
        /**
         * The ISO 4217 code `amount_off` is denominated in. Present only alongside
         * `amount_off`.
         */
        public val currency: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.SubscriptionDiscount &&
                other.duration == this.duration &&
                other.ends_at == this.ends_at &&
                other.label == this.label &&
                other.percent_off == this.percent_off &&
                other.amount_off == this.amount_off &&
                other.currency == this.currency

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.duration.hashCode()
            hash = 31 * hash + (this.ends_at?.hashCode() ?: 0)
            hash = 31 * hash + this.label.hashCode()
            hash = 31 * hash + (this.percent_off?.hashCode() ?: 0)
            hash = 31 * hash + (this.amount_off?.hashCode() ?: 0)
            hash = 31 * hash + (this.currency?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "SubscriptionDiscount(" +
                "duration=" + this.duration +
                ", ends_at=" + this.ends_at +
                ", label=" + this.label +
                ", percent_off=" + this.percent_off +
                ", amount_off=" + this.amount_off +
                ", currency=" + this.currency +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.SubscriptionDiscount], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot3: kotlin.Double? = null
            internal var slot4: kotlin.Long? = null
            internal var slot5: kotlin.String? = null

            /**
             * The discount as a percentage, so 20 is twenty percent off. Present only
             * when the discount is proportional, in which case `amount_off` is absent.
             */
            public var percent_off: kotlin.Double
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /**
             * The discount as a fixed sum, in the currency's minor unit. Present only
             * when the discount is a fixed sum, in which case `percent_off` is absent.
             */
            public var amount_off: kotlin.Long
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * The ISO 4217 code `amount_off` is denominated in. Present only alongside
             * `amount_off`.
             */
            public var currency: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [AdminSchema.SubscriptionDiscount]. */
        public companion object {
            /** Returns a [AdminSchema.SubscriptionDiscount] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                duration: kotlin.String,
                ends_at: kotlin.time.Instant?,
                label: kotlin.String,
                optional: AdminSchema.SubscriptionDiscount.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.SubscriptionDiscount {
                val given = AdminSchema.SubscriptionDiscount.Builder().apply(optional)
                return AdminSchema.SubscriptionDiscount(
                    duration = duration,
                    ends_at = ends_at,
                    label = label,
                    percent_off = given.slot3,
                    amount_off = given.slot4,
                    currency = given.slot5,
                )
            }
        }
    }

    /**
     * Lifecycle state of a subscription. It is the value the organization's `data`
     * capability follows, where the organization holds the Data API by its
     * subscription. `canceled`, `unpaid`, `incomplete-expired` and `paused` suspend what
     * the subscription grants, as does a subscription that reads `active` with its
     * `paused` flag set, and a request that needed one of those capabilities
     * answers `402` rather than failing silently. `active`, `trialing` and
     * `incomplete` keep what it grants in force, and so does `past-due` once the
     * subscription has paid an invoice. `past-due` is the window, at most 7 days,
     * in which a failed renewal is retried before the subscription is canceled. A
     * subscription that has never paid an invoice keeps nothing from its first
     * failed payment. No operation in this contract opens a subscription that
     * reads `trialing`, `incomplete` or `incomplete-expired`: the Data API
     * subscription is opened by the organization's first card and reads `active`
     * from that moment, or is not opened at all when that card is refused, or
     * when the customer, or that card, still owes an invoice, which is the `402`
     * and opens nothing, so those three are read only on a subscription Mountaya
     * arranged separately. A status
     * outside these eight, which the payment provider may add, is published in the
     * same kebab-case and suspends what the subscription grants; treat it as
     * unrecognized rather than as an error.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class SubscriptionStatus(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** In force. The Data API subscription opens in this status. It is metered from the first whole day, in UTC, after it opens, so the day it opens is not billed, and each billing period, which runs from the day of the month it opened to the same day of the next, is invoiced at that period's end. */
            public val Active: SubscriptionStatus = SubscriptionStatus("active")

            /** On a trial, and in force for as long as it runs. No operation in this contract opens one, so it is read only on a subscription agreed separately. */
            public val Trialing: SubscriptionStatus = SubscriptionStatus("trialing")

            /** A payment failed and the retry window, at most 7 days before cancellation, is still open. */
            public val PastDue: SubscriptionStatus = SubscriptionStatus("past-due")

            /** Canceled. */
            public val Canceled: SubscriptionStatus = SubscriptionStatus("canceled")

            /** An invoice is still unpaid after every retry. */
            public val Unpaid: SubscriptionStatus = SubscriptionStatus("unpaid")

            /** The first payment has not succeeded. */
            public val Incomplete: SubscriptionStatus = SubscriptionStatus("incomplete")

            /** The first payment was not completed in time and the subscription expired. */
            public val IncompleteExpired: SubscriptionStatus = SubscriptionStatus("incomplete-expired")

            /** Paused, and billing nothing for as long as it stays so. */
            public val Paused: SubscriptionStatus = SubscriptionStatus("paused")
        }
    }

    /**
     * A tax identifier held against the organization's billing account, such as an
     * EU VAT number. It is returned as it was registered: the value is what the
     * organization entered, and the type says which register that value belongs to.
     */
    @kotlinx.serialization.Serializable
    public class TaxId private constructor(
        /**
         * Which register the value belongs to, `eu_vat` for an EU VAT number. When
         * an update supplies a value and no type, the type is `eu_vat` rather than
         * a refusal. The vocabulary is the payment provider's
         * rather than this contract's, so what is declared here is the same length
         * ceiling the update parameter declares.
         */
        public val type: kotlin.String,
        /**
         * The registration number, for example a VAT number. The same ceiling the
         * update parameter declares, because it is the same value read back.
         */
        public val value: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.TaxId &&
                other.type == this.type &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.type.hashCode()
            hash = 31 * hash + this.value.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TaxId(" +
                "type=" + this.type +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.TaxId], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.TaxId]. */
        public companion object {
            /** Returns a [AdminSchema.TaxId] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                type: kotlin.String,
                value: kotlin.String,
                optional: AdminSchema.TaxId.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.TaxId {
                val given = AdminSchema.TaxId.Builder().apply(optional)
                return AdminSchema.TaxId(
                    type = type,
                    value = value,
                )
            }
        }
    }

    /**
     * One day's data usage, for one operation, on one surface, on one metric, in
     * one status class. A day on which an operation both succeeded and failed yields one
     * point per class rather than one total, so availability is read from the same
     * payload as the volume that counts, which is the `requests` figures of class
     * `2xx` alone, one per root field answered without an error, what a monthly
     * allowance counts and what a self-serve organization is invoiced for (the
     * per-minute limit counts HTTP requests instead): every other metric measures work inside a
     * request and is reported, not counted. A failed request records `requests`
     * alone: the other metrics count work that did not happen. Only the `api` and
     * `mcp` surfaces count; `studio-online` and `studio-embedded` are reported and
     * never counted.
     */
    @kotlinx.serialization.Serializable
    public class UsageDataPoint private constructor(
        /** The day the figure covers, as a UTC calendar day. */
        public val date: kotlin.String,
        /** Which Data API operation the figure belongs to. */
        public val resource_type: AdminSchema.UsageDataResourceType,
        /**
         * Where the usage was made: a Data API request from one of the
         * organization's own clients, a request through Mountaya's MCP server, or
         * the work Map Studio in Mountaya's web application or an Embedded Studio
         * did, which is reported and never counted.
         */
        public val surface: AdminSchema.UsageDataSurface,
        /** What this figure counts, which decides how to read `value`. */
        public val metric: AdminSchema.UsageMetric,
        /**
         * How the requests ended, so availability is read from the same payload as
         * the volume that counts.
         */
        public val status_class: AdminSchema.UsageStatusClass,
        /** How much of the metric this combination covers. */
        public val value: kotlin.Long,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.UsageDataPoint &&
                other.date == this.date &&
                other.resource_type == this.resource_type &&
                other.surface == this.surface &&
                other.metric == this.metric &&
                other.status_class == this.status_class &&
                other.value == this.value

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.date.hashCode()
            hash = 31 * hash + this.resource_type.hashCode()
            hash = 31 * hash + this.surface.hashCode()
            hash = 31 * hash + this.metric.hashCode()
            hash = 31 * hash + this.status_class.hashCode()
            hash = 31 * hash + this.value.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "UsageDataPoint(" +
                "date=" + this.date +
                ", resource_type=" + this.resource_type +
                ", surface=" + this.surface +
                ", metric=" + this.metric +
                ", status_class=" + this.status_class +
                ", value=" + this.value +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.UsageDataPoint], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.UsageDataPoint]. */
        public companion object {
            /** Returns a [AdminSchema.UsageDataPoint] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                date: kotlin.String,
                resource_type: AdminSchema.UsageDataResourceType,
                surface: AdminSchema.UsageDataSurface,
                metric: AdminSchema.UsageMetric,
                status_class: AdminSchema.UsageStatusClass,
                value: kotlin.Long,
                optional: AdminSchema.UsageDataPoint.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.UsageDataPoint {
                val given = AdminSchema.UsageDataPoint.Builder().apply(optional)
                return AdminSchema.UsageDataPoint(
                    date = date,
                    resource_type = resource_type,
                    surface = surface,
                    metric = metric,
                    status_class = status_class,
                    value = value,
                )
            }
        }
    }

    /**
     * Which Data API operation a usage figure belongs to, as the usage report
     * groups it. Each value is the Data API field it counts, spelled in kebab
     * case as every enum published here is: lowercase, with a hyphen before each
     * capital, so the field `reverseGeocode` is reported as `reverse-geocode` and
     * `terrainProfile` as `terrain-profile`.
     *
     * Every figure names the operation its request asked for, a request refused
     * or failed before any operation ran included: such a request is counted once
     * under each root field its document names, at most four, the most one
     * operation can carry, as a `requests` figure. A root field its document
     * leaves out with `@skip(if: true)` or `@include(if: false)` written as a
     * literal names nothing; one whose condition is a variable is counted. The
     * refusals so counted are three. A refusal in the checks that run before any
     * operation, on the products the organization holds, the key's scope, a
     * missing payment method or the rate limit, and a refusal of a document that
     * does not validate against the schema are named from the body as it was
     * sent, when it is at most 16 KiB and its document at most 1,000 tokens: a
     * longer one is not cut short and named, it names nothing. A refusal by the
     * GraphQL protocol of an operation the server parsed and validated is named
     * from that operation, whatever its size: variables that do not match what
     * the operation declares, an operation over the complexity ceiling or nested
     * deeper than the server admits, and an enum value spelled in another case.
     * The variables refusal is a `422`, or a `400` under
     * `application/graphql-response+json`; the others arrive in a `200` and are
     * counted as a `4xx`, since nothing was served. A request that names no
     * operation is counted nowhere: a body over 32 MiB, the `413`; a request
     * asking to upgrade its connection, a body not sent as `application/json` or
     * one that does not decode as a JSON object, the `400`; and, for the first two
     * refusals above, a body over 16 KiB, a document that does not parse or holds
     * more than 1,000 tokens, one sent as a hash that is not a SHA-256 or that the
     * server does not hold, one whose operation cannot be selected (an
     * `operationName` it does not hold, or several operations and none named), or
     * one whose root fields are no Data API operation. A persisted-query miss,
     * `PERSISTED_QUERY_NOT_FOUND`, is not counted either: the client sends the
     * document straight after it, and that request is the one counted. Nor is a
     * request refused for its key, its session token or its calling page.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class UsageDataResourceType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Unified search across several targets at once. */
            public val Search: UsageDataResourceType = UsageDataResourceType("search")

            /** Forward geocoding, from a place name to coordinates. */
            public val Geocode: UsageDataResourceType = UsageDataResourceType("geocode")

            /** Reverse geocoding, from coordinates to a place name. */
            public val ReverseGeocode: UsageDataResourceType = UsageDataResourceType("reverse-geocode")

            /** Points of interest. */
            public val Pois: UsageDataResourceType = UsageDataResourceType("pois")

            /** Directions between waypoints. */
            public val Directions: UsageDataResourceType = UsageDataResourceType("directions")

            /** Route suggestions. */
            public val Suggestions: UsageDataResourceType = UsageDataResourceType("suggestions")

            /** Time-distance matrix. */
            public val Matrix: UsageDataResourceType = UsageDataResourceType("matrix")

            /** Isochrones, the polygons reachable within a budget. */
            public val Isochrones: UsageDataResourceType = UsageDataResourceType("isochrones")

            /** Geometry analysis, which reads an elevation and surface profile off a line you supply. */
            public val AnalyzeGeometry: UsageDataResourceType = UsageDataResourceType("analyze-geometry")

            /** Elevation read at the points you name. */
            public val TerrainSamples: UsageDataResourceType = UsageDataResourceType("terrain-samples")

            /** Elevation sampled along a line, as a profile. */
            public val TerrainProfile: UsageDataResourceType = UsageDataResourceType("terrain-profile")

            /** Elevation sampled over a bounding box, as a grid. */
            public val TerrainGrid: UsageDataResourceType = UsageDataResourceType("terrain-grid")

            /** One itinerary read in full. */
            public val Itinerary: UsageDataResourceType = UsageDataResourceType("itinerary")

            /** One itinerary rendered into a file format. */
            public val ExportItinerary: UsageDataResourceType = UsageDataResourceType("export-itinerary")
        }
    }

    /**
     * Where a day's data usage was made. Only `api` and `mcp`, the organization's
     * own clients, count toward the Data API's per-minute limit, its monthly
     * allowance and its invoice. `studio-online` and `studio-embedded` are
     * Mountaya's own editors at work for the organization: they are recorded so
     * the organization can see them and are never counted, held instead to the
     * bounds the Data API states for them. A request made with a session token
     * minted for `studio-embedded` is `studio-embedded` only while the
     * organization holds Embedded Studio, and is recorded and counted as `api`
     * while it does not.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class UsageDataSurface(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** A request to the Data API under one of the organization's keys, with a session token minted for the `api` surface. Counted. */
            public val Api: UsageDataSurface = UsageDataSurface("api")

            /** A request made through Mountaya's MCP server on the organization's behalf. Counted. */
            public val Mcp: UsageDataSurface = UsageDataSurface("mcp")

            /** The work Map Studio in Mountaya's web application did for a person signed in to it, recorded under the organization the studio had open when that person is a member of it: the routing a Map Studio write made (a routed segment, an import preview and confirm, or a zone computed from a source) and the studio's own Data API requests. Never counted. */
            public val StudioOnline: UsageDataSurface = UsageDataSurface("studio-online")

            /** The work an Embedded Studio did for the organization under one of its keys: the routing a Map Studio write made (a routed segment, an import preview and confirm, or a zone computed from a source), and every Data API request made with a session token minted for the `studio-embedded` surface while the organization holds Embedded Studio. Never counted. */
            public val StudioEmbedded: UsageDataSurface = UsageDataSurface("studio-embedded")
        }
    }

    /**
     * What a Data API usage figure counts. `requests` is recorded for every call
     * that gets past the credential check, whatever it asked for; the rest are the
     * work one kind of call did, so a metric only appears beside the resource type
     * that can emit it, and each member below names the resource types that do. A
     * refused request records `requests` alone, because the work the other metrics
     * count never happened.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class UsageMetric(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Calls made, one per root field a request asks, so a request asking two records two. Every resource type records it. */
            public val Requests: UsageMetric = UsageMetric("requests")

            /** Results handed back to the caller, recorded by `search`, `geocode`, `reverse-geocode` and `pois`. */
            public val ResultsReturned: UsageMetric = UsageMetric("results-returned")

            /** Search targets that ran, which is fewer than the targets asked for when an earlier one already answered. Recorded by `search` alone. */
            public val TargetsExecuted: UsageMetric = UsageMetric("targets-executed")

            /** Routes computed, recorded by `directions` and `suggestions`. */
            public val Routes: UsageMetric = UsageMetric("routes")

            /** Waypoints supplied to a `directions` call, recorded by `directions` alone. */
            public val Waypoints: UsageMetric = UsageMetric("waypoints")

            /** Coordinates supplied to a geometry analysis, recorded by `analyze-geometry`. It is also accepted beside `directions`, where it reports the coordinates recorded under that resource type. */
            public val Coordinates: UsageMetric = UsageMetric("coordinates")

            /** Matrix cells computed, recorded by `matrix` alone. */
            public val Elements: UsageMetric = UsageMetric("elements")

            /** Isochrone polygons computed, recorded by `isochrones` alone. */
            public val Polygons: UsageMetric = UsageMetric("polygons")

            /** Elevation readings taken, which measures the work a terrain query did rather than the one request that asked for them; reported, not counted, since the Data API is counted and invoiced per call, the `requests` metric. Recorded by `terrain-samples`, `terrain-profile` and `terrain-grid`. */
            public val Samples: UsageMetric = UsageMetric("samples")
        }
    }

    /**
     * The status class a request was counted under, and the dimension availability
     * is read from. It is derived from the terminal error a request produced rather
     * than from its status line: a Data API query whose field fails is transported
     * as `200` with an `errors` array, and it is classified here from that entry's
     * `extensions.code`, so a failure upstream of the answer reads `5xx` on both
     * surfaces. Availability is `2xx / (2xx + 5xx)`; `4xx` is excluded, because a
     * malformed request is not an outage. A request whose caller went away before
     * its answer began is counted under no class, however long it had waited: a
     * request too slow is the server's to fail, at its own time budget, as the
     * `504` counted `5xx`.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class UsageStatusClass(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The request succeeded. It is what a monthly allowance counts on the counted surfaces (`api` and `mcp` on the Data API, `api` and `embedding` on the Tile API), and on the Data API what a self-serve organization is invoiced for. */
            public val Class2xx: UsageStatusClass = UsageStatusClass("2xx")

            /** The request was refused for a reason the caller owns: invalid input, a key outside its scope, an exhausted allowance, or a missing payment method. Not counted against availability. */
            public val Class4xx: UsageStatusClass = UsageStatusClass("4xx")

            /** The request failed inside Mountaya or inside something Mountaya depends on. Counted against availability. */
            public val Class5xx: UsageStatusClass = UsageStatusClass("5xx")
        }
    }

    /**
     * One day's Tile API usage, for one kind of resource, from one surface, in one
     * status class. A day on which a resource was served from both the embed and
     * the API, or both succeeded and failed, yields one point per combination
     * rather than one total: a client that wants the combined figure adds them,
     * and a client that wants what the monthly allowance counts reads the `2xx`
     * points of the `api` and `embedding` surfaces, the only ones counted.
     */
    @kotlinx.serialization.Serializable
    public class UsageTilePoint private constructor(
        /** The day the figure covers, as a UTC calendar day. */
        public val date: kotlin.String,
        /** What the requests asked for. */
        public val resource_type: AdminSchema.UsageTileResourceType,
        /** Where the requests came from. */
        public val surface: AdminSchema.UsageTileSurface,
        /**
         * How the requests ended, so availability is read from the same payload as
         * the volume the monthly allowance counts.
         */
        public val status_class: AdminSchema.UsageStatusClass,
        /** How many requests this combination covers. */
        public val requests: kotlin.Long,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.UsageTilePoint &&
                other.date == this.date &&
                other.resource_type == this.resource_type &&
                other.surface == this.surface &&
                other.status_class == this.status_class &&
                other.requests == this.requests

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.date.hashCode()
            hash = 31 * hash + this.resource_type.hashCode()
            hash = 31 * hash + this.surface.hashCode()
            hash = 31 * hash + this.status_class.hashCode()
            hash = 31 * hash + this.requests.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "UsageTilePoint(" +
                "date=" + this.date +
                ", resource_type=" + this.resource_type +
                ", surface=" + this.surface +
                ", status_class=" + this.status_class +
                ", requests=" + this.requests +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.UsageTilePoint], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.UsageTilePoint]. */
        public companion object {
            /** Returns a [AdminSchema.UsageTilePoint] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                date: kotlin.String,
                resource_type: AdminSchema.UsageTileResourceType,
                surface: AdminSchema.UsageTileSurface,
                status_class: AdminSchema.UsageStatusClass,
                requests: kotlin.Long,
                optional: AdminSchema.UsageTilePoint.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.UsageTilePoint {
                val given = AdminSchema.UsageTilePoint.Builder().apply(optional)
                return AdminSchema.UsageTilePoint(
                    date = date,
                    resource_type = resource_type,
                    surface = surface,
                    status_class = status_class,
                    requests = requests,
                )
            }
        }
    }

    /**
     * What a Tile API request asked for, as the usage report groups it. The values
     * follow the tile routes, which is why two of them are plural: `itineraries` is
     * the aggregate layer covering every itinerary, where `itinerary` is one
     * itinerary's own geometry.
     *
     * Every figure names the resource its request asked for, a request refused or
     * failed in the checks before its route ran included, on the products the
     * organization's agreement grants, the key's scope or the rate limit: it is
     * recorded under the resource its address names. A request that names none is
     * counted nowhere: an address no route serves, an identifier that does not
     * parse, and an overlay or a terrain the catalog does not list. Nor is a
     * request refused for its key, its session token or its calling page.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class UsageTileResourceType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Tiles for ONE itinerary's geometry, counted from getItineraryTile. */
            public val Itinerary: UsageTileResourceType = UsageTileResourceType("itinerary")

            /** Tiles for the aggregate layer covering EVERY itinerary the caller reaches, counted from getItinerariesTile. */
            public val Itineraries: UsageTileResourceType = UsageTileResourceType("itineraries")

            /** Zone tiles. */
            public val Zones: UsageTileResourceType = UsageTileResourceType("zones")

            /** Overlay tiles, such as hillshade or snow depth. */
            public val Overlay: UsageTileResourceType = UsageTileResourceType("overlay")

            /** Tiles for the contents of one collection. */
            public val Collection: UsageTileResourceType = UsageTileResourceType("collection")

            /** The base map, served through the Tile API: a terrain's style, TileJSON documents and tiles, the tiles of the sources its style draws, its sprites and glyphs, and the elevation raster. Recorded so the organization sees what its maps draw, and never counted toward its per-minute limit or its monthly allowance, whatever surface the request comes from. A base-map request refused, or failed, in the checks before its route ran is recorded here too, unless it names a terrain the catalog does not list, which is counted nowhere. */
            public val Terrain: UsageTileResourceType = UsageTileResourceType("terrain")
        }
    }

    /**
     * Which surface a tile request arrived from. `api`, the organization's own
     * clients, and `embedding`, the Mountaya-hosted embed drawing under one of its
     * keys, count toward the Tile API's per-minute limit and its monthly
     * allowance, recorded apart so the organization sees each. The others are
     * Mountaya's own surfaces drawing for the organization: they are recorded so
     * the organization can see them and are never counted, held instead to the
     * bounds the Tile API states for each.
     *
     * A tile drawn through a share link is `share`, whatever page asked for it.
     * Under a key, a tile drawn with a session token minted for the
     * `studio-embedded` surface is `studio-embedded` while the organization holds
     * Embedded Studio, and `api` while it does not; one drawn inside the
     * Mountaya-hosted embed is `embedding`, and anything else presenting the key
     * is `api`. A tile drawn by Map Studio in Mountaya's web application, for a
     * person signed in to it, is `studio-online`.
     *
     * It is one of two vocabularies spelled `surface` on this contract, and the
     * one the caller does not choose: the server decides it from where the request
     * arrived. `ReadSurface`, the `?surface=` query parameter, names which snapshot
     * a read returns.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class UsageTileSurface(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** A direct Tile API call under one of the organization's keys. Counted. */
            public val Api: UsageTileSurface = UsageTileSurface("api")

            /** A tile drawn by Map Studio in Mountaya's web application, recorded under the organization the studio had open when the person signed in to it is a member of it. Never counted. */
            public val StudioOnline: UsageTileSurface = UsageTileSurface("studio-online")

            /** A tile drawn by an Embedded Studio, with a session token minted for the `studio-embedded` surface, while the organization holds Embedded Studio. Never counted. */
            public val StudioEmbedded: UsageTileSurface = UsageTileSurface("studio-embedded")

            /** A tile drawn by the Mountaya-hosted embed under one of the organization's keys. Counted, as `api` is. */
            public val Embedding: UsageTileSurface = UsageTileSurface("embedding")

            /** A tile drawn through a share link, recorded under the organization that owns the link. Never counted. */
            public val Share: UsageTileSurface = UsageTileSurface("share")
        }
    }

    /**
     * An invitation as the invited person reads it under `/v1/me/invitations`:
     * which organization it is to, what that organization holds, and the role it
     * grants. It is matched to the person by an address their account has
     * verified, so an invitation to an address the account has not verified is
     * not listed.
     */
    @kotlinx.serialization.Serializable
    public class UserInvitation private constructor(
        /**
         * Identifier of the invitation, which `postMyInvitationAccept` and
         * `deleteMyInvitation` take.
         */
        public val id: kotlin.String,
        /** Identifier of the organization the invitation is to. */
        public val organization_id: kotlin.String,
        /** The organization's display name, as it appears to its members. */
        public val organization_name: kotlin.String,
        /**
         * The products the organization holds, as `Capability` members, each at
         * most once and sorted alphabetically, so a person sees what an
         * organization offers before answering. Empty when it holds none.
         */
        public val organization_capabilities: kotlin.collections.List<AdminSchema.Capability>,
        /** The role accepting grants. */
        public val role: AdminSchema.AssignableOrganizationRole,
        /** When the invitation was made. */
        public val created_at: kotlin.time.Instant,
        /** When the invitation lapses unanswered, thirty days after it was made. */
        public val expires_at: kotlin.time.Instant,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.UserInvitation &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.organization_name == this.organization_name &&
                other.organization_capabilities == this.organization_capabilities &&
                other.role == this.role &&
                other.created_at == this.created_at &&
                other.expires_at == this.expires_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.organization_name.hashCode()
            hash = 31 * hash + this.organization_capabilities.hashCode()
            hash = 31 * hash + this.role.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.expires_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "UserInvitation(" +
                "id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", organization_name=" + this.organization_name +
                ", organization_capabilities=" + this.organization_capabilities +
                ", role=" + this.role +
                ", created_at=" + this.created_at +
                ", expires_at=" + this.expires_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.UserInvitation], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.UserInvitation]. */
        public companion object {
            /** Returns a [AdminSchema.UserInvitation] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                organization_name: kotlin.String,
                organization_capabilities: kotlin.collections.List<AdminSchema.Capability>,
                role: AdminSchema.AssignableOrganizationRole,
                created_at: kotlin.time.Instant,
                expires_at: kotlin.time.Instant,
                optional: AdminSchema.UserInvitation.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.UserInvitation {
                val given = AdminSchema.UserInvitation.Builder().apply(optional)
                return AdminSchema.UserInvitation(
                    id = id,
                    organization_id = organization_id,
                    organization_name = organization_name,
                    organization_capabilities = organization_capabilities,
                    role = role,
                    created_at = created_at,
                    expires_at = expires_at,
                )
            }
        }
    }

    /**
     * How one attempt of a delivery ended.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookAttemptOutcome(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The endpoint answered a `2xx` within 10 seconds, or the email was sent. */
            public val Succeeded: WebhookAttemptOutcome = WebhookAttemptOutcome("succeeded")

            /** The attempt failed. `failure_kind` says how and `failure_class` whose the failure is. */
            public val Failed: WebhookAttemptOutcome = WebhookAttemptOutcome("failed")

            /** The attempt was not made: its endpoint was disabled or deleted first, no delivery slot came free before its schedule ended, or the recipient of a notification email stopped receiving it before it was sent. */
            public val Canceled: WebhookAttemptOutcome = WebhookAttemptOutcome("canceled")
        }
    }

    /**
     * One delivery of a webhook event to one destination: one of the
     * organization's endpoints, or a notification email to one person. It
     * carries where it stands and its last attempt; `getOrganizationWebhookDelivery`
     * reads every attempt.
     */
    @kotlinx.serialization.Serializable
    public class WebhookDelivery private constructor(
        /** Identifier of the delivery. */
        public val id: kotlin.String,
        /**
         * Identifier of the event delivered, which `getOrganizationWebhookEvent`
         * reads.
         */
        public val event_id: kotlin.String,
        /** The type of the event delivered. */
        public val event_type: AdminSchema.WebhookEventType,
        /** Where the delivery goes. */
        public val destination: AdminSchema.WebhookDestinationKind,
        /** Identifier of the endpoint, on a delivery to an endpoint. */
        public val webhook_endpoint_id: kotlin.String? = null,
        /** The person the email went to, on a notification email. */
        public val recipient: AdminSchema.WebhookDeliveryRecipient? = null,
        /** Which notification the email was, on a notification email. */
        public val notification: AdminSchema.NotificationType? = null,
        /** What started the delivery. */
        public val trigger: AdminSchema.WebhookDeliveryTriggerKind,
        /**
         * Identifier of the resend, test or redelivery that started the delivery,
         * the `id` `postOrganizationWebhookDelivery` answered. Absent when the event
         * itself started it.
         */
        public val trigger_id: kotlin.String? = null,
        /** Where the delivery stands. */
        public val outcome: AdminSchema.WebhookDeliveryOutcome,
        /** How many attempts were made, the canceled ones left out. */
        public val attempt_count: kotlin.Int,
        /** When the last attempt was made. Absent before the first. */
        public val last_attempt_at: kotlin.time.Instant? = null,
        /**
         * When the next attempt is due, null when none is: the delivery succeeded,
         * was exhausted or canceled.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val next_attempt_at: kotlin.time.Instant? = null,
        /** How the last attempt failed, present while that attempt is a failure. */
        public val failure_kind: AdminSchema.WebhookFailureKind? = null,
        /** Whose the last attempt's failure is, present beside `failure_kind`. */
        public val failure_class: AdminSchema.WebhookFailureClass? = null,
        /**
         * The HTTP status the endpoint answered the last attempt with, on a
         * delivery to an endpoint that answered at all.
         */
        public val response_status: kotlin.Int? = null,
        /** When the delivery was started. */
        public val created_at: kotlin.time.Instant,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookDelivery &&
                other.id == this.id &&
                other.event_id == this.event_id &&
                other.event_type == this.event_type &&
                other.destination == this.destination &&
                other.webhook_endpoint_id == this.webhook_endpoint_id &&
                other.recipient == this.recipient &&
                other.notification == this.notification &&
                other.trigger == this.trigger &&
                other.trigger_id == this.trigger_id &&
                other.outcome == this.outcome &&
                other.attempt_count == this.attempt_count &&
                other.last_attempt_at == this.last_attempt_at &&
                other.next_attempt_at == this.next_attempt_at &&
                other.failure_kind == this.failure_kind &&
                other.failure_class == this.failure_class &&
                other.response_status == this.response_status &&
                other.created_at == this.created_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.event_id.hashCode()
            hash = 31 * hash + this.event_type.hashCode()
            hash = 31 * hash + this.destination.hashCode()
            hash = 31 * hash + (this.webhook_endpoint_id?.hashCode() ?: 0)
            hash = 31 * hash + (this.recipient?.hashCode() ?: 0)
            hash = 31 * hash + (this.notification?.hashCode() ?: 0)
            hash = 31 * hash + this.trigger.hashCode()
            hash = 31 * hash + (this.trigger_id?.hashCode() ?: 0)
            hash = 31 * hash + this.outcome.hashCode()
            hash = 31 * hash + this.attempt_count.hashCode()
            hash = 31 * hash + (this.last_attempt_at?.hashCode() ?: 0)
            hash = 31 * hash + (this.next_attempt_at?.hashCode() ?: 0)
            hash = 31 * hash + (this.failure_kind?.hashCode() ?: 0)
            hash = 31 * hash + (this.failure_class?.hashCode() ?: 0)
            hash = 31 * hash + (this.response_status?.hashCode() ?: 0)
            hash = 31 * hash + this.created_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookDelivery(" +
                "id=" + this.id +
                ", event_id=" + this.event_id +
                ", event_type=" + this.event_type +
                ", destination=" + this.destination +
                ", webhook_endpoint_id=" + this.webhook_endpoint_id +
                ", recipient=" + this.recipient +
                ", notification=" + this.notification +
                ", trigger=" + this.trigger +
                ", trigger_id=" + this.trigger_id +
                ", outcome=" + this.outcome +
                ", attempt_count=" + this.attempt_count +
                ", last_attempt_at=" + this.last_attempt_at +
                ", next_attempt_at=" + this.next_attempt_at +
                ", failure_kind=" + this.failure_kind +
                ", failure_class=" + this.failure_class +
                ", response_status=" + this.response_status +
                ", created_at=" + this.created_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookDelivery], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot4: kotlin.String? = null
            internal var slot5: AdminSchema.WebhookDeliveryRecipient? = null
            internal var slot6: AdminSchema.NotificationType? = null
            internal var slot8: kotlin.String? = null
            internal var slot11: kotlin.time.Instant? = null
            internal var slot13: AdminSchema.WebhookFailureKind? = null
            internal var slot14: AdminSchema.WebhookFailureClass? = null
            internal var slot15: kotlin.Int? = null

            /** Identifier of the endpoint, on a delivery to an endpoint. */
            public var webhook_endpoint_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** The person the email went to, on a notification email. */
            public var recipient: AdminSchema.WebhookDeliveryRecipient
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /** Which notification the email was, on a notification email. */
            public var notification: AdminSchema.NotificationType
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Identifier of the resend, test or redelivery that started the delivery,
             * the `id` `postOrganizationWebhookDelivery` answered. Absent when the event
             * itself started it.
             */
            public var trigger_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /** When the last attempt was made. Absent before the first. */
            public var last_attempt_at: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot11 = value
                }

            /** How the last attempt failed, present while that attempt is a failure. */
            public var failure_kind: AdminSchema.WebhookFailureKind
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot13 = value
                }

            /** Whose the last attempt's failure is, present beside `failure_kind`. */
            public var failure_class: AdminSchema.WebhookFailureClass
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot14 = value
                }

            /**
             * The HTTP status the endpoint answered the last attempt with, on a
             * delivery to an endpoint that answered at all.
             */
            public var response_status: kotlin.Int
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot15 = value
                }
        }

        /** Builds a [AdminSchema.WebhookDelivery]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookDelivery] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                event_id: kotlin.String,
                event_type: AdminSchema.WebhookEventType,
                destination: AdminSchema.WebhookDestinationKind,
                trigger: AdminSchema.WebhookDeliveryTriggerKind,
                outcome: AdminSchema.WebhookDeliveryOutcome,
                attempt_count: kotlin.Int,
                next_attempt_at: kotlin.time.Instant?,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.WebhookDelivery.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookDelivery {
                val given = AdminSchema.WebhookDelivery.Builder().apply(optional)
                return AdminSchema.WebhookDelivery(
                    id = id,
                    event_id = event_id,
                    event_type = event_type,
                    destination = destination,
                    webhook_endpoint_id = given.slot4,
                    recipient = given.slot5,
                    notification = given.slot6,
                    trigger = trigger,
                    trigger_id = given.slot8,
                    outcome = outcome,
                    attempt_count = attempt_count,
                    last_attempt_at = given.slot11,
                    next_attempt_at = next_attempt_at,
                    failure_kind = given.slot13,
                    failure_class = given.slot14,
                    response_status = given.slot15,
                    created_at = created_at,
                )
            }
        }
    }

    /**
     * One attempt of a delivery: when it was due and made, how it ended, and
     * what the endpoint answered. Nothing an attempt records names how Mountaya
     * made it.
     */
    @kotlinx.serialization.Serializable
    public class WebhookDeliveryAttempt private constructor(
        /**
         * Which attempt it is, from 1, the `Mountaya-Delivery-Attempt` it was
         * sent with.
         */
        public val attempt: kotlin.Int,
        /** How the attempt ended. */
        public val outcome: AdminSchema.WebhookAttemptOutcome,
        /** How the attempt failed, present on a failed one. */
        public val failure_kind: AdminSchema.WebhookFailureKind? = null,
        /** Whose the failure is, present on a failed attempt. */
        public val failure_class: AdminSchema.WebhookFailureClass? = null,
        /**
         * When the delivery schedule made the attempt due, a receiver's
         * `Retry-After` included.
         */
        public val scheduled_at: kotlin.time.Instant,
        /** When the attempt was made, absent on one canceled before it was. */
        public val attempted_at: kotlin.time.Instant? = null,
        /**
         * How long the endpoint took to answer, in milliseconds, on an attempt
         * that reached it.
         */
        public val duration_ms: kotlin.Int? = null,
        /** The HTTP status the endpoint answered, on an attempt it answered. */
        public val response_status: kotlin.Int? = null,
        /**
         * The first 2 KiB of the endpoint's answer, as it sent them, on an attempt
         * it answered with a body. A notification email's attempt carries none.
         */
        public val response_body: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookDeliveryAttempt &&
                other.attempt == this.attempt &&
                other.outcome == this.outcome &&
                other.failure_kind == this.failure_kind &&
                other.failure_class == this.failure_class &&
                other.scheduled_at == this.scheduled_at &&
                other.attempted_at == this.attempted_at &&
                other.duration_ms == this.duration_ms &&
                other.response_status == this.response_status &&
                other.response_body == this.response_body

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.attempt.hashCode()
            hash = 31 * hash + this.outcome.hashCode()
            hash = 31 * hash + (this.failure_kind?.hashCode() ?: 0)
            hash = 31 * hash + (this.failure_class?.hashCode() ?: 0)
            hash = 31 * hash + this.scheduled_at.hashCode()
            hash = 31 * hash + (this.attempted_at?.hashCode() ?: 0)
            hash = 31 * hash + (this.duration_ms?.hashCode() ?: 0)
            hash = 31 * hash + (this.response_status?.hashCode() ?: 0)
            hash = 31 * hash + (this.response_body?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookDeliveryAttempt(" +
                "attempt=" + this.attempt +
                ", outcome=" + this.outcome +
                ", failure_kind=" + this.failure_kind +
                ", failure_class=" + this.failure_class +
                ", scheduled_at=" + this.scheduled_at +
                ", attempted_at=" + this.attempted_at +
                ", duration_ms=" + this.duration_ms +
                ", response_status=" + this.response_status +
                ", response_body=" + this.response_body +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookDeliveryAttempt], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot2: AdminSchema.WebhookFailureKind? = null
            internal var slot3: AdminSchema.WebhookFailureClass? = null
            internal var slot5: kotlin.time.Instant? = null
            internal var slot6: kotlin.Int? = null
            internal var slot7: kotlin.Int? = null
            internal var slot8: kotlin.String? = null

            /** How the attempt failed, present on a failed one. */
            public var failure_kind: AdminSchema.WebhookFailureKind
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /** Whose the failure is, present on a failed attempt. */
            public var failure_class: AdminSchema.WebhookFailureClass
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** When the attempt was made, absent on one canceled before it was. */
            public var attempted_at: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /**
             * How long the endpoint took to answer, in milliseconds, on an attempt
             * that reached it.
             */
            public var duration_ms: kotlin.Int
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /** The HTTP status the endpoint answered, on an attempt it answered. */
            public var response_status: kotlin.Int
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot7 = value
                }

            /**
             * The first 2 KiB of the endpoint's answer, as it sent them, on an attempt
             * it answered with a body. A notification email's attempt carries none.
             */
            public var response_body: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }
        }

        /** Builds a [AdminSchema.WebhookDeliveryAttempt]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookDeliveryAttempt] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                attempt: kotlin.Int,
                outcome: AdminSchema.WebhookAttemptOutcome,
                scheduled_at: kotlin.time.Instant,
                optional: AdminSchema.WebhookDeliveryAttempt.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookDeliveryAttempt {
                val given = AdminSchema.WebhookDeliveryAttempt.Builder().apply(optional)
                return AdminSchema.WebhookDeliveryAttempt(
                    attempt = attempt,
                    outcome = outcome,
                    failure_kind = given.slot2,
                    failure_class = given.slot3,
                    scheduled_at = scheduled_at,
                    attempted_at = given.slot5,
                    duration_ms = given.slot6,
                    response_status = given.slot7,
                    response_body = given.slot8,
                )
            }
        }
    }

    /**
     * What to deliver to one of the organization's endpoints, beside
     * `webhook_endpoint_id`: exactly one of `event_id`, a resend of one event;
     * `test`, a test event; or `since`, a redelivery of every event the endpoint
     * was sent from an instant on. None of them, or more than one, is a `400`.
     */
    @kotlinx.serialization.Serializable
    public class WebhookDeliveryCreateParams private constructor(
        /**
         * Identifier of the endpoint to deliver to. A disabled one is a `409`:
         * enable it first.
         */
        public val webhook_endpoint_id: kotlin.String,
        /**
         * A resend: the event to deliver again, recorded in the last 30 days,
         * whether or not the endpoint was sent it the first time or its
         * `enabled_events` names its type. It starts one delivery, its own
         * attempts on the delivery schedule.
         */
        public val event_id: kotlin.String? = null,
        /** A test: a test event of the type named, delivered once without retries. */
        public val test: AdminSchema.WebhookDeliveryTestParams? = null,
        /**
         * A redelivery: every event recorded from this instant on, at most 30 days
         * ago, whose type the endpoint's `enabled_events` names, delivered again
         * oldest first at a paced rate, each its own delivery carrying the
         * redelivery's `trigger_id`. An instant further back, or one still to
         * come, is a `400` naming it.
         */
        public val since: kotlin.time.Instant? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookDeliveryCreateParams &&
                other.webhook_endpoint_id == this.webhook_endpoint_id &&
                other.event_id == this.event_id &&
                other.test == this.test &&
                other.since == this.since

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.webhook_endpoint_id.hashCode()
            hash = 31 * hash + (this.event_id?.hashCode() ?: 0)
            hash = 31 * hash + (this.test?.hashCode() ?: 0)
            hash = 31 * hash + (this.since?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookDeliveryCreateParams(" +
                "webhook_endpoint_id=" + this.webhook_endpoint_id +
                ", event_id=" + this.event_id +
                ", test=" + this.test +
                ", since=" + this.since +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookDeliveryCreateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null
            internal var slot2: AdminSchema.WebhookDeliveryTestParams? = null
            internal var slot3: kotlin.time.Instant? = null

            /**
             * A resend: the event to deliver again, recorded in the last 30 days,
             * whether or not the endpoint was sent it the first time or its
             * `enabled_events` names its type. It starts one delivery, its own
             * attempts on the delivery schedule.
             */
            public var event_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /** A test: a test event of the type named, delivered once without retries. */
            public var test: AdminSchema.WebhookDeliveryTestParams
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * A redelivery: every event recorded from this instant on, at most 30 days
             * ago, whose type the endpoint's `enabled_events` names, delivered again
             * oldest first at a paced rate, each its own delivery carrying the
             * redelivery's `trigger_id`. An instant further back, or one still to
             * come, is a `400` naming it.
             */
            public var since: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [AdminSchema.WebhookDeliveryCreateParams]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookDeliveryCreateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                webhook_endpoint_id: kotlin.String,
                optional: AdminSchema.WebhookDeliveryCreateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookDeliveryCreateParams {
                val given = AdminSchema.WebhookDeliveryCreateParams.Builder().apply(optional)
                return AdminSchema.WebhookDeliveryCreateParams(
                    webhook_endpoint_id = webhook_endpoint_id,
                    event_id = given.slot1,
                    test = given.slot2,
                    since = given.slot3,
                )
            }
        }
    }

    /**
     * Where a delivery stands.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookDeliveryOutcome(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Not attempted yet, or a notification email handed over whose status is not known yet. */
            public val Pending: WebhookDeliveryOutcome = WebhookDeliveryOutcome("pending")

            /** Attempted and failed, with another attempt scheduled. */
            public val Retrying: WebhookDeliveryOutcome = WebhookDeliveryOutcome("retrying")

            /** Delivered: the endpoint answered a `2xx`, or the email was sent. */
            public val Succeeded: WebhookDeliveryOutcome = WebhookDeliveryOutcome("succeeded")

            /** Every attempt the schedule allows failed, and none is left. */
            public val Exhausted: WebhookDeliveryOutcome = WebhookDeliveryOutcome("exhausted")

            /** Stopped before it succeeded: its endpoint was disabled or deleted, no delivery slot came free before its schedule ended, or the recipient of a notification email stopped receiving it before it was sent. */
            public val Canceled: WebhookDeliveryOutcome = WebhookDeliveryOutcome("canceled")
        }
    }

    /**
     * The person a notification email was sent to. It names who received the
     * email and never what it said: the content of a notification is not kept.
     * A member is named by their account, and `email` is the address that account
     * holds when the delivery is read, answered only while the account is still a
     * member of the organization: an address is never kept beyond the account it
     * belongs to, and never answered once that person has left. An invitation's
     * address, which the organization typed, is kept with the delivery. Either way
     * at least one of the two is present.
     */
    @kotlinx.serialization.Serializable
    public class WebhookDeliveryRecipient private constructor(
        /**
         * Identifier of the recipient's account. Absent for an invitation, which
         * is sent to the address invited whatever account it belongs to, so the
         * delivery never tells whether one does.
         */
        public val user_id: kotlin.String? = null,
        /**
         * The address of the invitation the email was sent for, or the address the
         * recipient's account holds when the delivery is read. Absent beside a
         * `user_id` when that account has left the organization or been deleted,
         * or when its address could not be read just then: the delivery is
         * answered all the same, and a later read may carry it.
         */
        public val email: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookDeliveryRecipient &&
                other.user_id == this.user_id &&
                other.email == this.email

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.user_id?.hashCode() ?: 0)
            hash = 31 * hash + (this.email?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookDeliveryRecipient(" +
                "user_id=" + this.user_id +
                ", email=" + this.email +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookDeliveryRecipient], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot1: kotlin.String? = null

            /**
             * Identifier of the recipient's account. Absent for an invitation, which
             * is sent to the address invited whatever account it belongs to, so the
             * delivery never tells whether one does.
             */
            public var user_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * The address of the invitation the email was sent for, or the address the
             * recipient's account holds when the delivery is read. Absent beside a
             * `user_id` when that account has left the organization or been deleted,
             * or when its address could not be read just then: the delivery is
             * answered all the same, and a later read may carry it.
             */
            public var email: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [AdminSchema.WebhookDeliveryRecipient]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookDeliveryRecipient] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.WebhookDeliveryRecipient.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookDeliveryRecipient {
                val given = AdminSchema.WebhookDeliveryRecipient.Builder().apply(optional)
                return AdminSchema.WebhookDeliveryRecipient(
                    user_id = given.slot0,
                    email = given.slot1,
                )
            }
        }
    }

    /** A test event to send an endpoint. */
    @kotlinx.serialization.Serializable
    public class WebhookDeliveryTestParams private constructor(
        /**
         * The type of the test event. Its payload is the example this contract
         * gives for the type, with an `id` of its own, so a receiver can tell a
         * test by the identifiers it carries; it is recorded nowhere else, and
         * listed by no `getOrganizationWebhookEvents`. An endpoint is sent a test
         * of any type, whether or not its `enabled_events` names it.
         */
        public val type: AdminSchema.WebhookEventType,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookDeliveryTestParams &&
                other.type == this.type

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.type.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookDeliveryTestParams(" +
                "type=" + this.type +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookDeliveryTestParams], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookDeliveryTestParams]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookDeliveryTestParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                type: AdminSchema.WebhookEventType,
                optional: AdminSchema.WebhookDeliveryTestParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookDeliveryTestParams {
                val given = AdminSchema.WebhookDeliveryTestParams.Builder().apply(optional)
                return AdminSchema.WebhookDeliveryTestParams(
                    type = type,
                )
            }
        }
    }

    /**
     * A resend, a test or a redelivery `postOrganizationWebhookDelivery` started.
     * The deliveries it starts run after the answer, and each carries its `id` as
     * `trigger_id`, which `getOrganizationWebhookDeliveries` filters on.
     */
    @kotlinx.serialization.Serializable
    public class WebhookDeliveryTrigger private constructor(
        /** Identifier of the resend, test or redelivery. */
        public val id: kotlin.String,
        /** Which of the three it is. */
        public val kind: AdminSchema.WebhookDeliveryTriggerKind,
        /** Identifier of the endpoint it delivers to. */
        public val webhook_endpoint_id: kotlin.String,
        /** The event resent, on a resend. */
        public val event_id: kotlin.String? = null,
        /** The type of the test event, on a test. */
        public val event_type: AdminSchema.WebhookEventType? = null,
        /** The instant the redelivery starts from, on a redelivery. */
        public val since: kotlin.time.Instant? = null,
        /**
         * The delivery a resend or a test started, which
         * `getOrganizationWebhookDelivery` reads. Null on a redelivery, which
         * starts one delivery per event as it goes.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val delivery_id: kotlin.String? = null,
        /** When it was asked for. */
        public val created_at: kotlin.time.Instant,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookDeliveryTrigger &&
                other.id == this.id &&
                other.kind == this.kind &&
                other.webhook_endpoint_id == this.webhook_endpoint_id &&
                other.event_id == this.event_id &&
                other.event_type == this.event_type &&
                other.since == this.since &&
                other.delivery_id == this.delivery_id &&
                other.created_at == this.created_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.kind.hashCode()
            hash = 31 * hash + this.webhook_endpoint_id.hashCode()
            hash = 31 * hash + (this.event_id?.hashCode() ?: 0)
            hash = 31 * hash + (this.event_type?.hashCode() ?: 0)
            hash = 31 * hash + (this.since?.hashCode() ?: 0)
            hash = 31 * hash + (this.delivery_id?.hashCode() ?: 0)
            hash = 31 * hash + this.created_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookDeliveryTrigger(" +
                "id=" + this.id +
                ", kind=" + this.kind +
                ", webhook_endpoint_id=" + this.webhook_endpoint_id +
                ", event_id=" + this.event_id +
                ", event_type=" + this.event_type +
                ", since=" + this.since +
                ", delivery_id=" + this.delivery_id +
                ", created_at=" + this.created_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookDeliveryTrigger], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot3: kotlin.String? = null
            internal var slot4: AdminSchema.WebhookEventType? = null
            internal var slot5: kotlin.time.Instant? = null

            /** The event resent, on a resend. */
            public var event_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }

            /** The type of the test event, on a test. */
            public var event_type: AdminSchema.WebhookEventType
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** The instant the redelivery starts from, on a redelivery. */
            public var since: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [AdminSchema.WebhookDeliveryTrigger]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookDeliveryTrigger] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                kind: AdminSchema.WebhookDeliveryTriggerKind,
                webhook_endpoint_id: kotlin.String,
                delivery_id: kotlin.String?,
                created_at: kotlin.time.Instant,
                optional: AdminSchema.WebhookDeliveryTrigger.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookDeliveryTrigger {
                val given = AdminSchema.WebhookDeliveryTrigger.Builder().apply(optional)
                return AdminSchema.WebhookDeliveryTrigger(
                    id = id,
                    kind = kind,
                    webhook_endpoint_id = webhook_endpoint_id,
                    event_id = given.slot3,
                    event_type = given.slot4,
                    since = given.slot5,
                    delivery_id = delivery_id,
                    created_at = created_at,
                )
            }
        }
    }

    /**
     * What started a delivery.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookDeliveryTriggerKind(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The event itself, delivered to a destination it was recorded for. */
            public val Event: WebhookDeliveryTriggerKind = WebhookDeliveryTriggerKind("event")

            /** A resend of one event to one endpoint, asked for with `postOrganizationWebhookDelivery`. */
            public val Resend: WebhookDeliveryTriggerKind = WebhookDeliveryTriggerKind("resend")

            /** A test event sent to one endpoint, asked for with `postOrganizationWebhookDelivery`. */
            public val Test: WebhookDeliveryTriggerKind = WebhookDeliveryTriggerKind("test")

            /** A redelivery of every event an endpoint was sent since an instant, asked for with `postOrganizationWebhookDelivery`. */
            public val Redelivery: WebhookDeliveryTriggerKind = WebhookDeliveryTriggerKind("redelivery")
        }
    }

    /** A delivery with every attempt made of it, the first one first. */
    @kotlinx.serialization.Serializable
    public class WebhookDeliveryWithAttempts private constructor(
        /** Identifier of the delivery. */
        public val id: kotlin.String,
        /**
         * Identifier of the event delivered, which `getOrganizationWebhookEvent`
         * reads.
         */
        public val event_id: kotlin.String,
        /** The type of the event delivered. */
        public val event_type: AdminSchema.WebhookEventType,
        /** Where the delivery goes. */
        public val destination: AdminSchema.WebhookDestinationKind,
        /** Identifier of the endpoint, on a delivery to an endpoint. */
        public val webhook_endpoint_id: kotlin.String? = null,
        /** The person the email went to, on a notification email. */
        public val recipient: AdminSchema.WebhookDeliveryRecipient? = null,
        /** Which notification the email was, on a notification email. */
        public val notification: AdminSchema.NotificationType? = null,
        /** What started the delivery. */
        public val trigger: AdminSchema.WebhookDeliveryTriggerKind,
        /**
         * Identifier of the resend, test or redelivery that started the delivery,
         * the `id` `postOrganizationWebhookDelivery` answered. Absent when the event
         * itself started it.
         */
        public val trigger_id: kotlin.String? = null,
        /** Where the delivery stands. */
        public val outcome: AdminSchema.WebhookDeliveryOutcome,
        /** How many attempts were made, the canceled ones left out. */
        public val attempt_count: kotlin.Int,
        /** When the last attempt was made. Absent before the first. */
        public val last_attempt_at: kotlin.time.Instant? = null,
        /**
         * When the next attempt is due, null when none is: the delivery succeeded,
         * was exhausted or canceled.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val next_attempt_at: kotlin.time.Instant? = null,
        /** How the last attempt failed, present while that attempt is a failure. */
        public val failure_kind: AdminSchema.WebhookFailureKind? = null,
        /** Whose the last attempt's failure is, present beside `failure_kind`. */
        public val failure_class: AdminSchema.WebhookFailureClass? = null,
        /**
         * The HTTP status the endpoint answered the last attempt with, on a
         * delivery to an endpoint that answered at all.
         */
        public val response_status: kotlin.Int? = null,
        /** When the delivery was started. */
        public val created_at: kotlin.time.Instant,
        /**
         * Every attempt of the delivery, in the order they were due, a canceled
         * one included. At most one per step of the delivery schedule.
         */
        public val attempts: kotlin.collections.List<AdminSchema.WebhookDeliveryAttempt>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookDeliveryWithAttempts &&
                other.id == this.id &&
                other.event_id == this.event_id &&
                other.event_type == this.event_type &&
                other.destination == this.destination &&
                other.webhook_endpoint_id == this.webhook_endpoint_id &&
                other.recipient == this.recipient &&
                other.notification == this.notification &&
                other.trigger == this.trigger &&
                other.trigger_id == this.trigger_id &&
                other.outcome == this.outcome &&
                other.attempt_count == this.attempt_count &&
                other.last_attempt_at == this.last_attempt_at &&
                other.next_attempt_at == this.next_attempt_at &&
                other.failure_kind == this.failure_kind &&
                other.failure_class == this.failure_class &&
                other.response_status == this.response_status &&
                other.created_at == this.created_at &&
                other.attempts == this.attempts

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.event_id.hashCode()
            hash = 31 * hash + this.event_type.hashCode()
            hash = 31 * hash + this.destination.hashCode()
            hash = 31 * hash + (this.webhook_endpoint_id?.hashCode() ?: 0)
            hash = 31 * hash + (this.recipient?.hashCode() ?: 0)
            hash = 31 * hash + (this.notification?.hashCode() ?: 0)
            hash = 31 * hash + this.trigger.hashCode()
            hash = 31 * hash + (this.trigger_id?.hashCode() ?: 0)
            hash = 31 * hash + this.outcome.hashCode()
            hash = 31 * hash + this.attempt_count.hashCode()
            hash = 31 * hash + (this.last_attempt_at?.hashCode() ?: 0)
            hash = 31 * hash + (this.next_attempt_at?.hashCode() ?: 0)
            hash = 31 * hash + (this.failure_kind?.hashCode() ?: 0)
            hash = 31 * hash + (this.failure_class?.hashCode() ?: 0)
            hash = 31 * hash + (this.response_status?.hashCode() ?: 0)
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.attempts.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookDeliveryWithAttempts(" +
                "id=" + this.id +
                ", event_id=" + this.event_id +
                ", event_type=" + this.event_type +
                ", destination=" + this.destination +
                ", webhook_endpoint_id=" + this.webhook_endpoint_id +
                ", recipient=" + this.recipient +
                ", notification=" + this.notification +
                ", trigger=" + this.trigger +
                ", trigger_id=" + this.trigger_id +
                ", outcome=" + this.outcome +
                ", attempt_count=" + this.attempt_count +
                ", last_attempt_at=" + this.last_attempt_at +
                ", next_attempt_at=" + this.next_attempt_at +
                ", failure_kind=" + this.failure_kind +
                ", failure_class=" + this.failure_class +
                ", response_status=" + this.response_status +
                ", created_at=" + this.created_at +
                ", attempts=" + this.attempts +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookDeliveryWithAttempts], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot4: kotlin.String? = null
            internal var slot5: AdminSchema.WebhookDeliveryRecipient? = null
            internal var slot6: AdminSchema.NotificationType? = null
            internal var slot8: kotlin.String? = null
            internal var slot11: kotlin.time.Instant? = null
            internal var slot13: AdminSchema.WebhookFailureKind? = null
            internal var slot14: AdminSchema.WebhookFailureClass? = null
            internal var slot15: kotlin.Int? = null

            /** Identifier of the endpoint, on a delivery to an endpoint. */
            public var webhook_endpoint_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /** The person the email went to, on a notification email. */
            public var recipient: AdminSchema.WebhookDeliveryRecipient
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }

            /** Which notification the email was, on a notification email. */
            public var notification: AdminSchema.NotificationType
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }

            /**
             * Identifier of the resend, test or redelivery that started the delivery,
             * the `id` `postOrganizationWebhookDelivery` answered. Absent when the event
             * itself started it.
             */
            public var trigger_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot8 = value
                }

            /** When the last attempt was made. Absent before the first. */
            public var last_attempt_at: kotlin.time.Instant
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot11 = value
                }

            /** How the last attempt failed, present while that attempt is a failure. */
            public var failure_kind: AdminSchema.WebhookFailureKind
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot13 = value
                }

            /** Whose the last attempt's failure is, present beside `failure_kind`. */
            public var failure_class: AdminSchema.WebhookFailureClass
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot14 = value
                }

            /**
             * The HTTP status the endpoint answered the last attempt with, on a
             * delivery to an endpoint that answered at all.
             */
            public var response_status: kotlin.Int
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot15 = value
                }
        }

        /** Builds a [AdminSchema.WebhookDeliveryWithAttempts]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookDeliveryWithAttempts] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                event_id: kotlin.String,
                event_type: AdminSchema.WebhookEventType,
                destination: AdminSchema.WebhookDestinationKind,
                trigger: AdminSchema.WebhookDeliveryTriggerKind,
                outcome: AdminSchema.WebhookDeliveryOutcome,
                attempt_count: kotlin.Int,
                next_attempt_at: kotlin.time.Instant?,
                created_at: kotlin.time.Instant,
                attempts: kotlin.collections.List<AdminSchema.WebhookDeliveryAttempt>,
                optional: AdminSchema.WebhookDeliveryWithAttempts.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookDeliveryWithAttempts {
                val given = AdminSchema.WebhookDeliveryWithAttempts.Builder().apply(optional)
                return AdminSchema.WebhookDeliveryWithAttempts(
                    id = id,
                    event_id = event_id,
                    event_type = event_type,
                    destination = destination,
                    webhook_endpoint_id = given.slot4,
                    recipient = given.slot5,
                    notification = given.slot6,
                    trigger = trigger,
                    trigger_id = given.slot8,
                    outcome = outcome,
                    attempt_count = attempt_count,
                    last_attempt_at = given.slot11,
                    next_attempt_at = next_attempt_at,
                    failure_kind = given.slot13,
                    failure_class = given.slot14,
                    response_status = given.slot15,
                    created_at = created_at,
                    attempts = attempts,
                )
            }
        }
    }

    /**
     * Where a delivery goes.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookDestinationKind(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** One of the organization's webhook endpoints, named by `webhook_endpoint_id`. */
            public val Endpoint: WebhookDestinationKind = WebhookDestinationKind("endpoint")

            /** A notification email to a person, named by `recipient`. */
            public val Email: WebhookDestinationKind = WebhookDestinationKind("email")
        }
    }

    /**
     * A webhook endpoint: a URL of the organization's own that Mountaya delivers
     * webhook events to, signed with the endpoint's secret. The secret is answered
     * once, by `postOrganizationWebhookEndpoint`, and never by a read.
     */
    @kotlinx.serialization.Serializable
    public class WebhookEndpoint private constructor(
        /** Identifier of the endpoint, the `webhook_endpoint_id` a delivery names. */
        public val id: kotlin.String,
        /** Identifier of the organization the endpoint belongs to. */
        public val organization_id: kotlin.String,
        /**
         * The URL Mountaya delivers to: `https`, on a host that resolves to a public
         * address, answered within 10 seconds. A redirect is never followed.
         */
        public val url: com.mountaya.client.Url,
        /**
         * What the endpoint is for, as the organization wrote it, null when it has
         * none.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val description: kotlin.String? = null,
        /**
         * The event types the endpoint is sent: each a member of
         * `WebhookEventType`, a family followed by `.*` for every type of the
         * family, such as `organization.member.*`, or `*` for every public type,
         * those added to the contract later included.
         */
        public val enabled_events: kotlin.collections.List<kotlin.String>,
        /** Whether Mountaya delivers to the endpoint. */
        public val status: AdminSchema.WebhookEndpointStatus,
        /** Why the endpoint is disabled, null while it is enabled. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val disabled_reason: AdminSchema.WebhookEndpointDisabledReason? = null,
        /** When the endpoint was disabled, null while it is enabled. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val disabled_at: kotlin.time.Instant? = null,
        /**
         * When the endpoint's run of failed deliveries began, null while its last
         * delivery succeeded. At 24 hours Mountaya records
         * `organization.webhook_endpoint.failing` and emails the organization's
         * owners and admins; at 72 hours it disables the endpoint. A delivery that
         * succeeds after the run was reported records
         * `organization.webhook_endpoint.recovered`. Read it before enabling a
         * disabled endpoint again, which clears it, to redeliver from it.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val failing_since: kotlin.time.Instant? = null,
        /**
         * The version of the endpoint, the one the `ETag` of a read of it carries,
         * opaque and compared whole. It moves on every change to the endpoint's
         * configuration and status, and never with its health: a delivery failing or
         * succeeding again moves `failing_since` and leaves it as it is. Send it as
         * `If-Match` to make `patchOrganizationWebhookEndpoint` or
         * `deleteOrganizationWebhookEndpoint` conditional.
         */
        public val etag: kotlin.String,
        /**
         * The member who created the endpoint, null when their account has been
         * deleted.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val created_by: com.mountaya.client.generated.RestSchema.RelationToUser? = null,
        /** When the endpoint was created. */
        public val created_at: kotlin.time.Instant,
        /**
         * When the endpoint's configuration or status last changed. Its health,
         * `failing_since`, does not move it.
         */
        public val updated_at: kotlin.time.Instant,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEndpoint &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.url == this.url &&
                other.description == this.description &&
                other.enabled_events == this.enabled_events &&
                other.status == this.status &&
                other.disabled_reason == this.disabled_reason &&
                other.disabled_at == this.disabled_at &&
                other.failing_since == this.failing_since &&
                other.etag == this.etag &&
                other.created_by == this.created_by &&
                other.created_at == this.created_at &&
                other.updated_at == this.updated_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.url.hashCode()
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + this.enabled_events.hashCode()
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + (this.disabled_reason?.hashCode() ?: 0)
            hash = 31 * hash + (this.disabled_at?.hashCode() ?: 0)
            hash = 31 * hash + (this.failing_since?.hashCode() ?: 0)
            hash = 31 * hash + this.etag.hashCode()
            hash = 31 * hash + (this.created_by?.hashCode() ?: 0)
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.updated_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEndpoint(" +
                "id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", url=" + this.url +
                ", description=" + this.description +
                ", enabled_events=" + this.enabled_events +
                ", status=" + this.status +
                ", disabled_reason=" + this.disabled_reason +
                ", disabled_at=" + this.disabled_at +
                ", failing_since=" + this.failing_since +
                ", etag=" + this.etag +
                ", created_by=" + this.created_by +
                ", created_at=" + this.created_at +
                ", updated_at=" + this.updated_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEndpoint], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookEndpoint]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEndpoint] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                url: com.mountaya.client.Url,
                description: kotlin.String?,
                enabled_events: kotlin.collections.List<kotlin.String>,
                status: AdminSchema.WebhookEndpointStatus,
                disabled_reason: AdminSchema.WebhookEndpointDisabledReason?,
                disabled_at: kotlin.time.Instant?,
                failing_since: kotlin.time.Instant?,
                etag: kotlin.String,
                created_by: com.mountaya.client.generated.RestSchema.RelationToUser?,
                created_at: kotlin.time.Instant,
                updated_at: kotlin.time.Instant,
                optional: AdminSchema.WebhookEndpoint.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEndpoint {
                val given = AdminSchema.WebhookEndpoint.Builder().apply(optional)
                return AdminSchema.WebhookEndpoint(
                    id = id,
                    organization_id = organization_id,
                    url = url,
                    description = description,
                    enabled_events = enabled_events,
                    status = status,
                    disabled_reason = disabled_reason,
                    disabled_at = disabled_at,
                    failing_since = failing_since,
                    etag = etag,
                    created_by = created_by,
                    created_at = created_at,
                    updated_at = updated_at,
                )
            }
        }
    }

    /**
     * A webhook endpoint to create: where Mountaya delivers, and which event types
     * it is sent.
     */
    @kotlinx.serialization.Serializable
    public class WebhookEndpointCreateParams private constructor(
        /**
         * The URL Mountaya delivers to. It is `https`, written in printable ASCII,
         * at most 2048 characters, with no credentials before its host and no
         * fragment, its host, port included, at most 259 characters; and that host
         * resolves to a public address: a private, loopback or link-local one is a
         * `400` naming `url`, and so is one that cannot be resolved.
         */
        public val url: @kotlinx.serialization.Serializable(with = com.mountaya.client.SentUrlSerializer::class) com.mountaya.client.Url,
        /**
         * What the endpoint is for, at most 100 characters, trimmed. Omitted, the
         * endpoint has none.
         */
        public val description: kotlin.String? = null,
        /**
         * The event types the endpoint is sent: each a member of
         * `WebhookEventType`, a family followed by `.*`, or `*` for every public
         * type, those added later included. A type or a family the contract does
         * not publish is a `400` naming its position.
         */
        public val enabled_events: kotlin.collections.List<kotlin.String>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEndpointCreateParams &&
                other.url == this.url &&
                other.description == this.description &&
                other.enabled_events == this.enabled_events

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.url.hashCode()
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + this.enabled_events.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEndpointCreateParams(" +
                "url=" + this.url +
                ", description=" + this.description +
                ", enabled_events=" + this.enabled_events +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEndpointCreateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null

            /**
             * What the endpoint is for, at most 100 characters, trimmed. Omitted, the
             * endpoint has none.
             */
            public var description: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [AdminSchema.WebhookEndpointCreateParams]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEndpointCreateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                url: com.mountaya.client.Url,
                enabled_events: kotlin.collections.List<kotlin.String>,
                optional: AdminSchema.WebhookEndpointCreateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEndpointCreateParams {
                val given = AdminSchema.WebhookEndpointCreateParams.Builder().apply(optional)
                return AdminSchema.WebhookEndpointCreateParams(
                    url = url,
                    description = given.slot1,
                    enabled_events = enabled_events,
                )
            }
        }
    }

    /**
     * Why a webhook endpoint is disabled.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookEndpointDisabledReason(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** A member disabled it. */
            public val Requested: WebhookEndpointDisabledReason = WebhookEndpointDisabledReason("requested")

            /** Mountaya disabled it after every delivery to it failed for 72 hours. */
            public val Failing: WebhookEndpointDisabledReason = WebhookEndpointDisabledReason("failing")
        }
    }

    /**
     * A webhook endpoint as a webhook event carries it: its identifiers, the host of its
     * URL, its status and its version. Its URL is never carried, since a receiver's URL is
     * often a credential of its own, and neither are its secret and the event types it is
     * sent: `getOrganizationWebhookEndpoint` reads them.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class WebhookEndpointEventObject private constructor(
        /**
         * The kind of object this is, always `organization.webhook_endpoint`: the family of
         * the event types that carry it, and what tells the members of `WebhookEventObject`
         * apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "organization.webhook_endpoint",
        /** Identifier of the endpoint. */
        public val id: kotlin.String,
        /** Identifier of the organization the endpoint belongs to. */
        public val organization_id: kotlin.String,
        /**
         * The host of the URL Mountaya delivers the endpoint's events to, with its port
         * when the URL names one: never its path, its query or the credentials it may
         * carry, which a receiver's URL often uses as its secret.
         */
        public val host: kotlin.String,
        /** Whether Mountaya delivers to the endpoint once this change was made. */
        public val status: AdminSchema.WebhookEndpointStatus,
        /** Why the endpoint is disabled. Present while it is. */
        public val disabled_reason: AdminSchema.WebhookEndpointDisabledReason? = null,
        /**
         * When the endpoint's run of failed deliveries began, null while its last delivery
         * succeeded.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val failing_since: kotlin.time.Instant? = null,
        /**
         * The endpoint's version once this change was made. It climbs with every change to
         * the endpoint's configuration and status, so of two events about one endpoint the
         * one carrying the higher `version` is the later state: events are not delivered in
         * order. Its health, `failing_since`, does not move it: a delivery failing or
         * succeeding again changes no configuration, and a change asked with the endpoint's
         * `etag` is never refused for it.
         */
        public val version: kotlin.Long,
        /** When the endpoint was created. */
        public val created_at: kotlin.time.Instant,
        /**
         * When the endpoint's configuration or status last changed. Its health,
         * `failing_since`, does not move it.
         */
        public val updated_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEndpointEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.host == this.host &&
                other.status == this.status &&
                other.disabled_reason == this.disabled_reason &&
                other.failing_since == this.failing_since &&
                other.version == this.version &&
                other.created_at == this.created_at &&
                other.updated_at == this.updated_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.host.hashCode()
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + (this.disabled_reason?.hashCode() ?: 0)
            hash = 31 * hash + (this.failing_since?.hashCode() ?: 0)
            hash = 31 * hash + this.version.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.updated_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEndpointEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", host=" + this.host +
                ", status=" + this.status +
                ", disabled_reason=" + this.disabled_reason +
                ", failing_since=" + this.failing_since +
                ", version=" + this.version +
                ", created_at=" + this.created_at +
                ", updated_at=" + this.updated_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEndpointEventObject], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot5: AdminSchema.WebhookEndpointDisabledReason? = null

            /** Why the endpoint is disabled. Present while it is. */
            public var disabled_reason: AdminSchema.WebhookEndpointDisabledReason
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot5 = value
                }
        }

        /** Builds a [AdminSchema.WebhookEndpointEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEndpointEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                host: kotlin.String,
                status: AdminSchema.WebhookEndpointStatus,
                failing_since: kotlin.time.Instant?,
                version: kotlin.Long,
                created_at: kotlin.time.Instant,
                updated_at: kotlin.time.Instant,
                optional: AdminSchema.WebhookEndpointEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEndpointEventObject {
                val given = AdminSchema.WebhookEndpointEventObject.Builder().apply(optional)
                return AdminSchema.WebhookEndpointEventObject(
                    id = id,
                    organization_id = organization_id,
                    host = host,
                    status = status,
                    disabled_reason = given.slot5,
                    failing_since = failing_since,
                    version = version,
                    created_at = created_at,
                    updated_at = updated_at,
                )
            }
        }
    }

    /**
     * Whether Mountaya delivers events to a webhook endpoint.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookEndpointStatus(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Mountaya delivers the endpoint every event of a type it is sent. */
            public val Enabled: WebhookEndpointStatus = WebhookEndpointStatus("enabled")

            /** Mountaya delivers the endpoint nothing, and cancels the deliveries still retrying. `disabled_reason` says why. */
            public val Disabled: WebhookEndpointStatus = WebhookEndpointStatus("disabled")
        }
    }

    /**
     * The changes to make to a webhook endpoint. Every field is optional, and an
     * omitted one keeps its value.
     */
    @kotlinx.serialization.Serializable
    public class WebhookEndpointUpdateParams private constructor(
        /**
         * The URL Mountaya delivers to from the next delivery on, held to the rules
         * of a new endpoint's. A delivery still retrying is sent to it.
         */
        public val url: @kotlinx.serialization.Serializable(with = com.mountaya.client.SentUrlSerializer::class) com.mountaya.client.Url? = null,
        @kotlinx.serialization.SerialName("description")
        private val settable1: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent,
        /**
         * The event types the endpoint is sent, replacing the list whole, as a new
         * endpoint's. The list is read when an event is handed to its deliveries, a
         * moment after it is recorded: an event recorded just before the change may
         * be delivered by either list, and every later one by the new.
         */
        public val enabled_events: kotlin.collections.List<kotlin.String>? = null,
        /**
         * `disabled` stops every delivery to the endpoint, those still retrying
         * included, with `disabled_reason` `requested`; `enabled` starts them
         * again from the next event, and clears `failing_since`. The events
         * recorded meanwhile are not sent on their own: `postOrganizationWebhookDelivery`
         * redelivers them.
         */
        public val status: AdminSchema.WebhookEndpointStatus? = null,
    ) {
        /**
         * What the endpoint is for, at most 100 characters, trimmed. `null`
         * clears it.
         */
        public val description: kotlin.String?
            get() = settable1.valueOrNull

        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEndpointUpdateParams &&
                other.url == this.url &&
                other.settable1 == this.settable1 &&
                other.enabled_events == this.enabled_events &&
                other.status == this.status

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.url?.hashCode() ?: 0)
            hash = 31 * hash + this.settable1.hashCode()
            hash = 31 * hash + (this.enabled_events?.hashCode() ?: 0)
            hash = 31 * hash + (this.status?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEndpointUpdateParams(" +
                "url=" + this.url +
                ", description=" + this.description +
                ", enabled_events=" + this.enabled_events +
                ", status=" + this.status +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEndpointUpdateParams], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: com.mountaya.client.Url? = null
            internal var slot1: com.mountaya.client.Settable<kotlin.String?> = com.mountaya.client.Settable.Absent
            internal var slot2: kotlin.collections.List<kotlin.String>? = null
            internal var slot3: AdminSchema.WebhookEndpointStatus? = null

            /**
             * The URL Mountaya delivers to from the next delivery on, held to the rules
             * of a new endpoint's. A delivery still retrying is sent to it.
             */
            public var url: com.mountaya.client.Url
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * What the endpoint is for, at most 100 characters, trimmed. `null`
             * clears it.
             *
             * Left unset it is not sent and the stored value is untouched; set to `null` it is sent as `null`, which clears it.
             */
            public var description: kotlin.String?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = com.mountaya.client.Settable.Present(value)
                }

            /**
             * The event types the endpoint is sent, replacing the list whole, as a new
             * endpoint's. The list is read when an event is handed to its deliveries, a
             * moment after it is recorded: an event recorded just before the change may
             * be delivered by either list, and every later one by the new.
             */
            public var enabled_events: kotlin.collections.List<kotlin.String>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * `disabled` stops every delivery to the endpoint, those still retrying
             * included, with `disabled_reason` `requested`; `enabled` starts them
             * again from the next event, and clears `failing_since`. The events
             * recorded meanwhile are not sent on their own: `postOrganizationWebhookDelivery`
             * redelivers them.
             */
            public var status: AdminSchema.WebhookEndpointStatus
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [AdminSchema.WebhookEndpointUpdateParams]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEndpointUpdateParams] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                optional: AdminSchema.WebhookEndpointUpdateParams.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEndpointUpdateParams {
                val given = AdminSchema.WebhookEndpointUpdateParams.Builder().apply(optional)
                return AdminSchema.WebhookEndpointUpdateParams(
                    url = given.slot0,
                    settable1 = given.slot1,
                    enabled_events = given.slot2,
                    status = given.slot3,
                )
            }
        }
    }

    /**
     * A webhook endpoint as `postOrganizationWebhookEndpoint` answers it: the
     * endpoint, and the secret its deliveries are signed with. This is the one
     * answer that carries the secret, and it is never kept for the request's
     * `Idempotency-Key`, so store it before anything else.
     *
     * Its `toString` prints every property but its secrets, each printed as `<redacted>`, so a log never carries one, and each secret stays readable.
     */
    @kotlinx.serialization.Serializable
    public class WebhookEndpointWithSecret private constructor(
        /** Identifier of the endpoint, the `webhook_endpoint_id` a delivery names. */
        public val id: kotlin.String,
        /** Identifier of the organization the endpoint belongs to. */
        public val organization_id: kotlin.String,
        /**
         * The URL Mountaya delivers to: `https`, on a host that resolves to a public
         * address, answered within 10 seconds. A redirect is never followed.
         */
        public val url: com.mountaya.client.Url,
        /**
         * What the endpoint is for, as the organization wrote it, null when it has
         * none.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val description: kotlin.String? = null,
        /**
         * The event types the endpoint is sent: each a member of
         * `WebhookEventType`, a family followed by `.*` for every type of the
         * family, such as `organization.member.*`, or `*` for every public type,
         * those added to the contract later included.
         */
        public val enabled_events: kotlin.collections.List<kotlin.String>,
        /** Whether Mountaya delivers to the endpoint. */
        public val status: AdminSchema.WebhookEndpointStatus,
        /** Why the endpoint is disabled, null while it is enabled. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val disabled_reason: AdminSchema.WebhookEndpointDisabledReason? = null,
        /** When the endpoint was disabled, null while it is enabled. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val disabled_at: kotlin.time.Instant? = null,
        /**
         * When the endpoint's run of failed deliveries began, null while its last
         * delivery succeeded. At 24 hours Mountaya records
         * `organization.webhook_endpoint.failing` and emails the organization's
         * owners and admins; at 72 hours it disables the endpoint. A delivery that
         * succeeds after the run was reported records
         * `organization.webhook_endpoint.recovered`. Read it before enabling a
         * disabled endpoint again, which clears it, to redeliver from it.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val failing_since: kotlin.time.Instant? = null,
        /**
         * The version of the endpoint, the one the `ETag` of a read of it carries,
         * opaque and compared whole. It moves on every change to the endpoint's
         * configuration and status, and never with its health: a delivery failing or
         * succeeding again moves `failing_since` and leaves it as it is. Send it as
         * `If-Match` to make `patchOrganizationWebhookEndpoint` or
         * `deleteOrganizationWebhookEndpoint` conditional.
         */
        public val etag: kotlin.String,
        /**
         * The member who created the endpoint, null when their account has been
         * deleted.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val created_by: com.mountaya.client.generated.RestSchema.RelationToUser? = null,
        /** When the endpoint was created. */
        public val created_at: kotlin.time.Instant,
        /**
         * When the endpoint's configuration or status last changed. Its health,
         * `failing_since`, does not move it.
         */
        public val updated_at: kotlin.time.Instant,
        /**
         * The endpoint's signing secret: `whsk_` and 64 lowercase hexadecimal
         * characters. Every delivery's `Mountaya-Signature` is the HMAC-SHA256
         * of the delivery keyed by it, as that header says. It is shown here
         * once.
         *
         * It is a secret: its class's `toString` prints it as `<redacted>`, and it stays readable here.
         */
        public val secret: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEndpointWithSecret &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.url == this.url &&
                other.description == this.description &&
                other.enabled_events == this.enabled_events &&
                other.status == this.status &&
                other.disabled_reason == this.disabled_reason &&
                other.disabled_at == this.disabled_at &&
                other.failing_since == this.failing_since &&
                other.etag == this.etag &&
                other.created_by == this.created_by &&
                other.created_at == this.created_at &&
                other.updated_at == this.updated_at &&
                other.secret == this.secret

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.url.hashCode()
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + this.enabled_events.hashCode()
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + (this.disabled_reason?.hashCode() ?: 0)
            hash = 31 * hash + (this.disabled_at?.hashCode() ?: 0)
            hash = 31 * hash + (this.failing_since?.hashCode() ?: 0)
            hash = 31 * hash + this.etag.hashCode()
            hash = 31 * hash + (this.created_by?.hashCode() ?: 0)
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.updated_at.hashCode()
            hash = 31 * hash + this.secret.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEndpointWithSecret(" +
                "id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", url=" + this.url +
                ", description=" + this.description +
                ", enabled_events=" + this.enabled_events +
                ", status=" + this.status +
                ", disabled_reason=" + this.disabled_reason +
                ", disabled_at=" + this.disabled_at +
                ", failing_since=" + this.failing_since +
                ", etag=" + this.etag +
                ", created_by=" + this.created_by +
                ", created_at=" + this.created_at +
                ", updated_at=" + this.updated_at +
                ", secret=" + "<redacted>" +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEndpointWithSecret], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookEndpointWithSecret]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEndpointWithSecret] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                url: com.mountaya.client.Url,
                description: kotlin.String?,
                enabled_events: kotlin.collections.List<kotlin.String>,
                status: AdminSchema.WebhookEndpointStatus,
                disabled_reason: AdminSchema.WebhookEndpointDisabledReason?,
                disabled_at: kotlin.time.Instant?,
                failing_since: kotlin.time.Instant?,
                etag: kotlin.String,
                created_by: com.mountaya.client.generated.RestSchema.RelationToUser?,
                created_at: kotlin.time.Instant,
                updated_at: kotlin.time.Instant,
                secret: kotlin.String,
                optional: AdminSchema.WebhookEndpointWithSecret.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEndpointWithSecret {
                val given = AdminSchema.WebhookEndpointWithSecret.Builder().apply(optional)
                return AdminSchema.WebhookEndpointWithSecret(
                    id = id,
                    organization_id = organization_id,
                    url = url,
                    description = description,
                    enabled_events = enabled_events,
                    status = status,
                    disabled_reason = disabled_reason,
                    disabled_at = disabled_at,
                    failing_since = failing_since,
                    etag = etag,
                    created_by = created_by,
                    created_at = created_at,
                    updated_at = updated_at,
                    secret = secret,
                )
            }
        }
    }

    /**
     * A webhook event: a change Mountaya recorded, as it is delivered to an endpoint and
     * listed by `getOrganizationWebhookEvents`. The body of a delivery is exactly this
     * object, and its signature covers those bytes. Every public type and the lean object it
     * carries is an entry of this document's `webhooks`.
     */
    @kotlinx.serialization.Serializable
    public class WebhookEvent private constructor(
        /**
         * Identifier of the event. Its leading bits are the moment it was recorded, so
         * identifiers sort by time. A delivery can arrive more than once, so a receiver
         * deduplicates on it.
         */
        public val id: kotlin.String,
        /** Always `webhook_event`. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "webhook_event",
        /** What happened. The family before its last dot names the kind of `data.object`. */
        public val type: AdminSchema.WebhookEventType,
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
        public val data: AdminSchema.WebhookEventData,
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
            other is AdminSchema.WebhookEvent &&
                other.id == this.id &&
                other.`object` == this.`object` &&
                other.type == this.type &&
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
            hash = 31 * hash + this.type.hashCode()
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
            "WebhookEvent(" +
                "id=" + this.id +
                ", object=" + this.`object` +
                ", type=" + this.type +
                ", api_version=" + this.api_version +
                ", created_at=" + this.created_at +
                ", organization_id=" + this.organization_id +
                ", task=" + this.task +
                ", related_object=" + this.related_object +
                ", data=" + this.data +
                ", request=" + this.request +
                ", actor=" + this.actor +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEvent], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookEvent]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEvent] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                type: AdminSchema.WebhookEventType,
                api_version: kotlin.String,
                created_at: kotlin.time.Instant,
                organization_id: kotlin.String,
                task: kotlin.String,
                related_object: AdminSchema.WebhookEventRelatedObject,
                data: AdminSchema.WebhookEventData,
                request: AdminSchema.WebhookEventRequest?,
                actor: AdminSchema.WebhookEventActor,
                optional: AdminSchema.WebhookEvent.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEvent {
                val given = AdminSchema.WebhookEvent.Builder().apply(optional)
                return AdminSchema.WebhookEvent(
                    id = id,
                    type = type,
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

    /** Who made the change a webhook event records. */
    @kotlinx.serialization.Serializable
    public class WebhookEventActor private constructor(
        /** What kind of actor it is. */
        public val type: AdminSchema.WebhookEventActorType,
        /**
         * Identifier of the person, when `type` is `user`. Absent on
         * `organization.invitation.declined`: its actor is the person invited, whom the
         * invitation names by the address it was sent to, and an organization is not told
         * which account answers an invitation it made to an address.
         */
        public val user_id: kotlin.String? = null,
        /**
         * Identifier of the publishable key the request was made with: always when `type` is
         * `key`, and when `type` is `user` and the person acted through a key rather than
         * signed in to Mountaya's web application.
         */
        public val key_id: kotlin.String? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEventActor &&
                other.type == this.type &&
                other.user_id == this.user_id &&
                other.key_id == this.key_id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.type.hashCode()
            hash = 31 * hash + (this.user_id?.hashCode() ?: 0)
            hash = 31 * hash + (this.key_id?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEventActor(" +
                "type=" + this.type +
                ", user_id=" + this.user_id +
                ", key_id=" + this.key_id +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEventActor], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null
            internal var slot2: kotlin.String? = null

            /**
             * Identifier of the person, when `type` is `user`. Absent on
             * `organization.invitation.declined`: its actor is the person invited, whom the
             * invitation names by the address it was sent to, and an organization is not told
             * which account answers an invitation it made to an address.
             */
            public var user_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Identifier of the publishable key the request was made with: always when `type` is
             * `key`, and when `type` is `user` and the person acted through a key rather than
             * signed in to Mountaya's web application.
             */
            public var key_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }
        }

        /** Builds a [AdminSchema.WebhookEventActor]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEventActor] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                type: AdminSchema.WebhookEventActorType,
                optional: AdminSchema.WebhookEventActor.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEventActor {
                val given = AdminSchema.WebhookEventActor.Builder().apply(optional)
                return AdminSchema.WebhookEventActor(
                    type = type,
                    user_id = given.slot1,
                    key_id = given.slot2,
                )
            }
        }
    }

    /**
     * Who made the change a webhook event records.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookEventActorType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** A person, named by `actor.user_id`: the user a session token names, or a person signed in to Mountaya's web application. The person declining an invitation is named by no `actor.user_id`. */
            public val User: WebhookEventActorType = WebhookEventActorType("user")

            /** A request made under a publishable key with no user behind it, named by `actor.key_id`. */
            public val Key: WebhookEventActorType = WebhookEventActorType("key")

            /** Mountaya itself: a schedule, what the payment provider reports, or Mountaya's staff acting on the organization's agreement. */
            public val Mountaya: WebhookEventActorType = WebhookEventActorType("mountaya")
        }
    }

    /** A public webhook event type, as `getWebhookEventTypes` lists it. */
    @kotlinx.serialization.Serializable
    public class WebhookEventCatalogEntry private constructor(
        /** The event type. */
        public val type: AdminSchema.WebhookEventType,
        /**
         * The type's family: the type without what happened, which names the kind
         * of object `data.object` holds. `enabled_events` takes a family followed
         * by `.*` for every type of it.
         */
        public val family: kotlin.String,
        /** What happened, in one line of at most 120 characters. */
        public val summary: kotlin.String,
        /**
         * When the event is recorded, as this document's entry for the type says.
         * Mountaya writes it rather than an organization, so the 100-character
         * ceiling of a description an organization writes does not apply to it;
         * it holds at most 2000 characters.
         */
        public val description: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEventCatalogEntry &&
                other.type == this.type &&
                other.family == this.family &&
                other.summary == this.summary &&
                other.description == this.description

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.type.hashCode()
            hash = 31 * hash + this.family.hashCode()
            hash = 31 * hash + this.summary.hashCode()
            hash = 31 * hash + this.description.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEventCatalogEntry(" +
                "type=" + this.type +
                ", family=" + this.family +
                ", summary=" + this.summary +
                ", description=" + this.description +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEventCatalogEntry], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookEventCatalogEntry]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEventCatalogEntry] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                type: AdminSchema.WebhookEventType,
                family: kotlin.String,
                summary: kotlin.String,
                description: kotlin.String,
                optional: AdminSchema.WebhookEventCatalogEntry.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEventCatalogEntry {
                val given = AdminSchema.WebhookEventCatalogEntry.Builder().apply(optional)
                return AdminSchema.WebhookEventCatalogEntry(
                    type = type,
                    family = family,
                    summary = summary,
                    description = description,
                )
            }
        }
    }

    /** What a webhook event says about its object. */
    @kotlinx.serialization.Serializable
    public class WebhookEventData private constructor(
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
        public val `object`: AdminSchema.WebhookEventObject,
        /**
         * The fields of `data.object` the change moved, each with the value it held before.
         * Present on the types whose entry in `webhooks` says so, such as
         * `organization.member.updated`, which carries the member's former `role`. A field
         * that did not move is not in it.
         */
        public val previous_attributes: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEventData &&
                other.`object` == this.`object` &&
                other.previous_attributes == this.previous_attributes

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + (this.previous_attributes?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEventData(" +
                "object=" + this.`object` +
                ", previous_attributes=" + this.previous_attributes +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEventData], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>? = null

            /**
             * The fields of `data.object` the change moved, each with the value it held before.
             * Present on the types whose entry in `webhooks` says so, such as
             * `organization.member.updated`, which carries the member's former `role`. A field
             * that did not move is not in it.
             */
            public var previous_attributes: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }
        }

        /** Builds a [AdminSchema.WebhookEventData]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEventData] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                `object`: AdminSchema.WebhookEventObject,
                optional: AdminSchema.WebhookEventData.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEventData {
                val given = AdminSchema.WebhookEventData.Builder().apply(optional)
                return AdminSchema.WebhookEventData(
                    `object` = `object`,
                    previous_attributes = given.slot1,
                )
            }
        }
    }

    /**
     * The object a webhook event carries at `data.object`: one lean object per kind, told
     * apart by its `object`, which is the family of the event's type.
     *
     * Each member extends it and is told apart by `object`. It is open: a member the contract adds after this build decodes as [Unknown], so a `when` over it ends in `else`.
     */
    @kotlinx.serialization.Serializable(with = AdminSchema.WebhookEventObject.Serializer::class)
    public abstract class WebhookEventObject internal constructor() {
        /** Unknown is a member this build does not know, holding the object that arrived. */
        public class Unknown(
            /** The member as it arrived. */
            public val json: kotlinx.serialization.json.JsonObject,
        ) : AdminSchema.WebhookEventObject() {
            override fun equals(other: kotlin.Any?): kotlin.Boolean = other is AdminSchema.WebhookEventObject.Unknown && other.json == json

            override fun hashCode(): kotlin.Int = json.hashCode()

            override fun toString(): kotlin.String = "Unknown(" + json + ")"
        }

        internal object Serializer : kotlinx.serialization.KSerializer<AdminSchema.WebhookEventObject> {
            override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
                kotlinx.serialization.descriptors.buildClassSerialDescriptor("com.mountaya.admin.generated.AdminSchema.WebhookEventObject")

            override fun serialize(
                encoder: kotlinx.serialization.encoding.Encoder,
                value: AdminSchema.WebhookEventObject,
            ) {
                when (value) {
                    is AdminSchema.OrganizationEventObject -> encoder.encodeSerializableValue(AdminSchema.OrganizationEventObject.serializer(), value)
                    is AdminSchema.OrganizationMemberEventObject -> encoder.encodeSerializableValue(AdminSchema.OrganizationMemberEventObject.serializer(), value)
                    is AdminSchema.OrganizationInvitationEventObject -> encoder.encodeSerializableValue(AdminSchema.OrganizationInvitationEventObject.serializer(), value)
                    is AdminSchema.CollectionEventObject -> encoder.encodeSerializableValue(AdminSchema.CollectionEventObject.serializer(), value)
                    is AdminSchema.CollectionItineraryEventObject -> encoder.encodeSerializableValue(AdminSchema.CollectionItineraryEventObject.serializer(), value)
                    is AdminSchema.CollectionZoneEventObject -> encoder.encodeSerializableValue(AdminSchema.CollectionZoneEventObject.serializer(), value)
                    is AdminSchema.CollectionShareLinkEventObject -> encoder.encodeSerializableValue(AdminSchema.CollectionShareLinkEventObject.serializer(), value)
                    is AdminSchema.ItineraryEventObject -> encoder.encodeSerializableValue(AdminSchema.ItineraryEventObject.serializer(), value)
                    is AdminSchema.ItinerarySegmentEventObject -> encoder.encodeSerializableValue(AdminSchema.ItinerarySegmentEventObject.serializer(), value)
                    is AdminSchema.ItinerarySegmentGroupEventObject -> encoder.encodeSerializableValue(AdminSchema.ItinerarySegmentGroupEventObject.serializer(), value)
                    is AdminSchema.ItineraryWaypointEventObject -> encoder.encodeSerializableValue(AdminSchema.ItineraryWaypointEventObject.serializer(), value)
                    is AdminSchema.ItineraryShareLinkEventObject -> encoder.encodeSerializableValue(AdminSchema.ItineraryShareLinkEventObject.serializer(), value)
                    is AdminSchema.ZoneEventObject -> encoder.encodeSerializableValue(AdminSchema.ZoneEventObject.serializer(), value)
                    is AdminSchema.PublishableKeyEventObject -> encoder.encodeSerializableValue(AdminSchema.PublishableKeyEventObject.serializer(), value)
                    is AdminSchema.SecretKeyEventObject -> encoder.encodeSerializableValue(AdminSchema.SecretKeyEventObject.serializer(), value)
                    is AdminSchema.AiProviderConfigEventObject -> encoder.encodeSerializableValue(AdminSchema.AiProviderConfigEventObject.serializer(), value)
                    is AdminSchema.OrganizationSettingsEventObject -> encoder.encodeSerializableValue(AdminSchema.OrganizationSettingsEventObject.serializer(), value)
                    is AdminSchema.OrganizationEntitlementsEventObject -> encoder.encodeSerializableValue(AdminSchema.OrganizationEntitlementsEventObject.serializer(), value)
                    is AdminSchema.OrganizationSubscriptionEventObject -> encoder.encodeSerializableValue(AdminSchema.OrganizationSubscriptionEventObject.serializer(), value)
                    is AdminSchema.InvoiceEventObject -> encoder.encodeSerializableValue(AdminSchema.InvoiceEventObject.serializer(), value)
                    is AdminSchema.PaymentMethodEventObject -> encoder.encodeSerializableValue(AdminSchema.PaymentMethodEventObject.serializer(), value)
                    is AdminSchema.BillingInfoEventObject -> encoder.encodeSerializableValue(AdminSchema.BillingInfoEventObject.serializer(), value)
                    is AdminSchema.WebhookEndpointEventObject -> encoder.encodeSerializableValue(AdminSchema.WebhookEndpointEventObject.serializer(), value)
                    is AdminSchema.WebhookEventObject.Unknown -> encoder.encodeSerializableValue(kotlinx.serialization.json.JsonObject.serializer(), value.json)
                    else -> throw kotlinx.serialization.SerializationException("AdminSchema.WebhookEventObject has no member of this class.")
                }
            }

            override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): AdminSchema.WebhookEventObject {
                val input =
                    decoder as? kotlinx.serialization.json.JsonDecoder
                        ?: throw kotlinx.serialization.SerializationException("AdminSchema.WebhookEventObject is read from JSON only.")
                val json =
                    input.decodeJsonElement() as? kotlinx.serialization.json.JsonObject
                        ?: throw kotlinx.serialization.SerializationException("AdminSchema.WebhookEventObject is a JSON object.")
                val discriminator = json["object"] as? kotlinx.serialization.json.JsonPrimitive
                return when (if (discriminator != null && discriminator.isString) discriminator.content else null) {
                    "organization" -> input.json.decodeFromJsonElement(AdminSchema.OrganizationEventObject.serializer(), json)
                    "organization.member" -> input.json.decodeFromJsonElement(AdminSchema.OrganizationMemberEventObject.serializer(), json)
                    "organization.invitation" -> input.json.decodeFromJsonElement(AdminSchema.OrganizationInvitationEventObject.serializer(), json)
                    "collection" -> input.json.decodeFromJsonElement(AdminSchema.CollectionEventObject.serializer(), json)
                    "collection.itinerary" -> input.json.decodeFromJsonElement(AdminSchema.CollectionItineraryEventObject.serializer(), json)
                    "collection.zone" -> input.json.decodeFromJsonElement(AdminSchema.CollectionZoneEventObject.serializer(), json)
                    "collection.share_link" -> input.json.decodeFromJsonElement(AdminSchema.CollectionShareLinkEventObject.serializer(), json)
                    "itinerary" -> input.json.decodeFromJsonElement(AdminSchema.ItineraryEventObject.serializer(), json)
                    "itinerary.segment" -> input.json.decodeFromJsonElement(AdminSchema.ItinerarySegmentEventObject.serializer(), json)
                    "itinerary.segment_group" -> input.json.decodeFromJsonElement(AdminSchema.ItinerarySegmentGroupEventObject.serializer(), json)
                    "itinerary.waypoint" -> input.json.decodeFromJsonElement(AdminSchema.ItineraryWaypointEventObject.serializer(), json)
                    "itinerary.share_link" -> input.json.decodeFromJsonElement(AdminSchema.ItineraryShareLinkEventObject.serializer(), json)
                    "zone" -> input.json.decodeFromJsonElement(AdminSchema.ZoneEventObject.serializer(), json)
                    "organization.publishable_key" -> input.json.decodeFromJsonElement(AdminSchema.PublishableKeyEventObject.serializer(), json)
                    "organization.secret_key" -> input.json.decodeFromJsonElement(AdminSchema.SecretKeyEventObject.serializer(), json)
                    "organization.ai_provider_config" -> input.json.decodeFromJsonElement(AdminSchema.AiProviderConfigEventObject.serializer(), json)
                    "organization.settings" -> input.json.decodeFromJsonElement(AdminSchema.OrganizationSettingsEventObject.serializer(), json)
                    "organization.entitlements" -> input.json.decodeFromJsonElement(AdminSchema.OrganizationEntitlementsEventObject.serializer(), json)
                    "organization.billing.subscription" -> input.json.decodeFromJsonElement(AdminSchema.OrganizationSubscriptionEventObject.serializer(), json)
                    "organization.billing.invoice" -> input.json.decodeFromJsonElement(AdminSchema.InvoiceEventObject.serializer(), json)
                    "organization.billing.payment_method" -> input.json.decodeFromJsonElement(AdminSchema.PaymentMethodEventObject.serializer(), json)
                    "organization.billing.info" -> input.json.decodeFromJsonElement(AdminSchema.BillingInfoEventObject.serializer(), json)
                    "organization.webhook_endpoint" -> input.json.decodeFromJsonElement(AdminSchema.WebhookEndpointEventObject.serializer(), json)
                    else -> AdminSchema.WebhookEventObject.Unknown(json)
                }
            }
        }
    }

    /**
     * The object a webhook event is about, as a type and an identifier, so a receiver routes
     * the event before it reads `data`.
     */
    @kotlinx.serialization.Serializable
    public class WebhookEventRelatedObject private constructor(
        /**
         * The family of the event's type, which is the `object` of `data.object` too:
         * `organization.member` for `organization.member.updated`.
         */
        public val type: kotlin.String,
        /**
         * The identifier a read of the object takes: `data.object.id` for most kinds; a
         * member's `user_id`; for a share link, the itinerary's or the collection's; for an
         * itinerary or a zone of a collection, the itinerary's or the zone's; and for an
         * organization's settings, entitlements, AI provider configuration and billing
         * information, the organization's. A Data API subscription's, an invoice's and a
         * payment method's are the payment provider's, and are not UUIDs.
         */
        public val id: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEventRelatedObject &&
                other.type == this.type &&
                other.id == this.id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.type.hashCode()
            hash = 31 * hash + this.id.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEventRelatedObject(" +
                "type=" + this.type +
                ", id=" + this.id +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEventRelatedObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookEventRelatedObject]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEventRelatedObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                type: kotlin.String,
                id: kotlin.String,
                optional: AdminSchema.WebhookEventRelatedObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEventRelatedObject {
                val given = AdminSchema.WebhookEventRelatedObject.Builder().apply(optional)
                return AdminSchema.WebhookEventRelatedObject(
                    type = type,
                    id = id,
                )
            }
        }
    }

    /** The API request that made the change a webhook event records. */
    @kotlinx.serialization.Serializable
    public class WebhookEventRequest private constructor(
        /**
         * The request's identifier, the `X-Request-Id` its answer carried: 32 lowercase
         * hexadecimal characters. `getOrganizationWebhookEvents` finds every event one
         * request made by it, as `request_id`.
         */
        public val id: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEventRequest &&
                other.id == this.id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEventRequest(" +
                "id=" + this.id +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEventRequest], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookEventRequest]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEventRequest] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                optional: AdminSchema.WebhookEventRequest.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEventRequest {
                val given = AdminSchema.WebhookEventRequest.Builder().apply(optional)
                return AdminSchema.WebhookEventRequest(
                    id = id,
                )
            }
        }
    }

    /**
     * A webhook event type: the path of the resource the event is about, each segment's
     * hyphens written as underscores, then what happened to it, in the past tense.
     * Everything before the last dot is the type's family, the kind of object `data.object`
     * holds, which `related_object.type` names too. The members are the public types: each
     * is an entry of this document's `webhooks`, which describes its payload, and
     * `getWebhookEventTypes` lists them with their families.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookEventType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** An organization was created, with the user who created it as its owner. */
            public val OrganizationCreated: WebhookEventType = WebhookEventType("organization.created")

            /** An organization's own fields were changed, which is its name. */
            public val OrganizationUpdated: WebhookEventType = WebhookEventType("organization.updated")

            /** Somebody joined the organization: the owner of an organization just created, or a person accepting an invitation. */
            public val OrganizationMemberCreated: WebhookEventType = WebhookEventType("organization.member.created")

            /** A member's role changed: through `patchOrganizationMember`, or when ownership was handed on, which records one for each of the two members. */
            public val OrganizationMemberUpdated: WebhookEventType = WebhookEventType("organization.member.updated")

            /** A membership ended: the member was removed by somebody else, or left. `reason` says which. */
            public val OrganizationMemberDeleted: WebhookEventType = WebhookEventType("organization.member.deleted")

            /** An invitation was made. */
            public val OrganizationInvitationCreated: WebhookEventType = WebhookEventType("organization.invitation.created")

            /** The person invited accepted the invitation, and is a member from then on. */
            public val OrganizationInvitationAccepted: WebhookEventType = WebhookEventType("organization.invitation.accepted")

            /** The person invited declined the invitation. */
            public val OrganizationInvitationDeclined: WebhookEventType = WebhookEventType("organization.invitation.declined")

            /** A member withdrew an invitation still waiting for an answer. */
            public val OrganizationInvitationRevoked: WebhookEventType = WebhookEventType("organization.invitation.revoked")

            /** An invitation lapsed unanswered thirty days after it was made, and was cleared away. */
            public val OrganizationInvitationExpired: WebhookEventType = WebhookEventType("organization.invitation.expired")

            /** An invitation was cleared because the person it names joined the organization through another invitation. */
            public val OrganizationInvitationDeleted: WebhookEventType = WebhookEventType("organization.invitation.deleted")

            /** A collection was created. */
            public val CollectionCreated: WebhookEventType = WebhookEventType("collection.created")

            /** A collection's own fields were changed. */
            public val CollectionUpdated: WebhookEventType = WebhookEventType("collection.updated")

            /** A collection was deleted. What it held and its share link go with it, and record no event of their own. */
            public val CollectionDeleted: WebhookEventType = WebhookEventType("collection.deleted")

            /** An itinerary the collection did not hold was added to it. */
            public val CollectionItineraryAdded: WebhookEventType = WebhookEventType("collection.itinerary.added")

            /** An itinerary left a collection: it was removed from it, or the itinerary was deleted. */
            public val CollectionItineraryRemoved: WebhookEventType = WebhookEventType("collection.itinerary.removed")

            /** An itinerary moved to another position in a collection. */
            public val CollectionItineraryMoved: WebhookEventType = WebhookEventType("collection.itinerary.moved")

            /** A zone the collection did not hold was added to it. */
            public val CollectionZoneAdded: WebhookEventType = WebhookEventType("collection.zone.added")

            /** A zone left a collection: it was removed from it, or the zone was deleted. */
            public val CollectionZoneRemoved: WebhookEventType = WebhookEventType("collection.zone.removed")

            /** A collection's share link was minted, the first time it was asked for. */
            public val CollectionShareLinkCreated: WebhookEventType = WebhookEventType("collection.share_link.created")

            /** A collection's share link was given a new token, which disables the old one. */
            public val CollectionShareLinkRotated: WebhookEventType = WebhookEventType("collection.share_link.rotated")

            /** A collection's share link was removed, which disables its token. */
            public val CollectionShareLinkDeleted: WebhookEventType = WebhookEventType("collection.share_link.deleted")

            /** An itinerary was created, imported from a file or copied. `origin` says which. */
            public val ItineraryCreated: WebhookEventType = WebhookEventType("itinerary.created")

            /** An itinerary's own fields were written. A change to its segments, segment groups or waypoints is an event of their own. */
            public val ItineraryUpdated: WebhookEventType = WebhookEventType("itinerary.updated")

            /** An itinerary was deleted, with its segments, segment groups, waypoints, publication and share link, which record no event of their own. */
            public val ItineraryDeleted: WebhookEventType = WebhookEventType("itinerary.deleted")

            /** An itinerary was published: a first publication, or a republication replacing the live one. */
            public val ItineraryPublished: WebhookEventType = WebhookEventType("itinerary.published")

            /** An itinerary's live publication was taken down. */
            public val ItineraryUnpublished: WebhookEventType = WebhookEventType("itinerary.unpublished")

            /** A segment was appended or inserted, or made by a split as its second half. */
            public val ItinerarySegmentCreated: WebhookEventType = WebhookEventType("itinerary.segment.created")

            /** A segment was changed, or re-routed to meet a neighbour whose shared point a change moved. */
            public val ItinerarySegmentUpdated: WebhookEventType = WebhookEventType("itinerary.segment.updated")

            /** A segment was removed, or absorbed by the segment before it in a merge. */
            public val ItinerarySegmentDeleted: WebhookEventType = WebhookEventType("itinerary.segment.deleted")

            /** A segment was merged with the one after it and now runs over both. The one absorbed records `itinerary.segment.deleted`. */
            public val ItinerarySegmentMerged: WebhookEventType = WebhookEventType("itinerary.segment.merged")

            /** A segment was cut in two and is now the first half. The second half records `itinerary.segment.created`. */
            public val ItinerarySegmentSplit: WebhookEventType = WebhookEventType("itinerary.segment.split")

            /** A segment was converted between routed and freehand. */
            public val ItinerarySegmentConverted: WebhookEventType = WebhookEventType("itinerary.segment.converted")

            /** A segment group was created. */
            public val ItinerarySegmentGroupCreated: WebhookEventType = WebhookEventType("itinerary.segment_group.created")

            /** A segment group's name, colour, speed or segments changed. */
            public val ItinerarySegmentGroupUpdated: WebhookEventType = WebhookEventType("itinerary.segment_group.updated")

            /** A segment group was deleted, or dissolved because a change left it fewer than two segments. Its segments are kept. */
            public val ItinerarySegmentGroupDeleted: WebhookEventType = WebhookEventType("itinerary.segment_group.deleted")

            /** A waypoint was added. */
            public val ItineraryWaypointCreated: WebhookEventType = WebhookEventType("itinerary.waypoint.created")

            /** A waypoint was changed. */
            public val ItineraryWaypointUpdated: WebhookEventType = WebhookEventType("itinerary.waypoint.updated")

            /** A waypoint was removed. */
            public val ItineraryWaypointDeleted: WebhookEventType = WebhookEventType("itinerary.waypoint.deleted")

            /** An itinerary's share link was minted, the first time it was asked for. */
            public val ItineraryShareLinkCreated: WebhookEventType = WebhookEventType("itinerary.share_link.created")

            /** An itinerary's share link was given a new token, which disables the old one. */
            public val ItineraryShareLinkRotated: WebhookEventType = WebhookEventType("itinerary.share_link.rotated")

            /** An itinerary's share link was removed, which disables its token. */
            public val ItineraryShareLinkDeleted: WebhookEventType = WebhookEventType("itinerary.share_link.deleted")

            /** A zone was created. */
            public val ZoneCreated: WebhookEventType = WebhookEventType("zone.created")

            /** A zone was changed: its fields, its area or its validity window. */
            public val ZoneUpdated: WebhookEventType = WebhookEventType("zone.updated")

            /** A zone was deleted, with its publication, which records no event of its own. */
            public val ZoneDeleted: WebhookEventType = WebhookEventType("zone.deleted")

            /** A zone was published: a first publication, or a republication replacing the live one. */
            public val ZonePublished: WebhookEventType = WebhookEventType("zone.published")

            /** A zone's live publication was taken down. */
            public val ZoneUnpublished: WebhookEventType = WebhookEventType("zone.unpublished")

            /** A publishable key was created. */
            public val OrganizationPublishableKeyCreated: WebhookEventType = WebhookEventType("organization.publishable_key.created")

            /** A publishable key's name or scopes were written. */
            public val OrganizationPublishableKeyUpdated: WebhookEventType = WebhookEventType("organization.publishable_key.updated")

            /** A publishable key was revoked. */
            public val OrganizationPublishableKeyRevoked: WebhookEventType = WebhookEventType("organization.publishable_key.revoked")

            /** A secret key was created, signed in to Mountaya's web application. */
            public val OrganizationSecretKeyCreated: WebhookEventType = WebhookEventType("organization.secret_key.created")

            /** A secret key's name was written, signed in to Mountaya's web application. */
            public val OrganizationSecretKeyUpdated: WebhookEventType = WebhookEventType("organization.secret_key.updated")

            /** A secret key was revoked, signed in to Mountaya's web application. */
            public val OrganizationSecretKeyRevoked: WebhookEventType = WebhookEventType("organization.secret_key.revoked")

            /** An organization's AI provider configuration was created. */
            public val OrganizationAiProviderConfigCreated: WebhookEventType = WebhookEventType("organization.ai_provider_config.created")

            /** An organization's AI provider configuration was written: its model, its credential or its base URL. */
            public val OrganizationAiProviderConfigUpdated: WebhookEventType = WebhookEventType("organization.ai_provider_config.updated")

            /** An organization's AI provider configuration was deleted, its credential with it. */
            public val OrganizationAiProviderConfigDeleted: WebhookEventType = WebhookEventType("organization.ai_provider_config.deleted")

            /** An organization's Map Publishing settings were written: by a member, or by Mountaya where the organization's agreement fixes a setting. */
            public val OrganizationSettingsUpdated: WebhookEventType = WebhookEventType("organization.settings.updated")

            /** The capabilities or studio features an organization holds changed: its agreement with Mountaya changed, or its Data API subscription started or ended. */
            public val OrganizationEntitlementsUpdated: WebhookEventType = WebhookEventType("organization.entitlements.updated")

            /** An organization's Data API subscription was opened: by its first card, or by `postOrganizationBillingSubscription`. */
            public val OrganizationBillingSubscriptionCreated: WebhookEventType = WebhookEventType("organization.billing.subscription.created")

            /** An organization's Data API subscription changed: an end was scheduled or taken back, its status moved, or a new period began. */
            public val OrganizationBillingSubscriptionUpdated: WebhookEventType = WebhookEventType("organization.billing.subscription.updated")

            /** Collection on an organization's Data API subscription was paused. */
            public val OrganizationBillingSubscriptionPaused: WebhookEventType = WebhookEventType("organization.billing.subscription.paused")

            /** Collection on an organization's paused Data API subscription was resumed. */
            public val OrganizationBillingSubscriptionResumed: WebhookEventType = WebhookEventType("organization.billing.subscription.resumed")

            /** An organization's Data API subscription ended, at the end of its period or because it could not be paid. */
            public val OrganizationBillingSubscriptionCanceled: WebhookEventType = WebhookEventType("organization.billing.subscription.canceled")

            /** An invoice addressed to the organization was paid. */
            public val OrganizationBillingInvoicePaid: WebhookEventType = WebhookEventType("organization.billing.invoice.paid")

            /** A payment of an invoice addressed to the organization failed. The payment provider tries again on its own schedule. */
            public val OrganizationBillingInvoicePaymentFailed: WebhookEventType = WebhookEventType("organization.billing.invoice.payment_failed")

            /** A payment of an invoice addressed to the organization needs the payer to confirm it, as a card's own authentication asks. */
            public val OrganizationBillingInvoicePaymentActionRequired: WebhookEventType = WebhookEventType("organization.billing.invoice.payment_action_required")

            /** A payment method was saved to the organization. */
            public val OrganizationBillingPaymentMethodAttached: WebhookEventType = WebhookEventType("organization.billing.payment_method.attached")

            /** Which payment method the organization is charged on changed: one records it for the new default and one for the former. */
            public val OrganizationBillingPaymentMethodUpdated: WebhookEventType = WebhookEventType("organization.billing.payment_method.updated")

            /** A payment method was removed from the organization. */
            public val OrganizationBillingPaymentMethodDetached: WebhookEventType = WebhookEventType("organization.billing.payment_method.detached")

            /** What the organization's invoices are addressed to was written. */
            public val OrganizationBillingInfoUpdated: WebhookEventType = WebhookEventType("organization.billing.info.updated")

            /** A webhook endpoint was created. */
            public val OrganizationWebhookEndpointCreated: WebhookEventType = WebhookEventType("organization.webhook_endpoint.created")

            /** A webhook endpoint's URL, description or event types were written. */
            public val OrganizationWebhookEndpointUpdated: WebhookEventType = WebhookEventType("organization.webhook_endpoint.updated")

            /** A webhook endpoint was deleted, and nothing is delivered to it from then on. */
            public val OrganizationWebhookEndpointDeleted: WebhookEventType = WebhookEventType("organization.webhook_endpoint.deleted")

            /** A disabled webhook endpoint was enabled again. */
            public val OrganizationWebhookEndpointEnabled: WebhookEventType = WebhookEventType("organization.webhook_endpoint.enabled")

            /** A webhook endpoint was disabled: by a member, or by Mountaya after 72 hours of failed deliveries. `disabled_reason` says which. */
            public val OrganizationWebhookEndpointDisabled: WebhookEventType = WebhookEventType("organization.webhook_endpoint.disabled")

            /** Every delivery to a webhook endpoint has failed for 24 hours. Mountaya disables it at 72 hours unless one succeeds. */
            public val OrganizationWebhookEndpointFailing: WebhookEventType = WebhookEventType("organization.webhook_endpoint.failing")

            /** A delivery succeeded to a webhook endpoint reported failing: its run of failed deliveries is over. */
            public val OrganizationWebhookEndpointRecovered: WebhookEventType = WebhookEventType("organization.webhook_endpoint.recovered")

            /** A webhook endpoint's signing secret was replaced, signed in to Mountaya's web application. Both secrets sign deliveries for up to 7 days. */
            public val OrganizationWebhookEndpointSecretRotated: WebhookEventType = WebhookEventType("organization.webhook_endpoint.secret_rotated")
        }
    }

    /**
     * A webhook event with the deliveries made of it: one per endpoint it was
     * sent to, per notification email it caused, and per resend or redelivery
     * since. An event no destination was recorded for carries none.
     */
    @kotlinx.serialization.Serializable
    public class WebhookEventWithDeliveries private constructor(
        /**
         * Identifier of the event. Its leading bits are the moment it was recorded, so
         * identifiers sort by time. A delivery can arrive more than once, so a receiver
         * deduplicates on it.
         */
        public val id: kotlin.String,
        /** Always `webhook_event`. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "webhook_event",
        /** What happened. The family before its last dot names the kind of `data.object`. */
        public val type: AdminSchema.WebhookEventType,
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
        public val data: AdminSchema.WebhookEventData,
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
        /**
         * How many deliveries were made of the event, whatever `deliveries`
         * carries of them.
         */
        public val delivery_count: kotlin.Int,
        /**
         * The deliveries of the event, the first one started first: every one of
         * them up to 100, and the 100 most recently started when `delivery_count`
         * says there are more, which `getOrganizationWebhookDeliveries` pages in
         * full by `event_id`. Each carries its last attempt;
         * `getOrganizationWebhookDelivery` reads them all.
         */
        public val deliveries: kotlin.collections.List<AdminSchema.WebhookDelivery>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookEventWithDeliveries &&
                other.id == this.id &&
                other.`object` == this.`object` &&
                other.type == this.type &&
                other.api_version == this.api_version &&
                other.created_at == this.created_at &&
                other.organization_id == this.organization_id &&
                other.task == this.task &&
                other.related_object == this.related_object &&
                other.data == this.data &&
                other.request == this.request &&
                other.actor == this.actor &&
                other.delivery_count == this.delivery_count &&
                other.deliveries == this.deliveries

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.type.hashCode()
            hash = 31 * hash + this.api_version.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.task.hashCode()
            hash = 31 * hash + this.related_object.hashCode()
            hash = 31 * hash + this.data.hashCode()
            hash = 31 * hash + (this.request?.hashCode() ?: 0)
            hash = 31 * hash + this.actor.hashCode()
            hash = 31 * hash + this.delivery_count.hashCode()
            hash = 31 * hash + this.deliveries.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookEventWithDeliveries(" +
                "id=" + this.id +
                ", object=" + this.`object` +
                ", type=" + this.type +
                ", api_version=" + this.api_version +
                ", created_at=" + this.created_at +
                ", organization_id=" + this.organization_id +
                ", task=" + this.task +
                ", related_object=" + this.related_object +
                ", data=" + this.data +
                ", request=" + this.request +
                ", actor=" + this.actor +
                ", delivery_count=" + this.delivery_count +
                ", deliveries=" + this.deliveries +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookEventWithDeliveries], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookEventWithDeliveries]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookEventWithDeliveries] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                type: AdminSchema.WebhookEventType,
                api_version: kotlin.String,
                created_at: kotlin.time.Instant,
                organization_id: kotlin.String,
                task: kotlin.String,
                related_object: AdminSchema.WebhookEventRelatedObject,
                data: AdminSchema.WebhookEventData,
                request: AdminSchema.WebhookEventRequest?,
                actor: AdminSchema.WebhookEventActor,
                delivery_count: kotlin.Int,
                deliveries: kotlin.collections.List<AdminSchema.WebhookDelivery>,
                optional: AdminSchema.WebhookEventWithDeliveries.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookEventWithDeliveries {
                val given = AdminSchema.WebhookEventWithDeliveries.Builder().apply(optional)
                return AdminSchema.WebhookEventWithDeliveries(
                    id = id,
                    type = type,
                    api_version = api_version,
                    created_at = created_at,
                    organization_id = organization_id,
                    task = task,
                    related_object = related_object,
                    data = data,
                    request = request,
                    actor = actor,
                    delivery_count = delivery_count,
                    deliveries = deliveries,
                )
            }
        }
    }

    /**
     * Whose a failed attempt is. A receiver's failure counts against the endpoint's success
     * rate; Mountaya's counts against Mountaya's service availability, never against the
     * endpoint.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookFailureClass(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The receiver's: the endpoint, its network or its certificate, or the recipient's mailbox. */
            public val Receiver: WebhookFailureClass = WebhookFailureClass("receiver")

            /** Mountaya's: the attempt could not be made through no fault of the receiver. It is retried like any other. */
            public val Mountaya: WebhookFailureClass = WebhookFailureClass("mountaya")
        }
    }

    /**
     * How an attempt failed. `internal` is Mountaya's; every other kind is the receiver's.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookFailureKind(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The endpoint did not answer within 10 seconds. */
            public val Timeout: WebhookFailureKind = WebhookFailureKind("timeout")

            /** No connection could be made to the endpoint's host, or it closed before answering. */
            public val Connection: WebhookFailureKind = WebhookFailureKind("connection")

            /** The endpoint's TLS was refused: a certificate that does not verify, or a version older than 1.2. */
            public val Tls: WebhookFailureKind = WebhookFailureKind("tls")

            /** The endpoint answered a `3xx`, which is never followed. */
            public val Redirect: WebhookFailureKind = WebhookFailureKind("redirect")

            /** The endpoint answered a status other than `2xx` or `3xx`; `response_status` names it. */
            public val HttpStatus: WebhookFailureKind = WebhookFailureKind("http-status")

            /** The endpoint's host resolved to an address Mountaya never delivers to, such as a private or loopback one. */
            public val RefusedAddress: WebhookFailureKind = WebhookFailureKind("refused-address")

            /** The email bounced. */
            public val Bounced: WebhookFailureKind = WebhookFailureKind("bounced")

            /** The recipient's mail server does not know the address. */
            public val UnknownMailbox: WebhookFailureKind = WebhookFailureKind("unknown-mailbox")

            /** The address is on a blocklist, so the email was not sent. */
            public val Blocklisted: WebhookFailureKind = WebhookFailureKind("blocklisted")

            /** The recipient's mail server took the email for spam. */
            public val Spam: WebhookFailureKind = WebhookFailureKind("spam")

            /** Mountaya could not make the attempt, through no fault of the receiver. */
            public val Internal: WebhookFailureKind = WebhookFailureKind("internal")
        }
    }

    /**
     * The reliability of an organization's webhooks over a span of time: Mountaya's
     * service availability apart from each endpoint's success, as totals, as a
     * series of hours or days, and one line per destination. It covers the
     * organization's endpoints and its notification emails, or one endpoint.
     */
    @kotlinx.serialization.Serializable
    public class WebhookHealth private constructor(
        /** The endpoint the report is narrowed to, when it is. */
        public val webhook_endpoint_id: kotlin.String? = null,
        /** The first instant the report covers, inclusive. */
        public val start_at: kotlin.time.Instant,
        /** The instant the report ends at, exclusive. */
        public val end_at: kotlin.time.Instant,
        /** How long each point of `series` covers. */
        public val granularity: AdminSchema.WebhookHealthGranularity,
        /** The figures of the whole span. */
        public val totals: AdminSchema.WebhookHealthFigures,
        /**
         * The figures of each hour or day of the span, oldest first, every one of
         * them present, an empty one with zero counts and null rates.
         */
        public val series: kotlin.collections.List<AdminSchema.WebhookHealthPoint>,
        /**
         * One line per destination: each endpoint of the organization, or the one
         * endpoint the report is narrowed to, and the notification emails, on a
         * report that is not narrowed.
         */
        public val destinations: kotlin.collections.List<AdminSchema.WebhookHealthDestination>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookHealth &&
                other.webhook_endpoint_id == this.webhook_endpoint_id &&
                other.start_at == this.start_at &&
                other.end_at == this.end_at &&
                other.granularity == this.granularity &&
                other.totals == this.totals &&
                other.series == this.series &&
                other.destinations == this.destinations

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.webhook_endpoint_id?.hashCode() ?: 0)
            hash = 31 * hash + this.start_at.hashCode()
            hash = 31 * hash + this.end_at.hashCode()
            hash = 31 * hash + this.granularity.hashCode()
            hash = 31 * hash + this.totals.hashCode()
            hash = 31 * hash + this.series.hashCode()
            hash = 31 * hash + this.destinations.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookHealth(" +
                "webhook_endpoint_id=" + this.webhook_endpoint_id +
                ", start_at=" + this.start_at +
                ", end_at=" + this.end_at +
                ", granularity=" + this.granularity +
                ", totals=" + this.totals +
                ", series=" + this.series +
                ", destinations=" + this.destinations +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookHealth], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null

            /** The endpoint the report is narrowed to, when it is. */
            public var webhook_endpoint_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [AdminSchema.WebhookHealth]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookHealth] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                start_at: kotlin.time.Instant,
                end_at: kotlin.time.Instant,
                granularity: AdminSchema.WebhookHealthGranularity,
                totals: AdminSchema.WebhookHealthFigures,
                series: kotlin.collections.List<AdminSchema.WebhookHealthPoint>,
                destinations: kotlin.collections.List<AdminSchema.WebhookHealthDestination>,
                optional: AdminSchema.WebhookHealth.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookHealth {
                val given = AdminSchema.WebhookHealth.Builder().apply(optional)
                return AdminSchema.WebhookHealth(
                    webhook_endpoint_id = given.slot0,
                    start_at = start_at,
                    end_at = end_at,
                    granularity = granularity,
                    totals = totals,
                    series = series,
                    destinations = destinations,
                )
            }
        }
    }

    /**
     * One destination's line of a webhook reliability report: an endpoint, or the
     * organization's notification emails taken together.
     */
    @kotlinx.serialization.Serializable
    public class WebhookHealthDestination private constructor(
        /** Which kind of destination the line is. */
        public val destination: AdminSchema.WebhookDestinationKind,
        /** Identifier of the endpoint, on an endpoint's line. */
        public val webhook_endpoint_id: kotlin.String? = null,
        /**
         * Whether Mountaya delivers to the endpoint, on an endpoint's line. Absent
         * on the line of an endpoint the organization has deleted, which a report
         * narrowed to it still carries.
         */
        public val status: AdminSchema.WebhookEndpointStatus? = null,
        /**
         * When the endpoint's run of failed deliveries began, on an endpoint's
         * line, null while its last delivery succeeded.
         */
        public val failing_since: kotlin.time.Instant? = null,
        /**
         * When a delivery to the destination last succeeded. An endpoint's is
         * its own last success, however long ago, and null when it never had
         * one; the notification emails' is null when none succeeded in the 13
         * months deliveries are kept.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val last_success_at: kotlin.time.Instant? = null,
        /** The attempts made to the destination in the span. */
        public val attempts_made: kotlin.Long,
        /**
         * The share of the destination's deliveries that succeeded at their first
         * attempt, null when none was made.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val first_attempt_success_rate: kotlin.Double? = null,
        /**
         * The share of the destination's deliveries settled in the span that
         * succeeded, null when none settled.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val eventual_success_rate: kotlin.Double? = null,
        /** The destination's deliveries whose every attempt failed. */
        public val deliveries_exhausted: kotlin.Long,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookHealthDestination &&
                other.destination == this.destination &&
                other.webhook_endpoint_id == this.webhook_endpoint_id &&
                other.status == this.status &&
                other.failing_since == this.failing_since &&
                other.last_success_at == this.last_success_at &&
                other.attempts_made == this.attempts_made &&
                other.first_attempt_success_rate == this.first_attempt_success_rate &&
                other.eventual_success_rate == this.eventual_success_rate &&
                other.deliveries_exhausted == this.deliveries_exhausted

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.destination.hashCode()
            hash = 31 * hash + (this.webhook_endpoint_id?.hashCode() ?: 0)
            hash = 31 * hash + (this.status?.hashCode() ?: 0)
            hash = 31 * hash + (this.failing_since?.hashCode() ?: 0)
            hash = 31 * hash + (this.last_success_at?.hashCode() ?: 0)
            hash = 31 * hash + this.attempts_made.hashCode()
            hash = 31 * hash + (this.first_attempt_success_rate?.hashCode() ?: 0)
            hash = 31 * hash + (this.eventual_success_rate?.hashCode() ?: 0)
            hash = 31 * hash + this.deliveries_exhausted.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookHealthDestination(" +
                "destination=" + this.destination +
                ", webhook_endpoint_id=" + this.webhook_endpoint_id +
                ", status=" + this.status +
                ", failing_since=" + this.failing_since +
                ", last_success_at=" + this.last_success_at +
                ", attempts_made=" + this.attempts_made +
                ", first_attempt_success_rate=" + this.first_attempt_success_rate +
                ", eventual_success_rate=" + this.eventual_success_rate +
                ", deliveries_exhausted=" + this.deliveries_exhausted +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookHealthDestination], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot1: kotlin.String? = null
            internal var slot2: AdminSchema.WebhookEndpointStatus? = null
            internal var slot3: kotlin.time.Instant? = null

            /** Identifier of the endpoint, on an endpoint's line. */
            public var webhook_endpoint_id: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot1 = value
                }

            /**
             * Whether Mountaya delivers to the endpoint, on an endpoint's line. Absent
             * on the line of an endpoint the organization has deleted, which a report
             * narrowed to it still carries.
             */
            public var status: AdminSchema.WebhookEndpointStatus
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * When the endpoint's run of failed deliveries began, on an endpoint's
             * line, null while its last delivery succeeded.
             */
            public var failing_since: kotlin.time.Instant?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [AdminSchema.WebhookHealthDestination]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookHealthDestination] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                destination: AdminSchema.WebhookDestinationKind,
                last_success_at: kotlin.time.Instant?,
                attempts_made: kotlin.Long,
                first_attempt_success_rate: kotlin.Double?,
                eventual_success_rate: kotlin.Double?,
                deliveries_exhausted: kotlin.Long,
                optional: AdminSchema.WebhookHealthDestination.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookHealthDestination {
                val given = AdminSchema.WebhookHealthDestination.Builder().apply(optional)
                return AdminSchema.WebhookHealthDestination(
                    destination = destination,
                    webhook_endpoint_id = given.slot1,
                    status = given.slot2,
                    failing_since = given.slot3,
                    last_success_at = last_success_at,
                    attempts_made = attempts_made,
                    first_attempt_success_rate = first_attempt_success_rate,
                    eventual_success_rate = eventual_success_rate,
                    deliveries_exhausted = deliveries_exhausted,
                )
            }
        }
    }

    /** The failed attempts of one kind in a webhook reliability report. */
    @kotlinx.serialization.Serializable
    public class WebhookHealthFailure private constructor(
        /** How the attempts failed. */
        public val failure_kind: AdminSchema.WebhookFailureKind,
        /** Whose the failures are, which the kind decides. */
        public val failure_class: AdminSchema.WebhookFailureClass,
        /** How many attempts failed this way. */
        public val count: kotlin.Long,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookHealthFailure &&
                other.failure_kind == this.failure_kind &&
                other.failure_class == this.failure_class &&
                other.count == this.count

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.failure_kind.hashCode()
            hash = 31 * hash + this.failure_class.hashCode()
            hash = 31 * hash + this.count.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookHealthFailure(" +
                "failure_kind=" + this.failure_kind +
                ", failure_class=" + this.failure_class +
                ", count=" + this.count +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookHealthFailure], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookHealthFailure]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookHealthFailure] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                failure_kind: AdminSchema.WebhookFailureKind,
                failure_class: AdminSchema.WebhookFailureClass,
                count: kotlin.Long,
                optional: AdminSchema.WebhookHealthFailure.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookHealthFailure {
                val given = AdminSchema.WebhookHealthFailure.Builder().apply(optional)
                return AdminSchema.WebhookHealthFailure(
                    failure_kind = failure_kind,
                    failure_class = failure_class,
                    count = count,
                )
            }
        }
    }

    /**
     * The reliability figures of a span of time: what was recorded and delivered,
     * how fast, and how often it failed, split by whose the failure was. A rate is
     * a share between 0 and 1, null where nothing it divides by happened.
     */
    @kotlinx.serialization.Serializable
    public class WebhookHealthFigures private constructor(
        /** The public events recorded, whether or not anything was sent them. */
        public val events_recorded: kotlin.Long,
        /** The attempts the delivery schedule made due. */
        public val attempts_due: kotlin.Long,
        /** The attempts made. */
        public val attempts_made: kotlin.Long,
        /**
         * Mountaya's service availability: the share of the attempts due that
         * were made on schedule with no failure of Mountaya's. A receiver's
         * failure counts here as an attempt made; only a failure classed
         * `mountaya` and an attempt made late lower it.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val availability: kotlin.Double? = null,
        /**
         * The median time from an event being recorded to its first attempt, in
         * milliseconds.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val dispatch_delay_p50_ms: kotlin.Long? = null,
        /**
         * The 95th percentile of the time from an event being recorded to its
         * first attempt, in milliseconds.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val dispatch_delay_p95_ms: kotlin.Long? = null,
        /**
         * The share of the deliveries whose first attempt was made within 60
         * seconds of the event.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val dispatched_within_60s: kotlin.Double? = null,
        /** The share of the deliveries that succeeded at their first attempt. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val first_attempt_success_rate: kotlin.Double? = null,
        /**
         * The share of the deliveries settled in the span that succeeded, at any
         * attempt.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val eventual_success_rate: kotlin.Double? = null,
        /**
         * The median time an endpoint took to answer an attempt, in
         * milliseconds. Notification emails are left out.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val response_time_p50_ms: kotlin.Long? = null,
        /**
         * The 95th percentile of the time an endpoint took to answer an attempt,
         * in milliseconds. Notification emails are left out.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val response_time_p95_ms: kotlin.Long? = null,
        /** The deliveries that succeeded. */
        public val deliveries_succeeded: kotlin.Long,
        /**
         * The deliveries still unsettled at the end of the span: waiting for an
         * attempt, or for a retry.
         */
        public val deliveries_pending: kotlin.Long,
        /** The deliveries whose every attempt failed. */
        public val deliveries_exhausted: kotlin.Long,
        /** The deliveries canceled because their endpoint was disabled or deleted. */
        public val deliveries_canceled: kotlin.Long,
        /**
         * The failed attempts, one entry per kind that happened, the most frequent
         * first.
         */
        public val failures: kotlin.collections.List<AdminSchema.WebhookHealthFailure>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookHealthFigures &&
                other.events_recorded == this.events_recorded &&
                other.attempts_due == this.attempts_due &&
                other.attempts_made == this.attempts_made &&
                other.availability == this.availability &&
                other.dispatch_delay_p50_ms == this.dispatch_delay_p50_ms &&
                other.dispatch_delay_p95_ms == this.dispatch_delay_p95_ms &&
                other.dispatched_within_60s == this.dispatched_within_60s &&
                other.first_attempt_success_rate == this.first_attempt_success_rate &&
                other.eventual_success_rate == this.eventual_success_rate &&
                other.response_time_p50_ms == this.response_time_p50_ms &&
                other.response_time_p95_ms == this.response_time_p95_ms &&
                other.deliveries_succeeded == this.deliveries_succeeded &&
                other.deliveries_pending == this.deliveries_pending &&
                other.deliveries_exhausted == this.deliveries_exhausted &&
                other.deliveries_canceled == this.deliveries_canceled &&
                other.failures == this.failures

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.events_recorded.hashCode()
            hash = 31 * hash + this.attempts_due.hashCode()
            hash = 31 * hash + this.attempts_made.hashCode()
            hash = 31 * hash + (this.availability?.hashCode() ?: 0)
            hash = 31 * hash + (this.dispatch_delay_p50_ms?.hashCode() ?: 0)
            hash = 31 * hash + (this.dispatch_delay_p95_ms?.hashCode() ?: 0)
            hash = 31 * hash + (this.dispatched_within_60s?.hashCode() ?: 0)
            hash = 31 * hash + (this.first_attempt_success_rate?.hashCode() ?: 0)
            hash = 31 * hash + (this.eventual_success_rate?.hashCode() ?: 0)
            hash = 31 * hash + (this.response_time_p50_ms?.hashCode() ?: 0)
            hash = 31 * hash + (this.response_time_p95_ms?.hashCode() ?: 0)
            hash = 31 * hash + this.deliveries_succeeded.hashCode()
            hash = 31 * hash + this.deliveries_pending.hashCode()
            hash = 31 * hash + this.deliveries_exhausted.hashCode()
            hash = 31 * hash + this.deliveries_canceled.hashCode()
            hash = 31 * hash + this.failures.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookHealthFigures(" +
                "events_recorded=" + this.events_recorded +
                ", attempts_due=" + this.attempts_due +
                ", attempts_made=" + this.attempts_made +
                ", availability=" + this.availability +
                ", dispatch_delay_p50_ms=" + this.dispatch_delay_p50_ms +
                ", dispatch_delay_p95_ms=" + this.dispatch_delay_p95_ms +
                ", dispatched_within_60s=" + this.dispatched_within_60s +
                ", first_attempt_success_rate=" + this.first_attempt_success_rate +
                ", eventual_success_rate=" + this.eventual_success_rate +
                ", response_time_p50_ms=" + this.response_time_p50_ms +
                ", response_time_p95_ms=" + this.response_time_p95_ms +
                ", deliveries_succeeded=" + this.deliveries_succeeded +
                ", deliveries_pending=" + this.deliveries_pending +
                ", deliveries_exhausted=" + this.deliveries_exhausted +
                ", deliveries_canceled=" + this.deliveries_canceled +
                ", failures=" + this.failures +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookHealthFigures], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookHealthFigures]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookHealthFigures] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                events_recorded: kotlin.Long,
                attempts_due: kotlin.Long,
                attempts_made: kotlin.Long,
                availability: kotlin.Double?,
                dispatch_delay_p50_ms: kotlin.Long?,
                dispatch_delay_p95_ms: kotlin.Long?,
                dispatched_within_60s: kotlin.Double?,
                first_attempt_success_rate: kotlin.Double?,
                eventual_success_rate: kotlin.Double?,
                response_time_p50_ms: kotlin.Long?,
                response_time_p95_ms: kotlin.Long?,
                deliveries_succeeded: kotlin.Long,
                deliveries_pending: kotlin.Long,
                deliveries_exhausted: kotlin.Long,
                deliveries_canceled: kotlin.Long,
                failures: kotlin.collections.List<AdminSchema.WebhookHealthFailure>,
                optional: AdminSchema.WebhookHealthFigures.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookHealthFigures {
                val given = AdminSchema.WebhookHealthFigures.Builder().apply(optional)
                return AdminSchema.WebhookHealthFigures(
                    events_recorded = events_recorded,
                    attempts_due = attempts_due,
                    attempts_made = attempts_made,
                    availability = availability,
                    dispatch_delay_p50_ms = dispatch_delay_p50_ms,
                    dispatch_delay_p95_ms = dispatch_delay_p95_ms,
                    dispatched_within_60s = dispatched_within_60s,
                    first_attempt_success_rate = first_attempt_success_rate,
                    eventual_success_rate = eventual_success_rate,
                    response_time_p50_ms = response_time_p50_ms,
                    response_time_p95_ms = response_time_p95_ms,
                    deliveries_succeeded = deliveries_succeeded,
                    deliveries_pending = deliveries_pending,
                    deliveries_exhausted = deliveries_exhausted,
                    deliveries_canceled = deliveries_canceled,
                    failures = failures,
                )
            }
        }
    }

    /**
     * How long each point of a webhook reliability report's series covers.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookHealthGranularity(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** An hour, starting on the hour, in UTC. */
            public val Hour: WebhookHealthGranularity = WebhookHealthGranularity("hour")

            /** A UTC calendar day. */
            public val Day: WebhookHealthGranularity = WebhookHealthGranularity("day")
        }
    }

    /**
     * One point of a webhook reliability report's series: the figures of one hour
     * or one day.
     */
    @kotlinx.serialization.Serializable
    public class WebhookHealthPoint private constructor(
        /** The public events recorded, whether or not anything was sent them. */
        public val events_recorded: kotlin.Long,
        /** The attempts the delivery schedule made due. */
        public val attempts_due: kotlin.Long,
        /** The attempts made. */
        public val attempts_made: kotlin.Long,
        /**
         * Mountaya's service availability: the share of the attempts due that
         * were made on schedule with no failure of Mountaya's. A receiver's
         * failure counts here as an attempt made; only a failure classed
         * `mountaya` and an attempt made late lower it.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val availability: kotlin.Double? = null,
        /**
         * The median time from an event being recorded to its first attempt, in
         * milliseconds.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val dispatch_delay_p50_ms: kotlin.Long? = null,
        /**
         * The 95th percentile of the time from an event being recorded to its
         * first attempt, in milliseconds.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val dispatch_delay_p95_ms: kotlin.Long? = null,
        /**
         * The share of the deliveries whose first attempt was made within 60
         * seconds of the event.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val dispatched_within_60s: kotlin.Double? = null,
        /** The share of the deliveries that succeeded at their first attempt. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val first_attempt_success_rate: kotlin.Double? = null,
        /**
         * The share of the deliveries settled in the span that succeeded, at any
         * attempt.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val eventual_success_rate: kotlin.Double? = null,
        /**
         * The median time an endpoint took to answer an attempt, in
         * milliseconds. Notification emails are left out.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val response_time_p50_ms: kotlin.Long? = null,
        /**
         * The 95th percentile of the time an endpoint took to answer an attempt,
         * in milliseconds. Notification emails are left out.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val response_time_p95_ms: kotlin.Long? = null,
        /** The deliveries that succeeded. */
        public val deliveries_succeeded: kotlin.Long,
        /**
         * The deliveries still unsettled at the end of the span: waiting for an
         * attempt, or for a retry.
         */
        public val deliveries_pending: kotlin.Long,
        /** The deliveries whose every attempt failed. */
        public val deliveries_exhausted: kotlin.Long,
        /** The deliveries canceled because their endpoint was disabled or deleted. */
        public val deliveries_canceled: kotlin.Long,
        /**
         * The failed attempts, one entry per kind that happened, the most frequent
         * first.
         */
        public val failures: kotlin.collections.List<AdminSchema.WebhookHealthFailure>,
        /** When the hour or the day the point covers begins, in UTC. */
        public val start_at: kotlin.time.Instant,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.WebhookHealthPoint &&
                other.events_recorded == this.events_recorded &&
                other.attempts_due == this.attempts_due &&
                other.attempts_made == this.attempts_made &&
                other.availability == this.availability &&
                other.dispatch_delay_p50_ms == this.dispatch_delay_p50_ms &&
                other.dispatch_delay_p95_ms == this.dispatch_delay_p95_ms &&
                other.dispatched_within_60s == this.dispatched_within_60s &&
                other.first_attempt_success_rate == this.first_attempt_success_rate &&
                other.eventual_success_rate == this.eventual_success_rate &&
                other.response_time_p50_ms == this.response_time_p50_ms &&
                other.response_time_p95_ms == this.response_time_p95_ms &&
                other.deliveries_succeeded == this.deliveries_succeeded &&
                other.deliveries_pending == this.deliveries_pending &&
                other.deliveries_exhausted == this.deliveries_exhausted &&
                other.deliveries_canceled == this.deliveries_canceled &&
                other.failures == this.failures &&
                other.start_at == this.start_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.events_recorded.hashCode()
            hash = 31 * hash + this.attempts_due.hashCode()
            hash = 31 * hash + this.attempts_made.hashCode()
            hash = 31 * hash + (this.availability?.hashCode() ?: 0)
            hash = 31 * hash + (this.dispatch_delay_p50_ms?.hashCode() ?: 0)
            hash = 31 * hash + (this.dispatch_delay_p95_ms?.hashCode() ?: 0)
            hash = 31 * hash + (this.dispatched_within_60s?.hashCode() ?: 0)
            hash = 31 * hash + (this.first_attempt_success_rate?.hashCode() ?: 0)
            hash = 31 * hash + (this.eventual_success_rate?.hashCode() ?: 0)
            hash = 31 * hash + (this.response_time_p50_ms?.hashCode() ?: 0)
            hash = 31 * hash + (this.response_time_p95_ms?.hashCode() ?: 0)
            hash = 31 * hash + this.deliveries_succeeded.hashCode()
            hash = 31 * hash + this.deliveries_pending.hashCode()
            hash = 31 * hash + this.deliveries_exhausted.hashCode()
            hash = 31 * hash + this.deliveries_canceled.hashCode()
            hash = 31 * hash + this.failures.hashCode()
            hash = 31 * hash + this.start_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "WebhookHealthPoint(" +
                "events_recorded=" + this.events_recorded +
                ", attempts_due=" + this.attempts_due +
                ", attempts_made=" + this.attempts_made +
                ", availability=" + this.availability +
                ", dispatch_delay_p50_ms=" + this.dispatch_delay_p50_ms +
                ", dispatch_delay_p95_ms=" + this.dispatch_delay_p95_ms +
                ", dispatched_within_60s=" + this.dispatched_within_60s +
                ", first_attempt_success_rate=" + this.first_attempt_success_rate +
                ", eventual_success_rate=" + this.eventual_success_rate +
                ", response_time_p50_ms=" + this.response_time_p50_ms +
                ", response_time_p95_ms=" + this.response_time_p95_ms +
                ", deliveries_succeeded=" + this.deliveries_succeeded +
                ", deliveries_pending=" + this.deliveries_pending +
                ", deliveries_exhausted=" + this.deliveries_exhausted +
                ", deliveries_canceled=" + this.deliveries_canceled +
                ", failures=" + this.failures +
                ", start_at=" + this.start_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.WebhookHealthPoint], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.WebhookHealthPoint]. */
        public companion object {
            /** Returns a [AdminSchema.WebhookHealthPoint] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                events_recorded: kotlin.Long,
                attempts_due: kotlin.Long,
                attempts_made: kotlin.Long,
                availability: kotlin.Double?,
                dispatch_delay_p50_ms: kotlin.Long?,
                dispatch_delay_p95_ms: kotlin.Long?,
                dispatched_within_60s: kotlin.Double?,
                first_attempt_success_rate: kotlin.Double?,
                eventual_success_rate: kotlin.Double?,
                response_time_p50_ms: kotlin.Long?,
                response_time_p95_ms: kotlin.Long?,
                deliveries_succeeded: kotlin.Long,
                deliveries_pending: kotlin.Long,
                deliveries_exhausted: kotlin.Long,
                deliveries_canceled: kotlin.Long,
                failures: kotlin.collections.List<AdminSchema.WebhookHealthFailure>,
                start_at: kotlin.time.Instant,
                optional: AdminSchema.WebhookHealthPoint.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.WebhookHealthPoint {
                val given = AdminSchema.WebhookHealthPoint.Builder().apply(optional)
                return AdminSchema.WebhookHealthPoint(
                    events_recorded = events_recorded,
                    attempts_due = attempts_due,
                    attempts_made = attempts_made,
                    availability = availability,
                    dispatch_delay_p50_ms = dispatch_delay_p50_ms,
                    dispatch_delay_p95_ms = dispatch_delay_p95_ms,
                    dispatched_within_60s = dispatched_within_60s,
                    first_attempt_success_rate = first_attempt_success_rate,
                    eventual_success_rate = eventual_success_rate,
                    response_time_p50_ms = response_time_p50_ms,
                    response_time_p95_ms = response_time_p95_ms,
                    deliveries_succeeded = deliveries_succeeded,
                    deliveries_pending = deliveries_pending,
                    deliveries_exhausted = deliveries_exhausted,
                    deliveries_canceled = deliveries_canceled,
                    failures = failures,
                    start_at = start_at,
                )
            }
        }
    }

    /**
     * The window a webhook reliability report covers, ending at the hour the report is asked
     * in.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class WebhookHealthWindow(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** The last 24 hours, an hour at a time. */
            public val Window24h: WebhookHealthWindow = WebhookHealthWindow("24h")

            /** The last 7 days, an hour at a time. */
            public val Window7d: WebhookHealthWindow = WebhookHealthWindow("7d")

            /** The last 30 days, a day at a time. */
            public val Window30d: WebhookHealthWindow = WebhookHealthWindow("30d")
        }
    }

    /**
     * A zone as a webhook event carries it: its identifiers, its name and description, its
     * level, mode and validity window, its status, whether it is private, and its version.
     * Its geometry and source are never carried: `getZone` reads them.
     *
     * Every string it carries is bounded, and it carries no geometry, no file and no
     * credential, so a payload stays small whatever the resource holds.
     */
    @kotlinx.serialization.Serializable
    public class ZoneEventObject private constructor(
        /**
         * The kind of object this is, always `zone`: the family of the event types that
         * carry it, and what tells the members of `WebhookEventObject` apart.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val `object`: kotlin.String = "zone",
        /** Identifier of the zone. */
        public val id: kotlin.String,
        /** Identifier of the organization the zone belongs to. */
        public val organization_id: kotlin.String,
        /** The zone's name. */
        public val name: kotlin.String,
        /** The zone's description, null when it has none. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val description: kotlin.String? = null,
        /** The zone's severity level. */
        public val level: com.mountaya.client.generated.RestSchema.ZoneLevel,
        /** How the zone's area was drawn. */
        public val mode: com.mountaya.client.generated.RestSchema.ZoneMode,
        /** Whether the zone has a live publication once this change was made. */
        public val status: com.mountaya.client.generated.RestSchema.ZoneStatus,
        /** Whether the zone is private to the organization. */
        public val is_private: kotlin.Boolean,
        /** When the zone's validity window opens, null when it has always been in force. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val start_at: kotlin.time.Instant? = null,
        /** When the zone's validity window closes, null when it never lapses. */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val end_at: kotlin.time.Instant? = null,
        /**
         * The zone's version once this change was made. It climbs with every change a read
         * of the zone shows, so of two events about one zone the one carrying the higher
         * `version` is the later state: events are not delivered in order. A delete is the
         * zone's last version change: `zone.deleted` carries the version one past the last
         * the zone held, so an event about it that arrives after its delete is never the
         * later state.
         */
        public val version: kotlin.Long,
        /** The user who created the zone. */
        public val created_by: com.mountaya.client.generated.RestSchema.RelationToUser,
        /** When the zone was created. */
        public val created_at: kotlin.time.Instant,
        /** When the zone last changed. */
        public val updated_at: kotlin.time.Instant,
    ) : AdminSchema.WebhookEventObject() {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ZoneEventObject &&
                other.`object` == this.`object` &&
                other.id == this.id &&
                other.organization_id == this.organization_id &&
                other.name == this.name &&
                other.description == this.description &&
                other.level == this.level &&
                other.mode == this.mode &&
                other.status == this.status &&
                other.is_private == this.is_private &&
                other.start_at == this.start_at &&
                other.end_at == this.end_at &&
                other.version == this.version &&
                other.created_by == this.created_by &&
                other.created_at == this.created_at &&
                other.updated_at == this.updated_at

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.`object`.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.organization_id.hashCode()
            hash = 31 * hash + this.name.hashCode()
            hash = 31 * hash + (this.description?.hashCode() ?: 0)
            hash = 31 * hash + this.level.hashCode()
            hash = 31 * hash + this.mode.hashCode()
            hash = 31 * hash + this.status.hashCode()
            hash = 31 * hash + this.is_private.hashCode()
            hash = 31 * hash + (this.start_at?.hashCode() ?: 0)
            hash = 31 * hash + (this.end_at?.hashCode() ?: 0)
            hash = 31 * hash + this.version.hashCode()
            hash = 31 * hash + this.created_by.hashCode()
            hash = 31 * hash + this.created_at.hashCode()
            hash = 31 * hash + this.updated_at.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ZoneEventObject(" +
                "object=" + this.`object` +
                ", id=" + this.id +
                ", organization_id=" + this.organization_id +
                ", name=" + this.name +
                ", description=" + this.description +
                ", level=" + this.level +
                ", mode=" + this.mode +
                ", status=" + this.status +
                ", is_private=" + this.is_private +
                ", start_at=" + this.start_at +
                ", end_at=" + this.end_at +
                ", version=" + this.version +
                ", created_by=" + this.created_by +
                ", created_at=" + this.created_at +
                ", updated_at=" + this.updated_at +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ZoneEventObject], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.ZoneEventObject]. */
        public companion object {
            /** Returns a [AdminSchema.ZoneEventObject] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                organization_id: kotlin.String,
                name: kotlin.String,
                description: kotlin.String?,
                level: com.mountaya.client.generated.RestSchema.ZoneLevel,
                mode: com.mountaya.client.generated.RestSchema.ZoneMode,
                status: com.mountaya.client.generated.RestSchema.ZoneStatus,
                is_private: kotlin.Boolean,
                start_at: kotlin.time.Instant?,
                end_at: kotlin.time.Instant?,
                version: kotlin.Long,
                created_by: com.mountaya.client.generated.RestSchema.RelationToUser,
                created_at: kotlin.time.Instant,
                updated_at: kotlin.time.Instant,
                optional: AdminSchema.ZoneEventObject.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ZoneEventObject {
                val given = AdminSchema.ZoneEventObject.Builder().apply(optional)
                return AdminSchema.ZoneEventObject(
                    id = id,
                    organization_id = organization_id,
                    name = name,
                    description = description,
                    level = level,
                    mode = mode,
                    status = status,
                    is_private = is_private,
                    start_at = start_at,
                    end_at = end_at,
                    version = version,
                    created_by = created_by,
                    created_at = created_at,
                    updated_at = updated_at,
                )
            }
        }
    }

    @kotlinx.serialization.Serializable
    public class ResponseMetadataPostOrganizationInvitationMember private constructor(
        /** Identifier of the member, as a member listing carries it. */
        public val user_id: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ResponseMetadataPostOrganizationInvitationMember &&
                other.user_id == this.user_id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.user_id.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ResponseMetadataPostOrganizationInvitationMember(" +
                "user_id=" + this.user_id +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ResponseMetadataPostOrganizationInvitationMember], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.ResponseMetadataPostOrganizationInvitationMember]. */
        public companion object {
            /** Returns a [AdminSchema.ResponseMetadataPostOrganizationInvitationMember] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                user_id: kotlin.String,
                optional: AdminSchema.ResponseMetadataPostOrganizationInvitationMember.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ResponseMetadataPostOrganizationInvitationMember {
                val given = AdminSchema.ResponseMetadataPostOrganizationInvitationMember.Builder().apply(optional)
                return AdminSchema.ResponseMetadataPostOrganizationInvitationMember(
                    user_id = user_id,
                )
            }
        }
    }

    @kotlinx.serialization.Serializable
    public class ResponseMetadataPostOrganizationInvitationInvitation private constructor(
        /**
         * Identifier of the invitation, which `getOrganizationInvitation`
         * reads and `deleteOrganizationInvitation` withdraws.
         */
        public val id: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation &&
                other.id == this.id

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "ResponseMetadataPostOrganizationInvitationInvitation(" +
                "id=" + this.id +
                ")"

        /** Builder sets the optional properties of a [AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation]. */
        public companion object {
            /** Returns a [AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                optional: AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation.Builder.() -> kotlin.Unit = {},
            ): AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation {
                val given = AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation.Builder().apply(optional)
                return AdminSchema.ResponseMetadataPostOrganizationInvitationInvitation(
                    id = id,
                )
            }
        }
    }

    /** It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today. */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class GetOrganizationInvitationsSort(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Sent as `-created_at`. */
            public val CreatedAtDescending: GetOrganizationInvitationsSort = GetOrganizationInvitationsSort("-created_at")

            /** Sent as `created_at`. */
            public val CreatedAt: GetOrganizationInvitationsSort = GetOrganizationInvitationsSort("created_at")
        }
    }

    /** It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today. */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class GetOrganizationMembersSort(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Sent as `joined_at`. */
            public val JoinedAt: GetOrganizationMembersSort = GetOrganizationMembersSort("joined_at")

            /** Sent as `-joined_at`. */
            public val JoinedAtDescending: GetOrganizationMembersSort = GetOrganizationMembersSort("-joined_at")
        }
    }

    /** It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today. */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class GetOrganizationPublishableKeysSort(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Sent as `-created_at`. */
            public val CreatedAtDescending: GetOrganizationPublishableKeysSort = GetOrganizationPublishableKeysSort("-created_at")

            /** Sent as `created_at`. */
            public val CreatedAt: GetOrganizationPublishableKeysSort = GetOrganizationPublishableKeysSort("created_at")

            /** Sent as `name`. */
            public val Name: GetOrganizationPublishableKeysSort = GetOrganizationPublishableKeysSort("name")

            /** Sent as `-name`. */
            public val NameDescending: GetOrganizationPublishableKeysSort = GetOrganizationPublishableKeysSort("-name")
        }
    }

    /** DeleteMyInvitationParameters sets the optional parameters of `deleteMyInvitation`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class DeleteMyInvitationParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** DeleteOrganizationAiProviderConfigParameters sets the optional parameters of `deleteOrganizationAiProviderConfig`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class DeleteOrganizationAiProviderConfigParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null
        internal var ifMatchSlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }

        /** The `etag` the caller read the resource under, or `"*"`, sent as `If-Match` exactly as given, and nothing is sent where it is left unset. */
        public var ifMatch: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                ifMatchSlot = value
            }
    }

    /** DeleteOrganizationBillingPaymentMethodParameters sets the optional parameters of `deleteOrganizationBillingPaymentMethod`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class DeleteOrganizationBillingPaymentMethodParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** DeleteOrganizationBillingSubscriptionParameters sets the optional parameters of `deleteOrganizationBillingSubscription`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class DeleteOrganizationBillingSubscriptionParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** DeleteOrganizationInvitationParameters sets the optional parameters of `deleteOrganizationInvitation`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class DeleteOrganizationInvitationParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** DeleteOrganizationMemberParameters sets the optional parameters of `deleteOrganizationMember`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class DeleteOrganizationMemberParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** DeleteOrganizationPublishableKeyParameters sets the optional parameters of `deleteOrganizationPublishableKey`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class DeleteOrganizationPublishableKeyParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null
        internal var ifMatchSlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }

        /** The `etag` the caller read the resource under, or `"*"`, sent as `If-Match` exactly as given, and nothing is sent where it is left unset. */
        public var ifMatch: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                ifMatchSlot = value
            }
    }

    /** DeleteOrganizationWebhookEndpointParameters sets the optional parameters of `deleteOrganizationWebhookEndpoint`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class DeleteOrganizationWebhookEndpointParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null
        internal var ifMatchSlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }

        /** The `etag` the caller read the resource under, or `"*"`, sent as `If-Match` exactly as given, and nothing is sent where it is left unset. */
        public var ifMatch: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                ifMatchSlot = value
            }
    }

    /** GetAiProvidersParameters sets the optional parameters of `getAiProviders`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetAiProvidersParameters internal constructor() {
    }

    /** GetBillingProductsParameters sets the optional parameters of `getBillingProducts`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetBillingProductsParameters internal constructor() {
    }

    /** GetMyInvitationsParameters sets the optional parameters of `getMyInvitations`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetMyInvitationsParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }
    }

    /** GetMyOrganizationNotificationPreferencesParameters sets the optional parameters of `getMyOrganizationNotificationPreferences`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetMyOrganizationNotificationPreferencesParameters internal constructor() {
    }

    /** GetMyOrganizationsParameters sets the optional parameters of `getMyOrganizations`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetMyOrganizationsParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }
    }

    /** GetMyPreferencesParameters sets the optional parameters of `getMyPreferences`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetMyPreferencesParameters internal constructor() {
    }

    /** GetOrganizationParameters sets the optional parameters of `getOrganization`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationParameters internal constructor() {
    }

    /** GetOrganizationAiProviderConfigParameters sets the optional parameters of `getOrganizationAiProviderConfig`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationAiProviderConfigParameters internal constructor() {
    }

    /** GetOrganizationBillingInfoParameters sets the optional parameters of `getOrganizationBillingInfo`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationBillingInfoParameters internal constructor() {
    }

    /** GetOrganizationBillingInvoicesParameters sets the optional parameters of `getOrganizationBillingInvoices`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationBillingInvoicesParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null
        internal var slot2: AdminSchema.InvoiceStatus? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /** Keep only the invoices that stand at this status. */
        public var status: AdminSchema.InvoiceStatus
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }
    }

    /** GetOrganizationBillingPaymentMethodsParameters sets the optional parameters of `getOrganizationBillingPaymentMethods`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationBillingPaymentMethodsParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }
    }

    /** GetOrganizationBillingPromotionCodeParameters sets the optional parameters of `getOrganizationBillingPromotionCode`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationBillingPromotionCodeParameters internal constructor() {
    }

    /** GetOrganizationBillingSubscriptionParameters sets the optional parameters of `getOrganizationBillingSubscription`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationBillingSubscriptionParameters internal constructor() {
    }

    /** GetOrganizationInvitationParameters sets the optional parameters of `getOrganizationInvitation`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationInvitationParameters internal constructor() {
    }

    /** GetOrganizationInvitationsParameters sets the optional parameters of `getOrganizationInvitations`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationInvitationsParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null
        internal var slot2: AdminSchema.AssignableOrganizationRole? = null
        internal var slot3: AdminSchema.GetOrganizationInvitationsSort? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /**
         * List only the invitations granting this role on acceptance. Omitted,
         * every pending invitation is listed.
         */
        public var role: AdminSchema.AssignableOrganizationRole
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }

        /**
         * The order of the listing: `-created_at`, the newest invitation first,
         * which is the default, or `created_at`, the oldest first. Invitations made
         * at the same instant are ordered by `id`, so the order is total and a page
         * never repeats or skips one of them.
         *
         * Left out, the server applies its default: `"-created_at"`.
         */
        public var sort: AdminSchema.GetOrganizationInvitationsSort
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot3 = value
            }
    }

    /** GetOrganizationMembersParameters sets the optional parameters of `getOrganizationMembers`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationMembersParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null
        internal var slot2: com.mountaya.client.generated.RestSchema.OrganizationRole? = null
        internal var slot3: AdminSchema.GetOrganizationMembersSort? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /**
         * List only the members holding this role. Omitted, every member is
         * listed. `owner` names the one owner.
         */
        public var role: com.mountaya.client.generated.RestSchema.OrganizationRole
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }

        /**
         * The order of the listing: `joined_at`, the earliest to join first, which
         * is the default, or `-joined_at`, the latest first. Members who joined at
         * the same instant are ordered by `user_id`, so the order is total and a
         * page never repeats or skips one of them.
         *
         * Left out, the server applies its default: `"joined_at"`.
         */
        public var sort: AdminSchema.GetOrganizationMembersSort
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot3 = value
            }
    }

    /** GetOrganizationPublishableKeyParameters sets the optional parameters of `getOrganizationPublishableKey`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationPublishableKeyParameters internal constructor() {
    }

    /** GetOrganizationPublishableKeysParameters sets the optional parameters of `getOrganizationPublishableKeys`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationPublishableKeysParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null
        internal var slot2: AdminSchema.PublishableKeyScope? = null
        internal var slot3: AdminSchema.GetOrganizationPublishableKeysSort? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /**
         * Keep only the keys given this scope. It reads the scopes a key carries,
         * not the ones they include, so a key carrying `studio-embedded`, `tiles`
         * and `data` is not kept for `publishing`.
         */
        public var scope: AdminSchema.PublishableKeyScope
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }

        /**
         * The order of the listing, by one field: `created_at` or `name`,
         * ascending, or either with a leading `-`, descending. Omitted, it is
         * `-created_at`, the most recently created first. Publishable keys that
         * tie on the field are ordered by `id`.
         *
         * Left out, the server applies its default: `"-created_at"`.
         */
        public var sort: AdminSchema.GetOrganizationPublishableKeysSort
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot3 = value
            }
    }

    /** GetOrganizationSettingsParameters sets the optional parameters of `getOrganizationSettings`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationSettingsParameters internal constructor() {
    }

    /** GetOrganizationUsageDataParameters sets the optional parameters of `getOrganizationUsageData`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationUsageDataParameters internal constructor() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.String? = null
        internal var slot2: kotlin.String? = null
        internal var slot3: AdminSchema.UsageDataResourceType? = null
        internal var slot4: AdminSchema.UsageMetric? = null
        internal var slot5: AdminSchema.UsageDataSurface? = null

        /**
         * First day to report, inclusive, as a UTC calendar day. Omitted together with
         * `end_at`, the range is the current month plus the three complete months before
         * it. Sent alone, the range runs from it to the end of the current month, or to
         * the day 365 days after it when that comes first. Sent alone, a `start_at`
         * after the last day of the current month opens no range and is a `400` with
         * one entry, at `start_at`. A range is at most 366 calendar days, both ends
         * included, so `end_at` is at most 365 days after `start_at`. A `start_at`
         * after `end_at` is a `400`, and so is a wider range, with one entry at
         * `start_at` and one at `end_at`.
         */
        public var start_at: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * Last day to report, inclusive, as a UTC calendar day. Sent alone, the range
         * starts on the day 365 days before it, so it spans 366 calendar days, the
         * most a range may. A wider range, an `end_at` more than 365 days after
         * `start_at`, is a `400` with one entry at `start_at` and one at `end_at`.
         */
        public var end_at: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /**
         * Report only the usage that arrived on one publishable key, named by its `pk_`
         * value. It is spelled `used_publishable_key` rather than `publishable_key`
         * because the shorter name is how a credential is presented on a query string,
         * and a filter that collided with it would change who the request is
         * authenticated as. The `studio-online` and `share` figures are drawn by pages
         * of Mountaya's web application, under a key of its own rather than one of the
         * organization's, so a filter on one of the organization's keys leaves them
         * out.
         */
        public var used_publishable_key: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }

        /**
         * Report only one Data API operation. Combined with `metric`, the pair
         * must be one that exists; an impossible pair is a 400.
         */
        public var resource_type: AdminSchema.UsageDataResourceType
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot3 = value
            }

        /**
         * Report only one metric. Combined with `resource_type`, the pair must be
         * one that exists: `polygons` belongs to `isochrones` and nothing else, so
         * asking for it beside `directions` is a 400.
         */
        public var metric: AdminSchema.UsageMetric
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot4 = value
            }

        /**
         * Report only one surface. Omitted, all four are reported. Only `api` and
         * `mcp` count toward the per-minute limit, the monthly allowance and the
         * invoice; `studio-online` and `studio-embedded` are the work Map Studio in
         * Mountaya's web application and an Embedded Studio did, reported and never
         * counted.
         */
        public var surface: AdminSchema.UsageDataSurface
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot5 = value
            }
    }

    /** GetOrganizationUsageTilesParameters sets the optional parameters of `getOrganizationUsageTiles`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationUsageTilesParameters internal constructor() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.String? = null
        internal var slot2: kotlin.String? = null
        internal var slot3: AdminSchema.UsageTileResourceType? = null
        internal var slot4: kotlin.String? = null

        /**
         * First day to report, inclusive, as a UTC calendar day. Omitted together with
         * `end_at`, the range is the current month plus the three complete months before
         * it. Sent alone, the range runs from it to the end of the current month, or to
         * the day 365 days after it when that comes first. Sent alone, a `start_at`
         * after the last day of the current month opens no range and is a `400` with
         * one entry, at `start_at`. A range is at most 366 calendar days, both ends
         * included, so `end_at` is at most 365 days after `start_at`. A `start_at`
         * after `end_at` is a `400`, and so is a wider range, with one entry at
         * `start_at` and one at `end_at`.
         */
        public var start_at: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * Last day to report, inclusive, as a UTC calendar day. Sent alone, the range
         * starts on the day 365 days before it, so it spans 366 calendar days, the
         * most a range may. A wider range, an `end_at` more than 365 days after
         * `start_at`, is a `400` with one entry at `start_at` and one at `end_at`.
         */
        public var end_at: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /**
         * Report only the usage that arrived on one publishable key, named by its `pk_`
         * value. It is spelled `used_publishable_key` rather than `publishable_key`
         * because the shorter name is how a credential is presented on a query string,
         * and a filter that collided with it would change who the request is
         * authenticated as. The `studio-online` and `share` figures are drawn by pages
         * of Mountaya's web application, under a key of its own rather than one of the
         * organization's, so a filter on one of the organization's keys leaves them
         * out.
         */
        public var used_publishable_key: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }

        /**
         * Report only one kind of resource. The values follow the tile ROUTES
         * rather than the resources themselves, which is why `itinerary` and
         * `itineraries` are both here: the first counts one itinerary's own tiles
         * and the second the aggregate layer covering every itinerary the caller
         * reaches. A request refused, or failed, before any tile route ran, on the
         * products the organization's agreement grants, the key's scope or the
         * rate limit, is recorded under the resource its address names, so each
         * kind reports its refusals beside its traffic; one for the base map under
         * `terrain`. A request that names no resource, an address no route serves,
         * an identifier that does not parse, or an overlay or a terrain the catalog
         * does not list, is counted nowhere. No Tile API request asks for a
         * payment method, so none is refused for one. A request refused for its
         * key, its session token or its calling page is counted nowhere.
         *
         * It combines with `resource_id` rather than constraining it. The two are
         * independent filters, so a pair that names a resource of the other kind is
         * not a 400: it is a report with nothing in it.
         */
        public var resource_type: AdminSchema.UsageTileResourceType
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot3 = value
            }

        /**
         * Report only one resource, by its identifier. A `terrain` figure is
         * recorded under the `id` the layer catalog gives the terrain its address
         * names, or the elevation raster's own `id`.
         */
        public var resource_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot4 = value
            }
    }

    /** GetOrganizationWebhookDeliveriesParameters sets the optional parameters of `getOrganizationWebhookDeliveries`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationWebhookDeliveriesParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null
        internal var slot2: kotlin.String? = null
        internal var slot3: kotlin.String? = null
        internal var slot4: AdminSchema.WebhookDestinationKind? = null
        internal var slot5: AdminSchema.WebhookDeliveryOutcome? = null
        internal var slot6: AdminSchema.WebhookFailureClass? = null
        internal var slot7: AdminSchema.WebhookFailureKind? = null
        internal var slot8: AdminSchema.WebhookEventType? = null
        internal var slot9: kotlin.String? = null
        internal var slot10: kotlin.time.Instant? = null
        internal var slot11: kotlin.time.Instant? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /**
         * Keep only what concerns this webhook endpoint of the organization: one it
         * holds, or one it deleted, whose deliveries stay readable for the 13 months
         * they are kept. One that names no endpoint the organization held in those 13
         * months is a `400` naming it. Omitted, every destination is covered, the
         * notification emails included.
         */
        public var webhook_endpoint_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }

        /**
         * Keep only the deliveries of this event, by the `id`
         * `getOrganizationWebhookEvents` lists: every one of them, where
         * `getOrganizationWebhookEvent` carries the 100 most recently started.
         */
        public var event_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot3 = value
            }

        /**
         * Keep only the deliveries to this kind of destination: `email` keeps the
         * notification emails.
         */
        public var destination: AdminSchema.WebhookDestinationKind
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot4 = value
            }

        /** Keep only the deliveries standing so. */
        public var outcome: AdminSchema.WebhookDeliveryOutcome
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot5 = value
            }

        /**
         * Keep only the deliveries whose last attempt failed, and whose failure is this
         * one's.
         */
        public var failure_class: AdminSchema.WebhookFailureClass
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot6 = value
            }

        /** Keep only the deliveries whose last attempt failed this way. */
        public var failure_kind: AdminSchema.WebhookFailureKind
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot7 = value
            }

        /** Keep only the deliveries of events of this type. */
        public var event_type: AdminSchema.WebhookEventType
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot8 = value
            }

        /**
         * Keep only the deliveries one resend, test or redelivery started, by the `id`
         * `postOrganizationWebhookDelivery` answered.
         */
        public var trigger_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot9 = value
            }

        /**
         * The first instant covered, inclusive, as an RFC 3339 date-time. Events and
         * deliveries are kept 13 months, so an instant before that covers nothing
         * more. A `start_at` after `end_at` is a `400` naming both.
         */
        public var start_at: kotlin.time.Instant
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot10 = value
            }

        /**
         * The instant the range ends at, exclusive, as an RFC 3339 date-time.
         * Omitted, the range runs to the moment the request is answered.
         */
        public var end_at: kotlin.time.Instant
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot11 = value
            }
    }

    /** GetOrganizationWebhookDeliveryParameters sets the optional parameters of `getOrganizationWebhookDelivery`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationWebhookDeliveryParameters internal constructor() {
    }

    /** GetOrganizationWebhookEndpointParameters sets the optional parameters of `getOrganizationWebhookEndpoint`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationWebhookEndpointParameters internal constructor() {
    }

    /** GetOrganizationWebhookEndpointsParameters sets the optional parameters of `getOrganizationWebhookEndpoints`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationWebhookEndpointsParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }
    }

    /** GetOrganizationWebhookEventParameters sets the optional parameters of `getOrganizationWebhookEvent`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationWebhookEventParameters internal constructor() {
    }

    /** GetOrganizationWebhookEventsParameters sets the optional parameters of `getOrganizationWebhookEvents`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationWebhookEventsParameters internal constructor() : com.mountaya.client.PagedParameters() {
        internal var slot0: kotlin.String? = null
        internal var slot1: kotlin.Int? = null
        internal var slot2: AdminSchema.WebhookEventType? = null
        internal var slot3: kotlin.String? = null
        internal var slot4: kotlin.String? = null
        internal var slot5: kotlin.String? = null
        internal var slot6: AdminSchema.WebhookEventActorType? = null
        internal var slot7: kotlin.String? = null
        internal var slot8: AdminSchema.WebhookDeliveryOutcome? = null
        internal var slot9: kotlin.time.Instant? = null
        internal var slot10: kotlin.time.Instant? = null

        @com.mountaya.client.InternalMountayaApi
        override var pageCursor: kotlin.String?
            get() = slot0
            set(value) {
                slot0 = value
            }

        @com.mountaya.client.InternalMountayaApi
        override var pageLimit: kotlin.Int?
            get() = slot1
            set(value) {
                slot1 = value
            }

        /**
         * Where the page starts: the `next_cursor` the previous page reported at
         * `extensions.metadata.pagination`, sent back unchanged. Omitted, the listing
         * starts at its first item.
         *
         * A cursor is opaque: it is not an offset and not an identifier, and a client
         * never builds or edits one, and it is never longer than 1024 characters. It
         * is bound to the operation that issued it, to its filters and its `sort`,
         * and to the scope it listed, such as the organization or the collection in
         * the path or the key's own organization. On `getItineraries`, `getZones`
         * and `getCollections`, whose scope `organization_id` may leave implicit, it
         * is bound as well to the tier of the credential it was issued to, a key
         * alone (or with a session token minted without `user_id`, which reads as
         * one) or a key with a session token naming a member. The next page is asked
         * for the same way. A cursor sent to another operation, with different
         * filters, a different `sort`, another scope or, on those three, another tier
         * of credential, or altered in any way, is a `400` at
         * `["request", "query", "cursor"]`. On every listing, each page applies the
         * read tier of the credential that asks for it. Paging is by position in the
         * sort order rather than by count, so an item whose sort key does not change
         * is never skipped or served twice, whatever is created or deleted between
         * two pages.
         */
        public var cursor: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * How many items one page holds, between 1 and 100; 50 when omitted, on every
         * listing that pages. `extensions.metadata.pagination.limit` reports the size
         * the page was served at. A page shorter than `limit` is not how the end is
         * told: the end is a `next_cursor` of `null`. The page is cut after
         * authorization, so it never widens what a caller can read.
         *
         * Left out, the server applies its default: `50`.
         */
        public var limit: kotlin.Int
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /** Keep only the events of this type. */
        public var type: AdminSchema.WebhookEventType
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }

        /**
         * Keep only the events of this family, every type of it: `organization.member` keeps
         * `organization.member.created`, `.updated` and `.deleted`. A family the contract
         * does not publish is a `400`.
         */
        public var family: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot3 = value
            }

        /**
         * Keep only the events about this object, the `related_object.id` they carry: a
         * UUID, or an invoice's or a payment method's identifier.
         */
        public var related_object_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot4 = value
            }

        /**
         * Keep only the events this API request recorded: the `X-Request-Id` its answer
         * carried, 32 lowercase hexadecimal characters.
         */
        public var request_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot5 = value
            }

        /** Keep only the events this kind of actor made. */
        public var actor_type: AdminSchema.WebhookEventActorType
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot6 = value
            }

        /** Keep only the events this person made. */
        public var actor_user_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot7 = value
            }

        /**
         * Keep only the events with at least one delivery standing so: `exhausted` lists
         * what an endpoint never received.
         */
        public var delivery_outcome: AdminSchema.WebhookDeliveryOutcome
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot8 = value
            }

        /**
         * The first instant covered, inclusive, as an RFC 3339 date-time. Events and
         * deliveries are kept 13 months, so an instant before that covers nothing
         * more. A `start_at` after `end_at` is a `400` naming both.
         */
        public var start_at: kotlin.time.Instant
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot9 = value
            }

        /**
         * The instant the range ends at, exclusive, as an RFC 3339 date-time.
         * Omitted, the range runs to the moment the request is answered.
         */
        public var end_at: kotlin.time.Instant
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot10 = value
            }
    }

    /** GetOrganizationWebhookHealthParameters sets the optional parameters of `getOrganizationWebhookHealth`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOrganizationWebhookHealthParameters internal constructor() {
        internal var slot0: kotlin.String? = null
        internal var slot1: AdminSchema.WebhookHealthWindow? = null
        internal var slot2: kotlin.time.Instant? = null
        internal var slot3: kotlin.time.Instant? = null

        /**
         * Keep only what concerns this webhook endpoint of the organization: one it
         * holds, or one it deleted, whose deliveries stay readable for the 13 months
         * they are kept. One that names no endpoint the organization held in those 13
         * months is a `400` naming it. Omitted, every destination is covered, the
         * notification emails included.
         */
        public var webhook_endpoint_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * The window to report on, ending at the hour the request is answered in. Omitted
         * with no range, it is `24h`; beside `start_at` or `end_at` it is a `400`.
         */
        public var window: AdminSchema.WebhookHealthWindow
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /**
         * The first instant covered, inclusive, as an RFC 3339 date-time. Events and
         * deliveries are kept 13 months, so an instant before that covers nothing
         * more. A `start_at` after `end_at` is a `400` naming both.
         */
        public var start_at: kotlin.time.Instant
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }

        /**
         * The instant the range ends at, exclusive, as an RFC 3339 date-time.
         * Omitted, the range runs to the moment the request is answered.
         */
        public var end_at: kotlin.time.Instant
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot3 = value
            }
    }

    /** GetWebhookEventTypesParameters sets the optional parameters of `getWebhookEventTypes`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetWebhookEventTypesParameters internal constructor() {
    }

    /** PatchMyOrganizationNotificationPreferencesParameters sets the optional parameters of `patchMyOrganizationNotificationPreferences`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchMyOrganizationNotificationPreferencesParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PatchMyPreferencesParameters sets the optional parameters of `patchMyPreferences`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchMyPreferencesParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PatchOrganizationParameters sets the optional parameters of `patchOrganization`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchOrganizationParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null
        internal var ifMatchSlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }

        /** The `etag` the caller read the resource under, or `"*"`, sent as `If-Match` exactly as given, and nothing is sent where it is left unset. */
        public var ifMatch: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                ifMatchSlot = value
            }
    }

    /** PatchOrganizationAiProviderConfigParameters sets the optional parameters of `patchOrganizationAiProviderConfig`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchOrganizationAiProviderConfigParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null
        internal var ifMatchSlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }

        /** The `etag` the caller read the resource under, or `"*"`, sent as `If-Match` exactly as given, and nothing is sent where it is left unset. */
        public var ifMatch: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                ifMatchSlot = value
            }
    }

    /** PatchOrganizationBillingInfoParameters sets the optional parameters of `patchOrganizationBillingInfo`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchOrganizationBillingInfoParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PatchOrganizationMemberParameters sets the optional parameters of `patchOrganizationMember`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchOrganizationMemberParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PatchOrganizationPublishableKeyParameters sets the optional parameters of `patchOrganizationPublishableKey`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchOrganizationPublishableKeyParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null
        internal var ifMatchSlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }

        /** The `etag` the caller read the resource under, or `"*"`, sent as `If-Match` exactly as given, and nothing is sent where it is left unset. */
        public var ifMatch: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                ifMatchSlot = value
            }
    }

    /** PatchOrganizationSettingsParameters sets the optional parameters of `patchOrganizationSettings`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchOrganizationSettingsParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null
        internal var ifMatchSlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }

        /** The `etag` the caller read the resource under, or `"*"`, sent as `If-Match` exactly as given, and nothing is sent where it is left unset. */
        public var ifMatch: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                ifMatchSlot = value
            }
    }

    /** PatchOrganizationWebhookEndpointParameters sets the optional parameters of `patchOrganizationWebhookEndpoint`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PatchOrganizationWebhookEndpointParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null
        internal var ifMatchSlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }

        /** The `etag` the caller read the resource under, or `"*"`, sent as `If-Match` exactly as given, and nothing is sent where it is left unset. */
        public var ifMatch: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                ifMatchSlot = value
            }
    }

    /** PostMyInvitationAcceptParameters sets the optional parameters of `postMyInvitationAccept`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostMyInvitationAcceptParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationParameters sets the optional parameters of `postOrganization`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationAiProviderConfigParameters sets the optional parameters of `postOrganizationAiProviderConfig`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationAiProviderConfigParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationBillingPaymentMethodParameters sets the optional parameters of `postOrganizationBillingPaymentMethod`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationBillingPaymentMethodParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationBillingPaymentMethodSetDefaultParameters sets the optional parameters of `postOrganizationBillingPaymentMethodSetDefault`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationBillingPaymentMethodSetDefaultParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationBillingPaymentMethodSetupParameters sets the optional parameters of `postOrganizationBillingPaymentMethodSetup`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationBillingPaymentMethodSetupParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationBillingProvisionParameters sets the optional parameters of `postOrganizationBillingProvision`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationBillingProvisionParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationBillingSubscriptionParameters sets the optional parameters of `postOrganizationBillingSubscription`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationBillingSubscriptionParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationInvitationParameters sets the optional parameters of `postOrganizationInvitation`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationInvitationParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationPublishableKeyParameters sets the optional parameters of `postOrganizationPublishableKey`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationPublishableKeyParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationWebhookDeliveryParameters sets the optional parameters of `postOrganizationWebhookDelivery`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationWebhookDeliveryParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostOrganizationWebhookEndpointParameters sets the optional parameters of `postOrganizationWebhookEndpoint`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostOrganizationWebhookEndpointParameters internal constructor() {
        internal var idempotencyKeySlot: kotlin.String? = null

        /** The `Idempotency-Key` every attempt of the call sends: the caller's, which makes two of its calls one write, or, left unset, one the client draws once. */
        public var idempotencyKey: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                idempotencyKeySlot = value
            }
    }

    /** PostSessionParameters sets the optional parameters of `postSession`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class PostSessionParameters internal constructor() {
    }
}
