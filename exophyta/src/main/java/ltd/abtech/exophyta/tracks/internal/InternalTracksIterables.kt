package ltd.abtech.exophyta.tracks.internal

import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

internal data class FormatWithIndecies(val format: Format, val groupIndex: Int, val trackIngex: Int)
internal data class TrackGroupArrayWithIndecies(
    val trackGroupArray: TrackGroupArray,
    val renderType: Int,
    val renderIndex: Int
)

internal fun TrackSelection.iterable(): Iterable<Format> {
    val formats = mutableListOf<Format>()
    for (trackIndex in 0 until length()) {
        formats += getFormat(trackIndex)
    }
    return formats
}

internal fun TrackGroup.iterable(): Iterable<Format> {
    val formats = mutableListOf<Format>()
    for (trackIndex in 0 until length) {
        formats += getFormat(trackIndex)
    }
    return formats
}


internal fun TrackSelectionArray.iterable(): Iterable<Format?> {
    return all.filterNotNull().map {
        it.iterable()
    }.flatten()
}

internal fun TrackGroupArray.iterable(): Iterable<TrackGroup> {
    val trackGroups = mutableListOf<TrackGroup>()
    for (index in 0 until length) {
        trackGroups += get(index)
    }
    return trackGroups
}

internal fun Metadata.iterable(): Iterable<Metadata.Entry> {
    val entries = mutableListOf<Metadata.Entry>()
    for (index in 0 until length()) {
        entries += get(index)
    }
    return entries
}

internal fun MappingTrackSelector.MappedTrackInfo.iterable(): Iterable<TrackGroupArray> {
    val trackGroupArrays = mutableListOf<TrackGroupArray>()
    for (index in 0 until rendererCount) {
        trackGroupArrays += getTrackGroups(index)
    }
    return trackGroupArrays
}

internal fun TrackGroupArray.toFormatsWithIndices(): List<FormatWithIndecies> {
    val formats = mutableListOf<FormatWithIndecies>()
    iterable().forEachIndexed { groupIndex, trackGroup ->
        trackGroup.iterable().forEachIndexed { trackIndex, format ->
            formats += FormatWithIndecies(format, groupIndex, trackIndex)
        }
    }
    return formats
}

internal fun MappingTrackSelector.MappedTrackInfo.toTrackGroupArrayWithIndecies(): List<TrackGroupArrayWithIndecies> {
    return iterable().mapIndexed { index, trackGroupArray ->
        TrackGroupArrayWithIndecies(trackGroupArray, getRendererType(index), index)
    }
}