package com.androidbull.meme.generator.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.data.repository.RoomFontRepository
import com.androidbull.meme.generator.data.repository.RoomMemeRepository
import com.androidbull.meme.generator.helper.*
import com.androidbull.meme.generator.helper.FileUtils
import com.androidbull.meme.generator.model.*
import com.androidbull.meme.generator.ui.dialogs.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.developer.kalert.KAlertDialog
import com.facebook.ads.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yalantis.ucrop.UCrop
import ja.burhanrashid52.photoeditor.*
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt


private const val MEME_GEN_ACTIVITY_BANNER_AD_ID = "580015096002786_667246710612957"
private const val MEME_GEN_ACTIVITY_INTERSTITIAL_AD_ID = "580015096002786_667245823946379"
private const val TAG = "MemeGeneratorActivity"

class MemeGeneratorActivity : AdsActivity(), OnPhotoEditorListener {

    private lateinit var photoEditorView: PhotoEditorView
    private lateinit var photoEditor: PhotoEditor

    private lateinit var bannerAdContainer: ViewGroup

    private lateinit var btnAddText: Button
    private lateinit var btnUndo: Button
    private lateinit var btnRedo: Button
    private lateinit var btnSave: Button
    private lateinit var btnShare: Button

    private lateinit var etEditText: EditText
    private lateinit var ibEditTextSettings: ImageButton
    private lateinit var ibEditTextColorDialog: ImageButton
    private lateinit var ibDeleteText: ImageButton

    private lateinit var tbMemeCreator: MaterialToolbar

    private lateinit var groupEditText: Group

    private var viewBeingEdited: View? = null

    private var mSaveImageUri: Uri? = null

    private var meme: Meme2? = null

    private val memeRepository = RoomMemeRepository()
    private val fontRepository = RoomFontRepository()

    private var editTextCaptionSetting: CaptionSetting = getDefaultCaptionSettings()
    private var isSomethingEdited = false

    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meme_creator)

        initUi()
        initActions()
        initToolbar()
        initPhotoEditor()
        try {
            handleIntentImage()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun initToolbar() {
        setSupportActionBar(tbMemeCreator)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun initUi() {
        photoEditorView = findViewById(R.id.photoEditorView)

        btnAddText = findViewById(R.id.btnAddText)
        btnUndo = findViewById(R.id.btnUndo)
        btnRedo = findViewById(R.id.btnRedo)
        btnSave = findViewById(R.id.btnSave)
        btnShare = findViewById(R.id.btnShare)

        etEditText = findViewById(R.id.etEditText)
        ibEditTextSettings = findViewById(R.id.ibEditTextSettings)
        ibEditTextColorDialog = findViewById(R.id.ibEditTextColorDialog)
        ibDeleteText = findViewById(R.id.ibDeleteText)

        groupEditText = findViewById(R.id.groupEditText)
        tbMemeCreator = findViewById(R.id.tbMemeCreator)

        bannerAdContainer = findViewById(R.id.flBannerAdContainer)

    }

    private fun getSetUndoRedoUi() {
        btnUndo.isEnabled = photoEditor.addedViewsCount > 0
        btnRedo.isEnabled = photoEditor.redoViewsCount > 0
    }

    private fun initActions() {

        btnAddText.setOnClickListener {
            editTextCaptionSetting.positionX = CAPTION_DEFAULT_POSITION
            editTextCaptionSetting.positionY = CAPTION_DEFAULT_POSITION
            addCaption("", editTextCaptionSetting)
        }

        btnUndo.setOnClickListener {
            photoEditor.undo()
            getSetUndoRedoUi()
        }

        btnRedo.setOnClickListener {
            photoEditor.redo()
            getSetUndoRedoUi()
        }

        btnSave.setOnClickListener {
            saveMeme()
        }
        btnShare.setOnClickListener {
            shareMeme()
        }

        ibEditTextSettings.setOnClickListener {
            val captionSettingsDialog = CaptionSettingsDialog.newInstance()
            captionSettingsDialog.setCaptionSettings(editTextCaptionSetting)
            captionSettingsDialog.setCaptionSettingsChangeListener(::onEditCaptionSettingsChanged)
            captionSettingsDialog.show(supportFragmentManager, FRAGMENT_CAPTION_SETTINGS_TAG)
        }

        ibEditTextColorDialog.setOnClickListener {
            val captionColorDialog = CaptionColorDialog.newInstance()
            captionColorDialog.setCaptionSettings(editTextCaptionSetting)
            captionColorDialog.setCaptionSettingsChangeListener(::onEditCaptionSettingsChanged)
            captionColorDialog.show(supportFragmentManager, FRAGMENT_CAPTION_COLOR_TAG)
        }

        ibDeleteText.setOnClickListener {
            viewBeingEdited?.let {
                photoEditor.removeView(it)
            }
        }

        etEditText.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                finishEditingText()
                true
            } else false
        }

        etEditText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                editText(etEditText.text.toString(), editTextCaptionSetting)
            }
        })


        photoEditorView.setOnClickListener {
            hideCaptionFrame()

            if (etEditText.visibility == VISIBLE) {
                finishEditingText()
                hideEditCaptionUi()
            }
        }
    }

    private fun saveMeme() {
        if (hasPermissions(*STORAGE_PERMISSIONS, context = this)) {
            showSaveBottomSheet()
        } else {
            ActivityCompat.requestPermissions(
                this,
                STORAGE_PERMISSIONS,
                STORAGE_PERMISSION_REQUEST
            )
        }
    }

    override fun onPremiumMemberShipLost() {
        AdsManager.loadAndShowBannerAd(
            adId = MEME_GEN_ACTIVITY_BANNER_AD_ID,
            adContainer = bannerAdContainer
        )
        loadInterstitialAd()
    }

    override fun onPremiumMemberShipAcquired() {
        AdsManager.removeAds()
    }

    private fun loadInterstitialAd() {
        AdSettings.addTestDevice(ADS_TEST_ID)
        interstitialAd = InterstitialAd(this, MEME_GEN_ACTIVITY_INTERSTITIAL_AD_ID)
        val interstitialAdListener = object : InterstitialAdListener {
            override fun onAdClicked(ad: Ad?) {
            }

            override fun onError(ad: Ad?, adError: AdError?) {
                Log.d(TAG, "onError: ${adError?.errorMessage}")
            }

            override fun onAdLoaded(ad: Ad?) {
            }

            override fun onLoggingImpression(ad: Ad?) {
            }

            override fun onInterstitialDisplayed(ad: Ad?) {
            }

            override fun onInterstitialDismissed(ad: Ad?) {
                loadInterstitialAd()
            }
        }

        interstitialAd?.let {
            it.loadAd(
                it.buildLoadAdConfig()
                    .withAdListener(interstitialAdListener)
                    .build()
            )
        }
    }

    private fun showInterstitialAd() {
        if (interstitialAd != null) {
            if (!interstitialAd!!.isAdLoaded) {
                return
            } else if (interstitialAd!!.isAdInvalidated) {
                loadInterstitialAd()
            } else {
                interstitialAd!!.show()
            }
        } else {
            loadInterstitialAd()
        }
    }


    override fun onDestroy() {
        AdsManager.removeAd(MEME_GEN_ACTIVITY_BANNER_AD_ID)
        interstitialAd?.destroy()
        super.onDestroy()
    }


    private fun finishEditingText() {
        if (!TextUtils.isEmpty(etEditText.text.toString())) {
            editText(etEditText.text.toString(), editTextCaptionSetting)
        } else {
            viewBeingEdited?.let {
                photoEditor.removeViewWithoutHistory(it)
            }
        }
        hideEditCaptionUi()
    }

    private fun handleIntentImage() {
        val intent = intent
        intent?.let { it ->
            val intentType = it.type
            val extras = it.extras

            if (intentType != null && intentType.startsWith("image/")) {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(photoEditorView.source)
                    return
                }
            } else if (extras != null && !extras.isEmpty) {

                val memeId = extras.getLong(BUNDLE_EXTRA_MEME_ID)
                if (memeId > 0) {
                    loadMeme(memeId)
                    return
                }

                val customCameraMemePath = extras.getString("customCameraMemePath")
                if (!TextUtils.isEmpty(customCameraMemePath)) {
                    Glide.with(this)
                        .load(customCameraMemePath)
                        .into(photoEditorView.source)
                    return
                }

                val customGalleryMemePath = extras.getString("customGalleryMemePath")
                if (!TextUtils.isEmpty(customGalleryMemePath)) {
                    val uri = Uri.parse(customGalleryMemePath)
                    Glide.with(this)
                        .load(uri)
                        .into(photoEditorView.source)
                    return
                }

                val saveMemePath = extras.getString("savedMemePath")
                if (!TextUtils.isEmpty(saveMemePath)) {
                    mSaveImageUri = Uri.parse(saveMemePath)
                    Glide.with(this)
                        .load(Uri.parse(saveMemePath))
                        .into(photoEditorView.source)
                    return
                }
            }
            showSomeThingWentWrongDialog()
        }
    }

    private fun showSomeThingWentWrongDialog() {
        val errorDialog = ErrorDialog.newInstance(getString(R.string.something_went_wrong))
        errorDialog.show(supportFragmentManager, FRAGMENT_ERROR_DIALOG_TAG)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_meme_generator_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_crop -> {
                //TODO if picture is null don't crop
                saveFileTemporarilyAndCrop()
                return true
            }
            R.id.mi_rotate -> {
                val rotatedBitmap =
                    photoEditor.getClockWiseRotatedImage((photoEditorView.source.drawable as BitmapDrawable).bitmap)

                photoEditor.clearAllViews()
                photoEditorView.layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                    constrainedHeight = true
                }
                val parentLayout = findViewById<ConstraintLayout>(R.id.parentLayout)
                val constraintSet = ConstraintSet()
                constraintSet.clone(parentLayout)
                constraintSet.constrainDefaultHeight(
                    photoEditorView.id,
                    ConstraintSet.MATCH_CONSTRAINT_SPREAD
                )
                constraintSet.constrainDefaultWidth(
                    photoEditorView.id,
                    ConstraintSet.MATCH_CONSTRAINT_SPREAD
                )

                constraintSet.connect(
                    R.id.photoEditorView,
                    ConstraintSet.START,
                    R.id.parentLayout,
                    ConstraintSet.START,
                    0
                )
                constraintSet.connect(
                    R.id.photoEditorView,
                    ConstraintSet.END,
                    R.id.parentLayout,
                    ConstraintSet.END,
                    0
                )
                constraintSet.connect(
                    R.id.photoEditorView,
                    ConstraintSet.BOTTOM,
                    R.id.btnAddText,
                    ConstraintSet.TOP,
                    0
                )
                constraintSet.connect(
                    R.id.photoEditorView,
                    ConstraintSet.TOP,
                    R.id.etEditText,
                    ConstraintSet.BOTTOM,
                    0
                )
                constraintSet.applyTo(parentLayout)

                photoEditorView.source.setImageBitmap(rotatedBitmap)
                photoEditorView.source.viewTreeObserver
                    .addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            photoEditorView.source.viewTreeObserver.removeOnGlobalLayoutListener(
                                this
                            )
                            val finalHeight = photoEditorView.source.height
                            val finalWidth = photoEditorView.source.width
                            photoEditorView.layoutParams.width = finalWidth
                            photoEditorView.layoutParams.height = finalHeight

                            meme?.let {
                                addMemeCaptions(it)
                            }
                        }
                    })

                return true
            }
            R.id.mi_flip_horizontal -> {
                photoEditor.flipHorizontal()
                return true
            }
            R.id.mi_flip_vertical -> {
                photoEditor.flipVertical()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun loadMeme(memeId: Long) {

        meme = memeRepository.getMemeWithCaptionSets(memeId)
        meme?.let { meme ->

            supportActionBar?.let {
                if (!TextUtils.isEmpty(meme.imageTitle)) {
                    it.title = meme.imageTitle
                } else {
                    it.title = meme.imageName
                }
            }
            if (meme.isCreatedByUser) { // if meme is a template saved by user
                loadSavedMemeTemplateFromStorage(meme)
            } else if (StorageHelper.isExternalStorageReadable()) {    // check if meme exists in disk cache // downloadAllMemes
                loadMemeFromDiskCacheOrServer(meme)
            }
        }
    }

    private fun loadMemeFromDiskCacheOrServer(meme: Meme2) {
        Glide.with(this)
            .load(File(StorageHelper.getMemesPrivateDir() + meme.imageName).absolutePath)
            .listener(object : RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    photoEditorView.source.viewTreeObserver
                        .addOnGlobalLayoutListener(object :
                            ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                photoEditorView.source.viewTreeObserver.removeOnGlobalLayoutListener(
                                    this
                                )
                                val finalHeight = photoEditorView.source.height
                                val finalWidth = photoEditorView.source.width
                                photoEditorView.layoutParams.width = finalWidth
                                photoEditorView.layoutParams.height = finalHeight

                                addMemeCaptions(meme)
                            }
                        })
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Handler(mainLooper).post {
                        loadMemeFromServer(meme)
                    }
                    return false
                }
            })
            .into(photoEditorView.source)
    }


    private fun loadMemeFromServer(meme: Meme2) {
        val memeDownloadDialog: MemeDownloadDialog? = MemeDownloadDialog.newInstance()

        //TODO dialog fragment
        val retryDialog = MaterialAlertDialogBuilder(this@MemeGeneratorActivity)
            .setMessage(resources.getString(R.string.something_went_wrong))
            .setCancelable(false)
            .setNegativeButton(resources.getString(R.string.str_leave)) { _, _ ->
                finish()
            }
            .setPositiveButton(resources.getString(R.string.str_retry)) { _, _ ->
                loadMemeFromServer(meme)
            }.create()


        if (retryDialog.isShowing)
            retryDialog.dismiss()
        memeDownloadDialog?.show(
            supportFragmentManager,
            FRAGMENT_MEME_DOWNLOAD_TAG
        )
        Glide.with(this)
            .load(MEME_SERVER_BASE_URL + meme.imageName)
            .listener(object : RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (retryDialog.isShowing)
                        retryDialog.dismiss()
                    memeDownloadDialog?.dismiss()

                    photoEditorView.source.viewTreeObserver
                        .addOnGlobalLayoutListener(object :
                            ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                photoEditorView.source.viewTreeObserver.removeOnGlobalLayoutListener(
                                    this
                                )
                                val finalHeight = photoEditorView.source.height
                                val finalWidth = photoEditorView.source.width
                                photoEditorView.layoutParams.width = finalWidth
                                photoEditorView.layoutParams.height = finalHeight

                                addMemeCaptions(meme)
                            }
                        })
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {

                    e?.printStackTrace()
                    retryDialog.show()
                    memeDownloadDialog?.dismiss()
                    return false
                }

            })
            .into(photoEditorView.source)
    }


    private fun loadSavedMemeTemplateFromStorage(meme: Meme2) {
        if (StorageHelper.isExternalStorageReadable()) {

            Glide.with(this)
                .load(File(StorageHelper.getTemplatesPrivateDir() + meme.imageName).absolutePath)
                .listener(object : RequestListener<Drawable> {

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        photoEditorView.source.viewTreeObserver
                            .addOnGlobalLayoutListener(object :
                                ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    photoEditorView.source.viewTreeObserver.removeOnGlobalLayoutListener(
                                        this
                                    )
                                    val finalHeight = photoEditorView.source.height
                                    val finalWidth = photoEditorView.source.width
                                    photoEditorView.layoutParams.width = finalWidth
                                    photoEditorView.layoutParams.height = finalHeight

                                    addMemeCaptions(meme)
                                }
                            })
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                })
                .into(photoEditorView.source)


        } else {
            Toast.makeText(
                this,
                getString(R.string.str_external_storage_busy),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // scale fontSize and position X,Y for new memes
    private fun addMemeCaptions(meme: Meme2) {
        if (!meme.isCreatedByUser && !meme.isModernMeme) {  // default memes (classic only)
            addClassicMemeCaptions()
        } else if (meme.isCreatedByUser && meme.isModernMeme) { // user meme templates
            addMemeTemplateCaptions(meme.captionSets)
        } else if (!meme.isCreatedByUser && meme.id > 2000) {   // new memes
            addNewMemeCaptions(meme.captionSets)
        }
    }

    private fun addClassicMemeCaptions() {

        val classicMemeCaptionSetting = getClassicMemeCaptionSetting()

        // Classic meme font size is 10 percent of meme width
        // (photoEditorView.source.intrinsicWidth / resources.displayMetrics.density) * 0.10 (gives us font size in sp)
        val fontSizeRelativeToMemeWidth =
            ((photoEditorView.source.width / resources.displayMetrics.scaledDensity) * 0.10).roundToInt()

        classicMemeCaptionSetting.fontSize = fontSizeRelativeToMemeWidth

        classicMemeCaptionSetting.positionY = CAPTION_TOP_POSITION
        addCaption(
            getString(R.string.str_default_caption),
            classicMemeCaptionSetting
        )
        classicMemeCaptionSetting.positionY = CAPTION_BOTTOM_POSITION
        addCaption(
            getString(R.string.str_default_caption),
            classicMemeCaptionSetting
        )

        classicMemeCaptionSetting.positionX = CAPTION_DEFAULT_POSITION
        classicMemeCaptionSetting.positionY = CAPTION_DEFAULT_POSITION
        editTextCaptionSetting = classicMemeCaptionSetting

    }

    private fun addMemeTemplateCaptions(captionSets: MutableList<CaptionSet2>) {
        if (captionSets.isNotEmpty()) {

            captionSets[0].captions.forEach { caption ->

                val modernMemeCaptionSetting = getDefaultCaptionSettings()

                caption.fontType?.let { fontId ->
                    modernMemeCaptionSetting.font = fontRepository.getById(fontId)
                }
                caption.strokeWidth?.let {
                    modernMemeCaptionSetting.strokeWidth = it
                }
                caption.fontSize?.let { fontSize ->
                    modernMemeCaptionSetting.fontSize = fontSize
                }
                caption.textColor?.let {
                    modernMemeCaptionSetting.textColor = it
                }
                caption.strokeColor?.let {
                    modernMemeCaptionSetting.strokeColor = it
                }
                caption.maxLines?.let {
                    modernMemeCaptionSetting.maxLines = it
                }

                caption.positionX?.let {
                    modernMemeCaptionSetting.positionX = it
                }

                caption.positionY?.let {
                    modernMemeCaptionSetting.positionY = it
                }
                addCaption(caption.text!!, modernMemeCaptionSetting)
            }
        }
    }

    private fun addNewMemeCaptions(captionSets: MutableList<CaptionSet2>) {
        if (captionSets.isNotEmpty()) {

            captionSets[0].captions.forEach { caption ->

                val modernMemeCaptionSetting = getDefaultCaptionSettings()

                caption.fontType?.let { fontId ->
                    modernMemeCaptionSetting.font = fontRepository.getById(fontId)
                }
                caption.strokeWidth?.let {
                    modernMemeCaptionSetting.strokeWidth = it
                }
                caption.fontSize?.let { fontSize ->
                    modernMemeCaptionSetting.fontSize =
                        getFontSizeForScaledMemeDimensions(fontSize)
                }
                caption.textColor?.let {
                    modernMemeCaptionSetting.textColor = it
                }
                caption.strokeColor?.let {
                    modernMemeCaptionSetting.strokeColor = it
                }
                caption.maxLines?.let {
                    modernMemeCaptionSetting.maxLines = it
                }

                caption.positionX?.let {
                    modernMemeCaptionSetting.positionX = getPositionXForScaledDimensions(it)
                }

                caption.positionY?.let {
                    modernMemeCaptionSetting.positionY = getPositionYForScaledDimensions(it)
                }
                addCaption(caption.text!!, modernMemeCaptionSetting)
            }
        }
    }

    private fun addCaption(captionText: String, captionSetting: CaptionSetting) {
        val captionWrapper = CaptionWrapper()
        captionWrapper.id = System.currentTimeMillis()
        captionWrapper.text = captionText
        captionWrapper.textColor = captionSetting.textColor
        captionWrapper.fontSize = captionSetting.fontSize
        captionWrapper.strokeWidth = captionSetting.strokeWidth
        captionWrapper.strokeColor = captionSetting.strokeColor
        captionWrapper.positionX = captionSetting.positionX
        captionWrapper.positionY = captionSetting.positionY
        captionWrapper.maxLines = captionSetting.maxLines

        captionSetting.font?.let { captionFont ->
            captionWrapper.fontType = captionFont.id
            if (captionFont.isAppProvidedFont) {
                try {
                    val fontTypeFace =
                        Typeface.createFromAsset(
                            assets,
                            "$FONTS_SUB_FOLDER${captionFont.Name}"
                        )
                    captionWrapper.fontTypeFace = fontTypeFace
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else {
                if (StorageHelper.isExternalStorageReadable()) {
                    try {
                        val fontFilePath =
                            StorageHelper.getFontsPrivateDir() + captionFont.Name
                        captionWrapper.fontTypeFace = Typeface.createFromFile(fontFilePath)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
        photoEditor.addText(captionWrapper)

    }


    private fun editText(inputText: String, captionSetting: CaptionSetting) {

        viewBeingEdited?.let {

            val captionWrapper = CaptionWrapper()
            captionWrapper.id = System.currentTimeMillis()
            captionWrapper.text = inputText
            captionWrapper.textColor = captionSetting.textColor
            captionWrapper.fontSize = captionSetting.fontSize
            captionWrapper.strokeWidth = captionSetting.strokeWidth
            captionWrapper.strokeColor = captionSetting.strokeColor
            captionWrapper.positionX = captionSetting.positionX
            captionWrapper.positionY = captionSetting.positionY
            captionWrapper.maxLines = captionSetting.maxLines
            captionSetting.font?.let { captionFont ->
                captionWrapper.fontType = captionFont.id
                if (captionFont.isAppProvidedFont) {
                    try {
                        val fontTypeFace =
                            Typeface.createFromAsset(
                                assets,
                                "$FONTS_SUB_FOLDER${captionFont.Name}"
                            )
                        captionWrapper.fontTypeFace = fontTypeFace
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                } else {
                    if (StorageHelper.isExternalStorageReadable()) {
                        try {
                            val fontFilePath =
                                StorageHelper.getFontsPrivateDir() + captionFont.Name
                            captionWrapper.fontTypeFace =
                                Typeface.createFromFile(fontFilePath)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }

            photoEditor.editText(it, inputText, captionWrapper)
            isSomethingEdited = true
        }
    }


    private fun hideEditCaptionUi() {
        groupEditText.visibility = INVISIBLE
        hideSoftKeyboard(etEditText)
    }

    private fun showEditCaptionUi() {
        groupEditText.visibility = VISIBLE
        etEditText.requestFocus()
        etEditText.setSelection(etEditText.text.length, 0)
        showSoftKeyboard(etEditText)
    }

    private fun hideCaptionFrame() {
        photoEditor.clearHelperBox()
    }

    private fun crop(uri: Uri) {
        val options = UCrop.Options()
        options.setFreeStyleCropEnabled(true)
        options.setToolbarTitle(getString(R.string.str_crop))

        val destinationFileName = "CROPPED_IMAGE.png"
        val uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, destinationFileName)))

        uCrop.withOptions(options)
        uCrop.start(this)
    }


    private fun showSaveBottomSheet() {
        hideCaptionFrame()
        val saveBottomSheet = SaveBottomSheet()
        saveBottomSheet.setSaveOptionClickListener(object :
            SaveBottomSheet.OnSaveOptionClickListener {
            override fun onSaveToDeviceClick() {
                saveMemeToDevice()
                saveBottomSheet.dismiss()
            }

            override fun onSaveAsTemplateClick() {
                if (isMemeTemplateAlreadyExist()) { // template which is created by user
                    showOverwriteTemplateDialog()
                } else {
                    saveMemeAsTemplate()
                }
                saveBottomSheet.dismiss()
            }

            override fun onUploadNewMeme() {

                val memeNameInputDialog = MemeNameInputDialog.newInstance()
                memeNameInputDialog.isCancelable = false
                memeNameInputDialog.setOnSaveClickListener { memeName ->
                    uploadMeme(getCurrentMeme(memeName))
                }
                memeNameInputDialog.show(
                    supportFragmentManager,
                    FRAGMENT_MEME_NAME_INPUT_DIALOG_TAG
                )
                saveBottomSheet.dismiss()
            }
        })
        saveBottomSheet.show(supportFragmentManager, FRAGMENT_SAVE_BOTTOM_SHEET_TAG)
    }

    private fun showOverwriteTemplateDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.str_save_meme_to_share))
            .setMessage(getString(R.string.str_meme_template_overwrite_message))
            .setNegativeButton(getString(R.string.str_later)) { _, i -> }
            .setNegativeButton(getString(R.string.str_overwrite)) { _, i ->
//                updateMemeTemplate()
            }
            .setPositiveButton(getString(R.string.str_save_as_new_template)) { _, i ->
                saveMemeAsTemplate()
            }
            .show()
    }

    private fun isMemeTemplateAlreadyExist(): Boolean {
        meme?.let {
            if (it.isCreatedByUser)
                return true
        }
        return false
    }

    // TODO cancel task on back
    private fun uploadMeme(currentMeme: Meme2) {
        val alertDialog = KAlertDialog(this, KAlertDialog.PROGRESS_TYPE)
        alertDialog.setCancelable(false)
        alertDialog.setTitleText(getString(R.string.str_uploading))
            .show()

        val db = Firebase.firestore
        db.collection(NEW_MEMES_FIRESTORE_COLLECTION).document(currentMeme.imageName)
            .set(currentMeme)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    alertDialog.changeAlertType(KAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = getString(R.string.str_uploaded_successfully)
                    alertDialog.setCancelable(true)
                    alertDialog.setCanceledOnTouchOutside(true)
                    Handler(mainLooper).postDelayed({
                        alertDialog.let {  // can be null
                            if (it.isShowing) {
                                it.dismissWithAnimation()
                            }
                        }
                    }, 1500)
                } else {
                    alertDialog.titleText = getString(R.string.str_sorry_try_again)
                    alertDialog.changeAlertType(KAlertDialog.ERROR_TYPE)
                    alertDialog.setCancelable(true)
                    alertDialog.setCanceledOnTouchOutside(true)
                }
            }
    }

    private fun getCurrentMeme(memeName: String): Meme2 {
        val newCaptions = mutableListOf<Caption>()

        val uniqueId = System.currentTimeMillis()
        val uniqueName = "$memeName$uniqueId.jpg"
        val tempMeme = Meme2()
        tempMeme.id = uniqueId
        tempMeme.imageName = uniqueName
        tempMeme.imageTitle = memeName
        tempMeme.isModernMeme = true
        tempMeme.isCreatedByUser = false
        tempMeme.searchTags.add(
            SearchTag2(
                uniqueId,
                tempMeme.id,
                tempMeme.imageTitle
            )
        )

        for (i in 0 until photoEditorView.childCount) {
            val childAt: View = photoEditorView.getChildAt(i)
            val textView =
                childAt.findViewById<StrokeTextView?>(R.id.tvPhotoEditorText)
            textView?.let { strokeTextView ->

                val viewLocation = IntArray(2)
                strokeTextView.getLocationOnScreen(viewLocation)

                val rootLocation = IntArray(2)
                photoEditorView.getLocationOnScreen(rootLocation)

                val relativeLeft = viewLocation[0] - rootLocation[0]
                val relativeTop = viewLocation[1] - rootLocation[1]

                val captionWrapper = strokeTextView.tag as CaptionWrapper?
                captionWrapper?.let {

                    val tempCaption = Caption().apply {
                        id = it.id
                        text = it.text
                        textColor = it.textColor
                        fontSize = getFontSizeForOriginalDimensions(it.fontSize!!)
                        strokeWidth = it.strokeWidth
                        strokeColor = it.strokeColor
                        positionX =
                            getPositionXForOriginalDimensions(relativeLeft.toFloat())
                        positionY = getPositionYForOriginalDimensions(relativeTop.toFloat())
                        fontType = 1
                        maxLines = it.maxLines
                        it.fontType?.let {
                            fontType = it
                        }

                    }

                    newCaptions.add(tempCaption)
                }
            }
        }
        if (newCaptions.isNotEmpty()) {
            val captionSet =
                CaptionSet2(System.currentTimeMillis(), tempMeme.id, newCaptions)
            captionSet.captions.forEach { tempCaption ->
                tempCaption.captionSetId = captionSet.id
            }
            tempMeme.captionSets.add(captionSet)
        }

        return tempMeme

    }

    private fun getFontSizeForScaledMemeDimensions(fontSize: Int): Int {  // fontSize is Already in dp
        val originalWidth: Float =
            photoEditorView.source.drawable.intrinsicWidth.toFloat()
        val scaledWidth: Float = photoEditorView.source.width.toFloat()
        val widthScaleDifference = originalWidth / scaledWidth
        val scaledTextSizeForDevice: Float = fontSize / widthScaleDifference
        return scaledTextSizeForDevice.roundToInt() // important
    }


    private fun getPositionXForScaledDimensions(positionX: Float): Float {

        val originalWidth: Float =
            photoEditorView.source.drawable.intrinsicWidth.toFloat()
        val scaledWidth: Float = photoEditorView.source.width.toFloat()
        val widthScaleDifference = originalWidth / scaledWidth

        // widthScaleDifference is in px, position X should also be converted to device pixels first
        val marginStart =
            (resources.displayMetrics.density * positionX / widthScaleDifference).roundToInt()
        // -12dp because of frmBorder margin
        val positionXForScaledDimensions = (marginStart - 12.px).toFloat()

        return positionXForScaledDimensions
    }

    private fun getPositionYForScaledDimensions(positionY: Float): Float {
        val originalWidth: Float =
            photoEditorView.source.drawable.intrinsicWidth.toFloat()
        val scaledWidth: Float = photoEditorView.source.width.toFloat()
        val widthScaleDifference = originalWidth / scaledWidth

        // widthScaleDifference is in px, position X should also be converted to device pixels first
        val marginStart =
            (resources.displayMetrics.density * positionY / widthScaleDifference).roundToInt()
        // -12dp because of frmBorder margin
        val positionXForScaledDimensions = (marginStart - 8.px).toFloat()

        return positionXForScaledDimensions
    }

    private fun getFontSizeForOriginalDimensions(fontSize: Int): Int {  // fontSize is Already in dp
        val originalWidth: Float =
            photoEditorView.source.drawable.intrinsicWidth.toFloat()
        val scaledWidth: Float = photoEditorView.source.width.toFloat()
        val scaleDifference = originalWidth / scaledWidth
        val textSizeForOriginalDimensions = ((fontSize) * scaleDifference).roundToInt()
        return textSizeForOriginalDimensions
    }


    private fun getPositionXForOriginalDimensions(positionX: Float): Float {  // positionX is Already is in px
        // TODO drawable  null check
        val originalWidth: Float =
            photoEditorView.source.drawable.intrinsicWidth.toFloat()
        val scaledWidth: Float = photoEditorView.source.width.toFloat()
        val scaleDifference = originalWidth / scaledWidth
        val positionXForOriginalDimensions =
            (positionX / resources.displayMetrics.density) * scaleDifference
//        Log.d(TAG, "positionXForOriginalDimensions " + positionXForOriginalDimensions)
        return positionXForOriginalDimensions
    }

    private fun getPositionYForOriginalDimensions(positionY: Float): Float {     // positionX is Already is in px
        val originalHeight: Float =
            photoEditorView.source.drawable.intrinsicHeight.toFloat()
        val scaledHeight: Float = photoEditorView.source.height.toFloat()
        val scaleDifference = originalHeight / scaledHeight
        val positionYForOriginalDimensions =
            (positionY / resources.displayMetrics.density) * scaleDifference
//        Log.d(TAG, "positionYForOriginalDimensions " + positionYForOriginalDimensions)
        return positionYForOriginalDimensions
    }


    private fun initPhotoEditor() {

        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");
        photoEditor = PhotoEditor.Builder(this, photoEditorView)
            .setPinchTextScalable(true) // set flag to make text scalable when pinch
            //.setDefaultTextTypeface(mTextRobotoTf)
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk


        photoEditor.setOnPhotoEditorListener(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                UCrop.REQUEST_CROP -> {
                    isSomethingEdited = true
                    val resultUri = UCrop.getOutput(data!!)
                    photoEditor.clearAllViews()

                    photoEditorView.source.setImageURI(resultUri)
                    photoEditorView.source.viewTreeObserver
                        .addOnGlobalLayoutListener(object :
                            ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                photoEditorView.source.viewTreeObserver.removeOnGlobalLayoutListener(
                                    this
                                )
                                val finalHeight = photoEditorView.source.measuredHeight
                                val finalWidth = photoEditorView.source.measuredWidth
                                photoEditorView.layoutParams.width = finalWidth
                                photoEditorView.layoutParams.height = finalHeight
                                meme?.let {
                                    addMemeCaptions(it)
                                }
                            }
                        })
                }
                UCrop.RESULT_ERROR -> {
                    val cropError = UCrop.getError(data!!)
                    cropError?.printStackTrace()
                }
            }
        }
    }

    override fun onEditTextChangeListener(
        rootView: View?,
        textViewInput: TextView?,
        colorCode: Int
    ) {
        textViewInput?.let { textView ->

            rootView?.let {
                viewBeingEdited = rootView
            }

            textView.tag?.let { captionSetting ->
                if (captionSetting is CaptionWrapper) {
                    editTextCaptionSetting = getDefaultCaptionSettings()
                    captionSetting.maxLines?.let { editTextCaptionSetting.maxLines = it }
                    captionSetting.textColor?.let { editTextCaptionSetting.textColor = it }
                    captionSetting.strokeColor?.let {
                        editTextCaptionSetting.strokeColor = it
                    }
                    captionSetting.strokeWidth?.let {
                        editTextCaptionSetting.strokeWidth = it
                    }


                    captionSetting.fontSize?.let {
                        editTextCaptionSetting.fontSize = it
                    }

                    captionSetting.fontType?.let { fontId -> // fontType = fontId
                        editTextCaptionSetting.font = fontRepository.getById(fontId)
                    }
                } else {
                    getDefaultCaptionSettings()
                }
            }
            etEditText.setText(textView.text)
            showEditCaptionUi()
        }
    }

    override fun onAddViewListener(
        addedView: View?,
        viewType: ViewType?,
        numberOfAddedViews: Int
    ) {
        getSetUndoRedoUi()
    }


    override fun onRemoveViewListener(
        removedView: View?,
        viewType: ViewType?,
        numberOfAddedViews: Int
    ) {
        hideEditCaptionUi()
        getSetUndoRedoUi()
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
    }

    @SuppressLint("MissingPermission")
    private fun saveFileTemporarilyAndCrop() {

        try {

            val file = File(cacheDir.toString() + File.separator + TEMP_CROP_FILE)
            if (!file.exists()) {
                file.createNewFile()
            }

            photoEditor.saveFileTemporarily(file.absolutePath, object : OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    crop(Uri.fromFile(File(imagePath)))
                }

                override fun onFailure(exception: Exception) {
                    Toast.makeText(
                        this@MemeGeneratorActivity,
                        getString(R.string.str_failed_to_save_image),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveMemeToDevice() {

        val alertDialog = KAlertDialog(this, KAlertDialog.PROGRESS_TYPE)
        alertDialog.setCancelable(false)
        alertDialog.setTitleText(getString(R.string.str_saving))
            .show()

        alertDialog.setOnDismissListener {
            showInterstitialAd()
        }

        try {

            val saveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(false)
                .setTransparencyEnabled(true)
                .build()
            photoEditor.saveAsFileNew(saveSettings, object : OnSaveListener {
                override fun onSuccess(imagePath: String) {

                    alertDialog.changeAlertType(KAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = getString(R.string.str_saved)
                    alertDialog.setCancelable(true)
                    alertDialog.setCanceledOnTouchOutside(true)
                    Handler(mainLooper).postDelayed({
                        alertDialog.let {  // can be null
                            if (it.isShowing) {
                                it.dismissWithAnimation()
                            }
                        }
                    }, 1500)

                    if (!TextUtils.isEmpty(imagePath)) {
                        val tempFile = File(imagePath)
                        mSaveImageUri = Uri.parse(imagePath)

                    }
                    isSomethingEdited = false
                }

                override fun onFailure(exception: Exception) {

                    alertDialog.titleText = getString(R.string.str_sorry_try_again)
                    alertDialog.changeAlertType(KAlertDialog.ERROR_TYPE)
                    alertDialog.setCancelable(true)
                    alertDialog.setCanceledOnTouchOutside(true)
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()

            alertDialog.titleText = getString(R.string.str_sorry_try_again)
            alertDialog.changeAlertType(KAlertDialog.ERROR_TYPE)
            alertDialog.setCancelable(true)
            alertDialog.setCanceledOnTouchOutside(true)

        }
    }

    private fun saveMemeAsTemplate() {

        val alertDialog = KAlertDialog(this, KAlertDialog.PROGRESS_TYPE)
        alertDialog.setCancelable(false)
        alertDialog.setTitleText(getString(R.string.str_saving))
            .show()
        alertDialog.setOnDismissListener {
            showInterstitialAd()
        }
        try {
            if (StorageHelper.isExternalStorageWriteable()) {
                val templatesStorageDir = StorageHelper.getTemplatesPrivateDir()
                val fileDir = File(templatesStorageDir)
                fileDir.mkdirs()

                val filePath = templatesStorageDir + System.currentTimeMillis() + ".png"

                val file = File(filePath)
                if (!file.exists()) {
                    file.createNewFile()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.str_file_already_exist),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }

                photoEditor.saveAsTemplate(
                    file.absolutePath,
                    object : OnSaveListener {
                        override fun onSuccess(imagePath: String) {

                            alertDialog.changeAlertType(KAlertDialog.SUCCESS_TYPE)
                            alertDialog.titleText = getString(R.string.str_saved)
                            alertDialog.setCancelable(true)
                            alertDialog.setCanceledOnTouchOutside(true)
                            Handler(mainLooper).postDelayed({
                                alertDialog.let {  // can be null
                                    if (it.isShowing) {
                                        it.dismissWithAnimation()
                                    }
                                }
                            }, 1500)

                            val tempFile =
                                File(imagePath)              // Uri.parse(imagePath)
                            saveMemeTemplateToDatabase(tempFile.name)
                            isSomethingEdited = false
                        }

                        override fun onFailure(exception: Exception) {

                            alertDialog.titleText = getString(R.string.str_sorry_try_again)
                            alertDialog.changeAlertType(KAlertDialog.ERROR_TYPE)
                            alertDialog.setCancelable(true)
                            alertDialog.setCanceledOnTouchOutside(true)
                        }
                    })
            } else {
                alertDialog.titleText = getString(R.string.str_sorry_try_again)
                alertDialog.changeAlertType(KAlertDialog.ERROR_TYPE)
                alertDialog.setCancelable(true)
                alertDialog.setCanceledOnTouchOutside(true)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            alertDialog.titleText = getString(R.string.str_sorry_try_again)
            alertDialog.changeAlertType(KAlertDialog.ERROR_TYPE)
            alertDialog.setCancelable(true)
            alertDialog.setCanceledOnTouchOutside(true)
        }
    }



    private fun saveMemeTemplateToDatabase(fileName: String) {
        val newCaptions = mutableListOf<Caption>()

        val tempMeme = Meme2()
        tempMeme.id = System.currentTimeMillis()
        tempMeme.imageName = fileName
        tempMeme.imageTitle = "meme_maker_$fileName"
        tempMeme.isModernMeme = true
        tempMeme.isCreatedByUser = true
        tempMeme.searchTags.add(
            SearchTag2(
                System.currentTimeMillis(),
                tempMeme.id,
                tempMeme.imageTitle
            )
        )

        for (i in 0 until photoEditorView.childCount) {
            val childAt: View = photoEditorView.getChildAt(i)
            val textView =
                childAt.findViewById<StrokeTextView?>(R.id.tvPhotoEditorText)
            textView?.let { strokeTextView ->

                val viewLocation = IntArray(2)
                strokeTextView.getLocationOnScreen(viewLocation)

                val rootLocation = IntArray(2)
                photoEditorView.getLocationOnScreen(rootLocation)

                val relativeLeft = viewLocation[0] - rootLocation[0]
                val relativeTop = viewLocation[1] - rootLocation[1]

                val captionWrapper = strokeTextView.tag as CaptionWrapper?
                captionWrapper?.let {

                    Thread.sleep(1) // for System.currentTimeMillis()
                    val tempCaption = Caption().apply {
                        id = System.currentTimeMillis()
                        text = it.text
                        textColor = it.textColor
                        fontSize = it.fontSize
                        strokeWidth = it.strokeWidth
                        strokeColor = it.strokeColor
                        strokeColor = it.strokeColor
                        // -12, -8 for frmBorder margin
                        positionX = (relativeLeft - 12.px).toFloat()
                        positionY = (relativeTop - 8.px).toFloat()
                        fontType = 1
                        maxLines = it.maxLines
                        it.fontType?.let {
                            fontType = it
                        }
                    }
                    newCaptions.add(tempCaption)
                }
            }
        }
        Thread.sleep(2)

        if (newCaptions.isNotEmpty()) {
            val captionSet =
                CaptionSet2(System.currentTimeMillis(), tempMeme.id, newCaptions)
            captionSet.captions.forEach { tempCaption ->
                tempCaption.captionSetId = captionSet.id
            }
            tempMeme.captionSets.add(captionSet)
        }
        memeRepository.insertMeme(tempMeme)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
//                    showSaveBottomSheet()     // onRequestPermissionsResult is called before onResume
                } else {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            STORAGE_PERMISSIONS[0]
                        )
                    ) {
                        Toast.makeText(
                            this,
                            getString(R.string.str_storage_permission_denied),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
                return
            }
        }
    }

    private fun onEditCaptionSettingsChanged(captionSetting: CaptionSetting) {
        editTextCaptionSetting = captionSetting
        if (!TextUtils.isEmpty(etEditText.text.toString())) {
            editText(etEditText.text.toString(), editTextCaptionSetting)
        }
    }


    private fun shareMeme() {

        if (mSaveImageUri == null || isSomethingEdited) {
            //TODO dialog fragment
            MaterialAlertDialogBuilder(this)
                .setMessage(resources.getString(R.string.str_save_meme_to_share))
                .setNegativeButton(resources.getString(R.string.str_later)) { _, i -> }
                .setPositiveButton(resources.getString(R.string.str_save)) { _, i ->
                    saveMeme()
                }
                .show()
        } else {

            try {
                mSaveImageUri?.let { uri ->
                    uri.path?.let {
                        val file = File(FileUtils.getPath(this, uri))
                        if (file.exists()) {
                            val contentUri = buildFileProviderUri(file)

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                type = "image/*"
                                putExtra(
                                    Intent.EXTRA_STREAM,
                                    contentUri
                                )
                            }

                            startActivity(
                                Intent.createChooser(
                                    intent,
                                    getString(R.string.str_share_meme)
                                )
                            )
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (ex: Exception) {
                Toast.makeText(
                    this,
                    getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
                ex.printStackTrace()
            }
        }
    }

    private fun buildFileProviderUri(file: File): Uri? {
        return FileProvider.getUriForFile(
            this,
            FILE_PROVIDER_AUTHORITY,
            file
        )
    }


    override fun onBackPressed() {
        if (isSomethingEdited)
            showCancelEditingDialog()
        else {
            super.onBackPressed()
        }
    }

    //TODO dialog fragment
    private fun showCancelEditingDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(resources.getString(R.string.exit_dilaog_message))
            .setNegativeButton(resources.getString(R.string.no)) { _, i -> }
            .setPositiveButton(resources.getString(R.string.yes)) { _, i -> super.onBackPressed() }
            .show()
    }
}