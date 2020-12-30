package com.androidbull.meme.generator.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.data.repository.RoomMemeRepository
import com.androidbull.meme.generator.helper.*
import com.androidbull.meme.generator.model.Meme2
import com.androidbull.meme.generator.ui.dialogs.rate.RateAppBottomSheet
import com.androidbull.meme.generator.ui.fragments.ParentMemeFragment
import com.androidbull.meme.generator.ui.fragments.SavedMemeFragment
import com.androidbull.meme.generator.ui.fragments.SavedTemplateFragment
import com.androidbull.meme.generator.ui.fragments.SearchFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

private const val MAIN_ACTIVITY_BANNER_AD_ID = "580015096002786_580077345996561"

class MainActivity : AdsActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var tbMain: MaterialToolbar
    private lateinit var drawerMain: DrawerLayout
    private lateinit var navViewMain: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var listIcon: Drawable? = null
    private var memeRepository = RoomMemeRepository()
    private var mainMenu: Menu? = null
    private var shuffleIconVisibility = false

    private lateinit var bannerAdContainer: ViewGroup

    private var fragmentToReplace: Fragment? = null
    private var fragmentToReplaceTag: String = ""
    private var isReplaceFragment = false

    private val searchFragment = SearchFragment()

    private val _memes = MutableLiveData<List<Meme2>>()
    val memes: LiveData<List<Meme2>>
        get() = _memes


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUi()
        initActions()
        initToolbar()
        initDrawer()
        checkPermissions()
        addInitialFragment()
        setUpAppLaunchCounter()

        getMemes()
        getNewMemeUpdates()
    }


    private fun initUi() {
        tbMain = findViewById(R.id.tbMain)
        drawerMain = findViewById(R.id.drawerMain)
        navViewMain = findViewById(R.id.navViewMain)
        bannerAdContainer = findViewById(R.id.flBannerAdContainer)
    }

    private fun initActions() {
        navViewMain.setNavigationItemSelectedListener(this)

        supportFragmentManager.addOnBackStackChangedListener { // listener for navigation selected item on back pressed
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            currentFragment?.let {
                if (it is ParentMemeFragment) {
                    navViewMain.setCheckedItem(R.id.mi_memes)
                } else if (it is SavedMemeFragment)
                    navViewMain.setCheckedItem(R.id.mi_saved_memes)
            }
        }
    }


    private fun setUpAppLaunchCounter() {
        val appLaunchCounter: Int = SettingsManager.getAppLaunchCount() + 1
        SettingsManager.saveAppLaunchCount(appLaunchCounter)

        if (appLaunchCounter % 7 == 0 && !SettingsManager.getIsAppRated()) {
            RateAppBottomSheet.newInstance().show(supportFragmentManager, FRAGMENT_RATE_APP_TAG)
        }
    }

    private fun getMemes() {
        memeRepository.getAllMemesObservable().observe(this@MainActivity, {
            _memes.value = it
        })

    }

    override fun onPremiumMemberShipAcquired() {
        AdsManager.removeAds()
        hidePurchasesDrawerItem()
    }

    override fun onPremiumMemberShipLost() {
        AdsManager.loadAndShowBannerAd(
            adId = MAIN_ACTIVITY_BANNER_AD_ID,
            adContainer = bannerAdContainer
        )
    }

    override fun onDestroy() {
        AdsManager.removeAd(MAIN_ACTIVITY_BANNER_AD_ID)
        super.onDestroy()
    }

    private fun hidePurchasesDrawerItem() {
        navViewMain.menu.findItem(R.id.mi_remove_ads).isVisible = false
    }

    private fun addInitialFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, ParentMemeFragment(), FRAGMENT_PARENT_MEME_TAG).commit()
    }

    private fun initToolbar() {
        setSupportActionBar(tbMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }


    private fun initDrawer() {

        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerMain,
            tbMain,
            R.string.drawer_opened,
            R.string.drawer_closed
        )
        drawerMain.addDrawerListener(drawerToggle)
        drawerMain.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerClosed(drawerView: View) {
                if (isReplaceFragment) {
                    fragmentToReplace?.let {
                        replaceFragment(it, fragmentToReplaceTag)
                    }
                    isReplaceFragment = false
                    fragmentToReplace = null
                }
            }

            override fun onDrawerOpened(drawerView: View) {

            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.mi_memes -> {
                fragmentToReplace = ParentMemeFragment()
                fragmentToReplaceTag = FRAGMENT_PARENT_MEME_TAG
                isReplaceFragment = true
            }
            R.id.mi_saved_memes -> {
                fragmentToReplace = SavedMemeFragment()
                fragmentToReplaceTag = FRAGMENT_SAVED_MEMES_TAG
                isReplaceFragment = true
            }
            R.id.mi_saved_templates -> {
                fragmentToReplace = SavedTemplateFragment()
                fragmentToReplaceTag = FRAGMENT_SAVED_TEMPLATES_TAG
                isReplaceFragment = true
            }
            R.id.mi_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.mi_remove_ads -> {
                startActivity(Intent(this, PurchaseActivity::class.java))
            }
            R.id.mi_share_app -> {
                shareApp(this)
            }
            R.id.mi_more_apps -> {
                openDeveloperPageInPlayStore(this, GOOGLE_PLAY_DEVELOPER_ID)
            }
            R.id.mi_privacy_policy -> {
                openLinkInBrowser(this, PRIVACY_POLICY_LINK)
            }
            R.id.mi_contact_us -> {
                sendEmail(this, "Meme Maker Android App", "")
            }
            R.id.mi_rate_app -> {
                openAppInPlayStore(packageName)
            }
        }
        closeDrawer()
        return true
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment, tag)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun closeDrawer() {
        if (drawerMain.isDrawerOpen(GravityCompat.START)) {
            drawerMain.closeDrawer(GravityCompat.START)
            return
        }
    }

    override fun onBackPressed() {
        closeDrawer()
        super.onBackPressed()
    }


    private fun checkPermissions() {
        /* if (Build.VERSION.SDK_INT < 29) {
             val readPermission = arrayOf(
                 Manifest.permission.READ_EXTERNAL_STORAGE
             )
             if (!hasPermissions(*permissions)) {
                 ActivityCompat.requestPermissions(this, permissions, 788)
             }
         }*/
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        listIcon?.let {
            menu?.findItem(R.id.mi_list)?.icon = it
        }
        menu?.findItem(R.id.mi_shuffle)?.isVisible = shuffleIconVisibility

        return super.onPrepareOptionsMenu(menu)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_main_menu, menu)

        mainMenu = menu
        setSearchView(menu)

        return true
    }

    private fun setSearchView(menu: Menu?) {
        menu?.let {
            val searchMenuItem = it.findItem(R.id.mi_search)
            val searchView = searchMenuItem.actionView as SearchView

            searchView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {
                    setMenuItemsVisibility(false, searchMenuItem)
                    replaceFragment(searchFragment, FRAGMENT_SEARCH_TAG)
                }

                override fun onViewDetachedFromWindow(v: View?) {
                    setMenuItemsVisibility(true, searchMenuItem)
                    onBackPressed()
                }
            })

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (searchFragment.isAdded && searchFragment.isVisible) {
                        searchFragment.searchAdapter.filter.filter(newText)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
            })
        }
    }

    fun setMenuItemsVisibility(visible: Boolean, exception: MenuItem?) {
        mainMenu?.let { menu ->
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                if (item != exception)
                    item.isVisible = visible
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            R.id.mi_list -> {

                supportFragmentManager.findFragmentByTag(FRAGMENT_PARENT_MEME_TAG)
                    ?.let { fragment ->
                        if (fragment is ParentMemeFragment && fragment.isVisible) {
                            fragment.updateLayoutManager()
                            return true
                        }
                    }

                supportFragmentManager.findFragmentByTag(FRAGMENT_SAVED_MEMES_TAG)
                    ?.let { fragment ->
                        if (fragment is SavedMemeFragment && fragment.isVisible) {
                            fragment.updateLayoutManager()
                            return true
                        }
                    }

                supportFragmentManager.findFragmentByTag(FRAGMENT_SAVED_TEMPLATES_TAG)
                    ?.let { fragment ->
                        if (fragment is SavedTemplateFragment && fragment.isVisible) {
                            fragment.updateLayoutManager()
                            return true
                        }
                    }

                return true
            }
            R.id.mi_shuffle -> {

                supportFragmentManager.findFragmentByTag(FRAGMENT_PARENT_MEME_TAG)
                    ?.let { fragment ->
                        if (fragment is ParentMemeFragment && fragment.isVisible) {
                            fragment.randomizeAndUpdate()
                        }
                    }

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun updateToolbarIcons(listIcon: Drawable?, shuffleIconVisibility: Boolean) {
        listIcon?.let {
            this.listIcon = it
        }
        this.shuffleIconVisibility = shuffleIconVisibility
        invalidateOptionsMenu()

    }

    private fun hasPermissions(vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun getNewMemeUpdates() {

        val lastMemeId = memeRepository.getLastMemeId()
        if (lastMemeId > 0) {
            val db = Firebase.firestore
            db.collection(NEW_MEMES_FIRESTORE_COLLECTION)
                .whereGreaterThan("id", lastMemeId)
                .get()
                .addOnSuccessListener { documents ->
                    val newMemes = mutableListOf<Meme2>()
                    for (document in documents) {
                        val newMeme = document.toObject<Meme2>()
                        newMemes.add(newMeme)
                    }
                    if (newMemes.isNotEmpty()) {
                        memeRepository.insertAllMemes(newMemes)
                        SettingsManager.saveNewMemesAvailable(true)
                        supportFragmentManager.fragments.last()?.let {
                            if (it is ParentMemeFragment) {
                                it.showNewMemesBadge()
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("MainActivity", "Error getting documents: ", exception)
                }
        }
    }

}