package cl.saba.operaciones.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.saba.operaciones.BuildConfig
import cl.saba.operaciones.presentation.HomeState
import cl.saba.operaciones.presentation.SessionState

internal object SabaUiColors {
    val turquoise = Color(0xFF009D93)
    val darkBlue = Color(0xFF17627A)
    val ink = Color(0xFF23353D)
    val muted = Color(0xFF70848C)
    val line = Color(0xFFDDE8EA)
    val paleBlue = Color(0xFFEAF6F8)
    val background = Color(0xFFFFFFFF)
    val paleGreen = Color(0xFFE8F7EF)
    val green = Color(0xFF15805B)
    val paleAmber = Color(0xFFFFF4DE)
    val amber = Color(0xFFA76D0F)
    val paleRed = Color(0xFFFFE9E8)
    val red = Color(0xFFBC4F52)
}

private enum class HomePage { ORIGINAL, DASHBOARD, STATUS, INCIDENTS, GUIDES, ACCOUNT }

/**
 * Punto de entrada SIN cambiar el contrato que ya usa SabaApp.
 * La pantalla original permanece como inicio y utiliza el catálogo real
 * y el ViewModel/repo de SQLite, también en la variante preview.
 */
@Composable
fun HomeScreen(
    state: HomeState,
    session: SessionState,
    onSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onSignOut: () -> Unit
) {
    val preview = BuildConfig.PREVIEW_HOME
    val selectedParking = state.parkings.firstOrNull { it.id == state.selectedId }
    var screen by rememberSaveable { mutableStateOf(HomePage.ORIGINAL.name) }
    var dialogMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val requestedPage = HomePage.entries.firstOrNull { it.name == screen } ?: HomePage.ORIGINAL

    // Si se borra una selección, no se muestra un Home con datos inexistentes.
    val page = if (selectedParking == null && requestedPage != HomePage.ORIGINAL)
        HomePage.ORIGINAL else requestedPage

    AnimatedContent(
        targetState = (page == HomePage.ORIGINAL),
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(animationSpec = tween(350), initialOffsetX = { -it }) + fadeIn(tween(350)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it }) + fadeOut(tween(300)))
            } else {
                (slideInHorizontally(animationSpec = tween(350), initialOffsetX = { it }) + fadeIn(tween(350)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it }) + fadeOut(tween(300)))
            }
        },
        label = "PickerToDashboardTransition"
    ) { isOriginalPage ->
        if (isOriginalPage) {
            OriginalParkingSelection(
                state = state,
                session = session,
                onSelect = onSelect,
                onRetry = onRetry,
                onSignOut = onSignOut,
                onEnterHome = { if (selectedParking != null && !state.isSaving) screen = HomePage.DASHBOARD.name }
            )
        } else {
            Scaffold(
                containerColor = SabaUiColors.background,
                topBar = {
                    SabaPrototypeHeader(
                        subtitle = when (page) {
                            HomePage.STATUS -> "Parking ${selectedParking?.name.orEmpty()}"
                            else -> if (preview) "Operación Terreno · Vista previa" else "Operación Terreno"
                        },
                        showBack = page == HomePage.STATUS ||
                                page == HomePage.INCIDENTS || page == HomePage.GUIDES || page == HomePage.ACCOUNT,
                        onBack = {
                            screen = HomePage.DASHBOARD.name
                        },
                        onBell = { dialogMessage = "No existe un servicio de notificaciones conectado." }
                    )
                },
                bottomBar = {
                    SabaBottomNavigation(page) { screen = it.name }
                }
            ) { insets ->
                Box(Modifier.fillMaxSize().padding(insets)) {
                    AnimatedContent(
                        targetState = page,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.92f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)))
                                .togetherWith(scaleOut(targetScale = 1.08f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)))
                        },
                        label = "DashboardToStatusZoomTransition"
                    ) { targetPage ->
                        when (targetPage) {
                            HomePage.ORIGINAL -> Unit // Tratado fuera de este Scaffold.
                            HomePage.DASHBOARD -> ParkingDashboardContent(
                                name = selectedParking?.name.orEmpty(),
                                commune = selectedParking?.commune.orEmpty(),
                                spaces = selectedParking?.spaces ?: 0,
                                preview = preview,
                                onChangeParking = { screen = HomePage.ORIGINAL.name },
                                onStatus = { screen = HomePage.STATUS.name },
                                onIncident = { screen = HomePage.INCIDENTS.name },
                                onGuide = { screen = HomePage.GUIDES.name }
                            )
                            HomePage.STATUS -> ParkingStatusContent(
                                name = selectedParking?.name.orEmpty(),
                                preview = preview,
                                onRefresh = {
                                    dialogMessage = "Todavía no existe un servicio de monitoreo de equipos conectado. " +
                                            "Los estados del prototipo son ejemplos."
                                }
                            )
                            HomePage.INCIDENTS -> UnimplementedSection(
                                "Incidentes", "La consulta y registro de incidentes aún no están conectados. " +
                                        "El Home muestra únicamente ejemplos del prototipo Figma."
                            )
                            HomePage.GUIDES -> UnimplementedSection(
                                "Guías", "El módulo de guías y soporte todavía no está implementado. " +
                                        "Se ha conservado el resto de la aplicación."
                            )
                            HomePage.ACCOUNT -> AccountSection(preview, session.isLoading, onSignOut)
                        }
                    }
                }
            }
        }
    }

    dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            title = { Text("Saba") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { dialogMessage = null }) { Text("Entendido") } }
        )
    }
}

@Composable
internal fun SabaPrototypeHeader(
    subtitle: String,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    onBell: () -> Unit = {}
) {
    Column(Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                TextButton(onClick = onBack, modifier = Modifier.width(42.dp)) {
                    Text("‹", fontSize = 32.sp, color = SabaUiColors.ink)
                }
            }
            Column(Modifier.weight(1f)) {
                SabaLogo(Modifier.width(104.dp).height(36.dp))
                Text(subtitle, color = SabaUiColors.muted, fontSize = 10.sp, maxLines = 1)
            }
            TextButton(onClick = onBell) { Text("🔔", fontSize = 19.sp) }
        }
        HorizontalDivider(thickness = 2.dp, color = SabaUiColors.turquoise)
    }
}

@Composable
private fun SabaBottomNavigation(page: HomePage, onNavigate: (HomePage) -> Unit) {
    val entries = listOf(
        Triple(HomePage.DASHBOARD, "Inicio", "⌂"),
        Triple(HomePage.INCIDENTS, "Incidentes", "▤"),
        Triple(HomePage.GUIDES, "Guías", "▣"),
        Triple(HomePage.ACCOUNT, "Cuenta", "♙")
    )
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        entries.forEach { (destination, title, symbol) ->
            val selected = page == destination ||
                    (page == HomePage.STATUS && destination == HomePage.DASHBOARD)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Text(symbol, fontSize = 22.sp,
                        color = if (selected) SabaUiColors.turquoise else SabaUiColors.muted)
                },
                label = { Text(title, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = SabaUiColors.turquoise,
                    unselectedTextColor = SabaUiColors.muted,
                    indicatorColor = SabaUiColors.paleBlue
                )
            )
        }
    }
}

@Composable
private fun UnimplementedSection(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = SabaUiColors.ink)
        Text(message, color = SabaUiColors.muted, fontSize = 14.sp)
    }
}

@Composable
private fun AccountSection(preview: Boolean, isLoading: Boolean, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mi cuenta", fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = SabaUiColors.ink)
        if (preview) {
            Text("Estás utilizando la versión de demostración. No hay una sesión Microsoft iniciada.", color = SabaUiColors.muted)
        } else {
            Text("Sesión corporativa activa", color = SabaUiColors.muted)
            TextButton(onClick = onSignOut, enabled = !isLoading) { Text("Cerrar sesión") }
        }
    }
}
