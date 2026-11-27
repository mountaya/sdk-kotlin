# Mountaya Kotlin SDK

The documentation is at [docs.mountaya.com/en/sdks/kotlin](https://docs.mountaya.com/en/sdks/kotlin/): installing the
modules, the quickstarts for a Compose app and a JVM server, the map, navigation, and every public name by capability.

Mountaya's Kotlin SDK, written against contract v0.9.0 of [`mountaya/schemas`](https://github.com/mountaya/schemas). It declares five modules. Four are Kotlin Multiplatform, for the jvm, Android and iOS
(`iosArm64` and `iosSimulatorArm64`):

- `mountaya-client`, the client for the Map Studio API, the Tile API and the GraphQL Data API,
  holding every transport rule the contract states: credentials and the session lifecycle,
  retries, idempotency keys, conditional writes, the error ladder, dates, pagination, event
  streams and field selection.
- `mountaya-admin`, the client's admin unit, which only a server that manages an organization, mints
  session tokens or receives webhooks needs: the Map Studio API's `admin` operations, on the client as
  `mountaya.admin` once imported, and, on a server, the verification of the webhook events Mountaya
  delivers. An app that draws a map, edits an itinerary, queries the Data API or navigates leaves it out.
- `mountaya-geo`, what Mountaya decides about that data: distances and units, formatted values,
  the elevation profile and its chart, the itinerary's tables and a place's card, the camera, the
  map's layers and icons, the search and its engine, and
  navigation's rules, with the `NavigationEngine` interface, the machine every engine follows a
  route on, and a scripted engine for an app's tests. It calls nothing.
- `mountaya-elements`, how an app shows it: a map's state and its engine on every target, and on
  Android the map itself, a composable over [MapLibre Native](https://github.com/maplibre/maplibre-native)
  whose own requests carry your client's credential, with its controls, its panels, a search
  box, and an itinerary's elevation profile, its tables and a place's card.

The fifth is Android's:

- `mountaya-navigation`, the engine an Android app navigates with: the contract's navigation on the
  device, which says each maneuver aloud in your words. It calls nothing either: the route comes from
  your own client.

[The API by capability](https://docs.mountaya.com/en/sdks/kotlin/reference/api/) names what this SDK shares with
Mountaya's Swift, Dart and TypeScript SDKs, each name beside its TypeScript twin. [SPEC.md](SPEC.md) is what the SDK is and will be, and
[AGENTS.md](AGENTS.md) the rules a change to it is held to.

## Install

Each release is published to Maven Central, under the `com.mountaya` namespace. The modules'
coordinates are `com.mountaya:mountaya-client`, `com.mountaya:mountaya-admin`, `com.mountaya:mountaya-geo`,
`com.mountaya:mountaya-elements` and `com.mountaya:mountaya-navigation`, at one version, and a Kotlin
Multiplatform build resolves each target's variant from that one coordinate. A build depends on them
from the release it was written against, and names the modules it uses:

<!-- kotlin:skip a build script, which consumer/build.gradle.kts declares the same way -->
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mountaya:mountaya-client:0.1.0")
            implementation("com.mountaya:mountaya-geo:0.1.0")
            implementation("com.mountaya:mountaya-elements:0.1.0")
        }
    }
}
```

A consumer's build meets the modules' floors, the ones their own build is held to:

| Floor | Version |
| --- | --- |
| The Kotlin a consumer compiles with | Kotlin 2.4 or newer |
| The Java its jvm and Android code targets | Java 17 or newer |
| The API level an Android app runs from | `minSdk 24` or higher |
| The API level an Android app compiles against | `compileSdk 37` or higher |

An Android app that shows the map applies the Compose compiler, as every Compose app does;
`mountaya-elements` brings MapLibre and Compose's UI with it, so the app declares neither.

A server, or an app that manages its organization, adds the admin unit, `mountaya-admin`, at the
same version, and nothing else: it brings `mountaya-client` with it. It is optional, and an app that
leaves it out links none of it.

<!-- kotlin:skip a build script, which consumer/build.gradle.kts declares the same way -->
```kotlin
dependencies {
    implementation("com.mountaya:mountaya-admin:0.1.0")
}
```

An Android app that navigates adds `mountaya-navigation` to its Android dependencies, and no build
setting of its own:

<!-- kotlin:skip a build script, which examples/trail/build.gradle.kts declares the same way -->
```kotlin
dependencies {
    implementation("com.mountaya:mountaya-navigation:0.1.0")
}
```

## A first call

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken

val client =
    Mountaya(
        credential = Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")),
        language = "fr",
    )
val itinerary = client.studio.itineraries.getItinerary("itn_4e5f")
println("${itinerary.data.name} ${itinerary.status} ${itinerary.requestId}")
client.close()
```

Each module's public API is one package named after it, `com.mountaya.client`, `com.mountaya.admin`,
`com.mountaya.geo`, `com.mountaya.elements` and `com.mountaya.navigation`, with the types printed
from the contract beside the client's, the admin unit's and Geo's in
`com.mountaya.client.generated`, `com.mountaya.admin.generated` and `com.mountaya.geo.generated`.
One import per module suffices: `import com.mountaya.client.*` brings every name the client
publishes but its printed types. The snippets here import each name they use, as a formatter that
refuses wildcard imports asks.

`Mountaya` is the one type that reaches the network. An operation hangs where the contract files it:
`client.studio.itineraries`, `client.agent.runs`, then `client.tiles` and `client.data`, and, once
`mountaya-admin` is imported, `client.admin.organizations`. Each method is named by the contract's
`operationId`, verbatim, and takes its path parameters by name, its required query parameters after
them and its body as one `body` value; every optional query parameter, the `Idempotency-Key` and the
`If-Match` are set in its trailing builder. Every method suspends but a stream's and a listing's
walk, which return a cold `Flow`, and every one takes `options`. An enum the contract declares is
open, a value class that decodes a value a newer server sends as itself, so a `when` over one ends
in `else`, and each member is named by its words in UpperCamelCase, its type's name left off:
`TileSchema.TileLayerType.Line`, `DataSchema.ActivitySlug.HikingAndTrail`.

A client is configured once: `language` rides every request as `Accept-Language`, and a client
given none sends `*`, which asks for no language in particular, never for the device's. Pass the user's own
language to answer in it. Every request also names the SDK that made it, as `X-Mountaya-Client:
mountaya-kotlin/<version>`, the version you install; no call sets it, and one by declaration that
names it is refused.

```kotlin
import com.mountaya.client.BaseUrls
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.Url
import com.mountaya.client.generated.MODEL_DIGEST
import com.mountaya.client.generated.SCHEMA_VERSION

// The contract tag this build speaks, and the digest of the exact files it was printed from.
println("$SCHEMA_VERSION $MODEL_DIGEST")

// A client pointed at another environment, its Map Studio API at a staging origin.
val staging =
    Mountaya(
        Credential.Publishable("pk_test_0a1b"),
        baseUrls = BaseUrls(rest = Url("https://api.staging.example.com")),
    )
println(staging.baseUrls)
staging.close()
```

`SCHEMA_VERSION` is the contract tag a build speaks, `v0.9.0` for this one: every type and method
was printed from it, and it moves only when the client is printed again from a later tag, apart from
the modules' own version, which `X-Mountaya-Client` names. `MODEL_DIGEST` names the exact files it was
printed from, the contract's model and the elements' tables, as the contract's `modelDigest`, which
tells two builds printed between the same two tags apart; the SDK's conformance report carries it too.

`baseUrls` points a client at another environment, a staging deployment or a proxy. Each surface's
origin is set on its own, `rest`, `tiles` and `data`, one left out stays Mountaya's, and an origin
may carry a path of its own, a proxy's prefix, which every request's path follows. A `Url` is the
text as written: one an answer carries reads as the wire sent it, a web address with no scheme
included, while one you send is held to a scheme, an origin with none refused where `BaseUrls` is
built and a web address with none in a body you build refused before any request.

Each answer is read and decoded on the client's `dispatcher`, `Dispatchers.Default` unless you pass
another, so a call made from `viewModelScope` or a composable waits on the main thread and parses
nothing there; the call resumes on your own dispatcher once its answer is decoded.

## Examples you can run

`examples/trail` is Trail, the one sample app every native SDK ships, which the contract's `sdk/trail.yaml` describes
once for all three: an Android app in Compose, a Gradle build of its own that resolves the modules by their coordinates
from this checkout, as `consumer/` does, and that you run on an emulator or a phone. It is one portrait screen: the map,
under its zoom, compass, locate, layers, points-of-interest and preferences controls, its layers panel offering the time
slider; a search box over it; and a bottom sheet with an itinerary's elevation profile, its segment and waypoint tables
when you open it, and Start, which follows the whole itinerary end to end in one navigation, says each maneuver and each
waypoint passed in Trail's own words, and shows what is left of the route until you arrive. Replay walks the route for
you in place of your positions. The gate builds Trail, holds its size to a baseline (`mise run size:check`) and its code
to `sdk/trail.yaml` (`checkTrail`), and walks the scenario's journey through it on an emulator (`mise run test:device`).
Its code is MIT licensed, unlike the SDK's, so copy what you need into your own app.

### First, a publishable key

Trail's build reads its settings from your shell or from `examples/trail/.env`, which is never committed, into string
resources the app reads once as it starts: copy `examples/trail/.env.example` there and fill in what you have, a variable
in your shell standing over the file's, then build again. **`pk_…` in this README is a placeholder, not a working
key.** Yours comes from your organization's API keys page in the Mountaya dashboard, and the reference for what each
kind of key reaches is at [docs.mountaya.com](https://docs.mountaya.com). A key is its prefix followed by 32 lowercase
hex characters, with nothing in between that names an environment. A publishable key (`pk_…`) is the one an app
carries. A **secret** key (`sk_…`) mints session tokens and belongs on a server: no app reads it, and an Android or
iOS build has no symbol to construct one ([Credentials](#credentials)).

| Setting | Read by | Scopes | What it does |
| --- | --- | --- | --- |
| `MOUNTAYA_PUBLISHABLE_KEY` | the app and the session server | `tiles`, `publishing`, `data` | The key every request carries: `tiles` draws the map, `publishing` reads the itinerary and `data` searches. It carries no `management`, since the session minted for it reaches the device |
| `MOUNTAYA_ITINERARY_ID` | the app | `publishing` | A published itinerary your key reads, shown and followed in place of the demo itinerary |
| `MOUNTAYA_SESSION_URL` | the app | `data` | Where the app asks for the session token search needs |
| `MOUNTAYA_SECRET_KEY` | the session server | none | The key that mints the session token, which no app ever reads |

Without a publishable key, Trail shows a notice in place of the map and the search, and runs everything that needs no
network on its demo itinerary, Chamonix to La Floria: the profile, the tables, and a navigation with Replay on. A map the
API refuses, for a key without `tiles` among other reasons, shows a notice of its own.

The Data API answers a publishable key only beside a session token, which a secret key mints, and a secret key never goes
into an app. `mise run trail:session` runs Trail's session server, a JVM program on the JDK's own HTTP server over
`mountaya-admin`: it reads both keys from your shell and refuses to start without either, listens on `127.0.0.1:8787`
alone, and answers `POST /session` with a session `postSession` mints for your publishable key without `user_id`. The
Android emulator reaches your computer's loopback address at `10.0.2.2`, so the app's `MOUNTAYA_SESSION_URL` is
`http://10.0.2.2:8787/session`, over plain HTTP, which Trail's debug build alone allows. Without a session URL, search
stays disabled and says why.

```sh
cp examples/trail/.env.example examples/trail/.env   # then put your pk_… key, and the session URL, in it
MOUNTAYA_PUBLISHABLE_KEY=pk_… MOUNTAYA_SECRET_KEY=sk_… mise run trail:session   # in a second shell
./gradlew -p examples/trail installDebug             # with an emulator running, then open Trail on it
```

Trail reads your position only while it is on screen, at full accuracy, and asks for it only when you press Start with
Replay off. It declares no background location and runs no foreground service, which a sample you copy should not carry
into a store's review. An app that navigates with the screen off opts in itself: it declares
`ACCESS_BACKGROUND_LOCATION`, with the declaration Google Play asks for, or a foreground service of type `location` it
runs, and makes its source with `DeviceLocationSource(context, rate = LocationRate.Walking, backgroundUpdates = true)`,
which holds that to its merged manifest, as [Navigation](#navigation) says.

## Credentials

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.SessionTokenEngine

// In an app: a publishable key, and a session token your own backend mints with its secret key.
val engine = SessionTokenEngine(applicationScope, mint = { fetchSessionFromYourBackend() })
val app = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Source(engine)))

// For a visitor holding a share link: the share token replaces the key.
val visitor = Mountaya(Credential.Share("share_6a7b"))

// From parts held apart, a key from your configuration and a token from a link.
val linked = Mountaya(Credential(publishableKey = "pk_live_0a1b", shareToken = "share_6a7b"))

app.close()
visitor.close()
linked.close()
```

A session token accompanies a publishable key, and `SessionTokenEngine` keeps one fresh in the
scope you give it: it mints through the function you give it, refreshes before expiry, and runs
only while something collects its `snapshot`, a `StateFlow` whose `value` is the token now, which a
`Mountaya` built over it does until it is closed. A mint that fails is tried again five seconds later, each failure in a row after it
waiting twice as long as the one before, up to a minute, a timeout your own call set with
`withTimeout` among the failures, and one your app cancels waits for `refresh()`, `enable()` or a new
subscription to ask again. A token can also be a plain value, a minted `RestSchema.Session`, a function read on
every request, or any `SessionTokenSource` of your own.

`Credential(publishableKey, sessionToken, shareToken)` builds the credential from parts you hold
apart, a key from your configuration and a share token from a link, and applies "a share token
replaces the key" itself, so `linked` above sends the share token alone. Parts that make no
credential, a session token with no key among them, are a `ConfigurationException`. A share link
that rides the visitor's own session takes its `Cookie` header from the `CookieSource` you give
`Credential.Share`, per request: a read goes out on the visitor's session alone, one of an operation that
requires a session token included, since only the server can tell whether the cookie holds one, and a
write under it is refused. A read is a request sent with `GET`, `HEAD` or `OPTIONS`, in any letter case,
or a call to an operation declared one, `CredentialDeclaration(read = true)`, which every Data API query
is over its `POST`. A cookie no header carries is refused as a `ConfigurationException`, a map's request
included, its value never written out.

`Credential(secretKey = ...)` and `Credential.Secret(key)` are declared on the jvm alone, for a server, a
command-line tool or a desktop process: an Android or iOS build has no symbol to call, so a secret key
cannot ship in an app. The second reads as the class's constructor once `com.mountaya.client.invoke` is
imported, as an IDE imports it.

A server mints the session token an app's engine asks for under that secret key, through the admin unit:
it depends on `mountaya-admin` and imports `admin`, which declares `mountaya.admin` on the client, and
`postSession` answers the core's `RestSchema.Session`, which `SessionToken.Minted` takes as it is.

```kotlin
import com.mountaya.admin.admin
import com.mountaya.admin.generated.AdminSchema
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.RestSchema

/**
 * mintFor answers, on the app's backend under its secret key, the token the app's session engine asks
 * for, for one user, minted for the `studio-embedded` surface every write acts as a member on.
 */
suspend fun mintFor(
    server: Mountaya,
    userId: String,
): SessionToken {
    val minted =
        server.admin.sessions.postSession(
            AdminSchema.SessionCreateParams(publishable_key = "pk_live_0a1b") {
                user_id = userId
                surface = RestSchema.SessionSurface.StudioEmbedded
            },
        )
    return SessionToken.Minted(minted.data)
}
```

The secret key authenticates `postSession` alone, and the client refuses it for every other operation
before a request leaves. Every `admin` operation is called the same way, through the client's own
transport, its retries and its error classes, and a body or an answer only the admin unit reaches is
printed in `AdminSchema` beside the core's `RestSchema`.

A credential or a language no request could carry is refused where the client is built, and a
credential an operation does not take is refused before its request leaves, both as a
`ConfigurationException`. A header carries printable ASCII alone, since the platform's HTTP stack
sends no other character in one, so a value holding one, `é` among them, is refused alike, a header of
a call and an `ifMatch` before any request, and never repeated as a request that never arrived.

## Writes

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.RequestOptions
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.RestSchema
import kotlin.time.Duration.Companion.seconds

val client = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))
val zone = client.studio.zones.getZone("zon_8c9d")

val renamed =
    client.studio.zones.patchZone(
        "zon_8c9d",
        body = RestSchema.ZoneUpdateParams { name = "Vallée Blanche" },
        options = RequestOptions(timeout = 10.seconds),
    ) {
        idempotencyKey = "rename-zon_8c9d-1"
        ifMatch = zone.data.etag
    }
println("${renamed.status} ${renamed.data.name} ${renamed.etag}")
client.close()
```

A write acts as a member of the organization, so it carries a publishable key with the
`studio-embedded` scope and a session token minted for the `studio-embedded` surface, naming that
member: your server mints it with `postSession`, `surface = RestSchema.SessionSurface.StudioEmbedded`
and `user_id` set ([Credentials](#credentials)). A token minted for any other surface, `api` among
them, is refused there with a `401`, an `AuthenticationException`, and the organization must hold
Embedded Studio, or the mint itself is refused with a `402`, a `BillingException`, as it is with a
`403`, a `ForbiddenException`, for a key without the `studio-embedded` scope.

A body is built by a factory named after its type, which takes the required fields as arguments
and sets the optional ones in its trailing builder. A property that is optional and nullable in
the contract has three states there: left unassigned it is not sent, assigned a value it replaces
the stored one, and assigned `null` it sends `null`, which clears it.

A write that takes an `Idempotency-Key` sends the one you set, or draws one, and sends it on every
attempt, so a repeated write lands once. `ifMatch` sends the tag you read the resource under; a
stale one is a `PreconditionFailedException`, never repeated. A `Response` carries `data`,
`metadata`, the `status`, and the answer's `requestId`, `location` and `etag`, and every header the
answer sent, `headers`, its names in lowercase, which `header` reads in any letter case.

## Errors

```kotlin
import com.mountaya.client.ApiException
import com.mountaya.client.ConfigurationException
import com.mountaya.client.ConnectionException
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.NotFoundException
import com.mountaya.client.SelectionException
import com.mountaya.client.generated.ApiErrorCode

val client = Mountaya(Credential.Publishable("pk_live_0a1b"))
try {
    client.studio.itineraries.getItinerary("itn_4e5f")
} catch (failure: NotFoundException) {
    println("gone ${failure.requestId}")
} catch (failure: ApiException) {
    if (failure.hasCode(ApiErrorCode.Forbidden)) println("an entry names a scope the key does not reach")
    println("${failure.name} ${failure.code} ${failure.retryAfter}")
} catch (failure: ConnectionException) {
    println("the request never arrived, after every retry it was owed: ${failure.cause}")
} catch (failure: ConfigurationException) {
    println(failure.message)
} catch (failure: SelectionException) {
    println(failure.path)
}
client.close()
```

Every error is a `MountayaException`, and the class an answer raises is its row of the
contract's ladder, so `is` and `catch` tell them apart; a `when` over them keeps an `else`, since
the ladder grows with the contract. An error's message ends on what to do about it, where its
status names a step: the credential to check on a `401`, where a `402`'s remedies are taken, the
scope on a `403`, a stale tag on a `412`, and on a `429` the wait, a ceiling's remedies, or the
instant a spent monthly allowance renews. The exception that caused one, the exchange's own
failure or the decoder's, rides along as its `cause`, never as the type you catch, and the decoder's
carries no byte of the answer, so a session token or a client secret in a body that did not decode
never reaches your logs.

An error's `code`, its first entry's, and the `code` of each `ErrorEntry` in its `errors` are an
`ApiErrorCode`: one vocabulary for the Map Studio, Tile and Data APIs, a value class whose constants
are the codes the three declare, `ApiErrorCode.LimitReached` or `ApiErrorCode.Forbidden`, printed
from the contract with the rest of the surface. It is open, so a code a later release of the API
sends is kept exactly as sent, and a `when` over it keeps an `else`. The code is reported beside the
class rather than choosing it. `hasCode` asks every entry, not the first alone, since a refusal may
name several codes at once: a `402` whose second entry is coded `FORBIDDEN` answers it for both.

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.RequestOptions
import com.mountaya.client.RetryPolicy
import com.mountaya.client.isApiException
import com.mountaya.client.isMountayaException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.seconds

// The contract's schedule with a longer budget of waits for this client, and no retry for one call.
val client = Mountaya(Credential.Publishable("pk_live_0a1b"), retry = RetryPolicy(waitBudget = 120.seconds))
try {
    client.studio.activities.getActivities(
        options = RequestOptions(retry = RetryPolicy(maxRetries = 0), timeout = 10.seconds),
    )
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: Exception) {
    // Whether a failure is one of the family, and whether the API answered it.
    println("${isMountayaException(failure)} ${isApiException(failure)}")
}

// A call you withdraw throws its coroutine's `CancellationException`, never an error of the ladder.
coroutineScope {
    val call = async { client.studio.activities.getActivities() }
    call.cancel()
    try {
        call.await()
    } catch (withdrawn: CancellationException) {
        println("withdrawn")
    }
}
client.close()
```

A `429`, and a `502`, `503` or `504` or a dropped connection where the operation is idempotent or
carries a key, are repeated on the contract's schedule before an error is raised: two attempts
after the first, waits drawn up to a ceiling that doubles from 500 ms to 30 s, or what a
`Retry-After` names, a date's wait rounded up to the whole second the error's `retryAfter` names, all
out of one 60-second budget, and each attempt within the larger of 30
seconds and five seconds past the operation's own time, 35 seconds for a Data API call.
`RetryPolicy`, its `waitBudget` among it, and the `timeout` are the client's to change and a call's
to override through `RequestOptions`. An attempt that runs out of its time is a
`ConnectionException`, repeated as one; a call you no longer want to wait for is withdrawn instead.

A ceiling is the one `429` never repeated: the seats, the published itineraries or zones an
agreement allows, the webhook endpoints an organization may hold, the invitations waiting at once
or a personal space's itineraries. It is a `RateLimitException` whose `code` is
`ApiErrorCode.LimitReached` and whose `metadata` names the ceiling with what it allows and what is held, and it names no wait,
since only something freed or a changed agreement lifts it.

`isMountayaException` and `isApiException` answer whether any failure is one of the family and
whether the API answered it, by the contract's name each class declares, `MountayaException.name`,
so code that holds a `Throwable`, a logger or a crash reporter's filter, tells them apart without a
`when`. A failure of a class of your own that declares such a name, a `NamedException`, rebuilt where
the client's own classes did not reach, is recognised by it too.

A call is withdrawn by cancelling its coroutine: a `Job`'s `cancel()`, the end of the scope it runs
in, or `withTimeout`. A coroutine cancelled before the call starts sends nothing, a cancellation
during a request or a wait between two attempts settles it at once, and the call throws its
`CancellationException` as it is, since your cancellation is no failure of the API and is never
repeated. A stream or a walk ends the same way when the coroutine collecting it is cancelled.

## Lists, streams and the Data API

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.FieldSelection
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.readingAsUser

val client = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))

// A listing is read a page at a time, each page requested as the rows before it are read.
client.studio.itineraries
    .walkGetItineraries(maxPages = 10) { sort = RestSchema.GetItinerariesSort.UpdatedAtDescending }
    .collect { itinerary -> println(itinerary.name) }

// An event stream reopens, waits and resumes on the contract's schedule, and ends on `done`.
client.agent.runs.getAgentRunEvents("run_0f1e").collect { frame ->
    val event = frame.event
    if (event is RestSchema.AgentMessageEvent) println(event.content)
}

// An editor stream is read as its member's user, which a session token given as a value does not
// name, so that member's own arrival is not read as the stream moving on.
client.studio.collab.getItineraryEvents("itn_8f14").readingAsUser("usr_7a4e").collect { frame ->
    val event = frame.event
    if (event is RestSchema.CollabPresenceJoinEvent) println(event.user_id)
}

// A Data API call narrows its document to the fields it selects.
val places =
    client.data.geocode(
        DataSchema.GeocodeInput(query = "Chamonix"),
        select =
            FieldSelection {
                field("places") {
                    field("name")
                    field("point")
                }
            },
    )
println(places.places?.firstOrNull()?.name)
client.close()
```

Every listing that pages has its walk beside its method, `walk` before its operation's name, which
takes the method's own arguments and builder, its filters and its sort, and asks for pages of the size
its operation states, 50, unless you name another `pageSize`; `maxPages` is the most pages it requests
before it stops without an error. `PageWalk` walks a listing through its method and a builder of yours
that applies `paged` last, `PageWalk { paged -> client.studio.itineraries.getItineraries { paged() } }`.
A collector that stops early requests no further page. A stream is opened when it is collected and
closed when its collector leaves. A stream method answers an `EventStream`, a `Flow` of frames, and
`readingAsUser` is applied to it before any other operator: the user a minted session or a session
source names wins over the one it states.

An operation newer than this build is called by its declaration, written from the contract:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.DataOperation
import com.mountaya.client.FieldSelection
import com.mountaya.client.Mountaya
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.SessionToken
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

val client = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))

// A read this build prints no method for, by what the contract declares of it.
val getThing =
    OperationDeclaration(
        operationId = "getThing",
        method = "GET",
        idempotent = true,
        credentials = CredentialDeclaration(requiresSession = true),
    )
val thing =
    client.send(getThing, "/v1/things/thg_1a2b", JsonElement.serializer(), JsonObject.serializer().nullable)
println(thing.data)

// A listing, walked a page at a time: the walk writes the page size and the cursor into the query.
val getThings = OperationDeclaration("getThings", "GET", idempotent = true)
client
    .walk(getThings, "/v1/things", JsonElement.serializer(), query = listOf("kind" to "hut"), maxPages = 5)
    .collect { row -> println(row) }

// A Data API operation, by its name, its input's type and its field tree.
val things = DataOperation("things", "ThingsInput!", FieldSelection { field("things") { field("name") } })
println(client.query(things, buildJsonObject { put("query", "hut") }, JsonElement.serializer()))

// A stream, read as a feed of records.
val getThingEvents = OperationDeclaration("getThingEvents", "GET")
client.events(getThingEvents, "/v1/things/thg_1a2b/events").collect { frame ->
    println("${frame.event.event} ${frame.event.data}")
}
client.close()
```

`Mountaya.send` takes the `OperationDeclaration` you write from the contract, its `operationId`,
method, surface, idempotence, credential facts and budget, whether it `declaresIdempotencyKey` and
whether it `isConditional`, each fact you leave out the one no operation states by default, its
path already filled in, and the serializers its `data` and `metadata` are read with. Its `body` is
a JSON value, `RequestBody.Json(value)`, or a form,
`RequestBody.Multipart(listOf(MultipartPart.Text("name", "Hut"), MultipartPart.File("file", bytes, "tour.gpx", "application/gpx+xml")))`,
a file's part sent with the file name and the media type you give it, `application/octet-stream` where
you give none, and a
header the client writes itself, `X-Mountaya-Client` among them, one the HTTP stack writes,
`Transfer-Encoding` and `Upgrade`, and a name that is no token are refused in its `headers`, as a
`ConfigurationException`. `Mountaya.walk` reads a listing a page at a time, as a printed walk
does, `pageSize` and `maxPages` included, writing the page size and the cursor into the query you
give; `Mountaya.query` calls a Data API operation by its `DataOperation`, its name, its input's
type and the field tree a `select` is checked against; and `Mountaya.events` opens a stream by its
declaration, read as a feed, each frame carrying an `EventRecord`. A call made this way is held to
every rule a printed method is, and infers nothing its declaration does not state.

## Receiving webhooks

Mountaya delivers each change an organization's webhook endpoints subscribe to as a signed `POST`
to the endpoint's URL. `constructWebhookEvent` verifies the delivery's `Mountaya-Signature` with
the endpoint's signing secret and returns the event, a class of `WebhookEvent` for each type, so a
`when` reads what each type carries. Receiving webhooks is the admin unit's: a server depends on
`mountaya-admin` and imports from `com.mountaya.admin`.

<!-- kotlin:skip JVM code, which consumer/src/jvmMain/kotlin/com/example/consumer/Webhooks.kt compiles in receive() -->
```kotlin
import com.mountaya.admin.WebhookSignatureException
import com.mountaya.admin.constructWebhookEvent
import com.mountaya.admin.generated.WebhookEvent

/**
 * Receives a delivery on a server: the body exactly as it arrived, and its `Mountaya-Signature` header,
 * in; the status to answer the delivery with, out.
 */
fun receive(
    body: ByteArray,
    signature: String?,
): Int {
    val event =
        try {
            constructWebhookEvent(body, signature, webhookSecret)
        } catch (refusal: WebhookSignatureException) {
            println(refusal.reason)
            return 400
        }

    // Each event arrives at least once: act on its `id` once.
    println("${event.id} ${event.type} ${event.created_at}")
    when (event) {
        is WebhookEvent.OrganizationMemberUpdated -> {
            // The membership as it is now, and the role it held before the change.
            println("${event.data.`object`.role} ${event.data.previous_attributes?.role}")
        }

        is WebhookEvent.CollectionDeleted -> {
            println("collection ${event.data.`object`.id} deleted")
        }

        else -> {}
    }
    return 204
}
```

`constructWebhookEvent` is declared on the jvm alone, for servers, as `Credential(secretKey = ...)`
is: an Android or iOS build has no symbol to call, so the endpoint's secret never ships in an app.
Pass the body exactly as it arrived, before any JSON parser reads it: the signature covers those
bytes, and a body decoded and encoded again is other bytes, which do not verify. A body read as a
`String` is verified by its UTF-8 bytes, which are the bytes Mountaya signed. The secret is the
endpoint's, `whsk_` included, answered once by `admin.webhooks.postOrganizationWebhookEndpoint`:
keep it on the server. A secret that does not open with `whsk_`, which the message never quotes, and a
negative `tolerance` are each a `ConfigurationException`, thrown before the header is read, since
either is a mistake in your code rather than a delivery to refuse.

A delivery is refused with a `WebhookSignatureException`, a `MountayaException` like every error of
the client, so a handler catching the family catches it too, whose `reason` says why: `MissingHeader`,
`MalformedHeader`, `Signature` where no `v1` of the header matches, and `Timestamp` where the
signature verifies and was made further from your clock than the tolerance, 300 seconds either way
unless you pass another `tolerance`, a `Duration`. Answer it with a `400`. While a rotated secret overlaps its
successor the header carries a `v1` for each, and either secret verifies. `now` states the clock a
delivery is judged by, which a test pins; left out, it is the machine's wall clock.

Every member of the envelope, `id`, `created_at`, `task`, `actor` and the rest, is read off the
event whatever its class. The event's instants are `kotlin.time.Instant`s, as a method answers
them, and its previous attributes are the fields its change moved, each null where it did not move,
a field that may be null read as a `Stated` that holds null where it held null, and `Nothing` for a
type whose change moves none. Mountaya delivers an event at least once and in no set order, so keep
what you act on keyed by its `id`, and answer with a `2xx` within 10 seconds, before any slow work.
An event this build cannot type, of a type a newer contract added or whose payload its types do not
read, arrives as `WebhookEvent.Unknown`, holding the contract's envelope, `AdminSchema.WebhookEvent`,
as `admin.webhooks.getOrganizationWebhookEvents` lists it, and a `when` over an event keeps an
`else`, since the types grow with the contract.

## Testing your own code

Every attempt leaves through an `HttpExchange`, so a test answers it with no network. The
platform's is `SystemExchange`, over OkHttp on the jvm and Android and URLSession on iOS; it keeps
no cookie, follows no redirect and times nothing out of its own, and wrapping it is how an app
watches its traffic, since the client records nothing.

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.Mountaya
import com.mountaya.client.SystemExchange

// A test answers every attempt itself.
val answered =
    object : HttpExchange {
        override suspend fun send(request: ExchangeRequest): ExchangeResponse =
            ExchangeResponse(200, body = ExchangeBody("""{"data": []}""".encodeToByteArray()))
    }
val client = Mountaya(Credential.Publishable("pk_test_0a1b"), exchange = answered)
println(client.studio.activities.getActivities().data.size)

// An app watches its traffic by wrapping the platform's own exchange.
val platform = SystemExchange()
val watched =
    object : HttpExchange {
        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val response = platform.send(request)
            println("$request ${response.status} ${response.header("X-Request-Id")}")
            return response
        }
    }
val app = Mountaya(Credential.Publishable("pk_live_0a1b"), exchange = watched)
app.close()
client.close()
```

An `ExchangeRequest` prints its method and its address alone, never the headers that carry the
credential, and a printed type holding a secret, a session's token, a webhook endpoint's signing
secret, a setup intent's client secret or a share link's token, prints it as `<redacted>`, the value
staying readable. `clock`, `random` and `dispatcher` are seams too: a `Retry-After` date and a token's
expiry are read on the `Clock` you give, every jitter and drawn key comes from the `RandomSource`
you give, and each answer is read and decoded on the `CoroutineDispatcher` you give. Every wait and
every budget runs on the caller's coroutine time, so a test under `runTest` spends none of its own
once it passes its test dispatcher as `dispatcher`, `StandardTestDispatcher(testScheduler)`: the
read then runs on the test's virtual time too, where a read on `Dispatchers.Default` would leave
that time free to run on and spend the attempt's budget before the answer arrived.

An app that watches what each answer was, by operation rather than by address, reads `onResponse`,
told of every attempt an answer came back to:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya

val client =
    Mountaya(
        Credential.Publishable("pk_live_0a1b"),
        onResponse = { report ->
            println("${report.operationId} ${report.status} attempt ${report.attempt} ${report.requestId}")
            println(report.rateLimit?.policies)
        },
    )
client.studio.activities.getActivities()
client.close()
```

The hook is told the operation, the surface, the status, the attempt, whether the answer was
replayed for a key, the request id and the rate-limit state. It does not suspend, and nothing it
does, an exception it throws included, changes what a call answers. A test of your own error
handling builds the failure it means to handle through its class's public constructor,
`NotFoundException()`, and the map and navigation have scripted doubles of their own, below.

## The geo layer

```kotlin
import com.mountaya.client.parseTimestamp
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.ScriptedNavigationEngine
import com.mountaya.geo.TimeLength
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.formatDateTime
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatLngLat
import com.mountaya.geo.routeLengthMeters
import kotlin.time.Instant

// A rule is a function over the wire's own values: a position is [longitude, latitude].
val line = listOf(listOf(6.8652, 45.9237), listOf(6.8752, 45.9287), listOf(6.9271, 45.9682))
val meters = routeLengthMeters(line)

// A value is written for its reader, in their language and their units, never the device's.
val units = UnitPreferences(language = "fr-FR")
println("${formatDistance(meters, units)} ${formatLngLat(line[0])}")

// A time of day is the locale's own, on its own clock, in the zone you name: 오후 1:00 in Korean.
val arrival = Instant.parse("2026-03-14T00:00:00Z")
println(formatDateTime(arrival, "ko-KR", "Pacific/Auckland", timeLength = TimeLength.Short))
// A timestamp held as text is read as the wire's are, the same instant here, and null where it names none.
println(parseTimestamp("2026-03-14T13:00:00+13:00") == arrival)

// In an app's own tests, the scripted engine is the navigation, and the test answers each request.
val navigation = ScriptedNavigationEngine<String>()
navigation.start("Refuge du Couvercle")
navigation.sent[0].answer(NavigationRoute(line = listOf(line[0], line[2]), duration = 5_400.0))
navigation.setPosition(line[2])
println(navigation.state.value.status)
navigation.close()
```

Every rule is a function at its module's top level, named as the contract's other SDKs name it,
and answers the same numbers and words they answer for the same input: the contract's fixtures
prove each one. A rule handed a position that names no place answers rather than throws: a
distance is `NaN`, a printed position `null`. A number, a date and a time of day are written
through the platform's locale services with the locale you name, and the language is always yours
to pass; a number is grouped where the locale's CLDR minimum grouping digits say, on every target,
so a Spanish or a Polish 1234 is `1234`. `formatDateTime` writes an instant's date at a
`DateLength`, its time of day at a `TimeLength`, short or medium, or both, in the time zone you
name, each in the locale's own style and on its own 12- or 24-hour clock whatever the device is set
to, and the long date where you name no length. `parseTimestamp`, the client's, reads a timestamp you
hold as text by the contract's anchor, which every timestamp the wire carries is read by: the instant
it names, or null for a date alone, a time with no zone or a day no month holds. An engine reports its state as a `StateFlow`, which a composable
collects and any other reader reads, and is an `AutoCloseable`, which `use` or your own disposal
closes.

The elements' words, colours and glyphs are the contract's own tables, printed into Geo: the words
in English and French as `WordBundle.elements`, which `resolveBundle` folds your own words over; the
light and dark colours as `ElementTokens.standard`, which you restyle by copying it with the tokens
you change; and the marker and control glyphs as `GlyphSet.marker` and `GlyphSet.control`, whose
markup `parseGlyph` reads into shapes, each painted as `glyphPaint` answers. A glyph set of your own,
a licensed one, is built with `GlyphSet.of` and drawn alone or chained over a bundled set of the same
paint, box and width with `GlyphSet.chaining`; a set that cannot be drawn is refused, as null, where
you build it.

```kotlin
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.WordBundle
import com.mountaya.geo.WordOverrides
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.glyphPaint
import com.mountaya.geo.parseGlyph
import com.mountaya.geo.resolveBundle
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

val close = buildJsonObject { putJsonObject("controls") { put("close", "Fermer la carte") } }
val words = resolveBundle(WordBundle.elements, RestSchema.Language.Fr, WordOverrides.BySection(close))
val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
// A glyph's markup, read into the shapes a view draws, each painted as its set paints it.
val pin = parseGlyph(GlyphSet.marker.markup["map-pin"].orEmpty())
val paints = pin.map { glyphPaint(it, GlyphSet.marker) }
// A licensed set of your own, its missing glyphs drawn from the bundled marker set.
val licensed = GlyphSet.of("licensed", GlyphSet.Paint.Stroke, markup = mapOf("refuge" to "<path d=\"M3 21h18\"/>"))
val markers = licensed?.let { GlyphSet.chaining(listOf(it, GlyphSet.marker)) } ?: GlyphSet.marker
println("${words["controls"]} ${tokens.accent.light.hex} ${paints.size} shapes, ${markers.markup.size} glyphs")
```

The paint a map draws its content in is a value too, restyled on a copy, and a point of interest's glyph
and colour are read off its category:

```kotlin
import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.LayerRank
import com.mountaya.geo.MapPaint
import com.mountaya.geo.poiStyleOf

// Every member you leave is the contract's; a band of your own sits between two of the map's.
val paint =
    MapPaint.standard.copy {
        itinerary.casingColor = "#1b1b1b"
        layerRanks += "acme_avalanche" to LayerRank.Poi.rank + 2
    }
val hut = poiStyleOf(DataSchema.POICategory.Hut)
println("${paint.itinerary.lineWidthStops} ${hut?.glyph} ${hut?.color?.hex}")
```

An element handed an itinerary's id reads it through `ItineraryResourceEngine`, which shows its
geometry, its figures, its legs and its places at once. Answers are kept by a cache you make once per
client, over a scope of yours its reads run in, and hand to every engine, so two elements over one
itinerary read it once:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.geo.ItineraryResource
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItineraryResourceEngine
import kotlinx.coroutines.flow.first

val client = Mountaya(Credential.Publishable("pk_live_0a1b"))
// One cache per client: every element reading through it shares its reads.
val itineraries =
    ItineraryResourceCache(applicationScope) { id, surface ->
        client.studio.itineraries.getItinerary(id) { if (surface != null) this.surface = surface }.data
    }
// An element handed an id shows every projection at once, once its answer lands.
val profile = ItineraryResourceEngine(itineraries, itineraryId = "itn_4e5f")
val resource = profile.state.first { it is ItineraryResource.Resolved }
if (resource is ItineraryResource.Resolved) {
    println("${resource.itinerary.legs.map { it.name }} ${resource.itinerary.waypoints.mapNotNull { it.name }}")
}
// Once your app has saved the itinerary, every element over it reads it again.
itineraries.invalidate("itn_4e5f")
// An engine you let go of is closed, as a composable's `DisposableEffect` closes it.
profile.close()
```

The terrain at a point is read off the Tile API's elevation tiles by rules you can call yourself, the
decoding of a tile's image into its pixels being your platform's codec's, as `MapView`'s readout reads it:

```kotlin
import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile
import com.mountaya.geo.aspectCardinal
import com.mountaya.geo.elevationTilesAt
import com.mountaya.geo.terrainFromElevationTiles

val source = ElevationSource(ElevationEncoding.Mapbox, tileSize = 512, zoom = 14)
val here = listOf(6.8652, 45.9237)
// Each tile's pixels, red, green and blue packed as an Android `Bitmap` hands them.
val tiles = elevationTilesAt(here, source).map { address -> ElevationTile(address, 512, IntArray(512 * 512)) }
val terrain = terrainFromElevationTiles(here, source, tiles)
println("${terrain?.elevation} ${terrain?.slope} ${terrain?.aspect?.let(::aspectCardinal)?.code}")
```

## Navigation

`mountaya-navigation` follows a route on an Android phone, tablet, watch or car. Its engine takes
your route source, where positions come from and what says each maneuver, and runs on your scope,
`viewModelScope` or `lifecycleScope`, whose main thread is the one it is driven from:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Navigation.kt compiles in guided() -->
```kotlin
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.readDirectionsSteps
import com.mountaya.navigation.DeviceLocationSource
import com.mountaya.navigation.DeviceNavigationEngine
import com.mountaya.navigation.SpeechNavigationVoice
import kotlinx.coroutines.launch

// Your words for each instruction, which is a copy key and its arguments, in a voice made up front and
// prepared, so a speech engine that cannot start is told before the first maneuver.
val voice = SpeechNavigationVoice(context, "fr-FR") { instruction -> sentence(instruction) }
viewModelScope.launch { voice.prepare() }

val navigation =
    DeviceNavigationEngine<List<List<Double>>>(
        scope = viewModelScope,
        // Your route source: one Data API request, its steps read into one vocabulary.
        route = { waypoints ->
            val found =
                mountaya.data.directions(
                    DataSchema.DirectionsInput(activity = DataSchema.ActivitySlug.HikingAndTrail, waypoints = waypoints),
                )
            val route = found.routes?.firstOrNull() ?: throw NoRoute()
            val line = route.geometry as? GeoJsonLineString ?: throw NoRoute()
            val steps = readDirectionsSteps(route.steps, coordinateCount = line.coordinates.size)
            NavigationRoute(line.coordinates, steps.steps, route.duration)
        },
        location = DeviceLocationSource(context),
        voice = voice,
    )
navigation.start(listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682)))
```

`DeviceNavigationEngine` is a `NavigationEngine`, so it reports exactly what
`ScriptedNavigationEngine` reports in your tests: the same status, progress and arrival, a
traveller off the route past 50 m, and a new route asked for by `reroute` alone. It is Geo's
navigation with the device beside it, and no navigation library underneath: each maneuver is said
once, where Geo's progress finds it due, at the same metre as in the other SDKs, while the traveller
is on the route, through the voice you give it. The plan it says them from and the prompt due are
Geo's, `guidancePlanOf` and `duePrompt`, so an engine of your own that speaks says each at the same
metre.

Its seams are yours. `DeviceLocationSource` reads the device's satellite position while your app
holds `ACCESS_FINE_LOCATION`, which your manifest declares, and never asks for it, at the rate you
choose: every fix the platform reads for a navigation, a fix a second, by default, or a walker's,
`DeviceLocationSource(context, rate = LocationRate.Walking)`, a fix every two seconds once the device
moved 5 m, which still says a prompt on time at a walker's pace. Its `standing`
says where your app stands on location access, read again each time your app comes back to the
foreground; call its `recheckPermission()` from the result of your own permission request, and hand
it an `onError` to hear of each failure as a `LocationException`, whose `kind` says which: `NoProvider`
for a device with no satellite positioning, `PermissionRevoked`, `ProviderDisabled` for its location
turned off, `PlatformFailed` for any other failure of the platform's reader, carried as its `cause`,
and `BackgroundUndeclared` below. `DeviceLocationSource(context, backgroundUpdates = true)` keeps
reading with the screen off where your merged manifest declares `ACCESS_BACKGROUND_LOCATION` or a
foreground service of type `location`, which your app runs; where it declares neither, the source
tells `onError` `BackgroundUndeclared` as it starts and reads while your app is in use.
`ReplayLocationSource` walks each route followed at a speed you name, on your scope's time, for a
demo, a recording or a test under `runTest`, where you hand it `testScheduler.timeSource`, reading each
position off the measure the route holds, as `positionAtMeters(route, meters)` reads one for a source of
your own;
`FlowLocationSource` states the positions of your own `Flow`, for an app with its own location
stack, handing its `onError` a position the navigation refuses, then reading the next, and a failure
that ends your flow, never your scope; `InertLocationSource`, the default, states nothing, and you hand
each position to `setPosition`. `SpeechNavigationVoice` speaks your sentence for each instruction in
the language you name, or in the `fallbackLanguage` you name where the device holds no voice for the
first, through the platform's text-to-speech as navigation guidance, taking the audio focus while it
does, hands its `onError` a `SpeechException` whose `kind` is `EngineUnavailable` where the device's
speech engine fails to start, once, which `prepare()` waits for, so a voice made up front learns it
before the first maneuver, and `AudioRefused` each time the platform refuses a sentence the audio focus,
which it says anyway, and is yours to `close()` when you are done; `InertNavigationVoice`,
the default, says nothing. A source or a voice given no `onError` drops each failure, never throws it,
and logs it once with `Log.w` where your app is debuggable. The kinds are the ones the Dart and Swift
SDKs name. Your manifest
needs nothing for it: Android 11 and later let an app targeting API 30 or later see the platform's
speech engine only through a `TTS_SERVICE` query, which the library declares and the build merges into
your app's manifest.
Background location and the foreground service a navigation with the screen off runs in are your
app's, with the permissions its review needs.

Asking for location access is your app's too, in its own flow and words, and no module declares a
location permission. An app that reads the device's position, a navigation's or a map's locate,
declares both in its `AndroidManifest.xml`, the approximate one beside the full, since Android 12 asks
for the two together and lets the reader grant either:

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

A position is read under the full one alone: the approximate one places a traveller kilometres from
where they are, so `DeviceLocationSource` reads nothing under it, its `standing` stating the accuracy
as `LocationAccuracy.Reduced`, and a map's locate is refused as `denied`.

On the jvm and in a Kotlin Multiplatform app's shared code, `RouteFollowingEngine` in `mountaya-geo`
is the same navigation over the same route source, with the positions you hand it.

A saved itinerary is followed over all its segments at once: `readItineraryRoute(itinerary.segments,
itinerary.waypoints)` joins them, in chain order, into the one `NavigationRoute` your route source
answers, each arrival but the last said as a waypoint passed on the way, by the name of the itinerary's
waypoint standing within 60 m of where its segment ends. Your words for it are two more keys, which no
directions answer writes:

```kotlin
import com.mountaya.geo.NavigationInstruction
import com.mountaya.geo.NavigationInstructionKey

fun waypointSentence(instruction: NavigationInstruction): String? =
    when (instruction.key) {
        NavigationInstructionKey.Waypoint -> "Passez par ${instruction.name}"
        NavigationInstructionKey.WaypointPlain -> "Passez le point de passage"
        else -> null
    }
```

## The map

`mountaya-elements` shows a map. Its engine holds what the map shows and builds on every target, so
an app's shared code, a desktop app or a server holds a map's state as an Android app does:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.CameraFit
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapLayer
import com.mountaya.elements.MapSource
import com.mountaya.geo.GeoJsonFeatureCollection
import com.mountaya.geo.LayerRank
import com.mountaya.geo.PixelPadding
import kotlinx.serialization.json.JsonPrimitive

val mountaya = Mountaya(Credential.Publishable("pk_live_0a1b"), language = "fr")
val map = MapEngine(mountaya)
map.setTerrain()
// An itinerary by id, the camera framing it once it is drawn; a later call takes its place.
map.setItinerary("itn_4e5f", fit = CameraFit(padding = PixelPadding(48.0)))
// The zones of your credential's scope, each layer at the rank its source layer declares.
map.setZones(true)

map.addSource(MapSource("trail", MapSource.Kind.GeoJson(GeoJsonFeatureCollection(emptyList()))))
map.addLayer(
    MapLayer(
        id = "trail",
        type = TileSchema.TileLayerType.Line,
        source = "trail",
        rank = LayerRank.Poi.rank + 2,
        paint = mapOf("line-color" to JsonPrimitive("#e64980"), "line-width" to JsonPrimitive(4)),
    ),
)
// A member set to a value replaces the one held, JsonNull included; one mapped to null is kept.
map.editLayer("trail", paint = mapOf("line-color" to JsonPrimitive("#228be6"), "line-width" to null))
println(map.state.value.layers.map { it.id })
```

Each call draws in a slot of its own, an itinerary, a collection, the itineraries and the zones of your
scope, or a specification you hold under a name of yours, `setSpecification(specification, name)`, and
the latest call for a slot draws: one that answers after a later call answers false. A call is withdrawn
by cancelling its coroutine, as `LaunchedEffect` does when it leaves the composition, and gives its slot
back to the call it superseded.

On Android, `MapView` draws it, in Compose over MapLibre Native:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Map.kt compiles in TrailMap() -->
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.mountaya.client.Mountaya
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapView

@Composable
fun TrailMap(mountaya: Mountaya) {
    val map = remember { MapEngine(mountaya) }
    LaunchedEffect(map) { map.setTerrain() }
    MapView(map)
}
```

`setTerrain()` shows the first terrain of the Tile API's catalogue, or the one you name, in the variant
`setTerrainVariant` asks for, light until you ask for dark, and `setStyle` shows a style of your own.
Everything the map draws is drawn again on the base map a switch brings in, a layer you added by hand
included, and a base map that does not arrive leaves the one shown. The labels speak the language you
give the engine, else your client's, the one it sends as `Accept-Language`,
else the device's. Every request the map makes for itself, for its style, its tiles, its sprites and
its glyphs, is given your client's credential and language and the SDK's name where it goes to the
Tile API, and nothing where it goes to any other host a style names; the engine's `transformRequest`
rewrites a request before that, and a header it sets stands. Those requests are MapLibre's own: a
tile asked for before a first session token lands goes out under the key alone, and MapLibre repeats
a failed tile on its own schedule. MapLibre keeps one HTTP client for the process, which every map on
screen shares: each map's requests to its client's Tile API carry that client's credential, given again
at each hop, so a redirect away from the Tile API carries none, and two maps whose clients reach one Tile
API under two credentials send the credential of the map shown last. An app that wants its own
`OkHttpClient` under the map, a cache, a proxy or a pinned certificate, hands it to `MapView`'s
`okHttpClient` rather than to MapLibre.

A map reads its organization once it is drawn, through `postOrganizationLookup` with your publishable
key in the body and no credential sent, once per key and Map Studio API origin for the process and shared by
every map whose client holds that key and sends to that origin, so a staging client never shares an answer
with production, and you can read it too. Its answer decides one thing a map draws: the Mountaya watermark in
the top-left corner of the map's safe area, below the status bar and any display cutout of a map you draw edge to
edge, drawn unless the organization holds white label, once the read answers, where it fails, once it has waited
one attempt's time budget, 35 seconds by default, while it goes on, and at once where your client holds no
publishable key, linked to Mountaya's site in the scope's language, opened in the device's browser, and which
nothing you pass hides. The safe area is the window's own, its root insets as the system reports them, whatever
your layout consumes: the system bars and a display cutout, never the on-screen keyboard, so nothing of the map's
moves as the keyboard comes and goes. The mark stands at your reader's top left in a layout that runs right to left
too. The SDK publishes the frame's extent so you keep your own views over the map clear of it: `watermarkWidth`, 180 dp,
`watermarkHeight`, 180 × 58 / 138 dp, and its inset from the safe area's corner, `watermarkInset`, -4 dp, so the
frame ends `watermarkClearance`, about 72 dp, below the safe area's top, where Trail's search box starts and below
which Trail's sheet stops. Nothing of the SDK's own covers it: a `MapControls` stack keeps to the safe area in
whichever corner you put it and moves clear of the frame where it would reach over it, below it in the top-left
corner, its panels open beside their toggles inside the safe area and below the frame where they would reach over
it, the readout stands at the point you press, flipped to stay in the map and moved clear of the frame, and the
label a tapped marker shows, beside its pin toward the end its text runs to, keeps to the safe area and moves clear
of the frame the same way, drawn above the mark as the mark is drawn above every pin. The map, its safe
area and the frame they keep to are the map's own, which `MapView` publishes on its engine, so a stack you lay over
more than the map, the map below a header of yours, still keeps to the map; a stack you hand no map, and a
`PanelControl` you place in no stack, keep to the safe area of the space they are laid in and hold the frame at its
top left as a waiting read does. The frame is kept clear from the first draw, while the read waits too, so nothing
moves as the mark lands, and is released only once the read answers white label. `map.organizationRead` says where
the read stands, a `StateFlow` in the five states the contract names in every SDK, `OrganizationRead.Waiting`,
`OrganizationRead.Answered(whiteLabel)`, `OrganizationRead.Failed`, `OrganizationRead.TimedOut` and
`OrganizationRead.NoKey`, and `isWatermarkKeptClear(read)` answers whether the frame is held at a read, so you keep
your own views clear of the frame as the SDK keeps its own. Nothing else
is read from the organization: the controls, the units, the language, the base map and the accent are yours, else
the SDK's own, so what a map renders is read off your code and never changes from afar:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.elements.MapEngine
import com.mountaya.geo.isWatermarkKeptClear
import com.mountaya.geo.watermarkClearance
import com.mountaya.geo.watermarkInset
import com.mountaya.geo.watermarkWidth

val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
// Null where the client holds no publishable key; a read that fails throws, and the next load reads again.
val organization = map.loadOrganization()
println("${organization?.name} ${organization?.white_label} ${map.organization?.white_label}")
// Where the read stands now; collect the flow to be told each move. Only white label releases the frame.
val keepsClear = isWatermarkKeptClear(map.organizationRead.value)
// The frame's extent from the safe area's top-left corner, in dp: a view of yours starts right of it or below it.
println("Keep clear: $keepsClear, right of ${watermarkInset + watermarkWidth} or below $watermarkClearance")
```

A long press, or a secondary click, opens a readout of the point: the point of interest and the zone the
map draws there and the band of the classic overlay at any zoom, the terrain's altitude, slope and aspect
read off the elevation tiles from zoom 13, through your client's Tile API under the base map's credential,
and the coordinates with an action that copies them, its figures in the SDK's default units, the metric
system, a speed, degrees and Celsius, in the scope's language, the aspect beside its word, `123° · South-East`.
A point of interest reads its OpenMapTiles `subclass`, else its `class`, and its `name`; a zone its name, its
description, its level and its window, a time the tile writes as a string read as none. A moment is written in the
device's own time zone, and none is written where the platform knows no zone of that name, never a UTC time in its
place. While the card is open its moment follows the instant the map pins, and the band stays as
it was read at the press. A tap or a camera move closes it, and
`MapView(map, readout = false)` turns it off; the press is told to the engine's `events` either way.

The layers catalogue lists every terrain, overlay and enrichment your credential draws, read once and
shared. A map draws one classic overlay at a time and stacks enrichments over it; your word on a layer,
or on every layer of one concept, is kept by its id before the layer is drawn and across a base-map
switch; and an itinerary's restricted stretches, striped in their kind's colour beside its line, are
shown until you hide them:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.elements.MapEngine

val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
val catalogue = map.loadLayers()
println(catalogue.terrains.map { it.slug })
map.setOverlay("snow-depth")
map.addEnrichment("avalanche-risk")
map.setLayerVisibility("avalanche-risk-fills", visible = false)
for (key in map.drawnLayerKeys()) if (key == "labels") map.setKeyVisibility(key, visible = false)
map.setAccessRestrictionsVisibility(false)
```

A classic overlay the server stamps with a time is drawn for the instant you pin, its tiles asked again
in place as the pin moves, for an overlay drawn later too, and kept over a base-map switch; with none
pinned the map draws the manifest's own addresses, which the Tile API answers for the moment each tile
arrives, so a device whose clock is wrong still draws the present. Enrichments are never stamped. The geo
layer lays out the slider a layers panel draws, around a clock reading you hand it, one stop an hour from
71 hours back to 144 ahead:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.elements.MapEngine
import com.mountaya.geo.timeSliderReadout
import com.mountaya.geo.timeSliderRelease
import com.mountaya.geo.timeSliderWindow
import kotlin.time.Clock

val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
map.setOverlay("snow-depth")
val window = timeSliderWindow(Clock.System.now())
val sixHoursAhead = window.nowValue + 6
map.setInstant(timeSliderRelease(window, sixHoursAhead)?.pin)
val readout = timeSliderReadout(window, sixHoursAhead)
println("${readout.word} ${readout.count}")
map.setInstant(null)
```

A Data API result is drawn under an id of its own, asked through your client or handed over as you
already hold it; results of one kind accumulate until you remove or clear them, and an edit asks again
with the input your patch answers, a later edit or a removal cancelling a query still out. Three geometry
slots draw over everything else: the preview of an itinerary being planned and the emphasis of the leg a
reader points at, just above the itinerary's line, and the highlight of what the reader picked, above
everything:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.Mountaya
import com.mountaya.client.generated.DataSchema
import com.mountaya.elements.GeometrySlot
import com.mountaya.elements.MapEngine

val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
val reach =
    map.addIsochrones(
        DataSchema.IsochronesInput(
            DataSchema.ActivitySlug.HikingAndTrail,
            listOf(listOf(6.8652, 45.9237)),
            listOf(1_800.0, 3_600.0),
            DataSchema.RangeType.Time,
        ),
    )
// A patch answers the input to ask with, built from the one held.
map.editIsochrones(reach.id, patch = { DataSchema.IsochronesInput(it.activity, it.locations, listOf(900.0), it.rangeType) })
map.removeAnalysis(reach.id)

map.setGeometry(GeoJsonPoint(listOf(6.8652, 45.9237)), GeometrySlot.Highlight)
map.clearGeometry(GeometrySlot.Highlight)
```

Markers stand above everything the map draws, and the camera can be held to a pan lock and a zoom
range; a command handed something a map cannot take throws a `MapException` that says what to pass
instead, and leaves the map as it was:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapMarker
import com.mountaya.elements.ZoomLimit

val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
map.addMarker(MapMarker("hut", listOf(6.8652, 45.9237), glyph = "hut", label = "Refuge"))
// A member left null keeps the one the marker holds, and the marker keeps its place.
map.editMarker("hut", color = "#e64980")
// [west, south, east, north]: what the map shows stays within it, and null lifts it.
map.setMaxBounds(listOf(6.7, 45.8, 7.0, 46.0))
// A limit left out is kept, and a released one goes back to the renderer's own, 0 and 25.5.
map.setZoomBounds(min = ZoomLimit.Zoom(8.0), max = ZoomLimit.Zoom(17.0))
map.setZoomBounds(max = ZoomLimit.Released)
```

A waypoint is placed by an edit: each click on the map places it, and a click within 40 dp of an
active segment, such as the itinerary's line, places it on the line:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.elements.MapEngine
import com.mountaya.elements.ScreenProjection
import com.mountaya.elements.WaypointEdit

val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
map.setActiveSegments(listOf(listOf(listOf(6.85, 45.83), listOf(6.87, 45.83))))
map.beginWaypointEdit("w1")
// MapView places it at each click; a map drawn some other way hands its own projection.
map.placeWaypoint(listOf(6.86, 45.83), ScreenProjection(point = { it }, position = { it }))
val placed: WaypointEdit? = map.endWaypointEdit()
println(placed?.position)
```

`MapView`'s `onClick` hands you each click, the place and the marker it fell on, where it fell on
one, and shows that marker's label beside it; a double tap zooms and is no click. `onCameraChange`
hands you the camera each time it comes to rest, after the reader moved it or you did, and never frame
by frame:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Map.kt compiles in Selecting() -->
```kotlin
MapView(map, onClick = { tap -> picked = tap.marker }, onCameraChange = { camera = it })
```

The camera moves by `jump`, `ease` and `fly`, each stating only the members it changes, frames a box
by `fitBounds`, a fit this SDK computes, facing north at the highest zoom that fits the canvas less
its padding, and steps by `zoomIn`, `zoomOut` and `resetNorth`, which turns the pitch back to 0 too. A
point query, `featuresAt`, `poisAt`, `zonesAt` or `itinerariesAt`, answers the features the map itself
drew under a point, never its base map's, every one or those of the points of interest, the zones or
an itinerary, each with the layer that drew it, and `elevationAt` asks the Data API for the ground's elevation rather than
reading the drawn terrain. What happens on the map is told by `events()`, a `MapEvent` of each kind: a
click, `Click`, a long press, `LongClick`, the features a click lands on, `FeatureClick`, and those a
mouse or a stylus hovers over, `FeatureHover`, a refusal of access and its end, `AccessRefused` and
`AccessRestored`, a failed request, `Failure`, and a change of the layers catalogue, `LayersChange`,
carrying the catalogue the map holds after it, null where a move of the studio scope dropped it:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.elements.CameraFit
import com.mountaya.elements.CameraTarget
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.geo.PixelPadding
import kotlinx.coroutines.launch

val map = MapEngine(Mountaya(Credential.Publishable("pk_live_0a1b")))
// A move states the members it changes; the bearing and the pitch here are kept.
map.ease(CameraTarget(center = listOf(6.8652, 45.9237), zoom = 13.0))
map.fitBounds(listOf(6.8, 45.8, 6.95, 45.9), CameraFit(padding = PixelPadding(48.0), maxZoom = 15.0))
map.zoomIn()
// What the map drew under a point of the view, in density-independent pixels, topmost first: every
// feature with the layer that drew it, the points of interest alone, and the itineraries alone.
val point = map.project(listOf(6.8652, 45.9237))
println(map.featuresAt(point).map { it.layer })
println(map.poisAt(point).size)
println(map.itinerariesAt(point).map { it.properties["segment_id"] })
println(map.elevationAt(listOf(6.8652, 45.9237)))
// What a credit button lists: Mountaya first, then each source the map draws.
println(map.credits)
applicationScope.launch {
    map.events().collect { event ->
        when (event) {
            is MapEvent.FeatureClick -> println(event.features.map { it.layer })
            is MapEvent.AccessRefused -> println("${event.status} at ${event.url}")
            is MapEvent.LayersChange -> println(event.layers?.terrains?.map { it.slug })
            else -> Unit
        }
    }
}
```

A refusal of access, a 401, a 402 or a 403 to one of the map's own requests, stands until a tile of
its source loads, or the next style for one that names no source, and `state.isAccessRequired` reads
whether one stands: a notice you raise on it comes down by itself. A 401 or a 403 answering a request
that left under a credential your client no longer sends, a session token minted or replaced since, or
a visitor's cookie changed, is told to nobody, however late it arrives, and a style refused so is asked
for again under the credential your client sends now. A style whose document does not load, a `200`
MapLibre cannot read among them, is told as a `MapEvent.Failure`, and the map goes back to the one it
showed, which ends no refusal as it loads again: a switch refused stays refused until a style you ask
for loads. A request that never left because your client's credential could not be read for it, your
token's function throwing, is a `MapEvent.Failure` whose `cause` is what the read threw. `locate()`
frames the device's position at zoom 16 at most, the box its accuracy spans, and throws a `MapException` naming the
reason where there is no fix. It never asks the reader for location access: your app asks in its own
flow, and until it holds `ACCESS_FINE_LOCATION`, declared as [Navigation](#navigation) shows, a locate
is refused as `denied`. `MapView` hands the
engine the device's location services, `DeviceGeolocation`, where you gave it no geolocation of your
own, and `InertGeolocation` turns locating off. In your tests, `ScriptedMapSurface` draws a map
nowhere: it answers what a renderer would, records each move and fit, and plays clicks, hovers,
failed requests and loaded tiles, which the engine tells as `MapView`'s renderer has it tell them.

`MapView`'s `onReady` hands you MapLibre's own `MapLibreMap` once its first style is loaded, for
anything the view does not wrap. `mountaya-elements` exposes MapLibre for exactly that, within the
range it is tested on, so your build declares none of it. The watermark is a view of this SDK's own above
MapLibre's canvas, so a snapshot you take through `MapLibreMap.snapshot` records the canvas without it, and
Mountaya's terms of use govern what you do with one.

A map draws the pins its style names, an itinerary's waypoints and a map's points of interest, and its
markers' pins from the engine's glyph set: the bundled marker set, or the one you hand it,
`MapEngine(mountaya, glyphs = markers)`. A waypoint's pin is a disc of its colour with its glyph in
white, and a glyph the set cannot draw leaves its pin missing rather than blank. A marker's pin is a
disc 28 dp across in the marker's colour, the tokens' accent where it names none, ringed in white, with its
glyph in white, or a white dot where the set cannot draw it. `Glyph` draws one
glyph in Compose, from the control set of the scope around it unless you name another, the bundled
one where no `MountayaScope` states one, in the controls' ink, the tokens' `control-fg` under the
scheme in force, unless you pass a colour, as you would beside text of your own theme, since Compose's
foundation keeps no colour of the content around it:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Map.kt compiles in Glyphs() -->
```kotlin
Glyph("zoom-in")
Glyph("hut", set = GlyphSet.marker, size = 24.dp, color = Color(0xFF1C7ED6), label = "Refuge")
```

### Controls and panels

`MapControls` floats the map's controls in one of its corners, top right unless you name another, as
the reader sees it whichever way the text runs, inside the map's safe area and clear of the watermark's frame,
so a map drawn edge to edge needs no padding of yours around the stack. From the top it reads the zoom pair, the layers,
points-of-interest and preferences toggles as one group, your own controls, geolocate and the compass.
Each flag has a fixed default, which one you leave out takes and one you state, true or false, overrides:
the zoom, the compass, the layers and the preferences show, and the points of interest, geolocate and the
time only where you turn them on. None is read from your organization. Each drives the map unless you hand a
handler in its place, and
`enabled = false` shuts them all, yours among them. A toggle opens its panel in place of any other, so
one is open at a time, yours included, and you can hoist which one is open. The stack hands its layers
panel what you hand it: `offersTime = true` lists the time slider, and `thumbnail` each card's picture,
as `LayersPanel` takes them:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Stack.kt compiles in TrailControls() -->
```kotlin
Box(Modifier.fillMaxSize()) {
    MapView(map, Modifier.fillMaxSize())
    MapControls(
        map,
        zoom = true,
        compass = true,
        geolocate = true,
        layers = true,
        pois = true,
        preferences = true,
        openPanel = open,
        unitPreferences = units,
        offersTime = true,
        thumbnail = { _, slug -> pictures[slug] },
    ) {
        DownloadControl(onClick = { exporting = true })
        PanelControl(MapPanel("filters"), label = "Filters", glyph = "preferences", help = "Huts only") {
            BasicText("Huts only")
        }
    }
}
```

Zoom in is disabled at the camera's zoom ceiling and zoom out at its floor. Geolocate never asks the
reader for access: it reads the standing permission without prompting, says location is blocked while
it answers refused, and is drawn in the accent until every fix it asked for settles. Download and
directions drive nothing of the map's, so their `onClick` is required. Every button is 48 dp across,
Android's touch target, and every control, row, chip, clear and retry draws a ring 2 dp wide in the
tokens' `focus-ring` while it holds the keyboard's focus, and none where a touch pressed it. A camera move a control starts, a zoom step, a reset of north, a locate or a
search's pick, jumps where the reader turned animations off, Android's animator duration scale at 0.
A panel the stack opens is laid out as the embed's: 340 dp wide, the width its corner leaves on a map no
wider than 576 dp, no taller than the map leaves it below its top, under a title row that stays while its
sections scroll, its layer cards three to a row and its preferences two. The panels draw the same
models the stack's do, so you can place one where you like:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Stack.kt compiles in Panels() -->
```kotlin
LayersPanel(selection, isLocked = { it == "avalanche-risk" }, onLockedClick = { slug -> upsell(slug) })
PreferencesPanel(units, onPreferencesChange = { units = it }, map = map)
PoiPanel(map, groups = listOf(PoiPanelGroup("water", "Water", listOf("spring", "fountain"))))
```

A layers panel lists the catalogue as `LayersSelectionEngine` holds it, which drives the map, in the map
studio's four sections: the base maps as picture cards, the enrichments as switches, the points of interest
being `PoiPanel`'s, the classic overlays as picture cards one category at a time behind a segmented switch,
and the time, where you offer it. `thumbnail` hands a card its picture by its choice and its slug, and a
card shows its name alone where you hand none. `offersTime = true` lists the time slider: a track ticked
every day and labelled at -2d, now, +2d and +5d, filled from now to its thumb, whose release pins the
instant of its stop through the selection, `setInstant`, or none on now, and the map draws the overlay the
server stamps for it. Offer it only where your organization's overlay time control allows it: the Tile
API refuses a publishable key or a share token a stamped tile asked more than 15 minutes from now. The
slider reads the clock `LocalMountayaClock` provides, the system's unless you provide another, so a replay
moves its window, and writes its moment in the scope's locale and the device's time zone. Your own
selection target gains `setInstant`, a breaking change before 1.0, which a map with no time-aware overlay
answers doing nothing:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Stack.kt compiles in Panels() -->
```kotlin
val pictures = remember { mapOf("topo" to ColorPainter(Color(0xFF2F9E44)), "slopes" to ColorPainter(Color(0xFFE8590C))) }
CompositionLocalProvider(LocalMountayaClock provides replay) {
    LayersPanel(selection, offersTime = true, thumbnail = { _, slug -> pictures[slug] })
}
```

A preferences panel hands you your set back with one
preference changed, and relabels the map for a language; a points-of-interest panel offers the concepts
the map draws. The words and the glyph sets come from the scope around a composable, which
`MountayaScope` nests, and the colours from `LocalMountayaTokens`, read under the colour scheme in
force: the one a scope states, `MountayaScope(scheme = ThemeScheme.Dark)`, else the configuration's
night mode, so a dark scheme draws the tokens' dark values and a value you state stands in both. `MapView` draws its markers in the accent of the same tokens. A composable of
your own reads the scheme in force with `themeSchemeInForce()`, so what it draws beside the controls takes
their tokens under their scheme:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Map.kt compiles in Glyphs() -->
```kotlin
MountayaScope(scheme = ThemeScheme.Dark) {
    val ink = themeTokenValue(LocalMountayaTokens.current.controlFg, themeSchemeInForce())
}
```

Every control, panel, the stack, the search box, the profile, the two tables and the card also take
`strings` of their own: words by their path, `controls.zoomIn`, that stand over the scope's for that
composable and everything it draws, a stack's reaching the controls it holds and a control's own
standing over the stack's, so you rename one control without a scope of its own. A word they do not
name is the scope's:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Stack.kt compiles in OwnStack() -->
```kotlin
MapControls(map, zoom = true, strings = mapOf("controls.zoomIn" to "Plus")) {
    ZoomControl(map, strings = mapOf("controls.zoomOut" to "Moins"))
}
```

Every rule under the composables is Geo's, so a screen of your own reads the same words, tokens and models:

```kotlin
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.countedText
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.scopeOf
import com.mountaya.geo.themeTokenValue
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// A scope nested in another takes every setting it does not state from it.
val french = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-CH"))
val tokens = ElementTokens.standard.copy { accent = ThemeToken(light = HexColor("#e9501e")) }
val results = JsonObject(mapOf("one" to JsonPrimitive("{count} résultat"), "other" to JsonPrimitive("{count} résultats")))
println(french.word("controls.zoomIn"))
println(themeTokenValue(tokens.controlBg, ThemeScheme.Dark).hex)
println(countedText(results, 1_234, RestSchema.Language.Fr))
```

### Search

`SearchControl` is a search box over Mountaya's unified search, with its results under it, a list
that composes the rows it shows as it scrolls. It reads
the Data API through your map's client, starts confined to the visible area of the map, and moves the
camera onto a row you pick, drawing it on the map in the accent of your tokens. Its text and its scope
are `MutableState` you may hoist, kept in step with the search both ways:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/SearchBox.kt compiles in TrailSearch() -->
```kotlin
Box(Modifier.fillMaxSize()) {
    MapView(map, Modifier.fillMaxSize())
    SearchControl(map, Modifier.padding(16.dp), query = query, isConfined = confined)
}
```

A round is asked once the typing pauses, 150 ms, `SearchPause.standard`, one request per branch: itineraries, places, ways,
points of interest, zones and collections, their sections in that order whatever order they answer
in, or the branches you list in yours. A new query abandons the round in flight, whose answers land
nowhere, and a failed branch shows a line of its own, the elements' word for a failed search beside its
retry, never the API's message, which stays on the failure for you to log, while the others answer. A round
confined to the visible area that finds nothing asks once more everywhere, and says so; while the list
is open, the camera coming to rest asks the round again. A pick frames a result's extent, else flies to
its point, and the map's search highlight marks it: a place as a pin, a point of interest as its
category's own, a way as its line, its extent or a pin. The control takes the highlight down as it
leaves the composition. `onSelect` takes a pick over; `fetch` sends the box's requests through a
transport of yours, in place of your map's client; `wording`, a `SearchWording`, words a row's title
and second line, your word for each wire value and your units; and `onError` is handed each branch's
failure, which the list shows a line for, with its retry, either way. A row's title comes cut at what
the query matched, its `parts`, ready to draw. The list shows while the focus stands anywhere in the control, the
box, its filter chips or the list, over a query that is not white space alone, until a pick dismisses it, as Geo's
`isSearchListShown` decides.

The box holds a `SearchEngine` of its own unless you hand it yours, `engine`, which it then renders and
drives and never configures or closes: your engine's configuration holds, so `targets`, `language`,
`limit`, `wording`, `fetch` and `onError` are the box's own engine's alone. A `SearchEngine` starts
unconfined, where the box's own starts confined to the map, so hand yours `isConfined = true` for the
box's default. Where you hoist neither the text nor the scope, the box shows your engine's own and
follows it, a query you hand the engine reaching the box. Its own `strings` stand over the scope's
words, a count among them as a template its `{count}` fills:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/SearchBox.kt compiles in AppEngineSearch() -->
```kotlin
val search =
    remember {
        SearchEngine(
            applicationScope,
            SearchConfiguration(fetch = SearchFetch { input -> mountaya.data.search(input) }),
            isConfined = true,
        )
    }
DisposableEffect(search) { onDispose { search.close() } }
SearchControl(map, Modifier.padding(16.dp), engine = search, strings = mapOf("search.resultCount" to "{count} huts"))
```

A pause of your own is a `SearchPause`, `SearchPause.of(200.milliseconds)`, which answers null for one
below zero, so no search waits one. The engine and every rule under the control are Geo's, so a list
of your own reads the same search:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.SearchConfiguration
import com.mountaya.geo.SearchEngine
import com.mountaya.geo.SearchFetch
import com.mountaya.geo.SearchFilter
import com.mountaya.geo.dataLanguageFor
import com.mountaya.geo.pickTarget
import com.mountaya.geo.searchListState
import kotlinx.coroutines.flow.first

val mountaya = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))
// The engine waits for the typing to pause, asks one request per branch and keeps the latest.
val search =
    SearchEngine(
        applicationScope,
        SearchConfiguration(
            fetch = SearchFetch { input -> mountaya.data.search(input) },
            language = dataLanguageFor("fr-CH"),
            targets = listOf(DataSchema.SearchTarget.Pois, DataSchema.SearchTarget.Places),
        ),
    )
search.setQuery("refuge")
val settled = search.state.first { it.isSettled }
search.close()
// What a list shows under a filter, each title with what matched in brackets, and what picking a row
// does to a map.
for (row in searchListState(settled, SearchFilter.Places).rows) {
    val title = row.parts.joinToString("") { part -> if (part.match) "[${part.text}]" else part.text }
    println("$title: ${if (pickTarget(row.payload).mark == null) "unmarked" else "marked"}")
}
```

A `SearchEngine` starts unconfined, asking everywhere, where the box's own starts confined to its map:
a list of your own over a map hands it `isConfined = true` and states the map's camera as it rests,
`setCamera`. It marks a pick on the map as the box does, with
`map.setSearchHighlight(pickTarget(row.payload).mark, accent)`, handing the accent your tokens read under
the scheme in force, `themeTokenValue(LocalMountayaTokens.current.accent, themeSchemeInForce())`: left
out, the accent is the standard light one, whatever tokens you restyled and whatever scheme is in force.

### Elevation profile and tables

`ItineraryProfile` is an itinerary's elevation profile: the stat strip over it, the curve with its
places pinned on it and their services above them, and, as you ask, the elevation and distance
scales, a band strip, a chip per leg, the card at the cursor and a navigator over the window shown.
It reads the itinerary by its id through an `ItineraryResourceCache` you make once per client, one
read for every element over the itinerary, and a scrub over it pins a marker on your map at the
sample and lights the leg under it, the later leg at the meter two share. A scrub draws the cursor
again and not the curve, and the distance scale and the chips grow with the reader's font scale
rather than clipping. A leg's chip is a button where you listen, `onSegmentClick`, handed the leg's
id as the segment table hands it, a collapsed group's `group:<id>`; it writes nothing to your map,
what it lights being yours to decide:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in ItineraryScreen() -->
```kotlin
val itineraries =
    remember {
        ItineraryResourceCache(applicationScope) { id, surface ->
            mountaya.studio.itineraries.getItinerary(id) { if (surface != null) this.surface = surface }.data
        }
    }
Column {
    MapView(map, Modifier.fillMaxWidth().height(320.dp))
    ItineraryProfile(
        itineraryId = "itin_abc",
        cache = itineraries,
        modifier = Modifier.fillMaxWidth().height(220.dp),
        map = map,
        parts = setOf(ProfilePart.Amenities, ProfilePart.Tooltip, ProfilePart.DistanceAxis, ProfilePart.Navigator),
    )
}
```

Your cache reads through your client, so the elements never call the network themselves.
`ItinerarySegmentTable` states the legs as a roadbook, a row per leg with the itinerary's
sparkline, its length, its climb and loss over the running totals, and, as you ask, its altitude
band, its duration and its difficulty. `ItineraryWaypointTable` states the places, a row per pass,
with a chip per service over the rows that keeps the places offering every service picked. A row
pointed at pins the marker at the leg's end, or at the place, and a segment row lights its leg.

A cut-off, and a `date` field, are written only by the `formatCutoffAt` you hand, the time zone
being your app's, and an element handed none writes neither. Write one with `formatDateTime` in the
device's zone, and hand none where the platform knows no zone of that name, `formatDateTime`
answering null, as Trail's `cutoffWriter` does: never the instant in UTC, as `Instant.toString()`
writes it, and never an empty word in its place:

```kotlin
import com.mountaya.geo.DateLength
import com.mountaya.geo.TimeLength
import com.mountaya.geo.formatDateTime
import kotlin.time.Instant

/**
 * cutoffWriter answers how a cut-off is written for a reader of [language], its date and its time of
 * day in [zone], or null where the platform knows no zone of that name, so no cut-off is written.
 */
fun cutoffWriter(
    language: String,
    zone: String,
): ((Instant) -> String)? {
    // Whether the platform knows a zone does not hang on the instant, so one reading answers for every cut-off.
    formatDateTime(Instant.fromEpochSeconds(0), language, zone) ?: return null
    return { cutoff ->
        checkNotNull(formatDateTime(cutoff, language, zone, DateLength.Medium, TimeLength.Short)) {
            "The platform knew the zone $zone when the writer was made."
        }
    }
}
```

Handed one, the waypoint table writes each cut-off in the scope's locale and the device's zone,
`TimeZone.getDefault()`:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in ItineraryScreen() -->
```kotlin
val locale = LocalMountayaScope.current.locale
val zone = TimeZone.getDefault().id
val writeCutoff = remember(locale, zone) { cutoffWriter(locale, zone) }
Column(Modifier.verticalScroll(rememberScrollState())) {
    ItinerarySegmentTable(itineraryId = "itin_abc", cache = itineraries, map = map)
    ItineraryWaypointTable(
        itineraryId = "itin_abc",
        cache = itineraries,
        map = map,
        formatCutoffAt = writeCutoff,
    )
}
```

Each element also reads through an `ItineraryResourceEngine` of yours, handed in place of the id, the
cache and the surface, which it reads and never closes, the itinerary chosen by your engine's
`configure`, and each takes `strings` of its own:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in OverTheAppsEngine() -->
```kotlin
val engine = remember { ItineraryResourceEngine(itineraries, itineraryId = "itin_abc") }
DisposableEffect(engine) { onDispose { engine.close() } }
ItineraryProfile(engine, Modifier.height(220.dp), map = map, strings = mapOf("profile.title" to "Elevation"))
ItinerarySegmentTable(engine, map = map)
```

A profile and the two tables over one map never move or take down each other's marker, each
pinning under an id of its own, and each takes down what it wrote as it leaves the composition. A leg
your map's cursor hovers lights its row in the segment table, a collapsed group's row for each of its
members, and pins nothing; the row you name, `highlighted`, is lit in its place.
`onHover` takes the map over, handed the sample or the row, and `enabled = false` draws an element
and drives nothing. A window and a choice of services are `MutableState` you may hoist.

`WaypointCard` is a card about one place: its mark and its name, the line under it, then its
services, when it can be used, how to reach it, its own fields, its links and its photos, each
section drawn only where the place states a line. A website or a booking page is a link only at a
web address, so no script is ever opened. Compose's foundation loads no image, so each photo is
drawn by the composable you hand, your own image loader's, and is linked where you hand none:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in PlaceCard() -->
```kotlin
WaypointCard(place, formatCutoffAt = writeCutoff, photo = { photo -> BasicText(photo.caption ?: photo.url) })
```

A link the card draws, a phone, an email, a web address or a photo where you hand no `photo`, opens
through Compose's `LocalUriHandler`, the browser or the app the device opens it in. Provide your own
around the card to take every link over, to open a page in your app, ask first, or note the tap:

<!-- kotlin:skip Android code, which consumer/src/androidMain/kotlin/com/example/consumer/Roadbook.kt compiles in LinkedPlaceCard() -->
```kotlin
val links =
    remember {
        object : UriHandler {
            override fun openUri(uri: String) {
                opened = uri
            }
        }
    }
CompositionLocalProvider(LocalUriHandler provides links) {
    WaypointCard(place)
}
```

Every figure the elements draw is a rule of Geo's, so a screen of your own states the same rows,
the same chips and the same bands, in the reader's language and units:

```kotlin
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.geo.ResolvedItinerary
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.buildElevationSeries
import com.mountaya.geo.formatAltitudeRange
import com.mountaya.geo.formatDistance
import com.mountaya.geo.itinerarySegmentRows

val client = Mountaya(Credential.Publishable("pk_live_0a1b"))
val itinerary = client.studio.itineraries.getItinerary("itn_4e5f").data
// The projections every data element reads, and the rows a segment table states.
val resolved = ResolvedItinerary(itinerary)
val series = buildElevationSeries(resolved.geometry)
val units = UnitPreferences("fr")
for (row in itinerarySegmentRows(series, resolved.legs)) {
    val band = formatAltitudeRange(row.metrics.altitudeMin, row.metrics.altitudeMax, units)
    println("${row.name}: ${formatDistance(row.metrics.sectionDistance, units)}, $band")
}
client.close()
```

## Licence

`mountaya-client`, `mountaya-admin`, `mountaya-geo`, `mountaya-elements`, `mountaya-navigation` and
every module this repository declares later are published under the **Mountaya SDK License**,
version 1.0, in [LICENSE.txt](LICENSE.txt), with a French translation in
[LICENCE.fr.txt](LICENCE.fr.txt). Where the two differ, the English text prevails. The licence is
source-available, not open source: you may read the code and ship it inside your own application, in
conjunction with the Mountaya services and for as long as an agreement with Mountaya is in force.
The texts are the ones the TypeScript packages ship, carried verbatim, and every module's published
metadata names the licence by the address of its English text,
[mountaya.com/en/legal/sdk-license](https://www.mountaya.com/en/legal/sdk-license/). MapLibre
Native, which `mountaya-elements` carries, is under its own BSD licence.

The glyph sets `mountaya-geo` publishes hold the markup of icons from [Lucide](https://lucide.dev),
under the ISC licence, and, for the icons Lucide derives from Feather, the MIT licence.
[THIRD-PARTY-NOTICES.txt](THIRD-PARTY-NOTICES.txt) carries both texts, as every jar and AAR of
`mountaya-geo` does at `META-INF/mountaya-geo/THIRD-PARTY-NOTICES.txt`.

The examples under [examples](examples), Trail among them, are MIT licensed
([examples/LICENSE](examples/LICENSE)), the licence sdk-typescript's examples carry, so you can copy them into your own
code freely.

## Requirements

What working on this repository needs. Using the SDK needs none of it: see [Install](#install).

| Tool | How it is installed | What needs it |
| --- | --- | --- |
| Java, Gradle | pinned in `mise.toml` | Everything. `./gradlew` is the wrapper over that Gradle |
| Android SDK | its command-line tools pinned in `mise.toml`; the platform and build tools by `mise run android:packages`, the emulator by `mise run android:emulator` | The android target, which every run builds unless `-Pmountaya.targets` leaves it out, the device suites, and the app-size gate's `apkanalyzer`, which the command-line tools ship |
| Xcode, with its `xcodebuild` | installed by hand, on macOS, at the version `mise run toolchain:check` asserts, Xcode 27.2 | The iOS targets, which a run on macOS builds unless `-Pmountaya.targets` leaves them out, and `mise run toolchain:check` |
| git, curl | with the operating system, git with Xcode's command line tools | `mise run release`: the tag, its push, and the questions it asks Maven Central, the Central Portal, Entire and the GitHub mirror |
| lsof | with the operating system | `mise run clean:toolchains`, which asks it whether a running process holds a path open before deleting it, and keeps that path |

## Setup

```sh
sh .agents/setup           # Entire, mise and the pinned tools, the git hooks, the dependencies
sh .agents/setup --check   # what is missing, changing nothing
```

## Commands

| Command | What it does |
| --- | --- |
| `mise run lint` | `ktlintCheck` |
| `mise run test` | The JVM tests |
| `mise run build` | Assemble every module |
| `mise run format` | `ktlintFormat` |
| `mise run ci:quick` | What pre-commit runs: `lint` |
| `mise run ci` | The gate: Gradle's `verify` on every target, the ABI check, the hot-path budgets, Trail's scenario gate, the app-size gate after the consumer and Trail builds and the capability coverage gate included, after the release scripts' fixture test. It first deletes what this repository's builds left behind and no longer use, inside the repository alone, and stops where the disk lacks room or the toolchain is not the pinned one |
| `mise run ci:local` | The part of the gate CI cannot run, which a release runs once a commit: the toolchain check, the device tests and Trail's journey on a running emulator (`test:device`), and the iOS simulator suites, on macOS |
| `mise run toolchain:check` | Refuse a toolchain other than the pinned one: the JDK `./gradlew` starts Gradle on, held to the `java` pin, and on macOS the Xcode `mise.toml` names |
| `mise run size:check` | The app-size gate alone: Trail's release build, its dex code read module by module with `apkanalyzer`, each share held to `examples/trail/size/android.json` at the version of `sdk/trail.yaml` it records, and every library the SDK brings named by a share. `MOUNTAYA_SIZE_BASELINE=write mise run size:check` records the figures after a deliberate change |
| `mise run clean` | Delete what this repository's builds left behind and no longer use, inside the repository alone: Gradle's state for a version the wrapper no longer names, and the build output of a directory that holds no build any more. `mise run clean -- --all` deletes the build directories too, `-- --dry-run` only lists |
| `mise run clean:toolchains` | Yours to run, never an agent's: list what every checkout on this machine shares and no Mountaya checkout pins or uses any more, Gradle versions, Kotlin/Native toolchains, Android packages, mise's installs and `mountaya-*` emulator devices, with the room each holds. `mise run clean:toolchains -- --yes` deletes them, never a path a running process holds open |
| `mise run ci:coverage` | The capability coverage gate alone: every capability has a runner or a scope, and every name the registry's `kotlin` column records for it is declared as written |
| `mise run test:device` | The device tests, on an Android emulator that is running: each module's, and Trail's journey of `sdk/trail.yaml` |
| `mise run trail:session` | Trail's session server, on `127.0.0.1:8787`, minting the app's session tokens with the `MOUNTAYA_PUBLISHABLE_KEY` and `MOUNTAYA_SECRET_KEY` of your shell (Examples you can run) |
| `mise run conformance:report` | Run the fixtures and write `sdk/reports/kotlin.json` into the contract checkout |
| `mise run consumer` | The consumer build alone, the README's and the developer skill's snippets with it |
| `mise run readme` | Print the README's Kotlin snippets into the consumer build after a change |
| `mise run snippets -- <file.md> [<file.md>...]` | Compile the Kotlin fences of any Markdown documents, a documentation site's pages among them, against the modules for the jvm and Android, warnings as errors: each fence printed into `consumer/build/generated/snippets`, never committed, where its imports say, a `<!-- kotlin:skip <reason> -->` line above one opting it out. It prints how many compiled and were skipped, and fails naming each failing fence's document and line. Paths are read from where you run it |
| `mise run api:update` | Rewrite the committed ABI dumps after a deliberate change to the public API |
| `mise run version <X.Y.Z>` | Write a release into the files and nothing else: the catalog's `mountaya`, the README's install lines, and `CHANGELOG.md`'s `[Unreleased]` notes under the version and today's date. Commit it as "version: Release v<X.Y.Z>" and push `main`, and CI runs the gate on it |
| `mise run release <X.Y.Z>` | Yours to run, on a Mac: release the pushed commit CI passed. The preflight, every sibling clean at its pin and every credential checked online among it, then `ci:local` once a commit, the signed modules uploaded to the Central Portal and validated there before the tag, the tag pushed alone to Entire, the deployment published to Maven Central, and a wait until the GitHub mirror lists the tag, which the Depot workflow `mirror.yml` pushes there from Entire. It makes no commit and pushes no `main`; run again after a stop, it resumes where it stopped |
| `mise run release <X.Y.Z> --check` | Every check of the release, the credentials' included, and what it would do; nothing is built, tagged, uploaded or pushed |
| `mise run release <X.Y.Z> --credentials` | The release's credentials alone, from the environment, the macOS Keychain or a prompt, each checked online |
| `mise run test:release` | The fixture test of `scripts/version.sh` and `scripts/release.sh`, against fakes of the Central Portal, gpg, Depot and mise, with no network |

Pre-commit runs `mise run ci:quick`. `.depot/workflows/ci.yml` runs `mise run ci` on Depot CI for
every push to Entire, against the `../schemas` and `../ai` commits `siblings.lock` pins. Run `mise
run ci` yourself before you push. It reads the contract from the `../schemas` checkout beside this
one, or from `-Pmountaya.schemasDir=<path>`, and the Kotlin developer skill from the `../ai` checkout,
or from `-Pmountaya.aiDir=<path>`, each as it stands; without `../ai` it compiles no skill snippet,
which CI refuses.

Every Kotlin snippet in this file is compiled by the consumer build in `consumer/`, printed into
`consumer/src/commonMain/kotlin/com/example/consumer/readme/ReadmeSnippets.kt`, on every target the
gate builds. After changing one, run `mise run readme`, and the diff is the review. A fence that
cannot compile opts out with `<!-- kotlin:skip <reason> -->` on the line above it. The floors table
under [Install](#install) is this file's one statement of each floor, which `checkReadmeSnippets`
holds to the version catalog's, a floor stated on a line outside it failing too. The developer
skill's snippets, `skills/mountaya-sdk-kotlin` in `ai`, are compiled the same way, each in common,
jvm or Android code as its imports say, printed under `consumer/build/generated/skill` and never
committed, and the floors the skill states are held to the version catalog's. Every declaration a
consumer reaches carries KDoc, which `lintDocumentation` holds. Every hot path the contract's
`sdk/hot-paths.yaml` lists for this SDK keeps a budget test, a `@Test fun hotPathBudget<Id>…()` asserting a
count against a top-level `private const val <ID>_BUDGET` beside it, which `checkHotPathBudgets` holds; the
budgets that count recompositions are device tests, which `mise run test:device` runs. The public API
of each module is the dump under its `api/`, which `mise run ci` compares; after a deliberate
change, `mise run api:update` rewrites them, and a line removed or changed is a break.
