package vn.viettube.app.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import vn.viettube.app.WebSetup

/** Keyboard screen: search YouTube or type a web address. */
class SearchScreen(carContext: CarContext, private val browser: CarBrowser) : Screen(carContext) {

    private val quickLinks = listOf(
        "YouTube" to WebSetup.HOME,
        "YouTube Music" to "https://music.youtube.com/",
        "Google" to "https://www.google.com/",
    )

    override fun onGetTemplate(): Template {
        val list = ItemList.Builder()
        for ((name, url) in quickLinks) {
            list.addItem(
                Row.Builder()
                    .setTitle(name)
                    .addText(url)
                    .setOnClickListener { open(url) }
                    .build()
            )
        }

        return SearchTemplate.Builder(object : SearchTemplate.SearchCallback {
            override fun onSearchSubmitted(searchText: String) {
                if (searchText.isNotBlank()) open(WebSetup.toUrl(searchText))
            }
        })
            .setHeaderAction(Action.BACK)
            .setSearchHint("Tìm YouTube hoặc nhập địa chỉ web")
            .setShowKeyboardByDefault(true)
            .setItemList(list.build())
            .build()
    }

    private fun open(url: String) {
        browser.load(url)
        screenManager.pop()
    }
}
