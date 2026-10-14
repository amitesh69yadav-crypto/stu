package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.GroundingSource
import com.example.data.model.QuizQuestion
import com.example.data.model.StudyTopic
import com.example.data.model.TutorModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatResponse(
  val text: String,
  val modelName: String,
  val groundingSources: List<GroundingSource> = emptyList(),
  val searchQueries: List<String> = emptyList(),
  val isError: Boolean = false
)

data class EvaluationResult(
  val isCorrect: Boolean,
  val feedback: String
)

class GeminiStudyService {

  var customGeminiKey: String = ""
  var customGroqKey: String = ""
  var customOpenAIKey: String = ""
  var customDeepSeekKey: String = ""
  var customClaudeKey: String = ""
  var customOpenRouterKey: String = ""
  var preferredProviderMode: String = "AUTO" // "AUTO", "GEMINI", "GROQ", "OPENAI", "DEEPSEEK", "CLAUDE", "OPENROUTER", "LOCAL"

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(45, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  fun getGeminiApiKey(): String {
    val key = if (customGeminiKey.isNotBlank()) customGeminiKey else BuildConfig.GEMINI_API_KEY
    val sanitized = key.trim().removeSurrounding("\"", "\"")
    return if (sanitized.isNotBlank() && sanitized != "MY_GEMINI_API_KEY") sanitized else ""
  }

  fun getGroqApiKey(): String {
    val buildKey = try { BuildConfig.GROQ_API_KEY } catch (_: Exception) { "" }
    val rawKey = if (customGroqKey.isNotBlank()) customGroqKey else buildKey
    val sanitized = rawKey.trim().removeSurrounding("\"", "\"")
    return if (sanitized.isNotBlank() && sanitized != "MY_GROQ_API_KEY") {
      sanitized
    } else {
      "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"
    }
  }

  fun getOpenAIApiKey(): String = customOpenAIKey.trim().removeSurrounding("\"", "\"")
  fun getDeepSeekApiKey(): String = customDeepSeekKey.trim().removeSurrounding("\"", "\"")
  fun getClaudeApiKey(): String = customClaudeKey.trim().removeSurrounding("\"", "\"")
  fun getOpenRouterApiKey(): String = customOpenRouterKey.trim().removeSurrounding("\"", "\"")

  fun isGeminiConfigured(): Boolean = getGeminiApiKey().isNotBlank()
  fun isGroqConfigured(): Boolean = getGroqApiKey().isNotBlank()
  fun isOpenAIConfigured(): Boolean = getOpenAIApiKey().isNotBlank()
  fun isDeepSeekConfigured(): Boolean = getDeepSeekApiKey().isNotBlank()
  fun isClaudeConfigured(): Boolean = getClaudeApiKey().isNotBlank()
  fun isOpenRouterConfigured(): Boolean = getOpenRouterApiKey().isNotBlank()

  fun getActiveProviderDescription(): String {
    return when (preferredProviderMode.uppercase()) {
      "GEMINI" -> if (isGeminiConfigured()) "Google Gemini 2.5 Flash (Primary)" else "Gemini (Not Configured)"
      "GROQ" -> if (isGroqConfigured()) "Groq AI (GPT OSS 120B / Llama 3.3)" else "Groq (Not Configured)"
      "OPENAI", "CHATGPT" -> if (isOpenAIConfigured()) "OpenAI ChatGPT (GPT-4o)" else "OpenAI (Not Configured)"
      "DEEPSEEK" -> if (isDeepSeekConfigured()) "DeepSeek AI (Chat / Coder)" else "DeepSeek (Not Configured)"
      "CLAUDE" -> if (isClaudeConfigured()) "Anthropic Claude (3.5 Sonnet)" else "Claude (Not Configured)"
      "OPENROUTER" -> if (isOpenRouterConfigured()) "OpenRouter Unified AI" else "OpenRouter (Not Configured)"
      "LOCAL" -> "Offline Academic Mode"
      else -> {
        val active = mutableListOf<String>()
        if (isGeminiConfigured()) active.add("Gemini")
        if (isGroqConfigured()) active.add("Groq")
        if (isOpenAIConfigured()) active.add("ChatGPT")
        if (isDeepSeekConfigured()) active.add("DeepSeek")
        if (isClaudeConfigured()) active.add("Claude")
        if (isOpenRouterConfigured()) active.add("OpenRouter")
        if (active.isNotEmpty()) "Auto Fallback: ${active.joinToString(" → ")}"
        else "Local Deterministic Academic Mode"
      }
    }
  }

  suspend fun testConnection(provider: String, apiKeyToTest: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    val key = apiKeyToTest.trim().removeSurrounding("\"", "\"")
    if (key.isBlank()) return@withContext Pair(false, "API key is empty. Please enter a valid API key.")
    try {
      when (provider.uppercase()) {
        "GEMINI" -> {
          val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
              put(JSONObject().apply {
                put("parts", JSONArray().apply {
                  put(JSONObject().put("text", "Respond in 3 words: Gemini active ready."))
                })
              })
            })
          }
          val modelsToTry = listOf("gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash")
          var lastErr = ""
          for (m in modelsToTry) {
            val request = Request.Builder()
              .url("https://generativelanguage.googleapis.com/v1beta/models/$m:generateContent?key=$key")
              .post(jsonBody.toString().toRequestBody(jsonMediaType))
              .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful && body.contains("candidates")) {
              return@withContext Pair(true, "✅ Gemini API verified ($m)! Connection successful.")
            } else {
              lastErr = "($m HTTP ${response.code}): ${body.take(120)}"
            }
          }
          Pair(false, "❌ Gemini API Error: $lastErr")
        }
        "GROQ" -> {
          val groqModels = listOf("openai/gpt-oss-120b", "openai/gpt-oss-20b", "qwen/qwen3.8-27b", "llama-3.3-70b-versatile")
          var lastErr = ""
          for (gm in groqModels) {
            val res = callOpenAICompatibleApi(
              baseUrl = "https://api.groq.com/openai/v1/chat/completions",
              apiKey = key,
              models = listOf(gm),
              prompt = "Respond in 3 words: Groq active ready."
            )
            if (res != null) {
              return@withContext Pair(true, "✅ Groq AI verified ($gm)! Connection successful.")
            }
          }
          Pair(false, "❌ Groq API Error: Connection failed. Please check key.")
        }
        "OPENAI", "CHATGPT" -> {
          val res = callOpenAICompatibleApi(
            baseUrl = "https://api.openai.com/v1/chat/completions",
            apiKey = key,
            models = listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo"),
            prompt = "Respond in 3 words: OpenAI active ready."
          )
          if (res != null) {
            Pair(true, "✅ OpenAI ChatGPT verified! Connection successful.")
          } else {
            Pair(false, "❌ OpenAI API Error: Verification failed. Check key & quota.")
          }
        }
        "DEEPSEEK" -> {
          val res = callOpenAICompatibleApi(
            baseUrl = "https://api.deepseek.com/chat/completions",
            apiKey = key,
            models = listOf("deepseek-chat", "deepseek-coder"),
            prompt = "Respond in 3 words: DeepSeek active ready."
          )
          if (res != null) {
            Pair(true, "✅ DeepSeek AI verified! Connection successful.")
          } else {
            Pair(false, "❌ DeepSeek API Error: Verification failed. Check key & account balance.")
          }
        }
        "CLAUDE" -> {
          val res = callClaudeApiWithKey(key, "Respond in 3 words: Claude active ready.")
          if (res != null) {
            Pair(true, "✅ Anthropic Claude verified! Connection successful.")
          } else {
            Pair(false, "❌ Anthropic Claude API Error: Verification failed. Check key.")
          }
        }
        "OPENROUTER" -> {
          val res = callOpenAICompatibleApi(
            baseUrl = "https://openrouter.ai/api/v1/chat/completions",
            apiKey = key,
            models = listOf("openrouter/auto", "openai/gpt-4o-mini"),
            prompt = "Respond in 3 words: OpenRouter active ready."
          )
          if (res != null) {
            Pair(true, "✅ OpenRouter AI verified! Connection successful.")
          } else {
            Pair(false, "❌ OpenRouter API Error: Verification failed. Check key.")
          }
        }
        else -> Pair(false, "Unknown provider name: $provider")
      }
    } catch (e: Exception) {
      Pair(false, "❌ Exception connecting to $provider: ${e.message}")
    }
  }

  // --- Universal OpenAI-Compatible REST API Helper ---
  private suspend fun callOpenAICompatibleApi(
    baseUrl: String,
    apiKey: String,
    models: List<String>,
    prompt: String,
    systemInstruction: String = "You are StudyPilot AI, an elite university tutor.",
    temperature: Double = 0.7,
    extraHeaders: Map<String, String> = emptyMap()
  ): String? = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) return@withContext null

    for (m in models) {
      try {
        val jsonBody = JSONObject().apply {
          put("model", m)
          put("temperature", temperature)
          put("messages", JSONArray().apply {
            if (systemInstruction.isNotBlank()) {
              put(JSONObject().apply {
                put("role", "system")
                put("content", systemInstruction)
              })
            }
            put(JSONObject().apply {
              put("role", "user")
              put("content", prompt)
            })
          })
        }

        val reqBuilder = Request.Builder()
          .url(baseUrl)
          .addHeader("Authorization", "Bearer $apiKey")
          .addHeader("Content-Type", "application/json")
        extraHeaders.forEach { (k, v) -> reqBuilder.addHeader(k, v) }

        val request = reqBuilder.post(jsonBody.toString().toRequestBody(jsonMediaType)).build()
        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""
        if (response.isSuccessful) {
          val json = JSONObject(bodyStr)
          val choices = json.optJSONArray("choices")
          val content = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
          if (!content.isNullOrBlank()) {
            Log.i("GeminiStudyService", "OpenAI-style call ($m @ $baseUrl) successful")
            return@withContext content.trim()
          }
        } else {
          Log.w("GeminiStudyService", "API ($m @ $baseUrl) error ${response.code}: $bodyStr")
        }
      } catch (e: Exception) {
        Log.w("GeminiStudyService", "API ($m @ $baseUrl) exception: ${e.message}")
      }
    }
    null
  }

  private suspend fun chatWithOpenAICompatible(
    providerLabel: String,
    baseUrl: String,
    apiKey: String,
    models: List<String>,
    history: List<ChatMessage>,
    systemInstruction: String,
    extraHeaders: Map<String, String> = emptyMap()
  ): ChatResponse? = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) return@withContext null

    val messagesArray = JSONArray().apply {
      if (systemInstruction.isNotBlank()) {
        put(JSONObject().apply {
          put("role", "system")
          put("content", systemInstruction)
        })
      }
      val recentHistory = history.takeLast(12)
      for (msg in recentHistory) {
        put(JSONObject().apply {
          put("role", if (msg.role == "model" || msg.role == "assistant") "assistant" else "user")
          put("content", msg.text)
        })
      }
    }

    for (m in models) {
      try {
        val jsonBody = JSONObject().apply {
          put("model", m)
          put("temperature", 0.7)
          put("messages", messagesArray)
        }

        val reqBuilder = Request.Builder()
          .url(baseUrl)
          .addHeader("Authorization", "Bearer $apiKey")
          .addHeader("Content-Type", "application/json")
        extraHeaders.forEach { (k, v) -> reqBuilder.addHeader(k, v) }

        val request = reqBuilder.post(jsonBody.toString().toRequestBody(jsonMediaType)).build()
        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""
        if (response.isSuccessful) {
          val json = JSONObject(bodyStr)
          val choices = json.optJSONArray("choices")
          val content = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
          if (!content.isNullOrBlank()) {
            Log.i("GeminiStudyService", "$providerLabel chat ($m) successful")
            return@withContext ChatResponse(
              text = content.trim(),
              modelName = "$m ($providerLabel)",
              groundingSources = emptyList(),
              searchQueries = emptyList(),
              isError = false
            )
          }
        } else {
          Log.w("GeminiStudyService", "$providerLabel chat ($m) error ${response.code}: $bodyStr")
        }
      } catch (e: Exception) {
        Log.w("GeminiStudyService", "$providerLabel chat ($m) exception: ${e.message}")
      }
    }
    null
  }

  // --- Specific Helpers ---
  suspend fun callGroqApi(prompt: String, systemInstruction: String = "You are StudyPilot AI.", temperature: Double = 0.7): String? {
    val key = getGroqApiKey()
    return callOpenAICompatibleApi(
      baseUrl = "https://api.groq.com/openai/v1/chat/completions",
      apiKey = key,
      models = listOf("openai/gpt-oss-120b", "openai/gpt-oss-20b", "qwen/qwen3.8-27b", "llama-3.3-70b-versatile"),
      prompt = prompt,
      systemInstruction = systemInstruction,
      temperature = temperature
    )
  }

  suspend fun chatWithGroq(history: List<ChatMessage>, systemInstruction: String): ChatResponse? {
    val key = getGroqApiKey()
    return chatWithOpenAICompatible(
      providerLabel = "Groq AI",
      baseUrl = "https://api.groq.com/openai/v1/chat/completions",
      apiKey = key,
      models = listOf("openai/gpt-oss-120b", "openai/gpt-oss-20b", "qwen/qwen3.8-27b", "llama-3.3-70b-versatile"),
      history = history,
      systemInstruction = systemInstruction
    )
  }

  suspend fun callOpenAIApi(prompt: String, systemInstruction: String = "You are StudyPilot AI.", temperature: Double = 0.7): String? {
    val key = getOpenAIApiKey()
    return callOpenAICompatibleApi(
      baseUrl = "https://api.openai.com/v1/chat/completions",
      apiKey = key,
      models = listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo"),
      prompt = prompt,
      systemInstruction = systemInstruction,
      temperature = temperature
    )
  }

  suspend fun chatWithOpenAI(history: List<ChatMessage>, systemInstruction: String): ChatResponse? {
    val key = getOpenAIApiKey()
    return chatWithOpenAICompatible(
      providerLabel = "OpenAI ChatGPT",
      baseUrl = "https://api.openai.com/v1/chat/completions",
      apiKey = key,
      models = listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo"),
      history = history,
      systemInstruction = systemInstruction
    )
  }

  suspend fun callDeepSeekApi(prompt: String, systemInstruction: String = "You are StudyPilot AI.", temperature: Double = 0.7): String? {
    val key = getDeepSeekApiKey()
    return callOpenAICompatibleApi(
      baseUrl = "https://api.deepseek.com/chat/completions",
      apiKey = key,
      models = listOf("deepseek-chat", "deepseek-coder"),
      prompt = prompt,
      systemInstruction = systemInstruction,
      temperature = temperature
    )
  }

  suspend fun chatWithDeepSeek(history: List<ChatMessage>, systemInstruction: String): ChatResponse? {
    val key = getDeepSeekApiKey()
    return chatWithOpenAICompatible(
      providerLabel = "DeepSeek AI",
      baseUrl = "https://api.deepseek.com/chat/completions",
      apiKey = key,
      models = listOf("deepseek-chat", "deepseek-coder"),
      history = history,
      systemInstruction = systemInstruction
    )
  }

  suspend fun callOpenRouterApi(prompt: String, systemInstruction: String = "You are StudyPilot AI.", temperature: Double = 0.7): String? {
    val key = getOpenRouterApiKey()
    return callOpenAICompatibleApi(
      baseUrl = "https://openrouter.ai/api/v1/chat/completions",
      apiKey = key,
      models = listOf("openrouter/auto", "openai/gpt-4o-mini", "anthropic/claude-3.5-sonnet"),
      prompt = prompt,
      systemInstruction = systemInstruction,
      temperature = temperature
    )
  }

  suspend fun chatWithOpenRouter(history: List<ChatMessage>, systemInstruction: String): ChatResponse? {
    val key = getOpenRouterApiKey()
    return chatWithOpenAICompatible(
      providerLabel = "OpenRouter AI",
      baseUrl = "https://openrouter.ai/api/v1/chat/completions",
      apiKey = key,
      models = listOf("openrouter/auto", "openai/gpt-4o-mini", "anthropic/claude-3.5-sonnet"),
      history = history,
      systemInstruction = systemInstruction
    )
  }

  // --- Anthropic Claude Helper ---
  private suspend fun callClaudeApiWithKey(apiKey: String, prompt: String, systemInstruction: String = "You are StudyPilot AI."): String? = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) return@withContext null
    val models = listOf("claude-3-5-sonnet-20241022", "claude-3-haiku-20240307")
    for (m in models) {
      try {
        val jsonBody = JSONObject().apply {
          put("model", m)
          put("max_tokens", 1500)
          if (systemInstruction.isNotBlank()) {
            put("system", systemInstruction)
          }
          put("messages", JSONArray().apply {
            put(JSONObject().apply {
              put("role", "user")
              put("content", prompt)
            })
          })
        }

        val request = Request.Builder()
          .url("https://api.anthropic.com/v1/messages")
          .addHeader("x-api-key", apiKey)
          .addHeader("anthropic-version", "2023-06-01")
          .addHeader("Content-Type", "application/json")
          .post(jsonBody.toString().toRequestBody(jsonMediaType))
          .build()

        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""
        if (response.isSuccessful) {
          val json = JSONObject(bodyStr)
          val contentArr = json.optJSONArray("content")
          val text = contentArr?.optJSONObject(0)?.optString("text")
          if (!text.isNullOrBlank()) {
            return@withContext text.trim()
          }
        }
      } catch (e: Exception) {
        Log.w("GeminiStudyService", "Claude API ($m) exception: ${e.message}")
      }
    }
    null
  }

  suspend fun callClaudeApi(prompt: String, systemInstruction: String = "You are StudyPilot AI.", temperature: Double = 0.7): String? {
    val key = getClaudeApiKey()
    return callClaudeApiWithKey(key, prompt, systemInstruction)
  }

  suspend fun chatWithClaude(history: List<ChatMessage>, systemInstruction: String): ChatResponse? = withContext(Dispatchers.IO) {
    val apiKey = getClaudeApiKey()
    if (apiKey.isBlank()) return@withContext null
    val models = listOf("claude-3-5-sonnet-20241022", "claude-3-haiku-20240307")

    val messagesArray = JSONArray().apply {
      val recent = history.takeLast(12)
      for (msg in recent) {
        put(JSONObject().apply {
          put("role", if (msg.role == "model" || msg.role == "assistant") "assistant" else "user")
          put("content", msg.text)
        })
      }
    }

    for (m in models) {
      try {
        val jsonBody = JSONObject().apply {
          put("model", m)
          put("max_tokens", 2000)
          if (systemInstruction.isNotBlank()) {
            put("system", systemInstruction)
          }
          put("messages", messagesArray)
        }

        val request = Request.Builder()
          .url("https://api.anthropic.com/v1/messages")
          .addHeader("x-api-key", apiKey)
          .addHeader("anthropic-version", "2023-06-01")
          .addHeader("Content-Type", "application/json")
          .post(jsonBody.toString().toRequestBody(jsonMediaType))
          .build()

        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""
        if (response.isSuccessful) {
          val json = JSONObject(bodyStr)
          val contentArr = json.optJSONArray("content")
          val text = contentArr?.optJSONObject(0)?.optString("text")
          if (!text.isNullOrBlank()) {
            return@withContext ChatResponse(
              text = text.trim(),
              modelName = "$m (Anthropic Claude)",
              groundingSources = emptyList(),
              searchQueries = emptyList(),
              isError = false
            )
          }
        }
      } catch (e: Exception) {
        Log.w("GeminiStudyService", "Claude chat ($m) exception: ${e.message}")
      }
    }
    null
  }

  // --- Gemini Call Helper ---
  private suspend fun callGeminiPromptApi(prompt: String, systemInstruction: String = ""): String? = withContext(Dispatchers.IO) {
    val geminiKey = getGeminiApiKey()
    if (geminiKey.isBlank()) return@withContext null

    val modelsToTry = listOf("gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash")
    for (m in modelsToTry) {
      try {
        val jsonBody = JSONObject().apply {
          put("contents", JSONArray().apply {
            put(JSONObject().apply {
              put("parts", JSONArray().apply {
                if (systemInstruction.isNotBlank()) {
                  put(JSONObject().put("text", "$systemInstruction\n\n$prompt"))
                } else {
                  put(JSONObject().put("text", prompt))
                }
              })
            })
          })
        }

        val request = Request.Builder()
          .url("https://generativelanguage.googleapis.com/v1beta/models/$m:generateContent?key=$geminiKey")
          .post(jsonBody.toString().toRequestBody(jsonMediaType))
          .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
          val respBody = response.body?.string() ?: ""
          val jsonResp = JSONObject(respBody)
          val text = jsonResp.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")

          if (!text.isNullOrBlank()) {
            return@withContext text.trim()
          }
        }
      } catch (e: Exception) {
        Log.w("GeminiStudyService", "Gemini $m call failed: ${e.message}")
      }
    }
    null
  }

  // --- Multi-Engine Prompt Dispatcher ---
  private suspend fun dispatchPromptApiCall(
    prompt: String,
    systemInstruction: String = "You are StudyPilot AI, an expert academic tutor for university students.",
    temperature: Double = 0.7
  ): String? = withContext(Dispatchers.IO) {
    val mode = preferredProviderMode.uppercase()

    val explicitResult = when (mode) {
      "GROQ" -> callGroqApi(prompt, systemInstruction, temperature)
      "OPENAI", "CHATGPT" -> callOpenAIApi(prompt, systemInstruction, temperature)
      "DEEPSEEK" -> callDeepSeekApi(prompt, systemInstruction, temperature)
      "CLAUDE" -> callClaudeApi(prompt, systemInstruction, temperature)
      "OPENROUTER" -> callOpenRouterApi(prompt, systemInstruction, temperature)
      "GEMINI" -> callGeminiPromptApi(prompt, systemInstruction)
      else -> null
    }

    if (!explicitResult.isNullOrBlank()) {
      return@withContext explicitResult
    }

    // Auto Fallback Chain
    callGeminiPromptApi(prompt, systemInstruction)?.takeIf { it.isNotBlank() }?.let { return@withContext it }
    callGroqApi(prompt, systemInstruction, temperature)?.takeIf { it.isNotBlank() }?.let { return@withContext it }
    callOpenAIApi(prompt, systemInstruction, temperature)?.takeIf { it.isNotBlank() }?.let { return@withContext it }
    callDeepSeekApi(prompt, systemInstruction, temperature)?.takeIf { it.isNotBlank() }?.let { return@withContext it }
    callClaudeApi(prompt, systemInstruction, temperature)?.takeIf { it.isNotBlank() }?.let { return@withContext it }
    callOpenRouterApi(prompt, systemInstruction, temperature)?.takeIf { it.isNotBlank() }?.let { return@withContext it }

    null
  }

  // --- Summarization ---
  suspend fun summarizeMaterial(title: String, rawContent: String): String = withContext(Dispatchers.IO) {
    if (preferredProviderMode.equals("LOCAL", ignoreCase = true)) {
      return@withContext generateLocalSummary(title, rawContent)
    }

    val prompt = """
      You are StudyPilot AI, an expert academic tutor for university students.
      Summarize the following study material on "$title" concisely.
      Provide:
      1. Core Concept (2-3 sentences)
      2. 3-4 Key Takeaways / Definitions
      3. 1 Typical Exam Problem / Concept Focus
      Keep response focused, clear, and under 250 words.

      Study Material:
      $rawContent
    """.trimIndent()

    val apiResult = dispatchPromptApiCall(prompt)
    if (!apiResult.isNullOrBlank()) {
      return@withContext apiResult
    }

    // Fallback to local summary
    generateLocalSummary(title, rawContent)
  }

  // --- Question Generation ---
  suspend fun generateQuestionsForTopic(
    topic: StudyTopic,
    desiredCount: Int = 3,
    difficulty: String = "MEDIUM"
  ): List<QuizQuestion> = withContext(Dispatchers.IO) {
    if (preferredProviderMode.equals("LOCAL", ignoreCase = true)) {
      return@withContext generateLocalQuestions(topic, desiredCount, difficulty)
    }

    val prompt = """
      Generate exactly $desiredCount revision questions on the topic "${topic.title}" (${topic.subjectName}) with difficulty $difficulty.
      Based on these student notes:
      "${topic.descriptionNotes.take(1500)}"

      Format output as a raw valid JSON array of objects with keys:
      - "questionType": one of "MULTIPLE_CHOICE", "TRUE_FALSE", "SHORT_ANSWER", "CODING"
      - "difficulty": "$difficulty"
      - "questionText": string
      - "optionsJson": comma-separated options for MULTIPLE_CHOICE (4 options) or TRUE_FALSE ("True,False"), otherwise empty string
      - "correctAnswer": string (must match one option or be concise expected answer)
      - "explanation": string explaining why it is correct
      - "codeStarter": string with boilerplate code for CODING, or null for other types

      Do not include markdown ticks (```json), return raw JSON only.
    """.trimIndent()

    val apiResult = dispatchPromptApiCall(prompt, systemInstruction = "You are an expert exam item writer. Respond ONLY with a valid raw JSON array.", temperature = 0.3)
    if (!apiResult.isNullOrBlank()) {
      val parsed = parseQuestionsJson(apiResult, topic, difficulty)
      if (parsed.isNotEmpty()) return@withContext parsed
    }

    generateLocalQuestions(topic, desiredCount, difficulty)
  }

  private fun parseQuestionsJson(
    rawJson: String,
    topic: StudyTopic,
    difficulty: String
  ): List<QuizQuestion> {
    val list = mutableListOf<QuizQuestion>()
    try {
      val cleanJson = rawJson.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
      val jsonArray = JSONArray(cleanJson)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          QuizQuestion(
            topicId = topic.id,
            subjectName = topic.subjectName,
            topicTitle = topic.title,
            questionType = obj.optString("questionType", "MULTIPLE_CHOICE"),
            difficulty = obj.optString("difficulty", difficulty),
            questionText = obj.optString("questionText", "Practice Problem ${i + 1}"),
            optionsJson = obj.optString("optionsJson", ""),
            correctAnswer = obj.optString("correctAnswer", ""),
            explanation = obj.optString("explanation", "Review course notes for details."),
            codeStarter = obj.optString("codeStarter", null)
          )
        )
      }
    } catch (e: Exception) {
      Log.w("GeminiStudyService", "Failed to parse questions JSON: ${e.message}")
    }
    return list
  }

  // --- Answer Evaluation ---
  suspend fun evaluateAnswer(
    question: QuizQuestion,
    userAnswer: String
  ): EvaluationResult = withContext(Dispatchers.IO) {
    val directMatch = userAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
    if (preferredProviderMode.equals("LOCAL", ignoreCase = true)) {
      return@withContext EvaluationResult(
        isCorrect = directMatch,
        feedback = if (directMatch) "Correct! ${question.explanation}" else "Expected '${question.correctAnswer}'. ${question.explanation}"
      )
    }

    val prompt = """
      You are an automated academic evaluator.
      Question: "${question.questionText}"
      Expected Answer: "${question.correctAnswer}"
      Explanation Context: "${question.explanation}"
      Student Answer: "$userAnswer"

      Evaluate the student answer for conceptual accuracy.
      Format output strictly as JSON object with keys:
      - "isCorrect": boolean
      - "feedback": string (concise 1-2 sentence feedback explaining why it's right or wrong)

      Raw JSON only, no markdown ticks.
    """.trimIndent()

    val apiResult = dispatchPromptApiCall(prompt, systemInstruction = "You are an automated academic evaluator. Output raw JSON only.", temperature = 0.2)
    if (!apiResult.isNullOrBlank()) {
      try {
        val cleanJson = apiResult.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val evalObj = JSONObject(cleanJson)
        return@withContext EvaluationResult(
          isCorrect = evalObj.optBoolean("isCorrect", directMatch),
          feedback = evalObj.optString("feedback", question.explanation)
        )
      } catch (_: Exception) {}
    }

    EvaluationResult(
      isCorrect = directMatch,
      feedback = if (directMatch) "Correct! ${question.explanation}" else "Concept check: Expected '${question.correctAnswer}'. ${question.explanation}"
    )
  }

  // --- Interactive Tutor Chat ---
  suspend fun chatWithGemini(
    history: List<ChatMessage>,
    model: TutorModel = TutorModel.FLASH,
    useGoogleSearch: Boolean = false,
    systemInstruction: String = "You are StudyPilot AI Tutor, an elite university tutor and research guide. Assist students with course explanations, derivations, debugging, and academic research with accuracy, citing web sources when using Google Search."
  ): ChatResponse = withContext(Dispatchers.IO) {
    if (preferredProviderMode.equals("LOCAL", ignoreCase = true)) {
      val lastUserMsg = history.lastOrNull { it.role == "user" }?.text ?: "Hello"
      return@withContext generateLocalChatResponse(lastUserMsg, model, useGoogleSearch)
    }

    val mode = preferredProviderMode.uppercase()
    when (mode) {
      "GROQ" -> chatWithGroq(history, systemInstruction)?.let { return@withContext it }
      "OPENAI", "CHATGPT" -> chatWithOpenAI(history, systemInstruction)?.let { return@withContext it }
      "DEEPSEEK" -> chatWithDeepSeek(history, systemInstruction)?.let { return@withContext it }
      "CLAUDE" -> chatWithClaude(history, systemInstruction)?.let { return@withContext it }
      "OPENROUTER" -> chatWithOpenRouter(history, systemInstruction)?.let { return@withContext it }
    }

    // Try Gemini Primary
    val geminiKey = getGeminiApiKey()
    val primaryModelId = if (model.modelId == "gemini-3.5-flash") "gemini-2.5-flash" else model.modelId
    val modelsToTry = if (useGoogleSearch) {
      listOf("gemini-2.5-flash", "gemini-1.5-flash")
    } else {
      listOf(primaryModelId, "gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash")
    }.distinct()

    if (geminiKey.isNotBlank()) {
      for (targetModel in modelsToTry) {
        try {
          val contentsArray = JSONArray()
          val recentHistory = history.takeLast(12)
          for (msg in recentHistory) {
            val partObj = JSONObject().put("text", msg.text)
            val contentObj = JSONObject().apply {
              put("role", if (msg.role == "model" || msg.role == "assistant") "model" else "user")
              put("parts", JSONArray().put(partObj))
            }
            contentsArray.put(contentObj)
          }

          val jsonBody = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
              put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })

            if (useGoogleSearch) {
              put("tools", JSONArray().apply {
                put(JSONObject().put("googleSearch", JSONObject()))
              })
            }
          }

          val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$geminiKey")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

          val response = client.newCall(request).execute()
          val respBody = response.body?.string() ?: ""
          if (response.isSuccessful) {
            val jsonResp = JSONObject(respBody)
            val candidate = jsonResp.optJSONArray("candidates")?.optJSONObject(0)
            val text = candidate?.optJSONObject("content")
              ?.optJSONArray("parts")
              ?.optJSONObject(0)
              ?.optString("text")

            val groundingSources = mutableListOf<GroundingSource>()
            val searchQueries = mutableListOf<String>()

            val groundingMeta = candidate?.optJSONObject("groundingMetadata")
            if (groundingMeta != null) {
              val webQueries = groundingMeta.optJSONArray("webSearchQueries")
              if (webQueries != null) {
                for (i in 0 until webQueries.length()) {
                  searchQueries.add(webQueries.getString(i))
                }
              }

              val chunks = groundingMeta.optJSONArray("groundingChunks")
              if (chunks != null) {
                for (i in 0 until chunks.length()) {
                  val chunk = chunks.getJSONObject(i)
                  val web = chunk.optJSONObject("web")
                  if (web != null) {
                    val uri = web.optString("uri", "")
                    val title = web.optString("title", "Web Source")
                    if (uri.isNotBlank()) {
                      groundingSources.add(GroundingSource(title = title, url = uri))
                    }
                  }
                }
              }
            }

            if (!text.isNullOrBlank()) {
              return@withContext ChatResponse(
                text = text.trim(),
                modelName = "$targetModel (Gemini AI)",
                groundingSources = groundingSources,
                searchQueries = searchQueries,
                isError = false
              )
            }
          }
        } catch (e: Exception) {
          Log.w("GeminiStudyService", "Gemini chat $targetModel failed: ${e.message}")
        }
      }
    }

    // Auto Multi-Engine Fallback Chain
    chatWithGroq(history, systemInstruction)?.let { return@withContext it }
    chatWithOpenAI(history, systemInstruction)?.let { return@withContext it }
    chatWithDeepSeek(history, systemInstruction)?.let { return@withContext it }
    chatWithClaude(history, systemInstruction)?.let { return@withContext it }
    chatWithOpenRouter(history, systemInstruction)?.let { return@withContext it }

    // Local Fallback
    val lastUserMsg = history.lastOrNull { it.role == "user" }?.text ?: "Hello"
    generateLocalChatResponse(lastUserMsg, model, useGoogleSearch)
  }

  private fun generateLocalSummary(title: String, rawContent: String): String {
    val lines = rawContent.lines().filter { it.isNotBlank() }
    val lineCount = lines.size
    val firstFew = lines.take(3).joinToString(" ")
    return buildString {
      append("📌 **Core Concept**: $title focuses on ${if (firstFew.isNotBlank()) firstFew.take(120) else "fundamental principles and practical applications"}.\n\n")
      append("💡 **Key Takeaways**:\n")
      if (lineCount >= 2) {
        lines.take(4).forEach { line ->
          append("• ${line.take(90)}\n")
        }
      } else {
        append("• Core definitions and theoretical foundation\n")
        append("• Key algorithms and methodological workflow\n")
        append("• Exam-relevant application problems\n")
      }
      append("\n🎯 **Exam Focus**: Ensure mastery of definitions, edge cases, and step-by-step problem derivation.")
    }
  }

  private fun generateLocalQuestions(
    topic: StudyTopic,
    count: Int,
    difficulty: String
  ): List<QuizQuestion> {
    return List(count) { idx ->
      QuizQuestion(
        topicId = topic.id,
        subjectName = topic.subjectName,
        topicTitle = topic.title,
        questionType = if (idx % 2 == 0) "MULTIPLE_CHOICE" else "TRUE_FALSE",
        difficulty = difficulty,
        questionText = "Question #${idx + 1}: What is a fundamental property of ${topic.title}?",
        optionsJson = if (idx % 2 == 0) "Option A,Option B,Option C,Option D" else "True,False",
        correctAnswer = if (idx % 2 == 0) "Option A" else "True",
        explanation = "Refer to topic notes on ${topic.title} for complete derivation."
      )
    }
  }

  private fun generateLocalChatResponse(
    userMsg: String,
    model: TutorModel,
    useGoogleSearch: Boolean
  ): ChatResponse {
    val responseText = """
      Here is an academic analysis of your query:

      > **Topic Focus**: "${userMsg.take(60)}"

      1. **Key Concept**: Academic problem solving relies on breaking down complex prompts into core principles, variable definitions, and step-by-step logical execution.
      2. **Methodology**: Apply active recall and test your understanding by working through concrete example problems.
      3. **Next Steps**: Review related course notes and attempt practice quiz questions in the Practice section.

      *(Note: Responding in Offline Academic Study Mode. You can configure or switch AI keys in Settings.)*
    """.trimIndent()

    return ChatResponse(
      text = responseText,
      modelName = "Offline Deterministic Tutor Mode",
      isError = false
    )
  }
}
