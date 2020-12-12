package org.cornelldti.density.density.facilitydetail.feedback

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
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
    private var selectedAnswer : String = ""

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

        setRadioGroup(selectedAnswer)
        setButtonNext(selectedAnswer)
        setButtonClose()
    }

    private fun setRadioGroup(selectedAnswer: String){
        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            this.selectedAnswer = view?.findViewById<RadioButton>(i)?.text.toString()
            if (selectedAnswer=="Yes"){
                buttonNext.isEnabled = true
                buttonNext.setBackgroundColor(resources.getColor(R.color.feedback_button))
            } else if (selectedAnswer=="No") {
                buttonNext.isEnabled = true
                buttonNext.setBackgroundColor(resources.getColor(R.color.feedback_button))
            }
        }
    }

    private fun setButtonNext(selectedAnswer: String){
        buttonNext.isEnabled = false
        buttonNext.setBackgroundColor(resources.getColor(R.color.dark_grey))

        buttonNext.setOnClickListener {
            if (selectedAnswer=="Yes"){
                viewPager.setCurrentItem(getItem(2), false)
            } else if (selectedAnswer=="No") {
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
