package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsTransactionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
        val isSmsDetectionEnabled = prefs.getBoolean("sms_auto_detect_enabled", true)
        if (!isSmsDetectionEnabled) return

        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            // Group messages by originating address in case of multipart SMS
            val sender = messages[0]?.displayOriginatingAddress ?: "Bank"
            val fullBody = StringBuilder()
            for (sms in messages) {
                fullBody.append(sms?.displayMessageBody ?: "")
            }

            val body = fullBody.toString()

            // Parse transaction: only proceeds if SMS contains an actual financial transaction
            val parsedTransaction = SmsTransactionParser.parse(sender, body)

            if (parsedTransaction != null) {
                Log.d("SmsReceiver", "Transaction detected: ${parsedTransaction.amount} (${parsedTransaction.type}) at ${parsedTransaction.memo}")
                SmsNotificationHelper.sendTransactionNotification(context, parsedTransaction)
            } else {
                Log.d("SmsReceiver", "Ignored non-transaction SMS from $sender")
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error processing SMS", e)
        }
    }
}
