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
import pw.mihou.amelia.templates.Message;

public class HelpCommand extends Command {

    public HelpCommand(){
        super("help", "The general command point of Amelia.", "help, help [command]", false);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        if(args.length > 1){
            if(Commands.meta.containsKey(args[1])){
                Message.msg(commandEmbed(Commands.getCommand(args[1]))).send(event.getChannel());
            }
        } else {
            Message.msg(helpEmbed(event.getApi())).send(event.getChannel());
        }
    }

    private EmbedBuilder commandEmbed(CommandMeta meta){
        return new Embed().setTitle(meta.getCommand())
                .setDescription(meta.getDescription())
                .build().addInlineField("Usage", meta.getUsage())
                .addInlineField("Cooldown", meta.getCooldown() + " seconds");
    }

    private EmbedBuilder helpEmbed(DiscordApi api){
        return new Embed().setThumbnail(api.getYourself().getAvatar())
                .build().addInlineField("Creation", "`register`")
                .addInlineField("Deletion", "`remove`")
                .addInlineField("Settings", "`settings`")
                .addInlineField("Feeds", "`feeds`\n`subscribe`\n`unsubscribe`")
                .addInlineField("Miscellaneous", "`ping`\n`invite`\n`test`");
    }
}
