package com.qaspro.worldexplorer.screens.worldmap

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * One country's outline, already projected to normalized map space [0,1]x[0,1]
 * using a simple equirectangular projection:
 *   nx = (lng + 180) / 360
 *   ny = (90 - lat) / 180
 *
 * `path` is a prebuilt Compose Path (in normalized coords) for fast drawing;
 * `rings` keeps the raw outer-ring points for point-in-polygon hit-testing.
 */
data class CountryShape(
    val iso3: String,
    val path: Path,
    val rings: List<List<Offset>>
)

object MapProjection {
    fun project(lng: Double, lat: Double): Offset =
        Offset(((lng + 180.0) / 360.0).toFloat(), ((90.0 - lat) / 180.0).toFloat())
}

object WorldShapes {

    /** Parse assets/world.geo.json into projected, draw-ready country shapes. */
    suspend fun load(context: Context): List<CountryShape> = withContext(Dispatchers.IO) {
        val text = context.assets.open("world.geo.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val features = root.getJSONArray("features")
        val result = ArrayList<CountryShape>(features.length())

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val iso3 = feature.optString("id", "")
            val geom = feature.optJSONObject("geometry") ?: continue
            val type = geom.optString("type")
            val coords = geom.optJSONArray("coordinates") ?: continue

            val path = Path()
            val rings = ArrayList<List<Offset>>()

            when (type) {
                "Polygon" -> addPolygon(coords, path, rings)
                "MultiPolygon" -> {
                    for (p in 0 until coords.length()) {
                        addPolygon(coords.getJSONArray(p), path, rings)
                    }
                }
            }
            if (!rings.isEmpty()) result.add(CountryShape(iso3, path, rings))
        }
        result
    }

    // One polygon = an outer ring followed by optional hole rings.
    private fun addPolygon(
        polygon: org.json.JSONArray,
        path: Path,
        outerRings: MutableList<List<Offset>>
    ) {
        for (r in 0 until polygon.length()) {
            val ring = polygon.getJSONArray(r)
            val pts = ArrayList<Offset>(ring.length())
            for (c in 0 until ring.length()) {
                val pt = ring.getJSONArray(c)
                val projected = MapProjection.project(pt.getDouble(0), pt.getDouble(1))
                pts.add(projected)
                if (c == 0) path.moveTo(projected.x, projected.y)
                else path.lineTo(projected.x, projected.y)
            }
            path.close()
            if (r == 0) outerRings.add(pts) // only the outer ring is used for hit-testing
        }
    }

    /** Ray-casting point-in-polygon over a shape's outer rings (normalized coords). */
    fun contains(shape: CountryShape, p: Offset): Boolean {
        for (ring in shape.rings) {
            if (ringContains(ring, p)) return true
        }
        return false
    }

    private fun ringContains(ring: List<Offset>, p: Offset): Boolean {
        var inside = false
        var j = ring.size - 1
        for (i in ring.indices) {
            val a = ring[i]
            val b = ring[j]
            if (((a.y > p.y) != (b.y > p.y)) &&
                (p.x < (b.x - a.x) * (p.y - a.y) / ((b.y - a.y) + 1e-9f) + a.x)
            ) inside = !inside
            j = i
        }
        return inside
    }
}
