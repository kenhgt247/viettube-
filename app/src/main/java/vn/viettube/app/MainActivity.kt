package vn.viettube.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Phone and future eligible parked-host browser.
 * CAR_LAUNCHER does not bypass Android Auto's unsupported video category.
 */
class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var normalContent: LinearLayout
    private lateinit var fullscreenLayer: FrameLayout
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        normalContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        toolbar.addView(button("◀") { goBackInBrowser() }, weightedButton())
        toolbar.addView(button("YouTube") { web.loadUrl(WebSetup.HOME) }, weightedButton())
        toolbar.addView(button("↻") { web.reload() }, weightedButton())
        toolbar.addView(button("Mở YT") { openInYouTubeApp() }, weightedButton())
        normalContent.addView(toolbar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        web = WebView(this)
        WebSetup.configure(web)
        web.settings.mediaPlaybackRequiresUserGesture = true
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val scheme = request?.url?.scheme?.lowercase() ?: return false
                return scheme != "https" && scheme != "http"
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null) WebSetup.saveLastUrl(this@MainActivity, url)
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view == null || fullscreenView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                fullscreenView = view
                fullscreenCallback = callback
                normalContent.visibility = View.GONE
                fullscreenLayer.visibility = View.VISIBLE
                fullscreenLayer.addView(view, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
            }
            override fun onHideCustomView() {
                exitFullscreen(notifyWebView = false)
            }
        }
        normalContent.addView(web, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(normalContent, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        fullscreenLayer = FrameLayout(this).apply { visibility = View.GONE }
        root.addView(fullscreenLayer, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        setContentView(root)
        if (savedInstanceState != null) {
            if (web.restoreState(savedInstanceState) == null) web.loadUrl(WebSetup.HOME)
        } else {
            web.loadUrl(WebSetup.lastUrl(this))
        }
    }

    private fun weightedButton() =
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

    private fun button(label: String, click: () -> Unit) = Button(this).apply {
        text = label
        textSize = 12f
        isAllCaps = false
        setOnClickListener { click() }
    }

    private fun goBackInBrowser() {
        if (fullscreenView != null) exitFullscreen(notifyWebView = true)
        else if (web.canGoBack()) web.goBack()
        else finish()
    }

    private fun openInYouTubeApp() {
        val url = web.url ?: WebSetup.HOME
        val launch = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.google.android.youtube")
        }
        try {
            startActivity(launch)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun exitFullscreen(notifyWebView: Boolean) {
        if (fullscreenView == null) return
        fullscreenLayer.removeAllViews()
        fullscreenLayer.visibility = View.GONE
        normalContent.visibility = View.VISIBLE
        fullscreenView = null
        val callback = fullscreenCallback
        fullscreenCallback = null
        if (notifyWebView) callback?.onCustomViewHidden()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = goBackInBrowser()

    override fun onPause() {
        if (::web.isInitialized) {
            web.evaluateJavascript(
                "document.querySelectorAll('video,audio').forEach(function(m){m.pause()})",
                null
            )
            web.onPause()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) web.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::web.isInitialized) web.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (::fullscreenLayer.isInitialized) exitFullscreen(notifyWebView = true)
        if (::web.isInitialized) {
            web.stopLoading()
            (web.parent as? ViewGroup)?.removeView(web)
            web.destroy()
        }
        super.onDestroy()
    }
}
