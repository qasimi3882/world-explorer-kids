package com.qaspro.worldexplorer.data

import android.content.Context
import com.qaspro.worldexplorer.data.model.Country
import com.qaspro.worldexplorer.data.model.CountryIndex
import com.qaspro.worldexplorer.data.model.CountrySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Reads country data bundled in assets/countries/.
 *
 *   index.json      -> lightweight list for the browser (CountryIndex)
 *   <id>.json       -> full Country with every lesson (loaded on demand)
 *
 * Full countries are cached in memory after first load so re-opening is instant.
 */
class CountryRepository(private val appContext: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val fullCache = HashMap<String, Country>()
    @Volatile private var indexCache: List<CountrySummary>? = null

    suspend fun loadIndex(): List<CountrySummary> = withContext(Dispatchers.IO) {
        indexCache?.let { return@withContext it }
        val text = readAsset("countries/index.json")
        val parsed = json.decodeFromString(CountryIndex.serializer(), text).countries
        indexCache = parsed
        parsed
    }

    /** Full country, or null if its JSON is missing / unreadable. */
    suspend fun loadCountry(id: String): Country? = withContext(Dispatchers.IO) {
        fullCache[id]?.let { return@withContext it }
        val text = runCatching { readAsset("countries/$id.json") }.getOrNull()
            ?: return@withContext null
        val country = runCatching {
            json.decodeFromString(Country.serializer(), text)
        }.getOrNull() ?: return@withContext null
        fullCache[id] = country
        country
    }

    private fun readAsset(path: String): String =
        appContext.assets.open(path).bufferedReader().use { it.readText() }
}
