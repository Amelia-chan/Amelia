package dev.amelia

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dev.amelia.configuration.AmeConfiguration
import dev.amelia.logging.AmeErrorHandler
import dev.amelia.repositories.AmeMiddlewares
import dev.amelia.repositories.AmeSchedulerRepository
import io.sentry.Sentry
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.Javacord
import org.javacord.api.entity.activity.ActivityType
import org.javacord.api.entity.intent.Intent
import org.slf4j.LoggerFactory
import pw.mihou.dotenv.Dotenv
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor
import java.io.File
import java.util.Date

val NEXUS = Nexus.builder().build()
val LOGGER = LoggerFactory.getLogger("Ame-chan") as Logger
val MOSHI = Moshi.Builder()
    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
    .build()

val MONGO_CLIENT: MongoClient = MongoClients.create(
    MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(AmeConfiguration.MONGO_URI))
        .retryReads(true)
        .retryWrites(true)
        .build()
)

fun main() {
    Dotenv.asReflective(File(".env"), true).reflectTo(AmeConfiguration::class.java)
    NexusCommandInterceptor.addRepository(AmeMiddlewares)

    LOGGER.level = Level.DEBUG
    Sentry.init {
        it.dsn = AmeConfiguration.SENTRY_DSN
        it.serverName = "Ame-chan"
        it.release = "1.0.0"
        it.environment = if (AmeConfiguration.PRODUCTION) "PRODUCTION" else "SNAPSHOT"
    }

    println(
        """
        $BANNER
        ♥ Javacord: ${Javacord.VERSION} ♥
        ♥ Discord API Version: ${Javacord.DISCORD_API_VERSION} ♥
        ♥ Gateway: ${Javacord.DISCORD_GATEWAY_VERSION} ♥
        ♥ Display Version: ${Javacord.DISPLAY_VERSION} ♥
        ♥ Environment: ${ if (AmeConfiguration.PRODUCTION) "PRODUCTION" else "SNAPSHOT" } ♥
        """.trimIndent()
    )

    // This is done to warm-up the database connection beforehand, ensuring
    // speedy results afterwards.
    MONGO_CLIENT.listDatabaseNames()

    DiscordApiBuilder()
        .setToken(AmeConfiguration.DISCORD_TOKEN)
        .setAllIntentsExcept(
            Intent.GUILD_MESSAGE_TYPING,
            Intent.DIRECT_MESSAGE_TYPING,
            Intent.GUILD_INTEGRATIONS,
            Intent.GUILD_WEBHOOKS,
            Intent.GUILD_INVITES,
            Intent.GUILD_INTEGRATIONS,
            Intent.GUILD_BANS,
            Intent.GUILD_PRESENCES,
            Intent.GUILD_EMOJIS
        )
        .setUserCacheEnabled(false)
        .setTotalShards(AmeConfiguration.DISCORD_SHARDS)
        .addListener(NEXUS)
        .loginAllShards()
        .forEach { future -> future.thenAccept { Ame.onShardLogin(it) }.exceptionally(AmeErrorHandler::accept) }
}

object Ame {
    fun onShardLogin(shard: DiscordApi) {
        NEXUS.shardManager.put(shard)
        LOGGER.info(
            "Ame-chan has logged onto shard ${shard.currentShard} with size of ${shard.servers.size}."
        )

        shard.updateActivity(ActivityType.WATCHING, "I am dreaming of the stars!")
        shard.setAutomaticMessageCacheCleanupEnabled(true)
        shard.setMessageCacheSize(10, 60 * 60)
        shard.setReconnectDelay { it * 2 }

        AmeSchedulerRepository.scheduleOnce()
    }
}

private const val BANNER = "(っ◔◡◔)っ ♥ Ame-chan ♥"