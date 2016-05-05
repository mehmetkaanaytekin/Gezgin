package com.mirketech.gezgin.listeners;

import android.animation.Animator;
import android.view.View;

/**
 * Created by yasin.avci on 5.5.2016.
 */
public class ViewAnimatorListener implements Animator.AnimatorListener{

    boolean hide = false;
    boolean show = false;
    View view = null;

    public ViewAnimatorListener(boolean _hide , boolean _show , View _v){
        hide = _hide;
        show = _show;
        view = _v;
    }

    @Override
    public void onAnimationStart(Animator animation) {

        if(show){
            if(view.getVisibility() == View.VISIBLE){
                show = false;
                return;
            }


            view.setVisibility(View.VISIBLE);
            view.bringToFront();
            show = false;
        }
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if(view != null){
            if(hide){
                if(view.getVisibility() == View.GONE){
                    hide = false;
                    return;
                }


                view.setVisibility(View.GONE);
                hide = false;
            }

        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
