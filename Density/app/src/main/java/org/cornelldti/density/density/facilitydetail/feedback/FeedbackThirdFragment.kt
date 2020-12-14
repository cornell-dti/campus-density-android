package org.cornelldti.density.density.facilitydetail.feedback

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import org.cornelldti.density.density.R


class FeedbackThirdFragment : Fragment() {

    private lateinit var viewPager : ViewPager2
    private lateinit var editText : EditText
    private lateinit var buttonPrev : Button
    private lateinit var buttonSubmit : Button
    private lateinit var buttonClose : ImageView
    private var selectedAnswer = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = parentFragment?.view?.findViewById(R.id.viewPager) ?: context?.let { ViewPager2(it) }!!
        editText = view.findViewById(R.id.answer_dialog_edittext)
        buttonPrev = view.findViewById(R.id.button_previous)
        buttonSubmit = view.findViewById(R.id.button_submit)
        buttonClose = view.findViewById(R.id.button_close)

        setEditText()
        setButtonPrev()
        setButtonSubmit()
        setButtonClose()
    }

    private fun setEditText(){
        editText.addTextChangedListener( object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                selectedAnswer = p0.toString()
                (parentFragment as FeedbackDialogFragment).setThirdInput(selectedAnswer)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun setButtonPrev(){
        buttonPrev.setOnClickListener{
            if((parentFragment as FeedbackDialogFragment).getFirstInput()==1){
                viewPager.setCurrentItem(getItem(-2),false)
            } else {
                viewPager.setCurrentItem(getItem(-1),false)
            }
        }
    }

    private fun setButtonSubmit(){
        buttonSubmit.setOnClickListener{
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
