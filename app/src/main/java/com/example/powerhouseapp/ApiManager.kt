package com.example.powerhouseapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.com.retrofitwithrecyclerviewkotlin.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ApiManager @Inject constructor(){

    private val _userConfigResponse: MutableLiveData<UserConfigResponse> by lazy { MutableLiveData() }
    val userConfigResponse: LiveData<UserConfigResponse> by lazy { _userConfigResponse }

    fun getUserConfig(emailId: String){
        val apiInterface = ApiInterface.create().getUserConfig(UserConfigRequest(emailId))

        //apiInterface.enqueue( Callback<List<Movie>>())
        apiInterface.enqueue( object : Callback<UserConfigResponse> {
            override fun onResponse(call: Call<UserConfigResponse>?, response: Response<UserConfigResponse>?) {
                Log.d("MainActivity", "$response")

                response?.body().let {
                    _userConfigResponse.postValue(it?.apply { isSuccess = true })
                }
            }

            override fun onFailure(call: Call<UserConfigResponse>?, t: Throwable?) {
                _userConfigResponse.postValue(UserConfigResponse(isSuccess = false))
            }
        })
    }
}