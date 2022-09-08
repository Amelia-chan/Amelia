package pw.mihou.amelia.utility

import java.util.concurrent.CompletableFuture

fun <Result> future(task: () -> Result): CompletableFuture<Result> = CompletableFuture.supplyAsync {
    return@supplyAsync task()
}