package edu.cit.salonga.assetflow.features.assets.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.assets.model.AssetCreateDto
import edu.cit.salonga.assetflow.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddAssetFragment : Fragment() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var serialInput: TextInputEditText
    private lateinit var imageUrlInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var statusSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var loadingIndicator: ProgressBar

    private val categories = mutableListOf<String>()
    private val statuses = listOf("AVAILABLE", "BORROWED", "MAINTENANCE")
    private var pollingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_asset, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput = view.findViewById(R.id.formAssetName)
        serialInput = view.findViewById(R.id.formAssetSerial)
        imageUrlInput = view.findViewById(R.id.formAssetImageUrl)
        descriptionInput = view.findViewById(R.id.formAssetDescription)
        categorySpinner = view.findViewById(R.id.formAssetCategorySpinner)
        statusSpinner = view.findViewById(R.id.formAssetStatusSpinner)
        saveButton = view.findViewById(R.id.formSaveButton)
        loadingIndicator = view.findViewById(R.id.formLoadingIndicator)

        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter

        saveButton.setOnClickListener { validateAndSave() }
    }

    override fun onResume() {
        super.onResume()
        loadCategories(showLoader = true, showErrors = true)
        startPolling()
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                delay(5000)
                loadCategories(showLoader = false, showErrors = false)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun loadCategories(showLoader: Boolean = true, showErrors: Boolean = true) {
        if (showLoader) {
            loadingIndicator.visibility = View.VISIBLE
        }

        val selectedCategory = categorySpinner.selectedItem?.toString()


        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val categoriesResponse = ApiClient.categoryService.getAllCategories()
                if (categoriesResponse.isSuccessful && categoriesResponse.body() != null) {
                    categories.clear()
                    categories.addAll(categoriesResponse.body()!!)

                    val categoryAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categories
                    )
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = categoryAdapter

                    if (!selectedCategory.isNullOrBlank()) {
                        val index = categories.indexOfFirst { it.equals(selectedCategory, ignoreCase = true) }
                        if (index >= 0) {
                            categorySpinner.setSelection(index)
                        }
                    }
                } else {
                    if (showErrors) {
                        Toast.makeText(requireContext(), "Failed to load categories list", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (showErrors) {
                    Toast.makeText(requireContext(), "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (showLoader) {
                    loadingIndicator.visibility = View.GONE
                }
            }
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
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_LONG).show()
            return
        }

        val category = categorySpinner.selectedItem.toString()
        val status = statusSpinner.selectedItem.toString()

        loadingIndicator.visibility = View.VISIBLE
        saveButton.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = AssetCreateDto(name, serial, category, status, description, imageUrl)
                val response = ApiClient.assetService.createAsset(request)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Asset registered successfully", Toast.LENGTH_SHORT).show()
                    clearForm()
                } else {
                    val error = response.errorBody()?.string() ?: "Failed to register asset"
                    Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
                    saveButton.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }

    private fun clearForm() {
        nameInput.text?.clear()
        serialInput.text?.clear()
        imageUrlInput.text?.clear()
        descriptionInput.text?.clear()
        statusSpinner.setSelection(0)
        if (categories.isNotEmpty()) {
            categorySpinner.setSelection(0)
        }
        saveButton.isEnabled = true
    }
}
