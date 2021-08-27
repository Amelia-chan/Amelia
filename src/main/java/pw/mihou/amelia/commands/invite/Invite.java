package pw.mihou.amelia.commands.invite;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;

import java.util.List;

public class Invite implements VelenEvent, VelenSlashEvent {

    private static final EmbedBuilder embed = new Embed().setTitle("Your Majesty wishes to invite me?")
            .setThumbnail("https://media.discordapp.net/avatars/786464598835986483/8175d0e1793e99b786032be669537a4c.png?size=4096")
            .setDescription("You can invite me freely by pressing the button below!").build();

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        new MessageBuilder()
                .setEmbed(embed)
                .addActionRow(
                        Button.link(
                        "https://discord.com/api/oauth2/authorize?client_id=786464598835986483&permissions=67488832&scope=bot%20applications.commands&prompt=consent",
                        "Invite Now",
                        "💘")
                )
                .send(event.getChannel());
    }

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        firstResponder.addEmbed(embed)
                .addComponents(
                        ActionRow.of(
                                Button.link(
                                        "https://discord.com/api/oauth2/authorize?client_id=786464598835986483&permissions=67488832&scope=bot%20applications.commands&prompt=consent",
                                        "Invite Now",
                                        "💘")
                        )
                ).respond();
    }

}
