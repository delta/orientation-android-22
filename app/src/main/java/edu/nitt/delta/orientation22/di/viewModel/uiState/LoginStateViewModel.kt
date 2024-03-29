package edu.nitt.delta.orientation22.di.viewModel.uiState

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.nitt.delta.orientation22.di.viewModel.BaseViewModel
import edu.nitt.delta.orientation22.di.viewModel.actions.LoginAction
import edu.nitt.delta.orientation22.di.viewModel.repository.LoginRepository
import edu.nitt.delta.orientation22.models.IsRegisteredResponse
import edu.nitt.delta.orientation22.models.Result
import edu.nitt.delta.orientation22.models.auth.UserModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginState{
    IDLE,
    LOADING,
    ERROR,
    SUCCESS,
}

enum class DownloadState {
    IDLE,
    DOWNLOADING,
    ERROR,
    SUCCESS,
}


@HiltViewModel
class LoginStateViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : BaseViewModel<LoginAction>(){
    var user = UserModel(name = "", email = "")
    override fun doAction(action: LoginAction): Any = when(action) {
        is LoginAction.Login ->login(action.code)
        is LoginAction.IsLoggedIn -> isLoggedIn()
        is LoginAction.IsLoggedOut -> isLoggedOut()
        is LoginAction.IsRegistered -> isRegistered()
        is LoginAction.IsLive -> isLive()
        is LoginAction.DownloadAssets -> downloadAssets(action.urls, action.context)
        is LoginAction.IsDownloaded -> isDownloaded()
    }

    var uiState = mutableStateOf(LoginState.IDLE)
    var isRegistered = mutableStateOf<IsRegisteredResponse>(IsRegisteredResponse(false,"",0))
    var isLoggedIn = false
    var isAssetsDownloaded = false
    var isLive = mutableStateOf(false)
    var downloadState = mutableStateOf(DownloadState.IDLE)
    private fun login(code:String)=launch {
        uiState.value=LoginState.LOADING
        when(val res = loginRepository.Login(code)){
            is Result.Value-> {
                user = res.value
                isRegistered()
                isLive()

            }
            is Result.Error -> {
                mutableError.value = res.exception.message
                uiState.value=LoginState.ERROR
            }
        }
    }

    private fun isLoggedIn() = launch {
        when(val res = loginRepository.isLoggedInCheck()){
            is Result.Value -> {
                isLoggedIn = res.value
                if(isLoggedIn){
                    isRegistered()
                    isLive()
                    isDownloaded()
                }
            }
            is Result.Error -> mutableError.value = res.exception.message
        }
    }

    private fun isLoggedOut() = loginRepository.isLogOut()

    private fun isRegistered() =launch {
        when(val res= loginRepository.isRegistered()){
            is Result.Value -> {
                isRegistered.value=res.value
                uiState.value=LoginState.SUCCESS
                Log.v("1111",res.value.toString())
            }
            is Result.Error ->{
                mutableError.value = res.exception.message
                uiState.value=LoginState.ERROR
                Log.v("1111",res.exception.message.toString())
            }
        }
    }

    private fun isLive() = launch {
        when(val res = loginRepository.isLive()){
            is Result.Value -> isLive.value = res.value
            is Result.Error -> mutableError.value = res.exception.message
        }
    }

    private fun isDownloaded() = launch {
        when(val res = loginRepository.isDownloaded()){
            is Result.Value -> {
                isAssetsDownloaded = res.value
                if(isAssetsDownloaded){
                    downloadState.value = DownloadState.SUCCESS
                }
            }
            is Result.Error -> downloadState.value = DownloadState.ERROR
        }
    }

    private fun downloadAssets(urls: List<String>, context: Context) = launch {
        downloadState.value = DownloadState.DOWNLOADING
        isDownloaded()
        if (isAssetsDownloaded){
            downloadState.value = DownloadState.SUCCESS
        } else {
            when (val res = loginRepository.downloadAssets(urls, context)) {
                is Result.Value -> {
                    downloadState.value = DownloadState.SUCCESS
                    isAssetsDownloaded = true
                }
                is Result.Error -> {
                    downloadState.value = DownloadState.ERROR
                    mutableError.value = res.exception.message
                }
            }
        }
    }

}
