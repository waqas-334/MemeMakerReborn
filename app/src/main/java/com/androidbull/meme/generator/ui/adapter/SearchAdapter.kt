package com.androidbull.meme.generator.ui.adapter

import android.animation.Animator
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.helper.*
import com.androidbull.meme.generator.model.Meme2
import com.androidbull.meme.generator.ui.interfaces.OnMemeItemClickListener
import com.bumptech.glide.Glide
import xyz.hanks.library.bang.SmallBangView
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class SearchAdapter(
    private var memes: MutableList<Meme2>,
    private var onMemeItemClickListener: OnMemeItemClickListener
) :
    RecyclerView.Adapter<SearchAdapter.MemeViewHolder>(), Filterable {

    private lateinit var memesListFull: MutableList<Meme2>

    fun updateAdapter(memes: MutableList<Meme2>) {
        this.memes = memes
        this.memesListFull = ArrayList(memes)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeViewHolder {
        return MemeViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_meme_linear, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MemeViewHolder, position: Int) {
        holder.bind(memes[position], position)
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
                if (position != RecyclerView.NO_POSITION) {
                    onMemeItemClickListener.onMemeClicked(memes[position], position)
                }
            }

            cibAddToFavourites.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
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
                if (position != RecyclerView.NO_POSITION) {
                    val isFavourite = !sbvFavourite.isSelected
                    sbvFavourite.isSelected = isFavourite
                    if (isFavourite) {

                        sbvFavourite.likeAnimation(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                if (position != RecyclerView.NO_POSITION) {
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

        fun bind(meme: Meme2, position: Int) {
            tvMemeName.text = meme.imageTitle

            cibAddToFavourites.isChecked = meme.isFavourite
            sbvFavourite.isSelected = meme.isFavourite


            val urlSplits = meme.imageName.split(".jpg").toTypedArray()

            if (meme.isCreatedByUser) {
                cibAddToFavourites.visibility = View.GONE
                sbvFavourite.visibility = View.GONE

                if (StorageHelper.isExternalStorageReadable()) {
                    val bitmap: Bitmap? =
                        BitmapFactory.decodeFile(StorageHelper.getTemplatesPrivateDir() + meme.imageName)
                    if (bitmap != null) {
                        ivMeme.setImageBitmap(bitmap)
                    }
                }
            } else if (meme.id < TOTAL_DEFAULT_MEMES) { // default meme

                val assetManager: AssetManager = itemView.context.assets
                try {
                    assetManager.open("memes/${urlSplits[0]}_s.jpg").use {
                        val bitmap: Bitmap = BitmapFactory.decodeStream(it)
                        ivMeme.setImageBitmap(bitmap)
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }

            } else {    // new Meme
                Glide.with(itemView.context)
                    .load(MEME_SERVER_BASE_URL + meme.imageName)
                    .centerCrop()
                    .into(ivMeme)
            }
        }
    }

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<Meme2> = ArrayList()
            if (constraint.isEmpty()) {
                filteredList.addAll(memesListFull)
            } else {

                val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim()
                SearchHelper.getScoredSearchResult(memesListFull, filterPattern).forEach {
                    filteredList.add(it)
                }

            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            results.values?.let {
                memes.clear()
                memes = (results.values) as MutableList<Meme2>
                notifyDataSetChanged()
            }
        }
    }
}