package dev.patrickgold.florisboard.customization.viewholder

import android.graphics.Paint
import com.bumptech.glide.Glide
import com.framework.extensions.gone
import com.framework.extensions.visible
import dev.patrickgold.florisboard.R
import dev.patrickgold.florisboard.customization.adapter.BaseRecyclerItem
import dev.patrickgold.florisboard.customization.adapter.BaseRecyclerViewHolder
import dev.patrickgold.florisboard.customization.adapter.OnItemClickListener
import dev.patrickgold.florisboard.customization.model.response.ItemServices
import dev.patrickgold.florisboard.customization.model.response.Product
import dev.patrickgold.florisboard.customization.util.RecyclerViewActionType
import dev.patrickgold.florisboard.databinding.AdapterItemProductNewBinding

class ServiceViewHolder(binding: AdapterItemProductNewBinding, val listener: OnItemClickListener?) : BaseRecyclerViewHolder<AdapterItemProductNewBinding>(binding) {

  override fun bindTo(position: Int, item: BaseRecyclerItem?) {
    val service = (item as? ItemServices)?.data ?: return
    Glide.with(binding.imageView).load(service.tileImage).placeholder(R.drawable.placeholder_image_n).into(binding.imageView)
    binding.tvName.text = service.name

    binding.tvPrice.text = "${service.currency ?: "INR"} ${service.discountedPrice}"

    if ((service.price ?: 0.0 <= service.discountedPrice ?: 0.0).not()) {
      binding.tvDiscount.visible()
      binding.tvDiscount.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
      binding.tvDiscount.text = "${service.currency ?: "INR"} ${service.price}"
      binding.tvOffPercent.visible()
      binding.tvOffPercent.text = service.getServiceOffPrice()
    } else {
      binding.tvDiscount.gone()
      binding.tvOffPercent.gone()
    }

    if (service.duration ?: 0 <= 0) {
      binding.ctvDuration.gone()
    } else {
      binding.ctvDuration.visible()
      binding.ctvDuration.text = "${service.duration} min"
    }

    binding.tvDescription.text = service.description
    binding.btnShare.setOnClickListener { listener?.onItemClick(position, item, RecyclerViewActionType.SERVICE_SHARE_CLICK.ordinal) }
  }
}