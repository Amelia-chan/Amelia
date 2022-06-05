package dev.amelia.threadpool

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class ThreadFactory(private val namePattern: String, private val daemon: Boolean) : ThreadFactory {

    private val counter = AtomicInteger()

    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, String.format(namePattern, counter.incrementAndGet()))
        thread.isDaemon = daemon
        return thread
    }
}