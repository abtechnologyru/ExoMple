package ltd.abtech.exophyta.subtitles

import com.google.android.exoplayer2.util.MimeTypes

sealed class TrackMimeType() {
    abstract val value: String

    object WebVtt : TrackMimeType() {
        override val value =
            MimeTypes.TEXT_VTT
    }

    data class Other(override val value: String) : TrackMimeType()
}

typealias Tracks = List<Track>

data class Track constructor(
    val isoCode: String,
    val name: String,
    val selected: Boolean,
    val trackMimeType: TrackMimeType
)

fun Tracks.isAllDisabled() = all { !it.selected }

fun Tracks.firstSelected() = first { it.selected }

fun Tracks.indexOfFirstSelected() = indexOfFirst { it.selected }
