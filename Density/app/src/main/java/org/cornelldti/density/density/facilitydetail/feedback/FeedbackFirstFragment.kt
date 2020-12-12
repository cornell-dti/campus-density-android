package org.cornelldti.density.density.facilitydetail.feedback

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import org.cornelldti.density.density.R


class FeedbackFirstFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var radioGroup : RadioGroup
    private lateinit var buttonNext : Button
    private lateinit var buttonClose : ImageView
    private var selectedAnswer : Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = parentFragment?.view?.findViewById(R.id.viewPager) ?: context?.let { ViewPager2(it) }!!
        radioGroup = view.findViewById(R.id.answer_dialog_radio)
        buttonNext = view.findViewById(R.id.button_next)
        buttonClose = view.findViewById(R.id.button_close)

        setRadioGroup()
        setButtonNext()
        setButtonClose()
    }

    private fun setRadioGroup(){
        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i==R.id.answer_yes){
                this.selectedAnswer = 1
                buttonNext.isEnabled = true
                buttonNext.setBackgroundColor(resources.getColor(R.color.feedback_button))
            } else if (i==R.id.answer_no) {
                this.selectedAnswer = 0
                buttonNext.isEnabled = true
                buttonNext.setBackgroundColor(resources.getColor(R.color.feedback_button))
            }
        }
    }

    private fun setButtonNext(){
        buttonNext.isEnabled = false
        buttonNext.setBackgroundColor(resources.getColor(R.color.dark_grey))

        buttonNext.setOnClickListener {
            if (this.selectedAnswer==1){
                (parentFragment as FeedbackDialogFragment).setFirstInput(this.selectedAnswer)
                viewPager.setCurrentItem(getItem(2), false)
            } else if (this.selectedAnswer==0) {
                (parentFragment as FeedbackDialogFragment).setFirstInput(this.selectedAnswer)
                viewPager.setCurrentItem(getItem(1), false)
            }
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
