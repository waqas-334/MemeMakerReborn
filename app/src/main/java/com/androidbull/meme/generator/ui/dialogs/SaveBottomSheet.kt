package com.androidbull.meme.generator.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.androidbull.meme.generator.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SaveBottomSheet : BottomSheetDialogFragment() {

    private var onSaveOptionClickListener: OnSaveOptionClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_save_meme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnSaveToDevice).setOnClickListener {
            onSaveOptionClickListener?.onSaveToDeviceClick()
        }

        view.findViewById<Button>(R.id.btnSaveAsTemplate).setOnClickListener {
            onSaveOptionClickListener?.onSaveAsTemplateClick()
        }
        view.findViewById<Button>(R.id.btnUploadNewMeme).setOnClickListener {
            onSaveOptionClickListener?.onUploadNewMeme()
        }
    }

    fun setSaveOptionClickListener(onSaveOptionClickListener: OnSaveOptionClickListener) {
        this.onSaveOptionClickListener = onSaveOptionClickListener
    }

    interface OnSaveOptionClickListener {
        fun onSaveToDeviceClick()
        fun onSaveAsTemplateClick()
        fun onUploadNewMeme()   //TODO remove
    }
}

