package com.example.mawa.data.remote.supabase

data class SupabaseUser(
    val id: String,
    val email: String,
    val displayName: String = "",
    val createdAt: String = "",
    val lastSignInAt: String = ""
)

data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long = 3600,
    val tokenType: String = "bearer",
    val user: SupabaseUser
)

sealed class SupabaseAuthResult {
    data class Success(val user: SupabaseUser, val message: String) : SupabaseAuthResult()
    data class Error(val message: String, val code: Int? = null) : SupabaseAuthResult()
    data class PasswordResetSent(val email: String, val message: String) : SupabaseAuthResult()
}

data class CloudBackupItem(
    val id: Long = 0L,
    val userId: String = "",
    val backupName: String = "",
    val dataJson: String = "",
    val updatedAt: String = ""
)

data class CloudRecordItem(
    val id: Long = 0L,
    val userId: String = "",
    val domain: String = "BUSINESS",
    val entityType: String = "GENERAL",
    val entityId: String = "",
    val dataJson: String = "",
    val updatedAt: String = ""
)

sealed class CloudOperationResult<out T> {
    data class Success<T>(val data: T, val message: String = "") : CloudOperationResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : CloudOperationResult<Nothing>()
}
