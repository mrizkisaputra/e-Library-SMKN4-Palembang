package sch.id.smkn4palembang.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ListItemAdminManagementBookBorrowingBinding
import sch.id.smkn4palembang.model.BookBorrowing
import java.text.SimpleDateFormat
import java.util.Locale

class AdminManagementBookBorrowingAdapter(private val context: Context) :
    RecyclerView.Adapter<AdminManagementBookBorrowingAdapter.AdminBookBorrowingViewHolder>() {

    private var listBookBorrowing = ArrayList<BookBorrowing>()
    private var onDeleteItemClickListener: OnDeleteItemClickListener? = null
    private var onUpdateItemClickListener: OnUpdateItemClickListener? = null

    fun setData(bookBorrowing: ArrayList<BookBorrowing>) {
        this.listBookBorrowing = bookBorrowing
    }

    fun setOnDeleteItemListener(listener: (BookBorrowing, Int) -> Unit) {
        this.onDeleteItemClickListener = object : OnDeleteItemClickListener {
            override fun onItemClick(data: BookBorrowing, position: Int) {
                listener(data, position)
            }
        }
    }

    fun setOnUpdateItemListener(listener: (BookBorrowing, Int) -> Unit) {
        this.onUpdateItemClickListener = object : OnUpdateItemClickListener {
            override fun onItemClick(data: BookBorrowing, position: Int) {
                listener(data, position)
            }
        }
    }

    interface OnDeleteItemClickListener {
        fun onItemClick(data: BookBorrowing, position: Int)
    }

    interface OnUpdateItemClickListener {
        fun onItemClick(data: BookBorrowing, position: Int)
    }

    inner class AdminBookBorrowingViewHolder(val binding: ListItemAdminManagementBookBorrowingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookBorrowing) {
            binding.apply {
                itemNameTextview.text = item.name
                itemIdTextview.text = item.id
            }
        }

        fun bindExpandable(item: BookBorrowing) {
            val dateFormat = SimpleDateFormat("MMMM/dd/yyyy", Locale.getDefault())
            val borrowDate = item.borrowingDate?.let { dateFormat.parse(it) }
            val returnDate = item.returnDate?.let { dateFormat.parse(it) }

            val diffInMillis = borrowDate?.time?.let { returnDate?.time?.minus(it) }
            val diffInDays = diffInMillis?.div((24 * 60 * 60 * 1000)) // Konversi ke hari

            binding.apply {
                viewMoreButton.setImageResource(R.drawable.ic_expand_less)
                if (diffInDays != null) {
                    itemOfDayTextview.text = if (diffInDays <= -1) {
                        binding.itemOfDayTextview.setBackgroundColor(context.getColor(R.color.md_theme_light_error))
                        "Lama Meminjam : $diffInDays Hari"
                    } else {
                        binding.itemOfDayTextview.background = context.getDrawable(R.drawable.bg_textview_blue)
                        "Lama Meminjam : $diffInDays Hari"
                    }
                }
                itemTitleBookTextview.text = context.getString(R.string.item_title_book, item.title)
                itemIsbnBookTextview.text = context.getString(R.string.item_isbn_number, item.isbn)
                itemConditionBookTextview.text =
                    context.getString(R.string.item_condition, item.condition)
                itemBorrowingDateTextview.text =
                    String.format(
                        context.getString(R.string.item_borrowing_date),
                        item.borrowingDate, item.returnDate
                    )

                itemConfirmHasBeenReturnedButton.setOnClickListener {
                    onDeleteItemClickListener?.onItemClick(item, adapterPosition)
                }
                itemExtendButton.setOnClickListener {
                    onUpdateItemClickListener?.onItemClick(item, adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminBookBorrowingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_admin_management_book_borrowing, parent, false)
        return AdminBookBorrowingViewHolder(ListItemAdminManagementBookBorrowingBinding.bind(view))
    }

    override fun getItemCount(): Int = listBookBorrowing.size

    override fun onBindViewHolder(holder: AdminBookBorrowingViewHolder, position: Int) {
        val item = listBookBorrowing[position]

        holder.bind(item)

        if (item.isExpanded) {
            holder.binding.expandLayout.visibility = View.VISIBLE
            holder.bindExpandable(item)
            holder.binding.viewMoreButton.setImageResource(R.drawable.ic_expand_less)
        } else {
            holder.binding.expandLayout.visibility = View.GONE
            holder.binding.viewMoreButton.setImageResource(R.drawable.ic_expand_more)
        }

        holder.itemView.setOnClickListener {
            if (item.isExpanded) {
                holder.binding.expandLayout.visibility = View.GONE
                item.isExpanded = false
                holder.binding.viewMoreButton.setImageResource(R.drawable.ic_expand_more)
            } else {
                holder.binding.expandLayout.visibility = View.VISIBLE
                item.isExpanded = true
                holder.bindExpandable(item)
            }
        }

        holder.binding.viewMoreButton.setOnClickListener {
            if (item.isExpanded) {
                holder.binding.expandLayout.visibility = View.GONE
                item.isExpanded = false
                holder.binding.viewMoreButton.setImageResource(R.drawable.ic_expand_more)
            } else {
                holder.binding.expandLayout.visibility = View.VISIBLE
                item.isExpanded = true
                holder.bindExpandable(item)
            }
        }

    }
}