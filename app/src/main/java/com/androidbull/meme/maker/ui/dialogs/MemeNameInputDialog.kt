package com.androidbull.meme.maker.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.androidbull.meme.maker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MemeNameInputDialog : DialogFragment() {

    private var dialogView: View? = null
    private var btnSave: Button? = null
    private var btnCancel: Button? = null
    private var etMemeName: EditText? = null
    private var onSaveClickListener: ((memeName: String) -> Unit)? = null

    companion object {
        @JvmStatic
        fun newInstance() = MemeNameInputDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_meme_name_input, null)
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

        btnSave = dialogView?.findViewById(R.id.btnSave)
        btnCancel = dialogView?.findViewById(R.id.btnCancel)
        etMemeName = dialogView?.findViewById(R.id.etMemeName)

        btnSave?.setOnClickListener {
            etMemeName?.let {
                if (!TextUtils.isEmpty(it.text)) {
                    onSaveClickListener?.invoke(it.text.toString())
                    dismiss()
                } else {
                    it.error = getString(R.string.str_enter_valid_name)
                    it.requestFocus()
                }
            }
        }

        btnCancel?.setOnClickListener {
            dismiss()
        }
    }

    fun setOnSaveClickListener(onSaveClickListener: (memeName: String) -> Unit) {
        this.onSaveClickListener = onSaveClickListener
    }

}
