package ltd.abtech.exophyta.subtitles

typealias Tracks = List<Track>

data class Track constructor(
    val isoCode: String,
    val name: String,
    val selected: Boolean
)

fun Tracks.isAllDisabled() = all { !it.selected }

fun Tracks.firstSelected() = first { it.selected }

fun Tracks.indexOfFirstSelected() = indexOfFirst { it.selected }
