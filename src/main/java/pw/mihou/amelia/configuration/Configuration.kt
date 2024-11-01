package pw.mihou.amelia.configuration

import pw.mihou.envi.annotations.Alternatively
import pw.mihou.envi.annotations.Required

@Suppress("ktlint:standard:property-naming")
object Configuration {
    @Required
    lateinit var DISCORD_TOKEN: String

    @Required
    lateinit var MONGO_URI: String

    var BASE_SCRIBBLEHUB_URL = "https://scribblehub.com"
    var BASE_AUTHORIZATION_TOKEN: String? = null

    @Alternatively("\$DEVELOPER_SERVER_ID")
    var DEVELOPER_SERVER: Long = 0L
}
