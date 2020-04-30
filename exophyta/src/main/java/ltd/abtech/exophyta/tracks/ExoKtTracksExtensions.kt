package ltd.abtech.exophyta.tracks

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

fun ExoPlayer.getSubtitlesSelector(
    mimeType: TrackMimeType,
    defaultTrackSelector: DefaultTrackSelector
) = SubtitlesSelector(mimeType, this, defaultTrackSelector)

fun ExoPlayer.getAudioTracksSelector(
    defaultTrackSelector: DefaultTrackSelector
) = AudioTracksSelector(this, defaultTrackSelector)
