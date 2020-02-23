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
import android.location.Criteria
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


var ACTION_SMS_SENT = "ACTION_SMS_SENT"
var ACTION_SMS_DELIVERED = "ACTION_SMS_DELIVERED"
var MY_PERMISSIONS_REQUEST_SEND_SMS = 0
var longitudAnterior:Double = 0.0
var latitudAnterior:Double = 0.0
var new_latitude: Double = 0.0
var new_longitude: Double = 0.0
var isFirsTime = true
var provider: String = ""

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
            var criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            criteria.powerRequirement = Criteria.POWER_HIGH
            provider = locationManager.getBestProvider(criteria,true)?:""
            // Define a listener that responds to location updates

            val location = locationManager.getLastKnownLocation(provider)

            try {
                latitudAnterior = location.latitude
                longitudAnterior = location.longitude
            } catch (e:Exception) {

            }

            val locationListener = object : LocationListener {

                override fun onLocationChanged(location2: Location) {
                    // Called when a new location is found by the network location provider.
                    /*if(isFirsTime){
                        longitudAnterior = location.longitude
                        latitudAnterior = location.latitude
                        isFirsTime = false
                    }*/
                    var locaionssd = locationManager.getLastKnownLocation(provider)
                    new_latitude = locaionssd.latitude
                    new_longitude = locaionssd.longitude

                    txt_altitud.text = new_latitude.toString()//location.latitude.toString()
                    txt_longitud.text = new_longitude.toString()//location.longitude.toString()
                    Log.d("Location","($latitudAnterior,$longitudAnterior)-- ($new_latitude,$new_longitude)")
                    val metro:FloatArray = FloatArray(3)
                    Location.distanceBetween(latitudAnterior, longitudAnterior, new_latitude,
                        new_longitude,metro)
                    //val distanciaEnMetros = meterDistanceBetweenPoints(latitudAnterior, longitudAnterior, new_latitude, new_longitude)

                    txt_metros.text = String.format("${metro[0]} metros")
                    //txt_metros.text = String.format("$distanciaEnMetros metros")
                    //sendSms(distanciaEnMetros)
                    if(metro[0]>1.0){
                        //sendSms(metro[0])
                        Toast.makeText(this@MainActivity,"${metro[0]}",Toast.LENGTH_SHORT).show()
                    }
                    longitudAnterior = new_latitude
                    latitudAnterior = new_latitude

                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                }

                override fun onProviderEnabled(provider: String) {
                }

                override fun onProviderDisabled(provider: String) {
                }
            }
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(provider, 1000, 5.0f,locationListener)
        }

    }
    fun meterDistanceBetweenPoints(lat_a :Double, lng_a:Double, lat_b:Double, lng_b:Double) :Float {
        var earthRadius:Double = 6371000.0 //meters
        var dLat:Double = Math.toRadians(lat_b - lat_a)
        var dLng:Double = Math.toRadians(lng_b - lng_a)
        var a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat_a)) * cos(Math.toRadians(lat_b)) * sin(dLng / 2) * sin(dLng / 2)
        var c :Double = 2 * atan2(sqrt(a), sqrt(1 - a))
        //Toast.makeText(MainActivity.this, "calculated distance" + dist + "," + Math.abs((float) old_longitude - (float) new_longitude), Toast.LENGTH_LONG).show();
        //System.out.println("**********this is distance calculation**********" + dist);
        return (earthRadius * c).toFloat()
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
