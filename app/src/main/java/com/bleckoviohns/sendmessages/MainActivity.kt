package com.bleckoviohns.sendmessages

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log
import java.text.DecimalFormat


var ACTION_SMS_SENT = "ACTION_SMS_SENT"
var ACTION_SMS_DELIVERED = "ACTION_SMS_DELIVERED"
var MY_PERMISSIONS_REQUEST_SEND_SMS = 0
var longitudAnterior:Double = 0.0
var latitudAnterior:Double = 0.0
var isFirsTime = true

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
        getLocation()
    }
    @SuppressLint("MissingPermission")
    fun getLocation(){
        ActivityCompat.requestPermissions(this,
            arrayOf( Manifest.permission.ACCESS_FINE_LOCATION),1)
        if(Check_FINE_LOCATION(this)){
            // Acquire a reference to the system Location Manager
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // Define a listener that responds to location updates
            val locationListener = object : LocationListener {

                override fun onLocationChanged(location: Location) {
                    // Called when a new location is found by the network location provider.
                    if(isFirsTime){
                        longitudAnterior = location.longitude
                        latitudAnterior = location.latitude
                        isFirsTime = false
                    }
                    txt_altitud.text = location.latitude.toString()
                    txt_longitud.text = location.longitude.toString()
                    Log.d("Location","($latitudAnterior,$longitudAnterior)-- (${location.latitude},${location.longitude})")
                    val metro:FloatArray = FloatArray(4)
                    Location.distanceBetween(latitudAnterior, longitudAnterior,location.latitude,location.longitude,metro)

                    txt_metros.text = String.format("${metro[0]} metros")
                    if(metro[0]>1.0){
                        sendSms(metro[0])
                    }
                    longitudAnterior = location.longitude
                    latitudAnterior = location.latitude

                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                }

                override fun onProviderEnabled(provider: String) {
                }

                override fun onProviderDisabled(provider: String) {
                }
            }

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.98f,locationListener)
        }

    }

    fun sendSms(metros:Float){
        val sm: SmsManager  = SmsManager.getDefault()
        val parts : ArrayList<String> = sm.divideMessage("El celular se movio $metros metros")
        val iSent: Intent = Intent(ACTION_SMS_SENT)
        val piSent: PendingIntent = PendingIntent.getBroadcast(this, 0, iSent, 0)
        val iDel = Intent(ACTION_SMS_DELIVERED)
        val piDel = PendingIntent.getBroadcast(this, 0, iDel, 0)

        if (parts.size == 1) {
            val msg = parts[0]
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
                    sendSms(0.0f)
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

    fun Check_FINE_LOCATION(act: Activity): Boolean {
        val result =
            ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }
}
