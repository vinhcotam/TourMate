package com.example.tourmate.controller

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.core.content.ContextCompat.getSystemService
import com.example.tourmate.R

class NetworkChangeReceiver : BroadcastReceiver() {

    private var dialog: Dialog? = null
    private var isNetworkConnected = false

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                Log.d("Dialog11", "Network is connected")
                isNetworkConnected = true
                hideDialog()
            } else {
                isNetworkConnected = false
                showDialog(context)
            }
        }
    }

    private fun showDialog(context: Context) {
        if (dialog == null) {
            dialog = Dialog(context)
            dialog!!.setContentView(R.layout.alert_dialog_connect)
            dialog!!.setCanceledOnTouchOutside(false)
            dialog!!.window!!.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val buttonTryAgain = dialog!!.findViewById<Button>(R.id.button_try_again)
            buttonTryAgain.setOnClickListener {
                checkConnection(context)
            }
            dialog!!.show()
        }
    }

    private fun hideDialog() {
        dialog?.dismiss()
        dialog = null
    }

    private fun checkConnection(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            isNetworkConnected = true
            hideDialog()
        }
    }
}
