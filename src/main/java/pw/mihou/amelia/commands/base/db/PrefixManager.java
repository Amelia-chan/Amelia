package pw.mihou.amelia.commands.base.db;

public class PrefixManager {

    /**
     * Retrieves and returns the prefix for the server.
     *
     * @param serverId the server id.
     * @return the prefix of the server.
     */
    public static String prefix(long serverId) {
        return ServerDB.getServer(serverId).getPrefix();
    }

}
