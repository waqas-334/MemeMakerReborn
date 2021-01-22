package com.androidbull.meme.maker.ui.dialogs.rate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.androidbull.meme.maker.R

class RateAppOptionsFragment : Fragment() {

    private var onClickListener: View.OnClickListener? = null

    companion object {
        fun newInstance() = RateAppOptionsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rate_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActions(view)
    }

    private fun initActions(view: View) {

        view.findViewById<Button>(R.id.btnHappy).setOnClickListener {
            onClickListener?.onClick(it)
        }

        view.findViewById<Button>(R.id.btnConfused).setOnClickListener {
            onClickListener?.onClick(it)
        }

        view.findViewById<Button>(R.id.btnUnHappy).setOnClickListener {
            onClickListener?.onClick(it)
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            onClickListener?.onClick(it)
        }
    }


    fun setListener(onClickListener: View.OnClickListener) {
        this.onClickListener = onClickListener
    }

}