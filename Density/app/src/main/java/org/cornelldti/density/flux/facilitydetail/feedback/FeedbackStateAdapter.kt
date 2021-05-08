package org.cornelldti.density.flux.facilitydetail.feedback

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FeedbackStateAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {
    private val mFragmentList: ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun addFragment(dialogFragment: Fragment) {
        mFragmentList.add(dialogFragment)
    }

}