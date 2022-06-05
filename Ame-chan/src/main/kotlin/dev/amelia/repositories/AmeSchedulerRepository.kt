package dev.amelia.repositories

import dev.amelia.configuration.AmeConfiguration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object AmeSchedulerRepository {

    private val SCHEDULED_ONCE = AtomicBoolean(false)
    private val SHARD_COUNT = AtomicInteger(0)

    fun handshake() {

    }

    fun scheduleOnce() {
        if (SHARD_COUNT.incrementAndGet() == AmeConfiguration.DISCORD_SHARDS && !SCHEDULED_ONCE.get()) {
            SCHEDULED_ONCE.set(true)


        }
    }

}