# Changelog

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this SDK follows
[semantic versioning](https://semver.org/spec/v2.0.0.html).

The SDK version and the contract version are two different numbers. This file records the first;
`SCHEMA_VERSION` records the contract tag a release was printed from, and a contract change reaches
this file only when it changes something a caller can see.

The five modules are one release: `com.mountaya:mountaya-client`, `com.mountaya:mountaya-admin`,
`com.mountaya:mountaya-geo`, `com.mountaya:mountaya-elements` and `com.mountaya:mountaya-navigation`
are published to Maven Central together, at one version, and each names its siblings at exactly that
version.
`mise run release <version>` moves the notes under `[Unreleased]` to the version and its date, tags
it, and hands the signed modules to the Central Portal.

## [Unreleased]

## [0.1.0] - 2026-11-26

The first release, written against contract v0.9.0 of `mountaya/schemas` (registry version 20).

- Each module's public API is one package, so one import per module reaches it: com.mountaya.client,
  com.mountaya.admin, com.mountaya.geo, com.mountaya.elements and com.mountaya.navigation, the printed
  types beside them in com.mountaya.client.generated, com.mountaya.admin.generated and
  com.mountaya.geo.generated.
- Renamed before the first release, so nothing is deprecated, to the word every SDK names the same
  concept by, old → new: BaseUrls.default → `BaseUrls.standard`; the session's snapshots, a
  StateFlow on SessionTokenSource and SessionTokenEngine beside a snapshot read of its value, →
  `SessionTokenSource.snapshot` and `SessionTokenEngine.snapshot`, the one StateFlow; FormPart,
  FormPart.File and FormPart.Text → `MultipartPart`, `MultipartPart.File` and `MultipartPart.Text`; and
  a `SearchEngine` pause, a Duration refused with a ConfigurationException as the engine was built, →
  `SearchPause`; and formatDate, which wrote a date alone, → `formatDateTime`, which writes a date, a
  time of day or both, the word of the contract's dateTime and of TypeScript's dateTimeFormat. `Url.text`
  is published, the text a `Url` holds, as every SDK reads it.
- `mountaya-admin`, for the jvm, Android and iOS: the client's admin unit, as the contract declares
  it, which only a server that manages an organization, mints session tokens or receives webhooks
  needs, beside the core an app links alone. Importing `admin` declares the Map Studio API's `admin`
  operations on the client, `mountaya.admin.<resource>.<operationId>`, a session token's mint,
  `postSession`, among them, under the secret key the core declares; the types only they and the
  webhook events reach are printed in `AdminSchema`, and the core's are named as the core names
  them. On the jvm alone, `constructWebhookEvent` verifies a webhook delivery's signature, its
  tolerance a duration, and reads it as a `WebhookEvent`, on a server and never in an app, refusing
  an untrusted one with a `WebhookSignatureException`.
- `mountaya-client`, for the jvm, Android and iOS: `Mountaya`, the client for the Map Studio API,
  the Tile API and the GraphQL Data API, every operation printed from the contract's model but the
  admin unit's, with every transport rule the contract states: credentials and the session
  lifecycle, retries and idempotency keys, conditional writes, the error ladder and the next step
  each error names, dates, pagination, field selection, and event streams that reopen, wait and
  resume on the contract's schedule. `SessionTokenEngine` keeps a session token fresh, read and
  subscribed to through its `snapshot`, one StateFlow, as a `SessionTokenSource` of your own is.
  Every request names the SDK that made it, as `X-Mountaya-Client: mountaya-kotlin/<version>`, a
  Tile API request a map makes for itself included, and a call by declaration may not set it. Each
  answer is read and decoded on the dispatcher a client is given, the platform's default one unless
  you name another, and never on the thread that called. `readingAsUser` names the user an editor
  stream is read as where the session token names none. Each listing that pages has its walk printed
  beside its method, such as `walkGetItineraries`, which takes the method's arguments and builder
  and answers a cold flow of the listing's typed rows, a page at the size its operation states;
  `TileTemplates` lists the routes a map renderer fetches on its own, each a `UrlTemplate`. An
  operation this build prints no method for is called by its declaration: `Mountaya.send`, its body
  a `RequestBody`, a JSON value or a form of `MultipartPart`s, `Mountaya.query` for a Data API
  `DataOperation`, `Mountaya.walk` for a listing, and `Mountaya.events`. A `Response` carries every
  header of the answer, which `Response.header` reads in any letter case. An event stream's records
  are decoded on the client's dispatcher too, and an answer is read through one buffer, a buffered
  one straight into its bytes; an open whose answer arrives as its collector goes lets the answer
  go, its connection with it. A header is held to what the platform's HTTP stack sends, printable
  ASCII and a name that is a token: a credential's member and the language where the client is
  built, a call's headers and a conditional write's tag before any request, and each attempt's own
  as it is written, each refused as a `ConfigurationException` and never repeated as a request that
  never arrived. A client given no language asks for none in particular, sending
  `Accept-Language: *`, which `Credential.headers` answers too, and `Credential.headers` takes the
  call's method, telling a read from a write under a share token that asks for the visitor's
  cookies. A web address the contract types `format: uri` is a `Url` kept as the text an answer
  sent, one with no scheme included, and one with no scheme is refused with a
  `ConfigurationException` in a body you build, as an origin with none is where `BaseUrls` is built.
  A secret the model marks, a session token, a webhook endpoint's signing secret, a setup intent's
  client secret and a share link's token, is printed as `<redacted>` by the type that holds it, and
  stays readable. A failure of any class declaring an error's name, `NamedException`, is recognised
  by `isMountayaException` and `isApiException`. An error's code and each of its entries' code is an
  `ApiErrorCode`, printed from the contract's model: one open vocabulary, the union of the codes the Map
  Studio API's and the Tile API's ErrorExtensionsCode and the Data API's ErrorCode declare, which keeps a
  code a later release sends exactly as sent, and `ApiException.hasCode` asks whether any entry carries a
  code, the first or another (registry version 19). A session mint that fails or times out is minted
  again on the engine's schedule, and one your app cancels ends that run of minting until a
  subscription arrives, whatever subscription was held all along. A `Retry-After` date is waited for
  in whole seconds rounded up, the wait the error's `retryAfter` names. A success whose body does
  not decode carries the decoder's failure as its cause with no byte of the body, so a session
  token, a client secret or a signing secret in it never reaches a log, as a webhook body that holds
  no event does. `Mountaya.mapRequestHeaders` refuses a visitor's cookie no header carries with a
  `ConfigurationException`, as a call does. A read under a share token that asks for the visitor's
  cookies goes out on the visitor's session alone, one whose operation requires a session token
  included, since the server is the one to judge that cookie: a read is a request sent with GET,
  HEAD or OPTIONS, in any letter case, or a call to an operation `CredentialDeclaration` declares a
  read, `CredentialDeclaration.read`, as every Data API query is over its POST. A form's file part,
  `MultipartPart.File`, carries the file name and the media type its caller gives it, `filename` and
  `contentType`, and is sent as `application/octet-stream` where it is given none, and a printed form
  sends its file as the media type the contract's model names for that part. `SCHEMA_VERSION` names
  the contract tag the client was printed from, and `MODEL_DIGEST` the digest of the exact files, the
  model and the elements' tables, as the contract defines its modelDigest, which tells apart two builds
  printed between the same two tags. `parseTimestamp` is published, the contract's anchor every timestamp the wire
  carries is read by, so a timestamp held as text is read as the wire is. A secret key is built on
  the jvm alone, by `Credential.Secret` called with the key as its constructor reads, once its
  `invoke` is imported, or by the factory that takes a secret key. A field selected
  whole that holds fields is selected with its whole subtree, as `true` selects one. A listing's
  sort is named by the words its model gives each member, `-updated_at` being
  `GetItinerariesSort.UpdatedAtDescending`, a breaking rename of the members printed under their
  wire values before.
- `mountaya-geo`, on the same targets: what Mountaya decides about that data, the contract's rules
  as functions that call nothing: distances and units, formatted values in the language you name,
  the elevation profile, a segment's groups and its road access, the camera, the map's layers and
  icons, the map's paint and a point of interest's style, the glyph sets a host builds and chains and
  how each glyph is painted, search rows, an itinerary read by id through `ItineraryResourceEngine`
  over a cache your elements share, with its legs and its places, and navigation's rules, with the
  `NavigationEngine` interface, `RouteFollowingEngine`, `ScriptedNavigationEngine` for an app's
  tests, and the plan an engine that speaks announces a route's maneuvers from and the prompt due at a
  reading, `guidancePlanOf`, `duePrompt`, `GuidancePlan` and `PROMPT_METERS`, so an engine of your own
  says each maneuver at the meter `DeviceNavigationEngine` says it. Under the controls and panels: the elements' scope, `ElementsScope` nested by `scopeOf`, and
  its words, `countedText` and `filledText`; the theme's tokens read under a `ThemeScheme` by
  `themeTokenValue`; the zoom window's ends, `zoomLimitReached` and `zoomControlsDisabled`; the layers,
  points-of-interest and preferences pickers' models, `layersPanelSections`, `poiPanelRows` and
  `preferenceChoices`; and the two engines over a map, `LayersSelectionEngine` and
  `GeolocateControlEngine`, over `LayersSelectionTarget` and `GeolocateControlTarget`. Under the
  search: `SearchEngine`, which waits for the typing to pause, asks one request per branch through the
  `SearchFetch` you hand it, keeps the latest round, falls back once from the visible area and asks
  again as the camera rests, its `state` the `SearchSnapshot` a list shows; `searchListState`, what a
  result list shows under a filter; `pickTarget`, what picking a result does to a map; the second
  lines, `describePlace`, `describePoi` and `describeWay`; and `dataLanguageFor`. Under the data
  elements: a profile's chart in its 100-unit square, `profileX`, `profileY`, `profileDomain`,
  `profilePolyline`, `profileAreaPath`, `curveStops`, `stretchAt`, `slopeStretches`, `gradeAt`,
  `scrubGrid`, `clampRange`, `placeOnCurve`, `cullCrowdedLabels` and `placeSpans`; its strip, the card at
  its cursor and its chips, `statStripCells`, `profileReadout`, `sectionChipCells` and `amenityStacks`;
  the two tables, `itinerarySegmentRows`, `itineraryWaypointRows`, `segmentRowCell`, `waypointRowCell`,
  `amenityFilter` and `toggleAmenity`; a place's card, `waypointCard`, `waypointPropertyValue` and
  `waypointPropertyLink`; `lineBetween`, `rowAt` and `rowStandsFor`; `formatAltitudeRange` and
  `formatDistanceTick`; and `surfaceColor` and `wayTypeColor`. A place's web addresses in
  `WaypointDetails` are the text the wire answers, as written. A position is read in one walk of the
  route's line, which a `FollowedRoute` holds unboxed and whose measure it publishes as `measured`, and
  `followedRouteOf` measures a route once, a route with no steps of its own reading its departure's length
  off that measure;
  `RouteFollowingEngine` asks for a route before it commits the state that asks for it, so a stop made
  as that state is told sends no request, and tells nothing after a close. Every engine closes as a
  resource, `SearchEngine` and the engines over a map among them, and a `LayersSelectionTarget` takes
  a layer off in a suspending command, as it draws one. A search folds its query once per round, and
  a `SearchRow` carries its title cut at what matched, `parts`. `formatNumber`, and every measurement,
  groups a number where the locale's CLDR minimum grouping digits say on every target, so a Polish
  1531 is written 1531. A date is written in the Gregorian calendar whatever the locale keeps or its
  tag names, the viewport a time zone opens on is the contract's whole table, a `SearchEngine` waits a
  `SearchPause`, `SearchPause.standard` unless it is handed another, and `SearchPause.of` answers null
  for a pause below zero, so no search waits one, and the elements' tokens carry `focusRing`, the
  colour a control's keyboard focus is ringed in. A place's date field is read by the client's own
  timestamp anchor, as every timestamp the wire carries is read, so a lowercase or a second-precise
  offset no SDK reads as an instant reads as written. `readItineraryRoute` joins an
  itinerary's segments, in chain order, into the one route a traveller follows over all of them, each
  arrival but the last said as a waypoint passed on the way, `waypointManeuver`, named by the
  itinerary's waypoint within `WAYPOINT_PROXIMITY_METERS` of where its segment ends, with two copy keys
  of its own, `NavigationInstructionKey.Waypoint` and `NavigationInstructionKey.WaypointPlain`. A
  navigation failure, `NavigationError`, is identified where the engine records it, so two failures
  recorded apart are two whatever their sources threw, an empty route among them, now a class built
  for each. A band of `MapPaint` named after a member every JavaScript object has is left out, as
  every SDK's merge of a patch leaves those names out. `hoveredItinerary` and `HoveredItinerary` are
  public, what a pointer over a drawn itinerary is over.
- `mountaya-navigation`, for an Android app: `DeviceNavigationEngine`, the contract's navigation on
  the device, Geo's machine with guidance that says each maneuver aloud in your words where Geo's
  progress finds it due, at the same metre as every other SDK, a maneuver after a step of no length
  included, and the location source started before the first route is asked for. It links no
  navigation library, so a
  navigating app adds no build setting for it. Its seams: `DeviceLocationSource`, read at the `rate`
  you choose, every fix a navigation reads or a walker's every 5 m (`LocationRate`), whose `standing`
  says where the app stands on location access, read again as the app returns to the foreground and
  answered by `recheckPermission`, and which tells a failure of its reader as a `LocationException`
  whose `kind` the Dart and Swift SDKs name alike, PlatformFailed for any other failure of the platform's
  reader among them, and, where `backgroundUpdates` asks it to read with the screen off and the app's manifest declares
  neither the background location permission nor a location foreground service, the background left
  undeclared, as it starts;
  `FlowLocationSource`, over the app's own flow of positions, which hands its error handler a position
  the navigation refuses and a failure of the flow, and never the app's scope; `ReplayLocationSource`,
  each position placed by the time elapsed on its clock, a route's start stated at once, so a
  navigation started again on a route walks it from its start; and `SpeechNavigationVoice`, which
  speaks as navigation guidance over the platform's text-to-speech, takes the audio focus while it
  speaks, falls back on a second language where the device holds no voice for the first, and tells
  its error handler a `SpeechException` whose `kind` says the device's speech engine failed to start,
  once, or the audio focus was refused, each time, the sentence said anyway;
  `SpeechNavigationVoice.prepare` waits until the platform reports whether the engine started, so an app
  that makes its voice up front learns before the first maneuver whether it will speak. A source or a voice given
  no error handler drops each failure, logged once as a warning in a debuggable app. The module's
  own manifest declares the speech service query Android 11 asks of an app before it may bind the
  engine, so an app's manifest needs nothing for it. Every module runs from the first Android release
  that carries the platform's ICU, which writes an instant on the locale's own clock on every device,
  the floor the README's Install names, above the releases Compose, AndroidX and MapLibre Native run on.
- `mountaya-elements`: `MapEngine` and the map's state on every target, and on Android `MapView`, a
  Compose map over MapLibre Native drawing that state, every layer ranked as the contract ranks it,
  every tile fetched with its own map's client's credential, given again at each hop so a redirect off
  the Tile API carries none, each map hearing the refusals of what it draws, the waypoint and
  point-of-interest pins a style
  names and the stripes of an itinerary's restricted stretches, and your markers as pins carrying a
  glyph, drawn from the engine's glyph set, yours or the bundled one; and `Glyph`, a glyph drawn in
  Compose, in the controls' ink unless it is handed a colour. MapLibre's own compass stays hidden
  beside the controls' one, and its credit button sits bottom-right and its logo bottom-left, both 4 dp inside
  the map's safe area, so no system bar covers either on a map drawn edge to edge, in physical corners that a
  layout running right to left does not move. The engine draws itineraries, collections, zones and specifications by id or by name, each
  call withdrawn by cancelling its coroutine, a terrain's base map switched with your content drawn
  again on it, overlays and enrichments from the layers catalogue, layers and concepts shown and
  hidden, and Data API results and three geometry slots over the map. The camera moves by `jump`,
  `ease` and `fly`, frames a box by `fitBounds` on the contract's own fit, steps by `zoomIn`,
  `zoomOut` and `resetNorth`, and frames the device's position by `locate`, which never asks for
  location access; point queries answer the features the map drew as `QueriedFeature`; `events` tells
  clicks, long presses, the features under a click or a hovering pointer, refusals of access and
  their end, failed requests, each at the address the map asked for and never one a redirect led to,
  and the layers catalogue, and tells nobody a 401 or a 403 of a request
  that left under a credential your client no longer sends, a session token minted or replaced or a
  visitor's cookie changed since, however late its answer arrives, the style refused so asked for
  again, and a style answered whose document MapLibre does not read as a failure of its address with
  no status; `credits` lists what the map credits, Mountaya first. `ScriptedMapSurface` draws a map
  nowhere, for an app's tests, its `ScriptedMapSurface.failRequest` playing a request sent under such
  a credential too. On Android, `MapControls` floats
  the map's controls in a corner, `ZoomControl` gated by the zoom window, `CompassControl`,
  `GeolocateControl`, which never asks for location access, `PanelControl`, and `DownloadControl` and
  `DirectionsControl`, each handler required; its toggles open `LayersPanel`, `PoiPanel` and
  `PreferencesPanel`, one at a time; `SearchControl` is a search box over the map's visible area,
  which moves the camera onto a pick and marks it with `setSearchHighlight`, and gives a failed branch the
  elements' own word beside its retry, never the API's message; `ItineraryProfile`,
  `ItinerarySegmentTable` and `ItineraryWaypointTable` read an itinerary by id through your
  `ItineraryResourceCache`, or through your own `ItineraryResourceEngine` handed to the overload that
  takes one, which they never close, and pin their cursor's marker and light the leg pointed at on the
  map beside them (`showCursorMarker`, `writeLegEmphasis`), with `ItinerarySparkline` and `WaypointCard`;
  `SearchControl` renders and drives the `SearchEngine` you hand it, never configuring or closing it,
  or holds one of its own, confined to its map at the start, and shows its list while the focus stands anywhere in
  it, the box, its filter chips or the list, over a query that is not white space alone and not dismissed, as
  Geo's `isSearchListShown` decides; their words come from the
  scope `MountayaScope` nests and `LocalMountayaScope` carries, each word the composable's own strings
  name by its path standing over the scope's for it and everything it draws, and their colours, the
  markers' accent
  and the search highlight's among them, from `LocalMountayaTokens`, read under the colour scheme in
  force, the one a `MountayaScope` states, light or dark, else the system's, which a composable of the
  app's own reads with `themeSchemeInForce`. `Glyph` draws from the scope's control set unless it is
  handed another, and a profile coloured by its steepness, `ProfileColoring.Slope`, takes Geo's own
  ramp unless it is handed another. A camera move a control starts jumps where the
  reader turned animations off; every control, row, chip, clear and retry draws a ring in the tokens'
  `focusRing` while it holds the keyboard's focus, and none where a touch pressed it; a profile's leg
  chips are buttons where you listen, handed the leg's id and writing nothing to the map; the
  profile's distance scale and chips grow with the font scale; a
  scrub draws the profile's cursor and what reads it alone, the dot, the card or the strip and what
  accessibility services read of the chart, and never the profile around it, its curve or the navigator's; a
  marker's pin is drawn once for each look, and a change sends MapLibre the sources it changed alone; a press
  or a redraw of `LayersPanel` and `PoiPanel` draws again only the rows it changed, and `PoiPanel` draws its
  note again as the camera crosses its floor rather than at every camera frame; the search's rows are a lazy
  list; and a panel scrolls within its height rather than growing past the screen. A base map given
  back where a switch did not arrive ends no refusal as it loads again, the refused switch standing
  until a style the map asks for loads. A patch restating a number in another spelling changes nothing,
  `MapEngine.editLayer` comparing what the map holds as JSON values compare. A leg the map's cursor
  hovers lights its row in the segment table, unless the app names another. A GeoJSON feature's id is
  answered as the style's document writes it, a number or a text, and a layer you add with
  `MapEngine.addLayer` draws from a source the base map's style holds as from one of your own, where it
  waited for a source the map never adds. The device's geolocation timings are
  published, `locateMaxFixAge` and `locateFixWait`, and a scripted map states its base map's sources,
  `ScriptedMapSurface.baseSources`, whose TileJSON credits the map credits. A request that never left
  because the client's credential could not be read for it is a `MapEvent.Failure` carrying what the
  read threw as its `cause`, as `ScriptedMapSurface.failRequest` plays it.
- Trail, the one sample app every native SDK ships, as the contract's `sdk/trail.yaml` describes it, in
  `examples/trail`: an Android app on a map, under its controls and a search box, with an itinerary's profile
  and tables and a navigation that follows the whole itinerary, saying each waypoint passed, which runs on its
  demo itinerary where no key is set, and beside it the session server `mise run trail:session` runs, which
  mints the app's session tokens on your computer with your secret key. It frames the itinerary once the map
  is ready, clear of the search box and the sheet, draws its waypoints' markers, follows you at a walker's
  zoom, and prepares its one voice as it starts. Its code is MIT licensed, so you copy it into your own app
  freely; the README's Examples you can run says how to run it.
- What the SDK costs an app is held to a budget before each release: its share of the dex code of Trail,
  module by module, with the share of each library it brings, Ktor, kotlinx.serialization, OkHttp and
  MapLibre Native's Java side, each failing the gate where it grows past 2% of its recorded figure or
  16 KiB, a library's compared only at the release its figure was recorded at, so a newer MapLibre within
  its range is reported rather than failing, a library a module brings that no share accounts for failing
  it, and the work of each hot path
  the contract lists for Kotlin, a
  hit-test, a redraw, a measurement, a source sent or a pin drawn, held to a count its test asserts.
- The layers panel lists the map studio's sections in its order (registry version 14): the base maps as
  picture cards, the enrichments as switches, the points of interest left to `PoiPanel`, the classic
  overlays as picture cards one category at a time behind a segmented switch, and the time where the host
  offers it. `layersPanelSections` takes the category the reader picked last and whether the time is
  offered, opening the overlays on that pick, else on the category holding the selected overlay, else on
  the first; `LayersPanelSection.categories` lists each `LayersPanelCategory` the switch offers;
  `LayersPanelChoice.Time` is the time's section; and `POIS_OVERLAY_SLUG` names the overlay no layers
  picker lists. A section is headed by a word alone, so `LayersPanelHeading` is the path of that word, where
  it was a word's key or a classic category's name. `LayersPanel` takes a card's picture by its choice and
  its slug, and offers the time slider where the host says so, and `MapControls` takes both and hands them
  to the layers panel its own toggle opens.
- Time in the elements: a layers selection holds the instant pinned, `LayersSelectionEngine.setInstant` and
  `LayersSelection.instant`, and drives the map to it through `LayersSelectionTarget.setInstant`, which a map
  of an app's own now declares; `MapEngine.setInstant` draws the classic overlay the server stamps for that
  instant, its tiles asked again in place as the pin moves and kept over a base-map switch, and the
  manifest's own addresses with none pinned, `MapEngine.instant` reading it back. The geo layer answers the
  moment a surface shows, `overlayInstant`, the addresses a stamped overlay is asked at,
  `timestampedSources` and `TimestampedSource`, and the slider's track, ticks, release and readout,
  `timeSliderWindow`, `TimeSliderWindow`, `timeSliderTicks`, `TimeSliderTick`, `timeSliderRelease`,
  `TimeSliderRelease`, `timeSliderReadout` and `TimeSliderReadout`, none of them reading a clock; the layers
  panel reads the time through `LocalMountayaClock`, the system's clock unless a host provides another, and
  the elements' words gain the time's, English and French.
- An instant is written as a date, a time of day or both (registry version 15): `formatDateTime` takes a
  `DateLength`, a `TimeLength`, short, the hour and the minute, or medium, the second too, or both, each in
  the locale's own style and on the locale's own 12- or 24-hour clock whatever clock the device is set to,
  a date and a time joined as the platform's data for the locale joins them, and writes the long date where
  neither is named, as formatDate did. A time names no zone, so it has no long or full length. On Android
  an instant is written through the platform's ICU, android.icu, on every device the modules run on, since
  Android's java.text writes a short or a medium time on the device's 12- or 24-hour setting where one is set
  and joins a date and a time with one space. Trail writes the arrival as its time of day and each cut-off
  as its date and its time of day, and leaves either out where the platform knows no zone of the device's name.
- Every map matches the hosted embed (registry version 18). A map reads its organization once per
  publishable key and Map Studio API origin for the process, shared by every map whose client holds that key
  and sends to that origin, a read that fails kept by nobody: `MapEngine.loadOrganization` reads it through
  postOrganizationLookup, which moved from `mountaya-admin` to the core with the organization's types, with no
  credential sent, answering null and never throwing for a client holding no publishable key, and
  `MapEngine.organization` holds it; `MapEngine.organizationRead` publishes where the read
  stands, a StateFlow of `OrganizationRead` in the contract's five states, waiting, answered with white label or
  not, failed, timed out and no key, `OrganizationRead.NoKey`, the names every SDK gives them, so a host keeps its
  own views clear of the watermark's frame until the read answers white label, as the SDK keeps its own. Its answer serves the watermark and nothing else:
  `MapView` draws the Mountaya watermark natively in the top-left corner of the map's safe area, below the
  status bar and a display cutout of a map drawn edge to edge, from the contract's drawing, its gradients, its
  opacity and its drop shadow, unless the organization holds white label, once the engine's read answers,
  fails or has waited one attempt's budget, 35 seconds by default, while it goes on, linked to Mountaya's site
  and opened in the device's browser through an ACTION_VIEW intent, never through a handler the app provides,
  and nothing hides it; and a long press opens a readout of the point, its top-left corner at the point, flipped
  left or up to stay in the map's safe area and moved clear of the watermark's frame, its topmost
  point of interest, its OpenMapTiles subclass else its class and its name, zone, its name, description, level
  and window, a time written as a string read as none, the classic overlay's band, terrain read off the elevation source the layers
  catalogue names, decoded off the main thread, and coordinates with a copy action in the accent, written in the SDK's
  default units, which `MapView(readout = false)` turns off, and a tap or the start of a camera move closes
  it. Nothing of the SDK's own lays over the watermark's frame, which is kept clear from the map's first draw,
  while the read waits too, and released only once the read answers white label: a `MapControls` stack keeps to
  the map's safe area in whichever corner it sits and moves clear of the frame where it would reach over it, its
  panels open beside their toggles inside the safe area and below the frame where they would reach over it, and
  where nothing inside the map clears the frame, the readout or the panel opens with its top at the frame's
  bottom and the map's edge cuts it. A map's safe area is the window's own root insets, the status bar, a cutout
  and the navigation bar, never the on-screen keyboard and never a value the app's layout pads away, and the mark stands at
  the reader's top left in a layout that runs right to left too, every keep-clear figure physical. The stack and its
  panels keep to the map's real frame, which `MapView` publishes on its engine, so a stack laid over more than the
  map keeps to the map; a stack driving no map, and a `PanelControl` in no stack, keep to the safe area of the space
  they are laid in and hold the frame as a waiting read does. A panel kept clear of the frame moves below it. The
  label a tapped marker shows stands 18 dp past its pin toward the end its text runs to, to its left in a layout that
  runs right to left, keeps to the safe area and moves clear of the frame by the readout's smallest move, drawn above
  the mark and never over it, as the mark is drawn above every pin, in the tooltip-fg token on a card of tooltip-bg
  rounded at 4 dp that casts no shadow, both read under the scheme in force, and shown only while its marker's point stands on the map. A
  `MapView` handed a new engine draws the new map and its mark in place of the last. The mark shows as soon as the read draws it, before
  MapLibre has drawn its map too, above MapLibre's load colour. While a readout is open its moment follows the map's
  pinned instant, its band staying as it was read at the press, written in the device's own time zone, and none is
  written where the platform knows no zone of that name. `MapControls` takes each flag, its layers panel's time among them, as a plain Boolean with a fixed default, the
  zoom, the compass, the layers and the preferences shown and the points of interest, geolocate and the time
  offered only where you turn them on, and its own preferences panel starts at the SDK's default units in the
  scope's language; no control, unit, language, base map or colour is read from the organization. Its panels
  are laid out as the embed lays out its own, 340 dp wide or the width a narrow map's corner leaves, under a
  title row, the layer cards three to a row and the preferences as cards two to a row. `mountaya-geo`
  publishes the rules: `watermarkOf`, `MapWatermark` and `OrganizationRead`, in the five states every SDK's read
  stands in, waiting, answered, failed, `OrganizationRead.TimedOut` once one attempt's budget has passed without an
  answer while the read goes on, and no key; the frame's extent a host keeps its own views clear of, under the four
  names every SDK publishes at the top level of its geo module, `watermarkWidth`, `watermarkHeight`,
  `watermarkInset` and `watermarkClearance`, which Trail's search box starts below and its sheet stops below, and
  whether the frame is held at a read, `isWatermarkKeptClear`, released only by an answer of white label;
  `readoutRows`, `ReadoutPoint`,
  `ReadoutRow`, `ReadoutMeasure` and
  `READOUT_DETAIL_ZOOM`, a zone's level held as the contract's open `RestSchema.ZoneLevel`; `panelLayoutOf`
  and `PanelLayout`; the terrain at a point read off the elevation tiles,
  `decodeElevation`, `ElevationEncoding`, `elevationTilesAt`, `terrainFromElevationTiles`, `ElevationSource`,
  `ElevationTile`, `TerrainReading` and `ELEVATION_NEIGHBOUR_DEGREES`, and its compass direction,
  `aspectCardinal` and `AspectCardinal`; `formatHumidity` and `formatSnowfall`; and `filledText` with several
  values, each by its placeholder's name. The printed `ElementLayout` holds the tokens table's measures,
  `GlyphDrawing` a drawing's `opacity` and `GlyphDrawing.Shadow`, `GlyphDrawing.artwork` a view of the table that
  refuses every change, and a fill's
  `GlyphDrawing.Paint.LinearGradient`, and every printed elements file names the contract's tag and its digest,
  and the elements' words gain the watermark's and the readout's, English and French.

### Performance

- A replay walks a route without measuring it again: `ReplayLocationSource` reads each tick's position off the
  measure the `FollowedRoute` holds, through `positionAtMeters(route, meters)`, which `mountaya-geo` publishes for a
  source of your own and which answers the position `positionAtMeters` places along the route's line, number for
  number, by a binary search. Half an hour of ticks along one route and a walk of another to its end measure each
  line once, as it is followed, where every tick measured the whole route again, 2,918 measurements in all; the
  contract's `replay-measure` budget holds it.
- A multipart body, a `RequestBody.Multipart` or a printed form, is written into one array sized once, each part read
  once and its boundary found in one pass, the bytes sent unchanged: a 10 MB file is copied once, into the body,
  where it was copied three times, and written in 2.9 ms where it took 5.4 ms, or 42 ms for a file holding the
  boundary, which now takes 5.8 ms.
- An answer's headers are folded once as it arrives, and every header the client reads of it, `Response.headers`
  included, is one lookup of that fold, each value as it was: the headers of a success of eighteen headers are read
  in 5 µs where they took 33 µs, allocating 9 KB where they allocated 41 KB.
- On Android, the failures a map's own requests meet reach the main thread a batch at a time, one hop for every
  failure heard before it runs, each told to the engine once and in the order heard, as `MapEvent.Failure` or
  `MapEvent.AccessRefused` as before: a storm of eight hundred refused tiles heard while a frame is drawn is one hop
  where it was eight hundred.
