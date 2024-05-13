package com.example.playmusic4.media



//@Parcelize
data class MusicState(
    val isPlaying: Boolean = false,
    val currentDuration: Long = 0,
    var title: String = "",
    val album: String = "",
    var artist: String = "",

    // Default bitmap
    //val albumArt: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),

    val duration: Long = 0
)//: Parcelable