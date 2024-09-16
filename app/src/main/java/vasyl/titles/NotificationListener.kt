package vasyl.titles

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

@Suppress("DEPRECATION")
class NotificationListener : NotificationListenerService() {
    
    private lateinit var context: Context
    private lateinit var handler: Handler
    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var settings: SharedPreferences
    private lateinit var windowManager: WindowManager

    private var controllers: MutableList<MediaController>? = null
    private var overlayParam: Int = 0
    private var ll: LinearLayout? = null
    private var mediaController: MediaController? = null
    private var meta: MediaMetadata? = null
    private var paused: Boolean = false
    private var statusRemoved: Boolean = false
    
    private val componentName = ComponentName("vasyl.titles", "vasyl.titles.NotificationListener")

    var WIDTH = 400
    var MARGIN_LEFT = 280
    var DISPLAY_UI = true

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        sessionListener.let {
            mediaSessionManager.removeOnActiveSessionsChangedListener(it)
        }
        handler.removeCallbacks(runTask)
        ll?.let {
            windowManager.removeViewImmediate(it)
            ll = null
        }
        requestRebind(ComponentName(this, NotificationListenerService::class.java))
    }

    override fun onCreate() {
        super.onCreate()
        this.context = this

        settings = getSharedPreferences("savedInts", 0)
        DISPLAY_UI = settings.getBoolean("UI", true)

        statusRemoved = false
        paused = false

        overlayParam = if (isAppSystem(this)) {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, componentName)
        controllers = mediaSessionManager.getActiveSessions(componentName)
        mediaController = pickController(controllers!!)
        mediaController?.let {
            it.registerCallback(callback)
            meta = it.metadata
            ll?.let {
                windowManager.removeViewImmediate(it)
                ll = null
            }
            setStatus()
        }

        val phoneIntent = Intent(this, PhoneStateBroadcastReceiver::class.java)
        sendBroadcast(phoneIntent)

        val codeIntent = Intent(this, SecretCode::class.java)
        sendBroadcast(codeIntent)

        handler = Handler()
        handler.post(runTask)
    }    

    fun isAppSystem(context: Context): Boolean {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(context.packageName, 0)
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 || (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    override fun onDestroy() {
        sessionListener.let {
            mediaSessionManager.removeOnActiveSessionsChangedListener(it)
        }
        handler.removeCallbacks(runTask)
        ll?.let {
            windowManager.removeViewImmediate(it)
            ll = null
        }
    }

    private val runTask = object : Runnable {
        override fun run() {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (!am.isMusicActive || PhoneListener.CALLING) {
                removeWindowView()
            }
            if (am.isMusicActive && ll == null) {
                /* onActiveSessionsChanged switches between sources flawlessly as long as music continues to play,
                it doesn't switch when user had paused previous music source before playing the new one */
                checkActiveSessions()
            }
            handler.postDelayed(this, 10)
        }
    }

    fun removeWindowView() {
        if (!statusRemoved) {
            statusRemoved = true // prevents running more than once in runTask
            ll?.let {
                windowManager.removeViewImmediate(it)
                ll = null
            }
        }
    }

    private var callback: MediaController.Callback = object : MediaController.Callback() {
        override fun onSessionDestroyed() {
            statusRemoved = false
            ll?.let {
                windowManager.removeViewImmediate(it)
                ll = null
            }
            super.onSessionDestroyed()
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            // 2 - PAUSED, 3 - PLAYING
            val currentState = state?.state
            if (currentState == 2 && !paused) {
                paused = true
                statusRemoved = false
                removeWindowView()
            } else if (currentState == 3 && ll == null) {
                statusRemoved = false
                setStatus()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            if (meta == null) {
                meta = metadata
                ll?.let {
                    windowManager.removeViewImmediate(it)
                    ll = null
                }
                setStatus()
            } else if (metadata != null && meta != null) {
                // Check if the new title is different from the previous one
                if (metadata.getString(MediaMetadata.METADATA_KEY_TITLE) != meta!!.getString(MediaMetadata.METADATA_KEY_TITLE)) {
                    statusRemoved = false                       
                    meta = metadata
                    ll?.let {
                        windowManager.removeViewImmediate(it)
                        ll = null
                    }
                    setStatus()
                }
            }
        }
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun setStatus() {
        if (ll?.windowToken == null) {
            settings = getSharedPreferences("savedInts", 0)
            if (settings.getInt("margin", 0) != 0) {
                MARGIN_LEFT = settings.getInt("margin", 0)
            }

            if (settings.getInt("width", 0) != 0) {
                WIDTH = settings.getInt("width", 0)
            }
            
            try {
                // Status bar
                var statusBarHeight = 0
                val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) statusBarHeight = resources.getDimensionPixelSize(resourceId)

                val parameters = WindowManager.LayoutParams(
                    WIDTH, // width
                    statusBarHeight,
                    overlayParam,
                    WindowManager.LayoutParams.TYPE_WALLPAPER or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = MARGIN_LEFT // margin left
                }

                ll = LinearLayout(this).apply {
                    setBackgroundColor(Color.TRANSPARENT)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }

                val tv = TextView(this).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    ellipsize = TextUtils.TruncateAt.MARQUEE
                    marqueeRepeatLimit = -1
                    isSingleLine = true
                    isSelected = true
                }

                val song = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
                val artist = meta?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
                tv.text = if (artist != null) "$song - $artist" else song

                ll?.addView(tv)

                windowManager.addView(ll, parameters)
                paused = false
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    fun checkActiveSessions() {
        val ctrlrs: MutableList<MediaController> = mediaSessionManager.getActiveSessions(componentName)
        sessionListener.onActiveSessionsChanged(ctrlrs)
    }

    private fun pickController(controllers: MutableList<MediaController>?): MediaController? {
        for (mc in controllers!!) {
            if (mc.playbackState != null && mc.playbackState?.state == PlaybackState.STATE_PLAYING) {
                return mc
            }
        }
        return if (controllers.isNotEmpty()) controllers[0] else null
    }

    private val sessionListener = object: MediaSessionManager.OnActiveSessionsChangedListener {
        override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
            if (controllers!!.isNotEmpty()) {
                if (mediaController != null && controllers[0].sessionToken != mediaController?.sessionToken) {
                    // Detach current controller
                    mediaController?.unregisterCallback(callback)
                    mediaController = null
                    removeWindowView()
                }

                if (mediaController == null) {
                    // Attach new controller
                    mediaController = pickController(controllers)
                    mediaController?.registerCallback(callback)
                    callback.onMetadataChanged(mediaController?.metadata)
                    mediaController?.playbackState?.let { callback.onPlaybackStateChanged(it) }
                }
            }
        }
    }
}