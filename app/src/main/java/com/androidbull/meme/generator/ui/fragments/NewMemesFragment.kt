package com.androidbull.meme.generator.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
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
import com.androidbull.meme.generator.ui.adapter.NewMemeAdapter
import com.androidbull.meme.generator.ui.interfaces.OnMemeItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewMemesFragment : Fragment(), OnMemeItemClickListener {

    private lateinit var rvMeme: RecyclerView
    private lateinit var groupEmptyView: Group

    private var newMemes = mutableListOf<Meme2>()
    private val memeRepository = RoomMemeRepository()
    private val memeAdapter = NewMemeAdapter(newMemes, this)
    private var currentLayoutManagerType =
        SettingsManager.getCurrentLayoutManager(PREF_NEW_MEMES_LAYOUT_MANAGER_TYPE)

    override fun onResume() {
        super.onResume()
        updateToolBarIcons()
        showCustomMemeFab()
        getAllMemes()
    }

    /**
     *  Observe has been used to wait for first meme data fetch.
     *  Only the latest meme data update is required at (onResume),
     *  remove observer after getting latest update to avoid onMemesFetched getting called
     *  for every single database change
     **/
    private fun getAllMemes() {
        activity?.let {
            if (it is MainActivity) {
                it.memes.observe(this, { memes ->
                    onMemesFetched(memes)
                    it.memes.removeObservers(this)
                })
            }
        }
    }


    private fun onMemesFetched(memes: List<Meme2>) {
        newMemes.clear()
        memes.forEach { meme ->
            if (meme.id > 2000 && !meme.isCreatedByUser)     //TODO logic to detect new Meme
                newMemes.add(meme)
        }
        newMemes.reverse()
        memeAdapter.updateAdapter(newMemes)
        if (newMemes.isEmpty()) {
            groupEmptyView.visibility = View.VISIBLE
        } else {
            groupEmptyView.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_memes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi(view)
        initMemeRecyclerView(currentLayoutManagerType)

        rvMeme.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && recyclerView.computeVerticalScrollOffset() == 0
                ) {
                    parentFragment?.let {
                        if (it is ParentMemeFragment)
                            it.extendCustomMemeButton()
                    }
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) {
                    parentFragment?.let {
                        if (it is ParentMemeFragment)
                            it.shrinkCustomMemeButton()
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }


    private fun initUi(view: View) {
        rvMeme = view.findViewById(R.id.rvMeme)
        groupEmptyView = view.findViewById(R.id.groupEmptyView)

        if (newMemes.isEmpty()) {
            groupEmptyView.visibility = View.VISIBLE
        } else {
            groupEmptyView.visibility = View.GONE
        }
    }

    private fun initMemeRecyclerView(layoutManagerType: LayoutManagerType) {
        with(rvMeme)
        {
            setLayoutManager(layoutManagerType)
            setHasFixedSize(true)
            adapter = memeAdapter
        }
    }

    private fun saveLayoutManager() {
        SettingsManager.saveCurrentLayoutManager(
            PREF_NEW_MEMES_LAYOUT_MANAGER_TYPE,
            currentLayoutManagerType
        )
    }


    override fun onMemeClicked(meme: Meme2, position: Int) {
        val intent = Intent(requireActivity(), MemeGeneratorActivity::class.java)
        intent.putExtra(BUNDLE_EXTRA_MEME_ID, meme.id)
        startActivity(intent)
    }

    override fun onAddToFavouritesClicked(meme: Meme2, position: Int, isFavourite: Boolean) {
        updateFavouriteMeme(meme)
    }

    private fun updateFavouriteMeme(meme: Meme2) {
        meme.isFavourite = !meme.isFavourite
        GlobalScope.launch(Dispatchers.IO) {
            memeRepository.updateMeme(meme)
        }
    }

    private fun showCustomMemeFab() {
        parentFragment?.let {
            if (it is ParentMemeFragment)
                it.setCustomMemeFabVisibility(View.VISIBLE)
        }
    }

    private fun updateToolBarIcons() {
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


    fun updateLayoutManager() {

        if (currentLayoutManagerType == LayoutManagerType.GRID_LAYOUT_MANAGER) {
            currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
        } else if (currentLayoutManagerType == LayoutManagerType.LINEAR_LAYOUT_MANAGER) {
            currentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER
        }

        updateToolBarIcons()
        with(rvMeme)
        {
            setLayoutManager(currentLayoutManagerType)
            adapter = memeAdapter
        }
        saveLayoutManager()

    }

    private fun RecyclerView.setLayoutManager(layoutManagerType: LayoutManagerType?) {
        var scrollPosition = 0

        if (layoutManager != null) {
            scrollPosition = (rvMeme.layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
        }
        when (layoutManagerType) {
            LayoutManagerType.GRID_LAYOUT_MANAGER -> {
                val gridColumns = MEME_GRID_COLUMNS
//                    getNumberOfColumns(requireContext(), R.layout.item_meme_grid)
                layoutManager = GridLayoutManager(requireContext(), gridColumns)
                currentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER

            }
            LayoutManagerType.LINEAR_LAYOUT_MANAGER -> {
                layoutManager = LinearLayoutManager(requireContext())
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
            }
            else -> {
                layoutManager = LinearLayoutManager(requireContext())
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
            }
        }
        rvMeme.layoutManager = layoutManager
        rvMeme.scrollToPosition(scrollPosition)
    }

}