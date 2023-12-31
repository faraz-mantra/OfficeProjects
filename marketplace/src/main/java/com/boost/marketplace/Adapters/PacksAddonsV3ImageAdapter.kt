package com.boost.marketplace.Adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.boost.cart.utils.Utils
import com.boost.dbcenterapi.data.api_model.packageAddonsCompares.AddonsPacksIn
import com.boost.dbcenterapi.utils.SharedPrefs
import com.boost.marketplace.R
import com.boost.marketplace.interfaces.AddonsListenerV3
import com.bumptech.glide.Glide
import com.framework.utils.RootUtil
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class PacksAddonsV3ImageAdapter(
    var compareList: List<AddonsPacksIn>,
    val myAddonsListener: AddonsListenerV3,
    val activity: Activity
) : RecyclerView.Adapter<PacksAddonsV3ImageAdapter.upgradeViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): upgradeViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(
            R.layout.item_packsv3_image_addons, parent, false
        )
        context = itemView.context
        return upgradeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return compareList.size
    }

    override fun onBindViewHolder(holder: upgradeViewHolder, position: Int) {

        if (compareList.get(position).packageStatus) {
            if (compareList.get(position).count == -1) {
                if (compareList.get(position).packName.contains("##")) {
                    holder.count.visibility = View.GONE
                    holder.bundleLayout.visibility = View.VISIBLE
                    holder.packTitle.text = compareList.get(position).packName.split("##")[0]
                    Glide.with(context).load(compareList.get(position).packName.split("##")[1])
                        .into(holder.packImage)
                    holder.itemView.setOnClickListener {
                        myAddonsListener.onPackageItemClicked(
                            compareList.get(position).packName.split(
                                "##"
                            )[0]
                        )
                    }
                } else {
                    holder.bundleLayout.visibility = View.GONE
                    holder.count.visibility = View.VISIBLE
                    val prefs = SharedPrefs(activity)
                    val tempPrice = Utils.priceCalculatorForYear(compareList.get(position).packName.toDouble(), "", activity)
                    val bundlePrice = if (prefs.getYearPricing())
                        "₹" + NumberFormat.getNumberInstance(Locale.ENGLISH)
                            .format(RootUtil.round(tempPrice,0)) + "/year"
                    else "₹" + NumberFormat.getNumberInstance(Locale.ENGLISH)
                        .format(RootUtil.round(tempPrice,0)) + "/month"
                    holder.count.text = bundlePrice
                }
                holder.image.visibility = View.GONE
                holder.image1.visibility = View.GONE
            } else if (compareList.get(position).count > 2) {
                holder.count.visibility = View.VISIBLE
                holder.count.text = compareList.get(position).count.toString()
                holder.image.visibility = View.GONE
                holder.image1.visibility = View.GONE
            } else {
                holder.count.visibility = View.GONE
                holder.image.visibility = View.VISIBLE
                holder.image1.visibility = View.GONE
            }
        } else {
            holder.count.visibility = View.GONE
            holder.image.visibility = View.GONE
            holder.image1.visibility = View.VISIBLE
        }
    }

    fun addupdates(upgradeModel: List<AddonsPacksIn>) {
        compareList = upgradeModel
        notifyDataSetChanged()
    }

    class upgradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.imageStatus)!!
        val image1 = itemView.findViewById<ImageView>(R.id.imageStatus1)!!
        val count = itemView.findViewById<TextView>(R.id.count)!!
        val packImage = itemView.findViewById<ImageView>(R.id.packs_image)!!
        val packTitle = itemView.findViewById<TextView>(R.id.pack_title)!!
        val bundleLayout = itemView.findViewById<ConstraintLayout>(R.id.bundle_layout)!!
    }
}