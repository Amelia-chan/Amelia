@file:Suppress("ktlint:standard:filename")

package pw.mihou.amelia.logger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class ThresholdLoggerFilter : Filter<ILoggingEvent>() {
    private var level: Level? = null
    private var logger: String? = null

    override fun decide(event: ILoggingEvent): FilterReply {
        if (!isStarted) {
            return FilterReply.NEUTRAL
        }
        if (!event.loggerName.startsWith(logger!!)) return FilterReply.NEUTRAL
        return if (event.level.isGreaterOrEqual(level)) {
            FilterReply.NEUTRAL
        } else {
            FilterReply.DENY
        }
    }

    fun setLevel(level: Level) {
        this.level = level
    }

    fun setLogger(logger: String) {
        this.logger = logger
    }

    override fun start() {
        if (level != null && logger != null) {
            super.start()
        }
    }
}
