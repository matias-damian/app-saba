package cl.saba.operaciones.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.saba.operaciones.R
import cl.saba.operaciones.presentation.SessionState

@Composable
fun SignInScreen(state: SessionState, isConfigured: Boolean, onSignIn: () -> Unit){
    Scaffold{ padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center){
            Column(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ){
                SabaLogo(Modifier.width(240.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)){
                    OutlinedButton(
                        onClick = onSignIn,
                        enabled = isConfigured && !state.isLoading,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ){
                        if (state.isLoading){
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        }else{
                            Icon(painterResource(R.drawable.ic_microsoft), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified)
                        }
                        Text(stringResource(R.string.sign_in_microsoft), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                    state.error?.let{ error ->
                        Text(stringResource(error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
