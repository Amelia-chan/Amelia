package dev.amelia.models

import com.mongodb.client.model.Filters
import dev.amelia.NEXUS
import dev.amelia.database.FeedDatabase
import dev.amelia.http.akari.AkariFeedType
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.javacord.api.entity.server.Server
import java.util.Base64
import java.util.Date
import java.util.concurrent.ThreadLocalRandom

data class AmeFeed(
    val _id: ObjectId?,
    val server: Long,
    val channel: Long,
    val unique: Long,
    val name: String,
    val id: Int,
    val feedType: AkariFeedType,
    var date: Date,
    var mentions: List<Long>
) {

    companion object {
        fun from(bson: Document): AmeFeed {
            return AmeFeed(
                _id = bson.getObjectId("_id"),
                server = bson.getLong("server"),
                channel = bson.getLong("channel"),
                unique = bson.getLong("unique"),
                name = bson.getString("name"),
                feedType = AkariFeedType.from(bson.getString("feedType"))!!,
                id = bson.getInteger("id"),
                date = bson.getDate("date"),
                mentions = bson.getList("mentions", Long::class.java)
            )
        }

        fun generateUniqueId(server: Long): Long {
            val uniqueId = ThreadLocalRandom.current().nextInt(9999).toLong()

            if (FeedDatabase.has(server = server, unique = uniqueId))
                return generateUniqueId(server)

            return uniqueId
        }
    }

    fun filter(): Bson {
        if (_id != null) {
            return Filters.eq("_id", _id)
        }

        return Filters.and(
            Filters.eq("server", server),
            Filters.eq("channel", channel),
            Filters.eq("unique", unique),
            Filters.eq("id", id)
        )
    }

    fun bson(): Document {
        return Document()
            .append("server", server)
            .append("channel", channel)
            .append("unique", unique)
            .append("name", name)
            .append("id", id)
            .append("feedType", feedType.toString())
            .append("date", date)
            .append("mentions", mentions)
    }

    fun after(): String {
        return Base64.getEncoder().withoutPadding().encodeToString(date.toString().encodeToByteArray())
    }

    fun url(): String {
        return "https://www.scribblehub.com/rssfeed.php?type=$feedType&${ if (feedType == AkariFeedType.STORY) "sid" else "uid"}=$id"
    }

    fun server(): Server {
        return NEXUS.shardManager.getShardOf(server).flatMap { it.getServerById(server) }.orElseThrow()
    }

    fun mentions(): String {
        val feedServer = server()

        return mentions
            .filter { feedServer.getRoleById(it).isPresent }
            .joinToString(" ") {
                    roleId -> feedServer.getRoleById(roleId).map { it.mentionTag }.orElseThrow()
            }
    }

}