package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * searchTypingPause is how long a query goes unchanged before a search asks a round for it, unless the search is given
 * another pause (`geo.search.typing-pause`), the duration `SearchPause.standard` holds.
 */
public val searchTypingPause: Duration = 150.milliseconds

/**
 * searchViewportRefreshPause is how long a camera rests before a search confined to the visible area asks its round
 * again, a pause that folds a gesture's moves into one round (`geo.search.viewport-refresh`).
 */
public val searchViewportRefreshPause: Duration = 350.milliseconds

/**
 * SearchEngine is the whole of a search over the Data API: the typing pause, a request per branch, the abandon of a
 * round a later one replaced, the fall back from the visible area, the refresh as the camera rests, and what a
 * result list shows, which a composable draws or a consumer with a list of their own reads as [state].
 *
 * - **A round waits for the typing to pause** (`geo.search.typing-pause`). [setQuery] states what the reader typed,
 *   and a round is asked once it has gone unchanged for the pause, [SearchPause.standard], 150 ms, unless the engine
 *   is given another, a pause of zero asking as the query is typed and one below zero no [SearchPause] at all, so no
 *   engine waits it; a query the engine starts with owes none, being text typed before it existed. Until a round answers the query in the box, [state]
 *   is the skeleton of the round it is owed, never rows answering another query.
 * - **One request per branch, sections in one order** (`geo.search.section-order`). A round asks one request per
 *   branch of its configuration's targets, each enabling its branch alone under the round's cap, and every section
 *   renders in the targets' order whatever order the answers land in.
 * - **Only the latest round writes** (`geo.search.live-round`). A new query, a change of the confinement, the
 *   language, the cap, the branches, whether there is a transport, or the language or the length system of the
 *   wording's units, and a [retry], each start a fresh round that abandons the one in flight, whose requests are
 *   cancelled and whose answers and failures land nowhere; each branch of the live round fills its own section as
 *   it answers, a failed branch its own alone.
 * - **A confined round broadens once** (`geo.search.broaden`). A round confined to the visible area whose every
 *   branch settled empty, with none failed, asks once more everywhere and says it fell back, never twice and never
 *   after a pan, and a round counts as settled only once the last round it asks has answered.
 * - **A rest of the camera asks again** (`geo.search.viewport-refresh`). While the list is open, the round confined
 *   and a query held, a camera stated through [setCamera] asks the round again once it has rested 350 ms, the moves
 *   of one gesture folding into one round, which replaces the rows in place with no section loading and never
 *   broadens.
 *
 * A round is ranked around the camera's centre and confined to the box around it where it is confined
 * (`geo.search.focus`, `geo.search.viewport-bounds`), and starts unconfined, a default the caller changes by
 * [setConfined], as `SearchControl` starts confined.
 *
 * ```kotlin
 * val search = SearchEngine(scope, SearchConfiguration(fetch = SearchFetch { mountaya.data.search(it) }))
 * search.setQuery("chamonix")
 * ```
 *
 * It asks as soon as it holds a question, a query its constructor is handed included; one with no transport is
 * inert and shows every section settled with nothing. Each pause, round and request runs in a coroutine of its own
 * in [scope], the caller's, whose dispatcher is the thread the engine is driven from, the main thread in a
 * composition, so a test's virtual time drives its pauses; a round's requests leave in the order of its branches.
 * [state] holds a state only where it differs from the one before it. A closed engine is inert: its pauses and its
 * round are cancelled, nothing lands after it, and its state stays as it stood. It is an `AutoCloseable`, as every
 * engine is, so `use` closes it.
 *
 * @param scope The caller's scope, which every pause, round and request is launched in.
 * @param configuration What the engine asks its rounds with.
 * @param query The query the engine starts with, already settled, so it owes no pause.
 * @param camera The camera a round reads, or null while there is none to read.
 * @param isOpen Whether the result list is being looked at.
 * @param isConfined Whether a round is confined to the visible area, which it is not by default.
 * @param pause The typing pause, [SearchPause.standard] unless another is given; a pause of zero asks a round as the
 * query is typed.
 * @param refreshPause The pause after the camera rests; one below zero is none.
 */
public class SearchEngine(
    private val scope: CoroutineScope,
    configuration: SearchConfiguration = SearchConfiguration(),
    query: String = "",
    camera: MapViewport? = null,
    isOpen: Boolean = false,
    isConfined: Boolean = false,
    pause: SearchPause = SearchPause.standard,
    refreshPause: Duration = searchViewportRefreshPause,
) : AutoCloseable {
    /** The query in the box, as the reader typed it. */
    private var typed: String = query

    /** Whether the result list is being looked at. */
    private var open: Boolean = isOpen

    /** Whether a round is confined to the visible area. */
    private var confined: Boolean = isConfined

    /** The camera a round reads. */
    private var viewport: MapViewport? = camera

    /** What the engine asks its rounds with. */
    private var asking: SearchConfiguration = configuration

    /** The query the typing pause last settled, as typed. */
    private var settled: String = query

    private val pause: Duration = pause.duration
    private val refreshPause: Duration = if (refreshPause > Duration.ZERO) refreshPause else Duration.ZERO

    /** The engine's own snapshot, which names the query the pause last settled. */
    private var answered: SearchSnapshot = cleared(listed(configuration.targets), trimWhiteSpace(query))

    /** The typing pause running, or null for none. */
    private var pauseJob: Job? = null

    /** The pause after a rest of the camera running, or null for none. */
    private var refreshJob: Job? = null

    /** The live round's coroutine, whose children are its requests, or null for none. */
    private var roundJob: Job? = null

    /** The number of the round that writes, or null for none. */
    private var liveRound: Int? = null
    private var rounds = 0

    /** The retries asked for and the rests that asked a round again, each of which tells a round to run again. */
    private var retries = 0
    private var refreshes = 0

    /**
     * The identity of the round asked last and the rest it was asked at: a round is a refresh only where the identity
     * is the same and a rest moved the count on.
     */
    private var lastKey: RoundKey? = null
    private var lastRefresh = 0

    /**
     * Whether the live round is a refresh, during which no section loads, so the busy flag is held for the length of
     * the round.
     */
    private var isRefreshing = false
    private var closed = false

    private val held = MutableStateFlow(shown())

    /**
     * What a result list shows for the query in the box: the engine's answer where it answers that query, trimmed,
     * and otherwise the skeleton of the round the query is owed, or every section settled with nothing where it owes
     * none.
     */
    public val state: StateFlow<SearchSnapshot> = held.asStateFlow()

    /** The query in the box, as the reader typed it. */
    public val query: String get() = typed

    /** Whether the result list is being looked at, which alone lets a rest of the camera ask a round again. */
    public val isOpen: Boolean get() = open

    /** Whether a round is confined to the visible area. */
    public val isConfined: Boolean get() = confined

    /** The camera a round reads, its centre and its zoom, or null while there is none to read. */
    public val camera: MapViewport? get() = viewport

    /** What the engine asks its rounds with. */
    public val configuration: SearchConfiguration get() = asking

    init {
        schedule()
    }

    /**
     * States what the reader typed. A round is asked once it has gone unchanged for the typing pause, and white space
     * typed around the query already settled asks nothing.
     */
    public fun setQuery(query: String) {
        if (closed || typed == query) return
        typed = query
        pauseJob?.cancel()
        pauseJob = null
        if (pause <= Duration.ZERO) {
            settle(query)
        } else {
            val wait = pause
            pauseJob =
                scope.launch {
                    delay(wait)
                    pauseJob = null
                    settle(query)
                }
        }
        tell()
    }

    /**
     * States whether the result list is being looked at, which alone lets a rest of the camera ask a round again. It
     * changes nothing shown, and closing the list leaves a round in flight to land, so a reader who opens it again
     * waits for nothing already asked.
     */
    public fun setOpen(isOpen: Boolean) {
        if (closed || open == isOpen) return
        open = isOpen
        syncRefresh()
    }

    /**
     * States whether a round is confined to the visible area. A change is the reader asking another question, so it
     * asks a fresh round, every section loading again.
     */
    public fun setConfined(isConfined: Boolean) {
        if (closed || confined == isConfined) return
        confined = isConfined
        syncRefresh()
        schedule()
    }

    /**
     * States the camera a round reads, its centre and its zoom, as it comes to rest, or null where there is none to
     * read. While the list is open, the round confined and a query held, each rest asks the round again once the camera
     * has rested for the refresh pause, a rest during the pause starting it again.
     */
    public fun setCamera(camera: MapViewport?) {
        if (closed) return
        viewport = camera
        if (!isRefreshLive) return
        refreshJob?.cancel()
        val wait = refreshPause
        refreshJob =
            scope.launch {
                delay(wait)
                refreshJob = null
                refreshes += 1
                schedule()
            }
    }

    /**
     * Replaces what the engine asks its rounds with. A change of the language, the cap, the branches, whether there is
     * a transport, or the language or the length system of the wording's units asks a fresh round; a configuration that
     * changes none of them asks nothing, a replaced transport, word resolver or handler included, so a composable may
     * configure the engine each time it is drawn.
     */
    public fun configure(configuration: SearchConfiguration) {
        if (closed) return
        val before = roundKey
        asking = configuration
        syncRefresh()
        if (roundKey != before) schedule() else tell()
    }

    /** Asks the query again from the top, a fresh round, which is what a failed section's retry asks. */
    public fun retry() {
        if (closed) return
        retries += 1
        schedule()
    }

    /**
     * Stops the engine for good: its pauses and its round are cancelled, its requests out withdrawn, nothing lands
     * after it, and every later command does nothing. The state stays as it was. Closing twice does nothing more.
     */
    override fun close() {
        if (closed) return
        closed = true
        pauseJob?.cancel()
        pauseJob = null
        refreshJob?.cancel()
        refreshJob = null
        abandonRound()
    }

    // Rounds.

    /**
     * What a round asks besides the rest that moves it on: a change of any member means a fresh round rather than a
     * refresh.
     */
    private class RoundKey(
        val query: String,
        val isConfined: Boolean,
        val language: DataSchema.Language?,
        val limit: SearchLimit?,
        val branches: List<DataSchema.SearchTarget>,
        val isLive: Boolean,
        val retries: Int,
        val unitsLanguage: String?,
        val unitsLength: RestSchema.UnitLength?,
    ) {
        override fun equals(other: Any?): Boolean =
            other is RoundKey &&
                other.query == query &&
                other.isConfined == isConfined &&
                other.language == language &&
                other.limit == limit &&
                other.branches == branches &&
                other.isLive == isLive &&
                other.retries == retries &&
                other.unitsLanguage == unitsLanguage &&
                other.unitsLength == unitsLength

        override fun hashCode(): Int = query.hashCode() * 31 + retries
    }

    /**
     * One round from the moment it claims the slot: what it asks, where, and through what, and its query folded
     * once for every row it builds.
     */
    private class Round(
        val number: Int,
        val fetch: SearchFetch,
        val branches: List<DataSchema.SearchTarget>,
        val query: String,
        val needle: SearchNeedle,
        val language: DataSchema.Language?,
        val limit: SearchLimit?,
        val focus: List<Double>?,
        val area: List<Double>?,
        val isRefresh: Boolean,
    )

    /**
     * What a pass of a round's requests answered: whether every section stands settled with nothing, and whether a
     * branch threw.
     */
    private class Asked(
        val isEveryEmpty: Boolean,
        val hadError: Boolean,
    )

    /** The branches a round asks, in the order their sections render. */
    private val branches: List<DataSchema.SearchTarget> get() = listed(asking.targets)

    /** The identity of the round the engine would ask now. */
    private val roundKey: RoundKey
        get() =
            RoundKey(
                query = trimWhiteSpace(settled),
                isConfined = confined,
                language = asking.language,
                limit = asking.limit,
                branches = branches,
                isLive = asking.fetch != null,
                retries = retries,
                unitsLanguage = asking.wording.units?.language,
                unitsLength = asking.wording.units?.length,
            )

    /**
     * Whether a rest of the camera asks a round again: the engine open, its rounds confined, a query settled and a
     * transport to ask through.
     */
    private val isRefreshLive: Boolean
        get() = !closed && asking.fetch != null && open && confined && trimWhiteSpace(settled).isNotEmpty()

    /**
     * Takes [typed] as the query the pause settled, and asks a round for it unless it trims to the query settled before
     * (`searchQueryChangeOf`).
     */
    private fun settle(typed: String) {
        val asked = trimWhiteSpace(settled)
        settled = typed
        if (searchQueryChangeOf(asked, typed) is SearchQueryChange.Unchanged) return
        syncRefresh()
        schedule()
    }

    /** Drops a pending rest of the camera where a rest no longer asks a round again. */
    private fun syncRefresh() {
        if (isRefreshLive) return
        refreshJob?.cancel()
        refreshJob = null
    }

    /**
     * Starts a round for the query settled, abandoning the one in flight, or settles every section with nothing where
     * there is nothing to ask: a blank query, or no transport.
     */
    private fun schedule() {
        if (closed) return
        val query = trimWhiteSpace(settled)
        val branches = branches
        val fetch = asking.fetch
        if (fetch == null || query.isEmpty()) {
            abandonRound()
            lastKey = null
            isRefreshing = false
            commit(cleared(branches, query))
            return
        }
        abandonRound()
        val key = roundKey
        val isRefresh = key == lastKey && refreshes != lastRefresh
        lastKey = key
        lastRefresh = refreshes
        isRefreshing = isRefresh
        if (isRefresh) {
            // A refresh keeps the rows in place under the query they answer, which it shares.
            commit(answered.with(isSearching = true, isSettled = false, hasFallenBack = false))
        } else {
            commit(
                SearchSnapshot(
                    query,
                    isSearching = true,
                    sections = branches.map { SearchSection(it, SearchSectionState.Loading) },
                ),
            )
        }
        // The focus ranks a round around the camera's centre whether or not it is confined; a camera whose centre
        // names no point ranks around nothing and confines nothing, and one whose zoom is no number confines nothing.
        val camera = viewport
        val focus = camera?.let { searchFocus(it.center) }
        val area =
            if (confined && camera != null && focus != null && camera.zoom.isFinite()) {
                searchViewportBounds(camera.center, camera.zoom)
            } else {
                null
            }
        val round =
            Round(
                number = rounds,
                fetch = fetch,
                branches = branches,
                query = query,
                needle = searchNeedleOf(query),
                language = asking.language,
                limit = asking.limit,
                focus = focus,
                area = area,
                isRefresh = isRefresh,
            )
        rounds += 1
        liveRound = round.number
        roundJob = scope.launch { run(round) }
    }

    /** Cancels the round in flight, whose requests are withdrawn and whose answers land nowhere. */
    private fun abandonRound() {
        roundJob?.cancel()
        roundJob = null
        liveRound = null
    }

    /** Whether [round] still writes: no later round claimed the slot, and the engine is open. */
    private fun isLive(round: Round): Boolean = !closed && liveRound == round.number

    /**
     * Asks [round]'s branches and settles the snapshot, asking once more everywhere where a confined round found
     * nothing and none of its branches failed.
     */
    private suspend fun run(round: Round) {
        // A round abandoned before its coroutine ran sends nothing.
        if (!isLive(round)) return
        val first = ask(round, round.area)
        if (!isLive(round)) return
        // Only a confined round that came back cleanly empty broadens, and never a pan's: a reader panning is
        // looking somewhere on purpose.
        val broadens = !round.isRefresh && round.area != null && first.isEveryEmpty && !first.hadError
        if (!broadens) {
            isRefreshing = false
            commit(answered.with(isSearching = false, isSettled = true))
            return
        }
        commit(
            answered.with(
                isSearching = true,
                isSettled = false,
                hasFallenBack = true,
                sections = round.branches.map { SearchSection(it, SearchSectionState.Loading) },
            ),
        )
        ask(round, null)
        if (!isLive(round)) return
        isRefreshing = false
        commit(answered.with(isSearching = false, isSettled = true))
    }

    /**
     * Sends one request per branch of [round], confined to [area] where there is one, each in a coroutine of its own
     * launched in the branches' order, so the requests leave in that order, and writes each section as its answer
     * lands while the round is live. It answers once every branch has answered or failed: whether every section then
     * stands settled with nothing, and whether a branch threw.
     */
    private suspend fun ask(
        round: Round,
        area: List<Double>?,
    ): Asked {
        var hadError = false
        coroutineScope {
            for (branch in round.branches) {
                val targets = enabledTargets(branch) ?: continue
                val input =
                    DataSchema.SearchInput(round.query, targets) {
                        language = round.language
                        limit = round.limit?.value
                        focus = round.focus
                        bbox = area
                    }
                launch { if (answer(branch, input, round)) hadError = true }
            }
        }
        val isEveryEmpty = answered.sections.all { it.state.isSettled && it.rows.isEmpty() }
        return Asked(isEveryEmpty, hadError)
    }

    /**
     * Sends [input], the request of [round] for [branch], and writes the section its answer or its failure fills
     * while the round is live. It answers whether the request threw, or landed for a round no longer live.
     */
    private suspend fun answer(
        branch: DataSchema.SearchTarget,
        input: DataSchema.SearchInput,
        round: Round,
    ): Boolean {
        // A request of a round abandoned before its coroutine ran is never sent.
        if (!isLive(round)) return true
        val outcome =
            try {
                Result.success(round.fetch.search(input))
            } catch (failure: Throwable) {
                // A request the round's cancelling withdrew lands for nobody; any other failure is the branch's.
                currentCoroutineContext().ensureActive()
                Result.failure(failure)
            }
        if (!isLive(round)) return true
        val result = outcome.getOrNull()
        if (result != null) {
            replace(searchSectionOf(result, branch, round.needle, asking.wording), round)
            return false
        }
        val failure = outcome.exceptionOrNull() ?: return true
        // The section reads its failure before the host hears of it.
        replace(SearchSection(branch, SearchSectionState.Failed(failure)), round)
        asking.onError?.invoke(failure)
        return true
    }

    /**
     * Writes [section] into the snapshot in place of its branch's, while [round] is live; a refresh stays busy for the
     * length of the round, since none of its sections loads.
     */
    private fun replace(
        section: SearchSection,
        round: Round,
    ) {
        if (!isLive(round)) return
        val sections = answered.sections.map { if (it.id == section.id) section else it }
        commit(answered.with(isSearching = isRefreshing || sections.any { it.state.isLoading }, sections = sections))
    }

    /** Holds [next] as the engine's answer and tells the state flow what a list shows. */
    private fun commit(next: SearchSnapshot) {
        answered = next
        tell()
    }

    /** Tells the state flow what a list shows. */
    private fun tell() {
        held.value = shown()
    }

    /** What a list shows for the query in the box. */
    private fun shown(): SearchSnapshot =
        shownSearchSnapshot(
            answered,
            typed,
            unansweredSearchSnapshot(typed, isLive = asking.fetch != null, targets = branches),
        )
}

/** [targets] a round asks, each once, in the order first listed, a branch this build cannot enable left out. */
private fun listed(targets: List<DataSchema.SearchTarget>): List<DataSchema.SearchTarget> =
    targets.filter { enabledTargets(it) != null }.distinct()

/** The snapshot of a query asked nothing: every section of [branches] settled with nothing, and no round settled. */
private fun cleared(
    branches: List<DataSchema.SearchTarget>,
    query: String,
): SearchSnapshot = SearchSnapshot(query, sections = branches.map { SearchSection(it, SearchSectionState.Settled) })

/** This snapshot with the members given changed, every other kept. */
private fun SearchSnapshot.with(
    isSearching: Boolean = this.isSearching,
    isSettled: Boolean = this.isSettled,
    hasFallenBack: Boolean = this.hasFallenBack,
    sections: List<SearchSection> = this.sections,
): SearchSnapshot = SearchSnapshot(query, isSearching, isSettled, hasFallenBack, sections)
