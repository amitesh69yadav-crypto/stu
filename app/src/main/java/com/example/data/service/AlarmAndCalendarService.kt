package com.example.data.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.CalendarContract
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.TimetableSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CurrentScheduleStatus(
  val state: ScheduleState,
  val activeSession: TimetableSession? = null,
  val nextSession: TimetableSession? = null,
  val minutesRemaining: Int = 0,
  val minutesUntilNext: Int = 0,
  val progressFraction: Float = 0f,
  val formattedStatusText: String = "",
  val suggestionText: String = ""
)

enum class ScheduleState {
  ACTIVE_NOW,
  UPCOMING_NEXT,
  FREE_TIME,
  DAY_COMPLETE
}

class AlarmAndCalendarService(private val context: Context) {

  private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  private val channelId = "studypilot_alarms_channel"

  private var activeRingtone: Ringtone? = null

  init {
    createNotificationChannel()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      val channel = NotificationChannel(
        channelId,
        "StudyPilot Study Alarms",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Urgent reminders for scheduled study sessions and lectures"
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)
      }
      notificationManager?.createNotificationChannel(channel)
    }
  }

  // --- Real-Time "What Should I Do Now" Calculation ---
  fun evaluateCurrentStatus(sessions: List<TimetableSession>, currentTime: Date = Date()): CurrentScheduleStatus {
    val todayStr = dateFormat.format(currentTime)
    val todaySessions = sessions.filter { it.date == todayStr }
      .sortedBy { it.startTime }

    if (todaySessions.isEmpty()) {
      return CurrentScheduleStatus(
        state = ScheduleState.FREE_TIME,
        formattedStatusText = "No classes scheduled for today",
        suggestionText = "Tap 'Plan Today' to create your daily timetable or start a self-directed study sprint with the AI Tutor."
      )
    }

    val cal = Calendar.getInstance().apply { time = currentTime }
    val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

    // Check for currently active session
    for (session in todaySessions) {
      val (startH, startM) = parseTime(session.startTime)
      val (endH, endM) = parseTime(session.endTime)
      val startTotal = startH * 60 + startM
      val endTotal = endH * 60 + endM

      if (currentMinutes in startTotal until endTotal) {
        val remaining = endTotal - currentMinutes
        val elapsed = currentMinutes - startTotal
        val totalDuration = (endTotal - startTotal).coerceAtLeast(1)
        val progress = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)

        return CurrentScheduleStatus(
          state = ScheduleState.ACTIVE_NOW,
          activeSession = session,
          minutesRemaining = remaining,
          progressFraction = progress,
          formattedStatusText = "${session.subjectName}: ${session.topic}",
          suggestionText = "Currently in progress ($remaining min left). Stay focused or ask AI Tutor for rapid concept explanations!"
        )
      }
    }

    // Check for upcoming next session today
    val nextSession = todaySessions.firstOrNull { session ->
      val (startH, startM) = parseTime(session.startTime)
      val startTotal = startH * 60 + startM
      startTotal > currentMinutes
    }

    if (nextSession != null) {
      val (startH, startM) = parseTime(nextSession.startTime)
      val startTotal = startH * 60 + startM
      val untilNext = startTotal - currentMinutes

      return CurrentScheduleStatus(
        state = ScheduleState.UPCOMING_NEXT,
        nextSession = nextSession,
        minutesUntilNext = untilNext,
        formattedStatusText = "Next up at ${nextSession.startTime}: ${nextSession.subjectName}",
        suggestionText = if (untilNext <= 15) {
          "Starts in $untilNext minutes! Prepare your notes, room: ${nextSession.location.ifBlank { "Campus" }}."
        } else {
          "$untilNext minutes of free time available. Great window for a quick 10-minute quiz or reviewing weak topics."
        }
      )
    }

    return CurrentScheduleStatus(
      state = ScheduleState.DAY_COMPLETE,
      formattedStatusText = "All scheduled sessions for today are finished!",
      suggestionText = "Great effort today! Take a break, review flash topics in analytics, or plan tomorrow's schedule."
    )
  }

  private fun parseTime(timeStr: String): Pair<Int, Int> {
    return try {
      val trimmed = timeStr.trim().uppercase(Locale.getDefault())
      val isPm = trimmed.contains("PM")
      val isAm = trimmed.contains("AM")
      val cleanTime = trimmed.replace("AM", "").replace("PM", "").trim()
      val delimiter = if (cleanTime.contains(":")) ":" else if (cleanTime.contains(".")) "." else " "
      val parts = cleanTime.split(delimiter).filter { it.isNotBlank() }
      var hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
      val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
      if (isPm && hour < 12) hour += 12
      if (isAm && hour == 12) hour = 0
      Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    } catch (e: Exception) {
      Pair(9, 0)
    }
  }

  // --- Alarm Scheduling with Android AlarmManager ---
  private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
  private var toneJob: kotlinx.coroutines.Job? = null
  private var toneGenerator: android.media.ToneGenerator? = null

  fun computeSessionTriggerTimeMillis(session: TimetableSession): Long {
    return try {
      val (startH, startM) = parseTime(session.startTime)
      val localZone = TimeZone.getDefault()
      val cal = Calendar.getInstance(localZone)
      val dateParts = session.date.trim().split("-")
      
      if (dateParts.size == 3) {
        val year = dateParts[0].toIntOrNull() ?: cal.get(Calendar.YEAR)
        val month = (dateParts[1].toIntOrNull() ?: 1) - 1
        val day = dateParts[2].toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
        cal.set(year, month, day, startH, startM, 0)
      } else {
        cal.set(Calendar.HOUR_OF_DAY, startH)
        cal.set(Calendar.MINUTE, startM)
        cal.set(Calendar.SECOND, 0)
      }
      cal.set(Calendar.MILLISECOND, 0)

      // Ring exactly when event is starting (exactStartMs) - no early reminders and not when ending
      val exactStartMs = cal.timeInMillis
      var triggerTime = exactStartMs

      val now = System.currentTimeMillis()
      // Only if date was blank (undated recurring) and exactStartMs has already passed for today, roll forward to tomorrow's instance
      if (triggerTime <= now && session.date.trim().isEmpty()) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
        triggerTime = cal.timeInMillis
      }
      triggerTime
    } catch (e: Exception) {
      System.currentTimeMillis() + 60000L
    }
  }

  fun scheduleSessionAlarm(session: TimetableSession) {
    if (!session.isAlarmEnabled) return
    val triggerTime = computeSessionTriggerTimeMillis(session)
    val now = System.currentTimeMillis()
    if (triggerTime <= now) return

    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_TRIGGER_STUDY_ALARM
      putExtra(AlarmReceiver.EXTRA_SESSION_ID, session.id)
      putExtra(AlarmReceiver.EXTRA_SUBJECT_NAME, session.subjectName)
      putExtra(AlarmReceiver.EXTRA_TOPIC, session.topic)
      putExtra(AlarmReceiver.EXTRA_START_TIME, session.startTime)
      putExtra(AlarmReceiver.EXTRA_END_TIME, session.endTime)
      putExtra(AlarmReceiver.EXTRA_LOCATION, session.location)
      putExtra(AlarmReceiver.EXTRA_NOTES, session.notes)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      session.id.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val showIntent = Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
          putExtra("extra_ringing_session_id", session.id)
        }
        val showPendingIntent = PendingIntent.getActivity(
          context,
          (session.id + 50000).toInt(),
          showIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        alarmManager?.setAlarmClock(alarmClockInfo, pendingIntent)
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      } else {
        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      }
    } catch (e: Exception) {
      try {
        alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      } catch (ex: Exception) {
        alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      }
    }
  }

  fun cancelSessionAlarm(session: TimetableSession) {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_TRIGGER_STUDY_ALARM
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      session.id.toInt(),
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager?.cancel(pendingIntent)
      pendingIntent.cancel()
    }
  }

  fun scheduleTestAlarm(session: TimetableSession, delaySeconds: Int = 5) {
    val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L)
    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_TRIGGER_STUDY_ALARM
      putExtra(AlarmReceiver.EXTRA_SESSION_ID, session.id)
      putExtra(AlarmReceiver.EXTRA_SUBJECT_NAME, session.subjectName)
      putExtra(AlarmReceiver.EXTRA_TOPIC, session.topic)
      putExtra(AlarmReceiver.EXTRA_START_TIME, session.startTime)
      putExtra(AlarmReceiver.EXTRA_END_TIME, session.endTime)
      putExtra(AlarmReceiver.EXTRA_LOCATION, session.location)
      putExtra(AlarmReceiver.EXTRA_NOTES, session.notes)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      session.id.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      } else {
        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      }
    } catch (e: Exception) {
      alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }
  }

  fun scheduleSnoozeAlarm(sessionId: Long, subjectName: String, topic: String, startTime: String, minutes: Int = 5) {
    val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
    val intent = Intent(context, AlarmReceiver::class.java).apply {
      action = AlarmReceiver.ACTION_TRIGGER_STUDY_ALARM
      putExtra(AlarmReceiver.EXTRA_SESSION_ID, sessionId)
      putExtra(AlarmReceiver.EXTRA_SUBJECT_NAME, subjectName)
      putExtra(AlarmReceiver.EXTRA_TOPIC, topic)
      putExtra(AlarmReceiver.EXTRA_START_TIME, startTime)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      sessionId.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      } else {
        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      }
    } catch (e: Exception) {
      alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }
  }

  // --- Alarm Audio & Vibration Alert ---
  fun triggerAlarmAlert(session: TimetableSession) {
    // 1. Play central alarm sound and vibration
    AlarmSoundManager.startSoundAndVibration(context)

    // 2. Post System Notification
    postAlarmNotification(session)
  }

  fun stopAlarmSound() {
    // 1. Stop Foreground Ringing Service
    AlarmRingingService.stopRinging(context)

    // 2. Stop central sound and vibration synchronously
    AlarmSoundManager.stopSoundAndVibration(context)

    // 3. Cancel notification bar items
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.cancelAll()
    } catch (e: Exception) {
      // Ignore
    }
  }

  private fun postAlarmNotification(session: TimetableSession) {
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("extra_ringing_session_id", session.id)
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        session.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
        action = AlarmReceiver.ACTION_DISMISS_ALARM
        putExtra(AlarmReceiver.EXTRA_SESSION_ID, session.id)
      }
      val dismissPendingIntent = PendingIntent.getBroadcast(
        context,
        (session.id + 10000).toInt(),
        dismissIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
        action = AlarmReceiver.ACTION_SNOOZE_ALARM
        putExtra(AlarmReceiver.EXTRA_SESSION_ID, session.id)
        putExtra(AlarmReceiver.EXTRA_SUBJECT_NAME, session.subjectName)
        putExtra(AlarmReceiver.EXTRA_TOPIC, session.topic)
        putExtra(AlarmReceiver.EXTRA_START_TIME, session.startTime)
      }
      val snoozePendingIntent = PendingIntent.getBroadcast(
        context,
        (session.id + 20000).toInt(),
        snoozeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle("⏰ Event Starting Now: ${session.subjectName}")
        .setContentText("${session.topic} (Started at ${session.startTime})")
        .setStyle(NotificationCompat.BigTextStyle().bigText(
          "Event Starting Now: ${session.topic}\nScheduled: ${session.startTime} - ${session.endTime}\nLocation: ${session.location.ifBlank { "Campus" }}\n${session.notes}"
        ))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setAutoCancel(true)
        .setFullScreenIntent(pendingIntent, true)
        .setContentIntent(pendingIntent)
        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
        .addAction(android.R.drawable.ic_popup_reminder, "Snooze 5m", snoozePendingIntent)
        .addAction(android.R.drawable.ic_media_play, "Start Study", pendingIntent)
        .build()

      notificationManager?.notify(session.id.toInt(), notification)
    } catch (e: Exception) {
      // Ignore
    }
  }

  // --- Google Calendar Sync Helpers ---

  /**
   * Generates a Google Calendar web & app template intent or URL.
   * This guarantees seamless synchronization and opening in Google Calendar.
   */
  fun createGoogleCalendarInsertIntent(session: TimetableSession): Intent {
    val (startH, startM) = parseTime(session.startTime)
    val (endH, endM) = parseTime(session.endTime)

    val dateParts = session.date.split("-")
    val year = dateParts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
    val month = (dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
    val day = dateParts.getOrNull(2)?.toIntOrNull() ?: 1

    val startCal = Calendar.getInstance().apply {
      set(year, month, day, startH, startM, 0)
    }
    val endCal = Calendar.getInstance().apply {
      set(year, month, day, endH, endM, 0)
    }

    return Intent(Intent.ACTION_INSERT).apply {
      data = CalendarContract.Events.CONTENT_URI
      putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.timeInMillis)
      putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.timeInMillis)
      putExtra(CalendarContract.Events.TITLE, "${session.subjectName}: ${session.topic}")
      putExtra(CalendarContract.Events.DESCRIPTION, "StudyPilot Session\nNotes: ${session.notes}")
      putExtra(CalendarContract.Events.EVENT_LOCATION, session.location)
      putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
      putExtra(CalendarContract.Reminders.MINUTES, 0)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }

  /**
   * Generates a Google Calendar URL that directly creates the event in Google Calendar online/app.
   */
  fun getGoogleCalendarWebUrl(session: TimetableSession): String {
    return try {
      val (startH, startM) = parseTime(session.startTime)
      val (endH, endM) = parseTime(session.endTime)

      val dateParts = session.date.split("-")
      val year = dateParts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
      val month = (dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
      val day = dateParts.getOrNull(2)?.toIntOrNull() ?: 1

      val utcFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
      }

      val startCal = Calendar.getInstance().apply {
        set(year, month, day, startH, startM, 0)
      }
      val endCal = Calendar.getInstance().apply {
        set(year, month, day, endH, endM, 0)
      }

      val startStr = utcFormat.format(startCal.time)
      val endStr = utcFormat.format(endCal.time)

      val title = URLEncoder.encode("${session.subjectName}: ${session.topic}", "UTF-8")
      val details = URLEncoder.encode("StudyPilot AI Session\nNotes: ${session.notes}", "UTF-8")
      val location = URLEncoder.encode(session.location, "UTF-8")

      "https://calendar.google.com/calendar/render?action=TEMPLATE&text=$title&dates=$startStr/$endStr&details=$details&location=$location"
    } catch (e: Exception) {
      "https://calendar.google.com/calendar"
    }
  }

  /**
   * Directly inserts event into the local Android Calendar Provider if permission is available.
   */
  fun insertIntoCalendarProvider(session: TimetableSession): Long? {
    return try {
      val (startH, startM) = parseTime(session.startTime)
      val (endH, endM) = parseTime(session.endTime)

      val dateParts = session.date.split("-")
      val year = dateParts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
      val month = (dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
      val day = dateParts.getOrNull(2)?.toIntOrNull() ?: 1

      val startCal = Calendar.getInstance().apply {
        set(year, month, day, startH, startM, 0)
      }
      val endCal = Calendar.getInstance().apply {
        set(year, month, day, endH, endM, 0)
      }

      val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, startCal.timeInMillis)
        put(CalendarContract.Events.DTEND, endCal.timeInMillis)
        put(CalendarContract.Events.TITLE, "${session.subjectName}: ${session.topic}")
        put(CalendarContract.Events.DESCRIPTION, "StudyPilot AI Timetable Session\n${session.notes}")
        put(CalendarContract.Events.CALENDAR_ID, 1)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        put(CalendarContract.Events.EVENT_LOCATION, session.location)
      }

      val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
      uri?.lastPathSegment?.toLongOrNull()
    } catch (e: Exception) {
      null
    }
  }
}
