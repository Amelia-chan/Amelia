package dev.amelia.commands

import dev.amelia.repositories.AmeMiddlewares
import org.javacord.api.interaction.SlashCommandInteractionOption
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors

object RegisterCommand: NexusHandler {

    private val name: String = "register"
    private val description = "Subscribes one user or story's RSS feed to a given channel."
    private val options: List<SlashCommandOption> = listOf(
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "story",
            "Subscribes one story's RSS feed to a given channel.",
            listOf(
                SlashCommandOption.create(
                    SlashCommandOptionType.CHANNEL,
                    "channel",
                    "The channel to send future updates of the story towards.",
                    true
                ),
                SlashCommandOption.create(
                    SlashCommandOptionType.STRING,
                    "name",
                    "The name of the story to search from ScribbleHub.",
                    true
                )
            )
        ),
        SlashCommandOption.createWithOptions(
            SlashCommandOptionType.SUB_COMMAND,
            "user",
            "Subscribes one user's RSS feed to a given channel.",
            listOf(
                SlashCommandOption.create(
                    SlashCommandOptionType.CHANNEL,
                    "channel",
                    "The channel to send future updates of the user's stories towards.",
                    true
                ),
                SlashCommandOption.create(
                    SlashCommandOptionType.STRING,
                    "name",
                    "The name of the user to search from ScribbleHub.",
                    true
                )
            )
        )
    )

    private val middlewares: List<String> = listOf(
        NexusCommonInterceptors.NEXUS_GATE_SERVER,
        AmeMiddlewares.AMELIA_MODERATOR_ONLY
    )

    override fun onEvent(event: NexusCommandEvent) {

    }

    private fun onStorySubcommand(event: NexusCommandEvent, subcommand: SlashCommandInteractionOption) {
        val channel = subcommand.getOptionChannelValueByName("channel").orElseThrow()
        val name = subcommand.getOptionStringValueByName("name").orElseThrow()
    }

}