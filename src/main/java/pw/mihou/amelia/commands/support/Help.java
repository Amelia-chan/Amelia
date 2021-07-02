package pw.mihou.amelia.commands.support;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.utils.VelenUtils;

public class Help implements VelenEvent {

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (args.length > 0) {
            Amelia.velen.getCommands().stream().filter(velenCommand -> velenCommand.getName().equalsIgnoreCase(args[0]))
                    .findFirst().ifPresentOrElse(cmd -> message.reply(new Embed().setTitle(cmd.getName())
                            .setDescription(cmd.getDescription())
                            .build().addInlineField("Usage", cmd.getUsage())
                            .addInlineField("Cooldown", cmd.getCooldown().toSeconds() + " seconds")),
                    () -> message.reply("**ERROR**: We couldn't find any command that is named [" + args[0] + "], " +
                            "do you possibly mean `" + VelenUtils.getCommandSuggestion(Amelia.velen, args[0]) + "`?"));
        } else {
            message.reply(helpEmbed(event.getApi()));
        }
    }

    private EmbedBuilder helpEmbed(DiscordApi api) {
        return new Embed().setThumbnail(api.getYourself().getAvatar())
                .build()
                .addInlineField("Feeds", "`feeds`\n`subscribe`\n`unsubscribe`\n`register`\n`remove`")
                .addInlineField("Miscellaneous", "`ping`\n`invite`\n`test`\n`settings`")
                .addInlineField("Trending Notifications", "`iam`\n`author`");
    }

}
