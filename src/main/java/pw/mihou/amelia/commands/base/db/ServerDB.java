package pw.mihou.amelia.commands.base.db;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import pw.mihou.amelia.db.MongoDB;
import pw.mihou.amelia.models.ServerModel;

import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

public class ServerDB {

    private static final Map<Long, ServerModel> servers = new HashMap<>();
    private static final MongoCollection<Document> db = MongoDB.collection("servers", "amelia");

    /**
     * Adds a server to the database.
     *
     * @param model the server model.
     */
    public static ServerModel addServer(ServerModel model) {
        Document doc = new Document("id", model.getId()).append("prefix", model.getPrefix()).append("limit", model.getLimit())
                .append("role", model.getRole().isPresent() ? model.getRole().get() : 0L);

        if (validate(model.getId())) {
            // If the server already exists in the databse, replace it.
            db.replaceOne(eq("id", model.getId()), doc);
        } else {
            // If the server doesn't then add it.
            db.insertOne(doc);
        }

        // Adding it to the map, so we don't have to call our database every time which is exhausting.
        if (servers.containsKey(model.getId())) {
            servers.replace(model.getId(), model);
        } else {
            servers.put(model.getId(), model);
        }

        // Return back the model.
        return model;
    }

    /**
     * Deletes the server from the database.
     *
     * @param id the id of the server.
     */
    public static void deleteServer(long id) {
        db.deleteOne(eq("id", id));
    }

    /**
     * Checks if data of a server exists.
     *
     * @param id the id to check.
     * @return boolean.
     */
    public static boolean validate(long id) {
        return db.find(eq("id", id)).first() != null;
    }

    /**
     * Requests data for the server from the database.
     *
     * @param id the id of the server.
     * @return a server model.
     */
    public static ServerModel requestServer(long id) {
        // It doesn't exist then we insert data into our database and return that instead.
        if (!validate(id))
            return addServer(new ServerModel(id, "a.", true, 0L));

        Document doc = db.find(eq("id", id)).first();
        ServerModel model = new ServerModel(doc.getLong("id"), doc.getString("prefix"), doc.getBoolean("limit"), doc.getLong("role"));

        // Adds it to the map if it doesn't or replaces it if it does, this is added because there could be moments where
        // the data needs to be refreshed.
        if (servers.containsKey(model.getId())) {
            servers.replace(model.getId(), model);
        } else {
            servers.put(model.getId(), model);
        }

        return model;
    }

    /**
     * Retrieves the server model from the Map if it exists, otherwise requests it from the database.
     *
     * @param id the id of the server.
     * @return a server model.
     */
    public static ServerModel getServer(long id) {
        if (!servers.containsKey(id))
            return requestServer(id);

        return servers.get(id);
    }

}
