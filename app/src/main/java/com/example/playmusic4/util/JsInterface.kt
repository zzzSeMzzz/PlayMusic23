package com.example.playmusic4.util

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.playmusic4.MainActivity
import com.example.playmusic4.MainActivity.Companion.IMAGE_URL


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
                    .putBitmap(MediaMetadata.METADATA_KEY_ART, MediaUtil.musicState.albumArt)
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
                    .putBitmap(MediaMetadata.METADATA_KEY_ART, MediaUtil.musicState.albumArt)
                    .build()
                mediaSession.setMetadata(mediaMetadata)
            }

            MainActivity.wv.evaluateJavascript("(function() { return document.getElementsByClassName('simp-cover')[0].childNodes[0].style.background })();") {
                Log.d(TAG, "onReceive: JS image $it")
                try {
                    val match = "url(.*?)\\)".toRegex().findAll(it)
                    val foundedUrl = match.first().value
                    val result = IMAGE_URL + foundedUrl.substring(6, foundedUrl.length - 3)

                    Log.d(TAG, "mediaAction: $result")

                    Glide.with(context)
                        .asBitmap()
                        .load(GlideUrl(result))
                        //.load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTzrKkSUWhClLxv4B3t-fblyxNmw7nI4_DZj9foxbHJ8A&s")
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
                                val mediaMetadata = MediaMetadata.Builder()
                                    .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
                                    .putText(MediaMetadata.METADATA_KEY_ARTIST, MediaUtil.musicState.artist)
                                    .putText(MediaMetadata.METADATA_KEY_TITLE, MediaUtil.musicState.title)
                                    .putBitmap(MediaMetadata.METADATA_KEY_ART, MediaUtil.musicState.albumArt)
                                    .build()
                                mediaSession.setMetadata(mediaMetadata)
                                return false
                            }
                        }
                        ).submit()
                } catch (ignored: Exception) {}
            }

            mediaSession = MediaSession(context, MEDIA_SESSION_NAME)
            val mediaMetadata = MediaMetadata.Builder()
                .putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
                .putText(MediaMetadata.METADATA_KEY_ARTIST, MediaUtil.musicState.artist)
                .putText(MediaMetadata.METADATA_KEY_TITLE, MediaUtil.musicState.title)
                .putBitmap(MediaMetadata.METADATA_KEY_ART, MediaUtil.musicState.albumArt)
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