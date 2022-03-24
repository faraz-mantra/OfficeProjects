package com.boost.marketplace.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boost.dbcenterapi.upgradeDB.local.AppDatabase
import com.boost.marketplace.R
import com.boost.marketplace.interfaces.HomeListener
import com.boost.marketplace.ui.home.MarketPlaceActivity
import com.framework.analytics.SentryController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class HowToUseAdapter(
  val activity: Activity,
  cryptoCurrencies: List<String>?
) : RecyclerView.Adapter<HowToUseAdapter.upgradeViewHolder>() {

  private var upgradeList = ArrayList<String>()
  private lateinit var context: Context

  init {
    this.upgradeList = cryptoCurrencies as ArrayList<String>
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): upgradeViewHolder {
    val itemView = LayoutInflater.from(parent?.context).inflate(
      R.layout.how_to_use_item, parent, false
    )
    context = itemView.context
    return upgradeViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return 5//upgradeList.size
  }

  override fun onBindViewHolder(holder: upgradeViewHolder, position: Int) {
    holder.position.text = (position+1).toString()
    if(position==0){
      holder.topLine.visibility = View.GONE
      holder.bottomLine.visibility = View.VISIBLE
    }else if(position == 5-1/*upgradeList.size - 1*/){
      holder.topLine.visibility = View.VISIBLE
      holder.bottomLine.visibility = View.GONE
    }else{
      holder.topLine.visibility = View.VISIBLE
      holder.bottomLine.visibility = View.VISIBLE
    }
  }

  fun addupdates(upgradeModel: List<String>) {
    val initPosition = upgradeList.size
    upgradeList.clear()
    upgradeList.addAll(upgradeModel)
    notifyItemRangeInserted(initPosition, upgradeList.size)
  }

  class upgradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var position = itemView.findViewById<TextView>(R.id.position)
    var title = itemView.findViewById<TextView>(R.id.title)
    var desc = itemView.findViewById<TextView>(R.id.desc)
    var topLine = itemView.findViewById<View>(R.id.top_line)
    var bottomLine = itemView.findViewById<View>(R.id.bottom_line)
  }
}