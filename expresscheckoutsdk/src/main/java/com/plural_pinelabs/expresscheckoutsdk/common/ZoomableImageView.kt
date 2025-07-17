package com.plural_pinelabs.expresscheckoutsdk.common

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min


class ZoomableImageView : AppCompatImageView {
    private val currentScale: Float
        get() {
            matrix.getValues(matrixValues)
            return matrixValues[Matrix.MSCALE_X]
        }
    private val matrix = Matrix()
    private val matrixValues = FloatArray(9)

    private var scale = 1f
    private var scaleDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null

    private val lastTouch = PointF()
    private var isDragging = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector!!.onTouchEvent(event)
        gestureDetector!!.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouch[event.x] = event.y
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> if (isDragging) {
                val dx = event.x - lastTouch.x
                val dy = event.y - lastTouch.y
                matrix.postTranslate(dx, dy)
                imageMatrix = matrix
                lastTouch[event.x] = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isDragging = false
        }

        return true
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            scale *= scaleFactor
            scale = max(0.5, min(scale.toDouble(), 3.0)).toFloat() // Clamp scale
            matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            imageMatrix = matrix
            return true
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val targetScale = if (currentScale < 2f) 2f else 1f
            val scaleFactor: Float = targetScale / currentScale
            matrix.postScale(scaleFactor, scaleFactor, e.x, e.y)
            imageMatrix = matrix
            scale = targetScale
            return true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitImageToView()
    }

    private fun fitImageToView() {
        val drawable = drawable ?: return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()

        val scaleX = viewWidth / imageWidth
        val scaleY = viewHeight / imageHeight
        val scale = min(scaleX.toDouble(), scaleY.toDouble()).toFloat()

        val dx = (viewWidth - imageWidth * scale) / 2
        val dy = (viewHeight - imageHeight * scale) / 2

        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        imageMatrix = matrix
    }


}
