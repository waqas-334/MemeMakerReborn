package com.androidbull.meme.maker.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.helper.CaptionSettingsChangeListener
import com.androidbull.meme.maker.helper.SettingsManager
import com.androidbull.meme.maker.model.CaptionSetting
import com.androidbull.meme.maker.model.getDefaultCaptionSettings
import com.androidbull.meme.maker.ui.adapter.CaptionColorPagerAdapter
import com.androidbull.meme.maker.ui.fragments.CaptionStrokeColorFragment
import com.androidbull.meme.maker.ui.fragments.CaptionTextColorFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class CaptionColorDialog : DialogFragment(), TabLayout.OnTabSelectedListener, View.OnClickListener {

    private var captionSettingsChangeListener: CaptionSettingsChangeListener? = null

    private lateinit var btnSelect: Button
    private lateinit var btnCancel: Button
    private lateinit var tlCaptionColor: TabLayout
    private lateinit var vpCaptionColor: ViewPager2
    private lateinit var captionColorPagerAdapter: CaptionColorPagerAdapter
    private var dialogView: View? = null
    private lateinit var captionSetting: CaptionSetting

    private lateinit var vRecentlyUsedColorOne: View
    private lateinit var vRecentlyUsedColorTwo: View
    private lateinit var vRecentlyUsedColorThree: View
    private lateinit var vRecentlyUsedColorFour: View
    private var recentlyUsedColors: MutableList<String>? = null

    private val fragments =
        listOf(
            CaptionTextColorFragment(),
            CaptionStrokeColorFragment(),
        )

    companion object {
        @JvmStatic
        fun newInstance() = CaptionColorDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_color_dialog, null)
        return MaterialAlertDialogBuilder(
            requireContext()
        )
            .setView(dialogView)
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

        if (captionSetting == null)
            captionSetting = getDefaultCaptionSettings()

        initUi(view)
        initActions()
        setPagerAdapter()

        getSetRecentlyUsedColors()
    }

    //TODO init defaults in first run to avoid infinite loop on error
    private fun getSetRecentlyUsedColors() {
        recentlyUsedColors = SettingsManager.getRecentlyUsedColors()
        recentlyUsedColors?.let {
            if (it.isNotEmpty()) {
                vRecentlyUsedColorOne.setBackgroundColor(Color.parseColor(it[0]))
                vRecentlyUsedColorTwo.setBackgroundColor(Color.parseColor(it[1]))
                vRecentlyUsedColorThree.setBackgroundColor(Color.parseColor(it[2]))
                vRecentlyUsedColorFour.setBackgroundColor(Color.parseColor(it[3]))
            } else {
                SettingsManager.saveDefaultRecentlyUsedColors()
                getSetRecentlyUsedColors()
            }
        }
    }

    private fun initUi(view: View) {
        tlCaptionColor = view.findViewById(R.id.tlCaptionColor)
        vpCaptionColor = view.findViewById(R.id.vpCaptionColor)
        btnSelect = view.findViewById(R.id.btnSelect)
        btnCancel = view.findViewById(R.id.btnCancel)

        vRecentlyUsedColorOne = view.findViewById(R.id.vRecentlyUsedColorOne)
        vRecentlyUsedColorTwo = view.findViewById(R.id.vRecentlyUsedColorTwo)
        vRecentlyUsedColorThree = view.findViewById(R.id.vRecentlyUsedColorThree)
        vRecentlyUsedColorFour = view.findViewById(R.id.vRecentlyUsedColorFour)
    }


    private fun initActions() {
        tlCaptionColor.addOnTabSelectedListener(this)

        btnSelect.setOnClickListener {
            childFragmentManager.fragments.forEach {
                if (it is CaptionTextColorFragment) {
                    captionSetting.textColor = it.getSelectedColor()
                } else if (it is CaptionStrokeColorFragment) {
                    captionSetting.strokeColor = it.getSelectedColor()
                }
            }
            saveAsRecentlyUsedColor()
            captionSettingsChangeListener?.invoke(captionSetting)
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        vRecentlyUsedColorOne.setOnClickListener(this)
        vRecentlyUsedColorTwo.setOnClickListener(this)
        vRecentlyUsedColorThree.setOnClickListener(this)
        vRecentlyUsedColorFour.setOnClickListener(this)
    }

    private fun saveAsRecentlyUsedColor() {
        recentlyUsedColors?.let {
            if (it.isNotEmpty()) {
                if (!it.contains(captionSetting.textColor)) {
                    it.removeFirst()
                    it.add(captionSetting.textColor)
                }
                if (!it.contains(captionSetting.strokeColor)) {
                    it.removeFirst()
                    it.add(captionSetting.strokeColor)
                }
                SettingsManager.saveRecentlyUsedColors(it)
            }
        }
    }


    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                R.id.vRecentlyUsedColorOne,
                R.id.vRecentlyUsedColorTwo,
                R.id.vRecentlyUsedColorThree,
                R.id.vRecentlyUsedColorFour,
                -> {
                    setColor(v)
                }
            }
        }
    }

    private fun setColor(view: View) {
        val viewBackgroundDrawable = view.background
        if (viewBackgroundDrawable is ColorDrawable) {
            val viewColor: ColorDrawable = viewBackgroundDrawable
            val colorId: Int = viewColor.color
            fragments[tlCaptionColor.selectedTabPosition].setColor(colorId)
        }
    }


    private fun setPagerAdapter() {

        fragments.forEach {
            it.setCaptionSettings(captionSetting)
        }
        captionColorPagerAdapter = CaptionColorPagerAdapter(
            this,
            fragments
        )
        vpCaptionColor.adapter = captionColorPagerAdapter

        vpCaptionColor.isUserInputEnabled = false

    }


    override fun onDestroyView() {
        dialogView = null
        super.onDestroyView()
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.let {
            vpCaptionColor.currentItem = it.position
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }


    fun setCaptionSettings(captionSetting: CaptionSetting) {
        this.captionSetting = captionSetting
    }

    fun setCaptionSettingsChangeListener(captionSettingsChangeListener: CaptionSettingsChangeListener) {
        this.captionSettingsChangeListener = captionSettingsChangeListener
    }
}
