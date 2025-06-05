package com.plural_pinelabs.expresscheckoutsdk.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * A custom View that displays a blurred version of its underlying content.
 *
 * This view should be placed as a sibling to the content you want to blur,
 * and positioned above it in the Z-order.
 *
 * For Android 12 (API 31) and above, it's recommended to use RenderEffect for better performance.
 */
class BlurringView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var targetViewWeakRef: WeakReference<View>? = null
    private var blurredBitmap: Bitmap? = null
    private val matrix = Matrix()

    // Configuration for blur
    var blurRadius = 25 // Adjust this for desired blur strength
        set(value) {
            field = value.coerceIn(0, 25) // Max radius for reasonable performance
            invalidate() // Redraw when radius changes
        }

    var downsampleFactor = 8 // Adjust this for performance vs. quality (e.g., 4, 8, 16)
        set(value) {
            field = value.coerceAtLeast(1)
            invalidate() // Redraw when downsample factor changes
        }

    private val blurScope = CoroutineScope(Dispatchers.Default) // For bitmap blurring

    // Listener to re-capture when target view changes layout
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        updateBlurredBitmap()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Attach layout listener when view is attached
        targetViewWeakRef?.get()?.viewTreeObserver?.addOnGlobalLayoutListener(layoutListener)
        updateBlurredBitmap() // Initial blur capture
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Remove layout listener when view is detached
        targetViewWeakRef?.get()?.viewTreeObserver?.removeOnGlobalLayoutListener(layoutListener)
        blurredBitmap?.recycle()
        blurredBitmap = null
    }

    /**
     * Sets the view whose content should be blurred.
     * This view should typically be a sibling to the BlurringView,
     * positioned behind it in the layout hierarchy.
     */
    fun setTargetView(view: View) {
        // Remove previous listener if target view changes
        targetViewWeakRef?.get()?.viewTreeObserver?.removeOnGlobalLayoutListener(layoutListener)

        targetViewWeakRef = WeakReference(view)

        // Add new listener for the new target view
        view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

        // Trigger immediate update
        updateBlurredBitmap()
    }

    /**
     * Updates the blurred bitmap by capturing the target view and applying the blur.
     * This should be called whenever the target view's content changes or its size changes.
     */
    fun updateBlurredBitmap() {
        val targetView = targetViewWeakRef?.get() ?: return

        if (targetView.width == 0 || targetView.height == 0) {
            // View not yet laid out
            return
        }

        // We run the blur on a background thread
        blurScope.launch {
            val bitmap = captureViewAsBitmap(targetView, downsampleFactor)
            val blurred = bitmap?.let {
                StackBlurManager.process(it, blurRadius.toFloat()) // Use the StackBlur algorithm
            }

            withContext(Dispatchers.Main) {
                // Recycle old bitmap if it exists
                blurredBitmap?.recycle()
                blurredBitmap = blurred
                invalidate() // Request redraw on the UI thread
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        blurredBitmap?.let {
            matrix.reset()
            // Scale the blurred bitmap to fill this view
            val scaleX = width.toFloat() / it.width
            val scaleY = height.toFloat() / it.height
            matrix.postScale(scaleX, scaleY)
            canvas.drawBitmap(it, matrix, null)
        }
    }

    /**
     * Captures a view's content into a bitmap.
     * This is an expensive operation and should ideally be done on a background thread.
     *
     * @param view The view to capture.
     * @param downsampleFactor Factor to reduce the bitmap resolution for performance.
     * e.g., 2 means half width/height, 4 means quarter width/height.
     * @return The captured Bitmap, or null if capture fails.
     */
    @WorkerThread
    private fun captureViewAsBitmap(view: View, downsampleFactor: Int): Bitmap? {
        if (view.width == 0 || view.height == 0) {
            return null
        }

        val originalWidth = view.width
        val originalHeight = view.height

        val scaledWidth = originalWidth / downsampleFactor
        val scaledHeight = originalHeight / downsampleFactor

        if (scaledWidth == 0 || scaledHeight == 0) {
            return null // Prevent creating 0-dimension bitmap
        }

        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Scale the canvas to fit the downsampled bitmap
        val scale = 1f / downsampleFactor
        canvas.scale(scale, scale)

        // Draw the original view onto the scaled canvas
        view.draw(canvas)
        return bitmap
    }
}