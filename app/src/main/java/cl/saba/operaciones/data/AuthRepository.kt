package cl.saba.operaciones.data

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

import android.app.Activity
import android.content.Context
import android.net.Uri
import cl.saba.operaciones.BuildConfig
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException
import java.io.File
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import org.json.JSONObject

class AuthRepository(private val context: Context){
    private var application: ISingleAccountPublicClientApplication? = null

    val isConfigured: Boolean
        get() = BuildConfig.MICROSOFT_CLIENT_ID.isNotBlank() && BuildConfig.MICROSOFT_SIGNATURE_HASH.isNotBlank()

    suspend fun restore(): Boolean{
        if (!isConfigured){
            return false
        }
        return withContext(Dispatchers.IO){
            val authority = JSONObject()
                .put("type", "AAD")
                .put("default", true)
                .put("audience", JSONObject().put("type", "AzureADandPersonalMicrosoftAccount"))
            val redirectUri = Uri.Builder().scheme("msauth").authority(context.packageName).appendPath(BuildConfig.MICROSOFT_SIGNATURE_HASH).build()
            val config = JSONObject()
                .put("client_id", BuildConfig.MICROSOFT_CLIENT_ID)
                .put("redirect_uri", redirectUri.toString())
                .put("account_mode", "SINGLE")
                .put("broker_redirect_uri_registered", false)
                .put("authorities", JSONArray().put(authority))
            val configFile = File(context.filesDir, "microsoft.json")
            configFile.writeText(config.toString())
            val client = PublicClientApplication.createSingleAccountPublicClientApplication(context, configFile)
            application = client
            client.currentAccount.currentAccount != null
        }
    }

    suspend fun signIn(activity: Activity): Boolean{
        if (application == null){
            restore()
        }
        val client = checkNotNull(application)
        return suspendCancellableCoroutine{ continuation ->
            val parameters = SignInParameters.builder()
                .withActivity(activity)
                .withScopes(listOf("User.Read"))
                .withCallback(object : AuthenticationCallback{
                    override fun onSuccess(result: IAuthenticationResult){
                        if (continuation.isActive){
                            continuation.resume(true)
                        }
                    }

                    override fun onError(exception: MsalException){
                        if (continuation.isActive){
                            continuation.resumeWithException(exception)
                        }
                    }

                    override fun onCancel(){
                        if (continuation.isActive){
                            continuation.resume(false)
                        }
                    }
                })
                .build()
            client.signIn(parameters)
        }
    }

    suspend fun signOut(){
        withContext(Dispatchers.IO){
            check(application?.signOut() == true)
        }
    }
}
