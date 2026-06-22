package com.octanovus.restaurantpos.data

import com.octanovus.restaurantpos.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
//import io.github.jan.supabase.serializer.MoshiSerializer
import kotlinx.serialization.json.Json

object Supabase {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        defaultSerializer = KotlinXSerializer()
        install (Auth){
            {
                // In-memory session storage. Avoids the SharedPreferences-backed
                // SettingsSessionManager (which throws "No entry with the key" on
                // this setup). Staff log in each session, so we don't need the
                // login to survive an app restart.

                // sessionManager = MemorySessionManager()
            }
        }
                install (Postgrest)
                install (Realtime) // optional: live table status
    }
}
