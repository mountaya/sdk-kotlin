// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

@file:kotlin.OptIn(com.mountaya.client.InternalMountayaApi::class)

package com.mountaya.admin.generated

/** Admin is the `admin` operations of the REST API, one property per resource. */
public class Admin internal constructor(
    sender: com.mountaya.client.OperationSender,
) {
    /** The `admin.billing` operations. */
    public val billing: Admin.Billing = Admin.Billing(sender)

    /** The `admin.invitations` operations. */
    public val invitations: Admin.Invitations = Admin.Invitations(sender)

    /** The `admin.keys` operations. */
    public val keys: Admin.Keys = Admin.Keys(sender)

    /** The `admin.members` operations. */
    public val members: Admin.Members = Admin.Members(sender)

    /** The `admin.organizations` operations. */
    public val organizations: Admin.Organizations = Admin.Organizations(sender)

    /** The `admin.sessions` operations. */
    public val sessions: Admin.Sessions = Admin.Sessions(sender)

    /** The `admin.users` operations. */
    public val users: Admin.Users = Admin.Users(sender)

    /** The `admin.webhooks` operations. */
    public val webhooks: Admin.Webhooks = Admin.Webhooks(sender)

    /** Billing is the `admin.billing` operations of the REST API. */
    public class Billing internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Detach a payment method from an organization.
         *
         * Removes a saved payment method from the organization.
         * - Owner or admin only (403 otherwise), and refused for an organization whose
         *   billing is settled by its agreement with Mountaya (403).
         * - The method must belong to this organization: an identifier naming none of
         *   its methods is a 404, whether it names another organization's method or
         *   nothing at all, and the two answer alike. A method already detached names
         *   none of them, so a second detach is that 404 too; send a retried detach
         *   with the same `Idempotency-Key` and it is answered with the first one's
         *   200.
         * - Detaching the default while another method is saved is a 409: make
         *   another one the default with
         *   `postOrganizationBillingPaymentMethodSetDefault` first. Detaching the
         *   default when it is the only method leaves the organization without one:
         *   while its Data API subscription is in force, a key's Data API request
         *   then answers 402 until another is saved.
         * - The organization must already have a billing account, which
         *   creating the organization opens, or `postOrganizationBillingProvision` when
         *   it comes first. One that does not answers 404.
         *
         * The payment provider is asked as the call is made, so it is limited,
         * together with the six other billing writes this document's introduction
         * lists, to 30 a minute per user the token names and then to 300 a minute
         * per organization, an allowance apart from the billing reads'. Past
         * either, the answer is a `429` carrying `Retry-After`, and nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param payment_method_id Identifier of the payment method the operation acts on, as returned by
         * `getOrganizationBillingPaymentMethods`. It is an opaque `pm_` identifier
         * rather than a UUID, which is why it carries a pattern instead of a format; a
         * value that does not match the pattern is a 400 before the organization is
         * consulted.
         *
         * One naming none of the organization's methods is a 404: another organization's
         * method, one already detached and an identifier the payment provider never
         * issued answer alike, as every resource identifier around it does, so the
         * status cannot be read as confirmation that the identifier names a method
         * somewhere.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteOrganizationBillingPaymentMethod(
            organization_id: kotlin.String,
            payment_method_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.DeleteOrganizationBillingPaymentMethodParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.DeleteOrganizationBillingPaymentMethodParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteOrganizationBillingPaymentMethod,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/payment-methods/" + com.mountaya.client.encodePathSegment(payment_method_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * End an organization's Data API subscription at the end of its period.
         *
         * Ends the organization's metered Data API subscription when its current
         * period does. Nothing changes before then: the organization keeps `data`,
         * its requests are still counted, and the period is invoiced at its end, as
         * every period is, its usage up to the moment it ends included, its last day
         * among it. The subscription then ends, and from then the organization does
         * not hold `data`.
         * - Owner or admin only (403 otherwise), and refused for an organization whose
         *   billing is settled by its agreement with Mountaya (403). An agreement ends
         *   a subscription the organization still holds at the end of its period on
         *   its own.
         * - The subscription is returned with `cancel_at_period_end` true, and
         *   `current_period_end_at` the moment it ends.
         * - An organization that holds no Data API subscription, or only one that
         *   has ended, has nothing to end. It is answered 200 with the subscription
         *   as `getOrganizationBillingSubscription` reads it, `data` null where it
         *   never held one, and nothing changes.
         * - `postOrganizationBillingSubscription` takes the end back while the period
         *   runs, and runs the Data API again once it has ended.
         * - Calling it again changes nothing: an end already scheduled is answered as
         *   it stands.
         *
         * The payment provider is asked as the call is made, so it is limited,
         * together with the six other billing writes this document's introduction
         * lists, to 30 a minute per user the token names and then to 300 a minute
         * per organization, an allowance apart from the billing reads'. Past
         * either, the answer is a `429` carrying `Retry-After`, and nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteOrganizationBillingSubscription(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.DeleteOrganizationBillingSubscriptionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.OrganizationSubscriptionSummary?, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.DeleteOrganizationBillingSubscriptionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteOrganizationBillingSubscription,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/subscription",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.nullableSerializer(AdminSchema.OrganizationSubscriptionSummary.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List the billing catalog.
         *
         * Returns what an organization can buy: one entry per unit of access, with the
         * capabilities it grants, how it is counted, and the price it is sold on. It
         * is the catalog a client builds a checkout from, so the figures it shows are
         * the ones that will be charged rather than a copy kept in step by hand. The
         * Data API is the one product sold this way, metered by the month on the
         * requests made, and the first card an organization saves is what opens
         * it, unless the customer, or that card, still owes an invoice, which is the
         * `402` and opens nothing. Map Studio, Map Publishing, the Tile API, Embedded Studio and White
         * label are granted by an organization's agreement with Mountaya and are
         * not in the catalog.
         * - The catalog is the same for every organization, and it asks no member
         *   role, so a session token is answered alike whoever it names, or, minted
         *   for `api`, with no user at all; one minted for `studio-embedded` without
         *   `user_id` is a `401`. A product with no price set is left out rather than
         *   returned without one, so an entry that is here is one that can be bought.
         *   A product whose prices cannot be read from the payment provider at the
         *   moment is still listed, with `prices` empty: read that as an amount
         *   unavailable, not as a product that cannot be bought.
         * - It is a catalog, not a listing of what an organization made, so it is
         *   not paged: every product comes back at once.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getBillingProducts(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetBillingProductsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.BillingProduct>, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetBillingProductsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getBillingProducts,
                    path = "/v1/billing/products",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.BillingProduct.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve an organization's billing information.
         *
         * Returns what an invoice is addressed to: the name, the email, the billing
         * address when one is on file, and the tax identifier when one is registered.
         * Read it against the same object `patchOrganizationBillingInfo` writes: this
         * is the whole of what that operation changes.
         * - A session token minted for `api` without `user_id` is what authorizes this
         *   read, and no member role is checked on top of it; one minted for
         *   `studio-embedded` without `user_id` is the `401` a key alone gets. One
         *   that names a user is answered only while that user holds the owner or
         *   admin role in the organization, and is a 403 otherwise. The path names the
         *   key's own organization: another is a 403, whether or not it exists.
         * - `data` is null for an organization that has no billing account yet,
         *   which `postOrganizationBillingProvision` opens: there is nothing to
         *   address an invoice to until then.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization. The payment provider answers it as it is asked, so it
         * is also limited, together with `getOrganizationBillingPaymentMethods`,
         * `getOrganizationBillingInvoices`, `getOrganizationBillingPromotionCode` and
         * `postOrganizationBillingProvision`, to 60 a minute per user the token names
         * and then to 300 a minute per organization. Past any of these, the answer is
         * a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationBillingInfo(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationBillingInfoParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.BillingInfo?, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationBillingInfoParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationBillingInfo,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/info",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.nullableSerializer(AdminSchema.BillingInfo.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List an organization's invoices.
         *
         * Returns the organization's invoices, most recent first, each with its
         * number, date, total, what is still owed, and the links a payer follows to
         * view or download it. Amounts are in the currency's minor unit and are
         * reported as the payment provider holds them: nothing here is recomputed.
         * - A session token minted for `api` without `user_id` is what authorizes this
         *   read, and no member role is checked on top of it; one minted for
         *   `studio-embedded` without `user_id` is the `401` a key alone gets. One
         *   that names a user is answered only while that user holds the owner or
         *   admin role in the organization, and is a 403 otherwise. The path names the
         *   key's own organization: another is a 403, whether or not it exists.
         * - The organization must already have a billing account, which
         *   creating the organization opens, or `postOrganizationBillingProvision` when
         *   it comes first. One that does not answers 404.
         * - For a self-serve organization, the history is empty until its first
         *   card opens its Data API subscription. From then it grows by one invoice
         *   for each billing period the subscription is in force, a period running
         *   from the day of the month it opened to the same day of the next, each
         *   billing the Data API requests of the period that has just closed, and by
         *   the closing invoice of a subscription that ends. It never shrinks.
         * - For an organization billed by its agreement with Mountaya, or awaiting
         *   one, the history holds the invoices issued to it and never a draft, so
         *   `status=draft` lists none for it. Its invoices carry no
         *   `hosted_invoice_url`: it settles them as its agreement says, never through
         *   the payment provider's page, and downloads each one as a PDF.
         * - A void invoice is never listed: it was withdrawn and nobody owes it, so
         *   `status=void` lists none.
         * - `status` keeps the invoices that stand at it, so `status=open` lists
         *   what is still to be paid.
         * - The listing is paged, 50 invoices to a page unless `limit` says
         *   otherwise, the most recent first, as the payment provider returns them;
         *   the order is not the caller's to choose, so the listing takes no `sort`.
         *   Send the page's `next_cursor` back as `cursor` for the next one; the
         *   last page reports `next_cursor: null`.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization. The payment provider answers it as it is asked, so it
         * is also limited, together with `getOrganizationBillingPaymentMethods`,
         * `getOrganizationBillingInfo`, `getOrganizationBillingPromotionCode` and
         * `postOrganizationBillingProvision`, to 60 a minute per user the token names
         * and then to 300 a minute per organization. Past any of these, the answer is
         * a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationBillingInvoices(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationBillingInvoicesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.Invoice>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetOrganizationBillingInvoicesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationBillingInvoices,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/invoices",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("status" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.Invoice.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getOrganizationBillingInvoices` answers, one row at a time, requesting
         * a page only as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetOrganizationBillingInvoices(
            organization_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationBillingInvoicesParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.Invoice> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getOrganizationBillingInvoices(
                    organization_id = organization_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * List an organization's saved payment methods.
         *
         * Returns the payment methods saved against the organization, each marked with
         * whether it is the one invoices and subscriptions charge. Only display detail
         * is carried: a card's brand, last four digits and expiry, or a mandate's last
         * four and country. No card number and no IBAN passes through this contract.
         * - A session token minted for `api` without `user_id` is what authorizes this
         *   read, and no member role is checked on top of it; one minted for
         *   `studio-embedded` without `user_id` is the `401` a key alone gets. One
         *   that names a user is answered only while that user holds the owner or
         *   admin role in the organization, and is a 403 otherwise. The path names the
         *   key's own organization: another is a 403, whether or not it exists.
         * - The organization must already have a billing account, which
         *   creating the organization opens, or `postOrganizationBillingProvision` when
         *   it comes first. One that does not answers 404.
         * - The listing is paged, 50 methods to a page unless `limit` says
         *   otherwise, the most recently saved first; the order is not the
         *   caller's to choose, so the listing takes no `sort`. Send the page's
         *   `next_cursor` back as `cursor` for the next one; the last page reports
         *   `next_cursor: null`.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization. The payment provider answers it as it is asked, so it
         * is also limited, together with `getOrganizationBillingInvoices`,
         * `getOrganizationBillingInfo`, `getOrganizationBillingPromotionCode` and
         * `postOrganizationBillingProvision`, to 60 a minute per user the token names
         * and then to 300 a minute per organization. Past any of these, the answer is
         * a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationBillingPaymentMethods(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationBillingPaymentMethodsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.PaymentMethod>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetOrganizationBillingPaymentMethodsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationBillingPaymentMethods,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/payment-methods",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.PaymentMethod.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getOrganizationBillingPaymentMethods` answers, one row at a time,
         * requesting a page only as its collector takes the rows before it
         * (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetOrganizationBillingPaymentMethods(
            organization_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationBillingPaymentMethodsParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.PaymentMethod> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getOrganizationBillingPaymentMethods(
                    organization_id = organization_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Check what a promotion code is worth to an organization.
         *
         * Checks a promotion code against this organization and returns what it would
         * be worth, so a client can show the discount before the code is given to
         * `postOrganizationBillingProvision`, which records it for the organization's
         * first Data API subscription.
         *
         * It reads nothing and changes nothing: a code that is valid here has still
         * not been recorded or applied to anything. A code discounts the first Data
         * API subscription only, opened by the organization's first card, and no
         * operation puts one on a subscription that is already running.
         * - A session token minted for `api` without `user_id` is what authorizes this
         *   read, and no member role is checked on top of it; one minted for
         *   `studio-embedded` without `user_id` is the `401` a key alone gets. One
         *   that names a user is answered only while that user holds the owner or
         *   admin role in the organization, and is a 403 otherwise. The path names the
         *   key's own organization: another is a 403, whether or not it exists.
         * - A code that is unrestricted is valid for anyone. One restricted to a
         *   customer is valid only for that customer.
         * - An unknown code, an expired one, and one restricted to somebody else all
         *   answer 200 with `valid: false` and nothing further. They are deliberately
         *   indistinguishable, so this operation cannot be used to discover which
         *   codes exist. An ineligible code is not an error.
         * - An organization that has no billing account yet can still check
         *   unrestricted codes, and is answered 200 either way.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization. The payment provider answers it as it is asked, so it
         * is also limited, together with `getOrganizationBillingPaymentMethods`,
         * `getOrganizationBillingInvoices`, `getOrganizationBillingInfo` and
         * `postOrganizationBillingProvision`, to 60 a minute per user the token names
         * and then to 300 a minute per organization. Past any of these, the answer is
         * a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param code The promotion code to check, as it was handed to the customer: 1 to 64
         * letters, digits, hyphens and underscores. A value of any other shape is a
         * `400` naming `code`, and is never looked up.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationBillingPromotionCode(
            organization_id: kotlin.String,
            code: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationBillingPromotionCodeParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.PromotionCodePreview, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationBillingPromotionCodeParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationBillingPromotionCode,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/promotion-codes/" + com.mountaya.client.encodePathSegment(code),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.PromotionCodePreview.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve an organization's subscription.
         *
         * Returns the organization's subscription: the metered Data API
         * subscription, billed by the month on the Data API requests the
         * organization makes, which is the one subscription an operation in this
         * contract opens. It is the read to branch a Data API payment refusal on:
         * `has_payment_method` says whether a default payment method is on file, and
         * the subscription's `status`, read with its `paused` flag, says whether
         * what it grants is in force. What an agreement with Mountaya grants is not
         * a subscription and is not reported here, so an organization whose billing
         * its agreement settles can read `data` null too, and is never asked for a
         * card. For an owner or an admin of the key's organization, a `403` from
         * `postOrganizationBillingProvision`, `postOrganizationBillingPaymentMethodSetup`
         * or `postOrganizationBillingPaymentMethod` means exactly that: billing is
         * settled by the agreement, and no card is asked for.
         * - A session token minted for `api` without `user_id` is what authorizes this
         *   read, and no member role is checked on top of it; one minted for
         *   `studio-embedded` without `user_id` is the `401` a key alone gets. One
         *   that names a user is answered only while that user holds the owner or
         *   admin role in the organization, and is a 403 otherwise. The path names the
         *   key's own organization: another is a 403, whether or not it exists.
         * - `data` is null for an organization that has never held a subscription,
         *   which is where an organization stands until its first card opens its
         *   Data API subscription: opening billing opens none, and a card that is
         *   still processing leaves it null with the card saved. A subscription that
         *   has ended stays in its slot with the status it ended on, so `api` reads
         *   `status: canceled` once the Data API subscription has ended.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationBillingSubscription(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationBillingSubscriptionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.OrganizationSubscriptionSummary?, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationBillingSubscriptionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationBillingSubscription,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/subscription",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.nullableSerializer(AdminSchema.OrganizationSubscriptionSummary.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update an organization's billing information.
         *
         * Writes what invoices are addressed to, and returns the whole record after
         * the change. Every field is optional and every field present is a
         * replacement: an omitted field is left alone, an address whose fields are all
         * empty clears the address, and a `tax_id` with an empty value clears the tax
         * identifier. Sending `name` as the empty string restores the organization's
         * own name rather than clearing it.
         * - Owner or admin only (403 otherwise), and refused for an organization whose
         *   billing is settled by its agreement with Mountaya (403).
         * - The organization must already have a billing account, which
         *   creating the organization opens, or `postOrganizationBillingProvision` when
         *   it comes first. One that does not answers
         *   409, and nothing is written.
         * - A tax identifier the payment provider refuses is a 400 at `tax_id.value`,
         *   or at `tax_id.type` when the type is at fault, and nothing is changed: the
         *   previous tax identifier is replaced only once the new one is accepted.
         * - An address that would leave the organization's running Data API
         *   subscription without a location its tax can be computed from (the
         *   address cleared, its country removed, or one the payment provider cannot
         *   place) is a 400 at `address`, and the name, email and address are left
         *   as they were.
         * - It is refused with 503 while another billing change for the
         *   organization is running, and nothing is written.
         * - Changing the organization's own name through `patchOrganization` never
         *   changes the name invoices are addressed to. That name stays the
         *   organization's name as it was when billing was opened until it is changed
         *   here.
         *
         * The payment provider is asked as the call is made, so it is limited,
         * together with the six other billing writes this document's introduction
         * lists, to 30 a minute per user the token names and then to 300 a minute
         * per organization, an allowance apart from the billing reads'. Past
         * either, the answer is a `429` carrying `Retry-After`, and nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchOrganizationBillingInfo(
            organization_id: kotlin.String,
            body: AdminSchema.BillingInfoUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchOrganizationBillingInfoParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.BillingInfo, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchOrganizationBillingInfoParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchOrganizationBillingInfo,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/info",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.BillingInfoUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.BillingInfo.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Save a confirmed card against an organization.
         *
         * Completes the card collection started by
         * `postOrganizationBillingPaymentMethodSetup`, and returns the payment method
         * saved. The confirmed setup intent is checked, the metered Data API
         * subscription is opened on its card where the organization holds none, or
         * only one that has ended, and the card then becomes the organization's
         * default and the card every subscription that is charged automatically and
         * has not ended charges: one already charging another card moves onto it,
         * and every invoice such a subscription still has outstanding is charged to
         * it. A subscription billed by invoice is left as it is. When no billing
         * address is on file and the card carries one, the card's address becomes
         * the billing address, which is what lets tax be computed on the
         * organization's invoices. Read `getOrganizationBillingSubscription` for the
         * subscription the card opened or moved.
         * - The answer is `201`, with `Location` naming the payment method, when
         *   this request attached it to the organization, and `200` with the same
         *   payment method when it was already the organization's default card, by
         *   an earlier call or by the steps that run on their own once the setup
         *   succeeds. A card still confirming is not the default yet, so a repeat
         *   for it answers `201` again. A client treats both as the card being
         *   saved.
         * - The first card is what opens the Data API subscription, the one product
         *   an organization obtains by card. Where the organization has never held
         *   one, or the one it held has ended, one is opened on the card, and from
         *   then the organization holds `data`, which a key carrying `data` or `mcp`
         *   needs. It is metered: nothing is charged as it opens, and the requests the
         *   organization made are invoiced with tax at the end of each billing
         *   period, a month counted from the day it opened.
         *   The promotion code recorded by `postOrganizationBillingProvision` is
         *   applied to the organization's first subscription and to no later one.
         *   Where a subscription has not ended, none is opened, and its outstanding
         *   invoices are what the card is charged.
         * - Map Studio, Map Publishing, the Tile API, Embedded Studio and White label
         *   are granted by the organization's agreement with Mountaya. No card opens
         *   any of them, and saving one changes none of them.
         * - Where a subscription has not ended, a charge of its outstanding invoices
         *   the card's issuer refuses does not undo the save: the answer is still
         *   a success, the card stays the default, and the invoice it was for stays
         *   open.
         *   An issuer that answers by asking the cardholder to authenticate
         *   refuses in the same way, since the charge is made without the cardholder
         *   present: the card is saved, and another card, or the cardholder paying
         *   the invoice, is the remedy.
         * - The subscription's invoices are taxed. When the payment provider cannot
         *   tell where the organization is taxed, which is the case when no billing
         *   address is on file and the card carries none, the answer is a 400 naming
         *   `setup_intent_id`, the intent whose card has no address to be taxed at;
         *   its message is what asks for a billing address. The card is saved
         *   but is not made the default, and nothing is opened or charged. Set a
         *   billing address with `patchOrganizationBillingInfo` and call this again
         *   with the same setup intent: the repeat opens the subscription.
         * - When the promotion code recorded for the organization is refused by the
         *   payment provider as it is redeemed, the answer is a 400 whose entry
         *   names no field, since the code is no part of this request. Nothing is
         *   opened or charged, and the card is saved but is not made the default.
         *   Call `postOrganizationBillingProvision` again with another code, or
         *   with none, then call this again with the same setup intent.
         * - Before a subscription is opened, every invoice the organization still
         *   owes, open or uncollectible, those of a subscription that has ended
         *   included, is collected on the card being saved. When one cannot be
         *   collected on it, the answer is a 402: nothing is opened, and the card is
         *   saved but is not made the default. The organization does not start a
         *   new subscription while it owes for an earlier one: pay what is owed, or
         *   save a card that can pay it, and call this again.
         * - Nor is a subscription opened on a card another organization still owes
         *   on. A card is recognized by its number, whichever organization saved
         *   it, and while any other organization owes an invoice that a payment on
         *   a card of the same number failed to pay, one marked uncollectible or
         *   one still open after that attempt, the answer is the same 402: nothing
         *   is opened, nothing the other organization owes is charged here, and the
         *   card is saved but is not made the default. Only what the card itself
         *   failed to pay refuses it: a card the other organization merely has on
         *   file, or used to pay something else, blocks nothing, and neither does
         *   an invoice whose payment is still processing. What that organization
         *   owes is settled on it, and this is called again once it is.
         * - The subscription is opened on the card being saved. Nothing is charged
         *   as it opens, unless the organization already owes an amount outside any
         *   subscription, which its first invoice collects on that card at once.
         *   When the card is refused as the subscription opens, the answer is a 400
         *   naming `setup_intent_id`: nothing is opened, and the card is saved but is
         *   not made the default. Another card opens the subscription.
         * - Confirming the setup intent in the browser is enough on its own: once the
         *   payment provider reports it succeeded, the same steps run shortly after,
         *   whether or not this operation is called, the Data API subscription
         *   opened where they open one. For a card, calling it runs them before it
         *   answers, so the card is on file for the very next request. Calling it is
         *   also how either 400 above is learned of: when the steps run without this
         *   call, the card is saved without becoming the default and no answer says
         *   why.
         * - Owner or admin only (403 otherwise), and refused for an organization whose
         *   billing is settled by its agreement with Mountaya (403).
         * - A setup intent that is unknown, or that belongs to another
         *   organization, is a 404, and nothing is saved. One of another
         *   organization is answered as an unknown one is, so a setup intent cannot
         *   be probed here.
         * - A setup intent of this organization that has not been confirmed, that
         *   waits on the cardholder, that failed or was canceled, or that carries no
         *   payment method, is a 409 naming `setup_intent_id`, and nothing is saved.
         *   Complete the confirmation in the browser, or start again with
         *   `postOrganizationBillingPaymentMethodSetup`; sending the same intent
         *   again is answered the same way until it succeeds.
         * - The organization must already have a billing account, which
         *   creating the organization opens, or `postOrganizationBillingProvision` when
         *   it comes first. One that does not answers 409.
         * - A method that settles slowly, such as a SEPA Direct Debit mandate, is
         *   accepted while it is still confirming: it is saved and returned with
         *   `is_default` false, and the subscription is left as it was. It becomes
         *   the default once it confirms, so read `is_default` rather than assuming
         *   the method is live on the answer.
         * - Calling it twice with the same setup intent answers the same payment
         *   method (`200` once it is the default card), and changes nothing the
         *   second time unless the
         *   first was answered the 400 for a billing address or the one for a
         *   promotion code, either of which the repeat looks at afresh once its
         *   cause is put right. A refused card is refused again.
         *
         * The payment provider is asked as the call is made, so it is limited,
         * together with the six other billing writes this document's introduction
         * lists, to 30 a minute per user the token names and then to 300 a minute
         * per organization, an allowance apart from the billing reads'. Past
         * either, the answer is a `429` carrying `Retry-After`, and nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationBillingPaymentMethod(
            organization_id: kotlin.String,
            body: AdminSchema.OrganizationBillingPaymentMethodParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationBillingPaymentMethodParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.PaymentMethod, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationBillingPaymentMethodParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationBillingPaymentMethod,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/payment-methods",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.OrganizationBillingPaymentMethodParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.PaymentMethod.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Choose which payment method an organization is charged on.
         *
         * Makes one saved payment method the one invoices and subscriptions charge,
         * and returns it. At most one is the default at a time, so this both sets
         * the new one and unsets the old.
         * - Owner or admin only (403 otherwise), and refused for an organization whose
         *   billing is settled by its agreement with Mountaya (403).
         * - The method must belong to this organization: an identifier naming none of
         *   its methods is a 404, whether it names another organization's method or
         *   nothing at all, and the two answer alike.
         * - The organization must already have a billing account, which
         *   creating the organization opens, or `postOrganizationBillingProvision` when
         *   it comes first. One that does not answers 404.
         * - Every subscription that is charged automatically and has not ended moves
         *   onto this method, and the invoices such a subscription still has
         *   outstanding are charged to it at once. A charge the issuer refuses does
         *   not undo the choice: the answer is still 200, the method is the default,
         *   and that invoice stays open.
         * - It opens no subscription. An organization with no Data API subscription
         *   in force that makes a saved method its default has a default and still
         *   no subscription: opening one takes a card saved through
         *   `postOrganizationBillingPaymentMethod`, or
         *   `postOrganizationBillingSubscription`, which opens one on the default
         *   payment method.
         * - Setting the same default twice changes nothing the second time, and is
         *   answered with the method as it stands.
         *
         * The payment provider is asked as the call is made, so it is limited,
         * together with the six other billing writes this document's introduction
         * lists, to 30 a minute per user the token names and then to 300 a minute
         * per organization, an allowance apart from the billing reads'. Past
         * either, the answer is a `429` carrying `Retry-After`, and nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param payment_method_id Identifier of the payment method the operation acts on, as returned by
         * `getOrganizationBillingPaymentMethods`. It is an opaque `pm_` identifier
         * rather than a UUID, which is why it carries a pattern instead of a format; a
         * value that does not match the pattern is a 400 before the organization is
         * consulted.
         *
         * One naming none of the organization's methods is a 404: another organization's
         * method, one already detached and an identifier the payment provider never
         * issued answer alike, as every resource identifier around it does, so the
         * status cannot be read as confirmation that the identifier names a method
         * somewhere.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationBillingPaymentMethodSetDefault(
            organization_id: kotlin.String,
            payment_method_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationBillingPaymentMethodSetDefaultParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.PaymentMethod, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationBillingPaymentMethodSetDefaultParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationBillingPaymentMethodSetDefault,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/payment-methods/" + com.mountaya.client.encodePathSegment(payment_method_id) + "/set-default",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.PaymentMethod.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Start collecting a card for an organization.
         *
         * Returns a single-use secret for collecting a card inside the caller's own
         * page rather than sending the payer somewhere else. Hand it to Stripe
         * Elements, which confirms the card in the browser, then name the confirmed
         * setup intent on `postOrganizationBillingPaymentMethod` to save the card
         * against the organization. Each call starts a collection of its own with a
         * secret of its own. Nothing is saved by this operation on its own: a
         * secret that is never confirmed leaves the organization exactly as it was.
         * One that is confirmed is saved even if that call never comes: once the
         * payment provider reports the setup succeeded, the steps
         * `postOrganizationBillingPaymentMethod` describes run shortly after. Where
         * the organization holds no Data API subscription, or only one that has
         * ended, the card opens one, metered and invoiced by the month for what is
         * used, then becomes the default.
         * - Owner or admin only (403 otherwise), and refused for an organization whose
         *   billing is settled by its agreement with Mountaya (403).
         * - The organization must already have a billing account, which
         *   creating the organization opens, or `postOrganizationBillingProvision` when
         *   it comes first. One that does not answers 409.
         *
         * The payment provider is asked as the call is made, so it is limited,
         * together with the six other billing writes this document's introduction
         * lists, to 30 a minute per user the token names and then to 300 a minute
         * per organization, an allowance apart from the billing reads'. Past
         * either, the answer is a `429` carrying `Retry-After`, and nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationBillingPaymentMethodSetup(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationBillingPaymentMethodSetupParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.SetupIntentSession, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationBillingPaymentMethodSetupParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationBillingPaymentMethodSetup,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/payment-methods/setup",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.SetupIntentSession.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Open an organization's billing.
         *
         * Opens the organization's billing account, which collecting a card needs,
         * records the promotion code it is given, and returns the billing
         * information invoices will be addressed to. It opens no subscription,
         * raises no invoice and charges nothing. The Data API is the one product an
         * organization obtains by card: the first card saved through
         * `postOrganizationBillingPaymentMethodSetup` and
         * `postOrganizationBillingPaymentMethod` opens the metered Data API
         * subscription, billed by the month on the requests the organization makes,
         * and from then the organization holds `data`. Until then it holds no
         * product and owes nothing. Map Studio, Map Publishing, the Tile API,
         * Embedded Studio and White label are granted by the organization's
         * agreement with Mountaya, and no card opens any of them.
         * - Owner or admin only (403 otherwise), and refused for an organization whose
         *   billing is settled by its agreement with Mountaya (403).
         * - Until the first card, a key carrying `data` or `mcp` answers 402
         *   wherever it is used.
         * - `promotion_code` is kept for the organization's first Data API
         *   subscription and applied when the first card opens it. The last call
         *   decides: a call carrying another code replaces the one recorded, and a
         *   call carrying none clears it.
         * - A `promotion_code` this organization cannot use is a 400 naming the
         *   field, and nothing is created or recorded. That includes every code
         *   given to an organization that has already held a Data API subscription:
         *   a code applies to the first one only, and a call carrying none still
         *   succeeds. One accepted here that the payment provider refuses as it is
         *   redeemed is answered by the call that would have opened the
         *   subscription: `postOrganizationBillingPaymentMethod` for a card, or
         *   `postOrganizationBillingSubscription` on the default payment method.
         * - The billing information starts with the organization's own name and
         *   the email address of the user the session token names. Change either
         *   with `patchOrganizationBillingInfo`.
         * - Safe to repeat: a double-submit or a retry opens no second account, and
         *   two calls sent at the same moment open one billing account. A call on
         *   an organization whose billing is already open records the promotion
         *   code it carries, or clears it, and answers the billing information as
         *   it stands.
         * - The subscription the first card opens bills by the month, in arrears,
         *   on what the organization used. There is no interval to choose, here or
         *   afterwards.
         *
         * The promotion code is checked with the payment provider as its preview
         * checks it, so this operation is limited, together with
         * `getOrganizationBillingPaymentMethods`, `getOrganizationBillingInvoices`,
         * `getOrganizationBillingInfo` and `getOrganizationBillingPromotionCode`, to
         * 60 a minute per user the token names and then to 300 a minute per
         * organization, whether or not the call carries a code. Past either, the
         * answer is a `429` carrying `Retry-After`, and nothing is opened or
         * recorded.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationBillingProvision(
            organization_id: kotlin.String,
            body: AdminSchema.OrganizationBillingProvisionParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationBillingProvisionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.BillingInfo, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationBillingProvisionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationBillingProvision,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/provision",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.OrganizationBillingProvisionParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.BillingInfo.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Run an organization's Data API subscription.
         *
         * Runs the organization's metered Data API subscription, whatever state it is
         * in, and returns it.
         * - An end scheduled by `deleteOrganizationBillingSubscription` is taken back,
         *   and the subscription carries on into its next period.
         * - A subscription that is past due or unpaid has its outstanding invoices
         *   charged to the organization's default payment method, as saving a card
         *   charges them. A charge the card refuses does not fail the call: the
         *   subscription is returned as the charge left it. With no default payment
         *   method on file there is nothing to charge, and it is returned as it is.
         * - A paused subscription is returned as it is, and nothing changes.
         * - A subscription already running is returned as it is, and nothing changes.
         * - Where the organization holds none, because the one it held has ended, its
         *   agreement with Mountaya has ended, or it never held one, one is opened on
         *   the organization's default payment method, exactly as the first card
         *   opens one: billed by the month on the requests the organization makes,
         *   with tax. From then the organization holds `data`. The promotion code
         *   recorded by `postOrganizationBillingProvision` applies only to the
         *   organization's first Data API subscription.
         * - With no default payment method on file, or no billing account, the
         *   answer is a 402 and nothing is opened. Saving a card through
         *   `postOrganizationBillingPaymentMethodSetup` and
         *   `postOrganizationBillingPaymentMethod` opens the subscription on its own.
         * - Before one is opened, every invoice the organization still owes, open or
         *   uncollectible, those of a subscription that has ended included, is
         *   collected on the default payment method. When one cannot be collected
         *   on it, the answer is a 402 and nothing is opened: the organization does
         *   not start a new subscription while it owes for an earlier one. Pay what
         *   is owed, or make a card that can pay it the default, and call this
         *   again.
         * - Nor is one opened on a default card another organization still owes on.
         *   A card is recognized by its number, whichever organization saved it,
         *   and while any other organization owes an invoice that a payment on a
         *   card of the same number failed to pay, one marked uncollectible or one
         *   still open after that attempt, the answer is the same 402 and nothing
         *   is opened. Only what the card itself failed to pay refuses it: a card
         *   the other organization merely has on file, or used to pay something
         *   else, blocks nothing, and neither does an invoice whose payment is
         *   still processing. Nothing the other organization owes is charged here:
         *   it is settled on that organization.
         * - The subscription's invoices are taxed. When the payment provider cannot
         *   tell where the organization is taxed, the answer is a 400 whose entry
         *   names no field, since nothing in this request is what is missing; its
         *   message asks for a billing address. Nothing is opened. Set one with
         *   `patchOrganizationBillingInfo` and call this again.
         * - When the payment provider refuses the default payment method as the
         *   subscription opens, the answer is a 400 whose entry names no field, and
         *   nothing is opened. The refusal is remembered for a day: calling this
         *   again while the same method is the default is answered the same 400.
         *   Make another saved method the default with
         *   `postOrganizationBillingPaymentMethodSetDefault` and call this again, or
         *   save another card, which opens the subscription on its own. Each method
         *   is an attempt of its own, and a card saved again is a new method, so
         *   it is one too.
         * - When the promotion code recorded by `postOrganizationBillingProvision`
         *   for the organization's first Data API subscription is refused by the
         *   payment provider as it is redeemed, expired or used up since it was
         *   recorded, the answer is a 400 whose entry names no field, since the
         *   code is no part of this request. Nothing is opened or charged. Call
         *   `postOrganizationBillingProvision` again with another code, or with
         *   none, then call this again.
         * - Owner or admin only (403 otherwise), and refused for an organization whose
         *   billing is settled by its agreement with Mountaya (403).
         * - Calling it again changes nothing for a subscription that runs or is
         *   paused. One that is past due or unpaid is charged again: each call
         *   charges its outstanding invoices to the default payment method, so a
         *   call repeated after a refusal is another attempt on that method. An
         *   invoice already paid is not charged twice.
         *
         * The payment provider is asked as the call is made, so it is limited,
         * together with the six other billing writes this document's introduction
         * lists, to 30 a minute per user the token names and then to 300 a minute
         * per organization, an allowance apart from the billing reads'. Past
         * either, the answer is a `429` carrying `Retry-After`, and nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationBillingSubscription(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationBillingSubscriptionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.OrganizationSubscriptionSummary, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationBillingSubscriptionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationBillingSubscription,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/billing/subscription",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.OrganizationSubscriptionSummary.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Invitations is the `admin.invitations` operations of the REST API. */
    public class Invitations internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Withdraw a pending invitation.
         *
         * Withdraws an invitation still waiting for an answer, which frees its seat.
         * The person it names cannot accept it afterwards, and the address may be
         * invited again.
         * - Owner or admin only (403 otherwise), whatever role the invitation
         *   grants: withdrawing grants nothing.
         * - Withdrawing is an act on the invitation's state rather than on a
         *   resource, so an invitation already gone answers 200 and changes nothing:
         *   one already declined, withdrawn or lapsed, and an identifier naming no
         *   invitation of this organization at all. A withdrawal sent again, or
         *   racing the person's own answer, is therefore not an error.
         * - An invitation already accepted is gone as well: the person is a member,
         *   and `deleteOrganizationMember` removes them.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param invitation_id Identifier of the invitation, the `id` an `OrganizationInvitation` or a
         * `UserInvitation` carries. One that names no invitation this caller may see,
         * an expired one included, is answered as each operation says: a read or an
         * acceptance answers it `404`, and a withdrawal or a decline, which leave it
         * gone either way, answer `200`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteOrganizationInvitation(
            organization_id: kotlin.String,
            invitation_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.DeleteOrganizationInvitationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.DeleteOrganizationInvitationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteOrganizationInvitation,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/invitations/" + com.mountaya.client.encodePathSegment(invitation_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve a pending invitation.
         *
         * Returns one invitation to the organization that is still waiting for an
         * answer: the address it names, the role accepting grants, who made it, and
         * when it lapses. Once the person accepts or declines it, or it is withdrawn
         * or lapses, it is gone and its identifier is a 404; a person who accepted
         * is a member `getOrganizationMembers` lists.
         * - A session token minted for `api` without `user_id` is asked for no member
         *   role on this read; one minted for `studio-embedded` without `user_id` is
         *   the `401` a key alone gets. One that names a user is answered only while
         *   that user is a member of the organization, in any role, and is a 403
         *   otherwise.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param invitation_id Identifier of the invitation, the `id` an `OrganizationInvitation` or a
         * `UserInvitation` carries. One that names no invitation this caller may see,
         * an expired one included, is answered as each operation says: a read or an
         * acceptance answers it `404`, and a withdrawal or a decline, which leave it
         * gone either way, answer `200`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationInvitation(
            organization_id: kotlin.String,
            invitation_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationInvitationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.OrganizationInvitation, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationInvitationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationInvitation,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/invitations/" + com.mountaya.client.encodePathSegment(invitation_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.OrganizationInvitation.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List an organization's pending invitations.
         *
         * Returns the invitations to the organization still waiting for an answer:
         * the address each names, the role accepting grants, who made it, and when it
         * lapses. An invitation leaves this listing when the person accepts it, and
         * is then a member `getOrganizationMembers` lists, when they decline it, when
         * it is withdrawn, or when it expires thirty days after it was made. Every
         * invitation listed holds a seat.
         * - It names addresses and never accounts: whether an account carries an
         *   address is not part of the answer.
         * - A session token minted for `api` without `user_id` is asked for no member
         *   role on this read; one minted for `studio-embedded` without `user_id` is
         *   the `401` a key alone gets. One that names a user is answered only while
         *   that user is a member of the organization, in any role, and is a 403
         *   otherwise. Inviting and withdrawing take the owner or admin role.
         * - The listing pages by cursor. `role` narrows it to the invitations
         *   granting one role, and `sort` orders it by `created_at`, newest first
         *   unless asked otherwise. Follow
         *   `extensions.metadata.pagination.next_cursor` until it reads `null`,
         *   which is the end of the listing.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationInvitations(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationInvitationsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.OrganizationInvitation>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetOrganizationInvitationsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationInvitations,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/invitations",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("role" to it.value) }
                            given.slot3?.let { add("sort" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.OrganizationInvitation.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getOrganizationInvitations` answers, one row at a time, requesting a
         * page only as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetOrganizationInvitations(
            organization_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationInvitationsParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.OrganizationInvitation> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getOrganizationInvitations(
                    organization_id = organization_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Invite somebody to an organization by email.
         *
         * Invites one address, with the role the person holds once they accept. A
         * client inviting several people sends one request for each. Nothing is sent
         * to the address: telling the person is the caller's to do. The person finds
         * the invitation through `getMyInvitations` once their account has verified
         * the address, under a session token `postSession` mints for them before they
         * have joined, and answers it with `postMyInvitationAccept` or
         * `deleteMyInvitation`.
         * - Owner or admin only (403 otherwise). Under a key, inviting as `admin` is
         *   a 403 whoever asks, the owner included, whose session under a key is
         *   answered as an admin's: only the owner invites an admin, signed in to
         *   Mountaya's web application.
         * - Any well-formed address is accepted, stored trimmed and lowercased, and
         *   answered alike whether or not an account carries it, so this cannot be
         *   used to learn which addresses have accounts. An address no account
         *   carries waits for the person to sign up and verify it.
         * - An address that already belongs to a member, or that already has an
         *   invitation waiting for an answer, is a 409 naming which, so a second
         *   request for one address never makes a second invitation. To change the
         *   role of a waiting invitation, withdraw it with
         *   `deleteOrganizationInvitation` and invite again.
         * - `owner` is never granted by invitation, so asking for it is a 400.
         * - Every invitation holds a seat from the moment it is made, and seats count
         *   the organization's members and its unexpired invitations together. One
         *   that would take that count past the seats the organization's agreement
         *   allows is a 429 coded `LIMIT_REACHED`, whose `extensions.metadata.limit`
         *   reads `seats`; an agreement that names no seat limit allows any number.
         *   Invitations sent at the same moment cannot both take the last seat. It
         *   clears when a member is removed or leaves, an invitation is declined,
         *   withdrawn or lapses, or the organization's agreement allows more seats.
         * - At most 500 invitations wait at once in an organization: one more is a
         *   429 coded `LIMIT_REACHED`, whose `extensions.metadata.limit` reads
         *   `pending-invitations`, and which no agreement raises, so it clears only
         *   as invitations are answered, withdrawn or lapse.
         * - Neither ceiling carries `Retry-After`, since no wait lifts it, and
         *   `extensions.metadata` carries how many it allows, `allowed`, and how
         *   many are held, `used`. Nothing is invited. The seats are asked before
         *   the invitations waiting, and both after every other refusal, so a request
         *   refused with a `400`, a `409`, or a `403` for the caller's role is
         *   answered that way whatever is left of either.
         * - An invitation lapses thirty days after it is made if nobody answers it,
         *   which frees its seat and lets the address be invited again.
         * - Invitations are limited to 30 per minute for the user the session token
         *   names, and to 200 per day for the organization; past either limit the
         *   answer is a `429` carrying `Retry-After`. An invitation counts toward the
         *   organization's day only once the user is known to be one of its owners or
         *   admins and the request is well formed: one refused with a `400`, or with
         *   the `403` of a role that may not send it, never spends it, so nobody
         *   outside the organization can use up its day.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationInvitation(
            organization_id: kotlin.String,
            body: AdminSchema.OrganizationInvitationCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationInvitationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.OrganizationInvitation, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationInvitationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationInvitation,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/invitations",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.OrganizationInvitationCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.OrganizationInvitation.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Keys is the `admin.keys` operations of the REST API. */
    public class Keys internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Revoke a publishable key.
         *
         * Revokes a publishable key. Mountaya answers every client still presenting
         * it with a 401 from its next request, so revoke a key that has leaked and
         * create its replacement first if the surface using it cannot go dark. An
         * answer a shared cache already holds for the key is served from there until
         * its lifetime runs out, which no revocation shortens: at most 5 minutes for
         * a public tile, 6 for a published itinerary read or its pace, and 25 hours
         * for the layer catalog, as each of those reads states in its
         * `Cache-Control`.
         * - Owner or admin only (403 otherwise).
         * - It cannot be undone. A revoked key stays in
         *   `getOrganizationPublishableKeys` carrying `revoked_at`, and is never
         *   accepted again.
         * - The answer is the revoked key, carrying `revoked_at`, with its `ETag`.
         *   Revoking a key already revoked changes nothing and answers 200 again
         *   with the key as it stands, so a revocation is safe to repeat.
         * - Revoking the key a session was minted against ends that session too: a
         *   session token names the key it was issued for, and a stream opened with
         *   it ends with a `reconnect` event whose reason is `credential-revoked`.
         * - `If-Match` makes the revocation conditional: send the key's `etag`, and
         *   a key changed since is refused with `412`, revoking nothing.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param key_id Identifier of the publishable key the operation acts on. It is the key's
         * `id`, not the `pk_` value the key itself is.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteOrganizationPublishableKey(
            organization_id: kotlin.String,
            key_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.DeleteOrganizationPublishableKeyParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.PublishableKey, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.DeleteOrganizationPublishableKeyParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteOrganizationPublishableKey,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/publishable-keys/" + com.mountaya.client.encodePathSegment(key_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.PublishableKey.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve a publishable key.
         *
         * Returns one of the organization's publishable keys, as
         * `getOrganizationPublishableKeys` lists it: its scopes, when it was created,
         * when a request last presented it, and `revoked_at` once it is revoked. A
         * revoked key is still returned, so the key a `Location` or an audit names
         * can always be read back. The key value itself is returned in full, as on
         * every read.
         * - A session token minted for `api` without `user_id` is asked for no member
         *   role on this read; one minted for `studio-embedded` without `user_id` is
         *   the `401` a key alone gets. One that names a user is answered only while
         *   that user holds the owner or admin role in the organization, and is a 403
         *   otherwise.
         * - The answer's `ETag` is the key's `etag`. Send it as `If-Match` on
         *   `patchOrganizationPublishableKey` or `deleteOrganizationPublishableKey`
         *   to make the change conditional on nobody having changed the key since.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param key_id Identifier of the publishable key the operation acts on. It is the key's
         * `id`, not the `pk_` value the key itself is.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationPublishableKey(
            organization_id: kotlin.String,
            key_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationPublishableKeyParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.PublishableKey, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationPublishableKeyParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationPublishableKey,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/publishable-keys/" + com.mountaya.client.encodePathSegment(key_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.PublishableKey.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List an organization's publishable keys.
         *
         * Returns the publishable keys the organization holds, revoked ones
         * included, each with its scopes, when it was created and when a request last
         * presented it. The key value itself is returned in full: a publishable key is
         * meant to be seen, which is what distinguishes it from the secret key that
         * mints sessions.
         * - A session token minted for `api` without `user_id` is asked for no member
         *   role on this read; one minted for `studio-embedded` without `user_id` is
         *   the `401` a key alone gets. One that names a user is answered only while
         *   that user holds the owner or admin role in the organization, and is a 403
         *   otherwise. Creating, changing and revoking keys take the same role, held
         *   by the user the session token names, which a write requires.
         * - A revoked key carries `revoked_at` and stays in the listing, so an audit
         *   of what was once in circulation does not need a second source.
         * - `scope` keeps the keys given that scope, revoked ones included.
         * - The listing is paged. Nothing caps how many keys an organization mints,
         *   so a page holds 50 keys unless `limit` says otherwise, most recently
         *   created first unless `sort` says otherwise. Send the page's
         *   `next_cursor` back as `cursor` for the next one; the last page reports
         *   `next_cursor: null`.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationPublishableKeys(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationPublishableKeysParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.PublishableKey>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetOrganizationPublishableKeysParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationPublishableKeys,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/publishable-keys",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("scope" to it.value) }
                            given.slot3?.let { add("sort" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.PublishableKey.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getOrganizationPublishableKeys` answers, one row at a time, requesting
         * a page only as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetOrganizationPublishableKeys(
            organization_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationPublishableKeysParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.PublishableKey> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getOrganizationPublishableKeys(
                    organization_id = organization_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Update a publishable key's name or scopes.
         *
         * Changes what a key is called, what it reaches, or both. Each field is
         * optional and an omitted one is left alone, but `scopes` is replaced whole
         * rather than merged: a request that sends it lists every scope the key is to
         * keep, and a scope left out is removed. Mountaya answers the key's next
         * request by its new scopes, so a page still holding it starts getting 403
         * on what it stops reaching. An answer a shared cache already holds for the
         * key is served from there until its lifetime runs out: at most 5 minutes
         * for a public tile, 6 for a published itinerary read or its pace
         * (`max-age=300` and `stale-while-revalidate=60`), and 25 hours for the
         * layer catalog (`max-age=86400` and `stale-while-revalidate=3600`).
         * - Owner or admin only (403 otherwise).
         * - A key carrying `management` carries no other scope. A `scopes` list
         *   naming `management` beside another scope is a `400` and nothing is
         *   changed, the name included: the answer carries one `VALIDATION_FAILED`
         *   entry per scope listed beside `management`, each naming that scope's
         *   position in `scopes`. It is asked whatever the key carries now, and
         *   before any scope's product, so such a list is never answered `402`.
         * - A key carrying `studio-embedded` carries `tiles` and `data` beside it,
         *   the scopes its editor's map and previews are requested under. A
         *   `scopes` list naming `studio-embedded` without one of them is a `400`
         *   and nothing is changed, the name included: the answer carries one
         *   `VALIDATION_FAILED` entry per missing scope, each naming `scopes`.
         *   Neither is added for the caller, it is asked whatever the key carries
         *   now, and before any scope's product, so such a list is never answered
         *   `402`.
         * - A scope the key does not already carry can be granted only while the
         *   organization holds its product. Granting one it does not hold is a `402`
         *   and nothing is changed, the name included: the answer carries one entry
         *   per refused scope, each naming that scope's position in `scopes`.
         * - A scope the key already carries is kept even when its product has since
         *   been switched off, so sending a key its own list back is never refused.
         *   Such a scope answers `402` wherever a request uses it, until the product
         *   returns.
         * - The key is given exactly the list sent: none is added because another is
         *   present.
         * - An empty `scopes` list is a `400` naming `scopes`, as it is on creation:
         *   a key carries at least one scope.
         * - The `pk_` value never changes. Replacing a key that has leaked is revoking
         *   it and creating another.
         * - A revoked key cannot be changed: the request is a `409` and nothing is
         *   changed.
         * - `If-Match` makes the change conditional: send the key's `etag`, and a
         *   key changed since is refused with `412`, changing nothing.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param key_id Identifier of the publishable key the operation acts on. It is the key's
         * `id`, not the `pk_` value the key itself is.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchOrganizationPublishableKey(
            organization_id: kotlin.String,
            key_id: kotlin.String,
            body: AdminSchema.PublishableKeyUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchOrganizationPublishableKeyParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.PublishableKey, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchOrganizationPublishableKeyParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchOrganizationPublishableKey,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/publishable-keys/" + com.mountaya.client.encodePathSegment(key_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.PublishableKeyUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.PublishableKey.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Create a publishable key.
         *
         * Creates a publishable key with exactly the scopes the request names: none
         * is added because another is present. Both the name and the scopes are
         * required, and an empty `scopes` list is a `400` naming `scopes`: a key with
         * no scopes reaches nothing, so there is no useful default to fall back on.
         * The key value is returned here and on every later read, so nothing is lost
         * by not recording it now. A create sent twice without an `Idempotency-Key`
         * makes two keys; with one, the second is answered the first one's key. A
         * create sent again with it, after the first failed with a `5xx` once the
         * key was made, is the `409` coded `IDEMPOTENCY_ANSWER_UNAVAILABLE`, whose
         * `extensions.metadata.location` names the key made,
         * `/v1/organizations/{organization_id}/publishable-keys/{key_id}`, so carry
         * on with that one rather than creating it again.
         * - Owner or admin only (403 otherwise).
         * - A key carrying `management` carries no other scope. A `scopes` list
         *   naming `management` beside another scope is a `400` and no key is
         *   created: the answer carries one `VALIDATION_FAILED` entry per scope
         *   listed beside `management`, each naming that scope's position in
         *   `scopes`. It is asked before any scope's product, so such a list is
         *   never answered `402`.
         * - A key carrying `studio-embedded` carries `tiles` and `data` beside it,
         *   the scopes its editor's map and previews are requested under. A
         *   `scopes` list naming `studio-embedded` without one of them is a `400`
         *   and no key is created: the answer carries one `VALIDATION_FAILED` entry
         *   per missing scope, each naming `scopes`. Neither is added for the
         *   caller, and it is asked before any scope's product, so such a list is
         *   never answered `402`.
         * - A scope can be granted only while the organization holds its product.
         *   Granting one it does not hold is a `402` and no key is created: the
         *   answer carries one entry per refused scope, each naming that scope's
         *   position in `scopes`. `publishing` needs Map Publishing, `studio-embedded`
         *   needs Embedded Studio and Map Studio, and `tiles` needs the Tile API,
         *   each granted by the organization's agreement with Mountaya. `data` and
         *   `mcp` need the Data API, which the organization holds once its first
         *   card has opened its Data API subscription, or by its agreement, so they
         *   are refused before that. `management` belongs to no product and is
         *   never refused this way.
         * - A key keeps a scope whose product is later switched off. The scope
         *   answers `402` wherever a request uses it, and works again once the
         *   product returns.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationPublishableKey(
            organization_id: kotlin.String,
            body: AdminSchema.PublishableKeyCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationPublishableKeyParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.PublishableKey, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationPublishableKeyParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationPublishableKey,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/publishable-keys",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.PublishableKeyCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.PublishableKey.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Members is the `admin.members` operations of the REST API. */
    public class Members internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Remove a member from an organization, or leave it.
         *
         * Removes one member from the organization. What they created stays with the
         * organization, and their seat is freed. A member naming themself in the path
         * is leaving.
         * - Removing somebody else takes the owner or admin role. Under a key,
         *   removing a member who holds `admin` is a 403 whoever asks: the owner's
         *   session under a key is answered as an admin's, and only the owner
         *   removes an admin, signed in to Mountaya's web application.
         * - Leaving is open to contributors and viewers. Under a key, an admin
         *   naming themself is a 403, and so is the owner, answered as an admin
         *   there: an admin leaves only from Mountaya's web application, since a
         *   session token names whoever the holder of a secret key chose.
         * - The owner cannot be removed, and cannot leave from Mountaya's web
         *   application either: either is a 409. The owner hands the organization
         *   to another member there first; the former owner is then an admin, who
         *   leaves or is removed there.
         * - Removing is an act on a membership rather than on a resource: for an
         *   owner or admin, a `user_id` that matches no member who has joined the
         *   organization answers 200 and changes nothing, whether or not an account
         *   carries it, so a removal sent again, or racing another, is not an error.
         *   An invitation is not a membership and is not withdrawn here:
         *   `deleteOrganizationInvitation` withdraws it.
         * - A member who has left holds no role, so a leave sent again is refused
         *   with 403 unless it carries the first request's `Idempotency-Key`, which
         *   answers it with the first one's 200.
         * - The removed member's session tokens act as nobody in the organization
         *   from their next request: every operation asking a role refuses them,
         *   and every read, every tile and the Data API serve them published
         *   content only, as the key alone is served.
         * - Removing a member, or a member leaving, also ends every MCP connector
         *   that person authorized through Mountaya's OAuth consent, whichever
         *   organization's key it was bound to, when they joined the organization
         *   themself, signed in to Mountaya's web application: by creating it or by
         *   accepting its invitation there. The consents are ended for the person,
         *   not for this organization. A connector bound to this organization's key
         *   stops within a minute; one bound to another organization they still
         *   belong to keeps working until its access token expires and, once the
         *   consents are ended, cannot renew it, so they authorize it again. The
         *   removal is answered `200` once the membership is removed: ending the
         *   consents never fails that answer, waits on the provider for at most five
         *   seconds, and is retried until it is done. A connector the person
         *   authorizes again before it is done, for another organization or for this
         *   one once invited back, is ended with the rest, and they authorize it
         *   once more.
         * - A membership accepted under a publishable key, with a session token
         *   naming the person, ends none of their consents when it is removed: the
         *   organization made it without a session of the person's own, so its
         *   removal leaves their connectors to their other organizations alone. A
         *   connector bound to this organization's key still stops within a minute,
         *   since it is refused while the person is not a member. How the
         *   membership was accepted decides, whoever removes it and whichever
         *   credential the removal is sent under. A membership accepted before this
         *   release counts as accepted in Mountaya's web application, however it
         *   was accepted.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member of any role, whom the request acts as. A session
         * token minted without `user_id` is refused with **401**, and a publishable
         * key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param user_id Identifier of the member the operation acts on, the `user_id` a member
         * listing carries. Naming the user the session token names is how a member
         * acts on their own membership. An identifier that matches no member who
         * has joined the organization is answered as each operation says, alike
         * whether or not an account carries it, so no status tells a caller which
         * user identifiers exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteOrganizationMember(
            organization_id: kotlin.String,
            user_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.DeleteOrganizationMemberParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.DeleteOrganizationMemberParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteOrganizationMember,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/members/" + com.mountaya.client.encodePathSegment(user_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List an organization's members.
         *
         * Returns everybody who has joined the organization, with the role each
         * holds and when they joined. An invitation still waiting for an answer is
         * not a member and is never listed here: `getOrganizationInvitations` lists
         * those, and an invited person appears here once they accept. A member whose
         * account cannot be resolved at that moment is listed without `name` and
         * `email`.
         * - A session token minted for `api` without `user_id` is asked for no member
         *   role on this read; one minted for `studio-embedded` without `user_id` is
         *   the `401` a key alone gets. One that names a user is answered only while
         *   that user is a member of the organization, in any role, and is a 403
         *   otherwise.
         * - The writes beside it act as the user the session token names:
         *   `patchOrganizationMember` changes a member's role, and
         *   `deleteOrganizationMember` removes a member or lets one leave. The
         *   owner's own acts, granting, taking away or removing `admin`, an admin
         *   leaving and handing the organization to another member, are made only
         *   by the owner, signed in to Mountaya's web application.
         * - The listing pages by cursor. `role` narrows it to the members holding
         *   one role, and `sort` orders it by `joined_at`, oldest first unless asked
         *   otherwise. Follow `extensions.metadata.pagination.next_cursor` until it
         *   reads `null`, which is the end of the listing.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationMembers(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationMembersParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.OrganizationMember>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetOrganizationMembersParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationMembers,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/members",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("role" to it.value) }
                            given.slot3?.let { add("sort" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.OrganizationMember.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getOrganizationMembers` answers, one row at a time, requesting a page
         * only as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetOrganizationMembers(
            organization_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationMembersParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.OrganizationMember> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getOrganizationMembers(
                    organization_id = organization_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Change a member's role.
         *
         * Changes what one member may do in the organization. The role is the whole
         * of what a membership carries that a caller can change; the member keeps
         * their `joined_at`, and the new role governs every request they make from
         * the next one.
         * - Owner or admin only (403 otherwise).
         * - Under a key, `admin` is neither granted nor taken away: granting it, and
         *   changing the role of a member who holds it, an admin's own role
         *   included, is a 403 whoever asks. The owner's session under a key is
         *   answered as an admin's, because a session token names whoever the
         *   holder of a secret key chose, so these acts are made only by the owner,
         *   signed in to Mountaya's web application. A key moves members between
         *   `contributor` and `viewer`.
         * - The owner's role cannot be changed, the owner's own request included: it
         *   is a 409. Ownership moves only in Mountaya's web application, where the
         *   owner hands it to another member and becomes an admin. `owner` is not a
         *   value this takes, so asking for it is a 400.
         * - A `user_id` that matches no member who has joined the organization is a
         *   404, whether or not an account carries it, and whether or not an invitation
         *   to that person is waiting. An invitation's role is not changed here:
         *   withdraw it with `deleteOrganizationInvitation` and invite again.
         * - Sending the role the member already holds changes nothing and answers the
         *   member as they stand.
         * - A change of role changes nothing about seats: a member holds one seat
         *   whatever their role, so no role costs more than another and no change is
         *   refused for the organization's seats.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param user_id Identifier of the member the operation acts on, the `user_id` a member
         * listing carries. Naming the user the session token names is how a member
         * acts on their own membership. An identifier that matches no member who
         * has joined the organization is answered as each operation says, alike
         * whether or not an account carries it, so no status tells a caller which
         * user identifiers exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchOrganizationMember(
            organization_id: kotlin.String,
            user_id: kotlin.String,
            body: AdminSchema.OrganizationMemberUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchOrganizationMemberParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.OrganizationMember, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchOrganizationMemberParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchOrganizationMember,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/members/" + com.mountaya.client.encodePathSegment(user_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.OrganizationMemberUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.OrganizationMember.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Organizations is the `admin.organizations` operations of the REST API. */
    public class Organizations internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Delete an organization's AI provider configuration.
         *
         * Removes the organization's AI provider configuration, the stored
         * credential with it. Without one the co-pilot cannot run for this
         * organization's resources: `postAgentRun` answers 409
         * `AI_PROVIDER_NOT_CONFIGURED` until `postOrganizationAiProviderConfig`
         * creates a new one, and a run already in flight ends with an error at its
         * next model call.
         * - Owner or admin only (403 otherwise).
         * - Deleting is an act on the organization's state, so with none configured
         *   it answers 200 and changes nothing, whatever `If-Match` names: there is
         *   no configuration left for the condition to protect.
         * - `If-Match` makes it conditional on the configuration's `etag`, so a
         *   configuration somebody changed since it was read is not deleted
         *   unseen.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteOrganizationAiProviderConfig(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.DeleteOrganizationAiProviderConfigParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.DeleteOrganizationAiProviderConfigParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteOrganizationAiProviderConfig,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/ai-provider-config",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List the AI provider catalog.
         *
         * Returns the catalog of AI providers an organization may configure for the
         * studio co-pilot: each provider's id, label, selectable model ids, and the
         * credential fields it requires. The catalog is the same for every caller (no
         * per-organization data) and is safe to cache. It asks no member role, so a
         * session token is answered alike whoever it names, or, minted for `api`, with
         * no user at all; one minted for `studio-embedded` without `user_id` is a
         * `401`.
         *
         * It is the vocabulary `postOrganizationAiProviderConfig` and
         * `patchOrganizationAiProviderConfig` check a configuration against: the
         * model is one of the provider's models, and the credential carries exactly
         * the fields the provider names.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getAiProviders(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetAiProvidersParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.AiProviderCatalog>, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetAiProvidersParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getAiProviders,
                    path = "/v1/ai-providers",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.AiProviderCatalog.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve an organization.
         *
         * Returns the organization: its name, whether it awaits its agreement,
         * whether it may leave the Mountaya mark off its maps (`white_label`), and
         * the Map Publishing settings that shape what an embed of it looks like.
         *
         * Every credential is served the same object. Nothing is trimmed for a
         * publishable key or a share token: the settings an embed needs to paint
         * itself, `safety_rules.domains` among them, are what this bootstrap read
         * exists to serve. What the organization's agreement entitles it to is read
         * elsewhere: the products it holds as `organization_capabilities` on
         * `getMyOrganizations`, and its subscription with
         * `getOrganizationBillingSubscription`.
         *
         * The path names the key's own organization: a key reaches its own
         * organization and no other, and naming another is a `403` whether or not it
         * exists. A share token reaches the organization that owns what its link
         * names, because a share view is an embed: it paints the same layers, reads
         * the same branding and needs the same settings to do it. Naming any other
         * organization under a share token is the same `403`.
         *
         * An embed that does not know which organization it belongs to resolves one
         * from its own key with `postOrganizationLookup`, which takes the key itself
         * rather than an id.
         *
         * Reads are limited per minute. A publishable key sent without a session
         * token, or with one minted without `user_id`, is held to 600 per client IP
         * address and then 6000 per organization; a key with a session token naming a
         * user to 1200 per user the token names and then 6000 per organization,
         * counted apart from the reads made under the key alone; and a share token to
         * 600 per client IP address and then 6000 per organization that owns the link.
         * Past any of them the read is a `429` carrying `Retry-After`. A share token
         * not resolved moments earlier is refused the same way when its client IP
         * address has had 60 unknown share tokens refused within the minute.
         *
         * A publishable key needs the `publishing` scope, or the `studio-embedded`
         * scope that includes it. A share token carries its own authority and needs
         * none.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganization(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<com.mountaya.client.generated.RestSchema.Organization, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganization,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.generated.RestSchema.Organization.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve an organization's AI provider configuration.
         *
         * Returns the organization's AI provider configuration for the studio
         * co-pilot, masked: the provider, the model, the base URL override if one is
         * set, and a non-sensitive hint of the credential. The credential itself is
         * never returned.
         *
         * An organization with no configuration is not a missing resource: it
         * answers 200 with `data: null`, which is the state a client renders a setup
         * prompt from, and which `postOrganizationAiProviderConfig` leaves.
         * - A session token minted for `api` without `user_id` is asked for no member
         *   role on this read; one minted for `studio-embedded` without `user_id` is
         *   the `401` a key alone gets. One that names a user is answered only while
         *   that user is a member of the organization, in any role, and is a 403
         *   otherwise.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationAiProviderConfig(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationAiProviderConfigParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.AiProviderConfig?, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationAiProviderConfigParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationAiProviderConfig,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/ai-provider-config",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.nullableSerializer(AdminSchema.AiProviderConfig.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve an organization's Map Publishing settings.
         *
         * Returns the settings that shape an organization's embeds: the
         * default props a viewer starts from, the map theme, and the safety rules a
         * visitor can never exceed. Read it against the same object the PATCH beside
         * it replaces: this is the whole of what that operation writes, and nothing
         * else.
         * - A session token minted for `api` without `user_id` is asked for no member
         *   role on this read; one minted for `studio-embedded` without `user_id` is
         *   the `401` a key alone gets. One that names a user is answered only while
         *   that user is a member of the organization, in any role, and is a 403
         *   otherwise. Changing the settings takes the owner or admin role, which is
         *   why the PATCH needs a session token naming a user and this read does not.
         * - The settings carry an `etag` of their own, versioned apart from the
         *   organization's: a settings change moves it and leaves the
         *   organization's alone, and a rename moves the organization's and leaves
         *   this one alone, so neither ever refuses the other.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationSettings(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationSettingsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<com.mountaya.client.generated.RestSchema.OrganizationSettings, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationSettingsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationSettings,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/settings",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.generated.RestSchema.OrganizationSettings.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve an organization's data usage.
         *
         * Returns data usage a day at a time, one figure per operation per surface
         * per metric per status class. A combination with usage anywhere in the
         * range carries a figure for every day of it, `0` on the days it had none.
         * `requests` is recorded for every call that gets past the credential
         * check; the other metrics count the work one kind of call did, so each
         * appears only beside the operation that can emit it. A refused request
         * records `requests` alone, because the work the other metrics count never
         * happened, and one refused before any operation ran is counted under each
         * operation its document names. `UsageDataResourceType` says which refusals
         * are counted nowhere. A request whose caller went away before the server
         * began to answer it, by closing its connection or cancelling the request,
         * is counted nowhere, however long it had waited, since its answer reached
         * nobody. A caller that leaves once its answer has begun does not change how
         * it is counted. A request too slow is the server's to fail: one answered
         * `504` because its time budget ran out is counted as a `5xx`. So is one the
         * server could not finish checking once it knew the organization, answered
         * `500`, on the surface its session token was minted for, and a root field
         * that fails once its operation has run, its answer included, is counted as
         * the failure it was answered with, never as the success its work reached
         * first. A call through Mountaya's MCP server is counted as the same call
         * made to this API directly, its refusals included. One the MCP server could
         * not deliver to this API once it held a session for it, this API
         * unreachable or what stands in front of it answering in its place, is
         * counted as a `5xx` on the `mcp` surface; one it could get no session for
         * names no organization, and is counted nowhere.
         * - A session token minted for `api` without `user_id` is what authorizes this
         *   read, and no member role is checked on top of it; one minted for
         *   `studio-embedded` without `user_id` is the `401` a key alone gets. One
         *   that names a user is answered only while that user holds the owner or
         *   admin role in the organization, and is a 403 otherwise. The path names the
         *   key's own organization: another is a 403, whether or not it exists.
         * - Omit `start_at` and `end_at` and the range is the current month plus the
         *   three complete months before it. A lone `start_at` runs to the end of the
         *   current month, or to a year after it when that comes first, and a lone
         *   `end_at` starts a year before it.
         * - A `start_at` after `end_at` is a `400`, and so is a window of more than
         *   366 calendar days, both ends included, an `end_at` more than 365 days
         *   after `start_at`, refused at both parameters: ask for a long history a
         *   year at a time.
         * - `resource_type` and `metric` may be combined, but only into a pair that
         *   exists: `polygons` belongs to `isochrones` and nothing else, so asking for
         *   it beside `directions` is a 400 rather than an empty list. A value
         *   outside `UsageDataResourceType`, `UsageMetric` or `UsageDataSurface` is
         *   a `400` naming its parameter.
         * - Usage is recorded under one of four surfaces: `api`, a request to the
         *   Data API from one of the organization's own clients; `mcp`, a request
         *   made through Mountaya's MCP server; `studio-embedded`, the work an
         *   Embedded Studio did for the organization under one of its keys, which is
         *   the routing a Map Studio write did (a routed segment, an import preview
         *   and confirm, or a zone computed from a source) and every Data API
         *   request made with a session token minted for `studio-embedded` while
         *   the organization holds Embedded Studio, such a request being `api` while
         *   it does not; and `studio-online`, the same work done by Map
         *   Studio in Mountaya's web
         *   application for a member of the organization. Only `api` and `mcp`
         *   count toward the Data API's per-minute limit, its monthly allowance and
         *   the invoice. The two studio surfaces are reported so the organization
         *   can see them, and are never counted. `surface` keeps one of them;
         *   omitted, all four are reported.
         * - The figures are a series over the window, not a listing of resources, so
         *   they are not paged: every figure of the window comes back at once.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationUsageData(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationUsageDataParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.UsageDataPoint>, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.GetOrganizationUsageDataParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationUsageData,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/usage/data",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("start_at" to it) }
                            given.slot1?.let { add("end_at" to it) }
                            given.slot2?.let { add("used_publishable_key" to it) }
                            given.slot3?.let { add("resource_type" to it.value) }
                            given.slot4?.let { add("metric" to it.value) }
                            given.slot5?.let { add("surface" to it.value) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.UsageDataPoint.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve an organization's Tile API usage.
         *
         * Returns Tile API usage a day at a time, one figure per kind of resource per
         * surface per status class. The split is deliberate: a day on which a resource
         * was served from both the embed and the API, or both succeeded and failed,
         * yields one figure per combination rather than one total, so a reader can add
         * them for a headline number, take the `2xx` ones of the `api` and
         * `embedding` surfaces, `terrain` aside, for what the monthly allowance
         * counts, or
         * compare `2xx` against `5xx` for availability. A request whose caller went
         * away before the server began to answer it, as a map does with the tiles it
         * stops waiting for when it moves, is counted nowhere, however long it had
         * waited. A caller that leaves once its tile has begun does not change how
         * it is counted. A request the server could not finish checking once it
         * knew the organization, its own or the one a share link belongs to, is
         * answered `500` and counted as a `5xx`. A combination with usage anywhere
         * in the range carries a figure for every day of it, `0` on the days it had
         * none.
         * - A session token minted for `api` without `user_id` is what authorizes this
         *   read, and no member role is checked on top of it; one minted for
         *   `studio-embedded` without `user_id` is the `401` a key alone gets. One
         *   that names a user is answered only while that user holds the owner or
         *   admin role in the organization, and is a 403 otherwise. The path names the
         *   key's own organization: another is a 403, whether or not it exists.
         * - Omit `start_at` and `end_at` and the range is the current month plus the
         *   three complete months before it. A lone `start_at` runs to the end of the
         *   current month, or to a year after it when that comes first, and a lone
         *   `end_at` starts a year before it.
         * - A `start_at` after `end_at` is a `400`, and so is a window of more than
         *   366 calendar days, both ends included, an `end_at` more than 365 days
         *   after `start_at`, refused at both parameters: ask for a long history a
         *   year at a time.
         * - A `resource_type` outside `UsageTileResourceType` is a `400` naming it.
         * - The `api` surface, the organization's own clients, and the `embedding`
         *   surface, a tile drawn inside the Mountaya-hosted embed, count toward
         *   the Tile API's per-minute limit and its monthly allowance, and the
         *   allowance counts their figures of class `2xx` alone: a tile refused or
         *   failed is recorded here and never counted toward it. The other surfaces
         *   are recorded so the organization can see them and are never counted: a
         *   `studio-embedded` figure is a tile an Embedded Studio drew, a
         *   `studio-online` figure one Map Studio in Mountaya's web application drew
         *   for a member of the organization, and a `share` figure one drawn through
         *   one of the organization's share links. A `terrain` figure, the base map
         *   served through the Tile API, is recorded on whichever surface asked for
         *   it and never counted, on any surface.
         * - The figures are a series over the window, not a listing of resources, so
         *   they are not paged: every figure of the window comes back at once.
         *
         * Like every read made with a session token naming a user, it is limited to
         * 1200 a minute per user the token names and then to 6000 a minute per
         * organization, and one made with a session token minted without `user_id` is
         * held as the key alone is, to 600 a minute per client IP address and then
         * 6000 per organization; past either, the answer is a `429` carrying
         * `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token.
         * A publishable key without a session token is not accepted here: a key
         * written to be shipped in a browser cannot be sufficient on its own to read
         * or change an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationUsageTiles(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationUsageTilesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.UsageTilePoint>, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.GetOrganizationUsageTilesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationUsageTiles,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/usage/tiles",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("start_at" to it) }
                            given.slot1?.let { add("end_at" to it) }
                            given.slot2?.let { add("used_publishable_key" to it) }
                            given.slot3?.let { add("resource_type" to it.value) }
                            given.slot4?.let { add("resource_id" to it) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.UsageTilePoint.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update an organization.
         *
         * Changes the organization's own fields, merged into what is stored: a field
         * left out keeps its value, and an empty object changes nothing and answers
         * the organization as it stands. The display name is the whole of what is
         * settable here: the Map Publishing settings are `patchOrganizationSettings`,
         * the products the account holds follow from its agreement or its Data API
         * subscription, and its limits from its agreement or the defaults. A change
         * made here therefore cannot carry one of those along by accident.
         * - Owner or admin only (403 otherwise).
         * - A name of more than 30 characters as sent, or of fewer than 2 once leading
         *   and trailing whitespace is trimmed, is a 400.
         * - It does not change the name invoices are addressed to once one has been
         *   set through `patchOrganizationBillingInfo`.
         * - `If-Match` makes it conditional on the organization's `etag`. The
         *   settings carry an `etag` of their own, so a settings change made in the
         *   meantime never refuses this one.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchOrganization(
            organization_id: kotlin.String,
            body: AdminSchema.OrganizationUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchOrganizationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<com.mountaya.client.generated.RestSchema.Organization, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchOrganizationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchOrganization,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.OrganizationUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.generated.RestSchema.Organization.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update an organization's AI provider configuration.
         *
         * Changes the organization's AI provider configuration, merged into what is
         * stored: a field left out keeps its value, and an empty object changes
         * nothing and answers the configuration as it stands.
         * - Owner or admin only (403 otherwise).
         * - `model` must be one of the configured provider's models in
         *   `getAiProviders`. `base_url` set to null clears the override, so
         *   completions go to the provider's default endpoint again.
         * - `credential` replaces the stored credential whole, carrying every field
         *   the configured provider names, each non-empty; omitted, the stored
         *   credential is kept, so the model can be changed without sending the key
         *   again. It is write-only and never returned.
         * - A `base_url` other than the one stored must come with `credential`: the
         *   stored credential is never sent to a host it was not stored for, so a
         *   new URL without it is a 400 with one entry at `["credential"]`, and
         *   nothing changes. A null `base_url`, which returns completions to the
         *   provider's own endpoint, keeps the stored credential.
         * - The provider is not a field here: switching providers is
         *   `deleteOrganizationAiProviderConfig` and then
         *   `postOrganizationAiProviderConfig`.
         * - With none configured there is nothing to change: the answer is a 409
         *   coded `AI_PROVIDER_NOT_CONFIGURED`, whatever the body or `If-Match`
         *   says. It comes before any 400 this operation judges of the body (a
         *   model the provider lacks, a `base_url` refused or sent without
         *   `credential`, a credential of the wrong shape), since each is judged
         *   against the configured provider. Create one with
         *   `postOrganizationAiProviderConfig`.
         * - `If-Match` makes it conditional on the configuration's `etag`.
         * - A change takes effect at the next model call of every run, a run already
         *   in flight included.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchOrganizationAiProviderConfig(
            organization_id: kotlin.String,
            body: AdminSchema.AiProviderConfigUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchOrganizationAiProviderConfigParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.AiProviderConfig, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchOrganizationAiProviderConfigParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchOrganizationAiProviderConfig,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/ai-provider-config",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.AiProviderConfigUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.AiProviderConfig.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update an organization's Map Publishing settings.
         *
         * Applies a partial update to the settings that shape an organization's
         * embeds: the default props, the map theme, and the safety rules.
         * Only the groups present in the request body are replaced; the omitted groups
         * are preserved. A group that is present carries every one of its fields, so
         * a partial group is a 400 rather than a partial change.
         * - Owner or admin only (403 otherwise). That is the role difference from the
         *   GET beside it, and the reason this one needs a session token naming a
         *   user.
         * - `If-Match` makes it conditional on the settings' own `etag`, which only
         *   a settings change moves: a rename made in the meantime never refuses it.
         * - Three safety rules are set by Mountaya, and a request sends them as they
         *   stand: `safety_rules.with_overlays` and
         *   `safety_rules.with_overlays_control` on every organization, and
         *   the sites of `safety_rules.domains` on an organization covered by an
         *   agreement. A `safety_rules` group that changes any of them is a `403`
         *   whose entry names that field, and nothing is changed. `with_overlays` is
         *   compared as a set, so the order of its slugs changes nothing, and the
         *   sites of `domains` are compared as a set once each entry is normalized
         *   the way it is stored, its loopback entries (`localhost`, a name under
         *   `.localhost`, `127.0.0.0/8`, `[::1]`) left out: an administrator adds and
         *   removes those on any organization. A self-serve organization's
         *   administrators set its `domains` whole.
         * - `safety_rules.domains` holds as many counted entries as the
         *   organization's agreement allows, 1 unless it says otherwise, a loopback
         *   entry never counting: a request that would leave more, and more than the
         *   list counts now, is refused at `allowed-domains`, a `429` coded
         *   `LIMIT_REACHED` with no `Retry-After`, and nothing is changed. Remove one
         *   first, or agree a higher limit. A list counting more than a lowered limit
         *   is kept: its entries may be edited or removed, and no counted entry
         *   added until it counts fewer than the limit.
         * - The organization's limits, the per-minute rate limits and monthly
         *   allowances among them, are not settings and are not here: they are the
         *   organization's agreement's, and a caller that exhausts one is refused by
         *   the surface that limit governs.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchOrganizationSettings(
            organization_id: kotlin.String,
            body: AdminSchema.OrganizationSettingsPatchParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchOrganizationSettingsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<com.mountaya.client.generated.RestSchema.OrganizationSettings, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchOrganizationSettingsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchOrganizationSettings,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/settings",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.OrganizationSettingsPatchParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.generated.RestSchema.OrganizationSettings.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Create an organization.
         *
         * Creates an organization and makes the user the session token names its
         * owner.
         * - The new organization is not reachable with the key that created it. A
         *   publishable key acts on its own organization only, so every operation
         *   naming the new one is a `403` under that key, and no operation in this
         *   contract issues a key into another organization. Its subscription, keys
         *   and settings are managed with a key issued in it.
         * - It starts with no subscription and holds no product, so every
         *   operation that needs one answers `402` until it does. There are two
         *   ways in. Self-serve, its first card opens the metered Data API
         *   subscription, through `postOrganizationBillingProvision`,
         *   `postOrganizationBillingPaymentMethodSetup` and
         *   `postOrganizationBillingPaymentMethod` in that order, and from then it
         *   holds `data` for as long as that subscription is in force. Under an
         *   agreement with Mountaya, which `agreement_requested` asks for, it holds
         *   exactly the products the agreement names once it is set: Map Studio,
         *   Map Publishing, the Tile API, Embedded Studio and White label are only
         *   ever granted this way. Until then `awaiting_agreement` reads true and
         *   the organization is not asked for a card.
         * - Creating it also opens its billing account, whichever of the two ways
         *   it takes, normally within seconds of the answer and otherwise by the
         *   next weekly check at the latest: nothing is charged and no subscription
         *   is opened. Until the account is open, the billing
         *   operations that need one answer as they do for an organization without
         *   one, and `postOrganizationBillingProvision` opens it at once.
         * - The optional `website` seeds the embed domain allowlist from its host, so
         *   a new organization's keys work from its own site from the start. Omit it
         *   and the allowlist starts empty: no page but Mountaya's own can call the
         *   API with the organization's keys from a browser, and no site but
         *   Mountaya's own can frame its hosted embed, until it is set: by
         *   `patchOrganizationSettings` on a self-serve organization, and by
         *   Mountaya, as the agreement states, once an agreement covers it. Calls
         *   from the organization's own servers, which name no page, work either
         *   way.
         * - A name of more than 30 characters as sent, or of fewer than 2 once leading
         *   and trailing whitespace is trimmed, is a 400.
         * - Send an `Idempotency-Key`: a create sent again without one makes a second
         *   organization. A create sent again with it, after the first failed with a
         *   `5xx` once the organization was made, is the `409` coded
         *   `IDEMPOTENCY_ANSWER_UNAVAILABLE`, whose `extensions.metadata.location`
         *   names the organization made, `/v1/organizations/{organization_id}`, so
         *   carry on with that one rather than creating it again.
         * - Each user creates at most 10 organizations a minute and 50 a day. Past
         *   either, the request is a `429` carrying `Retry-After`, and nothing is
         *   created.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with a write role, whom the request acts as. A
         * session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganization(
            body: AdminSchema.OrganizationCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<com.mountaya.client.generated.RestSchema.Organization, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganization,
                    path = "/v1/organizations",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.OrganizationCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, com.mountaya.client.generated.RestSchema.Organization.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Configure an organization's AI provider.
         *
         * Creates the organization's AI provider configuration for the studio
         * co-pilot: the provider, the model, the credential, and optionally a base
         * URL override. From then on the organization can start agent runs, which
         * `postAgentRun` refuses while none is configured.
         * - Owner or admin only (403 otherwise).
         * - An organization has one configuration at most: creating one while one
         *   exists is a 409. Change it with `patchOrganizationAiProviderConfig`;
         *   the provider is fixed once configured, so switching providers is
         *   `deleteOrganizationAiProviderConfig` and then this.
         * - The model must be one of the provider's models, and the credential must
         *   carry every field the provider names and no other, each non-empty, as
         *   `getAiProviders` lists them; anything else is a 400 naming the field.
         * - The credential is write-only: it is encrypted at rest and never
         *   returned. The answer is the masked configuration, with a hint of the
         *   credential in its place.
         *
         * Requires a publishable key with the `management` scope AND a session token
         * naming a user, a member with the owner or admin role, whom the request acts
         * as. A session token minted without `user_id` is refused with **401**, and a
         * publishable key without a session token is not accepted here: a key written
         * to be shipped in a browser cannot be sufficient on its own to read or change
         * an organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationAiProviderConfig(
            organization_id: kotlin.String,
            body: AdminSchema.AiProviderConfigCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationAiProviderConfigParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.AiProviderConfig, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationAiProviderConfigParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationAiProviderConfig,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/ai-provider-config",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.AiProviderConfigCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.AiProviderConfig.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Sessions is the `admin.sessions` operations of the REST API. */
    public class Sessions internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Create a session token using a secret key.
         *
         * Creates a short-lived session token by exchanging a secret key and a
         * publishable key. The `X-API-Key` header with an `sk_` value is required to
         * authenticate this request. The token accompanies the publishable key it is
         * minted against: every Data API request, every `management` operation and
         * every `studio-embedded` operation require one beside the key.
         *
         * Give it `surface` to say what the token is for. Omitted, it is `api`, the
         * surface of your own clients. Every Embedded Studio you mount, the editor
         * and its read-only viewer alike, needs a token minted for
         * `studio-embedded`: it is the only token a `studio-embedded` operation
         * accepts, a token minted for any other surface being a `401` there, and
         * what the studio reaches with it on the Data API and the Tile API is
         * recorded as `studio-embedded` usage and never counted toward the
         * organization's limits, allowances or invoice. Such a token is minted only
         * for a publishable key that reaches the `studio-embedded` scope, and a key
         * that does not is a `403`, and only for an organization that holds
         * Embedded Studio, one that does not being the `402` a `studio-embedded`
         * operation answers. It names a member for the editor, or is minted without
         * `user_id` for a viewer, which acts as nobody and, like every token minted
         * without `user_id`, reads what the publishable key alone does. A viewer
         * that must show drafts takes a token naming the member who looks. A token
         * already minted stays valid for its whole life whatever the organization
         * holds, but what it reaches on the Data API and the Tile API while the
         * organization does not hold Embedded Studio is recorded and counted as
         * `api` usage.
         *
         * Give it `user_id` to mint a token naming one user, as your backend asserts
         * after signing that user in to your own product: a member of the key's
         * organization, or a user whose verified email address has a pending
         * invitation to it. The token names that user for its whole life and no
         * header can name anybody else, so a page holding it acts as that user and
         * nobody else. The answer's `user_id` is the user the token names, or null
         * for a token minted without one.
         * - A token naming a user is what acting as a user requires. Every operation
         *   whose `x-acting-user` is `required`, which is every `studio-embedded`
         *   operation, every `management` write and every operation under `/v1/me`,
         *   refuses with a `401` a token minted without `user_id`, and acts as the
         *   user the token names, answered only while that user holds the role it
         *   asks. A user whose invitation is still pending holds no role, and
         *   reaches only the operations under `/v1/me` marked `x-pending-invitee`.
         * - The session acts as that member with at most an admin's say over the
         *   organization's members. Any admin can create the secret key that mints a
         *   session, and the session can name any member, so the owner's own acts,
         *   granting, taking away or removing `admin`, inviting an admin, an admin
         *   leaving and handing the organization on, are refused under any session
         *   with a `403`, a session naming the owner included: the owner makes them
         *   signed in to Mountaya's web application.
         * - A token minted without `user_id` reads what the publishable key alone
         *   does, whatever surface it was minted for: the last published snapshot
         *   and never a draft, and public-only listings, and a backend that must
         *   read drafts mints for a member. It reaches no operation that acts as a
         *   user. Minted for `api` against a key that carries `management`, it
         *   reaches every `management` read with no member role asked, so such a
         *   token stays on your own backend; a token you hand to an anonymous
         *   visitor is minted for a key without `management`. Minted for
         *   `studio-embedded`, the `management` reads answer it the `401` a key
         *   alone gets.
         * - On a `management` read, a token naming a user is answered only while
         *   that user holds the role the read asks of a member of the organization,
         *   and is otherwise the `403` a member without that role gets.
         *
         * A token is valid for at most five minutes. A token minted moments earlier
         * for the same publishable key, the same surface and the same user, or for
         * the same key and surface and no user, is returned again while it has more
         * than two minutes to run, so read `expires_at` rather than assuming the
         * full lifetime. Sending the same
         * request twice is harmless for that reason: the second is answered the
         * first one's token, or another for the same key and user, and every token
         * stays valid until its own expiry.
         *
         * An unknown, revoked or malformed secret key is a `401`, the same answer as
         * sending none, and so is a key of an organization that has been canceled,
         * so this operation cannot be used to learn which secret keys exist.
         *
         * Five limits hold this operation, and each is answered `429` with
         * `Retry-After`, the seconds left in the minute:
         * - Each client IP address, an IPv6 address counted by its `/64` prefix, may
         *   fail 60 times a minute. Only a request that fails counts toward it, so a
         *   backend minting tokens for many users from one address is never held to
         *   it while its requests succeed. A request Mountaya's MCP server forwards
         *   is counted against the address of the person it acts for, which the
         *   server forwards, rather than against the server's own, and that address
         *   may fail 6,000 times a minute there: an assistant reaches the MCP server
         *   from a few addresses its provider shares among all its users, so one
         *   organization's failing key would otherwise lock out every other
         *   organization's sessions behind the same address.
         * - Each secret key may fail 10 times a minute with a `401`, `403` or `404`.
         *   Past that it is refused for the rest of the minute, even on a request
         *   that is otherwise correct: a deployment pairing its secret key with a
         *   wrong or revoked publishable key locks that secret key out until the
         *   minute ends.
         * - A `403` for `user_id` counts toward a limit of its own instead, 10 a
         *   minute for that secret key and that `user_id`: a token asked for somebody
         *   who has left the organization never locks out the tokens asked for
         *   everybody else.
         * - A refusal for `surface` counts toward a limit of its own instead, 10 a
         *   minute for that secret key and that surface: the `403` of `mcp`, the
         *   `402` of `mcp` that Mountaya's MCP server is answered for an
         *   organization that does not hold the Data API, the `402`
         *   of `studio-embedded` for an organization that does not hold Embedded
         *   Studio and its `403` for a key that does not reach the
         *   `studio-embedded` scope. Pages asking for a `studio-embedded` token for
         *   an organization whose agreement does not include Embedded Studio never
         *   lock out the tokens the same secret key asks for any other surface.
         * - Each secret key mints at most 6000 tokens a minute. Only a token actually
         *   minted counts toward it, and a token returned again is free. This
         *   ceiling, not the failure limits, is what bounds a secret key that has
         *   leaked: its requests succeed, so no failure is counted, and it mints at
         *   most 6000 tokens a minute until it is revoked.
         *
         * Requires no publishable-key scope of its own: the secret key is the
         * credential, and the publishable key named in the body is the subject of the
         * session rather than the caller. The calling-page refusal in this document's
         * introduction does not apply here.
         *
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postSession(
            body: AdminSchema.SessionCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostSessionParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<com.mountaya.client.generated.RestSchema.Session, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.PostSessionParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postSession,
                    path = "/v1/sessions",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.SessionCreateParams.serializer(), body)),
                    options = options,
                )
            return sender.send(call, com.mountaya.client.generated.RestSchema.Session.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Users is the `admin.users` operations of the REST API. */
    public class Users internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Decline an invitation.
         *
         * Declines an invitation waiting for the user the session token names, which
         * frees the seat it held. The organization sees it leave
         * `getOrganizationInvitations`, and may invite the address again.
         * - Only the invited person declines, and only an invitation that is theirs:
         *   one made to an address their account has verified, to the key's
         *   organization.
         * - Declining is an act on the invitation's state rather than on a resource,
         *   so an identifier naming nothing waiting for this user answers 200 and
         *   changes nothing: an invitation already declined, withdrawn or lapsed,
         *   another person's, one to an address the account has not verified, and
         *   one that never existed. A decline racing a withdrawal is therefore not
         *   an error, and nobody learns from it which invitation ids exist.
         * - A user whose declined invitation was their only tie to the organization
         *   holds nothing there afterwards, so a decline they send again is refused
         *   with 403 unless it carries the first request's `Idempotency-Key`, which
         *   answers it with the first one's 200.
         * - An invitation already accepted is gone as well: the person is a member,
         *   and leaves with `deleteOrganizationMember`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, the invited person, whom
         * the request acts as. A publishable key without a session token minted for
         * `studio-embedded` naming a user, a token minted for another surface
         * included, is refused with **401**.
         *
         * @param invitation_id Identifier of the invitation, the `id` an `OrganizationInvitation` or a
         * `UserInvitation` carries. One that names no invitation this caller may see,
         * an expired one included, is answered as each operation says: a read or an
         * acceptance answers it `404`, and a withdrawal or a decline, which leave it
         * gone either way, answer `200`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteMyInvitation(
            invitation_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.DeleteMyInvitationParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.DeleteMyInvitationParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteMyInvitation,
                    path = "/v1/me/invitations/" + com.mountaya.client.encodePathSegment(invitation_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List the invitations waiting for the acting user.
         *
         * Returns the invitations waiting for an answer from the user the session
         * token names, each with the organization's name and the products it holds
         * and the role accepting grants, so a person can decide without a second
         * read. Under a publishable key that is the invitations to the key's
         * organization alone.
         *
         * An invitation is the user's when it was made to an address their account
         * has VERIFIED. One made to an address the account carries unverified is not
         * listed, and is listed from the moment the account verifies it, so nobody
         * reads an invitation by claiming an address they do not hold. A lapsed
         * invitation is not listed.
         * - A user who has not joined the organization reaches this with the session
         *   token `postSession` mints for them because an invitation is waiting for
         *   them: that is how an invited person answers before joining.
         * - `postMyInvitationAccept` accepts an invitation and `deleteMyInvitation`
         *   declines it.
         * - The listing pages by cursor, newest invitation first, with invitations
         *   made at the same instant ordered by `id`; the order is not the
         *   caller's to choose, so the listing takes no `sort`. Follow
         *   `extensions.metadata.pagination.next_cursor` until it reads `null`,
         *   which is the end of the listing.
         *
         * Like every read made with a session token, it is limited to 1200 a minute
         * per user the token names and then to 6000 a minute per organization; past
         * either, the answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member of any role or a
         * user whose invitation to the key's organization is waiting, whom the
         * request acts as. A publishable key without a session token minted for
         * `studio-embedded` naming a user, a token minted for another surface
         * included, is refused with **401**.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getMyInvitations(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetMyInvitationsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.UserInvitation>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetMyInvitationsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getMyInvitations,
                    path = "/v1/me/invitations",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.UserInvitation.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getMyInvitations` answers, one row at a time, requesting a page only
         * as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetMyInvitations(
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetMyInvitationsParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.UserInvitation> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getMyInvitations(
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Retrieve the acting user's notification emails for an organization.
         *
         * Returns which notification emails about the organization the user the session token
         * names receives. Every one is on until the member turns it off, and the security
         * notifications, a role changed or a removal made by somebody else, are always sent:
         * they are listed locked, with the reason.
         * - An owner and an admin are listed the notifications about the organization's
         *   webhook endpoints as well; a contributor and a viewer are not, and are sent none.
         * - The invitation email goes to an address before it belongs to a member, and is
         *   never listed.
         *
         * Like every read made with a session token, it is limited to 1200 a minute per user
         * the token names and then to 6000 a minute per organization; past either, the answer
         * is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session token
         * minted for `studio-embedded` naming a user, a member of the key's organization in
         * any role, whom the request acts as. A publishable key without a session token minted
         * for `studio-embedded` naming a user, a token minted for another surface included, is
         * refused with **401**.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getMyOrganizationNotificationPreferences(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetMyOrganizationNotificationPreferencesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.NotificationPreferences, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetMyOrganizationNotificationPreferencesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getMyOrganizationNotificationPreferences,
                    path = "/v1/me/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/notification-preferences",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.NotificationPreferences.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List the acting user's memberships.
         *
         * Returns the memberships of the user the session token names, each carrying
         * the organization's name and the products it holds beside the role, so a
         * client can label the organization it acts in, and tell whether it holds
         * Map Studio, without a second read. Under a publishable key that is the
         * user's membership of the key's organization alone: a one-entry list, or an
         * empty one while the user has not joined it, as a user whose invitation is
         * still waiting has not. An invitation is not a membership:
         * `getMyInvitations` lists those.
         *
         * Each entry also says what the user may use in the studio there:
         * `studio_entitlements`, the features the organization's agreement covers
         * for that member's role, Map Publishing's publishing, video export and
         * embed among them. Read with a key, the features are narrowed by the
         * organization's safety rules, as what the key is served is: the points of
         * interest, the other overlays and the profile analysis each follow the rule
         * that governs them. An Embedded Studio reads its viewer from them, so it
         * shows what the agreement covers with no mapping of products to features of
         * its own.
         *
         * The listing pages by cursor like every listing of what an organization or
         * its members create, so a client written against it reads
         * `extensions.metadata.pagination.next_cursor` and stops at `null`, which
         * under a publishable key is always the first page. Memberships come in
         * the order of the organizations' names; the order is not the caller's to
         * choose, so the listing takes no `sort`.
         *
         * Like every read made with a session token, it is limited to 1200 a minute
         * per user the token names and then to 6000 a minute per organization; past
         * either, the answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member of any role or a
         * user whose invitation to the key's organization is waiting, whom the
         * request acts as. A publishable key without a session token minted for
         * `studio-embedded` naming a user, a token minted for another surface
         * included, is refused with **401**.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getMyOrganizations(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetMyOrganizationsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.Membership>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetMyOrganizationsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getMyOrganizations,
                    path = "/v1/me/organizations",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.Membership.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getMyOrganizations` answers, one row at a time, requesting a page only
         * as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetMyOrganizations(
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetMyOrganizationsParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.Membership> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getMyOrganizations(
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Retrieve the acting user's display preferences.
         *
         * Returns the six preferences the user the session token names reads and
         * renders by: the language, the theme, and the units of length, pace, slope
         * and temperature.
         *
         * They belong to the PERSON, not to an organization, so the same six are
         * read in every organization the user is a member of. They shape rendering
         * only: no operation returns different figures because of them, and a client
         * formats what it is given.
         *
         * Like every read made with a session token, it is limited to 1200 a minute
         * per user the token names and then to 6000 a minute per organization; past
         * either, the answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member of the key's
         * organization in any role, whom the request acts as. A publishable key
         * without a session token minted for `studio-embedded` naming a user, a token
         * minted for another surface included, is refused with **401**.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getMyPreferences(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetMyPreferencesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.Preferences, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetMyPreferencesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getMyPreferences,
                    path = "/v1/me/preferences",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.Preferences.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Turn the acting user's notification emails for an organization on or off.
         *
         * Turns notification emails about the organization on or off for the user the session
         * token names, and answers them all afterwards. A notification left out keeps its
         * setting. A user changes their own and nobody else's, so the operation takes no user
         * id: it writes those of the user the session token names.
         * - A security notification, `member-role-changed` or `member-removed`, is always
         *   sent: naming one is a `400` naming it, and nothing is changed. So is naming
         *   `invitation`, which no member is sent, or a webhook endpoint's notification as a
         *   contributor or a viewer.
         * - Any role changes them, a viewer's included, since they change nothing the
         *   organization holds.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session token
         * minted for `studio-embedded` naming a user, a member of the key's organization in
         * any role, whom the request acts as. A publishable key without a session token minted
         * for `studio-embedded` naming a user, a token minted for another surface included, is
         * refused with **401**.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchMyOrganizationNotificationPreferences(
            organization_id: kotlin.String,
            body: AdminSchema.NotificationPreferencesUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchMyOrganizationNotificationPreferencesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.NotificationPreferences, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchMyOrganizationNotificationPreferencesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchMyOrganizationNotificationPreferences,
                    path = "/v1/me/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/notification-preferences",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.NotificationPreferencesUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.NotificationPreferences.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update the acting user's display preferences.
         *
         * Changes any of the six preferences the user the session token names reads
         * and renders by, and returns all six afterwards. Every field is optional and
         * independent: what is sent is changed, what is omitted is left alone, and
         * the reply is the whole set rather than the part that moved.
         *
         * They belong to the PERSON, not to an organization, so a change follows the
         * user into every organization they are a member of. They shape rendering
         * only: no operation returns different figures because of them, and a client
         * formats what it is given. A user changes their own preferences and nobody
         * else's, so the operation takes no user id: it writes those of the user the
         * session token names.
         *
         * Any role changes them, a viewer's included, since they change nothing the
         * organization holds.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, a member of the key's
         * organization in any role, whom the request acts as. A publishable key
         * without a session token minted for `studio-embedded` naming a user, a token
         * minted for another surface included, is refused with **401**.
         *
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchMyPreferences(
            body: AdminSchema.PreferencesUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchMyPreferencesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.Preferences, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchMyPreferencesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchMyPreferences,
                    path = "/v1/me/preferences",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.PreferencesUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.Preferences.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Accept an invitation.
         *
         * Accepts an invitation waiting for the user the session token names, which
         * makes them a member of the organization with the role it grants, and
         * answers the membership. The invitation is consumed: it leaves
         * `getMyInvitations` and `getOrganizationInvitations`, and the person is
         * listed by `getOrganizationMembers` from then on.
         * - Only the invited person accepts. An invitation is theirs when it was made
         *   to an address their account has verified, and is to the key's
         *   organization. Any other identifier is a 404: an invitation to an address
         *   the account has not verified, another person's, one already answered,
         *   withdrawn or lapsed, and one that names nothing all answer alike.
         * - It takes no seat. The invitation has held its seat since it was made, so
         *   accepting is never refused for the organization's seats.
         * - The session token the person used before joining needs no replacing: it
         *   acts as the member, in the role granted, from the next request.
         * - Accepting consumes the invitation, so the same request sent again is a
         *   404 and changes nothing, unless it carries the first request's
         *   `Idempotency-Key`, which answers it with the first one's 200.
         *
         * Requires a publishable key with the `studio-embedded` scope AND a session
         * token minted for `studio-embedded` naming a user, the invited person, whom
         * the request acts as. A publishable key without a session token minted for
         * `studio-embedded` naming a user, a token minted for another surface
         * included, is refused with **401**.
         *
         * @param invitation_id Identifier of the invitation, the `id` an `OrganizationInvitation` or a
         * `UserInvitation` carries. One that names no invitation this caller may see,
         * an expired one included, is answered as each operation says: a read or an
         * acceptance answers it `404`, and a withdrawal or a decline, which leave it
         * gone either way, answer `200`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postMyInvitationAccept(
            invitation_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostMyInvitationAcceptParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.Membership, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostMyInvitationAcceptParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postMyInvitationAccept,
                    path = "/v1/me/invitations/" + com.mountaya.client.encodePathSegment(invitation_id) + "/accept",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.Membership.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }

    /** Webhooks is the `admin.webhooks` operations of the REST API. */
    public class Webhooks internal constructor(
        private val sender: com.mountaya.client.OperationSender,
    ) {
        /**
         * Delete a webhook endpoint.
         *
         * Deletes an endpoint: nothing is delivered to it from then on, and its deliveries
         * still retrying end `canceled`. Its deliveries already made stay readable for the 13
         * months deliveries are kept, and `getOrganizationWebhookDeliveries` and
         * `getOrganizationWebhookHealth` still narrow to it by its identifier. It frees its
         * place under the organization's `webhook-endpoints` limit.
         * - Owner or admin only (403 otherwise).
         * - An endpoint already deleted is a `404`; a delete retried with the first one's
         *   `Idempotency-Key` is answered its first `200`.
         * - `If-Match` makes the deletion conditional: send the endpoint's `etag`, and an
         *   endpoint changed since is refused with `412`, deleting nothing.
         * - It is limited, together with the organization's other endpoint writes this
         *   document's introduction lists, to 30 a minute per user and then 300 a minute per
         *   organization; past either, the answer is a `429` carrying `Retry-After`, and
         *   nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token naming a
         * user, a member with the owner or admin role, whom the request acts as. A session
         * token minted without `user_id` is refused with **401**, and a publishable key
         * without a session token is not accepted here: a key written to be shipped in a
         * browser cannot be sufficient on its own to read or change an organization's
         * account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param webhook_endpoint_id Identifier of the webhook endpoint, the `id` a `WebhookEndpoint` carries.
         * One that names no endpoint of the organization, another organization's
         * included, is answered `404`, so an endpoint id cannot be probed here.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun deleteOrganizationWebhookEndpoint(
            organization_id: kotlin.String,
            webhook_endpoint_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.DeleteOrganizationWebhookEndpointParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.DeleteOrganizationWebhookEndpointParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.deleteOrganizationWebhookEndpoint,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-endpoints/" + com.mountaya.client.encodePathSegment(webhook_endpoint_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.json.JsonElement.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List an organization's webhook deliveries.
         *
         * Returns every delivery of the organization's webhook events over the 13 months
         * deliveries are kept: to its endpoints, and the notification emails its events
         * caused, each with where it stands and its last attempt. An email delivery names its
         * recipient and its notification, never what the email said.
         * - A session token minted for `api` without `user_id` is asked for no member role
         *   on this read; one minted for `studio-embedded` without `user_id` is the `401`
         *   a key alone gets. One that names a user is answered only while that user holds
         *   the owner or admin role in the organization, and is a 403 otherwise.
         * - The filters combine: `webhook_endpoint_id` one endpoint, `event_id` one event,
         *   `destination` endpoints or emails, `outcome` where a delivery stands,
         *   `failure_class` and `failure_kind` how its last attempt failed, `event_type` the
         *   type delivered, `trigger_id` the deliveries one resend, test or redelivery
         *   started, and `start_at` and `end_at` when the deliveries started: a delivery is
         *   listed by its first attempt, wherever its later ones fall.
         * - The listing is paged, 50 deliveries a page unless `limit` says otherwise, the most
         *   recently started first. Send the page's `next_cursor` back as `cursor` for the
         *   next one; the last page reports `next_cursor: null`.
         *
         * Like every read made with a session token naming a user, it is limited to 1200 a
         * minute per user the token names and then to 6000 a minute per organization, and one
         * made with a session token minted without `user_id` is held as the key alone is, to
         * 600 a minute per client IP address and then 6000 per organization; past either, the
         * answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token. A
         * publishable key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationWebhookDeliveries(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookDeliveriesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.WebhookDelivery>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetOrganizationWebhookDeliveriesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationWebhookDeliveries,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-deliveries",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("webhook_endpoint_id" to it) }
                            given.slot3?.let { add("event_id" to it) }
                            given.slot4?.let { add("destination" to it.value) }
                            given.slot5?.let { add("outcome" to it.value) }
                            given.slot6?.let { add("failure_class" to it.value) }
                            given.slot7?.let { add("failure_kind" to it.value) }
                            given.slot8?.let { add("event_type" to it.value) }
                            given.slot9?.let { add("trigger_id" to it) }
                            given.slot10?.let { add("start_at" to com.mountaya.client.formatTimestamp(it)) }
                            given.slot11?.let { add("end_at" to com.mountaya.client.formatTimestamp(it)) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.WebhookDelivery.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getOrganizationWebhookDeliveries` answers, one row at a time,
         * requesting a page only as its collector takes the rows before it
         * (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetOrganizationWebhookDeliveries(
            organization_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookDeliveriesParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.WebhookDelivery> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getOrganizationWebhookDeliveries(
                    organization_id = organization_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Retrieve a webhook delivery with its attempts.
         *
         * Returns one delivery with every attempt made of it: when each was due and made, how
         * it ended and whose a failure was, and for an endpoint the status it answered and the
         * first 2 KiB of its answer. A notification email's delivery names its recipient and
         * its notification, never what the email said.
         * - A session token minted for `api` without `user_id` is asked for no member role
         *   on this read; one minted for `studio-embedded` without `user_id` is the `401`
         *   a key alone gets. One that names a user is answered only while that user holds
         *   the owner or admin role in the organization, and is a 403 otherwise.
         * - A delivery is kept 13 months; one older is a `404`.
         *
         * Like every read made with a session token naming a user, it is limited to 1200 a
         * minute per user the token names and then to 6000 a minute per organization, and one
         * made with a session token minted without `user_id` is held as the key alone is, to
         * 600 a minute per client IP address and then 6000 per organization; past either, the
         * answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token. A
         * publishable key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param delivery_id Identifier of the delivery, the `id` a `WebhookDelivery` carries. One that
         * names no delivery of the organization within the 13 months deliveries are
         * kept is answered `404`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationWebhookDelivery(
            organization_id: kotlin.String,
            delivery_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookDeliveryParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.WebhookDeliveryWithAttempts, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationWebhookDeliveryParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationWebhookDelivery,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-deliveries/" + com.mountaya.client.encodePathSegment(delivery_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.WebhookDeliveryWithAttempts.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Retrieve a webhook endpoint.
         *
         * Returns one of the organization's webhook endpoints, as
         * `getOrganizationWebhookEndpoints` lists it. Its secret is never answered.
         * - A session token minted for `api` without `user_id` is asked for no member role
         *   on this read; one minted for `studio-embedded` without `user_id` is the `401`
         *   a key alone gets. One that names a user is answered only while that user holds
         *   the owner or admin role in the organization, and is a 403 otherwise.
         * - The answer's `ETag` is the endpoint's `etag`. Send it as `If-Match` on
         *   `patchOrganizationWebhookEndpoint` or `deleteOrganizationWebhookEndpoint` to make
         *   the change conditional on nobody having changed the endpoint since.
         *
         * Like every read made with a session token naming a user, it is limited to 1200 a
         * minute per user the token names and then to 6000 a minute per organization, and one
         * made with a session token minted without `user_id` is held as the key alone is, to
         * 600 a minute per client IP address and then 6000 per organization; past either, the
         * answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token. A
         * publishable key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param webhook_endpoint_id Identifier of the webhook endpoint, the `id` a `WebhookEndpoint` carries.
         * One that names no endpoint of the organization, another organization's
         * included, is answered `404`, so an endpoint id cannot be probed here.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationWebhookEndpoint(
            organization_id: kotlin.String,
            webhook_endpoint_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookEndpointParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.WebhookEndpoint, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationWebhookEndpointParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationWebhookEndpoint,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-endpoints/" + com.mountaya.client.encodePathSegment(webhook_endpoint_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.WebhookEndpoint.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List an organization's webhook endpoints.
         *
         * Returns the organization's webhook endpoints, enabled and disabled, each with the
         * event types it is sent, its status and when its run of failed deliveries began. A
         * secret is never answered: `postOrganizationWebhookEndpoint` answers it once.
         * - A session token minted for `api` without `user_id` is asked for no member role
         *   on this read; one minted for `studio-embedded` without `user_id` is the `401`
         *   a key alone gets. One that names a user is answered only while that user holds
         *   the owner or admin role in the organization, and is a 403 otherwise.
         * - The listing is paged, 50 endpoints a page unless `limit` says otherwise, oldest
         *   first. An organization holds 2 unless its agreement says otherwise, 6 at most, so
         *   one page holds them all. Send the page's `next_cursor` back as `cursor` for the
         *   next one; the last page reports `next_cursor: null`.
         *
         * Like every read made with a session token naming a user, it is limited to 1200 a
         * minute per user the token names and then to 6000 a minute per organization, and one
         * made with a session token minted without `user_id` is held as the key alone is, to
         * 600 a minute per client IP address and then 6000 per organization; past either, the
         * answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token. A
         * publishable key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationWebhookEndpoints(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookEndpointsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.WebhookEndpoint>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetOrganizationWebhookEndpointsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationWebhookEndpoints,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-endpoints",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.WebhookEndpoint.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getOrganizationWebhookEndpoints` answers, one row at a time,
         * requesting a page only as its collector takes the rows before it
         * (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetOrganizationWebhookEndpoints(
            organization_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookEndpointsParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.WebhookEndpoint> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getOrganizationWebhookEndpoints(
                    organization_id = organization_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Retrieve a webhook event with its deliveries.
         *
         * Returns one webhook event, the envelope its deliveries carried, with the deliveries
         * made of it: one per endpoint it was sent to, per notification email it caused, and
         * per resend or redelivery since, each with its last attempt.
         * - `delivery_count` is how many deliveries were made of the event, and `deliveries`
         *   carries them all up to 100. Past that it carries the 100 most recently started:
         *   `getOrganizationWebhookDeliveries` pages every one of them by `event_id`.
         * - A session token minted for `api` without `user_id` is asked for no member role
         *   on this read; one minted for `studio-embedded` without `user_id` is the `401`
         *   a key alone gets. One that names a user is answered only while that user holds
         *   the owner or admin role in the organization, and is a 403 otherwise.
         * - An event is kept 13 months; one older is a `404`.
         *
         * Like every read made with a session token naming a user, it is limited to 1200 a
         * minute per user the token names and then to 6000 a minute per organization, and one
         * made with a session token minted without `user_id` is held as the key alone is, to
         * 600 a minute per client IP address and then 6000 per organization; past either, the
         * answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token. A
         * publishable key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param event_id Identifier of the webhook event, the `id` a `WebhookEvent` carries and the
         * `Mountaya-Event-Id` header of each of its deliveries. One that names no
         * event of the organization within the 13 months events are kept is answered
         * `404`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationWebhookEvent(
            organization_id: kotlin.String,
            event_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookEventParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.WebhookEventWithDeliveries, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetOrganizationWebhookEventParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationWebhookEvent,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-events/" + com.mountaya.client.encodePathSegment(event_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.WebhookEventWithDeliveries.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List an organization's webhook events.
         *
         * Returns every public webhook event recorded in the organization over the 13 months
         * events are kept, whether or not an endpoint was sent it: what its members' acts and
         * Mountaya recorded, before any endpoint subscribes. Each is the envelope a delivery
         * carries, exactly. It is how a receiver that was down past the retries reconciles:
         * list from the last event it processed, and redeliver or read what it missed.
         * - A session token minted for `api` without `user_id` is asked for no member role
         *   on this read; one minted for `studio-embedded` without `user_id` is the `401`
         *   a key alone gets. One that names a user is answered only while that user holds
         *   the owner or admin role in the organization, and is a 403 otherwise.
         * - The filters combine. `type` keeps one type and `family` every type of one family;
         *   `related_object_id` the events about one object; `request_id` the events one API
         *   request recorded, by the `X-Request-Id` its answer carried; `actor_type` and
         *   `actor_user_id` who made the change; `delivery_outcome` the events with at least
         *   one delivery standing so; `start_at` and `end_at` when they were recorded.
         * - The listing is paged, 50 events a page unless `limit` says otherwise, the most
         *   recent first. Send the page's `next_cursor` back as `cursor` for the next one; the
         *   last page reports `next_cursor: null`.
         * - An event is listed within seconds of its change. `getOrganizationWebhookEvent`
         *   reads one with its deliveries.
         *
         * Like every read made with a session token naming a user, it is limited to 1200 a
         * minute per user the token names and then to 6000 a minute per organization, and one
         * made with a session token minted without `user_id` is held as the key alone is, to
         * 600 a minute per client IP address and then 6000 per organization; past either, the
         * answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token. A
         * publishable key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationWebhookEvents(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookEventsParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.WebhookEvent>, com.mountaya.client.generated.RestSchema.ListMetadata> {
            val given = AdminSchema.GetOrganizationWebhookEventsParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationWebhookEvents,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-events",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("cursor" to it) }
                            given.slot1?.let { add("limit" to it.toString()) }
                            given.slot2?.let { add("type" to it.value) }
                            given.slot3?.let { add("family" to it) }
                            given.slot4?.let { add("related_object_id" to it) }
                            given.slot5?.let { add("request_id" to it) }
                            given.slot6?.let { add("actor_type" to it.value) }
                            given.slot7?.let { add("actor_user_id" to it) }
                            given.slot8?.let { add("delivery_outcome" to it.value) }
                            given.slot9?.let { add("start_at" to com.mountaya.client.formatTimestamp(it)) }
                            given.slot10?.let { add("end_at" to com.mountaya.client.formatTimestamp(it)) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.WebhookEvent.serializer()), com.mountaya.client.generated.RestSchema.ListMetadata.serializer())
        }

        /**
         * Walks the listing `getOrganizationWebhookEvents` answers, one row at a time, requesting a
         * page only as its collector takes the rows before it (`transport.pagination.walk`).
         *
         * Every request sends what [parameters] sets, its filters and `sort` included, with its
         * `limit` replaced by [pageSize] and its `cursor` by the one the walk has reached, starting
         * from the one [parameters] names, so a walk resumes where an earlier one stopped. It
         * follows every page for as long as a `next_cursor` names one, an empty page included, and
         * stops at a null `next_cursor`, at [maxPages] requests, which raises nothing, or at a
         * cursor it has already sent, which raises the base `ApiException`.
         *
         * It throws `ConfigurationException` here, before any flow is collected, for a [pageSize]
         * outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops
         * early, requests no further page.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param pageSize How many rows each request asks for, from 1 to 100.
         * @param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.
         * @param options What every request of the walk overrides of its client's configuration.
         * @param parameters Sets the optional parameters every request sends, each by assigning it.
         */
        public fun walkGetOrganizationWebhookEvents(
            organization_id: kotlin.String,
            pageSize: kotlin.Int = 50,
            maxPages: kotlin.Int? = null,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookEventsParameters.() -> kotlin.Unit = {},
        ): kotlinx.coroutines.flow.Flow<AdminSchema.WebhookEvent> =
            com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->
                getOrganizationWebhookEvents(
                    organization_id = organization_id,
                    options = options,
                ) {
                    parameters()
                    paged()
                }
            }

        /**
         * Retrieve the reliability of an organization's webhooks.
         *
         * Returns how reliably the organization's webhook events were delivered over a window:
         * as totals, as a series of hours or days, and one line per destination. It keeps
         * Mountaya's service availability apart from each endpoint's success, since every
         * failed attempt is classed as the receiver's or as Mountaya's.
         * - A session token minted for `api` without `user_id` is asked for no member role
         *   on this read; one minted for `studio-embedded` without `user_id` is the `401`
         *   a key alone gets. One that names a user is answered only while that user holds
         *   the owner or admin role in the organization, and is a 403 otherwise.
         * - `window` asks for the last 24 hours or 7 days, an hour at a time, or the last 30
         *   days, a day at a time, ending at the hour the request is answered in. Omitted with
         *   no range, it is `24h`.
         * - `start_at` and `end_at` ask for a range instead, at most 92 days within the 13
         *   months deliveries are kept: an hour at a time up to 7 days, a day at a time
         *   beyond. `window` beside either is a `400`.
         * - `webhook_endpoint_id` narrows the report to one endpoint; omitted, it covers every
         *   endpoint and the notification emails, each a destination line of its own.
         * - `availability` is Mountaya's: the share of the attempts due that were made on
         *   schedule with no failure of Mountaya's. The success rates are the endpoints': how
         *   often a delivery succeeded at its first attempt, and at any.
         * - The figures are a series over the window, not a listing, so they are not paged.
         *
         * Like every read made with a session token naming a user, it is limited to 1200 a
         * minute per user the token names and then to 6000 a minute per organization, and one
         * made with a session token minted without `user_id` is held as the key alone is, to
         * 600 a minute per client IP address and then 6000 per organization; past either, the
         * answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token. A
         * publishable key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getOrganizationWebhookHealth(
            organization_id: kotlin.String,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetOrganizationWebhookHealthParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.WebhookHealth, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.GetOrganizationWebhookHealthParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getOrganizationWebhookHealth,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-health",
                    query =
                        kotlin.collections.buildList {
                            given.slot0?.let { add("webhook_endpoint_id" to it) }
                            given.slot1?.let { add("window" to it.value) }
                            given.slot2?.let { add("start_at" to com.mountaya.client.formatTimestamp(it)) }
                            given.slot3?.let { add("end_at" to com.mountaya.client.formatTimestamp(it)) }
                        },
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, AdminSchema.WebhookHealth.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * List the public webhook event types.
         *
         * Returns every public webhook event type with its family and what records it: the
         * members of `WebhookEventType`, which an endpoint's `enabled_events` names. The
         * catalog is the same for every organization, and this document's `webhooks` describes
         * each type's payload.
         * - A session token minted for `api` without `user_id` is asked for no member role
         *   on this read; one minted for `studio-embedded` without `user_id` is the `401`
         *   a key alone gets. One that names a user is answered only while that user holds
         *   the owner or admin role in the key's organization, and is a 403 otherwise.
         * - The catalog is a set of a size Mountaya decides, so it is not paged.
         *
         * Like every read made with a session token naming a user, it is limited to 1200 a
         * minute per user the token names and then to 6000 a minute per organization, and one
         * made with a session token minted without `user_id` is held as the key alone is, to
         * 600 a minute per client IP address and then 6000 per organization; past either, the
         * answer is a `429` carrying `Retry-After`.
         *
         * Requires a publishable key with the `management` scope AND a session token. A
         * publishable key without a session token is not accepted here: a key written to be
         * shipped in a browser cannot be sufficient on its own to read or change an
         * organization's account.
         *
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun getWebhookEventTypes(
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.GetWebhookEventTypesParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.WebhookEventCatalogEntry>, kotlinx.serialization.json.JsonObject?> {
            AdminSchema.GetWebhookEventTypesParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.getWebhookEventTypes,
                    path = "/v1/webhook-event-types",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Empty,
                    options = options,
                )
            return sender.send(call, kotlinx.serialization.builtins.ListSerializer(AdminSchema.WebhookEventCatalogEntry.serializer()), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Update a webhook endpoint.
         *
         * Changes an endpoint's URL, description, event types or status. Each field is
         * optional and an omitted one keeps its value; `enabled_events` is replaced whole. An
         * empty object changes nothing and answers the endpoint as it stands.
         * - Owner or admin only (403 otherwise).
         * - `url` and `enabled_events` are held to the rules of a new endpoint's, and refused
         *   the same way.
         * - `status` `disabled` stops every delivery to the endpoint, the ones still retrying
         *   included, which end `canceled`. `enabled` starts them again from the next event
         *   recorded, and clears `failing_since`; the events recorded while it was disabled
         *   are not sent on their own, and `postOrganizationWebhookDelivery` redelivers those
         *   of the last 30 days.
         * - The secret is not changed here.
         * - `If-Match` makes the change conditional: send the endpoint's `etag`, and an
         *   endpoint changed since is refused with `412`, changing nothing.
         * - It is limited, together with the organization's other endpoint writes this
         *   document's introduction lists, to 30 a minute per user and then 300 a minute per
         *   organization; past either, the answer is a `429` carrying `Retry-After`, and
         *   nothing is done.
         *
         * Requires a publishable key with the `management` scope AND a session token naming a
         * user, a member with the owner or admin role, whom the request acts as. A session
         * token minted without `user_id` is refused with **401**, and a publishable key
         * without a session token is not accepted here: a key written to be shipped in a
         * browser cannot be sufficient on its own to read or change an organization's
         * account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param webhook_endpoint_id Identifier of the webhook endpoint, the `id` a `WebhookEndpoint` carries.
         * One that names no endpoint of the organization, another organization's
         * included, is answered `404`, so an endpoint id cannot be probed here.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun patchOrganizationWebhookEndpoint(
            organization_id: kotlin.String,
            webhook_endpoint_id: kotlin.String,
            body: AdminSchema.WebhookEndpointUpdateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PatchOrganizationWebhookEndpointParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.WebhookEndpoint, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PatchOrganizationWebhookEndpointParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.patchOrganizationWebhookEndpoint,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-endpoints/" + com.mountaya.client.encodePathSegment(webhook_endpoint_id),
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.WebhookEndpointUpdateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    ifMatch = given.ifMatchSlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.WebhookEndpoint.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Resend an event, send a test event, or redeliver an endpoint's events.
         *
         * Starts deliveries to one of the organization's endpoints, which run after the
         * answer. The body names the endpoint and exactly one of three things:
         * - `event_id`, a resend: one event recorded in the last 30 days, delivered again on
         *   the delivery schedule, whether or not the endpoint was sent it the first time.
         * - `test`, a test: a test event of the type named, its payload this contract's
         *   example for the type with an `id` of its own, delivered once without retries. It
         *   is recorded nowhere else and listed by no `getOrganizationWebhookEvents`.
         * - `since`, a redelivery: every event recorded from that instant on, at most 30 days
         *   ago, whose type the endpoint is sent, delivered again oldest first at a paced
         *   rate. Use it after an outage outlasted the retries, or after enabling an endpoint
         *   again, from the `failing_since` the endpoint carried before it was enabled. One
         *   redelivery runs at a time for an endpoint.
         *
         * Each delivery it starts carries the answer's `id` as `trigger_id`, and is read as
         * any other.
         * - Owner or admin only (403 otherwise).
         * - A disabled endpoint is a `409`: enable it first. A redelivery asked while another
         *   to the same endpoint still runs is a `409` coded `REDELIVERY_IN_PROGRESS`, naming
         *   the one running. An event older than 30 days, or a `since` further back or still to
         *   come, is a `400` naming it.
         * - It is limited to 30 a minute per user and then 300 a minute per organization, and
         *   a redelivery to 10 an hour per user and 30 an hour per organization besides; past
         *   any of them, the answer is a `429` carrying `Retry-After`.
         * - A request sent again with its `Idempotency-Key` starts nothing twice.
         *
         * Requires a publishable key with the `management` scope AND a session token naming a
         * user, a member with the owner or admin role, whom the request acts as. A session
         * token minted without `user_id` is refused with **401**, and a publishable key
         * without a session token is not accepted here: a key written to be shipped in a
         * browser cannot be sufficient on its own to read or change an organization's
         * account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationWebhookDelivery(
            organization_id: kotlin.String,
            body: AdminSchema.WebhookDeliveryCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationWebhookDeliveryParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.WebhookDeliveryTrigger, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationWebhookDeliveryParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationWebhookDelivery,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-deliveries",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.WebhookDeliveryCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.WebhookDeliveryTrigger.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }

        /**
         * Create a webhook endpoint.
         *
         * Creates an endpoint Mountaya delivers the organization's webhook events to: every
         * event of a type its `enabled_events` names, from the next one recorded on, signed
         * with a secret of its own, answered here once. A create sent twice without an
         * `Idempotency-Key` makes two endpoints; with one, the second is not answered the
         * first one's endpoint, since that answer carried the secret and is never kept: it is
         * the `409` coded `IDEMPOTENCY_ANSWER_UNAVAILABLE`. A create sent again with the key,
         * after the first failed with a `5xx` once the endpoint was made, is that `409` too,
         * whose `extensions.metadata.location` names the endpoint made,
         * `/v1/organizations/{organization_id}/webhook-endpoints/{webhook_endpoint_id}`. This
         * API answers a secret once: an owner or an admin reads that endpoint's again in
         * Mountaya's web application, or deletes it and creates another.
         * - Owner or admin only (403 otherwise).
         * - `url` is `https`, written in printable ASCII, at most 2048 characters, with no
         *   credentials before its host and no fragment, its host, port included, at most 259
         *   characters; and that host resolves to a public address: a private, loopback or
         *   link-local one is a `400` naming `url`, and so is one Mountaya cannot resolve. A
         *   redirect is never followed.
         * - `enabled_events` names types, families followed by `.*`, or `*`. A type or a
         *   family the contract does not publish is a `400` naming its position;
         *   `getWebhookEventTypes` lists them.
         * - An organization holds 2 endpoints, enabled and disabled, unless its agreement
         *   says otherwise, 6 at most: one more is refused at `webhook-endpoints`, a `429`
         *   coded `LIMIT_REACHED` with no `Retry-After`. Delete one first, or agree a higher
         *   limit. An organization whose limit was lowered below what it holds keeps its
         *   endpoints, and creates none until it holds fewer than the limit.
         * - It is limited, together with the organization's other endpoint writes this
         *   document's introduction lists, to 30 a minute per user and then 300 a minute per
         *   organization; past either, the answer is a `429` carrying `Retry-After`, and
         *   nothing is done.
         * - Mountaya delivers each event at least once, in no promised order, retrying a
         *   failed attempt for about 72 hours. This document's introduction says how a
         *   delivery is signed, retried and recorded.
         *
         * Requires a publishable key with the `management` scope AND a session token naming a
         * user, a member with the owner or admin role, whom the request acts as. A session
         * token minted without `user_id` is refused with **401**, and a publishable key
         * without a session token is not accepted here: a key written to be shipped in a
         * browser cannot be sufficient on its own to read or change an organization's
         * account.
         *
         * @param organization_id Identifier of the organization the operation acts on. A publishable key
         * reaches only its own organization: any other identifier is a 403, whether or
         * not it exists, so the parameter cannot be used to probe which organization ids
         * exist.
         * @param body The request body, sent as `application/json`.
         * @param options What this call overrides of its client's configuration.
         * @param parameters Sets the optional parameters, each by assigning it.
         */
        public suspend fun postOrganizationWebhookEndpoint(
            organization_id: kotlin.String,
            body: AdminSchema.WebhookEndpointCreateParams,
            options: com.mountaya.client.RequestOptions = com.mountaya.client.RequestOptions(),
            parameters: AdminSchema.PostOrganizationWebhookEndpointParameters.() -> kotlin.Unit = {},
        ): com.mountaya.client.Response<AdminSchema.WebhookEndpointWithSecret, kotlinx.serialization.json.JsonObject?> {
            val given = AdminSchema.PostOrganizationWebhookEndpointParameters().apply(parameters)
            val call =
                com.mountaya.client.OperationCall(
                    facts = OperationTable.Rest.postOrganizationWebhookEndpoint,
                    path = "/v1/organizations/" + com.mountaya.client.encodePathSegment(organization_id) + "/webhook-endpoints",
                    query = kotlin.collections.emptyList(),
                    headers = kotlin.collections.emptyList(),
                    body = com.mountaya.client.RequestBody.Json(com.mountaya.client.wireJson.encodeToString(AdminSchema.WebhookEndpointCreateParams.serializer(), body)),
                    idempotencyKey = given.idempotencyKeySlot,
                    options = options,
                )
            return sender.send(call, AdminSchema.WebhookEndpointWithSecret.serializer(), com.mountaya.client.nullableSerializer(kotlinx.serialization.json.JsonObject.serializer()))
        }
    }
}
