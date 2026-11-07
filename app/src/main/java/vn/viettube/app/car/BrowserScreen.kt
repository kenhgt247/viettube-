package vn.viettube.app.car

import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import vn.viettube.app.R
import vn.viettube.app.WebSetup

/** Main car screen: the web page fills the display, buttons float on top. */
class BrowserScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private val browser = CarBrowser(carContext)

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(browser)
        browser.startSpeedWatch()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        browser.release()
    }

    private fun iconAction(icon: Int, onClick: () -> Unit): Action =
        Action.Builder()
            .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, icon)).build())
            .setOnClickListener { onClick() }
            .build()

    @Suppress("DEPRECATION")
    override fun onGetTemplate(): Template {
        val strip = ActionStrip.Builder()
            .addAction(iconAction(R.drawable.ic_back) { browser.goBack() })
            .addAction(iconAction(R.drawable.ic_home) { browser.load(WebSetup.HOME) })
            .addAction(iconAction(R.drawable.ic_search) {
                screenManager.push(SearchScreen(carContext, browser))
            })
            .addAction(iconAction(R.drawable.ic_refresh) { browser.reload() })
            .build()

        val mapStrip = ActionStrip.Builder()
            .addAction(Action.PAN)
            .addAction(iconAction(R.drawable.ic_plus) { browser.zoomStep(1.15f) })
            .addAction(iconAction(R.drawable.ic_minus) { browser.zoomStep(1f / 1.15f) })
            .build()

        return NavigationTemplate.Builder()
            .setActionStrip(strip)
            .setMapActionStrip(mapStrip)
            .setPanModeListener { }
            .build()
    }
}
