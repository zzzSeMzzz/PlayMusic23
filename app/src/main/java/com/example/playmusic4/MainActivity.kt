package com.example.playmusic4

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.playmusic4.databinding.ActivityMainBinding
import com.example.playmusic4.util.JsInterface
import dev.funkymuse.viewbinding.viewBinding


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val DEFAULT_URL = "https://playmusic23.com/"


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

        vb.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString().toString()
                val urlU = request?.url

                if (url.contains("t.me") || url.contains("tg://") ||
                    url.contains("vk.com") || url.contains("www.youtube.com")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            this@MainActivity,
                            "Такого приложения нет на вашем телефоне",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    view?.loadUrl(url)
                }
                return true
            }
        }
        vb.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        vb.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        vb.webView.addJavascriptInterface(JsInterface(this), "JSOUT")
        with(vb.webView.settings) {
            javaScriptEnabled = true
            loadsImagesAutomatically = true
            domStorageEnabled = true
        }


        vb.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    val code = "var mediaElement;" +
                            "mediaCheck();" +
                            "document.onclick = function(){" +
                            "    mediaCheck();" +
                            "};" +
                            "function mediaCheck(){" +
                            "    for(var i = 0; i < document.getElementsByTagName('video').length; i++){" +
                            "        var media = document.getElementsByTagName('video')[i];" +
                            "        media.onplay = function(){" +
                            "            mediaElement = media;" +
                            "            JSOUT.mediaAction('true');" +
                            "        };" +
                            "        media.onpause = function(){" +
                            "            mediaElement = media;" +
                            "            JSOUT.mediaAction('false');" +
                            "        };" +
                            "    }" +
                            "    for(var i = 0; i < document.getElementsByTagName('audio').length; i++){" +
                            "        var media = document.getElementsByTagName('audio')[i];" +
                            "        media.onplay = function(){" +
                            "            mediaElement = media;" +
                            "            JSOUT.mediaAction('true');" +
                            "        };" +
                            "        media.onpause = function(){" +
                            "            mediaElement = media;" +
                            "            JSOUT.mediaAction('false');" +
                            "        };" +
                            "    }" +
                            "}"
                    vb.webView.evaluateJavascript(code, null)
                }
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                super.onReceivedIcon(view, icon)
                img = icon!!
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)

                webTitle = title!!
            }
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