package pw.mihou.amelia.commands.base.info;

import java.util.Objects;

public class CommandMeta {

    private String command;
    private String description;
    private String usage;
    private long cooldown;

    public CommandMeta(String command, String description, String usage, long cooldown) {
        this.command = command;
        this.description = description;
        this.usage = usage;
        this.cooldown = cooldown;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public long getCooldown() {
        return cooldown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandMeta that = (CommandMeta) o;
        return getCooldown() == that.getCooldown() &&
                Objects.equals(getCommand(), that.getCommand()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getUsage(), that.getUsage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommand(), getDescription(), getUsage(), getCooldown());
    }
}
