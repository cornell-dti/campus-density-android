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
    private val BROADCAST_ACTION = "BROADCAST_ACTION"

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

    private fun setUpViewPager(){
        val adapter = FeedbackStateAdapter(childFragmentManager, lifecycle)

        adapter.addFragment(FeedbackFirstFragment())
        adapter.addFragment(FeedbackSecondFragment())
        adapter.addFragment(FeedbackThirdFragment())
        adapter.addFragment(FeedbackFourthFragment())

        viewPager.adapter = adapter
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action != null && intent.action == BROADCAST_ACTION) {
                dialog!!.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            LocalBroadcastManager.getInstance(
                    it).registerReceiver(broadcastReceiver, IntentFilter(BROADCAST_ACTION))
        }
    }

    override fun onPause() {
        super.onPause()
        context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(broadcastReceiver) }
    }

}