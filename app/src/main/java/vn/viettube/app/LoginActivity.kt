package vn.viettube.app

import android.app.Activity
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient

/** Opens YouTube on the phone so the user can sign in; the car screen shares the same login. */
class LoginActivity : Activity() {

    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Đăng nhập YouTube"
        web = WebView(this)
        WebSetup.configure(web)
        web.webViewClient = object : WebViewClient() {}
        setContentView(web)
        web.loadUrl(WebSetup.HOME)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }
}
