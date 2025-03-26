package com.example.eventmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.eventmanager.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseHelper: DatabaseHelper
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.etEmail.error = "Email cannot be empty"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.etPassword.error = "Password cannot be empty"
                    return@setOnClickListener
                }
                else -> loginDatabase(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginDatabase(email: String, password: String) {
        try {
            val userExists = databaseHelper.readUser(email, password)

            if (userExists) {
                val userInfo = databaseHelper.getUserByEmail(email)

                getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .apply {
                        putString("user_email", email)
                        putString("user_name", userInfo?.first ?: "User")
                        putString("last_login", System.currentTimeMillis().toString())
                        apply()
                    }

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}