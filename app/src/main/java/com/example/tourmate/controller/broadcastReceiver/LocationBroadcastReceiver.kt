package com.example.tourmate.controller.broadcastReceiver

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.view.WindowManager
import android.widget.Button
import com.example.tourmate.R

class LocationBroadcastReceiver : BroadcastReceiver() {
    private var dialog: Dialog? = null
    private var isLocationOn = false
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                isLocationOn = true
            } else {
                isLocationOn = false
                showDialog(context)
            }
        }
    }

    private fun showDialog(context: Context) {
        if (dialog == null) {
            dialog = Dialog(context)
            dialog!!.setContentView(R.layout.alert_dialog_location)
            dialog!!.setCanceledOnTouchOutside(false)
            dialog!!.window!!.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val buttonTryAgain = dialog!!.findViewById<Button>(R.id.button_try_again)
            buttonTryAgain.setOnClickListener {
                checkLocation(context)
            }
            dialog!!.show()
        }
    }
    private fun hideDialog() {
        dialog?.dismiss()
        dialog = null
    }
    private fun checkLocation(context: Context) {

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isLocationOn = true
            hideDialog()
        }
    }
}