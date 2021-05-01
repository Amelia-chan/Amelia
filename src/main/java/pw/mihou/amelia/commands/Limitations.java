package pw.mihou.amelia.commands;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import pw.mihou.amelia.commands.base.db.ServerDB;

import static pw.mihou.amelia.commands.creation.RegisterCommand.hasRole;

public class Limitations {

    public static boolean isLimited(Server server, User user) {
        // If anarchy mode is enabled.
        if(!ServerDB.getServer(server.getId()).getLimit())
            return true;

        // If anarchy mode is disabled, then we check if the user is any of these.
        return server.canManage(user) || hasRole(user, server) || server.isAdmin(user) || server.isOwner(user);
    }

}
