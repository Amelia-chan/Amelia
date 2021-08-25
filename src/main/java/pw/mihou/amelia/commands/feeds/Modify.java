package pw.mihou.amelia.commands.feeds;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.commands.Limitations;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;

import java.util.List;
import java.util.stream.Collectors;

import static pw.mihou.amelia.templates.TemplateMessages.*;

public class Modify implements VelenEvent, VelenSlashEvent {

    private final boolean subscribe;

    public Modify(boolean subscribe) {
        this.subscribe = subscribe;
    }

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();

        if(!Limitations.isLimited(server, user)) {
            firstResponder.setContent(ERROR_MISSING_PERMISSIONS).setFlags(MessageFlag.EPHEMERAL).respond();
            return;
        }

        int feedId = event.getOptionIntValueByName("feed").orElseThrow();
        Role role = event.getOptionRoleValueByName("role").orElseThrow();

        event.respondLater().thenAccept(updater -> FeedDB.getServer(server.getId()).getFeedModel(feedId).ifPresentOrElse(feedModel -> {
            if(subscribe) {
                feedModel.subscribeRole(role.getId());
            }

            if(!subscribe) {
                feedModel.unsubscribeRole(role.getId());
            }

            feedModel.update(server.getId());
            updater.setContent("✔ We have " + (subscribe ? "subscribed" : "unsubscribed") + " the following roles: " + role.getMentionTag())
                    .setAllowedMentions(new AllowedMentionsBuilder()
                            .setMentionRoles(false)
                            .setMentionEveryoneAndHere(false)
                            .setMentionUsers(false).build())
                    .update();
        }, () -> updater.setContent(ERROR_FEED_NOT_FOUND).update()));
    }

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();

        if (args.length > 0) {
            if (!message.getMentionedRoles().isEmpty()) {
                try {
                    long i = Long.parseLong(args[0]);
                    FeedDB.getServer(server.getId()).getFeedModel(i).ifPresentOrElse(feedModel -> {
                        message.getMentionedRoles().forEach(role -> {
                            if(subscribe) {
                                feedModel.subscribeRole(role.getId());
                            }

                            if(!subscribe) {
                                feedModel.unsubscribeRole(role.getId());
                            }
                        });

                        feedModel.update(server.getId());
                        pw.mihou.amelia.templates.Message.msg("✔ We have " + (subscribe ? "subscribed" : "unsubscribed") + " the following roles: " +
                                        message.getMentionedRoles().stream().map(Role::getMentionTag).collect(Collectors.joining(" ")))
                                .setAllowedMentions(new AllowedMentionsBuilder()
                                        .setMentionRoles(false)
                                        .setMentionEveryoneAndHere(false)
                                        .setMentionUsers(false).build())
                                .send(event.getChannel());
                    }, () -> message.reply(ERROR_FEED_NOT_FOUND));
                } catch (NumberFormatException | ArithmeticException e) {
                    message.reply(ERROR_INT_ABOVE_LIMIT);
                }
            }
        }
    }
}
