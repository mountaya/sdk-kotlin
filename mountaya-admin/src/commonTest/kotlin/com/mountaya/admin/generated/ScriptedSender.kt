package com.mountaya.admin.generated

import com.mountaya.client.BinaryResponse
import com.mountaya.client.DataCall
import com.mountaya.client.EventStream
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.OperationCall
import com.mountaya.client.OperationSender
import com.mountaya.client.Response
import com.mountaya.client.wireJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * ScriptedSender stands where the core's transport will, for the printed admin unit's tests: it records every call a
 * printed method hands over and answers it from a script, through the same `Json` the transport reads with. Where it
 * is handed [pages], each envelope call is answered with the next page's `data` and `metadata`, and with [data] and
 * [metadata] once they run out. It implements the seam the printed unit hands its calls over through, which is
 * `InternalMountayaApi`; the admin unit prints no event stream and no Data API operation, so it answers neither.
 */
@OptIn(InternalMountayaApi::class)
internal class ScriptedSender(
    private val data: JsonElement = JsonNull,
    private val metadata: JsonElement = JsonNull,
    private val file: BinaryResponse = BinaryResponse(ByteArray(0), "application/octet-stream"),
    private val pages: List<Pair<JsonElement, JsonElement>> = emptyList(),
) : OperationSender {
    /** Every call handed over, in order. */
    val calls = mutableListOf<OperationCall>()

    override suspend fun <Value, Metadata> send(
        call: OperationCall,
        data: KSerializer<Value>,
        metadata: KSerializer<Metadata>,
    ): Response<Value, Metadata> {
        val (answered, page) = pages.getOrNull(calls.size) ?: (this.data to this.metadata)
        calls += call
        return Response(wireJson.decodeFromJsonElement(data, answered), wireJson.decodeFromJsonElement(metadata, page))
    }

    override suspend fun download(call: OperationCall): BinaryResponse {
        calls += call
        return file
    }

    override fun <Event> stream(
        call: OperationCall,
        event: KSerializer<Event>,
    ): EventStream<Event> = throw UnsupportedOperationException("The admin unit prints no event stream.")

    override suspend fun <Result : Any> query(
        call: DataCall,
        result: KSerializer<Result>,
    ): Result = throw UnsupportedOperationException("The admin unit prints no Data API operation.")
}
