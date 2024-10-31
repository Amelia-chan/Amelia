package pw.mihou.amelia.commands

import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.TextInput
import org.javacord.api.entity.message.component.TextInputStyle
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionChoice
import org.javacord.api.interaction.SlashCommandOptionType
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.amelia.configuration.Configuration
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler

object AnnounceCommand : NexusHandler {
    private val name = "announce"
    private val description = "Announces a special message to all servers. (DEVELOPERS)"

    private val serverIds = listOf(Configuration.DEVELOPER_SERVER)
    private val defaultEnabledForPermissions = listOf(PermissionType.ADMINISTRATOR)

    private val options =
        listOf(
            SlashCommandOption.createWithChoices(
                SlashCommandOptionType.STRING,
                "level",
                "The urgency level that this announcement should be.",
                true,
                listOf(
                    SlashCommandOptionChoice.create("URGENT", "urgent"),
                    SlashCommandOptionChoice.create("WARNING", "warning"),
                    SlashCommandOptionChoice.create("INFO", "info"),
                ),
            ),
        )

    override fun onEvent(event: NexusCommandEvent) {
        if (!event.user.isBotOwnerOrTeamMember) {
            event.respondNowEphemerallyWith("❌ You lack the privileges to use this command.")
            return
        }

        event.interaction
            .respondWithModal(
                "announcement:${event.interaction.getArgumentStringValueByName(
                    "level",
                ).orElseThrow()}",
                "Create an announcement.",
                ActionRow.of(
                    TextInput.create(TextInputStyle.SHORT, "title", "Title"),
                ),
                ActionRow.of(
                    TextInput.create(TextInputStyle.PARAGRAPH, "contents", "Contents"),
                ),
            ).exceptionally(ExceptionLogger.get())
    }
}
