package com.qaspro.worldexplorer.data.model

import kotlinx.serialization.Serializable

/**
 * Lightweight entry used by the world browser / search grid.
 * The full [Country] (with all lessons) is only loaded when a child taps in.
 */
@Serializable
data class CountrySummary(
    val id: String,
    val iso3: String = "",
    val name: String,
    val phonetic: String? = null,
    val flagEmoji: String = "🏳",
    val continent: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    /** true when a full country JSON exists in assets; false = "coming soon" */
    val ready: Boolean = false
)

@Serializable
data class CountryIndex(
    val countries: List<CountrySummary> = emptyList()
)
