package sch.id.smkn4palembang.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ListItemEbookMenuBinding
import sch.id.smkn4palembang.datastore.DataSource
import sch.id.smkn4palembang.model.EbookMenu

class ListEbookMenuAdapter(
    private val context: Context
) : RecyclerView.Adapter<ListEbookMenuAdapter.EbookMenuViewHolder>() {

    private val datastore: List<EbookMenu> = DataSource.getEbookMenu
    private lateinit var onItemClickListener: OnItemClickListener

    inner class EbookMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemEbookMenuBinding.bind(itemView)

        fun bind(image: Int, subtitle: String) {
            binding.apply {
                ebookTitleTextview.text = context.getString(R.string.e_book)
                ebookSubtitleTextview.text = subtitle
                ebookImageview.setImageResource(image)
            }
        }


    }

    interface OnItemClickListener {
        fun onClick(url: String)
    }

    fun setOnItemClickListener(onItemClickListener: (url: String) -> Unit) {
        this.onItemClickListener = object : OnItemClickListener {
            override fun onClick(url: String) {
                onItemClickListener(url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EbookMenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ebook_menu, parent, false)
        return EbookMenuViewHolder(view)
    }

    override fun getItemCount(): Int = datastore.size

    override fun onBindViewHolder(holder: EbookMenuViewHolder, position: Int) {
        val (image, subtitle, url) = datastore[position]

        holder.bind(image, subtitle)
        holder.itemView.setOnClickListener { onItemClickListener.onClick(url) }
    }
}