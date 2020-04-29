package ltd.abtech.exophyta.tracks

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import ltd.abtech.exophyta.tracks.internal.*

fun ExoPlayer.getSubtitles(mimeType: TrackMimeType, context: Context?): List<Track> {
    return getTracks(mimeType, context)
}

fun ExoPlayer.setSubtitlesAvailableListener(trackMimeType: TrackMimeType, block: (Tracks) -> Unit) {
    setTracksAvailableListener(trackMimeType, block)
}

fun DefaultTrackSelector.selectSubtitle(track: Track) {
    selectTrack(track)
}

fun DefaultTrackSelector.disableSubtitles() {
    currentMappedTrackInfo?.iterable()?.firstOrNull { it.renderType == C.TRACK_TYPE_TEXT }
        ?.let { trackGroups ->
            setParameters(
                buildUponParameters()
                    .clearSelectionOverride(trackGroups.renderIndex, trackGroups.trackGroupArray)
                    .setSelectionOverride(
                        trackGroups.renderIndex,
                        trackGroups.trackGroupArray, null
                    )
            )
        }
}

//-----


fun ExoPlayer.getAudioTracks(context: Context?): List<Track> {
    val tracks = getTracks(TrackMimeType.Audio, context)
    if (tracks.size <= 1) {
        //we filter only one track - it's default track, no need to display it
        return emptyList()
    }
    return tracks
}

fun ExoPlayer.setAudioTracksAvailableListener(block: (Tracks) -> Unit) {
    setTracksAvailableListener(TrackMimeType.Audio) {
        if (it.size > 1) {
            block(it)
        }
    }
}

fun DefaultTrackSelector.selectAudioTrack(track: Track) {
    selectTrack(track)
}