package edu.cit.salonga.assetflow.features.borrow.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.borrow.adapter.BorrowRequestAdapter
import edu.cit.salonga.assetflow.features.borrow.model.BorrowRequestDto
import edu.cit.salonga.assetflow.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyTransactionsFragment : Fragment() {

    private lateinit var searchInput: com.google.android.material.textfield.TextInputEditText
    private lateinit var sortDropdown: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var adapter: BorrowRequestAdapter
    private lateinit var prevButton: com.google.android.material.button.MaterialButton
    private lateinit var nextButton: com.google.android.material.button.MaterialButton
    private lateinit var pageText: TextView

    private var requestsList = listOf<BorrowRequestDto>()
    private var searchKeyword = ""
    private var sortOrder = "newest"

    private var currentPage = 1
    private val pageSize = 6
    private var totalPages = 1
    private var pollingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.transactionsRecyclerView)
        loadingIndicator = view.findViewById(R.id.transactionsLoadingIndicator)
        emptyStateText = view.findViewById(R.id.transactionsEmptyStateText)
        searchInput = view.findViewById(R.id.transactionsSearchInput)
        sortDropdown = view.findViewById(R.id.transactionsSortDropdown)
        prevButton = view.findViewById(R.id.transactionsPrevButton)
        nextButton = view.findViewById(R.id.transactionsNextButton)
        pageText = view.findViewById(R.id.transactionsPageText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BorrowRequestAdapter(listOf(), isAdmin = false)
        recyclerView.adapter = adapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchKeyword = s.toString().trim()
                currentPage = 1
                applyFilters()
            }
        })

        val sortOptions = resources.getStringArray(R.array.transaction_sort_options)
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sortOptions)
        sortDropdown.setAdapter(sortAdapter)
        sortDropdown.setText(sortOptions.first(), false)
        sortDropdown.setOnItemClickListener { _, _, position, _ ->
            sortOrder = if (position == 1) "oldest" else "newest"
            currentPage = 1
            applyFilters()
        }

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

    }

    override fun onResume() {
        super.onResume()
        loadTransactions(showLoader = true, showErrors = true)
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
                loadTransactions(showLoader = false, showErrors = false)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun loadTransactions(showLoader: Boolean = true, showErrors: Boolean = true) {
        if (showLoader) {
            loadingIndicator.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.borrowService.getMyBorrowRequests()
                if (response.isSuccessful && response.body() != null) {
                    requestsList = response.body()!!.sortedByDescending { 
                        it.requestDateTime ?: it.requestDate ?: "" 
                    }
                    applyFilters()
                } else {
                    if (showErrors) {
                        val error = response.errorBody()?.string() ?: "Failed to retrieve transactions"
                        Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
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

    private fun applyFilters() {
        val filtered = requestsList.filter { request ->
            val haystack = listOf(
                request.assetName,
                request.status,
                request.requestDate,
                request.dueDate,
                request.requestDateTime
            ).filterNotNull().joinToString(" ").lowercase()

            haystack.contains(searchKeyword.lowercase())
        }

        val sorted = filtered.sortedWith { a, b ->
            val aTime = a.requestDateTime ?: a.requestDate ?: ""
            val bTime = b.requestDateTime ?: b.requestDate ?: ""
            if (sortOrder == "oldest") {
                aTime.compareTo(bTime)
            } else {
                bTime.compareTo(aTime)
            }
        }

        val total = sorted.size
        totalPages = maxOf(1, kotlin.math.ceil(total / pageSize.toDouble()).toInt())
        if (currentPage > totalPages) {
            currentPage = totalPages
        }

        val start = (currentPage - 1) * pageSize
        val end = minOf(start + pageSize, total)
        val paged = if (start in 0 until end) sorted.subList(start, end) else emptyList()

        adapter.updateList(paged)
        pageText.text = "Page $currentPage of $totalPages"
        prevButton.isEnabled = currentPage > 1
        nextButton.isEnabled = currentPage < totalPages

        if (sorted.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
        } else {
            emptyStateText.visibility = View.GONE
        }
    }

    private fun updatePagedResults() {
        applyFilters()
    }
}
