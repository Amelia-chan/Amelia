package pw.mihou.amelia.models;

import pw.mihou.amelia.commands.db.FeedDB;

import java.util.*;

public class ChannelFeeds {

    private long channel;
    private long server;
    private Map<Long, FeedModel> feeds = new HashMap<>();

    public ChannelFeeds(long channel, long server){
        this.channel = channel;
        this.server = server;
    }

    public Optional<FeedModel> getFeedModel(long id){
        if(!feeds.containsKey(id))
            return FeedDB.requestModel(id, server);

        return Optional.of(feeds.get(id));
    }

    public FeedModel addFeed(FeedModel model){
        if(!feeds.containsKey(model.getUnique())) {
            feeds.put(model.getUnique(), model);
        } else {
            feeds.replace(model.getUnique(), model);
        }
        return model;
    }

    public void removeFeed(long id){
        // Removes it from both the database and the db.
        feeds.remove(id);
        FeedDB.removeModel(server, channel, id);
    }

    public ArrayList<FeedModel> getModels(){
        return new ArrayList<>(feeds.values());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelFeeds that = (ChannelFeeds) o;
        return Objects.equals(feeds, that.feeds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feeds);
    }
}
