package edu.cit.salonga.assetflow.features.assets.fragment

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.assets.model.AssetCreateDto
import edu.cit.salonga.assetflow.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddAssetFragment : Fragment() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var serialInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var statusSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var loadingIndicator: ProgressBar

    // Image Upload Views
    private lateinit var uploadImageCard: MaterialCardView
    private lateinit var uploadPlaceholderContainer: LinearLayout
    private lateinit var uploadPlaceholderIcon: ImageView
    private lateinit var uploadPlaceholderText: android.widget.TextView
    private lateinit var imagePreviewContainer: RelativeLayout
    private lateinit var imagePreview: ShapeableImageView
    private lateinit var btnRemoveImage: ImageButton

    private val categories = mutableListOf<String>()
    private val statuses = listOf("AVAILABLE", "BORROWED", "MAINTENANCE")
    private var pollingJob: Job? = null

    // Upload state variables
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            handleImageSelection(uri)
        }
    }

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
        descriptionInput = view.findViewById(R.id.formAssetDescription)
        categorySpinner = view.findViewById(R.id.formAssetCategorySpinner)
        statusSpinner = view.findViewById(R.id.formAssetStatusSpinner)
        saveButton = view.findViewById(R.id.formSaveButton)
        loadingIndicator = view.findViewById(R.id.formLoadingIndicator)

        // Bind image upload views
        uploadImageCard = view.findViewById(R.id.uploadImageCard)
        uploadPlaceholderContainer = view.findViewById(R.id.uploadPlaceholderContainer)
        uploadPlaceholderIcon = view.findViewById(R.id.uploadPlaceholderIcon)
        uploadPlaceholderText = view.findViewById(R.id.uploadPlaceholderText)
        imagePreviewContainer = view.findViewById(R.id.imagePreviewContainer)
        imagePreview = view.findViewById(R.id.imagePreview)
        btnRemoveImage = view.findViewById(R.id.btnRemoveImage)

        val ctx = context
        if (ctx != null) {
            val statusAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, statuses)
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = statusAdapter
        }

        // Set listeners for upload
        uploadImageCard.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnRemoveImage.setOnClickListener {
            removeSelectedImage()
        }

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
                
                // Safety check: verify fragment is attached and context is valid
                if (!isAdded) return@launch
                val ctx = context ?: return@launch

                if (categoriesResponse.isSuccessful && categoriesResponse.body() != null) {
                    categories.clear()
                    categories.addAll(categoriesResponse.body()!!)

                    val categoryAdapter = ArrayAdapter(
                        ctx,
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
                        Toast.makeText(ctx, "Failed to load categories list", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (showErrors && isAdded) {
                    val ctx = context
                    if (ctx != null) {
                        Toast.makeText(ctx, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                if (showLoader && isAdded) {
                    loadingIndicator.visibility = View.GONE
                }
            }
        }
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val ctx = context ?: return
            val contentResolver = ctx.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            var fileSize = 0L
            var fileName = "image.jpg"
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (sizeIndex != -1) fileSize = c.getLong(sizeIndex)
                    if (nameIndex != -1) fileName = c.getString(nameIndex)
                }
            }

            val mimeType = contentResolver.getType(uri) ?: ""
            val isValidType = mimeType == "image/png" || mimeType == "image/jpeg" || mimeType == "image/jpg" ||
                              fileName.endsWith(".png", true) || fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true)

            if (!isValidType) {
                Toast.makeText(ctx, "Only PNG or JPG images are allowed.", Toast.LENGTH_SHORT).show()
                return
            }

            if (fileSize > 1024 * 1024) {
                Toast.makeText(ctx, "Please upload an image up to 1MB.", Toast.LENGTH_SHORT).show()
                return
            }

            selectedImageUri = uri
            uploadPlaceholderContainer.visibility = View.GONE
            imagePreviewContainer.visibility = View.VISIBLE
            imagePreview.setImageURI(uri)
        } catch (e: Exception) {
            val ctx = context
            if (ctx != null) {
                Toast.makeText(ctx, "Error selecting image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeSelectedImage() {
        selectedImageUri = null
        imagePreviewContainer.visibility = View.GONE
        uploadPlaceholderContainer.visibility = View.VISIBLE
        imagePreview.setImageDrawable(null)
    }

    private suspend fun uploadSelectedImage(ctx: android.content.Context): String? {
        val uri = selectedImageUri ?: return null
        val contentResolver = ctx.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        var fileName = "image.jpg"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) fileName = c.getString(nameIndex)
            }
        }

        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()

        if (bytes == null) return null

        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", fileName, requestFile)

        val response = ApiClient.assetService.uploadAssetImage(body)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!["url"]
        } else {
            val error = response.errorBody()?.string() ?: "Failed to upload image"
            throw Exception(error)
        }
    }

    private fun validateAndSave() {
        val ctx = context ?: return
        val name = nameInput.text.toString().trim()
        val serial = serialInput.text.toString().trim()
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
            Toast.makeText(ctx, "Please select a category", Toast.LENGTH_LONG).show()
            return
        }

        val category = categorySpinner.selectedItem.toString()
        val status = statusSpinner.selectedItem.toString()

        loadingIndicator.visibility = View.VISIBLE
        saveButton.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Safety check inside asynchronous routine
                if (!isAdded) return@launch
                val innerCtx = context ?: return@launch

                // Upload image if selected
                val imageUrl = if (selectedImageUri != null) {
                    uploadSelectedImage(innerCtx)
                } else {
                    null
                }

                if (!isAdded) return@launch
                val request = AssetCreateDto(name, serial, category, status, description, imageUrl)
                val response = ApiClient.assetService.createAsset(request)

                if (response.isSuccessful) {
                    Toast.makeText(innerCtx, "Asset registered successfully", Toast.LENGTH_SHORT).show()
                    clearForm()
                } else {
                    val error = response.errorBody()?.string() ?: "Failed to register asset"
                    Toast.makeText(innerCtx, "Error: $error", Toast.LENGTH_LONG).show()
                    saveButton.isEnabled = true
                }
            } catch (e: Exception) {
                if (isAdded) {
                    val errorCtx = context
                    if (errorCtx != null) {
                        Toast.makeText(errorCtx, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    saveButton.isEnabled = true
                }
            } finally {
                if (isAdded) {
                    loadingIndicator.visibility = View.GONE
                }
            }
        }
    }

    private fun clearForm() {
        nameInput.text?.clear()
        serialInput.text?.clear()
        descriptionInput.text?.clear()
        removeSelectedImage()
        statusSpinner.setSelection(0)
        if (categories.isNotEmpty()) {
            categorySpinner.setSelection(0)
        }
        saveButton.isEnabled = true
    }
}
