package skycom.locateme

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import skycom.locateme.database.GeoData
import skycom.locateme.database.RoomDbProvider


class SmsService : Service() {
    private var smsReceiver: BroadcastReceiver? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerSmsReceiver()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSmsReceiver()
    }

    private fun unregisterSmsReceiver() {
        unregisterReceiver(smsReceiver)
    }

    private fun registerSmsReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                    val bundle = intent.extras
                    if (bundle != null) {
                        val pdus = bundle.get("pdus") as Array<Any>
                        for (pdu in pdus) {
                            val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                            val messageBody = smsMessage.messageBody
                            val phoneNumber = smsMessage.originatingAddress
                            Log.e("SMS", "Nr: $phoneNumber\nmsg: $messageBody")

                            if(phoneNumber != null) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (RoomDbProvider.getDatabase(context).phoneNumberDao()
                                            .isPhoneNumberEnabled(
                                                phoneNumber.toString()
                                            )
                                    ) {
                                        if (messageBody.contains("LocateMe!")) {
                                            requestedLocation(phoneNumber)
                                        } else if (messageBody.contains("LocateMe\n")) {
                                            receivedLocation(messageBody)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        registerReceiver(smsReceiver, intentFilter)
    }


    private fun extractValue(message: String, key: String): String? {
        val startIndex = message.indexOf(key)
        if (startIndex != -1) {
            val endIndex = message.indexOf('\n', startIndex)
            if (endIndex != -1) {
                return message.substring(startIndex + key.length, endIndex).trim()
            }
        }
        return null
    }

    private fun receivedLocation(messageBody: String) {
        val latitude = extractValue(messageBody, "Latitude:")
        val longitude = extractValue(messageBody, "Longitude:")
        val author = extractValue(messageBody, "From:")


        val geoUri =
            "http://maps.google.com/maps?q=loc:$latitude,$longitude ($author)"
        val openMaps = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        openMaps.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        baseContext.startActivity(openMaps)
        // Implement the action you want to perform when the specific string is found in an SMS
    }

    private fun requestedLocation(phoneNumber: String) {
        GeoData.getAndSendLocation(phoneNumber, applicationContext)
    }
}