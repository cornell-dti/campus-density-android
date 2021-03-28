package org.cornelldti.density.density.facilities.mainfeedback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import org.cornelldti.density.density.R

class MainFeedbackFeaturesFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonPrev: Button
    private lateinit var buttonNext: Button
    private lateinit var buttonClose: ImageView
    private lateinit var checkBoxPopular: CheckBox
    private lateinit var checkBoxAvailable: CheckBox
    private lateinit var checkBoxDining: CheckBox
    private lateinit var checkBoxMenu: CheckBox
    var checkBoxList: List<CheckBox> = ArrayList()
    var selectedAnswer: MutableList<Int> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_feedback_features, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = parentFragment?.view?.findViewById(R.id.viewPager)
                ?: context?.let { ViewPager2(it) }!!
        buttonPrev = view.findViewById(R.id.button_previous)
        buttonNext = view.findViewById(R.id.button_next)
        buttonClose = view.findViewById(R.id.button_close)
        checkBoxPopular = view.findViewById(R.id.popular_times)
        checkBoxAvailable = view.findViewById(R.id.availability_breakdown)
        checkBoxDining = view.findViewById(R.id.dining_area)
        checkBoxMenu = view.findViewById(R.id.menu)
        checkBoxList = listOf(checkBoxPopular, checkBoxAvailable, checkBoxDining, checkBoxMenu)

        for (i in 0..3) {
            checkBoxList[i].setOnCheckedChangeListener { checkBox, isChecked ->
                if(isChecked && !selectedAnswer.contains(i)) {
                    selectedAnswer.add(i)
                    selectedAnswer.sort()
                    Log.d("TAG", selectedAnswer.toString())
                } else if (!isChecked && selectedAnswer.contains(i)) {
                    selectedAnswer.remove(i)
                    selectedAnswer.sort()
                    Log.d("TAG", selectedAnswer.toString())
                }
            }
        }

        setButtonPrev()
        setButtonNext()
        setButtonClose()
    }

    private fun setButtonPrev() {
        buttonPrev.setOnClickListener {
            viewPager.setCurrentItem((parentFragment as MainFeedbackDialogFragment).getPagerItem(-1), false)
        }
    }

    private fun setButtonNext() {
        buttonNext.setOnClickListener {
            (parentFragment as MainFeedbackDialogFragment).featuresInput = selectedAnswer.toList()
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