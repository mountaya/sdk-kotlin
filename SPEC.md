# `sdk-kotlin` specification

Mountaya's Kotlin SDK: one Gradle build, Kotlin Multiplatform for the client, the geo layer and the
map's state and engine, Android for navigation and the views.

This document is written against `sdk/SPEC.md` in `mountaya/schemas`, which is the contract.
It is not written against another SDK. `sdk-typescript`, `sdk-dart` and `sdk-swift` implement
the same agreement, and writing this one against any of them would make Kotlin a translation
rather than an implementation. Where a rule here names what the TypeScript SDK does, the name is
corroboration of a rule argued on its own terms, never the reason for it.

## 1. Scope

**Contract tag.** One tag of `mountaya/schemas`, carried by the generated `SCHEMA_VERSION`
constant in `mountaya-client` and moved only by a regeneration. The constant lives in the client
because the client is the unit generated from the tag: the admin unit is printed from the same tag
and published at the client's own version, which it names exactly, so it carries none of its own,
Geo's printed tables are the elements' and no wire surface, and the contract's README gives a unit
with no wire surface no constant of its own. Beside it, `MODEL_DIGEST` names exactly the files the
surface and the elements' data were printed from, `modelDigest` (`sdk/SPEC.md` §11), which a tag
cannot: the files move between two tags. A conformance report is keyed on the registry's own
`version`, which moves with the contract's expected values rather than with its tag, and carries the
same digest (§9).

### The units

| Module | Targets | Layer | Is | Calls the network | Renders |
| --- | --- | --- | --- | --- | --- |
| `mountaya-client` | jvm, android, iOS | transport, the core | credentials, the session lifecycle, retries, idempotency keys, conditional writes, the error ladder, date decoding, pagination, event streams, field selection, and the generated wire surface but the admin unit's | yes, from one type | no |
| `mountaya-admin` | jvm, android, iOS | transport, the admin unit | the REST API's `admin` group, `postSession` among it, declared on the core's client as `mountaya.admin`, the components only those and the webhook events reach, and on the jvm the verification of a webhook delivery | only through the core's `Mountaya`, never a connection of its own | no |
| `mountaya-geo` | jvm, android, iOS | geo | the contract's geo layer, the navigation rules, the headless state and engines every view renders, and the navigation vocabulary with its engine interface | never | no |
| `mountaya-navigation` | android | geo, as the engine behind that interface | the engine an Android app navigates with, `DeviceNavigationEngine`, Geo's machine with the guidance that says each maneuver, and the seams it takes: where positions come from and the voice that says each maneuver | never | no |
| `mountaya-elements` | jvm, android, iOS; its renderer android alone | elements binding, and the `map` layer its engines own | the map's state and its engine on every target, the `map` rules the `map.*` capabilities prove, and on android the map view and the views over it, in Jetpack Compose over MapLibre Native | only through the app's `Mountaya` and MapLibre's own requests, never a connection of its own | on android |

The layer is the row of `sdk/SPEC.md`'s layer table the module implements, and `map` is the
layer the contract's registry names for the rendering engines' own behaviour, which an SDK whose
elements binding drives a renderer reproduces and the renderer-free geo layer cannot. The
artifacts are published as `com.mountaya:<module>`. **A Gradle module is the boundary unit**: a
consumer declares `com.mountaya:mountaya-geo` and resolves nothing else of this SDK, and the
published metadata, not a naming convention, decides what their build carries. All five are
declared: `mountaya-client` and `mountaya-geo`, each once every capability of its layer had its
runner, `mountaya-navigation`, once its engine stood on Geo's navigation rules,
`mountaya-elements`, once the map's engine and both `map.*` runners did, and `mountaya-admin`, once
the contract declared the admin unit (`sdk/SPEC.md`, "The three layers, in every language").

**The transport is two units, the core and the admin unit, as the contract declares it.** The admin
unit holds what only a server that manages an organization, mints session tokens or receives webhooks
needs: every operation of the REST API's `admin` group, `postSession` included, webhook receiving, and
every component only those reach. `sdk/model.json` states the unit of every REST operation, webhook
event and component as `unit`, and the printer splits the client by that key and by no list of its
own (§3). An app that draws a map, edits an itinerary, queries the Data API or navigates declares
`mountaya-client` alone, and links nothing of the admin unit: R8 would strip an unused admin surface
from an Android app anyway, so in Kotlin the split is parity of structure with the contract and the
other SDKs more than a saving, and it keeps a server's surface out of every app's compile classpath.
The secret key stays in the core, since it is a credential the core declares and its rules read (§4);
the one operation it authenticates, `postSession`, is the admin unit's.

**The client and the geo layer are Kotlin Multiplatform, from the first release.** A Kotlin
Multiplatform app's shared code needs the client and the rules on iOS as much as on Android, and
the jvm target keeps a server or desktop consumer free of the Android toolchain. The iOS targets
ship from the first release because common code proven on Kotlin/Native from its first line is
common code; a rule that compiles only on the JVM is caught the day it is written rather than the
day a target is added.

### Where each unit runs

| Module | JVM, servers and desktops | Android, every form factor | iPhone and iPad, through Kotlin/Native |
| --- | --- | --- | --- |
| `mountaya-client` | yes | yes | yes, `iosArm64` and `iosSimulatorArm64` |
| `mountaya-admin` | yes, webhook receiving and the secret key's mint included | yes, but webhook receiving and the mint | yes, `iosArm64` and `iosSimulatorArm64`, but webhook receiving and the mint |
| `mountaya-geo` | yes | yes | yes, `iosArm64` and `iosSimulatorArm64` |
| `mountaya-navigation` | no | yes | no |
| `mountaya-elements` | the map's state and engine | the map's state and engine, and the map view | the map's state and engine, `iosArm64` and `iosSimulatorArm64` |

**Navigation and the views are Android's**, because the navigation engine's seams read the Android
platform, its location services and its text-to-speech, and MapLibre's Kotlin Multiplatform binding,
maplibre-compose, is below 1.0. On iOS both come from `sdk-swift`, and the machine the navigation
engine stands on, `RouteFollowingEngine`, is Geo's, on every target. `mountaya-elements` is Kotlin
Multiplatform all the same: what a map shows and the
engine that holds it name no renderer type, so they are common code, and a desktop app, a server or
an app's shared code holds a map's state as an Android app does, as `sdk-swift`'s `MapEngine` builds
on every platform it does. Only the renderer, the composable over MapLibre and the hook its requests
leave through, is the android target's. A Compose Multiplatform map over maplibre-compose is
additive on top of this graph, later, and changes nothing below it.

**The limits, plainly.** Kotlin/Native has no visionOS target and only experimental Mac Catalyst
support, so a Vision Pro or Catalyst app is a Swift app, and so is any app on an Apple platform
beyond iPhone and iPad. The iOS targets serve Kotlin code running inside an app; they are not a
way for a Swift app to reach this SDK, which publishes no XCFramework.

### What each kind of app gets

| App | Takes from this SDK | Takes from elsewhere |
| --- | --- | --- |
| An Android app on a phone, tablet or foldable | every module but `mountaya-admin`, which an app that manages its organization adds; a Views app hosts a composable in a `ComposeView` | the Compose compiler, which a Compose app applies anyway |
| A Wear OS app, or a car app on Android Auto or Android Automotive | `mountaya-client`, `mountaya-geo` and `mountaya-navigation` | the screen, which the host draws with the platform's own templates from `NavigationState`; no Wear OS or car UI ships here |
| A Kotlin Multiplatform app with a native UI | `mountaya-client`, `mountaya-geo` and the map's state and engine of `mountaya-elements` in its shared code; on Android, `mountaya-navigation` and the map view | on iOS, the map view and the navigation engine from `sdk-swift` |
| A Compose Multiplatform app | `mountaya-client`, `mountaya-geo` and the map's state and engine in its shared code; on Android, `mountaya-navigation` and the map view | on iOS, `sdk-swift`'s map hosted through Compose's UIKit interop, until a Compose Multiplatform map exists here |
| A Flutter app | nothing | `sdk-dart`, whose navigation engine is Dart over its own geo package, its native code reading the device's location and speaking and deciding nothing, and which links no sibling SDK |
| A JVM server | `mountaya-client`, the secret key included, `mountaya-admin`, the session token's mint and a webhook delivery's verification included, and `mountaya-geo` | nothing |
| A JVM desktop app | `mountaya-client`, `mountaya-geo` and the map's state and engine | the drawing, from its own UI toolkit over Geo's data and the map's state |
| A Swift app on any Apple platform, or a Swift server on Linux | nothing | `sdk-swift` |

**A Kotlin Multiplatform app draws the seam through its own code.** Its shared Kotlin takes this
SDK; its iOS screens take the map and the navigation engine from `sdk-swift`, and the app
constructs a Swift `Mountaya` for the map's tile credentials. What crosses from Kotlin to Swift
crosses as values the app converts: `sdk-swift` never links Kotlin, and a type Kotlin/Native
exports to Swift is not one of `sdk-swift`'s types, whatever its name. The fixtures are the only
thing holding this SDK and `sdk-swift` together.

### How a consumer reads the units

| Module | Answers |
| --- | --- |
| `mountaya-client` | How do I get Mountaya data? |
| `mountaya-admin` | How do I run my organization: mint its session tokens, manage it, receive its webhooks? |
| `mountaya-geo` | What does Mountaya decide about it? |
| `mountaya-navigation` | How do I follow a route? |
| `mountaya-elements` | How do I show it? |

A consumer who asks only the first question declares only the first module, and each question
they add adds its module and nothing else.

### What a consumer declares

A normal integration declares Mountaya modules and nothing else, never MapLibre, and wires
navigation's route source to its own client in one line (§8). MapLibre is an implementation choice
behind a Mountaya-owned API, and a dependency the consumer declares is one whose next upgrade becomes
their migration. An app that navigates adds no build setting either, since the navigation links no
library (§2). An app that reads the device's position, a navigation's or a map's locate, declares
the fine and the coarse location permissions in its own manifest, which Android 12 asks for together
and the README shows, since neither `mountaya-navigation` nor `mountaya-elements` declares one (§8);
an app that speaks declares nothing, since
the one entry `mountaya-navigation`'s manifest holds, the query for the platform's speech service, is
merged into the app's by its build. An app that reaches through `MapView`'s
ready callback to MapLibre's own map (§8) needs no declaration of its own either: `mountaya-elements`
exposes MapLibre as an `api` dependency for exactly that callback, and OkHttp for the app's own client
the map's requests may be sent through. An app that shows the map applies the Compose compiler, as
every app that calls a composable does.

### No bridges

MapLibre Native, in C++, is a dependency behind a Mountaya-owned API, not a bridge. A bridge is a
Mountaya rule written in another language and wrapped here, and MapLibre holds none. Navigation
links no library at all: when a traveller is off route, which step they are in and when a prompt
falls due are Mountaya's rules, and each is Kotlin in this repository (§8). Every rule a fixture
proves is compiled from this repository's Kotlin.

### Out of scope

- **The studio.** The studio layer and its binding are TypeScript-only. They are Mountaya's
  editor, and an Android app does not host the editor. A rule an Android surface needs that the
  studio holds moves down into the geo layer of every SDK at once rather than bringing a studio
  with it, which is how it stays one rule: maneuver phrasing is the first, as
  `geo.navigation.instruction-keys`. Every `studio.*` capability is `scoped` here (§11).
- **A shared core.** Kotlin Multiplatform here serves Kotlin apps and nothing else. Shipping it to
  Swift as an XCFramework would make `sdk-swift` a wrapper over Kotlin, which is the bridge the
  constraint forbids, and a Swift consumer's stack traces, binary and concurrency model would stop
  being Swift's. No XCFramework is published, `sdk-swift` never links this SDK, and the fixtures
  are the only thing holding this SDK and `sdk-swift` together.
- **Views surfaces.** The views are Compose. A Views app hosts one in a `ComposeView`, which is
  one line, where a second view layer is a second implementation of every surface.
- **A transport binding.** The contract allows at most one per UI framework and asks for none. A
  composable already collects a `Flow`, and a cache a consumer brings has a retry of its own that
  would repeat what the client already repeated, so a consumer who brings one turns that retry
  off. The TypeScript SDK's query binding does exactly that to its own cache.
- **A published `ViewModel`.** An Android `ViewModel` is a lifecycle holder the consumer writes.
  Publishing one would put AndroidX into the geo layer and decide a consumer's architecture for
  them; a `StateFlow` on the caller's scope is the whole contract, and a `ViewModel` over it is a
  few lines in their own module.
- **JS and Wasm targets.** A browser target would bring the contract's secret-key defences for
  browsers into scope, for consumers `sdk-typescript` already serves.
- **Permissions and background execution.** Asking for location, and the foreground service a
  session runs in, are the host's decisions, with Play policy consequences only the host can
  weigh (§8).
- **Console-only resources.** The contract does not carry them.

### What waits on the contract

Nothing a consumer observes waits on `mountaya/schemas` at registry 19; `CONTRACT-GAPS.md` in
`sdk-swift` keeps the fixture and runner hygiene that changes no answer, every branch no fixture
reaches with the case or the sentence that would close it, with this SDK's answer in each row. This
SDK reads that file and keeps no copy of it, since a gap in the contract is a gap for every SDK and
two copies would disagree.

## 2. The graph

```
mountaya-client ──(api)──┬──> mountaya-geo ──┬──> mountaya-navigation
                         │                   │
                         │                   └──> mountaya-elements <──── MapLibre (api)
                         │
                         └──> mountaya-admin
```

Arrows point from a dependency to its dependents, third-party libraries included.

- **`mountaya-admin` depends on `mountaya-client` with `api`, and nothing depends on it.** The admin
  unit declares `admin` on the core's client, `public val Mountaya.admin: Admin`, an extension
  property of `com.mountaya.admin`, so importing it is the whole of what a call into it asks:
  `mountaya.admin.<resource>.<operationId>(…)`, as the contract spells it (`sdk/SPEC.md`, "The three
  layers, in every language"), and the core's `Mountaya` declares no `admin` of its own. Its
  signatures name the core's types, `Mountaya`, `Response`, `RequestOptions` and every component the
  core holds too, which is when `api` is right. It reaches the core's transport through one seam, the
  `OperationSender` every printed method hands its call over through, which the core's client exposes as
  `Mountaya.sender`, and the few helpers a printed method calls, each `InternalMountayaApi` (§7): public
  to the compiler and to no consumer, out of every dump, and read by the admin unit alone. It is Kotlin
  Multiplatform over the client's targets, since a phone that manages its organization calls the
  `admin` group as a server does; its jvm target alone holds webhook receiving, as the core's alone
  holds the secret key. The core never depends on it, and a core that compiled only beside it is a
  defect in the contract's list of the components the core keeps, `CORE_SCHEMAS`, mended there.
- **`mountaya-geo` depends on `mountaya-client` with `api`, for its wire vocabulary, and calls
  nothing.** The generated types Geo aliases appear in Geo's own public signatures, which is
  exactly when `api` is right. Kotlin has no type-only dependency, so the promise that Geo never
  calls the client is held by §7's boundary test rather than by the graph. §11 records the link.
- **`mountaya-navigation` links no third-party library.** Its engine is Geo's machine with its
  guidance, Kotlin in this repository, and its seams read the platform's own services: the location
  manager for the device's position, and the text-to-speech and the audio focus for the voice. It is
  an Android module because those seams are: Kotlin Multiplatform with the android target alone, its
  own convention plugin, `mountaya.android-library`, over what every module holds,
  `mountaya.kotlin-module`. The settings include it where the android target is built, so a run that
  leaves android out needs no Android SDK and configures no Android module, and its gates read its
  sources only then. No module links a navigation library, which §7 holds.
- **`mountaya-elements` is the only module that depends on MapLibre**, `org.maplibre.gl:android-sdk`
  in its android source set, with `api`, because the map view's ready callback hands back MapLibre's
  own map (§8); OkHttp is `api` beside it, since the view takes the app's own `OkHttpClient` for
  MapLibre's requests. It is Kotlin Multiplatform, `mountaya.multiplatform-library` with the Compose
  compiler applied to its android target alone: the map's state, its engine and the `map` rules are
  common code, built and tested on every target, and the renderer, MapLibre and Compose are its
  android source set's. A consumer of its jvm or iOS variant resolves no MapLibre, no Compose and no
  AndroidX.
- **`mountaya-elements` owns the `map` layer.** How a patch merges onto what a map holds and where
  an edit's magnet lands are the map and editing engines' own behaviour, which the contract files
  under `map` rather than `geo` because a renderer-free consumer has no map to patch and no edit
  to snap. Their rules name no Android, Compose or MapLibre type, and neither does the map engine
  that applies the patch, so all three are common code whose fixtures run on the jvm and the iOS
  simulator with no emulator (§9); the renderer that applies them to a live map is Android like the
  views, and measures the magnet through MapLibre's projection.
- **`mountaya-elements` does not depend on `mountaya-navigation`.** It renders `NavigationState`,
  which Geo declares, so a map-only app ships no navigation engine and a navigation app with no map
  ships no MapLibre.
- **`mountaya-navigation` and `mountaya-elements` are built on Geo's public API only**, and the
  client's, with no friend modules and no `internal` reached across modules. The headless promise, that a consumer
  can build their own views and their own engine on Geo, is only worth making if Mountaya's own are
  built the same way: a view or an engine that reached an `internal` declaration is one a consumer
  could not have written. The TypeScript SDK publishes its studio binding's `./headless` entry on
  the same principle. So what the navigation engine needs of Geo is published, deliberately and
  small: the machine every engine stands on, `RouteFollowingEngine`, with two public constructors,
  over a scope and a route source and over the two and an inline observer of its states, `onChange`,
  which follows a route on every target with the positions an app hands it, and through which the
  device's engine keeps its guidance and its location source to each state before any collector sees
  it; the off-route rule, `OFF_ROUTE_METERS`, `isOffRoute` and `isOffRouteAt`, which a host's own
  engine or screen reads as the machine does; and a position along a route followed read off the measure
  it holds, `positionAtMeters(route, meters)`, which the replay walks a route by and a host's own source
  reads as it does. The machine's ticketed wiring, its constructor over a
  send and an abandon and `answered`, stays `internal`: it is the scripted engine's, and a ticket is the route
  requests' own bookkeeping, not a consumer's (§8). `sdk-swift` publishes the same, by the same
  reasoning. What the map needs of the client is published the same way: `Mountaya.language`, which a
  map's labels fall back to, and `Mountaya.mapRequestHeaders(url)`, the client's credential and
  language projected for a request a renderer makes for itself, which is how a map's own requests
  carry the client's credential without reaching its transport, and `Mountaya.baseUrls`, which names
  the address of the layers manifest a map was refused. What the map needs of Geo is published the
  same way: `refusesMaxBounds`, the predicate the map engine refuses a pan lock by, which a consumer
  drawing a map of their own asks of a lock before their renderer does, and the camera rules the
  engine and the renderer apply, `cameraForBounds`, `itineraryBounds`, `specificationBounds`,
  `zoomIntoRange`, `accuracyBounds` and `LOCATE_MAX_ZOOM`, with `trimWhiteSpace`, which reads a blank
  credit.
- **`mountaya-elements` depends on Compose UI, never Material.** Material is a design system, and
  the elements binding brings none (§8). The map view needs Compose UI and the lifecycle's
  composition local alone; Compose Foundation joins with the first view that lays something out, and
  AndroidX Core with the read of the window's own insets, `ViewCompat.getRootWindowInsets`, which the
  map's safe area is measured from, named at the release Compose's UI requires, so an app resolves
  nothing it does not carry already.
- **One version across the repository, and internal dependencies are exact.** Every module is
  published at the same version, and each names its siblings by that exact version: a range
  would let a consumer resolve two modules from different releases, a state this build never
  tested.
- **`implementation` by default, `api` only where a type crosses.** An `api` dependency lands on
  every consumer's compile classpath, so one used where `implementation` would do publishes a
  surface nobody chose.
- **A platform floor lives in the version catalog, and the README's floors table is its one copy in
  this repository's prose, held to the catalog by a gate.** Prose names the reason for a floor: the
  Kotlin floor is the first release in which `kotlin.time.Instant` and `kotlin.uuid.Uuid` are both
  stable, the `compileSdk` an app builds with is the one OkHttp's Android artifact asks every app
  that carries it for (§4), and the `minSdk` every module runs on is the first release that carries
  the platform's ICU, `android.icu`, which Geo writes an instant through so a time keeps the locale's
  own clock and a date and a time are joined as the locale joins them on every device (`sdk/SPEC.md`
  §7, and §8 below), above the floors of the libraries the modules carry, Compose's, AndroidX's and
  MapLibre Native's, and of the runtime permissions the device's location sources read location access
  through. One floor holds every module, so none raises what an app carrying another may target. Its number is written in prose in
  one place, the README's floors table under Install, which `| Floor | Version |` opens and which
  states on a row of its own each floor a consumer's build meets, the Kotlin it compiles with, the
  Java its code targets, `minSdk` and `compileSdk`; `checkReadmeSnippets` holds the table to the
  catalog's figures, and fails on a row stating another figure, on a floor stated on no row or on
  two, and on a floor stated on any line of the README outside the table (§10). A number copied into
  any other prose of this repository is a second copy the next bump leaves behind. The developer
  skill, which `ai` keeps and an agent reads apart from the README, states the four too, and
  `checkSkillSnippets` holds them to the catalog by the same reading (§10).
- **A tool version lives in `mise.toml` and nowhere else.** The JDK, the Android SDK tools and
  Gradle are pinned there, the Gradle wrapper is held to that pin by a gate, and
  `mise run ci` is the repository's gate. The version catalog keeps what the build resolves
  rather than what runs it: plugins, libraries and the floors above. What mise cannot install is
  asserted instead: `mise run toolchain:check` (`scripts/check-toolchain.sh`), which the gate runs
  after the disk check, refuses a JDK under `./gradlew` other than the one mise installed for the
  `java` pin, and on macOS, whose Xcode builds the iOS klibs, an Xcode other than the version the
  task names.
- **A build never installs an Android SDK package.** Each Android build's `gradle.properties`, the
  repository's, the consumer's and Trail's, sets `android.builder.sdkDownload=false`, so a platform or
  build tools the build names and the SDK lacks fails the build rather than the Android Gradle plugin
  installing it, its licence accepted with it. `mise run android:packages` installs them, at the exact
  versions `mise.toml` writes, and it is the person's to run, as every licence is theirs to accept.
- **The gate cleans up inside the repository before it builds, and nowhere else.** `mise run ci`
  starts with `mise run clean` (`scripts/clean.sh`), which deletes what this repository's builds
  left behind and no longer use, and only paths inside the repository: Gradle's state under a
  `.gradle/` for a version the wrapper no longer names, and the build output of a directory that
  holds no build any more; a path outside it stops the script. The toolchains every checkout on the
  machine shares are `mise run clean:toolchains`' (`scripts/clean-toolchains.sh`), which the person
  runs and an agent is denied: it keeps what any Mountaya checkout's `mise.toml`, wrapper or version
  catalog names and the version `mise where` reports in use, lists the rest, a Gradle version's
  distribution, caches and daemon logs once no wrapper names it and no daemon of it ran this week, a
  Kotlin/Native toolchain for a Kotlin no checkout pins and the dependencies no kept toolchain names,
  mise's installs of Gradle and the Android SDK at another version (an Android SDK holds its system
  image, emulator and NDK inside its version's directory), an Android system image, platform or NDK
  no checkout names, the newest NDK kept, and a `mountaya-*` emulator device no checkout creates,
  and deletes them only with `--yes`, never one a running process holds open. Then `mise run
  disk:check` (`scripts/check-disk.sh`), which `test:device` runs too, stops before anything builds
  where the disk has less than 25 GB free for a first gate or 15 GB for a later one,
  `MOUNTAYA_MIN_FREE_GB` asking for another figure, and names both cleans; CI's runner is not
  checked. A build that runs the disk out of space fails part-way and stops every other program on
  the machine that writes.
- **`expect` and `actual` appear only under `seams/`, and only for what the platform provides**:
  the locale services, Unicode's canonical decomposition and composition and its general category
  of marks, SHA-256, HMAC-SHA256 and a comparison of bytes in constant time, and the HTTP engine.
  Every rule
  is common code, so every target runs the same rule, and each `actual` is a second
  implementation the fixtures prove on its own target (§9).

## 3. Generated surface

**Source.** `sdk/model.json` from `mountaya/schemas` at the recorded tag: `openapi/api.yaml`,
`openapi/tiles.yaml` and `graphql/data/*.graphqls` restated as one language-neutral document,
built there and compared byte for byte by that repository's own gate. The bundles and the SDL
remain the description. The model is what a generator reads, so the questions every language
would otherwise settle on its own are settled once: which encoding says a value may be null,
what an inline schema is called, which credentials an operation declares, what a query's full
selection is and in which order.

**Generator.** A printer in this repository's build logic, run as `./gradlew generateClient`
in each unit of the client and writing into its generated package: the core's into
`mountaya-client/src/commonMain/kotlin/com/mountaya/client/generated/`, and the admin unit's into
`mountaya-admin/src/commonMain/kotlin/com/mountaya/admin/generated/` (below). It
prints final bytes itself, with no formatter between it and the file, so no formatter release
can move the output, and `./gradlew generateClient --check` fails on drift; `check` runs the same
comparison as `checkGeneratedClient`, so a model that moved without a reprint fails the gate, and
a reprint deletes a file the model no longer prints. It refuses a `modelVersion` it was not
written for and a `kind` it does not know, and never skips one. It decides only what a language
decides: the Kotlin type for each shape, how a name is escaped, where a file goes. Its own tests
print a miniature model holding one of every kind and compare the result with golden files, so a
change to the printer is read as a diff before it moves a byte of the real surface. The generated
package is outside ktlint as it is outside the naming and layout gates: its bytes are what the
model prints and the check holds them there, so a formatter would be a second author of the same
file.

**Frozen, in every language at once:**

- `operationId` is the method name, verbatim, with no rename table.
- `x-sdk-namespace` is `<group>.<resource>`, the groups being `studio`, `admin` and `agent`.
  Tiles and Data are deliberately un-namespaced.
- An operation marked `x-sdk-ignore` is emitted by no client as a method. The model lists those
  routes under `templates`, apart from the operations, so a printer that binds a method to every
  operation cannot bind one: they are what a map renderer fetches on its own, the tiles and the
  base map's style, TileJSON, sprite and glyph documents. The printer emits each as a
  `UrlTemplate`, never as a method a caller awaits: a member of `TileTemplates`, named by its
  `operationId`, holding the path, the path's parameters in the order the path names them, the
  query's and the media types the route answers in, with `TileTemplates.all` listing every one in
  the model's order. `filled` places a value in a placeholder as one percent-encoded path segment
  and leaves every other as written, a renderer's `{z}`, `{x}` and `{y}` among them. A surface
  whose model lists no template, REST at this tag, prints no object of none, and a route named
  `all`, or whose path names other parameters than it declares, is a printer error. A jvm test
  holds every template to the model. `UrlTemplate` is hand-written, in the client's one package
  and filed under `surfaces/contracts/` (§6), as `sdk-swift`'s `URLTemplate` is.
- Idempotence is read from each operation's `x-idempotent`, never inferred from its method, and
  a jvm test compares every printed operation's flag with `sdk/model.json`, which the contract's
  own gate holds to the bundles byte for byte. The same test holds every other fact the
  transport reads at request time: the method, the path, the six credential facts, the attempt
  budget (`timeoutMs`), whether the operation takes an `Idempotency-Key` and an `If-Match`, its
  pagination, its success statuses with the headers each declares and its declared error
  statuses, operation by operation, and the count, and beside them the tasks a write runs, which
  name the `task` its webhook events carry and which no request reads. A success status is printed as a
  `SuccessResponse`, its status and the names of the headers it declares in the model's order,
  which decide nothing at request time: an answer's `X-Request-Id`, `Location` and `ETag` are read
  wherever they are sent, and the facts carry them so the gate holds all the model says. A generator that dropped one would ship a
  client that repeats a write after a dropped socket, or sends a write with no key, and nothing
  else would fail. The TypeScript SDK's `contract-checkout` test holds the same comparison over
  the facts. The facts are printed in each unit's `OperationTable`, one value per operation, listed
  per surface in the model's order as `restOperations`, `tileOperations` and `dataOperations` in
  the core's and as `restOperations` in the admin unit's, which its own jvm test holds to the model
  the same way; the
  same test holds every Data API operation to `POST` on the model's endpoint, idempotent,
  requiring a session token and waiting the `timeoutMs` the model carries, `SCHEMA_VERSION`
  to the model's `contract` and to `VERSION`, and `MODEL_DIGEST` to the digest of the checkout's
  files, taken there with the JDK's own SHA-256 rather than the printer's.
- A generated file is never hand-edited. A wrong shape is a defect in the description.

**Nullability, under every encoding.** The bundles are OpenAPI 3.1.1 and say a value may be null
the 3.1 way: a type array carrying `'null'`, or a `oneOf` with a `null` branch. The model reads
both, and 3.0's `nullable: true` beside them, and states one flag, which is the one the
generator reads. A generator reading only the 3.0 flag finds no nullable field in these
bundles, and in Kotlin that is `String` where the contract says
`String?`: a null-safety guarantee the compiler enforces everywhere and that is simply wrong,
and kotlinx.serialization throws on the first real response carrying the null. The TypeScript
generator reads both encodings for the same reason: reading only the flag types
`Collection.description` and `Zone.start_at` as always present. A property absent from `required`
is nullable with a `null` default, and a property with a `default` stays optional, because
applying the default is the server's job and a generated one is a second copy of it. A required
property that may be null reads `null` where an answer leaves it out, as `sdk-swift` reads it, and
is always written, a `null` included (`@EncodeDefault`), so a request still carries the key.

**Two units, split by the model.** `sdk/model.json` states the unit of every REST operation, webhook
event and component, `core` or `admin`, and the printer splits the client by that key and by no list
of its own (`sdk/SPEC.md`, "The three layers, in every language"). The core, `mountaya-client`,
prints the `studio` and `agent` groups, the Tile and the Data APIs and every REST component the model
states the core's; the admin unit, `mountaya-admin`, prints the `admin` group, `postSession` among it,
the webhook events and the components the model states its own, those only the `admin` operations
and the webhook events reach, into `com.mountaya.admin.generated`: its accessor `Admin`, its own
`OperationTable`, `WebhookEvent`, and its components in the object `AdminSchema`. A component of the
core it names is named in full, `com.mountaya.client.generated.RestSchema.Session`, and an alias of
the core's is read through the core's types; a core component that named one of the admin unit's
would compile only beside it, and is refused by name. Kotlin cannot add a member to an object from
another module, so the admin unit's components sit in an object of their own, named apart from the
core's so a file that names both units' types, a preferences update holding the core's
`RestSchema.UnitLength` or a session's mint holding `RestSchema.SessionSurface`, needs no import
alias: the contract's names are its components', and the namespace is this SDK's. The printer refuses
an operation of the `admin` group the model gives the core, or one of another group it gives the admin
unit, since the admin unit declares one accessor on the core's client; a Tile API operation, route
or component of the admin unit, since the Tile API is the core's; a webhook event the core would
receive; and a union whose member is another unit's, since a member extends its union in one
module. Either unit's print reads and checks the whole model, so a defect in one is refused by both,
and each has its own golden files. Every printed file of the admin unit opts in to the core's seam
it reads, `@file:OptIn(InternalMountayaApi::class)` (§7).

**Namespaces.** Generated types live in the objects `RestSchema`, `DataSchema` and `TileSchema`,
the names the TypeScript SDK gives the same surfaces, and `AdminSchema` for the admin unit's, and never
at package scope, but for the accessors (below), `TileTemplates` (above), `WebhookEvent` and
`ApiErrorCode`. Many names
exist on more than one surface, not always with one shape, and at package scope REST's
`Collection` would shadow `kotlin.collections.Collection`, imported into every Kotlin file by
default, wherever a consumer imports it. Inside a namespace the printer qualifies every platform
type it names, `kotlin.String` and `kotlin.collections.List` among them, so no component the
contract adds shadows one. It prints no type for the `Error` and `Errors` components, which the
model marks as envelopes: the transport reads an error body as its own `ErrorEnvelope` and
`ErrorEntry`, the envelope's own types. The code an entry carries is typed by `ApiErrorCode`, which the
core's print writes at package scope, since it is no surface's alone: one open vocabulary, the union
of the codes the Map Studio API's and the Tile API's `ErrorExtensionsCode` and the Data API's
`ErrorCode` declare, each once, in the order the Map Studio API, then the Tile API, then the Data API
first names it, a constant per code with the surfaces
that declare it in its KDoc (`sdk/SPEC.md` §3). Dart names it `ApiErrorCode` too and Swift
`APIErrorCode`; `DataSchema.ErrorCode` stays the Data API's own vocabulary, which the search reads.

**One printed class sits in the admin unit's package itself: `WebhookEvent`**, the event each public
type of the REST surface's `webhooks` is delivered as, which no component is and which
`AdminSchema.WebhookEvent`, the envelope, already names inside the namespace. It is an abstract class with an `internal`
constructor, as a union is, so a type the contract adds is one more class and a `when` over an
event ends in `else`. Each type is a final class of its own, named by the type with its dots and
underscores folded to UpperCamelCase (`OrganizationMemberUpdated`), extending
`WebhookEvent.Typed<Object, PreviousAttributes>`, which holds a `WebhookEvent.Payload` of every
member of the contract's envelope but its `type`, with a `data` whose `object` is the component the
type names and whose `previous_attributes` is a printed class holding the fields the type names
(`OrganizationMemberUpdatedPreviousAttributes`), or `Nothing` for a type that names none, whose
previous attributes are then never read; and `WebhookEvent.Unknown`, holding the envelope for an
event this build cannot type: a type a later contract adds, or a payload the class's types do not
read, its object then the envelope union's `Unknown` where the envelope's own types do not read it
either. Every member of the envelope but `type` and `data` is an abstract property of
`WebhookEvent` every class answers, and `type` is too. A previous attribute is the object's field
made optional, null where the change did not move it, and a field that may be null is read as a
`Stated`, which holds null where it held null, since a nullable alone would read the two absences
alike. The printer refuses an envelope other than `WebhookEvent`, an object or a field the model
does not declare, two types read as one class or as a class the event declares of its own, a set
of types that is not the envelope's `type` enum's, and an envelope an untyped event could not be
held in, each by name; `sdk-swift` prints the same events as the cases of one enum.

**Names are the contract's, verbatim, but an enum member's.** Kotlin's own convention would
camel-case a property and strip a prefix; the generated names are not adjusted, and the generated
files carry a header saying so. A casing transform is a rename table in disguise: graphql-codegen's
default turns the contract's `POIsInput` into `PoIsInput`, a name the contract never published. One
concept spelled differently per surface, kebab-case on REST and SCREAMING_SNAKE_CASE on the Data
API, stays a type per surface, and a geo rule that accepts both takes an adapter at its edge so the
rule itself speaks one vocabulary.

**An enum member is printed under its words**, by the one rule `sdk/SPEC.md` §9 states for every
SDK, since the name `x-enum-varnames` gives a member repeats its type at every use:
`TileLayerType.TileLayerTypeLine`. A member's words are its `x-enum-varnames` name with its type's
name taken off the front, split before each capital, or its Data API name split at each underscore,
and a first word that starts with a digit keeps the last word of its type's name in front of it,
since no identifier starts with one. Kotlin writes the words in UpperCamelCase, a capital then
lowercase, as Compose's `TextAlign.Center` and Ktor's `HttpMethod.Get` spell a value class's
constants and as this SDK's own sets spell theirs (§5): `TileLayerType.Line`,
`TileSourceType.RasterDem`, `ElevationTileSize.Size512`, `UsageStatusClass.Class2xx`, and the Data
API's `ActivitySlug.HikingAndTrail`, `TrailDifficultyType.Mtb0` and `SearchTarget.Places`, a member
both surfaces declare printed under one name on each. A Kotlin keyword is lowercase, so no word is
one. The type keeps its name, the wire value is untouched, and the model carries no key for the
words: the printer derives them from the name it reads (`EnumMembers`), and refuses, each by name, a
name the rule cannot read, two members of one type printed under one name once case is set aside,
since on the jvm two names that differ in their first letter's case share a getter, and a member
printed under a name the value class declares of its own, `value`, `toString`, `equals`,
`hashCode`, `serializer` or `Companion`, in any case. A set the contract writes inline, a listing's
`sort`, is named the same way where the model carries its `x-enum-varnames` as `enumNames`, which every
listing's `sort` does, a leading `-` reading `Descending`: `GetItinerariesSort.UpdatedAtDescending`
is `-updated_at`, and each member's KDoc names the wire value it is sent as. A set the model names no
member of keeps its wire values, between backticks where Kotlin needs them, and a model naming a
member count other than its values' is refused.

**One declared type set for the shared scalars.** The Data API's `GeoJSON`, `DateTime` and `URL`
scalars and REST's `GeoJSON*` components map onto one set of types: GeoJSON types declared once
in `mountaya-client`, since RFC 7946 rather than the bundle is their definition, then
`kotlin.time.Instant` and a URL type declared beside them. A geometry read from REST and one read
from the Data API are one Kotlin type, which is what the TypeScript SDK gets by mapping both onto
`@types/geojson`.

**Generated types are the vocabulary.** Geo and Elements alias a generated type, never copy it:
`public typealias DirectionsResult = DataSchema.DirectionsResult`, so a consumer names it from
Geo alone. A hand-written structural twin compiles while missing a field the contract added, and
the divergence surfaces as a request the platform rejects rather than as a type error; the
TypeScript SDK's geo layer says there is no structural copy, and there must never be one. A
hand-written type exists only where the wire's vocabulary stops, for what the SDK derives, such
as an elevation series or a navigation state, and never for what it receives.

**Forward compatibility.** An app in the field outlives the tag it was built against: the server
moves to the next tag while a build on a phone does not. kotlinx.serialization throws on a key it
does not know unless told otherwise, so the client's `Json` sets `ignoreUnknownKeys` and a
property the contract adds decodes. A wire enum is an open vocabulary: a `@JvmInline value class`
over the wire's own type, `@Serializable`, with a companion constant per member, named by its words
(above).
The wire's type is `String` for every enum but the tile-size ones, which are integers on the wire
and are value classes over `Int`. A value
this build has never seen decodes as itself and a new member is additive, where an `enum class`
fails the whole response on the first unknown value and turns every member the contract adds into
a source break for a consumer's exhaustive `when`. A discriminated union follows the same rule by
other means: it is an abstract class, never a sealed one, with a member holding what arrived, so
a member the contract adds decodes in a build made before it and breaks no `when`. A union the
contract writes inline with no discriminator is read by trying its members in the contract's
order, the first that reads winning, and holds `Unknown` where none does.

**A call is written from what the operation declares.** Path parameters are named parameters,
verbatim and in the path's own order, followed by the query parameters the operation requires.
A body is one value passed as `body`, built by a factory named after its type, which takes the
required fields as parameters and sets the optional ones in a trailing builder:
`RestSchema.ItineraryCreateParams(activity_id = id, is_private = false, name = "Tour du Mont Blanc") { description = "Seven days." }`.
The split follows the contract's own versioning: a required field added to a request is a
breaking change there, so a factory's signature moves only when the contract breaks, and an
optional field the contract adds is one more property a builder may set. Optional query
parameters are set in the method's own trailing builder, which every operation carries from its
first release even when it holds nothing, so a parameter the contract adds changes no call site
and no JVM signature: a parameter with a default is still a new signature to a consumer compiled
against the old one. `options` carries what §4 lets a call override.

**The headers a call carries are the client's, never an argument.** `sdk/model.json` lists a
caller's own headers under `headerParameters`, and at this tag it lists none: `Accept-Language`
is the client's configured language, `Last-Event-ID` is what a stream's reopen sends, and
`Idempotency-Key` and `If-Match` ride the call's own builder. A write whose operation declares
`idempotencyKey` has `idempotencyKey` in its trailing builder, the key a caller sets to make two
of its own calls one write, and otherwise draws one (§4). A write whose operation declares
`conditional` has `ifMatch` there, the `etag` the caller read the resource under or `"*"`, sent
as given on every attempt and never written for a caller who set none (§4). Each is in the
builder only where the operation declares it, so a key or a tag the operation would not read
cannot be written for it. Both sit in the builder for the reason an optional query parameter does: an `If-Match` the
contract adds to an operation, as a minor tag may, changes no signature. A name either would
share with a query parameter is a printer error.

**Three states, and the builder says so.** A property that is not required and is nullable, on a
schema a request sends, has three states in the contract's model: left out, a value, and `null`.
Such a builder property records whether it was assigned. Left unassigned it is not sent and the
stored value is untouched; assigned a value, it replaces it; assigned `null`, it sends `null` and
clears it. That is why `= null` clears there, where on every other optional nullable property it
is what leaving the property unassigned already means and nothing is sent. The rule reaches every
such property, a value object a merge body embeds included (`WaypointContact` is one), and each
one's KDoc says so where a caller reads it. A component that is sent and received keeps its three
states wherever it is named, since a class has one shape: read from a response, the property
reads the value, `null` where the answer left it out or sent `null`. A builder property is
write-only, so a builder is never read back as if it were the request.

**What a call returns.** A REST or Tile operation returns a `Response` carrying `data` and, where
the operation declares one, `metadata`, which is the envelope's `extensions.metadata`, and the
answer's `status`, its `requestId` (`X-Request-Id`), its `location` (`Location`, on a 201) and its
`etag` (`ETag`), each null where the answer sent none, beside every header the answer sent,
`headers`, each name in lowercase, `A` to `Z` folded alone, and a header that arrived more than once
holding its values joined by a comma and a space, which `header(name)` reads in any letter case, as
`sdk-swift`'s `Response` holds them. A write
returns the resource whose read it changed, a 201 carries the id of the part it created in its
metadata, a 202 carries what it started, and a `data` the contract types as null is `JsonNull`,
the envelope coming back whole all the same. A resource's `etag` is read from its body, as the
body states it, and never rebuilt from the header (`transport.response.envelope`). A Data API
operation returns its result type, and an answer that lacks its operation's field is a
`DataException`. The export returns a `BinaryResponse`: its bytes, the content type the server chose, the status
and the `X-Request-Id` its success declares, null where the answer sent none. An event
stream returns an `EventStream`, a cold `Flow` of frames that applies `sdk/SPEC.md` §12 itself,
because the registry files that loop under the transport (§4). A paged listing is read through one walk over
any operation the model marks with `pagination`, `PageWalk`, a cold `Flow` of rows (§4), and the printer
prints beside each such method its walk, `walk` before the operation's name (`walkGetItineraries`), which
takes the method's path parameters and the query parameters it requires, a `pageSize` defaulting to the
`default` the operation's `limit` states, `maxPages`, `options` and the method's own trailing builder,
and answers the rows as the listing types them through a `PageWalk` over the method. A paged operation
whose `limit` states no default, whose metadata is not the `ListMetadata` the walk reads, whose `data` is
no list, that takes a body or sits on a surface other than REST, or whose walk's name another operation of
its accessor holds, is a printer error. An operation this build prints no method for is called by its
declaration, `Mountaya.send`, `Mountaya.query`, `Mountaya.walk` and `Mountaya.events` (§4).

**Formats.** `format: date-time` is a `kotlin.time.Instant` (§4) and `format: uri` is the URL
type declared beside the shared scalars, as the Data API's scalars of the same meaning are.
Every other string format stays a `String`, identifiers included: the Data API types the same
identifiers as `ID`, so a string is the one type an identifier has on every surface. `int64` is a
`Long`, every other integer an `Int`, and every number a `Double`.

**The URL type is `Url`**, filed under the client's `scalars/contracts/`: a value class over the text
as written, which `toString()` returns. It parses nothing out of the text, since the text is what
is sent and compared, and it holds whatever text it is given, so an answer's web address reads as
the text the wire sent, one with no scheme or one a platform's address type would rewrite included,
and never fails the answer it arrives in (`transport.response.envelope`). Common Kotlin has no URL
type, and Ktor's would put the transport's library into every signature that names a URL. What the
client sends is held to a scheme, a colon and at least one more character, none a line break, since
a path or a host alone is not a URL and a request carrying one would go nowhere a caller meant: a
printed class a request sends writes each web address it holds through `SentUrlSerializer`,
`internal`, which the printer names on the property's type, and which refuses one with no scheme
with `ConfigurationException` as the request's body is written, before any request; a path, a
query or a form writes one through the same check; and `BaseUrls` refuses an origin with none where
it is given. A class only an answer carries writes its web addresses back as they were read, so
writing an answer's JSON never refuses what reading it kept.

**No `data class`.** A `data class` puts every property into its constructor, its `copy` and its
`componentN` functions, so a property the contract adds changes all of them: an ABI break every
time the contract grows. Generated types are plain classes, and an input is built through a
builder DSL, so an optional property the contract adds changes no call site.

**A secret is never printed.** A property the model marks `secret`, a session's `token`, a webhook
endpoint's signing `secret`, a setup intent's `client_secret` and a share link's `share_token`, is
printed `<redacted>` by its class's `toString`, every other property as it is, so a log never carries
one, and it stays readable: the printer reads the mark, and names no property of its own. A credential
and a session token the client holds print by their prefix alone (§4), as a session's snapshot does.

**Explicit API mode is on for every module, and has no per-directory opt-out.** The generated
files share a source set with hand-written code, so the generator writes `public` on every
declaration itself and the generated surface is held to the mode like the rest.

**Serialization is kotlinx.serialization**, reflection-free, so it survives R8 without a keep rule
and runs on every target.

**Every name is written in full.** A printed file imports nothing: a platform type is
`kotlin.String` and a printed one `RestSchema.Hut`, so no name the contract adds can shadow a
platform one inside a namespace, and the contract's `Collection` is simply a type of its own.

**The elements' data is printed too, into Geo.** `sdk/elements/` beside `sdk/model.json` holds the
words the elements put on screen, their colour tokens, the glyph table and the notice of the markup
it carries, and `./gradlew :mountaya-geo:generateElements` prints them into
`mountaya-geo/src/commonMain/kotlin/com/mountaya/geo/generated/`, with `THIRD-PARTY-NOTICES.txt` at
the repository root (§10); `--check`, and `checkGeneratedElements` in `check`, compare instead, and
`mise run generate` runs both printers. Geo holds the rules that read the three tables, and builds
and tests on every target. The words print as the bundle `WordBundle.elements` publishes, English and
every translation the table carries, each a JSON tree keyed as the table keys it, so a consumer folds
its own words over them through `resolveBundle` and `WordOverrides`, as over any bundle; a counted
word stays the table's object of CLDR forms, which no rule chooses among yet. `languageEndonyms`
names each label language in its own tongue. The tokens print as the class `ElementTokens`, one
member per token spelled in camel case (`control-bg` is `controlBg`), each a `ThemeToken` of a
`HexColor` (a shadow: each layer's colour; a dash: its lengths), with `ElementTokens.standard` the
table; it has no public constructor, and a consumer restyles any subset with
`standard.copy { accent = ... }`, so a token the contract adds is a member added and no call site
moved. The tokens table's `layout` group, the measures of the panels, the readout and the watermark,
prints as the object `ElementLayout`, each measure a member in camel case (`panel-width` is
`panelWidth`), a length a `Double` of density-independent pixels and a count of columns, a name ending
in `columns`, an `Int`; they read alike in both schemes, so no consumer restyles them. The glyph table's
two sets print as the ones `GlyphSet.marker` and `GlyphSet.control` publish, each glyph's markup by its
name (a glyph's Lucide name is checked and left in `schemas`), and its drawings as `GlyphDrawing.artwork`,
each shape's paint a `HexColor`, one of the tokens, named by its property (`ElementTokens::controlBg`),
or a fill's `GlyphDrawing.Paint.LinearGradient`, its two ends and its stops in the drawing's units, and
each drawing's `opacity` and `GlyphDrawing.Shadow` where the table states them, as the watermark does.
Every printed file's header names the contract's tag and the `modelDigest`, as the client printer's
does, which the task reads off the checkout's `sdk/model.json` and its files. Kotlin cannot add a stored member to a hand-written class from
another file, so each published accessor is a companion member written by hand over the printed
value. The printer refuses, and never skips, a member of a table it has no place for, a word that is
no string, counted word or section, a colour that is not six lowercase digits, a token stated twice
or with no light value, a measure that is no finite number or a count of columns that is no whole
number from 1, a set or a drawing it could not draw, a shadow of a blur below 0, a gradient whose ends
are not two numbers each or whose stops do not run from 0 to 1 in order, and a notice with no ISC and
MIT licence; its golden files are the ones a miniature of each table prints. The tables name no type, so
these files import the few fixed ones they use.

**Where a call is.** A REST operation hangs on `<group>.<resource>`, as
`studio.itineraries.postItineraryDuplicate(...)`; a Tile API operation on `tiles`; a Data API
operation on `data`, under its field's name, as `data.directions(...)`. Each accessor is the
segment capitalised, `Studio`, `Itineraries`, `Tiles`, `Data`, a class of the generated package
with an `internal` constructor taking the transport's seam. Kotlin cannot nest a printed class in
a hand-written one, so the accessors are top-level there, and `Mountaya` holds them. The admin
unit's, `Admin`, is printed into `com.mountaya.admin.generated` and held by no field of the core's:
`mountaya-admin` declares `public val Mountaya.admin: Admin`, which builds it over the client's seam on
each read, so `mountaya.admin.keys.getOrganizationPublishableKeys(…)` reads as a call of the core
does once `com.mountaya.admin.admin` is imported. The accessor holds nothing of its own, and reading
it again reads the same operations.

**How a value is built.** Every printed class has a private constructor and a factory in its
companion, so `RestSchema.ItineraryDuplicateParams(name = ..., segment_ids = ...) { ... }` is the
one way a caller builds one; a response type gets the same factory, which is what a consumer's own
test builds a fixture with. The trailing builder's properties are write-only, through a hidden
getter, so a builder is never read back as if it were the request. A property the contract fixes,
a `const` or a union member's discriminator, is no parameter: it is printed with its value and
always written. A Data API result type has the same factory with every field in the builder.

**A union's members extend it.** A discriminated union is an abstract class with an `internal`
constructor, and each member component is printed extending it, so `is RestSchema.AgentDoneEvent`
is how a caller tells a member, and a request union takes its member as it is,
`body = RestSchema.AgentUserMessageInput(content = ...)`. `Unknown` is the member nobody declared,
holding the object that arrived. A component that is a member of two unions has no Kotlin shape,
since a class extends one class, and the printer refuses it rather than pick one.

**Three states on the wire.** A three-state property is held privately in a `Settable` that is
left out, set to a value, or set to `null`, and its public property reads the value. The client's
`Json` leaves a default out and writes a `null` a property holds, and that pair is what makes an
unset property absent, a required nullable one written as `null`, and a cleared three-state
property written as `null`.

**What a call takes and returns, exactly.** `options: RequestOptions` defaults to the client's own
configuration: `RequestOptions.retry` carries a `RetryPolicy`, public and refusing a negative count
or wait with `ConfigurationException`, which holds the attempts, the backoff and the wait budget a
call may override, and `RequestOptions.timeout` is the call's own attempt budget, refused at zero or
less (§4). The attempt budget is not a retry policy's, since a policy is about the waits between
attempts, which is where `sdk-swift` puts it too. The trailing builder is
`<OperationId>Parameters`, printed in the surface's namespace; a name it would share with a
component is a printer error. An operation whose body the contract accepts in more than one
media type takes one `body` of the class `<OperationId>Body`, printed in the namespace with a
subclass per media type, `Json` and `Multipart`, each carrying the body sent as that type, and a
name it would share with a component is a printer error. No operation takes one at this tag:
`postItineraryPreview` takes `multipart/form-data` alone, so its `body` is the form the model
names, `RestSchema.PostItineraryPreviewMultipartBody`, whose properties become its parts, each a
`MultipartPart`, a file's property a `MultipartPart.File` and every other one's text a
`MultipartPart.Text`, as a call by declaration hands its own (§4). A file's part is sent as the media
type the form's `encoding` in the model names for it, `application/octet-stream` for `file` at this
tag, printed into the call as its `contentType`, and as the client's own default where the model names
none; an `encoding` the printer cannot send, one naming no part of the form, a part that is no file, a
part twice or a media type no header carries, and one on a body that is no form, is a printer error,
never a part sent otherwise. An
operation that declares no metadata returns the envelope's open map as `JsonObject?`, and one
that declares no data returns `data` as a `JsonElement`, a `null` there arriving as `JsonNull`,
which is what the envelope says it is. A file is a `BinaryResponse`, an event stream an
`EventStream<Event>`, a cold `Flow<EventFrame<Event>>` of the client's own on which what a stream
is read as is set (§4), and a paged listing's walk a cold `Flow` of its rows, each returned by a
function that does not suspend, since a `Flow` starts when it is collected, and every other
method suspends. `kotlinx-coroutines-core` and `kotlinx-serialization-json` are `api`
dependencies of `mountaya-client`, since a `Flow` and a `JsonElement`, the type of a value the
contract leaves untyped, sit in its public signatures.

**GeoJSON is declared once, sealed.** `GeoJsonGeometry` is a `sealed interface` over
`GeoJsonPoint`, `GeoJsonMultiPoint`, `GeoJsonLineString`, `GeoJsonMultiLineString`,
`GeoJsonPolygon`, `GeoJsonMultiPolygon` and `GeoJsonGeometryCollection`, positions being
`List<Double>`, each carrying the `bbox` RFC 7946 lets any GeoJSON object carry, read and written
back, null where it carries none, as `sdk-swift`'s geometries do. It is sealed where the contract's unions are not because RFC 7946, not the
contract, closes the set. REST's `GeoJSON*` components print as aliases of them, in the
contract's spelling, and the Data API's `GeoJSON` scalar is `GeoJsonGeometry`.

**The one seam.** A printed method settles what its signature decides, the path substituted and
percent-encoded, the query in the operation's order, the builder's key and tag and the body on
the wire's terms, and hands the call to `OperationSender`, an `InternalMountayaApi` interface (§7),
with the serializers its answer is read with. The transport is the one implementation, so it changes
without touching a printed file, and the admin unit's printed methods hand their calls to the same
transport through it, which the client exposes to them as `Mountaya.sender`. What a printed method
calls of the core beside it, the call it writes (`OperationCall`), the facts its table prints
(`OperationFacts`, `SuccessResponse`, `SuccessKind`, `Pagination`), the encoding of a path segment and
of a timestamp, the client's `Json`, a nullable serializer, a paged builder's cursor and limit, a
three-state property's storage and its serializer, the serializers of a sent web address and of a
timestamp, and a JSON body's text, is `InternalMountayaApi` too, and nothing else is.
A Data API operation's facts are the model's, as a REST
operation's are: the credentials it takes, every Data API operation requiring a session token and
acting as nobody, and the 200 its success arrives with, beside the `timeoutMs` the model carries for
each, none of them the printer's own, and the table prints the model's endpoint, `dataEndpoint`, which a Data API call by
declaration is sent to as a printed one is. A Data API operation is also printed as a value in `DataOperations`, its full
document and its selection tree, which `data.<field>` narrows to what the caller selects.

**Deprecation.** An operation or field marked `deprecated: true` becomes `@Deprecated` carrying
the first sentence of its description, and stays for as long as the contract holds it before
removal. Every module compiles with warnings as errors, so a deprecated property's own factory
and serializer reach it through a declaration that is not deprecated, and only a consumer's use
warns.

**`SCHEMA_VERSION` and `MODEL_DIGEST` are generated**, two `const val`s in `mountaya-client` beside
the surface they describe: the tag the model's `contract` names, and the digest of the five files the
contract's §11 lists, the model and the elements' tables, taken by the printer as that section defines
it. Every printed file's header names both, so a table that moved without a reprint fails
`generate:check` too.

## 4. Transport behaviour

Everything here is `sdk/SPEC.md` §1 to §6, §12 and §13 resolved to Kotlin. A rule the contract
states is named by its capability and not restated; what follows is what it means in this
language. Every network call is a `suspend` function and every stream a `Flow`, and there is no
callback API beside them: a second surface is a second thing to keep in step, and it hands
cancellation back to the consumer. Nothing creates its own `CoroutineScope`: an engine takes the
caller's, since one that owned its scope is one a consumer cannot cancel, and on Android that is
a leak across every configuration change. A dispatcher is injected, never named inside a rule:
the one the client works on is its `dispatcher` (below), and `Dispatchers.Default`, its default, is
named in `seams/` alone.

**The client is `Mountaya`**, the name the Swift and TypeScript clients carry, so one README
sentence reads the same in every SDK. It is filed under `transport/engines/`, beside the transport
it holds, and declared, as every public type of the client is, in `com.mountaya.client` (§6). It is
built as `Mountaya(credential, language, baseUrls, retry, timeout, onResponse, exchange, clock,
random, dispatcher)`, refusing a credential or a language no request could carry and a `timeout` of
zero or less with `ConfigurationException`. `agent`,
`studio`, `tiles` and `data` are its printed accessors, all over the one transport, and the admin
unit's `admin`, which `mountaya-admin` declares on it, is over the same one. The last four
parameters are seams, published so a consumer's test scripts the client the way the conformance
runners do:

- **`exchange: HttpExchange`** is what every attempt leaves through: `suspend fun
  send(ExchangeRequest): ExchangeResponse`, a request whose every header is already written and an
  answer whose `ExchangeBody` is read a chunk at a time, a buffered answer included, so the client
  decides itself how long a read may take. The exchange decides nothing: no redirect, no cookie, no
  header of its own, no deadline. The client closes every body it stops reading, which is what an
  exchange lets its connection go on. It defaults to `SystemExchange()`, the platform's exchange
  over Ktor (below), so `Mountaya(credential)` is a whole client, the parameter order unchanged.
- **`clock: Clock`** is the wall clock, `kotlin.time.Clock`, that a `Retry-After` date, a session
  token's expiry and the two minutes an agent run's opens answered 503 are held to are read against. Every wait and every budget is the caller's coroutine time, a
  `delay` and a `withTimeoutOrNull`, so a test under `runTest` spends none of its own; this is where
  `sdk-swift`'s clock parameter, a monotonic one, differs by idiom.
- **`random: RandomSource`** is what every wait's jitter (`unitDraw`) and every drawn
  `Idempotency-Key` (`cryptographicUuid`, null where the runtime has no cryptographic source, and
  then no key is sent) come from.
- **`dispatcher: CoroutineDispatcher`** is where a buffered answer's body is read and the answer
  decoded, `Dispatchers.Default` where none is given, so a call made on the main thread, from
  `viewModelScope` or a composable, waits there and parses nothing there; the call resumes on the
  caller's own dispatcher once both are done. A success is decoded in one pass over the body's text,
  straight into its types, and never built as a tree of JSON first, and a request's JSON body is
  written straight to the text sent. A multipart body is written into one array sized once, each
  part's content read once and the boundary found in one pass over it, the base lengthened one `x` past
  the longest run of `x` a part holds after it, which is the boundary lengthening it an `x` at a time
  would reach, so a file is copied once, into the body, and the bytes sent are the ones a writer
  appending each piece to the body so far sends. An answer's headers are folded once as the answer
  arrives, each name lowercased, `A` to `Z` alone, and a name sent in two spellings holding both values
  joined in the order of the names, as `Response.headers` holds them, and every header the transport
  reads of it, its hook's report, its error and its response, is one lookup of that fold rather than a
  read of every header again. The read runs inside the attempt's budget, which is the
  caller's coroutine time, so a test under `runTest` passes its own test dispatcher,
  `StandardTestDispatcher(testScheduler)`: on a thread of the platform's the read would leave the
  test's virtual time free to run on, and the budget would be spent before the body arrived. An event
  stream is decoded there too: each chunk is framed and its records' payloads decoded on the dispatcher
  in one hop, and each frame is then handed on in the collector's own coroutine, as a flow's emissions
  must be, so a stream collected on the main thread parses nothing there. A refused open's body is
  read and its error decoded there as a buffered answer's is. The read of a stream's next chunk stays
  the caller's, a suspending read inside the silence window, which waits on the network and parses
  nothing.

**An operation this build prints no method for is called by its declaration.**
`OperationDeclaration(operationId, method, surface, idempotent, credentials, timeout,
declaresIdempotencyKey, isConditional)`, each fact left out the one no operation states by default,
and `Mountaya.send(operation, path, data, metadata, query, headers, body, idempotencyKey, ifMatch,
options)`, which reads the envelope with the two serializers given, its `body` a `RequestBody`, nothing
(`RequestBody.Empty`, the default), a JSON value the client's `Json` writes (`RequestBody.Json`) or a form
of `MultipartPart`s, a field's text or a file's bytes, sent as `multipart/form-data` (`RequestBody.Multipart`),
a file's part with the file name its caller gives it, `MultipartPart.File.filename`, a quote and a line break in
it percent-encoded as HTML's form encoding writes them, and the media type its caller gives it,
`MultipartPart.File.contentType`, or `application/octet-stream` where it gives none, RFC 7578's word for a file of no
known type; a media type no header carries is refused with `ConfigurationException` before the request leaves
(`transport.request.form-parts`),
the two media types the printed surface sends; or `Mountaya.walk(operation, path, row, query, headers, pageSize,
maxPages, options)`, the listing's rows read with the one serializer given (below); or
`Mountaya.events(operation, path, query, options)`, a cold `Flow` of `EventRecord`s read as a
collaboration feed. A Data API operation this build prints no method for is called with
`Mountaya.query(operation, input, result, select, options)`: its `DataOperation`, the name, the input's
type, the field tree and the budget `timeout` it states, its `input` a JSON value the client's `Json`
writes, and its field read with the serializer given, held to every rule a printed method of `data` is,
the session token, the persisted query's digest, the budget, the retries and the ladder; a `select`
outside the tree is a `SelectionException` before any request, as `sdk-swift`'s `query` refuses it. The call is held
to every rule a printed method is. A caller's header naming one the client writes (`X-API-Key`,
`X-Session-Token`, `X-Share-Token`, `Accept-Language`, `X-Mountaya-Client`, `Idempotency-Key`,
`If-Match`, `Last-Event-ID`, `Cookie`), in any case, is refused with `ConfigurationException`, and
so is a name that is no RFC 9110 token, one the HTTP stack writes itself (`Transfer-Encoding`,
`Upgrade`), and a value holding a control character or a character outside printable ASCII, which
OkHttp does not send, every target held to it alike, as the contract holds every SDK (`sdk/SPEC.md` §1 and §13). Before any request the checks run in this order: the
credential and the language, the call's budget, the caller's headers, a buffered `Accept` asking for a
stream, the `If-Match`, then the `Idempotency-Key`, drawn last. Every attempt is held to the same rule as
it is written, a cookie a share token's function answers and the position a stream's reopen resumes from
among its headers, and one the stack would not send is refused with `ConfigurationException` before it
leaves and never repeated, where the stack would refuse it as it is sent, which reads as a request that
never arrived and is repeated. `Surface` is public for the declaration's sake, and carries and prints the contract's word for its surface (`rest`, `tiles`, `data`), as `sdk-swift`'s raw values do.

**Base URLs.** Each surface has its own default origin, and a client overrides each one
independently (`transport.base-urls`). `BaseUrls` takes one `Url` per surface, each defaulting to
Mountaya's own, and `BaseUrls.standard` is the set a client uses unless told otherwise. It is a
companion `val` and so `lowerCamelCase` (§5), the word every SDK gives a configuration type's default
instance, so one README sentence names it in all three.

**Language.** A client is configured with its language once, a language tag given to `Mountaya`,
and no method takes one: it rides every request as `Accept-Language`, an operation that takes no
credential included, and every event stream's open, held to the same header rules as a
credential (`transport.credentials`) and refused with `ConfigurationException` where the client
is built. A client given no language sends `Accept-Language: *`, which RFC 9110 reads as no
language in particular, as it reads no header at all (`transport.credentials`): the credential's
projection writes it, so `Credential.headers` answers it, every attempt carries it on every engine
and an exchange of the caller's own sees it, and URLSession, which would otherwise send the device's
languages, never chooses the answer's words for a client that asked for none. A map given no label
language of its own takes this one (§8).

**Identification.** Every request names the SDK that made it, as `X-Mountaya-Client:
mountaya-kotlin/<version>` (`sdk/SPEC.md` §1): every attempt the transport sends, on every engine,
an operation that takes no credential and every event stream's open and reopen included, and every
Tile API request a map makes for itself, among the headers `mapRequestHeaders` answers (§8). It is
the client's, as the language is: no method takes it, a call by its declaration that names it is
refused, and it is no part of a credential's projection, so `Credential.headers` answers without it
and `transport.credentials` holds that projection alone. `<version>` is the version every module is
published at, the version catalog's `mountaya`, and never the contract tag `SCHEMA_VERSION` holds.
The catalog is the one place it is written: `generateClientVersion`, a build-logic task, prints it at
every build into an `internal` constant in a source directory of the build's own, which common code
compiles with, so no copy sits in the source tree and no release names the SDK by a version it is not
published at. A jvm test reads the catalog itself and holds the constant to it.

**Credentials.** One `sealed interface Credential`, an implementation per shape, so a
combination prose cannot prevent is unrepresentable:

| Implementation | Sends | Carries | Why this shape |
| --- | --- | --- | --- |
| `Credential.Publishable` | `pk_` as `X-API-Key`, and `sess_` as `X-Session-Token` where a session token is given | the key, and optionally a `SessionToken`: a value, a session as `postSession` answered it, which names the user it was minted for, a function read for every request, or a source that keeps one fresh, `SessionTokenEngine` or one of the application's own | a session token ACCOMPANIES a publishable key, so it rides here; sent alone it is a 401 |
| `Credential.Secret` | `sk_` as `X-API-Key` | the key | minting session tokens, server-side only; constructible on the jvm target alone, below |
| `Credential.Share` | `share_` as `X-Share-Token` | the token, and the `CookieSource` it reads the visitor's cookies from where it asks for them | a share token REPLACES a key, so it is a shape of its own with nothing beside it: sent both, the server reads the key and ignores the token |

- **No header names a user.** The user an operation acts as is the one its session token was minted
  for, and an `X-User-Id` a client sends is ignored, so none is sent. Each operation's printed
  facts carry its `x-acting-user`, `required`, `optional` or `none`. Where the client is told whom
  its token names, because the token is a minted session (`SessionToken.Minted`, holding what
  `postSession` answered) or comes from `SessionTokenEngine`, whose snapshot reports the `userId`
  each token was minted for, an operation declaring `required` called under a token minted without
  `user_id` is refused with `ConfigurationException` before any request. A token given as a value
  (`SessionToken.Value`) or a function does not tell the client whom it names, so that request goes
  out for the server to decide.
- **What an operation declares decides what it refuses, never its method.** A secret key where a
  secret key does not authenticate the operation, a share token where it declares none, reads
  included but for a read under one that asks for the visitor's cookies (below), and no session
  token where every requirement names one, but for that same read, are each refused with
  `ConfigurationException`; an operation that takes no credential is sent none and refuses none.
  A key or a token that is empty or whitespace is a missing credential, and a member holding a
  control character or a character outside printable ASCII is refused, since OkHttp, which the jvm
  and Android send with, carries no other in a header, Latin-1 among them, and every target is held to
  it alike, as the contract holds every SDK (`sdk/SPEC.md` §1 and §13).
  `transport.credentials` proves the projection and every refusal. The sixth fact,
  `pendingInvitee`, is printed with the others and decides nothing locally.
- **The local check is the prefix, never the length.** A wrong prefix on any credential is a
  `ConfigurationException` before a request leaves the process. A length check would be a second
  copy of the server's format, and the day the format widens it refuses a key the server accepts.
- **A credential prints each key and token by its prefix alone**
  (`Credential.Secret(key=sk_…)`), in the shape the Swift SDK prints, so a credential logged while
  a client is set up shows which part sits where and never the part itself.
- **A session token's function does not suspend.** It is read for every request, and MapLibre's
  HTTP client reads it for every tile on a thread of its own that cannot suspend (§8); a token
  that has to be fetched is the engine's job, whose snapshot that thread reads. One that answers
  a wrong prefix is refused at that read.
- **A public factory from the separate parts** (publishable key, session token, share token)
  applies "a share token replaces the key" itself. A caller holding a key from configuration and
  a share token from a link does not write the precedence rule, so no two apps write it
  differently; the fixture's `share-token-wins-over-a-key-beside-it` runs through it. It is a
  function named `Credential`, the Kotlin spelling of a factory named after what it returns, and
  `Credential.Publishable` and `Credential.Share` are also built by their own constructors. Every
  way of building a credential checks its prefixes there and then, so a malformed one never
  exists to reach an operation.
- **The secret key cannot be constructed on an app platform.** The two public ways to construct
  `Credential.Secret`, `Credential.Secret(key)` and the factory overload that takes a secret key,
  are declared in `jvmMain`, so an Android or iOS build has no symbol to call: a compile-time
  barrier, where a runtime check would ship the credential and hope. The jvm target keeps both,
  because servers, command-line tools and desktop processes run there, and `sdk/SPEC.md` §1 counts
  none of them an app build: a desktop process may hold the secret key, and an application shipped to
  other people's desktops keeps it out of itself as a server's code keeps it out of a page.
  `Credential.Secret`'s constructor is `internal`, and the class declares an empty companion, which
  the jvm gives `operator fun Credential.Secret.Companion.invoke(key)`, so `Credential.Secret(key)`
  reads as a constructor's call on the jvm, as `Credential.Publishable(key)` does everywhere, once
  `com.mountaya.client.invoke` is imported, and resolves to nothing on Android or iOS. The overload
  repeats the common factory's parameters and adds `secretKey` last, with no default, so a call that
  does not name it, `Credential("pk_...")` included, resolves to the common factory and is never
  ambiguous on the jvm.
- **A request waits for a first session token on its way.** A request made while its source, the
  engine or one of the application's own, holds no token and reports a mint in flight, to an
  operation a session token can ride on, suspends on the source's `StateFlow` until it holds a token
  or reports none on its way, bounded by the caller's cancellation and the attempt's deadline, and
  is sent with the token once there is one. Once the engine reports none on its way, the request goes out under the key alone, and
  an operation that requires a session is refused with `ConfigurationException` carrying the
  failed mint's exception as its `cause`; a wait the deadline ends is a `ConnectionException`
  (`transport.session.first-token`).
- **No cookie rides a key, on any engine.** `transport.browser.cookie-mode` proves the rule, and
  it is run, not scoped: its name says browser, and the ambient jar it guards against is the
  Darwin engine's here. OkHttp keeps no cookies unless it is given a jar, and the jvm and android
  `actual` gives it `CookieJar.NO_COOKIES`; Darwin is `URLSession`, which sends what
  `NSHTTPCookieStorage` holds for the API's host unless told otherwise, so the iOS `actual` builds
  an ephemeral session with no cookie storage, a policy that accepts none and
  `HTTPShouldSetCookies` off, and turns `HTTPShouldHandleCookies` off on every request; Ktor's
  client installs no `HttpCookies`. Cookies therefore come from nowhere ambient, on any target,
  and a `Cookie` the client writes is sent as written. A share token that asks for the visitor's cookies carries the
  `CookieSource` they are read from: a function that does not suspend, for the reason a session
  token's does, answering the `Cookie` header for a request's URL. Only a request made under that
  share token, to an operation that takes a credential, carries what it answers, and a read that
  declares no share token is sent the cookies alone and not the token, as the fixture has it, one
  whose operation requires a session token among them, since a session the visitor's cookie holds is
  invisible to the client and plain to the server, which is the one to judge it; a write under it is
  refused with `ConfigurationException`, as every write under a share token is
  (`transport.credentials`). A read is a request sent with `GET`, `HEAD` or `OPTIONS`, in any letter
  case, or one of an operation declared a read whatever its method, `CredentialDeclaration.read`,
  which every Data API query is over its `POST`: the printer states it on every Data API operation's
  facts, `OperationFacts(DataOperation)` on a caller's, and `Credential.headers` takes the call's
  method, `GET` unless it names another, beside the declaration, to tell the two apart (`sdk/SPEC.md`
  §1). Where the visitor's cookies live, a WebView's store
  or the shared cookie storage, is the app's to know, and `mountaya-client` imports no
  `android.*` to read one.

**Session engine.** `SessionTokenEngine(scope, mint, clock)` in `mountaya-client`, running in the
caller's `CoroutineScope`, waiting on that scope's dispatcher and reading expiries against the
injected wall clock. It never mints itself: minting exchanges a secret key, so the engine takes a
`mint` function that calls the application's own endpoint, which answers the token, its expiry and
the `user_id` it was minted for. It is a `SessionTokenSource`, the public interface a source of the
application's own implements too: `snapshot: StateFlow<SessionSnapshot>`, whose `value` is the state
now, read without suspending from any thread, and whose collection is the subscription, as an engine's
`state` is (§5). `SessionSnapshot` holds the token, its expiry, its user, whether a mint is in flight and
the last mint's failure, equal where the first four are and both or neither failed, and prints its
token by its prefix alone. A `SessionToken.Source` carries any such source, and a
`SessionToken.Function` is read for every request; both are held to a token's rules at that read.
`refresh()` mints at once while a subscription is held, `disable()` and `enable()` stop and resume
it, and a failed mint keeps the token held, records its failure and waits as a spent token does, a
timeout the application set on its own mint, the `TimeoutCancellationException` its `withTimeout`
throws, among the failures. Any other cancellation the mint throws, the application cancelling its
own call, ends that run of minting, the cancellation its last error and nothing minting, so a request
awaiting a first token goes on at once, and the next mint is a `refresh()`'s, the next subscription's
or an `enable()`'s (`transport.session.refresh`); the engine's own cancellation, its scope's, ends
the loop and the mint in flight with it.

- **Refresh happens before expiry, never after a 401**, because the request that discovers an
  expiry is the one the user is waiting on, and because an event stream opened with a token ends
  when that token expires (§12): the stream is reopened with the next one.
- **One writer, and a snapshot readable from any thread.** The state is a `StateFlow`, `snapshot`:
  its `value` is the snapshot and its collection is the subscription, the shape
  `transport.session.observable` proves, the registry's `SessionTokenEngine.snapshot`. MapLibre's HTTP client asks for a tile's request on a thread of its own and cannot
  suspend (§8), which `value` serves. The snapshot carries the token, its expiry, the user it
  names, whether a mint is in flight and the last error, as one value replaced on every change
  rather than mutated.
- **The subscriptions are the lifecycle.** Minting runs only while the flow has a collector, read
  from its own `subscriptionCount`, and the last collector leaving parks it: a credential nothing
  holds is not worth a call to the application's endpoint. A collector arriving after the
  application cancelled a mint starts minting again, whatever collector was held all along, as
  `sdk-swift` and `sdk-dart` start on each new subscription: the count rising is an arrival, and one
  arriving while the engine mints on its schedule restarts nothing. A `Mountaya` whose credential carries
  the engine collects it itself until the client is closed, so a headless app that never renders
  still keeps its session fresh.
- **Disabling clears the token, its expiry, its user and the last error**, so a signed-out user's
  credential cannot be read off the snapshot.
- **Replacing `mint` does not re-mint.** A composable passes a new lambda on every recomposition,
  and an engine that re-minted on each one would call the endpoint on every recomposition.
- **The schedule is the contract's** (`transport.session.refresh`). The margin is proportional
  and capped; a delay under the floor is not armed, and each consecutive spent token doubles the
  wait up to the margin; an expiry that does not parse takes the flat margin; the wait is an
  absolute instant walked down in hops that re-read the wall clock, and a hop is not a mint. The
  clock is a seam.
- **The schedule reads the expiry the mint returns.** A token lives at most five minutes, and
  one minted moments earlier for the same key may come back with less of that left, so a
  schedule that assumed a full lifetime would refresh late.
- **Resuming is the hop rule's job.** The Data API needs a token on every request, and a `delay`
  on a device in Doze does not fire on time. The next hop re-reads the wall clock, sees the
  deadline passed and mints.
- **The transport reads the snapshot on every request**, so a refreshed token reaches the next
  request without rebuilding the client.

**Retries.** `sdk/SPEC.md` §2 exactly, proven by `transport.retry.eligibility`,
`transport.retry.backoff`, `transport.retry.timeout`, `transport.timeout.budget` and
`transport.idempotency.*`.

- **What is repeated.** A 429 always but a ceiling's, coded `LIMIT_REACHED`, which names no wait
  and settles at once whatever the operation declares; a 409 coded `IDEMPOTENCY_KEY_IN_USE` where
  the request carried a key, counted against no attempt; a 502, 503, 504 or a network failure where the
  operation declares itself idempotent, which on REST is its `x-idempotent` and on the Data API
  is every operation, or where the request carried a key. A 412 and a 409 coded
  `IDEMPOTENCY_ANSWER_UNAVAILABLE` are never repeated. `x-idempotent` promises that a repeat has
  no second effect, not that it gets the same answer; the key is what makes a repeated write
  answer as the first did, so a keyed delete repeated after it landed is answered with the 200
  the API kept, never a 404.
- **One `Idempotency-Key` per write call.** A call to an operation that declares the header
  sends the key its caller set, or else a version 4 UUID drawn once, before the first attempt,
  and the same key on every attempt. The draw goes through a seam whose platform implementation
  is `kotlin.uuid.Uuid.random()`, which reads the platform's cryptographic random source on every
  target, since `Uuid.random()` is refused outside `seams/` (§7). A key no header can carry is
  refused with `ConfigurationException` before any request.
- **The delay is a uniform draw from zero to a capped exponential**, and a `Retry-After` value,
  read in both of its spellings and ignored where it is neither (`transport.errors.retry-after`),
  replaces it with its seconds plus a uniform draw of up to one second, both draws taken from the
  `RandomSource` seam. An HTTP date is read as the whole seconds to it, rounded up, by the one
  parser the error's `retryAfter` is read by too, so a repeat never comes back before the date and
  the wait taken is the wait the error names, as `sdk-dart` reads it. The client honours one
  wherever it comes from and never waits for one that does not come.
- **Every wait of a call comes out of one budget.** A wait is taken only where it fits what the
  waits before it left; one that does not fit is not taken, and the attempt settles with the
  error its status maps to, carrying the seconds the answer named.
- **Each attempt has its own budget**: the call's own where it names one, else the larger of the
  client's and the operation's `timeoutMs` plus five seconds, so a Data API attempt waits 35
  seconds by default. The transport holds it as a coroutine deadline, `withTimeoutOrNull` around
  the attempt, rather than as Ktor's `HttpTimeout`. The deadline is the same code on every engine,
  where `HttpTimeout` is each engine's own reading of a timeout; it covers the whole attempt, the
  wait for a first session token and the read of the body included; and it answers its end as a
  value, so the transport tells a spent budget from the caller's own cancellation, which
  `withTimeout`'s `TimeoutCancellationException` would make one more `CancellationException`. An
  attempt that spends it is a `ConnectionException`, repeated as one.
- **The defaults are the contract's**: two attempts after the first, a 500 ms base, a 30 s
  ceiling and a 60 s wait budget, each configurable per client and per call through `RetryPolicy`,
  and a 30 s attempt budget, configurable per client as `Mountaya`'s `timeout` and per call as
  `RequestOptions.timeout`.
- **A key in use is asked again a second apart at least.** A 409 coded `IDEMPOTENCY_KEY_IN_USE`
  waits what its `Retry-After` names plus the draw, and never less than one second, the minimum
  applied before the wait is held to the budget, so every repeat spends the budget that bounds it;
  it takes the draw too, and advances neither the schedule's exponent nor the count of attempts.

The wait is `delay`, which is cancellable by construction, so a cancellation settles during the
delay rather than after it (`transport.retry.interruptible`). Cancellation is checked before
`delay` is called, because `delay` arms its timer before it looks and returns from a wait of zero
without looking at all, which would send the next attempt for a caller who left; a coroutine
cancelled before the wait therefore never arms one. The rule is still asserted, because a rewrite
onto a blocking sleep would lose it silently.

**Limits are reported, never counted against.** `RateLimit-Policy` and `RateLimit` are read into
one policy per String member, in the order the answer lists them, each with its name, quota,
window, what is left and when it resets, and a field that does not parse is ignored whole
(`transport.ratelimit.parse`). The state rides every error the API answered and reaches the
`onResponse` hook a client may be given, which is told of every attempt an answer came back to:
the operation, the surface, the status, the attempt, whether the answer was replayed for a key,
the request id and the rate-limit state. The hook is a function that does not suspend, so it
cannot hold the call up on a wait of its own, and an exception it throws is dropped, so nothing
it does changes what the call answers. A client delays no request on what the state says, and
publishes no figure of its own beside it, since an organization's figures are its agreement's and
the API is what holds them.

**Errors.** `sdk/SPEC.md` §3's ladder, under one `abstract class MountayaException` whose
constructor is `internal`. It is not sealed, for the reason a union is not (§3): the ladder grows
when the contract adds a class, and a sealed class makes a consumer's
`when` exhaustive, so the class added later would break every such `when` at compile time. Abstract
with an internal constructor keeps the hierarchy this module's alone and asks a `when` for an
`else`, which is what Swift's `@nonexhaustive` asks of a `switch`.

| Contract class | Kotlin |
| --- | --- |
| `ApiError` | `ApiException`, the class every row that carries a status extends; a status with no class of its own is an `ApiException` itself |
| each row that carries a status, from `BadRequestError` to `ServerError` | a subclass of `ApiException` per row: `BadRequestException`, `AuthenticationException`, `BillingException`, `ForbiddenException`, `NotFoundException`, `MethodNotAllowedException`, `ConflictException`, `PreconditionFailedException`, `PayloadTooLargeException`, `RateLimitException`, `ServerException` |
| `IdempotencyKeyInUseError`, `IdempotentAnswerUnavailableError` | `IdempotencyKeyInUseException` and `IdempotentAnswerUnavailableException`, subclasses of `ConflictException` |
| `DataError` | `DataException`, a subclass of `ApiException` carrying the 200 it came under |
| `ConnectionError` | `ConnectionException` |
| `ConfigurationError` | `ConfigurationException` |
| `SelectionError` | `SelectionException` |
| `WebhookSignatureError` | `WebhookSignatureException`, carrying its `reason` in the contract's spelling |

- **The suffix is `Exception`, one rule applied to every class**, as `sdk/SPEC.md` §3 lets an
  ecosystem whose word for a failure a caller handles is not `Error` write its own: on the JVM an
  `Error` is `java.lang.Error`, a failure nobody is meant to catch, and a catchable failure named
  `...Error` tells a JVM reader the opposite of the truth. The name each class declares as data stays
  the contract's, and the registry's `kotlin` column records each spelling, `ApiException`,
  `isApiException`.
- **Catching `ApiException` catches every error the API answered**, which is the contract's base:
  it separates a failure of the API from a bug in the calling program. A `DataException` is one,
  an error the API answered under a 200, so it extends `ApiException` rather than standing beside
  it.
- **The classes are the contract's rows.** Each 4xx has its own and the whole 5xx range has one. A
  402 is one `BillingException` and a 429 one `RateLimitException` whatever their entries carry. A
  ceiling, the seats, the published itineraries or the published zones an arrangement allows, the
  invitations an organization may have waiting or the itineraries a personal space keeps, is the
  429 coded `LIMIT_REACHED`: its `metadata` names the ceiling with what it allows and what is held,
  it names no wait, so its `retryAfter` is null, and a caller tells it from a spent window by its
  `code`. The two 409s that answer an `Idempotency-Key` are classes of their own beneath
  `ConflictException`, so a caller catching `ConflictException` catches both, and one that catches
  either first reads it as what it is.
- **`ConflictException` is the one row with rows beneath it**, so it is `open`, and its
  constructor is `internal` like `ApiException`'s: a public constructor on an open class would let
  a consumer's own class into the hierarchy. A consumer builds one by the class's name all the
  same, through the `invoke` of its companion, the spelling every printed class's factory takes: a
  top-level function of that name would be a conflicting overload of the internal constructor.
- **Every class declares the contract's name as data** (`"PreconditionFailedError"`), which is
  what a fixture's `expect` names and what the runner compares, and a failure prints as that name
  and its message (`RateLimitError: 429 LIMIT_REACHED: Seats used up. No wait lifts a ceiling, ...`),
  the line `sdk-swift` prints. The reason is R8, and it is not
  the one usually given: R8 renames a class and every reference to it together, so an `is` check
  and a `when` survive a release build, but `::class.simpleName` reports the renamed class. A
  Kotlin caller matches with `is`, the cheaper structural discriminator the contract allows; the
  declared name is for the predicates, the runner and a reader of a log.
- **An error's message ends on what a caller can do about it**, where its status names a step: the
  credential, the member's token or the secret key to check on a 401, where a 402's remedies are
  taken, the scope, the member's role or the key and user a mint names on a 403, the write made or
  the key still running on a 409 that answers a key, a stale tag on a 412, and on a 429 a ceiling's
  remedies, the instant a spent monthly allowance renews, or the wait, with what a mint is limited
  by. Each is the first SDK's sentence word for word, so a log reads alike in every SDK, and what
  the call's operation declares sharpens it: the transport hands each error it raises the
  operation's credential facts and the instant its answer arrived, and an error a caller builds
  carries neither, so it names the steps that need none. The message is the API's own words, a
  period where they end in none, then the step; no public property moves.
- **The two predicates of `sdk/SPEC.md` §3 are published**: `isMountayaException`, whether a value
  is an error of the family, and `isApiException`, whether it is one the API answered, top-level
  functions taking any `Throwable?`, so a caller never needs an identity check either. A
  `WebhookSignatureException` is of the family and no answer of the API: a receiver's verification
  of a webhook delivery raises it, and no answer of the API does. Each reads
  the contract name the value declares, never its class: a `NamedException`, the interface every
  `MountayaException` implements, declares it, so a value of another module's class that declares a
  name of the family, a failure rebuilt past a boundary the client's own classes did not cross, is
  recognised by it as one of the client's own is, and a value that declares no name of the family is
  none of its errors, whatever status it carries (`transport.errors.structural`).
- **What an error carries is the contract's list** (`transport.errors.envelope`,
  `transport.errors.request-id`, `transport.errors.idempotency-key`). An `ApiException` carries
  the status, every envelope entry, the classifying code (the first entry's), `extensions.metadata`
  exactly as sent, as a `JsonElement`, the whole seconds a `Retry-After` named, as a `Duration`, an
  HTTP date rounded up to them, the `X-Request-Id` exactly as sent, the rate-limit state and the
  `ETag` exactly as sent, each `null` where the answer carries none. The code, `ErrorEntry.code` and
  `ApiException.code`, is an `ApiErrorCode`, the one open vocabulary `sdk/SPEC.md` §3 types every code
  by: the union of the codes the Map Studio API's and the Tile API's `ErrorExtensionsCode` and the Data
  API's `ErrorCode` declare, as `sdk/model.json` names them, printed from the model beside the surface
  (§3) as every open vocabulary of the model is, a value class over the code as sent with a companion
  constant for each code a surface declares. It stays open, so a code a later release of the API sends
  is kept exactly as sent and never refused. `ApiException.hasCode` answers whether any entry carries a
  given code, the first or another, since a refusal may name several at once. An entry is an object with a
  string `message`, its code its `extensions.code` where that is a string; a field path is a list of
  keys and indices, kept only where every step is a string or a whole number, and an index stays an
  `Int`. `errors` present and not null makes a Data API success an error even with no entry in it. A
  `PreconditionFailedException`'s `etag` is the resource's tag now, and an
  `IdempotentAnswerUnavailableException`'s `answer` is that answer's status, `Location` and `ETag` as
  `extensions.metadata` states them, each null where it states none of its type. Every error a keyed
  call raises carries its key, whatever the error, from the moment the key is fixed before the
  first attempt, so `idempotencyKey` is a property of `MountayaException` itself rather than of
  `ApiException` alone, and `ConnectionException` and `ConfigurationException` take it beside their
  `cause`.
- **`ApiException.of` applies the ladder** to an answer's status, entries, metadata and headers,
  returning the subclass of the row a status and its code fall on, or an `ApiException` itself
  where the status has none; it is what the transport and the ladder's runner call
  (`transport.errors.ladder`), and it takes what the answer carried, each by name. `ApiException`'s
  own constructor is `internal`, so the hierarchy stays closed outside this module, while each final
  subclass has a public constructor whose status its class implies (`ServerException` takes its
  5xx), taking the same parts, so a consumer's test can throw the failure it means to handle.
- **A success whose body does not decode is the base `ApiException`**, carrying the status, the
  request id, the rate-limit state and the key, the decoder's failure as its `cause`, raised from
  one place in the transport, never a `ConnectionException`, since the answer came
  (`transport.errors.ladder`). The cause carries no byte of the body: kotlinx.serialization ends a
  decoding failure's message with `JSON input:` and the input itself, the whole of it under 200
  characters and the 60 around the failure otherwise, so the cause is the decoder's failure rebuilt
  as a `SerializationException` holding its message up to that copy, where and why the body did not
  decode, and no cause of its own. An answer carrying a session token, a setup intent's
  `client_secret`, a share link's token or a webhook endpoint's signing secret would otherwise put it
  in every log that prints the error, which `sdk-swift`'s and `sdk-dart`'s decoders never write.
- **An underlying platform or decoder exception may ride along as the `cause`** for diagnostics,
  never as the type a caller matches on, since matching it would tie a caller to how this SDK
  happens to be built.
- **Cancellation is `CancellationException`, never wrapped.** Structured concurrency reads
  cancellation by that type, and a wrapped one reads as a failure that cancels the caller's
  parent. A cancelled call never becomes a `ConnectionException` either, because a
  `ConnectionException` is retried and a retry after the caller left is a request nobody is
  waiting for. The platform's own report of a request it cancelled, OkHttp's `Canceled` and
  URLSession's `NSURLErrorCancelled`, is thrown by the platform's exchange as a
  `CancellationException`, at the answer and in its body, as `sdk-swift`'s `SystemExchange` throws
  `CancellationError`; and a `CancellationException` an exchange throws passes through the transport
  and a stream's reader as it is, never read as a request that never arrived and never repeated.

**Dates.** `sdk/SPEC.md` §4's anchored rule, decided by declared type as the contract decides it:
a property the bundle declares `format: date-time`, and the Data API's `DateTime` scalar, are a
`kotlin.time.Instant`, and every other string stays a string, nothing inside an object the
contract leaves open included. A custom `KSerializer` checks the anchor before `Instant.parse`
sees the value: what a parser accepts is the library's decision and can move with a release,
where the anchor is the contract's. The anchor is published, `parseTimestamp(value)`, which answers
the instant or null, so a rule that holds a timestamp as text reads it as the wire is read, Geo's
reading of a place's `date` field among them, as `sdk-swift` and `sdk-dart` publish theirs.
`transport.dates.reviver` runs against that one parse. An
`Instant` holds nanoseconds, so a fraction keeps its first nine digits and the digits past them
are dropped before the parse, never rounded. A declared value the anchor does not take fails the
decode, as `sdk/SPEC.md` §4 has it, and raises the base `ApiException` a body that does not decode
raises (`transport.errors.ladder`), the decoder's failure as its cause.

**Field selection.** The field tree is generated beside the operations, in the SDL's order. A
selection is validated against it before the request is built, and a field outside it throws
`SelectionException` at the call rather than failing at the server; an absent selection sends the
generated full document unchanged. Kotlin cannot compute a result type from a selection, so
`select` narrows the document and not the type (§11): the result type is returned whole, which is
why every field of a Data API result type is nullable with a `null` default in Kotlin, since a
field the caller did not select is absent from the response and kotlinx.serialization refuses a
missing field that has no default.

A selection is a `FieldSelection`, built by a function of that name: `FieldSelection {
field("routes") { field("distance") } }`, `field(name)` selecting a field whole and a block
selecting fields beneath it, a name given twice taking the last. One type serves the generated
tree and the caller's narrowing, and `DataOperation` carries an operation's name, input type and
tree and prints its document from them, which a jvm test holds to every document `sdk/model.json`
carries. The narrowed document is printed in the tree's order, whatever order the caller wrote, so
two selections of the same fields send the same bytes and the same digest
(`transport.data.selection`). An object selected without a block is selected whole, with every
field beneath it, its whole subtree, as `true` selects one in every SDK; a value given a block, and
a selection or a block that names nothing, are refused with `SelectionException` as a field outside
the tree is.

**Persisted queries.** The digest is SHA-256 over the document's exact UTF-8 bytes, hex-encoded
in lowercase, from the `seams/` actual: `MessageDigest` on jvm and android, CommonCrypto on iOS
(`transport.data.apq-digest`). A digest-only request's refusal is read before the rule that a 200
carrying errors is a failure, and a failing status is read for it too, before the status makes the
answer a failure: the Data API with the extension turned off answers a digest-only attempt
`GRAPHQL_VALIDATION_FAILED` under 422, or 400 where the answer is negotiated as
`application/graphql-response+json`, where a digest it does not hold yet is answered under 200, as
`sdk-swift` reads it. A server answering "not implemented" opts the transport out for its
lifetime, an answer carrying both that and a request to register the document reads as "not
implemented", and `GRAPHQL_VALIDATION_FAILED` with the message `no operation provided` reads so
only on a digest-only attempt; a rejected digest is dropped rather than remembered, because
memoising a failure turns one bad moment into a permanent opt-out (`transport.data.apq-refusal`).

**One walk reads a paged listing.** Every operation the model marks with `pagination` is read
through one generic walk, `PageWalk(pageSize, maxPages) { paged -> ... }`, a cold `Flow` of rows
returned by a function that does not suspend, which requests a page only as its collector takes the
rows before it. Its lambda calls the paged method with the caller's own trailing builder and
applies `paged` last in it: `client.studio.collections.getCollectionItineraries(id) { sort = name;
paged() }`, and the admin unit's alike, `client.admin.members.getOrganizationMembers(id) { role = admin;
paged() }`. The builder sets its filters and `sort` on every request, then `paged` sets `limit`
and `cursor` over whatever the builder set, and the first request carries the `cursor` the builder
named, so a walk resumes where an earlier one stopped. The printer has each paged operation's
builder extend `PagedParameters`, a public class whose constructor and whose two members, the cursor
and the limit, are `InternalMountayaApi` (§7), so the admin unit's printed builders extend it from beside
the core and no consumer reaches them: they are the one way the walk reads that cursor back, since a
builder's properties are otherwise write-only (§3), and each printed override carries the marker too;
a page whose call never applies `paged` is refused with
`ConfigurationException`. `PageWalk.defaultPageSize` and `PageWalk.maxPageSize` are published. The
rows of a page naming a cursor already sent are handed on before the base `ApiException`, carrying
that page's status and request id, is raised. Every paged method this build prints has its walk
printed beside it, which builds that one: `client.studio.itineraries.walkGetItineraries { sort = ... }`
walks the listing at the page size the operation's `limit` states, 50 at this tag, with the method's
arguments, its options and its builder, its rows typed as the listing types them (§3). A listing this
build prints no method for is walked by its declaration, `Mountaya.walk`, as `sdk-swift` walks any
query that conforms to its `PagedQuery`: each request is a call by declaration, its query the caller's
names and values, in their order, with the first `cursor` among them the one the walk resumes from, and
every `cursor` and `limit` among them replaced by the walk's, which follow the rest; each page's rows are
read with the serializer given, and the next cursor from the envelope's open `extensions.metadata`, its
`pagination.next_cursor`, a map naming no cursor as text ending the walk as a null one does. Both walks go
through one internal loop, so the two read a listing alike. It follows a page that holds fewer rows than asked, or none, for as long as a
`next_cursor` names one; it stops at a null `next_cursor`, at a ceiling on requests the caller
states, which raises nothing, or at a cursor it has already sent, which raises the base
`ApiException`. The page size is 50 unless the caller names another, and there is no ceiling
unless the caller states one. A page size outside 1 to 100 or a ceiling under 1 is a
`ConfigurationException` at the call, before any flow is collected, as Kotlin's own `take`
refuses a negative count. A collector that is cancelled, or that stops early with `take` or
`first`, requests no further page, and the walk ends with the collector's own
`CancellationException`, never as a listing read to its end (`transport.pagination.walk`).

**A conditional write sends the tag its caller names.** The builder's `ifMatch` is sent as
`If-Match` exactly as given, on every attempt of the call, and the client never writes a tag the
caller did not set, whatever tag it has seen. A tag no header can carry is a
`ConfigurationException` before any request. A 412 is a `PreconditionFailedException` carrying
the resource's current `ETag`, and it is never repeated: the caller reads the resource again and
decides what the write should be now (`transport.conditional.if-match`).

**An event stream is a stream, from open to end.** The printed stream methods and
`Mountaya.events` are the only way to open one, and the transport refuses a buffered request whose `Accept` names `text/event-stream`
before its first attempt (`transport.events.method-refusal`). A stream method returns an
`EventStream`, a cold `Flow` of the client's own: collecting it opens the stream, a second
collection opens a second one, and a collector that is cancelled or stops early closes it, an
open whose answer arrives as the collector goes included: the answer is let go, so the platform's call
and its connection end with it rather than stay open until the server ends them, each an open stream
the API counts against its user, as `sdk-swift`'s and `sdk-dart`'s let theirs go. It
implements `Flow` itself, which kotlinx.coroutines leaves open to inheritance, so every operator
applies to it, and what a stream is read as is set on it before any operator, which would hide it. A frame is read by the Server-Sent Events rules the
contract states, UTF-8 decoded across reads (`transport.events.framing`). The open waits one
attempt budget, and once the answer has arrived a stream that delivers nothing for 45 seconds is
a `ConnectionException`. That window bounds each read from the network and never an `emit`,
which suspends for as long as the collector holds the frame, so the collector's own work is never
the stream's silence (`transport.events.stall`). A failed open is an exception of the ladder,
carrying its request id (`transport.events.open-errors`). When a stream ends, when it is
reopened, after how long and from which record are §12's (`transport.events.terminal`,
`transport.events.reconnect`): the flow applies them itself, waits for a first session token
before a reopen as a request does, and sends the position of the last record it emitted as
`Last-Event-ID`. An exception the collector throws reaches it back through `emit`, and the flow,
held to Flow's exception transparency, never catches it, which is the end §12 gives a failure the
reader's own handler raises.

- **A record is read into its event by its stream's reading.** The record's `event:` name is folded
  into its data as `type`, whatever the data says there, and the union's own serializer decodes it.
  A record naming an event this build does not know, or carrying data that does not decode, is
  passed over: handed to nobody, the position unmoved. An agent run's `done` ends the stream and
  moves the position, its `reconnect` is handed to nobody and reopens at once, and every other member
  is ordinary; a collaboration feed's five change frames are ordinary, its roster and lock
  snapshots open it, its other live frames move no position, and its `reconnect` reopens. A member
  the contract adds is ordinary until its part is written here, since Kotlin has no exhaustive
  `when` over an open union to stop the build, which is where `sdk-swift`'s compiler stops it; each
  reading's test names the part of every member printed today. A stream by declaration hands every
  record on, ordinary but for a `reconnect` by name.
- **A reader reads by the rules of the stream its operation opens**, since one union is read by
  streams of more than one kind. An itinerary's and a zone's feeds, `getItineraryEvents` and
  `getZoneEvents`, are editor streams, and an organization's, `getOrganizationEvents`, is a list
  stream, as is a stream opened by its declaration; the contract names the editor streams in prose
  alone, so the two are written beside the collaboration reading, and an operation a later
  contract adds to the union is read as a list stream until it is named there.
- **Progress is counted per open**: every record with data but a `reconnect`, passed over or not;
  an agent run's open needs more than one, a collaboration feed's more than none, and a snapshot
  counts only once its kind has led the open or after any other frame. On an editor stream the
  `presence.join` of the reader's own member counts nowhere, wherever it comes, and is handed on all
  the same. That member is the user the session token names as the open is made, a minted
  session's `user_id` or a source's `userId`, and where the token names none, a token given as a
  value or a function, the user the stream is told it is read as, by `readingAsUser` on an
  `EventStream` of the collaboration union, a second call replacing the first and users compared
  exactly. A reader that can name neither counts every `presence.join`. An open that made
  progress, that ended on a `reconnect`, or that closed (not dropped) at or after its token's
  expiry, a minted session's or a source's, read at the open against the client's clock, is
  followed at once with the count reset. Any other open waits `draw × min(max, base × 2^(n−1))`
  after the n-th in a row; an agent run's reader stops past six, throwing the failure its last open
  failed with, or ending where that open closed.
- **A reopen waits on §12's schedule, or what its open named.** A 429 or a 503 naming its wait in
  `Retry-After` is waited out with a draw of up to a second above it, the draw alone for 0 seconds,
  on either stream, and it still doubles the ceiling. An agent run's open answered 503 neither adds
  to the six nor clears them: those opens are held to two minutes from the first since the last
  progress or `reconnect`, measured on the client's wall clock, `clock`, since a coroutine's own
  time cannot be read; an open that failed otherwise in between neither restarts that time nor
  counts toward it, and an open answered 503 two minutes or more after that first stops the reader,
  which throws its `ServerException`. Progress and a `reconnect` reset both the count and the two
  minutes.
- **A failed open ends the stream or reopens it by its status**: a dropped connection, a 429, a 503
  and a 504 reopen either stream, every other 5xx ends an agent run's and reopens a collaboration
  feed, and every other status ends either. A credential refused locally at an open, a session
  token missing or a wrong prefix a function answers, ends the stream before anything is sent. Every
  open an answer came back to is an attempt the `onResponse` hook is told of.

**A webhook delivery is verified before it is read** (`transport.webhooks.verify-signature`).
Receiving webhooks is the admin unit's, as the contract places it: `constructWebhookEvent`,
`WebhookSignatureException`, the rules that verify a delivery and their HMAC-SHA256 and constant-time
`seams/`, and the printed `WebhookEvent`, are `mountaya-admin`'s, in `com.mountaya.admin` and
`com.mountaya.admin.generated`, and the core declares none of them. `WebhookSignatureException`
extends the core's `MountayaException`, whose constructor is `InternalMountayaApi` for exactly that,
and is of the family `isMountayaException` recognises by the name it declares.
`constructWebhookEvent(rawBody, signatureHeader, secret, tolerance, now)` takes the body as a
`ByteArray` or as a `String`, read as its UTF-8 bytes, the request's `Mountaya-Signature` or null,
the endpoint's secret, a tolerance as a `kotlin.time.Duration`, 300 seconds unless the caller names
another, as `sdk-swift` and `sdk-dart` take a `Duration`, and the
receiver's clock as a `kotlin.time.Instant`, the machine's wall clock unless the caller names one,
as `sdk/SPEC.md` §3 lets a caller state it, so a test pins it; it does not suspend, since nothing it
does waits. It is declared in `jvmMain`, as the factory
that takes a secret key is: a delivery is received by a server, and the endpoint's secret never
ships in an app, so an Android or iOS build has nothing to call, which the admin unit's android and
klib ABI dumps hold, while `WebhookEvent` and `WebhookSignatureException` are its common code. A secret not
opening with `whsk_`, which the refusal never quotes, and a tolerance below 0, refused as "A tolerance
of {s} seconds is no number of seconds from 0: pass one, or leave it out for 300.", its seconds written
as a whole number where they are one and as Kotlin writes a `Double` otherwise, the sentence `sdk-swift`
and `sdk-dart` write, are `ConfigurationException`s before the header is read; an infinite tolerance holds
a delivery to no time at all, and a clock is an `Instant`, which is always finite. Then the header is read, then the signature, then the time: a header
missing or white space alone, one that is no `t=<seconds>,v1=<signature>`, a signature no `v1`
equals and a `t` further from the clock than the tolerance are each a `WebhookSignatureException`
of its own `reason`. The signature is the HMAC-SHA256 of the `seams/` actual, keyed by the secret's
UTF-8 bytes and signing `<t as written>.` and the body's bytes, in lowercase hexadecimal, and every
`v1` is compared with it through the constant-time comparison of the `seams/` actual, every one
compared, so the time taken says neither how much of a forged signature was right nor which secret
of a rotation signed. A body that verifies is decoded as the printed `WebhookEvent` with the
client's `Json`, its instants `kotlin.time.Instant`s. An event this build cannot type, of a type a
later contract adds or whose payload its types do not read, is `WebhookEvent.Unknown`, holding the
envelope, its object the union's `Unknown` where the envelope's own types do not read it either, so
a receiver still acknowledges it and deduplicates it by its `id`; every member of the envelope is
read off every event. Only a body that holds no envelope at all is a `ConfigurationException`
carrying the decoder's failure as its `cause`, rebuilt as an answer's is, with no byte of the body,
and naming no reason, since no answer and no status stands behind it
(`transport.webhooks.verify-signature`).

**One type reaches the network.** The transport sends every request through an `HttpExchange`,
and nothing else does. The platform's exchange, `SystemExchange` in `seams/engines`, the client's
default, sends through Ktor's `HttpClient`, named there and nowhere else, its engine named in one
`actual` under `seams/`: OkHttp on jvm and android, where MapLibre already brings it, and Darwin
on iOS, which is `URLSession`. The exchange is the seam a test substitutes, a scripted
`HttpExchange` on the test's virtual time, to run the retry, credential, error and stream fixtures
without a server, and a consumer's test can do the same with the published seam. The TypeScript
SDK reaches `fetch` from exactly one module for the same reason; §7 holds the rule.

- **The published exchange.** `SystemExchange` is public, and its constructor that takes nothing is
  its whole public surface beside `send`, as `sdk-swift`'s `init()` is: every exchange built so
  shares one Ktor client and one engine per process, so every client of a process shares its
  connections, and wrapping one in an exchange of the consumer's own is how an app watches its
  traffic, since the client records nothing. A test builds one over a client of its own.
- **The stack holds nothing for a request.** Ktor's client follows no redirect
  (`followRedirects = false`), installs none of its default transformations, which would add an
  `Accept` and an `Accept-Charset` of their own, and no plugin that keeps a cookie, caches an
  answer, holds a credential, repeats a request or times one out. OkHttp is given no cookie jar, no
  cache and no authenticator, and follows no redirect; URLSession's session is ephemeral, with no
  cookie, cache or credential storage and no wait for connectivity, and Ktor's delegate answers
  every redirect with none. A `3xx` is answered as it arrived, and the ladder reads it.
- **The platform's timeouts stand out of the way**, so the client's deadlines are the only ones an
  attempt has: OkHttp's connect, read, write and call timeouts are off, where its ten-second
  defaults would end an attempt the API is still running and a stream between two of its
  heartbeats, and URLSession's request and resource timeouts and every request's own are a week.
- **No queue the client does not decide.** OkHttp's dispatcher runs as many calls to one host as to
  all of them, where its default runs five, so an open event stream never queues a request behind
  it and a queued request never spends its budget waiting for a slot. Its threads are daemon
  threads, so a jvm process whose work is done is never kept alive by one waiting for a next call.
  OkHttp's own recovery from a dropped connection, a request sent again on another route or a
  fresh connection, is left as it is: every operation the contract declares is idempotent or takes
  an `Idempotency-Key`, which the request sent again carries.
- **A body is read as it arrives, and let go when it is closed.** Each read answers what Ktor's
  channel holds, up to 16 KiB, so an event stream's frames arrive as the platform reads them; a
  body that breaks off is a failure, never an end, Ktor's channel reporting it closed with a
  cause. Each answer is read through one buffer of 16 KiB, made at its first read and reused by
  every read after it, a chunk handed on being a copy sized to what the read filled; a buffered
  answer is read whole straight into the bytes it answers, an array its `Content-Length` sizes, up to
  8 MiB, or the buffer's size where it states none, doubled as it fills, and never gathered as chunks
  and joined. An exchange of the caller's own hands its chunks, which a body read whole joins once. The client's close lets the call go, and Ktor cancels it where its body was not read to
  its end, its connection with it; a read whose caller gives up lets it go too.
- **The headers the stack writes** are the ones that carry the request: `Host`, `Content-Length`,
  `Accept-Encoding`, whose coding the platform undoes, and Ktor's own `User-Agent` where the client
  writes none; the language is the client's to write, `*` where it names none (above). A body's `Content-Type` is
  carried with the body, exactly as the client wrote it, since Ktor refuses it among the headers.
  An address leaves as the client encoded it, but for a query name written twice apart, which
  Ktor's URL builder writes together.
- **Each engine's own edges.** Ktor's Darwin engine delivers an answer's head with the first bytes
  of its body or at its end, never alone, so an open waits on the API's first frame, within the
  open's budget; and URLSession holds back the head of an answer that names no `Content-Type`
  until it can sniff one, which the API, naming one on every answer, never meets. OkHttp's Android
  artifact asks every app that carries it to compile against the platform it names, which is why
  the catalog's `compileSdk` floor is that one, and it brings AndroidX Startup, the one AndroidX a
  headless module carries (§7). On the jvm, Ktor's client reaches SLF4J, which prints a notice once
  where the process has no logging provider; the client itself writes to no log.

## 5. The naming law

`sdk/SPEC.md` §9, in Kotlin.

| Rule | Kotlin spelling |
| --- | --- |
| `<Subject><Widget>` | the composable's last token is what it is on screen, and its subject is the domain entity its parameters ACCEPT: `ItineraryProfile`, `ItinerarySegmentTable`. A composable whose parameters name no entity takes no subject: `MapView`, `MapControls`. |
| One subject, one spelling | a `CompositionLocal` `mountaya-elements` publishes leads with Compose's `Local` and the package subject `Mountaya`: `LocalMountayaScope`, `LocalMountayaTokens`. The same subject never appears under two spellings across the modules. |
| Noun-symmetric controlled pair | `value` pairs with `onValueChange`, and a named value `x` with `onXChange`, matched on TYPE as well as name: a value may widen by a null the composable never emits, and may not be a different type wearing the same word. Compose's own convention is the same one, so this rule costs nothing to follow here. |
| Closed handler verbs | `onChange` `onClose` `onError` `onHover` `onSelect` `onClick` `onToggle` `onReady` `onDestroy`, plus compounds built from them (`onQueryChange`, `onTogglePanel`). A synonym of one already in the set is refused. |
| `Engine` for a machine | `SessionTokenEngine`, `SearchEngine`, `NavigationEngine`. A machine is what the layout law files under `engines` (§6), and a type there never ends in Controller, Manager, Session, Service or Coordinator, which all mean Engine. Outside `engines` the words are the domain's: a value describing an edit in progress may be an edit session. `ViewModel` is refused everywhere: it is the consumer's lifecycle holder, never a published type (§1). |
| `inert` for a null object | `InertLocationSource`. The other words for it are refused as the first whole word of a type or a top-level value: null, noop, nop, dummy, never, silent and a bare no, so `NullableValue` passes and `NoopClock` does not. `memory` does NOT mean `inert`: an implementation that keeps values for its own lifetime is a memory implementation that forgets, which is a different promise from a seam that does nothing. |
| A type says what it is | it never ends in Helper or Util, singular or plural. |

**`onClick` is the ecosystem's verb.** Compose's own `clickable` modifier and every Foundation
component that takes a press take `onClick`, as the DOM does for TypeScript, so here `onTap` is
refused as `onClick`'s synonym. Swift and Flutter say `onTap`, since a touch surface is tapped
and SwiftUI and Flutter name the gesture that way, and their gates refuse `onClick` in the mirror
of this one. The contract leaves the idiom to each ecosystem and holds the set closed in every
one.

**A published composable never shadows a name its UI framework publishes.** A consumer imports
Compose beside this SDK in every file that uses it, and a composable named `NavigationBar` would
make Material's own ambiguous there.

**Refused by name, and what stands instead:**

| Refused | Because | Instead |
| --- | --- | --- |
| `MountayaNavigator` | a type prefix the package already says; a machine with a lifecycle ends in `Engine`; shadows Jetpack Navigation's `Navigator` | `NavigationEngine` |
| `MountayaMap` | a type prefix; the last token names what it is on screen, a view of a map | `MapView` |
| `MountayaMapControls` | a type prefix the package already says | `MapControls` |
| `MountayaElevationProfile` | a type prefix; its input is an itinerary, so that is its subject | `ItineraryProfile` |

**The entity vocabulary** is the set of types `mountaya-client` and `mountaya-geo` publish. A name
claiming a subject outside it fails the gate.

**A `CompositionLocal` is read as one.** Compose names one in UpperCamelCase after `Local`, as
`LocalContext` is, so the gate reads a property whose type, or the factory its value is made by,
`compositionLocalOf` or `staticCompositionLocalOf`, names a `CompositionLocal` by that rule rather than
as a lowerCamelCase member, and holds it, published or `internal`, to leading with `LocalMountaya`.

**The law reads every name another file can name**, `internal` as much as `public`, since an
internal name is one modifier away from being published and a contributor reads the same
vocabulary a consumer does. The component, accessor, controlled-pair and handler rules are the
exception: they describe what a consumer is handed and read published declarations only. This is
`sdk/SPEC.md` §9, held the same way by every SDK's gate.

**The law stops at the generated boundary.** Generated names are the contract's (§3), an enum
member's by its words, and the gate does not read them.

**A published name is the registry's, in this SDK's own spelling.** Every `api: true` entry of the
transport, geo and map layers records under `published` each SDK's exact spellings of the declarations
a consumer calls for it, one column for each of `typescript`, `dart`, `swift` and `kotlin`
(`sdk/SPEC.md` §9), so each ecosystem keeps its idiom and the registry says what it is. This SDK's
column, `kotlin`, writes each name as the committed ABI dumps declare it, the declaration's path after
its package with `Companion` left out: a type, `ItineraryResourceEngine`; a top-level function or
property, `haversineMeters`; a member through its type, `MapEngine.fitBounds`, a companion's as its
type's, `MapPaint.standard`, and a nested type through the type around it; an acronym as Kotlin spells
it, `PoiStyle`; and a class of the error ladder by the suffix of §4, `ApiException`,
`WebhookSignatureException.reason`, `isMountayaException`. A declaration one target alone has is
spelled as that target's dump spells it, the jvm's `constructWebhookEvent`. The column is `scoped`
exactly where this SDK scopes the capability (§11). The coverage gate looks for each name of the column
in the dumps as written, never by package and never folding case or reading one suffix for another
(§9), and reads every published module's, the admin unit's beside the core's: a declaration is
published under one name whichever unit declares it (`sdk/SPEC.md` §9), so `constructWebhookEvent`,
`WebhookSignatureException` and its `reason`, which `mountaya-admin` declares, keep the names
`transport.webhooks.verify-signature` lists.

**Kotlin's own style, where Mountaya is stricter:** `UpperCamelCase` types, `lowerCamelCase`
members, and `SCREAMING_SNAKE_CASE` for a top-level `const val` only; acronyms are words
(`loadHttpUrl`); a composable that emits UI is `UpperCamelCase` and returns `Unit`, and one that
returns a value is `lowerCamelCase`, which is Compose's own rule and keeps a reader from calling a
state factory in a layout slot; no `!!` in shipped source, tests included, since it is an
assertion with no message that throws where a value is read rather than where the invariant
broke.

**A member of an open vocabulary is `UpperCamelCase`**, the one member a consumer reads that is not
`lowerCamelCase`. An open vocabulary is a `@JvmInline value class` over its spelling, a `String`,
whose companion names the members it knows, so a member a later build adds is additive and a `when`
over it ends in `else`; a member is a constant the companion declares of the class itself, its type
stated or its initializer the class's constructor. Kotlin's own libraries name one so, Compose's
`TextAlign.Center` and Ktor's `HttpMethod.Get`, and so does the printed surface for every wire enum
(§3), so one rule spells every member a consumer reads: `MapException.Kind.LayerNotFound`,
`LocationException.Kind.NoProvider`, `WebhookSignatureException.Reason.MissingHeader`,
`CredentialDeclaration.ActingUser.Required` and `MapPanel.Layers`, beside `TileLayerType.Line`. A
member's value keeps its own spelling, the contract's (`missing-header`) or the one a log names it
by (`layerNotFound`). The gate reads such a constant as a vocabulary member and refuses one in
`lowerCamelCase`; a constant of another type in the same companion, one a class that is no value
class declares of itself, and one a value class over anything but its spelling declares, `SearchPause`
over its `Duration`, is a member as any other is: `SearchPause.standard`, as every other SDK's default
instance is `standard`. ktlint's `property-naming` checks no `val` an object or a companion declares,
so it asks nothing of a vocabulary member.

**`MountayaException` is the one prefixed type**, and the gate records it as an exemption with
its sentence: a bare `Exception` would shadow `kotlin.Exception`, imported into every Kotlin file
by default.

**`OrganizationRead.NoKey` is the one name leading with `No`.** The gate reads a bare `No` as a
null object's word, and `NoKey` is no null object but a state of a map's read of its organization,
the contract's `no-key`, named alike in every SDK (§8), so the gate records it as an exemption with
that sentence.

**A device seam's failure is one type in every SDK, in each one's suffix.** `LocationException` and
`SpeechException`, each with an open `Kind`, are `sdk-dart`'s `LocationException` and
`SpeechException`, whose kinds are the top-level `LocationExceptionKind` and `SpeechExceptionKind`, and
`sdk-swift`'s `LocationError` and `SpeechError`, whose `Kind` is nested as here. The kinds are one set
under one spelling of their values, `noProvider`, `permissionRevoked`, `providerDisabled`,
`platformFailed`, `backgroundUndeclared` and `audioUndeclared` for location, `engineUnavailable` and
`audioRefused` for speech, each member `UpperCamelCase` here (§8).

**One concept is one word in every SDK.** Where the SDKs named one concept two ways, the word is the
one most of them used, else the contract's, and each spells it in its own case; only the differences
below are kept. Each of these was renamed before the first release, which deprecates nothing:

| Concept | The word, in Kotlin | Was |
| --- | --- | --- |
| A configuration type's default instance | `standard`: `BaseUrls.standard`, as `RetryPolicy.standard`, `MapPaint.standard`, `ElementsScope.standard`, `ProfilePart.standard` and `SearchPause.standard` | `BaseUrls.default` |
| The session's state, read now and subscribed to | `snapshot`, a `StateFlow<SessionSnapshot>`, on `SessionTokenSource` and `SessionTokenEngine`, as an engine's is `state` | `snapshots`, beside a `snapshot` read of its value |
| One part of a multipart body | `MultipartPart`, `MultipartPart.File` and `MultipartPart.Text`, in `RequestBody.Multipart` | `FormPart` |
| A typing pause a search may be given | `SearchPause`, built by `SearchPause.of` and refused there below zero, `SearchPause.standard` its 150 ms | a `Duration` refused with `ConfigurationException` as `SearchEngine` is built |
| An instant written for a reader, as a date, a time of day or both | `formatDateTime`, at a `DateLength`, a `TimeLength` or both: the word of the fixture's `kind`, `dateTime`, and of `sdk-typescript`'s `dateTimeFormat` | `formatDate`, which wrote a date alone at its `length` |

**Kept differences.** What each SDK writes in its own idiom, and why it stays two words. The table is
the same, row for row and cell for cell, in `sdk-dart`'s and `sdk-swift`'s SPEC.md, so a change to one
is made in all three:

| Concept | Dart | Kotlin | Swift | Why kept |
| --- | --- | --- | --- | --- |
| Member casing | lowerCamelCase: `cursorMarkerGlyph`, `searchLimitMax`, `schemaVersion`, `TileLayerType.line` | UpperCamelCase for an enum's and an open vocabulary's members, `MapException.Kind.LayerNotFound`, `LayerRank.Chrome`; SCREAMING_SNAKE_CASE for a top-level `const val`, `CURSOR_MARKER_GLYPH`, `SEARCH_LIMIT_MAX`, `SCHEMA_VERSION`, `MODEL_DIGEST` | lowerCamelCase: `cursorMarkerGlyph`, `searchLimitMax`, `schemaVersion` | each language's casing |
| Acronyms | words: `Url`, `UrlTemplate`, `PoiPanel`, `PoiStyle`, `GeoJsonPoint` | words: `Url`, `UrlTemplate`, `HttpExchange`, `PoiPanel`, `GeoJsonPoint` | capitals in a type, `URI`, `URLTemplate`, `APIError`, `BaseURLs`, `HTTPExchange`, `JSONValue`, `POIPanel`, `POIStyle`, `GeoJSONPoint`, and a word in a member, `baseUrls`, `requestId` | each language's acronym rule, Swift's set by the kind of declaration |
| A tap | `onTap`, `onZoomInTap`, `MapEventTap`, `longTap` | `onClick`, `onZoomInClick`, `MapEvent.Click`, `MapEvent.FeatureClick`, `MapEvent.LongClick`, `ScriptedMapSurface.longClick` | `onTap`, `onZoomInTap`, `MapEvent.tap(_:)`, `longTap` | each platform's word for a finger on a screen |
| A failure's type | `…Exception`: `ApiException`, `MapException` | `…Exception`: `ApiException`, `MapException` | `…Error`: `APIError`, `MapError`, `SearchBranchError` | each language's word for what is thrown, which `sdk/SPEC.md` §3 allows |
| A name the standard library holds | `QueriedFeature.id`, an `Object?` | `QueriedFeature.Id.Text`, since a nested `String` would shadow `kotlin.String` inside `Id` | `QueriedFeature.ID.string(_:)` | read as a reserved word |
| Argument labels | `featuresAt(point)`, `haversineMeters(a, b)` | `featuresAt`, `haversineMeters` | `features(at:)`, `haversineMeters(from:to:)`, `Glyph(_:in:)`, `layerIds(forKey:)` | Swift's labels, which the registry records |
| A constructor that may refuse | `SearchLimit.of`, `SearchPause.of`, `GlyphSet.of` | `SearchLimit.of`, `SearchPause.of`, `GlyphSet.of` | `init?(_:)`, `init?(name:…)`, `init?(chaining:)` | Swift's failable `init?` |
| A second form of a call | a name of its own: `PixelPadding.all`, `ProfilePlace.of`, `MapLayer.fromTile`, `MapSource.fromTile`, `roadAccessKindOfDataApi`, `addDirectionsResult` | overloads | overloads: `init(_:)`, `addDirections(_:fit:)` over an input or a result | Dart has no overloading |
| A position along a followed route | `positionAtMetersRoute(route, meters)`, beside `positionAtMeters(line, meters)` | `positionAtMeters(route, meters)`, overloaded on a `FollowedRoute` | `positionAtMeters(_:meters:)`, overloaded on a `FollowedRoute` | Dart has no overloading, so the second input takes the input's word, `Route` |
| The layers panel's time and pictures, through the stack | `MapControls(offersTime:, thumbnail:)` | `MapControls(…, offersTime, thumbnail)` | `MapControls(_:…offersTime:thumbnail:)` | the three take each as an argument of the stack's own, where `sdk-typescript`'s `MapControls` takes them in its `layersPanel` props bag, React's way of handing a child its props |
| An instant written with neither length named | `formatDateTime(instant, locale, timeZone)` writes the long date, `DateLength.long` | `formatDateTime(instant, locale, timeZone)` writes the long date, `DateLength.Long` | `formatDateTime(_:locale:timeZone:)` writes the long date, `DateLength.long` | every case of `geo.format.locale-services` names a length, so the three keep the default `formatDate` wrote, where `sdk-typescript`'s `dateTimeFormat` handed no style writes ECMA-402's numeric date, `Intl.DateTimeFormat`'s own default |
| The error ladder | a class per row, `ApiException.of`, `IdempotentAnswerUnavailableException.answer`, `MapException.reason` | a class per row, `ApiException.of`, `IdempotentAnswerUnavailableException.answer`, `MapException.reason` | one `APIError` with a `Kind` per row, `APIError.IdempotentAnswer`, `APIError.unavailableAnswer`, `MountayaError.api(_:)`; `MapError.Kind.fixRefused(_:)` carrying its reason | Swift writes one error type for each family |
| A kind | top-level: an exception's `MapExceptionKind`, `LocationExceptionKind` and `SpeechExceptionKind`, and `WebhookSignatureReason`, `ActingUser`, `CameraMotionKind`, `ItineraryStrokeKind`, `SearchLabelKind`, `MapSourceKind` | nested `Kind`, `Reason`, `ActingUser` | nested `Kind`, `Reason`, `ActingUser` | Dart nests no type, so an exception's kind writes its owner before `Kind`, as the natives' words read, and every other keeps its spelling |
| A nested type | top-level, the owner's words and the variant's in one name: `MapEventFailure`, `FunctionSessionToken`, `MultipartFile`, `ItineraryResourceIdle` | nested classes: `MapEvent.Failure`, `SessionToken.Function`, `MultipartPart.File`, `ItineraryResource.Idle` | nested cases: `MapEvent.failure(url:status:cause:)`, `SessionToken.function(_:)`, `MultipartPart.Content.file(_:filename:contentType:)`, `ItineraryResource.idle` | Dart nests no type; `sdk-dart`'s §5 lists every one it writes at the top level |
| A wire spelling's reader | `id` (an enum's that has one, and `MapExceptionKind`'s, which wraps its `name`), `value` (an open vocabulary's, `WebhookSignatureReason`, `LocationExceptionKind` and `SpeechExceptionKind` among them), `name` (`MapPanel`) | `id` (an enum's, `BadgeSurface`, `GeolocationPermission` and `ThemeScheme` among them), `value` (an open vocabulary's), `name` (`MapPanel`) | `rawValue` | Swift's `RawRepresentable` |
| A set of options | `Set<ProfilePart>`, `ProfilePart.standard` | `Set<ProfilePart>`, `ProfilePart.standard` | `ProfileParts`, an `OptionSet`, `.standard` | Swift's `OptionSet` |
| A span spanned again | `ProfileSpan.spanning(from, to)` | `ProfileSpan.spanning(from, to)` | settable `from` and `to` | Swift changes a copy |
| An engine observed | `state` and `subscribe()` | one `StateFlow`, an engine's `state`, the session's `snapshot` and `DeviceLocationSource.standing` | `state` and `subscribe()`, a seam `@Observable` | Kotlin's `StateFlow` is the read and the stream at once |
| A stream read | `listen` | `collect` | `makeAsyncIterator` | each language's |
| A value copied | `copyWith` | `copy`, and a `Builder`: `MapPaint.Builder`, `FieldSelection.Builder` | `var` and a change | each language's |
| A function handed in | a `typedef` | a `fun interface`, `SearchFetch`, `NavigationRouteSource`, `ItinerarySource`, or a function type a `typealias` names, `CookieSource` | a `typealias`: `SessionTokenEngine.Mint`, `CookieSource` | each language's |
| A location source over a stream | `LocationSource.fromStream` | `FlowLocationSource` | `StreamLocationSource` | each platform's stream |
| The HTTP seam | `httpClient:`, package:http's `Client` | `exchange`, `HttpExchange`, and `MapView(okHttpClient)` | `exchange:`, `HTTPExchange`, `SystemExchange` | each ecosystem's client |
| Time and chance | a zone's `Timer`, `random:` | `clock`, `random`, `dispatcher`, `scope`, `timeSource` | `clock:`, `wallClock:`, `sleep:`, `random:` | each SDK's seams |
| Cancellation | `AbortSignal`, `AbortedException`, `RequestOptions.signal` | a coroutine's | a task's | each language's |
| A cause's stack trace | `MountayaException.causeStackTrace` | none, a `Throwable`'s own | none | a Dart exception carries none |
| A client closed | `Mountaya.close()` | `Mountaya.close()` | none, a client freed with its last reference | Swift counts references |
| A JSON value | `Object?` | `JsonElement` | `JSONValue` | each language's |
| A page's metadata | the printed `ListMetadata` | `RestSchema.ListMetadata` | `PageMetadata`, a protocol | Swift's generic constraint |
| A paged query | `PagedQuery`, each printed `Get…Query` | `PagedParameters`, each printed `…Parameters` builder | `PagedQuery`, each printed `Get…Query` | each printer's structure: Kotlin's builds every operation's parameters in one builder |
| A form's parts | `MultipartBody(fields:, files:)`, `MultipartFile` | `List<MultipartPart>` | `[MultipartPart]` | package:http's `MultipartRequest` |
| A JSON error's path | `List<Object>?`, each a `String` or an `int` | `List<ErrorEntry.PathComponent>` | `[ErrorEntry.PathComponent]` | JSON's own key and index |
| A matched range | `HighlightRange` | `IntRange` | `Range<String.Index>` | Dart has no range type |
| A layer's patch | `MapPatch` | a map | `[String: JSONValue?]` | Dart has no undefined |
| An operation called by its declaration | `send(…, pathParameters:, query:, decodeData:, decodeMetadata:)`, its facts on `OperationDeclaration` | `send(operation, path, data: KSerializer, metadata, query: List<Pair>…)`, the printed `OperationTable` | `send(_:path:query:…)` over `Decodable`, the printed `OperationFacts` | each SDK's decoding and printed facts |
| A seam with no class of its own | `Geolocation.fromFunctions` | `object :` | a struct | Dart has no anonymous class |
| The UI framework's binding | `asValueListenable()`, on an `Engine<S>` and on a `NavigationEngine` | none | none | Flutter's `ValueListenable` |
| A scope stated | `MountayaScope(statement:, tokens:, scheme:, child:)`, `MountayaScope.of` | `MountayaScope(statement, scheme, content)`, `LocalMountayaScope`, `LocalMountayaTokens`, `themeSchemeInForce()` | `.mountayaScope(_:)`, `.mountayaTokens(_:)`, `\.mountayaScope` | each framework's accessor |
| A view's children | `child`, `children` | `content` | `content:`, a `@ViewBuilder` | each framework's |
| A view disabled | `enabled:` | `enabled` | `.disabled(_:)` | SwiftUI's modifier |
| A platform's arguments | none | `modifier`, `scope`, `Context` | none | Compose and Android |
| One SDK's arguments | `photoBuilder`, `onLinkTap`, `requestRules` with `MapRequestRule`, `queryFeaturesAt` | `photo`, `transformRequest` with `MapRequest` | `photo:`, `transformRequest` with `MapRequest` | Flutter's idiom |
| A handler's tense | `onChanged`, `onSelected`, `onToggled`, `onCameraChanged`, `onGroupToggled` | `onChange`, `onSelect`, `onToggle`, `onCameraChange`, `onToggleGroup` | `onChange`, `onSelect`, `onToggle`, `onCameraChange`, `onToggleGroup` | `sdk/SPEC.md` §9 holds the set closed and leaves each SDK its tense |
| A value a view and the app share | the value and `onXChanged`: `window`, `picked`, `query`, `isConfined` | a `MutableState`: `window`, `picked`, `query`, `isConfined` | a `Binding`: `window`, `picked`, `query`, `isConfined` | each framework's |
| A map view's plumbing | none | `ComposableSingletons` | `MapView.Coordinator`, `makeUIView(context:)` | each framework's |
| A position refused | `ConfigurationException` | `ConfigurationException` | `PositionError`, which `setPosition(_:)` throws | Swift's typed throw |
| A device source closed | `DeviceLocationSource.close()` | none | none | a Dart stream must end |
| A double's refusal | `ScriptedAnswerError` | `IllegalStateException`, from `check` | `ScriptedAnswerError` | Kotlin's `check` |
| A voice readied ahead of its first sentence | `SpeechNavigationVoice.prepare()`, a `Future<void>` that completes once the platform reports whether the engine started | `SpeechNavigationVoice.prepare()`, a `suspend fun` that returns once the platform reports whether the engine started | `SpeechNavigationVoice.prepare()`, `async`, which returns once the voice of the host's language is found or known to be missing | one name, each language's wait, so the first maneuver is never held while the platform readies its speech, and an app learns before it what its voice holds, `engineUnavailable` among it where an engine can fail to start; `sdk-typescript` ships no voice |
| The reserved word `continue` | `continue_`: `NavigationInstructionKey.continue_`, `ManeuverType.continue_`, as `dart:io` writes `HttpStatus.continue_` | `Continue`: `NavigationInstructionKey.Continue`, `RestSchema.ManeuverType.Continue`, which a member's UpperCamelCase keeps clear of the keyword | `` `continue` ``: `NavigationInstructionKey.continue`, `RestSchema.ManeuverType.continue`, escaped where it is declared and read as `.continue` | each language reserves it and writes the word itself where its grammar lets it; `sdk-typescript` writes the wire's string, `"continue"` |
| Where a large body is decoded | off the caller's isolate above a size the client takes, `decodeOffIsolateAbove`, 256 KiB unless another is passed | off the caller's thread, on the client's `dispatcher`, `Dispatchers.Default` unless another is passed, every body alike, with no option | off the caller's actor, in the transport's own asynchronous work, with no option | a Dart isolate shares no memory, so moving a body costs a copy only a large one repays |
| The secret credential | `SecretCredential(key)`, on the Dart VM alone, and `Credential(…)` from parts takes no secret key | `Credential.Secret(key)`, and `Credential(…, secretKey = …)` from parts, both on the jvm alone | `.secret(key:)`, and `Credential(secretKey:publishableKey:sessionToken:shareToken:)` from parts, unavailable on every app platform | each language's form of a credential's case, refused in an app at compile time where the language can, as `sdk-typescript`'s browser build refuses `{ secretKey }` |
| A previous value stated or absent | the value, and `moved`, the set of the fields the change moved, which tells a null held from a field not moved | `Stated<T>?`: null where the field did not move, a `Stated` holding null where it held null | `T??`: `nil` where the field did not move, `.some(nil)` where it held null | each language's way to tell absent from null; `sdk-typescript` leaves the key out where the field did not move |
| A webhook event's variant | a class per type, `<Type>Event`: `CollectionDeletedEvent`, `OrganizationMemberUpdatedEvent` | a class per type nested in `WebhookEvent`: `WebhookEvent.CollectionDeleted`, `WebhookEvent.OrganizationMemberUpdated` | a case per type of the `WebhookEvent` enum, `.collectionDeleted(_:)`, carrying a `WebhookEvent.Payload` | Dart nests no type, so it writes the type before `Event`; Swift's `switch` reads an enum's cases; `sdk-typescript` narrows one union by its `type` |
| A data element over an engine or an id | named constructors: `ItineraryProfile(cache:, itineraryId:)` and `ItineraryProfile.engine(engine:)`, the same for the two tables | overloads: `ItineraryProfile(itineraryId, cache, …)` and `ItineraryProfile(engine, …)`, the same for the two tables | overloads: `ItineraryProfile(itineraryId:cache:…)` and `ItineraryProfile(engine:…)`, the same for the two tables | Dart has no overloading; `sdk-typescript`'s `<ItineraryProfile itineraryId>` reads through the provider's client alone |
| What a map tells | a method, `MapEngine.events()`, a `Stream<MapEvent>` | a method, `MapEngine.events()`, a `Flow<MapEvent>` | a method, `MapEngine.events()`, an `AsyncStream<MapEvent>` | one name, each language's stream: a method, since each call hands a listener a stream of its own, as `subscribe()` does; `sdk-typescript`'s map tells by MapLibre's `on(type, listener)` |

**The gate** is `./gradlew lintNaming`, registered once in the root build. An exemption is keyed
by name and valued by the sentence that justifies it, and the gate prints "N checked, M exempt"
so a reviewer sees the exemptions move.

## 6. The layout law

The directory names the topic and the kind; the package is the module's. One kind of file per
directory, the topic being the directory above it, and every hand-written file, main and test code
on every target alike, declares its module's one package wherever it sits: `com.mountaya.client`,
`com.mountaya.admin`, `com.mountaya.geo`, `com.mountaya.elements` or `com.mountaya.navigation`, and
`com.mountaya.conformance` for the harness.

```
mountaya-admin/src/commonMain/kotlin/com/mountaya/admin/webhooks/rules/
mountaya-geo/src/commonMain/kotlin/com/mountaya/geo/profile/rules/
mountaya-geo/src/commonMain/kotlin/com/mountaya/geo/search/engines/
mountaya-geo/src/commonMain/kotlin/com/mountaya/geo/navigation/contracts/
mountaya-geo/src/commonMain/kotlin/com/mountaya/geo/navigation/testing/
mountaya-elements/src/commonMain/kotlin/com/mountaya/elements/map/engines/
mountaya-elements/src/commonMain/kotlin/com/mountaya/elements/edit/rules/
mountaya-elements/src/androidMain/kotlin/com/mountaya/elements/renderer/components/
```

**A consumer imports one package per module.** A first call imports `Credential`, `SessionToken` and
`Mountaya` from `com.mountaya.client`, and `import com.mountaya.client.*` reaches every name the
client publishes but its printed types, which sit in `com.mountaya.client.generated`: one import per
module, as `sdk-dart`'s one barrel import and `sdk-swift`'s one module are. A package per directory
would hand a consumer the topics and the kinds, which are this repository's filing of its own code,
and a file could never move between them without breaking every import that names it. A contributor
reads the topic and the kind off the directory, and so does every gate (§7): the layout, the
boundaries, the determinism scan and the naming law place a file by where it sits, never by the
package it declares.

**Kotlin's convention is the other way round, and this repository departs from it.** Kotlin's coding
conventions file a source in the directory its package spells, and IntelliJ IDEA's inspection of a
package directive that does not match its file's location marks every file here, and its new-file
action writes the directory's package. Neither is followed: the inspection is a setting of each
contributor's own project, `.idea/` being no part of the repository, and is turned off there, and the
package a new-file action writes is one `lintFileStructure` refuses. No lint the build runs reads the
convention, so none needs a setting to depart from it: ktlint's `package-name` rule reads the
characters of a package and never its directory, and the compiler places a declaration by its package
alone.

| Kind | Holds |
| --- | --- |
| `contracts` | interfaces and the value types they exchange |
| `engines` | machines that hold state across time, and a seam's platform implementation, which reads the platform and decides nothing |
| `rules` | pure functions that decide something; the default |
| `components` | composables, in `mountaya-elements` only |
| `testing` | published test doubles, such as the scripted `NavigationEngine` |

The kinds are closed. `rules` is the default for anything that decides something, and `engines`
is what a rule is not.

**`seams` is a topic, not a kind**, with kinds beneath it like any other, and it is the only topic
in which the environment is read (§7). Its `expect` declarations sit in `commonMain` and each
`actual` at the mirrored path of its own source set. A test's own seam follows the same law one
level down: its `expect` sits in `commonTest` and each `actual` in its target's test source set,
which is how a test reaches a declaration one target alone has.

**The generated packages are outside the law.** `com.mountaya.client.generated`,
`com.mountaya.admin.generated` and `com.mountaya.geo.generated` are laid out by their printers, and a
reprint would undo any other layout. The hand-written tests of the printed surface sit in its
directory and declare its package,
outside the law with it.

**A contract's test keeps its directory's package where a rule takes the contract's name.** A
contract and the rule answering it, `ProfileReadout` and `profileReadout` (K126), are two files of one
name, under `profile/contracts/` and `profile/rules/`, and the tests at their mirrored paths are two
classes of one name, `ProfileReadoutTest`, which one package holds once. The contract's test keeps the
package its directory spells, `com.mountaya.geo.profile.contracts`, fourteen of Geo's tests in all, each
an exemption `lintFileStructure` counts with its sentence in the root build file, so one that exempts
nothing fails the gate.

**Every geo rule is in `commonMain`.** A rule in a platform source set is a rule one target has,
and the fixtures could pass on the JVM while iOS answers differently.

**A rule in `mountaya-elements` names a MapLibre or Compose type, or belongs to the `map` layer,
or it belongs in Geo.** The elements binding holds no rule the geo layer could hold, and a rule
that names neither is one `sdk-swift` needs too. The one exception is the contract's own: a rule
of the map and editing engines, which a `map.*` capability proves, is the binding's even where it
names no renderer type, because the contract gives it to the engines that drive a renderer and the
renderer-free geo layer has no map to apply it to. It sits under the topic of the engine it
serves, `map/rules/` for the patch that `map.engine.patch` proves and `edit/rules/` for the magnet
that `map.edit.snap` proves, in `commonMain`, and it names no Android type either, since its fixture
runs on the jvm and the iOS simulator (§9).

The search fills three of Geo's kinds under `search/` (family 8e): its values under `contracts`, the
cap, the configuration, the snapshot, its sections and their states, a list's state and status, a pick
and the highlight it marks; its rules under `rules`, the pick, the list's state, whether the list shows, a result's second line
and the internal rules a round is asked by; and `SearchEngine` under `engines`. It takes the Data API
handed in, `SearchFetch`, and the camera stated, so it names no map and no client, and a composable of
`mountaya-elements` shows it.

The data elements' rules fill Geo's `profile/` topic beside milestone 6's (family 8f): their values under
`contracts` (the window and every stretch, `ProfileSpan`, `ProfileRange`, `CurveColor` and `ProfileClass`; the
scale, `ElevationDomain`, `ProfileAnchor`, `DomainRoundingStep`, `PlotPoint`, `PlotCommand` and `CurveStop`; the
places, `ProfilePlace`, `CurvePlacement`, `CurvePin`, `CurveDivider`, `CurveCutoff`, `AxisLabelBox` and
`PlacedSpan`; the strip, the card at the cursor and the chips, `ResolvedCharacteristics`, `StatStripStat`,
`StatStripCursor`, `StatStripCell`, `ReadoutStat`, `ReadoutCell`, `ProfileReadout`, `SectionChipStat`,
`SectionChipCell` and `AmenityStack`; and the two tables, `ItinerarySegmentRow`, `ItinerarySegmentRowMetrics`,
`ItineraryWaypointRow`, `ItineraryWaypointRowMetrics`, `AltitudeRange`, `SegmentTableColumn`, `SegmentRowCell`,
`WaypointTableColumn`, `WaypointRowCell` and `AmenityFilter`), and their rules under `rules`, each named after the
function it declares. A rule whose name is its answer type's keeps `sdk-typescript`'s name, since a contract and a
rule of one name sit in two directories here, in one package that tells the class from the function (K126):
`profileReadout`, `segmentRowCell`, `waypointRowCell` and `amenityFilter`, as `sdk-swift` spells them too. A place's card is `waypoints/`' (`WaypointCardFacts` and
`WaypointField` under `contracts`, `waypointCard`, `waypointPropertyValue` and `waypointPropertyLink` under
`rules`), the card's model taking TypeScript's name beside the composable `WaypointCard`; an altitude band is
`units/`' (`formatAltitudeRange`, the internal `RangePatterns` table, and `formatDistanceTick`, a stop of the
distance axis); the order two words sort in is a seam's (`seams/engines/`, `localeCollatedBefore`, an `expect`
with an `actual` per target), and a field's number is read by the internal `readNumber` under `numbers/rules/`.

`mountaya-elements` has four topics. `map` holds what a map shows and the engine that holds it, its
values under `contracts`, the surface the engine asks what draws it (`MapSurface`), the camera's
values (`CameraTarget`, `CameraMotion`, `CameraFit`, `ZoomLimit`), what a query and an event answer
(`QueriedFeature`, `MapEvent`), the geometry slots (`GeometrySlot`, `SlotGeometry`), a Data API
result's handle (`AnalysisHandle`), the maps a data element writes its links to and what it writes there
(`CursorMarkerTarget`, `LegEmphasisTarget`, `CursorMarkerPin`, `CursorMarkerPoint`, family 8f), the internal
values of what the map holds (`ContentSlot`,
`MapContent`, `AnalysisKind`, `HeldAnalysis`, and `ShownMap`, where the view drawing the map laid it out) and the descriptions of the images a map draws among
them (`MarkerSprite`, `MarkerImage`, `StripePattern`, `MarkerPin`), the rules a `map.*` capability
proves under `rules` (the patch, the camera's reads and targets, the zoom window, the query layers,
the access refusals and standing, the credits, the label scope, `describeMarkerSprite`,
`readMarkerImageId` and `roadAccessStripePattern`, which `map.icons.marker-sprite`,
`map.icons.missing-image` and `map.icons.stripe-pattern` prove, and the rules of family 8b,
`baseMapOf`, `terrainVariantOfStyle`, `roadAccessLayersOf`, `analysisLayers`, `geometrySlotLayers`
and `describeMarkerPin`, which the `map.basemap.*`, `map.road-access.*`, `map.analysis.*`,
`map.geometry.*` and `map.marker.*` capabilities prove, with `readRoadAccessStripeImageId`, the
stripe image the renderer answers, and of family 8e, `searchHighlightLayers` and the sprite id it
names, `poiIconImageId`, which `map.search.result-highlight` proves, and of family 8f, `showCursorMarker`,
`hideCursorMarker` and `writeLegEmphasis`, which `map.profile.cursor-marker` and `map.profile.emphasis` prove),
the engine, the one layer registry, the requests each slot holds
and a query's abort under `engines` (`MapEngine`, `MapLayerRegistry`, `SlotRequests`, `QueryAbort`),
and the scripted surface a test draws a map on under `testing` (`ScriptedMapSurface`); `edit` holds the
magnet, its projection and its answer; `seams` holds the geolocation a map locates through, its values
under `contracts` and the inert one under `engines`, and the one read of the environment the module
makes in common code, the device's preferred language; and `renderer` is the one topic that knows
Android, Compose, MapLibre and OkHttp (§7), the android source set's alone: the composables under
`components` (`MapView`, `Glyph`, the controls `MapControls`, `ZoomControl`, `CompassControl`,
`GeolocateControl`, `PanelControl`, `DownloadControl` and `DirectionsControl`, the panels `LayersPanel`,
`PoiPanel` and `PreferencesPanel`, the search box `SearchControl`, the data elements `ItineraryProfile`,
`ItinerarySegmentTable`, `ItineraryWaypointTable`, `ItinerarySparkline` and `WaypointCard`, `MountayaScope`,
the scheme in force an app's own composable reads (`themeSchemeInForce`), and the internal pieces they
stand on, a control's button and chip, the compass rose, a panel's surface, whether the reader removed
animations (`motionRemoved`), and a profile's chart, distance axis and navigator), the renderer, the request
hook, the device's geolocation over Android's location services, `MapLink`, what a data element points
at and writes to the map beside it, a profile's curve as Compose paths kept for each size (`PlotPaths`), the
markers' pins as images kept for each look (`PinImages`), the failures the request hook hears carried to the
main thread a batch at a time (`HeardFailures`), and what a hot-path budget counts the composables'
compositions and drawings with (`RenderProbe`, §7), under `engines` (`DeviceGeolocation`), a glyph's shape as
Compose draws it, the scope, the scheme, the tokens and the render probe a composition carries, a panel's name, a
stack's corner and its places, and what a data element is drawn by, under `contracts` (`GlyphFigure`,
`LocalMountayaScope`, `LocalMountayaScheme`, `LocalMountayaTokens`, `LocalMountayaRenderProbe`, `MapPanel`,
`MapCorner`, `ControlSlot`, `ProfileColoring`, `ProfilePart`, `ItineraryDrawing`, `MapTarget`),
and what draws a
glyph, a token, the map's images and its credits on the platform under `rules`: a glyph's shapes as
Compose paths (`glyphFigures`, `shapePath`, `svgTransform`), the colour a CSS hexadecimal colour is
(`cssColor`), a token's colour under a scheme (`tokenColor`, `hexColor`, `themeScheme`), a marker
sprite, a stripe tile and a marker's pin as Android bitmaps (`markerSpriteImage`, `stripePatternImage`,
`markerPinImage`), the credit sheet as Android's dialog (`creditSheet`), a profile's square as Compose paths
(`plotPath`, `plotLine`, `plotPoint`), a name's width as Compose measures it (`textWidth`), how much the
reader's font scale grows a line of text (`textGrowth`), what a map claims of the answers the request
hook hears (`claimedAddresses`, `tilePrefixes`), and what the renderer sends MapLibre of a map's sources
(`sourcePushes`). Everything but
`renderer` is `commonMain`, tested in
`commonTest` on every target; the renderer's tests load MapLibre's native library, or draw on
Android's own canvas, and sit in `androidDeviceTest` at the mirrored path, but for what loads neither, the
request hook's signing of a request, the pins' images with a drawing of the test's own, the sources sent and the
render probe's counts, which are tested on the host JVM too, in `androidHostTest`, a test tree of its own, since
the common tests run on the jvm target already.

`mountaya-admin` has four topics, all in `commonMain` but webhook receiving's entry point. `accessors`
holds the one declaration it adds to the core's client, `Mountaya.admin`, under `rules`; `webhooks`
holds a delivery's verification under `rules`, the signature header, the signature and the verified
event, and `constructWebhookEvent` in `jvmMain` at the mirrored path; `errors` holds
`WebhookSignatureException` under `contracts`; and `seams` holds HMAC-SHA256 and the comparison of
bytes in constant time under `engines`, an `expect` each with an `actual` per target, which only a
delivery's verification reads and which the core therefore no longer carries. Its tests mirror them
in `commonTest` and `jvmTest`, the printed surface's in the generated package's directory, a call
through the client's own transport over an `HttpExchange` of the test's own among them.

`mountaya-navigation` has three topics, all in `androidMain`. `seams` holds the location source and
the voice, their interfaces under `contracts` and the platform's, the replay and the inert
implementations under `engines`. `guidance` holds the engine that keeps to the route followed and says
each prompt once, over the plan of its prompts, `guidancePlanOf`, and the prompt due, `duePrompt`, which
Geo's `navigation` topic holds beside the machine, since a rule a fixture proves is Geo's
(`geo.navigation.prompts`). `adapter` holds the public engine, Geo's machine with its guidance
and the device's seams, and the voice over the platform's text-to-speech. Their tests run on the host
JVM, in `androidHostTest`, the public engine's among them; the voice's speech on the platform, and the
audio focus it takes, are tested on an emulator too, in `androidDeviceTest` at the mirrored path.

Explicit API mode makes `public` a deliberate act and `internal` the default in practice. A
package named `internal` is not the mechanism; the modifier is.

**The gate** is `./gradlew lintFileStructure`, registered once in the root build. It enforces the
kinds, read off the directory, the module's package in every file, an `UpperCamelCase.kt` filename
matching its primary declaration, no empty directory, and a test at the mirrored path of the
matching test source set.

## 7. Boundary rules

| Module | Never |
| --- | --- |
| `mountaya-client` | imports `android.*`, `androidx.*`, Compose, MapLibre or a navigation library; names a product concept; names Ktor, its `HttpClient` included, or OkHttp outside the platform's exchange in `seams/engines` |
| `mountaya-admin` | opens a connection of its own, naming Ktor, OkHttp or `java.net`: its calls leave through the core's transport; imports `android.*`, `androidx.*`, Compose, MapLibre or a navigation library; names `mountaya-geo`, `mountaya-elements` or `mountaya-navigation`, since it stands on the core alone |
| `mountaya-geo` | reaches the network; names `Mountaya`, the transport or `SessionTokenEngine`; imports `android.*`, but for the platform's ICU and the release it runs, `android.icu` and `android.os.Build`, in `seams/` alone, where android writes an instant (§8); imports `androidx.*`, Compose, MapLibre or a navigation library; reads the environment outside `seams/`; holds a rule outside `commonMain` |
| `mountaya-navigation` | reaches the network; names `Mountaya` or anything of the client beyond its wire vocabulary, the generated types and the GeoJSON values; imports Compose, MapLibre or a navigation library |
| `mountaya-elements` | opens a connection of its own; imports a navigation library, `mountaya-navigation`, Compose Material or a map engine other than MapLibre; names an `android.*`, AndroidX, Compose, MapLibre or OkHttp type outside its `renderer` topic, so the map's state, its engine and every rule a `map.*` capability proves name none; adds a map layer anywhere but through the one registry that reads `geo.layers.order`, MapLibre's `addLayer`, `addLayerAt`, `addLayerAbove` and `addLayerBelow` called outside the renderer's engines; holds a rule that names no MapLibre or Compose type and that no `map.*` capability proves |

**Source scans, not behaviour assertions.** A rule that holds only where someone remembered it is
a convention. Each module's suite scans its own tree, and the module and source-set lists it walks
are read from the Gradle model, never kept by hand: a hand-kept list is the one that misses the
module added next. A name is read whole, an import by its own name and a qualified name in code by
all of it, never the prefixes it is built of, so `com.mountaya.client.generated.RestSchema` names the
generated package and not the client. A module's name is read where it is declared: every file of a
module declares its one package (§6), so the scan reads `com.mountaya.client.Mountaya` as the
directory of `Mountaya.kt` spells it, `com.mountaya.client.transport.engines.Mountaya`, a star import
as every directory of the package it imports, and a row refuses a topic of another module by that
path, the client's `transport` and `session` to Geo. A file is placed by its directory the same way:
the renderer is the `renderer` topic, and the platform's exchange the client's `seams/engines/`.

**The built-artifact walk is the half that cannot be skipped.** Each suite reads its module's
resolved runtime classpath for every target from Gradle and fails on a path to MapLibre from
anything but `mountaya-elements`, to a navigation library from any module at all, or to
`androidx.*` from `mountaya-client`, `mountaya-admin` or `mountaya-geo`, but for AndroidX Startup, its tracing and
its annotations reached through OkHttp's Android artifact alone, which finds its context through
Startup: no AndroidX type reaches a signature of any of the three, and a path to any other AndroidX,
or to these three by any other road, still fails. A source scan passes on a module that
reaches a forbidden dependency transitively, and that is exactly the shape that puts a renderer
into a headless consumer's build.

**Navigation links no library, and the scan and the walk hold it so.** Every module refuses an
import of a navigation library, Ferrostar's packages `com.stadiamaps.ferrostar` and
`uniffi.ferrostar` named, `mountaya-navigation` as much as the others, and the built-artifact walk a
path to Ferrostar's group from any module: the navigation is Geo's machine with its guidance, Kotlin
in this repository (§8), so no library beside it decides a step, a prompt or a deviation the
contract decides.

**The public ABI names a third-party type where a view hands one across, and nowhere else.** The
ABI dump carries no MapLibre type outside `mountaya-elements`. `mountaya-navigation`'s is one dump,
its android target's, and names no type beyond Mountaya's, the standard library's, kotlinx's and the
platform's own `Context`. `mountaya-elements`' jvm and klib dumps carry no MapLibre, Compose, AndroidX
or OkHttp type, and its android dump carries them on the composables' lines alone: `MapView`'s, which
takes the host's `Modifier` and the app's `OkHttpClient` and hands back MapLibre's map, every other
composable's, which takes the host's `Modifier` and, for the stack, the search box, a profile's window
and a waypoint table's choice of services, Compose's `MutableState`, and for a card its photo's
composable, and the getters of `LocalMountayaScope` and `LocalMountayaTokens`, Compose's own
`ProvidableCompositionLocal`; `DeviceGeolocation`'s constructor takes Android's own `Context`, a
platform type and no dependency's. Compose's foundation, which the controls and panels are laid out
and pressed with, reaches no public signature, so it is an `implementation` dependency.

**The public API has a baseline.** The Kotlin Gradle plugin's `abiValidation` dumps every
published module's ABI, one dump for the jvm, one for android and one klib dump for the iOS
targets, committed under the module's `api/`, and `check` fails on an unreviewed change; the
standalone binary-compatibility-validator plugin is discontinued. `./gradlew updateKotlinAbi`
(`mise run api:update`) rewrites them after a deliberate change, with every target built, and the
diff is the review. With the jvm alone the plugin reads a single target's layout, which the dumps
are not in, so a jvm-only bring-up run checks none; any run that builds a second target checks the
dumps of the targets it builds. A source-compatible change that breaks binary compatibility is the
failure a JVM library has and a source-distributed one does not. A break needs a major (§10). The
android and klib dumps are also where the secret key's barrier is held: neither of the core's carries a
public way to build `Credential.Secret`, and neither of the admin unit's carries `constructWebhookEvent`,
which takes a webhook endpoint's signing secret (§4). An interface's default members compile to the JVM's own
default methods alone, with no `DefaultImpls` class, since no release before the first compiled
one.

**What one module reads of another's and no consumer does is opt-in.** Kotlin's `internal` stops at
its module, so a declaration a sibling module reads is `public` to the compiler and marked
`InternalMountayaApi`, a `@RequiresOptIn` marker at the `ERROR` level declared in `mountaya-client`:
a module reading it opts in where it reads it, `@OptIn(InternalMountayaApi::class)`, every printed file
of the admin unit with `@file:OptIn`, and a consumer that has not opted in cannot compile a call to
it. `mountaya-client` declares the marker and reads its own marked declarations throughout, so it opts
in for every compilation of its own, `compilerOptions.optIn`, and no other module does. The dumps
leave out what it marks, the
`abiValidation` filters excluding every declaration annotated with it in every published module, so it is
no part of the published API and may change in any release; the changelog names none. It is what
`sdk-swift` writes `package` for its siblings to import, and it marks: `ElementsScope.strings` and
`ElementsScope.withStrings`, Geo's half of a composable's own `strings`, which `mountaya-elements`
layers over the scope in force (§8); Geo's `MeasureProbe`, which the replay's budget test in
`mountaya-navigation` counts a walk's measurements through, as Geo's own tests count theirs (below):
a test reads it, and no module's main code does; and the seam the admin unit, `mountaya-admin`,
reaches the core's transport through, which the contract leaves each SDK to draw and names no part of
the published API (`sdk/SPEC.md`, "The three layers, in every language"). That seam is
`Mountaya.sender`, the client's transport as the `OperationSender` every printed method hands its call
over through, `DataCall` with it, and what a printed method calls of the core beside it (§3):
`OperationCall`, `OperationFacts`, `SuccessResponse`, `SuccessKind` and `Pagination`,
`encodePathSegment`, `formatTimestamp`, `nullableSerializer`, `wireJson`, `PagedParameters`'
constructor and its cursor and limit, `Settable` with its two states and its serializer, the
serializers of a sent web address and of a timestamp, and `RequestBody.Json`'s constructor from the
text it sends. Webhook receiving reads three more of the core's: `MountayaException`'s constructor,
which `WebhookSignatureException` extends, `decoderFailure`, which reads an undecodable delivery as an
answer's is read, and `SystemDefaults`, the clock a delivery is judged by where its receiver names
none. A class it marks has each of its nested classes marked too, since a jvm dump lists a nested class
on its own.

**Suppressions are counted.** A suppression, Kotlin's `@Suppress`, Android Lint's `@SuppressLint` or Java's
`@SuppressWarnings`, appears only as an exemption `checkSuppressions` counts, keyed by its file and the annotation with
its arguments, `WatermarkView.kt: @SuppressLint("ViewConstructor")`, each with the sentence that justifies it. A
suppression with no sentence is a claim nobody made; the TypeScript SDK's `no-suppressions` test holds its own the same
way.

**Every declaration a consumer reaches is documented.** `lintDocumentation` reads every hand-written
file outside a test source set and the generated packages with the compiler's parser, as the naming
law's gate does, and fails on a declaration with no KDoc that it and everything enclosing it leave
`public` or `protected`: a type, a companion object, named or not, a type alias, a function, a
property, a property its primary constructor declares, an enum entry and a secondary constructor. An
`override` is documented by what it overrides and is not read, and neither is a declaration in a
function's body, an object expression or an enum entry's body, which no consumer reaches. A
comment that is not KDoc documents nothing. Anything else with none is an exemption keyed by its path
below its package, as the gate prints it (`MapPaint.Companion`), and valued by the sentence that
justifies it; one that exempts nothing fails, and the gate prints "N checked, M exempt" as the naming
law's does. The printed surfaces carry the contract's descriptions, and `sdk-dart` holds its packages
to `public_member_api_docs` and `sdk-swift` its modules through swift-format, so a consumer of any of
the three reads a sentence on every name.

**Every hot path keeps a budget.** A hot path is work that runs per pointer frame, per camera frame or per
change: a hit-test a pointer pays for, a redraw a scrub causes, a source a change sends the renderer again. Its
failure is the one no correctness test sees, since the output stays right and only the count of work grows, one
reasonable change at a time. The contract lists every hot path in `sdk/hot-paths.yaml`, each with the SDKs that
budget it and the sentence its budget holds, and lists Kotlin for nine. Each keeps a budget test in the test file of
what it budgets, a `@Test` named `hotPathBudget`, the id in UpperCamelCase, then what it shows
(`hotPathBudgetProfileCursorSixtyPositions…`), the identifier form `sdk/SPEC.md` §10 gives a language whose test
names cannot hold the title `hot-path budget <id>:`, as a JVM method's cannot, that drives the path through a
realistic burst, counts the work, and
asserts the count against a top-level `private const val` the same file declares, the id in capitals with its
hyphens as underscores and `_BUDGET` after it, as the naming law spells a constant (`PROFILE_CURSOR_BUDGET`),
beside a control that the work still happened, since a count of none passes over a path the test never reached.
The numbers live beside the tests and never in the contract: a recomposition is not a SwiftUI body or a Flutter
rebuild, and the before and after counts of the change that sets a number are its review's.

| Hot path | Budget test | What it counts | Budget |
| --- | --- | --- | --- |
| `route-measure` | `mountaya-geo`'s `commonTest`: `navigation/engines/RouteFollowingEngineTest.kt` | the measurements `cumulativeMeters` makes of each line followed, by identity, over thirty fixes, a reroute and thirty more, and every measurement made, which is theirs alone, so a copy of a line measured is counted, as `sdk-swift`'s budget counts it | 1 a line |
| `replay-measure` | `mountaya-navigation`'s `androidHostTest`: `seams/engines/ReplayLocationSourceTest.kt` | the measurements `cumulativeMeters` makes of each line a replay walks, by identity, its following's among them, over half an hour of ticks a second along a route of 600 legs, a new route and its walk of 400 legs to its end, and every measurement made, which is theirs alone, so a copy of a line measured on a tick is counted | 1 a line |
| `hover-hit-test` | `mountaya-elements`' `commonTest`: `map/engines/MapEngineTest.kt` | `ScriptedMapSurface`'s queries over sixty frames of five pointer moves, over every layer a hover is told of, and none while nothing listens | 1 a frame |
| `source-pushes` | `mountaya-elements`' `androidHostTest`: `renderer/rules/SourcePushesTest.kt` | the sources `sourcePushes` sends MapLibre for thirty changes to one of five GeoJSON sources, and for sixty frames of a profile's cursor | 1 a change, 1 a leg lit anew |
| `pin-images` | `mountaya-elements`' `androidHostTest`: `renderer/engines/PinImagesTest.kt` | the pins `PinImages` draws for forty markers of four looks moved thirty times, a look changed and a new accent | 1 a look |
| `profile-cursor` | `mountaya-elements`' `androidDeviceTest`: `renderer/components/ItineraryProfileTest.kt` | over sixty scrubbed positions, with the card and without, the curve paths the chart builds and draws, the navigator's curve drawn, and the compositions of every part the cursor does not move | 0 |
| `scrub-root` | `mountaya-elements`' `androidDeviceTest`: `renderer/components/ItineraryProfileTest.kt` | the compositions of the profile and of the layout it lays its parts out in, over sixty scrubbed positions | 0 |
| `panel-rows` | `mountaya-elements`' `androidDeviceTest`: `renderer/components/PoiPanelTest.kt`, `LayersPanelTest.kt` | the rows, and the layers panel's cards, switches and segments, twenty redraws of the host, a press on another and a zoom note compose, of those they leave as they were | 0 |
| `zoom-threshold` | `mountaya-elements`' `androidDeviceTest`: `renderer/components/PoiPanelTest.kt` | the compositions of the points-of-interest panel, its note and its rows over sixty camera frames from zoom 13 to 11, the note's floor crossed once | 1 a flip |

A composition and a drawing are counted through an internal render probe, `RenderProbe`, which each part a budget
reads reports to through `LocalMountayaRenderProbe`, a static `CompositionLocal` that holds none unless a test
provides one, Compose telling nobody when it composes a function again: in an app a part reads a composition local
that is null and builds no name. A measurement is counted through `MeasureProbe`, which
`cumulativeMeters` records each line to while a test runs its work through it and which reads one null field
otherwise, `InternalMountayaApi` rather than `internal` so the replay's budget in `mountaya-navigation` holds it
too; a query on `ScriptedMapSurface`; and a source sent and a pin drawn through `sourcePushes` and
`PinImages`, which the renderer runs and a host test runs with no MapLibre. The budgets of a composition need a
device and sit in the device tests, which `verify` compiles and `mise run test:device` runs; the others run in
`mise run ci`. The gate is `checkHotPathBudgets`, in `verify` among the source gates. It reads the entries listing
`kotlin` from the contract checkout, refusing a list of any other shape, and every Kotlin file of every test source
set of every module, the device tests' included, with the compiler's parser rather than matched as text, so a name
in a comment or a string names nothing. It fails on a listed hot path with no live budget test, on a budget test
naming a hot path the contract does not list for Kotlin or none at all, on one that is no `@Test`, on one switched
off by an `@Ignore` or a `@Disabled` on it or on a class around it, at any depth, on one that hands its constant to
no `assert…` call, and on one whose file declares no such `private const val`; its tests plant a source for each.
An absent checkout leaves each budget test held to its constant alone, and fails the run under CI, where `CI` is set.

**An outside-in suite.** A consumer Gradle build in `consumer/`, a build of its own, depends on
the modules by their coordinates, `com.mountaya:mountaya-client`, `com.mountaya:mountaya-admin`,
`com.mountaya:mountaya-geo` and `com.mountaya:mountaya-elements` at the repository's version, and on android
`com.mountaya:mountaya-navigation`, as an app declares them, and a composite build resolves those
coordinates to the modules beside it: each build includes the other, so the gate builds the
consumer and the consumer builds on its own. Nothing is published to resolve them from, and
nothing is read from a local Maven repository or `~/.m2` (§10). It is a Kotlin Multiplatform
module, as an app's shared code is, with the jvm, android and iOS simulator targets the gate
builds, and compiles against the public surface only, warnings as errors: every credential shape,
the session engine, every seam implemented from outside, one call of every kind, the calls by
declaration, every error read by its class, the geo rules and the navigation engines, the scripted
one, the published machine over a route source and one of its own, the map's engine with every
command and every value of its state, on android the device's engine with every seam it ships and a
location source and a voice of the app's own and the map's composable over an app's own
`OkHttpClient`, MapLibre's map reached through its ready callback, the admin unit's `admin` and a
walk of its listings, and the secret key, a session token's mint through the admin unit and a webhook
delivery's verification in its jvm code alone. The Compose compiler is applied to its android
target, as an app's own build applies it.
It fails on what an
inside test cannot see: a public type with an internal constructor, an interface whose member
names an internal type, a variant a consumer's target cannot resolve. `verify` builds it as
`checkConsumer`. The TypeScript SDK's `tests/consumer` is the same idea.

**Trail is the app the gate builds, measures and walks.** The contract's `sdk/trail.yaml` describes one sample app
every native SDK ships, its steps, the accessibility ids its journey finds an element by, the settings it reads and
the journey each SDK's device test walks through it, and this SDK's is `examples/trail`, an Android application in
Compose, in the package `com.example.trail`, MIT licensed as `examples/LICENSE` states so a developer copies it freely.
It is a Gradle build of its own, as the consumer is: `examples/trail/settings.gradle.kts` includes this checkout, so
the coordinates `com.mountaya:mountaya-client`, `mountaya-geo`, `mountaya-elements` and `mountaya-navigation` resolve
to the modules beside it, and never `mountaya-admin`, which an app that draws a map and navigates never declares. The
repository's settings include it where this build is the one run and the android target is built, and Trail run on its
own, `./gradlew -p examples/trail installDebug`, includes this checkout instead; that is what lets `checkConsumer`,
`checkAppSize` and `verifyDevice` drive its tasks by its build's name, `trail`, as they drove the consumer's. Beside the
app stands its session server, `examples/trail/session`, a JVM module of the same build depending on
`mountaya-admin` alone, on the JDK's own `com.sun.net.httpserver`, which `mise run trail:session` runs: it reads both
keys from the shell and refuses to start without either in one sentence naming the missing one, listens on
`127.0.0.1:8787` alone, which the Android emulator reaches at `10.0.2.2`, answers `POST /session` with the `Session`
`postSession` mints for the publishable key without `user_id`, as the JSON object the API answered under `data`, any
other path a 404 and any other method a 405, and a failed mint a 502 whose body names no key and no token; its tests
answer the client's every request through the client's own `HttpExchange` seam, with no network and no live key.
`checkConsumer` builds Trail's release through R8 at the modules' own Android floor, because MapLibre's native core
reaches back into its Java side through JNI, which R8 does not see, and a release build is where a missing rule
surfaces: a class nothing on the classpath provides fails it, and so does a requirement of an AAR's published
metadata, a library's own Android floor among them. It also compiles and packages Trail's journey, runs its ktlint,
and runs its session server's tests. Trail's settings are read once into one value its root takes, so a test hands in
its own: each from the environment or from `examples/trail/.env`, which is never committed, into a string resource
the app reads at runtime with `getString`. R8 cannot fold a resource lookup, so the release build the size gate
measures keeps every screen an app with a key shows without the placeholders `sdk/trail.yaml`'s `measured` names
compiled in, which a compiler that folds a constant would need. Cleartext HTTP is allowed in Trail's debug manifest
alone, for the session server on the emulator's `10.0.2.2`; the app asks for fine and coarse location through the
Activity Result API only when Start is pressed with Replay off, keeps the screen on while it navigates, and declares
no background location and no foreground service. Its own interface keeps clear of the Mountaya watermark's frame as the
SDK's does: its search box starts `watermarkClearance` and 8 density-independent pixels below the top of the window's
safe area, and its sheet's top is capped at that safe area's top and `watermarkClearance`, the sheet laid at the screen's
bottom offered no more height than that leaves, its tables shrinking first, so however far it is opened it never rises
into the frame (`searchBoxPlacement` and `sheetPlacement`, each with a device test). It writes every moment through
Geo's `formatDateTime` in the device's own zone, and where the platform knows no zone of that name it writes none and
leaves out what would carry it: the profile and the waypoint table are handed no `formatCutoffAt`, so no cut-off is
marked or written (`cutoffWriter`), and the progress line keeps the distance and the time left and leaves the arrival
out (`progressLine`), the choice `sdk-swift`'s `CONTRACT-GAPS.md` records for every SDK's Trail; nothing stands in a
moment's place, neither the instant written in UTC or in ISO 8601 nor an empty word. Its notice of a map the API
refuses reads the engine's own standing refusal, `MapState.isAccessRequired`, and Trail keeps no second copy of it.

**The scenario gate holds Trail to `sdk/trail.yaml`.** `checkTrail`, in `verify` among the source gates, reads the
contract checkout's `sdk/trail.yaml` for its shape, as `checkHotPathBudgets` reads `sdk/hot-paths.yaml`, and fails on a
step it lists that no `trail: <id>` comment under `examples/trail` names, the session server's code included, a
`// trail: …` line in Kotlin and Gradle's scripts and a `<!-- trail: … -->` one in Android's XML; on a marker naming no
step; on a value shaped as a key or a session token anywhere under `examples/`, in either shape `sdk/SPEC.md` gives one,
a `pk_`, `sk_` or `sess_` prefix before 32 lowercase hex characters, the shape its §1 documents for every credential, and
`sess_`, a base64url JSON payload, a `.` and a signature, the form its studio section reads a session token in, as
`sdk-swift`'s gate reads both, each line told once for each kind it holds and never repeating the value it found; on a
copy of `sdk/trail/itinerary.json` that differs by a byte, the demo itinerary Trail decodes as the `ItineraryWithDetails`
it is; and on a missing `examples/LICENSE`, which
`checkLicence` holds to sdk-typescript's `examples/LICENSE` byte for byte too. What is never committed is never read:
the key file, `.env`, and what a build writes. An absent checkout leaves the markers and the copy unchecked on a
contributor's machine and fails the run under CI, where `CI` is set.

**The app's size has a baseline.** Trail's release build, the one `checkConsumer` makes, is measured, and
the size the SDK adds is compared with a recorded figure, so a change that defeats R8, a keep rule or a reflective
read holding a printed surface the app never calls, fails the gate rather than a consumer's store review. The gate is
`checkAppSize`, run as `mise run size:check`, which `verify` runs after `checkConsumer` wherever the android target is
built, on CI's Linux runner as on a Mac. It reads the release APK with `apkanalyzer dex packages --defined-only` of the
Android SDK's command-line tools `mise.toml` pins, every name read back through R8's mapping, and holds shares of the
dex code and never of the APK: the native libraries, MapLibre Native's core above all, are compiled whole whatever an
app calls and move with releases a consumer's own ranges choose, while the dex code is where R8 shrinks. Every
hand-written file of a module declares its one package (§6), so each module's share is the bytes its package holds
once R8 is done, `com.mountaya.client`, `com.mountaya.elements`, `com.mountaya.geo` and `com.mountaya.navigation`, a
printed `generated` package among its module's; the app declares no `mountaya-admin`, so the admin unit has no share,
and the core's share holds none of it. Beside them stand the libraries the SDK brings into an app, each a share
reported apart, `io.ktor`, `kotlinx.serialization`, `okhttp3` and `org.maplibre`: code an app pays for without naming
it lands in the library that carries it and not in the SDK's packages. `examples/trail/size/android.json`
records each share with the whole dex code, the version of `sdk/trail.yaml` the app followed, `"scenario"`, the
version each library the SDK brings resolved at, and the Android Gradle plugin and the Kotlin the build was made with,
`MOUNTAYA_SIZE_BASELINE=write mise run size:check` records them after a deliberate change, and no baseline fails. A
library's version is read off the app's own build: `writeReleaseRuntimeVersions`, which Trail's `size.gradle.kts`
holds apart from the app's own build script, writes the version of each module its release runtime classpath
resolved, from Gradle's resolution result and never from a file's name, and a library is read at the module that names
its release, `io.ktor:ktor-client-core`, `org.jetbrains.kotlinx:kotlinx-serialization-core`,
`com.squareup.okhttp3:okhttp` and `org.maplibre.gl:android-sdk`, and the gate fails where the classpath resolves no such
module. The same task writes the graph of that resolution, one edge a line from the app, an SDK module or a library to
what it depends on, a platform's constraint no edge, and the gate fails on a library no share names: one an SDK module
reaches without passing through a library a share names, `io.ktor:*`, `org.jetbrains.kotlinx:kotlinx-serialization-*`,
`com.squareup.okhttp3:*` and `org.maplibre.gl:*`, and that the app does not reach through a dependency of its own. A
named library's own dependencies are its release's to bring, as its share's version is, OkHttp's Okio and MapLibre's
Gson among them, so a new SDK dependency fails the gate until a share names it, its package and its version, and a
library's new release does not; nothing is recorded while one stands unnamed.

**A share counts where R8 put the code, not whose code it is.** R8 merges classes and moves static methods across
packages as it optimises, and `apkanalyzer` counts each method in the class it landed in, read back through the
mapping to that class's name: an SDK module's code R8 merged into a class of `okhttp3` counts in `okhttp3`'s share, and
Compose's or the app's own merged into a class of `com.mountaya.elements` counts in the module's. So a brought
library's share can hold merged SDK, Compose and app code, a module's can hold another package's, and a share can move
on a change that touched no code of its package; at Trail's size the code standing in a package not its own comes to
tens of kilobytes. The gate holds what it can hold exactly: R8 run over the same classes places them the same way, so
a commit that changes nothing measures its baseline to the byte, a change that moves a merge moves a share by the bytes
merged, which the tolerance absorbs where they are few, and the SDK's modules together and the whole dex code, printed
beside the shares, show a change that moved code from one share to another. Whose code an app pays for is a trace, read
off the mapping's per-method origins in review, which the gate does not read.

A share that grows past 2% of its baseline or 16 KiB, whichever is larger, fails, each share on a line of its own
(`<share> <bytes>, <change> on the baseline, within <tolerance>`, or what it grew or shrank by), then the SDK's
modules together and the app's whole dex code, which are never compared. R8 run twice over the same classes
writes the same APK, byte for byte, so the tolerance absorbs no noise: it absorbs the growth of a change that adds
code the app calls, so that such a change does not record the baseline each time, while what the gate exists for is
larger, a printed surface kept whole weighing a megabyte. The floor holds a share that is small at its baseline,
where 2% of it is a few bytes. A share that shrinks past the tolerance is told and not failed, since a smaller app is
no defect. A baseline measured against another version of `sdk/trail.yaml` is reported and not compared, since the
scenario decides what the app does and so what R8 keeps, and the change that moves the version records the baseline
again; an absent contract checkout leaves the version unknown, the figures reported and not compared on a
contributor's machine and the run failed under CI. A baseline measured with another Android Gradle plugin or another
Kotlin is reported and not compared,
since R8 and the compiler shape the dex code as much as the SDK does, and a moved pin would otherwise fail the gate
on bytes no change here moved: the change that moves either pin records the baseline again. So is the share of a
library the SDK brings that resolved at another version than the one its baseline records, a note saying both
versions, since the library's own release shapes its share as much as the SDK does: MapLibre is a range (§10), and a
release within it, or an OkHttp it raises, lands on a commit that changed nothing, so the share is compared again once
`MOUNTAYA_SIZE_BASELINE=write mise run size:check` records it at the new release, and a baseline recording no version
of a library fails as one recording no share does. The SDK's own four shares are compared whatever its libraries
resolved at. `sdk-dart` holds its Flutter app's Dart snapshot to the same rule, and `sdk-swift` its iOS app.

**Determinism is a boundary too.** `Clock.System.now()`, `Random`, `Uuid.random()`,
`Locale.getDefault()`, the system time zone, `SharedPreferences` and `NSUserDefaults` are refused
outside `seams/`. A rule that reads the environment cannot be held to a fixture, and
`Locale.getDefault()` in a formatter is the specific case that makes a conformance run pass on
one machine and fail on another. The elements read the clock and the device's time zone for one
element alone, a layers panel's time section: the system's clock as `seams/engines/SystemClock.kt`
holds it, the default of `LocalMountayaClock`, and the zone its readout writes a moment in,
`seams/engines/DeviceTimeZone.kt`; no rule of the time and no engine reads either. The
conformance harness and the build's own tooling hold to the same law, though the determinism scan
reads neither. A runner's draws are the case's own, and the idempotency keys its scripted
`RandomSource` draws are counted in a version 4 UUID's shape, never read off the platform's random
source (§9). Build-logic reads the wall clock in one seam alone, `com.mountaya.build.seams.BuildClock`,
for the start of a device run, the instant its reports are held to.

## 8. The elements family and navigation

The contract's geo layer, the headless state over it, the navigation engine, the map engines and
the views. One question places every piece: would `sdk-swift` need it too? If it would, it is not
in a view, because a behaviour inside a composable is one only this SDK has and no fixture can
reach.

### The geo layer

Every rule is a pure function over data in `commonMain`, and every `geo.*` capability is one of
them, navigation's among them, which contract v0.9.0 carries in §14. The rules are `sdk/SPEC.md`
§7 and §14, proven by the `geo.*` fixtures, and are not restated here; what follows is what they
mean in Kotlin.

- **A position is the wire's `List<Double>`**, `[longitude, latitude]` with an optional altitude,
  as `GeoJsonPoint` carries one, so a position the API answers is handed to a rule as it arrived;
  a value Geo derives holds one the same way (`MapViewport.center`), and a generated type a rule
  reads is aliased rather than copied (`Boundaries`). A position names a place where its first
  two values are finite, and a rule handed one that names none answers rather than throws: a
  distance is `NaN`, a printed position null, the distance to a route infinite and a map's center
  held by no boundary. The distance to a route with no segment is `Double.POSITIVE_INFINITY`,
  which every corridor width refuses.
- **Distance is the great-circle distance** on the radius `geo/geodesy.json` declares, and every
  other distance is that function or a sum of it. `Location.distanceBetween` is never used: it
  measures on the WGS84 ellipsoid, and a distance a user reads has to agree with the other SDKs to
  the fixture's tolerance. Ground resolution is the one exception the contract makes, Web
  Mercator's sphere on 512-pixel tiles, and it is one function too.
- **Numbers, dates and times of day go through the platform's locale services with an explicit
  locale**, never the default and never assembled digits, which are Latin digits by construction.
  Geo publishes both formatters: `formatNumber(value, locale, maximumFractionDigits = 3)` and
  `formatDateTime(instant, locale, timeZone, dateLength = null, timeLength = null)`, which writes
  the instant's date at a `DateLength`, `Short`, `Medium`, `Long` or `Full`, its time of day at a
  `TimeLength`, `Short`, the hour and the minute, or `Medium`, the second too, or both, and the date
  at `Long` where neither is named. Each length is the locale's own style of that length: its
  fields, their order, its words, its digits and its hour cycle, 12 or 24 hours, the locale's
  whatever clock the device is set to, so the reference instant in Auckland is `오후 1:00` in Korean
  and `13:00` in German. A time of day names no zone, so it has no long or full length, the two
  styles that name one in words each platform chooses (`sdk/SPEC.md` §7), and a date with a time is
  joined as the platform's locale data joins them, never by a separator of Geo's own. The locale is a
  BCP 47 tag in which an underscore parts subtags as a hyphen does and a value that is no tag reads
  as English, the zone an IANA name, placed at the offset it keeps at that instant, and the text
  null where the platform knows no zone of it. The calendar is always the Gregorian one, whatever
  calendar the locale keeps or a tag's `-u-ca-` extension names, so `th-TH` writes 2026 and the era
  that names it, never the Buddhist year 2569 (`geo.format.locale-services`): on the jvm and android
  the locale is read with its `ca` keyword set to `gregory`, since the pattern a locale's style
  writes is chosen by its calendar, android's ICU is handed a Gregorian calendar of the zone besides,
  and on iOS the formatter is handed the Gregorian calendar after its locale. An hour cycle a tag
  names of its own, `-u-hc-`, is outside the contract, and each platform reads it or not as it reads
  any keyword: the jvm's `java.text` ignores it. A number that is no finite number is the empty
  string, and three places are what every CLDR locale keeps for a plain number. On the jvm the
  `actual` is `java.text`'s `NumberFormat` and `DateFormat`, `getDateInstance`, `getTimeInstance` or
  `getDateTimeInstance`, which write a locale's own digits, `١٬٥٣١` for Egyptian Arabic, and read no
  setting of the machine they run on; the JDK joins a date and a time as its data joins the shorter
  of their two lengths, which writes every case of the fixture as ICU does. `java.time` is not used:
  its formatters write Latin digits unless handed `DecimalStyle.of(locale)`, `05:45` where Nepali
  writes `०५:४५`, and android has it only above this SDK's floor. On android a number is
  `java.text`'s too, and an instant is written by `android.icu.text.DateFormat` over the tag's
  `ULocale` and a `GregorianCalendar` of the zone, which the floor's release is the first to carry
  and the reason the floor is that release, since android's `java.text` follows the device's 12- or
  24-hour setting for a short and a medium time where the person set one, `13:00` for Korean on a device
  set to 24 hours, and joins a date and a time with one space whatever the locale writes, where its
  ICU keeps the locale's clock and joins a date and a time as CLDR joins a date of that length,
  `13.03.2026, 20:00` in German as on the jvm and iOS. The jvm and android hold the seam,
  `seams/engines/LocaleDateText.kt`, as one file twice, which asks each source set for its
  formatter: the jvm's `PlatformDateFormat.kt` hands it `java.text`'s, and android's
  `IcuDateFormat.kt` ICU's, the one file of Geo that names an Android package, as §7
  allows in `seams/` alone. On iOS the `actual` is Foundation's `NSNumberFormatter` and
  `NSDateFormatter`, its date style and its time style each the one of the length named and none
  where none is, the date's long where neither is, over a locale built from the components
  Foundation parses out of the tag: its canonical reading of the tag takes a `-u-nu-` system named
  as the language's script, `bn-BD-u-nu-beng`, for that script and drops it, and the formatters
  then write nothing, while a system Foundation does not carry is dropped, so the locale writes its
  own digits, as `java.text` reads such a tag. A formatter is expensive to build, so
  each target keeps its formatters, bounded at 64 because a cache keyed by locale grows with
  every locale a user passes through. On jvm and android each thread keeps its own, one per locale
  and shape, an instant's being its two lengths and its zone, and formats with it directly, with no
  copy per call, since neither a `java.text` formatter nor an ICU one is thread-safe and one
  thread's is never handed to another; past 64 the one that thread used least recently is dropped,
  so a locale in use is never built again. On iOS an `NSCache`, which Foundation makes safe to
  share, evicts on its own. A number is grouped where the locale's CLDR minimum
  grouping digits say, by Geo's own rule on every target (`isGroupedAt`, over CLDR 48's table as
  ICU 78 carries it, `minimumGroupingDigitsOf`), since `java.text` holds no such digits: Spanish,
  Italian, Polish and a dozen more group no number under 10,000, `1234` and `12.345` in Spanish,
  where Latin American Spanish groups from 1,000, as ICU writes them on iOS.
  Where a platform's locale data writes another string than the contract's vintage, the
  platform's string stands (§9). A position printed for
  a reader is the contract's one exemption: its digits and decimal point are the same in every
  language (`geo.format.lnglat`), and they come from no formatter. Each coordinate's four places
  are its binary value rounded half away from zero by integer arithmetic, so 6.86525, held as
  6.865249..., prints `6.8652` and 0.03125, exactly halfway, `0.0313`; every finite coordinate is
  written in full, never in an exponent, and a position that names no place prints null.
- **A value is rounded before a platform formatter sees it**, half away from zero on its decimal
  value, as the unit-formatting cases expect (850 m is `0.9 km`): `java.text` rounds half to even
  by default, on the binary value a `Double` holds, so a formatter handed an unrounded value
  rounds it its own way. The decimal value is the one a reader writes the double as: a value that
  is the double nearest a halfway decimal rounds away from zero, and any other as its binary value
  lies, so 0.85, held as 0.849999..., is `0.9` to one place. A value that rounds to zero is
  stated with no sign, `0°` and never `-0°`.
- **A measurement is stated through one table of unit patterns**, since neither `java.text` nor
  Foundation on every platform formats a unit: CLDR 48's short patterns, and its narrow ones for
  a duration's hour and minute, as ICU 78 carries them, for the seven languages the contract's
  `Locales` is keyed by, `de`, `en`, `es`, `fr`, `it`, `ja` and `zh`, read by the primary subtag
  as a label language is, and English's for any other, the digits still the locale's own. The
  table holds CLDR's minimum grouping digits too, so Spanish and Italian group no number under
  10,000, and a language outside it keeps its own, so a Polish 1531 is `1531` on every target, as
  `sdk-swift` writes it. Each formatter, `formatDistance`, `formatElevation`, `formatSlope`, `formatSpeed`,
  `formatTemperature` and `formatDuration`, takes a `UnitPreferences`, the reader's language and
  the wire's four preference types, reads its own preference and answers the empty string for a
  value that is missing or no finite number. A preference this build does not know reads as its
  default: metric, per time, degrees and Celsius. A pace per distance is the pace clock spliced
  into its pattern, `21:27 min/mi`, the clock's digits Latin in every language, as
  `geo.profile.pace-clock` writes them.
- **A unit converts through the exact value that defines it**, never a rounded factor, and the
  unit ids are the contract's, ECMA-402's with its stated exceptions. Geo computes every
  conversion from those values itself and never reads one off a platform's conversion table,
  `android.icu`'s included, whose factors are the platform's to choose. Each quantity is a closed
  `enum class` whose entries carry the contract's id (`LengthUnit.Mile.id` is `mile`), and one
  function per quantity converts between two of its units, `convertLength(value, from, to)`, so a
  conversion across quantities does not compile. A pair of whole magnitudes is reduced before it
  is applied, so a factor that is a whole number or a whole fraction is one exact operation: a
  mile is 5,280 feet, never 5,280.0002. `DurationUnit` shares its simple name with
  `kotlin.time.DurationUnit`, which no file imports by default, and a file naming both imports one
  under an alias.
- **Text is matched by code point, never by `Char` and never by grapheme cluster.** A Kotlin `Char`
  is a UTF-16 code unit, so the search rules read a surrogate pair as one code point in common code,
  where `codePointAt` is the JVM's alone, and an emphasized run is code points with their combining
  marks (`geo.search.highlight`). The accents a fold drops are read off Unicode's canonical
  decomposition, a segment's letters joined by its canonical composition and its marks by their
  general category, none of which common Kotlin carries: each comes from an `actual` under `seams/`,
  `java.text.Normalizer` and `Character.getType` on jvm and android and Foundation's canonical
  mappings and non-base character set on iOS, and the fold over them is common code. Whether a code
  point joins the segment before it is a table lookup: a combining mark joins, and of the code points
  that are none, only those whose decomposition begins with a letter that composes with the one
  before it, the Hangul medial vowels and final consonants and Kirat Rai's vowel signs E and AI in
  Unicode 17, ask the normalizer whether they compose; every other one starts a segment, and a test
  holds the table to each platform's own normalizer. A round folds its query once, and every row of
  every section it fills matches its title against that fold.
  `highlightRanges` answers UTF-16 index ranges, the unit a Kotlin string and a Compose
  `AnnotatedString` count in, and `highlightParts` the runs themselves, the unit every SDK shares,
  which a `SearchRow` carries ready to draw as `parts` beside its `matched`.
  The one rule that reads a grapheme cluster is a badge's first letter (`geo.icons.glyph-fallback`),
  which the contract names the first character a reader sees: common Kotlin reads no cluster, so it
  comes from an `actual` under `seams/`, `java.text.BreakIterator`'s character instance under the
  root locale on jvm and android, whose clusters are Unicode's extended ones, and Foundation's
  composed character sequences on iOS, which part a carriage return from the line feed after it
  where Unicode's segmentation never does, so that one pair is joined there. Which version of
  Unicode's segmentation reads the cluster is each platform's (`sdk-swift`'s `CONTRACT-GAPS.md`).
- **A word compared in ASCII is folded in ASCII.** A language subtag, a hex colour, a glyph's
  attribute name and a fill keyword are compared with their ASCII capitals folded and nothing
  else, never by a platform's full case mapping, which differs by platform and by Unicode version
  and folds the dotted capital I into an `i`. A label language is the generated
  `RestSchema.Language`, read off a tag's primary subtag before its first hyphen or underscore,
  English for any other (`geo.style.label-language`), and a catalogue entry's name in it is read
  from either bundle's `Locales`, an empty name falling through (`geo.i18n.localized-name`).
- **A label and a bundle of words are the JSON their source writes.** `relabelTextField` takes a
  symbol layer's `text-field` as a `JsonElement`, the style specification's own JSON and no
  renderer's type, and answers the label to write, or null where the layer is left as it is; it
  reads at most 64 arrays deep, with no recursion where it checks the depth. A bundle's words are
  a `JsonObject` tree, the English and each translation of a `WordBundle`, folded by
  `resolveBundle`, and a typed set of words is decoded from the tree it resolves to. An override
  is a `WordOverrides`, keyed by section or by language, and `wordOverridesOf` reads one from
  JSON, answering null for one that names a language and a section together, so no rule throws
  for it (`geo.i18n.bundle-resolution`).
- **A time zone is an argument**, its IANA name as a `String`, never a read of the ambient zone,
  and a zone is read under the name the platform reports for it, a link the tz database keeps
  included (`geo.timezone.viewport`), since `java.time` and a device's settings report the name a
  zone was set under. The name is matched exactly, letter case included, and the table is the
  contract's, `geo/timezone-viewport.json`'s `zoneViewports` and `reportedZoneNames` value for value.
- **A value Geo derives compares by what it holds, each number as the same number.**
  `MapViewport`, `CameraPose`, `ZoomBounds`, `PixelPadding`, `SlopeAspect`, `TerrainNeighbours`,
  `RouteGeometry`, and every value the profile, layers, icons, i18n and search topics publish, are
  equal where every field holds the same numbers in the same order, `NaN`
  the same as `NaN` and 0 another than -0, the relation `geo.camera.route-content` states for a
  route: never identity, and never IEEE `==`, under which a value differs from itself and hashes
  apart from one it equals.
- **A box is the wire's `List<Double>`, `[west, south, east, north]`**, as a boundary's or an RFC
  7946 `bbox` carries one, and a pan lock's longitudes are the pair `[west, east]`. A box across the
  antimeridian is written with its east edge past 180, and `normalizeBoundingBox` never reads a west
  edge greater than the east one as one. A box that is not four numbers answers four `NaN`s, as a
  lock with an edge at no number does, and `boundsOf` answers null where no position names a place.
- **Ground resolution is one function, `groundMetersPerPixel`**, on Web Mercator's sphere and
  512-pixel tiles, whose three constants are published. `altitudeToZoom` and `zoomToAltitude` take
  the canvas height, the nominal 800 pixels where the caller names none or one that is not a finite
  number above zero, and the zoom an altitude converts to is held within [4, 16], the floor and the
  ceiling `geo/zoom-altitude.json` declares beside the field-of-view factor of 1.5 the camera stands
  at, so one altitude is one zoom in every SDK.
- **A fly-through's route is a `RouteGeometry`**, which `itineraryRoute` builds from an itinerary's
  segments as the REST API answers them, `RestSchema.ItinerarySegment`, or from the one its id
  names. Its elevations and jumps are lists, empty where the route states none, so no list and an
  empty one are one route by construction, and a jump is an `Int` vertex index. A line is measured
  by `cumulativeMeters`, `routeLengthMeters` and `positionAtMeters`, a vertex that names no place
  bridged as every walk of a line reads it, and a line that names no place has no position along
  it, `[NaN, NaN]`. A route followed is read along without being measured again:
  `positionAtMeters(route, meters)` answers the position `positionAtMeters` places along the route's
  line, number for number, read off the distances the `FollowedRoute` measured as it was followed and
  the vertices that name a place it kept then, by a binary search over those distances, since a vertex
  that names no place adds nothing to the distance of those after it. Both read a line through one
  rule, so the two cannot drift apart, and it is how a replay reads each tick (`replay-measure`).
- **What a consumer only observes stays `internal`.** The ground distance a reader is told
  (`geo.camera.ground-meters`) is `internal` to `mountaya-geo` and runs from its own tests. Kotlin
  has no visibility between `internal` and `public`, so it is published, deliberately, when the
  engine of `mountaya-elements` that applies it is written. The pan-lock refusal
  (`geo.camera.max-bounds-refusal`) was published so, as `refusesMaxBounds`, with the map engine's
  `setMaxBounds`: a predicate, since no rule of the geo layer throws, and the class of the refusal is
  the engine's, a `MapException` of kind `MaxBoundsRefused`.
- **A fit's camera is computed by Geo, never asked of a renderer.** `cameraForBounds(bounds, canvas,
  window, padding, maxZoom)` answers the `MapViewport` a fit lands on (`geo.camera.fit`), its centre
  and its zoom, facing north by its type, since a fit decides no bearing to state wrongly and no
  pitch, which the camera framing it keeps: the box read in canonical form, its Web Mercator
  projection on 512-pixel tiles fitted to the canvas, `[width, height]`, less the `PixelPadding`, the
  zoom held under the fit's ceiling, then within the `ZoomBounds` window, and the centre moved by half
  the difference of opposite paddings at the zoom the camera lands at, never the one the box asked
  for, so a window that holds the camera moves the centre by the pixels the padding states at the
  zoom shown. It is null where the padding leaves the canvas no room, a padding or a canvas that is no
  number included, and where the box is not four numbers or the canvas not two; an edge that is
  `NaN` is kept unknown, a viewport of `NaN` answered rather than a guess, as in `sdk-swift`. Content
  is framed on what it stores: `itineraryBounds` is the box `boundsOf` draws around every segment's
  stored geometry, a directions segment's vias included, and `specificationBounds` reads a
  collection's box, written across the antimeridian as RFC 7946 writes it, with its east edge past
  180, null where it states none or not four numbers (`geo.camera.content-bounds`). `zoomIntoRange`
  answers the nearer edge of a window a camera stands outside of, and none for a zoom that is no
  number (`geo.camera.zoom-into-range`). `accuracyBounds` and `LOCATE_MAX_ZOOM`, the box a fix is
  framed by on the Web Mercator sphere and the zoom it is framed at at most
  (`geo.camera.accuracy-bounds`), and `trimWhiteSpace`, the one reading of white space a map's blank
  credit is read by, are published, since the map engine applies them. `zoomLimitReached`, which
  answers the `ZoomWindowEnd` a camera stands at, and `zoomControlsDisabled`, the `ZoomStep`s each end
  disables (`geo.camera.zoom-limit-reached`), are published, since the zoom pair of `mountaya-elements`
  reads them, a step named `ZoomStep` since the composable is `ZoomControl`, as in `sdk-swift`.
  `hoveredItinerary`, which reads the features under a pointer by the properties each carries, never by
  an id (`geo.layers.hovered-itinerary`), stays `internal` until the elements a hovered itinerary
  lights are written.
- **A tile layer draws at the rank its source layer declares**, and the table is a record a
  consumer can patch, never a frozen map: `tileSourceLayerRanks` is a `Map<String, Int>` a
  consumer adds to, and `LayerRank` names all twelve bands, ten apart from 10 to 100 but for the
  focus on an itinerary at 52, `LayerRank.ItineraryFocus`, the preview and the emphasis just above
  its line, and its restricted stretches at 55, over its line and beneath its direction, the number
  the contract's table gives `itinerary_restrictions`, so the elements' one layer registry ranks every
  layer alike in every SDK, and a consumer's own band between the points of interest and the
  waypoints takes `LayerRank.Poi.rank + 2`, 82, as `sdk-typescript`'s example does. The layer rules read the Tile
  API's own types, `TileSchema.TileLayer`, `TileSource` and `Layers`, which the map draws from;
  the REST bundle's twins of them, which Map Studio's `getLayers` answers, are another Kotlin
  type of the same shape. A specification paints across its layers' zoom window, a `ZoomBounds`
  or null (`geo.layers.paint-zoom-range`), and an overlay slug names the first overlay carrying
  it, in an `OverlayKind` of category where one is asked (`geo.layers.overlay-lookup`).
- **A Data API answer draws as a `GeoJsonFeatureCollection`**, Geo's own `Feature` and
  `FeatureCollection` over the client's `GeoJsonGeometry`, each feature's properties the
  `JsonObject` a renderer's expressions read: the datum's own members, null where it states none,
  and the `mtyaKind`, `mtyaIndex`, `mtyaColor` and `mtyaWidth` Geo stamps. A datum whose point
  names no place is a feature with no geometry, so every feature keeps its place in the answer
  (`geo.layers.data-geojson`).
- **A map's paint is a value restyled on a copy** (`geo.style.map-paint`). `MapPaint.standard` is
  the shipped paint, and a patch is its `copy { }`, whose builder's groups are builders too, so a
  patch reads as member setting: `MapPaint.standard.copy { itinerary.casingColor = "#1b1b1b" }`. A
  list set replaces the shipped one whole, a nested member set leaves its siblings, and
  `layerRanks += "acme_avalanche" to 40` adds a band; a band named `__proto__`, `constructor` or
  `prototype` is left out, as every SDK's merge of a patch leaves those names out. Like `ElementTokens` its constructors are
  `internal`, so a member the contract adds breaks no caller and no ABI line, and it compares each
  number as the same number. Its ranks, default colour and palette are `tileSourceLayerRanks`,
  `DEFAULT_SEGMENT_COLOR` and `livelyPalette` themselves. An itinerary's stroke is a line layer's
  style (`geo.style.itinerary-stroke`): `itineraryStrokeStyle(stroke, paint)` takes an
  `ItineraryStroke`, `Line(color)`, `Dashed(color)` or `Casing`, its colour a `JsonElement` literal or
  expression, and answers a `LineLayerStyle`, the `layout` and `paint` a style document writes, the
  width interpolated over the paint's table as it is held; the layer's id, source and filter are the
  caller's.
- **A point of interest is drawn by its category.** `poiStyleOf(category)` reads
  `poiCategoryStyles`, a `PoiStyle` of a marker glyph's name and a `HexColor`, matched as written,
  letter case included, and null for a category the table does not name (`geo.layers.poi-style`);
  `poiCategoryOf(category)` reads a tile's category through `poiTileCategories`, only the ASCII
  capitals folded (`geo.layers.poi-category`). An isochrone band below zero takes the ramp's first
  stop (`geo.layers.isochrone-color`), and a matrix connector's width is read off its cost by
  `matrixLineWidth` (`geo.layers.matrix-width`).
- **An element handed an itinerary's id reads it through `ItineraryResourceEngine`**
  (`geo.data.itinerary-resource`). Its `state` is a `StateFlow` of `ItineraryResource`: `Idle`,
  `Resolving`, `Resolved` holding a `ResolvedItinerary` with its four projections at once, or
  `Failed` with what the source threw, each reported only where it differs from the one held. It
  asks as soon as it holds a question, at construction or in `configure(cache, itineraryId,
  surface)`, and `close()` ends it; an empty id is none, a question asked again asks nothing, an
  answer to a question it no longer asks never lands, and an answer its cache holds is shown before
  the call returns. Common Kotlin has no weak reference, so a cache keeps an engine watching until
  the engine is closed: an app closes the engines it lets go of, by `use` among other ways, the engine
  being an `AutoCloseable`. The cache does not hold its engines weakly as `sdk-swift`'s does: a weak
  reference here would be an `expect` under `seams/` over the jvm's and Kotlin/Native's own, and on
  the jvm it clears whenever a collection happens to run, so which engines an invalidation asks again
  for would hang on the collector, which no fixture and no test can hold, where Swift's reference
  counting releases an engine the moment its last owner lets it go.
- **Answers are a cache's, one per client** (`geo.data.itinerary-cache`). The app makes one
  `ItineraryResourceCache(scope, source)` over its `ItinerarySource`, a `fun interface` whose one
  `suspend fun read(itineraryId, surface)` is `getItinerary` in one line, and hands it to every
  engine. Answers are kept by cache, id and surface, `ItineraryResourceCache.capacity`, 16, most
  recently read kept, reads in flight counted, and a failure dropped at once.
  `invalidate(itineraryId)` drops an id on every surface, or every answer, and asks again for each
  engine watching it, in the order each began to, each showing what it showed until the new answer
  lands. Each read is launched in the cache's `scope`, whose dispatcher is the thread the cache and
  its engines are driven from, and cancelling that scope cancels every read in flight.
- **An itinerary's projections are rules over the wire's types**: `itineraryGeometry(segments)`, a
  collection of each segment's stored geometry in chain order (`geo.data.itinerary-resource`),
  `itinerarySegments(segments, groups)` answering each `ItineraryLeg` with its six section figures
  and no running total (`geo.data.itinerary-segments`), and `itineraryWaypoints(waypoints)`
  answering each `ItineraryPlace` with its `WaypointDetails` (`geo.data.itinerary-waypoints`).
  Every chain is read by `position`, ties in the order listed, by Kotlin's stable `sortedBy`. A
  restricted stretch of a route is `roadAccessFeatures(geometry, info)`: REST's `road_access_info`
  ranges cut from the lines of a geometry at any depth of a collection, each piece a LineString
  feature whose `kind` property is the `RoadAccessKind` id the tiles' `itinerary_restrictions`
  features carry, so one pair of layers stripes both (`geo.data.road-access-stretches`).
- **An icon is resolved and read without a renderer.** A waypoint's sprite id is a pure function of
  its `RestSchema.WaypointIcon` and its colour, the colour's leading `#` dropped and its ASCII
  capitals folded (`geo.icons.resolution`); an activity's silhouette, `activityMarkerGlyph(activity,
  set)`, is named only where the set holds it, null for an empty slug, and a restricted stretch's
  glyph and colour are `roadAccessGlyph` and `roadAccessColor`, the second a `when` over
  `RoadAccessKind` that a kind the vocabulary gains does not compile past. An icon's markup is read
  by `parseGlyph` into `GlyphShape`s, in one pass, by a scanner written here rather than a
  platform's XML parser, which would refuse markup that is not well formed and read entities this
  rule never expands (`geo.icons.glyph-parse`); a fill, a stroke, a line's cap and join and a miter
  limit are kept through one internal rule, `keptGlyphValue`, a miter limit only as a plain number
  scanned character by character, never through a platform's number parser, which reads a sign, an
  exponent or `Infinity`.
- **A glyph set is a value that can always be drawn.** `GlyphSet` holds its name, its paint, its
  width, its box and its markup by glyph name, every member read-only, and its constructor is
  `internal`: the contract's two rules about sets are the two factories of its companion, each
  answering null, the refusal, as no rule of the geo layer throws. `GlyphSet.of(name, paint,
  strokeWidth = 2.0, viewBox = 24.0, markup)` builds a host's own set, keeping a copy of its markup
  (`geo.icons.glyph-set`), and `GlyphSet.chaining(sets)` chains sets, each glyph from the first that
  holds it, drawn as the first is, named by its sets' names joined by `+`
  (`geo.icons.glyph-set-chain`), so a renderer handed a set never meets one it cannot draw. A paint
  that is neither `fill` nor `stroke` is refused by the closed `GlyphSet.Paint` before any set is
  built; the printed sets, `GlyphSet.marker` and `GlyphSet.control`, are built through the internal
  constructor under their table's name, the printer holding them to the bounds the factories hold.
  `glyphPaint(shape, set)` answers how one shape is painted (`geo.icons.glyph-paint`), a
  `ShapePaint` whose fill and stroke are each in the current colour or not, `isFilled` and
  `isStroked`, and whose cap, join and fill rule are closed enums carrying SVG's keyword as their
  `id`; it reads a shape's own values through the parse's own reading, so a value the parse drops is
  no statement. What a surface draws in place of a glyph its set cannot draw, the fallback pin for a
  place and words for an amenity's badge, `placeGlyph` and `badgeFallback`
  (`geo.icons.glyph-fallback`), stays `internal` until a composable of `mountaya-elements` draws it;
  a badge's first letter is its first grapheme cluster (above), written in capitals by Kotlin's
  `uppercase`, Unicode's own case mapping, which reads no locale. A set's markup is a `Map<String,
  String>`, which keys a glyph's name by its UTF-16 units, as `sdk-typescript`'s object does, where
  `sdk-swift`'s dictionary reads two canonically equivalent names as one; every name the contract's
  table and vocabularies hold is ASCII, where the readings agree.
- **A token reads the consumer's value first** (`geo.style.theme-tokens`). `themeTokenValue(token,
  scheme)` reads a `ThemeToken` under a `ThemeScheme`, `Light` or `Dark`, each carrying the contract's
  spelling, never a platform's scheme: the dark value under the dark scheme where the token states one,
  and the light value otherwise. A consumer states a value of its own by setting the token whole on a
  copy, `ElementTokens.standard.copy { controlBg = ThemeToken(light = HexColor("#000000")) }`, which
  states no dark value, so its value stands in both schemes as the contract's does; one that wants a
  value of its own under each scheme states both, which the contract's override cannot say.
- **A nested scope inherits what it does not state** (`geo.i18n.nested-scope`). An `ElementsScope`
  holds a language, the locale its figures are written in, its words resolved from the elements' own
  (`WordBundle.elements`, French included, so no translation is handed over), and its control and
  marker glyph sets; `ElementsScope.standard` is English in the locale `en`, and `scopeOf(outer,
  statement)` nests an `ElementsScopeStatement` in it, every member optional. Its words are the outer
  scope's while the statement states no words and no language other than the outer one, a language
  restating the outer one keeping them, and resolve afresh otherwise; its locale follows a language
  other than the outer one, read as its own tag, unless a locale is stated; and a control set stated
  never stands in for the marker set. A scope's constructor is `internal`, so a member the contract adds
  breaks no caller. `word(path)` reads a word by its dotted path, the English word of the elements' own
  where an override left text out, and the path itself where neither holds text.
- **A counted word takes its language's form** (`geo.i18n.counted-words`). `countedText(word, count,
  language)` reads the table's object of forms: French takes `one` for zero and one, and English, as
  every language the table does not hold, `one` for one alone; its first `{count}` takes the count
  written by `formatNumber` in the words' language, so 1234 is `1,234` in English and `1 234` in
  French, grouped by U+202F, the bytes `java.text` writes on the jvm and android and Foundation on iOS,
  which a test holds on each. `filledText(template, value)` fills a template's first `{name}`, a name of
  ASCII letters and digits led by a letter, with the value exactly as handed, `$&` and all; null is the
  refusal of a word that is no counted word.
- **The pickers' models are Geo's** (`geo.layers.picker-rows`, `geo.layers.poi-rows`,
  `geo.units.preference-choices`), published since the panels of `mountaya-elements` read them.
  `layersPanelSections(selection, language, isLocked, picked, offersTime)` lists a `LayersSelection`'s
  catalogue in the four sections of the map studio's layers panel, in its order: the terrains, a section
  standing while the catalogue is still to land; the enrichments, every category whose overlays stack, the
  points of interest left out, `POIS_OVERLAY_SLUG`, since their own picker drives them; the classic
  overlays, one category at a time, `categories` listing each classic category holding an overlay as a
  `LayersPanelCategory`, the section opening on `picked`, the category the reader last picked, where it is
  listed, else on the first holding the selected overlay, else on the first; and the time, with no row,
  where the host offers it. Each `LayersPanelSection` states its `LayersPanelHeading`, a word of the
  `layers` section by its path, `layers.terrain`, `layers.enrichment`, `layers.overlays` or `layers.time`,
  its `LayersPanelChoice`, the catalogue category it lists, and its rows, each `LayersPanelRow` named
  through `localizedName`, checked by the selection's slugs compared exactly, and marked `Lock` in place of
  `Tick`; `layersPanelPress(choice, row)` answers a `LayersPanelPress`, the command of the row's choice or
  the report of a locked entry. A category's own name no longer heads a section: it names its segment of
  the overlays' switch.
  `poiPanelRows(drawn, isKeyVisible, keys, groups, activeGroups, labels)` answers `PoiPanelRows`, its
  `PoiPanelRow`s and the master row, over curated `PoiPanelGroup`s, and `poiPanelNoteZoom(minZoom)` the
  zoom below which the note stands. `preferenceChoices(preferences)` answers the five
  `PreferenceChoice`s over a `UnitPreferences`, each `PreferenceOption` a `PreferenceValue` in the
  wire's own types, labelled by a `PreferenceLabel`, a language checked as `labelLanguage` reads the
  held tag; `preferencePress(preferences, value)` answers the `PreferencePress`, the set with one
  preference changed and the language the map relabels in. The rules keep `sdk-typescript`'s names,
  where `sdk-swift`'s take `Of` against two files of one name in one target: here a contract and a rule
  of one name sit in two directories, in one package that tells the class from the function.
- **A layers selection drives a map it does not mirror** (`geo.layers.selection`,
  `geo.layers.selection-outcome`). `LayersSelectionEngine(scope, map)` stands over a
  `LayersSelectionTarget`, which `MapEngine` implements and a map of the app's own may: the terrain the
  map shows, its catalogue read, and `setTerrain`, `setOverlay`, `clearOverlay`, `addEnrichment` and
  `removeEnrichment`, every command suspending, the two that take something off as much as those that
  draw, as they are asynchronous in `sdk-dart` and `sdk-swift`, so a map of the app's own may wait on
  its renderer to take a layer off, and every one free to throw. Its `state` is a `StateFlow` of
  `LayersSelection`, the catalogue, the terrain, the classic overlay, the enrichments in the order turned
  on, and the refusal of the latest command or catalogue read to settle, two selections equal where they
  hold the same and the very same refusal. Each command drives the map in a coroutine of its own in the
  caller's scope, an outcome owed to an earlier map landing nowhere, the same map brought back included;
  it reads the catalogue when it is handed a map, and again where the same map is handed while its read
  failed, and a landing states the terrain the map reports where none is selected and ends a refusal
  standing, as the first SDK's does. `close()` cancels nothing it sent and lands nothing more.
- **A layers selection holds the instant pinned** (`geo.layers.selection-instant`).
  `LayersSelectionEngine.setInstant(instant)` holds an `Instant`, or none, the clock's reading, as
  `LayersSelection.instant`, and drives the map to it at once through `LayersSelectionTarget.setInstant`,
  a command as the others are: asked again for the instant already held, its refusal kept and reported,
  the pin standing through every other command, and a replaced map, or none, starting with none pinned.
  The target's `setInstant` has no default: a map of the app's own states what it does with a pin, nothing
  where it draws no time-aware overlay, rather than inheriting a silence. An `Instant` names an instant or
  is no value, so a pin that names none is refused by its type.
- **The time rules are Geo's, and none reads a clock** (`geo.layers.overlay-instant`,
  `geo.layers.tile-timestamp`, `geo.layers.time-window`, `geo.layers.time-slider`,
  `geo.layers.time-readout`). Each is handed the clock's reading, a `kotlin.time.Instant`, which a
  `kotlin.time.Clock` answers on every read: the contract's clock that reads no instant is refused by
  the type, so `overlayInstant(pinned, now)` answers an `Instant`, the pin, else the reading floored to
  its minute, a reading before the epoch falling in the minute before it. `timestampedSources(catalogue,
  overlay, instant)` answers each `TimestampedSource` of the selected classic overlay the server stamps,
  looked up among the classic categories alone, each address edited as text, `TIMESTAMP_PARAMETER` set
  where the first pair whose name decodes to it stands, or appended, the instant written by the client's
  `formatTimestamp`, in UTC to the millisecond with `.000` kept, and percent-encoded as a URI component.
  `timeSliderWindow(now)` lays out a `TimeSliderWindow`, `TIME_WINDOW_PAST_HOURS` back and
  `TIME_WINDOW_FUTURE_HOURS` ahead at one stop an hour, its `start` null where either end would fall more
  than 8.64e15 milliseconds from the epoch, which a Kotlin `Instant` would hold but no ECMAScript or Dart
  instant does; `valueAt` places an instant at its nearest stop, a half hour at the later one, counted
  in nanoseconds, and `dateAt` answers null for a value off the track. `timeSliderTicks(window)` answers
  each `TimeSliderTick`, its word by its path, `timeSliderRelease(window, value)` a `TimeSliderRelease`,
  or null on a track of no instant, and `timeSliderReadout(window, value)` a `TimeSliderReadout`, the
  instant beside the offset's word by its path and its count, flat where `sdk-typescript` nests the
  offset.
- **A locate control says what the permission says** (`geo.controls.geolocate-state`).
  `GeolocateControlEngine(scope, map)` stands over a `GeolocateControlTarget`, a fix it asks for and
  `isLocationRefused()`, both suspending, which reads the standing permission without asking anyone and
  throws where it cannot say, read as nothing refused. Its `state` is a `StateFlow` of
  `GeolocateControlState`, busy until every fix it asked for settles, blocked while the latest read
  answers refused, and live while it stands over a map; each read is numbered, so an earlier one's
  answer, and one from a map replaced, lands nowhere. `locate(onError)` hands on each failed fix, never
  after `close()`. `MapEngine` reads `GeolocationPermission.Denied` alone as refused, so a fix refused
  while the permission was never asked, `Prompt`, is no block (Q7).
- **A search's rounds are decided by rules.** `searchQueryChangeOf` answers what a typed query asks
  of a search after the one settled, a round, a cleared search or nothing, and `isSearchLimit` holds
  a round's cap (`geo.search.query`); `searchViewportBounds` is the box a viewport-restricted search
  sends; a `SearchFilter` shows its sections in the order given; and a result is a `SearchRow` built
  from the Data API's own result type, identified by its kind and its identity, worded by a
  `SearchWording`, and no row where it states no place or no identity (`geo.search.rows`).
- **A search is one engine** (`geo.search.section-order`, `geo.search.typing-pause`,
  `geo.search.live-round`, `geo.search.broaden`, `geo.search.viewport-refresh`). `SearchEngine`
  takes the caller's scope and launches each pause, round and request in it, and its `state` is a
  `StateFlow` of what a result list shows for the query in the box: its own answer where it answers
  that query, trimmed, and the skeleton of the round the query is owed otherwise. `setQuery` waits
  out the typing pause, `SearchPause.standard`, 150 ms, unless the engine is given another, a pause of
  zero asking as the query is typed and one below zero no `SearchPause`, `SearchPause.of` answering null
  for it as a cap outside 1 to 50 is no `SearchLimit`, so no engine is built over one; a query the engine
  starts with owes none; `setConfined`, a
  `configure` changing the language, the cap, the branches, whether there is a transport or the
  units' language or length system, and `retry` each ask a fresh round. A round sends one request
  per branch of its configuration's targets, each in a coroutine of its own launched in their order
  under the round's, every flag stated and the branch's alone true; a branch listed twice is asked
  once and one the build cannot enable none. A round a later one replaced is abandoned: its
  coroutine is cancelled, so a request out is withdrawn, and nothing it answers or throws lands, a
  round abandoned before its coroutine ran sending nothing. The pauses are waited out with `delay` on
  the caller's dispatcher, which a test's virtual time drives, so the engine takes no clock, a refresh
  pause below zero being none. The camera is stated as it rests, `setCamera`, a `MapViewport`; while the
  list is open, the round confined and a query held, a rest asks the round again after 350 ms, its
  rows kept in place under a busy flag and never broadened. `isSettled` is "a round counted and
  none in flight", the snapshot's own word where `sdk-typescript` counts rounds (C37). The engine
  starts unconfined, as the contract's engine does (Q21). A cap is a `SearchLimit`, a value class
  `SearchLimit.of` builds and answers null for outside 1 to 50, so no engine holds a cap the API
  refuses. A failed section holds what the transport threw, or a `SearchBranchException` where the
  Data API answered the branch as failed; only a thrown failure reaches the configuration's
  `onError`. A snapshot, a section and a section's state are equal where they hold the same, a
  failure being the very same throwable, so the state flow reports only a snapshot that differs.
  `close` cancels the engine's pauses and its round, and lands nothing more.
- **A round is ranked around the camera's centre** (`geo.search.focus`): the internal `searchFocus`
  brings its longitude within [-180, 180], one within it kept as written, and its latitude within the
  poles, and answers null for a centre that names no point, which then confines nothing either; a
  zoom that is no number confines nothing.
- **Whether a search box shows its list is Geo's.** `isSearchListShown(hasFocus, query, isDismissed)`, as `sdk-dart`'s
  Geo names it, answers true while the focus stands anywhere in the search control, the box, its filter chips or the
  list, the query trimmed by `trimWhiteSpace` is not empty and the reader has not dismissed the list; `SearchControl`
  reads it, so the rule a second language needs is Geo's and tested there, never a widget's (§6).
- **A result list's state is Geo's** (`geo.search.list-state`): `searchListState`, TypeScript's
  name, since a contract and a rule of one name sit in two directories here (K126), published since
  `SearchControl` reads it, answers the rows under a filter, each failed section a line of its own,
  whether nothing was found, whether the fall back is noted, and a `SearchListStatus` named by the
  key of its word in the `search` section, `ResultCount` counted through `countedText`. A result's
  second line is `describePlace`, `describePoi` and `describeWay` (`geo.search.describe`),
  published, in English titles and metric figures where no wording is handed, every figure rounded
  half away from zero on its decimal value. `dataLanguageFor` reads a tag into the language the Data
  API answers in, the map's label language in capitals.
- **Picking a result frames it, then marks it** (`geo.search.pick`): `pickTarget`, published since
  the control reads it, answers a `SearchPick`, a `SearchPickCamera` framing an extent as written or
  flying to a point at `POINT_ZOOM`, 13, and the `SearchHighlight` the map's search highlight is
  handed, null where a result marks nothing, which takes the mark before down. A point of interest
  is marked with its category, null where it states none.
- **A profile is read off an `ElevationSeries`**, which `buildElevationSeries` projects any
  geometry onto, the profile rules' one input. Its thinning slot is computed in whole numbers, so
  a slot exactly halfway between two samples keeps the later as the contract says, where a stride
  in floating point can read a half as just under it. Climbs are read on `RouteProfilePoint`
  samples in kilometers, since the contract's windows are differences of kilometers: 1.2 − 0.9 is
  a hair under 0.3, and a climb's foot is where that puts it. The itinerary's own records are the
  wire's: `profileCharacteristics`, `committedPace`, `buildInfoBands` and `applySegmentGroups`
  take the REST API's `ItinerarySegment` list, and a place's summary its `ItineraryWaypoint`, so a
  consumer passes what `getItinerary` answered; the summary's words are the caller's
  `WaypointSummaryWords` and a cut-off's spelling the caller's formatter. A collapsed group is a
  segment of the wire's type, its id `group:` then the group's, with no description, no steps and
  its five analysis lists empty where no member states an entry, and its road access ranges null
  where no member states them, the wire's "never analysed", and `[]` once one member states `[]`. A
  restricted stretch is shown as one `RoadAccessKind`, a closed `enum class` whose entries carry the
  contract's ids most severe first, and `roadAccessKindOf` reads it off the access values either
  surface answers, one overload for REST's `RoadAccessRestrictionType` and one for the Data API's,
  each value's ASCII capitals folded and nothing else, through the published table
  `roadAccessValueKinds`, keyed by REST's spelling, a value it does not name read as `Restricted`
  (`geo.data.road-access-kinds`). A web address is accepted by
  `isWebAddress`, as written, and linked by `linkedWebAddress`, which answers the address read
  past the white space at its ends or null; that white space, and the white space a pace clock is
  read past, is one set written out, the one ECMAScript's trim removes, never a platform's notion
  of it.
- **A profile is drawn in a 100-unit square, and every figure in it is a rule's** (`geo.profile.plot-scale`,
  `domain`, `chart-path`, `curve-stops`, `stretch-at`, `slope-stretches`, `grade-at`, `scrub-grid`,
  `window-clamp`, `curve-placement`, `axis-label-culling`, `window-spans`). `profileX` and `profileY` place a
  distance and an elevation, `PROFILE_VIEW_BOX`, `PROFILE_TOP_INSET`, `PROFILE_BOTTOM_INSET` and
  `PROFILE_BASELINE` published as `const val`s; `profileDomain`, `anchoredDomain`, `extentDomain` and
  `domainTicks` frame the elevation; `profilePolyline` and `profileAreaPath` answer values, `PlotPoint`s and
  `PlotCommand`s (`Move`, `Line`, `Close`), never path text, rounded to a thousandth a half up; `curveStops`,
  `stretchAt`, `slopeStretches` and `gradeAt` colour and read the curve; `scrubGrid` and `scrubDistance` read a
  pointer through `PROFILE_SCRUB_COLUMNS`, 240; and `clampRange` holds a window to the itinerary and to
  `MIN_RANGE_SPAN_METERS`, which `geo/elevation-totals.json`'s `minRangeSpanMeters` is held to as well. A
  stretch is any `ProfileSpan<Self>`: its two ends and `spanning(from, to)`, which answers the stretch moved, so
  a generic rule (`stretchAt`, `placeSpans`, `rowAt`) answers a stretch of the caller's own type with its
  members kept, and every value stays immutable, where `sdk-swift`'s protocol sets its two ends. A place is a
  `ProfilePlace`, at a position or at a distance, pinned on the curve, its name dividing the itinerary under
  the axis and its cut-off, a `kotlin.time.Instant`, marked (`placeOnCurve`), a place the itinerary passes
  twice placed twice; crowded names between the first and the last are left out (`cullCrowdedLabels`).
- **The strip, the card at the cursor and the chips read the itinerary's figures** (`geo.profile.stat-strip`,
  `cursor-readout`, `section-chips`, `amenity-stacks`). `resolveCharacteristics` folds the server's figures
  over the ones the geometry gives, figure by figure, into `ResolvedCharacteristics`, each a `Double?`;
  `statStripCells` states them, or the cursor's distance, altitude and grade; `profileReadout` states what the
  cursor stands on and the stretch it stands in; `sectionChipCells` a leg's length, climb, loss and grade; and
  `amenityStacks` the services above a pin, four shown and the rest counted. A strip's cell, and a table's, is a
  sealed interface with a class per figure or column, so a composable writes each by its type.
- **The two tables' rows are the itinerary's legs and its places' passes** (`geo.profile.segment-rows`,
  `waypoint-rows`, `segment-table-cells`, `waypoint-table-cells`, `row-match`, `line-between`).
  `itinerarySegmentRows` spans each leg on the distance axis, its figures `stated[leg.id]` over the leg's own
  over the cumulative scan, the running totals following a stated running figure; `itineraryWaypointRows`
  states each pass of a place, the figures a place states applying on its primary pass alone. A cell is
  `segmentRowCell` and `waypointRowCell`, a band an `AltitudeRange`; a place's glyph is its own where the
  table's set holds it, else the fallback pin. A row stands for a segment id, its own or a member's
  (`rowStandsFor`), and a leg's stretch of the series is `lineBetween`, the line the chart drew.
- **A place's services are named in the reader's words and sorted in the reader's order**
  (`geo.profile.amenity-filter`). `amenityName` reads the scope's `amenities` word, else the elements' English
  one, else the value; `amenityFilter` answers the chips, each service offered, collated in the scope's locale
  through `localeCollatedBefore`, a seam's, since collation is locale data: `java.text`'s `Collator` on the jvm
  and android, Foundation's locale-aware comparison on iOS, a tie keeping the order first offered;
  `toggleAmenity` adds a service at the end or takes it out wherever listed.
- **A place's card is a model of Geo's** (`geo.profile.waypoint-card`, `geo.profile.waypoint-property-value`).
  `waypointCard` answers `WaypointCardFacts`: the headline, the services by name, the timing, the contact, the
  fields, the links and the photos, each line labelled in the scope's `waypointCard` words. A phone, an email, a
  website and a booking page are anchored only where `isWebAddress` reads a website's or a booking page's
  address, so no script is ever a link, and a photo is kept only at a web address. A place's addresses,
  `WaypointDetails.Contact.website` and `bookingUrl`, a photo's and a link's `url`, are `String`s, the text the
  wire answers kept as it was sent, as `sdk-swift`'s are, so the card states an address with no scheme, an
  internationalised host or a percent sign that encodes nothing as written, and links none of them that
  `isWebAddress` refuses. A field is `WaypointField`, its value written by `waypointPropertyValue`, a number read as
  ECMAScript's `Number(string)` reads it, never by `toDoubleOrNull` alone, and a date only through the app's
  formatter, its value read by the client's anchor, `parseTimestamp` (§4), as every timestamp the wire carries
  is read and as `sdk-swift` and `sdk-dart` read it, never by `Instant.parse`, which takes forms the anchor does
  not, so a value that names no instant by the contract's grammar reads as written; and its link
  `waypointPropertyLink`.
- **An altitude band is the language's own range** (`geo.format.altitude-range`). `formatAltitudeRange` writes
  each bound whole by the locale services and the unit once, through a separator and an approximation of Geo's
  own table, CLDR 48's for the seven `Locales` languages as ICU 78.3 writes them, value for value
  `sdk-swift`'s: Android publishes range formatting of numbers from API 31 alone, under this SDK's floor, and
  neither `java.text` nor Foundation publishes any. Two bounds written alike read approximately, and a first
  bound below zero takes a space either side of the separator. A stop of the distance axis is
  `formatDistanceTick`, its fraction digits kept even where they are zeros.
- **A place's mark and a badge's fallback are published** for the composables that draw them: `placeGlyph`,
  `badgeFallback` and `BadgeSurface`. A surface's colours are `surfaceColor` and `wayTypeColor` over
  `surfaceColors` and `wayTypeColors`, a value the open vocabulary adds painted in `unclassifiedColor`
  (`geo.profile.classification-colors`).
- **The terrain under a point is read off the elevation tiles by rules** (`geo.terrain.elevation-decode`,
  `geo.terrain.elevation-sample`, `geo.terrain.aspect-cardinal`). `decodeElevation(pixel, encoding)` reads a
  pixel packed as an `Int`, red in bits 16 to 23, green in 8 to 15 and blue in 0 to 7, as an Android `Bitmap`
  hands one, under an `ElevationEncoding`, a value class over the TileJSON's word so an encoding the contract adds
  is held and reads no height, null reading `mapbox`. `elevationTilesAt(position, source)` answers the
  `ElevationTile.Address`es of the five places an `ElevationSource` is read at, `ELEVATION_NEIGHBOUR_DEGREES`
  apart, and `terrainFromElevationTiles(position, source, tiles)` the `TerrainReading` off the `ElevationTile`s at
  hand, each its address, its edge and its pixels; reading an image's bytes into those pixels is the platform's
  own codec's, `BitmapFactory` in the renderer, so Geo takes no image dependency. `aspectCardinal(degrees)`
  names an `AspectCardinal`, each spelled by its `code`, the key of its word under `readout.aspects`.
- **What a map does with its organization is ruled by Geo** (`geo.organization.watermark`). `watermarkOf(read,
  language)` answers the `MapWatermark` an `OrganizationRead` draws, or none. `OrganizationRead` holds the five
  states `sdk/SPEC.md` §15 names in every SDK's public type for the read: `Waiting`, within one attempt's budget;
  `Answered(whiteLabel)`, the organization itself held by `MapEngine.organization`; `Failed`; `TimedOut`, one
  attempt's budget passed without an answer while further attempts may go on; and `NoKey`, a client holding no
  publishable key, the contract's `no-key`: waiting, answered, failed, timed out and no key in every SDK, as
  `sdk-dart`'s and `sdk-swift`'s `OrganizationRead` hold them. `NoKey` leads with a word the naming law reads as a null
  object's (§5), and is an exemption the root build file counts with its sentence, since it names a state of the read
  and stands in for nothing. A host reads a map's read in that type, the public
  `StateFlow` `MapEngine.organizationRead` of `mountaya-elements` (§8), as it reads `sdk-swift`'s
  `MapEngine.organizationRead` and `sdk-dart`'s `MapState.organizationRead`. No parameter of the rule, nor of any element or
  engine, hides the mark. The mark's frame is published for a host's own interface under the four names every SDK
  gives it at the top level of its geo module: `watermarkWidth`, 180, `watermarkHeight`, 180 × 58 / 138,
  `watermarkInset`, -4, each from the top-left corner of the map's safe area, and `watermarkClearance`, the inset and
  the height, how far below the top of the map's safe area the frame ends, which Trail's search box starts below and
  its sheet stops below, top-level `val`s read off `ElementLayout` and the glyph table, so lowerCamelCase by the naming
  law (§5); no registry column holds them. `isWatermarkKeptClear(read)` answers whether nothing of a map's own may lay
  over that frame as an `OrganizationRead` stands: held while the read waits and in every state that draws the mark,
  `Answered` without white label, `Failed`, `TimedOut` and `NoKey`, and released only by `Answered(whiteLabel = true)`,
  as `sdk-dart`'s Geo publishes it, its two arguments, the state and the organization's `white_label`, one here, where
  `Answered` carries the flag. A host reads it off `MapEngine.organizationRead` to keep its own views clear of the frame,
  and the elements hold their own interface to it (`keptClearFrame`). The
  rule reads the drawing off the module's own table, never off `GlyphDrawing.artwork`, which publishes a view that
  refuses every change, so nothing a host does to the published table takes the mark away. The rule takes the
  browser document a map is drawn in too, an internal overload `watermarkOf(read, language, page)` over an internal
  `WatermarkPage`, its origin and whether it is top-level, as `sdk-swift`'s `package` rule takes it: a top-level page
  on one of Mountaya's own origins omits the mark, and a framed one keeps it. A Kotlin map is drawn in no browser
  document, so `MapView` reaches the published overload alone, which hands none, and only the conformance runner
  hands a page, so every case of the fixture runs the rule. White label is the one thing a map reads off its
  organization: no control, unit, language, base map or colour is read from it (`sdk/SPEC.md` §7).
- **A readout and a panel are laid out by rules** (`geo.readout.rows`, `geo.panels.layout`). `readoutRows(point,
  wording)` answers the `ReadoutRow`s a `ReadoutPoint` states, each naming the path of its word and holding its
  values, which a composable writes through the formatters and `filledText(template, values)`, the overload that
  fills each placeholder by its name; `READOUT_DETAIL_ZOOM` is the zoom from which a readout reads its terrain.
  `panelLayoutOf(mapWidth, mapHeight, topInset)` answers the `PanelLayout` a panel the controls open takes over a
  map, from the printed `ElementLayout`. `formatHumidity` and `formatSnowfall` write a band's humidity and depth
  of snow (`geo.profile.unit-formatting`).

### Engines and seams

**The headless state is the promise.** Every composable `mountaya-elements` publishes renders
state an engine holds, Geo's for navigation and `mountaya-elements`' own `MapEngine` for a map, so a
consumer who wants a different screen takes the same engine and draws their own. `MapEngine` lives
in `mountaya-elements` rather than Geo because it takes the app's `Mountaya`, which Geo never names,
and applies the `map` rules, which are the binding's; it names no MapLibre or Compose type, and
builds on every target.

- **An engine exposes a `StateFlow` and runs on the caller's scope.** `StateFlow` is common code,
  so Geo names no UI framework and the same engine serves Compose, a server and a Kotlin
  Multiplatform app's iOS side. Not `LiveData`, which is Android. An engine that launches nothing,
  as the scripted `NavigationEngine` launches nothing, takes no scope at all.
- **An engine is driven from one thread**, the one its commands are called on, as Compose's state
  and a `ViewModel`'s are, and each command has its effect before it returns. Common Kotlin has no
  lock a library can take without a dependency, and an engine that queued its commands would leave
  its state behind the command that moved it; a host whose positions arrive on another thread hands
  each one over first.
- **What makes a thing an engine is state, not size.** A rule is a pure function however large; an
  engine holds something across time, a query in flight or a session in progress, however small.
- **An engine closes as a resource.** Every published engine with a `close()` is an `AutoCloseable`,
  `NavigationEngine` and `MapEngine` as much as the geolocate, itinerary, layers selection and search
  engines, so a host's `use` or a disposal that closes every resource it holds closes each alike, and
  a second close does nothing more.
- **Data arrives through the narrowest seam**: a functional interface the app wires to its client
  in one line, `SearchFetch`, `ItinerarySource` or a route source, or plain data. Geo never names
  `Mountaya`, so an engine that needs Mountaya data takes a function, which is what the TypeScript
  SDK's `SearchFetch`, `SegmentRouteFn` and `TerrainSampler` are.
- **Nothing below the app constructs a client.** The map takes the app's `Mountaya` because it
  needs the tile specifications and the credentials to fetch tiles with; the TypeScript `MapView`
  takes its client the same way.
- **A seam is an interface with an implementation that reads the platform and an `Inert` one**
  that does nothing and is still total, so a host can decline it.
  `InertLocationSource` is a location that never moves, and `InertNavigationVoice` a voice that
  says nothing.

### Navigation

**The rules are contract, and they are written in `schemas` before the engine is.** Step
normalisation (`geo.navigation.steps`), the stepless route (`geo.navigation.stepless-route`),
instruction keys (`geo.navigation.instruction-keys`), the progress numbers a user reads
(`geo.navigation.progress`), the off-route distance (`geo.navigation.off-route`), the status set
(`geo.navigation.status`), one route request in flight (`geo.navigation.route-request`), the prompts
an engine that speaks announces a route with and the one due (`geo.navigation.prompts`), and how an
engine walks a route, its progress, its arrival and its new routes (`geo.navigation.engine-walk`), are
each a capability `sdk-swift` and `sdk-dart` answer identically, confirmed in `sdk/SPEC.md` §14,
so each is named here and none is restated: a rule restated in an SDK's spec is the rule the next
SDK reimplements from prose. Contract v0.9.0 carries them, and Geo's rules, vocabulary and engine
contract are written against it.

**A route is read into one vocabulary before any rule reads it.** `readDirectionsSteps` reads a
Data API route's steps and `readItineraryDirectionsSteps` a REST segment's into `NavigationStep`,
whose maneuver is REST's generated `ManeuverType`, an `unknown` one where its surface spells none
this build knows; a figure a Data API step leaves unselected is null, and a step that states no
range refuses its list, since it indexes no vertex. `followedRouteOf` answers the `FollowedRoute`
a `NavigationRoute` is followed as, measured once, a stepless route's departure reading its length off that
measure rather than measuring the line again (`route-measure`, §7), its measure published as `measured` so a guidance
plan or a host's own engine reads a step's length without measuring the line again, where `sdk-swift`
and `sdk-dart` keep it internal and measure it a second time; and `navigationProgressAt` reads a
position on it: the foot is placed by the corridor's own placement of a segment and measured along
the route's `cumulativeMeters`, so a traveller is off a route by the distance their progress is read
at.

**A saved itinerary is one route over all its segments** (`geo.navigation.itinerary-route`).
`readItineraryRoute(segments, waypoints)` takes the REST segments and waypoints as the API answers them
and joins the segments in chain order, a stable order by `position`, `sortedBy`, two at one position
keeping their listed order, and so the waypoints: each segment holding a line, its LineString or the
first its GeometryCollection holds, is followed as `followedRouteOf` follows it, the lines are laid end to
end, a joint on the very same longitude and latitude kept once as the earlier line's vertex, and a line
starting anywhere else reaching back across the gap by its first step; every step is moved on by the
index its line starts at, and the duration is the segments' summed, none where one holding a line states
none. Each arrival but the last is said as a waypoint passed on the way, `waypointManeuver`, a maneuver
no surface writes, so a surface spelling it reads `unknown`: it keeps its range and figures and is named
by the nearest itinerary waypoint within `WAYPOINT_PROXIMITY_METERS` of where its segment ends, the first
in chain order of two as near, and a departure naming no way right after it is folded into it, its
distance and duration summed. An itinerary holding no line is no route.

**A position is read in one walk of the route's line.** The machine reads each fix's progress and its
distance to the route, which decides whether the traveller is off it, in one pass over the line the
route holds unboxed, every stretch placed around the fix once by the one placement both rules stand
on, so the pass answers `navigationProgressAt`'s and `metersToRoute`'s own numbers, which a test holds
on routes that loop, double back and cross the antimeridian. The pass is the whole line, every fix,
with no window opened around the last progress: the distance is a minimum over every stretch, and a
stretch far along a route that loops back can pass as near the traveller as one beside their last
progress, nearer even, so no window bounded by the progress returns the full walk's answer for every
route, and the progress already reads only the stretches that end at or past the last one.

**An instruction is a key and its arguments, never a sentence.** `stepInstruction` resolves a step
to a `NavigationInstruction`: a `NavigationInstructionKey`, the closed set of the contract's 34
keys, the waypoint passed on the way's `Waypoint` and `WaypointPlain` among them, each with the
arguments it takes in order, and the way name or exit it takes. The words are
the host's, or a composable's own `strings`, so the same step reads alike in every SDK and in every
language a host ships, and a `when` over the key is exhaustive.

| Piece | Lives in | Because |
| --- | --- | --- |
| `NavigationState`, `NavigationStatus`, `NavigationError`, instructions and progress as Mountaya values | `mountaya-geo` | a view renders them, and a view cannot depend on the device's engine |
| the pure rules | `mountaya-geo`, in `commonMain` | fixtures reach them on every target |
| the `NavigationEngine` interface | `mountaya-geo` | an app codes against it without linking the device's engine, and a Kotlin Multiplatform app can implement it on iOS over `sdk-swift`'s engine |
| a scripted `NavigationEngine`, whose route requests a test answers | `mountaya-geo`, under `testing` | an app's unit tests answer each route request themselves, on every target, with no coroutine and no clock |
| the plan an engine that speaks announces a route with and the prompt due, `guidancePlanOf` and `duePrompt`, with `PROMPT_METERS` and `PROMPT_SLACK_METERS` | `mountaya-geo`, in `commonMain` | a fixture proves them on every target, and an engine an app writes that speaks lays out the plan `DeviceNavigationEngine` does |
| the device's engine, Geo's machine with its guidance and the device's seams | `mountaya-navigation` | its seams read the Android platform's location services and speech |

**The engine contract is one machine over the rules.** `NavigationEngine` is a
`StateFlow<NavigationState>` and five commands, `start`, `reroute`, `setPosition`, `stop` and
`close`, the surface of the first SDK's engine in Kotlin's idiom. Every engine is the same machine,
`RouteFollowingEngine`, published deliberately and small (§2): the route requests, the status, the
progress and the arrival are decided by the rules, and an engine differs only in how it sends a
request. The scripted engine hands each to a test, which answers it with a route or a failure, so
the states it reports are a navigation's, with no route source, no coroutine and no clock. How an
engine walks a route is the contract's, every SDK's engine answering alike (`geo.navigation.engine-walk`):

- **The arrival** is raised where a position read on the route leaves its progress at the end, so
  a traveller who stops short never arrives and one past the end within the threshold has.
- **Progress is read on every position while a route is held**, off the route and while a new one
  is awaited included, so a far-off position can push it ahead for good.
- **A new route is asked for by `reroute` alone**, which the status table takes only off the route
  and never while a new route is awaited; nothing asks on its own, and a reroute that fails is not
  retried.
- **What the state reports**: a state only where it differs from the one held, one a command at
  most but for a route that lands while a position is held, which is reported and then read, and
  nothing after `close`. A position that names no place is refused with `IllegalArgumentException`;
  a stop forgets the route and keeps the last position, read against the next route that lands.
- **A failure** is `NavigationError.RouteFailed`, carrying what the route source threw, or
  `NavigationError.EmptyRoute` for a route whose line holds no vertex; the class is abstract rather
  than sealed, as the client's exception ladder is, so a kind a later release adds breaks no host. A
  failure is identified where the engine records it, as `sdk-swift`'s is: each is equal to itself
  alone, so two recorded apart are two whatever their sources threw, the same throwable included, an
  empty route being a class built for each, and a state holding one never equals a state holding
  another.

**The machine is published, and is an engine of its own.** `RouteFollowingEngine(scope, route)` asks
a `NavigationRouteSource`, the host's `suspend` call, for each route, one request in flight, each in
a coroutine of its own in the host's scope that a stop or a close cancels, so the host's call ends
with it and its answer is dropped; a cancellation the source throws while it is still asked, a
timeout of its own, is its failure, so a navigation never waits for good, and so is an `Error` it
throws, a `TODO()` among them, which ends neither the host's scope nor the request's turn. The
abandoned request's own cancellation is the one throwable never read as a failure. The answer is
acted on where the scope's dispatcher resumes the coroutine, the main thread for `viewModelScope`
and `lifecycleScope`, and at once where the dispatcher runs a coroutine where it is launched and the
source needs no wait. It follows a route on every target Geo builds for, with the positions a host
hands it, which is how a jvm app, a Kotlin Multiplatform app's shared code, or an app with its own
location stack follows one, and the engine in `mountaya-navigation` is it with the device's seams
and its guidance beside it. It is `sdk-typescript`'s `NavigationEngine({ route })` and `sdk-swift`'s
`RouteFollowingEngine(route:onChange:)`, the route source the same seam.

**An engine built over the machine is told each state inline.** `RouteFollowingEngine(scope, route,
onChange)` tells `onChange` each state it commits, synchronously, in the order committed, before its
`StateFlow` holds it: a collector on a dispatcher that runs it where the flow is set, the main
thread's immediate one among them, would otherwise be handed a route that lands before the engine
built over the machine had kept anything of its own to it. A command `onChange` makes, or a collector
makes as it is handed a state, has its effect at once and its state is handed on after the one being
handed on, to `onChange` and then to the flow, so both see every state in the order committed; a
collector started while a state is handed on starts at the state the flow holds. A close made there
reports nothing more, the state being handed on included, and commits nothing more, so `onChange` is
never told a state after a close, the reading of a position held on a route that lands among them.
A `start` or a `reroute` asks for its route before it commits the state that asks for it, as
`sdk-dart` orders the two, and the request leaves once that state was handed on: a stop or a close
made there abandons the request before it leaves, so the host's source is never asked, and a request
that never left is abandoned to no one. Leaving after the state, rather than before it as `sdk-dart`'s
does, keeps a source that answers at once on an immediate dispatcher answering a `routing` already
committed. What `onChange` throws passes through the command that committed the state, the request it
asked for still leaves, and the flow still reaches the machine's state. The two-parameter
constructor stays as it was, so `RouteFollowingEngine(scope) { request -> route }` still hands its
trailing lambda to the route source: Kotlin has no forward scan of trailing lambdas, and a
three-parameter constructor with a defaulted observer would take it as the observer. The flow itself
promises what a `StateFlow` promises, the newest state and each one after it, conflated for a
collector that runs late; `sdk-swift`'s `subscribe()` hands each reader every state.

**The device's engine is `DeviceNavigationEngine`**: Geo's machine, `RouteFollowingEngine`, with the
device beside it and the guidance that says each maneuver, all of it Kotlin in this repository and
none of it a navigation library's, as `sdk-swift`'s and `sdk-dart`'s engines of the same name are. It
takes its seams at construction:

- **A scope**, the host's, `viewModelScope` or `lifecycleScope`, whose dispatcher is the main
  thread the engine is driven from, where each route request runs and its answer lands.
- **A route source**, `NavigationRouteSource<Request>`: a `suspend` function the host writes over
  `Mountaya.data.directions`, which is where the Data API's session token comes from, answering the
  `NavigationRoute` to follow, its steps read by `readDirectionsSteps` or
  `readItineraryDirectionsSteps`. The other routes of `DirectionsResult.routes` are the
  alternatives, which a host that offers one on a reroute keeps itself.
- **A location source**, `LocationSource`, started by the first `start`, before the route is asked
  for, and stopped by `stop` and `close`, each position it states read as `setPosition` reads one, a
  refused one thrown back to the source: `DeviceLocationSource`, the platform's;
  `ReplayLocationSource`, which walks each route followed at a speed the host names, stating the
  route's start at once as it is told of it, each position read off the measure the route holds,
  `positionAtMeters(route, meters)`, so a walk measures the line no more than its following did;
  `FlowLocationSource`, over the app's own `Flow` of
  positions, collected in the host's scope from each start to the stop, `sdk-dart`'s
  `LocationSource.fromStream` and `sdk-swift`'s `StreamLocationSource`, a position the engine refuses
  told to its `onError` and the next one read, and a failure of the flow, which ends it, told there
  too, nothing reaching the host's scope, so a navigation never takes the app's screen down with it;
  `InertLocationSource`, the default, which states nothing; or the app's own. A source is told of
  each route followed, `follow`, once the engine's guidance follows it, which a replay walks and the
  others ignore.
- **A voice**, `NavigationVoice`, handed each maneuver to say: `SpeechNavigationVoice`, the
  platform's text-to-speech in the host's words and language, or in its fallback language where the
  device holds no voice for the first; `InertNavigationVoice`, the default, which says nothing; or the
  app's own. An instruction is a key and its arguments, so a voice is where the host's strings turn it
  into words.
- **No clock of its own.** Nothing the engine decides reads the time: a position is judged by where
  it is. The one seam that waits, the replay, takes a scope of its own, whose time paces
  its wakes, and a time source it reads the time walked from, the device's monotonic clock by default
  and a test's scheduler's under `runTest`, `testScheduler.timeSource`: each position is placed by
  the time elapsed since the walk began, as `sdk-dart` and `sdk-swift` place it, so a main thread too
  busy to wake the walk on time delays a position and never the walk.

The engine holds what it is given and closes none of it: a seam the app made, a voice holding the
platform's speech service among them, is the app's to close.

**How the engine follows a route.** `DeviceNavigationEngine` holds the machine and is its
`onChange`, told each state the machine commits inline, before the flow holds it, so it stands on the
machine's public API alone and no collector, on any dispatcher, sees a route land that the guidance
does not follow yet. At each state, the guidance, `GuidanceEngine`, internal, keeps to the route the
machine follows, laid out afresh for each route that lands, and the location source is told of that
route as the guidance takes it up, so a position a source states the moment it is told is read on that
route; then the prompt due at that state is chosen from its own progress, `duePrompt`, so a fix the
machine moves nothing for says no prompt, and a prompt is said once per route and only while the
status is `navigating`. A route that lands while a position is held is read at that position next,
and is guided there, so the route unread says no prompt. A reading a command makes while the machine
hands on another state, a source stating a position as it is told of a route or a collector
commanding the engine, is told after it and guided then. A new route, a stop and a close hush the
voice; the state a stop commits is kept to by the stop itself, which stops the location source, hushes
the voice once and tells the source it follows no route. The location source starts before the
machine asks for the first route, so a route that lands within `start`, from a source that answers
without suspending on a dispatcher that runs a coroutine where it is launched, finds the source started,
and a replay states the route's start as it is told of the route, before it returns, whatever its
dispatcher: the machine keeps the last position across a stop, and the route is read where the new walk
begins, never at the last walk's arrival, which would hold a navigation started again on the same route
in `arrived` with nothing said, as `sdk-swift`'s and `sdk-dart`'s replays state the start at once too.
The plan is Geo's, `guidancePlanOf`, published beside the machine so an engine an app writes that
speaks lays out the same one: each step's length read off the route's published `measured` rather than measuring
the line again, and its prompts, each maneuver announced from the step that leads to it
`PROMPT_METERS`, 50 m, before its end, or as the step begins where it is shorter, the departure as the
first step begins, and nothing from the last step. A step of no length covers no ground, so Geo's
progress never stands in it where its line names a place at every vertex, and its prompts would never
fall due: it keeps none, even where Geo's progress names it, as it does a step whose vertices after the
first name no place, and they move, as `sdk-swift`'s and `sdk-dart`'s move them, the three plans laid
out alike. They move to the nearest earlier step of some length, after that step's own prompts and any
moved there before them, each due as that step ends, a trigger of 0, at the vertex the maneuvers share;
where steps of no length open the route, their departure covers no ground and is dropped, and what they
announce moves to the first step of some length, before its own prompts in travel order, each due as it
begins, a trigger of its length, so as the walk begins the last maneuver of the opening vertex is said
and the departure is not; and where no step has any length, a route of one vertex, each prompt stays on
its step. The prompts are numbered from 0 in travel order once moved and dropped. Since `duePrompt`
lets a step's last prompt due win and each prompt is said once, two maneuvers on one vertex are said in
order, the first 50 m before it and the second at it, and of three or more the first and the last are
said and those between are not. The plan and the prompt due are common code, which Geo's own tests
and `geo.navigation.prompts` run on every target; the engine is host code, tested on the host JVM: a
route followed fix by fix says each prompt once, at the metre `duePrompt` picks.

**How a prompt is chosen.** At each state the machine commits, `duePrompt` reads the step its
progress names, held within the plan, and the first before any position, where the traveller stands
at the route's start; the distance it reads is what is left to that step's end, the progress's
`toManeuver`, measured from Geo's foot on the route, which never moves back and is found on every
stretch ahead, and the whole step before any position. A prompt is due where that distance less its
trigger is `PROMPT_SLACK_METERS`, 5 m, or less, and of the step's prompts due, the last wins: the
farther a prompt is from its step's end, the earlier it stands, so the one nearest the maneuver is
the one standing. That is the rule `geo.navigation.prompts` proves, read over the distance every SDK
reads alike, `sdk-dart`'s and `sdk-swift`'s `duePrompt` too, so all three SDKs say a maneuver at the
same metre of the route.

**The engine's rules:**

- **Each command has its effect before it returns**, and only the host asks for a route: the engine
  reads no clock, reroutes on no timer and holds no lock.
- **Every number a user reads comes from Geo**, so the Kotlin app, the Swift app and the Flutter app
  show the same distance remaining for the same route.
- **The step is Mountaya's.** The step a prompt belongs to is the one Geo's progress reads the
  traveller in (`geo.navigation.progress`), so the maneuver a voice announces is the one the screen
  shows.
- **The off-route decision is Mountaya's.** The machine reads it, `isOffRoute` of the distance its
  walk of the line measures (`geo.navigation.off-route`), and the guidance says nothing while the
  status is other than `navigating`.
- **A prompt is Mountaya's too**, chosen from Geo's progress by the trigger rule (above), its
  instruction handed to the voice, and a fix is read as the contract's rules read one, a position
  with no accuracy, course or speed.
- **A failure is Mountaya's.** `failed` carries a `NavigationError`, the machine's.
- **Permission prompts and background execution are the host's.** The engine never requests
  location permission and never starts a foreground service: both are app-level decisions, with
  manifest declarations and Play policy consequences only the host can own. A location source is
  handed a permission the host already holds, which the host's own manifest declares, since
  `mountaya-navigation` declares none, and a session that must outlive its screen runs in the host's
  own foreground service. `DeviceLocationSource` is Mountaya's, over the platform's
  `LocationManager` and its satellite provider, at the rate the host chooses (below), started only while the app holds
  `ACCESS_FINE_LOCATION`: an approximate location alone places a traveller kilometres away, which no
  navigation reads. Its `standing` is a `StateFlow` of `LocationStanding`, the `LocationAuthorization`
  and the `LocationAccuracy` `sdk-dart` states, null before the first read and read from the
  permissions the app holds: as the source starts, again as each of the app's activities resumes
  while it is started, which is where a permission granted in the system's settings becomes known, as
  `sdk-dart`'s Android side reads it again, and by `recheckPermission()`, which answers it, for the
  moment between the app's own permission prompt and the next resume. A failure of the reader, a
  device with no satellite positioning, a permission revoked as the reader starts, the device's
  location turned off, or any other failure of the platform's reader, reaches the source's `onError`
  as a `LocationException` of that kind while it is started (the table below), and a reader that did
  not start is asked again at the next recheck.
- **Reading with the screen off is the host's to declare, and the source checks it.**
  `DeviceLocationSource(backgroundUpdates:)`, false by default, says the host keeps reading while the
  app is in the background, as `sdk-dart`'s and `sdk-swift`'s sources say it. The source starts no
  service and asks for no permission: since Android 10 the platform hands a backgrounded app fixes only
  where it holds `ACCESS_BACKGROUND_LOCATION` or runs a foreground service of type `location` of its
  own. So as the source starts it reads the app's merged manifest, as `sdk-dart`'s Android side reads
  it, and where the app declares neither, below Android 10 a declared `ACCESS_BACKGROUND_LOCATION`
  alone counting, as there, the request is refused: `onError` is told a `LocationException` of kind
  `BackgroundUndeclared`, and the fixes go on while the app is in use. Below API 33 the package is
  read through the one call the platform has there, which API 33 deprecates, and the build counts that
  suppression with its sentence (§7). The audio a voice needs with the screen off is iOS's to refuse, so
  `AudioUndeclared` is never raised here.
- **A failure of a device seam is one type per seam, with the same kinds in every SDK.** Each kind
  set is open, a value class over the kind's name (§5), so a `when` over it ends in `else`:

  | Seam | Kind | When, on Android | Told |
  | --- | --- | --- | --- |
  | location | `NoProvider` | the device holds no satellite positioning | as the reader is asked to start |
  | location | `PermissionRevoked` | the permission went between the check and the request, a `SecurityException` | as the reader is asked to start |
  | location | `ProviderDisabled` | the device's location is turned off | once each time it goes off |
  | location | `PlatformFailed` | any other failure of the platform's reader, carried as the `cause` | as it happens |
  | location | `BackgroundUndeclared` | `backgroundUpdates` asked for, and the merged manifest declares neither `ACCESS_BACKGROUND_LOCATION` nor a `location` foreground service | as the source starts |
  | location | `AudioUndeclared` | never: Android has no background audio mode to declare, and the set keeps it for `sdk-swift`'s and `sdk-dart`'s iOS | never |
  | speech | `EngineUnavailable` | `TextToSpeech` reports its start failed, or bound no engine | once per voice, every later sentence dropped |
  | speech | `AudioRefused` | the audio focus a sentence asks for is refused, `AUDIOFOCUS_REQUEST_FAILED`; the sentence is said anyway, over what other apps play | once per refusal |

  A failure is told before its source stops, where it stops. With no `onError` every failure is
  dropped, never thrown and never a crash, and a debuggable app, one whose `ApplicationInfo` carries
  `FLAG_DEBUGGABLE`, logs it once with `Log.w`, as `sdk-dart` prints it under `kDebugMode` and
  `sdk-swift` under `DEBUG`; a release build logs nothing.
- **The rate the device is read at is the host's choice.** `DeviceLocationSource(rate:)` takes a
  `LocationRate`. `navigation` (`Navigation` in Kotlin) is the default: every fix the platform reads
  for a navigation, on iOS at `kCLLocationAccuracyBestForNavigation` with no distance filter, and on
  Android from the GPS provider every 1,000 ms whatever the distance moved. `walking` (`Walking`)
  reads fewer fixes for a traveller on foot: on iOS at `kCLLocationAccuracyBest` with a 5 m distance
  filter, and on Android from the GPS provider every 2,000 ms once the device moved 5 m. Both rates
  read iOS's location as a fitness activity (`activityType` `.fitness`) and never let iOS pause the
  updates on its own. Why 5 m: a maneuver's prompt falls due 55 m before it, its 50 m trigger plus
  the 5 m of slack it is read with, so the fix that says it on time lands between 55 and 50 m
  before the maneuver. Android hands a fix on only once both its time and its distance have passed,
  so up to 2.5 m/s (9 km/h) the 5 m hold a fix back and two fixes stand 5 m apart; faster, the 2
  seconds do, and fixes stand farther apart than the prompt's slack, which is why the rate is a
  walker's.
- **Speech is the platform's, in the host's words.** `SpeechNavigationVoice` hands each sentence to
  the platform's `TextToSpeech`, which says it as navigation guidance, and takes the audio focus as it
  does, for the moment and letting other apps lower what they play, or pause it: it lets the focus go
  half a second after the last sentence ends, so a sentence that follows another keeps it, and at once
  where it is hushed, and another app that takes the focus from it stops what it says. Below API 26
  the focus is taken through the one call the platform has there, which API 26 deprecates, and the
  build counts that suppression with its sentence (§7). It names the voice of the host's language
  once the platform reports the speech engine started; where the device holds no voice for the
  language, the voice of its `fallbackLanguage` speaks, and where it holds none for that either, the
  engine's default, the order `sdk-dart`'s voice tries its voices in. The engine says nothing it is
  handed before it has started, so the voice keeps each sentence said before then, in order, and
  hands them on once the engine has started: the first maneuver of a route is said as the route
  lands, however soon that is. A hush drops what waits, and where the engine fails to start, nothing
  is said, now or later, and the voice tells its `onError` so, once, as a `SpeechException` of kind
  `EngineUnavailable`, rather than dropping every sentence without a word. The engine is asked to start
  as the voice is made, and `prepare()`, a `suspend fun`, waits for the platform's report, so an app
  that makes its voice up front learns before the first maneuver whether it will speak: it returns once
  the engine has started or failed to, the failure told to `onError` first, at once where the report
  came before or the voice is closed, starting nothing of its own, as `sdk-swift`'s `prepare()` and
  `sdk-dart`'s complete. A focus the platform
  refuses leaves the sentence said over what other apps play, and each refusal is told as a
  `SpeechException` of kind `AudioRefused`, outside the voice's lock, so a handler that says or hushes
  takes it afresh. It holds the speech service until the app closes it.
  From Android 11 an app targeting API 30 or later sees the platform's speech engine only through a
  `<queries>` entry for the `android.intent.action.TTS_SERVICE` intent, without which the engine never
  starts: `mountaya-navigation`'s own manifest declares it, the one entry it holds, and the build merges
  it into the app's, as `sdk-dart`'s plugin declares it; a device test reads it off the merged manifest,
  since an emulator whose speech engine is a system package every app may see starts it either way.
- **A native seam decides nothing the SDKs differ on, and the decisions it keeps are admitted here.** A
  seam's native code reads the device and hands it on; a choice it makes that a reader can hear or see is
  one every SDK makes alike, `sdk-swift`'s and `sdk-dart`'s SPECs admitting the same ones with the same
  reasons, and a choice not listed here is a defect. Four are kept:
  - **Both geolocation seams ask the platform for the precision their purpose reads**, the same in
    every SDK: a map's one fix about a hundred metres, `QUALITY_BALANCED_POWER_ACCURACY` from API 31 and the
    provider's own below it, under `ACCESS_FINE_LOCATION` alone (`map.camera.locate`, above), and a
    navigation's fixes the satellites' finest at the rate the host chooses (`LocationRate`, above). A locate
    control frames the box a fix's accuracy spans (`map.camera.locate`), which a hundred metres answers
    without waiting on a satellite fix, and a reader who turned precise location off is located nowhere
    rather than kilometres away.
  - **The voice holds the audio half a second after a sentence ends**, `FOCUS_RELEASE_MILLISECONDS`, 500
    ms, and lets it go at once where it is hushed: a maneuver's prompts come in quick succession near a
    junction, and an app whose music ducked would otherwise come back up and duck again between two
    sentences of one maneuver.
  - **The voice is hushed where another app takes the audio from it**, `AUDIOFOCUS_LOSS` and
    `AUDIOFOCUS_LOSS_TRANSIENT` on Android, an interruption on iOS: a call or another app's own speech
    takes precedence over guidance, and a sentence said over it is heard by nobody, so the voice stops
    rather than talking over the reader's call; the next prompt due is said as it falls due.
  - **The location source tells `ProviderDisabled` once each time the device's location goes off**, not
    once per fix the platform withholds, which is Android's decision alone: Android reports a provider
    disabled on every request and on every check while it stays off, and a host told the same failure each
    second would show the reader the same notice again and again; the source tells it again only after the
    location came back on. Core Location names no provider a reader switches off, so `sdk-swift`'s seams hold
    nothing in that decision's place.
- **The screen is the host's.** `mountaya-elements` renders `NavigationState`, or the host draws its
  own; no navigation library's views are linked.

### Views

- **Compose over MapLibre Native**, each composable rendering an engine's state (above).
- **MapLibre Native is the sole renderer core.** A composable that draws a map draws on MapLibre's
  own map, and no module links a platform map framework or a second map engine beside it, because
  a second renderer is a second copy of every style and layer rule the contract states once.
- **Each composable takes its own `on*` handlers, and its words from the scope around it**, so a
  host wires one composable without configuring every other, and localises one by stating a scope on
  it alone: `MountayaScope(statement, scheme) { }` nests an `ElementsScopeStatement` in the scope around its
  content (`geo.i18n.nested-scope`), carried by the composition as `LocalMountayaScope`, which Compose
  itself nests, and states the colour scheme its content is drawn in where it is handed one (below),
  each with a default, so a scope states either alone.
- **A composable's own `strings` stand over the scope's words for it alone.** The controls, the
  panels, the stack, the search box, the profile, the two tables and the card each take
  `strings: Map<String, String>? = null`, after `enabled` and before the handlers and `content`, as
  `sdk-dart`'s widgets and `sdk-swift`'s views take theirs: a word it names by its path, its section and
  its key joined by dots, `controls.zoomIn`, is the word read there by that composable and by everything
  it draws, the controls a stack holds and a rule of Geo's reading the scope for it, a card's lines among
  them, and every other word is the scope's. The composable provides the scope in force with its strings
  layered over it, `ElementsScope.withStrings` (§7), so a stack's strings reach its controls, a
  control's own stand over the stack's, and a scope nested inside keeps them only where it inherits its
  words, a language or words of its own resolving afresh (`scopeOf`). A counted word it names is a
  template whose `{count}` takes the count in the scope's locale, as `sdk-dart` fills one.
- **Looks are modifiers, the colours are the contract's tokens, and there is no design system.** The
  contract gives a design system to the studio binding, which this SDK does not have; a composable here
  builds on Compose UI, and on Foundation where it lays something out or takes a press, takes the
  host's `Modifier`, and never depends on Material. Every control and panel draws in the elements'
  tokens, `LocalMountayaTokens`, `ElementTokens.standard` unless a host provides others, each read under
  the colour scheme in force through `themeTokenValue` (`geo.style.theme-tokens`): the one the nearest
  `MountayaScope` around them states, `MountayaScope(scheme = ThemeScheme.Dark)`, light or dark whatever
  the system's, as `sdk-dart`'s `MountayaElements(scheme:)` states it, a nested scope stating none
  keeping the one around it; and where no scope states one, the configuration's night mode,
  `isSystemInDarkTheme()`, reads the dark values. A host still draws the elements dark in a light system
  by the configuration it hands its content, as Compose draws anything so. An app's own composable reads
  the scheme in force as the elements do, through `themeSchemeInForce()`, a read-only composable read, as
  `sdk-dart`'s `MountayaElements.schemeOf` reads it, so what it draws beside them takes their tokens
  under their scheme; the scope's own `LocalMountayaScheme` stays internal, since it holds null where no
  scope states one and the configuration decides. Sizes, radii,
  fonts, paddings and shadow geometry are the platform's (Q15): a control is 48 dp across, the touch
  target Android's guidelines name, where the web's is 36 pixels and iOS's 44 points; a chip's shadow is
  Android's elevation shadow in the `control-shadow` colours handed over opaque, since the platform
  multiplies a shadow colour's alpha by its theme's own.
- **The map's controls are a stack laid over it** (`sdk/SPEC.md` §7, which no fixture holds, Q22).
  `MapControls` fills the space it is laid in and sits in one corner, `MapCorner.TopRight` unless the
  app names another, read as the reader sees it whichever way the text runs, Compose's absolute
  alignment: a corner's name is physical, `TopRight` the reader's top right in a layout that runs right to left, and
  every figure the stack keeps clear by is physical too, placed with `place` and never `placeRelative`. It keeps to the
  map's safe area, its padding, 10 density-independent pixels, inside the safe area's edges in whichever corner it
  sits, so a status bar, a cutout or a navigation bar never covers it, and nothing of it lays over the watermark's
  frame (`sdk/SPEC.md` §15): a stack that would reach over the frame, widened by that padding, is moved clear of it by
  the smallest move that keeps it inside the safe area, below the frame or to its right, and below it where neither
  does, the map's edge then cutting it (`controlStackOrigin`, over `clearOfFrame`), so a stack in the top-left corner
  starts below the frame and one in the top-right corner of a map too narrow for both stands below it too. The map,
  its safe area and the frame are the map's real ones, never the stack's own box: the `MapView` drawing the map
  publishes on its engine where it laid the map out, its rect and its safe area in the window, `MapEngine.shownMap`, an
  internal `StateFlow<ShownMap?>` the view laid out last decides and a view leaving forgets, as `sdk-swift`'s
  `MapEngine.shown(at:)` records it, and the stack reads it there, its room the part of the map's safe area its space
  covers (`safeAreaIn`) and the frame measured from that safe area's corner, so a stack laid over more than the map,
  the map below a header of the app's, keeps to the map. A stack driving no map, or one whose map no view draws yet,
  keeps to the safe area of the space it is laid in, measured as the map measures its own (§8). The frame is held from
  the moment the stack is drawn, while the map's read of its organization waits too, so the stack does not move as a
  mark lands, and is released only once the read answers white label (`keptClearFrame`, over Geo's
  `isWatermarkKeptClear`); a stack driving no map holds
  it as a waiting read does. The stack is measured before it is placed, in one `Layout`, and hands its panels the map,
  the safe area it keeps to and the frame, `LocalMountayaMapBounds`. It
  reads from the top: `ZoomControl`; the layers, points-of-interest and preferences
  toggles as one group; the app's own controls, its `content`; `GeolocateControl`; `CompassControl`.
  Each flag is a plain `Boolean` with the contract's fixed default (`sdk/SPEC.md` §15), `zoom: Boolean = true`:
  `zoom`, `compass`, `layers` and `preferences` true, and `geolocate`, `pois` and the layers panel's `offersTime`
  false, so a flag the app leaves out takes its default and one it states, true or false, stands. The stack reads
  nothing of the map's organization: its own preferences panel starts at the SDK's default units,
  `UnitPreferences(language)`, in the scope's language, and its words and its `accent` are the scope's and the
  tokens' around it. A control
  drives the map only where a map or a handler stands
  in for its default: each takes the map, null included, and a handler of the app's in its place,
  `onZoomInClick` and `onZoomOutClick` half by half, `onClick` for the compass and the locate control, a
  handler alone driving where it stands in; one with neither is inert. An action control,
  `DownloadControl` and `DirectionsControl`, drives nothing but its required `onClick`. A disabled flag
  is Compose's own `enabled`, and the stack's shuts every control under it, the app's own composed of
  this SDK's controls among them, drawn in `control-fg-disabled`, with no look of the platform's laid
  over a face pressed or held. Zoom in is disabled at the window's ceiling and zoom out at its floor
  whoever answers the press (`zoomControlsDisabled`). A camera move a control starts, a zoom step, a
  reset of north, a locate and a search's pick, its fit or its flight, jumps where the reader turned
  animations off, Android's animator duration scale at 0, and eases or flies otherwise, landing where
  it would have: the controls read the scale as Compose reads it for the composition's own
  animations, the `MotionDurationScale` its effects carry, as they are composed, so the composition
  follows the setting and composes again as it changes, as `sdk-dart` reads
  `MediaQuery.disableAnimationsOf`. The engine's own `zoomIn`, `zoomOut`, `resetNorth` and `locate`
  keep their eases for an app's own calls. Each control is named to accessibility services
  by its word and reads out what a press does as its click's label, the blocked wording included.
- **A control the keyboard focused draws a ring** (`sdk/SPEC.md` §7, `geo.style.theme-tokens`). Every
  control, panel row, table row, chip, pin, search row, filter, toggle, clear and retry, and the search
  field, draws a ring 2 density-independent pixels wide in the `focus-ring` token under the scheme in
  force, inside its edge, while it holds the keyboard's focus, and none where a touch moved the focus to
  it: Compose's input mode tells the two apart, as the web's `:focus-visible` does, so a reader who
  taps sees no ring and one who moves with a keyboard sees where they stand. The ring is a colour of
  its own rather than the accent, so a consumer's accent leaves it standing at least 3 to 1 on the
  panel in both schemes, as WCAG 1.4.11 asks of a focus indicator.
- **One panel is open at a time.** `PanelControl` opens a panel of its own beside its toggle, toward
  the inside of the map its stack lies over, to the toggle's left where the toggle stands in the map's right half
  and to its right otherwise, top-aligned with it and held inside the map's safe area, and where a panel would reach
  over the watermark's frame it opens below it, offered only the height under it, and with its top at the frame's
  bottom where the safe area holds no room there, cut by the map's edge (`BesideAnchor`, over `panelLeft`, `panelRoom`
  and `panelTop`), so a phone's panel, filling the narrow map, never covers the mark (`sdk/SPEC.md` §15). A panel kept
  clear of the frame moves below it, never to its right. A toggle in no stack knows of no map: it keeps its panel inside
  its window's safe area, measured as the map measures its own, and clear of a frame at that safe area's top left held
  as a waiting read holds it, the panel laid out at its own width. A panel opens as a focusable popup, so a press outside it or the back gesture closes it,
  as the platform's own popups close, and a toggle in a stack opens its panel in place of any other,
  the stack's own and the app's alike, through the stack's open panel, a `MutableState<MapPanel?>` the
  app may hoist, `openPanel`. A panel control out of any stack holds its own word. The stack's layers
  toggle opens a `LayersPanel` over a `LayersSelectionEngine` the stack holds for its map, on the
  composition's own scope, configured only while the toggle stands, so a stack with no layers toggle
  reads no catalogue, offering the time and showing the pictures the stack's `offersTime` and `thumbnail` hand it; the points-of-interest toggle a `PoiPanel` of the concepts the map draws; and the
  preferences toggle a `PreferencesPanel` over `unitPreferences`, or a set the stack holds in the
  scope's language where the app hands none. A panel a stack opens is laid out as the embed lays out its
  own (`geo.panels.layout`): the toggle reads the size of the map's safe area off the stack, which fills the map, and
  its own top inset in it where it stands in the safe area's upper half, none in its lower half, where the panel rises
  beside it, and `panelLayoutOf` lays the panel `panel-width` wide, the width the safe area leaves inside the stack's
  padding on a map no wider than `panel-narrow-map-width`, and no taller than the safe area leaves it below its top
  and the room clear of the frame leaves it, a title row
  naming it by its control's word staying put while the sections under it scroll, its ground and its edge
  standing still, as `sdk-dart`'s `PanelSurface` scrolls. A layer card measures `panel-layer-card-width` by
  `panel-layer-card-height` at the panel's width, three to a row, its picture `panel-picture-height` tall, or
  `panel-picture-height-narrow` on the narrow map, and each preference is a card two to a row,
  `panel-preference-card-height` tall. A panel drawn on its own is `panel-width` wide and at most 420
  density-independent pixels high.
- **A locate control never asks for access.** `GeolocateControl` shows a `GeolocateControlEngine` on
  the composition's own scope, standing over its map as the composable is composed and as its map
  changes, drawn in the `accent` while busy, the action it reads out the blocked one, "allow it in your
  settings" (Q7), while the permission it read without prompting answers refused and only once it has
  read the map it shows. `onClick` stands in for the fix, for an app that runs its own flow, its prompt
  included, and `isLocating` then says when the app's own fix is out; `onError` is handed each failed
  fix of the control's.
- **The panels draw the pickers' models** (`geo.layers.picker-rows`, `geo.layers.poi-rows`,
  `geo.units.preference-choices`). `LayersPanel` lists its selection's catalogue in the scope's language
  and carries out a press through the selection, a locked entry's press handed to `onLockedClick` and
  selecting nothing; `PoiPanel` reads the map's concepts at the points of interest's band and shows or
  hides a row's keys, unless `onToggle`, `onToggleGroup` or `onToggleAll` stands in, its note standing
  below `minZoom` and no panel at all where the map draws no point of interest; `PreferencesPanel` holds
  no set of its own, `preferences` and `onPreferencesChange` being the controlled pair, handing the set
  back whole and relabelling the map for a language. Each row is the platform's touch target high and
  read as selected while checked. Each row of `LayersPanel` and `PoiPanel` is a composable of its own, handed
  equal values and pressed through a handler that stays as the panel is composed again, reading the panel as
  it was composed last, so a panel composed again for its host, a press on another row or its note composes
  no row it did not change (`panel-rows`, §7); `PoiPanel` derives what it offers and its note from the map's
  state, each compared with the last, so it is composed again as what it offers changes and its note as the
  camera crosses `minZoom`, never once per camera frame (`zoom-threshold`).
- **The layers panel is the map studio's** (`geo.layers.picker-rows`, `geo.layers.time-slider`). `LayersPanel`
  draws the four sections in its order, each under its heading: the terrains as picture cards, three a line,
  one base map among them; the enrichments as switches; the classic overlays as picture cards behind a
  segmented switch of the categories that hold one, drawn where two or more do, the reader's pick the
  panel's own state for as long as it lives; and, where the host offers it, `offersTime`, the time. A card
  shows the `Painter` `thumbnail` hands for its choice and its slug, and its name alone otherwise; a locked
  entry shows its lock in place of its tick or its switch. Every card, switch and segment is a composable of
  its own, reported to the render probe by its kind and label, the platform's touch target high, read as
  selected while checked, ringed while it holds the keyboard's focus. The time section is a caption,
  `layers.timeHint`, `layers.timeKeyboard` where a hardware keyboard is at hand and `layers.timeNote`, the
  readout, and a slider of Compose's foundation, since the module carries no Material: its track ticked at
  every day and labelled at four, filled in the accent from the stop of the reading to the thumb, which
  stands at the selection's instant; a tap, a drag's end, an arrow key and an accessibility service's
  adjustment each release it on a stop, which `timeSliderRelease` turns into `setInstant`, none on now. It
  reads as an adjustable value, a progress range of 216 stops whose state is the readout, the moment
  written through the platform's skeleton pattern of weekday, day, month, hour and minute in the scope's
  locale and the device's zone, then ` · `, then the offset counted in the scope's words. Its track is laid
  out afresh each time it is drawn around the clock `LocalMountayaClock` provides, the system's unless a
  host provides another, and it is held, under `layers.timeUnavailable`, taking no press and no focus, where
  that clock reads past the range a track is held to. The panel is at most 340 density-independent pixels
  wide, the studio's, so the caption wraps. `MapControls` takes `offersTime` and `thumbnail` and hands them
  to the layers panel its own toggle opens, so an app turns the time and the pictures on in one place, as
  `sdk-dart` and `sdk-swift` do.
- **The search box shows a `SearchEngine`** (`sdk/SPEC.md` §7), the app's or its own. Handed one,
  `engine`, it renders and drives it, its camera, the text, the scope and whether the list shows, never
  configures it, the engine's own `configuration` holding, so `targets`, `language`, `limit`, `wording`,
  `fetch` and `onError` are its own engine's alone, and never closes it. Handed none, it holds an engine
  of its own on the composition's own scope, made with no transport, so a composition made and dropped
  asks nothing, confined to the visible area of its map (Q21) where `SearchEngine` starts unconfined, over
  the text the app hands or none, and closed as it leaves; each composition then tells it, in its side
  effect, the question its rounds ask, its transport the app's `fetch`, else its map's client's
  `data.search`, as the map's content reads through it; with neither it is inert. Either way each
  composition tells the engine its map's camera first, then the text and the scope where the app hoists
  them and whether the list shows, and the map's camera is told again as it comes to rest between
  compositions. With no map it confines nothing and shows no toggle. Its text and its scope are
  `MutableState` the app may hoist, `query` and `isConfined`, kept in step with the engine both ways: an
  edit of the field or a press of the toggle writes the state and tells the engine at once, and a value
  the app writes is told at the next composition. Where the app hoists neither, the box shows the
  engine's own: the text it holds as typed, followed as the engine tells a new state, and its scope. The
  list shows while the focus stands anywhere in the control, the box, its filter chips or the list, which a
  `Modifier.onFocusChanged` over the whole control reads as `hasFocus`, over a query the reader has not dismissed, a
  pick dismissing it and the next keystroke or the box taking the focus again lifting that, as Geo's
  `isSearchListShown` decides, and the engine is told so, which alone lets a camera at rest ask again: the filter
  chips, the toggle, the note of a fall back, a line for each failed branch, the elements' word `search.error` beside
  `search.retry`, never the API's message, which is English whatever the reader's language and stays on the failure
  for the app to log, the rows, in
  a `LazyColumn` at most 320 density-independent pixels high that composes the rows it shows as it
  scrolls, each keyed by its id,
  each its title, the runs the query matched set bold, and its second line, an itinerary's distance and
  ascent in the control's `units` behind an ascent's arrow and a collection's count behind an
  itinerary's glyph, a bar while a round is in flight, and the empty state, every press the platform's
  touch target high; its status line is a polite live region, read out by accessibility services as it
  changes. Every word is the scope's `search` and `profile` sections, the box's own `strings` standing
  over them, its count a template where they name `search.resultCount`, and every colour a token under the
  colour scheme in force. A pick dismisses the list and leaves the box; it moves the map's camera as
  `pickTarget` answers, a fit through `fitBounds` and a flight to `POINT_ZOOM`, and hands its mark to
  `setSearchHighlight` in the accent the tokens read, null included, which takes the mark before down;
  `onSelect` stands in for the whole pick. The control takes its map's search highlight down as it
  leaves the composition or is handed another map, `DisposableEffect(map)`, as `sdk-typescript`'s does
  as it unmounts. A round's language is the scope's, `dataLanguageFor`, unless the app states one, and
  `enabled` false disables the box.
- **The data elements read an itinerary by id through the app's cache, or through the app's engine**
  (`sdk/SPEC.md` §7). `ItineraryProfile`, `ItinerarySegmentTable` and `ItineraryWaypointTable` each hold an
  `ItineraryResourceEngine` made idle in `remember`, so a composition made and dropped asks nothing, told the
  question at each composition in a side effect, and closed as it leaves; one read serves every element over
  an itinerary (`geo.data.itinerary-resource`). Each has a second overload taking the app's engine in place
  of `itineraryId`, `cache` and `surface`, so the compiler holds that one or the other is given, as
  `sdk-swift`'s second initializer does where `sdk-dart` asserts it: the element reads the engine's `state`
  and never configures or closes it, the app choosing the itinerary through `configure`, and starts over,
  its window and its cursor, as it is handed another engine. A failure is handed to `onError` once, as the
  state turns to it, and draws the empty state. What an element draws is read once per answer, the internal
  `ItineraryDrawing`: the geometry a chain the app states walks, else the itinerary's own, the series, the
  figures stated over derived, the rows, the places, and what pointing at a sample or a row writes to a map.
- **The profile is drawn where the rules place it.** `ItineraryProfile` draws the strip, then the chart, two
  layers of the 100-unit square stretched over its size (`ProfileChart`), and, as its `parts` ask, a set of
  `ProfilePart`s (`ProfilePart.standard` the amenities alone), the distance axis, the band strip, the chips and
  the navigator; the chart takes the height the rest leaves where the profile is given one, and 100
  density-independent pixels where it is not. Its curve is painted as `coloring` asks (`ProfileColoring`), the
  steepness's buckets Geo's `slopeBuckets` where `ProfileColoring.Slope` is handed none, as `sdk-dart`'s and
  `sdk-typescript`'s default theirs, its
  pins stand on the curve, a pin at the summit standing above the chart's top edge, unclipped, their services
  stacked above them, and a cut-off is drawn only where `formatCutoffAt` writes it. A touch reads the column
  under it while it is down and a hovering pointer while it hovers (Q9), the cursor standing on a sample;
  accessibility services read the chart as one adjustable value, a sample a step, and each navigator handle as
  one, a twentieth of the itinerary a step. The window is the app's `MutableState` where it hands `window`,
  the profile's own otherwise, which a new itinerary resets. `enabled` false draws the profile and drives
  nothing. The chart draws in two layers, each a `graphicsLayer` of its own, as `sdk-dart`'s draws in two
  `RepaintBoundary`s: the curve, the area and the line drawn from paths kept for its series, domain and
  window and built once for each size it is drawn at (`PlotPaths`), beside the elevation scale's lines,
  the drop lines, the cut-offs and the lit leg; and the cursor over it. The profile reads its domain, its
  placement, its classes and its window once for what each depends on, and the cursor is held where the parts
  it moves read it, never in the profile itself: the cursor's layer reads it as it is drawn, and the plot's
  reading by accessibility services, the card at the cursor and the strip stating it each as it is composed,
  so a scrub composes those again and draws the cursor's layer again, and composes neither the profile's root,
  the chart, its pins, their services, the scales, the band strip, the chips nor the navigator, the curve's
  layer recording nothing again, nor the navigator's, which draws the whole curve small on a layer of its own
  from paths built once for each size (`profile-cursor` and `scrub-root`, §7). The distance axis is 32 density-independent pixels high and the strip of chips 36, each taller
  as the reader's font scale draws its text larger, by as much as the scale grows a line of its text
  (`textGrowth`, the platform's own conversion of a size where it scales large text less), so neither
  clips its words, as `sdk-dart`'s grow with `MediaQuery.textScalerOf`; a scale under 1 leaves them as
  they are. A leg's chip is a button where the app listens, `onSegmentClick`, the segment table's word,
  handing over the id `geo.profile.row-match` names, a collapsed group's `group:<id>`: a press writes
  nothing to the map, the app deciding what it lights or where the camera goes, and a profile not
  `enabled` presses nothing, as `sdk-swift`'s `onSegmentTap:` and `sdk-dart`'s do (`sdk/SPEC.md` §7).
- **The tables and the card are the rows and the facts the rules state.** A table draws a header row of the
  scope's `table` words, then a row per leg or pass, each column an equal share of the width and each row the
  platform's touch target high, 48 density-independent pixels, lit in `table-row-hover` while pointed or
  highlighted and read by accessibility services as a selected button. A segment row is lit too where it
  stands for the leg the map's own cursor is over, read off the map's `FeatureHover` as
  `hoveredItinerary` reads an itinerary's features, a collapsed group's row standing for each of its
  members, and it pins nothing; the row the app names, `highlighted`, is lit in place of it, whatever leg
  the cursor is over (`map.profile.table-rows`). What a row pointed at writes, the marker at the sample
  nearest its leg's end or its place's pass and the leg lit, is `legTarget` and `passTarget`, written by
  the element's `MapTargetWriter`, common code a jvm runner plays. A press points at the row or lets it go
  and hands its id on (`onSegmentClick`, `onWaypointClick`); a hovering pointer points. The waypoint table's
  chips stand over its rows, each a checkbox the touch target high, their choice the app's `MutableState` where
  it hands `picked` and the table's own otherwise. `WaypointCard` draws `waypointCard`'s facts on a panel's
  surface, a line with an address a link, `LinkAnnotation.Url`, any other text, each section under its heading
  only where it states a line. Compose's foundation loads no image, so each photo is drawn by the composable
  the app hands, `photo`, and is a link to the photo where it hands none. A link carries no listener of the
  card's own, so Compose opens it through the `LocalUriHandler` in force, the platform's unless the app provides
  one around the card, which is how an app takes every link of it over, as `sdk-dart`'s `onLinkTap` and
  `sdk-swift`'s `openURL` environment do. `ItinerarySparkline` is public, a table's cell and an app's own list's.
- **A data element writes its links to the map beside it** (`map.profile.cursor-marker`,
  `map.profile.emphasis`). One internal engine, `MapLink`, is every data element's pointer and map link: what
  it points at is snapshot state the element draws by; a disabled element points at nothing new and only lets
  go, pointing where it already points tells nothing, `onHover` takes the map over, and otherwise the map is
  written the target `ItineraryDrawing` computes, through `showCursorMarker`, `hideCursorMarker` and
  `writeLegEmphasis`. A profile's cursor pins the marker at its sample and lights the leg under it, the later
  of two at a shared meter (Q17); a segment row pins at the sample nearest its leg's end and lights its
  stretch; a waypoint row pins at its pass and lights nothing. Each element pins under an id of its own,
  `PROFILE_CURSOR_MARKER_ID`, `SEGMENT_TABLE_CURSOR_MARKER_ID` or `WAYPOINT_TABLE_CURSOR_MARKER_ID`, takes down
  only what it wrote, and lets go as it leaves the composition, as `enabled` turns false and as a new itinerary
  arrives. `MapEngine` is a `CursorMarkerTarget` and a `LegEmphasisTarget` in its class header, the default
  values of `editMarker` and `setGeometry` stated on the interfaces. The cursor marker's glyph, `pin-filled`,
  stands in the control set alone, so on a map drawing the bundled marker set the marker is
  `describeMarkerPin`'s dot (C40).
- **The map's state is MapLibre's style document.** `MapState` holds the base map's style URL, the
  camera as Geo's `CameraPose`, the label language as the wire's `RestSchema.Language`, and the
  sources and layers drawn above the base map: a `MapSource` is vector, raster or elevation raster
  tiles or GeoJSON the app holds, and a `MapLayer` is a style layer, its type the Tile API's
  `TileLayerType`, its `paint` and `layout` a map of style names (`line-color`, `visibility`) to
  `JsonElement`, its filter a `JsonElement`, its zoom window a `ZoomBounds` and its rank a number.
  Both are built from a tile specification as it arrives (`MapSource(TileSchema.TileSource)`,
  `MapLayer(TileSchema.TileLayer, source)`): a source of a type the contract does not declare is
  refused before anything of its specification is drawn, a `MapException` of kind `TypeRefused`, a
  raster's tile size a specification leaves out is null and MapLibre's own, 512, as it is GL JS's, a
  raster or hillshade layer carries no source layer and no filter, and a layer carries the catalogue's
  key, `MapLayer.key`, null for the wire's empty key (`map.specification.style`). A base map is the
  style document at a URL: a terrain of the Tile API's catalogue, which `MapEngine.setTerrain(slug)`
  shows, the one of the slug named or the first, in the variant `setTerrainVariant(variant)` asked for
  last, light until dark is named, or a style of the app's own, `setStyle(style)`, under which a
  variant switches nothing; `state.terrain` and `state.terrainVariant` name it
  (`map.basemap.terrain`). The state also holds the markers shown above every
  layer, the pan lock and the zoom range the camera is held to (`maxBounds`, a `List<Double>` or
  null, and `zoomBounds`, MapLibre Native's own 0 to 25.5 where none is set), the lines an edit's
  magnet places a waypoint on (`activeSegments`), the waypoint being placed (`waypointEdit`), and
  whether a refusal of access stands (`isAccessRequired`). Every value compares as Geo's
  do, a JSON value as the serialization library compares it, as the generated types compare their
  own, and a position as a `List<Double>` compares, which is Geo's relation on every target (`NaN`
  the same as `NaN`, 0 another than -0, by the bits `Double.equals` reads), so no comparison helper
  is published for the map, where `sdk-swift` publishes `sameJson`, `sameText`, `sameNumber` and
  `sameNumbers` in Geo.
- **The engine is `MapEngine(client, style, terrainVariant, camera, labelLanguage, paint, glyphs,
  geolocation, transformRequest)`**, its state a `StateFlow<MapState>`, `paint` the `MapPaint` its
  slots and results draw in and its tile layers rank by, `MapPaint.standard` unless the app restyles a
  copy, `glyphs` the glyph set its marker sprites and its markers' pins are drawn from,
  `GlyphSet.marker` unless the app hands in its own, both fixed for the engine's life, and
  `geolocation` where `locate` asks for a fix. It launches nothing, so it takes no scope;
  `setTerrain(slug)` suspends on the client's `getTileLayers` and shows a terrain in the variant asked
  for, the first the catalogue lists unless one is named, and a slug it does not list is a
  `MapException` of kind `TerrainNotFound`, the base map left as it was. Its commands are `setStyle`,
  `setTerrain`, `setTerrainVariant`, `jump`, `ease`, `fly`, `fitBounds`, `zoomIn`, `zoomOut`,
  `resetNorth`, `easeIntoZoomRange`, `locate`, `setMaxBounds`, `setZoomBounds`, `setLabelLanguage`,
  `addSource`, `removeSource`, `addLayer`, `editLayer`, `removeLayer`, the content's (below),
  `addMarker`, `editMarker`, `removeMarker`, `clearMarkers`, `setActiveSegments`, `beginWaypointEdit`,
  `placeWaypoint`, `endWaypointEdit` and `close`, each with its effect before it returns, as every
  engine's; a command handed what a map cannot take throws a `MapException` and leaves the map as it
  was, and after `close` none throws. It answers `featuresAt`, `poisAt`, `zonesAt` and
  `itinerariesAt`, `project` and `unproject`, `elevationAt`, `loadLayers` and `credits`, and tells
  what happens on the map through `events`.
- **The engine asks what draws it through an internal surface.** Every rule of the map is the
  engine's: it reads a move, a fit and a zoom window, chooses the layers a query asks about, decides
  what a gesture tells, and keeps the refusals of access that stand, and asks the surface it is drawn
  on, the internal `MapSurface`, only what a renderer knows: its zoom limits and its projection, the
  camera moved or framed, which layers and sources its live style holds, the features drawn at a
  point, its symbol layers' labels, and the base map's sources and credits. The surface tells the
  engine, through internal methods, of a click, a long press, a hovering pointer, a frame, a failed
  request, a tile and a style loaded, and a camera at rest. The Android renderer is one surface, and
  `ScriptedMapSurface`, published under the elements' `testing` kind, is the other: it draws a map
  nowhere, answers from what a test states, records each move, fit, query and relabelling, and plays
  every gesture and load, so the `api: true` map capabilities run through the real engine with plain
  imports alone (§9), as `sdk-swift`'s `ScriptedMapSurface` does. Common Kotlin has no weak
  reference, so the engine holds its surface until the surface lets it go: the renderer detaches as
  it closes, so an engine that outlives its view holds no view, where `sdk-swift` holds it weakly. A
  map no view draws has no canvas and no projection: a fit frames nothing, a query answers none, and
  `project` and `unproject` answer `[NaN, NaN]`.
- **A camera move states the members it moves** (`map.camera.pose`). `jump`, `ease` and `fly` each
  take a `CameraTarget` of the members it states, `center`, `zoom`, `bearing` and `pitch`, each null to
  keep the camera's own, a centre past the antimeridian handed on as written, and refuse before
  anything moves a centre that is not exactly two finite numbers with its latitude in [-90, 90], an
  altitude beside them included, as `PositionRefused`, and a zoom, a bearing or a pitch that is not
  finite as `NumberRefused`, in that order, the refusal naming the member. The state takes the stated
  members, the zoom held within the window, and the renderer is handed them with their
  `CameraMotion`: `Jump` is MapLibre's `moveCamera`, `Ease` its `easeCamera`, half a second where no
  duration is stated, and `Fly` its `animateCamera`, its flight. `zoomIn` and `zoomOut` ease one level
  from the zoom the camera stands at, unrounded, held within the window, and `resetNorth` eases the
  bearing and the pitch to 0, which MapLibre's own verbs do not (`map.camera.control-moves`); the
  renderer is handed the members that move alone. How the camera travels, and for how long, is the
  platform's.
- **A fit is computed by the SDK on the canvas the map is drawn on** (`map.camera.fit-options`,
  `geo.camera.fit`). `fitBounds(bounds, fit)` reads the `CameraFit`, its `padding`, `maxZoom` and
  `duration`, first, refusing a ceiling that is not finite, a duration under 0 or endless, Kotlin's
  `Duration.INFINITE` included, and a padding edge that is not a finite number from 0 up as
  `NumberRefused`, then the box through `normalizeBoundingBox`, one that is not four finite numbers
  `PositionRefused`. The renderer frames it with `cameraForBounds` on the view's size in
  density-independent pixels, facing north and keeping the pitch, and eases MapLibre's camera there,
  never MapLibre's own fit, which takes no ceiling and keeps the bearing. `setItinerary(id, fit)` and
  `setCollection(id, fit)` read the fit before reading anything, frame the box an itinerary's segments
  span (`itineraryBounds`) or a collection's specification states (`specificationBounds`) once they
  drew, and answer whether they drew, which a closed map does not.
- **Locating asks a geolocation, and never the reader** (`map.camera.locate`). `locate()` asks the
  engine's geolocation for one fix and frames the box its accuracy spans, `accuracyBounds` at
  `LOCATE_MAX_ZOOM` at most, a fix of no accuracy framed on its position, and answers the position; a
  fix refused throws a `MapException` of kind `FixRefused` whose `reason` names it, `denied`, `no_api`
  or `unavailable`, and a closed map throws `Closed` without asking. `Geolocation` is the seam,
  `locate` and `permission`, both suspending and neither throwing; `InertGeolocation` answers `no_api`.
  On Android `DeviceGeolocation(context)` reads a fix only while the app holds
  `ACCESS_FINE_LOCATION`, the coarse permission alone answering `denied`, and never asks for one
  (§1); a fix of the last minute, `locateMaxFixAge`, is answered at once, otherwise one is asked of
  the fused, the satellites' or the network's provider and waited ten seconds for, `locateFixWait`,
  the `maxFixAgeMs` and `fixWaitMs` of `map/camera-locate.json`, both published in common code so a jvm
  runner holds them. The fix is asked at the precision every SDK's map asks one at, about a hundred metres, the
  box a locate frames being the accuracy it answers: `LocationRequest.QUALITY_BALANCED_POWER_ACCURACY` from API 31,
  as `sdk-swift` asks iOS for `kCLLocationAccuracyHundredMeters`, and below API 31, where a provider's fix takes no
  precision, the provider's own. Android tells a permission never
  asked from one refused only to an app's own activity, so with neither permission the permission
  reads `prompt`, which the app's own flow asks for. An engine given no geolocation of the app's own
  asks the device's, which `MapView` hands it over its context; with no view to reach it through, on
  the jvm, on iOS or before a view, a locate answers `no_api`, as a platform with no location
  services does.
- **A point query answers what the map drew** (`map.query.layers`, `map.query.feature-shape`).
  `featuresAt`, `poisAt`, `zonesAt` and `itinerariesAt` ask the renderer about the layers the map placed
  in the bands each reads, in the order the registry placed them, a layer placed again at its rank
  keeping its place there, and never a base map's layer or one the live style no longer holds; the
  renderer asks MapLibre each layer alone, topmost first, since MapLibre Android answers a feature
  with no layer. Each feature is a `QueriedFeature`: its id, a `QueriedFeature.Id.Number` or
  `QueriedFeature.Id.Text`, its properties holding a string, a number, a boolean or null, its
  geometry, and the layer and source layer that drew it. MapLibre Android writes every feature's id
  as text, so a vector tile's, which the tile format makes a whole number, is read back as that
  number, and a GeoJSON source's is read back as the style's document writes it, a number or a text,
  off the document MapLibre loaded (`writtenFeatureIds`), the text it is where the document writes
  none, a source whose data is an address (§11). `elevationAt` asks the Data API's terrain
  sample for its elevation alone, and a closed map still answers it (`map.query.elevation-at`).
- **What happens on the map is a `Flow` of `MapEvent`** (`map.events.*`). `events()` registers each
  collector as it begins, hands it every event from then on, none dropped, and ends at `close`: a
  `Click` and a `LongClick` carrying a `MapTap`, a `FeatureClick` after a click that landed on the map's
  own features, a `FeatureHover` where a hovering pointer ended a frame, an `AccessRefused`, an
  `AccessRestored` and a `Failure`. A gesture's longitude is brought within [-180, 180] only past it,
  and a gesture an edit takes is told to nobody. Kotlin names the gestures as Compose and MapLibre
  Android do, a click and a long click, where `sdk-swift` says tap and long tap; the value is `MapTap`
  in both. A hover is told on Android only for a pointer that hovers without pressing, a mouse, a
  trackpad or a stylus over the screen, MapLibre's view's hover events, and never for a finger; the
  engine keeps the last move and tells it at the next frame, the platform's `Choreographer`, which the
  renderer asks for only while a move waits.
- **A refusal of access is told, and stands** (`map.events.access-refused`,
  `map.events.access-standing`). A failure carrying 401, 402 or 403 is an `AccessRefused` with its
  status and address and never also a `Failure`, and every other failure a `Failure`. The layers
  manifest's read is `setTerrain`'s and `loadLayers`', told at the client's Tile API address with `/v1/tile-layers`
  joined on, which `Mountaya.baseUrls` publishes. On Android the request hook hears every answer of
  400 up to a request MapLibre makes, and every request that reached no answer, the connection
  failing or the request never leaving, at the address MapLibre asked for, never one the app's
  `transformRequest` or a redirect led to, since it is that address which lies under a source's tile
  templates or names the style asked for (`sdk/SPEC.md` §15), and a request MapLibre cancelled is
  told to nobody. It tells each to every live map that claims the address (`claimedAddresses`): one that starts
  with the text before the first `{` of a tile template of one of the map's sources, or that sits under
  its style's directory, the style's address cut before its query and its fragment, then up to and
  including the last `/` of its path, where a Mountaya terrain's document, tiles, sprites and glyphs sit,
  a style address of no path claiming nothing by it; an answer no map claims goes to the one
  live map where one alone is live, a font another host serves among them, and to nobody where several
  are (§11), as `sdk-swift`'s and `sdk-dart`'s hooks route theirs. The renderer names the map's own
  source whose tile templates the address lies under, a base map's naming none, and a tile MapLibre
  parsed, its tile action, MapLibre's own listener on the map's own view, is a tile of its source loaded. A
  refusal stands for its source until a tile of it loads, one naming no source until the next style
  loads, each answer ending the refusals of the sources the style no longer holds, and the end of the
  last is told once; `state.isAccessRequired` reads whether one stands. A 401 or a 403 answering a
  request that left under a credential the client no longer sends, a session token since replaced or
  minted after the request left under the key alone, or a visitor's cookie since changed, refuses one
  the map has already given up (`sdk/SPEC.md` §15): the engine tells it to nobody, neither as a refusal
  nor as a failure, and it starts no standing and ends none, while a 402, which refuses the
  organization's entitlement whatever credential asked, and every other failure are told as they are.
  On Android the hook reads each refusal of the credential as it arrives against the credential its
  request left with: the network interceptor keeps, for each call, the headers its last hop went out
  on the network with, and a call no hop of which reached the network is read by the headers its
  request entered the hook with; where `Mountaya.mapRequestHeaders` answers others now for the address
  that last hop went to, whose credential it took, under the same choice of map, the one attached last
  whose Tile API claims it, or that read throws, the request left under a credential the client no
  longer sends. The requests MapLibre
  sent before such a change are answered over many frames, so each refusal is read on its own as it
  arrives. A style refused so is asked for again, once an ask, its request leaving under the credential
  the client sends now: the hook posts its word to the main thread before MapLibre reads the answer,
  and MapLibre's own failure to load the style is read after it there, so it gives the map back the base
  map shown only where the style was not asked for again. What the hook hears reaches the main thread a
  batch at a time (`HeardFailures`): the first failure heard while no hop waits posts one, which hands
  the engine every failure heard until it runs, each once and in the order heard, so a storm of refused
  tiles heard while a frame is drawn is one hop rather than one a failure, as `sdk-dart`'s plugin tells
  Dart at most once a frame; the hop is posted as the first failure of its batch is heard, never held for
  a frame as `sdk-dart`'s is, which keeps the order above. A style refused under the credential the
  client sends is told once, as a refusal, its failing to load telling nothing more. A style whose
  request the hook told no failure of, a document answered 200 that MapLibre does not read, is told as
  a `Failure` of the style's own address carrying no status as its failing to load is read, before the
  map is given back the base map shown, as `sdk-swift` tells it, so no style fails without a word. A scripted map
  plays such a request with `ScriptedMapSurface.failRequest`'s `superseded`, the engine's rule reading
  it as it reads the renderer's. A request that never left because the client's credential could not be
  read for it, `Mountaya.mapRequestHeaders` throwing as the hook read it, is a `Failure` carrying what the
  read threw as its `cause`, and no status whatever the request would have met, as `sdk-dart`'s and
  `sdk-swift`'s failures carry it; every other `Failure`'s `cause` is null. A scripted map plays it with
  `failRequest`'s `cause`. The base map given back answers nothing as it loads again: it ends no
  refusal, the refusal of the style that did not arrive among them, which stands until a style the map
  asks for loads, as `sdk-typescript`'s stands (`map.events.access-standing`). The engine records the
  base map it gave back as `styleFailed` reports it, and the load of that base map is read as given back,
  a style the app asks for in between ending the record.
- **A map credits what it draws, Mountaya first** (`map.style.attribution`). `credits` is Mountaya's
  credit, then each base map source a visible layer draws at the camera's zoom, in the order its first
  layer is drawn, then each of the map's own a layer draws, in the order added, each credit once by its
  exact text and a blank one left out by `trimWhiteSpace`. A base map source the style names by its
  TileJSON's address credits what the TileJSON states where the style states nothing, as MapLibre Native
  reads the TileJSON into the source's attribution, and a scripted surface states its base sources so,
  `ScriptedMapSurface.baseSources`. The order is this SDK's own: the contract compares the credits as a
  set, MapLibre GL JS listing its line by length. The renderer replaces MapLibre's credit
  dialog, which reads the style's sources alone, with a sheet of these credits, each titled with the
  text its markup shows and opening its first link, and puts MapLibre's credit button bottom-right,
  where `sdk-swift`'s and `sdk-dart`'s sit, MapLibre's logo keeping the bottom-left, physical corners
  as `MapCorner`'s are, which a layout that runs right to left does not move, both 4 dp inside
  the map's safe area, the system bars and a display cutout and never the keyboard (below). MapLibre
  Android lays its ornaments out from its view's edges and applies no window inset to them, so on a
  map drawn edge to edge the navigation bar would cover both, where `sdk-swift`'s MapLibre anchors its
  ornaments to the safe area layout guide and `sdk-dart` places its credits inside the safe area. The
  renderer sets the credit button's margins, and the logo's where it shows, to the safe area's inset
  on each edge plus 4 dp each time the safe area moves and each time MapLibre's view is resized, on all
  four edges, so each keeps inside the safe area in whichever corner its gravity puts it, whichever way
  the layout runs: MapLibre sets the first margin it is handed as the start's and the third as the
  end's, so in a layout that runs right to left the left edge's is handed third. MapLibre's own
  compass, which shows on a map turned off north beside the one `MapControls` draws, is turned off, as
  theirs are.
- **A closed map is inert** (`map.engine.closed`). After `close` every command does nothing, a drawing
  call by id reads and draws nothing and answers false, a query answers none, a projection
  `[NaN, NaN]`, `locate` is refused as `Closed`, and so is a fix that arrives after the map closed,
  which frames nothing, `loadLayers` answers the catalogue it held and throws `Closed` where it held
  none, every flow of `events` has ended and a new one ends at once; `state` and `credits` read as the
  map ended, and `elevationAt` and a Data API result's add and edit, which read through the client,
  still answer, drawing nothing.
- **A map draws its content in slots, each drawing its latest request**
  (`map.slots.latest-request`). `setItinerary(id, fit)` and `setCollection(id, fit)` draw an
  itinerary and a collection by id, their specification read through the client,
  `getItinerarySpecification` and `getCollectionSpecification`, and answer whether they drew;
  `setItineraries(shown)` and `setZones(shown)` switch the credential's itineraries and zones on or
  off, a toggle switched on while drawn reading nothing; and `setSpecification(specification, name)`
  draws a specification the app holds under a name of its own, `removeSpecification(name)` and
  `clearSpecifications()` taking it off, `specifications` listing the names. A slot asked for again
  draws in the place it was first asked for, and a clear forgets it. A read that answers after a later
  call for its slot, or after the slot's clear, draws nothing, its call answering false. A fit is
  read before anything is read or drawn, and frames once the call drew: an itinerary's on the box its
  segments span, read beside its specification through `getItinerary`, and a collection's on the box
  its specification states (`geo.camera.content-bounds`).
- **A call is withdrawn by cancelling its coroutine**, Kotlin's own withdrawal, as `sdk-swift`
  cancels a task and where `sdk-typescript` takes an abort signal: withdrawn before it drew, a call
  throws its `CancellationException` and gives its slot back to the request it superseded; withdrawn
  after, nothing changes. Since every call can be withdrawn, a request a later call superseded waits
  for that call to draw, end or be withdrawn, and answers false only once it settles; every case ends
  as the contract's does. A toggle and a specification in hand take their slot for good. Every read
  runs in the coroutine of the call that asked for it, the engine launching nothing, so a call's
  cancellation reaches its read and a `LaunchedEffect` that leaves the composition withdraws it.
- **A base map switches whole, and the map's content is drawn again on it** (`map.basemap.terrain`,
  `map.basemap.replay`). The latest base map asked for wins, the variant the drawn document's where a
  terrain's two variants are two documents and the one asked for last otherwise; a slug the catalogue
  refused is never asked for again by a variant switch. Every layer the map draws is drawn again on
  the base map a switch brings in, at its rank and hidden where the app hid it, a layer the app added
  by hand with `addLayer` included: the contract leaves that one to each SDK (`sdk/SPEC.md` §15), and
  the renderer reconciles the state, every layer it holds, onto each style it loads, as `sdk-swift`'s
  does. A layer draws from a source of the map's own or from one the style it lands on holds, a base
  map's GeoJSON source among them, whose features a point query reads back as its document writes them,
  and one whose source neither holds waits for one. A document that does not arrive, MapLibre Android's `OnDidFailLoadingMapListener`, gives the
  map back the base map shown, its style, terrain and variant, and the renderer asks MapLibre for that
  style again and draws the content on it.
- **The layers catalogue is read once per studio scope** (`map.layers.manifest`). `loadLayers()`
  answers `getTileLayers`, held as `layersManifest` and shared by every load until
  `setLayersOrganization(organizationId)` moves the scope; each read that lands is told as
  `MapEvent.LayersChange` with the catalogue held, null where a move dropped it, so a reader of
  `events()` needs no read-back. A read that fails is kept by nobody, and one landing after the scope
  moved answers its loads the current scope's catalogue. `setTerrain`, `setOverlay` and
  `addEnrichment` resolve their slugs through it.
- **A map draws one classic overlay and stacks enrichments** (`map.overlays.composition`).
  `setOverlay(slug)` draws an overlay in place of the one drawn before and `addEnrichment(slug)` adds
  one on top, in the order first added, each easing the camera into the zooms it paints across; a slug
  the catalogue holds as the other kind is refused as `KindMismatch`, and one it holds under neither
  as `OverlayNotFound`. `clearOverlay()`, `removeEnrichment(slug)` and `clearEnrichments()` take them
  off, and `enrichments` lists the slugs drawn.
- **A map draws the overlay the server stamps for the instant pinned** (`map.overlays.instant`).
  `setInstant(instant)` pins the moment, the command a layers selection pins through, and `instant`
  reads it back; it outlives the overlay it was asked for, so a cleared overlay leaves it held. The
  classic overlay whose `capabilities.server.timestamp` is true is drawn from its sources' addresses
  stamped by `timestampedSources`, as it is drawn and again as the pin moves, and from the manifest's
  own addresses with none pinned, which the Tile API answers for the moment each tile arrives, so a
  device whose clock runs fast or slow still draws the present. A pin moved changes the addresses of
  the state's sources and nothing else, its layers standing where they stood; `MapView`'s renderer
  then gives MapLibre the source anew under them, taken off and added again with its layers made in
  their places (`sourcePushes`), since MapLibre Native's tile sources take their addresses as they are
  built and have no `setTiles`. A base-map switch keeps the addresses asked for, as the renderer draws
  the state's sources on every style it loads; an enrichment and a classic overlay the server does
  not stamp are drawn as the manifest writes them whatever is pinned; and a refused stamped tile is
  told at the address it was asked at, as any refusal is, the renderer naming its source by the
  template's prefix before its first brace.
- **A layer's visibility is the app's word, kept by its id** (`map.layers.visibility`).
  `setLayerVisibility(id, visible)` holds it before the layer is drawn, so it is born hidden, across a
  base-map switch and through a redraw of its content, until that content is cleared or the layer
  removed, and `isLayerVisible(id)` reads it; a hidden layer keeps its place and its tiles, its layout
  carrying `visibility: none`, and `MapLayer.isHidden` reads it. A concept is the catalogue's key a
  layer carries, `MapLayer.key` (`map.layers.keys`): `drawnLayerKeys(rank)` lists the concepts drawn
  in the order their content was first asked for, `layerIds(key)` names a concept's layers, and
  `setKeyVisibility(key, visible)` and `isKeyVisible(key)` show, hide and read them all. The wire's
  empty key names no concept, where `sdk-typescript` lists it, as in `sdk-swift`.
- **A plain map draws an itinerary's restricted stretches** (`map.road-access.layers`). Beside the
  first itinerary line of every specification the map draws, whatever its slot,
  `roadAccessLayersOf(source, isShown)` adds the stripes over each stretch at
  `LayerRank.ItineraryRestriction` and its kind's warning icon at the waypoints' band, both reading the
  tiles' `itinerary_restrictions`, shown until `setAccessRestrictionsVisibility(false)`, which
  `isAccessRestrictionsVisible` reads. Their layers stay drawn while hidden, so showing them again
  fetches nothing, and one the app hid by its id stays hidden either way. The renderer answers the
  stripe image a kind names with that kind's stripe tile, and a warning icon is a marker sprite, as a
  waypoint's is.
- **A Data API result is drawn under an id of its own** (`map.analysis.slots`,
  `map.analysis.layers`). Each kind has an add that asks the client and one that draws a result the app
  holds, asking nothing, both answering an `AnalysisHandle`, its id and its result: `addDirections`,
  `addSuggestions`, the candidate at `chosen` solid and the rest dashed, `addIsochrones`, `addPlaces`
  and `addMatrix`, whose result is handed over with its input, since its connectors run between the
  input's locations. Results accumulate, each drawn from a source of its own under
  `analysis:<kind>:<n>`, one count across every kind, at its kind's band, and taken off by
  `removeAnalysis(id)` or by its kind's clear. Points of interest are one result at a time,
  `setPointsOfInterest`, under `MapEngine.poisAnalysisId`, `analysis:pois`. An edit asks again with a
  patch, `((Input) -> Input)?`, that answers the input to ask with from the one held, since the
  generated inputs are immutable, and with no patch redraws the result held under the draw options it
  states, asking nothing (`map.analysis.edit`); it refuses an id nothing is drawn under,
  `AnalysisNotFound`, a result of another kind, `KindMismatch`, and a patch for a result handed over,
  `NoInput`. A query runs in a child of the caller's coroutine, raced against an abort: a later edit
  of its result, its removal or its kind's clear cancels it, and the call throws its
  `CancellationException` (`map.analysis.requests`).
- **Three geometry slots draw over the content** (`map.geometry.slots`, `map.geometry.layers`).
  `setGeometry(geometry, slot, color)` draws a geometry in `GeometrySlot.Preview`, `Highlight` or
  `Emphasis`, in the slot's own colour where it names none, `editGeometry(slot, geometry, color)`
  changes what it names, `clearGeometry(slot)` takes the slot down and `geometry(slot)` answers what it
  holds, a `SlotGeometry`; writing one slot never clears another, and a slot holding null draws
  nothing. Each draws from a source of its own as the itinerary's cased line in the map's paint, the
  highlight adding a circle for a point, at its band: the highlight at the chrome band, 100, above
  every content, and the preview and the emphasis at `LayerRank.ItineraryFocus`, 52, just above the
  itinerary lines, so the order the calls arrive in never stacks one under the route it lights.
- **A picked result is drawn above everything else** (`map.search.result-highlight`, Q5).
  `setSearchHighlight(highlight, accent)` draws a `SearchHighlight` from a source of its own,
  `mountaya-search-highlight`, at the chrome band, apart from the geometry slots, in the accent it is
  handed, the standard light one where a caller hands none: a place as the point-of-interest pin carrying
  `point`, or `map-pin` where the map's glyph set holds no point, a marker sprite the renderer answers as
  it answers a style's; a point of interest as its category's own pin in its category's colour, its
  feature carrying its category, and one of no category the table styles as a place; a way as its line
  under a halo and a white casing with a dot at each end, the dots a circle filtered to the collection's
  points, else its extent washed and outlined dashed, its east edge a turn further east where it crosses
  the antimeridian, else a pin at its anchor. The latest call wins and null takes it down at once; it is
  held as a slot of the map's content, so it is in `state` and drawn again on every base map a switch
  brings in; a closed map draws nothing. `MapEngine` holds no tokens, so the caller hands the accent,
  as `SearchControl` does.
- **A marker is the map's own, never a layer.** `MapMarker(id, position, color, glyph, label)` stands
  above everything the map draws, in the order added, the last on top; one added under an id in use
  replaces it and stands on top, an edit's null member keeps the one held and the marker keeps its
  place among the others, an id not shown is refused (`MarkerNotFound`), a removal of one not shown
  does nothing, and a position that is not two or three numbers, the first two finite with the
  latitude in [-90, 90], is refused (`PositionRefused`), a longitude past the range kept and the
  third number an altitude a marker drawn flat ignores, as `sdk-typescript`'s `addMarker` and
  `editMarker` answer (`map.marker.lifecycle`). On Android the renderer draws each as a pin of its
  own, a view above MapLibre's map, never a style layer and never through the registry, so no style
  change takes it off and no relabelling reaches its label. The pin is `map.marker.pin`'s: a disc
  28 dp across in its colour, the elements' `accent` token where it names none, as the tokens around the
  view, `LocalMountayaTokens`, read it under the scheme in force, a new accent drawing again every pin it colours,
  ringed in white 3 dp wide inside that diameter, with its glyph of the engine's set in
  white in the 16 dp square at its centre, or a white dot 8 dp across where it names no glyph or the
  set cannot draw the one it names, drawn as one bitmap (`markerPinImage`) on a view raised 2 dp over
  an oval outline, so the platform casts the soft shadow the web map's pin has. A look, what the pin is
  drawn as, the accent where its disc takes it and the screen's density, is drawn once and its bitmap shared
  by every marker carrying it, for as long as one does; a change of the markers updates the layer they stand
  on in one change, laid out once, a marker shown already keeping its view where its look stands, moved
  where its position changed, so only a look not shown before is drawn, as `sdk-dart` adds an image once
  per look. The waypoint being
  placed is a 24 dp white disc ringed 4 dp in the accent. The engine holds no tokens: the accent is
  presentation, which the host states around the view as it does for the controls. MapLibre Android's own
  annotations (`addMarker`, `Marker`, `setOnMarkerClickListener`) are deprecated in the range this SDK
  takes, and a click on one is consumed before any map click listener hears it. A marker's label is
  shown beside it when it is clicked, and taken off by a click elsewhere, as iOS shows an annotation's
  callout. It is drawn as `sdk-swift` and `sdk-dart` draw theirs: its words at 13 sp in the elements' `tooltip-fg`
  on a card of `tooltip-bg`, rounded at 4 dp and padded 8 dp across and 4 dp down, casting no shadow, both tokens read
  under the scheme in force from the tokens around the view, `LocalMountayaTokens`, and handed to the renderer as the
  accent is, a new pair painting the label again. It shows only while its marker's point stands on the map, from its left and top edges up
  to and not including its right and bottom ones (`isMarkerLabelShown`), as `sdk-dart` reads its canvas: a camera that
  takes the point off the map takes the label with it, never held at the map's edge without its pin, and one that
  brings the point back shows it again, the label kept for its marker meanwhile.
- **The camera's limits are the engine's state, and the renderer's to apply.** `setMaxBounds(bounds)`
  reads a lock as a box is (`normalizeBoundingBox`), a lock a turn or more wide as the whole world
  (`maxBoundsLongitudes`), and refuses one that encloses no area (`refusesMaxBounds`,
  `MaxBoundsRefused`); null lifts it. `setZoomBounds(min, max)` moves each limit a call states as a
  `ZoomLimit` (`map.camera.zoom-window`): `Kept`, the default, leaves it, `Released` sends it back to
  the renderer's own, MapLibre Native's floor of 0 and ceiling of 25.5 (`sdk/SPEC.md` §15), and `Zoom(zoom)` moves
  it there, both in one call; it refuses with nothing applied a limit that is not finite as
  `NumberRefused`, and a floor under the renderer's, a floor above the ceiling stated beside it or held,
  or a ceiling stated alone under the floor held as `ZoomBoundsRefused`. A call that moves the window
  reports one state and one that leaves it reports none. The engine brings the camera's zoom within the
  range itself, on `setZoomBounds` and every move, so the state is the camera shown. MapLibre Android
  takes a zoom limit from 0 to 25.5 and logs and ignores any other, so the renderer hands it the range
  held within those, the ceiling first where the floor rises past the one held, which MapLibre refuses
  otherwise. MapLibre
  Android bounds a pan lock's camera target, `setLatLngBoundsForCameraTarget`, which hands the bounds to
  the core's `setLatLngBounds` and clamps the camera's center, never the screen, and it publishes no
  bound of the screen, where iOS's `maximumScreenBounds` and the web's `maxBounds` hold what the view
  shows within the lock. The renderer therefore holds the screen itself: it raises the zoom floor to the
  zoom at which the view fits within the lock, and hands MapLibre, as the camera moves and as the view
  is laid out, the bounds the lock leaves the camera's target at the zoom shown, the lock with half the
  view taken off each side on Web Mercator, its middle on an axis the view is wider than; the view is
  read as it faces north and looks straight down. After a change of limits the renderer tells the
  engine where MapLibre then holds the camera, so the state is the map as it is shown, and tells it
  nothing where the engine holds another camera by then, one the app set since, which the renderer
  shows next, as `sdk-swift`'s renderer answers; the flow hands the renderer the newest state, so no
  report is made from a state the engine has left today, and the rule keeps it so wherever a state
  the engine has left is drawn. A camera the app sets is drawn as it is set, and held by MapLibre, the
  state keeping the app's. A lift hands MapLibre no bound at all, `setLatLngBoundsForCameraTarget(null)`,
  which the core reads as its unbounded `LatLngBounds()`, and MapLibre Android's constrain mode, which
  its Java API does not publish, stays the height-only one its map is made with, so a lifted lock
  leaves the view free to cross the antimeridian, as a map never locked crosses it, where MapLibre iOS
  keeps it a wall.
- **A click is `onClick`'s, and a camera at rest `onCameraChange`'s.** `MapView`'s `onClick` is handed
  each `MapEvent.Click` the engine tells, a moment after it lands, a `MapTap(position, point, marker)`:
  the place clicked, its longitude brought within [-180, 180] only past it, the point of the view in
  density-independent pixels, and the topmost marker whose point is drawn within 22 dp of the click,
  which the engine finds through the renderer's projection. It is MapLibre's map click, which is told
  after a single tap is confirmed, so a double tap zooms and is no click; the renderer answers MapLibre
  that the click is not consumed, so an app's own listeners on MapLibre's map are told it too. A long
  press is MapLibre's map long click, told as `MapEvent.LongClick`. `onCameraChange` is handed MapLibre's camera each time
  it comes to rest (`OnCameraIdleListener`), whoever moved it, as `sdk-typescript`'s `moveend`; a
  reader's move is told to the engine first. No handler is told frame by frame, since a composable
  recomposed at the frame rate is a cost no screen needs. The composable hands the renderer the handlers
  it was composed with last (`SideEffect`), as `sdk-swift`'s view does on each update. The value is
  `MapTap` in every SDK, whatever verb its ecosystem gives the handler (§5).
- **One layer registry.** Every layer a map draws above its base map goes through one registry that
  reads `geo.layers.order`: a layer is placed before the first layer of a strictly greater rank, so
  two of one rank draw in the order they were added, the later above. A layer placed again at the rank
  it holds keeps its place and takes its new definition, and one placed at another rank moves to the
  top of its new band; a content drawn again is taken off first, so its layers take the top of their
  bands (`map.layers.rank-insertion`). A tile specification's layers take the rank their source layer
  declares in the map's own table, `MapEngine.paint.layerRanks`. The renderer is the one code that adds a
  layer to MapLibre's map, every one above the base map's own, and a scan holds it (§7).
- **A patch is a map of members to nullable JSON elements** (`map.engine.patch`).
  `MapEngine.editLayer(id, paint, layout)` takes each as a `Map<String, JsonElement?>`: a key mapped
  to Kotlin's `null` is unstated and leaves the member held, and `JsonNull`, `false`, `0` and the empty
  string are values, a property set to `JsonNull` going back to the style's default on the map. A
  patch of a layer the map does not draw is a `MapException` of kind `LayerNotFound`, the map left as
  it was, as `sdk-swift`'s and `sdk-typescript`'s refuse an id they do not hold. A member stated as the
  value held keeps the held one, since a map compares what it holds as JSON values compare, a number as
  a number, an array and an object member by member: `4.0` restates `4` and `8e-1` restates `0.8`, so
  the state stands and nothing is told, while the text `"4"` is another value (`mergedMembers`). Kotlin has no
  `undefined`, and a nullable value is the language's own way to say a member is absent, so a
  builder that records each assignment, the shape a request body takes, would be a second spelling of
  the same thing; `sdk-swift`'s `[String: JSONValue?]` is the same shape.
- **A map draws the marker and stripe images its style names.** MapLibre Android asks for an image a
  style names and does not hold, `MapView.OnStyleImageMissingListener`, on the main thread as it lays a
  symbol or a patterned line out, and draws an image added to the style under the id before it goes
  on; the renderer answers where the id names a restricted stretch's stripes, `road-access-stripe-`
  then a kind of the vocabulary read as written (`readRoadAccessStripeImageId`), with the stripe tile
  of that kind, and where it names a marker sprite (`readMarkerImageId`, `map.icons.missing-image`),
  with the sprite of that glyph of the engine's set, `MapEngine.glyphs`, on a disc of that colour,
  each added at once with `Style.addImage`, not as a signed distance field, so the style never
  recolours it. Any other id stays missing, as MapLibre leaves it, a stripe id whose tail is no kind
  among them, as `sdk-typescript` leaves one. A plain `MapView` answers them, so an itinerary's
  waypoint pins, its restricted stretches and a map's points of interest draw with no editor, as
  `sdk-typescript`'s plain map answers them. The answer is synchronous, the sprite drawn on Android's own canvas in well under a
  frame, so no image waits on a later layout.
- **A sprite is drawn at the size its rule states, in density-independent pixels.** A marker sprite
  is a bitmap 48 density-independent pixels wide at the screen's density (`markerSpriteImage`,
  `map.icons.marker-sprite`), which states that density, since MapLibre Android reads an image's
  pixel ratio off its bitmap's density, so it shows 48 dp wide on every screen, as `sdk-swift` draws
  48 points and `sdk-typescript` 48 CSS pixels: the disc filled with the feature's colour, its
  border stroked centred on its edge, and the glyph's shapes, read through `parseGlyph` and never
  drawn as handed over, fitted to the 24 dp square at its centre and painted in white as
  `glyphPaint` answers, each shape's transform applied as it is drawn so a stroke scales with it as
  SVG scales it. A colour that is no CSS hexadecimal colour fills no disc. A restricted stretch's
  stripe tile (`stripePatternImage`, `map.icons.stripe-pattern`) is drawn the same way, at 16 dp,
  for the road access layers of `map.road-access.layers` to name, and so is a marker's pin
  (`markerPinImage`, `map.marker.pin`), 28 dp across, its ring drawn inside its diameter and its
  glyph fitted to the 16 dp square at its centre; a pin's fill that is no CSS hexadecimal colour
  fills no disc, as a sprite's does. All three are drawn through Compose's `DrawScope` on a bitmap's
  canvas, the scope `Glyph` draws in too, so one drawing of a glyph serves every one.
- **A glyph's geometry is SVG's, read by this SDK.** The renderer reads each shape into a Compose
  `Path` itself (`shapePath`, `svgTransform`), the same reading `sdk-swift`'s renderer makes: SVG
  1.1's path grammar, every command, implicit repetition, smooth curves and elliptical arcs drawn as
  cubic curves of a quarter turn at most, up to the command holding the first error as SVG draws a
  path in error; a circle, an ellipse, a rectangle with its corners as SVG 2 lays them out, a line,
  a polyline and a polygon; and a transform list, refused whole where any part is not SVG's.
  Compose's own `PathParser` is not used: it throws on a command it does not know, where SVG draws
  the path up to it, draws a path that opens with no move from the origin, where SVG draws none, and
  divides by an arc's radius as it is written, so an arc of no radius is no number where SVG draws a
  line, and a radius keeps its sign where SVG drops it; two SDKs would draw a licensed glyph two
  ways. Compose draws no `arcs` or `miter-clip` join, so both are drawn as a miter, as SVG asks of a
  renderer that draws neither, and the miter limit is set on every stroke; a path holds its points,
  and a transform its terms, at single precision, as Compose's do. The grammar is not contract, and
  it is proven on the emulator against every bundled glyph.
- **`Glyph` draws one glyph in Compose.** `Glyph(name, modifier, set, size, color, label)` draws a
  glyph of the set it is handed, the control set of the scope around it where it is handed none, the
  bundled one where no `MountayaScope` states another, as `sdk-dart`'s reads its scope's, in a square of its
  size, each shape painted as `glyphPaint` answers in `color`, the elements' `control-fg` token as
  `LocalMountayaTokens` reads it under the scheme in force where the host passes none, the controls' own
  ink, as `sdk-dart`'s draws in its scope's: Compose's foundation keeps no colour of the content around a
  composable, where SwiftUI's foreground style and CSS's `currentColor` are read from around the view, so
  a host drawing it beside text of its own theme passes that theme's colour, as it passes one to the
  text. A glyph the set does not hold, or whose markup draws nothing, emits
  nothing and takes no room, as a control draws a glyph its set cannot (`geo.icons.glyph-fallback`).
  It is hidden from accessibility services unless it is given a label, which it is then read as, an
  image. It builds on Compose UI alone, a leaf `Layout` drawn behind, and is the `Glyph` of
  `sdk-typescript`'s React binding and of `sdk-swift`.
- **The escape hatch is MapLibre's own map.** `MapView`'s ready callback hands back the
  `MapLibreMap`, so an app reaches what this SDK does not wrap without waiting for it. MapLibre is
  an `api` dependency of `mountaya-elements` for that reason and of nothing else (§7).
- **A map reads its organization once per publishable key and REST origin** (`map.organization.read`). `MapEngine.loadOrganization()`
  reads it through the core's `postOrganizationLookup`, its client's publishable key in the body, which the client
  hands over `InternalMountayaApi` as `Mountaya.publishableKey`, and no credential sent, and `MapEngine.organization`
  holds the answer. The read is kept for the process by its key and the REST origin its client sends to, so a
  staging client never shares an answer with Mountaya's, and is sent in a coroutine of its own on the dispatcher of
  the load that asked first and in no job of its, so a map that closes, or a caller that stops waiting, never
  cancels it for the loads still waiting: `select` hands a closed map's waiting load its `Closed` refusal while the
  read goes on. A read that fails is removed at once, and one that meets a `CancellationException`, which in no
  caller's job is never a caller's, a closed `HttpClient` or a transport scope cancelled under it, fails as a request
  that never arrived, a `ConnectionException` carrying it, so every load waiting on it fails rather than ending as if
  its own caller had left, and the watermark is drawn. Beside the read runs its one-attempt window, a timer of
  `Mountaya.organizationLookupBudget`, the client's budget for one attempt of `postOrganizationLookup`, 35 seconds
  where the client names none (`sdk/SPEC.md` §2): once it passes with no answer the read is overdue, and every load
  waiting on it, a later one included, moves the map's standing to `TimedOut` while the read's further attempts go on.
  `MapEngine.organizationRead` is that standing, public, as the contract asks of every SDK's public type for the read
  (`sdk/SPEC.md` §15): a `StateFlow<OrganizationRead>`, as the engine publishes its other state, `value` the standing
  now and a collector told each move, which every view drawing the map follows and a host reads to keep its own views
  clear of the mark's frame until the read answers white label: `Waiting` from the map's making, `TimedOut` once the
  read went overdue, `Failed` once a read failed, `Answered` once one answers, and `NoKey` where the client holds no
  publishable key. Each load moves it, the host's and a view's alike, and a closed map leaves it where it stood. A
  client holding no publishable key answers null, reads nothing and never throws, a closed map's load among them, so
  `loadOrganization` answers it as every SDK does, `sdk-swift`'s checking the key before the map's closing as this
  one does. Its answer serves the watermark and nothing else: the labels speak the engine's `labelLanguage`, its
  client's or `setLabelLanguage`'s language, else the device's, and the base map is the app's own `setTerrain` call,
  `setTerrain()` naming no slug drawing the catalogue's first terrain (`map.basemap.terrain`), whatever the
  organization prefers.
- **The map draws its watermark natively, and nothing hides it** (`geo.organization.watermark`). `MapView` begins its
  engine's read of its organization once it is composed, `loadOrganization` in an effect keyed by the engine, so a
  new engine handed to the same view is drawn in place of the last and read: the view makes a renderer for it, and
  hosts that renderer's view in an `AndroidView` keyed by the renderer, since an interop node makes its view once, so
  the new map, its pins and its mark are attached and the closed renderer's view leaves the window with its node. It
  draws the mark `watermarkOf` answers
  for the engine's standing, `MapEngine.organizationRead`, never a read of its own, so a read the host makes moves the
  mark too. It draws it as a view of its own, `WatermarkView`, laid on the layer the pins stand on, above every pin and
  beneath the label a marker shows and the readout, at its frame from the top-left corner of the map's safe area: the
  part of the map the window's insets leave visible, the system bars and a display cutout, read off where the map is
  laid in its window, its position and its
  size, and met with the map (`mapSafeArea`, `safeAreaOf`), so a map drawn edge to edge, as an app targeting API 35 is
  by default and as `enableEdgeToEdge()` draws one, shows the mark below the status bar, and one laid out clear of the
  insets measures it from its own corner. The insets are the window's own, its root window insets,
  `ViewCompat.getRootWindowInsets` of the composition's view (`rootSafeInsets`), which no `consumeWindowInsets` or
  padding of the app's layout rewrites, read again at each layout of the window's views, so a bar coming or going is
  followed; Compose's own value for the same types, `WindowInsets.systemBars` with `WindowInsets.displayCutout`
  (`composedSafeInsets`), stands in only until the window holds root insets. The on-screen keyboard is no part of the
  safe area: it comes and goes over the map as the reader types, and nothing laid out from the safe area, the mark, the
  stack, its panels, the readout or a marker's label, moves with it, as `sdk-swift` reads a window's `safeAreaInsets`
  and `sdk-dart` a view's `padding`, which leave the keyboard out too; `sdk-swift`'s `CONTRACT-GAPS.md` records the
  choice for every SDK. The frame's figures are
  physical: the layer the mark stands on is laid out left to right whatever way the text runs, so the mark stands at
  the reader's top left in a layout that runs right to left, and a pin over its position, every place on it reckoned
  from the map's top-left corner. The mark follows the read alone: it is laid out on the layer as soon as the read
  draws it, whether or not MapLibre has handed its map over, and the layer stands beside MapLibre's own view in the
  view `MapView` hosts, above it and never inside it, since MapLibre covers its own view in its load colour, a
  foreground, until its style has loaded and three frames are drawn, and for good where its surface never draws. Until
  MapLibre's map is handed over, no pin and no label has a place, so each is hidden, and the mark shows at once, as a
  client holding no publishable key draws it (`sdk/SPEC.md` §7). Android draws a view group's children by their height,
  their `z`, before their order, and hands a touch to the highest first, so the layer's order alone does not stack them:
  the mark is raised as high as a pin, 2 dp, which it follows on the layer, so it is drawn above every pin and takes a
  press over one, its outline none so the platform casts no shadow of it, and a marker's label as high as the mark,
  which it follows, so it is drawn above the mark, its outline none too, so it casts no shadow either, as `sdk-swift`'s
  and `sdk-dart`'s labels cast none. Nothing of this SDK's own lays over the frame, which is held clear
  from the map's first draw, while the read waits too, and released only once the read answers white label
  (`keptClearFrame`, over Geo's `isWatermarkKeptClear`): the `MapControls` stack in whichever corner it sits (below),
  its panels, the readout, and the label a tapped marker shows, which is the SDK's own interface too: it is laid at the
  size its words take, 18 dp past its pin toward the end its text runs to, to the pin's right in a layout that runs left
  to right and to its left in one that runs right to left, the map's own direction read since the layer runs left to
  right, and centred on the pin from top to bottom, as `sdk-swift`'s `markerLabelPlacement` and `sdk-dart`'s
  `markerLabelOrigin` place it, held inside the map's safe area and moved clear of the frame by the smallest move, as
  the readout is, its top at the frame's bottom where nothing inside the safe area clears it (`markerLabelOrigin`),
  drawn above the mark and never over it. The mark is
  drawn from its shapes' path data parsed by Geo's glyph reader, filled in their colours and
  their linear gradients, its silhouette blurred by a `BlurMaskFilter` of the drawing's Gaussian and cut out beneath
  the shapes, so the shadow is never over them, and the whole laid over the map at the drawing's opacity. It is drawn
  once into a bitmap at its size, so no frame of the map draws it again; it lets the bitmap go when it leaves its
  window and draws it again when it comes back at the size it kept, and the layer keeps every view it still shows
  attached rather than laying it again, so no change of the pins loses the mark. The layer clips what it holds to the
  map, so the drawing's margin falls outside it. A press opens its link through the platform's own opener,
  `openLinkOutside`, an `ACTION_VIEW` intent in a task of its own, as the credits open theirs and as `sdk-dart`'s
  device opener does, never through Compose's `LocalUriHandler`, which an app may provide, so no app redirects or
  swallows it; accessibility services read it by `watermark.label`. No parameter of `MapView`, of `MapEngine` or of any
  other element hides, removes or replaces it.
- **A long press opens the readout** (`map.readout.open`, `geo.readout.rows`). `MapView(readout = true)` holds a
  `ReadoutEngine` on its composition's scope, over a `TileElevationReader`, and hands it to its engine for as long as
  it holds it. The reader reads the elevation source the layers catalogue names: its TileJSON document at the address
  the catalogue states, `elevation.url`, for its `encoding`, its `maxzoom` and its tile template, the document's first
  `tiles`, and its tile size off `elevation.tile_size`, read once for each catalogue the engine holds, so a catalogue
  read again for another studio scope is read again; and each tile at the address the template fills, as `sdk-swift`
  reads both. Both go through the client's tile transport, `Mountaya.mapFile`, under the tile routes' own credential
  declaration, the base map's credential, and an address the client's Tile API does not claim, as
  `mapRequestHeaders` claims one, is refused before any request, so the credential never leaves for another host and
  the readout states no terrain. A tile is decoded through `BitmapFactory`, its pixels never premultiplied, on
  `Dispatchers.Default`, which `MapView` hands it through the seam `imageDecoding`, so no tile is decoded on the main
  thread and only its pixels land there. The reads a press needs leave in the order `elevationTilesAt` answers them,
  by `x` then `y`, each started where it is asked rather than dispatched. The engine opens and closes it, in common
  code, at the entry points the renderer calls, which the conformance runner plays: a long press no edit takes,
  `longClicked`, with MapLibre's camera zoom, opens it at the place the press is told to the map's listeners with; and
  a tap, `clicked`, whatever it lands on, and the start of every camera move close it, a reader's gesture as it
  begins, `cameraMoveStarted`, which MapLibre's move-started listener calls for every move, and a move the app or a
  control asks for, `jump`, `ease`, `fly`, a fit, a locate, a zoom step and a reset of north, as it is asked, as
  `sdk-dart` closes it. `ReadoutPopup` draws `readoutRows` over what the engine's point queries answer at the point:
  the topmost point of interest, whatever it names, its category its OpenMapTiles `subclass`, else its `class`, and
  its name its `name` (`readoutPoiOf`), the rows rule deciding what an empty one states, as `sdk-dart` and `sdk-swift`
  take it; the topmost zone's `name`, `description`, `level`, `start_at` and `end_at` (`readoutZoneOf`), the two times
  JSON numbers of seconds since the epoch truncated to the whole second, and a time written as a string, digits or
  not, read as none; and the first feature one of the classic overlay's own
  layers drew stating a `minValue` or a `maxValue`, `MapEngine.classicOverlayBandAt`, an enrichment's or the app's own
  feature being no band, the classic overlay drawn, named in the scope's language, its moment `overlayInstant` of the
  engine's pin on `LocalMountayaClock`. What stands at the point, the band among it, is read once, as the readout opens
  and as its terrain joins; the moment follows the engine's pin live while the card is open, read off an internal
  `StateFlow` the engine keeps beside `instant`, `MapEngine.pinnedInstant`, so a pin moved under an open card moves
  its moment and leaves its band as it was read at the press, as `sdk-dart` and `sdk-swift` draw it. A moment is
  written in the device's own time zone, `TimeZone.getDefault()` read through the seam `deviceTimeZone` as the card is
  drawn, never a zone an app states, so no card shows another zone while the device's is unknown. Where the platform
  knows no zone of the device's name, `formatDateTime` answering null, the card writes no moment: the row that would
  carry one is left out, the band's time, the moment row and a zone's window alike, and nothing stands in its place,
  neither the instant written in UTC or in ISO 8601 nor an empty word. Every SDK leaves a moment it cannot write out
  so, the readout's rows and Trail's cut-off and arrival alike, as `sdk-swift`'s `CONTRACT-GAPS.md` records. It is
  `readout-width` wide, its top-left corner at the point, flipped to the
  left of the point or above it where it would leave the map's safe area and held inside it, then moved clear of the
  watermark's frame by the smallest move, below the frame or to its right, that keeps it inside the safe area; where
  none does, it opens with its top at the frame's bottom and the map's edge cuts it, so the mark is never covered
  (`readoutOrigin`, over `clearOfFrame`). The frame is kept clear while the read waits too, as the stack keeps it. It
  writes its figures in the SDK's default units, `UnitPreferences(language)`, in the scope's language (`sdk/SPEC.md`
  §7), the aspect in whole degrees beside its direction's word, `123° · South-East`. A copy writes the pair to the platform's clipboard, and its button
  reads `readout.copied` for a second. The button's words are drawn in the elements' `accent` under the scheme in force,
  semibold, as `sdk-swift` tints its copy button and `sdk-dart` writes its own, the card's one action set apart from
  its `dimmed` captions.
- **The renderer draws the state, and reports the camera.** `MapView(engine, modifier, okHttpClient, readout,
  onReady, onClick, onCameraChange)` hosts MapLibre's `MapView` through `AndroidView`, drives it by the lifecycle of the
  composition's owner and destroys it when it leaves the composition, and draws every state the
  engine reports: a style URL MapLibre fetches, or a blank style document where the state names
  none, since MapLibre given no style shows a demonstration style of its own, fetched from the
  network; the camera as one `CameraPosition`, center, zoom, tilt and bearing together, which on
  Android is a zoom and never an eye's altitude, so a tilt leaves the zoom where it is; each source
  and layer reconciled, a GeoJSON source given its new features in place and a layer whose type,
  source or source layer changed made again; every property under its style name, through
  `PaintPropertyValue` and `LayoutPropertyValue`, read by MapLibre's own style parser as a style
  document's, so no table of MapLibre's names and no wrapping of a colour or a list is needed, where
  iOS needs both. A value the parser refuses is left unset and logged by MapLibre. A camera the
  reader moves by a gesture is reported to the engine once the camera comes to rest.
- **Tile credentials are attached per request, and only to Mountaya's hosts.** MapLibre also
  fetches styles, glyphs and sprites from hosts that are not Mountaya's, and a credential sent
  there is a credential leaked; the TypeScript SDK's `transformRequest` draws the same line.
  MapLibre offers one process-wide hook for it: on Android, the `OkHttpClient` installed through
  `HttpRequestUtil.setOkHttpClient`, carrying two interceptors `mountaya-elements` adds ahead of any
  other, an application one and a network one, as `sdk-dart`'s Android side carries them. It is one
  slot the app may want too, so `mountaya-elements` builds on an app's own `OkHttpClient`, the
  `okHttpClient` of the `MapView` attached last, rather than replacing it, and on MapLibre's own
  defaults where none is given. Every live map holds the hook at once, from its renderer's start to its
  close, so no credential outlives the last map. The application interceptor hears each request once,
  whatever redirect it follows: it calls the `transformRequest` of the map attached last that set one,
  with a `MapRequest` of the URL and the headers, and writes back what it answers, then reads the
  credential its address takes. The network interceptor gives each request as it goes out on the
  network, each hop of a redirect included, the headers `Mountaya.mapRequestHeaders(url)` answers for
  the hop's own address from the client of the map attached last whose Tile API claims it, by its base
  URL at a segment boundary of its path, each header the hop does not already carry, so a header the
  app's hook sets stands. OkHttp builds a redirect's request from the one it was handed, never from a
  hop on the network, so a credential never follows a redirect off the Tile API, where OkHttp would
  copy a header an application interceptor set to the new host, `Authorization` alone stripped; two
  maps of two clients on two Tile APIs each send their own credential. Each request reads the session
  snapshot (§4) and never waits, so a refreshed token reaches the next tile and a tile asked for before
  the first token lands goes out under the key alone. What the hook gives a request is the client's
  credential, its language and `X-Mountaya-Client`, which names this SDK on a map's own requests as on
  the client's, and nothing else of the transport, as `sdk/SPEC.md` §1 has a map library's own loader
  carry: MapLibre's `OkHttpClient` makes the request, over the app's own where the map is given one, so
  it is reported to no `onResponse` hook, carries no rate-limit state and is repeated on MapLibre's own
  schedule, never the `Retry-After` schedule, while a tile the client's own methods read is the
  transport's and held to all three. A request whose session token the client refuses
  at that read, whose credential holds a header OkHttp cannot carry, a cookie a share token's function
  answers among them, or that the app's hook answers with a URL or a header no request carries, never
  leaves the device: it fails with a `ConnectException` before any connection, which MapLibre reports
  and repeats on its own schedule, each repeat read again, as `sdk-swift` hands such a request back for
  a scheme no loader serves; where the client's read threw, the map's `Failure` carries what it threw as
  its `cause`, the hop of a redirect included. Two maps whose clients reach one Tile API with two credentials send the
  credential of the map attached last (§11).
- **A credential the map attaches is held to the client's checks before the map makes any
  request.** An empty key, a wrong prefix, a secret key in a publishable key's place and a
  character the HTTP stack does not send in a header (§4) are refused with `ConfigurationException` when the client is built,
  not at the first tile, and a session token a function answers per request is checked at that
  request (`sdk/SPEC.md` §1). A secret key, which the jvm alone can build, authenticates no route a
  map fetches, and `mapRequestHeaders` refuses it. A visitor's `Cookie` a share token's source
  answers is held to what a header carries as it is read, and one no header carries is refused with
  `ConfigurationException`, its value never written out, as `sdk-dart` and `sdk-swift` refuse it, so a
  host's own renderer is never handed it and the request hook fails that request as a refusal of the
  client's, as it fails a session token the client refuses. The client's language rides every tile request as
  it rides a call, and `*` where it names none, as `SystemExchange` sends, and so does the SDK that
  made it, `X-Mountaya-Client` (§4), beside the credential and to the Tile API alone.
- **The labels speak the client's language.** A map given no label language takes the one its
  `Mountaya` sends as `Accept-Language`, so the labels on a map and the words the API answers in
  are one language, and the device's where the client names none (`geo.style.label-language`). The
  engine relabels each symbol layer of the base map from the label its style wrote, through Geo's
  `relabelTextField` and the renderer's surface, when the language changes and on every style a switch
  brings in, and puts back as written a label it rewrote where a new language leaves it so; a layer the
  map placed itself keeps the label it states, in no language the map relabels
  (`map.style.label-scope`).
- **The map and editing engines are the binding's.** How a patch merges onto what a map holds
  (`map.engine.patch`) and where an edit's magnet lands (`map.edit.snap`) are the `map` layer's
  rules, held in this module (§2, §6) and applied by its engines to MapLibre's map. The magnet,
  `snapToActiveSegments` with its 40-pixel radius, is `internal`, as in `sdk-swift`; the renderer
  measures it through MapLibre's projection as the map is drawn, in density-independent pixels, the
  unit iOS's points and the web's CSS pixels are, so it reaches as far under a finger on every
  screen.
- **The editing engine is the map engine.** One engine per view and one state: `setActiveSegments`
  sets the lines the magnet places a waypoint on, none turning it off;
  `beginWaypointEdit(waypointId, position)` begins placing a waypoint, the same id again going on as it
  is and another replacing it; `placeWaypoint(position, projection)` places it where a click fell, its
  longitude wrapped, through the magnet and the projection it is handed, and does nothing while no
  edit is begun; `endWaypointEdit()` ends it, a save and a cancel alike, and answers the
  `WaypointEdit(waypointId, position, isSnapped)` as it stands. `ScreenProjection` is published, two
  functions to the screen and back, so a consumer with a renderer of their own places a waypoint
  through its projection. While an edit lasts, the view places the waypoint at each click, through
  MapLibre's projection, and tells no handler, as `sdk-typescript`'s point waypoint engine takes the
  click; the waypoint being placed is drawn as a white pin ringed in the accent. A placed waypoint
  is not rounded, where `sdk-typescript` rounds a click to nine decimals.

## 9. Conformance

A `conformance` module loads `sdk/registry.yaml` from the sibling `schemas` checkout and runs each
entry's fixture against this SDK. **The registry is the port manifest, and there is no other**: a
list of what this SDK has ported, kept beside the registry, is a second registry that disagrees
with the first.

- **One test per case, and every failure starts with `"<id> · <case>"`**, so a failure names the
  capability and the case without opening the fixture. The test function itself is generated,
  one per case, with a sanitised name: an id carries a `.` and a case may carry a `/`, and
  neither survives as a function name on every target. A framework with display names would
  carry the string as the name, at the cost of a compiler plugin tied to the Kotlin version.
- **The registry and the fixtures are embedded at build time.** A simulator and an emulator
  cannot read a sibling checkout, so a build task turns both into source the harness compiles
  in, and the same harness then runs on every target.
- **The harness pins no registry version.** The registry's `version` moves with the contract's
  expected values, not with its format, so a reader that refused a version it did not know would
  refuse a registry it reads perfectly well. The harness writes the version into the report and
  refuses a shape it was not written for: an entry missing a field, a layer it does not know, a
  fixture that is not on disk.
- **Scoping is by layer, and a scope is per entry.** Every `studio.*` entry is reported `scoped`,
  because §11 puts the studio out of scope, and it is reported rather than skipped: a skip is
  indistinguishable from a runner nobody wrote. One entry is scoped by id,
  `transport.browser.secret-key`, because this SDK publishes no browser target (§11). Nothing else
  is: `transport.browser.cookie-mode` is run (§4), and so is every `map.*` entry, since this SDK
  drives a renderer. A scope covers an entry whole and never a case of it, and a runner answers a
  case with a value or a refusal, never with a skip. Any other entry with no runner fails the
  gate, and the list of those entries is the work left.
- **A runner for an id the registry dropped fails too**, since it proves a rule the contract does
  not declare.
- **Every published name is declared, or its capability scoped.** The gate reads each entry's
  `kotlin` column of `published`, this SDK's exact spellings of the declarations a consumer calls for
  it (`sdk/SPEC.md` §10), and fails on a name of a capability this SDK does not scope that no module's
  committed ABI dump declares as written (§5). The jvm's, Android's and the klib's dumps are read
  together, each declaration by its path after its package, so a name one target alone declares
  answers too, the jvm's `constructWebhookEvent` and Android's composables; a JVM name is read
  without the suffix Kotlin mangles a value class's signature with, a nested class `Outer$Inner` as
  `Outer.Inner`, and a top-level property's getter under the property's name. A name is matched by
  its path and never by its package, so a declaration answers wherever its module files it. The
  column is `scoped` exactly where this SDK scopes the capability, and the gate fails where the two
  disagree, either way. The gate prints how many of the names it looked for are declared, and a
  capability the registry adds under a name no module declares fails it as one with no runner does.
  A `published` that is not the four columns, such as the one list the registry wrote for every SDK
  before them, is refused as a shape this reader was not written for.
- **The report says what is published.** Beside each capability's status, the report holds
  `published`: for every capability whose `kotlin` column is a list, the names of it the committed ABI
  dumps declare, in the column's order, none where they declare none, so the contract's own gate holds
  the report to the column and a report equals it where every name is declared.
- **Every layer and `api` flag the registry carries names the module its runners live in**, in
  the root build:

  | Layer | `api: true` | `api: false` |
  | --- | --- | --- |
  | `transport` | `conformance` | `mountaya-client` |
  | `geo` | `conformance` | `mountaya-geo` |
  | `map` | `conformance` | `mountaya-elements` |
  | `studio` | `conformance`, every entry scoped | no entry at this tag |

  A module may be named before it exists, and the table being the root build's rather than each
  module's is what lists a module's capabilities as having no runner, the work left, rather than as
  owned by nobody. A layer or a flag the registry adds is a gate failure until the table names its
  module.
- **An `api: true` entry runs from the `conformance` module**, which depends on the others as a
  consumer does and so proves the capability is reachable from outside. **An `api: false` entry
  runs from the owning module's own test source set**, the only place Kotlin shows `internal` to
  a test. **The `map` layer follows the same split**: `map.engine.patch` and the fifteen `map.*`
  entries of the camera, the queries, the events, the credits, the label scope and the closed map, all
  `api: true`, run from `conformance` through the public `MapEngine` drawn on the published
  `ScriptedMapSurface`, the real engine applying every rule with a plain import, and `map.edit.snap`
  and the three `map.icons.*` entries, `api: false`, from `mountaya-elements`' common tests, since
  their rules are `internal`. All are common code, the module being Kotlin Multiplatform with its
  renderer alone on android, so they run on the jvm and on the iOS simulator with no emulator; what
  MapLibre does with what the engine hands it is the renderer's device tests'. A task merges every module's results into one report. A runner
  is found by its file name, the capability's id in UpperCamelCase followed by `Runner`
  (`TransportRetryBackoffRunner`), so the registry stays the only list of what is ported. It sits in
  the directory its module's build names, `conformance/rules/`, or `runners/rules/` in `conformance`,
  and declares its module's package as every file does (§6); the tests printed over it take a package
  of the build's own beside it, `com.mountaya.client.conformance.generated` for the client's, which
  no hand-written file declares.
- **An entry runs whole on a target, or not at all there.** The jvm runs every entry this SDK does
  not scope, every case of it. The iOS simulator and an Android emulator run the entries of
  `mountaya-client`, `mountaya-geo`, `mountaya-elements` and `conformance`, because their locale, decomposition and SHA-256 `actual`s and their HTTP engines
  are second implementations and Android's locale data lives on the device rather than in the
  JVM, all but three: `transport.credentials` builds every case's credential through the public
  factory, and its secret-key cases need the overload only the jvm declares (§4);
  `transport.errors.envelope` sends `postSession`'s refusals, which a secret key alone
  authenticates, through that overload too; and `transport.webhooks.verify-signature` verifies every
  case through `constructWebhookEvent`, which the admin unit's jvm alone declares (§4), so their runners sit in
  `conformance`'s jvm tests and the three entries run there alone. What those cases would meet on Android and iOS is a missing symbol, which the ABI
  dumps of those targets hold (§7); the webhook rule they reach is the admin unit's common code, and its own tests
  run on every target with the clock stated. `conformance` depends on `mountaya-admin` as a consumer does, for that
  runner and for the two transport entries whose fixtures name an `admin` operation, `transport.response.envelope`
  and `transport.pagination.walk`.
  `transport.browser.cookie-mode`, whose secret-key case reaches `Credential.Secret`'s `internal`
  constructor from `mountaya-client`'s own tests, runs on every target.
- **Floating point compares within the fixture's `tolerance`**, as `sdk/SPEC.md` §10 defines its
  three kinds, `absolute`, `relative` and `angular`, and exactly where none is stated. A kind the
  harness does not know fails the fixture, and the harness reads every fixture the registry names,
  a scoped one included, so a new kind is caught the day it lands.
- **A number JSON cannot carry is decoded before a case runs.** `{ "$number": "NaN" }` and the two
  infinities become `Double.NaN`, `Double.POSITIVE_INFINITY` and `Double.NEGATIVE_INFINITY`, and a
  number that is not finite agrees with itself alone under any tolerance, so `NaN` agrees with
  `NaN`, which `==` on two `Double` values denies.
- **A refusal names the class, never the message**, spelled as `sdk/SPEC.md` §3 spells it. One
  fixture, the scoped `browser-secret-key.json`, still writes a refusal as `error` with `throws`,
  and the harness reads that spelling beside `refused`.
- **The transport's fixtures run on a scripted exchange and on virtual time.** Every runner of a
  call puts a scripted `HttpExchange` behind a real `Mountaya`, or behind the transport where the
  entry is `api: false`, answering each attempt from the case's script on `runTest`'s virtual time,
  with a wall clock that reads that time and the case's own draws through `RandomSource`, a drawn
  `Idempotency-Key` among them counted, the nth a version 4 UUID that holds n, so a run draws the
  same keys every time and `drawnKeyPattern` still matches each; a 60 s wait budget or a 45 s
  stall runs in no wall time. The public runners reach every operation through
  the published surface alone: a printed method, or a call by declaration for an operation the case
  invents, which is how `transport.events.method-refusal` reaches a buffered call that asks for a
  stream. `transport.pagination.walk` walks the printed `getOrganizationMembers`, whose builder
  takes the four query parameters the fixture writes, its rows members numbered by `user_id`. The
  stream runners stop a case by answering a reopen with a 404, which ends either stream, and read
  `Last-Event-ID` off it; in `transport.events.reconnect` an open that stays open writes the comment
  the API writes every 15 seconds, as `sdk/SPEC.md` §12 has an open a fixture keeps open write one, since
  the fixture's `outlives-its-token` opens would otherwise stay silent past the 45-second window §12 bounds
  an open stream's silence by, an open the case
  answers 429 or 503 is coded as the contract codes it with the `Retry-After` it names, and a case
  naming the user the reader reads the stream as is read both ways §12 gives it one, through a
  session source naming the user and through a plain token and `readingAsUser`, the two held to
  answer alike; the collaboration feed's open-errors class for a failure that reopens is read off
  the same answer to a request, and `transport.events.open-errors` draws every wait at its ceiling,
  so the opens an agent run's reader holds to two minutes move the test's clock toward them.
- **The interruptible-backoff fixture names a caller's cancellation `abort`.** The runner translates it: an
  abort before the call is a coroutine cancelled before it, an abort during the wait is a
  cancellation during it, no signal at all is the wait called from the test's own coroutine, and
  "the signal's own reason" is the `CancellationException`. Where a test runs with assertions on,
  kotlinx.coroutines rethrows a cancellation that crossed a suspension as a copy carrying the
  original as its cause, so the runner finds the reason in the cause chain rather than by
  identity alone.
- **A constant a fixture declares is held to the rule's own.** A runner reads each of the
  fixture's `constants` back through the surface the entry proves, a defining factor as the
  conversion of one unit and a radius as the constant Geo publishes, and compares the two within
  the fixture's tolerance. A constant the runner does not know fails the entry, so one the
  contract adds is read the day it lands rather than read past. A constant a rule keeps
  `internal` is read back through what the rule answers: the zoom floor as the minimum of a range
  that reaches zoom 0, the nominal canvas as the ratio of two altitudes, the zoom ceiling and the
  floor an altitude converts to as what an altitude far too low and far too high convert to, the
  field-of-view factor as an altitude over the ground one pixel spans, the unit patterns as what the
  formatters write of a measurement of 5 of each unit, a duration of 2 hours and one of 5 minutes and
  a pace of 5 minutes, each number written in place of `{0}`, the minimum grouping digits as the
  elevations 1531 and 15,310 each language groups, and the time zones' table as what
  `timeZoneViewport` answers for every name it holds.
- **A constant a fixture writes as a set is held as one.** `geo/layer-order.json` writes
  `poiSourceLayers` as the array of its members, whose order says nothing (`sdk/SPEC.md` §10), and the
  runner holds it to the members of Geo's `poiSourceLayers`, each once, in any order, beside
  `poiSourceLayerNames`, the same members held as a list in its order.
- **Nine more geo fixtures are read at their edge.** `geo/search-filters.json` names a section as
  the Data API's search answer names its field, `places`, and its description has a rule that holds
  a `SearchTarget` read the name in upper case, `PLACES`, as the runner does;
  `geo/search-query.json` writes a cap of 2.5, which the `Int` a round's cap is cannot hold, so the runner reports it refused; `geo/segment-colors.json` writes positions that
  are no whole number, which the runner hands the overload that takes a `Double`;
  `geo/overlay-lookup.json`, `geo/paint-zoom-range.json` and `geo/layer-order.json` write the
  Tile API's types with only the members a rule reads, and the runners build them with a
  placeholder in every other; `geo/search-rows.json` and `geo/data-geojson.json` write the Data
  API's results as its JSON, decoded by the wire's own serializer, a cell that is no finite number
  read as the double it is, and an answer is written back as
  the fixture writes it, a member a row does not state absent and one a feature does not state
  null; and `geo/i18n-bundle.json` writes a refusal as the value `refused`, which the runner
  answers for an override `wordOverridesOf` refuses.
- **A case names the question it asks in its input.** `geo/longitude.json` and `geo/pace-clock.json`
  name each case's `operation`, and `geo/route-geometry.json` its `measure`, as `sdk/SPEC.md` §10 has
  every case of the transport, geo and map layers do, and the runners dispatch on that value, never on
  the case's name. Two fixtures are read at their edge besides: `geo/meters-to-route.json` writes no
  distance as null, "where an SDK answers its infinity", and `geo/route-ground-meters.json` writes a
  jump that is no whole number, which a route's `Int` index cannot hold. The runners answer
  `Double.POSITIVE_INFINITY` as null, and build the route without such a jump, which is no jump by
  construction, as the rule answers for it.
- **Three profile fixtures write the wire in another spelling.** `geo/waypoint-summary.json` writes
  a place's `details` as `geo.data.itinerary-waypoints` answers them, a timing's `cutoffAt` and
  `stopDuration` and a contact's `openingHours`, and its description has a rule over the REST API's
  waypoint read them at its edge as `cutoff_at`, `stop_duration` and `opening_hours`, as the runner
  does; `geo/segment-groups.json` writes
  a mode of `ROUTED`, a creator as its id and segments with no colour, name or revision; and
  `geo/info-bands.json` gives a segment's entries as `info`. The runners build the REST API's own
  types from them, a placeholder in every member a case leaves out and no rule reads, and write the
  projected chain back as the wire writes it, a creator as its id, a segment that passes as itself
  with the members its case wrote. A constant that is no single number, a ramp, a rank table or a
  vocabulary, is held to the value the rule states, and `geo/locale-services.json`'s reference
  instant and zone are read by every `dateTime` case rather than held, a case's own `timeZone` read
  in place of the fixture's where it names one, and a `dateTime` case names the lengths it is
  written at, `dateLength`, `timeLength` or both, each the `DateLength` or the `TimeLength` of that
  name, so a case naming neither, or a length outside those, fails rather than being read as the
  long date `formatDateTime` writes where none is named.
- **The navigation fixtures are read at their edge.** `geo/navigation-steps.json` writes each
  surface's steps as its own JSON, decoded by the wire's serializer, so a maneuver neither surface
  spells reaches the adapter as written; `geo/navigation-instruction-keys.json` writes only the
  members of a step an instruction reads, and the runner gives the step a range of vertex 0 alone;
  `geo/navigation-progress.json` writes the steps a route is followed by, and the runner builds the
  route through `followedRouteOf`, which keeps them as they are; and
  `geo/navigation-route-request.json` names a request by the case and an answer by the request it
  answers, which the runner reads as the ticket that request last left under;
  `geo/navigation-prompts.json` builds its route as `geo/navigation-progress.json` does, through
  `followedRouteOf`, and lays it out by `guidancePlanOf`, a `due` case reading a whole progress or
  none; and `geo/navigation-engine-walk.json` plays its steps on one `ScriptedNavigationEngine`, whose
  requests the case answers, reading the state the engine committed last after each step, never a
  count of the states it told. The five entries a consumer only observes, `off-route`, `status`,
  `route-request`, `prompts` and `engine-walk`, run from `mountaya-geo`'s own tests, which hold their
  constants exactly with a holder of their own, since the conformance module's reads the public
  surface alone; `geo/navigation-status.json`'s table is held cell by cell.
- **The icon fixtures are read at their edge too.** `geo/glyph-set.json` and
  `geo/glyph-set-chain.json` write a set's paint as a string, which the runners read into the closed
  `GlyphSet.Paint`, so `filled` is refused by the type before any set is built, and they hand
  `GlyphSet.of` only the box and the width a case states, so a set that states neither is built at
  the factory's own defaults, which the fixture's constants are held to; `geo/glyph-set-chain.json`
  lists a chain's names sorted by code unit, the order a Kotlin string sorts in;
  `geo/glyph-paint.json` and `map/icons-marker-sprite.json` state a set by its paint, width and box
  alone, and the second, `geo/glyph-resolution.json`, `geo/glyph-fallback.json` and
  `map/icons-missing-image.json` by the glyphs it holds, so the runners build a set named for the
  case, each glyph it holds a placeholder no rule reads, and hold the paint's constants to what a
  shape stating nothing is painted with; `geo/glyph-parse.json` declares its lists, held to Geo's
  sorted as the fixture lists them, each shape read as a shape where it is written alone;
  `geo/glyph-fallback.json`'s `control` surface names no rule of Geo's, so the runner answers it
  from the set's holding, as `Glyph` reads it, and its place and badge surfaces through the internal
  rules; `map/icons-missing-image.json` declares the glyphs its map's set holds as a constant every
  case reads; and `map/icons-marker-sprite.json`'s `glyphBoxPx` is read back as the sprite's scale
  times its set's box. The three `map.icons.*` capabilities are `api: false` and run from
  `mountaya-elements`' common tests, as `map.edit.snap` does, and `geo.icons.glyph-fallback` from
  `mountaya-geo`'s, on every target.
- **The camera's fixtures are read at their edge.** `geo/camera-fit.json` and
  `geo/zoom-limit-reached.json` write a zoom window as `{ max, min }` and `geo/zoom-into-range.json`
  as a style layer writes one, `{ maxzoom, minzoom }`, each read into a `ZoomBounds`;
  `geo/camera-fit.json` writes a canvas as `[width, height]`, the list `cameraForBounds` takes, and a
  padding as one number or one per edge, read into a `PixelPadding`,
  none where it is left out; `geo/content-bounds.json` writes a segment by its `position` and its
  `geojson` alone, built as the REST API's segment with a placeholder in every other member, and its
  `waypoints` are handed to nothing; `geo/hovered-itinerary.json` writes features with an `id` and
  `properties`, handed to the rule as their properties alone, so an `id` is never read; and
  `geo/zoom-limit-reached.json` names the controls an end disables by their roles, `zoom-in` and
  `zoom-out`, which the runner writes from the `ZoomControl`s `zoomControlsDisabled` answers.
  `geo.camera.zoom-limit-reached`, `geo.camera.accuracy-bounds` and `geo.layers.hovered-itinerary`
  are `api: false` and run from `mountaya-geo`'s own tests, their constants held exactly.
- **The map's fixtures are played on the scripted surface.** A value no Kotlin type holds, a centre
  written as an object, a pitch or a status written as text, a fit that is neither a boolean nor
  options, is refused by the type before any call, as the kind the fixture names, as in `sdk-swift`; a
  member the case calls `unstated` is null. A drawing call of `map/camera-fit-options.json` is
  `setCollection` of a collection whose specification states a box, a refused fit reading nothing, and
  `map/engine-closed.json`'s is the real `setItinerary`; a case played on MapLibre GL JS's limits is played on a surface of -2 and 22,
  and MapLibre Native's are held to the window a map opens on and releases to; the access steps are
  told apart by a click between each, a refused step refused at the status it states and a 402 where it
  states none; a renderer's failure and a refused step are played under the credential their `sentUnder`
  names, handed to the engine as `superseded` as the renderer hands it its hook's reading, so the
  engine's own rule tells a refusal of one the client no longer sends to nobody; and the elevation's
  selection is read back off the document the
  Data API attempt sends whole, its first, a persisted digest, answered as unknown. `BaseUrls`
  refuses an origin with no scheme, so the manifest case whose Tile API address is a path of a page's
  own origin, `/tiles`, a web page's alone, is played on a stand-in origin and its address answered
  relative to it. Each constant is read back through what the engine tells: the refusing statuses by
  every status from 100 to 599 played, the manifest's path by a refused read, Mountaya's credit by a
  map that draws nothing, and the bands by `LayerRank`.
- **The family 8b fixtures are read at their edge too.** The `map.*` scripts of the map's content
  drive the real engine through a plain import on `ScriptedMapSurface`, as the camera's do; the six
  marked `api: false`, `map.basemap.replay`, `map.road-access.layers`, `map.analysis.layers`,
  `map.analysis.requests`, `map.geometry.layers` and `map.marker.pin`, run from `mountaya-elements`'
  own tests, the rules among them held through the map's paint with the case's patch folded on as a
  consumer restyles a copy. A script whose reads are held until a step answers them,
  `map.layers.manifest`, `map.slots.latest-request` and `map.analysis.requests`, plays every call in
  the case's `backgroundScope` and settles each step with `runCurrent()`: the engine and the client
  run on the test's one thread, so no step waits on real time; its exchange holds each read on a
  `CompletableDeferred`, a read withdrawn failing with its coroutine's cancellation, and a read is
  named by its path. `map.specification.style` draws through `setSpecification` and leaves the
  restricted stretches out, which are `map.road-access.layers`'s; `map.layers.rank-insertion` writes
  the base map's own layers first, beneath every layer the engine holds, as the renderer draws them; a
  `switch` (`map.layers.visibility`, `map.basemap.replay`) sets each base map in order, then plays its
  style loading or failing, `loadStyle()` or `failStyle()`. `map.analysis.edit`'s patch writes the
  members a case states onto the input held through the input's own serializer, a member named
  `unstated` left as held and `{}` no patch at all, and its queries are read off the full document
  each sends once its persisted digest is refused. `map.marker.lifecycle`'s edit leaves null each
  member its patch names `unstated`, as a Kotlin caller leaves a member it keeps. The eleven `geo.*`
  capabilities run through a plain import: `geo.style.map-paint` folds a patch through
  `MapPaint.copy`'s builder, a member written null left unset and one the paint does not hold failing
  the case; `geo.style.itinerary-stroke` assembles the stroke's layer with `type: "line"`; a point of
  interest's colour is written `#rrggbb` only where it is opaque; `geo.data.road-access-stretches`
  reads its ranges with REST's decoder; and `geo.data.itinerary-waypoints` builds each wire waypoint
  as its type holds it, a member in another shape left out, which a typed decoder would refuse whole,
  as in `sdk-swift`. The two engine fixtures of the itinerary resource play their reads on
  `CompletableDeferred`s in the case's background scope, settled by `runCurrent()`. Every runner whose
  fixture declares no constant holds that it declares none, so a constant the contract adds fails the
  entry.
- **The family 8d fixtures are read at their edge too.** `geo.style.theme-tokens` states each override
  as a consumer does, the token set whole on a copy of `ElementTokens.standard`, a token of its value
  alone, reached by its member through the runner's table of the contract's names, a pair of property
  references, the one the token is read by and the one the builder sets, which a jvm test holds to the
  contract's own table, `sdk/elements/tokens.json`, name for name, each entry reading the table's value
  and writing the member it reads; a colour is written back `{hex}`, with `opacity` only where it is
  translucent. `geo.i18n.nested-scope` names the glyph sets by name, the bundled ones
  `bundled-controls` and `bundled-markers` and any other a set the runner builds under that name, and
  reads the scope's words through `word(path)`. `geo.i18n.counted-words` reads its word off the
  elements' own words resolved for the case's language, a counted word through `countedText` and a
  template through `filledText`. The three selection scripts play a selection over doubles of
  `LayersSelectionTarget` holding every read and suspending command on a `CompletableDeferred` until a
  step settles it, numbered across maps, a catalogue named by slug built as one classic category,
  `conditions`, and the category whose overlays stack; a cleared overlay and a removed enrichment, which
  do not suspend, are numbered among the commands and answered at once, and no case settles one; a pin is
  written by its instant in epoch milliseconds, and an RFC 3339 pin that names no instant is handed as none. The
  locate control's script plays it over doubles of `GeolocateControlTarget`, whose read answers
  `denied` as refused, `granted`, `prompt` and `unknown` as not, and `fails` by throwing. Every script
  runs its engine in the case's `backgroundScope` and settles each step with `runCurrent()`, no clock
  read. The layers picker's runner writes a heading's word by its path, as Geo holds it, reads the case's
  `picked` and `time`, no pick and no time where it states none, and holds `poisOverlay` to
  `POIS_OVERLAY_SLUG`; the preferences' runner names a word by its key in the `preferences` section, as the
  fixture does; and `geo.layers.poi-rows` labels a group by its id and reads its note as the view does,
  the zoom not at least `poiPanelNoteZoom`. The four `api: false` capabilities,
  `geo.controls.geolocate-state`, `geo.layers.picker-rows`, `geo.layers.poi-rows` and
  `geo.units.preference-choices`, run from `mountaya-geo`'s own tests through rules a composable of
  `mountaya-elements` reads and so publishes. The controls and panels are proven on the emulator as
  Compose UI tests: each drawn under the light and the night configuration and read pixel by pixel, its
  presses performed through its semantics, with no network.
- **The time fixtures are read at their edge too.** A Kotlin `Instant` names an instant or is no value, and a
  `kotlin.time.Clock` answers one on every read, so the contract's pin that names no instant, `tomorrow` or
  the empty string, is read as no pin, and its clock that reads no instant, NaN, as no reading, each refused
  by the type: `geo.layers.overlay-instant` answers such a case the rule's answer without a reading, the pin
  as it is or no moment, and `geo.layers.time-window`, `geo.layers.time-slider` and `geo.layers.time-readout`
  lay out the track of no instant, which the rule lays out where a track would end past the range every SDK
  holds one to, at a reading of 8.64e15 milliseconds. Every instant is written in epoch milliseconds.
  `geo.layers.tile-timestamp` builds a typed catalogue of its categories, each overlay's capabilities and
  sources as written and every other member a placeholder, and an instant that names no moment asks nothing.
  `geo.layers.time-slider` reads `dayStops` and `labelledDays`, which Geo keeps to itself, back through the
  ticks the rule answers, and its `held` word where the window holds no instant, as `LayersPanel` holds its
  slider. `map.overlays.instant` plays the real `MapEngine` on `ScriptedMapSurface` over a client serving the
  case's manifest, a switch as `map.basemap.replay`'s runner plays one and a failure as
  `map.events.access-refused`'s, and reads each overlay's addresses off the sources the state hands the
  renderer.
- **The family 8e fixtures are read at their edge too.** The five engine scripts play a real
  `SearchEngine` in the case's `backgroundScope`, its pauses waited out on the case's virtual time,
  over a transport that records each request as the engine sends it, holds it on a
  `CompletableDeferred` until a step answers, fails or reports it, and records the requests the engine
  abandoned while open; every step settles with `runCurrent()`, and a `wait` moves the time with
  `advanceTimeBy`, so the pauses are waited out on no real time. A branch is read as the target its
  name upper-cases to, a request written through the Data API's own serializer with its targets'
  enabled flag alone, and an answer built as the Data API's result of one entity per name, each
  titled by it; `configure`'s `units` are a reader's preferences, English and metric where they leave
  one out; the answer is read before the script's search is closed, which abandons what it still
  holds. `geo.search.focus` plays a round of one branch through the engine, and holds its focus to the
  internal rule besides; `geo.search.list-state` builds a snapshot of rows titled as named, a failed
  section holding the API's report; `geo.search.describe` reads its entity through the Data API's
  serializer and its words through a resolver, and holds its elevation floor and separator through
  the line itself; `geo.search.pick` reads a payload by its kind. `map.search.result-highlight` draws
  through the published engine's `setSearchHighlight`, over a glyph set holding a point or none, and
  reads every constant back through what the engine draws for a way's line, a way's extent and a
  place. C34's matrix inputs state their activity, which the data-geojson runner reads as written.
  `geo.search.focus`, `geo.search.list-state` and `geo.search.pick` are `api: false` and run from
  `mountaya-geo`'s own tests. The search box is proven on the emulator as Compose UI tests, its engine
  on a scheduler the test moves, its pixels read under each scheme and its presses performed through
  its semantics, and the highlight on MapLibre's own map, with no network.
- **The family 8f fixtures are read at their edge too.** A stretch a fixture names is a `ProfileClass` whose
  word is its name; `geo.profile.curve-placement`'s places stand at their distance with no geometry, `pin` read
  as `isPinned` and a `cutoffAt` as the details' timing, written back as `Instant.toString()`, RFC 3339 in UTC;
  `geo.profile.scrub-grid`'s runs are folded from the grid's columns by the runner. `geo.profile.segment-rows`
  hands a segment's `metrics` as `stated` under its id; the card is read through a scope whose `amenities` and
  `waypointCard` words are their own paths, a text matching a path written `{word}`; a table's `held` glyph set
  is built under the case's name with placeholder markup through `GlyphSet.of`; the filter's rows carry their
  place's index as their own; a caller's formatter is stated by what it answers. `map.profile.cursor-marker` and
  `map.profile.emphasis` drive a real `MapEngine` through a double that hands each write on and records the ones
  the engine took, so a refused edit writes nothing: an element's `style` takes effect at once where its cursor
  stands, `leave` and `unmount` both hide, `remove` and `pin` write to the engine directly, `pin` with the glyph
  `map-pin`, and the emphasis slot's colour is read null where it is the slot's own, read off an empty slot
  first. `geo.profile.window-clamp` holds `minSpanMeters` to `MIN_RANGE_SPAN_METERS`, which `clampRange` states,
  that constant's one home in the contract.
  The nine `api: false` capabilities, `window-spans`, `stat-strip`, `cursor-readout`, `section-chips`,
  `amenity-stacks`, `segment-table-cells`, `waypoint-table-cells`, `amenity-filter` and `row-match`, run from
  `mountaya-geo`'s own tests, through rules a composable of `mountaya-elements` reads and so publishes; the
  conformance module and `mountaya-geo` each read the profile fixtures through readers of their own, since a
  module's tests cannot reach another's. The data elements are proven on the emulator as Compose UI tests,
  their pixels read under each scheme and their presses, hovers and adjustments performed through their
  semantics, over a cache answering at once, with no network.
- **The embed fixtures are read at their edge too.** `geo/organization-watermark.json` hands a read and a
  language, and a `page`, the browser document a map is drawn in: every case runs `watermarkOf`, its `page` handed
  to the rule's internal overload as a `WatermarkPage`, null where the case names none, as `sdk-swift`'s runner hands
  it, so no case is answered from its own expectation, and `firstPartyOrigins` is held to the origins the rule omits
  the mark on. `map/organization-read.json` plays its maps through the published engine, each client of the case's
  own REST origin, so no case reads another's kept answer, a `timeout` letting the test's clock run past the
  client's own budget; `map/readout-open.json` plays a map drawn on a `ScriptedMapSurface`, the readout's engine over
  a reader holding each tile until the script settles it handed to it as `MapView` hands one, each step through the
  engine's own entry point the renderer calls, a `press` as `longClicked` with its zoom, a `tap` as the surface's
  click and a `move` as `cameraMoveStarted`, so the runner closes nothing itself, and every press is held to a
  `MapEvent.LongClick` told to the map's listeners; it numbers the reads in the order they leave,
  `elevationTilesAt`'s, a pixel grid packed as `decodeElevation` reads a pixel, as
  `geo/elevation-sample.json`'s and `geo/elevation-decode.json`'s are. The watermark's `timed-out` read is handed to
  the rule as `OrganizationRead.TimedOut`, the standing a read overdue holds.
- **The locale is pinned per case.** The locale and unit-formatting runners build an explicit
  locale from each case and never read the default, since locale data differs by JDK, by Android
  release and by host. The cases of `geo.format.locale-services` expect strings every platform's
  locale data writes alike, Nepali's digits and date order among them (`ne-NP` writes 1531 as
  `१,५३१` and 14 March 2026 as `२०२६ मार्च १४`), so the jvm, the iOS simulator and an Android
  emulator each answer every case with their own data, an emulator's through `android.icu` (§8). A
  case a target's data wrote otherwise would fail there and pass nowhere by a workaround: no digit
  is ever assembled to match. An emulator leaves the device's 12- or 24-hour setting unset, so the
  cases cannot show what a device set to either does to a time; `mountaya-geo`'s device test
  `IcuDateFormatTest` sets the preference the platform's `java.text` reads, as the framework sets it
  from that setting, and holds a time to the locale's clock under both.
- **The report** is `schemas/sdk/reports/kotlin.json`, carrying `capabilities`, `modelDigest`,
  `published`, `registryVersion` and `sdk`, a `pass`, `fail` or `scoped` per capability, the shape
  `sdk/SPEC.md` §11 states and `PARITY.md` is generated from. A report keyed on another registry
  version counts as no report at all, and one whose `modelDigest` is not the digest of the contract's
  files fails its gate: the merge takes the digest of the checkout's files as the contract defines it,
  and refuses to write a report where the printed `MODEL_DIGEST` names other files, since its ticks
  were earned by a surface printed from them. It is written on request, by `mise run
  conformance:report`, and by nothing else: a plain test run writes nothing into another repository.
- **Where it runs.** The report comes from the jvm target and the host JVM's local unit tests,
  where every entry this SDK does not scope runs. The voice's speech on the platform and the audio
  focus it takes, and the map's renderer on a live map, its request hook and its composable, run as
  instrumented tests on an emulator, since the platform's speech engine and MapLibre's native library
  run nowhere else: the gate compiles every module's device tests
  and packages each module's into the APK a device installs, past every check an AAR it carries asks
  for, and `mise run test:device` runs them, under AndroidX's instrumentation runner, a run whose
  report names no test failing, since AGP's test engine passes one whose APK never installed. Beside
  them it runs Trail's journey, `examples/trail/src/androidTest`, the one `sdk/trail.yaml` states for
  every native SDK: a Compose UI test under the same runner that mounts Trail's root with no setting,
  so the app shows its notice and holds the demo itinerary, switches Replay on at the journey's 100 m/s,
  and holds its checks in order, the notice, the profile, an instruction in the voice's wording, a
  waypoint's instruction naming one of the journey's waypoints, the arrival within 30 seconds of the
  press on Start, and Stop showing Start again; `checkConsumer` compiles and packages it, and
  `verifyDevice` runs it through `checkTrailJourney`, held to its report as each module's run is. Everything
  else of `mountaya-navigation`, the device's engine over Geo's machine, its guidance, the plan, the
  prompts, the location sources and the replay, runs in the gate as the android target's host tests,
  each the twin of an `sdk-swift` host test, a route walked fix by fix where Geo's own
  `positionAtMeters` places each position along it. The
  map's state, its engine and both `map` rules are common code and run in the gate on the jvm and
  the iOS simulator, each the twin of an `sdk-swift` host test, and the renderer's device tests are
  the twins of `sdk-swift`'s simulator tests, every request MapLibre makes answered by an
  interceptor of the app's own client, so none reaches the network, but for the request hook's, whose
  network interceptor runs only on a connection: those reach a Tile API on the device's loopback
  address, over plain HTTP the device tests' own manifest permits, which the library's never does,
  MapLibre's own requests and two live maps among them. The request hook loads no native library to
  sign a request, so it runs in the gate too, as `mountaya-elements`' android host tests: the hook's
  own client, over MapLibre's defaults and never handed to MapLibre's slot, sends each request to a
  server on the loopback address, so each hop is signed on the network as on a device, a redirect
  among them.
- **The sibling checkout is a declared task input**, so Gradle never replays a green result
  against fixtures that moved under it.
- **A fixture is never edited to make a test pass.** A disagreement is a defect here or a defect
  in the contract, and the second is fixed in `schemas` under a new tag.

The sibling checkout is tolerated as absent locally and refused in CI, so a contributor builds
without it and a green CI run always means the fixtures ran.

## 10. Release

- **One version across the repository**, every module published together as `com.mountaya:*`, five
  of them: `mountaya-client`, `mountaya-admin`, `mountaya-geo`, `mountaya-elements` and
  `mountaya-navigation`, which `scripts/release.sh` names in that order, each after every module it
  depends on, and which one Central Portal deployment publishes together.
  The version is the catalog's `mountaya`, and the group `com.mountaya`, both set by the published
  module's convention plugin, which applies `maven-publish` and `signing`, declares each module's
  publications, and declares one repository, the release repository, which a release alone
  publishes into (below).
- **Gradle module metadata is published**, so a Kotlin Multiplatform consumer resolves the variant
  for each of its own targets from one coordinate.
- **No XCFramework.** The iOS targets are klibs for Kotlin code; a Swift app takes `sdk-swift`
  (§1).
- **Internal dependencies are exact** (§2).
- **`SCHEMA_VERSION` and `MODEL_DIGEST` are generated**, and a test compares the first with the
  sibling checkout's `VERSION` and the second with the digest of its files.
- **MapLibre is a range**, because a consumer's MapLibre is theirs: `[13.7.1,14.0)`, bounded within
  the major this SDK is tested on, never strict, and bounded rather than open because Maven's
  `latest` and `release` fields name a pre-release of another numbering, the `-globe` builds, which
  an open range or `latest.release` would take. Under Gradle's conflict resolution the range's upper
  bound states what this SDK was tested within rather than refusing a later version, so an app that
  names a later MapLibre itself gets it, and with it a version this SDK was not tested against; that
  is the app's choice, where a strict constraint would fail its build. MapLibre names OkHttp 4, and
  an Android app carrying `mountaya-client` resolves the client's OkHttp 5 for it, which the request
  hook's device tests run on. The range is also why the app-size gate holds `org.maplibre`'s share apart
  from the SDK's and compares it only at the release its baseline records (§7): a MapLibre release within the
  range moves the share on a commit that changed nothing, so the gate reports it beside both versions, and does not
  compare it, until `MOUNTAYA_SIZE_BASELINE=write mise run size:check` records it at the new release.
- **Navigation links no third-party library**, so no navigation library's release is ever a change
  a consumer sees, and an app that navigates adds no build setting: no module asks an app for core
  library desugaring or for any other option of its build. The navigation fixtures, the engine's host
  tests and the ABI baseline are what prove the engine.
- **A break in the ABI baseline needs a major.** Below 1.0 the contract's own rule applies: a
  minor carries what a major carries later, and nothing is deprecated before it is removed. From
  1.0 a contract operation is retired through a deprecation the generator prints (§3), never
  renamed in place.
- **Third-party licences travel with what they cover.** The glyph sets `mountaya-geo` publishes
  hold the markup of Lucide's icons, under the ISC licence and, for the icons Lucide derives from
  Feather, the MIT licence. `THIRD-PARTY-NOTICES.txt` at the root carries both texts, printed from the
  contract's notice with the glyph sets and held by `generate:check`; each printed glyph-set file
  carries them as its opening comment; and every jar and AAR `mountaya-geo` publishes packs the
  notices at `META-INF/mountaya-geo/THIRD-PARTY-NOTICES.txt`, a path no other library's notices take,
  which `checkPublications` opens each archive for. A klib has no `META-INF`, so the iOS variants
  carry the texts in their sources jar, as the printed files' comments. The README links the
  notices, and `checkLicence` holds the file at the root with both licences, and the link. The
  other modules carry no third-party code.
- **README snippets compile** against the real modules, in the outside-in build, because a
  snippet is the first code a consumer copies. The TypeScript SDK gates its README the same way.
  Every fence tagged `kotlin` is printed into the consumer's
  `src/commonMain/kotlin/com/example/consumer/readme/ReadmeSnippets.kt`, each into a suspending
  function of its own, its imports gathered at the top, so a snippet is common code a Kotlin
  Multiplatform app pastes and compiles on every target the gate builds; a hand-written `Prelude.kt`
  beside it declares what the snippets name without declaring, an app's own scope and its backend's
  session endpoint. A fence that cannot compile opts out with `<!-- kotlin:skip <reason> -->` on
  the line above it, the reason required, and the printed file lists every fence that opted out.
  `checkReadmeSnippets`, in `verify`, fails where the printed file differs from what the README
  prints, and `printReadmeSnippets` (`mise run readme`) prints it again after a change. The check
  also holds the README's floors table, its one prose copy of each floor (§2), to the version
  catalog's figures, reading each floor in the forms the skill's floors are read in (below), `Kotlin
  <kotlin-floor> or newer`, `Java <jvm-target> or newer`, `minSdk <android-minSdk>` or `from API level
  <android-minSdk>` and `compileSdk <android-compileSdk>` or `against API level <android-compileSdk>`:
  it fails on no `| Floor | Version |` table or two, on a floor stated on no row of the table or on
  two, on a row stating another figure than the catalog's, and on a floor stated on any line outside
  the table, each problem listed beside the snippets' own.
- **The developer skill's snippets compile too**, since an agent pastes them as a consumer pastes the
  README's. The skill is `skills/mountaya-sdk-kotlin` of the `ai` repository, read from the checkout
  beside this one at build time, or the one `-Pmountaya.aiDir=<path>` names, a draft of the skill's
  among them, and never from a copy: `SKILL.md` first, then every other document below it by path.
  `checkSkillSnippets`, in `verify` and ahead of `checkConsumer`, reads each fence as the README's
  are, a `<!-- kotlin:skip <reason> -->` marker honoured, and sorts it by its imports: into Android's
  code where one names a package Android alone has (`android.`, `androidx.`, `org.maplibre.`,
  `okhttp3.`) or a declaration the android dumps alone declare, `MapView` or `DeviceNavigationEngine`,
  into the jvm's where one names a declaration the jvm dumps alone declare, `constructWebhookEvent`,
  or a package of the JVM's, and into common code otherwise; a fence that needs both the jvm alone and
  Android compiles nowhere and fails. Each source set's fences are printed into the consumer's
  generated sources under `build/generated/skill`, in the `readme` package, whose `Prelude.kt` the
  skill's snippets stand on too, each into a function of its own named after its number among the
  skill's fences, a composable's body where an Android fence imports Compose or a composable and a
  suspending function's otherwise, and the consumer compiles them on every target the gate builds; a
  compiler's error names the printed file, whose comment above each function names the document and
  the line the fence opens on. Nothing printed is committed. The task also holds the floors the skill
  states to the version catalog's, each stated and every statement of one naming the catalog's
  figure: the Kotlin floor (`Kotlin <kotlin-floor> or newer`), the Java one (`Java <jvm-target> or
  newer`), minSdk (`from API level <android-minSdk>`, or `minSdk <android-minSdk>`) and compileSdk
  (`against API level <android-compileSdk>`, or `compileSdk <android-compileSdk>`), each figure read
  from the catalog and never written here, by the one reading the README's floors table is held by. An absent checkout prints nothing and is a note on a contributor's machine, and fails the run
  under CI, where `CI` is set, which fetches `ai` at the commit `siblings.lock` pins.
- **The licence is the Mountaya SDK License 1.0**, the text the TypeScript packages ship, in
  English and French at the repository root and named by every module's published metadata. It is
  carried verbatim and never edited for house style. Each publication's POM names it by its
  address, Mountaya's own page of the English text (`LicenceCheck.URL`), with its distribution and
  a comment naming both texts and the page of the French one. `checkLicence`, in `verify`, holds
  both texts at the root, the README linking each, every POM naming the licence, its address and
  both texts, and each text byte for byte the TypeScript packages' own where that checkout is
  beside this one, as `sdk-swift`'s licence tests do. A POM is never published again, so a release
  refuses an address that does not answer.
- **A consumer resolves the modules from Maven Central, under the `com.mountaya` namespace.** A
  Gradle build resolves a module only from a Maven repository it can reach, not from the
  repository's source, whose pages on Entire are public but whose clone takes an account, so each
  release is published there as signed publications through the Central Portal, and the iOS klibs with them, which a Kotlin
  Multiplatform consumer resolves from the same coordinate. Every POM states what Maven Central
  requires: its name, its description and the project's page, the licence by its address, Mountaya
  as its developer, and the repository's public, read-only mirror on GitHub,
  `https://github.com/mountaya/sdk-kotlin`, as its source repository, which Central requires and
  its page links; the repository itself is on Entire, where a contributor clones it, and
  `.depot/workflows/mirror.yml`, which `ai` renders, pushes every push of `main` and every `v*` tag
  from there to the mirror, which nothing pushes to by hand. `checkPublications`, in `verify`,
  holds every POM to it. Beside
  each artifact are its sources jar and a javadoc jar of its own publication's, which holds a README
  naming where the API is documented, the KDoc in the sources jar: a Kotlin module has no Javadoc,
  and Central takes such a jar in its place, as it does from the kotlinx libraries. Until the first
  release, a consumer depends on a checkout through a composite build. `sdk-swift` is served from a
  public mirror of its own repository instead, which SwiftPM clones.
- **A release is `mise run release <version>`, run by a maintainer on a Mac.** The whole gate
  runs there and nowhere else: the iOS simulator suites need macOS, and the device tests an
  emulator. `scripts/release.sh` refuses before it changes anything, every refusal told together
  with what to do: no git; a version that is not MAJOR.MINOR.PATCH, is already tagged here or on
  Entire, or is not newer than every tag and every version Maven Central holds; a version that is
  not the catalog's `mountaya` and the README's install lines; a working tree with changes, a
  commit other than the one Entire's `main` is at, or no `origin` remote; a changelog with nothing
  to say for it; a sibling that is not clean at the commit `siblings.lock` pins, which
  `sh .agents/siblings at-pins` names; licence texts that are not the TypeScript packages' bytes or
  a licence address that does not answer; no macOS or no emulator; no signing key, no passphrase
  for it, or no Central Portal token. Then it runs the gate: `mise run disk:check` on its own,
  since it passes under CI without looking, then, under `CI=true`, `mise run ci`, `mise run
  test:device` and the ABI check, every target built. It publishes
  every module, signed, into the release repository, `build/release/maven`, which
  `checkReleaseRepository` holds to what the Portal validates (each module with its Gradle module
  metadata and no other version, every POM as above, a sources and a javadoc jar beside every
  artifact, an MD5 and a SHA-1 checksum and a signature beside every file), and `centralBundle` zips
  it, leaving out what the Portal never reads; a key that does not sign stops the release there,
  before it asks. Then it moves the changelog's notes under the version and its date in a commit
  holding that alone, unless the section is there already, tags `v<version>`, uploads the bundle
  as a deployment of the publishing type the Portal leaves to a person (`scripts/central-upload.sh`,
  which waits until the Portal has validated it), pushes `main` at that commit and the tag to
  Entire in one atomic push, and waits, ten minutes at most, until the mirror lists the tag; where
  it never does, it says the release is done and names `mirror.yml`'s runs to read. Publishing the
  deployment to Maven Central is the maintainer's, and so is resuming a release that stopped after
  its tag, which starts by looking for its deployment in the Portal, so a validated one is never
  uploaded twice. The key is held in memory from `MOUNTAYA_SIGNING_KEY`, an
  ASCII-armoured secret key whose primary key signs, with `MOUNTAYA_SIGNING_PASSWORD` and, for a
  signing subkey, `MOUNTAYA_SIGNING_KEY_ID`; the token is `MOUNTAYA_CENTRAL_USERNAME` and
  `MOUNTAYA_CENTRAL_PASSWORD`. `--dry-run` runs every check but git's, and the gate, then publishes
  into a local Maven repository a consumer can resolve from, unsigned where no key is given; it
  touches no git and uploads nothing, and `--skip-gate` leaves its gate out. A first release needs, once: the `com.mountaya` namespace
  verified on the Central Portal, by a DNS TXT record on mountaya.com; the signing key's public half
  on a key server the Portal reads; and a Portal user token.
- **The changelog records the SDK's version, not the contract's.** `CHANGELOG.md` follows Keep a
  Changelog: `## [Unreleased]` first, holding what the next release carries, then each release as
  `## [MAJOR.MINOR.PATCH] - YYYY-MM-DD`, newest first, the shape `sdk-swift`'s changelog takes.
  `checkChangelog`, in `verify`, holds that shape, and every name the changelog writes in backticks
  to a module, its coordinate, or a declaration the committed ABI dumps publish, as the TypeScript
  packages' changelog gate holds theirs to what they export. A jvm or android dump names a property
  by its getter, which reads the same for `localMountayaScope` as for `LocalMountayaScope`, so a
  getter answering a `CompositionLocal` publishes its property under the UpperCamelCase spelling
  the naming law gives it (section 5) as well as under the lowerCamelCase one every getter does.
- **The release builds the iOS klibs on the Mac that ran the gate.** JetBrains made producing
  Apple klibs on any host Kotlin/Native supports, a Linux x86_64 one included, stable and on by
  default in Kotlin 2.2.20, but where the library or a module it depends on has cinterop
  dependencies. No module here declares a cinterop: the iOS sources reach C and Objective-C through
  the platform libraries Kotlin/Native ships, which its Linux distribution carries for the Apple
  targets too, and the cinterop klibs inside Ktor's published iOS artifacts are built already. A
  klib records the path of every source it is compiled from, so the multiplatform convention
  records each relative to the repository root (`-Xklib-relative-path-base`): a published klib
  names no directory of the machine that built it, and `checkReleaseRepository` refuses any
  published file, or archive inside one, that names the building machine's checkout or home. With
  that, a Linux x86_64 host builds the iOS klibs a Mac builds, byte for byte. One host publishes
  every artifact of a release, since Maven Central refuses a version published twice, and the host
  that runs the whole gate is a Mac, so the release publishes from there.

## 11. Deliberate divergences

Departures from `sdk/SPEC.md`, and nothing else. `PARITY.md` reads a ➖ in this SDK's column as a
sentence here.

What the contract states as every SDK's is no divergence, and is written where this SDK does it: a
class of the ladder ending in `Exception` (`sdk/SPEC.md` §3, here §4), `constructWebhookEvent`'s
`now`, the clock a caller may state (`sdk/SPEC.md` §3, here §4), the secret key and webhook
verification on the jvm alone, which runs servers, command-line tools and desktop processes and no
app build (`sdk/SPEC.md` §1, here §4), a declared timestamp the anchor refuses failing the decode
(`sdk/SPEC.md` §4, here §4), what MapLibre's own requests carry and do not (`sdk/SPEC.md` §1, here
§8), MapLibre Native's zoom window of 0 to 25.5 (`sdk/SPEC.md` §15, here §8), and a hot path's budget
test named in Kotlin's identifier form, `hotPathBudget…` (`sdk/SPEC.md` §10, here §7).

- **No studio layer.** The studio is TypeScript-only (§1), so every `studio.*` capability is
  `scoped`.
- **No browser refusal for the secret key.** This SDK publishes no browser target, so
  `transport.browser.secret-key` is `scoped`. The app-platform refusal stands in its place (§4):
  the secret key can be constructed on the jvm target alone, a compile-time barrier on Android
  and iOS. The prefix check and the server-only guidance remain.
- **`select` narrows the document and not the result type.** Kotlin has no mapped types. The
  generated result type is returned whole, its fields nullable, and a field the caller did not
  request is `null`.
- **`mountaya-geo` depends on `mountaya-client` with `api`.** Kotlin has no type-only dependency,
  so the "never calls the network" promise is held by §7 rather than by the graph.
- **A refusal and a failure of the map's own requests as MapLibre Android reports them.** The request
  hook hears every answer of 400 up and every request that reached no answer, at the address MapLibre
  asked for, so a failure with no answer is told as a `Failure` carrying no status, where MapLibre iOS
  reports none to `sdk-swift`; a request MapLibre cancelled is told to nobody. The renderer names a
  refusal's source by the map's own tile templates alone, so a base map's refused tile names no source
  and stands until the next style loads, as in `sdk-swift`.
- **A feature's id from a GeoJSON source fetched by its address is the text MapLibre Android
  writes.** MapLibre Android answers every feature's id as text, its number written out, so a vector
  tile's id, which the tile format makes a whole number, is read back as that number, and a GeoJSON
  source's whose data the style's document holds inline is read back as the document writes it, a
  number or a text. One whose data the style names by an address, which MapLibre fetches and never
  hands back, stays text, where `map.query.feature-shape` answers the id as the source gives it.
- **Live maps share MapLibre's one HTTP client.** MapLibre's HTTP client is process-wide, where the
  TypeScript SDK lets each map override its own, so every live map holds the request hook at once and
  a request is told apart by its address alone: each map's credential goes to its own client's Tile
  API, but two maps whose clients reach one Tile API with two credentials send the credential of the
  map attached last, the `transformRequest` of the map attached last that set one rewrites every
  map's requests, and the `okHttpClient` of the map attached last carries them, as `sdk-swift`'s and
  `sdk-dart`'s hooks share their platform's slot. An answer is told to every live map that claims its
  address, its sources' tile templates or its style's directory (§8), and one no map claims, a font
  another host serves or a tile outside the style's directory, to the one live map where one alone is
  live and to nobody where several are. A map showing a shared resource under a different credential
  on the same Tile API puts `share_token` in its tile URLs, which the Tile API accepts, as a query
  credential the contract otherwise sends as a header.
