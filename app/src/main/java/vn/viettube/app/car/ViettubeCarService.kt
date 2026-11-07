package vn.viettube.app.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/** Entry point Android Auto connects to. */
class ViettubeCarService : CarAppService() {
    // Sideloaded personal app: accept any Android Auto host.
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    @Suppress("DEPRECATION")
    override fun onCreateSession(): Session = ViettubeSession()
}

class ViettubeSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = BrowserScreen(carContext)
}
