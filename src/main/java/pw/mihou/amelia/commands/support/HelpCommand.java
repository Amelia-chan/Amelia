package pw.mihou.amelia.commands.support;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.commands.base.info.CommandMeta;
import pw.mihou.amelia.commands.base.info.Commands;
import pw.mihou.amelia.templates.Embed;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "The general command point of Amelia.", "help, help [command]", false);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        if (args.length > 1) {
            if (Commands.meta.containsKey(args[1])) {
                event.getMessage().reply(commandEmbed(Commands.getCommand(args[1])));
            }
        } else {
            event.getMessage().reply(helpEmbed(event.getApi()));
        }
    }

    private EmbedBuilder commandEmbed(CommandMeta meta) {
        return new Embed().setTitle(meta.getCommand())
                .setDescription(meta.getDescription())
                .build().addInlineField("Usage", meta.getUsage())
                .addInlineField("Cooldown", meta.getCooldown() + " seconds");
    }

    private EmbedBuilder helpEmbed(DiscordApi api) {
        return new Embed().setThumbnail(api.getYourself().getAvatar())
                .build()
                .addInlineField("Feeds", "`feeds`\n`subscribe`\n`unsubscribe`\n`register`\n`remove`")
                .addInlineField("Miscellaneous", "`ping`\n`invite`\n`test`\n`settings`")
                .addInlineField("Trending Notifications", "`iam`\n`author`");
    }
}
