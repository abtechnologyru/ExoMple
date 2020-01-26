package ltd.abtech.exomple.logs

import com.google.android.exoplayer2.drm.DefaultDrmSessionEventListener
import com.google.android.exoplayer2.drm.ExoMediaCrypto
import com.google.android.exoplayer2.drm.ExoMediaDrm
import com.google.android.exoplayer2.util.Log
import timber.log.Timber

class LoggerDefaultDrmSessionEventListener : DefaultDrmSessionEventListener {
    override fun onDrmSessionAcquired() {
        Timber.d("drm: onDrmSessionAcquired")
    }

    override fun onDrmKeysRestored() {
        Timber.d("drm: onDrmKeysRestored")
    }

    override fun onDrmKeysLoaded() {
        Timber.d("drm: onDrmKeysLoaded")
    }

    override fun onDrmKeysRemoved() {
        Timber.d("drm: onDrmKeysRemoved")
    }

    override fun onDrmSessionManagerError(error: Exception) {
        Timber.e("drm: onDrmSessionManagerError: $error")
    }

    override fun onDrmSessionReleased() {
        Timber.d("drm: onDrmSessionReleased")
    }
}

class LoggerDrmKeyStatus<T : ExoMediaCrypto> : ExoMediaDrm.OnKeyStatusChangeListener<T> {
    override fun onKeyStatusChange(
        mediaDrm: ExoMediaDrm<out T>,
        sessionId: ByteArray,
        exoKeyInformation: MutableList<ExoMediaDrm.KeyStatus>,
        hasNewUsableKey: Boolean
    ) {
        Timber.d("drm, onKeyStatusChange, mediaDrm: $mediaDrm, sessionId: $sessionId, exoKeyInformation: $exoKeyInformation, hasNewUsableKey: $hasNewUsableKey")
    }
}

class LoggerDrmEvent<T : ExoMediaCrypto> : ExoMediaDrm.OnEventListener<T> {
    override fun onEvent(
        mediaDrm: ExoMediaDrm<out T>,
        sessionId: ByteArray?,
        event: Int,
        extra: Int,
        data: ByteArray?
    ) {
        Timber.d("drm, onEvent, mediaDrm: $mediaDrm, sessionId: $sessionId, event: $event, extra: $extra, data: $data")
    }

}