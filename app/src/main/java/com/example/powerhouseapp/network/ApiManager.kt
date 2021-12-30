package com.example.powerhouseapp.network

import android.app.Application
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.powerhouseapp.R
import com.example.powerhouseapp.model.ImageUploadResponse
import com.example.powerhouseapp.model.UserConfigRequest
import com.example.powerhouseapp.model.UserConfigResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject

private const val TAG = "ApiManager"

class ApiManager @Inject constructor(application: Application) {

    private val _userConfigResponse: MutableLiveData<UserConfigResponse> by lazy { MutableLiveData() }
    val userConfigResponse: LiveData<UserConfigResponse> by lazy { _userConfigResponse }

    private val _imageUploadResponse: MutableLiveData<ImageUploadResponse> by lazy { MutableLiveData() }
    val imageUploadResponse: LiveData<ImageUploadResponse> by lazy { _imageUploadResponse }

    private val application: Application = application

    fun getUserConfig(emailId: String) {
        val apiInterface = ApiInterface.create().getUserConfig(UserConfigRequest(emailId))

        //apiInterface.enqueue( Callback<List<Movie>>())
        apiInterface.enqueue(object : Callback<UserConfigResponse> {
            override fun onResponse(
                call: Call<UserConfigResponse>?,
                response: Response<UserConfigResponse>?
            ) {
                Log.d(TAG, "$response")

                response?.body().let {
                    _userConfigResponse.postValue(it)
                }
            }

            override fun onFailure(call: Call<UserConfigResponse>?, t: Throwable?) {
                _userConfigResponse.postValue(UserConfigResponse(code = -1))
            }
        })
    }

    fun uploadImage(emailId: String, file: File, isDepthImage: Boolean) {

        val body: MultipartBody.Part = MultipartBody.Part.createFormData(
            "file",
            "file.jpg",
            file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
        )


        val apiInterface = if (!isDepthImage) ApiInterface.create()
            .uploadImage(emailId, body) else ApiInterface.create().uploadDepth(emailId, body)

        apiInterface.enqueue(object : Callback<ImageUploadResponse> {
            override fun onResponse(
                call: Call<ImageUploadResponse>?,
                response: Response<ImageUploadResponse>?
            ) {
                Log.d(TAG, "$response")
                response?.body().let {
                    if (it?.code == 1) {
                        Toast.makeText(
                            application.applicationContext,
                            application.getString(R.string.image_uploaded),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            application.applicationContext,
                            it?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    _imageUploadResponse.postValue(it)
                }
            }

            override fun onFailure(call: Call<ImageUploadResponse>?, t: Throwable?) {
                Log.d(TAG, "error---${t?.printStackTrace()}")
                _imageUploadResponse.postValue(ImageUploadResponse())
            }
        })
    }

    private fun getMimeType(file: File): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}