package com.androidbull.meme.maker.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.data.repository.RoomFontRepository
import com.androidbull.meme.maker.helper.*
import com.androidbull.meme.maker.model.CaptionFont
import com.androidbull.meme.maker.model.CaptionSetting
import com.androidbull.meme.maker.model.getDefaultCaptionSettings
import com.androidbull.meme.maker.ui.adapter.FontSpinnerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.warkiz.widget.IndicatorSeekBar

class CaptionSettingsDialog : DialogFragment() {

    private var captionSettingsChangeListener: CaptionSettingsChangeListener? = null
    private lateinit var captionSetting: CaptionSetting
    private var dialogView: View? = null

    private lateinit var spnFont: Spinner
    private lateinit var seekBarFontSize: IndicatorSeekBar
    private lateinit var seekBarOutlineSize: IndicatorSeekBar
    private lateinit var seekBarMaxLines: IndicatorSeekBar
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button
    private var captionFonts = mutableListOf<CaptionFont>()
    private var fontSizeTickPosition = 0f

    companion object {
        @JvmStatic
        fun newInstance() = CaptionSettingsDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(
            R.layout.fragment_caption_text_settings,
            null
        )
        return MaterialAlertDialogBuilder(
            requireContext()
        )
            .setView(dialogView)
            .setCancelable(false)
            .create()
    }

    // Need to return the view here or onViewCreated won't be called by DialogFragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi(view)
        initActions()


        captionFonts = RoomFontRepository().getAll()
        if (captionSetting == null)
            captionSetting = getDefaultCaptionSettings()


        initFontSpinner()

        initSeekbars()

        getSetCaptionSettings()
    }

    private fun initUi(view: View) {
        spnFont = view.findViewById(R.id.spnFont)
        seekBarFontSize = view.findViewById(R.id.isbFontSize)
        seekBarOutlineSize = view.findViewById(R.id.isbOutlineSize)
        seekBarMaxLines = view.findViewById(R.id.isbMaxLines)
        btnOk = view.findViewById(R.id.btnOk)
        btnCancel = view.findViewById(R.id.btnCancel)
    }


    private fun initSeekbars() {

        context?.let { seekBarFontSize.customTickTexts(getFontSizeIndicatorTextList(it)) }
        seekBarFontSize.setIndicatorTextFormat("\${TICK_TEXT}")
        seekBarFontSize.min = 1f
        seekBarFontSize.max = seekBarFontSize.tickCount.toFloat()

        seekBarOutlineSize.customTickTexts(getOutlineSizeIndicatorTextList())
        seekBarOutlineSize.setIndicatorTextFormat("\${TICK_TEXT}")
        seekBarOutlineSize.min = 1f
        seekBarOutlineSize.max = seekBarOutlineSize.tickCount.toFloat()

        seekBarMaxLines.customTickTexts(getMaxLinesIndicatorTextList())
        seekBarMaxLines.setIndicatorTextFormat("\${TICK_TEXT}")
        seekBarMaxLines.min = 1f
        seekBarMaxLines.max = seekBarMaxLines.tickCount.toFloat()


    }

    private fun getSetCaptionSettings() {

        captionSetting.font?.let { captionFont ->
            captionFonts.forEachIndexed { index, font ->
                if (font.id == captionFont.id) {
                    spnFont.setSelection(index)
                }
            }
        }


        fontSizeTickPosition =
            getTickPositionFromFontSize(captionSetting.fontSize, requireContext())
        seekBarFontSize.setProgress(fontSizeTickPosition)
        seekBarOutlineSize.setProgress(getTickPositionFromOutlineSize(captionSetting.strokeWidth))
        seekBarMaxLines.setProgress(getTickPositionFromMaxLines(captionSetting.maxLines))

    }

    private fun initActions() {
        btnOk.setOnClickListener {
            val newTickPosition =
                getTickPositionFromFontSize(
                    getFontSizeFromSeekBar(seekBarFontSize.progress, requireContext()),
                    requireContext()
                )
            if (newTickPosition != fontSizeTickPosition) {
                captionSetting.fontSize =
                    getFontSizeFromSeekBar(seekBarFontSize.progress, requireContext())
            }
            captionSetting.strokeWidth =
                getOutlineSizeFromSeekBar(seekBarOutlineSize.progress)
            captionSetting.maxLines = getMaxLinesFromSeekBar(seekBarMaxLines.progress)
            captionSettingsChangeListener?.invoke(captionSetting)
            dismiss()
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initFontSpinner() {

        spnFont.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onFontSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val fontSpinnerAdapter: ArrayAdapter<CaptionFont> =
            FontSpinnerAdapter(requireContext(), captionFonts)
        spnFont.adapter = fontSpinnerAdapter
    }

    private fun onFontSelected(position: Int) {

        captionFonts.forEachIndexed { index, _ ->
            if (index == position) {
                captionSetting.font = captionFonts[position]
                return
            }
        }
    }

    fun setCaptionSettingsChangeListener(captionSettingsChangeListener: CaptionSettingsChangeListener) {
        this.captionSettingsChangeListener = captionSettingsChangeListener
    }

    fun setCaptionSettings(captionSetting: CaptionSetting) {
        this.captionSetting = captionSetting
    }

    override fun onDestroyView() {
        dialogView = null
        super.onDestroyView()
    }
}
