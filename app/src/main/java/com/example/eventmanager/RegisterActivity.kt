package com.example.eventmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

        databaseHelper = DatabaseHelper(this)

        binding.btnRegister.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            when {
                name.isEmpty() -> {
                    binding.etFullName.error = "Name cannot be empty"
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    binding.etEmail.error = "Email cannot be empty"
                    return@setOnClickListener
                }
                !isValidEmail(email) -> {
                    binding.etEmail.error = "Invalid email format"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.etPassword.error = "Password cannot be empty"
                    return@setOnClickListener
                }
                password.length < 8 -> {
                    binding.etPassword.error = "Password must be at least 8 characters"
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    binding.etConfirmPassword.error = "Passwords do not match"
                    return@setOnClickListener
                }
                else -> registerDatabase(name, email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerDatabase(name: String, email: String, password: String) {
        if (databaseHelper.isEmailExists(email)) {
            Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
            return
        }

        val insertedRowId = databaseHelper.insertUser(name, email, password)

        if (insertedRowId != -1L) {
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex()) &&
                email.length <= 254 &&
                email.split("@")[0].length <= 64
    }
}