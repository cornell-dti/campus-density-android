package org.cornelldti.density.density.facilitydetail.feedback

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class FeedbackPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
    private val mFragmentList: ArrayList<DialogFragment> = ArrayList()

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList.get(position)
    }

    fun addFragment(dialogFragment: DialogFragment){
        mFragmentList.add(dialogFragment)
    }

}