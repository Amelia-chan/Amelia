package pw.mihou.amelia.commands;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import pw.mihou.amelia.commands.base.db.ServerDB;
import pw.mihou.amelia.commands.creation.Register;

public class Limitations {

    public static boolean isLimited(Server server, User user) {
        if (!ServerDB.getServer(server.getId()).getLimit())
            return true;

        return server.canManage(user) || Register.hasRole(user, server) || server.isAdmin(user) || server.isOwner(user);
    }

}
