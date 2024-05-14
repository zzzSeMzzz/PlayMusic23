package com.example.playmusic4.media

import android.graphics.Bitmap


//@Parcelize
data class MusicState(
    var isPlaying: Boolean = false,
    val currentDuration: Long = 0,
    var title: String = "",
    val album: String = "",
    var artist: String = "",

    // Default bitmap
    var albumArt: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),

    val duration: Long = 0
)//: Parcelable