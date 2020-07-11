package org.cornelldti.density.density.facilities

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.facilities_activity.*
import org.cornelldti.density.density.BaseFragment
import org.cornelldti.density.density.R
import kotlin.math.absoluteValue

class GymFacilitiesFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.gym_facilities_activity, container, false)
    }

}