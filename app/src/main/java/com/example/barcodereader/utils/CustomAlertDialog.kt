package com.example.barcodereader.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.barcodereader.databinding.CustomAlertDialogBinding


class CustomAlertDialog(private val context: Context) {
    private val alertDialog = AlertDialog.Builder(context).create()
    private var layout = CustomAlertDialogBinding.inflate(LayoutInflater.from(context))

    init {
        alertDialog.setView(layout.root)
        initializeVisibility()
    }

    private fun initializeVisibility() {
//        layout.image.visibility = View.GONE
        layout.titleText.visibility = View.GONE
        layout.messageText.visibility = View.GONE
        layout.body.visibility = View.GONE
        layout.cancelButton.visibility = View.GONE
        layout.okButton.visibility = View.GONE
    }

    fun setMessage(message: String): CustomAlertDialog {
        layout.messageText.visibility = View.VISIBLE
        layout.messageText.text = message
        return this
    }

    fun setTitle(title: String): CustomAlertDialog {
        layout.titleText.visibility = View.VISIBLE
        layout.titleText.text = title
        return this
    }

    fun showDialog() {
        alertDialog.show()
    }

    fun setPositiveButton(
        text: String,
        function: (CustomAlertDialog) -> Unit = {}
    ): CustomAlertDialog {
        layout.okButton.visibility = View.VISIBLE
        layout.okButton.text = text
        layout.okButton.setOnClickListener {
            function(this)
        }
        return this
    }

    fun setNegativeButton(
        text: String,
        function: (CustomAlertDialog) -> Unit = {}
    ): CustomAlertDialog {
        layout.cancelButton.visibility = View.VISIBLE
        layout.cancelButton.text = text
        layout.cancelButton.setOnClickListener {
            function(this)
        }
        return this
    }

    fun setOnDismiss(function: (CustomAlertDialog) -> Unit): CustomAlertDialog {
        alertDialog.setOnDismissListener {
            function(this)
            resetDialog()
        }
        return this
    }

    fun setBody(view: View): CustomAlertDialog {
        layout.body.visibility = View.VISIBLE
        layout.body.addView(view)
        return this
    }

    fun dismiss(): CustomAlertDialog {
        alertDialog.dismiss()
        return this
    }

    private fun resetDialog(): CustomAlertDialog {
        layout.body.removeAllViews()
        layout.body.visibility = View.GONE
        return this
    }
}