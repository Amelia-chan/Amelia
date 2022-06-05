package dev.amelia.http.akari

import dev.amelia.MOSHI
import dev.amelia.configuration.AmeConfiguration
import dev.amelia.logging.AmeErrorHandler
import dev.amelia.models.AkariFeed
import dev.amelia.models.AkariTrending
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException
import java.util.Optional

object AkariApi {

    private val USER_FEED_ROUTE: (Long) -> String = { "/user/$it" }
    private val SERIES_FEED_ROUTE: (Long) -> String = { "/series/$it" }
    private const val TRENDING_ROUTE = "/trending"

    private fun createUrl(route: String): String {
        return "${AmeConfiguration.AKARI_CHAN}$route"
    }

    /**
     * Requests the current trending status from Akari-chan, this shouldn't return a null-value
     * but if it does then there must be something wrong with Akari-chan.
     *
     * @return The current trending list of ScribbleHub collected by Akari-chan.
     */
    fun trending(): AkariTrending? {
        try {
            val response = Jsoup.connect(createUrl(TRENDING_ROUTE))
                .userAgent(AmeConfiguration.USER_AGENT)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(1000 * 10)
                .execute()

            if (response.statusCode() != 200) {
                throw Throwable("Akari-chan replied with ${response.statusCode()} on a request, possible infrastructure down?")
            }

            return MOSHI.adapter(AkariTrending::class.java).fromJson(response.body())
        } catch (exception: IOException) {
            AmeErrorHandler.report(exception, "AkariApi.trending()")
            return null
        }
    }

    /**
     * Requests information about a given feed from Akari-chan, this should contain all the feed chapters
     * and other information necessary to be dispatched onto Discord if the feed is present.
     *
     * @param type What kind of entity are we requesting?
     * @param id The identifier of the entity being requested.
     * @param after An optional field that is used by Akari-chan to filter out the chapters that we only need.
     *
     * @return All the feed chapters with all the other information needed, if present.
     */
    fun feed(type: AkariFeedType, id: Long, after: String?): Optional<AkariFeed> {
        try {
            var route = if (type == AkariFeedType.STORY)
                SERIES_FEED_ROUTE.invoke(id)
            else
                USER_FEED_ROUTE.invoke(id)

            if (after != null) {
                route += "?after=$after"
            }

            val response = Jsoup.connect(createUrl(route))
                .userAgent(AmeConfiguration.USER_AGENT)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(1000 * 10)
                .execute()

            if (response.statusCode() == 404) {
                return Optional.empty()
            }

            if (response.statusCode() != 200) {
                throw Throwable("Akari-chan replied with ${response.statusCode()} on a request, possible infrastructure down?")
            }

            return Optional.ofNullable(MOSHI.adapter(AkariFeed::class.java).fromJson(response.body()))
        } catch (exception: IOException) {
            AmeErrorHandler.report(exception, "AkariApi.feed($id)")
            return Optional.empty()
        }
    }
}

enum class AkariFeedType(val textEquivalent: String) {
    USER("author"), STORY("series");

    companion object {
        fun from(text: String): AkariFeedType? {
            return values().find { it.toString().equals(text, ignoreCase = true) }
        }
    }

    override fun toString(): String {
        return textEquivalent
    }
}