package org.cornelldti.density.density.facilitydetail.feedback

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import org.cornelldti.density.density.R


class FeedbackThirdFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonPrev : Button
    private lateinit var buttonNext : Button
    private lateinit var buttonClose : ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = parentFragment?.view?.findViewById(R.id.viewPager) ?: context?.let { ViewPager2(it) }!!
        buttonPrev = view.findViewById(R.id.button_previous)
        buttonNext = view.findViewById(R.id.button_next)
        buttonClose = view.findViewById(R.id.button_close)

        setButtonPrev()
        setButtonNext()
        setButtonClose()
    }

    private fun setButtonPrev(){
        buttonPrev.setOnClickListener{
            viewPager.setCurrentItem(getItem(-1),false)
        }
    }

    private fun setButtonNext(){
        buttonNext.setOnClickListener{
            viewPager.setCurrentItem(getItem(1), false)
        }
    }

    private fun setButtonClose(){
        buttonClose.setOnClickListener {
            val intent = Intent("BROADCAST_ACTION")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }
    }

    private fun getItem(i: Int): Int {
        return viewPager.getCurrentItem() + i
    }
}
