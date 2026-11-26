package com.mountaya.client

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.discard
import io.ktor.utils.io.readByteArray
import io.ktor.utils.io.readLine
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * StubServer is a local HTTP/1.1 server that answers each request from a script, writing every
 * answer byte for byte, and records what each request carried and when the client let a
 * connection go, as `sdk-swift`'s stub protocol does for a session's loads.
 *
 * It listens on the loopback address at a port of its own, so the exchange under test goes through
 * the platform's real stack: OkHttp on the jvm, URLSession on iOS. A request the script has no
 * step left for is answered `404`.
 */
internal class StubServer private constructor(
    private val selector: SelectorManager,
    private val socket: ServerSocket,
    steps: List<Step>,
) : AutoCloseable {
    /** One answer of the script, in the order requests arrive. */
    sealed interface Step {
        /**
         * An answer with [status], [headers] and a body of [chunks], each written and flushed on its
         * own, that ends as [ending] says. A body names `application/json` where [headers] names no
         * type, since URLSession holds back the head of an answer that names none until it can sniff
         * one, which a stream that stays open never gives it.
         */
        class Answer(
            val status: Int,
            val headers: Map<String, String> = emptyMap(),
            val chunks: List<String> = emptyList(),
            val ending: Ending = Ending.Ends,
        ) : Step

        /** A request read and never answered, its connection held until the client lets it go. */
        data object Silent : Step
    }

    /** How an answer's body ends. */
    enum class Ending {
        /** The body ends where its length says, and the connection stays open for the next request. */
        Ends,

        /** The body never ends: the connection is held until the client lets it go. */
        StaysOpen,

        /** The connection is closed short of the length the answer named. */
        BreaksOff,
    }

    /** One request as it arrived. */
    class Seen(
        val method: String,
        /** The request target exactly as sent: the path and the query. */
        val target: String,
        /** Every header, each name as sent. */
        val headers: Map<String, String>,
        val body: ByteArray,
    ) {
        /** The value of the header named [name], in any letter case, or null where none arrived. */
        fun header(name: String): String? = HeaderLookup.value(name, headers)
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val script = MutableStateFlow(steps)
    private val arrived = MutableStateFlow(emptyList<Seen>())
    private val released = MutableStateFlow(0)

    /** The port the server listens on. */
    val port: Int = (socket.localAddress as InetSocketAddress).port

    /** Every request that arrived, in order. */
    val seen: List<Seen> get() = arrived.value

    /** How many held connections the client let go: a silent request's, or a body's that stays open. */
    val stops: Int get() = released.value

    /** The server's origin, as a surface's base URL names one. */
    val origin: Url get() = Url("http://127.0.0.1:$port")

    /** Returns the address of [target] on this server. */
    fun url(target: String = "/"): Url = Url("http://127.0.0.1:$port$target")

    /** Waits, in real time, until [count] requests have arrived. */
    suspend fun awaitSeen(count: Int) {
        realTime { arrived.first { it.size >= count } }
    }

    /** Waits, in real time, until [count] held connections have been let go. */
    suspend fun awaitStops(count: Int) {
        realTime { released.first { it >= count } }
    }

    override fun close() {
        socket.close()
        selector.close()
        scope.cancel()
    }

    private fun listen() {
        scope.launch {
            while (isActive) {
                val connection =
                    try {
                        socket.accept()
                    } catch (_: Throwable) {
                        break
                    }
                launch { serve(connection) }
            }
        }
    }

    private suspend fun serve(connection: Socket) {
        val input = connection.openReadChannel()
        val output = connection.openWriteChannel(autoFlush = false)
        try {
            while (true) {
                val request = read(input) ?: break
                arrived.update { it + request }
                val step = next()
                if (step is Step.Silent) {
                    held(input)
                    break
                }
                val answer = step as Step.Answer
                if (!write(answer, input, output)) break
            }
        } catch (_: Throwable) {
            // A connection the client broke is one it let go; nothing else is read on it.
        } finally {
            connection.close()
        }
    }

    /** Takes the next step of the script, or a `404` once it is spent. */
    private fun next(): Step {
        var taken: Step = Step.Answer(404)
        script.update { steps ->
            if (steps.isEmpty()) {
                steps
            } else {
                taken = steps.first()
                steps.drop(1)
            }
        }
        return taken
    }

    /** Writes [answer], and returns whether the connection carries a next request. */
    private suspend fun write(
        answer: Step.Answer,
        input: ByteReadChannel,
        output: ByteWriteChannel,
    ): Boolean {
        val chunks = answer.chunks.map { it.encodeToByteArray() }
        val length = chunks.sumOf { it.size }
        val head = StringBuilder("HTTP/1.1 ${answer.status} Stub\r\n")
        for ((name, value) in answer.headers) {
            head
                .append(name)
                .append(": ")
                .append(value)
                .append("\r\n")
        }
        val typed = HeaderLookup.contains("Content-Type", answer.headers)
        if (!typed && (length > 0 || answer.ending != Ending.Ends)) head.append("Content-Type: application/json\r\n")
        when (answer.ending) {
            Ending.Ends -> head.append("Content-Length: ").append(length).append("\r\n")
            Ending.BreaksOff -> head.append("Content-Length: ").append(length + BROKEN_OFF).append("\r\n")
            Ending.StaysOpen -> head.append("Connection: close\r\n")
        }
        head.append("\r\n")
        output.writeStringUtf8(head.toString())
        output.flush()
        for (chunk in chunks) {
            output.writeFully(chunk)
            output.flush()
        }
        return when (answer.ending) {
            Ending.Ends -> {
                true
            }

            Ending.BreaksOff -> {
                false
            }

            Ending.StaysOpen -> {
                held(input)
                false
            }
        }
    }

    /** Holds a connection until the client lets it go, and counts it. */
    private suspend fun held(input: ByteReadChannel) {
        try {
            input.discard()
        } finally {
            released.update { it + 1 }
        }
    }

    /** Reads one request, or null where the client closed the connection before sending one. */
    private suspend fun read(input: ByteReadChannel): Seen? {
        val line = input.readLine() ?: return null
        if (line.isEmpty()) return null
        val (method, target) = line.split(' ').let { it[0] to it[1] }
        val headers = LinkedHashMap<String, String>()
        while (true) {
            val header = input.readLine() ?: return null
            if (header.isEmpty()) break
            val name = header.substringBefore(':')
            val value = header.substringAfter(':').trim()
            headers[name] = headers[name]?.let { "$it, $value" } ?: value
        }
        val length = HeaderLookup.value("Content-Length", headers)?.toInt() ?: 0
        val body =
            if (HeaderLookup.value("Transfer-Encoding", headers)?.contains("chunked") == true) {
                chunked(input)
            } else {
                input.readByteArray(length)
            }
        return Seen(method, target, headers, body)
    }

    /** Reads a body sent in chunks. */
    private suspend fun chunked(input: ByteReadChannel): ByteArray {
        var body = ByteArray(0)
        while (true) {
            val size = (input.readLine() ?: return body).substringBefore(';').trim().toInt(HEX)
            if (size == 0) {
                input.readLine()
                return body
            }
            body += input.readByteArray(size)
            input.readLine()
        }
    }

    companion object {
        /** How many bytes short of its length a body that breaks off ends. */
        private const val BROKEN_OFF = 100
        private const val HEX = 16

        /** Starts a server answering requests from [steps], in order. */
        suspend fun start(vararg steps: Step): StubServer =
            realTime {
                val selector = SelectorManager(Dispatchers.Default)
                val socket = aSocket(selector).tcp().bind("127.0.0.1", 0)
                StubServer(selector, socket, steps.toList()).also { it.listen() }
            }

        /** Returns an address nothing listens at, a port that was bound and let go. */
        suspend fun unreachable(): Url =
            realTime {
                val selector = SelectorManager(Dispatchers.Default)
                val socket = aSocket(selector).tcp().bind("127.0.0.1", 0)
                val port = (socket.localAddress as InetSocketAddress).port
                socket.close()
                selector.close()
                Url("http://127.0.0.1:$port/")
            }

        /** Runs [block] on real time, bounded, so a test's virtual clock never skips a real wait. */
        suspend fun <T> realTime(block: suspend CoroutineScope.() -> T): T =
            withContext(Dispatchers.Default) { withTimeout(20.seconds) { block() } }
    }
}
