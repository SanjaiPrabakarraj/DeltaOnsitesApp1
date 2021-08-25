package com.example.countdowntimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.countdowntimer.util.Constants
import com.example.countdowntimer.util.NotificationUtil
import com.example.countdowntimer.util.PrefUtil

class TimerNotificationReciever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            Constants.ACTION_STOP->{
                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
                NotificationUtil.hideNotification(context)
                NotificationUtil.flag = false

            }
            Constants.ACTION_PAUSE->{
                var secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)
                val nowSeconds = MainActivity.nowSeconds
                NotificationUtil.flag = false

                secondsRemaining -= nowSeconds - alarmSetTime
                PrefUtil.setSecondsRemaining(secondsRemaining, context)

                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Paused, context)
                //NotificationUtil.showTimerPaused(context)
            }
            Constants.ACTION_RESUME->{
                val secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
                NotificationUtil.flag = true
            }
            Constants.ACTION_START->{
                val timeRemaining = PrefUtil.getTimerLength(context)
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, timeRemaining)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                PrefUtil.setSecondsRemaining(timeRemaining, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
                NotificationUtil.flag = true
                NotificationUtil.hideNotification(context)
            }

        }
    }
}