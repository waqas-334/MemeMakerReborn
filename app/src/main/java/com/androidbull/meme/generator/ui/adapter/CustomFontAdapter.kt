package com.androidbull.meme.generator.ui.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.helper.StorageHelper
import com.androidbull.meme.generator.model.CaptionFont
import com.androidbull.meme.generator.ui.interfaces.OnCustomFontItemClickListener

class CustomFontAdapter(
    private var customFonts: List<CaptionFont>,
    private var onCustomFontItemClickListener: OnCustomFontItemClickListener
) :
    RecyclerView.Adapter<CustomFontAdapter.CustomFontViewHolder>() {

    fun updateAdapter(customFonts: List<CaptionFont>) {
        this.customFonts = customFonts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomFontViewHolder {
        return CustomFontViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_custom_font, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CustomFontViewHolder, position: Int) {
        holder.bind(customFonts[position])
    }

    override fun getItemCount() = customFonts.size

    inner class CustomFontViewHolder(customFontView: View) :
        RecyclerView.ViewHolder(customFontView) {

        private val tvCustomFont = customFontView.findViewById<TextView>(R.id.tvCustomFont)
        private val ibDeleteCustomFont =
            customFontView.findViewById<ImageButton>(R.id.ibDeleteCustomFont)

        init {
            ibDeleteCustomFont.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != NO_POSITION) {
                    onCustomFontItemClickListener.onCustomFontDeleteClicked(customFonts[position], position)
                }
            }
        }

        fun bind(customFont: CaptionFont) {

            tvCustomFont.text = customFont.displayName

            if (StorageHelper.isExternalStorageReadable()) {
                try {
                    val fontFilePath = StorageHelper.getFontsPrivateDir() + customFont.Name
                    tvCustomFont.typeface = Typeface.createFromFile(fontFilePath)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
}