package com.example.playmusic4

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
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
import com.example.playmusic4.util.isInternetAvailable
import dev.funkymuse.viewbinding.viewBinding


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val IMAGE_URL = "https://playmusic23.com/"
        private const val DEFAULT_URL = "https://playmusic23.com/"
        //private const val DEFAULT_URL = "https://playmusic23.com/playlist.php?key=VFBXVg#/"


        lateinit var wv: WebView

    }

    private val vb by viewBinding(ActivityMainBinding::inflate)

    private var currentUrl: String = DEFAULT_URL


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)


        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.let {
            it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    vb.webView.loadUrl(currentUrl)
                }

            })
        }

        wv = vb.webView

        vb.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString().toString()
                currentUrl = url
                if (!isInternetAvailable()) {
                    Log.d(TAG, "shouldOverrideUrlLoading: No internet")
                    return false
                }

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