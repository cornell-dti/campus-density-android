package org.cornelldti.density.density

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import com.google.android.material.appbar.AppBarLayout

import androidx.coordinatorlayout.widget.CoordinatorLayout

class LockableAppBarLayoutBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.Behavior(context, attrs) {

    private var locked: Boolean = false

    override fun onStartNestedScroll(
            parent: CoordinatorLayout,
            child: AppBarLayout,
            directTargetChild: View,
            target: View,
            nestedScrollAxes: Int,
            type: Int
    ): Boolean = !locked

    override fun onTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean =
            if (!locked) {
                super.onTouchEvent(parent, child, ev)
            } else {
                false
            }

    fun lockScroll() {
        this.locked = true
    }

    fun unlockScroll() {
        this.locked = false
    }

    fun scrollIsLocked(): Boolean = locked
}
