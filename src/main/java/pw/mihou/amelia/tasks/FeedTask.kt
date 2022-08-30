package pw.mihou.amelia.tasks

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import pw.mihou.amelia.Amelia
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.io.rome.RssReader
import pw.mihou.amelia.logger
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.nexus
import pw.mihou.amelia.session.AmeliaSession
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

object FeedTask: Runnable {

    private val lock = ReentrantLock()
    private val completedCounter = AtomicInteger(0)

    override fun run() {
        if (lock.isLocked) {
            logger.warn("A thread has locked onto the feed task, discarding further execution.")
            return
        }

        lock.lock()
        completedCounter.set(0)

        val feeds = FeedDatabase.connection.find().map { FeedModel.from(it) }.toList()
        logger.info("A total of ${feeds.size} feeds are now being queued for look-ups.")

        for (feed in feeds) {
            CompletableFuture.runAsync {
                try {
                    val server = nexus.shardManager.getShardOf(feed.server).flatMap { it.getServerById(feed.server) }.orElse(null)
                        ?: return@runAsync

                    val channel = server.getTextChannelById(feed.channel).orElse(null) ?: return@runAsync

                    val posts = RssReader.cached(feed.feedUrl).filter { it.date!!.after(feed.date) }
                    if (posts.isEmpty()) return@runAsync

                    val result = FeedDatabase.connection.updateOne(Filters.eq("unique", feed.unique), Updates.set("date", posts[0].date))

                    if (!result.wasAcknowledged()) {
                        logger.error("A feed couldn't be updated in the database. [feed=${feed.feedUrl}, id=${feed.unique}]")
                        return@runAsync
                    }

                    for (post in posts) {
                        channel.sendMessage(Amelia.format(post, feed, channel.server)).thenAccept {
                            AmeliaSession.feedsUpdated.incrementAndGet()
                            logger.info("I have sent a feed update to a server with success. [feed=${feed.feedUrl}, server=${channel.server.id}]")
                        }.exceptionally { exception ->
                            logger.error("Failed to send update for a feed to a server. [feed=${feed.feedUrl}, server=${channel.server.id}]", exception)
                            return@exceptionally null
                        }
                    }
                } catch (exception: Exception) {
                    logger.error("An exception was raised while attempting to send to a server. [feed=${feed.feedUrl}, server=${feed.server}]", exception)
                } finally {
                    if (completedCounter.addAndGet(1) == feeds.size) {
                        lock.unlock()
                        logger.info("Reentrant lock has been unlocked for feed task after completing all the tasks.")
                    }
                }
            }

            TimeUnit.SECONDS.sleep(4L)
        }
    }
}