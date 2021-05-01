package pw.mihou.amelia.commands.invite;

import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.amelia.commands.base.Command;
import pw.mihou.amelia.templates.Embed;

public class InviteCommand extends Command {

    public InviteCommand() {
        super("invite", "Want an invitation link for the bot?", "invite", false);
    }

    @Override
    protected void runCommand(MessageCreateEvent event, User user, Server server, String[] args) {
        event.getMessage().reply(new Embed().setTitle("Want to invite me?")
                .setThumbnail(event.getApi().getYourself().getAvatar()).setDescription("Feel free to invite me then using this link!" +
                        "\n\n- [Invite me now](" + event.getApi().createBotInvite(Permissions.fromBitmask(67488832)) + ")").build());
    }
}
