package com.androidbull.meme.generator.ui.dialogs.rate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.helper.SettingsManager
import com.androidbull.meme.generator.helper.sendEmail

class RateUnhappyFragment : Fragment() {

    companion object {
        fun newInstance() = RateUnhappyFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rate_un_happy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.tvContactTeam).setOnClickListener {
            SettingsManager.saveIsAppRated(true)
            contactUs()
        }
    }

    private fun contactUs() {
        context?.let {
            val subject = getString(R.string.str_meme_maker_contact)
            sendEmail(it, subject, "")
        }
    }
}