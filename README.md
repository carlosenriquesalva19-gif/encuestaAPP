#🗳️ EncuestaAPP - Elecciones Perú 2026

**EncuestaAPP** es una plataforma móvil moderna desarrollada en **Android (Kotlin + Jetpack Compose)** diseñada para realizar simulacros de votación en tiempo real. La aplicación conecta a los ciudadanos con la base de datos de Firebase para ofrecer resultados instantáneos.

##  Características Principales

###  Para Usuarios (Ciudadanos)
*   **Autenticación Segura**: Registro e inicio de sesión con Firebase Auth.
*   **Perfil Personalizable**: Edición de datos (Nombre, Apellido, Fecha de Nacimiento con selector de calendario y Sexo) y foto de perfil.
*   **Votación Real**: Interfaz para seleccionar candidatos reales. El voto se guarda de forma única por usuario en la nube.
*   **Transparencia**: Consulta en el perfil qué voto ha sido registrado.

###  Para Administradores
*   **Resultados en Vivo**: Panel que escucha los votos en tiempo real mediante Firestore Snapshots.
*   **Ranking Dinámico**: Gráficos de barras que muestran quién va ganando porcentualmente.
*   **Estadísticas**: Contador total de votos y detección automática del candidato líder.
*   **Seguridad**: Acceso restringido mediante roles definidos en la base de datos.

##  Tecnologías Utilizadas

*   **Android SDK**: Nivel 35 (Target).
*   **UI**: Jetpack Compose con Material Design 3.
*   **Backend**: 
    *   **Firebase Auth**: Control de usuarios.
    *   **Cloud Firestore**: Base de datos NoSQL en tiempo real para votos y roles.
*   **Librerías**:
    *   **Coil**: Para cargar las fotos de los candidatos.
    *   **Navigation Compose**: Para el flujo entre pantallas.
    *   **Kotlin Coroutines**: Para procesos asíncronos con la base de datos.

##  Configuración de Administrador

Para habilitar una cuenta como Administrador, sigue estos pasos en tu consola de Firebase:

1.  Ve a **Firestore Database**.
2.  Crea una colección llamada `users`.
3.  Crea un documento cuyo **ID** sea el **UID** del usuario (el que sale en Authentication).
4.  Agrega el campo: `role` (tipo String) con el valor `admin`.

##  Estructura del Código

*   `ui.screens.vote`: Lógica para emitir el voto y enviarlo a Firebase.
*   `ui.screens.admin`: Panel de control que suma y muestra los resultados reales.
*   `ui.screens.profile`: Gestión de datos personales con selector de fecha y edición.
*   `MainActivity.kt`: El "cerebro" que decide si mandarte a la vista de Admin o Usuario según tu rol.

