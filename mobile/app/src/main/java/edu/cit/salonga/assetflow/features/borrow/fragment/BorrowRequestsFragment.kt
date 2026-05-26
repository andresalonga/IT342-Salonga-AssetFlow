package edu.cit.salonga.assetflow.features.borrow.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.borrow.adapter.BorrowRequestAdapter
import edu.cit.salonga.assetflow.features.borrow.model.BorrowRequestDto
import edu.cit.salonga.assetflow.network.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BorrowRequestsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var searchInput: TextInputEditText
    private lateinit var sortDropdown: MaterialAutoCompleteTextView
    private lateinit var adapter: BorrowRequestAdapter

    private var requestsList = listOf<BorrowRequestDto>()
    private var searchKeyword = ""
    private var sortOrder = "newest"
    private var pollingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_borrow_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.requestsRecyclerView)
        loadingIndicator = view.findViewById(R.id.requestsLoadingIndicator)
        emptyStateText = view.findViewById(R.id.requestsEmptyStateText)
        searchInput = view.findViewById(R.id.requestsSearchInput)
        sortDropdown = view.findViewById(R.id.requestsSortDropdown)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BorrowRequestAdapter(
            requests = listOf(),
            isAdmin = true,
            onApproveClick = { request ->
                updateRequestStatus(request.id?.toLongOrNull() ?: return@BorrowRequestAdapter, "APPROVED", "")
            },
            onRejectClick = { request ->
                showRejectDialog(request)
            },
            onReturnClick = { request ->
                updateRequestStatus(request.id?.toLongOrNull() ?: return@BorrowRequestAdapter, "RETURNED", "")
            }
        )
        recyclerView.adapter = adapter

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchKeyword = s.toString().trim()
                applyFilters()
            }
        })

        val sortOptions = resources.getStringArray(R.array.transaction_sort_options)
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sortOptions)
        sortDropdown.setAdapter(sortAdapter)
        sortDropdown.setText(sortOptions.first(), false)
        sortDropdown.setOnItemClickListener { _, _, position, _ ->
            sortOrder = if (position == 1) "oldest" else "newest"
            applyFilters()
        }
    }

    override fun onResume() {
        super.onResume()
        loadRequests(showLoader = true, showErrors = true)
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
                loadRequests(showLoader = false, showErrors = false)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun loadRequests(showLoader: Boolean = true, showErrors: Boolean = true) {
        if (showLoader) {
            loadingIndicator.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.borrowService.getAllBorrowRequests()
                if (response.isSuccessful && response.body() != null) {
                    requestsList = response.body()!!.sortedByDescending { 
                        it.requestDateTime ?: it.requestDate ?: "" 
                    }
                    applyFilters()
                } else {
                    if (showErrors) {
                        val error = response.errorBody()?.string() ?: "Failed to retrieve requests"
                        Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                val message = e.message ?: "Network Error"
                if (showErrors && message.contains("timeout", ignoreCase = true)) {
                    Toast.makeText(requireContext(), "Request is processing. Refreshing...", Toast.LENGTH_SHORT).show()
                    loadRequests(showLoader = false, showErrors = false)
                } else if (showErrors) {
                    Toast.makeText(requireContext(), "Network Error: $message", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (showLoader) {
                    loadingIndicator.visibility = View.GONE
                }
            }
        }
    }

    private fun showRejectDialog(request: BorrowRequestDto) {
        val requestId = request.id?.toLongOrNull() ?: return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_borrow_request_action, null)
        val noteInput = dialogView.findViewById<TextInputEditText>(R.id.dialogNoteInput)
        val cancelBtn = dialogView.findViewById<Button>(R.id.dialogCancelBtn)
        val rejectBtn = dialogView.findViewById<Button>(R.id.dialogRejectBtn)
        val approveBtn = dialogView.findViewById<Button>(R.id.dialogApproveBtn)
        val dialogSubtitle = dialogView.findViewById<TextView>(R.id.dialogSubtitle)

        dialogSubtitle.text = "Provide a rejection note before rejecting this request."
        approveBtn.visibility = View.GONE

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        cancelBtn.setOnClickListener { dialog.dismiss() }

        rejectBtn.setOnClickListener {
            val note = noteInput.text.toString().trim()
            if (note.isEmpty()) {
                noteInput.error = "A note is required for rejection"
                Toast.makeText(requireContext(), "Please provide a rejection reason.", Toast.LENGTH_LONG).show()
            } else {
                dialog.dismiss()
                updateRequestStatus(requestId, "REJECTED", note)
            }
        }

        dialog.show()
    }

    private fun updateRequestStatus(requestId: Long, status: String, note: String) {
        loadingIndicator.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Construct simple PATCH body payload matching backend Map mapping
                val body = mapOf("status" to status, "note" to note)
                val response = ApiClient.borrowService.updateBorrowRequestStatus(requestId, body)
                
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Borrow request $status successfully!", Toast.LENGTH_SHORT).show()
                    loadRequests() // reload lists
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to update request"
                    Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }

    private fun applyFilters() {
        val filtered = requestsList.filter { request ->
            val haystack = listOf(
                request.userName,
                request.userEmail,
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

        adapter.updateList(sorted)
        emptyStateText.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
    }
}
