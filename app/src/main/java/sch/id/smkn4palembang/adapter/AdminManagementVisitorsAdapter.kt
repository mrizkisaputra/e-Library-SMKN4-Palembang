package sch.id.smkn4palembang.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.GridItemAdminManagementBooksBinding
import sch.id.smkn4palembang.databinding.ListItemAdminManagementVisitorsBinding
import sch.id.smkn4palembang.model.Book
import sch.id.smkn4palembang.model.Visitor

class AdminManagementVisitorsAdapter(val context: Context) :
    RecyclerView.Adapter<AdminManagementVisitorsAdapter.AdminManagementVisitorViewHolder>() {

    private var listVisitors = ArrayList<Visitor>()

    fun setData(listVisitors: ArrayList<Visitor>) {
        this.listVisitors = listVisitors
    }

    inner class AdminManagementVisitorViewHolder(private val binding: ListItemAdminManagementVisitorsBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(visitor: Visitor) {
            binding.apply {
                Glide.with(context)
                    .load(visitor.photo)
                    .placeholder(R.drawable.ic_avatar)
                    .into(itemPhotoImageview)

                itemNameTextview.text = visitor.name
                itemRoleTextview.text = visitor.role
                itemIdTextview.text = visitor.id
                itemVisitingTimeTextview.text = visitor.visitingTime
                if (visitor.timetamp != null) {
                    itemTimestampTextview.text = DateUtils.getRelativeTimeSpanString(visitor.timetamp)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminManagementVisitorViewHolder {
        val binding = ListItemAdminManagementVisitorsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminManagementVisitorViewHolder(binding)
    }

    override fun getItemCount(): Int = listVisitors.size

    override fun onBindViewHolder(holder: AdminManagementVisitorViewHolder, position: Int) {
        val visitor = listVisitors[position]
        holder.bind(visitor)
    }
}