package vn.viettube.app

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import java.net.URLEncoder

object WebSetup {
    const val HOME = "https://m.youtube.com/"
    private const val PREFS = "viettube"
    private const val KEY_LAST_URL = "last_url"
    private const val KEY_ZOOM = "zoom"

    /** Shared WebView settings for the phone screen and the car screen. */
    fun configure(web: WebView) {
        val s = web.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.mediaPlaybackRequiresUserGesture = false
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.setSupportZoom(true)
        s.builtInZoomControls = true
        s.displayZoomControls = false
        // Look like normal Chrome so sites (and Google sign-in) behave normally.
        s.userAgentString = s.userAgentString
            .replace("; wv", "")
            .replace(Regex("Version/\\d+\\.\\d+ "), "")
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
    }

    /** Turns what the user typed into a URL: a web address, or a YouTube search. */
    fun toUrl(input: String): String {
        val t = input.trim()
        if (t.startsWith("http://") || t.startsWith("https://")) return t
        if (!t.contains(' ') && Regex("^[\\w-]+(\\.[\\w-]+)+(/.*)?$").matches(t)) return "https://$t"
        return "https://m.youtube.com/results?search_query=" + URLEncoder.encode(t, "UTF-8")
    }

    fun lastUrl(ctx: Context): String =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LAST_URL, HOME) ?: HOME

    fun saveLastUrl(ctx: Context, url: String) {
        if (!url.startsWith("http")) return
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LAST_URL, url).apply()
    }

    fun zoom(ctx: Context): Float =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(KEY_ZOOM, 1f)

    fun saveZoom(ctx: Context, z: Float) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat(KEY_ZOOM, z).apply()
    }
}
