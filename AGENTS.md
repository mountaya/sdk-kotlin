# AGENTS.md

`sdk-kotlin` is Mountaya's Kotlin SDK, under construction. What is here is the Gradle build, the
gates, the conformance harness, the printer of the generated surface with the surface it printed,
and the five modules a consumer depends on, `com.mountaya:mountaya-client`, `com.mountaya:mountaya-admin`,
`com.mountaya:mountaya-geo`, `com.mountaya:mountaya-elements` and `com.mountaya:mountaya-navigation`,
declared with their coordinates and their licence, and released together to Maven Central by
`mise run release`, whose first release is 0.1.0. It is written against
`schemas` at contract v0.9.0. `mountaya-client`
is `Mountaya` over published seams: the exchange every attempt leaves through, `SystemExchange` by
default (Ktor's client over OkHttp on the jvm and android, over URLSession on iOS), the wall clock
and the random source, with every transport capability but the browser's secret key, which is
scoped: the retries and their budgets, idempotency keys and conditional writes, the error ladder
and what an error carries, an error's message ending on the next step its status names, rate
limits, the session engine, the pagination walk and event streams, an editor stream read as its
member's user, the one its session token names or else the one `readingAsUser` names: the core,
every surface but the contract's admin unit. `mountaya-admin` is that admin unit, as `sdk/model.json`'s
`unit` splits it: the REST API's `admin` group, `postSession` among it, declared on the core's client
as `mountaya.admin` once `com.mountaya.admin.admin` is imported, the components only those and the
webhook events reach, printed into `AdminSchema`, and, on the jvm alone, as the secret key is,
`constructWebhookEvent`, which verifies a webhook delivery and reads it into the printed
`WebhookEvent`, on a server and never in an app; it reaches the core's transport through the seam
`InternalMountayaApi` marks, and an app that draws a map, edits an itinerary, queries the Data API or
navigates declares none of it. `mountaya-geo` holds every geo rule
the contract carries, navigation's among them, with the navigation vocabulary, the
`NavigationEngine` interface, the machine every engine follows a route on, published over a route
source, a scripted engine for an app's tests, and the plan an engine that speaks announces a route's
maneuvers from and the prompt due at a reading, `guidancePlanOf` and `duePrompt`, the segment groups with their road access, a
stretch's road access kind and a route's restricted stretches as features, the map's paint and an
itinerary's stroke, a point of interest's style and category, and an itinerary read by id through
`ItineraryResourceEngine` over one cache per client, with its legs and its places, and the elements'
data the contract's tables hold, printed into it:
the words in English and French, the colour tokens, and the marker and control glyph sets with
Mountaya's own drawings, beside the glyph sets a host builds and chains and how each glyph is
painted, and what the map's controls and panels stand on: the elements' scope nested by `scopeOf`, a
word counted or filled, a token read under a `ThemeScheme`, the zoom window's ends, the layers,
points-of-interest and preferences pickers' models, the time a layers panel pins, its slider's track, ticks,
release and readout and the addresses a stamped overlay is asked at, none reading a clock, and the two engines over a map,
`LayersSelectionEngine` and `GeolocateControlEngine`, and the search: `SearchEngine` over the fetch
an app hands it, in the caller's scope, with what a result list shows, what a pick does to a map and a
result's second line, and what the data elements stand on: a profile's chart in its 100-unit square, its
strip, the card at its cursor and its chips, the rows and cells of the segment and waypoint tables, the
filter over a place's services, a place's card, an altitude band in the language's own range and the
classification colours, and what a map draws for its organization and at a point: the watermark
`watermarkOf` places, the one thing a map reads off its organization, in the five states `OrganizationRead` holds, and
the extent of its frame a host keeps clear, `watermarkWidth`, `watermarkHeight`, `watermarkInset` and
`watermarkClearance`, and whether it is kept clear at a read, `isWatermarkKeptClear`, whether a search box shows its list, `isSearchListShown`, the readout's rows `readoutRows` states, a panel's layout `panelLayoutOf`
reads off the printed `ElementLayout`, and the terrain at a point read off the elevation tiles,
`decodeElevation`, `elevationTilesAt`, `terrainFromElevationTiles` and `aspectCardinal`. Each passes its fixture on the jvm, where the
report comes from, on the iOS simulator and on an Android emulator. `mountaya-navigation`, Android's alone, is
`DeviceNavigationEngine`, the contract's navigation on the device: Geo's machine with the guidance
that says each maneuver where Geo's `duePrompt` finds it due, linking no navigation library, with the
seams it takes: the device's location source, at a navigation's rate or a walker's, a replay's, a
flow's and an inert one, and the platform's speech and an inert voice, the device's two telling each
failure to the `onError` an app hands as a `LocationException` or a `SpeechException` of an open kind.
Its host tests run in the gate, the engine's among them; its device tests, the platform's speech, the
audio focus the voice takes and the location source reading the app's merged manifest, are compiled
by the gate and run only on an emulator.
`mountaya-elements` is the map: its state and its
engine, `MapEngine`, with the one layer registry and every `map` rule, the read of its organization
once per publishable key and REST origin for the process, `loadOrganization`, and where that read stands for a host
to read, `organizationRead`, and the readout's engine over the
elevation tiles it reads through the client's tile transport, the base map by its terrain
and variant, content drawn in slots by id and by name, each drawing its latest request and withdrawn
by cancelling its coroutine, the layers catalogue, overlays and enrichments, the overlay the server stamps
drawn for the instant pinned, layers and concepts shown
and hidden, an itinerary's restricted stretches, Data API results drawn under ids of their own and
edited, three geometry slots, the markers as pins with their glyph, the camera's pan
lock and zoom window, its moves, its fits and its locate over a geolocation seam, the point queries,
the events, the refusals of access that stand, the credits and the label scope, a picked search result
drawn above everything else in the accent the caller hands, the cursor marker and the leg a data element
writes to the map beside it, and the editing engine
that places a waypoint a click at a time through the magnet, are common code that runs on every target,
the engine asking what draws it through an internal surface, which `ScriptedMapSurface` plays for a
test; and its android target alone holds the renderer, `MapView`, a composable over MapLibre Native
with its `readout` switch and its `onReady`, `onClick` and `onCameraChange` handlers, which draws the
Mountaya watermark natively unless its organization holds white label, at the reader's top left of the map's safe
area, the window's own root insets, in either layout direction, keeping its controls, their panels, its readout and a
tapped marker's label inside the map's safe area and clear of the mark's frame, read off the map's real frame, which
the view publishes on its engine, and opens the readout at a long press, whose own requests carry the client's
credential through the interceptors on the `OkHttpClient` MapLibre fetches with, which hear their
failures too, which draws the waypoint and point-of-interest pins and the restricted stretches'
stripes its style names, its markers' pins from the engine's glyph set, and the content again on a
base map a switch brings in or the one shown where a switch fails, and which lists the map's credits,
Mountaya first, `DeviceGeolocation`, the device's
location services read without a prompt, `Glyph`, a glyph drawn in Compose, and the map's controls and
panels: `MapControls`, the stack in a corner of the zoom, layers, points-of-interest and preferences,
locate and compass controls, each flag a plain boolean with its fixed default, one panel open at a time,
each laid out as the embed lays out its own, the controls on their own with handlers standing
in for their defaults, `DownloadControl` and `DirectionsControl`, and `LayersPanel`, `PoiPanel` and
`PreferencesPanel`, the search box, `SearchControl`, which shows a search over the map's visible
area and marks a pick on the map, and the data elements, `ItineraryProfile`, `ItinerarySegmentTable`,
`ItineraryWaypointTable`, `ItinerarySparkline` and `WaypointCard`, which read an itinerary by id through
the app's `ItineraryResourceCache` and pin their cursor's marker and light the leg pointed at on the map
beside them, in the words of the scope `MountayaScope` nests and the colours of
`LocalMountayaTokens`, which the markers' accent takes too; the renderer's tests are device tests,
compiled by the gate and run only on an emulator, the controls', the search box's and the data elements'
as Compose UI tests reading pixels and semantics, but for the request hook's signing of a request, which
loads nothing native and runs in the gate as the android target's host tests too.
Every request the client makes names the SDK that made it, `X-Mountaya-Client: mountaya-kotlin/<version>`,
the version printed at build time from the catalog's `mountaya`, a Tile API request the map makes included.
The machine every navigation engine follows a route on, `RouteFollowingEngine`, tells an engine
built over it each state inline, `onChange`, before its `StateFlow` holds it. The consumer build in
`consumer/` builds against the modules' public surface alone, the README's snippets and the developer
skill's snippets read from the `ai` checkout with it; Trail, `examples/trail`, the one sample app
the contract's `sdk/trail.yaml` describes for every native SDK, an Android app on a map built
through R8 and its session server beside it, does too, and the public API is held to committed ABI
dumps, every declaration a consumer reaches carrying KDoc. Every capability has a runner or a scope, and every name the registry's `kotlin` column
records for one is declared as written, so the coverage gate passes (below).

**`SPEC.md` beside this file is what to build.** It is the specification for this SDK:
scope, the module graph, the generated surface, transport behaviour, the naming
law, the layout law, the boundary rules, the elements family and navigation,
conformance, release, and the divergences this SDK takes deliberately. Read it before
anything else here.

## The contract, and where it is not

**The contract is `mountaya/schemas`, and it is the contract for every SDK at once.** It is
read from the sibling checkout, `../schemas`, or from `-Pmountaya.schemasDir=<path>`.

- `openapi/api.yaml`, `openapi/tiles.yaml` and `graphql/data/*.graphqls` describe the
  SHAPES. `sdk/model.json` restates them as one language-neutral document, built there, and it
  is what this repository's generator reads. A generated file is never hand-edited: a wrong
  shape is a defect in the description.
- `sdk/SPEC.md`, `sdk/registry.yaml` and `sdk/fixtures/**.json` describe the
  BEHAVIOUR: when a request is repeated, how a session token is kept fresh, which
  failure maps to which class, how an elevation series is derived, which words a
  capability gate knows. Those are contract too, and no bundle can express them.

**`mountaya/sdk-typescript` is not the reference.** It is the first implementation of
the same contract. Writing this SDK against that one would make it a translation of a
TypeScript module list rather than an implementation of an agreement, which is the soft form of
the dependency the "100% native, no bridges" rule exists to forbid. Where a question is
not answered by `SPEC.md` here or by `sdk/SPEC.md` there, the answer is to settle it in
`schemas` and write it down, not to read it off another language.

**Parity is proven against JSON.** The `conformance` module runs each fixture's cases; the
registry and the fixtures are embedded into it at build time, so the same harness runs on the
jvm, on the iOS simulator and on an Android emulator.

**`sdk-swift`'s `CONTRACT-GAPS.md` is the list of what the contract leaves open**, for every
SDK, with this SDK's answer in each row; this repository keeps none of its own. A question the
fixtures do not answer becomes a row there, with what each SDK does until the contract answers it.
It never becomes a guess presented as the contract.

## Commands

`mise.toml` pins what runs the build (the JDK, Gradle, the Android SDK tools) and
`gradle/libs.versions.toml` pins what the build resolves (plugins, libraries, floors).

```bash
mise run ci                             # the gate: clean, the disk check, the toolchain check, the release scripts' test, Gradle's verify, every target, the coverage gate included
mise run ci -- -Pmountaya.targets=jvm   # the gate on the jvm alone, for bring-up
mise run clean                          # delete what this repository's builds left behind, inside it alone; `-- --all` the build directories too, `-- --dry-run` only lists
mise run clean:toolchains               # the person's, never an agent's: list the shared toolchains no checkout pins; `-- --yes` deletes them
mise run disk:check                     # refuse to start the gate where the disk lacks room for it
mise run toolchain:check                # refuse a JDK other than the pinned one under ./gradlew, and on macOS an Xcode other than the one mise.toml names
mise run ci:coverage                    # the coverage gate alone: the capabilities with no runner, the published names no dump declares
mise run size:check                     # the app-size gate alone: Trail's dex code, module by module, against its baseline
mise run test:device                    # the tests that need an Android emulator running: each module's and Trail's journey
mise run trail:session                  # Trail's session server on 127.0.0.1:8787, minting sessions with the keys in your shell
./gradlew checkTrail                    # the scenario gate alone: Trail's markers, keys and tokens under examples/, the itinerary's copy
./gradlew checkTrailJourney             # Trail's journey alone, on an emulator that is running; test:device runs it with the rest
mise run conformance                    # only the contract's fixtures
mise run conformance:report             # writes sdk/reports/kotlin.json in the contract checkout
mise run generate                       # reprint the generated surface from sdk/model.json, the elements' data from sdk/elements/
mise run generate:check                 # fail where either differs from what the contract's files print
mise run consumer                       # the consumer build alone, the README's snippets with it
mise run readme                         # print the README's Kotlin snippets into the consumer build
mise run snippets -- <file.md> [...]    # compile the Kotlin fences of any Markdown documents, for the jvm and Android; never a gate
mise run api:update                     # rewrite the ABI dumps after a deliberate API change
mise run wrapper                        # regenerate the wrapper after moving the Gradle pin
mise run version <X.Y.Z>                # write a release into the files: the catalog's mountaya, the README's install lines, CHANGELOG.md's dated section
mise run ci:local                       # the part of the gate CI cannot run: the toolchain check, test:device and the iOS simulator suites
mise run release <X.Y.Z>                # the person's: tag the pushed commit CI passed, publish to Maven Central, wait for the mirror; run again, it resumes
mise run release <X.Y.Z> --check        # every check of the release, the credentials' included, changing nothing
mise run release <X.Y.Z> --credentials  # the release's credentials alone, each checked online
mise run test:release                   # the fixture test of scripts/version.sh and scripts/release.sh, which the gate runs
```

- `-Pmountaya.targets` takes `jvm`, `ios` and `android`, comma-separated. A published module
  carries all of them; the switch is for working on one target at a time. `mountaya-navigation`
  and Trail are Android's alone, so the settings include them only where `android` is built;
  `mountaya-elements` is in every run, its renderer with its android target.
- A plain test run never writes into `schemas`. Only `conformance:report` does.
- **The gate cleans up inside the repository before it builds, and nowhere else.** `ci` starts with
  `clean`, which deletes what this repository's builds left behind and no longer use, every path
  inside it: Gradle's state under a `.gradle/` for a version the wrapper no longer names, and the
  build output of a directory that holds no build any more. What every checkout on this machine
  shares, a Gradle version's distribution and caches, a Kotlin/Native toolchain, an Android
  package, mise's installs and an emulator device, is `clean:toolchains`' to prune: it lists what no
  Mountaya checkout pins or uses and deletes only with `-- --yes`, never what a running process
  holds open, and it is the person's to run, never an agent's and never the gate's. `disk:check`
  then stops the gate, and `test:device`, before they start where the disk has less than 25 GB
  free for a first gate or 15 GB for a later one (`MOUNTAYA_MIN_FREE_GB` asks for another figure;
  CI is not checked), naming both cleans: a full disk stops a build part-way and every other
  program on the machine that writes. Never copy the repository to build it elsewhere: the copy
  builds its own `build/` directories.
- **The generated packages are printed, never edited.**
  `mountaya-client/src/commonMain/kotlin/com/mountaya/client/generated/`, the core's, and
  `mountaya-admin/src/commonMain/kotlin/com/mountaya/admin/generated/`, the admin unit's, are what
  `mise run generate` prints from the contract checkout's `sdk/model.json`, split by its `unit`, and
  `mountaya-geo/src/commonMain/kotlin/com/mountaya/geo/generated/` and `THIRD-PARTY-NOTICES.txt` at
  the root what it prints from `sdk/elements/` (the elements' words, tokens and glyph sets, and the
  licences of the glyph markup); `verify` fails where any differs. Every file the client printer
  writes names the contract tag and `modelDigest`, the contract's digest of `sdk/model.json` and the
  elements' tables (`sdk/SPEC.md` section 11), which `MODEL_DIGEST` publishes beside `SCHEMA_VERSION`,
  so a table that moved without a reprint fails the check too, and `conformance:report` writes the
  digest into the report, refusing where the printed one names other files. A wrong value there is fixed in
  `schemas`, then reprinted; a change to how it is printed is a change to
  `build-logic/src/main/kotlin/com/mountaya/build/generator/`, whose golden files, both printers' and
  both units', `MOUNTAYA_UPDATE_GOLDEN=1 ./gradlew -p build-logic test` rewrites.
- **The README's Kotlin snippets are compiled, and its floors are the catalog's.** Every
  ```` ```kotlin ```` fence of `README.md` is printed into
  `consumer/src/commonMain/kotlin/com/example/consumer/readme/ReadmeSnippets.kt`, and `verify` fails
  where the two differ. After changing one, `mise run readme` prints them again, and the diff is the
  review. A fence that cannot compile opts out with `<!-- kotlin:skip <reason> -->` on the line above
  it. The same check, `checkReadmeSnippets`, holds the floors table under the README's Install,
  `| Floor | Version |`, the one place this repository's prose writes a floor's number, to the version
  catalog: each floor on one row at the catalog's figure, and none stated on a line outside the table
  (`SPEC.md` sections 2 and 10). A floor moved in the catalog is moved in the table in the same change.
- **The developer skill's Kotlin snippets are compiled too, and never copied here.**
  `checkSkillSnippets`, in `verify` and ahead of `checkConsumer`, reads `skills/mountaya-sdk-kotlin`
  from the `ai` checkout beside this one, or the one `-Pmountaya.aiDir=<path>` names, sorts each fence
  by its imports into common, jvm or Android code, and prints them into the consumer's
  `build/generated/skill`, which the consumer compiles; the same skip marker opts a fence out, and the
  skill is fixed in `ai`, never here. It also fails where the skill states a floor, the Kotlin and
  Java ones, minSdk or compileSdk, other than the version catalog's, read as the README's table is
  read (`build-logic/.../readme/Floors.kt`). A missing `../ai` is a note locally and a failure under
  CI.
- **Any Markdown document's Kotlin fences compile on request, never in the gate.** `mise run snippets -- <file.md> [...]`
  (`scripts/snippets.sh`, then `printDocSnippets` and `checkDocSnippets`) reads only the paths it is handed, names no
  other repository, and prints each fence, a skip marker honoured, into a file of its own under the consumer's
  `build/generated/snippets`, never committed: sorted by its imports as the skill's are, a fence that reads `System` the
  jvm's and one that reads `BuildConfig` Android's, which `consumer/src/androidMain/.../snippets/BuildConfig.kt` stands in
  for; a fence of statements as a function's body and a fence of declarations as an object's members. The consumer
  compiles them for the jvm and Android, warnings as errors, `activity-compose` and `kotlinx-coroutines-test` beside them
  then alone, and the script names each failing fence by its document and line. A documentation site runs it over its
  pages; the gate's consumer build reads none of it.
- **Every hot path keeps a live budget test.** The contract's `sdk/hot-paths.yaml` lists Kotlin for nine
  paths, and `checkHotPathBudgets`, in `verify`, reads the entries listing `kotlin` and every test source set,
  the device tests' included, with the compiler's parser: each path needs a `@Test fun hotPathBudget<Id>…()`
  that asserts a count against a top-level `private const val <ID>_BUDGET` in its own file, beside a control
  that the work happened, and the gate fails on a path with none, an unlisted id, a test asserting nothing
  against its constant, a file not declaring it and an `@Ignore` on the test or a class around it. A
  composition is counted through the internal `RenderProbe` (`LocalMountayaRenderProbe`, null in an app), a
  measurement through `MeasureProbe`, Geo's, marked `InternalMountayaApi` so the replay's budget in
  `mountaya-navigation` holds it too; the budgets of a composition are device tests, compiled by
  the gate and run by `mise run test:device`. A budget's number is argued in review with the before and
  after counts of the change that sets it (`SPEC.md` section 7).
- **Trail is the app the gate builds, measures and walks.** `examples/trail` is a Gradle build of its own,
  included by the repository's settings where this build is the one run and android is built, which resolves
  the modules by their coordinates through this checkout as `consumer/` does: `checkConsumer` builds its
  release through R8, compiles and packages its journey and runs its session server's tests, and
  `checkTrail`, in `verify` among the source gates, holds it to the contract's `sdk/trail.yaml`: a `trail: <id>`
  marker for every step and none naming no step, no value under `examples/` shaped as a key or a session token
  in either shape `sdk/SPEC.md` gives one, `pk_`, `sk_` or `sess_` and 32 lowercase hex characters, or `sess_`, a
  base64url JSON payload, a `.` and a signature, the demo itinerary a byte-for-byte copy of
  `sdk/trail/itinerary.json`, and `examples/LICENSE` beside it, MIT, which `checkLicence` holds to
  sdk-typescript's. Its settings come from the environment or `examples/trail/.env`, never committed, as string
  resources the app reads with `getString`; `mise run trail:session` runs its session server, which mints the app's
  session tokens with the keys in the person's shell, and `mise run test:device` walks its journey on an emulator.
- **The app's size is held to a baseline.** `checkAppSize` (`mise run size:check`), in `verify` after
  `checkConsumer` wherever android is built, reads Trail's R8-minified release APK with the command-line
  tools' `apkanalyzer dex packages` through R8's mapping: one share per module's package and one per library
  the SDK brings, `io.ktor`, `kotlinx.serialization`, `okhttp3` and `org.maplibre`, dex alone, then the SDK's
  modules together and the whole dex code, never compared. A share grown past 2% of its baseline or 16 KiB,
  whichever is larger, fails; a shrink past it is a note; a missing baseline fails; one of another version of
  `sdk/trail.yaml`, Android Gradle plugin or Kotlin is reported, not compared, and so is a library's share where
  the version the app's release runtime classpath resolved it at, which the app's build writes, is not the one the
  baseline records. A library an SDK module brings that no share names, and the app does not reach through a
  dependency of its own, fails it, read off the graph of that classpath the app's build writes beside the
  versions. `MOUNTAYA_SIZE_BASELINE=write mise run size:check` records `examples/trail/size/android.json` after a
  deliberate change, after the change that moves the scenario's version or either pin, or for a library resolved
  at a new release. A share is where R8 put the code, not whose it is: R8 merges classes and moves methods across
  packages, so a brought library's share can hold merged SDK, Compose and app code, and a share can move on a
  change that touched none of its package (`SPEC.md` section 7); whose code an app pays for is traced through
  the mapping in review.
- **Every declaration a consumer reaches carries KDoc.** `lintDocumentation`, in `verify`, fails on a
  `public` or `protected` declaration outside the tests and the generated packages that has none, a
  constructor property, an enum entry and a companion object included and an `override` exempt; an
  exemption is keyed by the path the gate prints, with its sentence, in `documentationExemptions`.
- **The public API is held to a baseline.** Each published module's ABI dumps sit under its `api/`
  (the jvm, android and the klib of the iOS targets), and `check` fails on a change. After a
  deliberate change, `mise run api:update` rewrites them with every target built, and a line
  removed or changed is a break.
- **The consumer is a build of its own.** `consumer/` depends on the modules by their coordinates,
  through a composite build each side includes, so `verify` builds it as `checkConsumer` and
  `./gradlew -p consumer assemble` builds it alone. It reads `-Pmountaya.targets` as the modules
  do. Nothing is published, and nothing reads or writes `~/.m2`.
- **Android needs its packages once.** `mise run android:packages` installs the platform and
  build tools the build compiles against, and `mise run android:emulator` the emulator and
  system image `test:device` runs on, each at the exact version `mise.toml` writes. The
  tasks call the `android sdk` command, which asks nothing but records each package's licence as
  accepted in the SDK's `licenses/` directory: running either task accepts those licences, so
  both are the person's to run, as `sh .agents/setup` runs the first, and never an agent's. No build
  installs one in their place: each Android build's `gradle.properties`, the repository's, the
  consumer's and Trail's, sets `android.builder.sdkDownload=false`, so a package the SDK lacks fails
  the build rather than the Android Gradle plugin installing it with its licence.
- **A release is `mise run version <X.Y.Z>`, a commit CI passes, then `mise run release <X.Y.Z>`,
  and the last is the person's to run.** `mise run version` (`scripts/version.sh`) writes the
  version into the catalog's `mountaya` and the README's install lines, and moves `CHANGELOG.md`'s
  `[Unreleased]` notes under the version and today's date, and nothing else, so an agent may run
  it; run again it changes nothing. The person commits that as "version: Release v<X.Y.Z>" and
  pushes `main`, and CI runs the gate on it. `scripts/release.sh` reruns no gate CI ran, and
  refuses, each refusal with what to do and all of them told together: a version that is not
  MAJOR.MINOR.PATCH, not newer than every `v*` tag, or tagged elsewhere than this commit, here, on
  Entire or on the mirror; a dirty tree, off `main` or not at Entire's `main`; a commit CI has not
  finished green on; a version the catalog or the README's install lines do not name, or no dated
  `CHANGELOG.md` section for it; a sibling that is not clean at the commit `siblings.lock` pins
  (`sh .agents/siblings at-pins`); licence texts that are not sdk-typescript's bytes or a licence
  address that does not answer; and, until `mise run ci:local` passed on the commit, a host that
  is not macOS or no emulator running. Then it takes the credentials, each from the environment,
  else the macOS Keychain, else a hidden prompt, and each checked online: the key and its
  passphrase sign a test payload in a throwaway GnuPG home, and the Portal answers the token. It
  refuses a version Maven Central holds, unless the tag is on this commit already. Once asked, it
  runs `mise run disk:check` on its own, since it passes under CI without looking, and `CI=true`
  `mise run ci:local` once a commit: the toolchain check, `test:device` and the iOS simulator
  suites, which CI's Linux runner cannot run, though it builds every target and checks every ABI
  dump, the iOS klibs' among them. Before the tag it proves the publish: every module, signed, into
  `build/release/maven`, zipped (`centralBundle`, which `checkReleaseRepository` holds to what the
  Central Portal validates, and to naming no directory of this machine), and uploaded as a
  USER_MANAGED deployment the Portal validates (`scripts/central-upload.sh`). Then it tags
  `v<X.Y.Z>`, noted with the changelog's section, pushes the tag alone to Entire, publishes the
  deployment through the Portal API and waits until Maven Central holds it, and waits, ten minutes
  at most, until the GitHub mirror lists the tag, which `.depot/workflows/mirror.yml`, rendered by
  `ai`, pushes there from Entire. It makes no commit and pushes no `main`. Run again after a stop,
  it reads where it stands from Entire, the mirror and the Portal: `ci:local` is not run twice on
  one commit, the tag is kept, a deployment of the version the Portal is publishing is waited on,
  the bundle is uploaded again where none is, and nothing Maven Central holds is published again.
  `--check` runs every check and changes nothing; `--credentials` takes and checks the credentials
  alone. The key is `MOUNTAYA_SIGNING_KEY` (ASCII-armoured, with `MOUNTAYA_SIGNING_PASSWORD`, and
  `MOUNTAYA_SIGNING_KEY_ID` for a signing subkey), the token `MOUNTAYA_CENTRAL_USERNAME` and
  `MOUNTAYA_CENTRAL_PASSWORD`. `mise run test:release`, in `mise run ci`, holds both scripts to
  fakes of the Portal, gpg, depot and mise, in a scratch copy.
- **The changelog is gated.** `CHANGELOG.md` keeps `## [Unreleased]` first, holding what the next
  release carries, then each release as `## [MAJOR.MINOR.PATCH] - YYYY-MM-DD`, newest first;
  `checkChangelog`, in `verify`, holds that shape and every name it writes in backticks to a
  module, its coordinate, or a declaration the committed ABI dumps publish.

**The coverage gate passes.** `checkConformanceCoverage` lists the capability ids with no runner,
under the module the root build names for each, and the list is empty: every capability of the
registry has a runner, or a scope with its sentence in `SPEC.md` section 11. It also looks for every
name the registry's `kotlin` column records for a capability this SDK does not scope in the committed
ABI dumps, exactly as written, the declaration's path after its package (`ApiException`,
`MapPaint.standard`), never by package and never folding case (`SPEC.md` sections 5 and 9), and none
is missing; the column is `scoped` exactly where this SDK scopes the capability. The report `mise run
conformance:report` writes carries, for every capability
whose `kotlin` column is a list, the names of it the dumps declare. The gate runs inside `mise run
ci`, last, and `mise run ci:coverage` runs it alone; a capability the registry adds without a runner
here, or under a name no module declares, fails it again, and that list is then the work left.
`mise run ci` green,
with every conformance case of every runner passing on every target and every `studio.*` entry and
`transport.browser.secret-key` reported `scoped`, is what "green" means, and `mise run
test:device`, on an emulator, is the half the gate cannot run. At the contract's registry version
19, with its 262 capabilities, this SDK passes 226 on every target and scopes 36, the 35 `studio.*`
entries and `transport.browser.secret-key`, as `sdk-swift` and `sdk-dart` do. No failure is expected. Do not
silence the gate, and do not add a runner that proves nothing.

## Where things are

Every hand-written file of a module declares the module's one package, `com.mountaya.client`,
`com.mountaya.admin`, `com.mountaya.geo`, `com.mountaya.elements` or `com.mountaya.navigation`, and the harness's
`com.mountaya.conformance`, whatever directory it sits in: the directory names its topic and its
kind, `<topic>/<kind>/` below, and the package is the module's (`SPEC.md` section 6). An editor that
writes the directory's package fails `lintFileStructure`.

```
build-logic/                    convention plugins, the fixture embedding, the report merge
build-logic/.../contract/       the registry reader, the coverage gate, the scopes it holds to SPEC.md section 11
build-logic/.../generator/      the printer of the generated surface, and its golden files in tests
build-logic/gates/              the source gates, over the Kotlin compiler's own parser
build-logic/.../readme/         the README's, the skill's and any documents' snippet printers (`mise run snippets`), and the floors the first two are held to; build-logic/.../licence/ the licence gate
build-logic/.../publication/    what every POM states, the publication gate, the release repository and its check
build-logic/.../changelog/      the changelog gate
build-logic/.../size/           the app-size gate: apkanalyzer's output read package by package, the release graph's brought libraries, and its baseline's rule
build-logic/.../trail/          the scenario gate, checkTrail, which holds examples/trail to sdk/trail.yaml
build-logic/.../seams/          BuildClock, the one place the build's own tooling reads the wall clock
scripts/                        version.sh, behind `mise run version`; release.sh and central-upload.sh, behind `mise run release`; test-release.sh, their fixture test; check-toolchain.sh, behind `mise run toolchain:check`; lib/common.sh and lib/release.sh, rendered by `ai`
mountaya-client/src/<set>/kotlin/com/mountaya/client/<topic>/<kind>/
mountaya-client/.../generated/  the printed core, in com.mountaya.client.generated: RestSchema, TileSchema, DataSchema, the accessors
mountaya-admin/src/<set>/kotlin/com/mountaya/admin/<topic>/<kind>/        accessors (Mountaya.admin), webhooks, errors, seams
mountaya-admin/.../generated/   the printed admin unit, in com.mountaya.admin.generated: Admin, AdminSchema, WebhookEvent
mountaya-geo/src/commonMain/kotlin/com/mountaya/geo/<topic>/<kind>/
mountaya-geo/.../generated/     the elements' data printed from sdk/elements/, in com.mountaya.geo.generated: words, endonyms, tokens, glyph sets, artwork
mountaya-elements/src/commonMain/kotlin/com/mountaya/elements/<topic>/<kind>/   map, edit, seams
mountaya-elements/src/androidMain/kotlin/com/mountaya/elements/renderer/       the composables, the renderer, the hook
mountaya-elements/src/androidDeviceTest/                                      the renderer's tests, which load MapLibre
mountaya-elements/src/androidHostTest/                                        the request hook's tests on the host JVM, MapLibre left out
mountaya-navigation/src/androidMain/kotlin/com/mountaya/navigation/<topic>/<kind>/   seams, guidance, adapter
mountaya-navigation/src/androidHostTest/, androidDeviceTest/   host tests, the engine's included; the platform speech's tests
conformance/                    the harness, and the runners of the `api: true` entries
mountaya-*/api/                 the committed ABI dumps, the public API's baseline
consumer/                       the outside-in consumer build, the README's snippets in its `readme` package, the skill's printed under build/generated/skill, a document's under build/generated/snippets
examples/trail/                 Trail, the sample app of sdk/trail.yaml, an Android app built through R8, a build of its own; examples/LICENSE, MIT
examples/trail/session/         Trail's session server, a JVM module over mountaya-admin, `mise run trail:session`
examples/trail/src/androidTest/ Trail's journey, the device test sdk/trail.yaml describes
examples/trail/size/            the app-size gate's baseline, android.json
LICENSE.txt, LICENCE.fr.txt     the Mountaya SDK License 1.0, verbatim, the TypeScript packages' own
CHANGELOG.md                    what each release carries, `[Unreleased]` first
THIRD-PARTY-NOTICES.txt         the licences of the glyph markup mountaya-geo publishes, printed with it
```

Adding a capability is: the rule in its module, its mirrored test, and a runner named after the
capability (`TransportRetryBackoffRunner`) in the module `SPEC.md` section 9 names for its layer
and its `api` flag. The coverage gate then stops listing it.

## What is settled

- **The units are modules `mountaya-client`, `mountaya-admin`, `mountaya-geo`, `mountaya-navigation` and
  `mountaya-elements`.** `client`, `admin`, `geo` and `elements` are Kotlin Multiplatform with a jvm target,
  so a JVM consumer still needs no Android toolchain; `navigation` is Android, and so is the renderer of
  `elements`, its android source set, where MapLibre and Compose are. `sdk-swift`
  never links this SDK. Each is `com.mountaya:<module>`, at the catalog's one version, and a
  consumer resolves all five from Maven Central, where every release publishes them together,
  signed, through the Central Portal (`SPEC.md` section 10).
- **The client is two units, the core and the admin unit, as the contract declares.** `sdk/model.json`
  states the `unit` of every REST operation, webhook event and component, and the printer splits by it
  and by no list of its own: `mountaya-admin` holds the `admin` group, the webhook events and the
  components only they reach, in `AdminSchema`, naming the core's in full, and declares
  `public val Mountaya.admin: Admin`; the core declares no `admin` of its own and names nothing of the
  admin unit. The admin unit reaches the core's transport through `Mountaya.sender` and the few
  helpers a printed method calls, all `InternalMountayaApi`, which the core reads throughout under
  its own module-wide opt-in and every other module opts in to where it reads it (`SPEC.md` sections
  2, 3 and 7). A component the core's hand-written code names stays core by the contract's list,
  `CORE_SCHEMAS` in `schemas`, never by a list here.
- **The order is the code realigned to the contract, then transport, then the renderer-free geo
  layer, then the navigation engine and the elements.** The elements own the `map` layer's
  engines, so `map.*` is run here rather than scoped: each `api: true` entry from `conformance`
  through the public engine on `ScriptedMapSurface`, each `api: false` one from `mountaya-elements`'
  own tests, all on every target.
- **The map draws on MapLibre Native Android, `[13.7.1,14.0)`, through Compose.** Its requests
  carry the client's credential through a network interceptor on MapLibre's process-wide
  `OkHttpClient`, each hop of a redirect signed for its own address, beside an application interceptor
  that hears each answer once, both ahead of the app's own client where one is given, claimed by the
  client's Tile API base URL (`Mountaya.mapRequestHeaders`); a request it cannot sign fails with a
  `ConnectException` and never leaves the device. Every live map holds the hook at once, each
  credential going to its own client's Tile API, and two maps of one Tile API send the credential of
  the map attached last (`SPEC.md` section 11).
- **Navigation is contract.** Its rules are the `geo.navigation.*` capabilities and §14 of
  `sdk/SPEC.md`, which contract v0.9.0 carries. Geo's navigation rules, its vocabulary, the
  `NavigationEngine` interface, the machine, the scripted engine and the guidance's plan and prompt due
  are written against them, and
  `mountaya-navigation`'s engine stands on Geo's public API alone.
- **A webhook delivery is verified on a server alone, by the admin unit.** `constructWebhookEvent` is
  declared in `mountaya-admin`'s `jvmMain`, as the core's factory that takes a secret key is in the
  core's, so an Android or iOS build has nothing to call and the endpoint's signing secret never ships in
  an app; the printed `WebhookEvent` and `WebhookSignatureException` are the admin unit's common code,
  and the capability's runner sits in `conformance`'s jvm tests, which depend on `mountaya-admin` as a
  consumer does (`SPEC.md` sections 4 and 9).
- **`mountaya-navigation` links no navigation library.** `DeviceNavigationEngine` drives Geo's
  `RouteFollowingEngine` and Geo's guidance rules, `guidancePlanOf` and `duePrompt`, directly: the step
  a prompt belongs to, whether the traveller is off the route and when a prompt falls due are the
  contract's rules and this repository's Kotlin, and the voice is the platform's text-to-speech and
  audio focus. The boundary scan and the classpath walk refuse Ferrostar in every module, and no module
  asks an app for core library desugaring (SPEC.md sections 2, 7 and 10).
- **A module's public API is one package.** Every hand-written file declares its module's package,
  so a consumer imports one package per module, `import com.mountaya.client.Mountaya`, and never
  learns a topic or a kind; the directory names those for a contributor, and every gate reads it.
  The printed surfaces stay in `com.mountaya.client.generated`, `com.mountaya.admin.generated` and
  `com.mountaya.geo.generated`, and
  fourteen of Geo's tests keep their directory's package, each with its sentence (`SPEC.md` section 6).
- **The studio is TypeScript-only.** No studio module is written here.
- **An `operationId` is the method name, verbatim, in every language at once.** There is no rename
  table; the contract freezes an operation from 1.0, and below it a minor of the contract may
  rename one, which a regeneration then carries.
- **Every capability in `sdk/registry.yaml` needs a conformance runner here, or is scoped
  with its sentence in `SPEC.md` section 11.** `checkConformanceCoverage` holds each scope to the
  bold opening of a bullet in that section, so section 11 keeps its number and each scope bullet
  its opening.

## Key rules

- **Git is read-only, without exception.** `git diff`, `git log`, `git blame`, `git show`,
  `git status` only. Every mutating command is the user's.
- **Never hand-edit a generated file.** The next regeneration overwrites it, and the
  defect it was papering over is in `mountaya/schemas`.
- **Never edit a fixture to make a test pass.** A disagreement is a defect here or a
  defect in the contract, and the second one is fixed in `schemas` under a new tag.
- **Ask before deciding.** Where `SPEC.md` is silent, ask, then write the answer into `SPEC.md`.
  This repository ships to consumers who cannot be refactored.
- **No suppression without its sentence.** `@Suppress` is counted by a gate (`SPEC.md`
  section 7), each one valued by the sentence that justifies it.
- **Never accept a licence on the user's behalf.** Should a tool ask for one again, the answer is
  theirs to give.
- **A build never installs an Android SDK package.** `android.builder.sdkDownload=false` in each
  Android build's `gradle.properties` makes a missing platform or build tools fail the build; the
  person installs them with `mise run android:packages`, never an agent, and never a build.

<!-- mountaya-ai:tooling:begin -->
<!-- Generated by mountaya/ai (scripts/apply.mjs). Edit .agents/manifest.json instead. -->

## Agent tooling

`entire` and `mise` are required. Every other tool here is optional, and nothing in the repository fails without it.

| Tool | What it does here | Install | Without it |
| --- | --- | --- | --- |
| `entire` (required) | Hosts every Mountaya repository and records agent sessions as checkpoints linked to commits; its git hooks record every session. Its Graph plugin answers structural questions (where a symbol is defined, what calls it, what a change breaks); its Brain plugin holds the memory of past work. | CLI 0.12.2 or newer: `brew install --cask entireio/tap/entire` (Linux: `curl -fsSL https://entire.com/install.sh \| bash`), then `entire login`. Plugins: `entire plugin install brain` (0.4.0 or newer) and `entire plugin install graph` (0.6.0 or newer), then `entire brain setup --no-backfill --no-daemon`. For recall across repositories: `entire brain workspace create mountaya`, then `entire brain workspace add mountaya <path>` for each checkout | Nothing can be cloned or pushed, sessions are not recorded and the git hooks skip Entire's steps. Without the plugins, fall back to search and reading files. |
| `mise` (required) | Installs every tool the repository's commands run, at the version its `mise.toml` pins (Node, Go, the linters, the CLIs), and the git hooks run every step on them. Docker Desktop is the one tool installed by hand. | `sh .agents/setup` installs it (`brew install mise`), then add `eval "$(mise activate zsh)"` (or bash, fish) to your shell profile so a shell in the repository uses the pinned tools | The git hooks refuse every step, and a command runs on whatever version the shell happens to have. |
| `rtk` | Rewrites shell commands so their output costs fewer tokens. A user-level hook, never a repository setting. | brew install rtk, then add the `rtk hook claude` PreToolUse hook to ~/.claude/settings.json | Nothing breaks. Command output is unfiltered. |
| `context-mode` | Runs large-output work (logs, JSON, web pages) in a sandbox so only the answer enters the conversation. | claude plugin marketplace add github:mksglu/context-mode | The `ctx_*` tools do not exist. Read files with offset and limit instead. |

`sh .agents/setup` installs or updates everything this table names, and checks the rest. It is the person's to run; an agent runs it with `--check` only, to report what is missing.

### Code structure

Ask Entire Graph before searching by hand: `entire graph query --profile full "<the task in one sentence>"` finds the code for a task, and `entire graph impact --symbol <name>` shows what a change to a symbol reaches. `entire graph agent-guide` is the reference for the installed version; the `graph-first` skill says when to use which.

<!-- entire-agent:begin -->
### Memory of past work

- Start a substantial task with `entire brain brief "<task>" --json`. Look for earlier work with `entire search "<topic>" --compact`, and across every Mountaya repository with `entire brain workspace query mountaya "<query>"`.
- Record a decision, gotcha or invariant the code cannot show with `entire brain remember "<fact>" --kind decision|gotcha|invariant`, and read them back with `entire brain recall "<query>"`. `--path` takes a taxonomy path (`architecture.billing.decision`), never a file; `recall --locus <file or symbol>` narrows to code. A fact about the repository goes to Brain, which every agent shares; an agent's own memory is for the person's preferences.
- Brain records into the repository the command runs in, and Entire records a session in the repository its working directory is in. So a session that changes a sibling works inside it: `cd ../<repo>` stays there for a session that lists it among its working directories, as platform's lists every sibling, Entire then records the session there too, and the commit the person makes there links its checkpoint. Read the sibling's AGENTS.md before changing it, since its rules do not load from here. A fact about a sibling is recorded from inside it (`cd ../<repo> && entire brain remember ...`), and one that holds in every repository with `--global`.
- Before changing a line you do not understand, ask `entire why <file>:<line>`: it names the commit, the checkpoint and the prompt behind it.
- Brain and checkpoints say what was decided and what went wrong before. Current source and a passing test outrank both.
- `entire brain setup` without `--no-backfill --no-daemon` installs a background service and spends model tokens distilling past sessions. Choose that deliberately: `entire brain workspace watch mountaya` keeps every brain of the workspace fresh and spends none.
<!-- entire-agent:end -->

### Review and CI

- Before you report a change done, run `sh .agents/siblings gate`. It runs the quick gate (`mise run ci:quick`) of every repository checked out beside this one that the change reaches, one at a time, with the render check of each one ai renders where ai changed and `mise run generate:check` where a sibling it reads changed; `sh .agents/siblings impact` lists them first, and why. A change to ai or schemas is done once every repository it reaches passes.
- CI runs on Depot for every push to Entire (`.depot/workflows/ci.yml`): the gate, `mise run ci` (tests and the build), each job a check on the commit. Pre-commit runs formatting and linting (`mise run ci:quick`). Run the gate yourself before you report a change done. `depot ci run list` and `depot ci logs <run>` read CI's runs, and `depot ci run --workflow .depot/workflows/ci.yml` runs CI's own gate on your uncommitted changes.
- CI builds against the sibling commits `siblings.lock` pins, fetched as the bundles each sibling's own CI publishes on every push. Pre-commit stops while any sibling checked out beside this one has uncommitted or untracked files or a HEAD no remote branch holds, naming each one and what to do. It then pins each sibling whose HEAD moved forward and stages the lock, so a commit pins the sibling commits it was tested against: it stops on a HEAD behind or diverged from its pin, it keeps the pins of a commit that changes the lock itself or concludes a merge, and `SIBLINGS_ALLOW_DRIFT=<name>` leaves a pin where it is, never past uncommitted or unpushed work. Pre-push stops a push whose commit carries no lock, which CI's checkout stops on, or whose pin no remote branch holds or its sibling's CI never ran on, so no bundle of it exists. `sh .agents/siblings status` shows each pin beside its sibling's HEAD and CI run, `sh .agents/siblings pin` moves pins by hand, and `sh .agents/siblings at-pins`, which a release runs before it tags, stops unless every sibling is checked out, clean and at its pin.
- A release of this repository runs with the rest of the workspace's, from ../platform (`mise run release`), and is a person's. `mise run version <X.Y.Z>` writes it into the files, and an agent may run it; `mise run ci:local` is the part of the gate CI cannot run, which the release runs on the commit it tags; `mise run release <X.Y.Z>` tags the pushed commit CI passed, publishes what a registry lacks and waits for the mirror, and run again it resumes where it stopped.
- Every push of `main` and of a release tag (`v*`) also reaches the read-only public mirror, https://github.com/mountaya/sdk-kotlin, which Depot CI pushes (`.depot/workflows/mirror.yml`) and nothing else does. Published metadata and links for readers name the mirror; contributors clone, push and review here.
- Review happens on Entire trails. Read one with `entire trail show <number>` and answer its findings with `entire trail finding` (the `address-findings` skill). Creating, approving and merging a trail is the person's.
- Entire's repository, trail, login and setup commands are the person's too: suggest them, never run them.

### Shared conventions

Claude Code: model `claude-opus-5-5`, effort `xhigh`, plugins `mountaya-core@mountaya`, `entire@entire-skills`, `mountaya-kotlin@mountaya`, all set in `.claude/settings.json`.

Any other agent: the same conventions are plain skills in the `mountaya/ai` repository (https://github.com/mountaya/ai), installed for your user (`-g`) rather than into this repository. `sh .agents/setup` does it for Codex and Cursor; by hand, from a checkout beside this one:

```bash
npx skills add ../ai -g --skill graph-first --skill kotlin-conventions --skill retrieve-via-ctx
```

Entire's own skills (search, explain, recall, review, address-findings and the rest) come with `entire@entire-skills` in Claude Code. Any other agent installs them with `npx skills add https://github.com/entireio/skills -g --all`.

<!-- mountaya-ai:tooling:end -->
