package org.cornelldti.density.density.facilities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cornelldti.density.density.BaseFragment
import org.cornelldti.density.density.R

class GymFacilitiesFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.gym_facilities_fragment, container, false)
        return v
    }

}