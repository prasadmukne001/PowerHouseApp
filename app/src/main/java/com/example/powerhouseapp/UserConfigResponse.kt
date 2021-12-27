package com.example.powerhouseapp

import com.google.gson.annotations.SerializedName

data class UserConfigResponse(

    @SerializedName("dO_PO") val doPo: String = "",
    @SerializedName("camera") val camera: String = "",
    @SerializedName("code") val code: Int = 0,
    @SerializedName("count_text") val count: String = "",
    @SerializedName("enable_manual") val enableManual: String = "",
    @SerializedName("enable_multiply") val enableMultiply: String = "",
    @SerializedName("location") val location: String = "",
    @SerializedName("photo_count") val photoCount: String = "",
    @SerializedName("isSuccess") var isSuccess: Boolean

)