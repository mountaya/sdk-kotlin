package com.mountaya.elements

/**
 * The HTTP statuses that refuse access, the `refusalStatuses` `map/events-access-refused.json` declares: 401
 * for a credential the API does not accept, 402 for an entitlement the agreement does not include, 403 for a
 * resource the credential may not read.
 */
internal val accessRefusalStatuses: List<Int> = listOf(401, 402, 403)

/**
 * accessRefusalStatus is the [status] a failure of one of the map's own requests carries where it refuses
 * access, and null where it is a fault (`map.events.access-refused`): any other status, a missing tile or a
 * server error, and none at all, a request that never reached the API, are told as failures, which a map
 * recovers from.
 */
internal fun accessRefusalStatus(status: Int?): Int? = status?.takeIf { it in accessRefusalStatuses }

/**
 * isCredentialRefusal is whether a failure of one of the map's own requests carrying [status] refuses the credential
 * that request carried, a 401 or a 403, where a 402 refuses the organization's entitlement whatever credential asked
 * (`map.events.access-refused`): such a refusal of a request that left under a credential the client no longer sends is
 * told to nobody, neither as a refusal nor as a failure, and starts no standing (`map.events.access-standing`).
 */
internal fun isCredentialRefusal(status: Int?): Boolean = status == 401 || status == 403
