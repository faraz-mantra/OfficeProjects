package com.dashboard.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.appservice.model.SessionData
import com.appservice.model.StatusKyc
import com.appservice.model.domainBooking.DomainDetailsResponse
import com.appservice.ui.bankaccount.startFragmentAccountActivityNew
import com.appservice.ui.bgImage.BackgroundImageContainerActivity
import com.appservice.ui.bgImage.setFragmentTypeNew
import com.appservice.ui.catalog.CatalogServiceContainerActivity
import com.appservice.ui.catalog.setFragmentType
import com.appservice.ui.catalog.startFragmentActivity
import com.appservice.ui.domainbooking.startFragmentDomainBookingActivity
import com.appservice.ui.paymentgateway.startFragmentPaymentActivityNew
import com.appservice.ui.staffs.ui.startStaffFragmentActivity
import com.appservice.ui.testimonial.startTestimonialFragmentActivity
import com.appservice.ui.updatesBusiness.startUpdateFragmentActivity
import com.boost.dbcenterapi.utils.DataLoader
import com.dashboard.R
import com.dashboard.controller.getDomainName
import com.dashboard.controller.startFragmentDashboardActivity
import com.dashboard.controller.ui.ownerinfo.startOwnersInfoNewActivity
import com.festive.poster.ui.festivePoster.FestivePosterContainerActivity
import com.framework.analytics.SentryController
import com.framework.firebaseUtils.FirebaseRemoteConfigUtil.featureNewOnBoardingFlowEnable
import com.framework.firebaseUtils.FirebaseRemoteConfigUtil.featureUpdateStudioSelectedUsers
import com.framework.pref.*
import com.framework.utils.DateUtils
import com.framework.utils.application
import com.framework.utils.startPromotionUpdates
import com.framework.webengageconstant.*
import com.inventoryorder.constant.AppConstant
import com.inventoryorder.constant.IntentConstant
import com.inventoryorder.model.PreferenceData
import com.inventoryorder.model.floatMessage.MessageModel
import com.inventoryorder.ui.startFragmentOrderActivity
import com.inventoryorder.utils.getFileName
import com.inventoryorder.utils.getUrlExtension
import com.onboarding.nowfloats.constant.FragmentType
import com.onboarding.nowfloats.ui.updateChannel.startFragmentChannelActivity
import com.onboarding.nowfloats.ui.webview.WebViewActivity

const val VISITS_TYPE_STRING = "visits_type_string"

const val RIA_NODE_DATA = "riaNodeDatas"

fun AppCompatActivity.startDigitalChannel(session: UserSessionManager, channelType: String = "") {
  try {
    WebEngageController.trackEvent(DIGITAL_CHANNEL_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val bundle = Bundle()
    session.setHeader(WA_KEY)
    bundle.putString(UserSessionManager.KEY_FP_ID, session.fPID)
    bundle.putString(Key_Preferences.GET_FP_DETAILS_TAG, session.fpTag)
    bundle.putString(Key_Preferences.GET_FP_EXPERIENCE_CODE, session.fP_AppExperienceCode)
    bundle.putBoolean(Key_Preferences.IS_UPDATE, true)
    bundle.putString(
      Key_Preferences.BUSINESS_NAME, session.getFPDetails(Key_Preferences.GET_FP_DETAILS_BUSINESS_NAME)
    )
    bundle.putString(
      Key_Preferences.CONTACT_NAME, session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CONTACTNAME)
    )
    var imageUri = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_LogoUrl)
    if (imageUri.isNullOrEmpty().not() && imageUri!!.contains("http").not()) {
      imageUri = BASE_IMAGE_URL + imageUri
    }
    bundle.putString(Key_Preferences.BUSINESS_IMAGE, imageUri)
    bundle.putString(
      Key_Preferences.BUSINESS_TYPE, session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CATEGORY)
    )

    val city = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CITY)
    val country = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_COUNTRY)
    bundle.putString(
      Key_Preferences.LOCATION, if (city.isNullOrEmpty().not() && country.isNullOrEmpty().not()) "$city, $country" else "$city$country"
    )
    bundle.putString(Key_Preferences.WEBSITE_URL, session.getDomainName(false))
    bundle.putString(Key_Preferences.PRIMARY_NUMBER, session.userPrimaryMobile)
    bundle.putString(Key_Preferences.PRIMARY_EMAIL, session.fPEmail)
    bundle.putString(
      com.onboarding.nowfloats.constant.IntentConstant.CHANNEL_TYPE.name, channelType
    )
    startFragmentChannelActivity(FragmentType.MY_DIGITAL_CHANNEL, bundle)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startVmnCallCard(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(TRACK_CALL_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val i = Intent(this, Class.forName("com.appservice.ui.calltracking.VmnCallCardsActivityV2"))
    startActivity(i)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startBusinessEnquiry(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BUSINESS_ENQUIRY_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val queries = Intent(this, Class.forName("com.nowfloats.Business_Enquiries.BusinessEnquiryActivity"))
    startActivity(queries)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

@Deprecated("startSearchQuery")
fun AppCompatActivity.startSearchQuery(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(SEARCH_QUERIES_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val queries = Intent(this, Class.forName("com.nowfloats.Analytics_Screen.SearchQueriesActivity"))
    startActivity(queries)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}


fun AppCompatActivity.startRevenueSummary(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(REVENUE_SUMMARY_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val queries = Intent(this, Class.forName("com.nowfloats.Analytics_Screen.RevenueSummaryActivity"))
    startActivity(queries)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startAptOrderSummary(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(ORDER_SUMMARY_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val queries = Intent(this, Class.forName("com.nowfloats.Analytics_Screen.OrderSummaryActivity"))
    startActivity(queries)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun Fragment.startBackgroundActivity(session: UserSessionManager?, type: com.appservice.constant.FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean = false, isResult: Boolean = false) {
  val intent = Intent(activity, BackgroundImageContainerActivity::class.java)
  intent.putExtras(bundle)
  intent.setFragmentTypeNew(type)
  if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
  if (isResult.not()) startActivity(intent) else startActivityForResult(intent, 101)
}

fun AppCompatActivity.startBackgroundActivity(session: UserSessionManager?, type: com.appservice.constant.FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean = false, isResult: Boolean = false) {
  val intent = Intent(this, BackgroundImageContainerActivity::class.java)
  intent.putExtras(bundle)
  intent.setFragmentTypeNew(type)
  if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
  if (isResult.not()) startActivity(intent) else startActivityForResult(intent, 101)
}

fun AppCompatActivity.startBackgroundImageGallery(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BACKGROUND_IMAGE_GALLERY_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val queries = Intent(this, Class.forName("com.appservice.ui.background_image.BackgroundImageContainerActivity"))
    startActivity(queries)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
    SentryController.captureException(e)
  }
}

fun AppCompatActivity.startFeviconImage(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(FEVICON_IMAGE_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val queries = Intent(this, Class.forName("com.nowfloats.BusinessProfile.UI.UI.FaviconImageActivity"))
    startActivity(queries)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
    SentryController.captureException(e)
  }
}

fun AppCompatActivity.startDomainDetail(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(DOMAIN_EMAIL_PAGE_CLICK, CLICK, TO_BE_ADDED)
    if (DomainDetailsResponse.isDomainAvailable()) {
      startFragmentDomainBookingActivity(activity = this, type = com.appservice.constant.FragmentType.ACTIVE_NEW_DOMAIN_FRAGMENT, bundle = Bundle(), clearTop = false)
    } else {
      val queries = Intent(this, Class.forName("com.appservice.ui.domainbooking.DomainBookingActivity"))
      startActivity(queries)
      overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startSiteViewAnalytic(session: UserSessionManager?, type: String, eventName: String = WEBSITE_VISITS_CHART_DURATION_CHANGED) {
  try {
    WebEngageController.trackEvent(eventName, EVENT_LABEL_NULL, TO_BE_ADDED)
    val intent = Intent(this, Class.forName("com.nowfloats.Analytics_Screen.Graph.SiteViewsAnalytics"))
    intent.putExtra(VISITS_TYPE_STRING, type)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startSubscriber(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(SUBSCRIBERS_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val subscribers = Intent(this, Class.forName("com.nowfloats.Analytics_Screen.SubscribersActivity"))
    startActivity(subscribers)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}


fun AppCompatActivity.startAnalytics(session: UserSessionManager?, table_name: Int?) {
  try {
    WebEngageController.trackEvent(ANALYTICS_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val intent = Intent(this, Class.forName("com.nowfloats.Analytics_Screen.Graph.AnalyticsActivity"))
    if (table_name != null) intent.putExtra("table_name", table_name)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.initiateAddonMarketplace(session: UserSessionManager, isOpenCardFragment: Boolean, screenType: String, buyItemKey: String?, isLoadingShow: Boolean = true) {
  try {
    if (isLoadingShow) delayProgressShow()
    WebEngageController.trackEvent(ADDON_MARKETPLACE_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val intent = Intent(this, Class.forName("com.boost.marketplace.ui.home.MarketPlaceActivity"))
    intent.putExtra("expCode", session.fP_AppExperienceCode)
    intent.putExtra("fpName", session.fPName)
    intent.putExtra("fpid", session.fPID)
    intent.putExtra("fpTag", session.fpTag)
    intent.putExtra("isOpenCardFragment", isOpenCardFragment)
    intent.putExtra("screenType", screenType)
    intent.putExtra("accountType", session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CATEGORY))
    intent.putStringArrayListExtra(
      "userPurchsedWidgets", session.getStoreWidgets() as ArrayList<String>
    )
    if (session.userProfileEmail != null) {
      intent.putExtra("email", session.userProfileEmail)
    } else {
      intent.putExtra("email", getString(R.string.ria_customer_mail))
    }
    if (session.userPrimaryMobile != null) {
      intent.putExtra("mobileNo", session.userPrimaryMobile)
    } else {
      intent.putExtra("mobileNo", getString(R.string.ria_customer_number))
    }
    if (buyItemKey != null && buyItemKey.isNotEmpty()) intent.putExtra("buyItemKey", buyItemKey)
    intent.putExtra("profileUrl", session.fPLogo)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.initiateCart(
  session: UserSessionManager, isOpenCardFragment: Boolean, screenType: String, buyItemKey: String?, isLoadingShow: Boolean = true
) {
  try {
    //additem to cart
    if (buyItemKey.isNullOrEmpty().not()) DataLoader.addItemtoCart(application, buyItemKey!!)

    if (isLoadingShow) delayProgressShow()
    WebEngageController.trackEvent(ADDON_MARKETPLACE_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val intent = Intent(this, Class.forName("com.boost.cart.CartActivity"))
    intent.putExtra("expCode", session.fP_AppExperienceCode)
    intent.putExtra("isDeepLink", true)
    intent.putExtra("fpName", session.fPName)
    intent.putExtra("fpid", session.fPID)
    intent.putExtra("isOpenCardFragment", isOpenCardFragment)
    intent.putStringArrayListExtra(
      "userPurchsedWidgets", session.getStoreWidgets() as ArrayList<String>
    )
    if (session.userProfileEmail != null) {
      intent.putExtra("email", session.userProfileEmail)
    } else {
      intent.putExtra("email", getString(R.string.ria_customer_mail))
    }
    if (session.userPrimaryMobile != null) {
      intent.putExtra("mobileNo", session.userPrimaryMobile)
    } else {
      intent.putExtra("mobileNo", getString(R.string.ria_customer_number))
    }
    if (buyItemKey != null && buyItemKey.isNotEmpty()) intent.putExtra("buyItemKey", buyItemKey)
    intent.putExtra("profileUrl", session.fPLogo)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.delayProgressShow() {
  ProgressAsyncTask(this).execute()
}

fun AppCompatActivity.startSettingActivity(session: UserSessionManager) {
  WebEngageController.trackEvent(ACCOUNT_SETTING_PAGE_CLICK, CLICK, TO_BE_ADDED)
  startAppActivity(fragmentType = "ACCOUNT_SETTING")
}

fun AppCompatActivity.startKeyboardActivity(session: UserSessionManager) {
  WebEngageController.trackEvent(EVENT_NAME_BIZ_KEYBOARD_CLICK, EVENT_LABEL_BIZ_KEYBOARD, session.fpTag)
  startAppActivity(fragmentType = "ACCOUNT_KEYBOARD")
}

fun AppCompatActivity.startManageContentActivity(session: UserSessionManager) {
  WebEngageController.trackEvent(MANAGE_CONTENT_PAGE_CLICK, CLICK, TO_BE_ADDED)
  startAppActivity(fragmentType = "MANAGE_CONTENT")
}

fun AppCompatActivity.startManageInventoryActivity(session: UserSessionManager) {
  WebEngageController.trackEvent(MANAGE_INVENTORY_PAGE_CLICK, CLICK, TO_BE_ADDED)
  startAppActivity(fragmentType = "MANAGE_INVENTORY")
}

fun AppCompatActivity.startHelpAndSupportActivity(session: UserSessionManager) {
  WebEngageController.trackEvent(HELP_AND_SUPPORT_PAGE_CLICK, CLICK, TO_BE_ADDED)
  startAppActivity(fragmentType = "HELP_AND_SUPPORT")
}

fun AppCompatActivity.startAboutBoostActivity(session: UserSessionManager) {
  WebEngageController.trackEvent(ABOUT_BOOST_PAGE_CLICK, CLICK, TO_BE_ADDED)
  startAppActivity(fragmentType = "ABOUT_BOOST")
}

fun AppCompatActivity.startManageCustomer(session: UserSessionManager) {
  WebEngageController.trackEvent(MANAGE_CUSTOMER_PAGE_CLICK, CLICK, TO_BE_ADDED)
  startAppActivity(fragmentType = "MANAGE_CUSTOMER_VIEW")
}

fun AppCompatActivity.startNotification(session: UserSessionManager) {
  WebEngageController.trackEvent(NOTIFICATION_PAGE_CLICK, CLICK, TO_BE_ADDED)
  startAppActivity(fragmentType = "NOTIFICATION_VIEW")
}

fun AppCompatActivity.startUpdateLatestStory(session: UserSessionManager) {
  WebEngageController.trackEvent(UPDATE_LATEST_STORY_PAGE_CLICK, CLICK, TO_BE_ADDED)
  if (featureUpdateStudioSelectedUsers(session.fpTag)) {
    startUpdateFragmentActivity(com.appservice.constant.FragmentType.PAST_UPDATES)
  } else {
    startUpdateFragmentActivity(com.appservice.constant.FragmentType.UPDATE_BUSINESS_FRAGMENT)
  }
//  startAppActivity(fragmentType = "UPDATE_LATEST_STORY_VIEW")
}

fun AppCompatActivity.startOldSiteMeter(session: UserSessionManager) {
  startAppActivity(bundle = Bundle().apply {
    putInt("StorebizFloats", MessageModel().getStoreBizFloatSize())
  }, fragmentType = "SITE_METER_OLD_VIEW")
}

fun AppCompatActivity.startAppActivity(bundle: Bundle = Bundle(), fragmentType: String) {
  try {
    val intent = Intent(this, Class.forName("com.nowfloats.helper.AppFragmentContainerActivity"))
    intent.putExtras(bundle)
    intent.putExtra("FRAGMENT_TYPE", fragmentType)
    startActivity(intent)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.createAnUpdate() {
  try {
    startUpdateFragmentActivity(com.appservice.constant.FragmentType.ADD_UPDATE_BUSINESS_FRAGMENT_V2)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startPostUpdate(session: UserSessionManager?,isDashboard: Boolean = false) {
  try {
    val type = if (isDashboard) POST_UPDATE_MESSAGE_PAGE_CLICK else Post_Promotional_Update_Click
    if (featureUpdateStudioSelectedUsers(session?.fpTag)) {
      startPromotionUpdatesFromDashboard(type)
    } else {
      WebEngageController.trackEvent(type, CLICK, TO_BE_ADDED)
      startUpdateFragmentActivity(com.appservice.constant.FragmentType.ADD_UPDATE_BUSINESS_FRAGMENT)
    }
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startThirdPartyQueries(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(THIRD_PARTY_QUERIES_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.customerassistant.ThirdPartyQueriesActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startBoostExtension(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BOOST_360_EXTENSIONS_PAGE_CLICK, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.NavigationDrawer.Boost360ExtensionsActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startReferralView(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(REFER_A_FRIEND_CLICK, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.helper.ReferralTransActivity"))
    startActivity(webIntent)
    overridePendingTransition(0, 0)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startMobileSite(session: UserSessionManager?, website: String, eventname: String = MOBILE_SITE_PAGE) {
  try {
    WebEngageController.trackEvent(eventname, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.NavigationDrawer.Mobile_Site_Activity"))
    webIntent.putExtra("WEBSITE_NAME", website)
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startAddImageGallery(session: UserSessionManager?, isCreate: Boolean = true) {
  try {
    WebEngageController.trackEvent(IMAGE_GALLERY, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.Image_Gallery.ImageGalleryActivity"))
    webIntent.putStringArrayListExtra("userPurchsedWidgets", session?.getStoreWidgets() as ArrayList<String>)
    webIntent.putExtra("create_image", isCreate)
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
    SentryController.captureException(e)
  }
}

fun AppCompatActivity.startProductGallery(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(PRODUCT_GALLERY_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.ProductGallery.ProductGalleryActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
    SentryController.captureException(e)
  }
}

fun AppCompatActivity.startTestimonial(session: UserSessionManager?, isAdd: Boolean = false) {
  val text = if (isAdd) ADD_TESTIMONIAL_PAGE else TESTIMONIAL_PAGE
  WebEngageController.trackEvent(text, CLICK, TO_BE_ADDED)
//    val webIntent = Intent(this, Class.forName("com.nowfloats.AccrossVerticals.Testimonials.TestimonialsActivity"))
//    webIntent.putExtra("IS_ADD", isAdd)
//    startActivity(webIntent)
//    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  val type = if (isAdd) com.appservice.constant.FragmentType.TESTIMONIAL_ADD_EDIT_FRAGMENT else com.appservice.constant.FragmentType.TESTIMONIAL_LIST_FRAGMENT
  this.startTestimonialFragmentActivity(type)
}

fun AppCompatActivity.startCustomPage(session: UserSessionManager?, isAdd: Boolean = false) {
  try {
    val text = if (isAdd) ADD_CUSTOM_PAGE else CUSTOM_PAGE
    WebEngageController.trackEvent(text, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.CustomPage.CustomPageActivity"))
    webIntent.putExtra("IS_ADD", isAdd)
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListServiceProduct(session: UserSessionManager?) {
  try {
    val type = if (getProductType(session?.fP_AppExperienceCode) == "SERVICES") {
      WebEngageController.trackEvent(SERVICE_INVENTORY, CLICK, TO_BE_ADDED)
      com.appservice.constant.FragmentType.SERVICE_LISTING
    } else {
      WebEngageController.trackEvent(PRODUCT_INVENTORY, CLICK, TO_BE_ADDED)
      com.appservice.constant.FragmentType.FRAGMENT_PRODUCT_LISTING
    }
    startFragmentActivity(type, bundle = getBundleData(session))
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startEcommerceAppointmentSetting(session: UserSessionManager?) {
  try {
    val type = if (getProductType(session?.fP_AppExperienceCode) == "SERVICES") {
      com.appservice.constant.FragmentType.APPOINTMENT_SETTINGS
    } else {
      com.appservice.constant.FragmentType.ECOMMERCE_SETTINGS
    }
    startFragmentActivity(type)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startCatalogSetup(session: UserSessionManager?) {
  try {
    val type = if (getProductType(session?.fP_AppExperienceCode) == "SERVICES") {
      com.appservice.constant.FragmentType.APPOINTMENT_CATALOG_SETTINGS
    } else {
      com.appservice.constant.FragmentType.ECOMMERCE_CATALOG_SETTINGS
    }
    startFragmentActivity(type)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startPaymentCatalogSetup(session: UserSessionManager?) {
  try {
    val type = if (getProductType(session?.fP_AppExperienceCode) == "SERVICES") {
      com.appservice.constant.FragmentType.APPOINTMENT_PAYMENT_SETTINGS
    } else {
      com.appservice.constant.FragmentType.ECOMMERCE_PAYMENT_SETTINGS
    }
    startFragmentActivity(type)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startCatalogAppointment(session: UserSessionManager?) {
  Toast.makeText(this, "Coming soon.. ", Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.startBusinessVerification(session: UserSessionManager?) {
  try {
    val type = if (getProductType(session?.fP_AppExperienceCode) == "SERVICES") {
      com.appservice.constant.FragmentType.APPOINTMENT_FRAGMENT_CUSTOMER_INVOICE
    } else {
      com.appservice.constant.FragmentType.ECOMMERCE_FRAGMENT_CUSTOMER_INVOICE
    }
    startFragmentActivity(type)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListStaff(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(LIST_STAFF_DASHBOARD, CLICK, TO_BE_ADDED)
    startStaffFragmentActivity(com.appservice.constant.FragmentType.STAFF_PROFILE_LISTING_FRAGMENT, bundle = getBundleData(session))
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListDoctors(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(LIST_DOCTOR_PROFILE_DASHBOARD, CLICK, TO_BE_ADDED)
    startStaffFragmentActivity(com.appservice.constant.FragmentType.STAFF_PROFILE_LISTING_FRAGMENT, bundle = getBundleData(session))
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startAddStaff(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(ADD_STAFF_DASHBOARD, CLICK, TO_BE_ADDED)
    startStaffFragmentActivity(com.appservice.constant.FragmentType.STAFF_PROFILE_LISTING_FRAGMENT, bundle = getBundleData(session, true))
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun getBundleData(session: UserSessionManager?, isAddNew: Boolean = false): Bundle {
  val bundle = Bundle()
  bundle.putBoolean(com.appservice.constant.IntentConstant.NON_PHYSICAL_EXP_CODE.name, session?.isNonPhysicalProductExperienceCode ?: false)
  bundle.putString(com.appservice.constant.IntentConstant.CURRENCY_TYPE.name, "INR")
  bundle.putString(com.appservice.constant.IntentConstant.FP_ID.name, session?.fPID)
  bundle.putString(com.appservice.constant.IntentConstant.FP_TAG.name, session?.fpTag)
  bundle.putString(com.appservice.constant.IntentConstant.USER_PROFILE_ID.name, session?.userProfileId)
  bundle.putString(com.appservice.constant.IntentConstant.CLIENT_ID.name, clientId)
  bundle.putBoolean(com.appservice.constant.IntentConstant.IS_ADD_NEW.name, isAddNew)
  bundle.putString(com.appservice.constant.IntentConstant.EXTERNAL_SOURCE_ID.name, session?.getFPDetails(Key_Preferences.EXTERNAL_SOURCE_ID))
  bundle.putString(com.appservice.constant.IntentConstant.APPLICATION_ID.name, session?.getFPDetails(Key_Preferences.GET_FP_DETAILS_APPLICATION_ID))
  return bundle
}

fun Fragment.startFragmentActivity(type: com.appservice.constant.FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean = false, isResult: Boolean = false) {
  val intent = Intent(activity, CatalogServiceContainerActivity::class.java)
  intent.putExtras(bundle)
  intent.setFragmentType(type)
  if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
  if (isResult.not()) startActivity(intent) else startActivityForResult(intent, 101)
}

fun AppCompatActivity.startBookTable(session: UserSessionManager?, isAdd: Boolean = false) {
  try {
    WebEngageController.trackEvent(BOOK_TABLE_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.Restaurants.BookATable.BookATableActivity"))
    webIntent.putExtra("is_add", isAdd)
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startPreSignUp(session: UserSessionManager?, isClearTask: Boolean = false) {
  try {
    WebEngageController.trackEvent(PRE_SIGN_UP_PAGE, START_VIEW, TO_BE_ADDED)
    val webIntent: Intent = if (featureNewOnBoardingFlowEnable()) {
      val fragmentType = if (UserSessionManager(this).hasUserLoggedInOnce) "ENTER_PHONE_FRAGMENT" else "INTRO_SLIDE_SHOW_FRAGMENT"

      Intent(this, Class.forName("com.boost.presignin.ui.newOnboarding.NewOnBoardingContainerActivity")).putExtra("FRAGMENT_TYPE", fragmentType)
    } else {
      Intent(this, Class.forName("com.boost.presignin.ui.intro.IntroActivity"))
    }
    if (isClearTask) webIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startAddServiceProduct(session: UserSessionManager?) {
  try {
    val type = if (getProductType(session?.fP_AppExperienceCode) == "SERVICES") {
      WebEngageController.trackEvent(ADD_SERVICE_PAGE, CLICK, TO_BE_ADDED)
      com.appservice.constant.FragmentType.SERVICE_DETAIL_VIEW
    } else {
      WebEngageController.trackEvent(ADD_PRODUCT_PAGE, CLICK, TO_BE_ADDED)
      com.appservice.constant.FragmentType.PRODUCT_DETAIL_VIEW
    }
    session?.let { startFragmentActivity(type, bundle = getBundleData(it)) }
//    } else {
//      WebEngageController.trackEvent(ADD_PRODUCT_PAGE, CLICK, TO_BE_ADDED)
//      val webIntent = Intent(this, Class.forName("com.nowfloats.ProductGallery.ProductCatalogActivity"))
//      webIntent.putExtra("IS_ADD", true)
//      startActivity(webIntent)
//      overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//    }
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startOrderCreate(session: UserSessionManager?) {
  if (getProductType(session?.fP_AppExperienceCode) == "PRODUCTS") {
    val bundle = getSessionOrder(session)
    startFragmentOrderActivity(
      type = com.inventoryorder.constant.FragmentType.CREATE_NEW_ORDER, bundle = bundle, isResult = true
    )
  }
}

fun AppCompatActivity.startBookAppointmentConsult(session: UserSessionManager?, isConsult: Boolean = true) {
  try {
    val txt = if (isConsult) CONSULTATION_CREATE_PAGE else APPOINTMENT_CREATE_PAGE
    WebEngageController.trackEvent(txt, CLICK, TO_BE_ADDED)
    val bundle = getSessionOrder(session)
    val fragmentType = when {
      (getAptType(session?.fP_AppExperienceCode) == "SPA_SAL_SVC") -> com.inventoryorder.constant.FragmentType.CREATE_SPA_APPOINTMENT
      else -> {
        bundle.putBoolean(IntentConstant.IS_VIDEO.name, isConsult)
        com.inventoryorder.constant.FragmentType.CREATE_APPOINTMENT_VIEW
      }
    }
    this.startFragmentOrderActivity(type = fragmentType, bundle = bundle, isResult = true)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startOrderAptConsultList(session: UserSessionManager?, isOrder: Boolean = false, isConsult: Boolean = false) {
  try {
    val txt = if (isOrder) ORDER_PAGE_CLICK else if (isConsult) CONSULTATION_PAGE_CLICK else APPOINTMENT_PAGE_CLICK
    WebEngageController.trackEvent(txt, CLICK, TO_BE_ADDED)
    val bundle = getSessionOrder(session)
    val fragmentType = when {
      isOrder -> com.inventoryorder.constant.FragmentType.ALL_ORDER_VIEW
      (getAptType(session?.fP_AppExperienceCode) == "SPA_SAL_SVC") -> com.inventoryorder.constant.FragmentType.ALL_APPOINTMENT_SPA_VIEW
      isConsult -> com.inventoryorder.constant.FragmentType.ALL_VIDEO_CONSULT_VIEW
      else -> com.inventoryorder.constant.FragmentType.ALL_APPOINTMENT_VIEW
    }
    this.startFragmentOrderActivity(type = fragmentType, bundle = bundle, isResult = true)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun getSessionOrder(session: UserSessionManager?): Bundle {
  val data = PreferenceData(
    AppConstant.CLIENT_ID_ORDER, session?.userProfileId, WA_KEY, session?.fpTag, session?.userPrimaryMobile, session?.getDomainName(false), session?.fPEmail, session?.getFPDetails(Key_Preferences.LATITUDE), session?.getFPDetails(Key_Preferences.LONGITUDE), session?.fP_AppExperienceCode
  )
  val bundle = Bundle()
  bundle.putSerializable(IntentConstant.PREFERENCE_DATA.name, data)
  return bundle
}

fun AppCompatActivity.startBusinessLogo(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BUSINESS_LOGO_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.BusinessProfile.UI.UI.Business_Logo_Activity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startFeatureLogo(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(FEATURE_IMAGE_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.BusinessProfile.UI.UI.FeaturedImageActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
    SentryController.captureException(e)
  }
}

fun AppCompatActivity.startBusinessAddress(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BUSINESS_ADDRESS_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.BusinessProfile.UI.UI.Business_Address_Activity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startBusinessInfoEmail(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BUSINESS_INFO_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.BusinessProfile.UI.UI.ContactInformationActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startAllImage(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(IMAGE_MENU_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.Image_Gallery.ImageGalleryActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
    SentryController.captureException(e)
  }
}

fun AppCompatActivity.startBusinessProfileDetailEdit(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BUSINESS_PROFILE_PAGE, CLICK, TO_BE_ADDED)
    startFragmentDashboardActivity(com.dashboard.constant.FragmentType.FRAGMENT_BUSINESS_PROFILE)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startUserProfileDetail(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(USER_MERCHANT_PROFILE_PAGE, CLICK, TO_BE_ADDED)
    startFragmentDashboardActivity(com.dashboard.constant.FragmentType.FRAGMENT_USER_PROFILE)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startBusinessContactInfo(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(CONTACT_INFORMATION_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.BusinessProfile.UI.UI.ContactInformationActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startBusinessHours(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BUSINESS_HOURS_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.BusinessProfile.UI.UI.BusinessHoursActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startFragmentsFactory(session: UserSessionManager?, fragmentType: String) {
  try {
    WebEngageController.trackEvent("$fragmentType Page", CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.NavigationDrawer.businessApps.FragmentsFactoryActivity"))
    webIntent.putExtra("fragmentName", fragmentType)
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startPricingPlan(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(PRICING_PLAN_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.Store.NewPricingPlansActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startWebViewPageLoad(session: UserSessionManager?, url: String?) {
  try {
    WebEngageController.trackEvent(WEB_VIEW_PAGE, CLICK, url)
    val intent = Intent(this, WebViewActivity::class.java)
    intent.putExtra(com.onboarding.nowfloats.constant.IntentConstant.DOMAIN_URL.name, url)
    startActivity(intent)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}


fun AppCompatActivity.startReadinessScoreView(session: UserSessionManager?, position: Int = 0) {
  try {
    WebEngageController.trackEvent(DIGITAL_READINESS_SCORE_PAGE, CLICK, TO_BE_ADDED)
    startFragmentDashboardActivity(com.dashboard.constant.FragmentType.DIGITAL_READINESS_SCORE, bundle = Bundle().apply { putInt(com.dashboard.constant.IntentConstant.POSITION.name, position) })
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startSelfBrandedGateway(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(SELF_BRANDED_GATEWAY_PAGE, CLICK, TO_BE_ADDED)
    session?.getBundleDataKyc()?.let {
      startFragmentPaymentActivityNew(this, com.appservice.constant.FragmentType.PAYMENT_GATEWAY, it, false)
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startBusinessKycBoost(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BUSINESS_KYC_BOOST_PAGE, CLICK, TO_BE_ADDED)

    if (session?.isSelfBrandedKycAdd == true) {
      startFragmentActivity(com.appservice.constant.FragmentType.ECOMMERCE_SETTINGS)
    } else startFragmentActivity(com.appservice.constant.FragmentType.ECOMMERCE_BUSINESS_VERIFICATION)

  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startMyBankAccount(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(MY_BANK_ACCOUNT_PAGE, CLICK, TO_BE_ADDED)
    val bundle = Bundle()
    bundle.putString(com.appservice.constant.IntentConstant.CLIENT_ID.name, clientId)
    bundle.putString(com.appservice.constant.IntentConstant.USER_PROFILE_ID.name, session?.userProfileId)
    bundle.putString(com.appservice.constant.IntentConstant.FP_ID.name, session?.fPID)
    if (session?.isAccountSave() == true) {
      startFragmentAccountActivityNew(this, com.appservice.constant.FragmentType.BANK_ACCOUNT_DETAILS, bundle, false)
    } else {
      startFragmentAccountActivityNew(this, com.appservice.constant.FragmentType.ADD_BANK_ACCOUNT_START, bundle, false)
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun UserSessionManager.getBundleDataKyc(): Bundle {
  val session = SessionData()
  session.clientId = clientId
  session.userProfileId = userProfileId
  session.fpId = fPID
  session.fpTag = fpTag
  session.experienceCode = fP_AppExperienceCode
  session.fpLogo = fPLogo
  session.fpEmail = fPEmail
  session.fpNumber = fPPrimaryContactNumber
  session.isSelfBrandedAdd = isSelfBrandedKycAdd ?: false
  session.isPaymentGateway = getStoreWidgets()?.contains(StatusKyc.CUSTOM_PAYMENTGATEWAY.name) ?: false
  val bundle = Bundle()
  bundle.putSerializable(com.appservice.constant.IntentConstant.SESSION_DATA.name, session)
  return bundle
}

fun AppCompatActivity.startListDigitalBrochure(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(DIGITAL_BROCHURE_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.manufacturing.digitalbrochures.DigitalBrochuresActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

//check is premium
fun AppCompatActivity.startAddDigitalBrochure(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(ADD_DIGITAL_BROCHURE_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.manufacturing.digitalbrochures.DigitalBrochuresDetailsActivity"))
    webIntent.putExtra("ScreenState", "new")
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListProjectAndTeams(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(PROJECT_AND_TEAMS_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.manufacturing.projectandteams.ui.home.ProjectAndTermsActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListTripAdvisor(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(TRIP_ADVISOR_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.hotel.tripadvisor.TripAdvisorActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListProject(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(PROJECT_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.manufacturing.projectandteams.ui.project.ProjectActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListTeams(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(TEAMS_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.manufacturing.projectandteams.ui.teams.TeamsActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startWebsiteTheme(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(WEBSITE_STYLE, CLICK, TO_BE_ADDED)
    session?.getBundleDataKyc()?.let {
      startFragmentDashboardActivity(com.dashboard.constant.FragmentType.FRAGMENT_WEBSITE_THEME, it, false)
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startOwnersInfo(session: UserSessionManager?) {
  WebEngageController.trackEvent(OWNER_INFO_CLICK, CLICK, TO_BE_ADDED)
  startOwnersInfoNewActivity(com.dashboard.constant.FragmentType.OWNER_INFO)
}


fun AppCompatActivity.startWebsiteNav(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(WEBSITE_NAV, CLICK, TO_BE_ADDED)
    session?.getBundleDataKyc()?.let { startFragmentDashboardActivity(com.dashboard.constant.FragmentType.FRAGMENT_WEBSITE_NAV, it, false) }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListSeasonalOffer(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(SEASONAL_OFFER_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.hotel.seasonalOffers.SeasonalOffersActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startAddSeasonalOffer(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(ADD_SEASONAL_OFFER_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.hotel.seasonalOffers.SeasonalOffersDetailsActivity"))
    webIntent.putExtra("ScreenState", "new")
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListToppers(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(TOPPERS_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.education.toppers.ToppersActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startListBatches(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(BATCHES_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.education.batches.BatchesActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startNearByView(session: UserSessionManager?, isAdd: Boolean = false) {
  try {
    WebEngageController.trackEvent(NEAR_BY_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.hotel.placesnearby.PlacesNearByActivity"))
    webIntent.putExtra("is_add", isAdd)
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startFacultyMember(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(FACULTY_MEMBER_PAGE, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.nowfloats.education.faculty.FacultyActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startYouTube(session: UserSessionManager?, url: String) {
  try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.setPackage("com.google.android.youtube")
    startActivity(intent)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startDownloadUri(url: String, isToast: Boolean = false) {
  try {
    val downloader = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val uri = Uri.parse(url)
    val request = DownloadManager.Request(uri)
    request.setTitle(uri.path?.getFileName() ?: getString(R.string.boost_file))
    request.setDescription(getString(R.string.boost_360_file))
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${getString(R.string.boost_file)}_${DateUtils.getCurrentDate().time}.${url.getUrlExtension()}")
    downloader.enqueue(request)
    if (isToast) Toast.makeText(this, "File downloading.. ", Toast.LENGTH_SHORT).show()
  } catch (e: Exception) {
    e.printStackTrace()
  }
}


fun AppCompatActivity.startFestivePosterActivity(isBanner: Boolean = false) {
  try {
    WebEngageController.trackEvent(if (isBanner) SHARE_FESTIVE_POSTER_BANNER else SHARE_FESTIVE_POSTER_CLICK)
    val intent = Intent(this, FestivePosterContainerActivity::class.java)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startLogoutActivity(event: String = BOOST_LOGOUT_CLICK) {
  try {
    WebEngageController.trackEvent(event, CLICK, TO_BE_ADDED)
    val i = Intent(this, Class.forName("com.nowfloats.helper.LogoutActivity"))
    this.startActivity(i)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun Context.startHelpSupportVideoActivity(supportType: String) {
  try {
    val i = Intent(this, Class.forName("com.onboarding.nowfloats.ui.supportVideo.SupportVideoPlayerActivity"))
    i.putExtra(com.onboarding.nowfloats.constant.IntentConstant.SUPPORT_VIDEO_TYPE.name, supportType)
    this.startActivity(i)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startPromotionUpdatesFromDashboard(type: String) {
  try {
    WebEngageController.trackEvent(type, CLICK, TO_BE_ADDED)
    startPromotionUpdates()
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
  }
}

fun AppCompatActivity.startUpdateStudio(session: UserSessionManager?) {
  try {
    WebEngageController.trackEvent(UPDATE_STUDIO_CLICK, CLICK, TO_BE_ADDED)
    val webIntent = Intent(this, Class.forName("com.festive.poster.ui.festivePoster.FestivePosterContainerActivity"))
    startActivity(webIntent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
  } catch (e: ClassNotFoundException) {
    e.printStackTrace()
    SentryController.captureException(e)
  }
}
