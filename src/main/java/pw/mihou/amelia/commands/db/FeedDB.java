package pw.mihou.amelia.commands.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import pw.mihou.amelia.db.MongoDB;
import pw.mihou.amelia.models.FeedModel;
import pw.mihou.amelia.models.ServerFeeds;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FeedDB {

    private static final Map<Long, ServerFeeds> servers = new HashMap<>();
    private static final MongoCollection<Document> db = MongoDB.collection("feeds", "amelia");
    private static final Random rand = new Random();

    /**
     * Adds a new model to the database.
     *
     * @param server The server where the model will be bound to.
     * @param model The feed model to store.
     */
    public static void addModel(long server, FeedModel model) {
        CompletableFuture.runAsync(() -> {
            servers.get(server).addFeed(model);

            if (validate(model.getUnique())) {
                db.replaceOne(Filters.eq("unique", model.getUnique()), model.toDocument(server));
                return;
            }

            db.insertOne(model.toDocument(server));
        });
    }

    /**
     * Deletes a server from the database, this performs a mass-deletion of the feeds
     * from the database and the memory.
     *
     * @param server The server to delete.
     */
    public static void deleteServer(long server) {
        CompletableFuture.runAsync(() -> {
            db.deleteMany(Filters.eq("server", server));
            servers.remove(server);
        });
    }

    /**
     * Requests all active models.
     *
     * @return all models.
     */
    public static CompletableFuture<ArrayList<FeedModel>> retrieveAllModels() {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<FeedModel> models = new ArrayList<>();
            db.find().forEach(doc -> models.add(FeedModel.from(doc)));

            return models;
        });
    }

    public static void preloadAllModels() {
        db.find().forEach(doc -> getServer(doc.getLong("server")).addFeed(FeedModel.from(doc)));
    }

    /**
     * Validates using the server, the channel and the identification number.
     *
     * @param unique The unique identification number for the rss tag.
     * @return boolean.
     */
    public static boolean validate(long unique) {
        return db.find(Filters.eq("unique", unique)).first() != null;
    }

    /**
     * Requests a feed model from the database.
     *
     * @param unique The unique id of the feed.
     * @param server The server where the feed is located.
     * @return A new Feed Model instance, if present.
     */
    public static Optional<FeedModel> requestModel(long unique, long server) {
        Document doc = db.find(Filters.eq("unique", unique)).first();
        if (doc == null)
            return Optional.empty();

        if (doc.getLong("server") != server)
            return Optional.empty();

        FeedModel model = FeedModel.from(doc);

        getServer(server).addFeed(model);
        return getServer(server).getFeedModel(model.getUnique());
    }

    /**
     * Removes a Feed Model from the database.
     *
     * @param unique The unique id of the feed.
     */
    public static void removeModel(long unique) {
        CompletableFuture.runAsync(() -> db.deleteOne(Filters.eq("unique", unique)));
    }

    /**
     * Creates a new unique id for this feed, this actively checks on the database
     * for any id that can be used.
     *
     * @return An ID that is completely unique to the feed.
     */
    public static long generateUnique() {
        long x = rand.nextInt(9999);

        return validate(x) ? generateUnique() : x;
    }

    /**
     * Retrieves a server feeds from the memory.
     *
     * @param server The server to retrieve.
     * @return A new servers feed instance if not present, otherwise the currently used.
     */
    public static ServerFeeds getServer(long server) {
        if (!servers.containsKey(server)) {
            servers.put(server, new ServerFeeds(server));
        }

        return servers.get(server);
    }

}
