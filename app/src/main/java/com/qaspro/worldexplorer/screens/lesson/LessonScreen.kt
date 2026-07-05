package com.qaspro.worldexplorer.screens.lesson

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qaspro.worldexplorer.data.model.Country
import com.qaspro.worldexplorer.data.model.LessonItem
import com.qaspro.worldexplorer.navigation.LocalAmbient
import com.qaspro.worldexplorer.navigation.LocalNarration
import com.qaspro.worldexplorer.navigation.LocalRepository
import com.qaspro.worldexplorer.ui.components.BackButton
import com.qaspro.worldexplorer.ui.components.LoadingSpinner
import com.qaspro.worldexplorer.ui.components.SpeakerButton

/**
 * The heart of the app: a full-screen picture gallery a child swipes through.
 * Each picture slowly zooms (Ken Burns), narration plays on its own, and a soft
 * ambient loop sets the mood. Almost no text — just look, listen, and swipe.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LessonScreen(
    countryId: String,
    lessonIndex: Int,
    onBack: () -> Unit
) {
    val repo = LocalRepository.current
    val narration = LocalNarration.current
    val ambient = LocalAmbient.current

    val country by produceState<Country?>(initialValue = null, countryId) {
        value = repo.loadCountry(countryId)
    }

    val lesson = remember(country, lessonIndex) {
        country?.orderedLessons?.getOrNull(lessonIndex)
    }
    val items = lesson?.items ?: emptyList()

    val pagerState = rememberPagerState(pageCount = { items.size })

    // Narrate the current card whenever the swipe settles; play its ambient.
    val introSpoken = remember(lesson) { mutableStateOf(false) }
    LaunchedEffect(lesson, pagerState.currentPage) {
        val l = lesson ?: return@LaunchedEffect
        val item = items.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        ambient.play(item.ambient)
        val lines = buildList {
            if (pagerState.currentPage == 0 && !introSpoken.value && l.intro.isNotBlank()) {
                add(l.intro)
                introSpoken.value = true
            }
            addAll(item.narration)
        }
        if (lines.isNotEmpty()) narration.speak(lines)
    }

    DisposableEffect(Unit) {
        onDispose {
            narration.stop()
            ambient.play(null)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            country == null -> LoadingSpinner(modifier = Modifier.fillMaxSize())
            items.isEmpty() -> EmptyLesson()
            else -> {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    LessonCard(
                        item = items[page],
                        isActive = page == pagerState.currentPage,
                        onSayTitle = {
                            val say = items[page].title +
                                (items[page].phonetic?.let { ". $it." } ?: "")
                            narration.speak(say)
                        }
                    )
                }

                // Progress dots
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(items.size) { i ->
                        val active = i == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (active) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        BackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
        )
    }
}

@Composable
private fun LessonCard(
    item: LessonItem,
    isActive: Boolean,
    onSayTitle: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Ken Burns: a slow, gentle zoom while the card is on screen.
        val transition = rememberInfiniteTransition(label = "kenburns")
        val scale by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(14000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val s = if (isActive) scale else 1f
                    scaleX = s
                    scaleY = s
                }
        )

        // Bottom scrim so the label is always readable over any photo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.65f)
                    )
                )
        )

        // Title + "say it" button
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
                    color = Color.White
                )
                item.phonetic?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            SpeakerButton(onClick = onSayTitle, size = 60)
        }
    }
}

@Composable
private fun EmptyLesson() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "More coming soon! 🎨",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
