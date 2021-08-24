package com.example.countdowntimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.countdowntimer.util.NotificationUtil
import com.example.countdowntimer.util.PrefUtil

class TimerExpiredReciever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtil.showTimerExpired(context)

        PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
        PrefUtil.setAlarmSetTime(0, context)
    }
}