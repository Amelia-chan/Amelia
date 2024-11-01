package pw.mihou.amelia.configuration

import pw.mihou.envi.annotations.Alternatively
import pw.mihou.envi.annotations.Required

@Suppress("ktlint:standard:property-naming")
object Configuration {
    var APP_NAME = "Amelia"
    var APP_ACTIVITY = "People read stories!"

    var LOADING_EMOJI = "<a:manaWinterLoading:880162110947094628>"
    var IS_SELF_HOSTED = false

    @Required
    lateinit var DISCORD_TOKEN: String

    @Required
    lateinit var MONGO_URI: String

    var BASE_SCRIBBLEHUB_URL = "https://scribblehub.com"
    var BASE_AUTHORIZATION_TOKEN: String? = null

    @Alternatively("\$DEVELOPER_SERVER_ID")
    var DEVELOPER_SERVER: Long = 0L
}
