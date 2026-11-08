package vn.viettube.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

/**
 * VietTube Cast: independently branded phone app, projected through WebLink Host Cast.
 * It is NOT a WebLink SDK app or a standalone Android Auto video app.
 * Vehicle parking state is not available here; platform safety restrictions remain.
 */
class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var appContent: LinearLayout
    private lateinit var fullscreenLayer: FrameLayout
    private lateinit var search: EditText
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    private val dark = Color.rgb(12, 20, 34)
    private val blue = Color.rgb(29, 79, 135)
    private val prefs by lazy { getSharedPreferences("viettube_cast_favorites", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = FrameLayout(this)
        appContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(dark)
        }
        val title = TextView(this).apply {
            text = "VietTube Cast  •  Chỉ xem khi xe đã đỗ"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(8), dp(12), dp(6))
        }
        appContent.addView(title)

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun addAction(label: String, click: () -> Unit) {
            actions.addView(button(label, click),
                LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        addAction("◀") { back() }
        addAction("YouTube") { web.loadUrl(WebSetup.HOME) }
        addAction("★ Lưu") { addFavorite() }
        addAction("★ Xem") { showFavorites() }
        addAction("Cast ?") { showCastInstructions() }
        appContent.addView(actions)

        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(3), dp(4), dp(3), dp(5))
        }
        search = EditText(this).apply {
            hint = "Tìm video hoặc dán liên kết YouTube..."
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setOnEditorActionListener { _, _, _ -> submitSearch(); true }
        }
        searchRow.addView(search, LinearLayout.LayoutParams(0, dp(54), 1f))
        searchRow.addView(button("Tìm") { submitSearch() },
            LinearLayout.LayoutParams(dp(76), dp(54)))
        appContent.addView(searchRow)

        web = WebView(this)
        WebSetup.configure(web)
        web.setBackgroundColor(Color.BLACK)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request == null || !request.isForMainFrame) return false
                val url = request.url.toString()
                if (WebSetup.isYouTubeUrl(url)) return false
                // External login and other links go to the system browser.
                openExternal(url)
                return true
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
                appContent.visibility = View.GONE
                fullscreenLayer.visibility = View.VISIBLE
                fullscreenLayer.addView(view, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
                fullscreenLayer.addView(button("Đóng toàn màn hình") { exitFullscreen(true) },
                    FrameLayout.LayoutParams(dp(165), dp(54), Gravity.TOP or Gravity.END))
            }
            override fun onHideCustomView() { exitFullscreen(false) }
        }
        appContent.addView(web, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        container.addView(appContent, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        fullscreenLayer = FrameLayout(this).apply {
            visibility = View.GONE
            setBackgroundColor(Color.BLACK)
        }
        container.addView(fullscreenLayer, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        setContentView(container)

        if (savedInstanceState != null) {
            if (web.restoreState(savedInstanceState) == null) web.loadUrl(WebSetup.HOME)
        } else {
            web.loadUrl(WebSetup.lastUrl(this))
        }
    }

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun button(label: String, click: () -> Unit): Button = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 13f
        setTextColor(Color.WHITE)
        setBackgroundColor(blue)
        setOnClickListener { click() }
    }

    private fun submitSearch() {
        val input = search.text.toString()
        if (input.isNotBlank()) {
            web.loadUrl(WebSetup.toUrl(input))
            search.text.clear()
            search.clearFocus()
        }
    }

    private fun favoriteList(): JSONArray =
        runCatching { JSONArray(prefs.getString("items", "[]")) }
            .getOrElse { JSONArray() }

    private fun addFavorite() {
        val url = web.url ?: return
        if (!WebSetup.isYouTubeUrl(url)) return
        val name = (web.title ?: "YouTube").take(80)
        val old = favoriteList()
        val next = JSONArray()
        next.put(JSONObject().put("title", name).put("url", url))
        for (i in 0 until old.length()) {
            val item = old.optJSONObject(i) ?: continue
            if (item.optString("url") != url && next.length() < 12) next.put(item)
        }
        prefs.edit().putString("items", next.toString()).apply()
        AlertDialog.Builder(this).setMessage("Đã lưu: $name").setPositiveButton("OK", null).show()
    }

    private fun showFavorites() {
        val items = favoriteList()
        if (items.length() == 0) {
            AlertDialog.Builder(this).setMessage("Chưa có video yêu thích.")
                .setPositiveButton("OK", null).show()
            return
        }
        val labels = Array(items.length()) { i ->
            items.optJSONObject(i)?.optString("title") ?: "Video"
        }
        AlertDialog.Builder(this).setTitle("Video đã lưu")
            .setItems(labels) { _, i ->
                val url = items.optJSONObject(i)?.optString("url").orEmpty()
                if (WebSetup.isYouTubeUrl(url)) web.loadUrl(url)
            }
            .setNegativeButton("Đóng", null).show()
    }

    private fun showCastInstructions() {
        AlertDialog.Builder(this).setTitle("Chiếu VietTube qua WebLink Cast")
            .setMessage(
                "1. Chỉ sử dụng khi xe đã đỗ.\n" +
                "2. Kết nối Samsung với WebLink Host trên màn hình Mitsubishi.\n" +
                "3. Nhấn ô Cast màu xanh lá trong WebLink và cấp quyền chia sẻ màn hình.\n" +
                "4. Quay về VietTube Cast trên điện thoại.\n\n" +
                "Ứng dụng không tự tạo biểu tượng trên WebLink và không vượt giới hạn an toàn của xe."
            ).setPositiveButton("Đã hiểu", null).show()
    }

    private fun openExternal(url: String) {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return
        if (uri.scheme != "https" && uri.scheme != "http") return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) { /* No capable browser */ }
    }

    private fun back() {
        if (fullscreenView != null) exitFullscreen(true)
        else if (web.canGoBack()) web.goBack()
        else finish()
    }

    private fun exitFullscreen(notifyWebView: Boolean) {
        if (fullscreenView == null) return
        fullscreenLayer.removeAllViews()
        fullscreenLayer.visibility = View.GONE
        appContent.visibility = View.VISIBLE
        fullscreenView = null
        val callback = fullscreenCallback
        fullscreenCallback = null
        if (notifyWebView) callback?.onCustomViewHidden()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { back() }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (::web.isInitialized) web.onResume()
    }

    override fun onPause() {
        if (::web.isInitialized) {
            // Never continue video when VietTube leaves the foreground.
            web.evaluateJavascript(
                "document.querySelectorAll('video,audio').forEach(function(m){m.pause()})",
                null
            )
            web.onPause()
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::web.isInitialized) web.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (::fullscreenLayer.isInitialized) exitFullscreen(true)
        if (::web.isInitialized) {
            web.stopLoading()
            (web.parent as? ViewGroup)?.removeView(web)
            web.destroy()
        }
        super.onDestroy()
    }
}
