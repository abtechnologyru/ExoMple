package ltd.abtech.exophyta.tracks

import com.google.android.exoplayer2.util.MimeTypes

sealed class TrackMimeType() {
    enum class Type {
        Audio, Text
    }

    abstract val value: String
    abstract val type: Type
    internal abstract val strictly: Boolean

    object WebVtt : TrackMimeType() {
        override val value =
            MimeTypes.TEXT_VTT
        override val type = Type.Text
        override val strictly = true
    }

    object Audio : TrackMimeType() {
        override val value =
            MimeTypes.BASE_TYPE_AUDIO
        override val type = Type.Audio
        override val strictly = false
    }

    data class Other(
        override val value: String,
        override val type: Type,
        override val strictly: Boolean
    ) : TrackMimeType()
}

typealias Tracks = List<Track>

data class Track constructor(
    val isoCode: String,
    val name: String,
    val selected: Boolean,
    val trackMimeType: TrackMimeType,
    internal val formatId: String?
)

fun Tracks.isAllDisabled() = all { !it.selected }

fun Tracks.firstSelected() = first { it.selected }

fun Tracks.indexOfFirstSelected() = indexOfFirst { it.selected }
