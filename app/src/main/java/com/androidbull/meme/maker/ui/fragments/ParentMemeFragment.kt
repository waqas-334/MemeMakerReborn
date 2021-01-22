package com.androidbull.meme.maker.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.helper.FILE_PROVIDER_AUTHORITY
import com.androidbull.meme.maker.helper.FRAGMENT_CUSTOM_MEME_BOTTOM_SHEET_TAG
import com.androidbull.meme.maker.helper.SettingsManager
import com.androidbull.meme.maker.helper.StorageHelper
import com.androidbull.meme.maker.ui.activities.MemeGeneratorActivity
import com.androidbull.meme.maker.ui.adapter.MainPagerAdapter
import com.androidbull.meme.maker.ui.dialogs.CustomMemeBottomSheet
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.io.IOException

private const val TAG = "ParentMemeFragment"
internal const val REQUEST_IMAGE_CAPTURE = 11
internal const val REQUEST_DOCUMENT_PROVIDER = 22

class ParentMemeFragment : Fragment(), TabLayout.OnTabSelectedListener {


    private lateinit var tlMain: TabLayout
    private lateinit var viewPagerMain: ViewPager2
    private lateinit var eFabCustomMeme: ExtendedFloatingActionButton
    private lateinit var pagerAdapter: MainPagerAdapter
    private var cameraPhotoPath = ""

    private val fragments: List<Fragment> =
        listOf(
            AllMemeFragment(),
            NewMemesFragment(),
            FavouriteMemeFragment(),
            RandomMemeFragment(),
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_meme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi(view)
        initActions()
        setPagerAdapter()
        initTabLayout()
    }

    private fun initUi(view: View) {
        eFabCustomMeme = view.findViewById(R.id.eFabCustomMeme)
        tlMain = view.findViewById(R.id.tlMain)
        viewPagerMain = view.findViewById(R.id.viewPagerMain)
    }

    private fun initActions() {
        eFabCustomMeme.setOnClickListener {
            showCustomMemeBottomSheet()
        }
    }

    private fun showCustomMemeBottomSheet() {
        val customMemeBottomSheet = CustomMemeBottomSheet()
        customMemeBottomSheet.setCustomMemeOptionClickListener(object :
            CustomMemeBottomSheet.OnCustomMemeOptionClickListener {
            override fun onGalleryClick() {
                dispatchStorageAccessFrameworkIntent()
                customMemeBottomSheet.dismiss()
            }

            override fun onCameraClick() {
                dispatchTakePictureIntent()
                customMemeBottomSheet.dismiss()
            }
        })
        customMemeBottomSheet.show(requireFragmentManager(), FRAGMENT_CUSTOM_MEME_BOTTOM_SHEET_TAG)
    }

    private fun dispatchStorageAccessFrameworkIntent() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*";
            startActivityForResult(intent, REQUEST_DOCUMENT_PROVIDER)
        } catch (ex: ActivityNotFoundException) {
            ex.printStackTrace()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    ex.printStackTrace()
                    null

                }

                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        FILE_PROVIDER_AUTHORITY,
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val storageDir = if (StorageHelper.isExternalStorageWriteable()) {
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + File.separator
        } else {
            requireContext().filesDir.path + File.separator
        }

        val fileDir = File(storageDir)
        fileDir.mkdirs()

        val filePath = storageDir + "camera_temp" + ".jpg"

        val file = File(filePath);
        if (!file.exists()) {
            file.createNewFile()
        }
        cameraPhotoPath = filePath
        return file

    }

    private fun setPagerAdapter() {
        pagerAdapter = MainPagerAdapter(this, fragments)
        viewPagerMain.adapter = pagerAdapter
    }

    private fun initTabLayout() {
        with(tlMain)
        {
            repeat(4) {
                newTab()
            }
        }

        TabLayoutMediator(tlMain, viewPagerMain) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.str_all_memes)
                }
                1 -> {
                    tab.text = getString(R.string.str_new)
                    tab.orCreateBadge.apply {
                        backgroundColor = ContextCompat.getColor(requireContext(), R.color.white)
                        isVisible = false
                    }
                }
                2 -> {
                    tab.text = getString(R.string.str_favourite)

                }
                3 -> {
                    tab.text = getString(R.string.str_random)

                }
            }
        }.attach()

        tlMain.addOnTabSelectedListener(this)
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        SettingsManager.saveNewMemesAvailable(false)
        tab?.orCreateBadge?.apply {
            isVisible = false
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (!TextUtils.isEmpty(cameraPhotoPath)) {
                val intent = Intent(requireContext(), MemeGeneratorActivity::class.java)
                intent.putExtra("customCameraMemePath", cameraPhotoPath)
                startActivity(intent)
            }
        } else if (requestCode == REQUEST_DOCUMENT_PROVIDER && resultCode == RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            data?.data?.let { uri ->
                val intent = Intent(requireActivity(), MemeGeneratorActivity::class.java)
                intent.putExtra("customGalleryMemePath", uri.toString())
                startActivity(intent)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    fun extendCustomMemeButton() {
        if (!eFabCustomMeme.isExtended) {
            eFabCustomMeme.extend()
        }
    }

    fun shrinkCustomMemeButton() {
        if (eFabCustomMeme.isExtended) {
            eFabCustomMeme.shrink()
        }
    }

    fun setCustomMemeFabVisibility(isVisible: Int) {
        eFabCustomMeme.visibility = isVisible
    }


    fun updateLayoutManager() {
        when (val fragment = fragments[tlMain.selectedTabPosition]) {
            is AllMemeFragment -> {
                fragment.updateLayoutManager()
            }
            is FavouriteMemeFragment -> {
                fragment.updateLayoutManager()
            }
            is RandomMemeFragment -> {
                fragment.updateLayoutManager()
            }
            is NewMemesFragment -> {
                fragment.updateLayoutManager()
            }
        }
    }

    fun scrollViewPagerToFirstPage(): Boolean {
        return if (viewPagerMain.currentItem != 0) {
            viewPagerMain.setCurrentItem(0, true)
            true
        }else{
            false
        }
    }

    fun showNewMemesBadge() {
        tlMain.getTabAt(1)?.orCreateBadge?.apply {
            backgroundColor = ContextCompat.getColor(requireContext(), R.color.white)
            isVisible = true
        }
    }

    fun randomizeAndUpdate() {
        val fragment = fragments[tlMain.selectedTabPosition]
        if (fragment is RandomMemeFragment) {
            fragment.randomizeAndUpdate()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.app_name)
        if (SettingsManager.getNewMemesAvailable())
            showNewMemesBadge()
    }
}