package org.cornelldti.density.density.facilitydetail.feedback

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.cornelldti.density.density.R
import org.cornelldti.density.density.facilitydetail.FacilityInfoPage
import org.cornelldti.density.density.network.API


class FeedbackThanksFragment : Fragment() {

    private lateinit var api: API
    private lateinit var buttonClose: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_thanks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonClose = view.findViewById(R.id.button_close)

        setButtonClose()
        submitFeedback()
    }

    private fun setButtonClose() {
        buttonClose.setOnClickListener {
            val intent = Intent("BROADCAST_ACTION")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }
    }

    private fun submitFeedback() {
        val campusLocation = (activity as FacilityInfoPage).getCampusLocation()
        val predictedDensity = (activity as FacilityInfoPage).getPredictedDensity()
        val accuracyInput = (parentFragment as FeedbackDialogFragment).getAccuracyInput()
        val observedDensityInput = (parentFragment as FeedbackDialogFragment).getObservedDensityInput()
        val commentInput = (parentFragment as FeedbackDialogFragment).getCommentInput()

//        if(accuracyInput == 1){
//            api.addFacilityInfoFeedback(
//                    campusLocation,
//                    predictedDensity,
//                    predictedDensity,
//                    commentInput
//            )
//        } else {
//            api.addFacilityInfoFeedback(
//                    campusLocation,
//                    predictedDensity,
//                    observedDensityInput,
//                    commentInput
//            )
//        }
    }
}
