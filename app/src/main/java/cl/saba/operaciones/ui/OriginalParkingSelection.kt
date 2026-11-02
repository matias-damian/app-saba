package cl.saba.operaciones.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cl.saba.operaciones.BuildConfig
import cl.saba.operaciones.R
import cl.saba.operaciones.presentation.HomeState
import cl.saba.operaciones.presentation.SessionState

/**
 * Conserva la pantalla de estacionamientos del proyecto original:
 * logo, rol, lista real del repositorio, plazas, selección de radio,
 * estado de carga, errores/reintento, vista previa y cierre de sesión.
 * ÚNICO elemento añadido: botón para entrar al Home del estacionamiento
 * desde la selección original.
 */
@Composable
internal fun OriginalParkingSelection(
    state: HomeState,
    session: SessionState,
    onSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    onEnterHome: () -> Unit
) {
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (BuildConfig.PREVIEW_HOME) {
                    Text(
                        stringResource(R.string.preview),
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                } else {
                    IconButton(onClick = onSignOut, enabled = !session.isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, stringResource(R.string.sign_out))
                    }
                }
            }
            SabaLogo(
                Modifier.width(208.dp).align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 32.dp)
            )
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.operator_role),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(stringResource(R.string.parkings), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.select_parking),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val error = state.error ?: session.error
                if (error != null) {
                    Text(stringResource(error), color = MaterialTheme.colorScheme.error)
                }
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.parkings.isEmpty() && state.error != null -> TextButton(
                    onClick = onRetry,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text(stringResource(R.string.retry)) }
                else -> LazyColumn(
                    modifier = Modifier.weight(1f).selectableGroup(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.parkings, key = { it.id }) { parking ->
                        val selected = state.selectedId == parking.id
                        ListItem(
                            headlineContent = { Text(parking.name) },
                            supportingContent = { Text(parking.commune) },
                            leadingContent = { RadioButton(selected = selected, onClick = null) },
                            trailingContent = {
                                Text(
                                    pluralStringResource(R.plurals.parking_spaces, parking.spaces, parking.spaces),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.selectable(
                                selected = selected,
                                enabled = !state.isSaving,
                                role = Role.RadioButton,
                                onClick = { onSelect(parking.id) }
                            )
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
            // El acceso al Home se añade sin modificar la selección original.
            if (!state.isLoading && state.parkings.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Button(
                        onClick = onEnterHome,
                        enabled = state.selectedId != null && !state.isSaving &&
                            state.parkings.any { it.id == state.selectedId },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Entrar al Home del estacionamiento") }
                }
            }
        }
    }
}
