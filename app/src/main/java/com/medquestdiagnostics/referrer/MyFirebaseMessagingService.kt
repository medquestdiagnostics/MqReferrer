package com.medquestdiagnostics.referrer

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.Notification.BADGE_ICON_SMALL
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.RingtoneManager.getDefaultUri
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService: FirebaseMessagingService() {

    private val CHANNEL_ID = "MyApp"
    internal var count = 0

   // var context = applicationContext

    @SuppressLint("WrongConstant")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("message"))

        // count = countStr.toInt()
         count = count + 1
        val requestID = System.currentTimeMillis().toInt()
//        val sp = getSharedPreferences("Notification", MODE_PRIVATE)
//        val Ed = sp.edit()
//        Ed.putString("fromnotification", "yes")
//        Ed.commit()

        var nottitle: String? = remoteMessage.getData()?.get("title")
        var message: String? = remoteMessage.getData().get("message")
        var clickAction: String? = remoteMessage.getData().get("click_action")

        if (nottitle == null || nottitle == "null"){
            nottitle = remoteMessage.getNotification()?.title
            message = remoteMessage.getNotification()?.body
            clickAction = remoteMessage.getNotification()?.clickAction
        }

        val intent = Intent(this, Home::class.java)
        intent.putExtra("FromNotification", "77")
        intent.putExtra("clickAction", clickAction)
        intent.putExtra("launchURL", com.medquestdiagnostics.siri.Settings.NOTIFICATION_URL)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent
                .getActivity(this,
                        requestID, intent, PendingIntent.FLAG_IMMUTABLE)

           val channelId = "Default"
           val defaultSound  = Settings.System.DEFAULT_NOTIFICATION_URI
            val builder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(com.medquestdiagnostics.siri.R.drawable.ic_notification_icon)
                .setContentTitle(nottitle)
                    .setAutoCancel(true)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setSound(defaultSound)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(contentIntent)
                .setBadgeIconType(BADGE_ICON_SMALL)
                .setNumber(count);
//
           val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

//        val intent = Intent(applicationContext, SplashActivity::class.java)
//       // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        val xId: String = "77"
//        intent.putExtra("FromNotification", xId)
////        intent.action = xId
////        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//
//      //  intent.putExtra("FromNotification", "1".toString());
//        val contentIntent = PendingIntent.getActivity(
//                applicationContext,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT)

        // for defaul notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    "Default channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

            // for custome sound
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                var mChannel =  NotificationChannel(channelId,
                        "Default channel",
                        NotificationManager.IMPORTANCE_DEFAULT)

                var attributes =  AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

                mChannel.setDescription(message);
                mChannel.setShowBadge(true)
                mChannel.enableLights(true);
                mChannel.enableVibration(true);
                mChannel.setSound(defaultSound, attributes); // This is IMPORTANT

                if (manager != null)
                    manager.createNotificationChannel(mChannel);

            }
            manager.notify(1000, builder.build())



    }

    override fun onNewToken(token: String) {


        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your app server.
    }


//    private fun createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = getString(R.string.app_name)
//            val description = getString(R.string.app_name)
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel(CHANNEL_ID, name, importance)
//            channel.description = description
//            channel.setShowBadge(true)
//            // Register the channel with the system; you can't change the importance or other notification behaviors after this
//            val notificationManager = getSystemService(NotificationManager::class.java)
//            notificationManager!!.createNotificationChannel(channel)
//
//        }
//    }




//    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
//
//
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val NOTIFICATION_CHANNEL_ID = "Nilesh_channel"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Your Notifications", NotificationManager.IMPORTANCE_HIGH)
//
//            notificationChannel.description = "Description"
//            notificationChannel.enableLights(true)
//            notificationChannel.lightColor = Color.RED
//            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
//            notificationChannel.enableVibration(true)
//            notificationManager.createNotificationChannel(notificationChannel)
//        }
//
//        // to diaplay notification in DND Mode
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
//            channel.canBypassDnd()
//        }
//
//        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//
//        notificationBuilder.setAutoCancel(true)
//            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
//            .setContentTitle(getString(R.string.app_name))
//            .setContentText(remoteMessage!!.getNotification()!!.getBody())
//            .setDefaults(Notification.DEFAULT_ALL)
//            .setWhen(System.currentTimeMillis())
//            .setSmallIcon(R.drawable.ic_launcher_background)
//            .setAutoCancel(true)
//
//
//        notificationManager.notify(1000, notificationBuilder.build())
//
//    }
}