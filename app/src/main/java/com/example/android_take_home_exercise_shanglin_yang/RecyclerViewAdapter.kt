import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_take_home_exercise_shanglin_yang.Item
import com.example.android_take_home_exercise_shanglin_yang.R
import java.util.Collections.min
import kotlin.math.min

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private var items: MutableList<Item> = mutableListOf()
    var currentPage = 0
    val itemsPerPage = 25
    var totalRows = 0

    fun setItemsAndTotalRows(newItems: MutableList<Item>, totalRows: Int) {
        items.clear()
        items.addAll(newItems)
        this.totalRows = totalRows
        notifyDataSetChanged()
    }

    fun loadPage(page: Int) {
        if (page in 0 until totalPages()) {
            currentPage = page
            val startIndex = currentPage * itemsPerPage
            val endIndex = startIndex + itemsPerPage
            if (startIndex < totalRows) {
                val newItems = items.subList(startIndex, min(endIndex, totalRows))
                clearItems()
                addItems(newItems)
            }
        }
    }

    private fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    private fun addItems(newItems: List<Item>) {
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun totalPages(): Int {
        var totalPages = totalRows / itemsPerPage
        val remainingRows = totalRows % itemsPerPage
        if (remainingRows > 0) {
            totalPages += 1
        }
        return totalPages
    }

    // 其他方法不需要修改，保持不变

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 在这里绑定视图元素和数据
        fun bind(item: Item) {
            val listIdTextView: TextView = itemView.findViewById(R.id.listIdTextView)
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

            listIdTextView.text = item.listId.toString()
            nameTextView.text = item.name
        }
    }

    fun loadNextPage() {
        if (currentPage < totalPages() - 1) {
            currentPage++
            loadPage(currentPage)
        }
    }

    fun loadPreviousPage() {
        if (currentPage > 0) {
            currentPage--
            loadPage(currentPage)
        }
    }
}
