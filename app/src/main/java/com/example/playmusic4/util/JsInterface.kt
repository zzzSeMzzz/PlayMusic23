package com.example.playmusic4.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import com.example.playmusic4.broadcast.NotificationListener
import com.example.playmusic4.media.MusicState
import kotlin.String


class JsInterface(private val context: Context) {
    companion object {
        var playing = false
        private const val TAG = "JsInterface"
    }


    @JavascriptInterface
    fun mediaAction(type: String?) {
        Log.e(TAG, "JS action = $type")
        playing = type?.toBoolean() ?: false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationUtil.createChannel(context)

        val pendingSwitchIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, NotificationListener::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val state = MusicState(
            isPlaying = playing,
            title = "title",
            album = "album"
        )

        val mediaSession = MediaSession(context, "MediaPlayerSessionService")
        val mediaMetadata = MediaMetadata.Builder().putLong(MediaMetadata.METADATA_KEY_DURATION, -1L).build()
        mediaSession.setMetadata(mediaMetadata)

        val notification = NotificationUtil.notificationMediaPlayer(
            context,
            Notification.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0,1,2)
            ,
            state = state
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notification)


    }

}