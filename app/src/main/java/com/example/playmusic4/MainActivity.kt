package com.example.playmusic4

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import dev.funkymuse.viewbinding.viewBinding
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.playmusic4.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val DEFAULT_URL = "https://playmusic23.com"


        lateinit var wv: WebView
        lateinit var img: Bitmap
        lateinit var webTitle: String

    }

    private val vb by viewBinding(ActivityMainBinding::inflate)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)

        wv = vb.webView

        vb.webView.webViewClient = WebViewClient()
        with(vb.webView.settings) {
            javaScriptEnabled = true
        }

        val path = if (intent?.action == Intent.ACTION_VIEW) {
            val appLink: Uri? = intent.data
            Log.d(TAG, "onCreate: onNewIntent: $appLink")
            appLink.toString().replace("intent://", "https://")
        } else {
            DEFAULT_URL
        }

        vb.webView.loadUrl(path)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ${intent.data}")
        intent.data?.let {
            vb.webView.loadUrl(it.toString().replace("intent://", "https://"))
        }
    }


}