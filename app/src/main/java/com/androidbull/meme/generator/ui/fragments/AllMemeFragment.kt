package com.androidbull.meme.generator.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.androidbull.meme.generator.ui.adapter.MemeAdapter
import com.androidbull.meme.generator.ui.interfaces.OnMemeItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AllMemeFragment : Fragment(), OnMemeItemClickListener {

    private lateinit var rvMeme: RecyclerView
    private lateinit var tvEmpty: TextView

    private val allMemesAdapter = MemeAdapter(listOf(), this)
    private val memeRepository = RoomMemeRepository()
    private var currentLayoutManagerType = SettingsManager.getCurrentLayoutManager(
        PREF_ALL_MEMES_LAYOUT_MANAGER_TYPE
    )


    override fun onResume() {
        super.onResume()
        updateToolBarIcons()
        showCustomMemeFab()
        activity?.let {
            if (it is MainActivity) {
                it.memes.observe(this, { memes ->
                    onMemeDataChanged(memes)
                    it.memes.removeObservers(this)
                })
            }
        }
    }

    private fun onMemeDataChanged(memes: List<Meme2>) {
        allMemesAdapter.updateAdapter(memes)
        if (memes.isNotEmpty()) {
            tvEmpty.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_meme, container, false)
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
        tvEmpty = view.findViewById(R.id.tvEmpty)

        if (allMemesAdapter.itemCount > 0) {
            tvEmpty.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun initMemeRecyclerView(layoutManagerType: LayoutManagerType) {
        with(rvMeme)
        {
            setLayoutManager(layoutManagerType)
            setHasFixedSize(true)
            adapter = allMemesAdapter
        }
    }

    private fun saveLayoutManager() {
        SettingsManager.saveCurrentLayoutManager(
            PREF_ALL_MEMES_LAYOUT_MANAGER_TYPE,
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
                it.updateToolBarIcons(listIcon, false)
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
            adapter = allMemesAdapter
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
        rvMeme.layoutManager = layoutManager
        rvMeme.scrollToPosition(scrollPosition)
    }

}
