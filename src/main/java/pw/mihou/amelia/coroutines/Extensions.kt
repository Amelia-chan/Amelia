package pw.mihou.amelia.coroutines

import kotlin.time.Duration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import pw.mihou.amelia.logger.logger
import pw.mihou.nexus.coroutines.utils.coroutine

fun repeat(
    delay: Duration,
    initialDelay: Duration?,
    async: Boolean = false,
    onError: ((Throwable) -> Unit)? = null,
    task: suspend () -> Unit,
) = coroutine {
    suspend fun execute(task: suspend () -> Unit) {
        try {
            task()
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            try {
                onError?.invoke(t) ?: logger.error("Uncaught error in 'repeat' routine", t)
            } catch (t2: Throwable) {
                logger.error("Uncaught error in 'repeat' error handler", t2)
            }
        }
    }

    if (initialDelay != null) {
        delay(initialDelay)
    }
    if (async) {
        coroutine {
            execute(task)
        }
    } else {
        execute(task)
    }
    delay(delay)
}
