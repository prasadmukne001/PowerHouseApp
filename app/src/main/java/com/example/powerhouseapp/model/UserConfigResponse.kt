package com.example.powerhouseapp.model

import com.google.gson.annotations.SerializedName

data class UserConfigResponse(

    @SerializedName("DO_PO") val dO_PO: Boolean = false,
    @SerializedName("camera") val camera: Boolean = false,
    @SerializedName("code") val code: Int,
    @SerializedName("count_text") val count_text: List<String> = emptyList(),
    @SerializedName("enable_manual") val enable_manual: List<Int> = emptyList(),
    @SerializedName("enable_multiply") val enable_multiply: List<Int> = emptyList(),
    @SerializedName("location") val location: Boolean = false,
    @SerializedName("photo_count") val photo_count: Int = 0,

    )