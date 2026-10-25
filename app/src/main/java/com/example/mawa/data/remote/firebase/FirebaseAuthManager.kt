package com.example.mawa.data.remote.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(private val context: Context) {
    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to get FirebaseAuth instance gracefully", e)
            null
        }
    }

    private val credentialManager: CredentialManager? by lazy {
        try {
            CredentialManager.create(context)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to create CredentialManager gracefully", e)
            null
        }
    }

    private val _currentUser = MutableStateFlow<FirebaseUserProfile?>(null)
    val currentUser: StateFlow<FirebaseUserProfile?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    companion object {
        private const val TAG = "FirebaseAuthManager"
        // Web Client ID from app/google-services.json (client_type: 3)
        const val WEB_CLIENT_ID = "830429065502-mkp415uh4ftsektrbi151bdnt6pcjo0t.apps.googleusercontent.com"
    }

    init {
        try {
            val a = auth
            if (a != null) {
                a.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser
                    _currentUser.value = user?.toProfile()
                }
                _currentUser.value = a.currentUser?.toProfile()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "FirebaseAuthManager init error gracefully handled", e)
        }
    }

    fun isLoggedIn(): Boolean = auth?.currentUser != null

    fun getUserId(): String? = auth?.currentUser?.uid

    suspend fun signInWithGoogle(activityContext: Context): FirebaseAuthResult {
        val a = auth ?: return FirebaseAuthResult.Error("ফায়ারবেজ ইনিশিয়ালাইজেশন সম্পন্ন হয়নি")
        val cm = credentialManager ?: return FirebaseAuthResult.Error("ক্রেডেনশিয়াল সার্ভিস অনুপলব্ধ")
        _isLoading.value = true
        return try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = cm.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = a.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    val profile = user.toProfile()
                    _currentUser.value = profile
                    FirebaseAuthResult.Success(
                        profile,
                        "গুগল দিয়ে সফলভাবে সাইন-ইন সম্পন্ন হয়েছে (${profile.displayName.ifEmpty { profile.email }})"
                    )
                } else {
                    FirebaseAuthResult.Error("ব্যবহারকারী পাওয়া যায়নি")
                }
            } else {
                FirebaseAuthResult.Error("অপ্রত্যাশিত ক্রেডেনশিয়াল ফরম্যাট")
            }
        } catch (e: GetCredentialCancellationException) {
            FirebaseAuthResult.Error("সাইন-ইন বাতিল করা হয়েছে")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Google sign-in failed", e)
            FirebaseAuthResult.Error("গুগল সাইন-ইন ব্যর্থ হয়েছে: ${e.localizedMessage ?: "অজ্ঞাত ত্রুটি"}", e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): FirebaseAuthResult {
        val a = auth ?: return FirebaseAuthResult.Error("ফায়ারবেজ ইনিশিয়ালাইজেশন সম্পন্ন হয়নি")
        _isLoading.value = true
        return try {
            val result = a.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = result.user
            if (user != null) {
                val profile = user.toProfile()
                _currentUser.value = profile
                FirebaseAuthResult.Success(profile, "সফলভাবে লগইন হয়েছে")
            } else {
                FirebaseAuthResult.Error("ব্যবহারকারী পাওয়া যায়নি")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email login error", e)
            FirebaseAuthResult.Error("লগইন ব্যর্থ: ${e.localizedMessage ?: "ভুল ইমেইল বা পাসওয়ার্ড"}", e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String = ""): FirebaseAuthResult {
        val a = auth ?: return FirebaseAuthResult.Error("ফায়ারবেজ ইনিশিয়ালাইজেশন সম্পন্ন হয়নি")
        _isLoading.value = true
        return try {
            val result = a.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = result.user
            if (user != null) {
                if (displayName.isNotBlank()) {
                    try {
                        val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName.trim())
                            .build()
                        user.updateProfile(profileUpdate).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to update profile name", e)
                    }
                }
                val profile = user.toProfile()
                _currentUser.value = profile
                FirebaseAuthResult.Success(profile, "সফলভাবে রেজিস্ট্রেশন সম্পন্ন হয়েছে")
            } else {
                FirebaseAuthResult.Error("অ্যাকাউন্ট তৈরি করা যায়নি")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email register error", e)
            FirebaseAuthResult.Error("রেজিস্ট্রেশন ব্যর্থ: ${e.localizedMessage ?: "ত্রুটি ঘটেছে"}", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Throwable) {
            Log.e(TAG, "signOut error", e)
        }
        _currentUser.value = null
    }

    private fun FirebaseUser.toProfile(): FirebaseUserProfile {
        return FirebaseUserProfile(
            uid = uid,
            email = email ?: "",
            displayName = displayName ?: "",
            photoUrl = photoUrl?.toString() ?: "",
            isAnonymous = isAnonymous
        )
    }
}
