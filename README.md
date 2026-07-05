# World Explorer Kids 🌍

An **audio-first, visual-first** learning app for children aged 4–10. A child
taps a country on a real world map, a friendly voice greets them, and every
topic — animals, food, mountains, festivals — is a swipe-through gallery of
beautiful photos that narrate themselves. **Reading is optional.**

> Think *National Geographic Kids + Disney + Google Earth for children*.

## How it works

1. **World map** — a real, pinch-zoom, pan, tappable vector map of all 180
   countries. Built countries glow green; the rest are "coming soon". A search
   box flies the map to any country.
2. **Country page** — big flag, the country's name (tap to hear it), and a grid
   of colourful lesson icons. A warm voice greets the child automatically.
3. **Lesson** — a full-screen photo gallery with a slow Ken Burns zoom.
   Narration plays on its own; a soft ambient loop sets the mood; the child
   just looks, listens, and swipes.

## Design principles

- **70% visuals · 25% audio · 5% text.** Every screen must be understandable by
  a child who cannot yet read.
- On-device Text-to-Speech is the narrator — warm, slightly slower, friendly.
  Names are respelled phonetically in the data (e.g. `soo-shee`) so they sound
  right. **No bundled voice files needed.**
- Photos stream on demand from Wikimedia Commons (public-domain / CC), and every
  image URL is verified to load before it ships.
- Safe by design: no politics, war, religion, or anything upsetting.

## Architecture

```
data/           Country + Lesson models, JSON repository (assets/countries/)
audio/          NarrationController (TTS)  ·  AmbientSoundPlayer (Media3)
navigation/     NavHost, routes, app-wide providers
screens/
  worldmap/     Vector map: GeoJSON parse, projection, pan/zoom, hit-test
  country/      Flag + icon-card grid
  lesson/       Swipe gallery, Ken Burns, auto-narration
ui/             Kid-friendly theme, shared components
assets/
  world.geo.json          country shapes (ISO-A3)
  countries/index.json    all 180 countries (lightweight)
  countries/<id>.json     full country content (loaded on demand)
```

Adding a country = drop a `countries/<id>.json` (same shape as `jp.json`) and
flip `ready: true` in `index.json`. That's the whole content pipeline.

## Content status

- ✅ **Japan** — golden template, 19 narrated visual lessons.
- ⏳ All other countries — appear on the map as "coming soon", built next using
  the Japan template with lighter content first (6–8 core lessons each).

## Building

No local Android tooling required — GitHub Actions builds it:

- Every push to `main` builds a **debug APK** (download from the Actions run
  artifacts, install on any Android phone).
- Add signing secrets (`KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`,
  `STORE_PASSWORD`) to also produce a Play Store **release AAB**.

Minimum Android 7.0 (API 24). Needs internet for the streamed photos.

## Roadmap

- [ ] Ambient sound loops (drop CC0 `.mp3`s into `assets/ambient/`)
- [ ] Lottie weather/water/particle overlays
- [ ] Build out remaining countries (Egypt, Brazil, France, Kenya, India…)
- [ ] Adaptive launcher icon + custom rounded kids font
- [ ] Reward moments (stars, stickers) after finishing a country
