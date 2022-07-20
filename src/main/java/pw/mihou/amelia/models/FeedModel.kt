package pw.mihou.amelia.models

import org.bson.Document
import pw.mihou.amelia.models.interfaces.BsonModel
import pw.mihou.amelia.models.interfaces.ObjectModel
import java.util.Date

@Suppress("UNCHECKED_CAST")
data class FeedModel(
    val unique: Long, val id: Int, val feedUrl: String, val channel: Long, val server: Long,
    val user: Long, val name: String, val date: Date, val mentions: List<Long>
): BsonModel {

    companion object: ObjectModel<FeedModel> {
        override fun from(bson: Document): FeedModel = FeedModel(
            id = bson.getInteger("id"),
            unique = bson.getLong("unique"),
            server = bson.getLong("server"),
            feedUrl = bson.getString("url"),
            channel = bson.getLong("channel"),
            user = bson.getLong("user"),
            name = bson.getString("name"),
            date = bson.getDate("date"),
            mentions = bson["mentions"] as List<Long>
        )
    }

    override fun bson(): Document = Document(mapOf(
        "id" to id,
        "unique" to unique,
        "server" to server,
        "url" to feedUrl,
        "channel" to channel,
        "user" to user,
        "name" to name,
        "date" to date,
        "mentions" to mentions
    ))

}