package com.qaspro.worldexplorer.navigation

/** Central place for navigation routes so screens never hand-type paths. */
object Routes {
    const val MAP = "map"

    const val COUNTRY = "country/{countryId}"
    fun country(id: String) = "country/$id"

    // lessonIndex = position within Country.orderedLessons
    const val LESSON = "lesson/{countryId}/{lessonIndex}"
    fun lesson(countryId: String, lessonIndex: Int) = "lesson/$countryId/$lessonIndex"
}
