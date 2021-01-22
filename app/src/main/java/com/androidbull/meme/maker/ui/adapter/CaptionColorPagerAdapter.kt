package com.androidbull.meme.maker.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.androidbull.meme.maker.ui.fragments.BaseCaptionColorFragment

class CaptionColorPagerAdapter(
    fragment: Fragment,
    private val captionColorFragments: List<BaseCaptionColorFragment>,
) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return captionColorFragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return captionColorFragments[position]
    }

}