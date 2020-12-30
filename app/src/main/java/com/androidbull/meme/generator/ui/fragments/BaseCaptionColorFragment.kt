package com.androidbull.meme.generator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.helper.SettingsManager
import com.androidbull.meme.generator.model.CaptionSetting
import com.jaredrummler.android.colorpicker.ColorPanelView
import com.jaredrummler.android.colorpicker.ColorPickerView

abstract class BaseCaptionColorFragment : Fragment(), ColorPickerView.OnColorChangedListener {

    protected var captionSetting = SettingsManager.getCaptionSettings()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    protected abstract fun getLayoutId(): Int
    protected abstract fun getInitialColor(): Int

    private lateinit var colorPickerView: ColorPickerView
    private lateinit var newColorPanelView: ColorPanelView
    private lateinit var colorPanelView: ColorPanelView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialColor = getInitialColor()

        colorPickerView = view.findViewById<View>(R.id.cpv_color_picker_view) as ColorPickerView
        colorPanelView = view.findViewById<View>(R.id.cpv_color_panel_old) as ColorPanelView
        newColorPanelView = view.findViewById<View>(R.id.cpv_color_panel_new) as ColorPanelView

        /*(colorPanelView.parent as LinearLayout).setPadding(
            colorPickerView.paddingLeft, 0,
            colorPickerView.paddingRight, 0
        )
*/
        colorPickerView.setOnColorChangedListener(this)
        colorPickerView.setColor(initialColor, true)
        colorPanelView.color = initialColor

    }

    override fun onColorChanged(newColor: Int) {
        newColorPanelView.color = colorPickerView.color
    }

    fun getSelectedColor() = String.format("#%08X", -0x1 and (colorPickerView.color))

    fun setCaptionSettings(captionSetting: CaptionSetting) {
        this.captionSetting = captionSetting
    }

    fun setColor(color: Int) {
        colorPickerView.setColor(color, true)
        colorPanelView.color = color
    }
}