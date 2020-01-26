package ltd.abtech.exomple

import android.annotation.TargetApi
import android.media.MediaDrm
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Log

class MainActivity : AppCompatActivity() {

    private class LoggerDefaultDrmSessionEventListener : DefaultDrmSessionEventListener {
        override fun onDrmSessionAcquired() {
            Log.d("ExoMple", "exoplayer drm: onDrmSessionAcquired")
        }

        override fun onDrmKeysRestored() {
            Log.d("ExoMple", "exoplayer drm: onDrmKeysRestored")

        }

        override fun onDrmKeysLoaded() {
            Log.d("ExoMple", "exoplayer drm: onDrmKeysLoaded")

        }

        override fun onDrmKeysRemoved() {
            Log.d("ExoMple", "exoplayer drm: onDrmKeysRemoved")

        }

        override fun onDrmSessionManagerError(error: Exception) {
            Log.d("ExoMple", "exoplayer drm: onDrmSessionManagerError: $error")

        }

        override fun onDrmSessionReleased() {
            Log.d("ExoMple", "exoplayer drm: onDrmSessionReleased")
        }
    }

//    drm license server: http://192.168.52.46:5000/proxy

//    open: http://192.168.107.250:1935/vod/mp4:star_trails.mp4/manifest.mpd
//    open: http://192.168.107.250:1935/vod/mp4:jellyfish.mp4/manifest.mpd
//    drm: http://192.168.107.250:1935/vod/mp4:sample.mp4/manifest.mpd

    companion object {
        private const val URL = "http://rdmedia.bbc.co.uk/dash/ondemand/testcard/1/client_manifest-events.mpd"
        private const val LICENSE_URL = ""

        private const val USERAGENT = "useragent"
    }

    private var exoPlayer: SimpleExoPlayer? = null
    private var exoMediaDrm: ExoMediaDrm<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val playerView = findViewById<PlayerView>(R.id.playerView)

        Log.setLogLevel(Log.LOG_LEVEL_ALL)
        exoPlayer = SimpleExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
    }

    override fun onStart() {
        super.onStart()

        val drmSessionManager = createDrmManager()

        if (drmSessionManager != null) {
            val dashMediaSourceFactory =
                DashMediaSource.Factory(DefaultHttpDataSourceFactory(USERAGENT))
            dashMediaSourceFactory.setDrmSessionManager(drmSessionManager)

            setupPlayer()

            exoPlayer?.prepare(dashMediaSourceFactory.createMediaSource(Uri.parse(URL)))
        } else {
            Toast.makeText(this, "Problem with drm", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupPlayer() {
        exoPlayer?.playWhenReady = true

        exoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException) {
                Log.e("ExoMple", "exoplayer error: $error")
            }
        })
        exoPlayer?.addAnalyticsListener(EventLogger(null))
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createDrmManager(): DrmSessionManager<*>? {
        val httpDataSourceFactory = DefaultHttpDataSourceFactory(USERAGENT)


        val httpMediaDrmCallback = HttpMediaDrmCallback(LICENSE_URL, httpDataSourceFactory)
        val mediaDrmProvider = FrameworkMediaDrm.DEFAULT_PROVIDER as ExoMediaDrm.Provider<*>
        exoMediaDrm = mediaDrmProvider.acquireExoMediaDrm(C.WIDEVINE_UUID)

        exoMediaDrm?.setOnKeyStatusChangeListener { mediaDrm, sessionId, exoKeyInformation, hasNewUsableKey ->
            Log.d(
                "ExoMple",
                "ExoMediaDrm, keyEventListener:: MediaDrm: $mediaDrm, sessionId: $sessionId, exoKeyInformation: $exoKeyInformation, hasNewUsableKey: $hasNewUsableKey"
            )

        }
        exoMediaDrm?.setOnEventListener { mediaDrm, sessionId, event, extra, data ->
            Log.d(
                "ExoMple",
                "ExoMediaDrm, eventListener:: MediaDrm: $mediaDrm, sessionId: $sessionId, event: $event, extra: $extra, data: $data"
            )
        }

//        val exoUniqueId1 = exoMediaDrm?.getPropertyString(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
//        Log.d("ExoMple", "exoUniqueId1: $exoUniqueId1")

        val exoUniqueIdArray = exoMediaDrm?.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
        exoUniqueIdArray?.let {
            val exoUniqueId2 = Base64.encodeToString(exoUniqueIdArray, Base64.DEFAULT)
            Log.d("ExoMple", "exoUniqueId2: $exoUniqueId2")
            Log.d("ExoMple", "exoUniqueIdByteArray: ${exoUniqueIdArray.asList().map { it.toChar() }}")
        }

        val deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("ExoMple", "deviceId: $deviceId")
//
//        val wvDrm = try {
//            MediaDrm(WIDEVINE_UUID)
//        } catch (e: UnsupportedSchemeException) {
//            //WIDEVINE is not available
//            null
//        }
//
//        wvDrm!!.apply {
//            val widevineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
//            val encodedWidevineId = java.util.Base64.getEncoder().encodeToString(widevineId).trim()
//            Log.d("ExoMple", "Widevine ID:$encodedWidevineId")
//            wvDrm.close()
//        }

        exoMediaDrm?.release()
        exoMediaDrm = null

        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, mediaDrmProvider)
            .build(httpMediaDrmCallback)
        drmSessionManager.addListener(Handler(), LoggerDefaultDrmSessionEventListener())
        return drmSessionManager
    }

    override fun onStop() {
        super.onStop()
        exoMediaDrm?.release()
        exoMediaDrm = null

        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }
}
