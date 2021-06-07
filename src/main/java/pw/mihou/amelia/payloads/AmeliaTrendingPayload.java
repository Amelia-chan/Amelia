package pw.mihou.amelia.payloads;

import com.google.gson.annotations.SerializedName;
import tk.mihou.amatsuki.entities.story.lower.StoryResults;

public class AmeliaTrendingPayload {

    @SerializedName("id")
    public long user;
    @SerializedName("story")
    public StoryResults story;
    @SerializedName("username")
    public String username;

    public AmeliaTrendingPayload(long user, StoryResults story, String username) {
        this.user = user;
        this.story = story;
        this.username = username;
    }

}
