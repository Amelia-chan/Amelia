package pw.mihou.amelia.commands.notifier;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.db.UserDB;
import pw.mihou.amelia.io.AmatsukiWrapper;
import pw.mihou.amelia.models.SHUser;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.pagination.Paginate;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.events.PaginateEvent;
import tk.mihou.amatsuki.entities.user.lower.UserResults;

import java.time.Duration;

public class IAm implements VelenEvent {

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (UserDB.get(user.getId()).getAccounts().size() < 3) {
            if (args.length > 0) {
                String content = event.getMessageContent().substring(args[0].length() + 1);
                AmatsukiWrapper.getConnector().searchUser(content).thenAccept(userResults ->
                        new Paginate<>(userResults).paginate(event, new PaginateEvent<>() {
                            @Override
                            public MessageBuilder onInit(MessageCreateEvent event, UserResults currentItem,
                                                         int arrow, Paginator<UserResults> paginator) {
                                return new MessageBuilder().setEmbed(userResultEmbed(currentItem, arrow, paginator.size()));
                            }

                            @Override
                            public void onPaginate(MessageCreateEvent event, Message paginateMessage, UserResults currentItem,
                                                   int arrow, Paginator<UserResults> paginator) {
                                message.edit(userResultEmbed(currentItem, arrow, paginator.size()));
                            }

                            @Override
                            public MessageBuilder onEmptyPaginator(MessageCreateEvent event) {
                                return new MessageBuilder().setContent("**ERROR**: There are no results for the query, maybe try a deeper query?");
                            }

                            @Override
                            public void onSelect(MessageCreateEvent event, Message paginateMessage, UserResults itemSelected,
                                                 int arrow, Paginator<UserResults> paginator) {
                                itemSelected.transformToUser().thenAccept(u ->
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
                                paginateMessage.delete("End of purpose.");
                            }
                        }, Duration.ofMinutes(5)));
            } else {
                message.reply("**ERROR**: Invalid usage, please refer to `iam [username]`");
            }
        } else {
            message.reply("**ERROR**: You already have 3 accounts associated with your Discord account." +
                    "\nWe intentionally have this limit to lessen the burden our servers will go through.");
        }
    }

    private EmbedBuilder userResultEmbed(UserResults result, int arrow, int maximum) {
        return new Embed().setTitle(result.getName() + "(" + (arrow + 1) + "/" + maximum + ")").setDescription("[Click here to redirect](" + result.getUrl() + ")")
                .attachImage(result.getAvatar()).build();
    }
}
