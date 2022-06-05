package dev.amelia.database

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import dev.amelia.MONGO_CLIENT
import dev.amelia.models.AmeFeed
import dev.amelia.threadpool.AmeThreadPool
import org.bson.Document
import java.util.concurrent.CompletableFuture

object FeedDatabase {

    private val connection: MongoCollection<Document> = MONGO_CLIENT.getDatabase("amelia").getCollection("feeds")

    fun ensureIndex() {
        connection.createIndex(
            Document()
            .append("feedId", -1)
            .append("unique", -1)
            .append("server", -1)
            .append("channel", -1)
        )
    }

    fun upsert(feed: AmeFeed): CompletableFuture<UpdateResult> {
        return CompletableFuture.supplyAsync({
            connection.replaceOne(feed.filter(), feed.bson(), ReplaceOptions().upsert(true))
        }, AmeThreadPool.EXECUTOR)
    }

    fun delete(feed: AmeFeed): CompletableFuture<DeleteResult> {
        return CompletableFuture.supplyAsync({
            connection.deleteOne(feed.filter())
        }, AmeThreadPool.EXECUTOR)
    }

    fun update(feed: AmeFeed, document: Document): CompletableFuture<UpdateResult> {
        return CompletableFuture.supplyAsync({
            connection.updateOne(feed.filter(), document)
        }, AmeThreadPool.EXECUTOR)
    }

    fun find(server: Long): CompletableFuture<List<AmeFeed>> {
        return CompletableFuture.supplyAsync({
            map(connection.find(Filters.eq("server", server)))
        }, AmeThreadPool.EXECUTOR)
    }

    fun find(server: Long, unique: Long): CompletableFuture<AmeFeed?> {
        return CompletableFuture.supplyAsync({
            val document = connection.find(Filters.and(
                Filters.eq("server", server),
                Filters.eq("unique", unique)
            )).first() ?: return@supplyAsync null

            return@supplyAsync AmeFeed.from(document)
        }, AmeThreadPool.EXECUTOR)
    }

    fun has(server: Long, unique: Long): Boolean {
        return connection.find(Document().append("server", server).append("unique", unique)).first() != null
    }

    private fun map(iterable: FindIterable<Document>): List<AmeFeed> {
        return iterable.map { AmeFeed.from(it) }.toList()
    }

}