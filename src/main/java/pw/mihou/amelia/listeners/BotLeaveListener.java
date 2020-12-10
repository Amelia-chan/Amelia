package pw.mihou.amelia.listeners;

import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.api.listener.server.ServerLeaveListener;
import pw.mihou.amelia.commands.base.db.ServerDB;
import pw.mihou.amelia.commands.db.FeedDB;

public class BotLeaveListener implements ServerLeaveListener {

    @Override
    public void onServerLeave(ServerLeaveEvent event) {
        // Deletes all the data that belonged to the server.
        FeedDB.deleteServer(event.getServer().getId());
        ServerDB.deleteServer(event.getServer().getId());
    }

}
