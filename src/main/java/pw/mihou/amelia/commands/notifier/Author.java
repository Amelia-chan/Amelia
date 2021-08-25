package pw.mihou.amelia.commands.notifier;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.amelia.db.UserDB;
import pw.mihou.amelia.models.SHUser;
import pw.mihou.amelia.templates.Embed;
import pw.mihou.amelia.utility.StringUtils;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenSlashEvent;

import java.util.Collection;
import java.util.List;

import static pw.mihou.amelia.templates.TemplateMessages.*;

public class Author implements VelenEvent, VelenSlashEvent {

    public EmbedBuilder userEmbed(Collection<SHUser> users) {
        EmbedBuilder embed = new Embed().setTitle("Your Associated Accounts")
                .setDescription("Here are all the accounts associated with you.").build();
        if (users.isEmpty())
            return embed.setDescription("There are no users associated with this account, please use `iam [username]` if you want to be notified when a user under that username" +
                    " gets to the top 9 trending on ScribbleHub!");

        users.forEach(shUser -> embed.addField(String.format("[ID: %d] %s", shUser.getUnique(), shUser.getName()),
                StringUtils.createEmbeddedFormat(
                        "**Username**: " + shUser.getName(),
                        "**ID**: " + shUser.getUnique()
                )));

        return embed.addField("Note", "The names here may be outdated (since we cache the names from IAm command)" +
                " but our checks will check the updated name, so don't worry if your name " +
                "on here is different (after changing usernames) since it won't affect anything.");
    }

    @Override
    public void onEvent(MessageCreateEvent event, Message message, User user, String[] args) {
        if (args.length > 0) {
            if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
                try {
                    int unique = Integer.parseInt(args[1]);
                    if (UserDB.doesExist(user.getId(), unique)) {
                        UserDB.remove(user.getId(), unique);
                        event.getMessage().reply(String.format(SUCCESS_ACCOUNT_REMOVE, unique));
                    } else {
                        event.getMessage().reply(ERROR_NO_ACCOUNTS_ASSOCIATED);
                    }
                } catch (NumberFormatException | ArithmeticException e) {
                    event.getMessage().reply(ERROR_INT_ABOVE_LIMIT);
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("me")) {
                event.getMessage().reply(userEmbed(UserDB.get(user.getId()).getAccounts()));
            }
        }
    }


    @Override
    public void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args,
                        List<SlashCommandInteractionOption> options, InteractionImmediateResponseBuilder firstResponder) {
        String subcommand = event.getFirstOption().orElseThrow().getName();

        event.respondLater().thenAccept(updater -> {
            if (subcommand.equalsIgnoreCase("remove")) {
                int unique = event.getFirstOption().orElseThrow().getOptionIntValueByName("id").orElseThrow();

                if (UserDB.doesExist(user.getId(), unique)) {
                    UserDB.remove(user.getId(), unique);
                    updater.setContent(String.format(SUCCESS_ACCOUNT_REMOVE, unique)).update();
                } else {
                    updater.setContent(ERROR_NO_ACCOUNTS_ASSOCIATED).update();
                }

            } else if (subcommand.equalsIgnoreCase("me")) {
                updater.addEmbed(userEmbed(UserDB.get(user.getId()).getAccounts())).update();
            }
        });

    }
}
