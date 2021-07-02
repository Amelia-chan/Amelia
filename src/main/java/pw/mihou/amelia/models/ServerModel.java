package pw.mihou.amelia.models;

import pw.mihou.amelia.commands.base.db.ServerDB;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ServerModel {

    private final long id;
    private String prefix;
    private boolean limit;
    private long role;

    public ServerModel(long id, String prefix, boolean limit, long role) {
        this.id = id;
        this.prefix = prefix;
        this.limit = limit;
        this.role = role;
    }

    /**
     * Returns the role required to be able to modify the feeds.
     *
     * @return the role required.
     */
    public Optional<Long> getRole() {
        // Checks if the limit is activated, if not then disable role, otherwise if it is activated, then check if role is not empty.
        // otherwise, disable again.
        return limit ? (role != 0 ? Optional.of(role) : Optional.empty()) : Optional.empty();
    }

    public ServerModel setRole(long role) {
        this.role = role;

        return this;
    }

    public CompletableFuture<Void> update() {
        return CompletableFuture.runAsync(() -> ServerDB.addServer(this));
    }

    /**
     * Returns whether the server opted to limit the feed creation to only people with Manage Server permissions.
     *
     * @return limitation is active?
     */
    public boolean getLimit() {
        return limit;
    }

    public ServerModel setLimit(boolean limit) {
        this.limit = limit;

        return this;
    }

    /**
     * Returns the id of the server.
     *
     * @return the server id.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the prefix used by the server.
     *
     * @return the prefix used by the server.
     */
    public String getPrefix() {
        return prefix;
    }

    public ServerModel setPrefix(String prefix) {
        this.prefix = prefix;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerModel that = (ServerModel) o;
        return Objects.equals(getPrefix(), that.getPrefix());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrefix());
    }
}
