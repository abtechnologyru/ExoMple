package ltd.abtech.exophyta.tracks.internal

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import ltd.abtech.exophyta.tracks.Track
import ltd.abtech.exophyta.tracks.TrackMimeType
import ltd.abtech.exophyta.tracks.Tracks

internal data class FormatWithIndecies(val format: Format, val groupIndex: Int, val trackIngex: Int)
internal data class TrackGroupArrayWithIndecies(
    val trackGroupArray: TrackGroupArray,
    val renderIndex: Int
)

internal fun TrackSelectionArray.forEachSelection(action: (format: Format) -> Unit) {
    all.forEach {
        if (it != null) {
            for (trackSelectionIndex in 0 until it.length()) {
                val format = it.getFormat(trackSelectionIndex)
                action(format)
            }
        }
    }
}

internal fun TrackGroupArray.forEachFormat(action: (format: Format, groupIndex: Int, trackIngex: Int) -> Unit) {
    for (groupIndex in 0 until length) {
        val trackGroup = this[groupIndex]
        for (trackIndex in 0 until trackGroup.length) {
            val format = trackGroup.getFormat(trackIndex)
            action(format, groupIndex, trackIndex)
        }
    }
}

internal fun TrackGroupArray.firstFormatWithIndeciesOrNull(predicate: (Format) -> Boolean): FormatWithIndecies? {
    for (groupIndex in 0 until length) {
        val trackGroup = this[groupIndex]
        for (trackIndex in 0 until trackGroup.length) {
            val format = trackGroup.getFormat(trackIndex)
            if (predicate(format)) {
                return FormatWithIndecies(
                    format,
                    groupIndex,
                    trackIndex
                )
            }
        }
    }

    return null
}

internal fun MappingTrackSelector.MappedTrackInfo.firstTrackGroupArrayOrNull(renderType: Int): TrackGroupArrayWithIndecies? {
    for (renderIndex in 0 until rendererCount) {
        if (getRendererType(renderIndex) == renderType) {
            return TrackGroupArrayWithIndecies(
                getTrackGroups(renderIndex),
                renderIndex
            )
        }
    }

    return null
}

internal fun ExoPlayer.getSelectedTracks(mimeType: TrackMimeType): List<Format> {
    val selectedLangs = mutableListOf<Format>()
    currentTrackSelections.forEachSelection { format ->
        val fit = if (mimeType.strictly) {
            format.sampleMimeType == mimeType.value
        } else {
            format.sampleMimeType.orEmpty().contains(mimeType.value)
        }
        if (fit) {
            selectedLangs += format
        }
    }
    return selectedLangs
}

internal fun DefaultTrackSelector.selectTrack(track: Track) {
    val mimeType = track.trackMimeType
    val trackType =
        if (mimeType.type == TrackMimeType.Type.Audio) C.TRACK_TYPE_AUDIO else C.TRACK_TYPE_TEXT
    currentMappedTrackInfo?.firstTrackGroupArrayOrNull(trackType)?.let { trackGroups ->
        trackGroups.trackGroupArray.firstFormatWithIndeciesOrNull {
            val fit = if (mimeType.strictly) {
                it.sampleMimeType == mimeType.value
            } else {
                it.sampleMimeType.orEmpty().contains(mimeType.value)
            }
            fit && it.id == track.formatId
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