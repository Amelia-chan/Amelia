package pw.mihou.amelia

import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import org.javacord.api.entity.server.Server
import pw.mihou.Amaririsu
import pw.mihou.amelia.commands.*
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.amelia.db.MongoDB
import pw.mihou.amelia.db.models.FeedModel
import pw.mihou.amelia.discord.DiscordClient
import pw.mihou.amelia.io.Amatsuki
import pw.mihou.amelia.io.rome.FeedItem
import pw.mihou.amelia.logger.logger
import pw.mihou.cache.Cache
import pw.mihou.cache.Cacheable
import pw.mihou.envi.Envi
import pw.mihou.envi.adapters.dotenv.SimpleDotenvAdapter
import pw.mihou.nexus.features.command.validation.errors.ValidationError

val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

fun main() {
    Envi
        .createConfigurator(SimpleDotenvAdapter)
        .read(File(".env"), Configuration::class.java)
    logger.info("Amelia Client by Shindou Mihou")
    logger.info("Preparing to connect to the database... this may take a moment.")

    // This is to trigger the initial init from the database.
    MongoDB.client.listDatabaseNames()
    ValidationError.Companion.textTemplate = { message -> "❌ $message" }

    Amaririsu.set(
        object : Cache {
            override fun get(uri: String): Cacheable? = Amatsuki.cache.getIfPresent(uri)

            override fun set(
                uri: String,
                item: Cacheable,
            ) {
                Amatsuki.cache.put(uri, item)
            }
        },
    )

    DiscordClient.connect()
}

object Amelia {
    val formatter = SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss")

    fun format(
        item: FeedItem,
        feedModel: FeedModel,
        server: Server,
    ): String? {
        if (item.title == null || item.link == null) {
            logger.error(
                "The title and link is not present on ${feedModel.feedUrl} with full item: $item",
            )
            return null
        }

        val author = Amatsuki.authorFrom(item, feedModel)
        val subscribed =
            feedModel.mentions.joinToString(" ") { role ->
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
            subscribed
    }
}
