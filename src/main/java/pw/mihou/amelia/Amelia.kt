package pw.mihou.amelia

import ch.qos.logback.classic.Logger
import com.mongodb.client.model.Filters
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.server.Server
import org.javacord.api.event.connection.ReconnectEvent
import org.javacord.api.event.connection.ResumeEvent
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.event.server.ServerLeaveEvent
import org.javacord.api.util.logging.ExceptionLogger
import org.slf4j.LoggerFactory
import pw.mihou.amelia.commands.*
import pw.mihou.amelia.commands.middlewares.Middlewares
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.amelia.db.MongoDB
import pw.mihou.amelia.io.Amatsuki
import pw.mihou.amelia.io.rome.ItemWrapper
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.tasks.FeedTask
import pw.mihou.dotenv.Dotenv
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

val nexus: Nexus = Nexus.builder().build()
val logger = LoggerFactory.getLogger("Amelia Client") as Logger
val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() / 2).also {
    logger.info("A scheduler has been created with a total core count of ${Runtime.getRuntime().availableProcessors() / 2}.")
}

fun main() {
    Dotenv.asReflective(File(".env"), true).reflectTo(Configuration::class.java)
    logger.info("Amelia Client" + "\nby Shindou Mihou")
    logger.info("Preparing to connect to the database... this may take a moment.")

    // This is to trigger the initial init from the database.
    MongoDB.client.listDatabaseNames()

    nexus.listenMany(
        FeedsCommand,
        FeedSubscriptionCommand(subscribe = false),
        FeedSubscriptionCommand(subscribe = true),
        PingCommand,
        RegisterCommand,
        RemoveCommand,
        TestCommand
    )
    NexusCommandInterceptor.addRepository(Middlewares)

    DiscordApiBuilder()
        .setToken(Configuration.DISCORD_TOKEN)
        .addListener(nexus)
        .addServerLeaveListener { event: ServerLeaveEvent ->
            MongoDB.client.getDatabase("amelia").getCollection("feeds").deleteMany(
                Filters.eq("server", event.server.id)
            )
        }
        .addMessageCreateListener { event: MessageCreateEvent ->
            if (event.messageContent == "<@${event.api.yourself.id}>") {
                event.message.reply(
                    "Hello, if you are wondering why message commands are no longer working, it is because Amelia has " +
                        "moved to slash commands completely to work better with Discord. " +
                            "\n\nIf you have any inquiries (e.g. custom bot instance) then " +
                            "please contact my developer at **amelia@mihou.pw**!"
                )
                return@addMessageCreateListener
            }

            // TODO: Remove this section when September hits.
            if (event.messageContent.startsWith("a.register")
                || event.messageContent.startsWith("a.subscribe")
                || event.messageContent.startsWith("a.unsubscribe")
                || event.messageContent.startsWith("a.test")
                || event.messageContent.startsWith("a.feeds")
                || event.messageContent.startsWith("a.remove")
                || event.messageContent.startsWith("a.help")) {
                event.message.reply(
                    "Hello, if you are wondering why message commands are no longer working, it is because Amelia has " +
                            "moved to slash commands completely to work better with Discord. " +
                            "\n\nIf you have any inquiries (e.g. custom bot instance) then " +
                            "please contact my developer at **amelia@mihou.pw**!"
                )
            }
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
    nexus.shardManager.put(shard)

    if (shard.currentShard == 0) {
        val commands = nexus.commandManager.commands
            .filter { nexusCommand: NexusCommand -> nexusCommand.serverIds.isEmpty() }
            .map { obj: NexusCommand -> obj.asSlashCommand() }
            .toList()

        logger.info("Attempting to synchronize ${commands.size} commands to Discord... this may take a moment")
        shard.bulkOverwriteGlobalApplicationCommands(commands)
            .thenAccept {
                logger.info("Synchronized ${commands.size} commands with Discord!")
            }.exceptionally(ExceptionLogger.get())

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

    fun format(item: ItemWrapper, feedModel: FeedModel, server: Server): String {
        if (item.valid()) return "\uD83D\uDCD6 **{title} by {author}**\n{link}\n\n{subscribed}"
            .replace("{title}", item.title)
            .replace("{author}", Amatsuki.authorFrom(item, feedModel))
            .replace("{link}", item.link)
            .replace("{subscribed}", feedModel.mentions.joinToString(" ") { role ->
                server.getRoleById(role).map { it.mentionTag }.orElse("")
            })

        logger.error("The title and link is not present on ${feedModel.feedUrl} with full item: $item")
        return ""
    }

}