package com.pandey.bytehome.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pandey.bytehome.data.AppInfo
import com.pandey.bytehome.databinding.ItemAppBinding
import com.pandey.bytehome.util.IconCache

/**
 * Grid adapter used by both the app drawer and the home-screen favorites.
 * Icons come from [IconCache]; a failed icon falls back to the system icon
 * and never crashes binding.
 */
class AppGridAdapter(
    private val onClick: (AppInfo) -> Unit,
    private val onLongClick: (AppInfo) -> Boolean
) : RecyclerView.Adapter<AppGridAdapter.ViewHolder>() {

    private var items: List<AppInfo> = emptyList()

    val currentList: List<AppInfo> get() = items

    fun submit(list: List<AppInfo>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]
        val context = holder.itemView.context
        holder.binding.appLabel.text = app.label
        val icon = try {
            IconCache.get(context, app.packageName)
        } catch (e: Exception) {
            null
        } ?: try {
            context.getDrawable(android.R.drawable.sym_def_app_icon)
        } catch (e: Exception) {
            null
        }
        holder.binding.appIcon.setImageDrawable(icon)
        holder.binding.root.setOnClickListener { onClick(app) }
        holder.binding.root.setOnLongClickListener { onLongClick(app) }
    }
}
