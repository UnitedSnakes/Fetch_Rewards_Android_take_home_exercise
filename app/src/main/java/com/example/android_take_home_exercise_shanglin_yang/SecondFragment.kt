import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_take_home_exercise_shanglin_yang.Item
import com.example.android_take_home_exercise_shanglin_yang.R
import com.example.android_take_home_exercise_shanglin_yang.SQLiteHelper
import com.example.android_take_home_exercise_shanglin_yang.databinding.FragmentSecondBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        val binding = _binding!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = context?.let { SQLiteHelper(it) }
        val db = dbHelper?.writableDatabase

        var jsonData: String = ""

        GlobalScope.launch(Dispatchers.Main) {
            try {
                jsonData = performRequest()
                if (db != null && jsonData.isNotBlank()) {
                    insertJsonDataToDatabase(db, jsonData)

                    val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
                    val adapter = RecyclerViewAdapter()
                    recyclerView.adapter = adapter

                    val pageNumberEditText: EditText? = view?.findViewById(R.id.pageNumberEditText)
                    val goToPageButton: Button? = view?.findViewById(R.id.goToPageButton)
                    val prevPageButton: Button? = view?.findViewById(R.id.prevPageButton)
                    val nextPageButton: Button? = view?.findViewById(R.id.nextPageButton)

                    prevPageButton?.setOnClickListener {
                        if (adapter.currentPage > 0) {
                            adapter.loadPage(adapter.currentPage - 1)
                        } else {
                            // 处理已经在第一页的情况
                        }
                    }

                    nextPageButton?.setOnClickListener {
                        if (adapter.currentPage < adapter.totalPages() - 1) {
                            adapter.loadPage(adapter.currentPage + 1)
                        } else {
                            // 处理已经在最后一页的情况
                        }
                    }

                    goToPageButton?.setOnClickListener {
                        val pageNumber = pageNumberEditText?.text.toString().toIntOrNull()
                        if (pageNumber != null && pageNumber >= 1 && pageNumber <= adapter.totalPages()) {
                            adapter.loadPage(pageNumber - 1)
                        } else {
                            // 处理无效页数的情况
                        }
                    }

                    // 初始加载第一页数据
                    loadDataAndUpdateAdapter(adapter, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("InternetConnection", "Exception: ${e.message}", e)
            }
        }
    }

    @SuppressLint("Range")
    private fun loadDataAndUpdateAdapter(adapter: RecyclerViewAdapter, page: Int) {
        val dbHelper = context?.let { SQLiteHelper(it) }
        val db = dbHelper?.writableDatabase
        val startIndex = page * adapter.itemsPerPage
        val endIndex = startIndex + adapter.itemsPerPage

        val query = "SELECT * FROM items WHERE name IS NOT NULL AND name != '' ORDER BY listId ASC, name ASC LIMIT $startIndex, ${adapter.itemsPerPage}"
        val cursor = db?.rawQuery(query, null)

        val itemsList = mutableListOf<Item>()

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val listId = cursor.getInt(cursor.getColumnIndex("listId"))
                val name = cursor.getString(cursor.getColumnIndex("name"))
                itemsList.add(Item(listId, name))
            }
            cursor.close()
        }

        adapter.setItemsAndTotalRows(itemsList, calculateTotalRows(db))
    }

    private fun calculateTotalRows(db: SQLiteDatabase?): Int {
        val query = "SELECT COUNT(*) FROM items"
        val cursor = db?.rawQuery(query, null)

        var rowCount = 0

        if (cursor != null && cursor.moveToFirst()) {
            rowCount = cursor.getInt(0)
            cursor.close()
        }
        return rowCount
    }

    private suspend fun performRequest(): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://fetch-hiring.s3.amazonaws.com/hiring.json")
            .build()

        val maxRetries = 5
        var retries = 0
        var jsonData: String? = null

        while (retries < maxRetries) {
            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    jsonData = response.body?.string()
                    if (!jsonData.isNullOrBlank()) {
                        return@withContext jsonData
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            retries++
            kotlinx.coroutines.delay(1000)
        }

        return@withContext ""
    }

    private fun insertJsonDataToDatabase(database: SQLiteDatabase, jsonData: String) {
        database.beginTransaction()

        try {
            database.delete("items", null, null)

            val jsonArray = JSONArray(jsonData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.optLong("id")
                val listId = jsonObject.optInt("listId")
                val name = jsonObject.optString("name")

                if (!name.isNullOrBlank() && name != "null") {
                    val contentValues = ContentValues()
                    contentValues.put("listId", listId)
                    contentValues.put("name", name)
                    database.insertOrThrow("items", null, contentValues)
                }
            }

            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
