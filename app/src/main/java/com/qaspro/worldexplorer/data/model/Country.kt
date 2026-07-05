package com.qaspro.worldexplorer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One card inside a lesson: a picture the child looks at while narration plays.
 *
 * @param title        short label shown under the image (e.g. "Giant Panda")
 * @param phonetic     optional "say it like this" hint (e.g. "soo-shee")
 * @param imageUrl     public-domain / CC photo streamed via Coil
 * @param narration    the short sentences the friendly voice speaks, in order
 * @param ambient      key of a looping background sound (see AmbientSound); null = none
 */
@Serializable
data class LessonItem(
    val title: String,
    val phonetic: String? = null,
    val imageUrl: String,
    val narration: List<String> = emptyList(),
    val ambient: String? = null
)

/**
 * A full visual lesson for one topic (Animals, Rivers, Food, ...).
 * `intro` is spoken once when the lesson opens, before the first card.
 */
@Serializable
data class Lesson(
    val type: LessonType,
    val intro: String = "",
    val items: List<LessonItem> = emptyList()
)

/**
 * A country. `lessons` is keyed by LessonType so the UI can show only the
 * lessons that have content and gracefully skip the rest.
 */
@Serializable
data class Country(
    val id: String,                       // ISO code, lowercase, e.g. "jp"
    val name: String,                     // "Japan"
    val phonetic: String? = null,         // "juh-PAN"
    val flagEmoji: String = "🏳",
    val flagUrl: String? = null,
    val continent: String = "",
    val capital: String = "",
    @SerialName("latitude") val latitude: Double = 0.0,
    @SerialName("longitude") val longitude: Double = 0.0,
    val welcome: String = "",             // spoken immediately on tap
    val summary: List<String> = emptyList(), // short spoken intro on the country page
    val lessons: Map<LessonType, Lesson> = emptyMap()
) {
    /** Lessons in the canonical LessonType order, only those with content. */
    val orderedLessons: List<Lesson>
        get() = LessonType.entries.mapNotNull { lessons[it] }
            .filter { it.items.isNotEmpty() }
}
