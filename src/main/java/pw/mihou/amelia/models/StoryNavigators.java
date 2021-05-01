package pw.mihou.amelia.models;

import tk.mihou.amatsuki.entities.story.lower.StoryResults;

import java.util.List;

public class StoryNavigators {

    private List<StoryResults> results;
    private int arrow = 0;

    public StoryNavigators(List<StoryResults> results) {
        this.results = results;
    }

    public StoryResults next() {
        if (arrow < results.size()) {
            arrow++;
        }
        return results.get(arrow);
    }

    public StoryResults backwards() {
        if (arrow > 0) {
            arrow--;
        }
        return results.get(arrow);
    }

    public StoryResults current() {
        return results.get(arrow);
    }

    public int getArrow() {
        return arrow;
    }

    public int getMaximum() {
        return results.size();
    }

}
