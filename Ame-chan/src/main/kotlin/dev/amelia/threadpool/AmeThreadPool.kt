package dev.amelia.threadpool

import java.util.concurrent.*

object AmeThreadPool {

    private const val CORE_POOL_SIZE = 1
    private const val MAXIMUM_POOL_SIZE = Int.MAX_VALUE
    private const val KEEP_ALIVE_TIME: Long = 60
    private val TIME_UNIT = TimeUnit.SECONDS

    val EXECUTOR: ExecutorService = ThreadPoolExecutor(
        CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, SynchronousQueue(),
        ThreadFactory("Ame - Executor - %d", false)
    )

    val SCHEDULER: ScheduledExecutorService =
        Executors.newScheduledThreadPool(CORE_POOL_SIZE, ThreadFactory("Ame - Scheduler - %d", false))

    val TRENDING_SCHEDULER =
        Executors.newScheduledThreadPool(CORE_POOL_SIZE, ThreadFactory("Ame - Trending Notifications Feature Scheduler - %d", false))

    val CHAPTER_SCHEDULER =
        Executors.newScheduledThreadPool(CORE_POOL_SIZE, ThreadFactory("Ame - Chapter Notifications Feature Scheduler - %d", false))

    val CHAPTER_EXECUTOR =
        Executors.newScheduledThreadPool(CORE_POOL_SIZE, ThreadFactory("Ame - Chapter Notifications Feature Executor - %d", false))

    val TRENDING_EXECUTOR =
        Executors.newScheduledThreadPool(CORE_POOL_SIZE, ThreadFactory("Ame - Trending Notifications Feature Executor - %d", false))

}