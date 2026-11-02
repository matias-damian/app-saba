package cl.saba.operaciones.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import cl.saba.operaciones.domain.Parking

/** Pantalla 2 del Figma: selección de parking. */
@Composable
internal fun ParkingPickerContent(
    parkings: List<Parking>,
    selectedId: String?,
    isLoading: Boolean,
    isSaving: Boolean,
    error: Int?,
    onRetry: () -> Unit,
    onSelect: (String) -> Unit,
    onContinue: () -> Unit,
    preview: Boolean
) {
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val filtered = parkings.filter { parking ->
        val matchesText = parking.name.contains(search, ignoreCase = true) ||
            parking.commune.contains(search, ignoreCase = true)
        val matchesFilter = filter == "Todos" || parking.commune.contains(filter, ignoreCase = true)
        matchesText && matchesFilter
    }
    val selected = parkings.firstOrNull { it.id == selectedId }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("¿Dónde vas a trabajar?", fontSize = 23.sp, fontWeight = FontWeight.SemiBold, color = SabaUiColors.ink)
        Text("Selecciona uno de los parkings asignados a tu cuenta.", fontSize = 13.sp, color = SabaUiColors.muted)

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por nombre o comuna") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Todos", "Santiago", "Providencia").forEach { item ->
                FilterChip(
                    selected = filter == item,
                    onClick = { filter = item },
                    label = { Text(if (item == "Todos") "Todos (${parkings.size})" else item) }
                )
            }
        }

        Text("Asignados a ti", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = SabaUiColors.ink)
        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filtered.isEmpty()) {
            Text("No encontramos parkings con esos filtros.", color = SabaUiColors.muted)
        } else {
            filtered.forEach { parking ->
                val selectedCard = selectedId == parking.id
                Card(
                    onClick = { if (!isSaving) onSelect(parking.id) },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, if (selectedCard) SabaUiColors.paleBlue else SabaUiColors.line),
                    colors = CardDefaults.cardColors(containerColor = if (selectedCard) SabaUiColors.paleBlue else Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(42.dp).background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("P", fontWeight = FontWeight.SemiBold, color = SabaUiColors.darkBlue) }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Parking ${parking.name}", fontWeight = FontWeight.SemiBold, color = SabaUiColors.ink, fontSize = 13.sp)
                            Text("${parking.commune} · ${parking.spaces} plazas registradas",
                                fontSize = 11.sp, color = SabaUiColors.muted)
                            StatusBadge(
                                "Sin datos de equipos", BadgeKind.INFO
                            )
                        }
                        Text("›", fontSize = 26.sp, color = SabaUiColors.muted)
                    }
                }
            }
        }

        if (error != null) {
            Text(stringResource(error), fontSize = 12.sp, color = SabaUiColors.red)
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }

        Button(
            onClick = onContinue,
            enabled = selected != null && !isSaving && !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SabaUiColors.darkBlue),
            shape = RoundedCornerShape(10.dp)
        ) { Text(if (selected != null) "Entrar a ${selected.name}" else "Selecciona un parking") }
        Text(
            "Esta lista procede del catálogo original. La selección se guarda en SQLite; los estados de equipos aún no están conectados.",
            fontSize = 11.sp,
            color = SabaUiColors.muted
        )
    }
}

/** Pantalla 3 del Figma: Home del parking seleccionado. */
@Composable
internal fun ParkingDashboardContent(
    name: String,
    commune: String,
    spaces: Int,
    preview: Boolean,
    onChangeParking: () -> Unit,
    onStatus: () -> Unit,
    onIncident: () -> Unit,
    onGuide: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = SabaUiColors.ink)
                Text("$commune · $spaces plazas registradas", fontSize = 12.sp, color = SabaUiColors.muted)
            }
            TextButton(onClick = onChangeParking) { Text("Cambiar", fontSize = 12.sp, color = SabaUiColors.darkBlue) }
        }

        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onStatus),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SabaUiColors.paleBlue)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Estado del parking", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = SabaUiColors.ink)
                    if (preview) StatusBadge("Degradado", BadgeKind.WARNING)
                }
                Text(
                    if (preview) "Operación parcial. Cajero N° 2 con falla; el resto del parking continúa funcionando."
                    else "El estado operacional en tiempo real aún no está disponible.",
                    fontSize = 13.sp,
                    color = SabaUiColors.ink
                )
                Text(
                    if (preview) "Información de ejemplo · Ver equipos ›" else "Ver equipos ›",
                    fontSize = 11.sp,
                    color = SabaUiColors.darkBlue
                )
            }
        }

        Button(
            onClick = onIncident,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SabaUiColors.darkBlue),
            shape = RoundedCornerShape(9.dp)
        ) { Text("＋  Reportar incidente") }

        SectionHeading("Incidentes activos", if (preview) "Ver todos · 2" else "Sin conexión")
        if (preview) {
            InfoCard("Cajero N° 2 · Sin vuelto", "INC-2026-0142 · 10:34 · Prioridad alta", "Escalado a SAU", BadgeKind.WARNING)
        } else {
            InfoCard("Sin información de incidentes", "Conecta el módulo de incidentes para ver registros.", null)
        }

        SectionHeading("Próximo mantenimiento", "Ver pauta")
        if (preview) {
            InfoCard("Chequeo semanal", "Hoy · 11:00 · Camila Rojas", "Pendiente", BadgeKind.INFO)
        } else {
            InfoCard("Sin información de mantenimiento", "No se han conectado las tareas de mantenimiento.", null)
        }

        SectionHeading("Todo lo que necesitas")
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SabaUiColors.line),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                SupportRow("▣", "Guías y protocolos", "Procedimientos y contingencias", onGuide)
                HorizontalDivider(color = SabaUiColors.line)
                SupportRow("◉", "Soporte y escalamiento", "Contacto SAU y responsables", onGuide)
            }
        }
        if (preview) Text("Vista de demostración: incidentes y tareas basados en el prototipo Figma.", color = SabaUiColors.muted, fontSize = 10.sp)
    }
}

/** Pantalla 4 del Figma: estado del parking y de sus equipos. */
@Composable
internal fun ParkingStatusContent(name: String, preview: Boolean, onRefresh: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFFF5F9FA)).verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusBadge(if (preview) "Parking degradado" else "Estado no disponible", BadgeKind.WARNING)
                Text(if (preview) "El parking sigue operando" else "Información no conectada",
                    fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = SabaUiColors.ink)
                Text(
                    if (preview) "Hay equipos con fallas parciales. Los equipos restantes permiten continuar la operación."
                    else "Todavía no existe una conexión con los equipos del parking $name.",
                    fontSize = 13.sp, color = SabaUiColors.muted
                )
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F7F3)), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(15.dp)) {
                Text(if (preview) "Sincronizado · 10:32 (ejemplo)" else "Sin sincronización configurada",
                    color = SabaUiColors.green, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("No se consultan equipos reales desde esta versión.", color = SabaUiColors.green, fontSize = 11.sp)
            }
        }
        Text("Equipos", color = SabaUiColors.ink, fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
            if (preview) {
                EquipmentRow("Barreras Entrada", "2 de 2 habilitadas · Accesos", "Operativo", BadgeKind.SUCCESS)
                HorizontalDivider(color = SabaUiColors.line)
                EquipmentRow("Lectura LPR", "Lectura intermitente · Revisión requerida", "Degradado", BadgeKind.WARNING)
                HorizontalDivider(color = SabaUiColors.line)
                EquipmentRow("Terminales POS", "2 de 2 habilitados · Frontdesk", "Operativo", BadgeKind.SUCCESS)
                HorizontalDivider(color = SabaUiColors.line)
                EquipmentRow("Cajero N° 2", "INC-2026-0142 · Sin vuelto", "Fuera de servicio", BadgeKind.ERROR)
            } else {
                Text("No hay un servicio de monitoreo de equipos conectado.", Modifier.padding(18.dp), color = SabaUiColors.muted)
            }
        }
        Text("Cómo leer los estados", color = SabaUiColors.ink, fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusExplanation("Operativo: funciona dentro de lo esperado.", BadgeKind.SUCCESS)
                StatusExplanation("Degradado: falla parcial con continuidad de servicio.", BadgeKind.WARNING)
                StatusExplanation("Fuera de servicio: requiere intervención.", BadgeKind.ERROR)
            }
        }
        OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("⟳  Actualizar estado", color = SabaUiColors.turquoise)
        }
        if (preview) Text("Vista previa de Figma · Estados y horarios ilustrativos.", color = SabaUiColors.muted, fontSize = 10.sp)
    }
}

internal enum class BadgeKind { SUCCESS, WARNING, ERROR, INFO }

@Composable
internal fun StatusBadge(label: String, kind: BadgeKind) {
    val foreground = when (kind) {
        BadgeKind.SUCCESS -> SabaUiColors.green
        BadgeKind.WARNING -> SabaUiColors.amber
        BadgeKind.ERROR -> SabaUiColors.red
        BadgeKind.INFO -> SabaUiColors.darkBlue
    }
    val background = when (kind) {
        BadgeKind.SUCCESS -> SabaUiColors.paleGreen
        BadgeKind.WARNING -> SabaUiColors.paleAmber
        BadgeKind.ERROR -> SabaUiColors.paleRed
        BadgeKind.INFO -> SabaUiColors.paleBlue
    }
    Text(
        label,
        modifier = Modifier.background(background, RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
        fontWeight = FontWeight.Medium,
        color = foreground,
        fontSize = 10.sp
    )
}

@Composable
private fun SectionHeading(title: String, detail: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = SabaUiColors.ink)
        if (detail != null) Text(detail, fontSize = 10.sp, color = SabaUiColors.darkBlue)
    }
}

@Composable
private fun InfoCard(title: String, subtitle: String, badge: String?, kind: BadgeKind = BadgeKind.INFO) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SabaUiColors.line),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = SabaUiColors.ink, fontSize = 13.sp)
            Text(subtitle, color = SabaUiColors.muted, fontSize = 11.sp)
            if (badge != null) StatusBadge(badge, kind)
        }
    }
}

@Composable
private fun SupportRow(symbol: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(symbol, fontSize = 20.sp, color = SabaUiColors.turquoise)
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = SabaUiColors.ink, fontSize = 13.sp)
            Text(subtitle, color = SabaUiColors.muted, fontSize = 11.sp)
        }
        Text("›", color = SabaUiColors.muted, fontSize = 21.sp)
    }
}

@Composable
private fun EquipmentRow(title: String, description: String, status: String, kind: BadgeKind) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(38.dp).background(SabaUiColors.paleBlue, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { Text("▣", color = SabaUiColors.turquoise, fontSize = 18.sp) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SabaUiColors.ink)
            Text(description, fontSize = 10.sp, color = SabaUiColors.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            StatusBadge(status, kind)
        }
    }
}

@Composable
private fun StatusExplanation(text: String, kind: BadgeKind) {
    val color = when (kind) {
        BadgeKind.SUCCESS -> SabaUiColors.green
        BadgeKind.WARNING -> SabaUiColors.amber
        BadgeKind.ERROR -> SabaUiColors.red
        BadgeKind.INFO -> SabaUiColors.darkBlue
    }
    Text("●  $text", color = color, fontSize = 12.sp)
}
