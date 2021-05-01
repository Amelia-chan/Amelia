package pw.mihou.amelia.commands.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import pw.mihou.amelia.db.MongoDB;

import java.util.HashMap;
import java.util.Map;

public class MessageDB {

    private static final Map<Long, String> messageCache = new HashMap<>();
    private static final MongoCollection<Document> db = MongoDB.collection("formats", "amelia");

    public static String setFormat(long server, String message) {
        Document doc = new Document("server", server).append("message", message);

        // Same for the one below but except for DB.
        if (doesExist(server)) {
            db.replaceOne(Filters.eq("server", server), doc);
        } else {
            db.insertOne(doc);
        }

        // If the format already exists for server then we replace it.
        if (messageCache.containsKey(server)) {
            messageCache.replace(server, message);
        } else {
            messageCache.put(server, message);
        }

        return message;
    }

    public static String requestFormat(long server) {
        if (!doesExist(server))
            return setFormat(server, "\uD83D\uDCD6 **{title} by {author}**\n{link}\n\n{subscribed}");

        String message = db.find(Filters.eq("server", server)).first().getString("message");

        // Update our cache...
        if (messageCache.containsKey(server)) {
            messageCache.replace(server, message);
        } else {
            messageCache.put(server, message);
        }

        return message;
    }

    /**
     * Retrieves from cache if possible, else retrieve from database.
     *
     * @param server the server.
     * @return message format.
     */
    public static String getFormat(long server) {
        if (!messageCache.containsKey(server))
            return requestFormat(server);

        return messageCache.get(server);
    }

    private static boolean doesExist(long server) {
        return db.find(Filters.eq("server", server)).first() != null;
    }


}
