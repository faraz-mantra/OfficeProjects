package com.appservice.viewmodel

import androidx.lifecycle.LiveData
import com.appservice.model.aptsetting.*
import com.appservice.model.businessmodel.BusinessProfileUpdateRequest
import com.appservice.model.generalApt.UpdateRequestGeneralApt
import com.appservice.rest.repository.BoostNowFloatsRepository
import com.appservice.rest.repository.NowfloatsApiRepository
import com.appservice.rest.repository.WithFloatTwoRepository
import com.appservice.model.serviceProduct.service.ServiceListingRequest
import com.appservice.rest.repository.StaffNowFloatsRepository
import com.framework.base.BaseResponse
import com.framework.models.BaseViewModel
import com.framework.models.toLiveData

class AppointmentSettingsViewModel : BaseViewModel() {

  fun getServiceListing(request: ServiceListingRequest): LiveData<BaseResponse> {
    return NowfloatsApiRepository.getServiceListing(request).toLiveData()
  }

  fun getFpDetails(fpId: String, map: Map<String, String>): LiveData<BaseResponse> {
    return WithFloatTwoRepository.getFpDetails(fpId, map).toLiveData()
  }

  fun updateCategoryInfo(request: UpdateInfoRequest): LiveData<BaseResponse> {
    return BoostNowFloatsRepository.updateInfo(request).toLiveData()
  }

  fun getDeliveryDetails(floatingPointId: String?, clientId: String?): LiveData<BaseResponse> {
    return WithFloatTwoRepository.getDeliveryDetails(floatingPointId, clientId).toLiveData()
  }

  fun addUpdatePaymentProfile(request: AddPaymentAcceptProfileRequest): LiveData<BaseResponse> {
    return WithFloatTwoRepository.addUpdatePaymentProfile(request).toLiveData()
  }

  fun invoiceSetup(request: InvoiceSetupRequest): LiveData<BaseResponse> {
    return WithFloatTwoRepository.invoiceSetup(request).toLiveData()
  }

  fun getPaymentProfileDetails(floatingPointId: String?, clientId: String?): LiveData<BaseResponse> {
    return WithFloatTwoRepository.getPaymentProfileDetails(floatingPointId, clientId).toLiveData()
  }

  fun setupDelivery(request: DeliverySetup): LiveData<BaseResponse> {
    return WithFloatTwoRepository.setupDelivery(request).toLiveData()
  }

  fun addMerchantUPI(request: UpdateUPIRequest): LiveData<BaseResponse> {
    return WithFloatTwoRepository.addMerchantUPI(request).toLiveData()
  }

  fun addBankAccount(floatingPointId: String?, clientId: String?, request: AddBankAccountRequest): LiveData<BaseResponse> {
    return WithFloatTwoRepository.addBankAccount(floatingPointId, clientId, request).toLiveData()
  }

  fun uploadSignature(request: UploadMerchantSignature): LiveData<BaseResponse> {
    return WithFloatTwoRepository.addMerchantSignature(request = request).toLiveData()
  }

  fun getWareHouseAddress(floatingPointId: String?, clientId: String?): LiveData<BaseResponse> {
    return WithFloatTwoRepository.getWareHouseAddress(floatingPointId, clientId).toLiveData()
  }

  fun addWareHouseAddress(request: RequestAddWareHouseAddress): LiveData<BaseResponse> {
    return WithFloatTwoRepository.addWareHouseAddress(request = request).toLiveData()
  }

  fun getAppointmentCatalogStatus(floatingPointId: String, clientId: String): LiveData<BaseResponse> {
    return WithFloatTwoRepository.getAppointmentCatalogStatus(floatingPointId, clientId).toLiveData()
  }

  fun updateGstSlab(request: GstSlabRequest): LiveData<BaseResponse> {
    return WithFloatTwoRepository.updateGstSlab(request).toLiveData()
  }

  fun updateProductCategoryVerb(request: ProductCategoryVerbRequest): LiveData<BaseResponse> {
    return WithFloatTwoRepository.updateProductCategoryVerb(request).toLiveData()
  }

  fun updateGeneralService(request: UpdateRequestGeneralApt?): LiveData<BaseResponse> {
    return StaffNowFloatsRepository.updateGeneralService(request).toLiveData()
  }

  fun getGeneralService(fpId: String?, fpTag: String?): LiveData<BaseResponse> {
    return StaffNowFloatsRepository.getGeneralService(fpId, fpTag).toLiveData()
  }

  fun updateBusinessDetails(businessProfileUpdateUrl:String, request: BusinessProfileUpdateRequest): LiveData<BaseResponse> {
    return WithFloatTwoRepository.updateBusinessProfile(businessProfileUpdateUrl,request).toLiveData()
  }
}