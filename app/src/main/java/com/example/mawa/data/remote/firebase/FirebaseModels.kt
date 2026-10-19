package com.example.mawa.data.remote.firebase

data class FirebaseUserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isAnonymous: Boolean = false
)

data class FirebaseBackupItem(
    val id: String = "",
    val userId: String = "",
    val backupName: String = "",
    val dataJson: String = "",
    val customerCount: Int = 0,
    val transactionCount: Int = 0,
    val productCount: Int = 0,
    val totalBaki: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = ""
)

sealed class FirebaseAuthResult {
    data class Success(val user: FirebaseUserProfile, val message: String) : FirebaseAuthResult()
    data class Error(val message: String, val exception: Throwable? = null) : FirebaseAuthResult()
}

sealed class FirebaseOperationResult<out T> {
    data class Success<T>(val data: T, val message: String = "") : FirebaseOperationResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : FirebaseOperationResult<Nothing>()
}
