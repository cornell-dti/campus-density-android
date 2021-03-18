package org.cornelldti.density.density.facilitydetail.feedback

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
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


class FeedbackObservedDensityFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var radioGroup: RadioGroup
    private lateinit var buttonPrev: Button
    private lateinit var buttonNext: Button
    private lateinit var buttonClose: ImageView
    private lateinit var textQuestion: TextView
    private var selectedAnswer = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_observed_density, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = parentFragment?.view?.findViewById(R.id.viewPager)
                ?: context?.let { ViewPager2(it) }!!
        radioGroup = view.findViewById(R.id.answer_dialog_radio)
        buttonPrev = view.findViewById(R.id.button_previous)
        buttonNext = view.findViewById(R.id.button_next)
        buttonClose = view.findViewById(R.id.button_close)
        textQuestion = view.findViewById(R.id.question_dialog)

        setRadioGroup()
        setButtonPrev()
        setButtonNext()
        setButtonClose()
        setTextQuestion()
    }

    private fun setRadioGroup() {
        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i != null) {
                buttonNext.isEnabled = true
                buttonNext.setBackgroundColor(resources.getColor(R.color.feedback_button))
                setBars(i)
                when (i) {
                    null -> selectedAnswer = -1
                    R.id.first_radio -> selectedAnswer = 0
                    R.id.second_radio -> selectedAnswer = 1
                    R.id.third_radio -> selectedAnswer = 2
                    R.id.fourth_radio -> selectedAnswer = 3
                }
            }
        }
    }

    private fun setButtonPrev() {
        buttonPrev.setOnClickListener {
            viewPager.setCurrentItem((parentFragment as FeedbackDialogFragment).getPagerItem(-1), false)
        }
    }

    private fun setButtonNext() {
        buttonNext.isEnabled = false
        buttonNext.setBackgroundColor(resources.getColor(R.color.dark_grey))

        buttonNext.setOnClickListener {
            (parentFragment as FeedbackDialogFragment).observedDensityInput = selectedAnswer
            viewPager.setCurrentItem((parentFragment as FeedbackDialogFragment).getPagerItem(1), false)
        }
    }

    private fun setButtonClose() {
        buttonClose.setOnClickListener {
            val intent = Intent("BROADCAST_ACTION")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }
    }

    private fun setTextQuestion() {
        textQuestion.setText("How crowded do you think " + (activity as FacilityInfoPage).getFacilityName() + " is right now?")
    }

    private fun setBars(rating: Int) {
        when (rating) {
            null -> setBarColors(R.color.filler_boxes, R.color.filler_boxes, R.color.filler_boxes, R.color.filler_boxes)
            R.id.first_radio -> setBarColors(R.color.very_empty, R.color.filler_boxes, R.color.filler_boxes, R.color.filler_boxes)
            R.id.second_radio -> setBarColors(R.color.pretty_empty, R.color.pretty_empty, R.color.filler_boxes, R.color.filler_boxes)
            R.id.third_radio -> setBarColors(R.color.pretty_crowded, R.color.pretty_crowded, R.color.pretty_crowded, R.color.filler_boxes)
            R.id.fourth_radio -> setBarColors(R.color.very_crowded, R.color.very_crowded, R.color.very_crowded, R.color.very_crowded)
        }
    }

    private fun setBarColors(first_color: Int, second_color: Int, third_color: Int, fourth_color: Int) {
        view?.findViewById<ImageView>(R.id.first_bar)?.setColorFilter(ContextCompat.getColor(context!!, first_color),
                PorterDuff.Mode.MULTIPLY)
        view?.findViewById<ImageView>(R.id.second_bar)?.setColorFilter(ContextCompat.getColor(context!!, second_color),
                PorterDuff.Mode.MULTIPLY)
        view?.findViewById<ImageView>(R.id.third_bar)?.setColorFilter(ContextCompat.getColor(context!!, third_color),
                PorterDuff.Mode.MULTIPLY)
        view?.findViewById<ImageView>(R.id.fourth_bar)?.setColorFilter(ContextCompat.getColor(context!!, fourth_color),
                PorterDuff.Mode.MULTIPLY)
    }

}
