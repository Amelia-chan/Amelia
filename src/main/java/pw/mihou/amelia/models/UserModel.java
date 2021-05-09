package pw.mihou.amelia.models;

import java.util.List;

public class UserModel {

    // User Model is meant to be mutable
    // since we don't want to create a new object everytime.

    private List<String> accounts;
    private final long user;

    public UserModel(long user, List<String> accounts){
        this.accounts = accounts;
        this.user = user;
    }

    public UserModel add(String user){
        this.accounts.add(user);
        return this;
    }

    public UserModel remove(String user){
        this.accounts.remove(user);
        return this;
    }

    public List<String> getAccounts(){
        return accounts;
    }

    public long getUser(){
        return user;
    }

    public UserModel replace(List<String> accounts){
        this.accounts = accounts;
        return this;
    }
}
