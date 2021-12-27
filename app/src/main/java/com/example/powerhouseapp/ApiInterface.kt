package app.com.retrofitwithrecyclerviewkotlin

import com.example.powerhouseapp.UserConfigRequest
import com.example.powerhouseapp.UserConfigResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {

    @Headers("Content-Type: application/json")
    @POST("/config")
    fun getUserConfig(@Body userConfigRequest: UserConfigRequest) : Call<UserConfigResponse>

    companion object {

        var BASE_URL = "https://countgo-n34mdgp5qq-as.a.run.app"

        fun create() : ApiInterface {

            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()
            return retrofit.create(ApiInterface::class.java)

        }
    }
}