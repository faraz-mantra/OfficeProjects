package com.boost.dbcenterapi.upgradeDB.model.coupon


data class CouponServiceModel(
  val coupon_key: String? = null,
  val couponDiscountAmt: Double? = null,
  val success: Boolean? = null,
  val message: String? = null,

  )