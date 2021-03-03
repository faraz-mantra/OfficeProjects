package com.appservice.offers.selectservices

import android.view.View
import com.appservice.R
import com.appservice.constant.IntentConstant
import com.appservice.databinding.BottomSheetSelectServiceListingBinding
import com.appservice.offers.models.SelectServiceModel.DataItemOfferService
import com.appservice.offers.viewmodel.OfferViewModel
import com.appservice.recyclerView.AppBaseRecyclerViewAdapter
import com.appservice.recyclerView.BaseRecyclerViewItem
import com.appservice.recyclerView.RecyclerItemClickListener
import com.appservice.staffs.model.DataItemService
import com.appservice.staffs.model.FilterBy
import com.appservice.staffs.model.ServiceListRequest
import com.appservice.staffs.model.ServiceListResponse
import com.appservice.staffs.ui.UserSession
import com.framework.base.BaseBottomSheetDialog
import java.util.*
import kotlin.collections.ArrayList

class OfferSelectServiceBottomSheet : BaseBottomSheetDialog<BottomSheetSelectServiceListingBinding, OfferViewModel>(), RecyclerItemClickListener {
    private var dataOffer: DataItemOfferService? = null
    var onClicked: (dataOffer: DataItemOfferService?) -> Unit = {}
    private var isEdit: Boolean? = null
    lateinit var data: List<DataItemService?>
    var adapter: AppBaseRecyclerViewAdapter<DataItemOfferService>? = null
    private var listServices: ArrayList<DataItemOfferService>? = null
    private var serviceIds: ArrayList<String>? = null
    override fun onCreateView() {
        init()
    }

    private fun getBundleData() {
        if (listServices == null) listServices = arrayListOf()
        serviceIds = arguments?.getStringArrayList(IntentConstant.OFFER_SERVICES.name)
        isEdit = serviceIds.isNullOrEmpty().not()
    }


    private fun fetchServices() {
        viewModel!!.getServiceListing(ServiceListRequest(
                FilterBy("ALL", 0, 0), "", floatingPointTag = UserSession.fpTag)
        ).observe(viewLifecycleOwner, {
            listServices = ArrayList()
            data = (it as ServiceListResponse).result!!.data!!
            data.forEach { service -> listServices!!.add(DataItemOfferService(service?.isChecked, service?.type, service?.category, service?.secondaryTileImages, service?.price, service?.discountedPrice, service?.duration, service?.id, service?.image, service?.secondaryImages, service?.discountAmount, service?.name, service?.tileImage)) }
            this.adapter = AppBaseRecyclerViewAdapter(activity = baseActivity, list = listServices!!, itemClickListener = this@OfferSelectServiceBottomSheet)
            binding?.rvServices?.adapter = adapter
//            when {
//                isEdit!! -> {
//                    data.forEach { datum ->
//                        if (serviceIds?.contains(datum?.id) == true) {
//                            datum?.isChecked = true
//
//                            listServices?.add(datum!!)
//                        }
//                    }
//                }
//            }
//            adapter?.notifyDataSetChanged()

        })
    }

    private fun init() {
        getBundleData()
        fetchServices()
        setOnClickListener(binding?.btnApply, binding?.btnCancel)
    }


    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
        this.dataOffer = item as DataItemOfferService
        listServices?.forEach { if (it != item) it.isChecked = false }
        adapter?.notifyDataSetChanged()
    }

    override fun getLayout(): Int {
        return R.layout.bottom_sheet_select_service_listing
    }

    override fun getViewModelClass(): Class<OfferViewModel> {
        return OfferViewModel::class.java
    }

    fun setData(edit: Boolean) {

    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding?.btnApply -> {
                if (dataOffer != null)
                    onClicked(dataOffer)
                dismiss()
            }
            binding?.btnCancel -> {
                dismiss()
            }
        }
    }
}