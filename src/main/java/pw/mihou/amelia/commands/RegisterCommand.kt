package pw.mihou.amelia.commands

import org.javacord.api.entity.channel.ChannelType
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import pw.mihou.amelia.commands.middlewares.Middlewares
import pw.mihou.amelia.commands.subcommands.RegisterAuthorSubcommand
import pw.mihou.amelia.commands.subcommands.RegisterHelpSubcommand
import pw.mihou.amelia.commands.subcommands.RegisterListSubcommand
import pw.mihou.amelia.templates.TemplateMessages
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler

@Suppress("UNUSED")
object RegisterCommand : NexusHandler {

    private const val name = "register"
    private const val description = "Registers a feed from a user or a story."

    private val options = listOf(
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "user",
            "Creates a notification for all story updates of a given user from ScribbleHub.",
            listOf(
                SlashCommandOption.createStringOption("name", "The username of the author in ScribbleHub.", true),
                SlashCommandOption.createChannelOption(
                    "channel", "The channel to send updates for this feed.",
                    true, listOf(ChannelType.SERVER_TEXT_CHANNEL)
                )
            )
        ),
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "help",
            "Gets links to the official guides over how to use this command."
        ),
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "list",
            "Creates a notification for all updates of the given reading list from ScribbleHub.",
            listOf(
                SlashCommandOption.createStringOption("name", "The name to use for this feed.", true),
                SlashCommandOption.createStringOption("link", "The link to the reading list.", true),
                SlashCommandOption.createChannelOption(
                    "channel",
                    "The channel to send updates for this feed.",
                    true,
                    listOf(ChannelType.SERVER_TEXT_CHANNEL)
                )
            )
        ),
    )

    private val middlewares = listOf(Middlewares.MODERATOR_ONLY)

    override fun onEvent(event: NexusCommandEvent) {
        val subcommand = event.options.first()

        if (subcommand.name == "help") {
            RegisterHelpSubcommand.run(event)
            return
        }

        val channel = subcommand.getOptionChannelValueByName("channel").flatMap { it.asServerTextChannel() }.orElseThrow()
        if (!(channel.canYouSee() && channel.canYouWrite() && channel.canYouReadMessageHistory())) {
            event.respondNow().setContent(TemplateMessages.ERROR_CHANNEL_NOT_FOUND).respond()
            return
        }

        when(subcommand.name) {
            "author" -> RegisterAuthorSubcommand.run(event, subcommand)
            "list" -> RegisterListSubcommand.run(event, subcommand)
        }
    }

}