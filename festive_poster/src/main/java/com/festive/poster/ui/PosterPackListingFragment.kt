package com.festive.poster.ui

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.festive.poster.R
import com.festive.poster.base.AppBaseFragment
import com.festive.poster.constant.RecyclerViewActionType
import com.festive.poster.databinding.FragmentPosterPackListingBinding
import com.festive.poster.models.*
import com.festive.poster.models.response.GetTemplateViewConfigResponse
import com.festive.poster.models.response.GetTemplatesResponse
import com.festive.poster.recyclerView.AppBaseRecyclerViewAdapter
import com.festive.poster.recyclerView.BaseRecyclerViewItem
import com.festive.poster.recyclerView.RecyclerItemClickListener
import com.festive.poster.viewmodels.FestivePosterSharedViewModel
import com.festive.poster.viewmodels.FestivePosterViewModel
import com.framework.base.BaseActivity
import com.framework.pref.UserSessionManager
import com.framework.utils.toArrayList
import com.google.gson.Gson

class PosterPackListingFragment:
    AppBaseFragment<FragmentPosterPackListingBinding, FestivePosterViewModel>(),RecyclerItemClickListener {

    private  val TAG = "PosterPackListingFragme"
    private var adapter: AppBaseRecyclerViewAdapter<PosterPackModel>?=null
    private var sharedViewModel:FestivePosterSharedViewModel?=null
    private var dataList:ArrayList<PosterPackModel>?=null
    private var session:UserSessionManager?=null
    companion object {
        @JvmStatic
        fun newInstance(): PosterPackListingFragment {
            return PosterPackListingFragment()
        }
    }
    override fun getLayout(): Int {
        return R.layout.fragment_poster_pack_listing
    }

    override fun getViewModelClass(): Class<FestivePosterViewModel> {
        return FestivePosterViewModel::class.java
    }

    override fun onCreateView() {
        super.onCreateView()
        session = UserSessionManager(requireActivity())
        sharedViewModel = ViewModelProvider(requireActivity()).get(FestivePosterSharedViewModel::class.java)
        setObserver()
        getTemplateViewConfig()
    }

    private fun setObserver() {
        sharedViewModel?.customizationDetails?.observe(viewLifecycleOwner,{

            Log.i(TAG, "customizationDetails Observer: ")

            dataList?.find {posterPack-> posterPack.tagsModel.Name==it.tag }?.posterList?.forEach {
                
            }

            adapter?.notifyDataSetChanged()

        })
    }


    private fun getTemplateViewConfig() {

        viewModel?.getTemplateConfig(session?.fPID,session?.fpTag)
            ?.observe(viewLifecycleOwner,{
                Log.i(TAG, "template config: ${Gson().toJson(it)}")
                val response = it as? GetTemplateViewConfigResponse
                response?.let {
                    val tagArray = prepareTagForApi(response.Result.TodaysPicks.Tags)
                    fetchTemplates(tagArray,response)
                }
            })

     /*   val keyList = arrayListOf(PosterKeyModel(
            "https://file-examples-com.github.io/uploads/2017/10/file_example_JPG_100kB.jpg",10,"IMAGE_PATH","Image"),
            PosterKeyModel(
                "Hello boost 36",10,"Beautiful Smiles","Text"))*/


       /* dataList = arrayListOf(
            PosterPackModel("Navratri",10, arrayListOf(PosterModel(null,posterKeys = keyList),PosterModel(null,posterKeys = keyList))),
            PosterPackModel("Navratri",10, arrayListOf(PosterModel(null,posterKeys = keyList))),
            PosterPackModel("Navratri",10, arrayListOf(PosterModel(null,posterKeys = keyList))),
            )
*/



    }

    private fun fetchTemplates(tagArray: ArrayList<String>, response: GetTemplateViewConfigResponse) {
        viewModel?.getTemplates(session?.fPID,session?.fpTag,tagArray)
            ?.observe(viewLifecycleOwner,{
                dataList = ArrayList()
                val templates_response = it as? GetTemplatesResponse

                templates_response?.let {
                    response.Result.TodaysPicks.Tags.forEach {pack_tag->

                        val template =   templates_response.Result.Templates.filter {template->
                            template.Tags.find { posterTag-> posterTag ==pack_tag.Tag }!=null }

                        dataList?.add(PosterPackModel(pack_tag,template.toArrayList()))
                    }

                    adapter = AppBaseRecyclerViewAdapter(requireActivity() as BaseActivity<*, *>,
                        dataList!!,this)
                    binding?.rvPosters?.adapter = adapter
                    binding?.rvPosters?.layoutManager = LinearLayoutManager(requireActivity())


                }
            })
    }


    private fun prepareTagForApi(tags: List<PosterPackTagModel>): ArrayList<String> {

        val list = ArrayList<String>()
        tags.forEach {
            list.add(it.Tag)
        }
        return list
    }

    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {

    }

    override fun onChildClick(
        childPosition: Int,
        parentPosition: Int,
        childItem: BaseRecyclerViewItem?,
        parentItem: BaseRecyclerViewItem?,
        actionType: Int
    ) {
        when(actionType){
            RecyclerViewActionType.POSTER_TAP_TO_EDIT_CLICK.ordinal->{
                parentItem as PosterPackModel
                CustomizePosterSheet.newInstance(parentItem.tagsModel.Tag).show(requireActivity().supportFragmentManager,CustomizePosterSheet::class.java.name)
            }
        }
    }


}
