package pw.mihou.amelia.clients.listeners.interfaces;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.clients.ClientHandler;
import pw.mihou.amelia.clients.listeners.ListenerManager;

import java.net.URI;

public class MainClient extends WebSocketClient {

    public MainClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Amelia.connected = true;
        Amelia.log.info("Amelia has successfully shook hands with websocket.");
    }

    @Override
    public void onMessage(String s) {
        try {
            JSONObject object = new JSONObject(s);
            if (object.isNull("session"))
                ListenerManager.dispatch(object);
        } catch (JSONException e) {
            if (!s.equalsIgnoreCase("The handshake was accepted.")) {
                Amelia.log.error("An error occurred, the server sent this request: {}", s);
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        if (i != -1) {
            Amelia.connected = false;
            Amelia.log.error("Amelia has disconnected from websocket with status code {} and reason {}, attempting to reconnect...", i, s);
            ClientHandler.connect();
        }
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
}