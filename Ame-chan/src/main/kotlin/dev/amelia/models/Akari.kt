package dev.amelia.models

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class AkariStory(
    val id: Long,
    val title: String,
    val author: String,
    val cover: String
)

@JsonClass(generateAdapter = true)
data class AkariChapter(
    val title: String,
    val link: String,
    val pubDate: Date
)

@JsonClass(generateAdapter = true)
data class AkariChapterStory(
    val title: String,
    val creator: String
)

@JsonClass(generateAdapter = true)
data class AkariFeed(
    val lastBuildDate: Date,
    val chapters: List<AkariChapter>,
    val after: String
)

@JsonClass(generateAdapter = true)
data class AkariTrending(
    val cachedUntil: Date,
    val stories: List<AkariStory>
)