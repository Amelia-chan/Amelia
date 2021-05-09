package pw.mihou.amelia.models;

import pw.mihou.amelia.io.AmatsukiWrapper;
import tk.mihou.amatsuki.entities.user.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserModel {

    private final Map<Integer, SHUser> accounts = new TreeMap<>();
    private final long user;

    public UserModel(long user, List<SHUser> accounts){
        accounts.forEach(s -> this.accounts.put(s.getUnique(), s));
        this.user = user;
    }

    public void addAccount(SHUser acc){
        this.accounts.put(acc.getUnique(), acc);
    }

    public void removeAccount(int unique){
        this.accounts.remove(unique);
    }

    public Collection<SHUser> getAccounts(){
        return accounts.values();
    }

    public CompletableFuture<List<User>> getUsers(){
        return CompletableFuture.supplyAsync(() -> {
            List<User> s = new ArrayList<>();
            accounts.values().forEach(x -> AmatsukiWrapper.getConnector().getUserFromUrl(x.getUrl()).thenAccept(s::add));
            return s;
        });
    }

    public long getUser(){
        return user;
    }
}
