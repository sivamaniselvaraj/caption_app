package com.octanovus.restaurantpos.data

/**
 * In-memory holder for the signed-in staff member's profile.
 * Set on successful login, cleared on logout. Read role/name from anywhere via
 * Session.profile (e.g. Session.profile?.role == "manager").
 *
 * Note: this is not persisted across process death — on a cold start the user
 * logs in again. Auto-restoring a session would be a separate enhancement.
 */
object Session {
    @Volatile
    var profile: Profiles? = null

    val isManager: Boolean get() = profile?.role == "manager"
}
