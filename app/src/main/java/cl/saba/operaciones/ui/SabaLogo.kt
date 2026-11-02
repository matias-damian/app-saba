package cl.saba.operaciones.ui

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.saba.operaciones.R

@Composable
fun SabaLogo(modifier: Modifier = Modifier){
    Image(painterResource(R.drawable.saba_logo), stringResource(R.string.app_name), modifier = modifier)
}
