package pw.mihou.amelia.commands

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.client.result.UpdateResult
import java.awt.Color
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import pw.mihou.amelia.commands.middlewares.Middlewares
import pw.mihou.amelia.commands.templates.TemplateMessages
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.db.methods.feeds.Feeds
import pw.mihou.amelia.db.models.FeedModel
import pw.mihou.amelia.utility.confirmationMenu
import pw.mihou.amelia.utility.redactListLink
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler

@Suppress("UNUSED")
data class FeedSubscriptionCommand(
    val subscribe: Boolean,
) : NexusHandler {
    val name = if (subscribe) "subscribe" else "unsubscribe"
    val description =
        if (subscribe) {
            "Subscribes a role to a feed to be mentioned when the feed has an update."
        } else {
            "Unsubscribes a role from a feed to no longer be mentioned when the feed has an update."
        }

    val options =
        listOf(
            SlashCommandOption.createWithOptions(
                SlashCommandOptionType.SUB_COMMAND,
                "id",
                "Subscribes a given feed by using its unique identifier that can be found in feeds command.",
                listOf(
                    SlashCommandOption.createLongOption(
                        "value",
                        "The ID of the feed to remove, can be found when using /feeds command.",
                        true,
                    ),
                    SlashCommandOption.createRoleOption(
                        "role",
                        "The role to mention when the feed has a new update.",
                        true,
                    ),
                ),
            ),
            SlashCommandOption.createWithOptions(
                SlashCommandOptionType.SUB_COMMAND,
                "name",
                "Subscribes a given feed by using the name that can be found in feeds command.",
                listOf(
                    SlashCommandOption.createStringOption(
                        "value",
                        "The ID of the feed to remove, can be found when using /feeds command.",
                        true,
                    ),
                    SlashCommandOption.createRoleOption(
                        "role",
                        "The role to mention when the feed has a new update.",
                        true,
                    ),
                ),
            ),
        )

    private val middlewares = listOf(Middlewares.MODERATOR_ONLY)

    override fun onEvent(event: NexusCommandEvent) {
        val server = event.server.orElseThrow()
        val subcommand = event.interaction.options.first()
        val role = subcommand.getArgumentRoleValueByName("role").orElseThrow()

        val feed: FeedModel? = Feeds.findFeedBySubcommand(server, subcommand)
        if (feed == null) {
            event.respondNow().setContent(TemplateMessages.ERROR_FEED_NOT_FOUND).respond()
            return
        }

        if (feed.mentions.contains(role.id) && subscribe) {
            event
                .respondNow()
                .setContent(
                    "❌ The aforementioned role is already subscribed to the feed.",
                ).respond()
            return
        }

        if (!feed.mentions.contains(role.id) && !subscribe) {
            event
                .respondNow()
                .setContent(
                    "❌ The aforementioned role is not subscribed to the feed.",
                ).respond()
            return
        }

        event.respondLater().thenAccept { updater ->
            updater.confirmationMenu(
                event.user,
                "Are you sure you want to ${if (subscribe) "subscribe" else "unsubscribe"} ${role.mentionTag} notifications " +
                    "on ${feed.name}?",
            ) { _, _, messageUpdater ->
                val result: UpdateResult? =
                    if (subscribe) {
                        FeedDatabase.connection.updateOne(
                            Filters.eq("unique", feed.unique),
                            Updates.addToSet("mentions", role.id),
                        )
                    } else {
                        FeedDatabase.connection.updateOne(
                            Filters.eq("unique", feed.unique),
                            Updates.pull("mentions", role.id),
                        )
                    }

                if (result!!.wasAcknowledged()) {
                    messageUpdater
                        .removeAllComponents()
                        .removeAllEmbeds()
                        .setEmbed(
                            EmbedBuilder()
                                .setTimestampToNow()
                                .setColor(
                                    Color.YELLOW,
                                ).setDescription(
                                    "I have ${if (subscribe) "subscribed" else "unsubscribed"} ${role.mentionTag} " +
                                        "notifications on **${feed.name}** (${
                                            if (feed.feedUrl.contains("unq=")) {
                                                redactListLink(feed.feedUrl)
                                            } else {
                                                feed.feedUrl
                                            }
                                        })!",
                                ).setAuthor(event.user),
                        ).applyChanges()
                    return@confirmationMenu
                }

                messageUpdater
                    .removeAllComponents()
                    .removeAllEmbeds()
                    .setEmbed(
                        EmbedBuilder()
                            .setTimestampToNow()
                            .setAuthor(
                                event.user,
                            ).setColor(Color.RED)
                            .setDescription(TemplateMessages.ERROR_DATABASE_FAILED),
                    ).applyChanges()
            }
        }
    }
}
