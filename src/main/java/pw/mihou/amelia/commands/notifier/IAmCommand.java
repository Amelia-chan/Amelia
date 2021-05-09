package pw.mihou.amelia.commands.notifier;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.db.UserDB;
import pw.mihou.amelia.io.AmatsukiWrapper;
import pw.mihou.amelia.models.SHUser;
import pw.mihou.amelia.models.UserNavigators;
import pw.mihou.amelia.templates.Embed;
import tk.mihou.amatsuki.entities.user.lower.UserResults;

import java.util.concurrent.TimeUnit;

public class IAmCommand extends Command {

    public IAmCommand() {
        super("iam", "Associate your ScribbleHub account to your Discord account to notify you whenever you trend.", "iam [username]", false);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        if (UserDB.get(user.getId()).getAccounts().size() < 3) {
            if (args.length > 1) {
                String content = event.getMessageContent().replace(args[0] + " ", "");
                AmatsukiWrapper.getConnector().searchUser(content).thenAccept(userResults -> {
                    if (!userResults.isEmpty()) {
                        UserNavigators navigators = new UserNavigators(userResults);
                        event.getMessage().reply(userResultEmbed(navigators.current(), navigators.getArrow(), navigators.getMaximum())).thenAccept(message -> {
                            if (navigators.getMaximum() > 1) {
                                message.addReactions("⬅", "👎", "👍", "➡");
                            } else {
                                message.addReactions("👎", "👍");
                            }
                            message.addReactionAddListener(e -> {
                                if (e.getUserId() == event.getMessageAuthor().getId()) {
                                    if (e.getEmoji().equalsEmoji("➡") && navigators.getMaximum() > 1) {
                                        if (navigators.getArrow() < navigators.getMaximum() - 1) {
                                            message.edit(userResultEmbed(navigators.next(), navigators.getArrow(), navigators.getMaximum()));
                                        }
                                    } else if (e.getEmoji().equalsEmoji("⬅") && navigators.getMaximum() > 1) {
                                        if (navigators.getArrow() > 0) {
                                            message.edit(userResultEmbed(navigators.backwards(), navigators.getArrow(), navigators.getMaximum()));
                                        }
                                    } else if (e.getEmoji().equalsEmoji("👍")) {
                                        navigators.current().transformToUser().thenAccept(u ->
                                                UserDB.get(user.getId())
                                                        .getAccounts()
                                                        .stream()
                                                        .map(SHUser::getUrl)
                                                        .filter(s -> s.equalsIgnoreCase(String.format("https://www.scribblehub.com/profile/%d/amelia/", u.getUID())))
                                                        .findAny()
                                                        .ifPresentOrElse(s -> message.reply("**ERROR**: `Your account is already associated with this user`."), () -> {
                                                                    UserDB.add(user.getId(), String.format("https://www.scribblehub.com/profile/%d/amelia/", u.getUID()), u.getName());
                                                                    event.getMessage().reply(new Embed().setThumbnail(u.getAvatar()).setFooter("Created by Shindou Mihou @ patreon.com/mihou")
                                                                            .setTitle("Success!").setDescription(String.format("You will now be notified whenever any of your (%s) stories are on the frontpage (trending, top 9)!", u.getName())).build()
                                                                            .addField("When will I be notified?", "\nWe begin doing checks at 1:00 A.M (GMT+0) instead of 0:00 A.M (GMT+0) to allow trending to " +
                                                                                    "stabilize since ScribbleHub usually takes one hour to calculate all stories for trending. You will be notified at that time if your story reaches " +
                                                                                    "the top nine of the list.")
                                                                            .addField("How do I disassociate my account?", "You can do `author remove [id]` with the id being found at `author me`")
                                                                            .addField("How to check all accounts associated with my Discord?", "You can use `author me`.")
                                                                            .addField("How many accounts can I associate with my account?", "You can associate 3 unique ScribbleHub accounts per Discord account."));
                                                                }));
                                        message.delete("End of purpose.");
                                    } else if (e.getEmoji().equalsEmoji("👎")) {
                                        message.delete("End of purpose.");
                                        event.getMessage().delete();
                                    }
                                }
                                if (e.getUserId() != event.getApi().getYourself().getId()) {
                                    e.removeReaction();
                                }
                            }).removeAfter(5, TimeUnit.MINUTES).addRemoveHandler(message::removeAllReactions);
                        });
                    } else {
                        event.getMessage().reply("Error: No results found, try a deeper query.");
                    }
                });
            } else {
                event.getMessage().reply("**Usage**: `iam [username]`");
            }
        } else {
            event.getMessage().reply("**ERROR**: You already have 3 accounts associated with your Discord account." +
                    "\nWe intentionally have this limit to lessen the burden our servers will go through.");
        }
    }

    private EmbedBuilder userResultEmbed(UserResults result, int arrow, int maximum) {
        return new Embed().setTitle(result.getName() + "(" + (arrow + 1) + "/" + maximum + ")").setDescription("[Click here to redirect](" + result.getUrl() + ")")
                .attachImage(result.getAvatar()).build();
    }
}
