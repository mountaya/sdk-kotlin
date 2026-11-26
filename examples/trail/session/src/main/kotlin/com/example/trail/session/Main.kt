package com.example.trail.session

import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.system.exitProcess

/** The port the server listens on, on the loopback address alone. */
private const val PORT = 8787

/**
 * main runs Trail's session server: it reads both keys from the shell, refuses to start without either, and listens on
 * `127.0.0.1:8787` alone, which the Android emulator reaches at `10.0.2.2:8787`.
 */
fun main() {
    // trail: trail.session
    val keys =
        SessionKeys.read(System.getenv()).getOrElse { refusal ->
            System.err.println(refusal.message)
            exitProcess(1)
        }
    val mountaya = Mountaya(Credential(secretKey = keys.secret))
    // The requests' coroutines, one failing alone, on the pool the platform's blocking writes are made on.
    val requests = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val server =
        SessionServer(SessionServer.minting(mountaya, keys.publishable))
            .listen(InetSocketAddress(InetAddress.getByName("127.0.0.1"), PORT), requests)
    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(0)
            requests.cancel()
            mountaya.close()
        },
    )
    println(
        "Minting sessions at http://127.0.0.1:$PORT${SessionServer.PATH}, which the Android emulator reaches at " +
            "http://10.0.2.2:$PORT${SessionServer.PATH}. Ctrl-C stops it.",
    )
}
