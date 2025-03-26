package com.example.eventmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        try {
            databaseHelper = DatabaseHelper(this)
            Log.d(TAG, "Database helper initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database helper", e)
            Toast.makeText(this, "Database initialization error", Toast.LENGTH_LONG).show()
        }

        binding.btnLogin.setOnClickListener {
            try {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                Log.d(TAG, "Login attempt for email: $email")
                loginDatabase(email, password)
            } catch (e: Exception) {
                Log.e(TAG, "Error during login button click", e)
                Toast.makeText(this, "Login button error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            try {
                startActivity(Intent(this, RegisterActivity::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to registration", e)
                Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginDatabase(email: String, password: String) {
        try {
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            // Debug: print all users in database
            val allUsers = databaseHelper.getAllUsers()
            Log.d(TAG, "All users in database: $allUsers")

            val userExists = databaseHelper.readUser(email, password)
            Log.d(TAG, "User exists check result: $userExists")

            if (userExists) {
                // Get additional user info
                val userInfo = databaseHelper.getUserByEmail(email)

                // Save comprehensive user session data
                val sharedPreferences = getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("user_email", email)
                    putString("user_name", userInfo?.first ?: "User")
                    putString("last_login", System.currentTimeMillis().toString())
                    if (!sharedPreferences.contains("account_created_date")) {
                        // Set account creation date if first login
                        putString("account_created_date", java.text.SimpleDateFormat("yyyy-MM-dd",
                            java.util.Locale.getDefault()).format(java.util.Date()))
                    }
                    apply()
                }

                Toast.makeText(this, "Logged in Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login Failed - Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in loginDatabase function", e)
            Toast.makeText(this, "Login process error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}



