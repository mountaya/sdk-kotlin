package com.mountaya.client.generated

import com.mountaya.client.DataCall
import com.mountaya.client.OperationCall
import com.mountaya.client.OperationSender
import com.mountaya.client.BinaryResponse
import com.mountaya.client.EventFrame
import com.mountaya.client.EventStream
import com.mountaya.client.Response
import com.mountaya.client.wireJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * ScriptedSender stands where the transport will, for the printed surface's tests: it records
 * every call a printed method hands over and answers it from a script, through the same `Json` the
 * transport reads with. Where it is handed [pages], each envelope call is answered with the next
 * page's `data` and `metadata`, and with [data] and [metadata] once they run out.
 */
internal class ScriptedSender(
    private val data: JsonElement = JsonNull,
    private val metadata: JsonElement = JsonNull,
    private val file: BinaryResponse = BinaryResponse(ByteArray(0), "application/octet-stream"),
    private val events: List<Pair<String?, JsonElement>> = emptyList(),
    private val result: JsonElement = JsonObject(emptyMap()),
    private val pages: List<Pair<JsonElement, JsonElement>> = emptyList(),
) : OperationSender {
    /** Every REST and Tile call handed over, in order. */
    val calls = mutableListOf<OperationCall>()

    /** Every Data API call handed over, in order. */
    val dataCalls = mutableListOf<DataCall>()

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
    ): EventStream<Event> {
        calls += call
        return EventStream({ _ ->
            flow {
                for ((id, frame) in events) emit(EventFrame(id, wireJson.decodeFromJsonElement(event, frame)))
            }
        })
    }

    override suspend fun <Result : Any> query(
        call: DataCall,
        result: KSerializer<Result>,
    ): Result {
        dataCalls += call
        return wireJson.decodeFromJsonElement(result, this.result)
    }
}
