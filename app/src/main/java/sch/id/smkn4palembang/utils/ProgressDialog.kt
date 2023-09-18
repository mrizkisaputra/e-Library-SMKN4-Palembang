package sch.id.smkn4palembang.utils

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import sch.id.smkn4palembang.R

class ProgressDialog(private val activity: AppCompatActivity) {
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