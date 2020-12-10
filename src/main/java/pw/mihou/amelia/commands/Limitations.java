package pw.mihou.amelia.commands;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import pw.mihou.amelia.commands.base.db.ServerDB;

import static pw.mihou.amelia.commands.creation.RegisterCommand.hasRole;

public class Limitations {

    public static boolean isLimited(Server server, User user){
        if (ServerDB.getServer(server.getId()).getLimit() ) {
            if(server.canManage(user) || hasRole(user, server)|| server.isAdmin(user) || server.isOwner(user)){
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
