package cl.saba.operaciones.ui

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.runtime.Composable
import cl.saba.operaciones.BuildConfig
import cl.saba.operaciones.presentation.HomeViewModel
import cl.saba.operaciones.presentation.SessionViewModel

@Composable
fun SabaApp(session: SessionViewModel, home: HomeViewModel, onSignIn: () -> Unit){
    val sessionState by session.uiState.collectAsStateWithLifecycle()
    val homeState by home.uiState.collectAsStateWithLifecycle()
    val showHome = BuildConfig.PREVIEW_HOME || sessionState.isSignedIn
    if (showHome){
        HomeScreen(homeState, sessionState, home::selectParking, home::loadParkings, session::signOut)
    }else{
        SignInScreen(sessionState, session.isConfigured, onSignIn)
    }
}
