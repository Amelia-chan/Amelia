package pw.mihou.amelia;

import pw.mihou.amelia.io.AmatsukiWrapper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) {
        AtomicInteger integer = new AtomicInteger(0);
        AmatsukiWrapper.getConnector().getTrending().join().stream().limit(10).collect(Collectors.toList()).forEach(storyResults -> System.out.printf("Position: %d\nTitle: %s\nAuthor:%s\n\n", integer.addAndGet(1), storyResults.getName(), storyResults.getCreator()));
    }

}
