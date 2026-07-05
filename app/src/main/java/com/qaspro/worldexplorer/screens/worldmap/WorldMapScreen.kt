package com.qaspro.worldexplorer.screens.worldmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qaspro.worldexplorer.data.model.CountrySummary
import com.qaspro.worldexplorer.navigation.LocalNarration
import com.qaspro.worldexplorer.navigation.LocalRepository
import com.qaspro.worldexplorer.ui.components.LoadingSpinner
import com.qaspro.worldexplorer.ui.theme.CardWhite
import com.qaspro.worldexplorer.ui.theme.GrassGreen
import com.qaspro.worldexplorer.ui.theme.InkText

private val Ocean = Color(0xFF9AD9EA)
private val LandReady = GrassGreen
private val LandComingSoon = Color(0xFFE6D8B5)
private val LandStroke = Color(0xFFFFFFFF)

/**
 * A real, tappable world map. Children pan and pinch-zoom the globe and tap a
 * country's actual shape to visit it. Built countries glow green; the rest are
 * a soft sand colour ("coming soon"). A search box flies the map to any country.
 */
@Composable
fun WorldMapScreen(onCountryTap: (String) -> Unit) {
    val context = LocalContext.current
    val repo = LocalRepository.current
    val narration = LocalNarration.current

    val shapes by produceState(initialValue = emptyList<CountryShape>()) {
        value = runCatching { WorldShapes.load(context) }.getOrElse {
            android.util.Log.e("WorldMap", "GeoJSON load failed", it)
            emptyList()
        }
    }
    val index by produceState(initialValue = emptyList<CountrySummary>()) {
        value = repo.loadIndex()
    }
    val byIso3 = remember(index) { index.associateBy { it.iso3 } }

    // View transform: normalized map [0,1]² -> screen. mapW = W*zoom, mapH = mapW/2.
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasW by remember { mutableStateOf(0f) }
    var canvasH by remember { mutableStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }

    fun mapW() = canvasW * zoom
    fun mapH() = mapW() / 2f

    fun clampOffset(o: Offset, mw: Float, mh: Float): Offset {
        val minX = if (mw <= canvasW) (canvasW - mw) / 2f else canvasW - mw
        val maxX = if (mw <= canvasW) (canvasW - mw) / 2f else 0f
        val minY = if (mh <= canvasH) (canvasH - mh) / 2f else canvasH - mh
        val maxY = if (mh <= canvasH) (canvasH - mh) / 2f else 0f
        return Offset(o.x.coerceIn(minX, maxX), o.y.coerceIn(minY, maxY))
    }

    fun flyTo(country: CountrySummary) {
        val nx = ((country.longitude + 180.0) / 360.0).toFloat()
        val ny = ((90.0 - country.latitude) / 180.0).toFloat()
        zoom = 4f
        val mw = mapW(); val mh = mapH()
        offset = clampOffset(
            Offset(canvasW / 2f - nx * mw, canvasH / 2f - ny * mh), mw, mh
        )
    }

    fun handleSelection(iso3: String?) {
        val summary = iso3?.let { byIso3[it] } ?: return
        if (summary.ready) {
            narration.speak("Hello! Welcome to ${summary.name}.")
            onCountryTap(summary.id)
        } else {
            narration.speak("${summary.name} is coming soon!")
        }
    }

    var query by rememberSaveable { mutableStateOf("") }
    val matches = remember(index, query) {
        if (query.isBlank()) emptyList()
        else index.filter { it.name.contains(query.trim(), ignoreCase = true) }.take(8)
    }

    Box(modifier = Modifier.fillMaxSize().background(Ocean)) {
        if (shapes.isEmpty()) {
            LoadingSpinner(modifier = Modifier.fillMaxSize())
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { sz ->
                        canvasW = sz.width.toFloat()
                        canvasH = sz.height.toFloat()
                        if (!initialized && canvasW > 0f) {
                            val mh = canvasW / 2f
                            offset = Offset(0f, (canvasH - mh) / 2f)
                            initialized = true
                        }
                    }
                    .pointerInput(shapes, index) {
                        awaitEachGesture {
                            val firstDown = awaitFirstDown(requireUnconsumed = false)
                            var pastTouchSlop = false

                            do {
                                val event = awaitPointerEvent()
                                val panChange = event.calculatePan()
                                val zoomChange = event.calculateZoom()

                                if (!pastTouchSlop &&
                                    (panChange.getDistance() > viewConfiguration.touchSlop || zoomChange != 1f)
                                ) {
                                    pastTouchSlop = true
                                }

                                if (pastTouchSlop) {
                                    val centroid = event.calculateCentroid(useCurrent = false)
                                    val newZoom = (zoom * zoomChange).coerceIn(1f, 9f)
                                    val pannedOffset = offset + panChange
                                    val oldW = canvasW * zoom
                                    val u = if (oldW != 0f) (centroid.x - pannedOffset.x) / oldW else 0f
                                    val v = if (oldW != 0f) (centroid.y - pannedOffset.y) / (oldW / 2f) else 0f
                                    val newW = canvasW * newZoom
                                    val newH = newW / 2f
                                    zoom = newZoom
                                    offset = clampOffset(
                                        Offset(centroid.x - u * newW, centroid.y - v * newH),
                                        newW, newH
                                    )
                                    event.changes.forEach { it.consume() }
                                }
                            } while (event.changes.any { it.pressed })

                            if (!pastTouchSlop) {
                                val mw = canvasW * zoom
                                val mh = mw / 2f
                                if (mw != 0f && mh != 0f) {
                                    val p = Offset(
                                        (firstDown.position.x - offset.x) / mw,
                                        (firstDown.position.y - offset.y) / mh
                                    )
                                    val hit = shapes.firstOrNull { WorldShapes.contains(it, p) }
                                    handleSelection(hit?.iso3)
                                }
                            }
                        }
                    }
            ) {
                val mw = canvasW * zoom
                val mh = mw / 2f
                val strokeW = (1.1f / mw).coerceAtLeast(0.00005f)

                withTransform({
                    translate(offset.x, offset.y)
                    scale(mw, mh, pivot = Offset.Zero)
                }) {
                    shapes.forEach { shape ->
                        val ready = byIso3[shape.iso3]?.ready == true
                        drawPath(shape.path, color = if (ready) LandReady else LandComingSoon, style = Fill)
                        drawPath(shape.path, color = LandStroke, style = Stroke(width = strokeW))
                    }
                }
            }
        }

        // ── Title + search overlay ──────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Tap a country! 🌍",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                color = InkText
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Find a country…") },
                keyboardOptions = KeyboardOptions.Default,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(CardWhite, RoundedCornerShape(20.dp))
            )
            if (matches.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardWhite,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                        items(matches, key = { it.id }) { c ->
                            Text(
                                text = "${c.flagEmoji}  ${c.name}" + if (c.ready) "  ✨" else "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = InkText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        query = ""
                                        flyTo(c)
                                        handleSelection(c.iso3)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
