package pw.mihou.amelia.io;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class ResetCalculator {

    // This calculates the next reset
    // time for trending (0:00 GMT+0) but with an hour added
    // since we want it to stabilize.

    private static int determineNextTarget() {
        return LocalDateTime.now().getMinute() % 60 != 0
                ? (LocalDateTime.now().getMinute() + (60 - LocalDateTime.now().getMinute() % 60))
                - LocalDateTime.now().getMinute() : 0;
    }

    private static long determine(long seconds){
        return determineNextTarget() != 0 ? (seconds - 3600) + (determineNextTarget() * 60) : seconds + (determineNextTarget() * 60);
    }

    private static long getHour(long seconds){
        return Long.parseLong(Long.toString(seconds).replaceFirst("-", ""));
    }

    public static long nextTrending(){
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        return getHour(now.getHour()) * 3600 != 3600 ?
                determine((((getHour(now.getHour()) * 3600) + ((7200 + 82800) - (getHour(now.getHour()) * 3600)))
                        - getHour(now.getHour()) * 3600)) : determineNextTarget() * 60;
    }

    public static long defaultReset(){
        return TimeUnit.HOURS.toSeconds(24);
    }

}
