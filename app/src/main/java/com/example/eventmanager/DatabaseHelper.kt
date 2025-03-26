package com.example.eventmanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.security.MessageDigest
import java.util.regex.Pattern

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DatabaseHelper"
        private const val DATABASE_NAME = "Events.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
    }

    // Existing code remains the same...

    private fun hashPassword(password: String): String {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val hashBytes = messageDigest.digest(password.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error hashing password", e)
            password // Fallback if hashing fails
        }
    }

    fun insertUser(name: String, email: String, password: String): Long {
        // Additional input validation
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            Log.d(TAG, "Invalid input for user registration")
            return -1
        }

        if (!isValidEmail(email)) {
            Log.d(TAG, "Invalid email format")
            return -1
        }

        if (password.length < 8) {
            Log.d(TAG, "Password too short")
            return -1
        }

        var db: SQLiteDatabase? = null
        try {
            val hashedPassword = hashPassword(password)
            val values = ContentValues().apply {
                put(COLUMN_NAME, name.trim())
                put(COLUMN_EMAIL, email.trim())
                put(COLUMN_PASSWORD, hashedPassword)
            }

            db = this.writableDatabase
            val result = db.insert(TABLE_NAME, null, values)
            Log.d(TAG, "User insertion result: $result")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user", e)
            return -1
        } finally {
            db?.close()
        }
    }

    // Enhanced email validation method
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return Pattern.compile(emailRegex)
            .matcher(email)
            .matches() &&
                email.length <= 254 &&
                email.split("@")[0].length <= 64
    }

    // Other methods remain the same...


    fun isEmailExists(email: String): Boolean {
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            db = this.readableDatabase
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ?"
            cursor = db.rawQuery(query, arrayOf(email))
            val exists = cursor.count > 0
            Log.d(TAG, "Email exists check: $email - $exists")
            return exists
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if email exists", e)
            return false
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun readUser(email: String, password: String): Boolean {
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            val hashedPassword = hashPassword(password)
            db = this.readableDatabase
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
            cursor = db.rawQuery(query, arrayOf(email, hashedPassword))
            val exists = cursor.count > 0
            Log.d(TAG, "User login attempt: $email - Success: $exists")
            return exists
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user credentials", e)
            return false
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    // For debugging: get all users
    fun getAllUsers(): List<String> {
        val userList = mutableListOf<String>()
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            db = this.readableDatabase
            cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                    val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                    userList.add("ID: $id, Name: $name, Email: $email")
                } while (cursor.moveToNext())
            }

            Log.d(TAG, "Total users in database: ${userList.size}")
            return userList
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all users", e)
            return emptyList()
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    // Add these methods to your existing DatabaseHelper.kt class

    fun getUserByEmail(email: String): Pair<String, String>? {
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            db = this.readableDatabase
            val query = "SELECT $COLUMN_NAME, $COLUMN_EMAIL FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ?"
            cursor = db.rawQuery(query, arrayOf(email))

            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(COLUMN_NAME)
                val emailIndex = cursor.getColumnIndex(COLUMN_EMAIL)

                if (nameIndex != -1 && emailIndex != -1) {
                    val name = cursor.getString(nameIndex)
                    val userEmail = cursor.getString(emailIndex)
                    Log.d(TAG, "User found: $name, $userEmail")
                    return Pair(name, userEmail)
                }
            }
            Log.d(TAG, "No user found with email: $email")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by email", e)
            return null
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun updateUserProfile(email: String, newName: String): Boolean {
        var db: SQLiteDatabase? = null
        try {
            db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_NAME, newName)
            }

            val rowsAffected = db.update(TABLE_NAME, values, "$COLUMN_EMAIL = ?", arrayOf(email))
            Log.d(TAG, "Profile update result: $rowsAffected rows affected")
            return rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            return false
        } finally {
            db?.close()
        }
    }

    fun updateUserPassword(email: String, oldPassword: String, newPassword: String): Boolean {
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            // First verify old password
            val hashedOldPassword = hashPassword(oldPassword)
            db = this.readableDatabase
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
            cursor = db.rawQuery(query, arrayOf(email, hashedOldPassword))

            if (cursor != null && cursor.count > 0) {
                // Old password is correct, update to new password
                cursor.close()
                db.close()

                db = this.writableDatabase
                val hashedNewPassword = hashPassword(newPassword)
                val values = ContentValues().apply {
                    put(COLUMN_PASSWORD, hashedNewPassword)
                }

                val rowsAffected = db.update(TABLE_NAME, values, "$COLUMN_EMAIL = ?", arrayOf(email))
                Log.d(TAG, "Password update result: $rowsAffected rows affected")
                return rowsAffected > 0
            } else {
                Log.d(TAG, "Password update failed: old password incorrect")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user password", e)
            return false
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    fun deleteUserAccount(email: String, password: String): Boolean {
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            // verify password
            val hashedPassword = hashPassword(password)
            db = this.readableDatabase
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
            cursor = db.rawQuery(query, arrayOf(email, hashedPassword))

            if (cursor != null && cursor.count > 0) {
                // Password is correct, delete account
                cursor.close()
                db.close()

                db = this.writableDatabase
                val rowsAffected = db.delete(TABLE_NAME, "$COLUMN_EMAIL = ?", arrayOf(email))
                Log.d(TAG, "Account deletion result: $rowsAffected rows affected")
                return rowsAffected > 0
            } else {
                Log.d(TAG, "Account deletion failed: password incorrect")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user account", e)
            return false
        } finally {
            cursor?.close()
            db?.close()
        }
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        TODO("Not yet implemented")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }
}





























































