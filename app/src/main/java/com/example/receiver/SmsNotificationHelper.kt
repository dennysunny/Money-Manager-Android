package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import java.text.DecimalFormat

object SmsNotificationHelper {

    const val CHANNEL_ID = "sms_transactions"
    const val ACTION_RECORD_SMS_TRANSACTION = "com.example.ACTION_RECORD_SMS_TRANSACTION"
    const val EXTRA_SMS_AMOUNT = "EXTRA_SMS_AMOUNT"
    const val EXTRA_SMS_TYPE = "EXTRA_SMS_TYPE"
    const val EXTRA_SMS_CATEGORY = "EXTRA_SMS_CATEGORY"
    const val EXTRA_SMS_MEMO = "EXTRA_SMS_MEMO"
    const val EXTRA_SMS_WALLET = "EXTRA_SMS_WALLET"
    const val EXTRA_SMS_DATE = "EXTRA_SMS_DATE"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Transaction Alerts"
            val descriptionText = "Notifications to record detected payments and credits from bank SMS"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendTransactionNotification(context: Context, transaction: ParsedSmsTransaction) {
        createNotificationChannel(context)

        val numberFormat = DecimalFormat("#,##,##0.00")
        val formattedAmt = numberFormat.format(transaction.amount)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_RECORD_SMS_TRANSACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SMS_AMOUNT, transaction.amount)
            putExtra(EXTRA_SMS_TYPE, transaction.type)
            putExtra(EXTRA_SMS_CATEGORY, transaction.category)
            putExtra(EXTRA_SMS_MEMO, transaction.memo)
            putExtra(EXTRA_SMS_WALLET, transaction.wallet)
            putExtra(EXTRA_SMS_DATE, System.currentTimeMillis())
        }

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            pendingIntentFlags
        )

        val title = if (transaction.type == "Expense") {
            "Payment Detected: ₹$formattedAmt"
        } else {
            "Money Received: ₹$formattedAmt"
        }

        val subtext = if (transaction.type == "Expense") {
            "Spent at ${transaction.memo} via ${transaction.wallet}. Tap to record."
        } else {
            "Received from ${transaction.memo} via ${transaction.wallet}. Tap to record."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(subtext)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText("$subtext\n\nCategory: ${transaction.category}\n\"${transaction.rawSnippet}\"")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_input_add,
                "Record in App",
                pendingIntent
            )

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Notification permission was not granted by user yet
        }
    }
}
