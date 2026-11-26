package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.HexColor
import com.mountaya.geo.MapViewport
import com.mountaya.geo.SearchConfiguration
import com.mountaya.geo.SearchEngine
import com.mountaya.geo.SearchFetch
import com.mountaya.geo.SearchFilter
import com.mountaya.geo.SearchLimit
import com.mountaya.geo.SearchListState
import com.mountaya.geo.SearchListStatus
import com.mountaya.geo.SearchPickCamera
import com.mountaya.geo.SearchRow
import com.mountaya.geo.SearchRowSecondary
import com.mountaya.geo.SearchWording
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.countedText
import com.mountaya.geo.dataLanguageFor
import com.mountaya.geo.filledText
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatElevation
import com.mountaya.geo.formatNumber
import com.mountaya.geo.isSearchListShown
import com.mountaya.geo.pickTarget
import com.mountaya.geo.searchListState
import com.mountaya.geo.searchSectionOrder
import com.mountaya.geo.themeTokenValue
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration

/** The narrowest the control is laid out, in density-independent pixels. */
private const val SEARCH_MIN_WIDTH_DP = 280

/** The widest the control is laid out, in density-independent pixels. */
private const val SEARCH_MAX_WIDTH_DP = 400

/** The tallest the rows are laid out before they scroll, in density-independent pixels. */
private const val SEARCH_ROWS_MAX_HEIGHT_DP = 320

/**
 * SearchControl is a search box over Mountaya's unified search, with its results under it: the box, the filters,
 * the scope to the visible area, a line for each branch that failed, the elements' word `search.error` beside
 * `search.retry` and never the API's message, the rows with their second lines, and the empty state.
 *
 * It shows a `SearchEngine`, which waits for the typing to pause, asks one request per branch, abandons a round a
 * later one replaced, falls back from the visible area once where it found nothing there and asks again as the
 * camera of its map rests, and whose rows never answer another query than the one in the box (`geo.search.*`).
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     MapView(map, Modifier.fillMaxSize())
 *     SearchControl(map, Modifier.padding(16.dp))
 * }
 * ```
 *
 * Handed the app's [engine], it renders and drives it, its camera, the text, the scope and whether the list shows,
 * and never configures or closes it: the engine's own configuration holds, so [targets], [language], [limit], [wording],
 * [fetch] and [onError] are the control's own engine's alone. Handed none, it holds an engine of its own, closed as it
 * leaves the composition, which reads the Data API through the client of its map, as the map's content does, unless the
 * app hands [fetch] in its place; with neither it is inert, a box that answers nothing. Its own engine starts confined
 * to the visible area of its map, where a `SearchEngine` starts unconfined, a default the reader changes with its
 * toggle and the app with [isConfined]; with no map it confines nothing and shows no toggle.
 *
 * The text in the box, [query], and the scope, [isConfined], are `MutableState` the app may hoist, kept in step with the
 * engine both ways: an edit of the field or a press of the toggle writes the state and tells the engine at once, and a
 * value the app writes is told at the next composition. Where the app hoists neither, the box shows the engine's own:
 * the text it holds as typed, followed as the engine tells a new state, and its scope.
 *
 * Picking a row moves the map's camera onto it and draws it in the map's search highlight, in the `accent` the tokens
 * around the control read under the colour scheme in force (`pickTarget`, `map.search.result-highlight`), the camera
 * moved at once where the reader turned animations off, Android's animator duration scale at 0; a row that marks
 * nothing, a collection, takes the mark of the pick before down, and the control takes its map's search highlight down
 * as it leaves the composition or is handed another map. [onSelect] stands in for the whole pick, for an app that routes
 * a pick its own way.
 *
 * The list shows while the focus stands anywhere in the control, the box, its filter chips or the list, over a query
 * the reader has not dismissed, as Geo's `isSearchListShown` reads them; a pick dismisses it and leaves the box, and the
 * next keystroke or the focus coming back to the box lifts that. Its status line, the failure, the emptiness or the count of rows, is read out by accessibility
 * services as it changes.
 *
 * Its words are the scope's (`MountayaScope`), its `search` and `profile` sections, each one its own [strings] name
 * standing over the scope's, a count a template where they name `search.resultCount`, and its colours the tokens'
 * (`LocalMountayaTokens`), read under the colour scheme in force.
 *
 * @param map The map the control searches the visible area of and marks a pick on, or null for none.
 * @param modifier The host's modifier, as every composable takes one.
 * @param engine The app's engine the control renders and drives, never configuring or closing it, or null for one of
 * the control's own.
 * @param query The text in the box, which the app may hoist to read or write it, or null for the engine's own.
 * @param isConfined Whether a round is confined to the visible area of the map, which the app may hoist, or null for the
 * engine's own, its own engine's starting confined.
 * @param targets The branches each round of its own engine asks, every one by default, their sections rendering in this
 * order.
 * @param language The language a round of its own engine asks the Data API to answer in, the scope's as the Data API
 * spells it by default.
 * @param limit The cap a round of its own engine asks each branch for, the API's own default where it is null.
 * @param units The units every figure is stated in, the scope's locale in metric where it is null.
 * @param wording How a row's title and second line are worded by its own engine, its units standing over [units] for
 * the second line.
 * @param fetch How a request of its own engine reaches the Data API, in place of the map's client.
 * @param enabled Whether the box may be typed into.
 * @param strings The words it states for itself, by path, `search.placeholder`, each standing over the scope's for it
 * and everything it draws, or null for the scope's own.
 * @param onSelect Handed a pick, in place of moving and marking the map, where the app hands one.
 * @param onError Handed each failure of a branch's request of its own engine; the list shows a line for it either way.
 */
@Composable
public fun SearchControl(
    map: MapEngine?,
    modifier: Modifier = Modifier,
    engine: SearchEngine? = null,
    query: MutableState<String>? = null,
    isConfined: MutableState<Boolean>? = null,
    targets: List<DataSchema.SearchTarget> = searchSectionOrder,
    language: DataSchema.Language? = null,
    limit: SearchLimit? = null,
    units: UnitPreferences? = null,
    wording: SearchWording? = null,
    fetch: SearchFetch? = null,
    enabled: Boolean = true,
    strings: Map<String, String>? = null,
    onSelect: ((SearchRow) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    ProvideStrings(strings) {
        if (engine != null) {
            SearchControlOver(
                map,
                engine,
                configures = false,
                modifier,
                query,
                isConfined,
                targets,
                language,
                limit,
                units,
                wording,
                fetch,
                enabled,
                onSelect,
                onError,
            )
        } else {
            val coroutines = rememberCoroutineScope()
            // The text and the scope are the control's own where the app hoists neither, its engine its own too, so
            // the box's own state is the engine's: the text empty, and confined over a map.
            val ownQuery = remember { mutableStateOf("") }
            val ownConfinement = remember { mutableStateOf(true) }
            val text = query ?: ownQuery
            val confinement = isConfined ?: ownConfinement
            // The engine is made inert, with no transport, and handed one once the composition is applied, so a
            // composition made and dropped asks nothing; the text the box starts with owes no pause.
            val own =
                remember {
                    SearchEngine(
                        coroutines,
                        SearchConfiguration(targets = targets),
                        query = text.value,
                        camera = map?.let { viewportOf(it.state.value.camera) },
                        isConfined = map != null && confinement.value,
                    )
                }
            DisposableEffect(own) { onDispose { own.close() } }
            SearchControlOver(
                map,
                own,
                configures = true,
                modifier,
                text,
                confinement,
                targets,
                language,
                limit,
                units,
                wording,
                fetch,
                enabled,
                onSelect,
                onError,
            )
        }
    }
}

/**
 * The control over [map], showing [engine], which its host makes and closes, and configures from the arguments where it
 * [configures] it, the control's own; an app's engine keeps its own configuration. [query] and [isConfined] are kept in
 * step with the engine both ways where they are handed, and where null the box shows the engine's own text, followed as
 * the engine tells a new state, and its own scope.
 */
@Composable
internal fun SearchControlOver(
    map: MapEngine?,
    engine: SearchEngine,
    configures: Boolean,
    modifier: Modifier = Modifier,
    query: MutableState<String>? = null,
    isConfined: MutableState<Boolean>? = null,
    targets: List<DataSchema.SearchTarget> = searchSectionOrder,
    language: DataSchema.Language? = null,
    limit: SearchLimit? = null,
    units: UnitPreferences? = null,
    wording: SearchWording? = null,
    fetch: SearchFetch? = null,
    enabled: Boolean = true,
    onSelect: ((SearchRow) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
) {
    val scope = LocalMountayaScope.current
    val accent = themeTokenValue(LocalMountayaTokens.current.accent, themeSchemeInForce())
    val jumps = motionRemoved()
    val focus = LocalFocusManager.current
    // The handler of the latest composition is the one a failure reaches.
    val currentOnError by rememberUpdatedState(onError)
    val relay = remember<(Throwable) -> Unit> { { failure -> currentOnError?.invoke(failure) } }
    var filter by remember { mutableStateOf(SearchFilter.All) }
    var isDismissed by remember { mutableStateOf(false) }
    // Whether the focus stands anywhere in the control: the box, its filter chips or the list.
    var hasFocus by remember { mutableStateOf(false) }
    // The engine's own text, as typed, which the box shows where the app hoists none, followed as the engine tells
    // each new state, so text the app hands the engine directly reaches the box.
    val followed = remember(engine) { mutableStateOf(engine.query) }
    val hoisted by rememberUpdatedState(query)
    LaunchedEffect(engine) {
        engine.state.collect { if (hoisted == null && followed.value != engine.query) followed.value = engine.query }
    }
    val textState = query ?: followed
    val figures = units ?: UnitPreferences(scope.locale)
    val words = wording ?: SearchWording()
    val text = textState.value
    val state by engine.state.collectAsState()
    // The scope shown: the hoisted one where the app holds it, else the engine's own, read again as the engine tells a
    // new state, which a change of the scope always does, every section loading again.
    val confined = if (isConfined != null) map != null && isConfined.value else engine.isConfined
    val isListShown = isSearchListShown(hasFocus, text, isDismissed)
    val configuration =
        SearchConfiguration(
            fetch = fetch ?: map?.let(::searchFetchOf),
            language = language ?: dataLanguageFor(scope.language.value),
            limit = limit,
            targets = targets,
            wording = SearchWording(words.units ?: figures, words.resolveLabel),
            onError = relay,
        )
    // Each composition tells the engine the camera, then the text and the scope the app hoists, the question where the
    // engine is the control's own, and whether the list shows; the engine asks only where one of them changed what a
    // round asks, so text the app writes reaches it as text typed does, and the camera is stated first, so the round a
    // transport first asks reads it.
    SideEffect {
        val viewport = map?.let { viewportOf(it.state.value.camera) }
        if (viewport != engine.camera) engine.setCamera(viewport)
        if (query != null) engine.setQuery(query.value)
        if (isConfined != null) engine.setConfined(confined)
        if (configures) engine.configure(configuration)
        engine.setOpen(isListShown)
    }
    // The camera a round reads is the map's at rest, told to the engine as it changes between compositions.
    LaunchedEffect(engine, map) {
        val states = map?.state ?: return@LaunchedEffect
        val rests = states.map { viewportOf(it.camera) }.distinctUntilChanged()
        rests.collect { camera -> if (camera != engine.camera) engine.setCamera(camera) }
    }
    // The highlight is the control's: it is taken down from the map the control showed as the control leaves or is
    // handed another map, so no mark stays where nothing points at it.
    DisposableEffect(map) { onDispose { map?.setSearchHighlight(null) } }
    val list = searchListState(state, filter)
    // The status line is a node of its own laid over the control, so it reads out as it changes and takes no room.
    val line = searchStatusText(list.status, scope)
    Box(
        modifier
            .widthIn(min = SEARCH_MIN_WIDTH_DP.dp, max = SEARCH_MAX_WIDTH_DP.dp)
            .onFocusChanged { hasFocus = it.hasFocus },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchField(
                text = text,
                enabled = enabled,
                onTextChange = { typed ->
                    textState.value = typed
                    isDismissed = false
                    engine.setQuery(typed)
                },
                onFocusChange = { focused -> if (focused) isDismissed = false },
                onSearchClick = { focus.clearFocus() },
            )
            if (isListShown) {
                SearchList(
                    list = list,
                    isSearching = state.isSearching,
                    filter = filter,
                    onFilterChange = { filter = it },
                    isConfined = if (map == null) null else confined,
                    onConfinedChange = { next ->
                        isConfined?.value = next
                        engine.setConfined(next)
                    },
                    units = figures,
                    onRetryClick = { engine.retry() },
                    onSelect = { row ->
                        isDismissed = true
                        focus.clearFocus()
                        searchPick(row, map, accent, onSelect, jumps)
                    },
                )
            }
        }
        Box(
            Modifier.size(1.dp).clearAndSetSemantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = line
            },
        )
    }
}

/**
 * SearchField is the search box: a glyph, the field, and the clear button while there is text to clear, on a control's
 * chip.
 */
@Composable
private fun SearchField(
    text: String,
    enabled: Boolean,
    onTextChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onSearchClick: () -> Unit,
) {
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val ink = tokenColor(tokens.controlFg, scheme)
    val dimmed = tokenColor(tokens.dimmed, scheme)
    val label = scope.word("search.label")
    val field = remember { MutableInteractionSource() }
    val clear = remember { MutableInteractionSource() }
    ControlChip(Modifier.fillMaxWidth().keyboardFocusRing(field, RoundedCornerShape(8.dp))) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = controlSide).padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Glyph("search", set = scope.controlGlyphs, size = 18.dp, color = dimmed)
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (text.isEmpty()) {
                    BasicText(
                        scope.word("search.placeholder"),
                        Modifier.clearAndSetSemantics {},
                        style = TextStyle(color = dimmed, fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onFocusChanged { onFocusChange(it.isFocused) }
                            .semantics { contentDescription = label },
                    enabled = enabled,
                    textStyle = TextStyle(color = ink, fontSize = 16.sp),
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search,
                        ),
                    keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
                    singleLine = true,
                    cursorBrush = SolidColor(tokenColor(tokens.accent, scheme)),
                    interactionSource = field,
                )
            }
            if (text.isNotEmpty()) {
                Box(
                    Modifier
                        .size(controlSide)
                        .keyboardFocusRing(clear)
                        .clickable(
                            interactionSource = clear,
                            indication = null,
                            enabled = enabled,
                            role = Role.Button,
                            onClick = { onTextChange("") },
                        ).semantics { contentDescription = scope.word("search.clear") },
                    contentAlignment = Alignment.Center,
                ) { Glyph("close", set = scope.controlGlyphs, size = 14.dp, color = ink) }
            }
        }
    }
}

/**
 * SearchList is what shows under the box: the filter chips, the scope to the visible area where there is a map, the
 * note of a fall back, a line for each failed branch with its retry, the rows, the bar of a round in flight and the
 * empty state, on a panel's ground and edge.
 */
@Composable
internal fun SearchList(
    list: SearchListState,
    isSearching: Boolean,
    filter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    isConfined: Boolean?,
    onConfinedChange: (Boolean) -> Unit,
    units: UnitPreferences,
    onRetryClick: () -> Unit,
    onSelect: (SearchRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val ink = tokenColor(tokens.panelFg, scheme)
    val dimmed = tokenColor(tokens.dimmed, scheme)
    val accent = tokenColor(tokens.accent, scheme)
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier
            .fillMaxWidth()
            .background(tokenColor(tokens.panelBg, scheme), shape)
            .border(1.dp, tokenColor(tokens.panelBorder, scheme), shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (chip in SearchFilter.entries) {
                val isSelected = chip == filter
                val pill = RoundedCornerShape(percent = 50)
                val interactions = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .heightIn(min = controlSide)
                        .keyboardFocusRing(interactions, pill)
                        .selectable(
                            selected = isSelected,
                            interactionSource = interactions,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onFilterChange(chip) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        searchFilterLabel(chip, scope),
                        Modifier
                            .background(
                                tokenColor(if (isSelected) tokens.controlBgHover else tokens.panelBg, scheme),
                                pill,
                            ).border(1.dp, tokenColor(tokens.panelBorder, scheme), pill)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        style =
                            TextStyle(
                                color = if (isSelected) accent else ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                    )
                }
            }
        }
        if (isConfined != null) {
            val interactions = remember { MutableInteractionSource() }
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = controlSide)
                    .keyboardFocusRing(interactions)
                    .toggleable(
                        value = isConfined,
                        interactionSource = interactions,
                        indication = null,
                        role = Role.Switch,
                        onValueChange = onConfinedChange,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicText(
                    scope.word("search.viewportScope"),
                    Modifier.weight(1f),
                    style = TextStyle(color = ink, fontSize = 13.sp),
                )
                Box(
                    Modifier
                        .size(width = 36.dp, height = 20.dp)
                        .background(
                            if (isConfined) accent else tokenColor(tokens.controlFgDisabled, scheme),
                            RoundedCornerShape(percent = 50),
                        ).padding(2.dp),
                    contentAlignment = if (isConfined) Alignment.CenterEnd else Alignment.CenterStart,
                ) { Box(Modifier.size(16.dp).background(tokenColor(tokens.controlBg, scheme), CircleShape)) }
            }
        }
        if (list.hasFallbackNote) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Glyph("info", set = scope.controlGlyphs, size = 14.dp, color = dimmed)
                BasicText(scope.word("search.viewportFallback"), style = TextStyle(color = dimmed, fontSize = 13.sp))
            }
        }
        for (section in list.failed) {
            key(section.id.value) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Glyph("error", set = scope.controlGlyphs, size = 14.dp, color = ink)
                    // The elements' own word, never the API's message, which is English whatever the reader's language
                    // and stays on the failure for the app to log.
                    BasicText(
                        scope.word("search.error"),
                        Modifier.weight(1f),
                        style = TextStyle(color = ink, fontSize = 13.sp),
                    )
                    val retry = remember { MutableInteractionSource() }
                    Box(
                        Modifier
                            .heightIn(min = controlSide)
                            .keyboardFocusRing(retry)
                            .clickable(
                                interactionSource = retry,
                                indication = null,
                                role = Role.Button,
                                onClick = onRetryClick,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            scope.word("search.retry"),
                            style = TextStyle(color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }
        }
        if (list.rows.isNotEmpty()) {
            // The rows are composed as they scroll into view, so a long list composes only the rows it shows.
            LazyColumn(Modifier.heightIn(max = SEARCH_ROWS_MAX_HEIGHT_DP.dp)) {
                items(list.rows, key = { it.id }) { row -> SearchResultRow(row, units, onSelect) }
            }
        }
        if (isSearching) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(accent, RoundedCornerShape(1.dp)))
        }
        if (list.isNothingFound) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Glyph("search-empty", set = scope.controlGlyphs, size = 20.dp, color = dimmed)
                BasicText(
                    scope.word("search.emptyTitle"),
                    style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                )
                BasicText(scope.word("search.emptyDescription"), style = TextStyle(color = dimmed, fontSize = 13.sp))
            }
        }
    }
}

/**
 * SearchResultRow is one row of a search's list: its title, the runs the query matched set strong, and its second
 * line, pressed as a whole, the platform's touch target high.
 */
@Composable
internal fun SearchResultRow(
    row: SearchRow,
    units: UnitPreferences,
    onSelect: (SearchRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = LocalMountayaScope.current
    val tokens = LocalMountayaTokens.current
    val scheme = themeSchemeInForce()
    val ink = tokenColor(tokens.panelFg, scheme)
    val dimmed = tokenColor(tokens.dimmed, scheme)
    val small = TextStyle(color = dimmed, fontSize = 12.sp)
    val interactions = remember { MutableInteractionSource() }
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = controlSide)
            .keyboardFocusRing(interactions)
            .clickable(
                interactionSource = interactions,
                indication = null,
                role = Role.Button,
                onClick = { onSelect(row) },
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        BasicText(
            searchRowTitle(row),
            style = TextStyle(color = ink, fontSize = 15.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        when (val secondary = row.secondary) {
            is SearchRowSecondary.Text -> {
                BasicText(secondary.text, style = small, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            is SearchRowSecondary.Metric -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BasicText(formatDistance(secondary.distance, units) + " ·", style = small, maxLines = 1)
                    Glyph(
                        "ascent",
                        set = scope.controlGlyphs,
                        size = 12.dp,
                        color = dimmed,
                        label = scope.word("profile.ascent"),
                    )
                    BasicText(formatElevation(secondary.ascent?.toDouble(), units), style = small, maxLines = 1)
                }
            }

            is SearchRowSecondary.Count -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Glyph(
                        "itinerary",
                        set = scope.controlGlyphs,
                        size = 12.dp,
                        color = dimmed,
                        label = scope.word("search.categoryItineraries"),
                    )
                    BasicText(secondary.itineraries?.toString().orEmpty(), style = small, maxLines = 1)
                }
            }

            null -> {}
        }
    }
}

/**
 * Shows [row] on [map], its camera moved onto it and its mark drawn in [accent], the mark of the pick before taken
 * down where it marks nothing, or hands it to [onSelect] alone where the app handed one. The camera fits or flies
 * onto the row, and moves onto it at once where it [jumps], the reader having turned animations off.
 */
internal fun searchPick(
    row: SearchRow,
    map: MapEngine?,
    accent: HexColor,
    onSelect: ((SearchRow) -> Unit)?,
    jumps: Boolean = false,
) {
    if (onSelect != null) {
        onSelect(row)
        return
    }
    if (map == null) return
    val pick = pickTarget(row.payload)
    try {
        when (val camera = pick.camera) {
            is SearchPickCamera.Fit -> {
                map.fitBounds(camera.bounds, if (jumps) CameraFit(duration = Duration.ZERO) else CameraFit())
            }

            is SearchPickCamera.Fly -> {
                val target = CameraTarget(center = camera.center, zoom = camera.zoom)
                if (jumps) map.jump(target) else map.fly(target)
            }

            null -> {}
        }
    } catch (refused: MapException) {
        // A move the map refuses leaves its camera where it stands; the pick is marked all the same.
    }
    map.setSearchHighlight(pick.mark, accent)
}

/**
 * What the status line announces in the words of [scope]: the failure, the emptiness or the count of rows, and nothing
 * where it announces none. The count is the scope's counted word, or, where the control's own strings name
 * `search.resultCount`, that template with the count in place of its `{count}`, written in the scope's locale.
 */
@OptIn(InternalMountayaApi::class)
internal fun searchStatusText(
    status: SearchListStatus?,
    scope: ElementsScope,
): String =
    when (status) {
        null -> {
            ""
        }

        is SearchListStatus.ResultCount -> {
            scope.strings["search.resultCount"]
                ?.let { filledText(it, formatNumber(status.count.toDouble(), scope.locale, maximumFractionDigits = 0)) }
                ?: (scope.words["search"] as? JsonObject)
                    ?.get("resultCount")
                    ?.let { countedText(it, status.count, scope.language) }
                    .orEmpty()
        }

        else -> {
            scope.word("search.${status.key}")
        }
    }

/** The word a filter's chip reads in [scope]. */
internal fun searchFilterLabel(
    filter: SearchFilter,
    scope: ElementsScope,
): String =
    scope.word(
        when (filter) {
            SearchFilter.All -> "search.categoryAll"
            SearchFilter.Itineraries -> "search.categoryItineraries"
            SearchFilter.Places -> "search.categoryPlaces"
            SearchFilter.Zones -> "search.categoryZones"
        },
    )

/** A row's title with the runs the query matched set strong, each run a range of the title's UTF-16 indexes. */
internal fun searchRowTitle(row: SearchRow): AnnotatedString =
    buildAnnotatedString {
        val title = row.title
        var cursor = 0
        for (range in row.matched) {
            val end = minOf(range.last + 1, title.length)
            if (range.first < cursor || range.first >= end) continue
            append(title.substring(cursor, range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(title.substring(range.first, end)) }
            cursor = end
        }
        append(title.substring(cursor))
    }

/** The search request of [map]'s client, the Data API reached through the app's own `Mountaya`. */
private fun searchFetchOf(map: MapEngine): SearchFetch = SearchFetch { input -> map.client.data.search(input) }

/** The camera a round reads off a map's: its centre and its zoom. */
private fun viewportOf(camera: CameraPose): MapViewport = MapViewport(camera.center, camera.zoom)
