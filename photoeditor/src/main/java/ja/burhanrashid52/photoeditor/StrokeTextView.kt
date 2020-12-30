package ja.burhanrashid52.photoeditor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView


fun Float.toPx(context: Context) = (this * context.resources.displayMetrics.scaledDensity + 0.5F)


class StrokeTextView : AppCompatTextView {

    private val defaultStrokeWidth = 0F
    private var isDrawing: Boolean = false

    private var strokeColor: Int = 0
    private var strokeWidth: Float = 0.toFloat()

    constructor(context: Context) : super(context) {
        initResources(context, null)

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initResources(context, attrs)

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initResources(context, attrs)

    }

    private fun initResources(context: Context?, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context?.obtainStyledAttributes(attrs, R.styleable.StrokeTextView)
            strokeColor = a!!.getColor(R.styleable.StrokeTextView_textStrokeColor,
                currentTextColor)
            strokeWidth = a.getFloat(R.styleable.StrokeTextView_textStrokeWidth,
                defaultStrokeWidth)

            a.recycle()
        } else {
            strokeColor = currentTextColor
            strokeWidth = defaultStrokeWidth
        }
        setStrokeWidth(strokeWidth)
    }

    fun setStrokeColor(color: Int) {
        strokeColor = color
    }

    /**
     *  give value in sp
     */
    fun setStrokeWidth(width: Float) {
        strokeWidth = width.toPx(context)
    }

    fun setStrokeWidth(unit: Int, width: Float) {
        strokeWidth = TypedValue.applyDimension(
            unit, width, context.resources.displayMetrics)
    }

    override fun invalidate() {
        if (isDrawing) return
        super.invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        if (strokeWidth > 0) {
            isDrawing = true
            val p = paint
            p.style = Paint.Style.FILL

            super.onDraw(canvas)

            val currentTextColor = currentTextColor
            p.style = Paint.Style.STROKE
            p.strokeWidth = strokeWidth
            setTextColor(strokeColor)
            super.onDraw(canvas)
            setTextColor(currentTextColor)
            isDrawing = false
        } else {
            super.onDraw(canvas)
        }
    }

}