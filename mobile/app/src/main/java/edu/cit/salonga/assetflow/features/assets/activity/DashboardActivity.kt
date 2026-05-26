package edu.cit.salonga.assetflow.features.assets.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.assets.fragment.AssetsFragment
import edu.cit.salonga.assetflow.features.assets.fragment.AddAssetFragment
import edu.cit.salonga.assetflow.features.borrow.fragment.BorrowRequestsFragment
import edu.cit.salonga.assetflow.features.borrow.fragment.MyTransactionsFragment
import edu.cit.salonga.assetflow.features.profile.fragment.ProfileFragment
import edu.cit.salonga.assetflow.features.auth.utils.TokenManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupNavigationBasedOnRole()
    }

    private fun setupNavigationBasedOnRole() {
        val role = TokenManager.getUserRole() ?: "USER"
        
        // Inflate correct menu based on user role
        if (role.equals("ADMIN", ignoreCase = true)) {
            bottomNavigation.inflateMenu(R.menu.menu_admin_nav)
        } else {
            bottomNavigation.inflateMenu(R.menu.menu_student_nav)
        }

        // Set default fragment
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            loadFragment(AssetsFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_assets -> AssetsFragment()
                R.id.nav_transactions -> {
                    MyTransactionsFragment()
                }
                R.id.nav_requests -> {
                    BorrowRequestsFragment()
                }
                R.id.nav_add_asset -> {
                    AddAssetFragment()
                }
                R.id.nav_profile -> {
                    ProfileFragment()
                }
                else -> return@setOnItemSelectedListener false
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
