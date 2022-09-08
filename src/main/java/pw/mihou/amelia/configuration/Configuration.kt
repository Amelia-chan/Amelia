package pw.mihou.amelia.configuration

import pw.mihou.dotenv.annotations.EnvironmentItem

object Configuration {

    lateinit var DISCORD_TOKEN: String
    lateinit var MONGO_URI: String

    @EnvironmentItem(key = "\$DEVELOPER_SERVER_ID")
    var DEVELOPER_SERVER: Long = 0L

}