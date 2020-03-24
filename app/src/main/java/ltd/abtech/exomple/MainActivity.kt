package ltd.abtech.exomple

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.UdpDataSource
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import ltd.abtech.exomple.logs.*

class MainActivity : AppCompatActivity() {

//    drm license server: http://192.168.52.46:5000/proxy

//    open: http://192.168.107.250:1935/vod/mp4:star_trails.mp4/manifest.mpd
//    open: http://192.168.107.250:1935/vod/mp4:jellyfish.mp4/manifest.mpd
//    drm: http://192.168.107.250:1935/vod/mp4:sample.mp4/manifest.mpd

    companion object {

        //dash: "http://rdmedia.bbc.co.uk/dash/ondemand/testcard/1/client_manifest-events.mpd"
        //hls: "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8"
        //ss: "http://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism/Manifest"
        //udp: "udp://@239.255.105.19:5000"
        private const val URL =
            "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"
        private const val LICENSE_URL = ""

        private const val USERAGENT = "useragent"
    }

    private lateinit var defaultTrackSelector: DefaultTrackSelector

    private var exoPlayer: SimpleExoPlayer? = null
    private var exoMediaDrm: ExoMediaDrm<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val playerView = findViewById<PlayerView>(R.id.playerView)

        Log.setLogLevel(Log.LOG_LEVEL_ALL)

        defaultTrackSelector = DefaultTrackSelector(this)
        exoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(defaultTrackSelector).build()
        playerView.player = exoPlayer
    }

    override fun onStart() {
        super.onStart()

        setupPlayer()

        val uri = Uri.parse(URL)

        val mediaSource = createMediaSource(uri)

        mediaSource?.let {
            exoPlayer?.prepare(it)
        } ?: Toast.makeText(this, "Can't create MediaSource", Toast.LENGTH_SHORT).show()


//        val drmSessionManager = createDrmManager()
//
//        if (drmSessionManager != null) {
//            val dashMediaSourceFactory =
//                DashMediaSource.Factory(DefaultHttpDataSourceFactory(USERAGENT))
//            dashMediaSourceFactory.setDrmSessionManager(drmSessionManager)
//
//            setupPlayer()
//
//
//
////            exoPlayer?.prepare(dashMediaSourceFactory.createMediaSource(Uri.parse(URL)))
//
//        } else {
//            Toast.makeText(this, "Problem with drm", Toast.LENGTH_SHORT).show()
//            finish()
//        }
    }

    private fun createMediaSource(uri: Uri): MediaSource? {
        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory(USERAGENT)
        val udpDataSourceFactory = object : DataSource.Factory {
            override fun createDataSource() = UdpDataSource()
        }

        val type = Util.inferContentType(uri)
        return when (@ContentType type) {
            C.TYPE_DASH -> DashMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(
                uri
            )
            C.TYPE_HLS -> HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(uri)
            C.TYPE_SS -> SsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(uri)
            C.TYPE_OTHER ->  {
                val dataSourceFactory = if ("udp".equals(uri.getScheme())) udpDataSourceFactory else defaultHttpDataSourceFactory
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            }
            else -> {
                Log.e("ExoMple", "Unknown URI format, can't create MediaSource")
                null
            }
        }
    }

    private fun setupPlayer() {
        exoPlayer?.playWhenReady = true

        exoPlayer?.addListener(LoggerExoplayerEvents())
        exoPlayer?.addAnalyticsListener(LoggerAnalytics(defaultTrackSelector))
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createDrmManager(): DrmSessionManager<*>? {
        val httpDataSourceFactory = DefaultHttpDataSourceFactory(USERAGENT)


        val httpMediaDrmCallback = HttpMediaDrmCallback(LICENSE_URL, httpDataSourceFactory)
        val mediaDrmProvider = FrameworkMediaDrm.DEFAULT_PROVIDER as ExoMediaDrm.Provider<*>
        exoMediaDrm = mediaDrmProvider.acquireExoMediaDrm(C.WIDEVINE_UUID)

        exoMediaDrm?.setOnKeyStatusChangeListener(LoggerDrmKeyStatus())
        exoMediaDrm?.setOnEventListener(LoggerDrmEvent())

//        val exoUniqueId1 = exoMediaDrm?.getPropertyString(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
//        Log.d("ExoMple", "exoUniqueId1: $exoUniqueId1")

//        val exoUniqueIdArray = exoMediaDrm?.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
//        exoUniqueIdArray?.let {
//            val exoUniqueId2 = Base64.encodeToString(exoUniqueIdArray, Base64.DEFAULT)
//            Log.d("ExoMple", "exoUniqueId2: $exoUniqueId2")
//            Log.d(
//                "ExoMple",
//                "exoUniqueIdByteArray: ${exoUniqueIdArray.asList().map { it.toChar() }}"
//            )
//        }
//
//        val deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        Log.d("ExoMple", "deviceId: $deviceId")
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
