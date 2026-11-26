package com.mountaya.geo

import kotlinx.coroutines.CompletableDeferred

/** HeldLocateMap is a map a locate control stands over, each permission read and each fix held until the test answers it, in the order sent. */
internal class HeldLocateMap : GeolocateControlTarget {
    /** What a refused fix or a failed read throws. */
    class Refused : Exception("refused")

    private val heldReads = mutableListOf<CompletableDeferred<Boolean>?>()
    private val heldFixes = mutableListOf<CompletableDeferred<List<Double>>?>()

    /** How many permission reads the map was asked for. */
    val reads: Int get() = heldReads.size

    /** How many fixes the map was asked for. */
    val fixes: Int get() = heldFixes.size

    override suspend fun locate(): List<Double> {
        val fix = CompletableDeferred<List<Double>>()
        heldFixes += fix
        return fix.await()
    }

    override suspend fun isLocationRefused(): Boolean {
        val read = CompletableDeferred<Boolean>()
        heldReads += read
        return read.await()
    }

    /** Answers the read [read], refused or not. */
    fun answer(
        read: Int,
        refused: Boolean,
    ) {
        takeRead(read)?.complete(refused)
    }

    /** Fails the read [read]. */
    fun fail(read: Int) {
        takeRead(read)?.completeExceptionally(Refused())
    }

    /** Lets the fix [fix] land. */
    fun land(fix: Int) {
        takeFix(fix)?.complete(listOf(6.86, 45.83))
    }

    /** Refuses the fix [fix]. */
    fun refuse(fix: Int) {
        takeFix(fix)?.completeExceptionally(Refused())
    }

    /** Answers everything still held, so no coroutine waits for ever. */
    fun release() {
        for (index in heldReads.indices) takeRead(index)?.complete(false)
        for (index in heldFixes.indices) takeFix(index)?.completeExceptionally(Refused())
    }

    private fun takeRead(index: Int): CompletableDeferred<Boolean>? =
        heldReads.getOrNull(index).also {
            if (index in
                heldReads.indices
            ) {
                heldReads[index] = null
            }
        }

    private fun takeFix(index: Int): CompletableDeferred<List<Double>>? =
        heldFixes.getOrNull(index).also {
            if (index in
                heldFixes.indices
            ) {
                heldFixes[index] = null
            }
        }
}
