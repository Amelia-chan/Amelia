package pw.mihou.amelia.discord.delegates

import java.util.concurrent.CompletionException
import kotlinx.coroutines.future.await
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.exception.MissingPermissionsException
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.amelia.discord.DiscordClient
import pw.mihou.amelia.logger.logger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.NexusConfiguration
import pw.mihou.nexus.coroutines.useCoroutines
import pw.mihou.nexus.coroutines.utils.coroutine
import pw.mihou.nexus.features.command.synchronizer.exceptions.NexusSynchronizerException
import pw.mihou.reakt.Reakt

typealias ReaktConfigurator = Reakt.Companion.() -> Unit
typealias NexusConfigurator = NexusConfiguration.() -> Unit

object DiscordClientDelegate {
    init {
        DiscordClient.ReaktConfiguration(Reakt)
        DiscordClient.NexusConfiguration(Nexus.configuration)
        Nexus.commands(*DiscordClient.Commands.toTypedArray())
        Nexus.contextMenus(*DiscordClient.ContextMenus.toTypedArray())

        Nexus.addGlobalAfterwares(*DiscordClient.GlobalAfterwares.toTypedArray())
        Nexus.addGlobalMiddlewares(*DiscordClient.GlobalMiddlewares.toTypedArray())
    }

    fun connect() {
        Nexus.useCoroutines()
        logger.info("Connecting to Discord")
        createClientBuilder()
            .loginAllShards()
            .forEach { future ->
                coroutine {
                    val shard = future.await()
                    Nexus.sharding.set(shard)
                    DiscordClient.onLogin(shard)
                }
            }
        Nexus.synchronizer
            .synchronize()
            .addTaskErrorListener {
                if (it is NexusSynchronizerException) {
                    val exception = it.exception
                    if (exception is CompletionException) {
                        if (exception.cause is MissingPermissionsException) {
                            return@addTaskErrorListener
                        }
                    }
                    return@addTaskErrorListener
                }
                logger.error("Failed to migrate commands: ", it)
            }.addFinalCompletionListener {
                logger.info("All commands are now migrated")
            }
        pw.mihou.amelia.coroutines.repeat(
            delay = DiscordClient.TickRate,
            initialDelay = DiscordClient.TickRate,
        ) {
            Nexus.express.broadcast {
                coroutine {
                    DiscordClient.onTick(it)
                }
            }
        }
    }

    private fun createClientBuilder() =
        DiscordApiBuilder()
            .setToken(Configuration.DISCORD_TOKEN)
            .setTotalShards(1)
            .setIntents(*DiscordClient.Intents.toTypedArray())
            .withListeners
}

private val DiscordApiBuilder.withListeners
    get(): DiscordApiBuilder {
        for (listener in DiscordClient.Listeners) {
            this.addListener(listener)
        }
        return this
    }
