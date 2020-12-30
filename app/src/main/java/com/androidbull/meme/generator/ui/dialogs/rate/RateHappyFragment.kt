package com.androidbull.meme.generator.ui.dialogs.rate

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.helper.SettingsManager
import com.androidbull.meme.generator.helper.openAppInPlayStore
import com.androidbull.meme.generator.helper.sendEmail
import java.util.*

class RateHappyFragment : Fragment() {

    companion object {
        fun newInstance() = RateHappyFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rate_happy, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActions(view)
    }

    private fun initActions(view: View) {
        view.findViewById<Button>(R.id.btnReview).setOnClickListener {
            SettingsManager.saveIsAppRated(true)
            writeReview()
        }

        view.findViewById<Button>(R.id.btnContactTeam).setOnClickListener {
            SettingsManager.saveIsAppRated(true)
            contactUs()
        }

        view.findViewById<Button>(R.id.btnShareOnTwitter).setOnClickListener {
            initShareIntent("twitter")
        }

        view.findViewById<Button>(R.id.btnShareOnFacebook).setOnClickListener {
            initShareIntent("facebook")
        }
    }

    private fun writeReview() {
        context?.let {
            openAppInPlayStore(it.packageName)
        }
    }

    private fun contactUs() {
        context?.let {
            val subject = getString(R.string.str_meme_maker_contact)
            sendEmail(it, subject, "")
        }
    }

    private fun initShareIntent(type: String) {
        context?.let {
            var found = false
            var share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            val resInfo: List<ResolveInfo> =
                it.packageManager.queryIntentActivities(share, 0)
            if (resInfo.isNotEmpty()) {
                for (info in resInfo) {
                    if (info.activityInfo.packageName.toLowerCase(Locale.ROOT).contains(type) ||
                        info.activityInfo.name.toLowerCase(Locale.ROOT).contains(type)
                    ) {
                        share.putExtra(
                            Intent.EXTRA_SUBJECT,
                            getString(R.string.app_name) + " " + getString(R.string.str_is_amazing)
                        )
                        share.putExtra(
                            Intent.EXTRA_TEXT,
                            getString(R.string.share_app_message) + "https://play.google.com/store/apps/details?id=" + it.packageName
                        )
                        //          share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(myPath)) ); // Optional, just if you wanna share an image.
                        share.setPackage(info.activityInfo.packageName)
                        found = true
                        break
                    }
                }
                if (!found) {
                    if (type == "twitter")
                        share = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"))
                    else if (type == "facebook")
                        share = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/"))

                }
                startActivity(Intent.createChooser(share, "Select"))
            }
        }
    }
}