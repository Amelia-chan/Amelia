package dev.amelia.logging

import dev.amelia.LOGGER
import io.sentry.Sentry

object AmeErrorHandler {

    /**
     * Accepts a throwable given by methods such as CompletableFuture's exceptionally and
     * reports them to [AmeErrorHandler.report] for processing. This is simply a short-hand expression
     * of the method.
     *
     * @param throwable The throwable that was thrown.
     */
    fun <T> accept(throwable: Throwable): T? {
        report(throwable, if (throwable.stackTrace.isNotEmpty()) throwable.stackTrace[0].className else "N/A")

        return null
    }

    /**
     * Generically handles the exceptions being thrown by methods or classes. This sends
     * a log message onto the console whilst also notifying Sentry of the exception.
     *
     * @param throwable The throwable that was thrown from the error.
     * @param fault The fault line of this throwable whether as a class name or a feature.
     */
    fun report(throwable: Throwable, fault: String) {
        LOGGER.error("A throwable was thrown from $fault with stacktrace:", throwable)
        Sentry.captureException(throwable)
    }

}