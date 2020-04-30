package ltd.abtech.exophyta.tracks

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import ltd.abtech.exophyta.tracks.internal.getTracks
import ltd.abtech.exophyta.tracks.internal.selectTrack
import ltd.abtech.exophyta.tracks.internal.setTracksAvailableListener
import ltd.abtech.exophyta.tracks.internal.toTrackGroupArrayWithIndecies

class AudioTracksSelector(
    private val exoPlayer: ExoPlayer,
    private val defaultTrackSelector: DefaultTrackSelector
) {
    private val defMimeType = TrackMimeType.Audio

    fun getAudioTracks(context: Context?): List<Track> {
        val tracks = exoPlayer.getTracks(defMimeType, context)
        if (tracks.size > 1) {
            return tracks
        }
        return emptyList()
    }

    fun setAudioTracksAvailableListener(block: (Tracks) -> Unit) {
        exoPlayer.setTracksAvailableListener(defMimeType) {
            if (it.size > 1) {
                block(it)
            }
        }
    }

    fun selectAudioTrack(track: Track) {
        defaultTrackSelector.selectTrack(track)
    }
}