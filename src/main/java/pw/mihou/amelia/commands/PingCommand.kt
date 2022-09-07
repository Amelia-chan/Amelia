package pw.mihou.amelia.commands

import com.sun.management.OperatingSystemMXBean
import org.javacord.api.entity.message.embed.EmbedBuilder
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.session.AmeliaSession
import pw.mihou.amelia.tasks.FeedTask
import pw.mihou.amelia.utility.StringUtils
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import java.awt.Color
import java.lang.management.ManagementFactory
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

@Suppress("UNUSED")
object PingCommand: NexusHandler {

    private const val name = "ping"
    private const val description = "Shows metrics and information about Amelia."

    private val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    override fun onEvent(event: NexusCommandEvent) {
        event.respondLater().thenAccept { updater ->
            event.api.measureRestLatency().thenAccept { restLatency ->
                val gatewayLatency = event.api.latestGatewayLatency.toMillis()

                val feeds = FeedDatabase.connection.find()
                    .map { FeedModel.from(it) }
                    .filter { !it.feedUrl.contains("?type=series&sid=") }
                    .map { it.accessible }

                val inaccessibleFeedsCount = feeds.count { !it }

                updater.addEmbed(
                    EmbedBuilder().setTimestampToNow()
                        .setColor(Color.YELLOW)
                        .setThumbnail(event.api.yourself.avatar)
                        .setTitle("Ping pong!")
                        .setDescription("Here are my current metrics and information for this session!")
                        .addField("Latency", StringUtils.createEmbeddedFormat(
                            "⏰ Gateway: `" + format(gatewayLatency) + "ms`",
                            "🕚 REST: `" + format(restLatency.toMillis()) + "ms`"
                        ))
                        .addField("Bot Information", StringUtils.createEmbeddedFormat(
                            "🕰 Uptime: `" + uptime() + "`",
                            "💻 Servers: `" + event.api.servers.size + " servers`",
                            "💿 Memory: `" + format(getUsedMemory()) + " MB / " + format(os.totalMemorySize / (1000 * 1000)) + " MB`"
                        ))
                        .addField("ScribbleHub Status", StringUtils.createEmbeddedFormat(
                            "\uD83D\uDD8B️ Author Feeds: ${booleanToEmoji(FeedTask.canAccessAuthor())}",
                            "\uD83D\uDCD6 Story Feeds: Unsupported ([Statement from Tony](https://forum.scribblehub.com/threads/cloudflare-blocking-rss-feeds.11117/post-243945))",
                            "‼️ Inaccessible Feeds: $inaccessibleFeedsCount out of ${feeds.size}"
                        ))
                        .addField("Session Information", "☁ Total updates sent: `" + format(AmeliaSession.feedsUpdated.get().toLong()) + " chapters notified to servers`")
                        .addField("Inquiries", "You can send inquiries about Amelia like custom private bot, etc. on our email at **amelia@mihou.pw**!")
                ).update()
            }
        }
    }

    private fun booleanToEmoji(boolean: Boolean) = if (boolean) "✅" else "❌"


    private fun getUsedMemory(): Long {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1000 * 1000)
    }

    fun uptime(): String {
        val uptime = ManagementFactory.getRuntimeMXBean().uptime
        return String.format(
            "%d days, %d hours, %d minutes, %d seconds",
            TimeUnit.MILLISECONDS.toDays(uptime),
            TimeUnit.MILLISECONDS.toHours(uptime) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(uptime)),
            TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptime)),
            TimeUnit.MILLISECONDS.toSeconds(uptime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uptime))
        )
    }

    /**
     * Formats the number into human-readable content.
     *
     * @param number The number to format.
     * @return The formatted string.
     */
    private fun format(number: Long): String? {
        return NumberFormat.getInstance().format(number)
    }

}