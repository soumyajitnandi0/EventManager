package com.example.eventmanager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton


class Profile : Fragment() {
    private val TAG = "ProfileFragment"
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    // UI elements
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var editProfileButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        try {
            // Initialize database helper
            databaseHelper = DatabaseHelper(requireContext())

            // Initialize shared preferences for user session
            sharedPreferences = requireActivity().getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)

            // Initialize UI elements
            initializeViews(view)

            // Get current logged in user's email
            val userEmail = sharedPreferences.getString("user_email", "") ?: ""
            Log.d(TAG, "Current user email: $userEmail")

            // Load user data and set up click listeners
            loadUserData(userEmail)
            setupClickListeners()

        } catch (e: Exception) {
            Log.e(TAG, "Error in ProfileFragment", e)
            Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Reload user data on resume to reflect any changes made in EditProfileActivity
        val userEmail = sharedPreferences.getString("user_email", "") ?: ""
        if (userEmail.isNotEmpty()) {
            loadUserData(userEmail)
        }
    }

    private fun initializeViews(view: View) {
        usernameTextView = view.findViewById(R.id.tv_username)
        emailTextView = view.findViewById(R.id.tv_user_email)
        logoutButton = view.findViewById(R.id.btn_logout)
        editProfileButton = view.findViewById(R.id.btn_edit_profile)
    }

    private fun loadUserData(email: String) {
        try {
            if (email.isNotEmpty()) {
                // Get user info from database
                val userInfo = databaseHelper.getUserByEmail(email)

                if (userInfo != null) {
                    usernameTextView.text = userInfo.first
                    emailTextView.text = userInfo.second
                    // Store username in shared preferences for other fragments to use
                    with(sharedPreferences.edit()) {
                        putString("user_name", userInfo.first)
                        apply()
                    }
                    Log.d(TAG, "User info loaded: ${userInfo.first}, ${userInfo.second}")
                } else {
                    handleNoUserData("No user found with email: $email")
                }
            } else {
                handleNoUserData("No email saved in session")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data", e)
            handleNoUserData("Error: ${e.message}")
        }
    }

    private fun handleNoUserData(logMessage: String) {
        Log.d(TAG, logMessage)
        usernameTextView.text = "Guest User"
        emailTextView.text = "Please log in"


        AlertDialog.Builder(requireContext())
            .setTitle("Session Expired")
            .setMessage("Your session has expired or you're not logged in. Please log in again.")
            .setPositiveButton("Login") { _, _ ->
                navigateToLogin()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            confirmLogout()
        }

        editProfileButton.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }

    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        try {
            // Clear user session
            with(sharedPreferences.edit()) {
                clear()
                apply()
            }

            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            Log.d(TAG, "User logged out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            Toast.makeText(requireContext(), "Logout error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }



    companion object {
        @JvmStatic
        fun newInstance() = Profile()
    }
}
