package pw.mihou.amelia.db

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import pw.mihou.amelia.models.FeedModel
import java.util.concurrent.ThreadLocalRandom

object FeedDatabase {

    val connection = MongoDB.client.getDatabase("amelia").getCollection("feeds")

    fun upsert(model: FeedModel) =
        connection.replaceOne(Filters.eq("unique", model.unique), model.bson(), ReplaceOptions().upsert(true))

    fun delete(unique: Long) =
        connection.deleteOne(Filters.eq("unique", unique))

    fun get(unique: Long) =
        connection.find(Filters.eq("unique", unique)).map { FeedModel.from(it) }.first()

    fun all(server: Long) =
        connection.find(Filters.eq("server", server)).map { FeedModel.from(it) }.toList()

    fun unique(): Long {
        val unique: Long = ThreadLocalRandom.current().nextLong(9999)
        return if (get(unique) != null) unique() else unique
    }
}