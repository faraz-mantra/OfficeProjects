package dev.patrickgold.florisboard.customization.viewholder.visitingCard

import com.framework.extensions.gone
import com.framework.extensions.visible
import com.framework.glide.util.glideLoad
import com.framework.utils.fromHtml
import com.onboarding.nowfloats.databinding.ItemVisitingCardFiveBinding
import com.onboarding.nowfloats.model.channel.statusResponse.ChannelAccessStatusResponse
import dev.patrickgold.florisboard.customization.adapter.BaseRecyclerItem
import dev.patrickgold.florisboard.customization.adapter.BaseRecyclerViewHolder
import dev.patrickgold.florisboard.customization.adapter.OnItemClickListener
import dev.patrickgold.florisboard.customization.model.response.DigitalCardDataKeyboard

class VisitingCardFiveViewHolder(binding: ItemVisitingCardFiveBinding, val listener: OnItemClickListener?) :
  BaseRecyclerViewHolder<ItemVisitingCardFiveBinding>(binding) {

  override fun bindTo(position: Int, item: BaseRecyclerItem?) {
    val data = (item as? DigitalCardDataKeyboard)?.cardData ?: return
    binding.businessName.text = data.businessName
    binding.number.text = data.number
    ChannelAccessStatusResponse.visibleChannels(binding.itemChannelsGroup.containerChannels)
    data.cardIcon?.let { binding.imgLogo.setImageResource(it) }
    if (data.businessLogo.isNullOrEmpty().not()) {
      binding.profileView.visible()
      binding.channels.gone()
      binding.imgBusinessLogo.context.glideLoad(binding.imgBusinessLogo, data.businessLogo!!)
    } else {
      binding.profileView.gone()
      binding.channels.visible()
    }
    binding.email.text = fromHtml("<u>${data.email}</u>")
    binding.website.text = fromHtml("<u>${data.website}</u>")
  }
}