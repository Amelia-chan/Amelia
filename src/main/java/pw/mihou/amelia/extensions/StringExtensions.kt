package pw.mihou.amelia.extensions

import java.net.URL

fun String.toUrlParams() = split( '&').map {
    val keyValue = it.split('=', limit = 2)
    return@map Pair(keyValue[0].lowercase(), keyValue[1])
}.toMap()

fun String.toUrl() = URL(this)