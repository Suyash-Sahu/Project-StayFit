package com.example.stayfit

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stayfit.animations.ViewAnimations
import com.example.stayfit.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth

class registration : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gradientAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Start background gradient animation
        val constraintLayout = binding.main
        gradientAnimation = constraintLayout.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(2000)
        gradientAnimation.setExitFadeDuration(4000)
        gradientAnimation.start()

        // Apply spring animation to profile image and title
        ViewAnimations.applySpringAnimation(binding.userimg)
        ViewAnimations.applySpringAnimation(binding.titleText)

        // Apply pulse animation to register button
        ViewAnimations.applyPulseAnimation(binding.buttonsg)

        binding.userimg.setOnClickListener {
            ViewAnimations.applyButtonClickAnimation(it)
            // Add image picker logic here
        }

        binding.buttonsg.setOnClickListener {
            ViewAnimations.applyButtonClickAnimation(it)
            val email = binding.sgemail.text.toString()
            val pass = binding.sgpassword.text.toString()
            val confirmPass = binding.sgrepassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            ViewAnimations.applyShakeAnimation(binding.sgemail.rootView)
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    ViewAnimations.applyShakeAnimation(binding.sgpassword.rootView)
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                ViewAnimations.applyShakeAnimation(binding.sgemail.rootView)
                Toast.makeText(this, "Empty Fields Are Not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginbutsg.setOnClickListener {
            ViewAnimations.applyButtonClickAnimation(it)
            val intent = Intent(this, login::class.java)
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
