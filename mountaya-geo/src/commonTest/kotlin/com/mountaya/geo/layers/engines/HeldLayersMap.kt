package com.mountaya.geo

import com.mountaya.client.generated.TileSchema
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Instant

/**
 * HeldLayersMap is a map a layers selection drives, each catalogue read and each command that draws held until the
 * test answers it, in the order sent, or answered at once where [answersAtOnce] says so. A command that takes
 * something off is answered at once, still numbered among the calls, unless [holdsTakeOffs] says to hold it as a
 * command that draws is held.
 */
internal class HeldLayersMap(
    private val catalogue: TileSchema.Layers,
    override var terrain: String?,
    private val answersAtOnce: Boolean = false,
    private val holdsTakeOffs: Boolean = false,
) : LayersSelectionTarget {
    /** What a refused command or a failed read throws. */
    class Refused : Exception("refused")

    /** Every command the map was given, each its command and its slug. */
    val calls = mutableListOf<String>()
    private val heldCalls = mutableListOf<CompletableDeferred<Unit>?>()
    private val heldReads = mutableListOf<CompletableDeferred<TileSchema.Layers>?>()

    /** How many catalogue reads the map was asked for. */
    val reads: Int get() = heldReads.size

    override suspend fun loadLayers(): TileSchema.Layers {
        val read = CompletableDeferred<TileSchema.Layers>()
        heldReads += read
        return read.await()
    }

    override suspend fun setTerrain(slug: String?) = command("terrain", slug)

    override suspend fun setOverlay(slug: String) = command("overlay", slug)

    override suspend fun clearOverlay() = if (holdsTakeOffs) command("overlay", null) else answered("overlay", null)

    override suspend fun addEnrichment(slug: String) = command("add-enrichment", slug)

    override suspend fun removeEnrichment(slug: String) =
        if (holdsTakeOffs) command("remove-enrichment", slug) else answered("remove-enrichment", slug)

    override suspend fun setInstant(instant: Instant?) = command("instant", instant?.toString())

    /** Lands the catalogue read [read]. */
    fun land(read: Int) {
        takeRead(read)?.complete(catalogue)
    }

    /** Fails the catalogue read [read]. */
    fun failRead(read: Int) {
        takeRead(read)?.completeExceptionally(Refused())
    }

    /** Lets the command [call] succeed. */
    fun accept(call: Int) {
        takeCall(call)?.complete(Unit)
    }

    /** Lets the command [call] fail. */
    fun refuse(call: Int) {
        takeCall(call)?.completeExceptionally(Refused())
    }

    /** Fails everything still held, so no coroutine waits for ever. */
    fun release() {
        for (index in heldReads.indices) takeRead(index)?.completeExceptionally(Refused())
        for (index in heldCalls.indices) takeCall(index)?.completeExceptionally(Refused())
    }

    private fun takeRead(index: Int): CompletableDeferred<TileSchema.Layers>? =
        heldReads.getOrNull(index).also {
            if (index in
                heldReads.indices
            ) {
                heldReads[index] = null
            }
        }

    private fun takeCall(index: Int): CompletableDeferred<Unit>? =
        heldCalls.getOrNull(index).also {
            if (index in
                heldCalls.indices
            ) {
                heldCalls[index] = null
            }
        }

    private suspend fun command(
        name: String,
        slug: String?,
    ) {
        calls += "$name:${slug ?: "-"}"
        if (answersAtOnce) {
            heldCalls += null
            return
        }
        val answer = CompletableDeferred<Unit>()
        heldCalls += answer
        answer.await()
    }

    private fun answered(
        name: String,
        slug: String?,
    ) {
        calls += "$name:${slug ?: "-"}"
        heldCalls += null
    }
}
