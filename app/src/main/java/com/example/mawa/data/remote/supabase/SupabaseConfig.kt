package com.example.mawa.data.remote.supabase

object SupabaseConfig {
    const val SUPABASE_URL = "https://pkpcfksbslbileordrqs.supabase.co"
    const val SUPABASE_ANON_KEY = "sb_publishable_Tp8GPJO0_ee3FpITfqes1A_DMn2BZJO"
    const val SUPABASE_AUTH_URL = "$SUPABASE_URL/auth/v1"
    const val SUPABASE_REST_URL = "$SUPABASE_URL/rest/v1"

    // Tables as provided by user
    const val TABLE_USER_BACKUPS = "user_backups"
    const val TABLE_CLOUD_RECORDS = "mawa_cloud_records"
}
