package com.madappgang.architecture.recorder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.madappgang.architecture.recorder.helpers.FileManager.Companion.recordFormat
import kotlinx.android.synthetic.main.cell_item.view.*
import java.io.File


class FolderAdapter(private val basePath: String) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    private lateinit var clickListener: ItemClickListener
    private var dataSet: MutableList<File> = mutableListOf()
    private var currentPath: String = ""
    private var isNormalMode: Boolean = true
    private val fileManager = AppInstance.appInstance.fileManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cell_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = dataSet[position]
        holder.itemName.text = file.name.removeSuffix(recordFormat)
        holder.itemTypeImage.setImageResource(if (file.isDirectory) {
            R.drawable.ic_folder_open_white_24dp
        } else {
            R.drawable.ic_audiotrack_black_36dp
        })
        if (isNormalMode) {
            holder.itemView.setOnClickListener({
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(dataSet[adapterPosition])
                }
            })
            holder.itemFolderIndicator.visibility = if (file.isDirectory) {
                View.VISIBLE
            } else {
                View.GONE
            }
            holder.itemRemove.visibility = View.GONE
        } else {
            holder.itemView.setOnClickListener(null)
            holder.itemFolderIndicator.visibility = View.GONE
            holder.itemRemove.visibility = View.VISIBLE
            holder.itemRemove.setOnClickListener { removeFile(file) }
        }
    }

    fun setPathForAdapter(path: String) {
        currentPath = path
        updateListFiles()
    }

    fun prevPath(): String {
        if (currentPath == basePath) return basePath

        val folders: MutableList<String> = currentPath.split("/").map { it.trim() }.toMutableList()
        folders.removeAt(folders.size - 1)

        return folders.joinToString("/")
    }

    fun updateListFiles() {
        dataSet.clear()
        dataSet = fileManager.getListFiles(currentPath)
        notifyDataSetChanged()
    }

    fun getCurrentPath() = currentPath
    fun setCurrentPath(path: String) {
        currentPath = path
    }

    fun setNormalMode() {
        isNormalMode = true
        notifyDataSetChanged()
    }

    fun setRemoveMode() {
        isNormalMode = false
        notifyDataSetChanged()
    }

    private fun removeFile(file: File) {
        fileManager.removeFile(file)
        updateListFiles()
    }

    override fun getItemCount() = dataSet.size

    fun setupItemClickListener(listener: ItemClickListener) {
        clickListener = listener
    }

    interface ItemClickListener {
        fun onItemClick(file: File)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemTypeImage: ImageView = view.itemTypeImage
        val itemName: TextView = view.itemName
        val itemFolderIndicator: ImageView = view.itemFolderIndicator
        val itemRemove: ImageView = view.itemRemove
    }
}