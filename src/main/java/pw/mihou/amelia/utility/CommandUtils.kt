package pw.mihou.amelia.utility

import java.awt.Color
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageUpdater
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.util.logging.ExceptionLogger

fun InteractionOriginalResponseUpdater.confirmationMenu(
    user: User,
    confirmation: String,
    additionalEmbeds: List<EmbedBuilder> = listOf(),
    onConfirmed: (
        message: Message,
        buttonEvent: ButtonClickEvent,
        messageUpdater: MessageUpdater,
    ) -> Unit,
) {
    addEmbed(
        EmbedBuilder()
            .setTimestampToNow()
            .setColor(
                Color.RED,
            ).setAuthor(user)
            .setDescription(confirmation),
    )

    for (embed in additionalEmbeds) {
        addEmbed(embed)
    }

    addComponents(
        ActionRow.of(
            Button.primary("continue", "Continue"),
            Button.danger("cancel", "Cancel"),
        ),
    )

    update().thenAccept { message ->
        message.addButtonClickListener { buttonEvent ->
            buttonEvent.buttonInteraction.acknowledge().exceptionally(ExceptionLogger.get())

            if (buttonEvent.interaction.user.id != user.id) {
                return@addButtonClickListener
            }

            message.buttonClickListeners.forEach { message.removeMessageAttachableListener(it) }
            when (buttonEvent.buttonInteraction.customId) {
                "cancel" -> {
                    message.delete()
                }
                "continue" -> {
                    onConfirmed.invoke(
                        message,
                        buttonEvent,
                        message
                            .createUpdater()
                            .removeAllComponents()
                            .removeAllEmbeds(),
                    )
                }
            }
        }
    }
}
