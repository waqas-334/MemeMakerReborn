package com.androidbull.meme.maker.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.data.repository.RoomFontRepository
import com.androidbull.meme.maker.helper.*
import com.androidbull.meme.maker.model.CaptionFont
import com.androidbull.meme.maker.ui.adapter.CustomFontAdapter
import com.androidbull.meme.maker.ui.fragments.REQUEST_DOCUMENT_PROVIDER
import com.androidbull.meme.maker.ui.interfaces.OnCustomFontItemClickListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.File


class CustomFontActivity : BaseActivity(), OnCustomFontItemClickListener {

    private lateinit var tbCustomFont: MaterialToolbar
    private lateinit var rvCustomFont: RecyclerView
    private lateinit var customFontAdapter: CustomFontAdapter
    private var customFonts = mutableListOf<CaptionFont>()
    private lateinit var eFabCustomFont: ExtendedFloatingActionButton
    private val fontRepository = RoomFontRepository()
    private lateinit var groupEmptyView: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_font)

        initUi()
        initToolbar()
        initActions()
        customFonts = getCustomFonts()
        initCustomFontAdapter()
        initCustomFontRecyclerView()
    }


    private fun initUi() {
        tbCustomFont = findViewById(R.id.tbCustomFont)
        eFabCustomFont = findViewById(R.id.eFabCustomFont)
        rvCustomFont = findViewById(R.id.rvCustomFont)
        groupEmptyView = findViewById(R.id.groupEmptyView)
    }

    private fun initActions() {
        eFabCustomFont.setOnClickListener {
            dispatchStorageAccessFrameworkIntent()
        }
    }

    private fun getCustomFonts() = fontRepository.getCustomFonts()


    private fun initToolbar() {
        setSupportActionBar(tbCustomFont)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }


    private fun initCustomFontAdapter() {
        customFontAdapter = CustomFontAdapter(customFonts, this@CustomFontActivity)
        customFontAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEmptyView()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmptyView()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmptyView()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                checkEmptyView()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                checkEmptyView()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                checkEmptyView()
            }
        })
    }

    private fun checkEmptyView() {
        groupEmptyView.visibility = if (customFontAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun initCustomFontRecyclerView() {
        with(rvCustomFont) {
            layoutManager = LinearLayoutManager(this@CustomFontActivity)
            adapter = customFontAdapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    //TODO move complete delete to font repo
    override fun onCustomFontDeleteClicked(font: CaptionFont, position: Int) {
        if (StorageHelper.isExternalStorageWriteable()) {
            try {
                val fontFile = File(StorageHelper.getFontsPrivateDir() + font.Name)

                if (fontRepository.delete(font) > 0) {
                    fontFile.delete()
                    customFontAdapter.updateAdapter(getCustomFonts())
                    Toast.makeText(
                        this,
                        getString(R.string.str_custom_font_deleted_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun dispatchStorageAccessFrameworkIntent() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                intent.type = "*/*"
                val mimetypes = arrayOf("font/*", "application/*")
                intent.putExtra(
                    Intent.EXTRA_MIME_TYPES, mimetypes
                )
            } else {
                intent.type = "application/*"
            }

            startActivityForResult(intent, REQUEST_DOCUMENT_PROVIDER)
        } catch (ex: ActivityNotFoundException) {
            ex.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == REQUEST_DOCUMENT_PROVIDER && resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    if (isValidFontFile(uri)) {
                        saveCustomFont(uri)
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.str_not_valid_font_file),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun isValidFontFile(uri: Uri): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val fileExtension = FileUtils.getExtension(uri.path)
            return !TextUtils.isEmpty(fileExtension) && (fileExtension.equals(
                SUPPORTED_FONT_EXTENSION_1,
                true
            ) || fileExtension.equals(
                SUPPORTED_FONT_EXTENSION_2,
                true
            ))
        } else {
            val mimeType = contentResolver.getType(uri)
            return mimeType != null && mimeType.contains("font", true)
        }
    }

    private fun saveCustomFont(fontUri: Uri) {
        try {
            if (StorageHelper.isExternalStorageWriteable()) {

                val fileDir = File(StorageHelper.getFontsPrivateDir())
                fileDir.mkdirs()

                val file =
                    FileUtils.generateFileName(FileUtils.getFileName(this, fontUri), fileDir)

                if (file != null && file.exists()) {
                    if (FileUtils.saveFileFromUriWithResult(this, fontUri, file.path)) {

                        val captionFont = CaptionFont(
                            System.currentTimeMillis(),
                            file.name,
                            file.nameWithoutExtension,
                            false
                        )
                        fontRepository.insert(captionFont)
                        customFontAdapter.updateAdapter(getCustomFonts())
                        Toast.makeText(
                            this,
                            getString(R.string.str_custom_font_added_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.str_external_storage_busy),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG)
                .show()
        }
    }
}