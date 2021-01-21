package com.androidbull.meme.generator.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.helper.CheckableImageButton
import com.androidbull.meme.generator.model.Meme2
import com.androidbull.meme.generator.ui.interfaces.OnSavedMemeItemClickListener
import com.bumptech.glide.Glide

class SavedMemeAdapter(
    private var memes: List<Meme2>,
    private var onSavedMemeItemClickListener: OnSavedMemeItemClickListener
) :
    RecyclerView.Adapter<SavedMemeAdapter.MemeViewHolder>() {

    private var showSelectionUi = false

    private var layoutManager: RecyclerView.LayoutManager? = null

    fun updateAdapter(memes: List<Meme2>) {
        this.memes = memes
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        layoutManager?.let {
            if (it is GridLayoutManager) {
                return R.layout.item_saved_meme_grid
            } else if (it is LinearLayoutManager) {
                return R.layout.item_saved_meme_linear
            }
        }
        return R.layout.item_saved_meme_grid
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeViewHolder {
        return MemeViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
        )
    }


    override fun onBindViewHolder(holder: MemeViewHolder, position: Int) {
        holder.bind(memes[position])
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.layoutManager = recyclerView.layoutManager
    }

    override fun getItemCount() = memes.size

    fun updateSelectionUi(showSelectionUi: Boolean) {
        this.showSelectionUi = showSelectionUi
        notifyDataSetChanged()
    }

    inner class MemeViewHolder(memeView: View) : RecyclerView.ViewHolder(memeView) {

        private val tvMemeName = memeView.findViewById<TextView>(R.id.tvMemeName)
        private val ivMeme = memeView.findViewById<ImageView>(R.id.ivMeme)
        private val cibSelectMeme =
            memeView.findViewById<CheckableImageButton>(R.id.cibSelectMeme)

        init {

            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (showSelectionUi) {
                        cibSelectMeme.isChecked = !cibSelectMeme.isChecked  // isFavourite is acting as isSelected
                        onSavedMemeItemClickListener.onMemeSelected(
                            memes[position], position, cibSelectMeme.isChecked
                        )
                    } else {
                        onSavedMemeItemClickListener.onMemeClicked(memes[position], position)
                    }
                }
            }

            cibSelectMeme.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSavedMemeItemClickListener.onMemeSelected(
                        memes[position], position, cibSelectMeme.isChecked
                    )
                }
            }
        }


        fun bind(meme: Meme2) {

            tvMemeName.text = meme.imageTitle

            if (showSelectionUi) {
                cibSelectMeme.visibility = View.VISIBLE
                cibSelectMeme.isChecked = meme.isFavourite  // isFavourite is acting as isSelected
            } else {
                cibSelectMeme.visibility = View.GONE
                cibSelectMeme.isChecked = false
            }
            Glide.with(itemView.context)
                .load(meme.imageName)
                .centerCrop()
                .into(ivMeme)
        }
    }
}