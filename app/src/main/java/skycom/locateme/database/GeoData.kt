package skycom.locateme.database

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import skycom.locateme.SmsHandler
import skycom.locateme.fusedLocationClient

class GeoData(
    private val lat: String,
    private val lon: String,
    private val author: String
){
    fun createMessageWithLocation() : String{
        return "LocateMe\nLatitude:$lat\nLongitude:$lon\nFrom:$author\n" +
                "http://maps.google.com/maps?q=loc:$lat,$lon($author)\n"
    }

    companion object{

        fun getAndSendLocation(number: String, ctx: Context) {

            val locationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
            var locationData: GeoData

            if (ActivityCompat.checkSelfPermission(
                    ctx, Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {

                fusedLocationClient.getCurrentLocation(
                    locationRequest,
                    object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                            CancellationTokenSource().token

                        override fun isCancellationRequested() = false
                    }
                ).addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        Toast.makeText(
                            ctx,
                            "Couldn't get location.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val lat = location.latitude
                        val lon = location.longitude
                        Toast.makeText(
                            ctx,
                            "Latitude: $lat\nLongitude: $lon",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("LAT", lat.toString())
                        Log.e("LONG", lon.toString())

                        locationData = GeoData(
                            lat.toString(),
                            lon.toString(),
                            "Sky"
                        )
                        SmsHandler.sendSMS(number, locationData.createMessageWithLocation(), ctx)
                    }
                }
            }
        }
    }
}