package org.cornelldti.density.density.facilities.mainfeedback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.cornelldti.density.density.R
import org.cornelldti.density.density.facilities.FacilitiesActivity
import org.cornelldti.density.density.network.API


class MainFeedbackThanksFragment : Fragment() {

    private lateinit var api: API
    private lateinit var buttonClose: ImageView
    private lateinit var buttonHome: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_feedback_thanks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonClose = view.findViewById(R.id.button_close)
        buttonHome = view.findViewById(R.id.button_home)
        api = API(context = (activity as FacilitiesActivity))
        val user = FirebaseAuth.getInstance().currentUser

        setButtonClose(buttonClose)
        setButtonClose(buttonHome)
        requestToken(user)
    }

    private fun setButtonClose(v: View) {
        v.setOnClickListener {
            val intent = Intent("MAIN_FEEDBACK_BROADCAST_ACTION")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }

    }

    /**
     * This function re-requests for the user authentication token since the idToken field needs
     * to be re-initialized due to this fragment not extending BaseActivity.
     */
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

    /**
     * This function submits the main page feedback.
     */
    private fun submitFeedback() {
        val recommendInput = (parentFragment as MainFeedbackDialogFragment).recommendInput
        val featuresInput = (parentFragment as MainFeedbackDialogFragment).featuresInput
        val overallInput = (parentFragment as MainFeedbackDialogFragment).overallInput
        val commentInput = (parentFragment as MainFeedbackDialogFragment).commentInput

        api.addFacilitiesFeedback(
                recommendInput,
                featuresInput,
                overallInput,
                commentInput
        )

    }
}
