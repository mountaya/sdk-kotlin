package com.mountaya.admin.generated

import com.mountaya.client.ConfigurationException
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.RequestBody
import com.mountaya.client.RequestOptions
import com.mountaya.client.RetryPolicy
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * What a printed method of the admin unit hands the core's transport: the path, the query and the body, settled from
 * its signature, and the facts the admin unit's own table prints.
 */
@OptIn(InternalMountayaApi::class)
class GeneratedCallsTest {
    /**
     * The one operation a secret key authenticates is the admin unit's, and its body a component only it reaches,
     * which names a component of the core, `SessionSurface`, as the core names it.
     */
    @Test
    fun aSessionsMintIsWrittenFromItsBodyAndHandedOverWithTheAdminUnitsFacts() =
        runTest {
            val session = """{"expires_at":"2026-11-23T10:05:00Z","user_id":null,"token":"sess_test_4f9a"}"""
            val sender = ScriptedSender(data = Json.parseToJsonElement(session))

            val minted =
                Admin(sender).sessions.postSession(
                    body =
                        AdminSchema.SessionCreateParams(publishable_key = "pk_test_0a1b") {
                            surface = RestSchema.SessionSurface.StudioEmbedded
                        },
                )

            val call = sender.calls.single()
            assertSame(OperationTable.Rest.postSession, call.facts)
            assertEquals("/v1/sessions", call.path)
            assertEquals(emptyList(), call.query)
            assertEquals(
                RequestBody.Json(Json.parseToJsonElement("""{"publishable_key":"pk_test_0a1b","surface":"studio-embedded"}""")),
                call.body,
            )
            assertTrue(call.facts.credentials.requiresSecretKey)
            val token: RestSchema.Session = minted.data
            assertEquals("sess_test_4f9a", token.token)
        }

    /** A page of members whose ids are [ids], naming [next] as the page after it. */
    private fun membersPage(
        ids: List<String>,
        next: String?,
    ) = Json.parseToJsonElement(
        ids.joinToString(",", "[", "]") { """{"user_id":"$it","role":"viewer","joined_at":"2026-01-01T00:00:00Z"}""" },
    ) to Json.parseToJsonElement("""{"pagination":{"limit":2,"next_cursor":${next?.let { "\"$it\"" } ?: "null"}}}""")

    @Test
    fun aWalkReadsEveryPageThroughItsMethodTheCallersFiltersBesideTheWalksCursorAndLimit() =
        runTest {
            val sender = ScriptedSender(pages = listOf(membersPage(listOf("u1", "u2"), "c2"), membersPage(listOf("u3"), null)))
            val options = RequestOptions(retry = RetryPolicy(maxRetries = 0))

            val members =
                Admin(sender)
                    .members
                    .walkGetOrganizationMembers("org/1", pageSize = 2, options = options) {
                        role = RestSchema.OrganizationRole.Viewer
                    }.toList()

            assertEquals(listOf("u1", "u2", "u3"), members.map { it.user_id })
            assertEquals(List(2) { "/v1/organizations/org%2F1/members" }, sender.calls.map { it.path })
            assertEquals(
                listOf(
                    listOf("limit" to "2", "role" to "viewer"),
                    listOf("cursor" to "c2", "limit" to "2", "role" to "viewer"),
                ),
                sender.calls.map { it.query },
            )
            assertTrue(sender.calls.all { it.options == options && it.facts === OperationTable.Rest.getOrganizationMembers })
        }

    @Test
    fun aWalkAsksForTheOperationsOwnPageSizeResumesFromTheCallersCursorAndStopsAtItsCeiling() =
        runTest {
            val sender = ScriptedSender(pages = List(3) { membersPage(listOf("u$it"), "c${it + 1}") })

            val members =
                Admin(sender)
                    .members
                    .walkGetOrganizationMembers("org", maxPages = 2) { cursor = "c0" }
                    .toList()

            assertEquals(listOf("u0", "u1"), members.map { it.user_id })
            assertEquals(
                listOf(listOf("cursor" to "c0", "limit" to "50"), listOf("cursor" to "c1", "limit" to "50")),
                sender.calls.map { it.query },
            )
            assertFailsWith<ConfigurationException> { Admin(sender).members.walkGetOrganizationMembers("org", pageSize = 101) }
            assertEquals(2, sender.calls.size, "a refused walk sends nothing")
        }
}
