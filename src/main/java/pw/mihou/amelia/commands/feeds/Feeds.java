package pw.mihou.amelia.commands.feeds;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.Amelia;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.amelia.models.FeedModel;
import pw.mihou.amelia.models.FeedNavigator;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Feeds implements VelenEvent, VelenSlashEvent {

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        if(event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();
        event.respondLater().thenAccept(updater -> {
            FeedNavigator navigator = new FeedNavigator(FeedDB.getServer(server.getId()).getModels());
            if(!navigator.getModels().isEmpty()) {
                String unique = UUID.randomUUID().toString();
                ActionRow actionRow;

                if (navigator.getMaximumPage() > 1) {
                    actionRow = ActionRow.of(
                            Button.primary(unique+"-BACK", "⬅"),
                            Button.danger(unique+"-CANCEL", "🗑"),
                            Button.primary(unique+"-NEXT", "➡")
                    );
                } else {
                    actionRow = ActionRow.of(
                            Button.danger(unique+"-CANCEL", "🗑")
                    );
                }

                updater.addEmbed(embed(server, navigator.current().orElse(new ArrayList<>()), 1))
                        .addComponents(actionRow).update()
                        .thenAccept(message -> message.addButtonClickListener(e -> {
                            if (e.getButtonInteraction().getUser().getId() != user.getId())
                                return;

                            String c = e.getButtonInteraction().getCustomId();
                            if(c.equals(unique+"-BACK") && navigator.canReverse()) {
                                message.edit(embed(server, navigator.backwards().orElse(new ArrayList<>()), navigator.getPage()));
                            }

                            if(c.equals(unique+"-NEXT") && navigator.hasNext()) {
                                message.edit(embed(server, navigator.next().orElse(new ArrayList<>()), navigator.getPage()));
                            }

                            if(c.equals(unique+"-CANCEL")) {
                                message.delete();
                                navigator.reset();
                            }

                        }).removeAfter(5, TimeUnit.MINUTES)
                                .addRemoveHandler(() -> new MessageUpdater(message).removeAllComponents().applyChanges()));
            } else {
                updater.addEmbed(embed(server, new ArrayList<>(), 1)).update();
            }
        });
    }

    @Override
    public void onEvent(MessageCreateEvent event, Message msg, User user, String[] args) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();
        FeedNavigator navigator = new FeedNavigator(FeedDB.getServer(server.getId()).getModels());
        if (!navigator.getModels().isEmpty()) {
            String unique = UUID.randomUUID().toString();
            ActionRow actionRow;

            if (navigator.getMaximumPage() > 1) {
                actionRow = ActionRow.of(
                        Button.primary(unique+"-BACK", "⬅"),
                        Button.danger(unique+"-CANCEL", "🗑"),
                        Button.primary(unique+"-NEXT", "➡")
                );
            } else {
                actionRow = ActionRow.of(
                        Button.danger(unique+"-CANCEL", "🗑")
                );
            }

            new MessageBuilder().addEmbed(embed(server, navigator.current().orElse(new ArrayList<>()), 1))
                    .addComponents(actionRow)
                    .send(event.getChannel())
                    .thenAccept(message -> message.addButtonClickListener(e -> {
                                if (e.getButtonInteraction().getUser().getId() != user.getId())
                                    return;

                                String c = e.getButtonInteraction().getCustomId();
                                if(c.equals(unique+"-BACK") && navigator.canReverse()) {
                                    message.edit(embed(server, navigator.backwards().orElse(new ArrayList<>()), navigator.getPage()));
                                }

                                if(c.equals(unique+"-NEXT") && navigator.hasNext()) {
                                    message.edit(embed(server, navigator.next().orElse(new ArrayList<>()), navigator.getPage()));
                                }

                                if(c.equals(unique+"-CANCEL")) {
                                    message.delete();
                                    navigator.reset();
                                }

                            }).removeAfter(5, TimeUnit.MINUTES)
                            .addRemoveHandler(() -> new MessageUpdater(message).removeAllComponents().applyChanges()));
        } else {
            msg.reply(embed(server, new ArrayList<>(), 1));
        }
    }

    private EmbedBuilder embed(Server server, ArrayList<FeedModel> objects, int page) {
        EmbedBuilder embed = new Embed().setTitle(server.getName() + "'s feeds")
                .setFooter("Page: " + page)
                .setDescription(!objects.isEmpty() ? "Here are the feeds registered on the server." : "The server has no feeds registered.")
                .build();

        if (!objects.isEmpty()) {
            for (FeedModel object : objects) {
                embed.addField("[" + object.getUnique() + "] " + object.getName(), "\n" +
                        "\nLink: " + object.getFeedURL() +
                        "\nFeed Unique ID: `" + object.getUnique() +
                        "`\nFeed Name: `" + object.getName() +
                        "`\nRoles Subscribed: " + Amelia.getMentions(object.getMentions(), server) +
                        "\nLast Update: `" + object.getDate().toString() +
                        "`\nAssigned Channel: " + server.getTextChannelById(object.getChannel())
                        .map(ServerTextChannel::getMentionTag).orElse("❓ Channel Not Found") +
                        "\nCreated by: " + server.getMemberById(object.getUser()).map(user -> "<@" + user.getId() + ">").orElse("❓ User Not Found (possibly left)."));
            }
        }

        return embed;
    }
}
