package com.example.nusantaraview.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseCulinaryClient {

    private const val SUPABASE_URL = "https://cecoesmvmsqrmrogyxjg.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNlY29lc212bXNxcm1yb2d5eGpnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM5Nzc5NTUsImV4cCI6MjA3OTU1Mzk1NX0.4iCVSUR-ybMSB0iEDZhtl-IqdYq5cXpWzwTdEUXHBw8"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Storage)
    }
}
