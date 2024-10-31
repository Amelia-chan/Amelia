package pw.mihou.amelia.listeners

import java.awt.Color
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.interaction.ModalSubmitEvent
import org.javacord.api.listener.interaction.ModalSubmitListener
import pw.mihou.amelia.db.FeedDatabase
import pw.mihou.amelia.db.models.FeedModel
import pw.mihou.amelia.utility.confirmationMenu
import pw.mihou.nexus.Nexus

object AnnouncementModalListener : ModalSubmitListener {
    override fun onModalSubmit(event: ModalSubmitEvent) {
        val interaction = event.modalInteraction
        if (!interaction.customId.startsWith("announcement")) return

        if (!interaction.user.isBotOwnerOrTeamMember) {
            interaction
                .createImmediateResponder()
                .setFlags(MessageFlag.EPHEMERAL)
                .setContent("❌ You lack the privileges to use this command.")
                .respond()
            return
        }

        val components = interaction.customId.split(":")
        val title = interaction.getTextInputValueByCustomId("title").orElseThrow()
        val contents = interaction.getTextInputValueByCustomId("contents").orElseThrow()

        val embed =
            EmbedBuilder()
                .setColor(
                    when (components[1]) {
                        "urgent" -> Color.RED
                        "warning" -> Color.YELLOW
                        else -> Color.GREEN
                    },
                ).setTitle(title)
                .setDescription(contents)
                .setAuthor(interaction.user)
                .setTimestampToNow()

        val channelsGroupedServer =
            FeedDatabase.connection
                .find()
                .map { FeedModel.from(it) }
                .groupBy { it.server }
                .mapValues { it.value.map { feed -> feed.channel }.toHashSet() }

        val channelCount =
            run {
                var channelCount = 0

                for (channels in channelsGroupedServer.values) {
                    channelCount += channels.size
                }

                channelCount
            }

        val serverCount = channelsGroupedServer.size

        interaction.respondLater().thenAccept { updater ->
            updater.confirmationMenu(
                user = interaction.user,
                confirmation = "Are you sure you want to send the below embed to $channelCount channels and $serverCount servers?",
                additionalEmbeds = listOf(embed),
            ) { _, _, messageUpdater ->
                var messagesSent = 0
                messageUpdater
                    .setContent(
                        "<a:manaWinterLoading:880162110947094628> Sending message to $channelCount channels and $serverCount servers... this may take awhile.",
                    ).applyChanges()

                for ((serverId, channels) in channelsGroupedServer) {
                    Nexus.express
                        .await(serverId)
                        .thenAccept { server ->
                            for (channelId in channels) {
                                server.getTextChannelById(channelId).ifPresent { channel ->
                                    channel.sendMessage(embed)
                                    messagesSent++
                                }
                            }
                        }.join()
                }

                messageUpdater
                    .setContent(
                        "✅ A total of $messagesSent messgaes was sent successfully.",
                    ).applyChanges()
            }
        }
    }
}
