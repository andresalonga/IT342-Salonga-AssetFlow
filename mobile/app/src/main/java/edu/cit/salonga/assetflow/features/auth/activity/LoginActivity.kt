package edu.cit.salonga.assetflow.features.auth.activity

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
import android.widget.Toast
import edu.cit.salonga.assetflow.features.auth.model.LoginRequest
import edu.cit.salonga.assetflow.network.ApiClient
import edu.cit.salonga.assetflow.features.auth.utils.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        signUpLink = findViewById(R.id.signUpLink)

        loginButton.setOnClickListener { validateAndLogin() }
        
        signUpLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun validateAndLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Validation
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

        // All validations pass - call API
        loginWithBackend(email, password)
    }

    private fun loginWithBackend(email: String, password: String) {
        // Show loading state
        loginButton.isEnabled = false
        loginButton.text = "Signing in..."

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = ApiClient.authService.login(request)

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
                        
                        showSuccess("Login successful! Welcome ${authResponse.name}")
                        // Navigate to Dashboard after delay
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                        }, 1500)
                    } else {
                        showError(authResponse.message ?: "Login failed")
                        loginButton.isEnabled = true
                        loginButton.text = "Sign In"
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Login failed"
                    showError(errorMsg)
                    loginButton.isEnabled = true
                    loginButton.text = "Sign In"
                }
            } catch (e: Exception) {
                showError("Error: ${e.message ?: "Unknown error occurred"}")
                loginButton.isEnabled = true
                loginButton.text = "Sign In"
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
