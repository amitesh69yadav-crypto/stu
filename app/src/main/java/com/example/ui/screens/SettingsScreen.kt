package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.RadioButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.Subject
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PaletteCreamIvory
import com.example.ui.theme.PaletteDeepSage
import com.example.ui.theme.PaletteMediumSage
import com.example.ui.theme.PalettePaleCeladon
import com.example.ui.viewmodel.StudyPilotViewModel

@Composable
fun SettingsScreen(
  viewModel: StudyPilotViewModel
) {
  val subjects by viewModel.subjects.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val savedAccounts by viewModel.savedAccounts.collectAsState()

  val customGeminiKey by viewModel.customGeminiKey.collectAsState()
  val customGroqKey by viewModel.customGroqKey.collectAsState()
  val customOpenAIKey by viewModel.customOpenAIKey.collectAsState()
  val customDeepSeekKey by viewModel.customDeepSeekKey.collectAsState()
  val customClaudeKey by viewModel.customClaudeKey.collectAsState()
  val customOpenRouterKey by viewModel.customOpenRouterKey.collectAsState()
  val aiProviderMode by viewModel.aiProviderMode.collectAsState()
  val apiTestStatus by viewModel.apiTestStatus.collectAsState()

  var inputGeminiKey by remember(customGeminiKey) { mutableStateOf(customGeminiKey) }
  var inputGroqKey by remember(customGroqKey) { mutableStateOf(customGroqKey) }
  var inputOpenAIKey by remember(customOpenAIKey) { mutableStateOf(customOpenAIKey) }
  var inputDeepSeekKey by remember(customDeepSeekKey) { mutableStateOf(customDeepSeekKey) }
  var inputClaudeKey by remember(customClaudeKey) { mutableStateOf(customClaudeKey) }
  var inputOpenRouterKey by remember(customOpenRouterKey) { mutableStateOf(customOpenRouterKey) }

  var showGeminiKeyPassword by remember { mutableStateOf(false) }
  var showGroqKeyPassword by remember { mutableStateOf(false) }
  var showOpenAIKeyPassword by remember { mutableStateOf(false) }
  var showDeepSeekKeyPassword by remember { mutableStateOf(false) }
  var showClaudeKeyPassword by remember { mutableStateOf(false) }
  var showOpenRouterKeyPassword by remember { mutableStateOf(false) }

  var showAddSubjectDialog by remember { mutableStateOf(false) }
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var accountToDelete by remember { mutableStateOf<com.example.data.model.UserProfile?>(null) }
  var showDeleteCurrentAccountDialog by remember { mutableStateOf(false) }

  var editName by remember(currentUser) { mutableStateOf(currentUser.name) }
  var editEmail by remember(currentUser) { mutableStateOf(currentUser.email) }
  var editUniversity by remember(currentUser) { mutableStateOf(currentUser.university) }
  var editRole by remember(currentUser) { mutableStateOf(currentUser.role) }

  val isGeminiConfigured = BuildConfig.GEMINI_API_KEY.isNotBlank() &&
    BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
  ) {
    // 0. Account Profile & Device Sync Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Top Row: Avatar + Student Details + Active Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(PaletteDeepSage),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = currentUser.avatarInitials,
                  fontWeight = FontWeight.ExtraBold,
                  color = PaletteCreamIvory,
                  fontSize = 16.sp
                )
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = currentUser.name,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = currentUser.email,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${currentUser.university} • ${currentUser.studentId}",
                  style = MaterialTheme.typography.labelSmall,
                  color = PaletteDeepSage,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = EmeraldSuccess.copy(alpha = 0.12f),
              border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(EmeraldSuccess)
                )
                Text(
                  text = "Active",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldSuccess,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Account Action Buttons: Evenly weighted & styled
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            FilledTonalButton(
              onClick = {
                editName = currentUser.name
                editEmail = currentUser.email
                editUniversity = currentUser.university
                editRole = currentUser.role
                showEditProfileDialog = true
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = PalettePaleCeladon,
                contentColor = PaletteDeepSage
              ),
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Edit Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { viewModel.logout() },
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            FilledTonalButton(
              onClick = { viewModel.syncCloudData() },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = CyanAccent.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.onSurface
              ),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
            ) {
              Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(15.dp), tint = PaletteDeepSage)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Cloud Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { showDeleteCurrentAccountDialog = true },
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
              ),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Delete Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                Icons.Default.Devices,
                contentDescription = null,
                tint = PaletteDeepSage,
                modifier = Modifier.size(20.dp)
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Laptop + Mobile Sync (${currentUser.authProvider})",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Dual responsive mode keeps coursework and alarms in sync.",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 0.5. Student Account Switcher (Strict Per-Student Data Isolation)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Student Profiles (${savedAccounts.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Isolated profiles prevent clash between students",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = "Zero-Clash",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            savedAccounts.forEach { account ->
              val isCurrent = account.id == currentUser.id
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCurrent) PalettePaleCeladon.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isCurrent) PaletteMediumSage else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) PaletteDeepSage else MaterialTheme.colorScheme.outline),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = account.avatarInitials,
                        fontWeight = FontWeight.Bold,
                        color = PaletteCreamIvory,
                        fontSize = 13.sp
                      )
                    }
                    Column {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = account.name,
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrent) {
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f)
                          ) {
                            Text(
                              text = "Active",
                              fontSize = 10.sp,
                              fontWeight = FontWeight.Bold,
                              color = EmeraldSuccess,
                              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                          }
                        }
                      }
                      Text(
                        text = "${account.university} • ${account.studentId}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    if (!isCurrent) {
                      FilledTonalButton(
                        onClick = { viewModel.switchAccount(account) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                      ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      }
                    }
                    IconButton(
                      onClick = { accountToDelete = account },
                      modifier = Modifier.size(34.dp)
                    ) {
                      Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Profile",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = { viewModel.logout() },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(40.dp)
          ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Register / Sign In Another Student", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    // 1. AI Provider Selection & Custom API Key Configuration
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Header Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  "AI Model & API Key Settings",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleMedium
                )
                Text(
                  text = viewModel.repository.geminiService.getActiveProviderDescription(),
                  style = MaterialTheme.typography.bodySmall,
                  color = PaletteDeepSage,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldSuccess.copy(alpha = 0.15f)
            ) {
              Text(
                text = "Configurable",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = EmeraldSuccess,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Provider Mode Selection Header
          Text(
            text = "Preferred AI Provider",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))

          // Multi-Provider Selection Options
          val modes = listOf(
            "AUTO" to "Auto (Multi-Engine Fallback Chain)",
            "GEMINI" to "Google Gemini 2.5 Flash",
            "GROQ" to "Groq AI (GPT OSS 120B / Llama 3.3)",
            "OPENAI" to "OpenAI ChatGPT (GPT-4o / GPT-4o-mini)",
            "DEEPSEEK" to "DeepSeek AI (Chat / Coder / R1)",
            "CLAUDE" to "Anthropic Claude (3.5 Sonnet / Haiku)",
            "OPENROUTER" to "OpenRouter AI (Unified Multi-Model)",
            "LOCAL" to "Local Deterministic Mode (Offline)"
          )

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            modes.forEach { (modeKey, modeTitle) ->
              val isSelected = aiProviderMode.equals(modeKey, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) PalettePaleCeladon else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) PaletteMediumSage else Color.Transparent
                ),
                onClick = { viewModel.setAiProviderMode(modeKey) },
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                      selected = isSelected,
                      onClick = { viewModel.setAiProviderMode(modeKey) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = modeTitle,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                  if (isSelected) {
                    Icon(
                      Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = PaletteDeepSage,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "API Key Configuration",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))

          // --- 1. Google Gemini Key ---
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PalettePaleCeladon.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(Icons.Default.Key, contentDescription = null, tint = PaletteDeepSage, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Google Gemini API Key", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (customGeminiKey.isNotBlank()) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                  Text(
                    text = if (customGeminiKey.isNotBlank()) "Custom Active" else "Default Active",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (customGeminiKey.isNotBlank()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = inputGeminiKey,
                onValueChange = { inputGeminiKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("AIzaSy...", fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (showGeminiKeyPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  IconButton(onClick = { showGeminiKeyPassword = !showGeminiKeyPassword }) {
                    Icon(if (showGeminiKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle key", modifier = Modifier.size(18.dp))
                  }
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { viewModel.saveCustomGeminiKey(inputGeminiKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Save Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                  onClick = { viewModel.testApiConnection("GEMINI", inputGeminiKey.ifBlank { viewModel.repository.geminiService.getGeminiApiKey() }) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Test Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (customGeminiKey.isNotBlank()) {
                  TextButton(
                    onClick = {
                      inputGeminiKey = ""
                      viewModel.saveCustomGeminiKey("")
                    },
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // --- 2. Groq AI Key ---
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PalettePaleCeladon.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(Icons.Default.Key, contentDescription = null, tint = PaletteDeepSage, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Groq AI API Key", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = EmeraldSuccess.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = if (customGroqKey.isNotBlank()) "Custom Active" else "Default Active",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = inputGroqKey,
                onValueChange = { inputGroqKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("gsk_...", fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (showGroqKeyPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  IconButton(onClick = { showGroqKeyPassword = !showGroqKeyPassword }) {
                    Icon(if (showGroqKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle key", modifier = Modifier.size(18.dp))
                  }
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { viewModel.saveCustomGroqKey(inputGroqKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Save Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                  onClick = { viewModel.testApiConnection("GROQ", inputGroqKey.ifBlank { viewModel.repository.geminiService.getGroqApiKey() }) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Test Groq", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (customGroqKey.isNotBlank()) {
                  TextButton(
                    onClick = {
                      inputGroqKey = ""
                      viewModel.saveCustomGroqKey("")
                    },
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // --- 3. OpenAI ChatGPT Key ---
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PalettePaleCeladon.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(Icons.Default.Key, contentDescription = null, tint = PaletteDeepSage, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("OpenAI (ChatGPT) Key", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (customOpenAIKey.isNotBlank()) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                  Text(
                    text = if (customOpenAIKey.isNotBlank()) "Configured" else "Not Set",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (customOpenAIKey.isNotBlank()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = inputOpenAIKey,
                onValueChange = { inputOpenAIKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("sk-proj-...", fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (showOpenAIKeyPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  IconButton(onClick = { showOpenAIKeyPassword = !showOpenAIKeyPassword }) {
                    Icon(if (showOpenAIKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle key", modifier = Modifier.size(18.dp))
                  }
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { viewModel.saveCustomOpenAIKey(inputOpenAIKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Save Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                  onClick = { viewModel.testApiConnection("OPENAI", inputOpenAIKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Test ChatGPT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (customOpenAIKey.isNotBlank()) {
                  TextButton(
                    onClick = {
                      inputOpenAIKey = ""
                      viewModel.saveCustomOpenAIKey("")
                    },
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // --- 4. DeepSeek Key ---
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PalettePaleCeladon.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(Icons.Default.Key, contentDescription = null, tint = PaletteDeepSage, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("DeepSeek AI Key", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (customDeepSeekKey.isNotBlank()) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                  Text(
                    text = if (customDeepSeekKey.isNotBlank()) "Configured" else "Not Set",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (customDeepSeekKey.isNotBlank()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = inputDeepSeekKey,
                onValueChange = { inputDeepSeekKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("sk-...", fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (showDeepSeekKeyPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  IconButton(onClick = { showDeepSeekKeyPassword = !showDeepSeekKeyPassword }) {
                    Icon(if (showDeepSeekKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle key", modifier = Modifier.size(18.dp))
                  }
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { viewModel.saveCustomDeepSeekKey(inputDeepSeekKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Save Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                  onClick = { viewModel.testApiConnection("DEEPSEEK", inputDeepSeekKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Test DeepSeek", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (customDeepSeekKey.isNotBlank()) {
                  TextButton(
                    onClick = {
                      inputDeepSeekKey = ""
                      viewModel.saveCustomDeepSeekKey("")
                    },
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // --- 5. Anthropic Claude Key ---
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PalettePaleCeladon.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(Icons.Default.Key, contentDescription = null, tint = PaletteDeepSage, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Anthropic Claude Key", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (customClaudeKey.isNotBlank()) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                  Text(
                    text = if (customClaudeKey.isNotBlank()) "Configured" else "Not Set",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (customClaudeKey.isNotBlank()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = inputClaudeKey,
                onValueChange = { inputClaudeKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("sk-ant-...", fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (showClaudeKeyPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  IconButton(onClick = { showClaudeKeyPassword = !showClaudeKeyPassword }) {
                    Icon(if (showClaudeKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle key", modifier = Modifier.size(18.dp))
                  }
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { viewModel.saveCustomClaudeKey(inputClaudeKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Save Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                  onClick = { viewModel.testApiConnection("CLAUDE", inputClaudeKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Test Claude", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (customClaudeKey.isNotBlank()) {
                  TextButton(
                    onClick = {
                      inputClaudeKey = ""
                      viewModel.saveCustomClaudeKey("")
                    },
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // --- 6. OpenRouter Key ---
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PalettePaleCeladon.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMediumSage.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(Icons.Default.Key, contentDescription = null, tint = PaletteDeepSage, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("OpenRouter Key", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (customOpenRouterKey.isNotBlank()) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                  Text(
                    text = if (customOpenRouterKey.isNotBlank()) "Configured" else "Not Set",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (customOpenRouterKey.isNotBlank()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                value = inputOpenRouterKey,
                onValueChange = { inputOpenRouterKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("sk-or-...", fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (showOpenRouterKeyPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                  IconButton(onClick = { showOpenRouterKeyPassword = !showOpenRouterKeyPassword }) {
                    Icon(if (showOpenRouterKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle key", modifier = Modifier.size(18.dp))
                  }
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { viewModel.saveCustomOpenRouterKey(inputOpenRouterKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Save Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                  onClick = { viewModel.testApiConnection("OPENROUTER", inputOpenRouterKey) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f).height(36.dp)
                ) {
                  Text("Test OpenRouter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (customOpenRouterKey.isNotBlank()) {
                  TextButton(
                    onClick = {
                      inputOpenRouterKey = ""
                      viewModel.saveCustomOpenRouterKey("")
                    },
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                  }
                }
              }
            }
          }

          // Live API Test Feedback Message
          apiTestStatus?.let { status ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (status.contains("successful") || status.contains("verified")) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (status.contains("successful") || status.contains("verified")) EmeraldSuccess else MaterialTheme.colorScheme.error),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = status,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.weight(1f)
                )
                IconButton(
                  onClick = { viewModel.clearApiTestStatus() },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Close status", modifier = Modifier.size(14.dp))
                }
              }
            }
          }
        }
      }
    }

    // 2. Subject & Course Manager
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Enrolled Subjects (${subjects.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Button(
              onClick = { showAddSubjectDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.height(36.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Subject", fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          subjects.forEach { sub ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primaryContainer,
                  modifier = Modifier.width(82.dp)
                ) {
                  Text(
                    text = sub.code,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = sub.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
              }

              IconButton(onClick = { viewModel.deleteSubject(sub) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }

    // 3. Data Persistence & Sample Data
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Local Database Persistence", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "All schedules, study topics, quiz attempts, and spaced repetition intervals are stored in on-device SQLite via Room. Your progress is completely preserved across sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))
          OutlinedButton(
            onClick = { viewModel.seedSampleData() },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
          ) {
            Text("Reload Sample University Courses & Topics", fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }

  if (showAddSubjectDialog) {
    AddSubjectDialog(
      onDismiss = { showAddSubjectDialog = false },
      onConfirm = { name, code ->
        viewModel.addSubject(name, code, "#2563EB")
        showAddSubjectDialog = false
      }
    )
  }

  if (showEditProfileDialog) {
    AlertDialog(
      onDismissRequest = { showEditProfileDialog = false },
      title = { Text("Edit Academic Profile", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Full Name") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          OutlinedTextField(
            value = editEmail,
            onValueChange = { editEmail = it },
            label = { Text("Academic Email") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          OutlinedTextField(
            value = editUniversity,
            onValueChange = { editUniversity = it },
            label = { Text("University / Institution") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          OutlinedTextField(
            value = editRole,
            onValueChange = { editRole = it },
            label = { Text("Academic Major / Role") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateProfile(editName, editEmail, editUniversity, editRole)
            showEditProfileDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PaletteDeepSage),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Save Profile")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditProfileDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showDeleteCurrentAccountDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteCurrentAccountDialog = false },
      title = { Text("Delete Current Account", fontWeight = FontWeight.Bold) },
      text = {
        Text("Are you sure you want to permanently delete the profile for ${currentUser.name} (${currentUser.email})? All local coursework, study records, timetable sessions, and cloud synced data for this student will be permanently removed.")
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteCurrentAccountDialog = false
            viewModel.deleteUser(currentUser.id)
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Delete Account", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteCurrentAccountDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  accountToDelete?.let { target ->
    AlertDialog(
      onDismissRequest = { accountToDelete = null },
      title = { Text("Delete Student Profile", fontWeight = FontWeight.Bold) },
      text = {
        Text("Are you sure you want to delete the profile for ${target.name} (${target.email} • ${target.studentId})? All courses, study tasks, and sessions for this student will be removed.")
      },
      confirmButton = {
        Button(
          onClick = {
            val id = target.id
            accountToDelete = null
            viewModel.deleteUser(id)
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Delete Profile", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { accountToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun AddSubjectDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var code by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Subject / Course", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = code,
          onValueChange = { code = it },
          label = { Text("Course Code (e.g. CS201, BIO101)") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Course Name (e.g. Operating Systems)") },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && code.isNotBlank()) {
            onConfirm(name, code)
          }
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Add Subject")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
