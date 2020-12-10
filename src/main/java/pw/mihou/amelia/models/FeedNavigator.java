package pw.mihou.amelia.models;

import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeedNavigator {

    private ArrayList<FeedModel> models = new ArrayList<>();
    private int page = 1;

    public FeedNavigator(ArrayList<FeedModel> models){
        this.models.addAll(models);
    }

    public ArrayList<FeedModel> getModels(){
        return models;
    }

    public Optional<ArrayList<FeedModel>> current() {
        ArrayList<FeedModel> objects = new ArrayList<>();
        // Expected Behavior: Checks if warnings size is greater than 10 multiplied by page and so replies with 10 otherwise replies with warning size.
        try {
            for(int i = 0; i < getLimit(); i++){
                // Expected Behavior: Checks if page is above one, if so adds 10 multiplied by page (for example, page 2: 20+i).
                objects.add(models.get(i + (page > 1 ? (10 * (page - 1)) : 0)));
            }
        } catch(IndexOutOfBoundsException e){
            return Optional.empty();
        }
        return objects.isEmpty() ? Optional.empty() : Optional.of(objects);
    }


    public Optional<ArrayList<FeedModel>> next() {
        page++;

        if (current().isEmpty()) {
            return backwards();
        }

        return current();
    }

    public Optional<ArrayList<FeedModel>> backwards() {
        page--;

        if (current().isEmpty()) {
            return next();
        }

        return current();
    }

    public void reset() {
        page = 1;
    }

    public boolean hasNext() {
        return (getMaximumPage() - page >= 1);
    }

    public boolean canReverse() {
        return (page > 1);
    }

    public int getPage() {
        return page;
    }

    public int getMaximumPage() {
        return 1 + models.size() / 10;
    }

    private int getLimit(){
        int x = 0;
        try {
            for (int i = 0; i < 10; i++) {
                if (models.get(i + (page > 1 ? (10 * (page - 1)) : 0)) != null) {
                    x++;
                } else {
                    return x;
                }
            }
        } catch(IndexOutOfBoundsException e){
            return x;
        }
        return x;
    }


}
