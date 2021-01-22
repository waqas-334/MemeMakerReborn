package com.androidbull.meme.maker.helper

import android.content.Context
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.ui.SeekBarIndicator


private fun getFontSizeIndicatorList(context: Context): List<SeekBarIndicator> {
    context.getString(R.string.cd_todo)
    return listOf(
        SeekBarIndicator(12, context.getString(R.string.str_tiny)),
        SeekBarIndicator(14, context.getString(R.string.str_tiny_plus)),
        SeekBarIndicator(16, context.getString(R.string.str_small)),
        SeekBarIndicator(18, context.getString(R.string.str_small_plus)),
        SeekBarIndicator(24, context.getString(R.string.str_normal)),
        SeekBarIndicator(30, context.getString(R.string.str_normal_plus)),
        SeekBarIndicator(40, context.getString(R.string.str_large)),
        SeekBarIndicator(50, context.getString(R.string.str_large_plus)),
        SeekBarIndicator(60, context.getString(R.string.str_huge)),
    )
}


fun getFontSizeIndicatorTextList(context: Context): Array<String> {
    val fontSizeIndicatorTextList: MutableList<String> = mutableListOf()
    getFontSizeIndicatorList(context).forEach {
        fontSizeIndicatorTextList.add(it.text)
    }
    return fontSizeIndicatorTextList.toTypedArray()
}

fun getFontSizeFromSeekBar(tickIndex: Int, context: Context): Int {
    return if (tickIndex <= getFontSizeIndicatorList(context).size)
        getFontSizeIndicatorList(context)[(tickIndex - 1)].value
    else
        40 // Large
}

fun getTickPositionFromFontSize(fontSize: Int, context: Context): Float {
    val intArrayList: MutableList<Int> = mutableListOf()
    getFontSizeIndicatorList(context).forEachIndexed { index, seekBarIndicator ->
        intArrayList.add(seekBarIndicator.value)
    }
    return getClosestIndex(intArrayList.toIntArray(), fontSize).toFloat()
}

private fun getClosestIndex(values: IntArray, value: Int): Int {
    class Closest {
        var dif: Int? = null
        var index = -1
    }

    val closest = Closest()
    for (i in values.indices) {
        val dif = Math.abs(value - values[i])
        if (closest.dif == null || dif < closest.dif!!) {
            closest.index = i
            closest.dif = dif
        }
    }
    return (closest.index + 1)
}


private val outlineSizeIndicatorList =
    listOf(
        SeekBarIndicator(0, "Off"),
        SeekBarIndicator(1, "1"),
        SeekBarIndicator(2, "2"),
        SeekBarIndicator(3, "3"),
        SeekBarIndicator(4, "4")
    )

fun getOutlineSizeIndicatorTextList(): Array<String> {
    val outlineSizeIndicatorTextList: MutableList<String> = mutableListOf()
    outlineSizeIndicatorList.forEach {
        outlineSizeIndicatorTextList.add(it.text)
    }
    return outlineSizeIndicatorTextList.toTypedArray()
}

fun getOutlineSizeFromSeekBar(tickIndex: Int): Int {
    return if (tickIndex <= outlineSizeIndicatorList.size)
        outlineSizeIndicatorList[(tickIndex - 1)].value
    else
        1 // Off
}

fun getTickPositionFromOutlineSize(fontSize: Int): Float {
    outlineSizeIndicatorList.forEachIndexed { index, seekBarIndicator ->
        if (seekBarIndicator.value == fontSize) {
            return (index + 1).toFloat()
        }
    }
    return 1f // first position
}


private val maxLinesIndicatorList =
    listOf(
        SeekBarIndicator(Integer.MAX_VALUE, "Auto"),
        SeekBarIndicator(1, "1"),
        SeekBarIndicator(2, "2"),
        SeekBarIndicator(3, "3"),
        SeekBarIndicator(4, "4"),
        SeekBarIndicator(5, "5"),
        SeekBarIndicator(6, "6"),
        SeekBarIndicator(7, "7"),
        SeekBarIndicator(8, "8"),
        SeekBarIndicator(9, "9"),
        SeekBarIndicator(10, "10")
    )

fun getMaxLinesIndicatorTextList(): Array<String> {
    val maxLinesIndicatorTextList: MutableList<String> = mutableListOf()
    maxLinesIndicatorList.forEach {
        maxLinesIndicatorTextList.add(it.text)
    }
    return maxLinesIndicatorTextList.toTypedArray()
}

fun getMaxLinesFromSeekBar(tickIndex: Int): Int {
    return if (tickIndex <= maxLinesIndicatorList.size)
        maxLinesIndicatorList[(tickIndex - 1)].value
    else
        2 // Off
}

fun getTickPositionFromMaxLines(fontSize: Int): Float {
    maxLinesIndicatorList.forEachIndexed { index, seekBarIndicator ->
        if (seekBarIndicator.value == fontSize) {
            return (index + 1).toFloat()
        }
    }
    return 1f // first position
}
