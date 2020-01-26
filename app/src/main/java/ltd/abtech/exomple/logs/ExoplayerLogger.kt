package ltd.abtech.exomple.logs

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.util.EventLogger
import timber.log.Timber

class LoggerExoplayerEvents : Player.EventListener {
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        Timber.d("player, onPlaybackParametersChanged, params: $playbackParameters")
    }

    override fun onSeekProcessed() {
        Timber.d("player, onSeekProcessed")
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        Timber.d("player, onTracksChanged, trackGroups: $trackGroups, trackSelections: $trackSelections")
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        Timber.e("player, onPlayerError, error: $error")
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        Timber.d("player, onLoadingChanged, isLoading: $isLoading")
    }

    override fun onPositionDiscontinuity(reason: Int) {
        Timber.d("player, onPositionDiscontinuity, reason: $reason")
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        Timber.d("player, onRepeatModeChanged, repeatMode: $repeatMode")
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        Timber.d("player, onShuffleModeEnabledChanged, shuffleModeEnabled: $shuffleModeEnabled")
    }

    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
        Timber.d("player, onPlaybackSuppressionReasonChanged, playbackSuppressionReason: $playbackSuppressionReason")
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        Timber.d("player, onTimelineChanged, timeline: $timeline, reason: $reason")
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        Timber.d("player, onTimelineChanged, timeline: $timeline, manifest: $manifest, reason: $reason")
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Timber.d("player, onPlayerStateChanged, playWhenReady: $playWhenReady, playbackState: $playbackState")

    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Timber.d("player, onIsPlayingChanged, isPlaying: $isPlaying")
    }
}

class LoggerAnalytics(defaultTrackSelector: DefaultTrackSelector) :
    EventLogger(defaultTrackSelector) {
    override fun loge(msg: String, tr: Throwable?) {
        Timber.e("analytics, error, msg: $msg, t: $tr")
    }

    override fun logd(msg: String) {
        Timber.d("analytics, debug, msg:$msg")
    }
}