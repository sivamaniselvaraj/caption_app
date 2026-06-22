package com.octanovus.restaurantpos.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.octanovus.restaurantpos.data.AuthRepository
import com.octanovus.restaurantpos.data.Session
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun login(onSuccess: () -> Unit) = viewModelScope.launch {
        loading = true; error = null
        try {
            // 1. Supabase Auth verifies the credentials.
            repo.signIn(email.trim(), password)
            // 2. profiles authorizes the user as active staff.
            val profile = repo.currentProfile()
            if (profile == null || !profile.isActive) {
                repo.signOut()
                error = "This account isn't set up as active staff. Contact a manager."
                return@launch
            }
            Session.profile = profile
            onSuccess()
        } catch (e: Exception) {
            error = e.message ?: "Login failed"
        } finally {
            loading = false
        }
    }
}


@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    vm: com.octanovus.restaurantpos.ui.login.LoginViewModel = viewModel()
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Restaurant POS", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = vm.email, onValueChange = { vm.email = it },
            label = { Text("Email") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = vm.password, onValueChange = { vm.password = it },
            label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        vm.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { vm.login(onLoggedIn) },
            enabled = !vm.loading && vm.email.isNotBlank() && vm.password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (vm.loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Log in")
        }
    }
}
