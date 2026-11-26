package com.mountaya.client

import com.mountaya.client.generated.OperationTable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * OperationFacts is what the contract declares about one operation and the transport acts on:
 * where it is sent, whether a repeat of it has a second effect, which credentials it takes, and
 * what it answers.
 *
 * Every printed operation carries its own, printed from `sdk/model.json`, and a test holds the
 * printed table to the model fact by fact. Nothing here is inferred from a method.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, prints its own from beside the core, and
 * no consumer reads it (SPEC.md section 7).
 */
@InternalMountayaApi
public class OperationFacts(
    /** The surface the operation is sent to. */
    public val surface: Surface,
    /** The `operationId`, or the field of a Data API operation. */
    public val operationId: String,
    /** The HTTP method, uppercase. */
    public val method: String,
    /** The path template, `{name}` marking each path parameter, or the Data API's endpoint. */
    public val path: String,
    /** Whether a repeat has no second effect: `x-idempotent` on REST, and every Data API operation. */
    public val idempotent: Boolean,
    /**
     * What the operation declares about credentials. For the Data API it is a read, which every Data API
     * query is over its `POST` (`sdk/SPEC.md` section 1), requiring a session token.
     */
    public val credentials: CredentialDeclaration,
    /**
     * The most the API spends on the operation before it answers a 504 itself, its `x-timeout-ms`,
     * in milliseconds, from which an attempt's budget is derived. It is null for an event stream,
     * which declares none.
     */
    public val timeoutMs: Int?,
    /** Whether the operation takes an `Idempotency-Key`, which every call to it sends. */
    public val declaresIdempotencyKey: Boolean,
    /** Whether the operation takes an `If-Match`, which a call sends only where its caller names a tag. */
    public val isConditional: Boolean,
    /** Whether the operation answers a conditional read from the caller's copy with a 304. */
    public val answersNotModified: Boolean,
    /** How the operation pages, or null where it does not page by cursor. */
    public val pagination: Pagination?,
    /** The shape its success arrives in. */
    public val success: SuccessKind,
    /**
     * Every status its success arrives with, each with the headers it declares, in the contract's
     * order, and none for the Data API.
     */
    public val successResponses: List<SuccessResponse>,
    /** Every failing status it declares, in ascending order. */
    public val errorStatuses: List<Int>,
    /**
     * The tasks a write runs, each the name the contract's `x-task` gives it, in the contract's order,
     * and none for a read and for the Data API. The transport reads none of them: they are the names
     * the webhook events a write records carry as their `task`.
     */
    public val tasks: List<String> = emptyList(),
    /**
     * [timeoutMs] as a duration, or null for an event stream: the operation's own budget, which a
     * caller's declaration states at any precision.
     */
    public val timeout: Duration? = timeoutMs?.milliseconds,
) {
    /**
     * Creates the facts a caller's [declaration] states, for an operation this build prints no
     * method for: its path the caller's, already filled in, its success [success], and its
     * statuses unknown.
     */
    internal constructor(
        declaration: OperationDeclaration,
        path: String,
        success: SuccessKind = SuccessKind.Envelope,
    ) : this(
        surface = declaration.surface,
        operationId = declaration.operationId,
        method = declaration.method,
        path = path,
        idempotent = declaration.idempotent,
        credentials = declaration.credentials,
        timeoutMs = declaration.timeout?.inWholeMilliseconds?.toInt(),
        declaresIdempotencyKey = declaration.declaresIdempotencyKey,
        isConditional = declaration.isConditional,
        answersNotModified = false,
        pagination = null,
        success = success,
        successResponses = emptyList(),
        errorStatuses = emptyList(),
        tasks = emptyList(),
        timeout = declaration.timeout,
    )

    /**
     * Creates the facts of a Data API [operation] a caller declares, for one this build prints no method
     * for: sent to the Data API's endpoint, idempotent, a read requiring a session token, answered 200 and
     * waiting the budget it states, as the contract states every Data API operation is.
     */
    internal constructor(operation: DataOperation) : this(
        surface = Surface.Data,
        operationId = operation.name,
        method = "POST",
        path = OperationTable.dataEndpoint,
        idempotent = true,
        credentials = CredentialDeclaration(requiresSession = true, read = true),
        timeoutMs = operation.timeout?.inWholeMilliseconds?.toInt(),
        declaresIdempotencyKey = false,
        isConditional = false,
        answersNotModified = false,
        pagination = null,
        success = SuccessKind.Graphql,
        successResponses = listOf(SuccessResponse(200, emptyList())),
        errorStatuses = emptyList(),
        tasks = emptyList(),
        timeout = operation.timeout,
    )

    override fun toString(): String = "OperationFacts($surface $operationId: $method $path)"
}
