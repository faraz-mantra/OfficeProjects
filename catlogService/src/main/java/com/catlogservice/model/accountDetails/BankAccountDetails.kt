package com.catlogservice.model.accountDetails


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BankAccountDetails(
    @SerializedName("AccountAlias")
    var accountAlias: String? = null,
    @SerializedName("AccountName")
    var accountName: String? = null,
    @SerializedName("AccountNumber")
    var accountNumber: String? = null,
    @SerializedName("BankName")
    var bankName: String? = null,
    @SerializedName("IFSC")
    var iFSC: String? = null,
    @SerializedName("KYCDetails")
    var kYCDetails: KYCDetails? = null
) : Serializable {

  fun isValidAccount(): Boolean {
    return bankName.isNullOrEmpty().not() && iFSC.isNullOrEmpty().not() && accountNumber.isNullOrEmpty().not() && bankName.isNullOrEmpty().not()
  }
}