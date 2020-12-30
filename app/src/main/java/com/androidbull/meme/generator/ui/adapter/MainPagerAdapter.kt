package com.androidbull.meme.generator.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(
    fragment: Fragment,
    private val fragments: List<Fragment>
) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int) = fragments[position]


}