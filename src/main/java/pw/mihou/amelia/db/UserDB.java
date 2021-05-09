package pw.mihou.amelia.db;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import pw.mihou.amelia.models.SHUser;
import pw.mihou.amelia.models.UserModel;
import pw.mihou.amelia.templates.SingleRandom;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserDB {

    private static final Map<Long, UserModel> userCache = new TreeMap<>();
    private static final MongoDatabase database = MongoDB.database("notifications");

    public static void add(long user, String url, String name){
        int unique = generateUnique(user);
        database.getCollection(Long.toString(user)).insertOne(new Document("unique", unique)
                    .append("url", url).append("name", name));

        get(user).addAccount(new SHUser(url, unique, name));
    }

    private static int generateUnique(long user){
        int x = SingleRandom.rand.nextInt(9999);
        return database.getCollection(Long.toString(user)).find(Filters.eq("unique", x)).first() != null ? generateUnique(user) : x;
    }

    public static UserModel get(long id){
        return userCache.getOrDefault(id, retrieve(id));
    }

    public static Collection<UserModel> aggregate(){
        return userCache.values();
    }

    public static CompletableFuture<List<UserModel>> load(){
        return CompletableFuture.supplyAsync(() -> {
            List<UserModel> users = new ArrayList<>();
            database.listCollectionNames().forEach(s -> {
                List<SHUser> c = new ArrayList<>();
                database.getCollection(s).find().forEach(document -> c.add(new SHUser(document.getString("url"),
                        document.getInteger("unique"), document.getString("name"))));
                UserModel model = new UserModel(Long.parseLong(s), c);
                userCache.put(model.getUser(), model);

                users.add(model);
            });

            return users;
        });
    }

    public static boolean doesExist(long user, int unique){
        return database.getCollection(Long.toString(user)).find(Filters.eq("unique", unique)).first() != null;
    }

    public static void remove(long user, int unique){
        get(user).removeAccount(unique);
        database.getCollection(Long.toString(user)).deleteOne(Filters.eq("unique", unique));
    }

    public static UserModel retrieve(long user){
        List<SHUser> c = new ArrayList<>();
        database.getCollection(Long.toString(user)).find().forEach(document -> c.add(new SHUser(document.getString("url"), document.getInteger("unique"), document.getString("name"))));
        userCache.put(user, new UserModel(user, c));

        return userCache.get(user);
    }

}
