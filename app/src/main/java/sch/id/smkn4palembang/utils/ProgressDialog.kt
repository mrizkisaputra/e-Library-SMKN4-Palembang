package sch.id.smkn4palembang.utils

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import sch.id.smkn4palembang.R

class ProgressDialog(private val activity: Activity) {
    private lateinit var dialog: AlertDialog

    fun showProgressDialog() {
        val view = activity.layoutInflater.inflate(R.layout.dialog_progress, null)

        val builder = AlertDialog.Builder(activity).apply {
            this.setView(view)
            this.setCancelable(false)
        }

        dialog = builder.create()
        dialog.show()
    }

    fun dismissProgressDialog() {
        dialog.dismiss()
    }
}