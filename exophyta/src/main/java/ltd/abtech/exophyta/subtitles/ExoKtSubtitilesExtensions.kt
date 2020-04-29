package ltd.abtech.exophyta.subtitles

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import ltd.abtech.exophyta.subtitles.internal.*
import ltd.abtech.exophyta.subtitles.internal.firstTrackGroupArrayOrNull
import ltd.abtech.exophyta.subtitles.internal.forEachFormat
import ltd.abtech.exophyta.subtitles.internal.getSelectedTracks

internal fun ExoPlayer.getTracks(mimeType: TrackMimeType, context: Context?): List<Track> {
    val selectedLangs = getSelectedTracks(mimeType)

    val tracks = mutableListOf<Track>()
    val trackNameProvider =
        if (context != null) DefaultTrackNameProvider(context.resources) else null
    currentTrackGroups.forEachFormat { format, _, _ ->
        val lang = format.language

        val fit = if (mimeType.strictly) {
            format.sampleMimeType == mimeType.value
        } else {
            format.sampleMimeType.orEmpty().contains(mimeType.value)
        }

        if (fit && lang != null) {
            tracks += Track(
                lang,
                trackNameProvider?.getTrackName(format) ?: lang,
                selectedLangs.contains(format),
                mimeType, format.id
            )
        }
    }
    return tracks
}

internal fun ExoPlayer.setTracksAvailableListener(
    trackMimeType: TrackMimeType,
    block: (Tracks) -> Unit
) {
    addListener(object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            val tracks = getTracks(trackMimeType, null)
            if (tracks.isNotEmpty()) {
                block(tracks)
            }
        }
    })
}

//------

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
    currentMappedTrackInfo?.firstTrackGroupArrayOrNull(C.TRACK_TYPE_TEXT)?.let { trackGroups ->
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