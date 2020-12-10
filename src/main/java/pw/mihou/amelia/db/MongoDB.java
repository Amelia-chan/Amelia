package pw.mihou.amelia.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDB {

    // Used to build the Amelia database, please add a variable to your environment variables named "amelia_db" with the value pointing towards your MongoDB server.
    private static final MongoClient client = MongoClients.create(MongoClientSettings.builder().applicationName("Amelia")
            .applyConnectionString(new ConnectionString(System.getenv("amelia_db"))).build());

    /**
     * Used to retrieve a database from the MongoDB server.
     * @param database the database name.
     * @return MongoDatabase instance.
     */
    public static MongoDatabase database(String database){
        return client.getDatabase(database);
    }

    /**
     * Used to retrieve a collection from a certain database on the MongoDB server.
     * @param collectionName the collection name.
     * @param database the database name.
     * @return MongoCollection<Document>
     */
    public static MongoCollection<Document> collection(String collectionName, String database){
        return client.getDatabase(database).getCollection(collectionName);
    }

    /**
     * Shutdowns the MongoClient, added already to shutdown hook.
     */
    public static void shutdown(){
        client.close();
    }

    /**
     * A simple method to test the connectivity of the database by printing all the database names.
     */
    public static void testConnectivity(){
        client.listDatabaseNames().forEach(System.out::println);
    }

}
