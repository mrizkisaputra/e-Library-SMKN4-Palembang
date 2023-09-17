package sch.id.smkn4palembang.admin.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.checkerframework.checker.units.qual.A
import sch.id.smkn4palembang.databinding.DialogFailedBinding

class FailedDialog(private val activity: AppCompatActivity) {
    private var onCloseListener: OnCloseListener? = null

    fun showFailedDialog(message: Any) {
        val binding = DialogFailedBinding.inflate(LayoutInflater.from(activity))
        val alertDialog = AlertDialog.Builder(activity).apply {
            setView(binding.root)
            setCancelable(false)
        }.create()

        binding.apply {
            when {
                message is String -> {
                    failedInsertDataTextview.text = message
                }
            }

            closeDialogButton.setOnClickListener {
                onCloseListener?.onClose()
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    fun setOnCloseListener(listener: () -> Unit) {
        onCloseListener = object : OnCloseListener {
            override fun onClose() {
                listener()
            }
        }
    }

    interface OnCloseListener {
        fun onClose()
    }

}