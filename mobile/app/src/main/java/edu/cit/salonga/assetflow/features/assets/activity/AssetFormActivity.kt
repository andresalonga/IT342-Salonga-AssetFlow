package edu.cit.salonga.assetflow.features.assets.activity

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.assets.model.AssetCreateDto
import edu.cit.salonga.assetflow.features.assets.model.AssetDto
import edu.cit.salonga.assetflow.features.assets.model.AssetUpdateDto
import edu.cit.salonga.assetflow.network.ApiClient
import kotlinx.coroutines.launch

class AssetFormActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var nameInput: TextInputEditText
    private lateinit var serialInput: TextInputEditText
    private lateinit var imageUrlInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var statusSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var loadingIndicator: ProgressBar

    private var assetId: Long = -1L
    private var isEditMode: Boolean = false
    
    private var categories = mutableListOf<String>()
    private val statuses = listOf("AVAILABLE", "BORROWED", "MAINTENANCE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_form)

        assetId = intent.getLongExtra("assetId", -1L)
        isEditMode = assetId != -1L

        // Bind layouts
        toolbar = findViewById(R.id.formToolbar)
        nameInput = findViewById(R.id.formAssetName)
        serialInput = findViewById(R.id.formAssetSerial)
        imageUrlInput = findViewById(R.id.formAssetImageUrl)
        descriptionInput = findViewById(R.id.formAssetDescription)
        categorySpinner = findViewById(R.id.formAssetCategorySpinner)
        statusSpinner = findViewById(R.id.formAssetStatusSpinner)
        saveButton = findViewById(R.id.formSaveButton)
        loadingIndicator = findViewById(R.id.formLoadingIndicator)

        // Setup Toolbar back press
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup dynamic status list
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter

        // Setup toolbar text
        if (isEditMode) {
            toolbar.title = "Edit Asset"
        } else {
            toolbar.title = "Register Asset"
        }

        saveButton.setOnClickListener { validateAndSave() }

        loadCategoriesAndData()
    }

    private fun loadCategoriesAndData() {
        loadingIndicator.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Fetch dynamic categories
                val categoriesResponse = ApiClient.categoryService.getAllCategories()
                if (categoriesResponse.isSuccessful && categoriesResponse.body() != null) {
                    categories.clear()
                    categories.addAll(categoriesResponse.body()!!)
                    
                    val categoryAdapter = ArrayAdapter(this@AssetFormActivity, android.R.layout.simple_spinner_item, categories)
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = categoryAdapter
                } else {
                    Toast.makeText(this@AssetFormActivity, "Failed to load categories list", Toast.LENGTH_SHORT).show()
                }

                // If edit mode, load existing asset values
                if (isEditMode) {
                    val assetResponse = ApiClient.assetService.getAssetById(assetId)
                    if (assetResponse.isSuccessful && assetResponse.body() != null) {
                        populateFormFields(assetResponse.body()!!)
                    } else {
                        Toast.makeText(this@AssetFormActivity, "Failed to fetch asset data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AssetFormActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }

    private fun populateFormFields(asset: AssetDto) {
        nameInput.setText(asset.name ?: "")
        serialInput.setText(asset.serialNumber ?: "")
        imageUrlInput.setText(asset.imageUrl ?: "")
        descriptionInput.setText(asset.description ?: "")

        // Select correct spinner values
        val categoryIndex = categories.indexOfFirst { it.equals(asset.category, ignoreCase = true) }
        if (categoryIndex != -1) {
            categorySpinner.setSelection(categoryIndex)
        }

        val statusIndex = statuses.indexOfFirst { it.equals(asset.status, ignoreCase = true) }
        if (statusIndex != -1) {
            statusSpinner.setSelection(statusIndex)
        }
    }

    private fun validateAndSave() {
        val name = nameInput.text.toString().trim()
        val serial = serialInput.text.toString().trim()
        val imageUrl = imageUrlInput.text.toString().trim().let { if (it.isEmpty()) null else it }
        val description = descriptionInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error = "Asset Name is required"
            return
        }

        if (serial.isEmpty()) {
            serialInput.error = "Serial Number is required"
            return
        }

        if (categorySpinner.selectedItem == null) {
            Toast.makeText(this, "Please select or add a category", Toast.LENGTH_LONG).show()
            return
        }

        val category = categorySpinner.selectedItem.toString()
        val status = statusSpinner.selectedItem.toString()

        loadingIndicator.visibility = View.VISIBLE
        saveButton.isEnabled = false

        lifecycleScope.launch {
            try {
                if (isEditMode) {
                    val request = AssetUpdateDto(name, serial, category, status, description, imageUrl)
                    val response = ApiClient.assetService.updateAsset(assetId, request)
                    
                    if (response.isSuccessful) {
                        Toast.makeText(this@AssetFormActivity, "Asset updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = response.errorBody()?.string() ?: "Failed to update asset"
                        Toast.makeText(this@AssetFormActivity, "Error: $error", Toast.LENGTH_LONG).show()
                        saveButton.isEnabled = true
                    }
                } else {
                    val request = AssetCreateDto(name, serial, category, status, description, imageUrl)
                    val response = ApiClient.assetService.createAsset(request)
                    
                    if (response.isSuccessful) {
                        Toast.makeText(this@AssetFormActivity, "Asset registered successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = response.errorBody()?.string() ?: "Failed to register asset"
                        Toast.makeText(this@AssetFormActivity, "Error: $error", Toast.LENGTH_LONG).show()
                        saveButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AssetFormActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }
}
