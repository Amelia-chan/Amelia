package pw.mihou.amelia.clients;

import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.clients.listeners.interfaces.MainClient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler {

    private static final AtomicBoolean attempted = new AtomicBoolean(false);
    private static MainClient client;

    public static void connect() {
        CompletableFuture.runAsync(() -> {
            try {
                if (!attempted.get()) {
                    createSocket();
                    if (!Amelia.connected)
                        attempted.set(true);
                    if (client.connectBlocking()) {
                        Amelia.connected = true;
                        attempted.set(false);
                    } else {
                        Amelia.log.error("Attempt to connect to websocket failed, retrying...");
                        attemptConnect(0);
                    }
                }
            } catch (InterruptedException e) {
                Amelia.log.error("Attempt to connect to websocket was interrupted, retrying...");
                attemptConnect(0);
            }
        });
    }

    private static void createSocket() {
        String address = System.getenv("amelia_websocket");
        address = address == null || address.isEmpty() || address.isBlank() ? "ws://127.0.0.1:3201" : address;
        client = new MainClient(URI.create(address));
        client.addHeader("Authorization", System.getenv("amelia_auth"));
    }

    private static void attemptConnect(int i) {
        CompletableFuture.runAsync(() -> {
            try {
                createSocket();
                if (!Amelia.connected) {
                    if (client.connectBlocking()) {
                        Amelia.connected = true;
                        attempted.set(false);
                    } else {
                        try {
                            Amelia.log.error("Attempt to connect to websocket failed, retrying in {} seconds...", i + 1);
                            Thread.sleep((i + 1) * 1000);
                            attemptConnect(i + 1);
                        } catch (InterruptedException interruptedException) {
                            Amelia.log.error("Wait was interrupted, forcing attempt connection.");
                            attemptConnect(i + 1);
                        }
                    }
                }
            } catch (InterruptedException e) {
                try {
                    Amelia.log.error("Attempt to connect to websocket was interrupted, retrying in {} seconds...", i + 1);
                    Thread.sleep((i + 1) * 1000);
                    attemptConnect(i + 1);
                } catch (InterruptedException interruptedException) {
                    Amelia.log.error("Wait was interrupted, forcing attempt connection.");
                    attemptConnect(i + 1);
                }
            }
        });
    }

    public static void close() {
        client.close(-1, "The client is shutting down (no refresh).");
    }

}
