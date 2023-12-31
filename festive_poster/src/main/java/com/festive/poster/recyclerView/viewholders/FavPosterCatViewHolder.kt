package com.festive.poster.recyclerView.viewholders

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.festive.poster.R
import com.festive.poster.constant.RecyclerViewActionType
import com.festive.poster.databinding.ListItemBrowseAllCatBinding
import com.festive.poster.databinding.ListItemBrowseTabTemplateCatBinding
import com.festive.poster.databinding.ListItemFavCatBinding
import com.festive.poster.databinding.ListItemPosterPackBinding
import com.festive.poster.models.FavouriteTemplatesDetail
import com.festive.poster.models.PosterPackModel
import com.festive.poster.models.response.FavCategory
import com.festive.poster.recyclerView.AppBaseRecyclerViewAdapter
import com.festive.poster.recyclerView.AppBaseRecyclerViewHolder
import com.festive.poster.recyclerView.BaseRecyclerViewItem
import com.festive.poster.recyclerView.RecyclerItemClickListener
import com.framework.BaseApplication
import com.framework.base.BaseActivity
import com.framework.utils.loadFromUrl
import com.framework.views.itemdecoration.LineItemDecoration
import com.google.android.material.tabs.TabLayoutMediator

class FavPosterCatViewHolder(binding: ListItemFavCatBinding) :
  AppBaseRecyclerViewHolder<ListItemFavCatBinding>(binding) {

  override fun bind(position: Int, item: BaseRecyclerViewItem) {
    val model = item as FavCategory

      binding.tvCount.text =model.templates?.size.toString()
      binding.ivImg.loadFromUrl(model.thumbnailUrl)

    if (model.isSelected){
         getColor(R.color.color4ACDFF)?.let {
        binding.borderCard.strokeColor=it
        }
        binding.root.alpha =1F
    }else{
      binding.borderCard.strokeColor=0
      binding.root.alpha = 0.5F
    }
    binding.tvTitle.text = model.name

    binding.root.setOnClickListener {
      listener?.onItemClick(position,model,RecyclerViewActionType.FAV_CAT_CLICKED.ordinal)
    }

    super.bind(position, item)
  }

}