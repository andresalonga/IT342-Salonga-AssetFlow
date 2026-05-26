package edu.cit.salonga.assetflow.features.assets.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.cit.salonga.assetflow.R
import edu.cit.salonga.assetflow.features.assets.model.AssetDto

class AssetAdapter(
    private var assets: List<AssetDto>,
    private val isAdmin: Boolean,
    private val onAssetClick: (AssetDto) -> Unit,
    private val onEditClick: ((AssetDto) -> Unit)? = null,
    private val onDeleteClick: ((AssetDto) -> Unit)? = null
) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    fun updateList(newList: List<AssetDto>) {
        assets = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asset, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets[position]
        holder.bind(asset)
    }

    override fun getItemCount(): Int = assets.size

    inner class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.assetName)
        private val categoryView: TextView = itemView.findViewById(R.id.assetCategory)
        private val serialView: TextView = itemView.findViewById(R.id.assetSerial)
        private val imageView: ImageView = itemView.findViewById(R.id.assetImage)
        private val statusBadge: TextView = itemView.findViewById(R.id.assetStatusBadge)
        private val adminPanel: LinearLayout = itemView.findViewById(R.id.adminActionPanel)
        private val editBtn: ImageButton = itemView.findViewById(R.id.editAssetButton)
        private val deleteBtn: ImageButton = itemView.findViewById(R.id.deleteAssetButton)

        fun bind(asset: AssetDto) {
            val context = itemView.context
            
            nameView.text = asset.name ?: "Unnamed Asset"
            categoryView.text = asset.category ?: "Uncategorized"
            serialView.text = "SN: ${asset.serialNumber ?: "N/A"}"

            // Bind status badge with colors matching design specs
            val status = asset.status?.uppercase() ?: "AVAILABLE"
            statusBadge.text = status
            
            val statusColorRes = when (status) {
                "AVAILABLE" -> R.color.status_available
                "BORROWED" -> R.color.status_borrowed
                "MAINTENANCE" -> R.color.status_maintenance
                else -> R.color.text_secondary
            }
            val statusBgColorRes = when (status) {
                "AVAILABLE" -> R.color.status_available_bg
                "BORROWED" -> R.color.status_borrowed_bg
                "MAINTENANCE" -> R.color.status_maintenance_bg
                else -> R.color.card_border
            }
            statusBadge.setTextColor(ContextCompat.getColor(context, statusColorRes))
            statusBadge.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, statusBgColorRes)
            )

            // Setup Glide with local host substitution to support emulator fetching
            val rawUrl = asset.imageUrl
            val finalUrl = rawUrl
                ?.replace("localhost", edu.cit.salonga.assetflow.network.ApiClient.BASE_IP)
                ?.replace("10.0.2.2", edu.cit.salonga.assetflow.network.ApiClient.BASE_IP)
            
            Glide.with(context)
                .load(finalUrl)
                .placeholder(R.drawable.bg_asset_placeholder)
                .error(R.drawable.bg_asset_placeholder)
                .centerCrop()
                .into(imageView)

            // Setup role-based access to controls
            adminPanel.visibility = View.GONE

            itemView.setOnClickListener { onAssetClick(asset) }
        }
    }
}
