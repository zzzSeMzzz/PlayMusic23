package com.example.playmusic4.util

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import com.example.playmusic4.media.MusicState


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


        val state = MusicState(
            isPlaying = playing,
            title = "title",
            album = "album",
            artist = "artist"
        )

        val mediaSession = MediaSession(context, "MediaPlayerSessionService")
        val mediaMetadata = MediaMetadata.Builder()
            .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
            .putText(MediaMetadata.METADATA_KEY_ARTIST, state.artist)
            .putText(MediaMetadata.METADATA_KEY_TITLE, state.title)
            .build()
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