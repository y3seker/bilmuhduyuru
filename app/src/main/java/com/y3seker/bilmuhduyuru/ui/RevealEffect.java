package com.y3seker.bilmuhduyuru.ui;


import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;


/**
 * Created by Yunus Emre Şeker on 11.03.2015.
 * -
 */

public class RevealEffect {

    public static String TAG = "REVEAL_EFFECT";

    public interface AnimationListener {
        void onAnimEnd();
    }

    public static void revealFrom(int x, int y, View target, int duration, final AnimationListener callback) {
        int radius = Math.max(target.getWidth(), target.getHeight());

        SupportAnimator animator2 = ViewAnimationUtils.createCircularReveal(target, x, y, 0, radius);

        if (duration > 0)
            animator2.setDuration(duration);

        animator2.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                if(callback != null)
                callback.onAnimEnd();
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        animator2.setInterpolator(new AccelerateDecelerateInterpolator());
        target.setVisibility(View.VISIBLE);
        animator2.start();
    }

    public static void revealTo(int x, int y, View target, int duration, final AnimationListener callback) {
        int radius = Math.max(target.getWidth(), target.getHeight());

        SupportAnimator animator2 = ViewAnimationUtils.createCircularReveal(target, x, y, radius, 0);

        if (duration > 0)
            animator2.setDuration(duration);

        animator2.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {

                if(callback != null)callback.onAnimEnd();
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        animator2.setInterpolator(new AccelerateDecelerateInterpolator());
        animator2.start();
    }

}
