package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.tvSignupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.etEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.etEmail.error = null
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.etPassword.error = null
        }

        return isValid
    }

    private fun authenticateUser(email: String, password: String): Boolean {
        val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        val storedEmail = sharedPref.getString("email", null)
        val storedPassword = sharedPref.getString("password", null)

        return email == storedEmail && password == storedPassword
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (validateInput(email, password)) {
            if (authenticateUser(email, password)) {
                // Save session data
                val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("loggedInUser", email)
                    putString("userEmail", email) // Save email to session
                    apply()
                }

                // Navigate to the main page or dashboard
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                binding.tvError.text = "Invalid email or password"
                binding.tvError.visibility = View.VISIBLE
            }
        } else {
            binding.tvError.text = "Login failed. Please check your input."
            binding.tvError.visibility = View.VISIBLE
        }
    }
}