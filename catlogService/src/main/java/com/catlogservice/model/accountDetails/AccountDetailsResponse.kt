package com.catlogservice.model.accountDetails


import com.framework.base.BaseResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountDetailsResponse(
    @SerializedName("Error")
    var errorN: Error? = null,
    @SerializedName("Result")
    var result: Result? = null,
    @SerializedName("StatusCode")
    var statusCode: Int? = null
) : BaseResponse(), Serializable