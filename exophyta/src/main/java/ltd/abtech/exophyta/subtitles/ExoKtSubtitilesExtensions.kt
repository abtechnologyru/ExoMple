package ltd.abtech.exophyta.subtitles

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import ltd.abtech.exophyta.subtitles.internal.firstTrackGroupArrayOrNull
import ltd.abtech.exophyta.subtitles.internal.forEachFormat
import ltd.abtech.exophyta.subtitles.internal.getSelectedSubtitles
import ltd.abtech.exophyta.subtitles.internal.selectTrackByIsoCodeAndType

fun ExoPlayer.getSubtitles(mimeType: TrackMimeType, context: Context?): List<Track> {
    val selectedLangs = getSelectedSubtitles(mimeType)

    val tracks = mutableListOf<Track>()
    val trackNameProvider =
        if (context != null) DefaultTrackNameProvider(context.resources) else null
    currentTrackGroups.forEachFormat { format, _, _ ->
        val lang = format.language
        if (format.sampleMimeType == mimeType.value && lang != null) {
            tracks += Track(
                lang,
                trackNameProvider?.getTrackName(format) ?: lang,
                selectedLangs.contains(lang),
                mimeType
            )
        }
    }
    return tracks
}

fun ExoPlayer.setSubtitlesAvailableListener(trackMimeType: TrackMimeType, block: (Tracks) -> Unit) {
    addListener(object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            val tracks = getSubtitles(trackMimeType, null)
            if (tracks.isNotEmpty()) {
                block(tracks)
            }
        }
    })
}

fun DefaultTrackSelector.selectSubtitle(track: Track) {
    selectTrackByIsoCodeAndType(track.isoCode, track.trackMimeType)
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