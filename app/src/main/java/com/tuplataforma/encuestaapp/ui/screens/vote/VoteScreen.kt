package com.tuplataforma.encuestaapp.ui.screens.vote

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Candidate(
    val id: Int,
    val name: String,
    val party: String,
    val partyColor: Color,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    
    val candidates = remember {
        listOf(
            Candidate(1, "Alfredo Barnechea", "Acción Popular", Color(0xFFD31124), "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Alfredo_Barnechea_Garc%C3%ADa.jpg/220px-Alfredo_Barnechea_Garc%C3%ADa.jpg"),
            Candidate(2, "Alfonso López-Chau", "Ahora Nación", Color(0xFF1A233E), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_6uM-J7yP6P_r5jR_zH_f_X_vX_vX_vX_vX&s"),
            Candidate(3, "César Acuña Peralta", "Alianza para el Progreso", Color(0xFF004B8D), "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/C%C3%A9sar_Acu%C3%B1a_Peralta.jpg/220px-C%C3%A9sar_Acu%C3%B1a_Peralta.jpg"),
            Candidate(12, "Keiko Fujimori", "Fuerza Popular", Color(0xFFFFA500), "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Keiko_Fujimori_2016.jpg/220px-Keiko_Fujimori_2016.jpg"),
            Candidate(24, "Martín Vizcarra", "Partido Político Perú Primero", Color(0xFF03A9F4), "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Mart%C3%ADn_Vizcarra_Cornejo.jpg/220px-Mart%C3%ADn_Vizcarra_Cornejo.jpg"),
            Candidate(40, "Rafael López Aliaga", "Renovación Popular", Color(0xFF29B6F6), "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Rafael_L%C3%B3pez_Aliaga_2021.jpg/220px-Rafael_L%C3%B3pez_Aliaga_2021.jpg")
        )
    }

    var selectedId by remember { mutableStateOf<Int?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    var isVoting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(24.dp), shape = RoundedCornerShape(4.dp), color = Color(0xFFD31124)) {
                            Icon(Icons.Default.HowToVote, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Elecciones Perú 2026", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA))) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Simulacro de Votación", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Selecciona a un candidato para registrar tu intención de voto real.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                items(candidates) { candidate ->
                    CandidateCard(candidate, selectedId == candidate.id) { selectedId = candidate.id }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    val isBlanco = selectedId == 0
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { selectedId = 0 },
                        shape = RoundedCornerShape(12.dp), color = Color.White,
                        border = BorderStroke(1.dp, if (isBlanco) Color(0xFFD31124) else Color(0xFFE0E0E0))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, null, tint = Color.Gray)
                            Spacer(Modifier.width(16.dp))
                            Text("Voto en Blanco", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Icon(if (isBlanco) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (isBlanco) Color(0xFFD31124) else Color.LightGray)
                        }
                    }
                }
            }

            if (isVoting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Button(
                    onClick = { showConfirmation = true },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD31124)),
                    enabled = selectedId != null
                ) {
                    Text("EMITIR VOTO REAL", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirmar Voto") },
            text = { Text("¿Deseas registrar este voto en la base de datos central?") },
            confirmButton = {
                TextButton(onClick = {
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    scope.launch {
                        isVoting = true
                        showConfirmation = false
                        try {
                            val candidateName = if (selectedId == 0) "Voto en Blanco" else candidates.find { it.id == selectedId }?.name ?: "Desconocido"
                            
                            val voteData = hashMapOf(
                                "userId" to userId,
                                "candidateName" to candidateName,
                                "candidateId" to selectedId,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )

                            // Guardamos el voto en Firestore
                            db.collection("votes").document(userId).set(voteData).await()
                            
                            // También guardamos local para el perfil
                            context.getSharedPreferences("votos_prefs", Context.MODE_PRIVATE)
                                .edit().putString("selected_candidate", candidateName).apply()
                            
                            Toast.makeText(context, "¡Voto registrado con éxito!", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isVoting = false
                        }
                    }
                }) { Text("Confirmar", color = Color(0xFFD31124)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun CandidateCard(candidate: Candidate, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFFD31124)) else BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column {
            AsyncImage(model = candidate.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop)
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = candidate.name, fontWeight = FontWeight.Bold)
                    Text(text = candidate.party, color = Color(0xFFD31124), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Icon(if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null, tint = if (isSelected) Color(0xFFD31124) else Color.LightGray)
            }
        }
    }
}
