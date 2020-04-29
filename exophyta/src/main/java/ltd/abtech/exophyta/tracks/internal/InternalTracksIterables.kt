package ltd.abtech.exophyta.tracks.internal

import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

internal data class FormatWithIndecies(val format: Format, val groupIndex: Int, val trackIngex: Int)
internal data class TrackGroupArrayWithIndecies(
    val trackGroupArray: TrackGroupArray,
    val renderType: Int,
    val renderIndex: Int
)

internal fun TrackSelectionArray.iterable(): Iterable<Format?> {
    return object : Iterable<Format?> {
        override operator fun iterator(): Iterator<Format?> {
            return object : Iterator<Format?> {
                private var arrayIndex = 0
                private var trackSelectionIndex = 0

                override operator fun hasNext(): Boolean {
                    if (arrayIndex < length) {
                        val trackSelection = this@iterable[arrayIndex]
                        if (trackSelection != null) {
                            return trackSelectionIndex < trackSelection.length()
                        }
                        return true
                    }
                    return false
                }

                override operator fun next(): Format? {
                    val format = this@iterable[arrayIndex]?.getFormat(trackSelectionIndex)
                    trackSelectionIndex++
                    if (format == null || trackSelectionIndex == this@iterable[arrayIndex]?.length()) {
                        trackSelectionIndex = 0
                        arrayIndex++
                    }
                    return format
                }
            }
        }
    }
}

internal fun TrackGroupArray.iterable(): Iterable<FormatWithIndecies> {
    return object : Iterable<FormatWithIndecies> {
        override operator fun iterator(): Iterator<FormatWithIndecies> {
            return object : Iterator<FormatWithIndecies> {
                private var groupIndex = 0
                private var trackIndex = 0

                override operator fun hasNext(): Boolean {
                    return groupIndex < length && trackIndex < this@iterable[groupIndex].length
                }

                override operator fun next(): FormatWithIndecies {
                    val formatwi = FormatWithIndecies(
                        this@iterable[groupIndex].getFormat(trackIndex),
                        groupIndex,
                        trackIndex
                    )
                    trackIndex++
                    if (trackIndex == this@iterable[groupIndex].length) {
                        trackIndex = 0
                        groupIndex++
                    }
                    return formatwi
                }
            }
        }
    }
}

internal fun MappingTrackSelector.MappedTrackInfo.iterable(): Iterable<TrackGroupArrayWithIndecies> {
    return object : Iterable<TrackGroupArrayWithIndecies> {
        override operator fun iterator(): Iterator<TrackGroupArrayWithIndecies> {
            return object : Iterator<TrackGroupArrayWithIndecies> {
                private var index = 0

                override operator fun hasNext(): Boolean {
                    return index < rendererCount
                }

                override operator fun next(): TrackGroupArrayWithIndecies {
                    val trackGroupArray = TrackGroupArrayWithIndecies(
                        getTrackGroups(index),
                        getRendererType(index),
                        index
                    )
                    index++
                    return trackGroupArray
                }
            }
        }
    }
}