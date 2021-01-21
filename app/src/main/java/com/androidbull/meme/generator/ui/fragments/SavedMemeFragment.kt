package com.androidbull.meme.generator.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.data.repository.RoomMemeRepository
import com.androidbull.meme.generator.helper.*
import com.androidbull.meme.generator.model.Meme2
import com.androidbull.meme.generator.ui.activities.MainActivity
import com.androidbull.meme.generator.ui.activities.MemeGeneratorActivity
import com.androidbull.meme.generator.ui.adapter.SavedMemeAdapter
import com.androidbull.meme.generator.ui.interfaces.OnSavedMemeItemClickListener
import com.developer.kalert.KAlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SavedMemeFragment : Fragment(), OnSavedMemeItemClickListener, ActionMode.Callback {

    private val STORAGE_PERMISSION_REQUEST_FOR_DELETION = 789
    private lateinit var groupEmptyView: Group
    private lateinit var fabSelection: FloatingActionButton
    private lateinit var rvSavedMemes: RecyclerView

    private lateinit var savedMemeAdapter: SavedMemeAdapter
    private val savedMemes = mutableListOf<Meme2>()
    private val memeRepository = RoomMemeRepository()
    private var currentLayoutManagerType = SettingsManager.getCurrentLayoutManager(PREF_SAVED_MEMES_LAYOUT_MANAGER_TYPE)

    private var selectAll = false
    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved_meme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        updateToolbarIcons()
        updateToolbarTitle()
        initUi(view)
        initActions()
        initMemeAdapter()
        initMemeRecyclerView()

        onSavedMemesFetched(getSavedMemes())
    }

    @SuppressLint("MissingPermission")
    private fun getSavedMemes(): MutableList<Meme2> {
        if (hasPermissions(*STORAGE_PERMISSIONS, context = requireContext())) {
            return memeRepository.getSavedMemes()
        } else {
            requestPermissions(
                STORAGE_PERMISSIONS,
                STORAGE_PERMISSION_REQUEST
            )
        }
        return mutableListOf()
    }


    private fun onSavedMemesFetched(memes: List<Meme2>) {

        savedMemes.clear()
        memes.forEach { meme ->
            savedMemes.add(meme)
        }

        updateAdapter()

        if (memes.isEmpty()) {
            groupEmptyView.visibility = View.VISIBLE
            fabSelection.visibility = View.GONE
        } else {
            groupEmptyView.visibility = View.GONE
            fabSelection.visibility = View.VISIBLE
        }
    }

    private fun updateToolbarTitle() {
        activity?.let {
            it.title = getString(R.string.str_saved_memes)
        }
    }

    private fun initUi(view: View) {
        rvSavedMemes = view.findViewById(R.id.rvSavedMemes)
        groupEmptyView = view.findViewById(R.id.groupEmptyView)
        fabSelection = view.findViewById(R.id.fabSelection)
    }


    private fun initActions() {
        fabSelection.setOnClickListener {
            if (actionMode != null) {
                actionMode?.finish()
            } else {
                startContextualActionMode()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            STORAGE_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    onSavedMemesFetched(getSavedMemes())
                } else {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            STORAGE_PERMISSIONS[1]
                        )
                    ) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.str_storage_permission_requires_for_saved_memes),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.str_storage_permission_required),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
                return
            }
            STORAGE_PERMISSION_REQUEST_FOR_DELETION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            STORAGE_PERMISSIONS[1]
                        )
                    ) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.str_storage_permission_requires_for_saved_memes),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.str_storage_permission_required),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
                return
            }

        }
    }

    private fun selectAllMemes() {
        if (selectAll) {
            savedMemes.forEach {
                it.isFavourite = false   // isFavourite is acting as isSelected
                selectAll = false
            }
        } else {
            savedMemes.forEach {
                it.isFavourite = true   // isFavourite is acting as isSelected
                selectAll = true
            }
        }
        savedMemeAdapter.notifyDataSetChanged()
    }

    private fun getSelectedMemes(): MutableList<Meme2> {
        val memesToDelete = mutableListOf<Meme2>()
        savedMemes.forEach {
            if (it.isFavourite) {    // isFavourite is acting as isSelected
                memesToDelete.add(it)
            }
        }
        return memesToDelete
    }

    private fun deleteMemes(memesToDelete: MutableList<Meme2>) {

        if (hasPermissions(*STORAGE_PERMISSIONS, context = requireContext())) {
            if (memesToDelete.isNotEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.str_delete_confirmation))
                    .setNegativeButton(getString(R.string.no)) { _, _ -> }
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->

                        val alertDialog = KAlertDialog(requireContext(), KAlertDialog.PROGRESS_TYPE)
                        alertDialog.setCancelable(false)
                        alertDialog.setTitleText(getString(R.string.str_deleting))
                            .show()

                        if (deleteSavedMemes(memesToDelete)) {

                            actionMode?.finish()

                            alertDialog.changeAlertType(KAlertDialog.SUCCESS_TYPE)
                            alertDialog.titleText = getString(R.string.str_deleted_successfully)
                            alertDialog.setCancelable(true)
                            alertDialog.setCanceledOnTouchOutside(true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                alertDialog?.let {  // can be null
                                    if (it.isShowing) {
                                        it.dismissWithAnimation()
                                    }
                                }
                            }, 1500)
                            onSavedMemesFetched(getSavedMemes())
                        } else {
                            alertDialog.titleText = getString(R.string.str_sorry_try_again)
                            alertDialog.changeAlertType(KAlertDialog.ERROR_TYPE)
                            alertDialog.setCancelable(true)
                            alertDialog.setCanceledOnTouchOutside(true)
                        }
                    }
                    .show()
            }
        } else {
            requestPermissions(
                STORAGE_PERMISSIONS,
                STORAGE_PERMISSION_REQUEST_FOR_DELETION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun deleteSavedMemes(memesToDelete: MutableList<Meme2>) =
        memeRepository.deleteSavedMemes(memesToDelete)


    private fun updateAdapter() {
        savedMemeAdapter.updateAdapter(savedMemes)
    }

    private fun initMemeAdapter() {
        savedMemeAdapter = SavedMemeAdapter(listOf(), this@SavedMemeFragment)
    }

    private fun initMemeRecyclerView() {
        with(rvSavedMemes)
        {
            setLayoutManager(currentLayoutManagerType)
            setHasFixedSize(true)
            adapter = savedMemeAdapter
        }
    }



    override fun onMemeClicked(meme: Meme2, position: Int) {
        val intent = Intent(requireActivity(), MemeGeneratorActivity::class.java)
        intent.putExtra("savedMemePath", meme.imageName)
        startActivity(intent)
    }

    override fun onMemeSelected(meme: Meme2, position: Int, isSelected: Boolean) {
        savedMemes.forEach {
            if (it.id == meme.id) {
                it.isFavourite = isSelected    // isFavourite is acting as isSelected
            }
        }
    }


    fun updateLayoutManager() {

        if (currentLayoutManagerType == LayoutManagerType.GRID_LAYOUT_MANAGER) {
            currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
        } else if (currentLayoutManagerType == LayoutManagerType.LINEAR_LAYOUT_MANAGER) {
            currentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER
        }

        updateToolbarIcons()
        with(rvSavedMemes)
        {
            setLayoutManager(currentLayoutManagerType)
            adapter = savedMemeAdapter
        }
        saveLayoutManager()

    }

    private fun saveLayoutManager() {
        SettingsManager.saveCurrentLayoutManager(
            PREF_SAVED_MEMES_LAYOUT_MANAGER_TYPE,
            currentLayoutManagerType
        )
    }

    private fun updateToolbarIcons() {
        var listIcon = ContextCompat.getDrawable( //default icon
            requireContext(),
            R.drawable.ic_baseline_list_24
        )

        if (currentLayoutManagerType == LayoutManagerType.LINEAR_LAYOUT_MANAGER) {
            listIcon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_grid_on_24
            )
        }

        activity?.let {
            if (it is MainActivity)
            {
                it.updateToolBarIcons(listIcon, shuffleIconVisibility = false,searchIconVisibility = false)

            }
        }

    }

    private fun RecyclerView.setLayoutManager(layoutManagerType: LayoutManagerType?) {
        var scrollPosition = 0

        if (layoutManager != null) {
            scrollPosition = (rvSavedMemes.layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
        }
        when (layoutManagerType) {
            LayoutManagerType.GRID_LAYOUT_MANAGER -> {
                val gridColumns: Int = MEME_GRID_COLUMNS
//                    getNumberOfColumns(requireContext(), R.layout.item_meme_grid)

                layoutManager = GridLayoutManager(activity, gridColumns)
                currentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER
            }
            LayoutManagerType.LINEAR_LAYOUT_MANAGER -> {
                layoutManager = LinearLayoutManager(activity)
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
            }
            else -> {
                layoutManager = LinearLayoutManager(activity)
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
            }
        }
        rvSavedMemes.layoutManager = layoutManager
        rvSavedMemes.scrollToPosition(scrollPosition)
    }

    private fun startContextualActionMode() {
        savedMemeAdapter.updateSelectionUi(true)
        actionMode = activity?.startActionMode(this)

    }

    // called on finish()
    private fun stopContextualActionMode() {
        savedMemes.forEach { // unselect all
            it.isFavourite = false   // isFavourite is acting as isSelected
            selectAll = false
        }
        savedMemeAdapter.updateSelectionUi(false)
        actionMode = null
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.delete_meme_context_menu, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_delete -> {
                deleteMemes(getSelectedMemes())
                true
            }
            R.id.mi_select_all -> {
                selectAllMemes()
                true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        stopContextualActionMode()
    }
}