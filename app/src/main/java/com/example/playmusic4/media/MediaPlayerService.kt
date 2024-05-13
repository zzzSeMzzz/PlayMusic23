package com.example.playmusic4.media

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.os.IBinder
import android.view.KeyEvent
import com.example.playmusic4.util.NotificationUtil


class MediaPlayerService: Service() {

    private lateinit var mediaSession: MediaSession
    private lateinit var mediaStyle: Notification.MediaStyle
    private lateinit var notificationManager: NotificationManager

    //private val binder: IBinder = MediaPlayerServiceBinder()

    private var songController: SongController? = null
    private var isForegroundService = false

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mediaSession = MediaSession(this, "MediaPlayerSessionService")
        mediaStyle = Notification.MediaStyle().setMediaSession(mediaSession.sessionToken)

        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                if (Intent.ACTION_MEDIA_BUTTON == mediaButtonIntent.action) {
                    val event = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

                    event?.let {
                        when (it.keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY -> songController?.play()
                            KeyEvent.KEYCODE_MEDIA_PAUSE -> songController?.pause()
                            KeyEvent.KEYCODE_MEDIA_NEXT -> songController?.next()
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> songController?.previous()
                            else -> {}
                        }
                    }
                }

                return true
            }
        })

        // Start foreground notification
        startForeground(1, NotificationUtil.foregroundNotification(this)).also {
            isForegroundService = true
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}