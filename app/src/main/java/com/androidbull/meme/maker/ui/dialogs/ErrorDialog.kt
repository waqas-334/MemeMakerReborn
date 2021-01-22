package com.androidbull.meme.maker.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.androidbull.meme.maker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val ARG_PARAM1 = "error_message"

class ErrorDialog : DialogFragment() {

    private var dialogView: View? = null
    private var errorMessage: String? = null

    companion object {
        @JvmStatic
        fun newInstance(errorMessage: String) =
            ErrorDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, errorMessage)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            errorMessage = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_error, null)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        errorMessage?.let {
            view.findViewById<TextView>(R.id.tvTitle).text = it
        }

        view.findViewById<Button>(R.id.btnLeave).setOnClickListener {
            activity?.finish()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

}
