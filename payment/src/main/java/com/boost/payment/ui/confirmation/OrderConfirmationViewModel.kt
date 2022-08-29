package com.boost.payment.ui.confirmation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boost.dbcenterapi.data.api_model.Domain.DomainBookingRequest
import com.boost.dbcenterapi.data.remote.ApiInterface
import com.boost.dbcenterapi.data.remote.NewApiInterface
import com.boost.dbcenterapi.upgradeDB.local.AppDatabase
import com.boost.dbcenterapi.utils.Constants.Companion.clientid
import com.boost.dbcenterapi.utils.SharedPrefs
import com.boost.dbcenterapi.utils.Utils
import es.dmoral.toasty.Toasty
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class OrderConfirmationViewModel : ViewModel() {
  var ApiService = Utils.getRetrofit().create(NewApiInterface::class.java)
  var updatesLoader: MutableLiveData<String> = MutableLiveData()

  fun getLoaderStatus(): LiveData<String> {
    return updatesLoader
  }

  fun emptyCurrentCartWithDomainActivate(app: Application, activity: Activity) {
    CompositeDisposable().add(
      AppDatabase.getInstance(app)!!
        .cartDao()
        .getCartItems()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess {
          for(singleItem in it){
            if(singleItem.feature_code!!.equals("DOMAINPURCHASE") && !singleItem.addon_title.isNullOrEmpty()){
              updatesLoader.postValue("Domain Activation Is in Progress")
              val pref = app.getSharedPreferences("nowfloatsPrefs", Context.MODE_PRIVATE)
              val fpTag = pref.getString("GET_FP_DETAILS_TAG", null)
              val splitStr: ArrayList<String> = singleItem.addon_title!!.split(".")?.toCollection(ArrayList())!!
              var domainName = ""
              var domainType = "."
              val prefs = SharedPrefs(activity)
              var validityInYears = "1"
              var months: Double = 0.0
              if (prefs.getYearPricing()){
                //do the yearly calculation here
                months = (prefs.getStoreMonthsValidity() * 12).toDouble()
              }else{
                months = prefs.getStoreMonthsValidity().toDouble()
              }
              val tempMonth: Double = months / 12.0
              if(tempMonth.toInt() < tempMonth){ //example 1 < 1.2
                validityInYears = (tempMonth.toInt()+1).toString()
              }else{
                validityInYears = (tempMonth.toInt()).toString()
              }
              splitStr.forEachIndexed { index, element ->
                if(splitStr.size > 2){
                  if(index == splitStr.size-1 ){
                    domainType = domainType + element
                  }else{
                    domainName = domainName +"."+ element
                  }
                }else{
                  if(index==0)
                  domainName = element
                  else if(index==1)
                    domainType = domainType + element
                }
              }
              val bookingRequest = DomainBookingRequest(
                0,
                0,
                clientid,
                1,
                domainName,
                domainType,
                fpTag!!,
                validityInYears
              )
              ApiService.buyDomainBooking(bookingRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                  {
                    Log.i("buyDomainBooking","Success >>>"+it)
                    updatesLoader.postValue("")
                    Toasty.success(activity.applicationContext,"Successfully Booked Your Domain. It Takes 24 to 48 hours to activate",
                      Toast.LENGTH_LONG)
                  },{
                    Log.i("buyDomainBooking", "Failure >>>$it")
                    updatesLoader.postValue("")
                    Toasty.error(activity.applicationContext,"Payment is successsfull. But Failed to Booked Your Domain. Try after 24 hours in [My Current Plan]",
                      Toast.LENGTH_LONG)
                  }
                )
            }
          }
          Completable.fromAction {
            AppDatabase.getInstance(app)!!.cartDao().emptyCart()
          }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
              //if any action post empty_cart
            }
            .doOnError {
              //in case of error
            }
            .subscribe()
        }
        .doOnError {
          Log.e("getCartItems","Failed to get item -> "+it.message)
        }
        .subscribe()
    )
  }

  fun emptyCurrentCart(app: Application) {
    Completable.fromAction {
      AppDatabase.getInstance(app)!!.cartDao().emptyCart()
    }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnComplete {
        //if any action post empty_cart
      }
      .doOnError {
        //in case of error
      }
      .subscribe()
  }

//  fun getFpDetails(fpId: String, map: Map<String, String>): LiveData<BaseResponse> {
//    return WithFloatTwoRepository.getFpDetails(fpId, map).toLiveData()
//  }
}
