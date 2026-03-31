package edu.cit.salonga.assetflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Navigate to RegisterActivity and finish MainActivity
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }
}