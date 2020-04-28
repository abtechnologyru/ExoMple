package ltd.abtech.exophyta.subtitles.internal

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
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

internal fun ExoPlayer.getSelectedSubtitles(mimeType: TrackMimeType): List<String> {
    val selectedLangs = mutableListOf<String>()
    currentTrackSelections.forEachSelection { format ->
        val lang = format.language
        if (format.sampleMimeType == mimeType.value && lang != null) {
            selectedLangs += lang
        }
    }
    return selectedLangs
}

internal fun DefaultTrackSelector.selectTrackByIsoCodeAndType(
    isoCode: String,
    mimeType: TrackMimeType
) {
    currentMappedTrackInfo?.firstTrackGroupArrayOrNull(C.TRACK_TYPE_TEXT)?.let { trackGroups ->
        trackGroups.trackGroupArray.firstFormatWithIndeciesOrNull {
            it.sampleMimeType == mimeType.value && it.language == isoCode
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