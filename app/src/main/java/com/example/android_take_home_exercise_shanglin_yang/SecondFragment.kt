package com.example.android_take_home_exercise_shanglin_yang

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
import com.example.android_take_home_exercise_shanglin_yang.databinding.FragmentSecondBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        // This property is only valid between onCreateView and
        // onDestroyView.
        val binding = _binding!!

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = context?.let { SQLiteHelper(it) } // context is certainly non-null
        val db = dbHelper?.writableDatabase

        var jsonData: String = "" // 初始化为一个空字符串

        // 在协程作用域中执行网络请求
        GlobalScope.launch(Dispatchers.Main) {
            try {
                jsonData = performRequest()
                if (db != null && jsonData.isNotBlank()) {
                    insertJsonDataToDatabase(db, jsonData)

                    // 移动以下按钮的点击监听器设置到这里
                    val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
                    val layoutManager = LinearLayoutManager(requireContext())
                    recyclerView.layoutManager = layoutManager

                    val adapter = RecyclerViewAdapter()
                    recyclerView.adapter = adapter


                    // 点击事件监听器
                    val pageNumberEditText: EditText? = view?.findViewById(R.id.pageNumberEditText)
                    val goToPageButton: Button? = view?.findViewById(R.id.goToPageButton)
                    val prevPageButton: Button? = view?.findViewById(R.id.prevPageButton)
                    val nextPageButton: Button? = view?.findViewById(R.id.nextPageButton)

// 向前翻页按钮点击事件处理
                    prevPageButton?.setOnClickListener {
                        if (adapter.currentPage > 0) {
                            // 向前翻页
                            adapter.loadPage(adapter.currentPage - 1)
                        } else {
                            // 处理已经在第一页的情况
                            // 可以显示错误消息或采取其他操作
                        }
                    }

// 向后翻页按钮点击事件处理
                    nextPageButton?.setOnClickListener {
                        if (adapter.currentPage < adapter.totalPages - 1) {
                            // 向后翻页
                            adapter.loadPage(adapter.currentPage + 1)
                        } else {
                            // 处理已经在最后一页的情况
                            // 可以显示错误消息或采取其他操作
                        }
                    }

// 指定页数按钮点击事件处理
                    goToPageButton?.setOnClickListener {
                        val pageNumber = pageNumberEditText?.text.toString().toIntOrNull()
                        if (pageNumber != null && pageNumber >= 1 && pageNumber <= adapter.totalPages) {
                            // 跳转到指定页数
                            adapter.loadPage(pageNumber - 1)
                        } else {
                            // 处理无效页数的情况
                            // 可以显示错误消息或采取其他操作
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
    fun loadDataAndUpdateAdapter(adapter: RecyclerViewAdapter, page: Int) {
        // 在这里执行从数据库加载数据的逻辑
        // 例如，您可以从数据库查询数据，然后将数据添加到适配器中
        // 根据页数加载数据

        val dbHelper = context?.let { SQLiteHelper(it) } // context is certainly non-null

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

        adapter.setItems(itemsList)
        fun calculateTotalRows(db: SQLiteDatabase?): Int {
            val query = "SELECT COUNT(*) FROM items"
            val cursor = db?.rawQuery(query, null)

            var rowCount = 0

            if (cursor != null && cursor.moveToFirst()) {
                rowCount = cursor.getInt(0)
                cursor.close()
            }
            return rowCount
        }


        adapter.totalRows = calculateTotalRows(db)
        adapter.totalPages = adapter.calculateTotalPages()
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
                    // 请求成功，获取响应数据
                    jsonData = response.body?.string()
                    if (!jsonData.isNullOrBlank()) {
                        // 将JSON字符串转换为JSONObject
                        return@withContext jsonData
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            retries++
            // 添加重试的等待时间，可根据需要调整
            kotlinx.coroutines.delay(1000) // 等待1秒后重试
        }

        // 如果请求失败或达到最大重试次数，返回空的JSONObject或其他适当的默认值
        return@withContext ""
    }


    private fun insertJsonDataToDatabase(database: SQLiteDatabase, jsonData: String) {
        // 开始事务
        database.beginTransaction()

        try {
            // 删除所有数据
            database.delete("items", null, null)

            // 解析JSON数据并插入到数据库中
            val jsonArray = JSONArray(jsonData)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.optLong("id")
                val listId = jsonObject.optInt("listId")
                val name = jsonObject.optString("name")

                // 过滤掉name为空的项
                if (!name.isNullOrBlank() && name != "null") {
                    // 插入数据
                    val contentValues = ContentValues()
                    contentValues.put("listId", listId)
                    contentValues.put("name", name)
                    database.insertOrThrow("items", null, contentValues)
                }
            }

            // 设置事务为成功，提交
            database.setTransactionSuccessful()
        } finally {
            // 结束事务
            database.endTransaction()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
