package vn.viettube.app.car

import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Display
import android.view.Gravity
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import vn.viettube.app.WebSetup

/** The browser window that lives on the car's (virtual) display. */
class BrowserPresentation(
    outer: Context,
    display: Display,
    private val host: CarBrowser,
) : Presentation(outer, display) {

    private lateinit var root: FrameLayout
    private lateinit var web: WebView
    private lateinit var overlay: TextView
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null
    private var ready = false

    private val handler = Handler(Looper.getMainLooper())
    private var dragging = false
    private var dragDownTime = 0L
    private var dragX = 0f
    private var dragY = 0f
    private val endDragRunnable = Runnable { endDrag() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val match = ViewGroup.LayoutParams.MATCH_PARENT

        root = FrameLayout(context).apply { setBackgroundColor(Color.BLACK) }

        web = WebView(context).apply { setBackgroundColor(Color.BLACK) }
        WebSetup.configure(web)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val scheme = request.url.scheme ?: return true
                // Keep web pages inside; ignore app links like intent:// or vnd.youtube://
                return scheme != "http" && scheme != "https"
            }

            override fun onPageFinished(view: WebView, url: String) {
                host.onPageLoaded(url)
                applyZoom()
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                fullscreenView?.let { root.removeView(it) }
                fullscreenView = view
                fullscreenCallback = callback
                root.addView(view, 1, FrameLayout.LayoutParams(match, match))
                web.visibility = View.GONE
            }

            override fun onHideCustomView() {
                fullscreenView?.let { root.removeView(it) }
                fullscreenView = null
                fullscreenCallback = null
                web.visibility = View.VISIBLE
            }
        }
        root.addView(web, FrameLayout.LayoutParams(match, match))

        overlay = TextView(context).apply {
            text = "🚗 Xe đang chạy\nDừng xe để tiếp tục xem"
            gravity = Gravity.CENTER
            textSize = 26f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(240, 0, 0, 0))
            visibility = View.GONE
        }
        root.addView(overlay, FrameLayout.LayoutParams(match, match))

        setContentView(root)
        ready = true
        web.loadUrl(host.currentUrl)
        setMoving(host.moving)
    }

    // ---- Commands ----

    fun load(url: String) {
        if (ready) web.loadUrl(url)
    }

    fun goBack() {
        if (!ready) return
        when {
            fullscreenView != null -> fullscreenCallback?.onCustomViewHidden()
            web.canGoBack() -> web.goBack()
        }
    }

    fun reload() {
        if (ready) web.reload()
    }

    fun applyZoom() {
        if (ready) web.evaluateJavascript("document.documentElement.style.zoom='${host.zoom}'", null)
    }

    fun setMoving(moving: Boolean) {
        if (!ready) return
        if (moving) {
            endDrag()
            fullscreenCallback?.onCustomViewHidden()
            web.evaluateJavascript(
                "document.querySelectorAll('video,audio').forEach(function(m){m.pause()})", null
            )
            overlay.visibility = View.VISIBLE
        } else {
            overlay.visibility = View.GONE
        }
    }

    // ---- Touch forwarding ----

    private fun send(action: Int, downTime: Long, x: Float, y: Float) {
        val ev = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
        ev.source = InputDevice.SOURCE_TOUCHSCREEN
        root.dispatchTouchEvent(ev)
        ev.recycle()
    }

    fun tap(x: Float, y: Float) {
        if (!ready) return
        endDrag()
        val down = SystemClock.uptimeMillis()
        send(MotionEvent.ACTION_DOWN, down, x, y)
        handler.postDelayed({ send(MotionEvent.ACTION_UP, down, x, y) }, 40)
    }

    /** Android Auto only reports how far the finger moved, so we replay it as a drag from the centre. */
    fun drag(dx: Float, dy: Float) {
        if (!ready) return
        val w = root.width.toFloat()
        val h = root.height.toFloat()
        if (!dragging) {
            dragging = true
            dragX = w / 2f
            dragY = h / 2f
            dragDownTime = SystemClock.uptimeMillis()
            send(MotionEvent.ACTION_DOWN, dragDownTime, dragX, dragY)
        }
        dragX -= dx
        dragY -= dy
        val outside = dragX < 1f || dragX > w - 1f || dragY < 1f || dragY > h - 1f
        dragX = dragX.coerceIn(1f, w - 1f)
        dragY = dragY.coerceIn(1f, h - 1f)
        send(MotionEvent.ACTION_MOVE, dragDownTime, dragX, dragY)
        handler.removeCallbacks(endDragRunnable)
        if (outside) endDrag() else handler.postDelayed(endDragRunnable, 150)
    }

    fun endDrag() {
        handler.removeCallbacks(endDragRunnable)
        if (!dragging) return
        dragging = false
        send(MotionEvent.ACTION_UP, dragDownTime, dragX, dragY)
    }

    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        if (!ready) return
        ready = false
        try {
            web.stopLoading()
            root.removeAllViews()
            web.destroy()
        } catch (_: Exception) {}
    }
}
