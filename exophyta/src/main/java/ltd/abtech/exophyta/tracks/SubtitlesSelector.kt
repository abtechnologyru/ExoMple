package ltd.abtech.exophyta.tracks

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import ltd.abtech.exophyta.tracks.internal.getTracks
import ltd.abtech.exophyta.tracks.internal.selectTrack
import ltd.abtech.exophyta.tracks.internal.setTracksAvailableListener
import ltd.abtech.exophyta.tracks.internal.toTrackGroupArrayWithIndecies

class SubtitlesSelector(
    private val mimeType: TrackMimeType,
    private val exoPlayer: ExoPlayer,
    private val defaultTrackSelector: DefaultTrackSelector
) {
    fun getSubtitles(context: Context?): List<Track> {
        return exoPlayer.getTracks(mimeType, context)
    }

    fun setSubtitlesAvailableListener(block: (Tracks) -> Unit) {
        exoPlayer.setTracksAvailableListener(mimeType, block)
    }

    fun selectSubtitle(track: Track) {
        defaultTrackSelector.selectTrack(track)
    }

    fun disableSubtitles() {
        with(defaultTrackSelector) {
            currentMappedTrackInfo?.toTrackGroupArrayWithIndecies()
                ?.firstOrNull { it.renderType == C.TRACK_TYPE_TEXT }
                ?.let { trackGroups ->
                    setParameters(
                        buildUponParameters()
                            .clearSelectionOverride(
                                trackGroups.renderIndex,
                                trackGroups.trackGroupArray
                            )
                            .setSelectionOverride(
                                trackGroups.renderIndex,
                                trackGroups.trackGroupArray, null
                            )
                    )
                }
        }

    }
}