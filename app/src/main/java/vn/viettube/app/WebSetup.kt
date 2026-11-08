package vn.viettube.app

import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebView
import java.net.URLEncoder

/**
 * YouTube website navigation for the phone-side WebLink Cast prototype.
 * No video downloading, ad suppression, identity spoofing, or stream extraction.
 */
object WebSetup {
    const val HOME = "https://m.youtube.com/"
    private const val PREFS = "viettube"
    private const val KEY_LAST_URL = "last_url"

    fun configure(web: WebView) {
        val s = web.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.mediaPlaybackRequiresUserGesture = true
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.setSupportZoom(true)
        s.builtInZoomControls = true
        s.displayZoomControls = false
        // Preserve Android WebView's real user-agent and media integrity identity.
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
    }

    fun isYouTubeUrl(url: String): Boolean {
        val parsed = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        if (parsed.scheme?.lowercase() != "https") return false
        val host = parsed.host?.lowercase() ?: return false
        return host == "youtu.be" || host == "youtube.com" ||
            host.endsWith(".youtube.com")
    }

    fun toUrl(input: String): String {
        val term = input.trim()
        if (term.isEmpty()) return HOME
        val candidate = if (term.startsWith("https://", ignoreCase = true)) term
            else if (term.startsWith("youtube.com/") ||
                term.startsWith("www.youtube.com/") ||
                term.startsWith("m.youtube.com/") ||
                term.startsWith("youtu.be/")) "https://$term"
            else null
        if (candidate != null && isYouTubeUrl(candidate)) return candidate
        return "https://m.youtube.com/results?search_query=" +
            URLEncoder.encode(term, "UTF-8")
    }

    fun lastUrl(ctx: Context): String {
        val url = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_URL, HOME) ?: HOME
        return if (isYouTubeUrl(url)) url else HOME
    }

    fun saveLastUrl(ctx: Context, url: String) {
        if (isYouTubeUrl(url)) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_LAST_URL, url).apply()
        }
    }
}
