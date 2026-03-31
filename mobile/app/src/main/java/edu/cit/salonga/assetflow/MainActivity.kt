package edu.cit.salonga.assetflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Navigate to SplashActivity and finish MainActivity
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}