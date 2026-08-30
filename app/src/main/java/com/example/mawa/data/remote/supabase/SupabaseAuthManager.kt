package com.example.mawa.data.remote.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseAuthManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("mawa_supabase_prefs", Context.MODE_PRIVATE)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val _currentUser = MutableStateFlow<SupabaseUser?>(null)
    val currentUser: StateFlow<SupabaseUser?> = _currentUser.asStateFlow()

    private val _session = MutableStateFlow<SupabaseSession?>(null)
    val session: StateFlow<SupabaseSession?> = _session.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        restoreSessionFromPrefs()
    }

    private fun restoreSessionFromPrefs() {
        val accessToken = prefs.getString("access_token", null)
        val refreshToken = prefs.getString("refresh_token", null)
        val userId = prefs.getString("user_id", null)
        val userEmail = prefs.getString("user_email", null)
        val displayName = prefs.getString("display_name", "") ?: ""

        if (!accessToken.isNullOrBlank() && !userId.isNullOrBlank() && !userEmail.isNullOrBlank()) {
            val user = SupabaseUser(
                id = userId,
                email = userEmail,
                displayName = displayName
            )
            val sess = SupabaseSession(
                accessToken = accessToken,
                refreshToken = refreshToken ?: "",
                user = user
            )
            _currentUser.value = user
            _session.value = sess
        }
    }

    fun getAccessToken(): String {
        return _session.value?.accessToken ?: SupabaseConfig.SUPABASE_ANON_KEY
    }

    fun getUserId(): String? {
        return _currentUser.value?.id
    }

    fun getUserEmail(): String? {
        return _currentUser.value?.email
    }

    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }

    suspend fun signUp(email: String, password: String, displayName: String): SupabaseAuthResult = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim())
                put("password", password.trim())
                val metadata = JSONObject().apply {
                    put("display_name", displayName.trim())
                    put("full_name", displayName.trim())
                }
                put("data", metadata)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.SUPABASE_AUTH_URL}/signup")
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SupabaseAuth", "SignUp response code: ${response.code}, body: $responseBody")

                if (response.isSuccessful) {
                    val root = JSONObject(responseBody)
                    val accessToken = root.optString("access_token", "")
                    val refreshToken = root.optString("refresh_token", "")
                    
                    val userObj = if (root.has("user")) root.getJSONObject("user") else root
                    val userId = userObj.optString("id", "")
                    val userEmail = userObj.optString("email", email)
                    val userMeta = userObj.optJSONObject("user_metadata")
                    val name = userMeta?.optString("display_name", displayName) ?: displayName

                    val user = SupabaseUser(
                        id = userId,
                        email = userEmail,
                        displayName = name
                    )

                    if (accessToken.isNotBlank()) {
                        val session = SupabaseSession(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            user = user
                        )
                        saveSessionToPrefs(session)
                        _currentUser.value = user
                        _session.value = session
                    }

                    _isLoading.value = false
                    return@withContext SupabaseAuthResult.Success(
                        user = user,
                        message = "সুপাবেজ অ্যাকাউন্ট সফলভাবে তৈরি হয়েছে!"
                    )
                } else {
                    _isLoading.value = false
                    val errorMsg = parseErrorMessage(responseBody)
                    return@withContext SupabaseAuthResult.Error(
                        message = "রেজিস্ট্রেশন ব্যর্থ: $errorMsg",
                        code = response.code
                    )
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("SupabaseAuth", "SignUp error", e)
            return@withContext SupabaseAuthResult.Error(
                message = "সংযোগ সমস্যা: ${e.localizedMessage ?: "ইন্টারনেট কানেকশন চেক করুন"}"
            )
        }
    }

    suspend fun signIn(email: String, password: String): SupabaseAuthResult = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim())
                put("password", password.trim())
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.SUPABASE_AUTH_URL}/token?grant_type=password")
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SupabaseAuth", "SignIn response code: ${response.code}, body: $responseBody")

                if (response.isSuccessful) {
                    val root = JSONObject(responseBody)
                    val accessToken = root.getString("access_token")
                    val refreshToken = root.optString("refresh_token", "")
                    val expiresIn = root.optLong("expires_in", 3600)

                    val userObj = root.getJSONObject("user")
                    val userId = userObj.getString("id")
                    val userEmail = userObj.optString("email", email)
                    val userMeta = userObj.optJSONObject("user_metadata")
                    val displayName = userMeta?.optString("display_name")
                        ?: userMeta?.optString("full_name")
                        ?: userEmail.substringBefore("@")

                    val user = SupabaseUser(
                        id = userId,
                        email = userEmail,
                        displayName = displayName,
                        createdAt = userObj.optString("created_at", "")
                    )

                    val session = SupabaseSession(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresIn = expiresIn,
                        user = user
                    )

                    saveSessionToPrefs(session)
                    _currentUser.value = user
                    _session.value = session
                    _isLoading.value = false

                    return@withContext SupabaseAuthResult.Success(
                        user = user,
                        message = "সফলভাবে লগইন হয়েছে! স্বাগতম, ${user.displayName}।"
                    )
                } else {
                    _isLoading.value = false
                    val errorMsg = parseErrorMessage(responseBody)
                    return@withContext SupabaseAuthResult.Error(
                        message = "লগইন ব্যর্থ: $errorMsg",
                        code = response.code
                    )
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("SupabaseAuth", "SignIn error", e)
            return@withContext SupabaseAuthResult.Error(
                message = "লগইন ত্রুটি: ${e.localizedMessage ?: "নেটওয়ার্ক কানেকশন পরীক্ষা করুন"}"
            )
        }
    }

    suspend fun resetPassword(email: String): SupabaseAuthResult = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim())
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.SUPABASE_AUTH_URL}/recover")
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SupabaseAuth", "ResetPassword response code: ${response.code}, body: $responseBody")

                _isLoading.value = false
                if (response.isSuccessful) {
                    return@withContext SupabaseAuthResult.PasswordResetSent(
                        email = email,
                        message = "পাসওয়ার্ড রিসেট লিংক $email ঠিকানায় পাঠানো হয়েছে। অনুগ্রহ করে আপনার ইমেইল ইনবক্স চেক করুন।"
                    )
                } else {
                    val errorMsg = parseErrorMessage(responseBody)
                    return@withContext SupabaseAuthResult.Error(
                        message = "পাসওয়ার্ড রিসেট ব্যর্থ: $errorMsg",
                        code = response.code
                    )
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("SupabaseAuth", "ResetPassword error", e)
            return@withContext SupabaseAuthResult.Error(
                message = "ত্রুটি: ${e.localizedMessage ?: "ইন্টারনেট কানেকশন সমস্যা"}"
            )
        }
    }

    suspend fun updatePassword(newPassword: String): SupabaseAuthResult = withContext(Dispatchers.IO) {
        val currentSess = _session.value
        if (currentSess == null) {
            return@withContext SupabaseAuthResult.Error("ব্যবহারকারী লগইন করা নেই")
        }

        _isLoading.value = true
        try {
            val bodyJson = JSONObject().apply {
                put("password", newPassword.trim())
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.SUPABASE_AUTH_URL}/user")
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${currentSess.accessToken}")
                .addHeader("Content-Type", "application/json")
                .put(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                _isLoading.value = false

                if (response.isSuccessful) {
                    return@withContext SupabaseAuthResult.Success(
                        user = currentSess.user,
                        message = "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে!"
                    )
                } else {
                    val errorMsg = parseErrorMessage(responseBody)
                    return@withContext SupabaseAuthResult.Error(
                        message = "পাসওয়ার্ড আপডেট ব্যর্থ: $errorMsg"
                    )
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
            return@withContext SupabaseAuthResult.Error("ত্রুটি: ${e.localizedMessage}")
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val currentSess = _session.value
        if (currentSess != null) {
            try {
                val request = Request.Builder()
                    .url("${SupabaseConfig.SUPABASE_AUTH_URL}/logout")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer ${currentSess.accessToken}")
                    .post("{}".toRequestBody(jsonMediaType))
                    .build()
                client.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.w("SupabaseAuth", "SignOut remote error (ignored): ${e.message}")
            }
        }

        clearSessionPrefs()
        _currentUser.value = null
        _session.value = null
    }

    private fun saveSessionToPrefs(session: SupabaseSession) {
        prefs.edit().apply {
            putString("access_token", session.accessToken)
            putString("refresh_token", session.refreshToken)
            putString("user_id", session.user.id)
            putString("user_email", session.user.email)
            putString("display_name", session.user.displayName)
            putLong("saved_at", System.currentTimeMillis())
            apply()
        }
    }

    private fun clearSessionPrefs() {
        prefs.edit().apply {
            remove("access_token")
            remove("refresh_token")
            remove("user_id")
            remove("user_email")
            remove("display_name")
            remove("saved_at")
            apply()
        }
    }

    private fun parseErrorMessage(responseBody: String): String {
        return try {
            val obj = JSONObject(responseBody)
            when {
                obj.has("msg") -> obj.getString("msg")
                obj.has("message") -> obj.getString("message")
                obj.has("error_description") -> obj.getString("error_description")
                obj.has("error") -> obj.getString("error")
                else -> responseBody.take(120)
            }
        } catch (e: Exception) {
            if (responseBody.isNotBlank()) responseBody.take(120) else "অজ্ঞাত ত্রুটি"
        }
    }
}
