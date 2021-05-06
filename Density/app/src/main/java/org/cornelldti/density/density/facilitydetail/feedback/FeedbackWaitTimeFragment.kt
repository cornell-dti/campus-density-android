package org.cornelldti.density.density.facilitydetail.feedback

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import org.cornelldti.density.density.R

class FeedbackWaitTimeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var waitTimePicker: NumberPicker
    private lateinit var buttonPrev: Button
    private lateinit var buttonNext: Button
    private lateinit var buttonClose: ImageView
    private var selectedAnswer = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_wait_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = parentFragment?.view?.findViewById(R.id.viewPager)
                ?: context?.let { ViewPager2(it) }!!
        waitTimePicker = view.findViewById(R.id.picker_wait_time)
        buttonPrev = view.findViewById(R.id.button_previous)
        buttonNext = view.findViewById(R.id.button_next)
        buttonClose = view.findViewById(R.id.button_close)

        setWaitTimePicker()
        setButtonPrev()
        setButtonNext()
        setButtonClose()
    }

    /**
     * This function sets the observedWaitTime input. Input is set as the lower bound for each 2-min time range.
     * (e.g. 0 for "0-2" and 2 for "2-4")
     */
    private fun setWaitTimePicker() {
        val options = arrayOf("0 - 2", "2 - 4", "4 - 6", "6 - 8", "8 - 10", "10 - 12", "12 - 14", "14 - 16", "16 - 18", "18 - 20", "20 +")

        waitTimePicker.maxValue = 0
        waitTimePicker.maxValue = options.size - 1
        waitTimePicker.displayedValues = options
        waitTimePicker.wrapSelectorWheel = false
        waitTimePicker.setOnValueChangedListener { picker, oldVal, newVal ->
            selectedAnswer = newVal * 2
        }
    }

    private fun setButtonPrev() {
        buttonPrev.setOnClickListener {
            viewPager.setCurrentItem((parentFragment as FeedbackDialogFragment).getPagerItem(-1), false)
        }
    }

    private fun setButtonNext() {
        buttonNext.setOnClickListener {
            (parentFragment as FeedbackDialogFragment).observedWaitTime = selectedAnswer
            viewPager.setCurrentItem((parentFragment as FeedbackDialogFragment).getPagerItem(1), false)
        }
    }

    private fun setButtonClose() {
        buttonClose.setOnClickListener {
            val intent = Intent("FEEDBACK_BROADCAST_ACTION")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }
    }

}