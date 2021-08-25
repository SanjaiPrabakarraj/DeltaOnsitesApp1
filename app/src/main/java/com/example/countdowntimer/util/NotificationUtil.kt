package com.example.countdowntimer.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.countdowntimer.MainActivity
import com.example.countdowntimer.R
import com.example.countdowntimer.TimerNotificationReciever
import java.text.SimpleDateFormat
import java.util.*


class NotificationUtil{
    companion object{
        private const val CHANNEL_ID_TIMER = "menu_timer"
        private const val CHANNEL_NAME_TIMER = "Timer App"
        private const val TIMER_ID = 0
        var flag = true

        fun showTimerExpired(context: Context){
            val startIntent = Intent(context, TimerNotificationReciever::class.java)
            startIntent.action = Constants.ACTION_START
            val startPendingIntent = PendingIntent.getBroadcast(context,
            0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer Expired")
                .setContentText("Start Again?")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .addAction(R.drawable.ic_resume, "Ok", startPendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)
            nManager.notify(1, nBuilder.build())
        }
        fun showTimerRunning(context: Context, wakeUpTime:Long){
            val stopIntent = Intent(context, TimerNotificationReciever::class.java)
            stopIntent.action = Constants.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context,
                0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val pauseIntent = Intent(context, TimerNotificationReciever::class.java)
            pauseIntent.action = Constants.ACTION_PAUSE
            val pausePendingIntent = PendingIntent.getBroadcast(context,
                0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            val nowSeconds = MainActivity.nowSeconds
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            var temp = MainActivity.timeRM
            nBuilder.setContentTitle("Timer is Running")
                .setContentText("End: ${temp}")//df.format(Date(wakeUpTime))
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
                .setOnlyAlertOnce(true)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)
            nManager.notify(TIMER_ID, nBuilder.build())

            Thread(Runnable {
                while (temp>0 && flag){
                    val hoursUntilFinished = temp/3600
                    val minutesUntilFinished = (temp - hoursUntilFinished*3600)/60
                    val secondsUntilFinished = temp - hoursUntilFinished*3600 - minutesUntilFinished*60
                    val hoursString = hoursUntilFinished.toString()
                    val minutesString = minutesUntilFinished.toString()
                    val secondsString = secondsUntilFinished.toString()
                    val timeView = "${
                        if(hoursString.length == 2)hoursUntilFinished
                        else "0" + hoursString}:${
                        if (minutesString.length == 2)minutesUntilFinished
                        else "0" + minutesString}:${
                        if (secondsString.length == 2) secondsString
                        else "0" + secondsString}"
                    temp -= 1L
                    nBuilder.setContentText(timeView)
                    nManager.notify(TIMER_ID, nBuilder.build())
                    SystemClock.sleep(1000)
                }
                //nManager.cancelAll()
                nManager.cancel(0)
            }).start()
        }
        fun showTimerPaused(context: Context){
            val resumeIntent = Intent(context, TimerNotificationReciever::class.java)
            resumeIntent.action = Constants.ACTION_RESUME
            val resumePendingIntent = PendingIntent.getBroadcast(context,
                0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer is Paused")
                .setContentText("Resume?")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_resume, "Resume", resumePendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)
            nManager.notify(TIMER_ID, nBuilder.build())
        }

        fun hideNotification(context: Context){
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancelAll()
        }

        private fun getBasicNotificationBuilder(context: Context, channelId: String, playSound: Boolean): NotificationCompat.Builder {
            val notifSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(true)
                .setDefaults(0)
            if (playSound) nBuilder.setSound(notifSound)
            return nBuilder
        }

        private fun <T> getPendingIntentWithStack(context: Context, javaClass: Class<T>):PendingIntent{
            val resultIntent = Intent(context, javaClass)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntent)

            return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun NotificationManager.createNotificationChannel(channelID:String, channelName:String, playSound:Boolean){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
                val nChannel = NotificationChannel(channelID, channelName, channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor = Color.BLUE
                this.createNotificationChannel(nChannel)
            }
        }
    }

}