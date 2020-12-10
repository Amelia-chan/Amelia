package pw.mihou.amelia.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ServerFeeds {

    private long server;
    private static Map<Long, ChannelFeeds> channels = new HashMap<>();
    private static ArrayList<FeedModel> models = new ArrayList<>();

    public ServerFeeds(long server){
        this.server = server;
    }

    public ChannelFeeds getChannel(long id){
        if(!channels.containsKey(id)) {
            channels.put(id, new ChannelFeeds(id, server));
        }

        return channels.get(id);
    }

    public CompletableFuture<ArrayList<FeedModel>> getModels(){
        return CompletableFuture.supplyAsync(() -> {
            // We want models to clear models to make way for a newer version.
            if(!models.isEmpty()){
                models.clear();
            }

            // Add all the new feeds.
            channels.forEach((aLong, channelFeeds) -> {
                models.addAll(channelFeeds.getModels());
            });
            return models;
        });
    }

    public ArrayList<FeedModel> getCachedModels(){
        if(models.isEmpty())
            getModels();

        return models;
    }

    public ArrayList<ChannelFeeds> getFeeds(){
        return new ArrayList<>(channels.values());
    }


}
