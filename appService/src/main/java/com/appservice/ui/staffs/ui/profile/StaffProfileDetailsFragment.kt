package com.appservice.ui.staffs.ui.profile

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.FragmentType
import com.appservice.constant.IntentConstant
import com.appservice.databinding.FragmentStaffProfileBinding
import com.appservice.model.staffModel.*
import com.appservice.ui.staffs.ui.StaffFragmentContainerActivity
import com.appservice.ui.staffs.ui.startStaffFragmentActivity
import com.appservice.ui.catalog.common.AppointmentModel
import com.appservice.ui.staffs.ui.Constants
import com.appservice.ui.staffs.ui.bottomsheets.InActiveBottomSheet
import com.appservice.ui.staffs.ui.bottomsheets.InActiveStaffConfirmationBottomSheet
import com.appservice.ui.staffs.ui.bottomsheets.RemoveStaffConfirmationBottomSheet
import com.appservice.viewmodel.StaffViewModel
import com.appservice.utils.WebEngageController
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.glide.util.glideLoad
import com.framework.pref.UserSessionManager
import com.framework.views.customViews.CustomTextView
import com.framework.webengageconstant.*

class StaffProfileDetailsFragment : AppBaseFragment<FragmentStaffProfileBinding, StaffViewModel>() {
  private var popupWindow: PopupWindow? = null
  private var serviceIds: ArrayList<String>? = null
  private var staffDetails: StaffDetailsResult? = null
  private var isUpdate: Boolean = false


  override fun getLayout(): Int {
    return R.layout.fragment_staff_profile
  }

  override fun getViewModelClass(): Class<StaffViewModel> {
    return StaffViewModel::class.java
  }

  companion object {
    fun newInstance(): StaffProfileDetailsFragment {
      return StaffProfileDetailsFragment()
    }
  }

  override fun onCreateView() {
    super.onCreateView()
    sessionLocal = UserSessionManager(requireActivity())
    WebEngageController.trackEvent(if (isDoctorClinic) DOCTOR_PROFILE_DETAIL else STAFF_PROFILE_DETAIL, PAGE_VIEW, NO_EVENT_VALUE)
    setOnClickListener(binding?.civMenu, binding?.ctvEdit, binding?.ctvEditLeaves, binding?.ctvEditServices, binding?.ctvEditTiming)
    getStaffDetail()
  }

  private fun updateStaffProfile() {
    viewModel?.updateStaffProfile(requestUpdate())?.observeOnce(viewLifecycleOwner) {
      if (it.isSuccess()) {
        isUpdate = true
        getStaffDetail()
      } else showShortToast(it.errorMessage() ?: getString(R.string.something_went_wrong))
    }
  }

  private fun requestUpdate(): StaffProfileUpdateRequest {
    val staffProfile = StaffProfileUpdateRequest()
    staffProfile.description = staffDetails?.description
    staffProfile.floatingPointTag = sessionLocal.fpTag
    staffProfile.name = staffDetails?.name
    staffProfile.speciality = staffDetails?.speciality
    staffProfile.serviceIds = staffDetails?.serviceIds
    staffProfile.businessLicence = staffDetails?.businessLicence
    staffProfile.specialisations = staffDetails?.specialisations
    staffProfile.isAvailable = staffDetails?.isAvailable
    staffProfile.age = staffDetails?.age
    staffProfile.gender = staffDetails?.gender
    staffProfile.experience = staffDetails?.experience?.toInt()
    staffProfile.staffId = staffDetails?.id
    staffProfile.education = staffDetails?.education
    staffProfile.contactNumber = staffDetails?.contactNumber
    staffProfile.memberships = staffDetails?.memberships
    staffProfile.registration = staffDetails?.registration
    staffProfile.appointmentType = staffDetails?.appointmentType
    staffProfile.bookingWindow = staffDetails?.bookingWindow
    return staffProfile
  }

  private fun getStaffDetail() {
    showProgress()
    val get = arguments?.get(IntentConstant.STAFF_DATA.name) as? DataItem
    viewModel?.getStaffDetails(get?.id)?.observeOnce(viewLifecycleOwner) { res ->
      if (res.isSuccess()) {
        this.staffDetails = (res as StaffDetailsResponse).result
        binding?.ctvStaffName?.text = staffDetails?.name
        binding?.ctvExperience?.text = staffDetails?.getExperienceN()
        binding?.ctvStaffGenderAge?.text = staffDetails?.getGenderAndAge()
        binding?.ctvAboutHeading?.text = "About ${staffDetails?.name}"
        binding?.ctvAboutStaff?.text = staffDetails?.description
        binding?.civStaffProfileImg?.let { activity?.glideLoad(it, staffDetails?.getUrlImage(), R.drawable.placeholder_image_n) }
        binding?.ctvSpecialization?.text = staffDetails?.specialisations?.firstOrNull()?.value ?: ""
        if (staffDetails?.isAvailable == false) showInactiveProfile()
        fetchServices()
        setTimings()
        if (isDoctorClinic) setDoctorsData()
      } else showShortToast(res.errorMessage() ?: getString(R.string.something_went_wrong))
      hideProgress()
    }

  }

  private fun setDoctorsData() {
    binding?.ctvHeadingSpecialization?.gone()
    binding?.ctvSpecialization?.gone()
    binding?.llDoctorsAdditionalInfo?.visible()
    binding?.ctvBusinessLicense?.visible()
    binding?.ctvBusinessLicenseHeading?.visible()
    binding?.ctvBusinessAppointment?.visible()
    binding?.ctvBusinessAppointmentHeading?.visible()
    binding?.ctvBusinessLicense?.text = staffDetails?.businessLicence
    binding?.ctvBusinessAppointment?.text = staffDetails?.getBookingWindowN()
    binding?.ctvHeadingTiming?.text = getString(R.string.appointment_hours)
    binding?.ctvEducation?.text = staffDetails?.education
    binding?.ctvMobNo?.text = staffDetails?.contactNumber
    binding?.ctvMembership?.text = staffDetails?.memberships
    binding?.ctvRegistration?.text = staffDetails?.registration
    binding?.ctvAppointmentType?.text = AppointmentType.typeMap[staffDetails?.appointmentType]
    binding?.civDoctorsSignature?.let { activity?.glideLoad(it, staffDetails?.getUrlSignature(), R.drawable.placeholder_image_n) }

  }

  private fun setViewBackgrounds() {
    when (staffDetails?.isAvailable) {
      null, true -> {
        binding?.ctvEdit?.visibility = View.VISIBLE
        binding?.ctvEditServices?.visibility = View.VISIBLE
        binding?.civMenu?.visibility = View.VISIBLE
        binding?.civStaffProfileImg?.clearColorFilter()
        binding?.ctvEditTiming?.visibility = View.VISIBLE
        binding?.ctvBusinessAppointment?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_747474))
        binding?.ctvBusinessAppointmentHeading?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_4a4a4a))
        binding?.ctvBusinessLicenseHeading?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_4a4a4a))
        binding?.ctvBusinessLicense?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_747474))
        binding?.ctvHeadingExperience?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_4a4a4a))
        binding?.ctvExperience?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_747474))
        binding?.ctvAboutStaff?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_747474))
        binding?.ctvExperience?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_747474))
        binding?.ctvAboutHeading?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_4a4a4a))
        binding?.ctvHeadingServices?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_4a4a4a))
        binding?.ctvHeadingSpecialization?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_4a4a4a))
        binding?.ctvSpecialization?.setTextColor(ContextCompat.getColor(baseActivity, R.color.gray_4e4e4e))
        binding?.ctvHeadingTiming?.setTextColor(ContextCompat.getColor(baseActivity, R.color.black_4a4a4a))
        binding?.rlStaffContainer?.setBackgroundColor(ContextCompat.getColor(baseActivity, R.color.yellow_ffb900))
        (requireActivity() as StaffFragmentContainerActivity).getToolbar()?.setBackgroundColor(ContextCompat.getColor(baseActivity, R.color.yellow_ffb900))
        (requireActivity() as StaffFragmentContainerActivity).window.statusBarColor = ContextCompat.getColor(baseActivity, R.color.yellow_f5b200)

      }
      else -> {
        binding?.ctvEdit?.visibility = View.INVISIBLE
        binding?.ctvEditServices?.visibility = View.INVISIBLE
        binding?.civMenu?.visibility = View.INVISIBLE
        binding?.ctvEditTiming?.visibility = View.INVISIBLE
        binding?.ctvBusinessAppointment?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvBusinessAppointmentHeading?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvBusinessLicenseHeading?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvBusinessLicense?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvHeadingExperience?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvExperience?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvAboutStaff?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvExperience?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvAboutHeading?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvHeadingServices?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvHeadingSpecialization?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvSpecialization?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.ctvHeadingTiming?.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        binding?.civStaffProfileImg?.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        binding?.rlStaffContainer?.setBackgroundColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        (requireActivity() as StaffFragmentContainerActivity).getToolbar()?.setBackgroundColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
        (requireActivity() as StaffFragmentContainerActivity).window.statusBarColor = Color.parseColor("#ADADAD")
      }
    }
  }

  private fun setTimings() {
    binding?.llTimingContainer?.removeAllViews()
    staffDetails?.timings?.forEach {
      if (it.timeSlots.isNullOrEmpty().not()) binding?.llTimingContainer?.addView(getTimeView(it))
    }
  }

  private fun getTimeView(appointmentModel: AppointmentModel?): View {
    val itemView = LayoutInflater.from(binding?.llTimingContainer?.context).inflate(R.layout.recycler_item_service_timing, null, false);
    val timeTextView = itemView.findViewById(R.id.ctv_timing_services) as CustomTextView
    if (staffDetails?.isAvailable == true) timeTextView.setTextColor(ContextCompat.getColor(baseActivity, R.color.gray_4e4e4e)) else timeTextView.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))

    val str = StringBuilder()
    str.clear()
    when {
      appointmentModel?.day?.equals("monday", true) == true -> {
        str.append("Mon: ")
      }
      appointmentModel?.day?.equals("tuesday", true) == true -> {
        str.append("Tue: ")
      }
      appointmentModel?.day?.equals("wednesday", true) == true -> {
        str.append("Wed: ")
      }
      appointmentModel?.day?.equals("thursday", true) == true -> {
        str.append("Thu: ")
      }
      appointmentModel?.day?.equals("friday", true) == true -> {
        str.append("Fri: ")
      }
      appointmentModel?.day?.equals("saturday", true) == true -> {
        str.append("Sat: ")
      }
      appointmentModel?.day?.equals("sunday", true) == true -> {
        str.append("Sun: ")
      }
    }

    appointmentModel?.timeSlots?.forEachIndexed { index, item ->
      if (item.from?.isNotEmpty() == true) {
        str.append("${item.from} to ${item.to}")
        if (index < appointmentModel.timeSlots.size - 1) str.append(", ")
      }
    }
    timeTextView.text = str
    return itemView
  }

  private fun fetchServices() {
    var servicesProvided: ArrayList<DataItemService>? = null
    val request = ServiceListRequest(filterBy = FilterBy("ALL", 0, 0), category = "", floatingPointTag = sessionLocal.fpTag)
    viewModel?.getServiceListing(request)?.observeOnce(viewLifecycleOwner) { res ->
      if (res?.isSuccess() == true) {
        val data = (res as? ServiceListResponse)?.result?.data
        if (staffDetails?.serviceIds.isNullOrEmpty().not()) {
          servicesProvided = data?.filter { item -> staffDetails?.serviceIds?.contains(item?.id) == true } as? ArrayList<DataItemService>
          this.serviceIds = data?.filter { item -> staffDetails?.serviceIds?.contains(item?.id) == true } as? ArrayList<String>
        }
        setServices(servicesProvided?.map { it.name ?: "" })
      } else showShortToast(res?.errorMessage() ?: getString(R.string.something_went_wrong))
      setViewBackgrounds()
    }
  }

  private fun setServices(map: List<String>?) {
    binding?.llServices?.removeAllViews()
    val isEmpty = (map.isNullOrEmpty().not())
    binding?.llServices?.visibility = if (isEmpty) View.VISIBLE else View.GONE
    if (isEmpty) map?.forEach { binding?.llServices?.addView(getServiceView(it)) }
  }

  private fun getServiceView(services: String?): View {
    val itemView = LayoutInflater.from(binding?.llServices?.context).inflate(R.layout.recycler_item_service_timing, null, false);
    val serviceTextView = itemView.findViewById(R.id.ctv_timing_services) as CustomTextView
    if (staffDetails?.isAvailable == true) serviceTextView.setTextColor(ContextCompat.getColor(baseActivity, R.color.gray_4e4e4e)) else serviceTextView.setTextColor(ContextCompat.getColor(baseActivity, R.color.pinkish_grey))
    serviceTextView.text = services
    return itemView;
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.civMenu -> {
        if (this.popupWindow?.isShowing == true) this.popupWindow?.dismiss()
        else showPopupWindow(v)
      }
      binding?.ctvEdit -> {
        val bundle = Bundle()
        bundle.putSerializable(IntentConstant.STAFF_DATA.name, staffDetails)
        startStaffFragmentActivity(requireActivity(), if (isDoctorClinic) FragmentType.DOCTOR_ADD_EDIT_FRAGMENT else FragmentType.STAFF_DETAILS_FRAGMENT, bundle, false, isResult = true, Constants.STAFF_PROFILE_UPDATED_DATA)
      }
      binding?.ctvEditLeaves -> {

      }
      binding?.ctvEditServices -> {
        val bundle = Bundle()
        if (staffDetails?.serviceIds.isNullOrEmpty().not()) bundle.putStringArrayList(IntentConstant.STAFF_SERVICES.name, staffDetails?.serviceIds)
        startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_SELECT_SERVICES_FRAGMENT, bundle, clearTop = false, isResult = true, requestCode = Constants.REQUEST_CODE_SERVICES_PROVIDED)
      }
      binding?.ctvEditTiming -> {
        val bundle = Bundle()
        bundle.putSerializable(IntentConstant.STAFF_DATA.name, staffDetails)
        startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_TIMING_FRAGMENT, bundle, clearTop = false, isResult = true, requestCode = Constants.REQUEST_CODE_STAFF_TIMING)
      }
    }
  }

  private fun updateStaffTimings() {
    val request = StaffTimingAddUpdateRequest(staffId = staffDetails?.id, workTimings = staffDetails?.timings)
    viewModel?.updateStaffTiming(request)?.observeOnce(viewLifecycleOwner) {
      if (it.isSuccess()) {
        isUpdate = true
        showShortToast(getString(R.string.staff_timings_updated))
        getStaffDetail()
      } else showShortToast(getString(R.string.staff_timings_unable_to_update))
    }
  }

  private fun showPopupWindow(anchor: View) {
    val view = LayoutInflater.from(baseActivity).inflate(R.layout.popup_window_staff_menu, null)
    this.popupWindow = PopupWindow(view, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true)
    val markAsActive = this.popupWindow?.contentView?.findViewById<CustomTextView>(R.id.mark_as_active)
    val removeStaff = this.popupWindow?.contentView?.findViewById<CustomTextView>(R.id.remove_staff_profile)
    if (isDoctorClinic) removeStaff?.text = getString(R.string.remove_doctor)
    markAsActive?.setOnClickListener {
      staffDetails?.isAvailable = true
      showInactiveConfirmation()
      this.popupWindow?.dismiss()
    }
    removeStaff?.setOnClickListener {
      showRemoveStaffConfirmation()
      this.popupWindow?.dismiss()
    }
    this.popupWindow?.elevation = 5.0F
    this.popupWindow?.showAsDropDown(anchor, 0, 0)
  }

  private fun showInactiveProfile() {
    val inActiveBottomSheet = InActiveBottomSheet()
    inActiveBottomSheet.onClicked = {
      staffDetails?.isAvailable = true
      updateStaffProfile()
    }
    inActiveBottomSheet.onBackPres = { onBackPresDetail() }
    inActiveBottomSheet.isCancelable = false
    inActiveBottomSheet.show(this@StaffProfileDetailsFragment.parentFragmentManager, InActiveBottomSheet::class.java.name)
  }

  private fun showInactiveConfirmation() {
    val inActiveStaffConfirmationBottomSheet = InActiveStaffConfirmationBottomSheet()
    inActiveStaffConfirmationBottomSheet.onClicked = {
      staffDetails?.isAvailable = false
      updateStaffProfile()
    }
    inActiveStaffConfirmationBottomSheet.show(this@StaffProfileDetailsFragment.parentFragmentManager, InActiveStaffConfirmationBottomSheet::class.java.name)
  }

  private fun showRemoveStaffConfirmation() {
    val removeStaffConfirmationBottomSheet = RemoveStaffConfirmationBottomSheet()
    removeStaffConfirmationBottomSheet.onClicked = { removeStaffProfile() }
    val bundle = Bundle()
    bundle.putBoolean(IntentConstant.STAFF_DATA.name, isDoctorClinic)
    removeStaffConfirmationBottomSheet.arguments = bundle
    removeStaffConfirmationBottomSheet.show(this@StaffProfileDetailsFragment.parentFragmentManager, RemoveStaffConfirmationBottomSheet::class.java.name)
  }


  private fun removeStaffProfile() {
    showProgress()
    viewModel?.deleteStaffProfile(StaffDeleteImageProfileRequest(staffDetails?.id, sessionLocal.fpTag))?.observe(viewLifecycleOwner) { res ->
      if (res.isSuccess()) {
        isUpdate = true
        onBackPresDetail()
      } else showShortToast(res?.errorMessage() ?: getString(R.string.unable_to_delete))
      hideProgress()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when {
      requestCode == Constants.REQUEST_CODE_SERVICES_PROVIDED && resultCode == AppCompatActivity.RESULT_OK -> {
        val resultServices = data?.extras?.get(IntentConstant.STAFF_SERVICES.name) as? ArrayList<DataItemService>
        staffDetails?.serviceIds = ArrayList()
        resultServices?.forEach { if (it.id.isNullOrEmpty().not()) staffDetails?.serviceIds?.add(it.id!!) }
        updateStaffProfile()
      }
      requestCode == Constants.REQUEST_CODE_STAFF_TIMING && resultCode == AppCompatActivity.RESULT_OK -> {
        val result = data?.extras?.get(IntentConstant.STAFF_TIMINGS.name) as? StaffDetailsResult
        staffDetails?.timings = result?.timings
        updateStaffTimings()
      }
      requestCode == Constants.STAFF_PROFILE_UPDATED_DATA && resultCode == AppCompatActivity.RESULT_OK -> {
        isUpdate = (data?.extras?.get(IntentConstant.IS_UPDATED.name) as? Boolean) ?: false
        if (isUpdate) getStaffDetail()
      }
    }
  }

  fun onBackPresDetail() {
    val intent = Intent()
    intent.putExtra(IntentConstant.IS_UPDATED.name, isUpdate)
    baseActivity.setResult(AppCompatActivity.RESULT_OK, intent)
    baseActivity.finish()
  }
}