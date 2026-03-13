package com.tuplataforma.encuestaapp.ui.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val state = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estado para el DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(state.isRegisterSuccess) {
        if (state.isRegisterSuccess) onRegisterSuccess()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Date(it)
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                        viewModel.onBirthDateChange(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = Color(0xFFD31124))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFD31124), Color(0xFF93000A))))
            ) {
                IconButton(
                    onClick = { if (state.step == 2) viewModel.previousStep() else onNavigateToLogin() },
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (state.step == 1) "Datos Personales" else "Crear Cuenta",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (state.step == 1) "PASO 1 DE 2" else "PASO 2 DE 2",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.step == 1) {
                    // PASO 1: DATOS PERSONALES
                    RegisterTextField(state.name, viewModel::onNameChange, "Nombres", Icons.Outlined.Person)
                    Spacer(modifier = Modifier.height(16.dp))
                    RegisterTextField(state.lastName, viewModel::onLastNameChange, "Apellidos", Icons.Outlined.Badge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de Fecha
                    Box(modifier = Modifier.fillMaxWidth()) {
                        RegisterTextField(
                            state.birthDate, 
                            {}, 
                            "Fecha de Nacimiento", 
                            Icons.Outlined.CalendarToday,
                            enabled = false,
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                        // Overlay transparente para detectar el click ya que el TextField está disabled
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Sexo", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = state.gender == "Masculino", 
                            onClick = { viewModel.onGenderChange("Masculino") },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD31124))
                        )
                        Text("Masculino", color = Color.Black, modifier = Modifier.clickable { viewModel.onGenderChange("Masculino") })
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = state.gender == "Femenino", 
                            onClick = { viewModel.onGenderChange("Femenino") },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD31124))
                        )
                        Text("Femenino", color = Color.Black, modifier = Modifier.clickable { viewModel.onGenderChange("Femenino") })
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = viewModel::nextStep,
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD31124))
                    ) {
                        Text("SIGUIENTE", fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    // PASO 2: DATOS DE CUENTA
                    RegisterTextField(state.email, viewModel::onEmailChange, "Correo Electrónico", Icons.Outlined.Mail)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var passVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Color(0xFFD31124)) },
                        trailingIcon = {
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD31124),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,
                            focusedLabelColor = Color(0xFFD31124)
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var confirmVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Color(0xFFD31124)) },
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD31124),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,
                            focusedLabelColor = Color(0xFFD31124)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = viewModel::register,
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD31124)),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        else Text("REGISTRARSE", fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Ya tienes cuenta?", color = Color.Gray)
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Inicia sesión", color = Color(0xFFD31124), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFFD31124)) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFD31124),
            focusedLabelColor = Color(0xFFD31124),
            unfocusedContainerColor = Color(0xFFF8F9FA),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            disabledBorderColor = Color(0xFFE0E0E0),
            disabledLabelColor = Color.Gray
        ),
        singleLine = true
    )
}
