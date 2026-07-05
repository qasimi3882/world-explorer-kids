package com.qaspro.worldexplorer.data.model

/**
 * The fixed set of visual lessons every country can offer.
 * `emoji` is the card icon, `title` the short label. Order here is display order.
 */
enum class LessonType(val emoji: String, val title: String) {
    LOCATION("📍", "Location"),
    CAPITAL("🏛", "Capital"),
    NEIGHBOURS("🗺", "Neighbours"),
    CONTINENT("🌎", "Continent"),
    PEOPLE("👥", "People"),
    CITIES("🏙", "Cities"),
    MOUNTAINS("🏔", "Mountains"),
    RIVERS("🌊", "Rivers"),
    NATURE("🌳", "Nature"),
    ANIMALS("🦁", "Animals"),
    PLANTS("🌸", "Plants"),
    FOOD("🍜", "Food"),
    LANDMARKS("🗽", "Landmarks"),
    CLOTHES("👕", "Clothes"),
    MUSIC("🎵", "Music"),
    TRANSPORT("🚆", "Transport"),
    WEATHER("🌤", "Weather"),
    CURRENCY("💰", "Currency"),
    LANGUAGE("🗣", "Language"),
    SPORTS("⚽", "Sports"),
    FESTIVALS("🎉", "Festivals"),
    FUN_FACTS("⭐", "Fun Facts")
}
