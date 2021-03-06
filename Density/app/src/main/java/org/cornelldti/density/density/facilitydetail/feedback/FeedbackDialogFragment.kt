package org.cornelldti.density.density.facilitydetail.feedback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import org.cornelldti.density.density.R


class FeedbackDialogFragment : DialogFragment() {

    private lateinit var viewPager: ViewPager2
    private val FEEDBACK_BROADCAST_ACTION = "FEEDBACK_BROADCAST_ACTION"
    var predictedWaitTime = 0
    var accuracyInput = 0
    var observedDensityInput = 0
    var observedWaitTime = 0
    var commentInput = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = false
        setUpViewPager()

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
    }

    /**
     * This function sets up the feedback viewPager by appending fragments to the adapter.
     */
    private fun setUpViewPager() {
        val adapter = FeedbackStateAdapter(childFragmentManager, lifecycle)

        adapter.addFragment(FeedbackAccuracyFragment())
        adapter.addFragment(FeedbackObservedDensityFragment())
        adapter.addFragment(FeedbackWaitTimeFragment())
        adapter.addFragment(FeedbackCommentFragment())
        adapter.addFragment(FeedbackThanksFragment())

        viewPager.adapter = adapter
    }

    /**
     * This function receives the broadcast call to close the dialog.
     */
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action != null && intent.action == FEEDBACK_BROADCAST_ACTION) {
                dialog!!.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            LocalBroadcastManager.getInstance(
                    it).registerReceiver(broadcastReceiver, IntentFilter(FEEDBACK_BROADCAST_ACTION))
        }
    }

    override fun onPause() {
        super.onPause()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver) }
    }

    fun getPagerItem(i: Int): Int {
        return viewPager.currentItem + i
    }
}