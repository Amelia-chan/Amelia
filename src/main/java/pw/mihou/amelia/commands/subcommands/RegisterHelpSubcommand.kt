package pw.mihou.amelia.commands.subcommands

import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import java.awt.Color

object RegisterHelpSubcommand {

    fun run(event: NexusCommandEvent) {
        event.respondNowAsEphemeral()
            .addEmbed(EmbedBuilder().setColor(Color.YELLOW)
                .setTimestampToNow()
                .setDescription(
                    "You can read all our official guides over how to use the `/register` command on the following links:" +
                            "\n- [**Creating Author Feeds**](https://github.com/Amelia-chan/Amelia/discussions/18)" +
                            "\n- [**Creating Reading List Feeds**](https://github.com/Amelia-chan/Amelia/discussions/19)"
                )
            )
            .respond()
            .exceptionally(ExceptionLogger.get())
    }

}