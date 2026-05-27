package edu.cit.salonga.assetflow.features.profile.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.auth.activity.LoginActivity
import edu.cit.salonga.assetflow.features.auth.utils.TokenManager

class ProfileFragment : Fragment() {

    private lateinit var userTitle: TextView
    private lateinit var profileInitials: TextView
    private lateinit var roleBadge: TextView
    private lateinit var fullNameText: TextView
    private lateinit var emailText: TextView
    private lateinit var roleText: TextView
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind layouts
        userTitle = view.findViewById(R.id.profUserTitle)
        profileInitials = view.findViewById(R.id.profileInitials)
        roleBadge = view.findViewById(R.id.profUserRoleBadge)
        fullNameText = view.findViewById(R.id.profFullName)
        emailText = view.findViewById(R.id.profEmail)
        roleText = view.findViewById(R.id.profRole)
        logoutButton = view.findViewById(R.id.profileLogoutButton)

        // Populate fields using secure TokenManager preferences
        val name = TokenManager.getUserName() ?: "Unknown User"
        val email = TokenManager.getUserEmail() ?: "N/A"
        val role = TokenManager.getUserRole() ?: "USER"

        userTitle.text = name
        fullNameText.text = name
        emailText.text = email
        roleBadge.text = if (role.equals("ADMIN", ignoreCase = true)) {
            "ADMIN"
        } else {
            "STUDENT"
        }

        profileInitials.text = buildInitials(name)
        
        roleText.text = if (role.equals("ADMIN", ignoreCase = true)) {
            "Administrator"
        } else {
            "Student"
        }

        logoutButton.setOnClickListener { showSignOutConfirmation() }
    }

    private fun buildInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (parts.isEmpty()) {
            return "?"
        }
        val first = parts.first().first().uppercaseChar()
        val last = if (parts.size > 1) parts.last().first().uppercaseChar() else '\u0000'
        return if (last == '\u0000') {
            first.toString()
        } else {
            "${first}${last}"
        }
    }

    private fun showSignOutConfirmation() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Sign Out")
        builder.setMessage("Are you sure you want to log out of your AssetFlow account?")
        builder.setPositiveButton("Sign Out") { dialog, _ ->
            dialog.dismiss()
            performLogout()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun performLogout() {
        // Clear all token and credential values in SharedPreferences
        TokenManager.clearAll()

        // Explicitly sign out of Google OAuth to clear session and show account chooser next time
        try {
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
            ).build()
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireContext(), gso)
            googleSignInClient.signOut()
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error signing out of Google OAuth", e)
        }

        // Redirect to login screen and clear activity history backstack
        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }
}
