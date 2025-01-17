package com.example.stayfit

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.stayfit.animations.ViewAnimations
import com.example.stayfit.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gradientAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Start background gradient animation
        val constraintLayout = binding.main
        gradientAnimation = constraintLayout.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(2000)
        gradientAnimation.setExitFadeDuration(4000)
        gradientAnimation.start()

        // Apply spring animation to logo and slogan
        ViewAnimations.applySpringAnimation(binding.logoImage)
        ViewAnimations.applySpringAnimation(binding.sloganText)

        // Apply pulse animation to login button
        ViewAnimations.applyPulseAnimation(binding.logbutton)

        binding.logbutton.setOnClickListener {
            ViewAnimations.applyButtonClickAnimation(it)
            val email = binding.editTextlogEmailAddress.text.toString()
            val pass = binding.editTextlogPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        ViewAnimations.applyShakeAnimation(binding.editTextlogEmailAddress.rootView)
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                ViewAnimations.applyShakeAnimation(binding.editTextlogEmailAddress.rootView)
                Toast.makeText(this, "Empty Fields Are Not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.logsignup.setOnClickListener {
            ViewAnimations.applyButtonClickAnimation(it)
            val intent = Intent(this, registration::class.java)
            startActivity(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
