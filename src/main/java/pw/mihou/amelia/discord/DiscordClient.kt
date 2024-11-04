package pw.mihou.amelia.discord

import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import org.javacord.api.DiscordApi
import org.javacord.api.entity.intent.Intent
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.amelia.discord.commands.AnnounceCommand
import pw.mihou.amelia.discord.commands.FeedSubscriptionCommand
import pw.mihou.amelia.discord.commands.FeedsCommand
import pw.mihou.amelia.discord.commands.PingCommand
import pw.mihou.amelia.discord.commands.RegisterCommand
import pw.mihou.amelia.discord.commands.RemoveCommand
import pw.mihou.amelia.discord.commands.TestCommand
import pw.mihou.amelia.discord.delegates.NexusConfigurator
import pw.mihou.amelia.discord.delegates.ReaktConfigurator
import pw.mihou.amelia.discord.listeners.ActivityListener
import pw.mihou.amelia.discord.listeners.AnnouncementModalListener
import pw.mihou.amelia.discord.listeners.CleanupServerListener
import pw.mihou.amelia.logger.logger
import pw.mihou.amelia.scheduledExecutorService
import pw.mihou.amelia.tasks.FeedTask
import pw.mihou.commons.discord.delegates.DiscordClientInterface
import pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors
import pw.mihou.nexus.features.command.validation.errors.ValidationError
import pw.mihou.nexus.features.contexts.facade.NexusContextMenuHandler
import pw.mihou.reakt.logger.adapters.FastLoggingAdapter

@Suppress("ktlint:standard:property-naming")
object DiscordClient : DiscordClientInterface {
    val NexusConfiguration: NexusConfigurator = {
        ValidationError.textTemplate = { message -> "❌ $message" }
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
            add(ActivityListener)
            add(CleanupServerListener)
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
        shard.updateActivity(Configuration.APP_ACTIVITY_TYPE, Configuration.APP_ACTIVITY)

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
        shard.updateActivity(Configuration.APP_ACTIVITY_TYPE, Configuration.APP_ACTIVITY)
    }
}
