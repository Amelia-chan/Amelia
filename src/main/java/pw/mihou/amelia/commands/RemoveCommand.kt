package pw.mihou.amelia.commands

import com.mongodb.client.model.Filters
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import pw.mihou.amelia.commands.middlewares.Middlewares
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.amelia.utility.confirmationMenu
import pw.mihou.amelia.utility.redactListLink
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import java.awt.Color

@Suppress("UNUSED")
object RemoveCommand: NexusHandler {

    private const val name = "remove"
    private const val description = "Stops sending notifications for a given feed."

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
        var feed: FeedModel? = null
        val subcommand = event.interaction.options.first()

        if (subcommand.name == "id") {
            feed = FeedDatabase.get(subcommand.getArgumentLongValueByName("value").orElseThrow())

            if (feed != null && feed.server != event.serverId.orElseThrow()) {
                feed = null
            }
        }

        if (subcommand.name == "name") {
            feed = FeedDatabase.connection
                .find(Filters.and(Filters.eq("server", event.serverId.orElseThrow()), Filters.text(subcommand.getArgumentStringValueByName("value").orElseThrow())))
                .map { FeedModel.from(it) }
                .first()
        }

        if (feed == null) {
            event.respondNow().setContent(TemplateMessages.ERROR_FEED_NOT_FOUND).respond()
            return
        }

        event.respondLater().thenAccept { updater ->
            var link = feed.feedUrl

            if (link.contains("unq=")) {
                link = redactListLink(link)
            }

            updater.confirmationMenu(
                event.user,
                "Are you sure you want to remove **${feed.name}** ($link), please note that this action is irreversible."
            ) { _, _, messageUpdater ->
                val result = FeedDatabase.delete(feed.unique)

                if (result.wasAcknowledged()) {
                    messageUpdater.setEmbed(EmbedBuilder().setTimestampToNow().setColor(Color.YELLOW)
                        .setAuthor(event.user)
                        .setDescription("I have removed **${feed.name}** ($link) from this server's feeds!"))
                        .applyChanges()
                    return@confirmationMenu
                }

                messageUpdater
                    .removeAllComponents()
                    .removeAllEmbeds()
                    .setEmbed(EmbedBuilder().setTimestampToNow().setColor(Color.RED).setAuthor(event.user).setDescription(TemplateMessages.ERROR_DATABASE_FAILED))
                    .applyChanges()
            }
        }
    }
}