package com.sap.codelab.platform.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.sap.codelab.R
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.notification.MemoNotifier
import com.sap.codelab.domain.notification.reminderText
import com.sap.codelab.presentation.detail.BUNDLE_MEMO_ID
import com.sap.codelab.presentation.detail.ViewMemo
import com.sap.codelab.presentation.home.Home

private const val CHANNEL_ID = "location_reminders"

/**
 * [MemoNotifier] showing status bar notifications. One notification per memo (id = memo id);
 * tapping it opens the memo, with Home on the back stack.
 */
internal class AndroidMemoNotifier(private val context: Context) : MemoNotifier {

    private val notificationManager = NotificationManagerCompat.from(context)

    @SuppressLint("MissingPermission") // checked with areNotificationsEnabled()
    override fun showReminder(memo: Memo) {
        if (!notificationManager.areNotificationsEnabled()) return
        ensureChannel()
        val text = memo.reminderText()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(memo.title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openMemoIntent(memo.id))
            .setAutoCancel(true)
            .build()
        notificationManager.notify(memo.id.toInt(), notification)
    }

    private fun openMemoIntent(memoId: Long): PendingIntent? {
        val intent = Intent(context, ViewMemo::class.java).putExtra(BUNDLE_MEMO_ID, memoId)
        return TaskStackBuilder.create(context)
            .addNextIntent(Intent(context, Home::class.java))
            .addNextIntent(intent)
            // Request code per memo, so each notification opens its own memo
            .getPendingIntent(memoId.toInt(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    /** Creating an existing channel again is a no-op. */
    private fun ensureChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.notification_channel_reminders))
                .setDescription(context.getString(R.string.notification_channel_reminders_description))
                .build()
        )
    }
}
