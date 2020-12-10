package pw.mihou.amelia.commands.base.info;

import java.util.HashMap;
import java.util.Map;

public class Commands {

    // Holds all the metadata of the commands.
    public static final Map<String, CommandMeta> meta = new HashMap<>();

    /**
     * Adds meta data to a command.
     * @param command the command name.
     * @param description the command description.
     * @param usage the usage of the command.
     * @param cooldown the cooldown of the command.
     */
    public static void addCommand(String command, String description, String usage, long cooldown){
        meta.putIfAbsent(command, new CommandMeta(command, description, usage, cooldown));
    }

    /**
     * Returns back the metadata of a command.
     * @param command the command name.
     * @return the metadata of the command.
     */
    public static CommandMeta getCommand(String command){
        return meta.get(command);
    }

}
