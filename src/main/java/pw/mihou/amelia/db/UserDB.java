package pw.mihou.amelia.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import pw.mihou.amelia.models.UserModel;

import java.util.*;

public class UserDB {

    private static final Map<Long, UserModel> userCache = new TreeMap<>();
    private static final MongoCollection<Document> db = MongoDB.collection("users", "notifications");

    public static void add(UserModel model){
        if(db.find(Filters.eq("id", model.getUser())).first() != null) {
            db.replaceOne(Filters.eq("id", model.getUser()), new Document("id", model.getUser())
                    .append("accounts", model.getAccounts()));
        } else {
            db.insertOne(new Document("id", model.getUser())
                    .append("accounts", model.getAccounts()));
        }

        userCache.put(model.getUser(), model);
    }

    public static UserModel get(long id){
        return userCache.getOrDefault(id, retrieve(id));
    }

    public static Collection<UserModel> aggregate(){
        return userCache.values();
    }

    public static void load(){
        db.find().forEach(document -> userCache.put(document.getLong("id"), new UserModel(document.getLong("id"), (List<String>) document.get("accounts"))));
    }

    public static void remove(long id){
        userCache.remove(id); db.deleteOne(Filters.eq("id", id));
    }

    public static UserModel retrieve(long id){
        if(db.find(Filters.eq("id", id)).first() != null) {
            Document doc = db.find(Filters.eq("id", id)).first();
            if(userCache.containsKey(id)){
                // Ignore the casting, they have the same effect as getList("accounts", String.class).
                userCache.get(id).replace((List<String>) doc.get("accounts"));
            } else {
                userCache.put(id, new UserModel(id, (List<String>) doc.get("accounts")));
            }
        } else {
            add(new UserModel(id, new ArrayList<>()));
        }

        return userCache.get(id);
    }

}
