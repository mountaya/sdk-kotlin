package com.mountaya.client

import kotlinx.serialization.KSerializer

/**
 * OperationSender is the one seam every printed method calls, and the only thing between the
 * generated surface and the network.
 *
 * A printed method settles what its signature decides and hands over the rest: the transport
 * puts the call on the network, attaches the credential, repeats it on the contract's schedule,
 * reads the envelope and raises the error ladder's class. Each method fails with a
 * `MountayaException`, and a cancelled caller's `CancellationException` passes through
 * untouched.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, hands its calls over through it from
 * beside the core, reaching it as `Mountaya.sender`, and no consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public interface OperationSender {
    /** Sends a call answered with the envelope, reading its `data` with [data] and its `extensions.metadata` with [metadata]. */
    public suspend fun <Value, Metadata> send(
        call: OperationCall,
        data: KSerializer<Value>,
        metadata: KSerializer<Metadata>,
    ): Response<Value, Metadata>

    /** Sends a call answered with a file. */
    public suspend fun download(call: OperationCall): BinaryResponse

    /** Returns the cold stream of a call answered with an event stream, reading each frame's event with [event]. */
    public fun <Event> stream(
        call: OperationCall,
        event: KSerializer<Event>,
    ): EventStream<Event>

    /**
     * Sends a Data API call, reading the operation's result with [result]. A result is never null,
     * since an answer that lacks its operation's field is a failure.
     */
    public suspend fun <Result : Any> query(
        call: DataCall,
        result: KSerializer<Result>,
    ): Result
}
