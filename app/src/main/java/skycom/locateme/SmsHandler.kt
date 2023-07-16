package skycom.locateme

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager

class SmsHandler {

    companion object{
        fun sendSMS(number: String, message: String, ctx: Context) {
            val sentPI: PendingIntent = PendingIntent.getBroadcast(
                ctx,
                0,
                Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )
            SmsManager.getDefault().sendTextMessage(number, null, message, sentPI, null)
        }
    }
}