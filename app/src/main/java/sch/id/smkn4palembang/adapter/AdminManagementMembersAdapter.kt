package sch.id.smkn4palembang.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ListItemAdminManagementMembersBinding
import sch.id.smkn4palembang.model.Member

class AdminManagementMembersAdapter(val context: Context) :
    RecyclerView.Adapter<AdminManagementMembersAdapter.AdminManagementMembersViewHolder>() {
    private var listMembers = ArrayList<Member>()
    private var onItemClickListener: OnItemClickListener? = null

    fun setData(listMembers: ArrayList<Member>) {
        this.listMembers = listMembers
    }

    interface OnItemClickListener {
        fun onItemClick(item: Member, position: Int)
    }

    fun setOnItemClickListener(member: (Member, Int) -> Unit) {
        this.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(item: Member, position: Int) {
                member(item, position)
            }
        }
    }

    inner class AdminManagementMembersViewHolder(private val binding: ListItemAdminManagementMembersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Member) {
            binding.apply {
                Glide.with(context)
                    .load(item.photo)
                    .placeholder(R.drawable.default_image_avatar_placeholder)
                    .into(itemPhotoImageview)
                itemNameTextview.text = item.name
                if (!item.contact.isNullOrEmpty()) {
                    itemContactTextview.text =
                        context.getString(R.string.list_contact_member, item.contact)
                } else {
                    itemContactTextview.text = context.getString(R.string.list_contact_member, "-")
                }
                itemRegisterTextview.text =
                    context.getString(R.string.list_registrasi_member, item.dateTime)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminManagementMembersViewHolder {
        val binding = ListItemAdminManagementMembersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminManagementMembersViewHolder(binding)
    }

    override fun getItemCount(): Int = listMembers.size

    override fun onBindViewHolder(holder: AdminManagementMembersViewHolder, position: Int) {
        val member = listMembers[position]
        holder.bind(member)

        holder.itemView.setOnClickListener { onItemClickListener?.onItemClick(member, position) }
    }
}