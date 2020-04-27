package ltd.abtech.exophyta.subtitles

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
import com.google.android.exoplayer2.util.MimeTypes

internal data class FormatWithIndecies(val format: Format, val groupIndex: Int, val trackIngex: Int)
internal data class TrackGroupArrayWithIndecies(
    val trackGroupArray: TrackGroupArray,
    val renderIndex: Int
)

sealed class SubtitlesMimeType() {
    abstract val value: String

    object WebVtt : SubtitlesMimeType() {
        override val value = MimeTypes.TEXT_VTT
    }

    data class Other(override val value: String) : SubtitlesMimeType()
}

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

fun ExoPlayer.getSelectedVttSubtitles(mimeType: SubtitlesMimeType): List<String> {
    val selectedVttLangs = mutableListOf<String>()
    currentTrackSelections.forEachSelection { format ->
        val lang = format.language
        if (format.sampleMimeType == mimeType.value && lang != null) {
            selectedVttLangs += lang
        }
    }
    return selectedVttLangs
}

fun ExoPlayer.getVttSubtitles(mimeType: SubtitlesMimeType, context: Context?): List<Track> {
    val selectedVttLangs = getSelectedVttSubtitles(mimeType)

    val tracks = mutableListOf<Track>()
    val trackNameProvider =
        if (context != null) DefaultTrackNameProvider(context.resources) else null
    currentTrackGroups.forEachFormat { format, _, _ ->
        val lang = format.language
        if (format.sampleMimeType == mimeType.value && lang != null) {
            tracks += Track(
                lang,
                trackNameProvider?.getTrackName(format) ?: lang,
                selectedVttLangs.contains(lang)
            )
        }
    }
    return tracks
}

fun DefaultTrackSelector.selectTrackByIsoCodeAndType(isoCode: String, mimeType: SubtitlesMimeType) {
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

fun ExoPlayer.setSubtitlesAvailable(subtitlesMimeType: SubtitlesMimeType, block: (Tracks) -> Unit) {
    addListener(object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            val tracks = getVttSubtitles(subtitlesMimeType, null)
            if (tracks.isNotEmpty()) {
                block(tracks)
            }
        }
    })
}