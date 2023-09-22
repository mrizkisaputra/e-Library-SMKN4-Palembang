package sch.id.smkn4palembang.utils

import android.app.Activity
import android.app.AlertDialog
import com.google.android.material.button.MaterialButton
import sch.id.smkn4palembang.R

class SuccessDialog(private val activity: Activity) {
    private var onCloseListener: OnCloseListener? = null
    private var dialog: AlertDialog? = null

    fun showSuccessDialog() {
        val view = activity.layoutInflater.inflate(R.layout.dialog_success, null)
        val builder = AlertDialog.Builder(activity).apply {
            setView(view)
            setCancelable(false)
        }.create()

        val closeButton: MaterialButton = view.findViewById(R.id.close_dialog_button)
        closeButton.setOnClickListener {
            onCloseListener?.onClose()
            builder.dismiss()
        }
        builder.show()
    }

    fun setOnCloseListener(onCloseListener: () -> Unit) {
        this.onCloseListener = object : OnCloseListener {
            override fun onClose() {
                onCloseListener()
            }

        }
    }

    interface OnCloseListener {
        fun onClose()
    }

}