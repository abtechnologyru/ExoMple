package ltd.abtech.exophyta.subtitles.internal

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import ltd.abtech.exophyta.subtitles.Track
import ltd.abtech.exophyta.subtitles.TrackMimeType

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