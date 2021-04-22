package com.boost.presignin.ui.registration

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.boost.presignin.R
import com.boost.presignin.constant.IntentConstant
import com.boost.presignin.constant.RecyclerViewActionType
import com.boost.presignin.constant.RecyclerViewItemType
import com.boost.presignin.databinding.FragmentCategoryBinding
import com.boost.presignin.model.BusinessInfoModel
import com.boost.presignin.model.RequestFloatsModel
import com.boost.presignin.model.category.CategoryDataModel
import com.boost.presignin.recyclerView.AppBaseRecyclerViewAdapter
import com.boost.presignin.recyclerView.BaseRecyclerViewItem
import com.boost.presignin.recyclerView.RecyclerItemClickListener
import com.boost.presignin.rest.response.ResponseDataCategory
import com.boost.presignin.ui.mobileVerification.MobileFragment
import com.boost.presignin.viewmodel.CategoryVideoModel
import com.framework.base.BaseFragment
import com.framework.extensions.observeOnce
import com.framework.extensions.visible


class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryVideoModel>() , RecyclerItemClickListener {

    private val TAG = "CategoryFragment"
    private lateinit var baseAdapter: AppBaseRecyclerViewAdapter<CategoryDataModel>
    private var category: CategoryDataModel? = null
    private var categoryList = ArrayList<CategoryDataModel>()


    companion object {
        private const val PHONE_NUMBER = "phone_number"
        @JvmStatic
        fun newInstance(phoneNumber: String?) =
                CategoryFragment().apply {
                    arguments = Bundle().apply {
                        putString(PHONE_NUMBER, phoneNumber)
                    }
                }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_category
    }

    override fun getViewModelClass(): Class<CategoryVideoModel> {
        return CategoryVideoModel::class.java
    }
    private val phoneNumber by lazy {
        arguments?.getString(IntentConstant.EXTRA_PHONE_NUMBER.name)
    }

    override fun onCreateView() {
        baseAdapter = AppBaseRecyclerViewAdapter(baseActivity, ArrayList(), this)
        val gridLayoutManager = GridLayoutManager(baseActivity, 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (baseAdapter.getItemViewType(position)) {
                    RecyclerViewItemType.SECTION_HEADER_ITEM.getLayout() -> 2
                    else -> 1
                }
            }
        }
        binding?.recyclerView?.layoutManager = gridLayoutManager
        binding?.recyclerView?.adapter = baseAdapter
        baseAdapter.runLayoutAnimation(binding?.recyclerView, R.anim.grid_layout_animation_scale)

        viewModel?.getCategories(requireContext())?.observeOnce(viewLifecycleOwner){
            if(it.error!=null){
                //TODO show error
                return@observeOnce
            }
            val categoryResponse = it as ResponseDataCategory
            baseAdapter.addItems(categoryList.apply {
                clear()
                addAll(categoryResponse.data!!)
            })
        }

        binding?.confirmButton?.setOnClickListener {
            addFragmentReplace(com.framework.R.id.container, BusinessDetailsFragment.newInstance(RequestFloatsModel(
                    categoryDataModel = category,ProfileProperties =  BusinessInfoModel(userMobile = phoneNumber)
            )),true);
         //   addFragmentReplace()
        }
    }

    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
        when (actionType) {
            RecyclerViewActionType.CATEGORY_ITEM_CLICKED.ordinal -> {
                category = item as? CategoryDataModel
                for (listItem in categoryList) {
                    (listItem as? CategoryDataModel)?.let {
                        it.isSelected = (it.category_key == (item as? CategoryDataModel)?.category_key)
                    }
                }
                baseAdapter.notifyDataSetChanged()

                binding?.confirmButton?.visible()
               // baseAdapter?.notifyDataSetChanged()
            }
        }
    }


}