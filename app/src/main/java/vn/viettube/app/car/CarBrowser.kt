package vn.viettube.app.car

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.common.CarValue
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.Speed
import androidx.core.content.ContextCompat
import vn.viettube.app.WebSetup

/**
 * Draws a WebView onto the car screen: Android Auto gives us a Surface, we put a
 * private virtual display on it and show the browser there as a Presentation.
 * Car touches (tap / drag / pinch) are forwarded into the WebView.
 */
class CarBrowser(private val ctx: CarContext) : SurfaceCallback {

    var currentUrl: String = WebSetup.lastUrl(ctx)
        private set
    var zoom: Float = WebSetup.zoom(ctx)
        private set
    var moving = false
        private set

    private var display: VirtualDisplay? = null
    private var presentation: BrowserPresentation? = null
    private var speedListener: OnCarDataAvailableListener<Speed>? = null

    // ---- Surface from Android Auto ----

    override fun onSurfaceAvailable(container: SurfaceContainer) {
        val surface = container.surface ?: return
        if (container.width <= 0 || container.height <= 0) return
        closePresentation()
        val dm = ctx.getSystemService(DisplayManager::class.java)
        val vd = dm.createVirtualDisplay(
            "Viettube", container.width, container.height, container.dpi, surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
        )
        display = vd
        try {
            presentation = BrowserPresentation(ctx, vd.display, this).also { it.show() }
        } catch (e: Exception) {
            Log.e(TAG, "Không mở được màn hình trình duyệt", e)
        }
    }

    override fun onSurfaceDestroyed(container: SurfaceContainer) {
        closePresentation()
    }

    override fun onClick(x: Float, y: Float) {
        if (!moving) presentation?.tap(x, y)
    }

    override fun onScroll(distanceX: Float, distanceY: Float) {
        if (!moving) presentation?.drag(distanceX, distanceY)
    }

    override fun onFling(velocityX: Float, velocityY: Float) {
        presentation?.endDrag()
    }

    override fun onScale(focusX: Float, focusY: Float, scaleFactor: Float) {
        if (!moving) setZoom(zoom * scaleFactor)
    }

    private fun closePresentation() {
        presentation?.let {
            it.cleanup()
            try { it.dismiss() } catch (_: Exception) {}
        }
        presentation = null
        display?.release()
        display = null
    }

    // ---- Browser commands ----

    fun load(url: String) {
        currentUrl = url
        WebSetup.saveLastUrl(ctx, url)
        presentation?.load(url)
    }

    fun goBack() = presentation?.goBack()
    fun reload() = presentation?.reload()

    fun zoomStep(factor: Float) = setZoom(zoom * factor)

    private fun setZoom(z: Float) {
        zoom = z.coerceIn(0.5f, 3f)
        WebSetup.saveZoom(ctx, zoom)
        presentation?.applyZoom()
    }

    /** Called by the presentation when a page finishes loading. */
    fun onPageLoaded(url: String) {
        currentUrl = url
        WebSetup.saveLastUrl(ctx, url)
    }

    // ---- Safety: pause and cover the screen while the car is moving ----

    fun startSpeedWatch() {
        try {
            val hw = ctx.getCarService(CarHardwareManager::class.java)
            val listener = OnCarDataAvailableListener<Speed> { speed ->
                val v = speed.rawSpeedMetersPerSecond
                if (v.status == CarValue.STATUS_SUCCESS) {
                    val nowMoving = (v.value ?: 0f) > MOVING_SPEED
                    if (nowMoving != moving) {
                        moving = nowMoving
                        presentation?.setMoving(nowMoving)
                    }
                }
            }
            hw.carInfo.addSpeedListener(ContextCompat.getMainExecutor(ctx), listener)
            speedListener = listener
        } catch (e: Exception) {
            Log.w(TAG, "Không đọc được tốc độ xe (chưa cấp quyền?)", e)
        }
    }

    fun release() {
        speedListener?.let {
            try {
                ctx.getCarService(CarHardwareManager::class.java).carInfo.removeSpeedListener(it)
            } catch (_: Exception) {}
        }
        speedListener = null
        closePresentation()
    }

    companion object {
        private const val TAG = "Viettube"
        /** About 5 km/h. */
        private const val MOVING_SPEED = 1.4f
    }
}
