package com.tuplataforma.encuestaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tuplataforma.encuestaapp.ui.screens.admin.AdminScreen
import com.tuplataforma.encuestaapp.ui.screens.login.LoginScreen
import com.tuplataforma.encuestaapp.ui.screens.login.LoginViewModel
import com.tuplataforma.encuestaapp.ui.screens.main.MainScreen
import com.tuplataforma.encuestaapp.ui.screens.register.RegisterScreen
import com.tuplataforma.encuestaapp.ui.screens.register.RegisterViewModel
import com.tuplataforma.encuestaapp.ui.theme.EncuestaAPPTheme
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EncuestaAPPTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    
    // Estado para manejar el destino inicial de forma asíncrona si hay un usuario logueado
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val role = userDoc.getString("role") ?: "user"
                startDestination = if (role == "admin") "admin" else "main"
            } catch (e: Exception) {
                startDestination = "login"
            }
        } else {
            startDestination = "login"
        }
    }

    // Mostramos el NavHost solo cuando ya sabemos a dónde ir
    startDestination?.let { destination ->
        NavHost(navController = navController, startDestination = destination) {
            composable("login") {
                val loginViewModel: LoginViewModel = viewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        val role = loginViewModel.uiState.userRole
                        if (role == "admin") {
                            navController.navigate("admin") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                val registerViewModel: RegisterViewModel = viewModel()
                RegisterScreen(
                    viewModel = registerViewModel,
                    onRegisterSuccess = {
                        navController.navigate("main") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable("main") {
                MainScreen(
                    onLogout = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
            composable("admin") {
                AdminScreen(
                    onLogout = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("admin") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
