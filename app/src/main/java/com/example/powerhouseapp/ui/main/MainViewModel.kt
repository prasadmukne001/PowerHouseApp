package com.example.powerhouseapp.ui.main

import androidx.lifecycle.ViewModel
import com.example.powerhouseapp.ApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val apiManager: ApiManager
) : ViewModel() {

    fun getUserConfig(emailId: String){
        apiManager.getUserConfig(emailId)
    }
}