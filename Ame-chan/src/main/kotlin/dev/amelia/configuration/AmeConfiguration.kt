package dev.amelia.configuration

import pw.mihou.dotenv.annotations.DoNotReflect

object AmeConfiguration {

    @JvmStatic
    lateinit var MONGO_URI: String

    @JvmStatic
    lateinit var DISCORD_TOKEN: String

    @JvmStatic
    var DISCORD_SHARDS: Int = 1

    @JvmStatic
    lateinit var AKARI_CHAN: String

    @JvmStatic
    lateinit var SENTRY_DSN: String

    @JvmStatic
    var PRODUCTION: Boolean = true

    @DoNotReflect
    const val USER_AGENT = "Ame-chan/1.0.0 (https://github.com/Amelia-chan/Amelia; Language=Java 18)"

    @DoNotReflect
    const val REFERRER = "https://github.com/Amelia-chan/Amelia"
}