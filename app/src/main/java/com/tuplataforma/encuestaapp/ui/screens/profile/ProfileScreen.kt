package com.tuplataforma.encuestaapp.ui.screens.profile

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.tuplataforma.encuestaapp.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val prefs = context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)
    val votePrefs = context.getSharedPreferences("votos_prefs", Context.MODE_PRIVATE)
    
    val candidateVoted = votePrefs.getString("selected_candidate", "Aún no has votado")

    var showEditDialog by remember { mutableStateOf(false) }
    var showVoteDialog by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }

    // Estados de información personal
    var firstName by remember { mutableStateOf(user?.displayName?.split(" ")?.getOrNull(0) ?: "") }
    var lastName by remember { mutableStateOf(prefs.getString("last_name", "") ?: "") }
    var birthDate by remember { mutableStateOf(prefs.getString("birth_date", "") ?: "") }
    var gender by remember { mutableStateOf(prefs.getString("gender", "") ?: "") }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        profileImageUri = uri
    }

    // Configuración del DatePickerDialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            birthDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Información", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    
                    // Campo de Fecha de Nacimiento como "cuadrito" clickeable
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { },
                        label = { Text("Fecha de Nacimiento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { datePickerDialog.show() },
                        enabled = false, // Desactivamos escritura manual
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.clickable { datePickerDialog.show() })
                        }
                    )
                    
                    Text("Sexo", modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = gender == "Masculino", onClick = { gender = "Masculino" })
                        Text("Masculino")
                        Spacer(Modifier.width(8.dp))
                        RadioButton(selected = gender == "Femenino", onClick = { gender = "Femenino" })
                        Text("Femenino")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val fullName = "$firstName $lastName".trim()
                            val update = UserProfileChangeRequest.Builder().setDisplayName(fullName).build()
                            user?.updateProfile(update)?.await()
                            
                            prefs.edit().apply {
                                putString("last_name", lastName)
                                putString("birth_date", birthDate)
                                putString("gender", gender)
                                apply()
                            }
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD31124))
                ) {
                    Text("GUARDAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showVoteDialog) {
        AlertDialog(
            onDismissRequest = { showVoteDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HowToVote, null, tint = Color(0xFFD31124))
                    Spacer(Modifier.width(8.dp))
                    Text("Mi Intención de Voto", fontWeight = FontWeight.Bold)
                }
            },
            text = { 
                Column {
                    Text("Has registrado tu intención de voto por:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = candidateVoted ?: "No seleccionado",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFD31124)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showVoteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD31124))) {
                    Text("Entendido")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil Electoral", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFFBFBFB)).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(120.dp).border(3.dp, Color(0xFFD31124), CircleShape).padding(5.dp).clickable { pickerLauncher.launch("image/*") },
                    shape = CircleShape,
                    color = Color(0xFFF0F0F0)
                ) {
                    if (profileImageUri != null) {
                        AsyncImage(model = profileImageUri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    }
                }
                Surface(
                    modifier = Modifier.size(36.dp).offset(x = (-4).dp, y = (-4).dp).clickable { showEditDialog = true },
                    shape = CircleShape, color = Color(0xFFD31124), shadowElevation = 6.dp
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.padding(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = user?.displayName ?: "Usuario", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
            Text(text = user?.email ?: "", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showVoteDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFF9E6E9)) {
                        Icon(Icons.Default.HowToVote, null, tint = Color(0xFFD31124), modifier = Modifier.padding(10.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Mi Voto Registrado", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
                        Text("Ver el candidato seleccionado", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { auth.signOut(); onLogout() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9E6E9), contentColor = Color(0xFFD31124))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(12.dp))
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
