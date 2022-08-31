package pw.mihou.amelia.extensions

import java.net.URL

val URL.params: Map<String, String>
        get() = query.toUrlParams()