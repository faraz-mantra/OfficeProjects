package com.boost.cart.data.api_model.customerId.get

data class TaxDetails(
  val GSTIN: String? = null,
  val TDS: Int,
  val TanNumber: Any,
  val Tax: Int
)