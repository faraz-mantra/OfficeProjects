package com.appservice.staffs.ui.breaks

import android.text.Html
import android.view.View
import androidx.fragment.app.Fragment
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.databinding.FragmentScheduleBreaksBinding
import com.appservice.staffs.ui.bottomsheets.BottomSheetFragment
import com.framework.models.BaseViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [ScheduledBreaksFragmnt.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScheduledBreaksFragmnt : AppBaseFragment<FragmentScheduleBreaksBinding, BaseViewModel>() {


    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(): ScheduledBreaksFragmnt {
            return ScheduledBreaksFragmnt()
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_schedule_breaks
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {
        super.onCreateView()
        setOnClickListener(binding!!.flAddBreaks)
        binding!!.ctvHeading.text = Html.fromHtml(getString(R.string.add_a_leave_break_for_br_gaurav_sharma))
    }

    override fun onClick(v: View) {
        super.onClick(v)
//        val bundle: Bundle = Bundle.EMPTY
        when (v) {
            binding?.flAddBreaks -> {
                val bottomSheet = BottomSheetFragment()
                bottomSheet.show(requireActivity().supportFragmentManager, null)
            }
        }
    }
}