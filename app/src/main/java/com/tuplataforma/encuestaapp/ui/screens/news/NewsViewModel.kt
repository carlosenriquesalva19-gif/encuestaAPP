package com.tuplataforma.encuestaapp.ui.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NewsUiState(
    val newsList: List<NewsItem> = emptyList(),
    val isLoading: Boolean = false
)

class NewsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState

    init {
        fetchRealTimeNews()
    }

    fun fetchRealTimeNews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulación de conexión a internet para obtener noticias frescas
            delay(1200)

            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            
            val realTimeNews = listOf(
                NewsItem(
                    1,
                    "ELECCIONES 2026",
                    "JNE establece cronograma electoral para las elecciones generales",
                    "El organismo electoral definió las fechas clave para la inscripción de candidaturas y el inicio de la campaña oficial.",
                    "Actualizado hace 2 min",
                    "https://www.elperuano.pe/fotografia/thumbnail/2024/03/12/000244432M.jpg",
                    "https://www.elperuano.pe/noticia/238455-jne-cronograma-electoral-2026",
                    isFeatured = true
                ),
                NewsItem(
                    2,
                    "POLÍTICA",
                    "Partidos políticos inician alianzas estratégicas",
                    "Varios líderes de agrupaciones inscritas se reunieron hoy para discutir posibles coaliciones de cara al próximo año.",
                    "En vivo",
                    "https://imgmedia.larepublica.pe/640x377/larepublica/original/2024/01/15/65a581210f8c6742593b4904.webp",
                    "https://larepublica.pe/politica/actualidad"
                ),
                NewsItem(
                    3,
                    "ACTUALIDAD",
                    "ONPE: Así puedes verificar si eres miembro de mesa",
                    "La institución habilitó el link oficial para que ciudadanos consulten su rol en los próximos simulacros nacionales.",
                    "Hace 15 min",
                    "https://www.onpe.gob.pe/modRegistroMiembrosMesa/img/banner-registro.jpg",
                    "https://www.onpe.gob.pe/modElectoral/elecciones/2024/EG2024/"
                ),
                NewsItem(
                    4,
                    "ECONOMÍA",
                    "Impacto de las elecciones en la estabilidad del mercado",
                    "Analistas debaten sobre cómo el panorama político actual está influyendo en la inversión extranjera directa.",
                    "Hace 1 hora",
                    "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?q=80&w=800",
                    "https://elcomercio.pe/economia/peru/"
                ),
                NewsItem(
                    5,
                    "REGIONES",
                    "Voto electrónico se aplicará en 50 distritos adicionales",
                    "La ONPE confirmó la expansión tecnológica para agilizar el conteo de votos en zonas rurales de difícil acceso.",
                    "Hace 3 horas",
                    "https://images.unsplash.com/photo-1540910419892-f0e682355127?q=80&w=800",
                    "https://rpp.pe/politica/elecciones"
                ),
                NewsItem(
                    6,
                    "SOCIEDAD",
                    "Jóvenes de 18 años lideran intención de participación",
                    "Nuevos electores muestran un alto interés en las propuestas de reforma educativa y laboral de los candidatos.",
                    "Hace 5 horas",
                    "https://images.unsplash.com/photo-1529107386315-e1a2ed48a620?q=80&w=800",
                    "https://rpp.pe/politica"
                ),
                NewsItem(
                    7,
                    "REFORMAS",
                    "Debate sobre la bicameralidad entra en etapa decisiva",
                    "El Congreso sesiona hoy para definir los últimos detalles del retorno a las dos cámaras legislativas.",
                    "Hace 6 horas",
                    "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?q=80&w=800",
                    "https://larepublica.pe/politica/congreso"
                )
            )

            _uiState.value = NewsUiState(newsList = realTimeNews, isLoading = false)
        }
    }
}
