package com.androidbull.meme.generator.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.data.repository.RoomMemeRepository
import com.androidbull.meme.generator.helper.LayoutManagerType
import com.androidbull.meme.generator.helper.MEME_GRID_COLUMNS
import com.androidbull.meme.generator.helper.PREF_SAVED_TEMPLATES_LAYOUT_MANAGER_TYPE
import com.androidbull.meme.generator.helper.SettingsManager
import com.androidbull.meme.generator.model.Meme2
import com.androidbull.meme.generator.ui.activities.MainActivity
import com.androidbull.meme.generator.ui.activities.MemeGeneratorActivity
import com.androidbull.meme.generator.ui.adapter.SavedTemplateAdapter
import com.androidbull.meme.generator.ui.interfaces.OnSavedMemeItemClickListener
import com.developer.kalert.KAlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SavedTemplateFragment : Fragment(), OnSavedMemeItemClickListener, ActionMode.Callback {

    private lateinit var tvEmpty: TextView
    private lateinit var fabSelection: FloatingActionButton
    private lateinit var rvSavedTemplates: RecyclerView
    private var selectAll = false
    private var actionMode: ActionMode? = null

    private val savedTemplates = mutableListOf<Meme2>()
    private val savedTemplateAdapter = SavedTemplateAdapter(mutableListOf(), this)
    private val memeRepository = RoomMemeRepository()
    private var currentLayoutManagerType = SettingsManager.getCurrentLayoutManager(
        PREF_SAVED_TEMPLATES_LAYOUT_MANAGER_TYPE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved_templates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateToolbarIcons()
        updateToolbarTitle()
        initUi(view)
        initActions()
        initMemeRecyclerView()
        getAllMemes()
    }

    /** observing here is safe (onViewCreated is not called repeatedly)
     *  observing because of updates after delete operation
     */
    private fun getAllMemes() {
        activity?.let {
            if (it is MainActivity) {
                it.memes.observe(this, { memesList ->
                    onMemesFetched(memesList)
                })
            }
        }
    }

    private fun onMemesFetched(memes: List<Meme2>) {

        savedTemplates.clear()
        memes.forEach { meme ->
            if (meme.isCreatedByUser)   // isCreatedByUser = Saved Template
                savedTemplates.add(meme)
        }
        savedTemplates.reverse()
        savedTemplateAdapter.updateAdapter(savedTemplates)

        if (savedTemplates.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            fabSelection.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            fabSelection.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbarIcons()
    }

    private fun updateToolbarTitle() {
        activity?.let {
            it.title = getString(R.string.str_saved_templates)
        }
    }

    private fun initUi(view: View) {
        rvSavedTemplates = view.findViewById(R.id.rvSavedTemplates)
        tvEmpty = view.findViewById(R.id.tvEmpty)
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

    private fun selectAllMemes() {
        if (selectAll) {
            savedTemplates.forEach {
                it.isFavourite = false   // isFavourite is acting as isSelected
                selectAll = false
            }
        } else {
            savedTemplates.forEach {
                it.isFavourite = true   // isFavourite is acting as isSelected
                selectAll = true
            }
        }
        savedTemplateAdapter.notifyDataSetChanged()
    }

    private fun getSelectedMemes(): MutableList<Meme2> {
        val memesToDelete = mutableListOf<Meme2>()
        savedTemplates.forEach {
            if (it.isFavourite) {    // isFavourite is acting as isSelected
                memesToDelete.add(it)
            }
        }
        return memesToDelete
    }

    private fun deleteMemes(memesToDelete: MutableList<Meme2>) {
        if (memesToDelete.isNotEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.str_delete_confirmation))
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .setPositiveButton(getString(R.string.yes)) { _, _ ->

                    val alertDialog = KAlertDialog(requireContext(), KAlertDialog.PROGRESS_TYPE)
                    alertDialog.setCancelable(false)
                    alertDialog.setTitleText(getString(R.string.str_deleting))
                        .show()

                    memeRepository.deleteSavedTemplates(memesToDelete)
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
                }
                .show()
        }
    }

    private fun initMemeRecyclerView() {
        with(rvSavedTemplates)
        {
            setLayoutManager(currentLayoutManagerType)
            setHasFixedSize(true)
            adapter = savedTemplateAdapter
        }
    }

    override fun onMemeClicked(meme: Meme2, position: Int) {
        val intent = Intent(requireActivity(), MemeGeneratorActivity::class.java)
        intent.putExtra("memeId", meme.id)
        startActivity(intent)
    }

    override fun onMemeSelected(meme: Meme2, position: Int, isSelected: Boolean) {
        savedTemplates.forEach {
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
        with(rvSavedTemplates)
        {
            setLayoutManager(currentLayoutManagerType)
            adapter = savedTemplateAdapter
        }
        saveLayoutManager()

    }

    private fun saveLayoutManager() {
        SettingsManager.saveCurrentLayoutManager(
            PREF_SAVED_TEMPLATES_LAYOUT_MANAGER_TYPE,
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
            if (it is MainActivity) {
                it.updateToolbarIcons(listIcon, false)
            }
        }

    }

    private fun RecyclerView.setLayoutManager(layoutManagerType: LayoutManagerType?) {
        var scrollPosition = 0

        if (layoutManager != null) {
            scrollPosition = (rvSavedTemplates.layoutManager as LinearLayoutManager)
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
        rvSavedTemplates.layoutManager = layoutManager
        rvSavedTemplates.scrollToPosition(scrollPosition)
    }


    private fun startContextualActionMode() {
        savedTemplateAdapter.updateSelectionUi(true)
        actionMode = activity?.startActionMode(this)

    }

    private fun stopContextualActionMode() {
        savedTemplates.forEach { // unselect all
            it.isFavourite = false   // isFavourite is acting as isSelected
            selectAll = false
        }
        savedTemplateAdapter.updateSelectionUi(false)
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