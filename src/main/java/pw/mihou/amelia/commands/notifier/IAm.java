package pw.mihou.amelia.commands.notifier;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.amelia.db.UserDB;
import pw.mihou.amelia.io.AmatsukiWrapper;
import pw.mihou.amelia.models.SHUser;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;
import pw.mihou.velen.pagination.Paginate;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.events.PaginateButtonEvent;
import tk.mihou.amatsuki.entities.user.lower.UserResults;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class IAm implements VelenEvent, VelenSlashEvent {

    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        if(event.getFirstOptionStringValue().isEmpty())
            return;

        // Prevent more than three accounts.
        if (UserDB.get(user.getId()).getAccounts().size() > 3) {
            firstResponder.setContent("**ERROR**: You already have 3 accounts associated with your Discord account." +
                    "\nWe intentionally have this limit to lessen the burden our servers will go through.").setFlags(MessageFlag.EPHEMERAL).respond();
            return;
        }

        String content = event.getFirstOptionStringValue().get();
        event.respondLater().thenAccept(updater -> AmatsukiWrapper.getConnector().searchUser(content).thenAccept(userResults ->
                new Paginate<>(userResults).paginateWithButtons(UUID.randomUUID().toString().replaceAll("-", ""), originalEvent.getInteraction(), new
                        PaginateButtonEvent<>() {
                            @Override
                            public void onSelect(InteractionImmediateResponseBuilder responder, Interaction event, Message paginateMessage, UserResults u, int arrow, Paginator<UserResults> paginator) {
                                int uid = Integer.parseInt(u.getUrl().replaceAll("[^\\d]", ""));
                                UserDB.get(user.getId())
                                        .getAccounts()
                                        .stream()
                                        .map(SHUser::getUrl)
                                        .filter(s -> s.equalsIgnoreCase(String.format("https://www.scribblehub.com/profile/%d/amelia/", uid)))
                                        .findAny()
                                        .ifPresentOrElse(s -> responder.setContent("**ERROR**: `Your account is already associated with this user`.").respond(), () -> {
                                            UserDB.add(user.getId(), String.format("https://www.scribblehub.com/profile/%d/amelia/", uid), u.getName());
                                            responder.addEmbed(new Embed().setThumbnail(u.getAvatar()).setFooter("Created by Shindou Mihou @ patreon.com/mihou")
                                                            .setTitle("Success!").setDescription(String.format("You will now be notified whenever any of your (%s) stories are on the frontpage (trending, top 9)!", u.getName())).build()
                                                            .addField("When will I be notified?", "\nWe begin doing checks at 1:00 A.M (GMT+0) instead of 0:00 A.M (GMT+0) to allow trending to " +
                                                                    "stabilize since ScribbleHub usually takes one hour to calculate all stories for trending. You will be notified at that time if your story reaches " +
                                                                    "the top nine of the list.")
                                                            .addField("How do I disassociate my account?", "You can do `author remove [id]` with the id being found at `author me`")
                                                            .addField("How to check all accounts associated with my Discord?", "You can use `author me`.")
                                                            .addField("How many accounts can I associate with my account?", "You can associate 3 unique ScribbleHub accounts per Discord account."))
                                                    .respond();
                                        });
                                paginateMessage.delete("End of purpose.");
                            }

                            @Override
                            public void onPaginate(InteractionImmediateResponseBuilder responder, Interaction event, Message paginateMessage, UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                paginateMessage.edit(userResultEmbed(currentItem, arrow, paginator.size()));
                                responder.respond();
                            }

                            @Override
                            public InteractionOriginalResponseUpdater onInit(Interaction event, UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                return updater.addEmbed(userResultEmbed(currentItem, arrow, paginator.size()));
                            }

                            @Override
                            public InteractionOriginalResponseUpdater onEmptyPaginator(Interaction event) {
                                return updater.setContent("**ERROR**: There are no results for the query, maybe try a deeper query?");
                            }

                        }, Duration.ofMinutes(5))));
    }

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (UserDB.get(user.getId()).getAccounts().size() < 3) {
            if (args.length > 0) {
                String content = event.getMessageContent().substring(args[0].length() + 1);
                AmatsukiWrapper.getConnector().searchUser(content).thenAccept(userResults ->
                        new Paginate<>(userResults).paginateWithButtons(UUID.randomUUID().toString().replaceAll("-", ""), event,
                                new PaginateButtonEvent<>() {

                            @Override
                            public MessageBuilder onInit(MessageCreateEvent event, UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                return new MessageBuilder().setEmbed(userResultEmbed(currentItem, arrow, paginator.size()));
                            }

                            @Override
                            public MessageBuilder onEmptyPaginator(MessageCreateEvent event) {
                                return new MessageBuilder().setContent("**ERROR**: There are no results for the query, maybe try a deeper query?");
                            }

                            @Override
                            public void onPaginate(InteractionImmediateResponseBuilder responder, MessageCreateEvent event, Message paginateMessage,
                                                   UserResults currentItem, int arrow, Paginator<UserResults> paginator) {
                                paginateMessage.edit(userResultEmbed(currentItem, arrow, paginator.size()));
                                responder.respond();
                            }

                            @Override
                            public void onSelect(InteractionImmediateResponseBuilder responder, MessageCreateEvent event, Message paginateMessage,
                                                 UserResults u, int arrow, Paginator<UserResults> paginator) {
                                int uid = Integer.parseInt(u.getUrl().replaceAll("[^\\d]", ""));
                                UserDB.get(user.getId())
                                        .getAccounts()
                                        .stream()
                                        .map(SHUser::getUrl)
                                        .filter(s -> s.equalsIgnoreCase(String.format("https://www.scribblehub.com/profile/%d/amelia/", uid)))
                                        .findAny()
                                        .ifPresentOrElse(s -> responder.setContent("**ERROR**: `Your account is already associated with this user`.").respond(), () -> {
                                            UserDB.add(user.getId(), String.format("https://www.scribblehub.com/profile/%d/amelia/", uid), u.getName());
                                            responder.addEmbed(new Embed().setThumbnail(u.getAvatar()).setFooter("Created by Shindou Mihou @ patreon.com/mihou")
                                                            .setTitle("Success!").setDescription(String.format("You will now be notified whenever any of your (%s) stories are on the frontpage (trending, top 9)!", u.getName())).build()
                                                            .addField("When will I be notified?", "\nWe begin doing checks at 1:00 A.M (GMT+0) instead of 0:00 A.M (GMT+0) to allow trending to " +
                                                                    "stabilize since ScribbleHub usually takes one hour to calculate all stories for trending. You will be notified at that time if your story reaches " +
                                                                    "the top nine of the list.")
                                                            .addField("How do I disassociate my account?", "You can do `author remove [id]` with the id being found at `author me`")
                                                            .addField("How to check all accounts associated with my Discord?", "You can use `author me`.")
                                                            .addField("How many accounts can I associate with my account?", "You can associate 3 unique ScribbleHub accounts per Discord account."))
                                                    .respond();
                                        });
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
