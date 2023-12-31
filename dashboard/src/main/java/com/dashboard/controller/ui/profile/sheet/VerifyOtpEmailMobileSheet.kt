package com.dashboard.controller.ui.profile.sheet

import android.view.View
import com.boost.presignin.views.otptextview.OTPListener
import com.dashboard.R
import com.dashboard.controller.DashboardFragmentContainerActivity
import com.dashboard.databinding.SheetVerifyOtpEmailNumberBinding
import com.dashboard.utils.WebEngageController
import com.dashboard.utils.capitalizeUtil
import com.dashboard.viewmodel.UserProfileViewModel
import com.framework.base.BaseBottomSheetDialog
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.pref.UserSessionManager
import com.framework.webengageconstant.*
import com.framework.webengageconstant.USER_MERCHANT_PROFILE_EMAIL_PAGE

class VerifyOtpEmailMobileSheet : BaseBottomSheetDialog<SheetVerifyOtpEmailNumberBinding, UserProfileViewModel>() {

  private var sheetType: String? = null
  private var emailOrMob: String? = null

  companion object {
    val IK_TYPE = "IK_TYPE"
    val IK_EMAIL_OR_MOB = "IK_EMAIL_OR_MOB"
  }

  enum class SheetType {
    EMAIL,
    MOBILE
  }

  override fun getLayout(): Int {
    return R.layout.sheet_verify_otp_email_number
  }

  override fun getViewModelClass(): Class<UserProfileViewModel> {
    return UserProfileViewModel::class.java
  }

  override fun onCreateView() {
    sheetType = arguments?.getString(IK_TYPE)
    emailOrMob = arguments?.getString(IK_EMAIL_OR_MOB)
    if (sheetType == SheetType.EMAIL.name) {
      WebEngageController.trackEvent(USER_MERCHANT_PROFILE_EMAIL_OTP, PAGE_VIEW, NO_EVENT_VALUE)
      binding?.tvMobOrEmail?.text = emailOrMob
      binding?.tvHeading?.text = getString(R.string.verify_email)
    } else {
      WebEngageController.trackEvent(USER_MERCHANT_PROFILE_NUMBER_OTP, PAGE_VIEW, NO_EVENT_VALUE)
      binding?.tvHeading?.text = getString(R.string.verify_mobile_number)
      binding?.tvMobOrEmail?.text = "+91 $emailOrMob"

    }

    viewListeners()
    setOnClickListener(binding?.btnPublish, binding?.btnResend, binding?.rivCloseBottomSheet)
  }

  private fun viewListeners() {
    binding?.pinTv?.otpListener = object : OTPListener {
      override fun onInteractionListener() {
        binding?.btnPublish?.isEnabled = binding?.pinTv?.otp?.length ?: 0 == 4

      }

      override fun onOTPComplete(otp: String) {
        binding?.btnPublish?.isEnabled = true

      }

    }
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.btnPublish -> {
        updateEmailMobApi()
      }
      binding?.btnResend -> {
        sendOtp()
      }
      binding?.rivCloseBottomSheet -> dismiss()
    }
  }

  private fun updateEmailMobApi() {
    binding?.progressBar?.visible()
    if (sheetType == SheetType.EMAIL.name) {
      WebEngageController.trackEvent(USER_MERCHANT_PROFILE_EMAIL_OTP_VERIFY, CLICK, NO_EVENT_VALUE)
      viewModel?.updateEmail(emailOrMob, binding?.pinTv?.otp, UserSessionManager(baseActivity).userProfileId)?.observeOnce(viewLifecycleOwner, {
        if (it.isSuccess()) {
          (baseActivity as? DashboardFragmentContainerActivity)?.onRefresh()
          dismiss()
        } else {
          val errorMessage = it.errorIPMessage()
          if (errorMessage.isNullOrEmpty().not()) binding?.tvInvalidOtp?.text = errorMessage?.capitalizeUtil()
          binding?.tvInvalidOtp?.visible()
        }
        binding?.progressBar?.gone()

      })

    } else {
      WebEngageController.trackEvent(USER_MERCHANT_PROFILE_NUMBER_OTP_VERIFY, CLICK, NO_EVENT_VALUE)
      viewModel?.updateMobile(emailOrMob, binding?.pinTv?.otp, UserSessionManager(baseActivity).userProfileId)?.observeOnce(viewLifecycleOwner) {
        if (it.isSuccess()) {
          (baseActivity as? DashboardFragmentContainerActivity)?.onRefresh()
          dismiss()
        } else {
          val errorMessage = if (it.errorIPMessage()!!.toLowerCase().contains("invalid otp")) {
            "Invalid code entered.Please re-enter again."
          } else {
            it.errorIPMessage()
          }
          if (errorMessage.isNullOrEmpty().not()) binding?.tvInvalidOtp?.text =
            errorMessage?.capitalizeUtil()
          binding?.tvInvalidOtp?.visible()
        }
        binding?.progressBar?.gone()
      }
    }
  }

  private fun sendOtp() {
    binding?.progressBar?.visible()
    if (sheetType == SheetType.EMAIL.name) {
      viewModel?.sendEmailOTP(emailOrMob)?.observeOnce(viewLifecycleOwner, {
        if (it.isSuccess()) {
          showLongToast(getString(R.string.otp_resent))
        }
        binding?.progressBar?.gone()
      })
    } else {
      viewModel?.sendMobileOTP(emailOrMob)?.observeOnce(viewLifecycleOwner, {
        if (it.isSuccess()) {
          showLongToast(getString(R.string.otp_resent))
        }
        binding?.progressBar?.gone()
      })
    }

  }
}