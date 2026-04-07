package edu.cit.salonga.assetflow

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.cit.salonga.assetflow.utils.TokenManager

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create a simple splash layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(resources.getColor(R.color.primary, null))
        }
        
        val logoView = TextView(this).apply {
            text = "AssetFlow"
            textSize = 32f
            setTextColor(resources.getColor(R.color.white, null))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 32, 0, 0)
            }
        }
        
        layout.addView(logoView)
        layout.addView(progressBar)
        
        setContentView(layout)
        
        // Check login status after a short delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 1500)
    }
    
    private fun checkLoginStatus() {
        val isLoggedIn = TokenManager.isLoggedIn()
        
        val intent = if (isLoggedIn) {
            Intent(this, DashboardActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}
