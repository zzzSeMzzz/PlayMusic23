package com.example.playmusic4.broadcast

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.playmusic4.MainActivity
import com.example.playmusic4.MainActivity.Companion.wv
import com.example.playmusic4.media.SongAction
import com.example.playmusic4.util.JsInterface
import com.example.playmusic4.util.MEDIA_SESSION_NAME
import com.example.playmusic4.util.MediaUtil
import com.example.playmusic4.util.NotificationUtil


class NotificationListener : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationListener"
    }

   // private val state = MusicState()

    override fun onReceive(context: Context, intent: Intent) {
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
                JsInterface.isPlaying = false
            }
            SongAction.Resume -> {
                wv.evaluateJavascript("mediaElement.play();", null)
                JsInterface.isPlaying = true
            }
            SongAction.Next -> {
                wv.evaluateJavascript("document.getElementsByClassName('simp-next fa fa-forward')[0].click();", null)
                JsInterface.isPlaying = true
            }
            SongAction.Previous -> {
                wv.evaluateJavascript("document.getElementsByClassName('simp-prev fa fa-backward')[0].click();", null)
                JsInterface.isPlaying = true
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


        wv.evaluateJavascript("(function() { return document.getElementsByClassName('simp-artist')[0].innerText; })();") {
            val result = if(it.isNotEmpty() && it.length>2) it.substring(1, it.length-1) else it
            MediaUtil.musicState.artist = result
            Log.d(TAG, "onReceive: JS $it")
        }



        wv.evaluateJavascript("(function() { return document.getElementsByClassName('simp-title')[0].innerText; })();") {
            val result = if(it.isNotEmpty() && it.length>2) it.substring(1, it.length-1) else it
            MediaUtil.musicState.title = result
        }

        wv.evaluateJavascript("(function() { return document.getElementsByClassName('simp-cover')[0].childNodes[0].style.background })();") {
            Log.d(TAG, "onReceive: JS image $it")
            try {
                val match = "url(.*?)\\)".toRegex().findAll(it)
                val foundedUrl = match.first().value
                val result = MainActivity.IMAGE_URL + foundedUrl.substring(6, foundedUrl.length - 3)

                Log.d(TAG, "mediaAction: $result")

                Glide.with(context)
                    .asBitmap()
                    .load(GlideUrl(result))
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d(TAG, "onLoadFailed")
                            return false;
                        }

                        override fun onResourceReady(
                            resource: Bitmap,
                            model: Any,
                            target: Target<Bitmap>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d(TAG, "onResourceReady: success load")
                            MediaUtil.musicState.albumArt = resource
                            return false
                        }
                    }
                    ).submit()
            } catch (ignored: Exception) {}
        }


        val mediaSession = MediaSession(context, MEDIA_SESSION_NAME)
        val mediaMetadata = MediaMetadata.Builder()
            .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
            .putText(MediaMetadata.METADATA_KEY_ARTIST, MediaUtil.musicState.artist)
            .putText(MediaMetadata.METADATA_KEY_TITLE, MediaUtil.musicState.title)
            .putBitmap(MediaMetadata.METADATA_KEY_ART, MediaUtil.musicState.albumArt)
            //.putText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title)
            .build()
        mediaSession.setMetadata(mediaMetadata)

        MediaUtil.musicState.isPlaying = JsInterface.isPlaying

        val notification = NotificationUtil.notificationMediaPlayer(
            context,
            Notification.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0,1,2)
            ,
            MediaUtil.musicState
        )

        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notification)
    }

}