package pw.mihou.amelia.commands.support;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;
import pw.mihou.velen.utils.VelenUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Help implements VelenEvent, VelenSlashEvent {

    private EmbedBuilder helpEmbed;

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        event.getOptionStringValueByName("command").ifPresentOrElse(s -> Amelia.velen.getCommands().stream().filter(velenCommand -> velenCommand.getName().equalsIgnoreCase(s))
                .findFirst().ifPresentOrElse(cmd -> firstResponder.addEmbed(new Embed().setTitle(cmd.getName())
                                .setDescription(cmd.getDescription())
                                .build().addInlineField("Usage", cmd.getUsage())
                                .addInlineField("Alias", String.join(", ", cmd.getShortcuts()))
                                .addInlineField("Cooldown", cmd.getCooldown().toSeconds() + " seconds")).respond(),
                        () -> firstResponder.setContent("❌ Amelia couldn't find any command that is named [" + s + "], " +
                                "do you possibly mean `" + VelenUtils.getCommandSuggestion(Amelia.velen, s) + "`?").respond()), () -> firstResponder.addEmbed(helpEmbed(event.getApi())).respond());
    }

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (args.length > 0) {
            Amelia.velen.getCommands().stream().filter(velenCommand -> velenCommand.getName().equalsIgnoreCase(args[0]))
                    .findFirst().ifPresentOrElse(cmd -> message.reply(new Embed().setTitle(cmd.getName())
                            .setDescription(cmd.getDescription())
                            .build().addInlineField("Usage", cmd.getUsage())
                    .addInlineField("Alias", String.join(", ", cmd.getShortcuts()))
                            .addInlineField("Cooldown", cmd.getCooldown().toSeconds() + " seconds")),
                    () -> message.reply("❌ Amelia couldn't find any command that is named [" + args[0] + "], " +
                            "do you possibly mean `" + VelenUtils.getCommandSuggestion(Amelia.velen, args[0]) + "`?"));
        } else {
            message.reply(helpEmbed(event.getApi()));
        }
    }

    private EmbedBuilder helpEmbed(DiscordApi api) {
        if(helpEmbed == null) {
            EmbedBuilder embed = new Embed().setThumbnail(api.getYourself().getAvatar()).build();
            Amelia.velen.getCategories().forEach((s, velenCommands) -> embed.addInlineField(s, velenCommands.stream().map(VelenCommand::getName)
                    .map(s2 -> "`" + s2 + "`").collect(Collectors.joining("\n"))));

            helpEmbed = embed;
        }

        return helpEmbed;
    }
}
