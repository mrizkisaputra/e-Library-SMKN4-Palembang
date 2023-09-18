package sch.id.smkn4palembang.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.integrity.internal.c
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.DialogAlertBinding
import sch.id.smkn4palembang.model.Book
import sch.id.smkn4palembang.model.Member

class AlertDialog(private val activity: AppCompatActivity) {
    private var onButtonClickListener: OnButtonClickListener? = null

    interface OnButtonClickListener {
        fun onButtonDeleteClick()

        fun onButtonUpdateClick()
    }

    // mengimplementasikan interface untuk callback lamda ketika salah satu button di klik
    fun setOnButtonClickListener(deleteItem: () -> Unit, updateItem: () -> Unit) {
        this.onButtonClickListener = object : OnButtonClickListener {
            override fun onButtonDeleteClick() {
                deleteItem()
            }

            override fun onButtonUpdateClick() {
                updateItem()
            }

        }
    }

    fun createAlertDialog(message: Any) {
        val binding = DialogAlertBinding.inflate(LayoutInflater.from(activity))

        // membuat alert dialog dengan view kostum
        val alertDialog = AlertDialog.Builder(activity).apply {
            setView(binding.root)
            setCancelable(true)
        }.create()

        binding.apply {
            // menampilkan pesan di view alert dialog
            when (message) {

                // jika pesan yang dikirim merupakan instance objek Book
                // dilakukan down casting secara otomatis ke real objek Book
                is Book -> {
                    itemMessage1Textview.text = activity.getString(R.string.item_title, message.title)
                    itemMessage2Textview.text = activity.getString(R.string.item_isbn, message.isbn)
                    itemMessage3Textview.text = activity.getString(R.string.item_category, message.category)
                }

                is Member -> {
                    itemMessage1Textview.text = message.name
                    itemMessage2Textview.text = activity.getString(R.string.list_id_member, message.id)
                    itemMessage3Textview.text = activity.getString(R.string.list_password_member, message.password)
                }
            }

            // menangani button klik
            deleteDataButton.setOnClickListener {
                onButtonClickListener?.onButtonDeleteClick()
                alertDialog.dismiss()
            }
            updateDataButton.setOnClickListener { onButtonClickListener?.onButtonUpdateClick() }
        }


        // menampilkan alert dialog
        alertDialog.show()
    }


}