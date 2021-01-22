package com.androidbull.meme.maker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.data.repository.RoomMemeRepository
import com.androidbull.meme.maker.model.Meme2
import com.androidbull.meme.maker.ui.activities.MainActivity
import com.androidbull.meme.maker.ui.activities.MemeGeneratorActivity
import com.androidbull.meme.maker.ui.adapter.SearchAdapter
import com.androidbull.meme.maker.ui.interfaces.OnMemeItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchFragment : Fragment(), OnMemeItemClickListener {

    private lateinit var rvSearch: RecyclerView

    private val memes = mutableListOf<Meme2>()
    private val memeRepository = RoomMemeRepository()
    val searchAdapter = SearchAdapter(mutableListOf(), this)

    override fun onResume() {
        super.onResume()
        getAllMemes()
    }

    private fun getAllMemes() {
        activity?.let {
            if (it is MainActivity) {
                it.memes.value?.let { memesList ->
                    onMemesFetched(memesList)
                }
            }
        }
    }

    private fun onMemesFetched(memesList: List<Meme2>) {
        memes.clear()
        memes.addAll(memesList)
        searchAdapter.updateAdapter(memes)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvSearch = view.findViewById(R.id.rvSearch)
        rvSearch.layoutManager = LinearLayoutManager(requireContext())
        rvSearch.setHasFixedSize(true)
        rvSearch.adapter = searchAdapter

    }


    override fun onMemeClicked(meme: Meme2, position: Int) {
        activity?.let {
            val intent = Intent(it, MemeGeneratorActivity::class.java)
            intent.putExtra("memeId", meme.id)
            startActivity(intent)
        }
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

}