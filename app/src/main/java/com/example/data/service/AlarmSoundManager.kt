package com.example.data.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object AlarmSoundManager {
  private var activeRingtone: Ringtone? = null
  private var toneGenerator: ToneGenerator? = null
  private var toneJob: Job? = null
  private var activeVibrator: Vibrator? = null
  private val soundScope = CoroutineScope(Dispatchers.Default)

  @Synchronized
  fun startSoundAndVibration(context: Context) {
    // 1. Immediately halt any previously active audio/vibration
    stopSoundAndVibration(context)

    // 2. Start repeating vibration pattern
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        activeVibrator = vibratorManager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        activeVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        activeVibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 300, 600, 300, 600), 0))
      } else {
        @Suppress("DEPRECATION")
        activeVibrator?.vibrate(longArrayOf(0, 600, 300, 600, 300, 600), 0)
      }
    } catch (e: Exception) {
      // Ignore vibration failures
    }

    // 3. Start default system alarm ringtone
    try {
      val alertUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
      activeRingtone = RingtoneManager.getRingtone(context.applicationContext, alertUri)?.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        }
        play()
      }
    } catch (e: Exception) {
      // Ignore ringtone load failure
    }

    // 4. Start fallback tone generator beeps
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
      toneJob = soundScope.launch {
        while (isActive) {
          try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
            delay(650)
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
            delay(750)
          } catch (e: Exception) {
            break
          }
        }
      }
    } catch (e: Exception) {
      // Ignore
    }
  }

  @Synchronized
  fun stopSoundAndVibration(context: Context? = null) {
    // Stop Ringtone
    try {
      activeRingtone?.stop()
      activeRingtone = null
    } catch (e: Exception) {
      // Ignore
    }

    // Cancel Tone Generator Loop
    try {
      toneJob?.cancel()
      toneJob = null
      toneGenerator?.stopTone()
      toneGenerator?.release()
      toneGenerator = null
    } catch (e: Exception) {
      // Ignore
    }

    // Cancel Vibrator
    try {
      activeVibrator?.cancel()
      activeVibrator = null
    } catch (e: Exception) {
      // Ignore
    }

    // Context fallback for system vibration & notification cancellation
    context?.let { ctx ->
      try {
        @Suppress("DEPRECATION")
        val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.cancel()
      } catch (e: Exception) {
        // Ignore
      }
      try {
        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancelAll()
      } catch (e: Exception) {
        // Ignore
      }
    }
  }
}
