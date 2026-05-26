package edu.cit.salonga.assetflow.features.assets.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.assets.model.AssetDto
import edu.cit.salonga.assetflow.features.auth.utils.TokenManager
import edu.cit.salonga.assetflow.features.borrow.model.BorrowRequestCreateDto
import edu.cit.salonga.assetflow.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AssetDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var imageView: ImageView
    private lateinit var nameView: TextView
    private lateinit var statusBadge: TextView
    private lateinit var categoryView: TextView
    private lateinit var serialView: TextView
    private lateinit var createdView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var loadingIndicator: ProgressBar

    // Action layouts
    private lateinit var borrowButton: Button
    private lateinit var adminActionPanel: LinearLayout
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button

    private var assetId: Long = -1L
    private var asset: AssetDto? = null
    private var isAdmin: Boolean = false
    private var pollingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_detail)

        assetId = intent.getLongExtra("assetId", -1L)
        isAdmin = TokenManager.getUserRole().equals("ADMIN", ignoreCase = true)

        if (assetId == -1L) {
            Toast.makeText(this, "Invalid Asset ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Bind Views
        toolbar = findViewById(R.id.detailToolbar)
        imageView = findViewById(R.id.detailAssetImage)
        nameView = findViewById(R.id.detailAssetName)
        statusBadge = findViewById(R.id.detailAssetStatusBadge)
        categoryView = findViewById(R.id.detailAssetCategory)
        serialView = findViewById(R.id.detailAssetSerial)
        createdView = findViewById(R.id.detailAssetCreated)
        descriptionView = findViewById(R.id.detailAssetDescription)
        loadingIndicator = findViewById(R.id.detailLoadingIndicator)

        borrowButton = findViewById(R.id.studentBorrowButton)
        adminActionPanel = findViewById(R.id.adminDetailActionPanel)
        editButton = findViewById(R.id.adminEditButton)
        deleteButton = findViewById(R.id.adminDeleteButton)

        // Setup toolbar back navigation
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup actions based on role
        if (isAdmin) {
            adminActionPanel.visibility = View.VISIBLE
            borrowButton.visibility = View.GONE
            
            editButton.setOnClickListener {
                val intent = Intent(this, AssetFormActivity::class.java).apply {
                    putExtra("assetId", assetId)
                }
                startActivity(intent)
            }
            
            deleteButton.setOnClickListener {
                showDeleteConfirmationDialog()
            }
        } else {
            adminActionPanel.visibility = View.GONE
            // Borrow button visible state will be evaluated upon fetching asset details
            borrowButton.visibility = View.GONE 
            borrowButton.setOnClickListener { showDatePicker() }
        }

        loadAssetDetails()
    }

    override fun onResume() {
        super.onResume()
        loadAssetDetails(showLoader = true, showErrors = true)
        startPolling()
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = lifecycleScope.launch {
            while (true) {
                delay(5000)
                loadAssetDetails(showLoader = false, showErrors = false)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun loadAssetDetails(showLoader: Boolean = true, showErrors: Boolean = true) {
        if (showLoader) {
            loadingIndicator.visibility = View.VISIBLE
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.assetService.getAssetById(assetId)
                if (response.isSuccessful && response.body() != null) {
                    val loadedAsset = response.body()!!
                    asset = loadedAsset
                    bindAssetDetails(loadedAsset)
                } else {
                    if (showErrors) {
                        Toast.makeText(this@AssetDetailActivity, "Failed to load asset details", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (showErrors) {
                    Toast.makeText(this@AssetDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (showLoader) {
                    loadingIndicator.visibility = View.GONE
                }
            }
        }
    }

    private fun bindAssetDetails(asset: AssetDto) {
        nameView.text = asset.name ?: "Unnamed Asset"
        categoryView.text = asset.category ?: "Uncategorized"
        serialView.text = "Serial Number: ${asset.serialNumber ?: "N/A"}"
        descriptionView.text = asset.description ?: "No description provided."
        
        // Format creation date (remove time signature if present)
        val rawDate = asset.createdAt ?: "N/A"
        val formattedDate = if (rawDate.contains("T")) rawDate.split("T")[0] else rawDate
        createdView.text = "Registered On: $formattedDate"

        // Status Badge binding
        val status = asset.status?.uppercase() ?: "AVAILABLE"
        statusBadge.text = status

        val colorRes = when (status) {
            "AVAILABLE" -> R.color.status_available
            "BORROWED" -> R.color.status_borrowed
            "MAINTENANCE" -> R.color.status_maintenance
            else -> R.color.text_secondary
        }
        val bgColorRes = when (status) {
            "AVAILABLE" -> R.color.status_available_bg
            "BORROWED" -> R.color.status_borrowed_bg
            "MAINTENANCE" -> R.color.status_maintenance_bg
            else -> R.color.card_border
        }
        statusBadge.setTextColor(ContextCompat.getColor(this, colorRes))
        statusBadge.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, bgColorRes)
        )

        // Load visual illustration via Glide
        val rawUrl = asset.imageUrl
        val finalUrl = rawUrl
            ?.replace("localhost", ApiClient.BASE_IP)
            ?.replace("10.0.2.2", ApiClient.BASE_IP)
        
        Glide.with(this)
            .load(finalUrl)
            .placeholder(R.drawable.bg_asset_placeholder)
            .error(R.drawable.bg_asset_placeholder)
            .centerCrop()
            .into(imageView)

        // Dynamic visibility evaluation for students
        if (!isAdmin) {
            if (status == "AVAILABLE") {
                borrowButton.visibility = View.VISIBLE
                borrowButton.isEnabled = true
            } else {
                borrowButton.visibility = View.VISIBLE
                borrowButton.isEnabled = false
                borrowButton.text = "Not Available ($status)"
                borrowButton.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.text_hint)
                )
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val chosenDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, 23, 59, 59)
                }
                
                // Validate selected date is not in the past
                if (chosenDate.before(Calendar.getInstance())) {
                    Toast.makeText(this, "Due date cannot be in the past", Toast.LENGTH_LONG).show()
                } else {
                    val formattedDateString = String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                    )
                    submitBorrow(formattedDateString)
                }
            },
            year,
            month,
            day
        )
        // Restrict user from selecting past dates
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun submitBorrow(dueDate: String) {
        loadingIndicator.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val request = BorrowRequestCreateDto(dueDate)
                val response = ApiClient.borrowService.submitBorrowRequest(assetId, request)
                
                if (response.isSuccessful) {
                    showSuccessDialog()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to submit borrow request"
                    Toast.makeText(this@AssetDetailActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AssetDetailActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Request Submitted!")
        builder.setMessage("Your borrow request has been logged successfully and is currently pending administrator review.")
        builder.setPositiveButton("Okay") { dialog, _ ->
            dialog.dismiss()
            loadAssetDetails() // refresh screen details
        }
        builder.create().show()
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Asset")
        builder.setMessage("Are you sure you want to permanently delete this asset? This action is irreversible.")
        builder.setPositiveButton("Delete") { dialog, _ ->
            dialog.dismiss()
            performDelete()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun performDelete() {
        loadingIndicator.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.assetService.deleteAsset(assetId)
                if (response.isSuccessful) {
                    Toast.makeText(this@AssetDetailActivity, "Asset deleted successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close details and return
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Delete failed"
                    Toast.makeText(this@AssetDetailActivity, "Failed to delete: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AssetDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }
}
