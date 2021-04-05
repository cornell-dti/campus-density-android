package org.cornelldti.density.density.facilities.mainfeedback

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


class MainFeedbackDialogFragment : DialogFragment() {

    private lateinit var viewPager: ViewPager2
    private val MAIN_FEEDBACK_BROADCAST_ACTION = "MAIN_FEEDBACK_BROADCAST_ACTION"
    var recommendInput = 0
    var featuresInput: List<Int> = ArrayList()
    var overallInput = 0
    var commentInput = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_feedback_dialog, container, false)
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
     * This function sets up the main feedback viewPager by appending fragments to the adapter.
     */
    private fun setUpViewPager() {
        val adapter = MainFeedbackStateAdapter(childFragmentManager, lifecycle)

        adapter.addFragment(MainFeedbackRecommendFragment())
        adapter.addFragment(MainFeedbackFeaturesFragment())
        adapter.addFragment(MainFeedbackOverallFragment())
        adapter.addFragment(MainFeedbackCommentFragment())
        adapter.addFragment(MainFeedbackThanksFragment())

        viewPager.adapter = adapter
    }

    /**
     * This function receives the broadcast call to close the dialog.
     */
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action != null && intent.action == MAIN_FEEDBACK_BROADCAST_ACTION) {
                dialog!!.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            LocalBroadcastManager.getInstance(
                    it).registerReceiver(broadcastReceiver, IntentFilter(MAIN_FEEDBACK_BROADCAST_ACTION))
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