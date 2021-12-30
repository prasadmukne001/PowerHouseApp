package com.example.powerhouseapp.model

import com.google.gson.annotations.SerializedName

data class UserConfigRequest(
    @SerializedName("email_id") val userEmail: String
)
