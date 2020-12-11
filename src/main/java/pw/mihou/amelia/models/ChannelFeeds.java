package pw.mihou.amelia.models;

import pw.mihou.amelia.commands.db.FeedDB;

import java.util.*;

public class ChannelFeeds {

    private final long server;
    private final Map<Long, FeedModel> feeds = new HashMap<>();

    public ChannelFeeds(long server){
        this.server = server;
    }

    public Optional<FeedModel> getFeedModel(long id){
        if(!feeds.containsKey(id))
            return FeedDB.requestModel(id, server);

        return Optional.of(feeds.get(id));
    }

    public void addFeed(FeedModel model){
        if(!feeds.containsKey(model.getUnique())) {
            feeds.put(model.getUnique(), model);
        } else {
            feeds.replace(model.getUnique(), model);
        }
    }

    public void removeFeed(long id){
        // Removes it from both the database and the db.
        feeds.remove(id);
        FeedDB.removeModel(id);
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
