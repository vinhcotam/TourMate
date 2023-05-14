package com.example.tourmate.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.databinding.ActivityLoginBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult ->
        if (result.resultCode == RESULT_OK) {
            //success
            openMainActivity()
        } else {
            //failed
        }
    }
    private val listAuthProviders = listOf(
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.PhoneBuilder().build(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.buttonLogin.setOnClickListener {
            startSignIn()
        }
        if (auth.currentUser != null) {
            openMainActivity()
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startSignIn() {
        val signInIntent = AuthUI.getInstance().createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(listAuthProviders)
            .setLogo(R.drawable.welcome)
//            .setTheme(R.style.ThemeLoginUI)
            .build()
        signInLauncher.launch(signInIntent)
    }
}
