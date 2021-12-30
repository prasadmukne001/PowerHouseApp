package com.example.powerhouseapp.camera

import androidx.lifecycle.ViewModel
import com.example.powerhouseapp.network.ApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    val apiManager: ApiManager
) : ViewModel() {

}