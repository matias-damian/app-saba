package cl.saba.operaciones.presentation

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.saba.operaciones.BuildConfig
import cl.saba.operaciones.R
import cl.saba.operaciones.data.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow

data class SessionState(val isLoading: Boolean = true, val isSignedIn: Boolean = false, val error: Int? = null)

class SessionViewModel(private val repository: AuthRepository) : ViewModel(){
    private val state = MutableStateFlow(SessionState())
    val isConfigured = repository.isConfigured
    val uiState = state.asStateFlow()

    init{
        viewModelScope.launch{
            try{
                val signedIn = if (BuildConfig.PREVIEW_HOME) false else repository.restore()
                state.value = SessionState(isLoading = false, isSignedIn = signedIn)
            }catch (exception: CancellationException){
                throw exception
            }catch (exception: Exception){
                state.value = SessionState(isLoading = false, error = R.string.auth_error)
            }
        }
    }

    fun signIn(activity: Activity){
        if (state.value.isLoading || !isConfigured){
            return
        }
        state.value = state.value.copy(isLoading = true, error = null)
        viewModelScope.launch{
            try{
                state.value = SessionState(isLoading = false, isSignedIn = repository.signIn(activity))
            }catch (exception: CancellationException){
                throw exception
            }catch (exception: Exception){
                state.value = SessionState(isLoading = false, error = R.string.auth_error)
            }
        }
    }

    fun signOut(){
        if (state.value.isLoading){
            return
        }
        state.value = state.value.copy(isLoading = true, error = null)
        viewModelScope.launch{
            try{
                repository.signOut()
                state.value = SessionState(isLoading = false)
            }catch (exception: CancellationException){
                throw exception
            }catch (exception: Exception){
                state.value = state.value.copy(isLoading = false, error = R.string.sign_out_error)
            }
        }
    }
}
