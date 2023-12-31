package com.boost.dbcenterapi.data.api_model.gst


import com.google.gson.annotations.SerializedName

data class GSTApiResponse(
  @SerializedName("Error")
  var error: Error? = null,
  @SerializedName("Result")
  var result: Result? = null,
  @SerializedName("StatusCode")
  var statusCode: Int? = null
)