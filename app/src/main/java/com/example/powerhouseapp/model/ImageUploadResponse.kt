package com.example.powerhouseapp.model

import com.google.gson.annotations.SerializedName

data class ImageUploadResponse(

    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",

    )