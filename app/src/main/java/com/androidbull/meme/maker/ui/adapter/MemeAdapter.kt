package com.androidbull.meme.maker.ui.adapter

import android.animation.Animator
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.helper.*
import com.androidbull.meme.maker.model.Meme2
import com.androidbull.meme.maker.ui.interfaces.OnMemeItemClickListener
import com.bumptech.glide.Glide
import xyz.hanks.library.bang.SmallBangView
import java.io.IOException


class MemeAdapter(
    private var memes: List<Meme2>,
    private var onMemeItemClickListener: OnMemeItemClickListener
) :
    RecyclerView.Adapter<MemeAdapter.MemeViewHolder>() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    val assetManager: AssetManager = AppContext.getInstance().context.assets


    fun updateAdapter(memes: List<Meme2>) {
        this.memes = memes
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        layoutManager?.let {
            if (it is GridLayoutManager) {
                return R.layout.item_meme_grid
            } else if (it is LinearLayoutManager) {
                return R.layout.item_meme_linear
            }
        }
        return R.layout.item_meme_grid
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

    inner class MemeViewHolder(memeView: View) : RecyclerView.ViewHolder(memeView) {

        private val tvMemeName = memeView.findViewById<TextView>(R.id.tvMemeName)
        private val ivMeme = memeView.findViewById<ImageView>(R.id.ivMeme)
        private val cibAddToFavourites =
            memeView.findViewById<CheckableImageButton>(R.id.cibAddToFavourites)
        private val sbvFavourite = memeView.findViewById<SmallBangView>(R.id.sbvFavourite)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (bindingAdapterPosition != NO_POSITION) {
                    onMemeItemClickListener.onMemeClicked(memes[position], position)
                }
            }

            cibAddToFavourites.setOnClickListener {
                val position = bindingAdapterPosition
                if (bindingAdapterPosition != NO_POSITION) {
                    val isFavourite = !cibAddToFavourites.isSelected
                    cibAddToFavourites.isSelected = isFavourite
                    onMemeItemClickListener.onAddToFavouritesClicked(
                        memes[position],
                        position,
                        isFavourite
                    )
                }
            }

            sbvFavourite.setOnClickListener {
                val position = bindingAdapterPosition
                if (bindingAdapterPosition != NO_POSITION) {
                    val isFavourite = !sbvFavourite.isSelected
                    sbvFavourite.isSelected = isFavourite
                    if (isFavourite) {

                        sbvFavourite.likeAnimation(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                if (bindingAdapterPosition != NO_POSITION) {
                                    onMemeItemClickListener.onAddToFavouritesClicked(
                                        memes[position],
                                        position,
                                        isFavourite
                                    )
                                }
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                            }
                        })


                    } else {
                        onMemeItemClickListener.onAddToFavouritesClicked(
                            memes[position],
                            position,
                            isFavourite
                        )
                    }
                }
            }

        }

        fun bind(meme: Meme2) {

            tvMemeName.text = meme.imageName
            cibAddToFavourites.isChecked = meme.isFavourite
            sbvFavourite.isSelected = meme.isFavourite

            if (meme.id < TOTAL_DEFAULT_MEMES) // default meme
            {
                val urlSplits = meme.imageName.split(".jpg").toTypedArray()
                val fileName = urlSplits[0] + "_s.jpg"  // append '_s' at the end for thumbnails

                try {
                    assetManager.open(MEMES_ASSET_FOLDER_NAME + fileName).use { it ->
                        BitmapFactory.decodeStream(it)?.let {
                            ivMeme.setImageBitmap(it)
                        }
                    }
                } catch (ex: IOException) {
                }
            } else {    // new Meme
                Glide.with(itemView.context)
                    .load(MEME_SERVER_BASE_URL + meme.imageName)
                    .centerCrop()
                    .into(ivMeme)
            }
        }
    }
}