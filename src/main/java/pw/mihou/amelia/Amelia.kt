package pw.mihou.amelia

import ch.qos.logback.classic.Logger
import com.mongodb.client.model.Filters
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.server.Server
import org.javacord.api.event.connection.ReconnectEvent
import org.javacord.api.event.connection.ResumeEvent
import org.javacord.api.event.server.ServerLeaveEvent
import org.javacord.api.util.logging.ExceptionLogger
import org.slf4j.LoggerFactory
import pw.mihou.Amaririsu
import pw.mihou.amelia.commands.*
import pw.mihou.amelia.commands.middlewares.Middlewares
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.amelia.db.MongoDB
import pw.mihou.amelia.io.Amatsuki
import pw.mihou.amelia.io.rome.FeedItem
import pw.mihou.amelia.listeners.AnnouncementModalListener
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.tasks.FeedTask
import pw.mihou.cache.Cache
import pw.mihou.cache.Cacheable
import pw.mihou.dotenv.Dotenv
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor
import pw.mihou.nexus.features.command.validation.errors.ValidationError
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

val logger = LoggerFactory.getLogger("Amelia Client") as Logger
val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
fun main() {
    Dotenv.asReflective(File(".env"), true).reflectTo(Configuration::class.java)
    logger.info("Amelia Client by Shindou Mihou")
    logger.info("Preparing to connect to the database... this may take a moment.")

    // This is to trigger the initial init from the database.
    MongoDB.client.listDatabaseNames()
    ValidationError.Companion.textTemplate = { message -> "❌ $message" }

    Amaririsu.set(object: Cache {
        override fun get(uri: String): Cacheable? {
            return Amatsuki.cache.getIfPresent(uri)
        }

        override fun set(uri: String, item: Cacheable) {
            Amatsuki.cache.put(uri, item)
        }
    })

    Nexus.commands(
        FeedsCommand,
        FeedSubscriptionCommand(subscribe = false),
        FeedSubscriptionCommand(subscribe = true),
        PingCommand,
        RegisterCommand,
        RemoveCommand,
        TestCommand
    )

    if (Configuration.DEVELOPER_SERVER != 0L) {
        Nexus.command(AnnounceCommand)
    }

    NexusCommandInterceptor.addRepository(Middlewares)

    DiscordApiBuilder()
        .setToken(Configuration.DISCORD_TOKEN)
        .addListener(Nexus)
        .addModalSubmitListener(AnnouncementModalListener)
        .addServerLeaveListener { event: ServerLeaveEvent ->
            MongoDB.client.getDatabase("amelia").getCollection("feeds").deleteMany(
                Filters.eq("server", event.server.id)
            )
        }
        .addResumeListener { event: ResumeEvent ->
            event.api.updateActivity(ActivityType.WATCHING, "People read stories!")
        }
        .addReconnectListener { event: ReconnectEvent ->
            event.api.updateActivity(ActivityType.WATCHING, "People read stories!")
        }
        .setTotalShards(1)
        .loginAllShards()
        .forEach { future -> future.thenAccept(::onShardLogin).exceptionally(ExceptionLogger.get()) }
}

private fun onShardLogin(shard: DiscordApi) {
    Nexus.sharding.set(shard)

    if (shard.currentShard == 0) {
        val commands = Nexus.commandManager.commands
        
        logger.info("Attempting to synchronize ${commands.size} commands to Discord... this may take a moment")
        Nexus.synchronizer.synchronize().join()

        logger.info("Preparing to schedule the feed updater... this will not take long!")
        scheduledExecutorService.scheduleAtFixedRate(FeedTask, 1, 10, TimeUnit.MINUTES)
    }

    shard.setAutomaticMessageCacheCleanupEnabled(true)
    shard.setMessageCacheSize(10, 60 * 60)
    shard.setReconnectDelay { it * 2 }
    shard.updateActivity(ActivityType.WATCHING, "People read stories!")

    logger.info("Connected to shard ${shard.currentShard} with ${shard.servers.size} servers.")
}

object Amelia {

    val formatter = SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss")

    fun format(item: FeedItem, feedModel: FeedModel, server: Server): String? {
        if (item.title == null || item.link == null) {
            logger.error("The title and link is not present on ${feedModel.feedUrl} with full item: $item")
            return null
        }

        val author = Amatsuki.authorFrom(item, feedModel)
        val subscribed = feedModel.mentions.joinToString(" ") { role ->
            server.getRoleById(role).map { it.mentionTag }.orElse("")
        }

        if (feedModel.format != null) {
            return feedModel.format
                .replace("{title}", item.title)
                .replace("{author}", author)
                .replace("{link}", item.link)
                .replace("{mentions}", subscribed)
        }

        return "\uD83D\uDCD6 **${item.title} by $author**\n" +
                "${item.link}\n" +
                "\n" +
                subscribed
    }

}