package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.TimetableSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AlarmRingingService : Service() {

  companion object {
    const val CHANNEL_ID = "studypilot_foreground_alarm_channel"
    const val NOTIFICATION_ID = 8881

    const val ACTION_START_RINGING = "com.example.data.service.ACTION_START_RINGING"
    const val ACTION_STOP_RINGING = "com.example.data.service.ACTION_STOP_RINGING"

    fun startRinging(context: Context, session: TimetableSession) {
      val intent = Intent(context, AlarmRingingService::class.java).apply {
        action = ACTION_START_RINGING
        putExtra(AlarmReceiver.EXTRA_SESSION_ID, session.id)
        putExtra(AlarmReceiver.EXTRA_SUBJECT_NAME, session.subjectName)
        putExtra(AlarmReceiver.EXTRA_TOPIC, session.topic)
        putExtra(AlarmReceiver.EXTRA_START_TIME, session.startTime)
        putExtra(AlarmReceiver.EXTRA_END_TIME, session.endTime)
        putExtra(AlarmReceiver.EXTRA_LOCATION, session.location)
        putExtra(AlarmReceiver.EXTRA_NOTES, session.notes)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stopRinging(context: Context) {
      AlarmSoundManager.stopSoundAndVibration(context)
      val intent = Intent(context, AlarmRingingService::class.java).apply {
        action = ACTION_STOP_RINGING
      }
      try {
        context.startService(intent)
      } catch (e: Exception) {
        // Ignore
      }
    }
  }

  private var wakeLock: PowerManager.WakeLock? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START_RINGING -> {
        val sessionId = intent.getLongExtra(AlarmReceiver.EXTRA_SESSION_ID, 0L)
        val subjectName = intent.getStringExtra(AlarmReceiver.EXTRA_SUBJECT_NAME) ?: "Study Session"
        val topic = intent.getStringExtra(AlarmReceiver.EXTRA_TOPIC) ?: "Scheduled Alarm"
        val startTime = intent.getStringExtra(AlarmReceiver.EXTRA_START_TIME) ?: "Now"
        val endTime = intent.getStringExtra(AlarmReceiver.EXTRA_END_TIME) ?: ""
        val location = intent.getStringExtra(AlarmReceiver.EXTRA_LOCATION) ?: ""
        val notes = intent.getStringExtra(AlarmReceiver.EXTRA_NOTES) ?: ""

        val session = TimetableSession(
          id = sessionId,
          subjectId = 0L,
          subjectName = subjectName,
          topic = topic,
          date = "",
          startTime = startTime,
          endTime = endTime,
          location = location,
          notes = notes,
          isAlarmEnabled = true
        )

        // 1. Acquire WakeLock so CPU doesn't sleep
        acquireWakeLock()

        // 2. Build High Priority Notification with FullScreenIntent
        val notification = buildForegroundNotification(session)
        startForeground(NOTIFICATION_ID, notification)

        // 3. Play Loud Ringtone & Audio Beeps
        startSoundAndVibration()

        // 4. Emit event so in-app UI pops up dialog if app is open
        AlarmEventBus.emit(
          AlarmEvent.Triggered(
            sessionId = sessionId,
            subjectName = subjectName,
            topic = topic,
            startTime = startTime,
            endTime = endTime,
            location = location,
            notes = notes
          )
        )
      }

      ACTION_STOP_RINGING -> {
        stopSoundAndVibration()
        releaseWakeLock()
        stopForeground(true)
        stopSelf()
      }
    }

    return START_NOT_STICKY
  }

  private fun acquireWakeLock() {
    try {
      if (wakeLock == null) {
        val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StudyPilot:AlarmWakeLock")
        wakeLock?.acquire(2 * 60 * 1000L) // 2 minutes auto-timeout
      }
    } catch (e: Exception) {
      // Ignore
    }
  }

  private fun releaseWakeLock() {
    try {
      if (wakeLock?.isHeld == true) {
        wakeLock?.release()
      }
      wakeLock = null
    } catch (e: Exception) {
      // Ignore
    }
  }

  private fun startSoundAndVibration() {
    AlarmSoundManager.startSoundAndVibration(applicationContext)
  }

  private fun stopSoundAndVibration() {
    AlarmSoundManager.stopSoundAndVibration(applicationContext)
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      val channel = NotificationChannel(
        CHANNEL_ID,
        "StudyPilot Alarm Foreground Service",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Urgent high-priority alarms for scheduled classes and study sessions"
        enableVibration(true)
        setSound(
          RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
          AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        )
      }
      notificationManager?.createNotificationChannel(channel)
    }
  }

  private fun buildForegroundNotification(session: TimetableSession): android.app.Notification {
    val showIntent = Intent(applicationContext, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("extra_ringing_session_id", session.id)
    }
    val showPendingIntent = PendingIntent.getActivity(
      applicationContext,
      (session.id + 70000).toInt(),
      showIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val dismissIntent = Intent(applicationContext, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_DISMISS_ALARM
      putExtra(AlarmReceiver.EXTRA_SESSION_ID, session.id)
    }
    val dismissPendingIntent = PendingIntent.getBroadcast(
      applicationContext,
      (session.id + 80000).toInt(),
      dismissIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val snoozeIntent = Intent(applicationContext, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_SNOOZE_ALARM
      putExtra(AlarmReceiver.EXTRA_SESSION_ID, session.id)
      putExtra(AlarmReceiver.EXTRA_SUBJECT_NAME, session.subjectName)
      putExtra(AlarmReceiver.EXTRA_TOPIC, session.topic)
      putExtra(AlarmReceiver.EXTRA_START_TIME, session.startTime)
    }
    val snoozePendingIntent = PendingIntent.getBroadcast(
      applicationContext,
      (session.id + 90000).toInt(),
      snoozeIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("⏰ ALARM: ${session.subjectName}")
      .setContentText("${session.topic} (${session.startTime})")
      .setStyle(NotificationCompat.BigTextStyle().bigText(
        "Scheduled Event: ${session.topic}\nTime: ${session.startTime} - ${session.endTime}\nLocation: ${session.location.ifBlank { "Mobile" }}"
      ))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setOngoing(true)
      .setAutoCancel(true)
      .setFullScreenIntent(showPendingIntent, true)
      .setContentIntent(showPendingIntent)
      .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss Alarm", dismissPendingIntent)
      .addAction(android.R.drawable.ic_popup_reminder, "Snooze 5 Min", snoozePendingIntent)
      .build()
  }

  override fun onDestroy() {
    stopSoundAndVibration()
    releaseWakeLock()
    super.onDestroy()
  }
}
