package com.androidbull.meme.maker.helper

import com.androidbull.meme.maker.model.Meme2
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.util.*

object SearchHelper {

    private fun removeNonSearchableCharacters(s: String) = s.replace("'", "").replace(",", "")


    fun getScoredSearchResult(memes: List<Meme2>, searchQuery: String): List<Meme2> {
        val searchString = removeNonSearchableCharacters(searchQuery)

        val memeSearchWithWeightList = ArrayList<MemeSearchWithWeight>()

        memes.forEach {
            memeSearchWithWeightList.add(MemeSearchWithWeight(it))
        }

        val searchResultMemeBySearchWeight: List<MemeSearchWithWeight> =
            getSearchResultMemeSearchWeight(memeSearchWithWeightList, searchString)

        val resultList = mutableListOf<Meme2>()

        for (x in 0..20) {
            resultList.add(searchResultMemeBySearchWeight.get(x).meme)
        }

        return resultList

    }

    private fun getSearchResultMemeSearchWeight(
        memeSearchWithWeightList: List<MemeSearchWithWeight>,
        searchString: String
    ): List<MemeSearchWithWeight> {
        val resultList = mutableListOf<MemeSearchWithWeight>()

        val list = mutableListOf<String>()
        /* memeSearchWithWeightList.forEach {
             val end = it.meme.imageName.lastIndexOf('.')
             val memeName = it.meme.imageName.substring(0, end).replace('_', ' ').toLowerCase()

             list.add(memeName)
         }

         FuzzySearch.extractTop(searchString, list, 10).forEach {
             resultList.add(memeSearchWithWeightList[it.index])
         }
 */

        memeSearchWithWeightList.forEach { memeSearchWithWeight ->

            memeSearchWithWeight.incrementWeightForNameMatch(
                FuzzySearch.ratio(
                    memeSearchWithWeight.meme.imageTitle.toLowerCase(Locale.ROOT).trim(),
                    searchString
                )
            )
            resultList.add(memeSearchWithWeight)

        }

        return resultList.sorted()

    }
}