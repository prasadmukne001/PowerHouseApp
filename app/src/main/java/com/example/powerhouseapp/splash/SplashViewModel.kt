package com.example.powerhouseapp.splash

import androidx.lifecycle.ViewModel
import com.example.powerhouseapp.network.ApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val apiManager: ApiManager
) : ViewModel() {

    fun getUserConfig(emailId: String) {
        apiManager.getUserConfig(emailId)
    }
}