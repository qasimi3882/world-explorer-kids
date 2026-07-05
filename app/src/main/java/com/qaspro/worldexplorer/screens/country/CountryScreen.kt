package com.qaspro.worldexplorer.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qaspro.worldexplorer.data.model.Country
import com.qaspro.worldexplorer.data.model.Lesson
import com.qaspro.worldexplorer.navigation.LocalAmbient
import com.qaspro.worldexplorer.navigation.LocalNarration
import com.qaspro.worldexplorer.navigation.LocalRepository
import com.qaspro.worldexplorer.ui.components.BackButton
import com.qaspro.worldexplorer.ui.components.LoadingSpinner
import com.qaspro.worldexplorer.ui.components.SpeakerButton
import com.qaspro.worldexplorer.ui.theme.CardTints
import com.qaspro.worldexplorer.ui.theme.CardWhite
import com.qaspro.worldexplorer.ui.theme.CreamBackground
import com.qaspro.worldexplorer.ui.theme.InkText

/**
 * A country's home page: big flag, its name (with a "say it" button),
 * where it is, and a grid of colourful lesson icons to explore.
 * Narration greets the child automatically; ambient sound is silenced here.
 */
@Composable
fun CountryScreen(
    countryId: String,
    onBack: () -> Unit,
    onOpenLesson: (Int) -> Unit
) {
    val repo = LocalRepository.current
    val narration = LocalNarration.current
    val ambient = LocalAmbient.current

    val country by produceState<Country?>(initialValue = null, countryId) {
        value = repo.loadCountry(countryId)
    }

    // Greet on open; stop the greeting + any ambient when leaving.
    LaunchedEffect(country) {
        ambient.play(null)
        country?.let { c ->
            val lines = buildList {
                if (c.welcome.isNotBlank()) add(c.welcome)
                addAll(c.summary)
            }
            if (lines.isNotEmpty()) narration.speak(lines)
        }
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { narration.stop() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        val c = country
        if (c == null) {
            LoadingSpinner(modifier = Modifier.fillMaxSize())
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // ── Header spans the full row ────────────────────────────────
                item(span = { GridItemSpan(3) }) {
                    CountryHeader(
                        country = c,
                        onSayName = {
                            narration.speak(c.name + (c.phonetic?.let { ". $it." } ?: "."))
                        }
                    )
                }

                // ── Lesson icon cards ────────────────────────────────────────
                itemsIndexed(c.orderedLessons) { index, lesson ->
                    LessonIconCard(
                        lesson = lesson,
                        tint = CardTints[index.mod(CardTints.size)],
                        onClick = {
                            narration.speak(lesson.type.title)
                            onOpenLesson(index)
                        }
                    )
                }
            }

            BackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun CountryHeader(country: Country, onSayName: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 44.dp)) {
        // Flag
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .shadow(10.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(CardWhite),
            contentAlignment = Alignment.Center
        ) {
            if (!country.flagUrl.isNullOrBlank()) {
                AsyncImage(
                    model = country.flagUrl,
                    contentDescription = "Flag of ${country.name}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            } else {
                Text(country.flagEmoji, fontSize = 96.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp),
                    color = InkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val where = listOf(country.continent, country.capital)
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")
                if (where.isNotBlank()) {
                    Text(
                        text = where,
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkText.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            SpeakerButton(onClick = onSayName, size = 66)
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "What would you like to discover?",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = InkText.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
        )
    }
}

@Composable
private fun LessonIconCard(
    lesson: Lesson,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .background(tint.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(lesson.type.emoji, fontSize = 40.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = lesson.type.title,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
            color = CardWhite,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}
