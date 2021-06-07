package pw.mihou.amelia.clients.listeners.interfaces;

import pw.mihou.amelia.payloads.AmeliaPayload;

public interface FeedListener extends Listener {

    void onEvent(AmeliaPayload payload);

    @Override
    default String type(){
        return "feed";
    }
}
