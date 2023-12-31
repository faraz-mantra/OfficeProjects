package com.dashboard.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.appservice.rest.repository.AzureWebsiteNewRepository
import com.appservice.rest.repository.WithFloatTwoRepository
import com.boost.dbcenterapi.upgradeDB.local.AppDatabase
import com.dashboard.model.DisableBadgeNotificationRequest
import com.dashboard.rest.repository.*
import com.framework.base.BaseResponse
import com.framework.constants.PremiumCode
import com.framework.models.BaseViewModel
import com.framework.models.toLiveData
import com.framework.utils.PreferencesKey
import com.framework.utils.PreferencesUtils
import com.framework.utils.application
import com.framework.utils.saveData
import com.inventoryorder.model.summary.request.SellerSummaryRequest
import com.inventoryorder.rest.repositories.ApiTwoWithFloatRepository
import com.inventoryorder.rest.repositories.ApiWithFloatRepository
import com.inventoryorder.rest.repositories.InventoryOrderRepository
import com.onboarding.nowfloats.model.nfxProcess.NFXProcessRequest
import com.onboarding.nowfloats.model.uploadfile.UploadFileBusinessRequest
import com.onboarding.nowfloats.rest.repositories.CategoryRepository
import com.onboarding.nowfloats.rest.repositories.ChannelRepository
import com.onboarding.nowfloats.rest.repositories.UploadImageRepository
import com.onboarding.nowfloats.rest.repositories.WhatsAppRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import org.json.JSONObject

class DashboardViewModel : BaseViewModel() {

  private val TAG = "DashboardViewModel"
  init {
    checkUpdateStudioAvailalbilty()
  }
  fun getCategories(context: Context): LiveData<BaseResponse> {
    return CategoryRepository.getCategories(context).toLiveData()
  }

  fun getMoreSettings(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getMoreSettings(context).toLiveData()
  }

  fun getWebsiteNavData(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getWebsiteNavData(context).toLiveData()
  }

  fun getFirebaseToken(): LiveData<BaseResponse> {
    return WithFloatTwoRepositoryD.getFirebaseAuthToken().toLiveData()
  }

  @Deprecated("NFX token API")
  fun getChannelsAccessToken(nowfloatsId: String?): LiveData<BaseResponse> {
    return ChannelRepository.getChannelsAccessToken(nowfloatsId).toLiveData()
  }

  fun getChannelsAccessTokenStatus(nowfloatsId: String?): LiveData<BaseResponse> {
    return ChannelRepository.getChannelsStatus(nowfloatsId).toLiveData()
  }

  fun getCapLimitFeatureDetails(fpId: String?, clientId: String?): LiveData<BaseResponse> {
    return AzureWebsiteNewRepository.getCapLimitFeatureDetails(fpId, clientId).toLiveData()
  }

  fun getChannelsInsight(nowfloatsId: String?, identifier: String?): LiveData<BaseResponse> {
    return ChannelRepository.getChannelsInsights(nowfloatsId, identifier).toLiveData()
  }

  fun getWhatsappBusiness(request: String?, auth: String): LiveData<BaseResponse> {
    return WhatsAppRepository.getWhatsappBusiness(request = getJsonRequest(request), auth = auth).toLiveData()
  }

  fun nfxProcess(request: NFXProcessRequest?): LiveData<BaseResponse> {
    return ChannelRepository.nfxProcess(request).toLiveData()
  }

  fun getBizFloatMessage(request: Map<String, String>): LiveData<BaseResponse> {
    return ApiTwoWithFloatRepository.getBizFloatMessage(request).toLiveData()
  }

  fun getNotificationCount(clientId: String?, fpId: String?): LiveData<BaseResponse> {
    return WithFloatTwoRepository.getNotificationCount(clientId, fpId).toLiveData()
  }

  fun getSearchAnalytics(fpTag: String?, startDate: String?, endDate: String?): LiveData<BaseResponse> {
    return DevBoostKitRepository.getSearchAnalytics(fpTag, startDate, endDate).toLiveData()
  }

  fun getDrScoreUi(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getDrScoreUi(context).toLiveData()
  }

  fun getQuickActionData(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getQuickActionData(context).toLiveData()
  }

  fun getBoostAddOnsTop(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getBoostAddOnsTop(context).toLiveData()
  }

  fun getWelcomeDashboardData(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getWelcomeDashboardData(context).toLiveData()
  }

  fun getBoostVisitingMessage(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getBoostVisitingMessage(context).toLiveData()
  }

  fun getBoostCustomerItem(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getBoostCustomerItem(context).toLiveData()
  }

  fun getBoostWebsiteItem(context: Context): LiveData<BaseResponse> {
    return WithFloatRepository.getBoostWebsiteItem(context).toLiveData()
  }

//  fun getSellerSummary(clientId: String?, sellerId: String?): LiveData<BaseResponse> {
//    return InventoryOrderRepository.getSellerSummary(clientId, sellerId).toLiveData()
//  }

  fun getSellerSummaryV2_5(clientId: String?, sellerId: String?, request: SellerSummaryRequest?): LiveData<BaseResponse> {
    return InventoryOrderRepository.getSellerSummaryV2_5(clientId, sellerId, request).toLiveData()
  }

  fun getUserSummary(fpTag: String?, clientId: String?, startDate: String? = null, endDate: String? = null): LiveData<BaseResponse> {
    return ApiWithFloatRepository.getUserSummary(fpTag, clientId, startDate, endDate).toLiveData()
  }

  fun getSubscriberCount(fpTag: String?, clientId: String?, startDate: String?, endDate: String?): LiveData<BaseResponse> {
    return ApiWithFloatRepository.getSubscriberCount(fpTag, clientId, startDate, endDate).toLiveData()
  }

  fun getMapVisits(fpTag: String?, mapData: Map<String, String>?): LiveData<BaseResponse> {
    return ApiWithFloatRepository.getMapVisits(fpTag, mapData).toLiveData()
  }

  fun getUserCallSummary(
    clientId: String?, fpIdParent: String?, identifierType: String?, startDate: String? = null, endDate: String? = null
  ): LiveData<BaseResponse> {
    return ApiWithFloatRepository.getUserCallSummary(clientId, fpIdParent, identifierType, startDate, endDate).toLiveData()
  }

  fun putUploadSecondaryImage(request: UploadFileBusinessRequest): LiveData<BaseResponse> {
    return UploadImageRepository.putUploadSecondaryImage(request).toLiveData()
  }

  fun putUploadBusinessLogo(
    clientId: String?, fpId: String?, reqType: String?, reqId: String?, totalChunks: String?,
    currentChunkNumber: String?, file: RequestBody?
  ): LiveData<BaseResponse> {
    return WithFloatTwoRepositoryD.uploadBusinessLogo(clientId, fpId, reqType, reqId, totalChunks, currentChunkNumber, file).toLiveData()
  }

  fun getUpgradePremiumBanner(): LiveData<BaseResponse> {
    return DevBoostKitRepository.getUpgradePremiumBanner().toLiveData()
  }

  fun getUpgradeDashboardBanner(website_id: String? = "5fd88e1fb456eb000133ad31"): LiveData<BaseResponse> {
    return DevBoostKitRepository.getUpgradeDashboardBanner(website_id = website_id).toLiveData()
  }

  fun getDomainDetailsForFloatingPoint(fpTag: String?, map: Map<String, String>?): LiveData<BaseResponse> {
    return PluginFloatRepository.getDomainDetailsForFloatingPoint(fpTag, map).toLiveData()
  }

  fun getMerchantSummary(clientId: String?, fpTag: String?): LiveData<BaseResponse> {
    return WithFloatTwoRepository.getMerchantSummary(clientId, fpTag).toLiveData()
  }


  fun getUserProfileData(loginId: String?): LiveData<BaseResponse> {
    return WithFloatTwoRepositoryD.getUserProfileData(loginId).toLiveData()
  }

  fun disableBadgeNotification(request: DisableBadgeNotificationRequest): LiveData<BaseResponse> {
    return UsCentralNowFloatsCloudRepository.disableBadgeNotification(request).toLiveData()
  }

  fun checkUpdateStudioAvailalbilty(){

    AppDatabase.getInstance(application())
      ?.featuresDao()?.getFeaturesItemByFeatureCode(PremiumCode.CALLTRACKER.value)
      ?.subscribeOn(Schedulers.io())
      ?.observeOn(AndroidSchedulers.mainThread())
      ?.subscribe ({feature->
        if (feature!=null){
          PreferencesUtils.instance
            .saveData(PreferencesKey.IS_UPDATE_STUDIO_ENABLED.name,true)
        }


      },
        {t->
          Log.e(TAG, "checkUpdateStudioAvailalbilty: ", )
        }

      )

  }

}


fun getJsonRequest(fpTag: String?): String {
  val jsonObject = JSONObject()
  jsonObject.put("WebsiteId", fpTag)
  return jsonObject.toString()
}