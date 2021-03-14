package org.cornelldti.density.density.facilitydetail.feedback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
        api = API(context = (activity as FacilityInfoPage))
        val user = FirebaseAuth.getInstance().currentUser

        setButtonClose()
        requestToken(user)
    }

    private fun setButtonClose() {
        buttonClose.setOnClickListener {
            val intent = Intent("BROADCAST_ACTION")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }
    }

    private fun requestToken(user: FirebaseUser?) {
        user?.getIdToken(true)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.token?.let { api.setIdToken(it) }
                        submitFeedback()
                    } else {
                        Log.d("AUTH ERROR", "Error obtaining Firebase Auth ID token")
                    }
                }
    }

    private fun submitFeedback() {
        val campusLocation = (activity as FacilityInfoPage).getCampusLocation()
        val accuracyInput = (parentFragment as FeedbackDialogFragment).accuracyInput
        val predictedWaitTime = (parentFragment as FeedbackDialogFragment).predictedWaitTime
        val observedWaitTime = (parentFragment as FeedbackDialogFragment).observedWaitTime
        val commentInput = (parentFragment as FeedbackDialogFragment).commentInput

        if (accuracyInput == 1) {
            api.addFacilityInfoFeedback(
                    campusLocation,
                    predictedWaitTime,
                    predictedWaitTime,
                    commentInput
            )
        } else {
            api.addFacilityInfoFeedback(
                    campusLocation,
                    predictedWaitTime,
                    observedWaitTime,
                    commentInput
            )
        }
    }
}
