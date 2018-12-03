package org.cornelldti.density.density;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class LockableAppBarLayoutBehavior extends AppBarLayout.Behavior {

    private boolean locked;

    public LockableAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        return !locked;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        if(!locked){
            return super.onTouchEvent(parent, child, ev);
        }else{
            return false;
        }
    }

    public void lockScroll(){
        this.locked = true;
    }

    public void unlockScroll(){
        this.locked = false;
    }

    public boolean scrollIsLocked(){
        return locked;
    }
}