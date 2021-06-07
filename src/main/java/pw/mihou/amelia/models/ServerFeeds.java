package pw.mihou.amelia.models;

import pw.mihou.amelia.commands.db.FeedDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServerFeeds {

    private final long server;
    private final Map<Long, FeedModel> feeds = new HashMap<>();

    public ServerFeeds(long server) {
        this.server = server;
    }

    public Optional<FeedModel> getFeedModel(long id) {
        if (!feeds.containsKey(id))
            return FeedDB.requestModel(id, server);

        return Optional.of(feeds.get(id));
    }

    public void addFeed(FeedModel model) {
        if (!feeds.containsKey(model.getUnique())) {
            feeds.put(model.getUnique(), model);
        } else {
            feeds.replace(model.getUnique(), model);
        }
    }

    public void removeFeed(long id) {
        // Removes it from both the database and the db.
        feeds.remove(id);
        FeedDB.removeModel(id);
    }

    public ArrayList<FeedModel> getModels() {
        return new ArrayList<>(feeds.values());
    }

}
