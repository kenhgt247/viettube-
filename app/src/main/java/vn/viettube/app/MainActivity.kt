package vn.viettube.app

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Phone screen: setup steps, YouTube sign-in, and the car-speed permission. */
class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        col.addView(TextView(this).apply {
            text = "Viettube"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_HORIZONTAL
        })

        col.addView(TextView(this).apply {
            textSize = 16f
            setPadding(0, pad, 0, pad)
            text = """
                Xem YouTube và lướt web trên màn hình xe qua Android Auto (chỉ khi xe đang đỗ).

                Cài đặt một lần:
                1. Cấp quyền đọc tốc độ xe (nút bên dưới) để ứng dụng tự dừng video khi xe chạy.
                2. Mở ứng dụng Android Auto trên điện thoại → chạm 10 lần vào "Phiên bản" để bật chế độ nhà phát triển.
                3. Bấm ⋮ góc trên → Cài đặt nhà phát triển → bật "Unknown sources".
                4. Cắm điện thoại vào xe → Viettube sẽ hiện trong danh sách ứng dụng.

                Trên xe:
                • Chạm để bấm, vuốt để cuộn.
                • ◀ quay lại · 🏠 về YouTube · 🔍 tìm kiếm / nhập địa chỉ web · ⟳ tải lại
                • ＋ / － phóng to, thu nhỏ trang.
            """.trimIndent()
        })

        col.addView(Button(this).apply {
            text = "Đăng nhập tài khoản YouTube"
            setOnClickListener { startActivity(Intent(this@MainActivity, LoginActivity::class.java)) }
        })

        col.addView(Button(this).apply {
            text = "Cấp quyền đọc tốc độ xe"
            setOnClickListener { requestPermissions(arrayOf(SPEED_PERMISSION), 1) }
        })

        status = TextView(this).apply {
            textSize = 15f
            setPadding(0, pad, 0, 0)
        }
        col.addView(status)

        setContentView(ScrollView(this).apply { addView(col) })
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        updateStatus()
    }

    private fun updateStatus() {
        val ok = checkSelfPermission(SPEED_PERMISSION) == PackageManager.PERMISSION_GRANTED
        status.text = if (ok) "✅ Đã cấp quyền tốc độ xe — video sẽ tự dừng khi xe chạy."
        else "⚠️ Chưa cấp quyền tốc độ xe."
    }

    companion object {
        const val SPEED_PERMISSION = "com.google.android.gms.permission.CAR_SPEED"
    }
}
