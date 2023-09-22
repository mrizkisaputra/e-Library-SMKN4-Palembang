package sch.id.smkn4palembang.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.GridItemAdminManagementBooksBinding
import sch.id.smkn4palembang.model.Book

class AdminManagementBooksAdapter(val context: Context) :
    RecyclerView.Adapter<AdminManagementBooksAdapter.AdminManagementBooksViewHolder>() {
    private var listBook: ArrayList<Book> = ArrayList()
    private var onItemClickListener: OnItemClickListener? = null

    fun setData(listBook: ArrayList<Book>) {
        this.listBook = listBook
    }

    interface OnItemClickListener {
        fun onItemClick(book: Book, position: Int)
    }

    fun setOnItemClickListener(listener: (Book, Int) -> Unit) {
        this.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(book: Book, position: Int) {
                listener(book, position)
            }
        }
    }

    inner class AdminManagementBooksViewHolder(private val binding: GridItemAdminManagementBooksBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.apply {
                Glide.with(context)
                    .load(book.cover)
                    .placeholder(R.drawable.default_book_cover)
                    .into(itemCoverBookImageview)

                itemTitleBookTextview.text = book.title
                itemIsbnBookTextview.text = context.getString(R.string.item_isbn, book.isbn)
                itemCategoryBookTextview.text =
                    context.getString(R.string.item_category, book.category)
                itemStockBookTextview.text = context.getString(R.string.item_stock, book.stock)
                itemAvailabilityBookTextview.text = book.availability
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminManagementBooksViewHolder {
        val binding = GridItemAdminManagementBooksBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminManagementBooksViewHolder(binding)
    }

    override fun getItemCount(): Int = listBook.size

    override fun onBindViewHolder(holder: AdminManagementBooksViewHolder, position: Int) {
        val book: Book = listBook[position]
        holder.bind(book)
        holder.itemView.setOnClickListener { onItemClickListener?.onItemClick(book, position) }
    }


}