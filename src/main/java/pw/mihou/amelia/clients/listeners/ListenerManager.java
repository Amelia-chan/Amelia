package pw.mihou.amelia.clients.listeners;

import org.json.JSONObject;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.clients.listeners.impl.FeedUpdater;
import pw.mihou.amelia.clients.listeners.impl.TrendingNotification;
import pw.mihou.amelia.clients.listeners.interfaces.FeedListener;
import pw.mihou.amelia.clients.listeners.interfaces.Listener;
import pw.mihou.amelia.clients.listeners.interfaces.TrendingListener;
import pw.mihou.amelia.io.Scheduler;
import pw.mihou.amelia.payloads.AmeliaPayload;
import pw.mihou.amelia.payloads.AmeliaTrendingPayload;

import java.util.ArrayList;
import java.util.List;

public class ListenerManager {

    public static List<Listener> listeners = new ArrayList<>();

    static {
        attach(new FeedUpdater());
        attach(new TrendingNotification());
    }

    public static void attach(Listener listener){
        listeners.add(listener);
    }

    public static void dispatch(JSONObject message){
        if(message.getString("payload_type").equalsIgnoreCase("trending")){
            listeners.stream().filter(listener -> listener.type().equalsIgnoreCase("trending"))
                    .map(listener -> (TrendingListener) listener)
                    .forEachOrdered(listener -> Scheduler.getExecutorService().submit(() -> listener
                            .onEvent(Amelia.gson.fromJson(message.getString("payload"), AmeliaTrendingPayload.class))));
        } else if(message.getString("payload_type").equalsIgnoreCase("feed")){
            listeners.stream().filter(listener -> listener.type().equalsIgnoreCase("feed"))
                    .map(listener -> (FeedListener) listener)
                    .forEachOrdered(listener -> Scheduler.getExecutorService().submit(() -> listener
                            .onEvent(Amelia.gson.fromJson(message.getString("payload"), AmeliaPayload.class))));
        } else {
            Amelia.log.error("Amelia wasn't able to dispatch event for packet {}: {}, are you sure you are using the latest version?", message.getString("payload_type"), message.toString());
        }
    }

}
