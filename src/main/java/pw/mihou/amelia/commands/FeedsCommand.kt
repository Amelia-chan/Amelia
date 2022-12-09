package pw.mihou.amelia.commands

import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.scheduledExecutorService
import pw.mihou.amelia.utility.redactListLink
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.command.validation.OptionValidation
import pw.mihou.nexus.features.command.validation.errors.ValidationError
import pw.mihou.nexus.features.paginator.NexusPaginatorBuilder
import pw.mihou.nexus.features.paginator.enums.NexusPaginatorButtonAssignment
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorCursor
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorEvents
import java.awt.Color
import java.util.concurrent.TimeUnit

@Suppress("UNUSED")
object FeedsCommand: NexusHandler {

    private const val name = "feeds"
    private const val description = "Views all the feeds that are registered for this server."

    private val options = listOf(
        SlashCommandOption.createLongOption("id", "The id of the feed to look-up.", false)
    )

    val validators = listOf(
        OptionValidation.create(
            collector = { event -> event.interaction.getArgumentLongValueByName("id") },
            validator = { id -> id <= 9999 },
            error = { ValidationError.create("A feed identifier has at maximum 4 digits of numbers (0-9999).") },
            requirements = OptionValidation.createRequirements {
                nonNull = createErrorableRequireSetting(ValidationError.Companion.create("You cannot leave the `feed` option."))
            }
        )
    )

    override fun onEvent(event: NexusCommandEvent) {
        val server = event.server.orElse(null)

        if (server == null) {
            event.respondNowAsEphemeral().setContent("❌ You cannot use this command in a private channel.").respond()
            return
        }

        when (event.interaction.getArgumentLongValueByName("id").isPresent) {
            true -> {
                val id = event.interaction.getArgumentLongValueByName("id").get()
                val feed = FeedDatabase.get(id)

                if (feed == null) {
                    event.respondNowAsEphemeral().setContent("❌ There are no feeds with the id of **$id**.").respond()
                    return
                }

                event.respondNowAsEphemeral().addEmbed(embed(server, feed, event.user)).respond()
            }
            false -> {
                val chunks = FeedDatabase.all(server.id).chunked(5)

                if (chunks.isEmpty()) {
                    event.respondNow().setContent("❌ There are no feeds registered on this server, please use the `/register` command to register one!").respond()
                    return
                }

                NexusPaginatorBuilder<List<FeedModel>>(chunks)
                    .setButton(NexusPaginatorButtonAssignment.NEXT, Button.secondary("", "Next"))
                    .setButton(NexusPaginatorButtonAssignment.PREVIOUS, Button.secondary("", "Previous"))
                    .setEventHandler(object: NexusPaginatorEvents<List<FeedModel>> {
                        override fun onInit(
                            updater: InteractionOriginalResponseUpdater,
                            cursor: NexusPaginatorCursor<List<FeedModel>>
                        ): InteractionOriginalResponseUpdater = updater.addEmbed(embed(server, cursor))

                        override fun onPageChange(cursor: NexusPaginatorCursor<List<FeedModel>>, event: ButtonClickEvent) {
                            event.buttonInteraction.message.edit(embed(server, cursor))
                        }
                    })
                    .build()
                    .send(event.baseEvent.interaction, event.respondLater().join())
                    .thenAccept {  instance ->
                        scheduledExecutorService.schedule({
                            instance.parent.destroy(instance.uuid)
                            instance.parent.destroy()

                            instance.message.thenAccept { message -> message.createUpdater().removeAllComponents().replaceMessage() }
                        }, 5, TimeUnit.MINUTES)
                    }
            }
        }
    }

    /**
     * Creates a description based on the available parameters of the feed model.
     *
     * @param server the server where this was executed.
     * @param feed   the feed to preview.
     * @param link   the value of the link to display.
     * @return a displayable description of the feed model.
     */
    private fun description(server: Server, feed: FeedModel, link: String): String {
        val subscribed = feed.mentions.joinToString(" ") {
                role -> server.getRoleById(role).map { it.mentionTag }.orElse("")
        }

        val channel = server.getTextChannelById(feed.channel).map { it.mentionTag }.orElse("Channel Not Found ❓")

        return ("ID: ${feed.unique}"
                + "\nLink: $link"
                + "\nName: ${feed.name}"
                + "\nRoles Subscribed: $subscribed"
                + "\nLast Update: `${feed.date}`"
                + "\nChannel: $channel"
                + "\nCreated by: <@${feed.user}>"
                + "\nStatus: ${accessible(feed.accessible)}")
    }

    private fun accessible(status: Boolean) = if (status) "**ONLINE**" else "**__ERROR__**"

    /**
     * Creates an embed based on the feed model and whether the user is authorized to see
     * the link (if the feed is of a reading list).
     *
     * @param server the server where this was executed.
     * @param feed   the feed model to preview.
     * @param user   the user who executed this.
     * @return an embed that contains all the information of the user.
     */
    private fun embed(server: Server, feed: FeedModel, user: User): EmbedBuilder {
        var link = feed.feedUrl + " [**(DO NOT LEAK)**](https://github.com/Amelia-chan/Amelia/discussions/19)"

        if (link.contains("unq=") && user.id != feed.user) {
            link = redactListLink(link)
        }

        return EmbedBuilder()
            .setTimestampToNow()
            .setColor(Color.YELLOW)
            .setTitle(feed.name)
            .setDescription(description(server, feed, link))
    }

    /**
     * Creates an embed based on the current cursor of the paginator.
     *
     * @param server the server where this was executed.
     * @param cursor the cursor from the paginator.
     * @return an embed that contains all the feeds available from the cursor.
     */
    private fun embed(server: Server, cursor: NexusPaginatorCursor<List<FeedModel>>): EmbedBuilder {
        val embed = EmbedBuilder().setTimestampToNow().setColor(Color.YELLOW).setTitle("${server.name}'s feeds")
            .setDescription("You are viewing **${cursor.displayablePosition}** out of **${cursor.maximumPages}** pages of feeds registered for this server.")
            .setFooter("Page ${cursor.displayablePosition} out of ${cursor.maximumPages}")

        for (feed in cursor.item) {
            var link = feed.feedUrl

            if (link.contains("unq=")) {
                link = redactListLink(link)
            }

            embed.addField("[${feed.unique}] ${feed.name}", description(server, feed, link))
        }

        return embed
    }


}