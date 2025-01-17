package com.example.stayfit.animations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

object ViewAnimations {
    fun applySpringAnimation(view: View) {
        val springForce = SpringForce().apply {
            finalPosition = 1f
            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            stiffness = SpringForce.STIFFNESS_MEDIUM
        }

        val scaleXAnim = SpringAnimation(view, DynamicAnimation.SCALE_X, 0f).apply {
            spring = springForce
        }
        val scaleYAnim = SpringAnimation(view, DynamicAnimation.SCALE_Y, 0f).apply {
            spring = springForce
        }

        view.scaleX = 0f
        view.scaleY = 0f
        view.alpha = 0f

        view.animate().alpha(1f).setDuration(300).start()
        scaleXAnim.start()
        scaleYAnim.start()
    }

    fun applyShakeAnimation(view: View) {
        val shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shake.duration = 1000
        shake.interpolator = AccelerateDecelerateInterpolator()
        shake.start()
    }

    fun applyButtonClickAnimation(view: View) {
        val scaleDown = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f)
        val scaleUp = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f)
        
        val shrinkAnim = ObjectAnimator.ofPropertyValuesHolder(view, scaleDown, scaleUp).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
        }

        val returnScaleDown = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)
        val returnScaleUp = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)

        val returnAnim = ObjectAnimator.ofPropertyValuesHolder(view, returnScaleDown, returnScaleUp).apply {
            duration = 100
            interpolator = OvershootInterpolator()
        }

        AnimatorSet().apply {
            playSequentially(shrinkAnim, returnAnim)
            start()
        }
    }

    fun applySlideInAnimation(view: View, fromRight: Boolean = true) {
        view.translationX = if (fromRight) view.width.toFloat() else -view.width.toFloat()
        view.alpha = 0f
        
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun applyPulseAnimation(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
        
        ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}
