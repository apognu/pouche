package com.github.apognu.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.github.apognu.push.model.Message
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MessagingService : FirebaseMessagingService() {
  private val channelId = "notifications"

  private val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.US)

  override fun onMessageReceived(message: RemoteMessage) {
    val uid = message.messageId ?: ""
    val title = message.data["title"] ?: ""
    val body = message.data["body"] ?: ""
    val banner = message.data["banner"] ?: ""
    val topic = message.from?.removePrefix("/topics/") ?: ""
    val color = message.data["color"] ?: ""
    val date = Date(message.sentTime)

    (application as Pouche).messageRepository.insert(
        Message(0, uid, title, body, banner, dateFormat.format(date), topic, color)
    )

    notify(Random().nextInt(), title, body, banner)

    super.onMessageReceived(message)
  }

  private fun notify(id: Int, title: String, body: String, banner: String) {
    createChannel()

    val intent =
        TaskStackBuilder.create(this).run {
          addNextIntentWithParentStack(Intent(this@MessagingService, MainActivity::class.java))
          getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

    val bannerDrawable =
        try {
          if (banner.isNotEmpty()) {
            Picasso.get().load(banner).get()
          } else {
            null
          }
        } catch (_: Exception) {
          null
        }

    val builder =
        NotificationCompat.Builder(this, channelId)
            .setContentIntent(intent)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setLargeIcon(bannerDrawable)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(bannerDrawable).bigLargeIcon(null)
            )

    with(NotificationManagerCompat.from(this)) { notify(id, builder.build()) }
  }

  private fun createChannel() {
    val name = "Notifications"
    val descriptionText = "Important information about this app"
    val importance = NotificationManager.IMPORTANCE_HIGH

    NotificationChannel(channelId, name, importance).apply {
      description = descriptionText

      (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
          .createNotificationChannel(this)
    }
  }
}
