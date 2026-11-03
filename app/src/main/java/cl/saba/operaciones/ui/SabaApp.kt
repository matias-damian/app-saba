package cl.saba.operaciones.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.saba.operaciones.BuildConfig
import cl.saba.operaciones.presentation.HomeViewModel
import cl.saba.operaciones.presentation.SessionViewModel

@Composable
fun SabaApp(session: SessionViewModel, home: HomeViewModel, onSignIn: () -> Unit) {
    val sessionState by session.uiState.collectAsStateWithLifecycle()
    val homeState by home.uiState.collectAsStateWithLifecycle()
    val showHome = BuildConfig.PREVIEW_HOME || sessionState.isSignedIn

    AnimatedContent(
        targetState = showHome,
        transitionSpec = {
            (scaleIn(initialScale = 0.92f, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))) togetherWith
                    (scaleOut(targetScale = 1.08f, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)))
        },
        label = "ScaleTransition"
    ) { targetShowHome ->
        if (targetShowHome) {
            HomeScreen(
                homeState,
                sessionState,
                home::selectParking,
                home::loadParkings,
                onSignOut = session::signOut
            )
        } else {
            SignInScreen(sessionState, session.isConfigured, onSignIn)
        }
    }
}
