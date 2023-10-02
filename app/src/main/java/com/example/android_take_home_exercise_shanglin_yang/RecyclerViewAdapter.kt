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

    // Set new data and total rows
    fun setItemsAndTotalRows(newItems: MutableList<Item>, totalRows: Int) {
        items.clear()
        items.addAll(newItems)
        this.totalRows = totalRows
        notifyDataSetChanged()
    }

    // Load data for a specific page
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

    // Calculate the total number of pages
    fun totalPages(): Int {
        var totalPages = totalRows / itemsPerPage
        val remainingRows = totalRows % itemsPerPage
        if (remainingRows > 0) {
            totalPages += 1
        }
        return totalPages
    }

    // Create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row_layout, parent, false)
        return ViewHolder(view)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    // Return the number of items in the list
    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind view elements and data here
        fun bind(item: Item) {
            val listIdTextView: TextView = itemView.findViewById(R.id.listIdTextView)
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

            listIdTextView.text = item.listId.toString()
            nameTextView.text = item.name
        }
    }

    // Load the next page of data
    fun loadNextPage() {
        if (currentPage < totalPages() - 1) { // Check if it's already the last page
            currentPage++
            loadPage(currentPage)
        }
    }

    // Load the previous page of data
    fun loadPreviousPage() {
        if (currentPage > 0) { // Check if it's already the first page
            currentPage--
            loadPage(currentPage)
        }
    }
}
