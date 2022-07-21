package pw.mihou.amelia.commands

import com.mongodb.client.model.Filters
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import pw.mihou.amelia.Amelia
import pw.mihou.amelia.commands.middlewares.Middlewares
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.io.rome.ReadRSS
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler

@Suppress("UNUSED")
object TestCommand: NexusHandler {

    private const val name = "test"
    private const val description = "Tests a feed to make sure that Amelia is working on that feed."

    private val options = listOf(
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "id",
            "Removes a given feed by using its unique identifier that can be found in feeds command.",
            listOf(
                SlashCommandOption.createLongOption(
                    "value",
                    "The ID of the feed to remove, can be found when using /feeds command.",
                    true
                )
            )
        ),
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "name",
            "Removes a given feed by using the name that can be found in feeds command.",
            listOf(
                SlashCommandOption.createStringOption(
                    "value",
                    "The ID of the feed to remove, can be found when using /feeds command.",
                    true
                )
            )
        )
    )

    private val middlewares = listOf(Middlewares.MODERATOR_ONLY)

    override fun onEvent(event: NexusCommandEvent) {
        val server = event.server.orElseThrow()
        var feed: FeedModel? = null
        val subcommand = event.interaction.options.first()

        if (subcommand.name == "id") {
            feed = FeedDatabase.get(subcommand.getOptionLongValueByName("value").orElseThrow())
        }

        if (subcommand.name == "name") {
            feed = FeedDatabase.connection
                .find(Filters.and(Filters.eq("server", event.serverId.orElseThrow()), Filters.text(subcommand.getOptionStringValueByName("value").orElseThrow())))
                .map { FeedModel.from(it) }
                .first()
        }

        if (feed == null) {
            event.respondNow().setContent(TemplateMessages.ERROR_FEED_NOT_FOUND).respond()
            return
        }

        event.respondLater().thenAccept { updater ->
            val latestPost = ReadRSS.getLatest(feed.feedUrl)

            if (latestPost.isEmpty()) {
                updater.setContent(TemplateMessages.ERROR_SCRIBBLEHUB_NOT_ACCESSIBLE).update()
                return@thenAccept
            }

            val channel = server.getTextChannelById(feed.channel).orElse(null)

            if (channel == null) {
                updater.setContent(TemplateMessages.ERROR_CHANNEL_NOT_FOUND).update()
                return@thenAccept
            }

            channel.sendMessage(Amelia.format(latestPost[0], feed, server)).thenAccept messageAccept@{
                updater.setContent("✅ Amelia was able to complete the testing of feed without a problem, you can find the message on ${channel.mentionTag}!").update()
                return@messageAccept
            }.exceptionally { exception ->
                updater.setContent("❌ Amelia encountered a problem while trying to send to ${channel.mentionTag}: ${exception.message}").update()

                return@exceptionally null
            }
        }

    }

}