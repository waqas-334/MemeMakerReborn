package com.androidbull.meme.maker.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.androidbull.meme.maker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MemeDownloadDialog : DialogFragment() {

    private var dialogView: View? = null

    companion object {
        @JvmStatic
        fun newInstance() = MemeDownloadDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_meme_download_progress, null)
        return MaterialAlertDialogBuilder(
            requireContext()
        )
            .setView(dialogView)
            .create()
    }

    // Need to return the view here or onViewCreated won't be called by DialogFragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return dialogView
    }


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

}
