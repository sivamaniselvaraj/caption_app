package com.octanovus.restaurantpos.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

class TablesRepository {
    private val pg get() = Supabase.client.postgrest
    suspend fun getTables(): List<RestaurantTable> =
        pg.from("tables")
            .select(Columns.list("id", "table_number", "capacity",  "outlet_id", "floor_area", "status"))
            {filter  { eq("outlet_id", Session.profile?.outletId ?: "") } }
            .decodeList<RestaurantTable>()

    /** Emits on any insert/update/delete to tables. Channel is torn down on cancel. */
    fun tableChanges(): Flow<PostgresAction> {
        val channel = Supabase.client.channel("public:tables")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "tables"
        }
        return flow
            .onStart { channel.subscribe() }
            .onCompletion { Supabase.client.realtime.removeChannel(channel) }
    }
}
