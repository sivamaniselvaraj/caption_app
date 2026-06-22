package com.octanovus.restaurantpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.octanovus.restaurantpos.ui.AppNav
import com.octanovus.restaurantpos.ui.theme.RestaurantPos
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
//import io.github.jan.supabase.serializer.MoshiSerializer

//import com.example.restaurantpos.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _root_ide_package_.com.octanovus.restaurantpos.ui.theme.RestaurantPos {
                _root_ide_package_.com.octanovus.restaurantpos.ui.AppNav()
            }
        }
    }
}
