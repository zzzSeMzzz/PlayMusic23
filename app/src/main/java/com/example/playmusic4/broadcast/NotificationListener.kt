package com.example.playmusic4.broadcast

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import android.util.Log
import com.example.playmusic4.MainActivity.Companion.wv
import com.example.playmusic4.media.MusicState
import com.example.playmusic4.util.JsInterface
import com.example.playmusic4.util.NotificationUtil


class NotificationListener : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationListener"
    }


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationUtil.createChannel(context)


        val pendingSwitchIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, NotificationListener::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (JsInterface.playing) {
            wv.evaluateJavascript("mediaElement.pause();", null)
            JsInterface.playing = false
        } else {
            wv.evaluateJavascript("mediaElement.play();", null)
            JsInterface.playing = true
        }

        val mediaSession = MediaSession(context, "MediaPlayerSessionService")
        val mediaMetadata = MediaMetadata.Builder().putLong(MediaMetadata.METADATA_KEY_DURATION, -1L).build()
        mediaSession.setMetadata(mediaMetadata)

        val state = MusicState(
            isPlaying = JsInterface.playing,
            title = "title",
            album = "album"
        )

        val notification = NotificationUtil.notificationMediaPlayer(
            context,
            Notification.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0,1,2)
            ,
            state
        )

        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notification)
    }

}