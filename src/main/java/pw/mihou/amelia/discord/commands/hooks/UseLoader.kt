package pw.mihou.amelia.discord.commands.hooks

import pw.mihou.reakt.Reakt

typealias Task = () -> Unit

fun Reakt.useLoader(): Pair<Reakt.Writable<Boolean>, (Task) -> Unit> {
    val isLoading = Reakt.Writable(false)
    val executeLongTask: (Task) -> Unit = { task ->
        try {
            isLoading.set(true)
            task()
        } finally {
            isLoading.set(false)
        }
    }

    return isLoading to executeLongTask
}
