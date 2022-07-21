package pw.mihou.amelia.commands

import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.models.FeedModel
import pw.mihou.amelia.scheduledExecutorService
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
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

    override fun onEvent(event: NexusCommandEvent) {
        val server = event.server.orElse(null)

        if (server == null) {
            event.respondNowAsEphemeral().setContent("❌ You cannot use this command in a private channel.").respond()
            return
        }

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

    private fun embed(server: Server, cursor: NexusPaginatorCursor<List<FeedModel>>): EmbedBuilder {
        val embed = EmbedBuilder().setTimestampToNow().setColor(Color.YELLOW).setTitle("${server.name}'s feeds")
            .setDescription("You are viewing **${cursor.displayablePosition}** out of **${cursor.maximumPages}** pages of feeds registered for this server.")
            .setFooter("Page ${cursor.displayablePosition} out of ${cursor.maximumPages}")

        for (feed in cursor.item) {
            embed.addField("[${feed.unique}] ${feed.name}",
                "ID: ${feed.unique}"
                        + "\nLink: ${feed.feedUrl}"
                        + "\nName: ${feed.name}"
                        + "\nRoles Subscribed: ${feed.mentions.joinToString(" ") { role -> server.getRoleById(role).map { it.mentionTag }.orElse("") }}"
                        + "\nLast Update: `${feed.date}`"
                        + "\nChannel: ${server.getTextChannelById(feed.channel).map { it.mentionTag }.orElse("Channel Not Found ❓")}"
                        + "\nCreated by: <@${feed.user}>"
            )
        }

        return embed
    }
}