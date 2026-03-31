package edu.cit.salonga.assetflow

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import edu.cit.salonga.assetflow.models.RegisterRequest
import edu.cit.salonga.assetflow.network.ApiClient
import edu.cit.salonga.assetflow.utils.TokenManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var registerContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)
        registerContainer = findViewById(R.id.registerContainer)

        registerButton.setOnClickListener { validateAndRegister() }
        
        loginLink.setOnClickListener {
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateAndRegister() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        // Validation
        if (TextUtils.isEmpty(name)) {
            showError("Name is required")
            return
        }

        if (TextUtils.isEmpty(email)) {
            showError("Email is required")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email format")
            return
        }

        if (TextUtils.isEmpty(password)) {
            showError("Password is required")
            return
        }

        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match")
            return
        }

        // All validations pass - call API
        registerWithBackend(name, email, password)
    }

    private fun registerWithBackend(name: String, email: String, password: String) {
        // Show loading state
        registerButton.isEnabled = false
        registerButton.text = "Registering..."

        lifecycleScope.launch {
            try {
                val request = RegisterRequest(name, email, password)
                val response = ApiClient.authService.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    if (authResponse.success) {
                        // Save token and user info
                        if (authResponse.token != null) {
                            TokenManager.saveToken(authResponse.token!!)
                        }
                        if (authResponse.userId != null && authResponse.name != null && 
                            authResponse.email != null && authResponse.role != null) {
                            TokenManager.saveUserInfo(
                                authResponse.userId!!,
                                authResponse.name!!,
                                authResponse.email!!,
                                authResponse.role!!
                            )
                        }
                        
                        showSuccess("Account created successfully!")
                        // Clear fields
                        nameInput.text.clear()
                        emailInput.text.clear()
                        passwordInput.text.clear()
                        confirmPasswordInput.text.clear()
                        // Navigate to Dashboard after delay
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                            finish()
                        }, 1500)
                    } else {
                        showError(authResponse.message ?: "Registration failed")
                        registerButton.isEnabled = true
                        registerButton.text = "Register"
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Registration failed"
                    showError(errorMsg)
                    registerButton.isEnabled = true
                    registerButton.text = "Register"
                }
            } catch (e: Exception) {
                showError("Error: ${e.message ?: "Unknown error occurred"}")
                registerButton.isEnabled = true
                registerButton.text = "Register"
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(registerContainer, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, null))
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(registerContainer, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(android.R.color.holo_green_dark, null))
            .show()
    }
}
