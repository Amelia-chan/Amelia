package pw.mihou.amelia.commands.feeds;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.Limitations;
import pw.mihou.amelia.commands.db.FeedDB;
import pw.mihou.velen.interfaces.VelenEvent;

import java.util.stream.Collectors;

public class Modify implements VelenEvent {

    private final boolean subscribe;

    public Modify(boolean subscribe) {
        this.subscribe = subscribe;
    }

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (event.getServer().isEmpty())
            return;

        Server server = event.getServer().get();
        if (!Limitations.isLimited(server, user)) {
            pw.mihou.amelia.templates.Message.msg("You do not have permission to use this command, required permission: " +
                    "Manage Server, or lacking the required role to modify feeds.").send(event.getChannel());
            return;
        }

        if (args.length > 0) {
            if (!message.getMentionedRoles().isEmpty()) {
                try {
                    long i = Long.parseLong(args[0]);
                    if (FeedDB.validate(i)) {
                        FeedDB.getServer(server.getId()).getFeedModel(i).ifPresentOrElse(feedModel -> {
                            message.getMentionedRoles().forEach(role -> {
                                if (subscribe)
                                    feedModel.subscribeRole(role.getId());
                                else
                                    feedModel.unsubscribeRole(role.getId());
                            });

                            feedModel.update(server.getId());
                            pw.mihou.amelia.templates.Message.msg("**SUCCESS**: We have " + (subscribe ? "subscribed" :
                                    "unsubscribed") + " the following roles: " +
                                    message.getMentionedRoles().stream().map(Role::getMentionTag)
                                            .collect(Collectors.joining(" ")))
                                    .setAllowedMentions(new AllowedMentionsBuilder()
                                            .setMentionRoles(false)
                                            .setMentionEveryoneAndHere(false)
                                            .setMentionUsers(false).build())
                                    .send(event.getChannel());
                        }, () -> message.reply("**ERROR**: We couldn't find the feed with the unique id [" + i + "]." +
                                "\nPlease verify the unique id through `feeds`"));
                    } else {
                        message.reply("**ERROR**: We couldn't find the feed with the unique id [" + i + "]." +
                                "\nPlease verify the unique id through `feeds`");
                    }
                } catch (NumberFormatException | ArithmeticException e) {
                    message.reply("**ERROR**: Arithmetic or " +
                            "NumberFormatException occurred. Are you sure you giving the proper value for parameter [" + args[0] + "]?");
                }
            }
        }
    }

}
