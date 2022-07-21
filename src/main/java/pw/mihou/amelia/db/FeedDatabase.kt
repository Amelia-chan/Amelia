package pw.mihou.amelia.db

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import pw.mihou.amelia.models.FeedModel
import java.util.concurrent.ThreadLocalRandom

object FeedDatabase {

    val connection = MongoDB.client.getDatabase("amelia").getCollection("feeds")

    fun upsert(model: FeedModel) =
        connection.replaceOne(Filters.eq("unique", model.unique), model.bson(), ReplaceOptions().upsert(true))

    fun delete(unique: Int) = connection.deleteOne(Filters.eq("unique", unique))

    fun delete(unique: Long) = delete(unique.toInt())


    fun get(unique: Int) =
        connection.find(Filters.eq("unique", unique)).map { FeedModel.from(it) }.first()

    fun get(unique: Long) = get(unique.toInt())

    fun all(server: Long) =
        connection.find(Filters.eq("server", server)).map { FeedModel.from(it) }.toList()

    fun unique(): Int {
        val unique: Int = ThreadLocalRandom.current().nextInt(9999)
        return if (get(unique) != null) unique() else unique
    }
}