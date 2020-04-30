package ltd.abtech.exophyta.tracks.internal

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import ltd.abtech.exophyta.tracks.Track
import ltd.abtech.exophyta.tracks.TrackMimeType
import ltd.abtech.exophyta.tracks.Tracks


private fun Format.containsMimeType(mimeType: TrackMimeType): Boolean {
    return if (mimeType.strictly) {
        sampleMimeType == mimeType.value
    } else {
        sampleMimeType.orEmpty().contains(mimeType.value)
    }
}

internal fun ExoPlayer.getSelectedTracks(mimeType: TrackMimeType): List<Format> {
    val selectedLangs = mutableListOf<Format>()
    currentTrackSelections.iterable().filterNotNull().forEach { format ->
        if (format.containsMimeType(mimeType)) {
            selectedLangs += format
        }
    }
    return selectedLangs
}

internal fun DefaultTrackSelector.selectTrack(track: Track) {
    val mimeType = track.trackMimeType
    val trackType =
        if (mimeType.type == TrackMimeType.Type.Audio) C.TRACK_TYPE_AUDIO else C.TRACK_TYPE_TEXT
    currentMappedTrackInfo?.toTrackGroupArrayWithIndecies()?.firstOrNull { it.renderType == trackType }
        ?.let { trackGroups ->
            trackGroups.trackGroupArray.toFormatsWithIndices().firstOrNull {
                val format = it.format
                format.containsMimeType(mimeType) && format.id == track.formatId
            }?.let {
                setParameters(
                    buildUponParameters().clearSelectionOverride(
                        trackGroups.renderIndex,
                        trackGroups.trackGroupArray
                    )
                        .setSelectionOverride(
                            trackGroups.renderIndex,
                            trackGroups.trackGroupArray,
                            DefaultTrackSelector.SelectionOverride(it.groupIndex, it.trackIngex)
                        )
                )
            }
        }
}

internal fun ExoPlayer.getTracks(mimeType: TrackMimeType, context: Context?): List<Track> {
    val selectedLangs = getSelectedTracks(mimeType)

    val tracks = mutableListOf<Track>()
    val trackNameProvider =
        if (context != null) DefaultTrackNameProvider(context.resources) else null
    currentTrackGroups.toFormatsWithIndices().forEach { formatwi ->
        val format = formatwi.format
        val lang = format.language
        if (format.containsMimeType(mimeType) && lang != null) {
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