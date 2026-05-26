package edu.cit.salonga.assetflow.features.assets.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.assets.activity.AssetDetailActivity
import edu.cit.salonga.assetflow.features.assets.activity.AssetFormActivity
import edu.cit.salonga.assetflow.features.assets.adapter.AssetAdapter
import edu.cit.salonga.assetflow.features.assets.model.AssetDto
import edu.cit.salonga.assetflow.features.auth.utils.TokenManager
import edu.cit.salonga.assetflow.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AssetsFragment : Fragment() {

    private lateinit var searchInput: TextInputEditText
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var assetsRecyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var addAssetFab: FloatingActionButton
    private lateinit var prevButton: com.google.android.material.button.MaterialButton
    private lateinit var nextButton: com.google.android.material.button.MaterialButton
    private lateinit var pageText: TextView
    private lateinit var welcomeMessage: TextView
    private lateinit var totalAssetsValue: TextView
    private lateinit var availableAssetsValue: TextView
    private lateinit var borrowedAssetsValue: TextView
    private lateinit var maintenanceAssetsValue: TextView

    private var allAssets = listOf<AssetDto>()
    private var filteredAssets = listOf<AssetDto>()
    private var categories = listOf<String>()
    
    private var selectedCategory: String? = null
    private var searchKeyword: String = ""
    private var isAdmin: Boolean = false

    private var currentPage = 1
    private val pageSize = 9
    private var totalPages = 1
    private var pollingJob: Job? = null
    
    private lateinit var assetAdapter: AssetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_assets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isAdmin = TokenManager.getUserRole().equals("ADMIN", ignoreCase = true)

        // Bind layouts
        searchInput = view.findViewById(R.id.searchInput)
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup)
        assetsRecyclerView = view.findViewById(R.id.assetsRecyclerView)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        addAssetFab = view.findViewById(R.id.addAssetFab)
        prevButton = view.findViewById(R.id.assetsPrevButton)
        nextButton = view.findViewById(R.id.assetsNextButton)
        pageText = view.findViewById(R.id.assetsPageText)
        welcomeMessage = view.findViewById(R.id.welcomeMessage)
        totalAssetsValue = view.findViewById(R.id.totalAssetsValue)
        availableAssetsValue = view.findViewById(R.id.availableAssetsValue)
        borrowedAssetsValue = view.findViewById(R.id.borrowedAssetsValue)
        maintenanceAssetsValue = view.findViewById(R.id.maintenanceAssetsValue)

        // Setup Recycler
        assetsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Setup adapter with callbacks
        assetAdapter = AssetAdapter(
            assets = listOf(),
            isAdmin = isAdmin,
            onAssetClick = { asset ->
                val intent = Intent(requireContext(), AssetDetailActivity::class.java).apply {
                    putExtra("assetId", asset.id?.toLongOrNull() ?: -1L)
                }
                startActivity(intent)
            },
            onEditClick = { asset ->
                val intent = Intent(requireContext(), AssetFormActivity::class.java).apply {
                    putExtra("assetId", asset.id?.toLongOrNull() ?: -1L)
                }
                startActivity(intent)
            },
            onDeleteClick = { asset ->
                showDeleteConfirmationDialog(asset)
            }
        )
        assetsRecyclerView.adapter = assetAdapter

        // Setup FAB for Admin
        addAssetFab.visibility = View.GONE

        updateWelcomeMessage()

        // Setup search filter listener
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchKeyword = s.toString().trim()
                currentPage = 1
                applyFilters()
            }
        })

        prevButton.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updatePagedResults()
            }
        }

        nextButton.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                updatePagedResults()
            }
        }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data on returning to screen to ensure updates sync
        loadData(showLoader = true, showErrors = true)
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
                if (!isAdded) return@launch
                loadData(showLoader = false, showErrors = false)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun loadData(showLoader: Boolean = true, showErrors: Boolean = true) {
        if (showLoader) {
            loadingIndicator.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ctx = context ?: return@launch

                // Fetch assets
                val assetsResponse = ApiClient.assetService.getAllAssets()
                if (assetsResponse.isSuccessful && assetsResponse.body() != null) {
                    allAssets = assetsResponse.body()!!
                    updateSummaryCards()
                } else {
                    if (showErrors) {
                        Toast.makeText(ctx, "Failed to load assets", Toast.LENGTH_SHORT).show()
                    }
                }

                // Fetch categories
                val categoriesResponse = ApiClient.categoryService.getAllCategories()
                if (categoriesResponse.isSuccessful && categoriesResponse.body() != null) {
                    categories = categoriesResponse.body()!!
                    if (isAdded) {
                        setupCategoryChips()
                    }
                }

                if (isAdded) {
                    applyFilters()
                }
            } catch (e: Exception) {
                if (showErrors) {
                    context?.let {
                        Toast.makeText(it, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                if (isAdded && showLoader) {
                    loadingIndicator.visibility = View.GONE
                }
            }
        }
    }

    private fun setupCategoryChips() {
        categoryChipGroup.removeAllViews()

        // Create "All" Chip programmatically
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = selectedCategory == null
            chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_background)
            chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.card_border)
            chipStrokeWidth = 1f
            setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text))
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategory = null
                    currentPage = 1
                    applyFilters()
                }
            }
        }
        categoryChipGroup.addView(allChip)

        // Create chip for each category
        for (category in categories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isChecked = selectedCategory == category
                chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_background)
                chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.card_border)
                chipStrokeWidth = 1f
                setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text))
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedCategory = category
                        currentPage = 1
                        applyFilters()
                    }
                }
            }
            categoryChipGroup.addView(chip)
        }
    }

    private fun applyFilters() {
        filteredAssets = allAssets.filter { asset ->
            val matchesSearch = searchKeyword.isEmpty() || 
                    (asset.name?.contains(searchKeyword, ignoreCase = true) == true) || 
                    (asset.serialNumber?.contains(searchKeyword, ignoreCase = true) == true)
            
            val matchesCategory = selectedCategory == null || 
                    asset.category?.equals(selectedCategory, ignoreCase = true) == true
            
            matchesSearch && matchesCategory
        }

        updatePagedResults()
    }

    private fun updatePagedResults() {
        totalPages = maxOf(1, kotlin.math.ceil(filteredAssets.size / pageSize.toDouble()).toInt())
        if (currentPage > totalPages) {
            currentPage = totalPages
        }

        val start = (currentPage - 1) * pageSize
        val end = minOf(start + pageSize, filteredAssets.size)
        val paged = if (start in 0 until end) filteredAssets.subList(start, end) else emptyList()

        assetAdapter.updateList(paged)
        pageText.text = "Page $currentPage of $totalPages"
        prevButton.isEnabled = currentPage > 1
        nextButton.isEnabled = currentPage < totalPages

        if (filteredAssets.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
        } else {
            emptyStateText.visibility = View.GONE
        }
    }

    private fun updateWelcomeMessage() {
        val fullName = TokenManager.getUserName()?.trim().orEmpty()
        val firstName = fullName.split(" ").firstOrNull { it.isNotBlank() } ?: "there"
        val message = if (isAdmin) {
            "Welcome back, $firstName. Manage your organization's assets."
        } else {
            "Welcome back, $firstName. Browse available assets."
        }
        welcomeMessage.text = message
    }

    private fun updateSummaryCards() {
        val total = allAssets.size
        val available = allAssets.count { it.status.equals("AVAILABLE", ignoreCase = true) }
        val borrowed = allAssets.count { it.status.equals("BORROWED", ignoreCase = true) }
        val maintenance = allAssets.count { it.status.equals("MAINTENANCE", ignoreCase = true) }

        totalAssetsValue.text = total.toString()
        availableAssetsValue.text = available.toString()
        borrowedAssetsValue.text = borrowed.toString()
        maintenanceAssetsValue.text = maintenance.toString()
    }

    private fun showDeleteConfirmationDialog(asset: AssetDto) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Asset")
        builder.setMessage("Are you sure you want to delete ${asset.name} (SN: ${asset.serialNumber})?")
        builder.setPositiveButton("Delete") { dialog, _ ->
            dialog.dismiss()
            performDelete(asset)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun performDelete(asset: AssetDto) {
        val assetId = asset.id?.toLongOrNull() ?: return
        loadingIndicator.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.assetService.deleteAsset(assetId)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Asset deleted successfully", Toast.LENGTH_SHORT).show()
                    loadData() // reload list
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Delete failed"
                    Toast.makeText(requireContext(), "Failed to delete: $errorMsg", Toast.LENGTH_LONG).show()
                    loadingIndicator.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                loadingIndicator.visibility = View.GONE
            }
        }
    }
}
