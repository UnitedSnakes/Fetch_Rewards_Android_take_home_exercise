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
import androidx.navigation.fragment.findNavController
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
import org.json.JSONObject
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

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        // 在协程作用域中执行网络请求
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val jsonData = performRequest()

                // 更新 UI
                val dbHelper = context?.let { SQLiteHelper(it) } // context is certainly non-null

                val db = dbHelper?.writableDatabase
                // 假设你已经有了数据库实例 db 和格式化的 JSON 字符串 formattedJson
                if (db != null) {
                    insertJsonDataToDatabase(db, jsonData)

                    val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
                    val layoutManager = LinearLayoutManager(requireContext())
                    recyclerView.layoutManager = layoutManager

                    val adapter = RecyclerViewAdapter()
                    recyclerView.adapter = adapter

                    // 加载数据并更新适配器
                    loadDataAndUpdateAdapter(adapter)
                }
            } catch (e: Exception) {
                // 处理请求或解析失败
                e.printStackTrace()
                Log.e("InternetConnection", "Exception: ${e.message}", e)
            }
        }
    }

    @SuppressLint("Range")
    private fun loadDataAndUpdateAdapter(adapter: RecyclerViewAdapter) {
        // 在这里执行从数据库加载数据的逻辑
        // 例如，您可以从数据库查询数据，然后将数据添加到适配器中
        // 每次加载25行数据，并在适配器中更新

        val dbHelper = context?.let { SQLiteHelper(it) } // context is certainly non-null

        val db = dbHelper?.writableDatabase
        val query =
            "SELECT * FROM items WHERE name IS NOT NULL AND name != '' ORDER BY listId ASC, name ASC"
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
    }

//    @SuppressLint("Range")
//    private fun sortJsonData(db: SQLiteDatabase):  {
//        val query =
//            "SELECT * FROM items WHERE name IS NOT NULL AND name != '' ORDER BY listId ASC, name ASC"
//        val cursor = db.rawQuery(query, null)
//
//        val itemsMap = mutableMapOf<Int, MutableList<String>>()
//
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                val listId = cursor.getInt(cursor.getColumnIndex("listId"))
//                val name = cursor.getString(cursor.getColumnIndex("name"))
//
//                // Put data into the itemsMap
//                if (!itemsMap.containsKey(listId)) {
//                    itemsMap[listId] = mutableListOf()
//                }
//                itemsMap[listId]?.add(name)
//            }
//            cursor.close()
//        }
//
//        // Convert itemsMap to a JSON string
//
//        return JSONObject(itemsMap as Map<*, *>)
//    }


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
//        } catch (e: Exception) {
//            Log.e("JSONParsingError", "Error parsing JSON data: ${e.message}")
//            throw IOException("Error parsing JSON data: ${e.message}")
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
