package pw.mihou.amelia.models;

import tk.mihou.amatsuki.entities.user.lower.UserResults;

import java.util.List;

public class UserNavigators {

    private List<UserResults> results;
    private int arrow = 0;

    public UserNavigators(List<UserResults> results){
        this.results = results;
    }

    public UserResults next(){
        if(arrow < results.size()){
            arrow++;
        }
        return results.get(arrow);
    }

    public UserResults backwards(){
        if(arrow > 0){
            arrow--;
        }
        return results.get(arrow);
    }

    public UserResults current(){
        return results.get(arrow);
    }

    public int getArrow(){
        return arrow;
    }

    public int getMaximum(){
        return results.size();
    }

}
