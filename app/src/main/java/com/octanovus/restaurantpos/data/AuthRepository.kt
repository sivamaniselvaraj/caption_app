package com.octanovus.restaurantpos.data

import androidx.compose.foundation.layout.Column
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class AuthRepository {
    private val auth get() = Supabase.client.auth

    suspend fun signIn(email: String, password: String) =
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

    suspend fun signOut() = auth.signOut()

    val currentUserId: String? get() = auth.currentUserOrNull()?.id

    /** The staff profile for the signed-in user, or null if none exists. */
    suspend fun currentProfile(): Profiles? {
        val uid = currentUserId ?: return null
        val profilesData = Supabase.client.postgrest.from("profiles")
            .select(Columns.list("id", "user_id",  "outlet_id")) { filter { eq("user_id", uid) } }
            //.decodeList<Profile>().single()
            .decodeSingleOrNull<Profiles>()
        return profilesData
    }
}