package pw.mihou.amelia.clients.listeners.interfaces;

import pw.mihou.amelia.payloads.AmeliaTrendingPayload;

public interface TrendingListener extends Listener {

    void onEvent(AmeliaTrendingPayload payload);
    @Override
    default String type(){
        return "trending";
    }

}
