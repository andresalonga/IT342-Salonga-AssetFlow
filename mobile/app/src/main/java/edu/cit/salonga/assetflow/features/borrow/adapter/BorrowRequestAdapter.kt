package edu.cit.salonga.assetflow.features.borrow.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.borrow.model.BorrowRequestDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class BorrowRequestAdapter(
    private var requests: List<BorrowRequestDto>,
    private val isAdmin: Boolean,
    private val onApproveClick: ((BorrowRequestDto) -> Unit)? = null,
    private val onRejectClick: ((BorrowRequestDto) -> Unit)? = null,
    private val onReturnClick: ((BorrowRequestDto) -> Unit)? = null
) : RecyclerView.Adapter<BorrowRequestAdapter.BorrowViewHolder>() {

    fun updateList(newList: List<BorrowRequestDto>) {
        requests = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_borrow_request, parent, false)
        return BorrowViewHolder(view)
    }

    override fun onBindViewHolder(holder: BorrowViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    inner class BorrowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val assetNameView: TextView = itemView.findViewById(R.id.reqAssetName)
        private val statusBadge: TextView = itemView.findViewById(R.id.reqStatusBadge)
        private val userPanel: LinearLayout = itemView.findViewById(R.id.reqUserPanel)
        private val userNameView: TextView = itemView.findViewById(R.id.reqUserName)
        private val userEmailView: TextView = itemView.findViewById(R.id.reqUserEmail)
        private val requestDateView: TextView = itemView.findViewById(R.id.reqRequestDate)
        private val dueDateView: TextView = itemView.findViewById(R.id.reqDueDate)
        private val noteCard: MaterialCardView = itemView.findViewById(R.id.reqNoteCard)
        private val noteTextView: TextView = itemView.findViewById(R.id.reqNoteText)
        private val adminActions: LinearLayout = itemView.findViewById(R.id.reqAdminActions)
        private val approveButton: Button = itemView.findViewById(R.id.reqApproveButton)
        private val rejectButton: Button = itemView.findViewById(R.id.reqRejectButton)
        private val returnButton: Button = itemView.findViewById(R.id.reqReturnButton)

        fun bind(request: BorrowRequestDto) {
            val context = itemView.context
            
            assetNameView.text = request.assetName ?: "Unnamed Asset"
            
            // Format Dates
            val requested = formatDateTime(request.requestDateTime ?: request.requestDate)
            requestDateView.text = "Requested: ${requested ?: "N/A"}"

            val dueDateClean = formatDateOnly(request.dueDate)
            dueDateView.text = "Due: ${dueDateClean ?: "N/A"}"

            // Setup Badge Status Colors
            val status = request.status?.uppercase() ?: "PENDING"
            statusBadge.text = status
            
            val statusColorRes = when (status) {
                "APPROVED" -> R.color.status_approved
                "REJECTED" -> R.color.status_rejected
                "RETURNED" -> R.color.status_returned
                else -> R.color.status_pending
            }
            val statusBgColorRes = when (status) {
                "APPROVED" -> R.color.status_approved_bg
                "REJECTED" -> R.color.status_rejected_bg
                "RETURNED" -> R.color.status_returned_bg
                else -> R.color.status_pending_bg
            }
            statusBadge.setTextColor(ContextCompat.getColor(context, statusColorRes))
            statusBadge.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, statusBgColorRes)
            )

            // Setup notes box if comments exist
            val notes = request.rejectionNote
            if (!notes.isNullOrEmpty()) {
                noteCard.visibility = View.VISIBLE
                noteTextView.text = if (status == "REJECTED") "Rejection Reason: $notes" else "Admin Note: $notes"
                noteTextView.setTextColor(
                    ContextCompat.getColor(
                        context, 
                        if (status == "REJECTED") R.color.status_rejected else R.color.text_secondary
                    )
                )
            } else {
                noteCard.visibility = View.GONE
            }

            // Setup Role Access to Actions
            if (isAdmin) {
                userPanel.visibility = View.VISIBLE
                userNameView.text = "Requester: ${request.userName ?: "Unknown User"}"
                userEmailView.text = "Email: ${request.userEmail ?: "N/A"}"

                when (status) {
                    "PENDING" -> {
                        adminActions.visibility = View.VISIBLE
                        returnButton.visibility = View.GONE
                        approveButton.setOnClickListener { onApproveClick?.invoke(request) }
                        rejectButton.setOnClickListener { onRejectClick?.invoke(request) }
                    }
                    "APPROVED" -> {
                        adminActions.visibility = View.GONE
                        returnButton.visibility = View.VISIBLE
                        returnButton.setOnClickListener { onReturnClick?.invoke(request) }
                    }
                    else -> {
                        adminActions.visibility = View.GONE
                        returnButton.visibility = View.GONE
                    }
                }
            } else {
                userPanel.visibility = View.GONE
                adminActions.visibility = View.GONE
                returnButton.visibility = View.GONE
            }
        }
    }

    private fun formatDateTime(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val zoned = parseToManila(raw) ?: return raw
        return DATE_TIME_FORMATTER.format(zoned)
    }

    private fun formatDateOnly(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val zoned = parseToManila(raw) ?: return raw
        return DATE_FORMATTER.format(zoned)
    }

    private fun parseToManila(raw: String): ZonedDateTime? {
        return try {
            when {
                raw.contains("Z") || raw.contains("+") -> OffsetDateTime.parse(raw).toInstant().atZone(MANILA_ZONE)
                raw.contains("T") -> LocalDateTime.parse(raw).atZone(MANILA_ZONE)
                else -> LocalDate.parse(raw).atStartOfDay(MANILA_ZONE)
            }
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        private val MANILA_ZONE = ZoneId.of("Asia/Manila")
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.ENGLISH)
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
    }
}
