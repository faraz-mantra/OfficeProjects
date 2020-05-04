package com.boost.upgrades.ui.home

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.biz2.nowfloats.boost.updates.data.remote.ApiInterface
import com.biz2.nowfloats.boost.updates.persistance.local.AppDatabase
import com.boost.upgrades.data.api_model.GetAllFeatures.response.FeatureDeals
import com.boost.upgrades.data.model.BundlesModel
import com.boost.upgrades.data.model.CartModel
import com.boost.upgrades.data.model.FeaturesModel
import com.boost.upgrades.data.model.WidgetModel
import com.boost.upgrades.utils.Utils
import com.google.gson.Gson
import com.luminaire.apolloar.base_class.BaseViewModel
import es.dmoral.toasty.Toasty
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HomeViewModel(application: Application) : BaseViewModel(application) {
    var updatesResult: MutableLiveData<List<WidgetModel>> = MutableLiveData()
    var allAvailableFeaturesDownloadResult: MutableLiveData<List<FeaturesModel>> = MutableLiveData()
    var allBundleResult: MutableLiveData<List<BundlesModel>> = MutableLiveData()
    var allFeatureDealsResult: MutableLiveData<List<FeatureDeals>> = MutableLiveData()
    var _totalActiveAddonsCount: MutableLiveData<Int> = MutableLiveData()

    var updatesError: MutableLiveData<String> = MutableLiveData()
    var updatesLoader: MutableLiveData<Boolean> = MutableLiveData()
    var cartResult: MutableLiveData<List<CartModel>> = MutableLiveData()

    val compositeDisposable = CompositeDisposable()
    var ApiService = Utils.getRetrofit().create(ApiInterface::class.java)

    var experienceCode: String = "SVC"

    fun upgradeResult(): LiveData<List<WidgetModel>> {
        return updatesResult
    }

    fun getAllAvailableFeatures(): LiveData<List<FeaturesModel>> {
        return allAvailableFeaturesDownloadResult
    }

    fun getAllFeatureDeals(): LiveData<List<FeatureDeals>> {
        return allFeatureDealsResult
    }

    fun getAllBundles(): LiveData<List<BundlesModel>> {
        return allBundleResult
    }

    fun getTotalActiveWidgetCount(): LiveData<Int> {
        return _totalActiveAddonsCount
    }

    fun cartResult(): LiveData<List<CartModel>> {
        return cartResult
    }

    fun updatesError(): LiveData<String> {
        return updatesError
    }

    fun updatesLoader(): LiveData<Boolean> {
        return updatesLoader
    }

    fun setCurrentExperienceCode(code: String) {
        experienceCode = code
    }

    fun loadUpdates(fpid: String, clientId: String) {
        updatesLoader.postValue(true)

        if (Utils.isConnectedToInternet(getApplication())) {
            compositeDisposable.add(
                    ApiService.GetAllFeatures()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    {
                                        Log.e("GetAllFeatures", it.toString())
                                        var data = arrayListOf<FeaturesModel>()
                                        for (item in it.Data[0].features) {
                                            if (item.exclusive_to_categories != null && item.exclusive_to_categories.size > 0) {
                                                var applicableToCurrentExpCode = false
                                                for (code in item.exclusive_to_categories) {
                                                    if (code.equals(experienceCode, true))
                                                        applicableToCurrentExpCode = true
                                                }
                                                if (!applicableToCurrentExpCode)
                                                    continue;
                                            }

                                            val primaryImage = if (item.primary_image == null) null else item.primary_image.url
                                            val secondaryImages = ArrayList<String>()
                                            if (item.secondary_images != null) {
                                                for (sec_images in item.secondary_images) {
                                                    if (sec_images.url != null) {
                                                        secondaryImages.add(sec_images.url)
                                                    }
                                                }
                                            }
                                            data.add(FeaturesModel(
                                                    item.boost_widget_key,
                                                    item.name,
                                                    item.feature_code,
                                                    item.description,
                                                    item.description_title,
                                                    item.createdon,
                                                    item.updatedon,
                                                    item.websiteid,
                                                    item.isarchived,
                                                    item.is_premium,
                                                    item.target_business_usecase,
                                                    item.feature_importance,
                                                    item.discount_percent,
                                                    item.price,
                                                    item.time_to_activation,
                                                    primaryImage,
                                                    if (item.feature_banner == null) null else item.feature_banner.url,
                                                    if (secondaryImages.size == 0) null else Gson().toJson(secondaryImages),
                                                    Gson().toJson(item.learn_more_link),
                                                    if (item.total_installs == null) "--" else item.total_installs,
                                                    if (item.extended_properties != null && item.extended_properties.size > 0) Gson().toJson(item.extended_properties) else null,
                                                    if (item.exclusive_to_categories != null && item.exclusive_to_categories.size > 0) Gson().toJson(item.exclusive_to_categories) else null
                                            ))
                                        }
                                        Completable.fromAction {
                                            AppDatabase.getInstance(getApplication())!!
                                                    .featuresDao()
                                                    .insertAllFeatures(data)
                                        }
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .doOnComplete {
                                                    Log.i("insertAllFeatures", "Successfully")
                                                    compositeDisposable.add(
                                                            AppDatabase.getInstance(getApplication())!!
                                                                    .featuresDao()
                                                                    .getFeaturesItems(true)
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .doOnSuccess {
                                                                        allAvailableFeaturesDownloadResult.postValue(it)
                                                                        updatesLoader.postValue(false)

                                                                        compositeDisposable.add(
                                                                                ApiService.GetFloatingPointWebWidgets(fpid, clientId)
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe(
                                                                                                {
                                                                                                    compositeDisposable.add(
                                                                                                            AppDatabase.getInstance(getApplication())!!
                                                                                                                    .featuresDao()
                                                                                                                    .getallActivefeatureCount(it.Result)
                                                                                                                    .subscribeOn(Schedulers.io())
                                                                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                                                                    .doOnSuccess {
                                                                                                                        _totalActiveAddonsCount.postValue(it)
                                                                                                                    }
                                                                                                                    .doOnError {
                                                                                                                        updatesError.postValue(it.message)
                                                                                                                    }
                                                                                                                    .subscribe()
                                                                                                    )
                                                                                                },
                                                                                                {
                                                                                                    updatesError.postValue(it.message)
                                                                                                    updatesLoader.postValue(false)
                                                                                                }
                                                                                        )

                                                                        )

                                                                    }
                                                                    .doOnError {
                                                                        updatesError.postValue(it.message)
                                                                        updatesLoader.postValue(false)
                                                                    }
                                                                    .subscribe()
                                                    )
                                                }
                                                .doOnError {
                                                    updatesError.postValue(it.message)
                                                    updatesLoader.postValue(false)
                                                }
                                                .subscribe()

                                        //saving bundle info in bundle table
                                        val bundles = arrayListOf<BundlesModel>()
                                        for (item in it.Data[0].bundles) {
                                            bundles.add(BundlesModel(
                                                    item._kid,
                                                    item.name,
                                                    item.min_purchase_months,
                                                    item.overall_discount_percent,
                                                    if (item.primary_image != null) item.primary_image.url else null,
                                                    Gson().toJson(item.included_features)
                                            ))
                                        }
                                        Completable.fromAction {
                                            AppDatabase.getInstance(getApplication())!!
                                                    .bundlesDao()
                                                    .insertAllBundles(bundles)
                                        }
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .doOnComplete {
                                                    Log.i("insertAllBundles", "Successfully")
                                                    allBundleResult.postValue(bundles)
                                                    updatesLoader.postValue(false)
                                                }
                                                .doOnError {
                                                    updatesError.postValue(it.message)
                                                    updatesLoader.postValue(false)
                                                }
                                                .subscribe()

                                        //getting features deals
                                        if (it.Data[0].feature_deals.size > 0)
                                            allFeatureDealsResult.postValue(it.Data[0].feature_deals)

                                    },
                                    {
                                        Log.e("GetAllFeatures", "error" + it.message)
                                        updatesLoader.postValue(false)
                                    }
                            )
            )
        } else {
            compositeDisposable.add(
                    AppDatabase.getInstance(getApplication())!!
                            .widgetDao()
                            .queryUpdates()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSuccess {
                                updatesResult.postValue(it)
                                updatesLoader.postValue(false)
                            }
                            .doOnError {
                                updatesError.postValue(it.message)
                                updatesLoader.postValue(false)
                            }
                            .subscribe()
            )
        }
    }

    fun getCartItems() {
        compositeDisposable.add(
                AppDatabase.getInstance(getApplication())!!
                        .cartDao()
                        .getCartItems()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            cartResult.postValue(it)
                            updatesLoader.postValue(false)
                        }, {
                            updatesError.postValue(it.message)
                            updatesLoader.postValue(false)
                        }
                        )
        )
    }

    fun addItemToCart(updatesModel: FeaturesModel) {
        updatesLoader.postValue(true)
        val discount = 100 - updatesModel.discount_percent
        val paymentPrice = (discount * updatesModel.price) / 100.0
        val cartItem = CartModel(
                updatesModel.boost_widget_key,
                updatesModel.name,
                updatesModel.description,
                updatesModel.primary_image,
                paymentPrice,
                updatesModel.price.toDouble(),
                updatesModel.discount_percent,
                1,
                "features",
                updatesModel.extended_properties
        )


        Completable.fromAction {
            AppDatabase.getInstance(getApplication())!!.cartDao()
                    .insertToCart(cartItem)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    getCartItems()
                    updatesLoader.postValue(false)
                }
                .doOnError {
                    updatesError.postValue(it.message)
                    updatesLoader.postValue(false)
                }
                .subscribe()
    }

}
