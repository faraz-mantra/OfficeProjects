package com.boost.cart.data.api_model.stateCode


import com.google.gson.annotations.SerializedName

data class Result(
  @SerializedName("data")
  var `data`: List<Data>? = null
)