package com.boost.cart.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boost.cart.R
import com.boost.dbcenterapi.data.api_model.GetAllWidgets.Review
import java.util.*

class ReviewViewPagerAdapter(val list: ArrayList<Review>) :
  RecyclerView.Adapter<ReviewViewPagerAdapter.PagerVH>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
    val item = View.inflate(parent.context, R.layout.snippet_items, null)
    val lp = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    )
    item.layoutParams = lp
    return PagerVH(item)
  }

  override fun getItemCount(): Int {
    return list.size
  }

  fun addupdates(newList: List<Review>) {
    val initPosition = list.size
    list.clear()
    list.addAll(newList)
    notifyItemRangeInserted(initPosition, list.size)
  }

  override fun onBindViewHolder(holder: PagerVH, position: Int) {
    holder.name.setText(list.get(position).name)
    holder.businessType.setText(list.get(position).businessType)
    holder.title.setText(list.get(position).title)
    holder.desc.setText(list.get(position).desc)
    when (list.get(position).rating) {
      1 -> {
        holder.rating1.visibility = View.VISIBLE
      }
      2 -> {
        holder.rating1.visibility = View.VISIBLE
        holder.rating2.visibility = View.VISIBLE
      }
      3 -> {
        holder.rating1.visibility = View.VISIBLE
        holder.rating2.visibility = View.VISIBLE
        holder.rating3.visibility = View.VISIBLE
      }
      4 -> {
        holder.rating1.visibility = View.VISIBLE
        holder.rating2.visibility = View.VISIBLE
        holder.rating3.visibility = View.VISIBLE
        holder.rating4.visibility = View.VISIBLE
      }
      5 -> {
        holder.rating1.visibility = View.VISIBLE
        holder.rating2.visibility = View.VISIBLE
        holder.rating3.visibility = View.VISIBLE
        holder.rating4.visibility = View.VISIBLE
        holder.rating5.visibility = View.VISIBLE
      }
    }
  }

  class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var name = itemView.findViewById<TextView>(R.id.name)!!
    var businessType = itemView.findViewById<TextView>(R.id.textView16)!!
    var profileImage = itemView.findViewById<ImageView>(R.id.cir_img)!!
    var title = itemView.findViewById<TextView>(R.id.txt21)!!
    var desc = itemView.findViewById<TextView>(R.id.review_description)!!
    var rating1 = itemView.findViewById<ImageView>(R.id.rating_1)!!
    var rating2 = itemView.findViewById<ImageView>(R.id.rating_2)!!
    var rating3 = itemView.findViewById<ImageView>(R.id.rating_3)!!
    var rating4 = itemView.findViewById<ImageView>(R.id.rating_4)!!
    var rating5 = itemView.findViewById<ImageView>(R.id.rating_5)!!
  }

}