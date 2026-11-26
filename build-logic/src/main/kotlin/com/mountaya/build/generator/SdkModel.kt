package com.mountaya.build.generator

/**
 * SdkModel is `sdk/model.json`, read: the contract's shapes restated once for every SDK.
 *
 * Only what the printer reads is kept. A key the model carries and nothing here names is
 * ignored, as the model's own reading rules say; a `kind` nothing here knows is refused when
 * the document is read, never skipped.
 */
class SdkModel(
    /** The contract tag the model was built from, such as `v0.7.0`. */
    val contract: String,
    /** The REST API. */
    val rest: OpenApiSurface,
    /** The Tile API. */
    val tiles: OpenApiSurface,
    /** The Data API. */
    val data: DataSurface,
) {
    /** The format version the printer reads. */
    companion object {
        /** The one format version this printer was written for. */
        const val VERSION = 2
    }
}

/**
 * ClientUnit is the unit of the client a REST operation, a webhook event or a component belongs to, as the model states
 * it (`sdk/SPEC.md`, "The three layers, in every language"): the core, which every application links, or the admin
 * unit, which only a server that manages an organization, mints session tokens or receives webhooks does. Each is
 * printed into a module of its own, the core into `mountaya-client` and the admin unit into `mountaya-admin`.
 */
enum class ClientUnit(
    /** The model's own spelling. */
    val wire: String,
) {
    /** The core, which every application links. */
    Core("core"),

    /**
     * The admin unit, which only a server that manages an organization, mints session tokens or receives webhooks
     * links.
     */
    Admin("admin"),
    ;

    /** The units by the model's spelling. */
    companion object {
        /** Returns the unit the model spells [wire], or null where it spells none this printer knows. */
        fun of(wire: String): ClientUnit? = entries.firstOrNull { it.wire == wire }
    }
}

/** OpenApiSurface is one OpenAPI surface: the REST API or the Tile API. */
class OpenApiSurface(
    /** The origin every operation's path is sent to. */
    val origin: String,
    /** The operations a printed method is bound to, in the model's order. */
    val operations: List<Operation>,
    /** The routes a map renderer fetches on its own, marked `x-sdk-ignore`, which no printed method is bound to. */
    val templates: List<Operation>,
    /** The components, in the model's order. */
    val schemas: List<Schema>,
    /** The webhook events the surface delivers, in the model's order, none where it delivers none. */
    val webhooks: List<Webhook> = emptyList(),
)

/** Webhook is one public webhook event type, and what it is delivered as. */
class Webhook(
    /** The event's type, as the wire spells it, such as `organization.member.updated`. */
    val type: String,
    /** The unit that receives it. */
    val unit: ClientUnit,
    /** The component the event is delivered in. */
    val envelope: String,
    /** The component the type carries at `data.object`. */
    val objectName: String,
    /** The fields of that component the type carries the former value of, in the model's order. */
    val previousAttributes: List<String>,
    /** The event's one-line summary, or null. */
    val summary: String?,
    /** The event's description, or null. */
    val description: String?,
)

/** TypeNode is a type node: one shape, and whether it may be null. */
sealed class TypeNode {
    /** Whether the value may be null, however the bundle says so. */
    abstract val nullable: Boolean
}

/** ScalarType is a string, an integer, a number or a boolean, with what narrows it. */
class ScalarType(
    /** `string`, `integer`, `number` or `boolean`. */
    val kind: String,
    /** The format the bundle spells, such as `date-time`, or null. */
    val format: String?,
    /** The one value it may hold, where the bundle fixes it. */
    val constant: Any?,
    /** The name of an inline closed set, and its values. */
    val enumName: String?,
    /** The values of the inline closed set [enumName] names, or null where it is none. */
    val enumValues: List<Any>?,
    override val nullable: Boolean,
    /**
     * The name the model gives each of [enumValues], in their order, its `x-enum-varnames` entry, or null where it
     * gives none, the inline set's members then printed under their wire values.
     */
    val enumNames: List<String>? = null,
) : TypeNode()

/** ArrayType is a list of one shape. */
class ArrayType(
    /** The shape of each item. */
    val items: TypeNode,
    override val nullable: Boolean,
) : TypeNode()

/** MapType is an object keyed by text, each value of one shape. */
class MapType(
    /** The shape of each value. */
    val values: TypeNode,
    /** The shape that constrains each key, or null where any text is a key. */
    val keys: TypeNode?,
    override val nullable: Boolean,
) : TypeNode()

/** InlineObject is an inline object, named by the model from where it sits. */
class InlineObject(
    /** The name the model gives it from where it sits. */
    val name: String,
    /** Its properties, in the order it declares them. */
    val properties: List<Property>,
    /** The shape of every key it does not declare, or null where it takes none. */
    val additional: TypeNode?,
    /** Whether the object refuses every key it does not declare, as every object a request body reaches does. */
    val closed: Boolean,
    override val nullable: Boolean,
) : TypeNode()

/** InlineUnion is an inline `oneOf` with no discriminator, named by the model from where it sits. */
class InlineUnion(
    /** The name the model gives it from where it sits. */
    val name: String,
    /** Its members, in the bundle's order. */
    val members: List<TypeNode>,
    override val nullable: Boolean,
) : TypeNode()

/** RefType is a component of the same surface, by name. */
class RefType(
    /** The component's name. */
    val name: String,
    override val nullable: Boolean,
) : TypeNode()

/** AnyType stands for no declared type: any JSON value. */
class AnyType(
    override val nullable: Boolean,
) : TypeNode()

/** NullType is a value that is always null, which a success with no payload declares at `data`. */
class NullType(
    override val nullable: Boolean,
) : TypeNode()

/** Property is a property of an object, in the order the object declares it, parents first. */
class Property(
    /** The name the wire carries it under. */
    val name: String,
    /** Whether the object always carries it. */
    val required: Boolean,
    /** Its shape. */
    val type: TypeNode,
    /** Its description, or null. */
    val description: String?,
    /** The server's default, where the bundle states one, or null. */
    val default: Any?,
    /** Whether the bundle states a default, which [default] holds, null included. */
    val hasDefault: Boolean,
    /** The value this property carries on the one union member it belongs to, or null. */
    val discriminatorValue: String?,
    /** Whether the bundle marks it deprecated. */
    val deprecated: Boolean,
    /** The parent component a composed component inherits it from, which `extends` names, or null. */
    val from: String? = null,
    /** Whether the server writes it and a request never sends it. */
    val readOnly: Boolean = false,
    /** Whether a request sends it and the server never answers it. */
    val writeOnly: Boolean = false,
    /**
     * Whether the model marks it a secret, a credential such as a session token or a signing secret,
     * which a printed type never prints.
     */
    val secret: Boolean = false,
)

/** Schema is a component of an OpenAPI surface. */
sealed class Schema {
    /** The component's name. */
    abstract val name: String

    /** Whether it is sent in a `request`, received in a `response`, or both. */
    abstract val usage: Set<String>

    /** Its description, or null. */
    abstract val description: String?

    /** `envelope` for a part of the response envelope every operation shares, or null. */
    abstract val role: String?

    /** The unit it belongs to: the admin unit's where only the admin unit reaches it, the core's otherwise. */
    abstract val unit: ClientUnit
}

/** ObjectSchema is an object component, a composed one flattened parents first. */
class ObjectSchema(
    override val name: String,
    override val usage: Set<String>,
    override val description: String?,
    override val role: String?,
    override val unit: ClientUnit = ClientUnit.Core,
    /** Its properties, in the order it declares them, parents first. */
    val properties: List<Property>,
    /** Whether the component refuses every key it does not declare. It decides nothing printed. */
    val closed: Boolean = false,
    /** The parents a composed component is flattened from, parents first. It decides nothing printed. */
    val extends: List<String> = emptyList(),
) : Schema()

/** EnumSchema is a closed set of values. */
class EnumSchema(
    override val name: String,
    override val usage: Set<String>,
    override val description: String?,
    override val role: String?,
    override val unit: ClientUnit = ClientUnit.Core,
    /** `string` or `integer`, the type the wire carries a member as. */
    val valueType: String,
    /** The format the bundle spells, or null. */
    val format: String?,
    /** Its members, in the bundle's order. */
    val members: List<EnumMember>,
) : Schema()

/** EnumMember is one member of an [EnumSchema]. */
class EnumMember(
    /** The wire's value: a string, or an integer. */
    val value: Any,
    /** The name `x-enum-varnames` gives it. */
    val name: String,
    /** Its description, or null. */
    val description: String?,
    /** Whether the bundle marks it deprecated. */
    val deprecated: Boolean,
    /** The values whose reach a key carrying this one has, `x-pkey-scope-includes`. */
    val includes: List<String> = emptyList(),
    /** The values a key carrying this one carries beside it, `x-pkey-scope-needs`. */
    val needs: List<String> = emptyList(),
)

/** UnionSchema is a union discriminated by one property. */
class UnionSchema(
    override val name: String,
    override val usage: Set<String>,
    override val description: String?,
    override val role: String?,
    override val unit: ClientUnit = ClientUnit.Core,
    /** The property whose value names the member. */
    val discriminator: String,
    /** Its members, each by the value that names it. */
    val members: List<UnionMember>,
) : Schema()

/** UnionMember is one member of a [UnionSchema]. */
class UnionMember(
    /** The discriminator's value that names it. */
    val value: String,
    /** The component it is. */
    val type: String,
)

/** AliasSchema is another type under this component's name. */
class AliasSchema(
    override val name: String,
    override val usage: Set<String>,
    override val description: String?,
    override val role: String?,
    override val unit: ClientUnit = ClientUnit.Core,
    /** The type it names. */
    val type: TypeNode,
) : Schema()

/** GeoJsonSchema is one of RFC 7946's shapes, which an SDK declares once by hand. */
class GeoJsonSchema(
    override val name: String,
    override val usage: Set<String>,
    override val description: String?,
    override val role: String?,
    override val unit: ClientUnit = ClientUnit.Core,
    /** RFC 7946's name for the shape: `Point`, ..., `GeometryCollection`, or `Geometry` for any. */
    val geometry: String,
) : Schema()

/** Operation is one operation of an OpenAPI surface. */
class Operation(
    /** The method name it is printed as, verbatim. */
    val operationId: String,
    /** The HTTP method, as the bundle spells it. */
    val method: String,
    /** The path, its parameters in braces. */
    val path: String,
    /** `<group>.<resource>`, or null on the Tile API, which is un-namespaced. */
    val namespace: String?,
    /** The unit it belongs to, which the model states by its namespace: every one of the `admin` group is the admin unit's. */
    val unit: ClientUnit,
    /** Whether repeating it is safe, so a retry may resend it. */
    val idempotent: Boolean,
    /** The most the API spends on it before it answers a 504, `x-timeout-ms`, or null for an event stream. */
    val timeoutMs: Int?,
    /** Whether it takes an `Idempotency-Key`. */
    val idempotencyKey: Boolean,
    /** Whether it takes an `If-Match`. */
    val conditional: Boolean,
    /** Whether it answers a conditional read from the caller's copy with a 304. */
    val notModified: Boolean,
    /** How it pages by cursor, or null where it does not. */
    val pagination: Pagination?,
    /** What it takes to authenticate, and so what it refuses locally. */
    val credentials: Credentials,
    /** The parameters its path names, in its order. */
    val pathParameters: List<Parameter>,
    /** The parameters its query takes, in the bundle's order. */
    val queryParameters: List<Parameter>,
    /** The headers a caller sets, none of the four the client sets itself among them. */
    val headerParameters: List<Parameter>,
    /** The body it takes, or null where it takes none. */
    val requestBody: RequestBody?,
    /** What it answers with when it succeeds. */
    val success: Success,
    /** The error statuses it declares. */
    val errors: List<ErrorResponse>,
    /** Its one-line summary, or null. */
    val summary: String?,
    /** Its description, or null. */
    val description: String?,
    /** Whether the bundle marks it deprecated. */
    val deprecated: Boolean,
    /** The tasks a write runs, each the name the contract's `x-task` gives it, none for a read. */
    val tasks: List<String> = emptyList(),
)

/** Credentials holds the six credential facts, each read from what the operation declares. */
class Credentials(
    /** `x-acting-user`: `required`, `optional` or `none`. */
    val actingUser: String,
    /** Whether a share token authenticates it. */
    val acceptsShareToken: Boolean,
    /** Whether it needs a session token beside the publishable key. */
    val requiresSession: Boolean,
    /** Whether a secret key alone authenticates it. */
    val requiresSecretKey: Boolean,
    /** Whether it is sent with no credential at all. */
    val takesNoCredential: Boolean,
    /** `x-pending-invitee`, which decides nothing the client checks. */
    val pendingInvitee: Boolean,
)

/**
 * Pagination says how an operation that pages by cursor sorts: every order its `sort` takes, and the one it defaults
 * to.
 */
class Pagination(
    /** The orders its `sort` parameter takes, empty where it takes none. */
    val sort: List<String>,
    /** The order it sorts by when the caller names none, or null. */
    val defaultSort: String?,
)

/** Parameter is one parameter of an operation, or one header a success declares. */
class Parameter(
    /** Its name, as the wire carries it. */
    val name: String,
    /** Whether a caller must send it. */
    val required: Boolean,
    /** Its shape. */
    val type: TypeNode,
    /** Its description, or null. */
    val description: String?,
    /** The server's default, where the bundle states one, or null. */
    val default: Any?,
    /** Whether the bundle states a default, which [default] holds, null included. */
    val hasDefault: Boolean,
    /** Whether the bundle marks it deprecated. */
    val deprecated: Boolean,
)

/** RequestBody is the body an operation takes. */
class RequestBody(
    /** Whether a caller must send it. */
    val required: Boolean,
    /** Each media type it is taken in, in the bundle's order. */
    val content: List<MediaContent>,
)

/** MediaContent is one media type a body or a document is carried in, and its shape. */
class MediaContent(
    /** The media type, such as `application/json`. */
    val mediaType: String,
    /** The shape it carries. */
    val type: TypeNode,
    /**
     * A form's `encoding`, where the bundle states one: the media type each part named by its property is sent as
     * where its caller names none, in the model's order. Empty for every other body.
     */
    val encoding: List<PartEncoding> = emptyList(),
)

/**
 * PartEncoding is one part of a form's `encoding`: the part its [property] is sent as, and the [contentType] it
 * carries.
 */
class PartEncoding(
    /** The property the part is sent for. */
    val property: String,
    /** The media type the part carries where its caller names none. */
    val contentType: String,
)

/** Success is what an operation answers with when it succeeds, one shape however many statuses answer it. */
class Success(
    /** `envelope`, `event-stream` or `binary`. */
    val kind: String,
    /** What an envelope carries at `data`, or null where the operation does not narrow it. */
    val data: Payload?,
    /** What an envelope carries at `metadata`, or null where the operation does not narrow it. */
    val metadata: Payload?,
    /** The type of each event of an event stream, or null for any other success. */
    val event: TypeNode?,
    /** The media types a binary success arrives in, empty for any other. */
    val mediaTypes: List<String>,
    /** Every status a success arrives with, in the model's order, and the headers each declares. */
    val responses: List<SuccessResponse>,
    /** The documents a template's route answers, named for its documentation. */
    val documents: List<MediaContent>,
)

/** SuccessResponse is one status a success arrives with, and the headers it declares. */
class SuccessResponse(
    /** The HTTP status. */
    val status: Int,
    /** The headers it declares, by name, requirement and type. */
    val headers: List<Parameter>,
)

/** Payload is one part of an envelope: whether it is always present, and its shape. */
class Payload(
    /** Whether it is always present. */
    val required: Boolean,
    /** Its shape. */
    val type: TypeNode,
)

/** ErrorResponse is one error status an operation declares. */
class ErrorResponse(
    /** The HTTP status. */
    val status: Int,
    /** What the status adds under `extensions.metadata`, or null. */
    val metadata: Payload?,
)

/** DataSurface is the Data API: its operations, and the types of the SDL. */
class DataSurface(
    /** The origin the Data API is sent to. */
    val origin: String,
    /** The path every Data API request is sent to. */
    val endpoint: String,
    /** The operations, in the model's order. */
    val operations: List<DataOperationModel>,
    /** The object types of the SDL. */
    val objects: List<DataType>,
    /** The input types of the SDL. */
    val inputs: List<DataType>,
    /** The enums of the SDL. */
    val enums: List<DataEnum>,
    /** The scalars of the SDL. */
    val scalars: List<DataScalar>,
)

/** DataOperationModel is one Data API operation, as the SDL declares it. */
class DataOperationModel(
    /** The operation's name, which its method is printed as. */
    val name: String,
    /** The name of the argument its input is passed as. */
    val input: String,
    /** The type of that argument, as the SDL writes it. */
    val inputType: String,
    /** The type it answers, as the SDL writes it. */
    val resultType: String,
    /** The scopes a key reaches the operation under. It decides nothing printed. */
    val scopes: List<String>,
    /** The budget the SDL states for every Data API operation. */
    val timeoutMs: Int,
    /** What the operation takes, and so what it refuses locally, as a REST operation states it. */
    val credentials: Credentials,
    /** Every status its success arrives with, in the model's order, and the headers each declares. */
    val responses: List<SuccessResponse>,
    /** The ordered tree of every field it selects when a caller narrows nothing. */
    val selection: List<SelectionNode>,
    /** The exact bytes sent when a caller narrows nothing. */
    val document: String,
    /** Its description, or null. */
    val description: String?,
    /** Whether the SDL marks it deprecated. */
    val deprecated: Boolean,
)

/** SelectionNode is one field of a full selection, with the fields beneath it where it is an object. */
class SelectionNode(
    /** The field's name. */
    val name: String,
    /** The fields selected beneath it, or null where it is no object. */
    val fields: List<SelectionNode>?,
)

/** DataType is an object type or an input type of the SDL. */
class DataType(
    /** The type's name. */
    val name: String,
    /** Its description, or null. */
    val description: String?,
    /** Its fields, in the SDL's order. */
    val fields: List<DataField>,
)

/** DataField is one field of an object type or an input type of the SDL. */
class DataField(
    /** The field's name. */
    val name: String,
    /** Whether a caller must send it. Always false on an object type, whose fields are read. */
    val required: Boolean,
    /** Its shape. */
    val type: TypeNode,
    /** Its description, or null. */
    val description: String?,
    /** Its default, where the SDL states one, or null. */
    val default: Any?,
    /** Whether the SDL states a default, which [default] holds, null included. */
    val hasDefault: Boolean,
    /** Whether the SDL marks it deprecated. */
    val deprecated: Boolean,
)

/** DataEnum is an enum of the SDL. */
class DataEnum(
    /** The enum's name. */
    val name: String,
    /** Its description, or null. */
    val description: String?,
    /** Its members, in the SDL's order. */
    val members: List<DataEnumMember>,
)

/** DataEnumMember is one member of a [DataEnum]. */
class DataEnumMember(
    /** The member's name, as the wire carries it. */
    val name: String,
    /** Its description, or null. */
    val description: String?,
    /** Whether the SDL marks it deprecated. */
    val deprecated: Boolean,
)

/** DataScalar is a scalar of the SDL: built in, or carried on the wire as `wire` says. */
class DataScalar(
    /** The scalar's name. */
    val name: String,
    /** Whether it is one of GraphQL's own. */
    val builtin: Boolean,
    /** The type it is carried on the wire as, or null for a built-in one. */
    val wire: TypeNode?,
    /** RFC 7946's shape, where the wire is a GeoJSON geometry. */
    val geometry: String?,
)
