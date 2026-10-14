package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PaletteCreamIvory
import com.example.ui.theme.PaletteDarkText
import com.example.ui.theme.PaletteDeepSage
import com.example.ui.theme.PaletteMediumSage
import com.example.ui.theme.PalettePaleCeladon
import com.example.ui.theme.PaletteMintFoam
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.VolumeUp

@Composable
fun AcademicHeader(
  title: String,
  subtitle: String? = null,
  modifier: Modifier = Modifier,
  action: @Composable (() -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.5).sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )
      if (!subtitle.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
    if (action != null) {
      Spacer(modifier = Modifier.width(12.dp))
      Box(contentAlignment = Alignment.CenterEnd) {
        action()
      }
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )
    if (!actionText.isNullOrBlank() && onActionClick != null) {
      Text(
        text = actionText,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable { onActionClick() }
          .padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector,
  iconTint: Color = MaterialTheme.colorScheme.primary,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.defaultMinSize(minHeight = 92.dp),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(iconTint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconTint,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = title,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
        if (!subtitle.isNullOrBlank()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = iconTint,
            maxLines = 1
          )
        } else {
          Spacer(modifier = Modifier.height(14.dp))
        }
      }
    }
  }
}

@Composable
fun MasteryBadge(
  level: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, label) = when (level.uppercase()) {
    "MASTERED" -> Triple(EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess, "Mastered")
    "FAMILIAR" -> Triple(CyanAccent.copy(alpha = 0.15f), CyanAccent, "Familiar")
    "STRUGGLING" -> Triple(RoseError.copy(alpha = 0.15f), RoseError, "Needs Review")
    else -> Triple(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), MaterialTheme.colorScheme.onSurfaceVariant, "Untested")
  }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    color = bgColor
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
      color = textColor,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun EmptyState(
  icon: ImageVector,
  title: String,
  description: String,
  actionButton: @Composable (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp)
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 16.dp)
    )
    if (actionButton != null) {
      Spacer(modifier = Modifier.height(16.dp))
      actionButton()
    }
  }
}

@Composable
fun MobileClockAlarmCard(
  activeAlarmsCount: Int,
  onQuickAlarmClick: () -> Unit,
  onTestAlarmSoundClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentTimeText by remember { mutableStateOf("") }
  var timeZoneText by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    val timeFormat = java.text.SimpleDateFormat("HH:mm:ss z", java.util.Locale.getDefault())
    val tz = java.util.TimeZone.getDefault()
    timeZoneText = "${tz.id} (${tz.getDisplayName(false, java.util.TimeZone.SHORT)})"
    while (true) {
      currentTimeText = timeFormat.format(java.util.Date())
      kotlinx.coroutines.delay(1000)
    }
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = PaletteCreamIvory
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(PaletteDeepSage),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = "Mobile Clock",
              tint = PaletteCreamIvory,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "MOBILE DEVICE CLOCK & ALARM",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
              ),
              color = PaletteDarkText
            )
            Text(
              text = currentTimeText.ifBlank { "Syncing local time..." },
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              ),
              color = PaletteDarkText
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = PalettePaleCeladon
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AlarmOn,
              contentDescription = "Active Alarms",
              tint = PaletteDarkText,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$activeAlarmsCount Active",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = PaletteDarkText
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "📍 Timezone: $timeZoneText • System Alarm Clock Sync Active",
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = PaletteDarkText.copy(alpha = 0.8f)
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        androidx.compose.material3.Button(
          onClick = onQuickAlarmClick,
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = PaletteDeepSage,
            contentColor = PaletteCreamIvory
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AlarmAdd,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Set Quick Alarm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        androidx.compose.material3.OutlinedButton(
          onClick = onTestAlarmSoundClick,
          colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = PaletteDarkText
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = null,
            tint = PaletteDarkText,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Test Sound Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun QuickMobileAlarmDialog(
  onDismiss: () -> Unit,
  onConfirm: (subjectName: String, topic: String, delayMinutes: Int, customTime: String?) -> Unit
) {
  var subjectName by remember { mutableStateOf("Quick Study") }
  var topic by remember { mutableStateOf("Mobile Study Session") }
  var selectedPresetMinutes by remember { mutableStateOf(1) }
  var isCustomTime by remember { mutableStateOf(false) }
  var customTimeInput by remember { mutableStateOf("") }

  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(PaletteDeepSage),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AlarmAdd,
            contentDescription = "Set Quick Alarm",
            tint = PaletteCreamIvory,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Set Mobile Device Alarm",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = PaletteDarkText
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Select when your alarm should ring based on your mobile device clock.",
          style = MaterialTheme.typography.bodySmall,
          color = PaletteDarkText
        )

        Text(
          text = "Quick Offset Preset:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = PaletteDarkText
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(1 to "+1 Min", 2 to "+2 Min", 5 to "+5 Min", 10 to "+10 Min", 30 to "+30 Min").forEach { (mins, label) ->
            val selected = !isCustomTime && selectedPresetMinutes == mins
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selected) PaletteDeepSage else PalettePaleCeladon,
              border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage),
              modifier = Modifier
                .weight(1f)
                .clickable {
                  isCustomTime = false
                  selectedPresetMinutes = mins
                }
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) PaletteCreamIvory else PaletteDarkText,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isCustomTime = !isCustomTime }
        ) {
          androidx.compose.material3.RadioButton(
            selected = isCustomTime,
            onClick = { isCustomTime = true }
          )
          Text(
            text = "Or Set Exact Mobile Time (e.g. 16:30 or 4:30 PM)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PaletteDarkText
          )
        }

        if (isCustomTime) {
          androidx.compose.material3.OutlinedTextField(
            value = customTimeInput,
            onValueChange = { customTimeInput = it },
            placeholder = { Text("e.g. 16:45 or 05:00 PM") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        androidx.compose.material3.OutlinedTextField(
          value = subjectName,
          onValueChange = { subjectName = it },
          label = { Text("Subject / Class Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        androidx.compose.material3.OutlinedTextField(
          value = topic,
          onValueChange = { topic = it },
          label = { Text("Session Topic / Note") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      androidx.compose.material3.Button(
        onClick = {
          onConfirm(
            subjectName,
            topic,
            selectedPresetMinutes,
            if (isCustomTime) customTimeInput else null
          )
          onDismiss()
        },
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
          containerColor = PaletteDeepSage,
          contentColor = PaletteCreamIvory
        )
      ) {
        Text("Set Alarm Now", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      androidx.compose.material3.TextButton(onClick = onDismiss) {
        Text("Cancel", color = PaletteDarkText)
      }
    }
  )
}
