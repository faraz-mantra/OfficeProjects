package com.boost.dbcenterapi.data.api_model.GetAllFeatures.response

data class DiscountCoupons(
    val _kid: String,
    val _parentClassId: String,
    val _parentClassName: String,
    val _propertyName: String,
    val code: String,
    val createdon: String,
    val description: String?,
    val discount_percent: Int,
    val isarchived: Boolean,
    val updatedon: String,
    val websiteid: String,
    val termsandconditions: String?,
    val title: String?
)