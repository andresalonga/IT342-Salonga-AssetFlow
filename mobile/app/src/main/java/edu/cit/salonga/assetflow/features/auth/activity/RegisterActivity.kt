package edu.cit.salonga.assetflow.features.auth.activity

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.assets.activity.DashboardActivity
import edu.cit.salonga.assetflow.features.auth.model.RegisterRequest
import edu.cit.salonga.assetflow.network.ApiClient
import edu.cit.salonga.assetflow.features.auth.utils.TokenManager
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import edu.cit.salonga.assetflow.features.auth.model.GoogleAuthRequest

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AssetFlowGoogleAuth"
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var googleRegisterButton: MaterialButton
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)

        registerButton.setOnClickListener { validateAndRegister() }
        googleRegisterButton = findViewById(R.id.googleRegisterButton)
        googleRegisterButton.setOnClickListener { startGoogleSignIn() }
        
        loginLink.setOnClickListener {
            loginLink.paintFlags = loginLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    Log.d(TAG, "Google sign-in succeeded for ${account.email}")
                    registerWithGoogle(idToken)
                } else {
                    Log.e(TAG, "Google sign-in completed but returned a null idToken")
                    showError("Google sign-in failed: missing token")
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign-in failed with status ${e.statusCode}", e)
                if (e.statusCode == 10) {
                    showError(
                        "Google sign-in configuration error. Use a Web OAuth client ID in requestIdToken(), not the Android client ID. Keep the Android client registered with your package name and SHA-1 in Google Console."
                    )
                } else {
                    showError("Google sign-in failed: ${e.statusCode}")
                }
            }
        }
    }

    private fun registerWithGoogle(idToken: String) {
        // disable button while signing in
        googleRegisterButton.isEnabled = false
        googleRegisterButton.text = "Signing in..."

        lifecycleScope.launch {
            try {
                val request = GoogleAuthRequest(idToken)
                val response = ApiClient.authService.googleMobile(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    if (authResponse.success) {
                        if (authResponse.token != null) {
                            TokenManager.saveToken(authResponse.token)
                        }
                        if (authResponse.userId != null && authResponse.name != null &&
                            authResponse.email != null && authResponse.role != null) {
                            TokenManager.saveUserInfo(
                                authResponse.userId,
                                authResponse.name,
                                authResponse.email,
                                authResponse.role
                            )
                        }

                        showSuccess("Account created via Google! Welcome ${authResponse.name}")
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                            finish()
                        }, 1200)
                    } else {
                        showError(authResponse.message ?: "Registration failed")
                        googleRegisterButton.isEnabled = true
                        googleRegisterButton.text = getString(R.string.google_signin)
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Registration failed"
                    showError(errorMsg)
                    googleRegisterButton.isEnabled = true
                    googleRegisterButton.text = getString(R.string.google_signin)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google backend sign-in failed", e)
                showError("Error: ${e.message ?: "Unknown error occurred"}")
                googleRegisterButton.isEnabled = true
                googleRegisterButton.text = getString(R.string.google_signin)
            }
        }
    }

    private fun validateAndRegister() {
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        // Validation
        if (TextUtils.isEmpty(firstName)) {
            showError("First name is required")
            return
        }

        if (TextUtils.isEmpty(lastName)) {
            showError("Last name is required")
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
        val fullName = "$firstName $lastName".trim()
        registerWithBackend(fullName, email, password)
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
                        firstNameInput.text.clear()
                        lastNameInput.text.clear()
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
                Log.e(TAG, "Email/password registration failed", e)
                showError("Error: ${e.message ?: "Unknown error occurred"}")
                registerButton.isEnabled = true
                registerButton.text = "Register"
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
