package com.example.playmusic4.util

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import com.example.playmusic4.MainActivity


class JsInterface(private val context: Context) {
    companion object {
        var isPlaying = false
        private const val TAG = "JsInterface"
    }

    private lateinit var mediaSession: MediaSession
    /*private val state = MusicState(
        isPlaying = isPlaying,
    )*/


    @JavascriptInterface
    fun mediaAction(type: String?) {
        Log.d(TAG, "JS mediaAction = $type")
        isPlaying = type?.toBoolean() ?: false
        Log.d(TAG, "mediaAction: isPlaying = $isPlaying")
        MediaUtil.musicState.isPlaying = isPlaying

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationUtil.createChannel(context)

        Handler(Looper.getMainLooper()).post {
            MainActivity.wv.evaluateJavascript("(function() { return document.getElementsByClassName('simp-artist')[0].innerText; })();") {
                Log.d(TAG, "onReceive: JS artist $it")
                val result = if(it.isNotEmpty() && it.length>2) it.substring(1, it.length-1) else it
                MediaUtil.musicState.artist = result
                val mediaMetadata = MediaMetadata.Builder()
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
                    .putText(MediaMetadata.METADATA_KEY_ARTIST, MediaUtil.musicState.artist)
                    .putText(MediaMetadata.METADATA_KEY_TITLE, MediaUtil.musicState.title)
                    .build()
                mediaSession.setMetadata(mediaMetadata)
            }

            MainActivity.wv.evaluateJavascript("(function() { return document.getElementsByClassName('simp-title')[0].innerText; })();") {
                Log.d(TAG, "onReceive: JS title $it")
                val result = if(it.isNotEmpty() && it.length>2) it.substring(1, it.length-1) else it
                MediaUtil.musicState.title = result
                val mediaMetadata = MediaMetadata.Builder()
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
                    .putText(MediaMetadata.METADATA_KEY_ARTIST, MediaUtil.musicState.artist)
                    .putText(MediaMetadata.METADATA_KEY_TITLE, MediaUtil.musicState.title)
                    .build()
                mediaSession.setMetadata(mediaMetadata)
            }

            mediaSession = MediaSession(context, MEDIA_SESSION_NAME)
            val mediaMetadata = MediaMetadata.Builder()
                .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
                .putText(MediaMetadata.METADATA_KEY_ARTIST, MediaUtil.musicState.artist)
                .putText(MediaMetadata.METADATA_KEY_TITLE, MediaUtil.musicState.title)
                .build()
            mediaSession.setMetadata(mediaMetadata)

            val notification = NotificationUtil.notificationMediaPlayer(
                context,
                Notification.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2),
                state = MediaUtil.musicState
            )

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(0, notification)
        }
    }

}