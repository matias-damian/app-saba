package cl.saba.operaciones.presentation

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.saba.operaciones.R
import cl.saba.operaciones.data.ParkingRepository
import cl.saba.operaciones.domain.Parking
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow

data class HomeState(val parkings: List<Parking> = emptyList(), val selectedId: String? = null, val isLoading: Boolean = true, val isSaving: Boolean = false, val error: Int? = null)

class HomeViewModel(private val repository: ParkingRepository) : ViewModel(){
    private val state = MutableStateFlow(HomeState())
    val uiState = state.asStateFlow()

    init{
        loadParkings()
    }

    fun loadParkings(){
        state.value = state.value.copy(isLoading = true, error = null)
        viewModelScope.launch{
            try{
                val catalog = repository.load()
                state.value = state.value.copy(parkings = catalog.parkings, selectedId = catalog.selectedId, isLoading = false)
            }catch (exception: CancellationException){
                throw exception
            }catch (exception: Exception){
                state.value = state.value.copy(isLoading = false, error = R.string.parking_error)
            }
        }
    }

    fun selectParking(parkingId: String){
        val current = state.value
        if (current.isSaving || parkingId == current.selectedId){
            return
        }
        if (current.parkings.none{ it.id == parkingId }){
            state.value = current.copy(error = R.string.invalid_parking)
            return
        }
        state.value = current.copy(isSaving = true, error = null)
        viewModelScope.launch{
            try{
                repository.select(parkingId)
                state.value = state.value.copy(selectedId = parkingId, isSaving = false)
            }catch (exception: CancellationException){
                throw exception
            }catch (exception: Exception){
                state.value = state.value.copy(isSaving = false, error = R.string.selection_error)
            }
        }
    }

    override fun onCleared(){
        repository.close()
    }
}
