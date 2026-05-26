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
import edu.cit.salonga.assetflow.features.auth.model.LoginRequest
import edu.cit.salonga.assetflow.network.ApiClient
import edu.cit.salonga.assetflow.features.auth.utils.TokenManager
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.content.pm.PackageManager
import java.security.MessageDigest
import java.util.Locale
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import edu.cit.salonga.assetflow.features.auth.model.GoogleAuthRequest

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AssetFlowGoogleAuth"
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpLink: TextView
    private lateinit var googleLoginButton: MaterialButton
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        signUpLink = findViewById(R.id.signUpLink)

        loginButton.setOnClickListener { validateAndLogin() }
        googleLoginButton = findViewById(R.id.googleLoginButton)
        googleLoginButton.setOnClickListener { startGoogleSignIn() }
        
        signUpLink.setOnClickListener {
            signUpLink.paintFlags = signUpLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        
        // Log the actual SHA-1 of the certificate used to sign this running APK
        getAppSignaturesSHA1()
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
                    loginWithGoogle(idToken)
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

    private fun loginWithGoogle(idToken: String) {
        // disable button while signing in
        googleLoginButton.isEnabled = false
        googleLoginButton.text = "Signing in..."

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

                        showSuccess("Login successful! Welcome ${authResponse.name}")
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                        }, 1200)
                    } else {
                        showError(authResponse.message ?: "Login failed")
                        googleLoginButton.isEnabled = true
                        googleLoginButton.text = getString(R.string.google_signin)
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Login failed"
                    showError(errorMsg)
                    googleLoginButton.isEnabled = true
                    googleLoginButton.text = getString(R.string.google_signin)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google backend sign-in failed", e)
                showError("Error: ${e.message ?: "Unknown error occurred"}")
                googleLoginButton.isEnabled = true
                googleLoginButton.text = getString(R.string.google_signin)
            }
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
                Log.e(TAG, "Email/password login failed", e)
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

    private fun getAppSignaturesSHA1() {
        try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                packageInfo.signatures
            }

            if (signatures != null) {
                for (signature in signatures) {
                    val md = MessageDigest.getInstance("SHA-1")
                    val publicKey = md.digest(signature.toByteArray())
                    val hexString = StringBuilder()
                    for (aPublicKey in publicKey) {
                        val appendString = Integer.toHexString(0xFF and aPublicKey.toInt())
                            .uppercase(Locale.US)
                        if (appendString.length == 1) hexString.append("0")
                        hexString.append(appendString).append(":")
                    }
                    var result = hexString.toString()
                    if (result.endsWith(":")) {
                        result = result.substring(0, result.length - 1)
                    }
                    Log.d("AssetFlowGoogleAuth", "ACTUAL RUNTIME SHA-1 SIGNATURE: $result")
                }
            }
        } catch (e: Exception) {
            Log.e("AssetFlowGoogleAuth", "Failed to get SHA-1 signature", e)
        }
    }
}
