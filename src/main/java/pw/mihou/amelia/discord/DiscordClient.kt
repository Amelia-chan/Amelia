package pw.mihou.amelia.discord

import com.mongodb.client.model.Filters
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import org.javacord.api.DiscordApi
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.intent.Intent
import org.javacord.api.listener.connection.ReconnectListener
import org.javacord.api.listener.connection.ResumeListener
import org.javacord.api.listener.server.ServerLeaveListener
import pw.mihou.amelia.commands.AnnounceCommand
import pw.mihou.amelia.commands.FeedSubscriptionCommand
import pw.mihou.amelia.commands.FeedsCommand
import pw.mihou.amelia.commands.PingCommand
import pw.mihou.amelia.commands.RegisterCommand
import pw.mihou.amelia.commands.RemoveCommand
import pw.mihou.amelia.commands.TestCommand
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.amelia.db.MongoDB
import pw.mihou.amelia.discord.delegates.NexusConfigurator
import pw.mihou.amelia.discord.delegates.ReaktConfigurator
import pw.mihou.amelia.listeners.AnnouncementModalListener
import pw.mihou.amelia.logger.logger
import pw.mihou.amelia.scheduledExecutorService
import pw.mihou.amelia.tasks.FeedTask
import pw.mihou.commons.discord.delegates.DiscordClientInterface
import pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors
import pw.mihou.nexus.features.contexts.facade.NexusContextMenuHandler
import pw.mihou.reakt.logger.adapters.FastLoggingAdapter

@Suppress("ktlint:standard:property-naming")
object DiscordClient : DiscordClientInterface {
    val NexusConfiguration: NexusConfigurator = {
        // You can configure the settings of Nexus here to make things easier.
    }

    val ReaktConfiguration: ReaktConfigurator = {
        this.logger = FastLoggingAdapter
    }

    val Commands =
        buildList {
            add(FeedsCommand)
            add(FeedSubscriptionCommand(subscribe = false))
            add(FeedSubscriptionCommand(subscribe = true))
            add(PingCommand)
            add(RegisterCommand)
            add(RemoveCommand)
            add(TestCommand)

            if (Configuration.DEVELOPER_SERVER != 0L) {
                add(AnnounceCommand)
            }
        }

    val ContextMenus = buildList<NexusContextMenuHandler<*, *>> { }
    val Intents = buildList<Intent> {}
    val Listeners =
        buildList {
            add(AnnouncementModalListener)
            add(
                ResumeListener { event ->
                    event.api.updateActivity(ActivityType.WATCHING, "People read stories!")
                },
            )
            add(
                ReconnectListener { event ->
                    event.api.updateActivity(ActivityType.WATCHING, "People read stories!")
                },
            )
            add(
                ServerLeaveListener { event ->
                    MongoDB.client.getDatabase("amelia").getCollection("feeds").deleteMany(
                        Filters.eq("server", event.server.id),
                    )
                },
            )
        }

    var GlobalMiddlewares = buildList<String> { }
    var GlobalAfterwares =
        buildList {
            add(NexusCommonInterceptors.NEXUS_LOG)
        }

    /**
     * You can add additional tasks to perform when a shard logs in, such as logging, or
     * configuring parts of the shard here.
     */
    fun onLogin(shard: DiscordApi) {
        if (shard.currentShard == 0) {
            logger.info("Preparing to schedule the feed updater... this will not take long!")
            scheduledExecutorService.scheduleAtFixedRate(FeedTask, 1, 10, TimeUnit.MINUTES)
        }

        shard.setAutomaticMessageCacheCleanupEnabled(true)
        shard.setMessageCacheSize(10, 60 * 60)
        shard.setReconnectDelay { it * 2 }
        shard.updateActivity(ActivityType.WATCHING, "People read stories!")

        logger.info("Connected as ${shard.yourself.name}.")
        logger.info("Connected to shard ${shard.currentShard} with ${shard.servers.size} servers.")
    }

    /**
     * The [TickRate] dictates how often the [onTick] will execute.
     */
    val TickRate = 1.minutes

    /**
     * [onTick] is a task that is executed on all shards every [TickRate] and is used to perform tasks
     * such as changing the bot's status messages, or even doing some occasional checks that you may want to do.
     */
    suspend fun onTick(shard: DiscordApi) {
        shard.updateActivity(ActivityType.WATCHING, "People read stories!")
    }
}
