package com.androidbull.meme.generator.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.data.repository.RoomFontRepository
import com.androidbull.meme.generator.helper.*
import com.androidbull.meme.generator.model.CaptionFont
import com.androidbull.meme.generator.ui.adapter.CustomFontAdapter
import com.androidbull.meme.generator.ui.fragments.REQUEST_DOCUMENT_PROVIDER
import com.androidbull.meme.generator.ui.interfaces.OnCustomFontItemClickListener
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
    }

    private fun initActions() {
        eFabCustomFont.setOnClickListener {
            dispatchStorageAccessFrameworkIntent()
        }
    }

    private fun getCustomFonts() = fontRepository.getCustomFonts()

    /* private fun getCustomFonts(): MutableList<CaptionFont> {
         val tempCustomFonts = mutableListOf<CaptionFont>()
         try {
             if (StorageHelper.isExternalStorageReadable()) {
                 val fontsPrivateDir = File(StorageHelper.getFontsPrivateDir())
                 if (fontsPrivateDir.exists() && fontsPrivateDir.isDirectory) {

                     fontsPrivateDir.listFiles()?.let { fontList ->
                         if (fontList.isNotEmpty()) {
                             fontList.forEach { fontFile ->
                                 val customFont =
                                     CaptionFont(
                                         System.currentTimeMillis(),
                                         fontFile.name,
                                         fontFile.nameWithoutExtension,
                                         isAppProvidedFont = false
                                     )
                                 tempCustomFonts.add(customFont)
                             }
                         }
                     }
                 }
             } else {
                 Toast.makeText(
                     this,
                     getString(R.string.str_external_storage_busy),
                     Toast.LENGTH_SHORT
                 )
                     .show()
             }
         } catch (np: NullPointerException) {
             np.printStackTrace()
         } finally {
             return tempCustomFonts
         }
     }*/

    private fun initToolbar() {
        setSupportActionBar(tbCustomFont)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }


    private fun initCustomFontAdapter() {
        customFontAdapter = CustomFontAdapter(customFonts, this@CustomFontActivity)
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

//                    if (hasPermissions(*STORAGE_PERMISSIONS, context = this)
//                    ) {
                        val fileExtension = FileUtils.getExtension(uri.path)
                        if (!TextUtils.isEmpty(fileExtension) && (fileExtension.equals(
                                SUPPORTED_FONT_EXTENSION_1,
                                true
                            ) || fileExtension.equals(
                                SUPPORTED_FONT_EXTENSION_2,
                                true
                            ))
                        ) {
                            saveCustomFont(uri)
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.str_not_valid_font_file),
                                Toast.LENGTH_LONG
                            ).show()
                        }
//                    } else {
//                        ActivityCompat.requestPermissions(
//                            this,
//                            STORAGE_PERMISSIONS,
//                            STORAGE_PERMISSION_REQUEST
//                        )
//                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            super.onActivityResult(requestCode, resultCode, data)
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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                ) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            STORAGE_PERMISSIONS[0]
                        )
                    ) {
                        Toast.makeText(
                            this,
                            getString(R.string.str_storage_permission_denied_for_custom_fonts),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.str_storage_permission_required_for_storage),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

}