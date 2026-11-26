package com.mountaya.admin

import com.mountaya.admin.generated.Admin
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.Mountaya

/**
 * admin is the REST API's `admin` operations of this client, `mountaya.admin.<resource>.<operationId>(…)`: its
 * billing, its organizations, its webhook endpoints, the acting user's memberships and preferences, its keys,
 * invitations and members, and minting a session token, `mountaya.admin.sessions.postSession`, which a secret key
 * alone authenticates.
 *
 * They are the contract's admin unit, which this module declares beside the core, `mountaya-client`, so an app that
 * draws a map, edits an itinerary or queries the Data API links none of them: importing it, `com.mountaya.admin.admin`,
 * is the whole of what a call into it asks. Each call is sent through the client's own transport, under its
 * credential, its retries and its error classes, as a call of the core is. The accessor holds nothing of its own,
 * so reading it again is reading the same operations.
 */
@OptIn(InternalMountayaApi::class)
public val Mountaya.admin: Admin
    get() = Admin(sender)
