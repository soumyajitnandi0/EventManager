package com.example.eventmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eventmanager.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var userEmail: String
    private lateinit var userName: String
    private val TAG = "EditProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // Initialize database and get user data
            databaseHelper = DatabaseHelper(this)
            val sharedPreferences = getSharedPreferences("PetukPrefs", Context.MODE_PRIVATE)
            userEmail = sharedPreferences.getString("user_email", "") ?: ""
            userName = sharedPreferences.getString("user_name", "") ?: ""

            if (userEmail.isEmpty()) {
                Toast.makeText(this, "No user logged in. Please login again.", Toast.LENGTH_LONG).show()
                navigateToLogin()
                return
            }

            // Initialize UI with current values
            binding.tvCurrentEmail.text = userEmail
            binding.etCurrentName.setText(userName)

            setupClickListeners()
            setupTabSelection()

        } catch (e: Exception) {
            Log.e(TAG, "Error in EditProfileActivity initialization", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTabSelection() {
        // Set up tab selection logic
        binding.btnChangeNameTab.setOnClickListener {
            showTab(TAB_CHANGE_NAME)
        }

        binding.btnChangePasswordTab.setOnClickListener {
            showTab(TAB_CHANGE_PASSWORD)
        }

        binding.btnDeleteAccountTab.setOnClickListener {
            showTab(TAB_DELETE_ACCOUNT)
        }

        // Show default tab (name change)
        showTab(TAB_CHANGE_NAME)
    }

    private fun showTab(tab: Int) {
        // Reset all tab selections
        binding.btnChangeNameTab.setBackgroundResource(R.drawable.tab_unselected_bg)
        binding.btnChangePasswordTab.setBackgroundResource(R.drawable.tab_unselected_bg)
        binding.btnDeleteAccountTab.setBackgroundResource(R.drawable.tab_unselected_bg)

        // Hide all layouts
        binding.layoutChangeName.visibility = View.GONE
        binding.layoutChangePassword.visibility = View.GONE
        binding.layoutDeleteAccount.visibility = View.GONE

        // Show selected tab and layout
        when (tab) {
            TAB_CHANGE_NAME -> {
                binding.btnChangeNameTab.setBackgroundResource(R.drawable.tab_selected_bg)
                binding.layoutChangeName.visibility = View.VISIBLE
            }
            TAB_CHANGE_PASSWORD -> {
                binding.btnChangePasswordTab.setBackgroundResource(R.drawable.tab_selected_bg)
                binding.layoutChangePassword.visibility = View.VISIBLE
            }
            TAB_DELETE_ACCOUNT -> {
                binding.btnDeleteAccountTab.setBackgroundResource(R.drawable.tab_selected_bg)
                binding.layoutDeleteAccount.visibility = View.VISIBLE
            }
        }
    }

    private fun setupClickListeners() {
        // Name change button
        binding.btnSaveName.setOnClickListener {
            val newName = binding.etCurrentName.text.toString().trim()
            updateName(newName)
        }

        // Password change button
        binding.btnSavePassword.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmNewPassword.text.toString().trim()
            updatePassword(currentPassword, newPassword, confirmPassword)
        }

        // Delete account button
        binding.btnConfirmDelete.setOnClickListener {
            val password = binding.etDeletePassword.text.toString().trim()
            confirmDeleteAccount(password)
        }

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateName(newName: String) {
        try {
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return
            }

            if (newName == userName) {
                Toast.makeText(this, "New name is same as current name", Toast.LENGTH_SHORT).show()
                return
            }

            val success = databaseHelper.updateUserProfile(userEmail, newName)
            if (success) {
                // Update shared preferences
                val sharedPreferences = getSharedPreferences("PetukPrefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("user_name", newName)
                    apply()
                }

                Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()
                userName = newName
            } else {
                Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating name", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        try {
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return
            }

            if (currentPassword == newPassword) {
                Toast.makeText(this, "New password should be different from current password", Toast.LENGTH_SHORT).show()
                return
            }

            val success = databaseHelper.updateUserPassword(userEmail, currentPassword, newPassword)
            if (success) {
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                // Clear the password fields
                binding.etCurrentPassword.text.clear()
                binding.etNewPassword.text.clear()
                binding.etConfirmNewPassword.text.clear()
            } else {
                Toast.makeText(this, "Failed to update password. Check your current password", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteAccount(password: String) {
        try {
            if (password.isEmpty()) {
                Toast.makeText(this, "Password is required to delete account", Toast.LENGTH_SHORT).show()
                return
            }

            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you absolutely sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes, Delete") { _, _ ->
                    deleteAccount(password)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing delete confirmation", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAccount(password: String) {
        try {
            val success = databaseHelper.deleteUserAccount(userEmail, password)
            if (success) {
                // Clear shared preferences
                val sharedPreferences = getSharedPreferences("PetukPrefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    clear()
                    apply()
                }

                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show()
                navigateToLogin()
            } else {
                Toast.makeText(this, "Failed to delete account. Check your password", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAB_CHANGE_NAME = 0
        private const val TAB_CHANGE_PASSWORD = 1
        private const val TAB_DELETE_ACCOUNT = 2
    }
}