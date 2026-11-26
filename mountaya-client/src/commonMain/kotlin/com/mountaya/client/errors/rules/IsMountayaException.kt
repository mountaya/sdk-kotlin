package com.mountaya.client

/** The contract's name of every class an answer of the API raises, the base `ApiError` and `DataError` included. */
private val apiNames: Set<String> =
    setOf(
        "ApiError",
        "BadRequestError",
        "AuthenticationError",
        "BillingError",
        "ForbiddenError",
        "NotFoundError",
        "MethodNotAllowedError",
        "ConflictError",
        "IdempotencyKeyInUseError",
        "IdempotentAnswerUnavailableError",
        "PreconditionFailedError",
        "PayloadTooLargeError",
        "RateLimitError",
        "ServerError",
        "DataError",
    )

/**
 * The contract's name of every class of the family: every class the API's answers raise, and the
 * ones raised where their rule is proven, a webhook delivery's verification among them.
 */
private val familyNames: Set<String> =
    apiNames + setOf("ConnectionError", "ConfigurationError", "SelectionError", "WebhookSignatureError")

/**
 * isMountayaException says whether [value] is an error of the family, read by the name its class declares as data
 * rather than by its class (`transport.errors.structural`): a value that declares no name of the
 * family is none of its errors, whatever status it carries, and a [NamedException] of a class the
 * client did not build that declares one is, as one of the client's own is. A release build renames
 * classes, and the declared name survives it.
 */
public fun isMountayaException(value: Throwable?): Boolean = (value as? NamedException)?.name in familyNames

/**
 * isApiException says whether [value] is an error the API answered, a Data API answer carrying errors
 * under a success included, read by the name its class declares as data, whatever class declares it. A
 * `WebhookSignatureException` is of the family and never one of these.
 */
public fun isApiException(value: Throwable?): Boolean = (value as? NamedException)?.name in apiNames
