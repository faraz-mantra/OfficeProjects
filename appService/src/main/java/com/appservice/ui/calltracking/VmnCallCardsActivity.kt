package com.appservice.ui.calltracking

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appservice.R
import com.appservice.base.AppBaseActivity
import com.appservice.databinding.ActivityVmnCallCardsBinding
import com.appservice.model.VmnCallModel
import com.appservice.utils.WebEngageController
import com.appservice.viewmodel.VmnCallsViewModel
import com.framework.models.BaseViewModel
import com.framework.pref.Key_Preferences
import com.framework.pref.Key_Preferences.GET_FP_DETAILS_CATEGORY
import com.framework.pref.UserSessionManager
import com.framework.pref.clientId
import com.framework.utils.InAppReviewUtils
import com.framework.utils.toArrayList
import com.framework.views.zero.old.AppFragmentZeroCase
import com.framework.views.zero.old.AppOnZeroCaseClicked
import com.framework.views.zero.old.AppRequestZeroCaseBuilder
import com.framework.views.zero.old.AppZeroCases
import com.framework.webengageconstant.BUSINESS_CALLS
import com.framework.webengageconstant.EVENT_LABEL_BUSINESS_CALLS
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.onboarding.nowfloats.constant.IntentConstant
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by Admin on 27-04-2017.
 */
class VmnCallCardsActivity : AppBaseActivity<ActivityVmnCallCardsBinding, VmnCallsViewModel>(),
    AppOnZeroCaseClicked {
    var sessionManager: UserSessionManager? = null
    var seeMoreLessStatus = false
    var totalCallCount = 0
    var totalPotentialCallCount = 0
    var stopApiCall = false
    var allowCallPlayFlag // This flag allows only one audio to play at a time. True means an audio can be played.
            = false
    var headerList: ArrayList<VmnCallModel> = ArrayList<VmnCallModel>()
    var vmnCallAdapter: VmnCall_Adapter? = null
    var mRecyclerView: RecyclerView? = null
    var selectedViewType = "ALL"
    private var offset = 0
    private var appFragmentZeroCase: AppFragmentZeroCase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = UserSessionManager(applicationContext)
        appFragmentZeroCase = AppRequestZeroCaseBuilder(
            AppZeroCases.BUSINESS_CALLS,
            this,
            this,
            isPremium
        ).getRequest().build()
        addFragmentReplace(binding?.childContainer?.getId(), appFragmentZeroCase)
       // MixPanelController.track(MixPanelController.VMN_CALL_TRACKER, null)
        setSupportActionBar(binding?.toolbar)
        if (supportActionBar != null) {
            title = "Business Calls"
            supportActionBar!!.setDisplayShowHomeEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        WebEngageController.trackEvent(BUSINESS_CALLS, EVENT_LABEL_BUSINESS_CALLS, null)

        binding?.tvTrackedCall?.setText(
            getString(R.string.tracked_calls) + " " + sessionManager?.getFPDetails(
                Key_Preferences.GET_FP_DETAILS_PRIMARY_NUMBER
            )
        )

        allowCallPlayFlag = true

        //tracking calls
        showTrackedCalls()
        mRecyclerView = findViewById<View>(R.id.call_recycler) as RecyclerView
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.layoutManager = linearLayoutManager
        mRecyclerView!!.setHasFixedSize(true)
        vmnCallAdapter = VmnCall_Adapter(this, headerList)
        mRecyclerView!!.adapter = vmnCallAdapter
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = linearLayoutManager.itemCount
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if (lastVisibleItem >= totalItemCount - 2 && !stopApiCall) {
                    calls()
                }
            }
        })
        setOnClickListener(binding?.seeMoreLess,binding?.websiteHelper,
        binding?.phoneHelper,binding?.parentLayout,binding?.cardViewViewCalllog)


        //show or hide if feature is available to user
       /* mainLayout = findViewById<View>(R.id.main_layout) as LinearLayout
        primaryLayout = findViewById<ConstraintLayout>(R.id.primary_layout)
        secondLayout = findViewById(R.id.second_layout)
        firstLayout = findViewById(R.id.first_layout)
        secondaryLayout = findViewById<View>(R.id.secondary_layout) as LinearLayout*/
        websiteCallCount()
        if (isPremium) {
            nonEmptyView()
            calls
        } else {
            emptyView()
        }
        vmnCallAdapter.setAllowAudioPlay(object : AllowAudioPlay() {
            fun allowAudioPlay(): Boolean {
                return allowCallPlayFlag
            }

            fun toggleAllowAudioPlayFlag(setValue: Boolean) {
                allowCallPlayFlag = setValue
            }
        })
    }

    private val isPremium: Boolean
        get() {
            val keys = session?.getStoreWidgets()
            return if (keys != null && keys.contains(PremiumCode.CALLTRACKER.getValue())) true else false
        }

    private fun showTrackedCalls() {
        binding.tableLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    if (selectedViewType != "ALL") {
                        selectedViewType = "ALL"
                        updateRecyclerData(null)
                    }
                } else if (tab.position == 1) {
                    if (selectedViewType != "MISSED") {
                        selectedViewType = "MISSED"
                        updateRecyclerData(null)
                    }
                } else if (tab.position == 2) {
                    if (selectedViewType != "CONNECTED") {
                        selectedViewType = "CONNECTED"
                        updateRecyclerData(null)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    fun calls() {
            Log.i(TAG, "getCalls: function called")
            stopApiCall = true
            showProgress()
            val startOffset = offset.toString()
            val hashMap: MutableMap<String, String?> = HashMap()
            hashMap["clientId"] = clientId
            hashMap["fpid"] = if (sessionManager?.iSEnterprise.equals("true")) sessionManager?.fPParentId else sessionManager?.fPID

            hashMap["offset"] = startOffset
            hashMap["limit"] = 100.toString()
            hashMap["identifierType"] = if (sessionManager?.iSEnterprise?.equals("true") == true) "MULTI" else "SINGLE"
            viewModel.trackerCalls(hashMap).observe(this) {
                    Log.i(TAG, "getCalls success: ")
                val vmnCallModels = (it.arrayResponse as? Array<VmnCallModel>)?.toList()?.toArrayList()
                    hideProgress()
                    if (vmnCallModels == null ||it.isSuccess().not()) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@observe
                    }else{
                        hideProgress()
                        stopApiCall = false
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val size = vmnCallModels.size
                    Log.v("getCalls", " $size")
                    stopApiCall = size < 100
                    updateRecyclerData(vmnCallModels)
                    if (size != 0) {
                        offset += 100
                    }
                    if (size < 1) {
                        emptyView()
                    } else {
                        nonEmptyView()
                    }
                }
        }

    private fun updateRecyclerData(newItems: ArrayList<VmnCallModel>?) {
        if (newItems != null) {
            val sizeOfList = headerList.size
            val listSize = newItems.size
            for (i in 0 until listSize) {
                val model: VmnCallModel = newItems[i]
                headerList.add(model)
                //                vmnCallAdapter.notifyItemInserted(sizeOfList + i);
            }
        }
        Log.i(TAG, "updateRecyclerData: header list size " + getSelectedTypeList(headerList).size)
        vmnCallAdapter.updateList(getSelectedTypeList(headerList))
    }

    private fun getSelectedTypeList(list: ArrayList<VmnCallModel>): ArrayList<VmnCallModel> {
        var selectedItems: ArrayList<VmnCallModel> = ArrayList<VmnCallModel>()
        when (selectedViewType) {
            "ALL" -> {
                selectedItems = list
            }
            "MISSED" -> {
                var i = 0
                while (i < list.size) {
                    if (list[i].callStatus.equals("MISSED")) {
                        selectedItems.add(list[i])
                    }
                    i++
                }
            }
            "CONNECTED" -> {
                var i = 0
                while (i < list.size) {
                    if (list[i].callStatus.equals("CONNECTED")) {
                        selectedItems.add(list[i])
                    }
                    i++
                }
            }
        }
        return selectedItems
    }

    private fun showEmptyScreen() {
        if (totalCallCount == 0) {
            binding?.emptyLayout?.visibility = View.VISIBLE
            val imageView = binding?.emptyLayout?.image_item
            val mMainText = binding?.emptyLayout?.main_text
            val mDescriptionText = binding?.emptyLayout?.description_text
            imageView.setImageResource(R.drawable.icon_no_calls)
            mMainText.text = "You have no call records yet."
            mDescriptionText.text =
                "Your customers haven't contacted\nyou on your call tracking number yet."
        } else {
            findViewById<View>(R.id.calls_details_layout).visibility = View.VISIBLE
            binding?.cardViewViewCalllog?.setVisibility(View.VISIBLE)
            binding?.cardViewViewCalllog?.setOnClickListener(this)
        }
    }

    //oldcode

    //        CallTrackerApis trackerApis = Constants.restAdapter.create(CallTrackerApis.class);
    //    private void setVmnTotalCallCount() {
    //        showProgress();
    //        CallTrackerApis trackerApis = Constants.restAdapter.create(CallTrackerApis.class);
    //        String type = sessionManager.getISEnterprise().equals("true") ? "MULTI" : "SINGLE";
    //
    //        trackerApis.getVmnSummary(Constants.clientId, sessionManager.getFPID(), type, new Callback<JsonObject>() {
    //            @Override
    //            public void success(JsonObject jsonObject, Response response) {
    //                hideProgress();
    //
    //                if (jsonObject == null || response.getStatus() != 200) {
    //                    Methods.showSnackBarNegative(VmnCallCardsActivity.this, getString(R.string.something_went_wrong));
    //
    //                } else {
    //                    if (jsonObject.has("TotalCalls")) {
    //                        String vmnTotalCalls = jsonObject.get("TotalCalls").getAsString();
    //                        // oldcode
    ////                        totalCount.setText(vmnTotalCalls != null && !"null".equalsIgnoreCase(vmnTotalCalls) ? vmnTotalCalls : "0");
    //                        if(vmnTotalCalls != null && !"null".equalsIgnoreCase(vmnTotalCalls)){
    //                            totalCallCount = Integer.parseInt(vmnTotalCalls);
    //                            allCountText.setText(vmnTotalCalls);
    //                            potentialCallsText.setText("View potential calls ("+totalCallCount+")");
    //                        }else{
    //                            allCountText.setText("0");
    //                        }
    //                    }
    //                    if (jsonObject.has("MissedCalls")) {
    //                        String vmnMissedCalls = jsonObject.get("MissedCalls").getAsString();
    //                        missedCountText.setText(vmnMissedCalls != null && !"null".equalsIgnoreCase(vmnMissedCalls) ? vmnMissedCalls : "0");
    //                    }
    //                    if (jsonObject.has("ReceivedCalls")) {
    //                        String vmnReceivedCalls = jsonObject.get("ReceivedCalls").getAsString();
    //                        receivedCountText.setText(vmnReceivedCalls != null && !"null".equalsIgnoreCase(vmnReceivedCalls) ? vmnReceivedCalls : "0");
    //                    }
    //                    getWebsiteCallCount();
//                }
//                showEmptyScreen();
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                hideProgress();
//                showEmptyScreen();
//                Methods.showSnackBarNegative(VmnCallCardsActivity.this, getString(R.string.something_went_wrong));
//            }
//        });
//    }
    fun websiteCallCount() {
            showProgress()

            //oldcode
//        CallTrackerApis trackerApis = Constants.restAdapter.create(CallTrackerApis.class);
            viewModel.getCallCountByType(
                session.fpTag,
                "POTENTIAL_CALLS",
                "WEB",).observe(this){

                hideProgress()
               /* val jsonObject = it as? JsonObject
                if (jsonObject == null || response.getStatus() !== 200) {
                    showEmptyScreen()
                    Methods.showSnackBarNegative(
                        this@VmnCallCardsActivity,
                        getString(R.string.something_went_wrong)
                    )
                } else {
                    val callCount: String = jsonObject.get("POTENTIAL_CALLS").getAsString()
                    binding?.webCallCount!!.text = callCount
                    totalPotentialCallCount += callCount.toInt()
                    binding?.totalNumberOfCalls.text =
                        "View potential calls ($totalPotentialCallCount)"
                    getPhoneCallCount()
                }*/
            }

        }

    private fun getPhoneCallCount() {
        showProgress()

        //old code
//        CallTrackerApis trackerApis = Constants.restAdapter.create(CallTrackerApis.class);
       viewModel.getCallCountByType(
            session.fpTag,
            "POTENTIAL_CALLS",
            "MOBILE").observe(this){
           /*hideProgress()
           if (jsonObject == null || it.isSuccess().not()) {
               showEmptyScreen()
               Methods.showSnackBarNegative(
                   this@VmnCallCardsActivity,
                   getString(R.string.something_went_wrong)
               )
           } else {
               val callCount: String = jsonObject.get("POTENTIAL_CALLS").getAsString()
               //                    webCallCount.setText(callCount);
               binding?.phoneCallCount?.text = callCount
               totalPotentialCallCount += callCount.toInt()
               binding?.totalNumberOfCalls!!.text =
                   "View potential calls ($totalPotentialCallCount)"
           }*/

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v) {
            binding?.cardViewViewCalllog -> {
                val i = Intent(this@VmnCallCardsActivity, ShowVmnCallActivity::class.java)

                if (totalCallCount == 0) {
                    Methods.showSnackBarNegative(
                        this@VmnCallCardsActivity,
                        "You do not have call logs."
                    )
                    return
                }
                startActivity(i)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            binding?.seeMoreLess->{
                if (!seeMoreLessStatus) {
                    seeMoreLessStatus = true
                    binding?.seeMoreLessImage?.setImageResource(R.drawable.up_arrow)
                    binding?.helpWebPhoneLayout?.setVisibility(View.VISIBLE)
                } else {
                    seeMoreLessStatus = false
                    binding?.seeMoreLessImage?.setImageResource(R.drawable.down_arrow)
                    binding?.helpWebPhoneLayout?.setVisibility(View.GONE)

                    //hide info
                    binding?.helpWebsiteInfo?.setVisibility(View.GONE)
                    binding?.helpPhoneInfo?.setVisibility(View.GONE)
                }
            }
            binding?.websiteHelper->{
                binding?.helpPhoneInfo?.setVisibility(View.GONE)
                if (binding?.helpWebsiteInfo?.getVisibility() == View.VISIBLE) {
                    binding?.helpWebsiteInfo?.setVisibility(View.GONE)
                } else {
                    binding?.helpWebsiteInfo?.setVisibility(View.VISIBLE)
                }
            }
            binding?.phoneHelper->{
                binding?.helpWebsiteInfo?.setVisibility(View.GONE)
                if (binding?.helpPhoneInfo?.getVisibility() == View.VISIBLE) {
                    binding?.helpPhoneInfo?.setVisibility(View.GONE)
                } else {
                    binding?.helpPhoneInfo?.setVisibility(View.VISIBLE)
                }
            }
            binding?.parentLayout->{
                if (seeMoreLessStatus) {
                    seeMoreLessStatus = false
                    binding?.seeMoreLessImage?.setImageResource(R.drawable.down_arrow)
                    binding?.helpWebPhoneLayout.setVisibility(View.GONE)

                    //hide info
                    binding?.helpWebsiteInfo?.setVisibility(View.GONE)
                    binding?.helpPhoneInfo?.setVisibility(View.GONE)
                }
            }
        }
    }


    private fun initiateBuyFromMarketplace() {
        val progressDialog = ProgressDialog(this)
        val status = "Loading. Please wait..."
        progressDialog.setMessage(status)
        progressDialog.setCancelable(false)
        progressDialog.show()
        val intent = Intent(this, UpgradeActivity::class.java)
        intent.putExtra("expCode", session?.fP_AppExperienceCode)
        intent.putExtra("fpName", session?.fPName)
        intent.putExtra("fpid", session?.fPID)
        intent.putExtra("fpTag", session?.fpTag)
        intent.putExtra("accountType", session?.getFPDetails(GET_FP_DETAILS_CATEGORY))
        intent.putStringArrayListExtra(
            "userPurchsedWidgets",
            ArrayList<String>(session?.getStoreWidgets())
        )
        if (session?.userPrimaryMobile != null) {
            intent.putExtra("email", session.userPrimaryMobile)
        } else {
            intent.putExtra("email", getString(R.string.ria_customer_mail))
        }
        if (session?.userPrimaryMobile != null) {
            intent.putExtra("mobileNo", session?.userPrimaryMobile)
        } else {
            intent.putExtra("mobileNo", getString(R.string.ria_customer_number))
        }
        intent.putExtra("profileUrl", session?.fPLogo)
        intent.putExtra("buyItemKey", "CALLTRACKER")
        startActivity(intent)
        Handler().postDelayed({ progressDialog.dismiss() }, 1000)
    }

    private fun nonEmptyView() {
        binding?.primaryLayout?.setVisibility(View.VISIBLE)
        binding?.childContainer?.setVisibility(View.GONE)
    }

    private fun emptyView() {
        binding?.primaryLayout?.setVisibility(View.GONE)
        binding?.childContainer?.setVisibility(View.VISIBLE)
    }

    override fun primaryButtonClicked() {
        initiateBuyFromMarketplace()
    }

    override fun secondaryButtonClicked() {
        try {
            startActivity(
                Intent(
                    this,
                    Class.forName("com.onboarding.nowfloats.ui.supportVideo.SupportVideoPlayerActivity")
                )
                    .putExtra(IntentConstant.SUPPORT_VIDEO_TYPE.name, SupportVideoType.TOB.value)
            )
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun ternaryButtonClicked() {}
    override fun appOnBackPressed() {}
    override fun onStop() {
        super.onStop()
        if (vmnCallAdapter.getItemCount() > 1) {
            InAppReviewUtils.showInAppReview(
                this,
                InAppReviewUtils.Events.in_app_review_out_of_customer_calls
            )
        }
    }

    companion object {
        private const val TAG = "VmnCallCardsActivity"
    }

    override fun getLayout(): Int {
        return R.layout.activity_vmn_call_cards
    }

    override fun getViewModelClass(): Class<VmnCallsViewModel> {
        return VmnCallsViewModel::class.java
    }
}