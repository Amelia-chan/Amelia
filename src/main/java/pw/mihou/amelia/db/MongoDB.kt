package pw.mihou.amelia.db

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.Indexes
import pw.mihou.amelia.configuration.Configuration

object MongoDB {
    val client: MongoClient =
        MongoClients.create(
            MongoClientSettings
                .builder()
                .applicationName("Amelia Client")
                .applyConnectionString(ConnectionString(Configuration.MONGO_URI))
                .build(),
        )

    init {
        client.getDatabase("amelia").getCollection("feeds").createIndex(
            Indexes.compoundIndex(
                Indexes.descending("unique"),
                Indexes.descending("server"),
            ),
        )

        client.getDatabase("amelia").getCollection("feeds").createIndex(
            Indexes.compoundIndex(
                Indexes.descending("server"),
                Indexes.text("name"),
            ),
        )
    }
}
