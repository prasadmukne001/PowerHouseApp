package com.example.powerhouseapp

import com.google.gson.annotations.SerializedName

data class UserConfigRequest(
    @SerializedName("email_id") val userEmail: String)
