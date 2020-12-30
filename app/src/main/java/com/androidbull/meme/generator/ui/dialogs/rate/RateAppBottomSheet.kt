package com.androidbull.meme.generator.ui.dialogs.rate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.androidbull.meme.generator.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RateAppBottomSheet : BottomSheetDialogFragment(), View.OnClickListener {

    companion object {
        fun newInstance() = RateAppBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rate_app_container, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInitialFragment(savedInstanceState)

    }

    private fun setInitialFragment(savedInstanceState: Bundle?) {
        if (view?.findViewById<FrameLayout>(R.id.fragmentContainer) != null) {
            if (savedInstanceState != null) {
                return
            }
            childFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, RateAppOptionsFragment.newInstance())
                .commit()
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is RateAppOptionsFragment) {
            fragment.setListener(this)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnHappy -> {
                replaceFragment(RateHappyFragment.newInstance())
            }
            R.id.btnConfused -> {
                replaceFragment(RateConfusedFragment.newInstance())
            }
            R.id.btnUnHappy -> {
                replaceFragment(RateUnhappyFragment.newInstance())
            }
            R.id.btnCancel -> {
                dismiss()
            }
        }
    }
}