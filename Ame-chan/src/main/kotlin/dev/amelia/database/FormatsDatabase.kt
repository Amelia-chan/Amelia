package dev.amelia.database

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import dev.amelia.MONGO_CLIENT
import org.bson.Document
import java.util.Optional

object FormatsDatabase {

    private val connection: MongoCollection<Document> = MONGO_CLIENT.getDatabase("amelia").getCollection("formats")

    fun ensureIndex() {
        connection.createIndex(Document().append("server", -1))
    }

    fun find(server: Long): Optional<String> {
        return Optional.ofNullable(
            connection.find(Filters.eq("server", server)).first()?.getString("message")
        )
    }

    fun upsert(server: Long, message: String): UpdateResult {
        return connection.replaceOne(
            Filters.eq("server", server),
            Document().append("server", server).append("message", message),
            ReplaceOptions().upsert(true)
        )
    }

    fun delete(server: Long): DeleteResult {
        return connection.deleteOne(Filters.eq("server", server))
    }

}