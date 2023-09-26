package sch.id.smkn4palembang.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.GridItemHorizontalOpacBinding
import sch.id.smkn4palembang.model.Book

class OpacSearchResultAdapter(private val context: Context)  :
    RecyclerView.Adapter<OpacSearchResultAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    private var listOpac: MutableList<Book> = mutableListOf()

    interface OnItemClickListener {
        fun onClick(book: Book, position: Int)
    }

    fun setOnItemClickListener(listener: (Book, Int) -> Unit) {
        onItemClickListener = object : OnItemClickListener {
            override fun onClick(book: Book, position: Int) {
                listener(book, position)
            }
        }
    }

    fun setData(opac: MutableList<Book>) {
        listOpac = opac
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val binding = GridItemHorizontalOpacBinding.bind(view)
        fun bind(item: Book) {
            binding.apply {
                itemTitleBookTextview.text = item.title
                itemCategoryTextview.text = context.getString(R.string.item_category, item.category)
                itemIsbnTextview.text = context.getString(R.string.isbn, item.isbn)
                Glide.with(context)
                    .load(item.cover)
                    .placeholder(R.drawable.default_book_cover)
                    .into(itemCoverBookImageview)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.grid_item_search, parent, false)
        )
    }

    override fun getItemCount(): Int = listOpac.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemOpac = listOpac[position]
        holder.bind(itemOpac)

        holder.itemView.setOnClickListener { onItemClickListener?.onClick(itemOpac, position) }
    }


}