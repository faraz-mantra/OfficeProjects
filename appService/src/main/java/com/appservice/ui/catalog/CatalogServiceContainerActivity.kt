package com.appservice.ui.catalog

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.appservice.R
import com.appservice.base.AppBaseActivity
import com.appservice.constant.FragmentType
import com.appservice.ui.catalog.catalogProduct.information.ProductInformationFragment
import com.appservice.ui.catalog.catalogProduct.product.ProductDetailFragment
import com.appservice.ui.catalog.catalogService.information.ServiceInformationFragment
import com.appservice.ui.catalog.catalogService.service.ServiceDetailFragment
import com.framework.base.BaseFragment
import com.framework.base.FRAGMENT_TYPE
import com.framework.databinding.ActivityFragmentContainerBinding
import com.framework.exceptions.IllegalFragmentTypeException
import com.framework.models.BaseViewModel
import com.framework.views.customViews.CustomToolbar


open class CatalogServiceContainerActivity : AppBaseActivity<ActivityFragmentContainerBinding, BaseViewModel>() {

  private var type: FragmentType? = null
  private var serviceDetailFragment: ServiceDetailFragment? = null
  private var serviceInformationFragment: ServiceInformationFragment? = null
  private var productDetailFragment: ProductDetailFragment? = null
  private var productInformationFragment: ProductInformationFragment? = null

  override fun getLayout(): Int {
    return com.framework.R.layout.activity_fragment_container
  }

//  override fun customTheme(): Int? {
//    return when (type) {
//      FragmentType.PRODUCT_DETAIL_VIEW, FragmentType.PRODUCT_INFORMATION -> R.style.AppBaseTheme
//      else -> super.customTheme()
//    }
//  }

  override fun getViewModelClass(): Class<BaseViewModel> {
    return BaseViewModel::class.java
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    intent?.extras?.getInt(FRAGMENT_TYPE)?.let { type = FragmentType.values()[it] }
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView() {
    super.onCreateView()
    setFragment()
  }

  override fun customTheme(): Int? {
    return when (type) {
      FragmentType.PRODUCT_INFORMATION, FragmentType.PRODUCT_DETAIL_VIEW, FragmentType.SERVICE_DETAIL_VIEW -> R.style.CatalogTheme
      FragmentType.SERVICE_INFORMATION -> R.style.AppTheme_cataloge
      else -> super.customTheme()
    }
  }

  override fun getToolbar(): CustomToolbar? {
    return binding?.appBarLayout?.toolbar
  }

  override fun getToolbarTitleSize(): Float? {
    return resources.getDimension(R.dimen.body_2)
  }

  override fun getToolbarBackgroundColor(): Int? {
    return when (type) {
      FragmentType.PRODUCT_INFORMATION, FragmentType.PRODUCT_DETAIL_VIEW, FragmentType.SERVICE_DETAIL_VIEW -> ContextCompat.getColor(this, R.color.orange)
      else -> super.getToolbarBackgroundColor()
    }
  }

  override fun getToolbarTitleColor(): Int? {
    return when (type) {
      FragmentType.SERVICE_INFORMATION, FragmentType.SERVICE_DETAIL_VIEW -> ContextCompat.getColor(this, R.color.white)
      else -> super.getToolbarTitleColor()
    }
  }

  override fun getNavigationIcon(): Drawable? {
    return when (type) {
      FragmentType.SERVICE_INFORMATION, FragmentType.SERVICE_DETAIL_VIEW, FragmentType.PRODUCT_DETAIL_VIEW, FragmentType.PRODUCT_INFORMATION -> ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)

      else -> super.getNavigationIcon()
    }
  }

  override fun getToolbarTitle(): String? {
    return when (type) {
      FragmentType.SERVICE_INFORMATION -> resources.getString(R.string.other_information)
      FragmentType.SERVICE_DETAIL_VIEW -> resources.getString(R.string.service_details)
      FragmentType.PRODUCT_DETAIL_VIEW -> "Adding a product"
      FragmentType.PRODUCT_INFORMATION -> "Other Info"
      else -> super.getToolbarTitle()
    }
  }

  override fun getToolbarTitleGravity(): Int {
    return Gravity.START
  }


  private fun shouldAddToBackStack(): Boolean {
    return when (type) {
      else -> false
    }
  }

  private fun setFragment() {
    val fragment = getFragmentInstance(type)
    fragment?.arguments = intent.extras
    binding?.container?.id?.let { addFragmentReplace(it, fragment, shouldAddToBackStack()) }
  }

  private fun getFragmentInstance(type: FragmentType?): BaseFragment<*, *>? {
    return when (type) {
      FragmentType.SERVICE_DETAIL_VIEW -> {
        serviceDetailFragment = ServiceDetailFragment.newInstance()
        serviceDetailFragment
      }
      FragmentType.SERVICE_INFORMATION -> {
        serviceInformationFragment = ServiceInformationFragment.newInstance()
        serviceInformationFragment
      }
      FragmentType.PRODUCT_DETAIL_VIEW -> {
        productDetailFragment = ProductDetailFragment.newInstance()
        productDetailFragment
      }
      FragmentType.PRODUCT_INFORMATION -> {
        productInformationFragment = ProductInformationFragment.newInstance()
        productInformationFragment
      }
      else -> throw IllegalFragmentTypeException()
    }
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    serviceDetailFragment?.onActivityResult(requestCode, resultCode, data)
    serviceInformationFragment?.onActivityResult(requestCode, resultCode, data)
    productInformationFragment?.onActivityResult(requestCode, resultCode, data)
    productDetailFragment?.onActivityResult(requestCode, resultCode, data)
  }

  override fun onBackPressed() {
    when (type) {
      FragmentType.SERVICE_DETAIL_VIEW -> serviceDetailFragment?.onNavPressed()
      FragmentType.SERVICE_INFORMATION -> serviceInformationFragment?.onNavPressed()
      FragmentType.PRODUCT_INFORMATION -> productInformationFragment?.onNavPressed()
      FragmentType.PRODUCT_DETAIL_VIEW -> productDetailFragment?.onNavPressed()
      else -> super.onBackPressed()
    }
  }
}

fun Fragment.startFragmentActivity(type: FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean = false, isResult: Boolean = false) {
  val intent = Intent(activity, CatalogServiceContainerActivity::class.java)
  intent.putExtras(bundle)
  intent.setFragmentType(type)
  if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
  if (isResult.not()) startActivity(intent) else startActivityForResult(intent, 101)
}

fun startFragmentActivityNew(activity: Activity, type: FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean, isResult: Boolean = false) {
  val intent = Intent(activity, CatalogServiceContainerActivity::class.java)
  intent.putExtras(bundle)
  intent.setFragmentType(type)
  if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
  if (isResult.not()) activity.startActivity(intent) else activity.startActivityForResult(intent, 300)
}

fun AppCompatActivity.startFragmentActivity(type: FragmentType, bundle: Bundle = Bundle(), clearTop: Boolean = false) {
  val intent = Intent(this, CatalogServiceContainerActivity::class.java)
  intent.putExtras(bundle)
  intent.setFragmentType(type)
  if (clearTop) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
  startActivity(intent)
}

fun Intent.setFragmentType(type: FragmentType): Intent {
  return this.putExtra(FRAGMENT_TYPE, type.ordinal)
}