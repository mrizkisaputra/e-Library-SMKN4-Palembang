package sch.id.smkn4palembang.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityBorrowingHistoryBinding
import sch.id.smkn4palembang.databinding.GridItemAdminBorrowingHistoryBinding
import sch.id.smkn4palembang.model.BookBorrowing
import java.text.SimpleDateFormat
import java.util.Locale

class BorrowingHistoryAdapter(private val context: Context) :
    RecyclerView.Adapter<BorrowingHistoryAdapter.ViewHolder>() {

    private var listBorrowingHistory = ArrayList<BookBorrowing>()

    fun setData(list: ArrayList<BookBorrowing>) {
        this.listBorrowingHistory = list
    }

    inner class ViewHolder(val binding: GridItemAdminBorrowingHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookBorrowing) {
            binding.apply {
                itemNameTextview.text = item.name
                itemIdTextview.text = item.id
                if (item.timestamp != null)
                    itemTimestampTextview.text = DateUtils.getRelativeTimeSpanString(item.timestamp)
            }
        }

        fun bindExpandable(item: BookBorrowing) {
            val dateFormat = SimpleDateFormat("MMMM/dd/yyyy", Locale.getDefault())
            val borrowDate = item.borrowingDate?.let { dateFormat.parse(it) }
            val returnDate = item.returnDate?.let { dateFormat.parse(it) }

            val diffInMillis = borrowDate?.time?.let { returnDate?.time?.minus(it) }
            val diffInDays = diffInMillis?.div((24 * 60 * 60 * 1000)) // Konversi ke hari

            binding.apply {
                if (diffInDays != null) {
                    itemOfDayTextview.text = if (diffInDays <= -1) {
                        "Lama Meminjam : $diffInDays Hari"
                    } else {
                        "Lama Meminjam : $diffInDays Hari"
                    }
                }
                itemTitleBookTextview.text = context.getString(R.string.item_title_book, item.title)
                itemIsbnBookTextview.text = context.getString(R.string.item_isbn_number, item.isbn)
                itemConditionBookTextview.text = context.getString(R.string.item_condition, item.condition)
                itemBorrowingDateTextview.text =
                    String.format(
                        context.getString(R.string.item_borrowing_date),
                        item.borrowingDate, item.returnDate
                    )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GridItemAdminBorrowingHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listBorrowingHistory.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listBorrowingHistory[position]

        holder.bind(item)

        if (item.isExpanded) {
            holder.binding.expandLayout.visibility = View.VISIBLE
            holder.bindExpandable(item)
        } else {
            holder.binding.expandLayout.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (item.isExpanded) {
                holder.binding.expandLayout.visibility = View.GONE
                item.isExpanded = false
            } else {
                holder.binding.expandLayout.visibility = View.VISIBLE
                item.isExpanded = true
                holder.bindExpandable(item)
            }
        }
    }

}