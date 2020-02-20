package com.bleckoviohns.sendmessages

import android.Manifest
import android.R.id.message
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


var ACTION_SMS_SENT = "ACTION_SMS_SENT"
var ACTION_SMS_DELIVERED = "ACTION_SMS_DELIVERED"
var MY_PERMISSIONS_REQUEST_SEND_SMS = 0

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    MY_PERMISSIONS_REQUEST_SEND_SMS)
            } else {
                ActivityCompat.requestPermissions(this,
                     arrayOf(Manifest.permission.SEND_SMS),
                    MY_PERMISSIONS_REQUEST_SEND_SMS)
            }
        }

    }
    fun sendSms(){
        var sm: SmsManager  = SmsManager.getDefault()
        var parts : ArrayList<String> = sm.divideMessage("El celular se movio")
        var iSent: Intent = Intent(ACTION_SMS_SENT)
        var piSent: PendingIntent = PendingIntent.getBroadcast(this, 0, iSent, 0)
        val iDel = Intent(ACTION_SMS_DELIVERED)
        val piDel = PendingIntent.getBroadcast(this, 0, iDel, 0)

        if (parts.size == 1) {
            var msg = parts[0]
            sm.sendTextMessage("+525525188210", null, msg, piSent, piDel)
        } else {
            val sentPis = ArrayList<PendingIntent>()
            val delPis = ArrayList<PendingIntent>()
            val ct = parts.size
            for (i in 0 until ct) {
                sentPis.add(i, piSent)
                delPis.add(i, piDel)
            }
            sm.sendMultipartTextMessage("+525525188210", null, parts, sentPis, delPis)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSms()
                    Toast.makeText(applicationContext, "SMS sent.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "SMS faild, please try again.", Toast.LENGTH_LONG).show()
                    return
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    class SmsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == ACTION_SMS_SENT) {
                when (resultCode) {
                    Activity.RESULT_OK -> Toast.makeText(
                        context, "SMS sent",
                        Toast.LENGTH_SHORT
                    ).show()
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Toast.makeText(
                        context, "Generic failure",
                        Toast.LENGTH_SHORT
                    ).show()
                    SmsManager.RESULT_ERROR_NO_SERVICE -> Toast.makeText(
                        context, "No service",
                        Toast.LENGTH_SHORT
                    ).show()
                    SmsManager.RESULT_ERROR_NULL_PDU -> Toast.makeText(
                        context, "Null PDU",
                        Toast.LENGTH_SHORT
                    ).show()
                    SmsManager.RESULT_ERROR_RADIO_OFF -> Toast.makeText(
                        context, "Radio off",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (action == ACTION_SMS_DELIVERED) {
                when (resultCode) {
                    Activity.RESULT_OK -> Toast.makeText(
                        context, "SMS delivered",
                        Toast.LENGTH_SHORT
                    ).show()
                    Activity.RESULT_CANCELED -> Toast.makeText(
                        context, "SMS not delivered",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
