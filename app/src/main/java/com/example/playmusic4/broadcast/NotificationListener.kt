package com.example.playmusic4.broadcast

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import android.util.Log
import com.example.playmusic4.MainActivity.Companion.wv
import com.example.playmusic4.media.MusicState
import com.example.playmusic4.media.SongAction
import com.example.playmusic4.util.JsInterface
import com.example.playmusic4.util.NotificationUtil


class NotificationListener : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationListener"
    }


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationUtil.createChannel(context)

        val action = try {
            SongAction.entries[intent.action?.toInt() ?: SongAction.Stop.ordinal]
        } catch (e: Exception) {
            SongAction.Stop
        }

        Log.d(TAG, "onReceive: action = $action")

        when(action) {
            SongAction.Pause -> {
                wv.evaluateJavascript("mediaElement.pause();", null)
                JsInterface.playing = false
            }
            SongAction.Resume -> {
                wv.evaluateJavascript("mediaElement.play();", null)
                JsInterface.playing = true
            }
            SongAction.Next -> {
                wv.evaluateJavascript("document.getElementsByClassName('simp-next fa fa-forward')[0].click();", null)
                JsInterface.playing = true
            }
            SongAction.Previous -> {
                wv.evaluateJavascript("document.getElementsByClassName('simp-prev fa fa-backward')[0].click();", null)
                JsInterface.playing = true
            }
            else -> {}
        }

       /* if (JsInterface.playing) {
            wv.evaluateJavascript("mediaElement.pause();", null)
            JsInterface.playing = false
        } else {
            wv.evaluateJavascript("mediaElement.play();", null)
            JsInterface.playing = true
        }*/

        var title = "Unknown title"
        var album = "Unknown album"

        wv.evaluateJavascript("document.getElementsByClassName('simp-artist')[0].innerText") {
            album = it
        }
        wv.evaluateJavascript("document.getElementsByClassName('simp-title')[0].innerText") {
            title = it
        }

        Log.d(TAG, "onReceive: title=$title album=$album")

        val mediaSession = MediaSession(context, "MediaPlayerSessionService")
        val mediaMetadata = MediaMetadata.Builder()
            .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
            .putText(MediaMetadata.METADATA_KEY_ALBUM, album)
            .putText(MediaMetadata.METADATA_KEY_TITLE, title)
            //.putText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title)
            .build()
        mediaSession.setMetadata(mediaMetadata)

        val state = MusicState(
            isPlaying = JsInterface.playing,
            title = title,
            album = album
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