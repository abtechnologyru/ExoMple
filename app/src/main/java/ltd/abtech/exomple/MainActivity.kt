package ltd.abtech.exomple

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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
import ltd.abtech.exophyta.subtitles.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

//    drm license server: http://192.168.52.46:5000/proxy

//    open: http://192.168.107.250:1935/vod/mp4:star_trails.mp4/manifest.mpd
//    open: http://192.168.107.250:1935/vod/mp4:jellyfish.mp4/manifest.mpd
//    drm: http://192.168.107.250:1935/vod/mp4:sample.mp4/manifest.mpd

    companion object {

        //dash: "http://rdmedia.bbc.co.uk/dash/ondemand/testcard/1/client_manifest-events.mpd"
        //hls: "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8"
        //hls aes: http://try.mediastage.tv/streamingGateway/GetPlayList?fileName=bomfunk4aes.OTT_HLS_CUSTOM.m3u8&serviceArea=LABSPB
        //ss: "http://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism/Manifest"

        // "https://file-examples.com/wp-content/uploads/2017/04/file_example_MP4_480_1_5MG.mp4"
        //udp: "udp://@239.90.10.132:5000"

        private const val URL =
            "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"
        private const val LICENSE_URL = ""

        private const val USERAGENT = "useragent"
    }

    private lateinit var defaultTrackSelector: DefaultTrackSelector

    private lateinit var exoPlayer: SimpleExoPlayer
    private var exoMediaDrm: ExoMediaDrm<*>? = null

    private lateinit var subtitlesBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val playerView = findViewById<PlayerView>(R.id.playerView)
        val urlView = findViewById<EditText>(R.id.url)
        urlView.setText(URL)

        findViewById<Button>(R.id.button).setOnClickListener {
            val uri = Uri.parse(urlView.text.toString())
            val mediaSource = createMediaSource(uri)

            mediaSource?.let {
                exoPlayer.prepare(it)
            } ?: Toast.makeText(this, "Can't create MediaSource", Toast.LENGTH_SHORT).show()

        }

        subtitlesBtn = findViewById(R.id.subtitlesBtn)
        with(subtitlesBtn) {
            visibility = View.GONE
            setOnClickListener {
                exoPlayer.getSubtitles(TrackMimeType.WebVtt, this@MainActivity).showPopup()
            }
        }
        subtitlesBtn.visibility = View.GONE

        Log.setLogLevel(Log.LOG_LEVEL_ALL)

        defaultTrackSelector = DefaultTrackSelector(this)
        exoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(defaultTrackSelector).build()
        playerView.player = exoPlayer
    }

    override fun onStart() {
        super.onStart()

        setupPlayer()

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

        return when (@ContentType Util.inferContentType(uri)) {
            C.TYPE_DASH -> DashMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(
                uri
            )
            C.TYPE_HLS -> HlsMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(uri)
            C.TYPE_SS -> SsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(uri)
            C.TYPE_OTHER -> {
                //Util.inferContentType(uri) checks only endWith(".m3u8") for HLS stream
                //But some streams may have ".m3u8" at the middle of URI.
                if (uri.toString().contains(".m3u8")) {
                    HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(uri)
                } else {
                    val udpDataSourceFactory = DataSource.Factory { UdpDataSource() }
                    val dataSourceFactory =
                        if ("udp" == uri.scheme) udpDataSourceFactory else defaultHttpDataSourceFactory
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                }
            }
            else -> {
                Log.e("ExoMple", "Unknown URI format, can't create MediaSource")
                null
            }
        }
    }

    private fun setupPlayer() {
        exoPlayer.playWhenReady = true

        exoPlayer.addListener(LoggerExoplayerEvents())
        exoPlayer.addAnalyticsListener(LoggerAnalytics(defaultTrackSelector))


        exoPlayer.setSubtitlesAvailableListener(TrackMimeType.WebVtt) {
            Timber.e("ExoMple subtitles: $it")
            subtitlesBtn.visibility = View.VISIBLE
        }
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

        exoPlayer.stop()
        exoPlayer.release()
    }

    @SuppressLint("DefaultLocale")
    private fun Tracks.showPopup() {
        if (isNotEmpty()) {
            val popup = PopupMenu(this@MainActivity, subtitlesBtn)
            popup.menu.add(
                0,
                0,
                0,
                "Disabled"
            ).isChecked = isAllDisabled()
            forEachIndexed { index, track ->
                popup.menu.add(0, index, 0, track.name.capitalize()).isChecked = track.selected
            }
            popup.menu.setGroupCheckable(0, true, true)

            popup.setOnMenuItemClickListener {
                if (it.itemId == 0) {
                    defaultTrackSelector.disableSubtitles()
                } else {
                    defaultTrackSelector.selectSubtitle(get(it.itemId))
                }
                true
            }
            popup.show()
        }
    }
}
