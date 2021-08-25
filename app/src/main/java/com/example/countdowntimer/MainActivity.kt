package com.example.countdowntimer

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.example.countdowntimer.util.NotificationUtil
import com.example.countdowntimer.util.PrefUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity(), ExampleDialog.ExampleDialogListener {

    companion object{
        fun setAlarm(context: Context, nowSeconds:Long, secondsRemaining:Long):Long{
            val wakeUpTime = (nowSeconds + secondsRemaining)*1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReciever::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context, TimerExpiredReciever::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds:Long
            get() = Calendar.getInstance().timeInMillis/1000
        var timeRM = 0L
        var wUTime = 0L
    }

    enum class TimerState{
        Stopped, Paused, Running
    }

    lateinit var timer: CountDownTimer
    var timerLengthSeconds = 0L
    var timerState = TimerState.Stopped
    var secondsRemaining = 0L

    var timeStarted = false
    var timePaused = false
    lateinit var setTime: FloatingActionButton
    lateinit var startTime: FloatingActionButton
    lateinit var resetTime: FloatingActionButton
    lateinit var pauseTime: FloatingActionButton
    lateinit var forwardTime: FloatingActionButton
    lateinit var backwardTime: FloatingActionButton
    lateinit var progressBar: ProgressBar
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setTime = findViewById(R.id.setTimeButton) as FloatingActionButton
        startTime = findViewById(R.id.startButton) as FloatingActionButton
        pauseTime = findViewById(R.id.pauseButton) as FloatingActionButton
        resetTime = findViewById(R.id.resetButton) as FloatingActionButton
        forwardTime = findViewById(R.id.forwardButton) as FloatingActionButton
        backwardTime = findViewById(R.id.backwardButton) as FloatingActionButton
        progressBar = findViewById(R.id.progressBar) as ProgressBar

        setTime.setOnClickListener{
            openDialog()
        }
        startTime.setOnClickListener{
            if (!timeStarted) {
                startTimer()
                timerState = TimerState.Running
                //startStopService()
            } else {
                timer.cancel()
                timerState = TimerState.Stopped
                timerLengthSeconds = 0
                onTimerFinished()
            }
            updateButtons()
        }
        pauseTime.setOnClickListener{
            if(timePaused == false){
                timer.cancel()
                timerState = TimerState.Paused
            } else {
                startTimer()
                timerState = TimerState.Running
                timePaused = false
            }
            updateButtons()
        }
        resetTime.setOnClickListener{
            timer.cancel()
            timerState = TimerState.Stopped
            timerLengthSeconds = 0
            onTimerFinished()
            updateButtons()
        }
        forwardTime.setOnClickListener{
            startStopService()
        }

        notificationManager = NotificationManagerCompat.from(this)

    }
    private fun startStopService() {
        if (isMyServiceRunning(MyService::class.java)){
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show()
            stopService(Intent(this, MyService::class.java))
        }
        else{
            Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show()
            startService(Intent(this, MyService::class.java))
        }
    }

    private fun isMyServiceRunning(mClass: Class<MyService>): Boolean {
        val manager:ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)){
            if (mClass.name.equals(service.service.className)){
                return true
            }
        }
        return false
    }


    override fun onResume() {
        super.onResume()

        initTimer()
        removeAlarm(this)
        NotificationUtil.hideNotification(this)
        NotificationUtil.flag = false
        //stopService(Intent(this, NotificationUtil::class.java))
    }

    override fun onPause() {
        super.onPause()

        if(timerState == TimerState.Running){
            NotificationUtil.flag = true
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            wUTime = wakeUpTime
            timeRM = secondsRemaining
            NotificationUtil.showTimerRunning(this, wakeUpTime)
            //startService(Intent(this, NotificationUtil::class.java))
        }
        else if (timerState == TimerState.Paused){
            //NotificationUtil.showTimerPaused(this)
        }
        PrefUtil.setTimerLength(timerLengthSeconds, this)
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun initTimer() {
        timerState = PrefUtil.getTimerState(this)
        /*
        if (timerState == TimerState.Stopped || timerState == TimerState.Running){
            setNewTimerLength()
        }
        else
            setPreviousTimerLength()

         */
        setNewTimerLength()

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime>0)
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished(){
        timerState = TimerState.Stopped

        setNewTimerLength()

        progressBar.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun updateCountdownUI() {
        val hoursUntilFinished = secondsRemaining/3600
        val minutesUntilFinished = (secondsRemaining - hoursUntilFinished*3600)/60
        val secondsUntilFinished = secondsRemaining- hoursUntilFinished*3600 - minutesUntilFinished*60
        val hoursString = hoursUntilFinished.toString()
        val minutesString = minutesUntilFinished.toString()
        val secondsString = secondsUntilFinished.toString()
        val timeView = findViewById(R.id.timeView) as TextView
        timeView.text = "${
            if(hoursString.length == 2)hoursUntilFinished
            else "0" + hoursString}:${
            if (minutesString.length == 2)minutesUntilFinished
            else "0" + minutesString}:${
            if (secondsString.length == 2) secondsString
            else "0" + secondsString}"
        progressBar.progress = (secondsRemaining).toInt()
    }

    private fun updateButtons() {
        when (timerState){
            TimerState.Running ->{
                startTime.setImageResource(R.drawable.ic_stop)
                pauseTime.setImageResource(R.drawable.ic_pause)
                pauseTime.isClickable = true
                timeStarted = true
            }
            TimerState.Stopped ->{
                startTime.setImageResource(R.drawable.ic_start)
                pauseTime.setImageResource(R.drawable.ic_pause)
                pauseTime.isClickable = false
                timeStarted = false
                timePaused = false
            }
            TimerState.Paused ->{
                startTime.setImageResource(R.drawable.ic_stop)
                pauseTime.setImageResource(R.drawable.ic_resume)
                timePaused = true
            }
        }
    }

    private fun startTimer() {
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining*1000, 1000){
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished/1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength(){
        //timerLengthSeconds = PrefUtil.getSecondsRemaining(this)         //lengthMinutes*60L
        timerLengthSeconds = PrefUtil.getTimerLength(this)
        progressBar.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength(){
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progressBar.max = timerLengthSeconds.toInt()
    }

    private fun openDialog() {
        val exampleDialog = ExampleDialog()
        exampleDialog.show(supportFragmentManager, "example")
    }

    override fun applyTexts(hourInput: String, minuteInput: String, secondInput: String) {
        val text = hourInput + ":" + minuteInput + ":" + secondInput
        var seconds = hourInput.toLong()*3600 + minuteInput.toLong()*60 + secondInput.toLong()
        PrefUtil.setSecondsRemaining(seconds, this)
        PrefUtil.setTimerLength(seconds, this)
        timerLengthSeconds = seconds
        initTimer()
        val timeView = findViewById(R.id.timeView) as TextView
        timeView.text = text
    }
}