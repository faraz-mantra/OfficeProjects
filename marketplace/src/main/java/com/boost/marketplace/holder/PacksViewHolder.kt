package com.boost.marketplace.holder

import com.boost.dbcenterapi.recycleritem.AppBaseRecyclerViewHolder
import com.boost.dbcenterapi.recycleritem.BaseRecyclerViewItem
import com.boost.marketplace.constant.RecyclerViewActionType
import com.boost.marketplace.databinding.ItemPacksBinding

class PacksViewHolder(binding: ItemPacksBinding) : AppBaseRecyclerViewHolder<ItemPacksBinding>(binding) {

  override fun bind(position: Int, item: BaseRecyclerViewItem) {
    super.bind(position, item)

    binding.packsFL.setOnClickListener {
      listener?.onItemClick(
        position,
        item,
        RecyclerViewActionType.PACKS_CLICK.ordinal
      )
    }
  }
}