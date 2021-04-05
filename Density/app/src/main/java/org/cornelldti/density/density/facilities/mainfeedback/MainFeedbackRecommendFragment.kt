package org.cornelldti.density.density.facilities.mainfeedback

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import org.cornelldti.density.density.R
import org.cornelldti.density.density.facilitydetail.FacilityInfoPage


class MainFeedbackRecommendFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var radioGroup: RadioGroup
    private lateinit var buttonNext: Button
    private lateinit var buttonClose: ImageView
    private var selectedAnswer = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_feedback_recommend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = parentFragment?.view?.findViewById(R.id.viewPager)
                ?: context?.let { ViewPager2(it) }!!
        radioGroup = view.findViewById(R.id.answer_dialog_radio)
        buttonNext = view.findViewById(R.id.button_next)
        buttonClose = view.findViewById(R.id.button_close)

        setRadioGroup()
        setButtonNext()
        setButtonClose()
    }

    private fun setRadioGroup() {
        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i != null) {
                buttonNext.isEnabled = true
                buttonNext.setBackgroundColor(resources.getColor(R.color.feedback_button))
                when (i) {
                    null -> selectedAnswer = -1
                    R.id.first_radio -> selectedAnswer = 1
                    R.id.second_radio -> selectedAnswer = 2
                    R.id.third_radio -> selectedAnswer = 3
                    R.id.fourth_radio -> selectedAnswer = 4
                    R.id.fifth_radio -> selectedAnswer = 5
                }
            }
        }
    }

    private fun setButtonNext() {
        buttonNext.isEnabled = false
        buttonNext.setBackgroundColor(resources.getColor(R.color.dark_grey))

        buttonNext.setOnClickListener {
            (parentFragment as MainFeedbackDialogFragment).recommendInput = selectedAnswer
            viewPager.setCurrentItem((parentFragment as MainFeedbackDialogFragment).getPagerItem(1), false)
        }
    }

    private fun setButtonClose() {
        buttonClose.setOnClickListener {
            val intent = Intent("MAIN_FEEDBACK_BROADCAST_ACTION")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }
    }
}
