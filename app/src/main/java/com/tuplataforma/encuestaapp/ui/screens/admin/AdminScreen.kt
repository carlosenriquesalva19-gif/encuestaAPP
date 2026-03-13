package com.tuplataforma.encuestaapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onLogout: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    
    // Estados para los datos reales de los votos
    var totalVotes by remember { mutableLongStateOf(0L) }
    var candidateStats by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar votos de Firestore en tiempo real
    LaunchedEffect(Unit) {
        db.collection("votes").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            
            val stats = mutableMapOf<String, Int>()
            snapshot.documents.forEach { doc ->
                val name = doc.getString("candidateName") ?: "Otros"
                stats[name] = stats.getOrDefault(name, 0) + 1
            }
            candidateStats = stats
            totalVotes = snapshot.size().toLong()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(24.dp), shape = RoundedCornerShape(4.dp), color = Color(0xFFD31124)) {
                            Icon(Icons.Default.BarChart, null, tint = Color.White, modifier = Modifier.padding(2.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resultados Reales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) 
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, null, tint = Color(0xFFD31124))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFD31124))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Tarjeta del Líder Actual (Real)
                val leader = candidateStats.maxByOrNull { it.value }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD31124))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("CANDIDATO LÍDER ACTUAL", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(leader?.key ?: "Sin votos", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val leaderPercent = if (totalVotes > 0) (leader?.value?.toFloat() ?: 0f) / totalVotes * 100 else 0f
                        Text("${leader?.value ?: 0} votos (${String.format("%.1f", leaderPercent)}%)", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("CONTEO POR CANDIDATO", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                // Lista Dinámica de Candidatos con Votos Reales
                candidateStats.forEach { (name, count) ->
                    val percent = if (totalVotes > 0) count.toFloat() / totalVotes else 0f
                    VotoProgresoRealItem(name, percent, "${(percent * 100).toInt()}%", count)
                }

                if (candidateStats.isEmpty()) {
                    Text("Esperando los primeros votos...", modifier = Modifier.padding(16.dp), color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Estadísticas Generales
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EstadisticaSimpleCard("TOTAL VOTOS", totalVotes.toString(), Modifier.weight(1f))
                    EstadisticaSimpleCard("ACTUALIZADO", "En vivo", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun VotoProgresoRealItem(nombre: String, progreso: Float, porcentaje: String, votos: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(nombre, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("$votos votos ($porcentaje)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFD31124))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = Color(0xFFD31124),
                trackColor = Color(0xFFF0F0F0)
            )
        }
    }
}

@Composable
fun EstadisticaSimpleCard(titulo: String, valor: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD31124))
        }
    }
}
