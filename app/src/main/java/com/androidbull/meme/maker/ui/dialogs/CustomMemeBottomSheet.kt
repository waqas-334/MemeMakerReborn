package com.androidbull.meme.maker.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.androidbull.meme.maker.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CustomMemeBottomSheet : BottomSheetDialogFragment() {

    private var onCustomMemeOptionClickListener: OnCustomMemeOptionClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_custom_meme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnGallery).setOnClickListener {
            onCustomMemeOptionClickListener?.onGalleryClick()
        }

        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            onCustomMemeOptionClickListener?.onCameraClick()
        }
    }

    fun setCustomMemeOptionClickListener(onCustomMemeOptionClickListener: OnCustomMemeOptionClickListener) {
        this.onCustomMemeOptionClickListener = onCustomMemeOptionClickListener
    }

    interface OnCustomMemeOptionClickListener {
        fun onGalleryClick()
        fun onCameraClick()
    }
}

