package pw.mihou.amelia.clients

import pw.mihou.amelia.clients.listeners.interfaces.MainClient
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.amelia.logger
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

object WebsocketClient {

    val connecting = AtomicBoolean(false)
    @JvmStatic
    val connected = AtomicBoolean(false)

    var client: MainClient? = null

    fun connect() {
        if (connecting.get() || connected.get()) return

        connecting.set(true)
        CompletableFuture.runAsync {
            createSocket()
            connect(0)
        }
    }

    private fun createSocket() {
        client = MainClient(URI.create(Configuration.WEBSOCKET_URI))
        client!!.addHeader("Authorization", Configuration.WEBSOCKET_AUTHORIZATION)
    }

    private fun connect(attempt: Int = 0) {
        if (connected.get()) return

        if (client!!.connectBlocking()) {
            connecting.set(false)
            return
        }

        logger.info("Failed to connect to the websocket, attempting to retry in a moment...")
        Thread.sleep((attempt + 1) * 1000L)
        connect(attempt + 1)
    }

    fun close() {
        client!!.close(-1, "The client is shutting down (no refresh).")
    }

}