package com.festive.poster.utils

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.framework.pref.Key_Preferences
import com.framework.pref.UserSessionManager
import com.framework.webengageconstant.ADDON_MARKETPLACE_PAGE_CLICK
import com.framework.webengageconstant.CLICK
import com.framework.webengageconstant.TO_BE_ADDED
import java.util.ArrayList

object MarketPlaceUtils {

    fun initiateAddonMarketplace(
        session: UserSessionManager,
        isOpenCardFragment: Boolean,
        screenType: String,
        buyItemKey: String?,
        isLoadingShow: Boolean = true,
        context: Context
    ) {
        try {
           // if (isLoadingShow) delayProgressShow()
            WebEngageController.trackEvent(ADDON_MARKETPLACE_PAGE_CLICK, CLICK, TO_BE_ADDED)
            val intent = Intent(context, Class.forName("com.boost.upgrades.UpgradeActivity"))
            intent.putExtra("expCode", session.fP_AppExperienceCode)
            intent.putExtra("fpName", session.fPName)
            intent.putExtra("fpid", session.fPID)
            intent.putExtra("fpTag", session.fpTag)
            intent.putExtra("isOpenCardFragment", isOpenCardFragment)
            intent.putExtra("screenType", screenType)
            intent.putExtra("accountType", session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CATEGORY))
            intent.putExtra("boost_widget_key","TESTIMONIALS")
            intent.putExtra("feature_code","TESTIMONIALS")
            intent.putExtra("isFestivePoster",true)

            intent.putStringArrayListExtra(
                "userPurchsedWidgets",
                session.getStoreWidgets() as ArrayList<String>
            )
            if (session.userProfileEmail != null) {
                intent.putExtra("email", session.userProfileEmail)
            } else {
                intent.putExtra("email", "ria@nowfloats.com")
            }
            if (session.userPrimaryMobile != null) {
                intent.putExtra("mobileNo", session.userPrimaryMobile)
            } else {
                intent.putExtra("mobileNo", "9160004303")
            }
            if (buyItemKey != null && buyItemKey.isNotEmpty()) intent.putExtra("buyItemKey", buyItemKey)
            intent.putExtra("profileUrl", session.fPLogo)
            context.startActivity(intent)
           // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}