package com.example.countdowntimer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.countdowntimer.util.NotificationUtil


class MyService:Service() {
    val CHANNEL_ID = "123"
    val SAMPLE_ID = 2
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotifChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()

        return START_STICKY
    }

    private fun showNotification() {
        var temp = 0
        val notifIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("$temp")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
        val nManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(SAMPLE_ID, notification.build())
        Thread{
            while (temp>0 && temp<100){
                temp += 1
                notification.setContentText("$temp")
                nManager.notify(SAMPLE_ID, notification.build())
                NotificationUtil.showTimerRunning(applicationContext, MainActivity.wUTime)
                startForeground(SAMPLE_ID, notification.build())
                SystemClock.sleep(1000)
            }
        }

        startForeground(SAMPLE_ID, notification.build())
    }

    private fun createNotifChannel(){
        val serviceChannel = NotificationChannel(
            CHANNEL_ID, "My Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(serviceChannel)
    }

}