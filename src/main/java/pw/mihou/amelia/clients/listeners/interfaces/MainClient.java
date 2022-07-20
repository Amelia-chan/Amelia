package pw.mihou.amelia.clients.listeners.interfaces;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import pw.mihou.amelia.AmeliaKt;
import pw.mihou.amelia.clients.WebsocketClient;
import pw.mihou.amelia.clients.listeners.ListenerManager;

import java.net.URI;

public class MainClient extends WebSocketClient {

    public MainClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        WebsocketClient.getConnected().set(true);
        AmeliaKt.getLogger().info("Amelia has successfully shook hands with websocket.");
    }

    @Override
    public void onMessage(String s) {
        try {
            JSONObject object = new JSONObject(s);
            if (object.isNull("session"))
                ListenerManager.dispatch(object);
        } catch (JSONException e) {
            if (!s.equalsIgnoreCase("The handshake was accepted.")) {
                AmeliaKt.getLogger().error("An error occurred, the server sent this request: {}", s);
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        if (i != -1) {
            WebsocketClient.getConnected().set(true);
            AmeliaKt.getLogger().error("Amelia has disconnected from websocket with status code {} and reason {}, attempting to reconnect...", i, s);
            WebsocketClient.INSTANCE.connect();
        }
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
}