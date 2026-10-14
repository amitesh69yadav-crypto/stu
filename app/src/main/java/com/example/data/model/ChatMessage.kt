package com.example.data.model

data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val role: String, // "user" or "model"
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val modelName: String = "gemini-2.5-flash",
  val searchGroundingUsed: Boolean = false,
  val groundingSources: List<GroundingSource> = emptyList(),
  val searchQueries: List<String> = emptyList(),
  val isError: Boolean = false
)

data class GroundingSource(
  val title: String,
  val url: String
)

enum class TutorModel(
  val modelId: String,
  val displayName: String,
  val description: String,
  val iconBadge: String
) {
  FLASH_LITE(
    modelId = "gemini-2.0-flash-lite",
    displayName = "Flash-Lite",
    description = "Ultra-fast quick definitions & answers",
    iconBadge = "⚡ Fast"
  ),
  FLASH(
    modelId = "gemini-2.5-flash",
    displayName = "Flash 2.5 (Default)",
    description = "Balanced academic tutor + Google Search grounding",
    iconBadge = "🎯 Balanced"
  ),
  PRO(
    modelId = "gemini-2.5-pro",
    displayName = "Pro 2.5",
    description = "Deep mathematical proofs, coding & complex reasoning",
    iconBadge = "🧠 Complex"
  )
}
