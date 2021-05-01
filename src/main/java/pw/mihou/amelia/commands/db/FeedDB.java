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

    public static void addModel(long server, FeedModel model) {
        Document doc = new Document("id", model.getId()).append("unique", model.getUnique()).append("server", server).append("url", model.getFeedURL())
                .append("channel", model.getChannel()).append("user", model.getUser()).append("name", model.getName()).append("date", model.getDate())
                .append("mentions", model.getMentions());

        if (validate(model.getUnique())) {
            // Simplified to now use unique instead of feed model, since feed model id is not unique between user and story.
            db.replaceOne(Filters.eq("unique", model.getUnique()), doc);
        } else {
            db.insertOne(doc);
        }

        servers.get(server).getChannel(model.getChannel()).addFeed(model);
    }

    public static void deleteServer(long server) {
        db.deleteMany(Filters.eq("server", server));
    }

    /**
     * Requests all active models.
     *
     * @return all models.
     */
    public static CompletableFuture<ArrayList<FeedModel>> retrieveAllModels() {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<FeedModel> models = new ArrayList<>();
            db.find().forEach(doc -> models.add(new FeedModel(doc.getLong("unique"), doc.getInteger("id"),
                    doc.getString("url"), doc.getLong("channel"), doc.getLong("user"), doc.getString("name"), doc.getDate("date"),
                    doc.get("mentions", new ArrayList<>()))));
            return models;
        });
    }

    public static void preloadAllModels() {
        db.find().forEach(doc -> getServer(doc.getLong("server")).getChannel(doc.getLong("channel"))
                .addFeed(new FeedModel(doc.getLong("unique"), doc.getInteger("id"),
                        doc.getString("url"), doc.getLong("channel"),
                        doc.getLong("user"), doc.getString("name"), doc.getDate("date"),
                        doc.get("mentions", new ArrayList<>()))));
    }

    public static long generateUnique() {
        // Generate a unique long with a seed of 9999 (the reason why we are using long is for future scalability).
        long x = rand.nextInt(9999);

        // Validates if the unique already exists, if so then regenerate a new one, and repeat the same procedures.
        return validate(x) ? generateUnique() : x;
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

    public static Optional<FeedModel> requestModel(long unique, long server) {
        if (!validate(unique))
            return Optional.empty();

        Document doc = db.find(Filters.eq("unique", unique)).first();

        if (doc.getLong("server") != server)
            return Optional.empty();

        FeedModel model = new FeedModel(doc.getLong("unique"), doc.getInteger("id"), doc.getString("url"), doc.getLong("channel"), doc.getLong("user"), doc.getString("name"), doc.getDate("date"),
                doc.get("mentions", new ArrayList<>()));

        // Stores for future use.
        getServer(server).getChannel(model.getChannel()).addFeed(model);

        // Returns back the stored model.
        return getServer(server).getChannel(model.getChannel()).getFeedModel(model.getUnique());
    }

    public static void removeModel(long unique) {
        db.deleteOne(Filters.eq("unique", unique));
    }

    public static ServerFeeds getServer(long server) {
        if (!servers.containsKey(server)) {
            servers.put(server, new ServerFeeds(server));
        }

        return servers.get(server);
    }

}
