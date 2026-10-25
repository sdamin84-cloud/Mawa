package com.example.mawa
 
import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MawaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("MawaApplication", "FirebaseApp initialized successfully")
            }
        } catch (e: Throwable) {
            Log.e("MawaApplication", "FirebaseApp initialization failed gracefully", e)
        }
    }
}
