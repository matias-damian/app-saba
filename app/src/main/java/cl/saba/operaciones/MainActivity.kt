package cl.saba.operaciones

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel

import android.os.Bundle
import androidx.activity.ComponentActivity
import cl.saba.operaciones.data.AuthRepository
import cl.saba.operaciones.data.ParkingRepository
import cl.saba.operaciones.presentation.HomeViewModel
import cl.saba.operaciones.presentation.SessionViewModel
import cl.saba.operaciones.ui.SabaApp
import cl.saba.operaciones.ui.theme.SabaTheme

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            val session: SessionViewModel = viewModel{ SessionViewModel(AuthRepository(applicationContext)) }
            val home: HomeViewModel = viewModel{ HomeViewModel(ParkingRepository(applicationContext)) }
            SabaTheme{
                SabaApp(session, home, onSignIn = { session.signIn(this) })
            }
        }
    }
}
