package com.festive.poster.ui.promoUpdates

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.festive.poster.R
import com.festive.poster.base.AppBaseActivity
import com.festive.poster.base.AppBaseFragment
import com.festive.poster.constant.RecyclerViewActionType
import com.festive.poster.databinding.FragmentFavouriteListBinding
import com.festive.poster.models.*
import com.festive.poster.models.response.FavCategory
import com.festive.poster.recyclerView.AppBaseRecyclerViewAdapter
import com.festive.poster.recyclerView.BaseRecyclerViewItem
import com.festive.poster.recyclerView.RecyclerItemClickListener
import com.festive.poster.ui.TemplateDiffUtil
import com.festive.poster.utils.WebEngageController
import com.festive.poster.utils.posterPostClicked
import com.festive.poster.utils.posterWhatsappShareClicked
import com.festive.poster.viewmodels.PostUpdatesViewModel
import com.festive.poster.viewmodels.PromoUpdatesViewModel
import com.framework.base.BaseActivity
import com.framework.pref.UserSessionManager
import com.framework.rest.NetworkResult
import com.framework.utils.showToast
import com.framework.utils.toArrayList
import com.framework.webengageconstant.*


class FavouriteListFragment : AppBaseFragment<FragmentFavouriteListBinding, PostUpdatesViewModel>(), RecyclerItemClickListener {


  private val DEFAULT_SELECTED_POS = 0
  private var promoUpdatesViewModel: PromoUpdatesViewModel? = null
  private var session: UserSessionManager? = null
  private var selectedPos: Int = 0
  private var posterRvAdapter: AppBaseRecyclerViewAdapter<FavTemplate>? = null
  private var categoryAdapter: AppBaseRecyclerViewAdapter<FavCategory>? = null
  var categoryList = ArrayList<FavCategory>()


  override fun getLayout(): Int {
    return R.layout.fragment_favourite_list
  }

  override fun getViewModelClass(): Class<PostUpdatesViewModel> {
    return PostUpdatesViewModel::class.java
  }

  companion object {

    val BK_POSTER_PACK_LIST = "POSTER_PACK_LIST"
    val BK_SELECTED_POS = "BK_SELECTED_POS"

    fun newInstance(): FavouriteListFragment {
      val bundle = Bundle()
      val fragment = FavouriteListFragment()
      fragment.arguments = bundle
      return fragment
    }


  }


  override fun onCreateView() {
    super.onCreateView()
    WebEngageController.trackEvent(FAVOURITE_UPDATES_SCREEN_LOADED, PAGE_VIEW, NO_EVENT_VALUE)
    session = UserSessionManager(requireActivity())
    promoUpdatesViewModel = ViewModelProvider(requireActivity()).get(PromoUpdatesViewModel::class.java)
    initUi()

    getFavTemp()
  }

  private fun initUi() {
    binding.rvCat.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
    binding.rvPosters.layoutManager = LinearLayoutManager(requireActivity())
    categoryAdapter = AppBaseRecyclerViewAdapter(
      requireActivity() as BaseActivity<*, *>, categoryList, this
    )
    binding.rvCat.adapter = categoryAdapter

    posterRvAdapter = AppBaseRecyclerViewAdapter(
      requireActivity() as BaseActivity<*, *>, ArrayList(), this
    )
    binding.rvPosters.adapter = posterRvAdapter
    binding.rvPosters.layoutManager = LinearLayoutManager(requireActivity())
  }

  private fun getFavTemp() {

    promoUpdatesViewModel?.favData?.observe(viewLifecycleOwner) {


      when (it) {
        is NetworkResult.Loading -> {
          if (posterRvAdapter?.isEmpty() == true) showProgress()
        }
        is NetworkResult.Success -> {
          hideProgress()
          val data = it.data ?: return@observe
          if (data.isEmpty() || data.firstOrNull()?.getParentTemplates()?.isEmpty() == true) {
            zerothCase(true)
            return@observe
          } else {
            zerothCase(false)
          }

          if (categoryList.isEmpty()) {
            setCatgories(data)
            switchToSelectedItem(
              DEFAULT_SELECTED_POS, data[DEFAULT_SELECTED_POS].getParentTemplates()?.asFavModels()
            )
          } else {
            WebEngageController.trackEvent(PROMOTIONAL_UPDATE_CATEGORY_LOADED, NO_EVENT_LABLE, NO_EVENT_VALUE)
            setCatgories(data)
            switchToSelectedItem(selectedPos, data[selectedPos].getParentTemplates()?.asFavModels())

          }
        }
        is NetworkResult.Error -> {
          hideProgress()
          showToast(it.msg)
        }
      }
    }

  }

  private fun setCatgories(it: java.util.ArrayList<CategoryUi>) {
    categoryList.clear()
    categoryList.addAll(it.asFavModels().toArrayList())
    categoryAdapter?.notifyDataSetChanged()
  }


  private fun zerothCase(b: Boolean) {
    binding.dataLayout.isVisible = b.not()
    binding.layoutZeroth.isVisible = b
  }


  override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
    when (actionType) {
      RecyclerViewActionType.FAV_CAT_CLICKED.ordinal -> {
        WebEngageController.trackEvent(PROMOTIONAL_UPDATE_CATEGORY_CLICK, CLICK, CLICKED)

        switchToSelectedItem(position, categoryList[position].templates)
      }
      RecyclerViewActionType.WHATSAPP_SHARE_CLICKED.ordinal -> {
        posterWhatsappShareClicked(
          item as TemplateUi, requireActivity() as BaseActivity<*, *>
        )
      }
      RecyclerViewActionType.POST_CLICKED.ordinal -> {
        posterPostClicked(item as TemplateUi, requireActivity() as AppBaseActivity<*, *>)
      }

      RecyclerViewActionType.POSTER_LOVE_CLICKED.ordinal -> {
        val templateUi = item as TemplateUi
        WebEngageController.trackEvent(if (templateUi.isFavourite) UPDATE_STUDIO_UNMARK_FAVOURITE_CLICK else UPDATE_STUDIO_MARK_FAVOURITE_CLICK, CLICK, CLICKED)
        callFavApi(position, item as TemplateUi)
      }
    }

  }

  private fun callFavApi(position: Int, templateUi: TemplateUi) {
    promoUpdatesViewModel?.markAsFav(templateUi.isFavourite.not(), templateUi.id)
    promoUpdatesViewModel?.favStatus?.observe(viewLifecycleOwner) { it1 ->
      when (it1) {
        is NetworkResult.Loading -> showProgress()
        is NetworkResult.Success -> {
          posterRvAdapter?.list?.map { if (templateUi.id == it.id) it.isFavourite = it.isFavourite.not() }
          posterRvAdapter?.notifyItemChanged(position)
          hideProgress()
        }
        is NetworkResult.Error -> {
          showShortToast(it1.msg ?: getString(R.string.something_went_wrong))
          hideProgress()
        }
        else -> hideProgress()
      }
    }

  }

  private fun switchToSelectedItem(positon: Int, newList: List<FavTemplate>?) {
    if (newList == null) {
      return
    }
    selectedPos = positon
    selectCategory()
    posterRvAdapter?.setUpUsingDiffUtil(newList)
  }

  private fun selectCategory() {
    val selectedItem = categoryList[selectedPos]
    categoryList.forEach { it.isSelected = false }
    selectedItem.isSelected = true
    categoryAdapter?.notifyDataSetChanged()
    binding.tvCatTitle.text = selectedItem.name
    binding.tvCatSize.text = selectedItem.templates?.size.toString()
  }

  fun AppBaseRecyclerViewAdapter<FavTemplate>.setUpUsingDiffUtil(newList: List<FavTemplate>) {
    val templateDiffUtil = TemplateDiffUtil(
      this.list, newList
    )

    val diffResult = DiffUtil.calculateDiff(templateDiffUtil)

    this.list.clear()
    this.list.addAll(newList)
    diffResult.dispatchUpdatesTo(this)
  }
}

