# ExoMple
This is the sample showcase app how to use ExoPlayer with minimal config.
Also this is include a small library Exophyta - small Kotlin extensions to make some things with Exo easily.

Currently it contains extensions to get/set subtitles and audio tracks:
```kotlin
fun ExoPlayer.getSubtitlesSelector(mimeType: TrackMimeType, 
      defaultTrackSelector: DefaultTrackSelector) : SubtitlesSelector

fun ExoPlayer.getAudioTracksSelector(defaultTrackSelector: DefaultTrackSelector) : AudioTracksSelector
```



## Download
```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.abtechnologyru.ExoMple:exophyta:0.0.6"
}
```
