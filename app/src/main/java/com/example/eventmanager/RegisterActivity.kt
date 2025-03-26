package com.example.eventmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eventmanager.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var databaseHelper: DatabaseHelper
    private val TAG = "RegistrationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            databaseHelper = DatabaseHelper(this)
            Log.d(TAG, "Database helper initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database helper", e)
            Toast.makeText(this, "Database initialization error", Toast.LENGTH_LONG).show()
        }

        binding.btnRegister.setOnClickListener {
            try {
                val name = binding.etFullName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                val confirmPassword = binding.etConfirmPassword.text.toString().trim()
                Log.d(TAG, "Registration attempt for email: $email")
                registerDatabase(name, email, password, confirmPassword)
            } catch (e: Exception) {
                Log.e(TAG, "Error during register button click", e)
                Toast.makeText(this, "Register button error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            try {
                startActivity(Intent(this, LoginActivity::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to login", e)
                Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerDatabase(name: String, email: String, password: String, confirmPassword: String) {
        try {
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return
            }

            if (!isValidPassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
                return
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return
            }

            val emailExists = databaseHelper.isEmailExists(email)
            Log.d(TAG, "Email exists check result: $emailExists")

            if (emailExists) {
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                return
            }

            val insertedRowId = databaseHelper.insertUser(name, email, password)
            Log.d(TAG, "User insertion result: $insertedRowId")

            if (insertedRowId != -1L) {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Registration Failed - Database Error", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in registerDatabase function", e)
            Toast.makeText(this, "Registration process error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
}
