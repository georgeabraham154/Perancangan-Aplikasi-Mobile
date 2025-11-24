package com.example.nusantaraview.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    private const val SUPABASE_URL = "https://mqxzrmtpceutfcfhktbt.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1xeHpybXRwY2V1dGZjZmhrdGJ0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM5MjA3MjAsImV4cCI6MjA3OTQ5NjcyMH0.NDQ_sK5hqjv2HUHa9MJrSwwkz6r5tYOsrstxEsbzCYw" // ⬅️ GANTI dengan anon key yang benar!

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}