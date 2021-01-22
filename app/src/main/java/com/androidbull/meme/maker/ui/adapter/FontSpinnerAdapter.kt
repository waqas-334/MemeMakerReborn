package com.androidbull.meme.maker.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.helper.FONTS_SUB_FOLDER
import com.androidbull.meme.maker.helper.StorageHelper
import com.androidbull.meme.maker.model.CaptionFont


class FontSpinnerAdapter(
    context: Context,
    fonts: List<CaptionFont>,
) :
    ArrayAdapter<CaptionFont>(context, 0, fonts) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        val captionFont = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.item_font_spinner, parent, false)
        }

        captionFont?.let {
            val tvFontName = view!!.findViewById<TextView>(R.id.tvFontName)
            tvFontName.text = it.displayName

            try {
                if (captionFont.isAppProvidedFont) {
                    val typeFace =
                        Typeface.createFromAsset(context.assets, "$FONTS_SUB_FOLDER${it.Name}")
                    tvFontName.typeface = typeFace
                } else {
                    if (StorageHelper.isExternalStorageReadable()) {
                        val fontFilePath = StorageHelper.getFontsPrivateDir() + it.Name
                        tvFontName.typeface = Typeface.createFromFile(fontFilePath)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                tvFontName.text = view.context.getString(R.string.something_went_wrong)
            }
        }

        return view!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        val captionFont = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.item_font_spinner_dropdown_view, parent, false)
        }

        captionFont?.let {
            val tvFontName = view!!.findViewById<TextView>(R.id.tvFontName)
            tvFontName.text = it.displayName

            try {
                if (captionFont.isAppProvidedFont) {
                    val typeFace =
                        Typeface.createFromAsset(context.assets, "$FONTS_SUB_FOLDER${it.Name}")
                    tvFontName.typeface = typeFace
                } else {
                    if (StorageHelper.isExternalStorageReadable()) {
                        val fontFilePath = StorageHelper.getFontsPrivateDir() + it.Name
                        tvFontName.typeface = Typeface.createFromFile(fontFilePath)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                tvFontName.text = view.context.getString(R.string.something_went_wrong)
            }
        }

        return view!!
    }
}