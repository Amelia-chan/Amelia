package pw.mihou.amelia.session;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Amelia Session is a class that is used to track insights for this
 * session of Amelia (session is the time from the start of this bot to the next restart).
 */
public class AmeliaSession {

    public static final AtomicInteger feedsUpdated = new AtomicInteger(0);

}
