package pw.mihou.amelia.io;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Terminal {

    private static String getTime(){
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
    }

    public static void log(String message){
        System.out.printf("[%s]: %s", getTime(), message);
    }


}
