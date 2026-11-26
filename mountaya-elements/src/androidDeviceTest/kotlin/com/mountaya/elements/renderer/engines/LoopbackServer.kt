package com.mountaya.elements

import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

/**
 * LoopbackServer is an HTTP/1.1 server on the loopback address, at a port of its own, so a request reaches it
 * through OkHttp's whole stack, its network interceptors included, which an interceptor answering a request
 * never runs. It answers each request as [answer] answers its path, records the line and the headers of each
 * as it arrived, and closes each connection once it has answered.
 */
class LoopbackServer(
    private val answer: (path: String) -> Answer = { Answer(200) },
) : AutoCloseable {
    /** An answer: its status, its headers and its body. */
    class Answer(
        val status: Int,
        val headers: Map<String, String> = emptyMap(),
        val body: String = "",
    )

    /** One request as it arrived: its method, its target, and its headers by their names in lowercase. */
    class Seen(
        val method: String,
        val target: String,
        val headers: Map<String, String>,
    )

    /**
     * The IPv4 loopback address by its number, which Android does not answer for the loopback address it names first.
     */
    private val socket = ServerSocket(0, BACKLOG, InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1)))
    private val seen = CopyOnWriteArrayList<Seen>()
    private val serving =
        Thread { serve() }.apply {
            isDaemon = true
            start()
        }

    /** Every request that arrived, in order. */
    val requests: List<Seen> get() = seen.toList()

    /** The address of [path] on the server, reached under [host], the loopback address by its number unless named. */
    fun url(
        path: String = "/",
        host: String = "127.0.0.1",
    ): String = "http://$host:${socket.localPort}$path"

    override fun close() {
        socket.close()
        serving.join(JOIN_MILLISECONDS)
    }

    private fun serve() {
        while (!socket.isClosed) {
            val connection =
                try {
                    socket.accept()
                } catch (closed: IOException) {
                    return
                }
            connection.use(::handle)
        }
    }

    private fun handle(connection: Socket) {
        val input = connection.getInputStream().bufferedReader(Charsets.ISO_8859_1)
        val line = input.readLine() ?: return
        val parts = line.split(' ')
        val headers = LinkedHashMap<String, String>()
        while (true) {
            val header = input.readLine()
            if (header.isNullOrEmpty()) break
            val colon = header.indexOf(':')
            headers[header.substring(0, colon).trim().lowercase()] = header.substring(colon + 1).trim()
        }
        seen += Seen(parts[0], parts[1], headers)
        val answered = answer(parts[1].substringBefore('?'))
        val body = answered.body.toByteArray()
        val head =
            buildString {
                append("HTTP/1.1 ").append(answered.status).append(" Answered\r\n")
                for ((name, value) in answered.headers) append(name).append(": ").append(value).append("\r\n")
                append("Content-Length: ").append(body.size).append("\r\nConnection: close\r\n\r\n")
            }
        val output = connection.getOutputStream()
        output.write(head.toByteArray(Charsets.ISO_8859_1))
        output.write(body)
        output.flush()
    }

    private companion object {
        /** How many connections wait to be accepted. */
        const val BACKLOG = 50

        /** How long a close waits for the server's thread to end. */
        const val JOIN_MILLISECONDS = 1_000L
    }
}
