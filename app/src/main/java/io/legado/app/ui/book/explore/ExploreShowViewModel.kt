package io.legado.app.ui.book.explore

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import io.legado.app.App
import io.legado.app.base.BaseViewModel
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.SearchBook
import io.legado.app.model.WebBook
import kotlinx.coroutines.Dispatchers.IO

class ExploreShowViewModel(application: Application) : BaseViewModel(application) {

    val booksData = MutableLiveData<List<SearchBook>>()
    private var bookSource: BookSource? = null
    private var exploreUrl: String? = null
    private var page = 1

    fun initData(intent: Intent) {
        execute {
            val sourceUrl = intent.getStringExtra("sourceUrl")
            exploreUrl = intent.getStringExtra("exploreUrl")
            if (bookSource == null && sourceUrl != null) {
                bookSource = App.db.bookSourceDao().getBookSource(sourceUrl)
            }
            explore()
        }
    }

    fun explore() {
        val source = bookSource
        val url = exploreUrl
        if (source != null && url != null) {
            WebBook(source).exploreBook(url, page, this)
                .timeout(30000L)
                .onSuccess(IO) { searchBooks ->
                    searchBooks?.let {
                        booksData.postValue(searchBooks)
                        App.db.searchBookDao().insert(*searchBooks.toTypedArray())
                        page++
                    }
                }
        }
    }

}