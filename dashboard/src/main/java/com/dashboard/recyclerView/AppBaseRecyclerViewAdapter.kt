package com.dashboard.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.dashboard.R
import com.dashboard.constant.RecyclerViewItemType.*
import com.dashboard.databinding.*
import com.dashboard.holder.*
import com.framework.base.BaseActivity

open class AppBaseRecyclerViewAdapter<T : AppBaseRecyclerViewItem>(activity: BaseActivity<*, *>, list: ArrayList<T>, itemClickListener: RecyclerItemClickListener? = null) : BaseRecyclerViewAdapter<T>(activity, list, itemClickListener) {

  override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerViewHolder<*> {
    val inflater = LayoutInflater.from(parent.context)
    val recyclerViewItemType = values().first { it.getLayout() == viewType }
    val binding = getViewDataBinding(inflater, recyclerViewItemType, parent)
    return when (recyclerViewItemType) {
      PAGINATION_LOADER -> PagingViewHolder(binding as PaginationLoaderBinding)
      CHANNEL_ITEM_VIEW -> ChannelViewHolder(binding as ItemChannelDBinding)
      BUSINESS_SETUP_ITEM_VIEW -> BusinessSetupViewHolder(binding as ItemBusinessManagementBinding)
      QUICK_ACTION_ITEM_VIEW -> QuickActionViewHolder(binding as ItemQuickActionBinding)
      RIA_ACADEMY_ITEM_VIEW -> RiaAcademyViewHolder(binding as ItemLearnDigitalJourneyBinding)
      BOOST_PREMIUM_ITEM_VIEW -> BoostPremiumViewHolder(binding as ItemBoostPremiumBinding)
      ROI_SUMMARY_ITEM_VIEW -> RoiSummaryViewHolder(binding as ItemRoiSummaryBinding)
      MANAGE_BUSINESS_ITEM_VIEW -> ManageBusinessViewHolder(binding as ItemManageBusinessDBinding)
      GROWTH_STATE_ITEM_VIEW -> GrowthStateViewHolder(binding as ItemGrowthStateBinding)
    }
  }

  fun runLayoutAnimation(recyclerView: RecyclerView?, anim: Int = R.anim.layout_animation_fall_down) = recyclerView?.apply {
    layoutAnimation = AnimationUtils.loadLayoutAnimation(context, anim)
    notifyDataSetChanged()
    scheduleLayoutAnimation()
  }

  override fun getItemViewType(position: Int): Int {
    return if (isLoaderVisible) {
      return if (position == list.size - 1) PAGINATION_LOADER.getLayout() else super.getItemViewType(position)
    } else super.getItemViewType(position)
  }

  fun notify(list: ArrayList<T>?) {
    list?.let { updateList(it) }
  }

  open fun addItems(addList: ArrayList<T>?) {
    addList?.let { list.addAll(it) }
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int {
    return if (list.isNotEmpty()) list.size else 0
  }


  open fun remove(item: T) {
    val position = list.indexOf(item)
    if (position > -1) {
      list.removeAt(position)
      notifyItemRemoved(position)
    }
  }

  open fun clear() {
    isLoaderVisible = false
    while (itemCount > 0) {
      getItem(0)?.let { remove(it) }
    }
  }

  open fun isEmpty(): Boolean {
    return itemCount == 0
  }

  open fun addLoadingFooter(t: T) {
    isLoaderVisible = true
    list.add(t)
    notifyItemInserted(list.size - 1)
  }

  open fun removeLoadingFooter() {
    isLoaderVisible = false
    val position = list.size - 1
    if (position > -1) {
      val item: T? = getItem(position)
      if (item != null) {
        list.removeAt(position)
        notifyItemRemoved(position)
      }
    }
  }

  open fun getItem(position: Int): T? {
    return list[position]
  }

  open fun list(): ArrayList<T> {
    return list
  }
}